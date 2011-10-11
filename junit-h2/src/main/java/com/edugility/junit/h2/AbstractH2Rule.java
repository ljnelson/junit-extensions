/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
 *
 * Copyright (c) 2010-2011 Edugility LLC.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * The original copy of this license is available at
 * http://www.opensource.org/license/mit-license.html.
 */
package com.edugility.junit.h2;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.edugility.junit.db.AbstractDBRule;

import org.junit.rules.TestRule;

import org.junit.runner.Description;

import org.junit.runners.model.Statement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractH2Rule extends AbstractDBRule {

  private final boolean initialShutdownValue;

  private transient boolean shutdown;

  private transient Connection connectionProxy;

  private transient Connection connection;

  protected AbstractH2Rule(final String catalog, final String schema, final String username, final String password, final boolean shutdown) {
    super(catalog, schema, username, password);
    this.setShutdown(shutdown);
    this.initialShutdownValue = this.getShutdown();
  }

  @Override
  public void create() throws Exception {
    super.create();
    this.setShutdown(this.initialShutdownValue);
    if (this.connectionProxy == null || this.connectionProxy.isClosed()) {
      this.createConnectionProxy();
    }
    this.validateConnectionProxy();
  }

  @Override
  public void destroy() throws Exception {
    try {
      if (this.getShutdown()) {
        if (this.connection != null) {
          final java.sql.Statement s = this.connection.createStatement();
          assertNotNull(s);
          s.executeUpdate("SHUTDOWN IMMEDIATELY");
          s.close();
        }
      }
    } finally {
      this.closeConnection();
      super.destroy();
    }
  }

  private final void closeConnection() {
    if (this.connection != null) {
      try {
        this.connection.close();
      } catch (final SQLException ignore) {
      }
    }
  }

  private final void validateConnectionProxy() throws SQLException {
    validateConnectionProxy(this.connectionProxy);
  }

  private static final void validateConnectionProxy(final Connection connection) throws SQLException {
    validateConnection(connection);
    assertTrue(Proxy.isProxyClass(connection.getClass()));
  }

  private static final void validateConnection(final Connection connection) throws SQLException {
    assertNotNull(connection);
    final DatabaseMetaData dmd = connection.getMetaData();
    assertNotNull(dmd);
    final String url = dmd.getURL();
    assertNotNull(url);
    assertTrue(url.startsWith("jdbc:h2:mem:"));
  }

  private final void createConnectionProxy() throws Exception {
    this.connection = this.createConnection();
    validateConnection(this.connection);
    final InvocationHandler handler = new InvocationHandler() {
        @Override
        public final Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
          if (proxy != null && method != null && !"close".equals(method.getName())) {
            return method.invoke(connection);
          } else {
            return null; // close() has a void return type
          }
        }
      };
    final Connection proxy = (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[] { Connection.class }, handler);
    assertNotNull(proxy);
    this.connectionProxy = proxy;
  }

  protected abstract Connection createConnection() throws Exception;

  @Override
  public final Connection getConnection() {
    return this.connectionProxy;
  }

  private boolean getShutdown() {
    return this.shutdown;
  }

  private void setShutdown(final boolean shutdown) {
    this.shutdown = shutdown;
  }

}