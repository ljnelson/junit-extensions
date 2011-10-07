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

import org.junit.rules.TestRule;

import org.junit.runner.Description;

import org.junit.runners.model.Statement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class H2Rule implements TestRule {

  private final boolean initialShutdownValue;

  private transient boolean shutdown;

  private transient Connection connectionProxy;

  private transient Connection connection;

  public H2Rule() {
    this(true);
  }

  public H2Rule(final boolean shutdown) {
    super();
    this.setShutdown(shutdown);
    this.initialShutdownValue = this.getShutdown();
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    this.setShutdown(this.initialShutdownValue);
    Statement returnValue = base;
    if (base != null) {
      final Statement newStatement = new Statement() {
          @Override
          public final void evaluate() throws Throwable {
            if (connectionProxy == null || connectionProxy.isClosed()) {
              createConnectionProxy();
            }
            validateConnectionProxy();
            init();
            try {
              base.evaluate();
            } finally {
              try {
                cleanup();
                if (getShutdown()) {
                  shutdown();
                }
              } finally {
                closeConnection();
              }
            }
          }
        };
      returnValue = newStatement;
    }
    return returnValue;
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

  protected Connection createConnection() throws SQLException {
    final Connection connection = DriverManager.getConnection(this.getURL(), this.getUsername(), this.getPassword());
    return connection;
  }

  private final void createConnectionProxy() throws SQLException {
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

  protected String getURL() {
    String url = null;
    String catalog = System.getProperty("testDatabaseCatalog", "test").trim();
    if (catalog.isEmpty()) {
      catalog = "test";
    }
    String initCommand = System.getProperty("testDatabaseInitCommand");
    if (initCommand == null) {
      final String schema = System.getProperty("testDatabaseSchema", "").trim();
      if (!schema.isEmpty()) {
        initCommand = String.format("CREATE SCHEMA %s", schema);
      }
    }
    if (initCommand == null) {
      url = String.format("jdbc:h2:mem:%s", catalog);
    } else {
      url = String.format("jdbc:h2:mem:%s;INIT=%s", catalog, initCommand);
    }
    return url;
  }

  protected String getUsername() {
    return System.getProperty("testDatabaseUsername", "sa");
  }

  protected String getPassword() {
    return System.getProperty("testDatabasePassword", "");
  }

  public final Connection getConnection() {
    return this.connectionProxy;
  }

  public boolean getShutdown() {
    return this.shutdown;
  }

  public void setShutdown(final boolean shutdown) {
    this.shutdown = shutdown;
  }

  private final void shutdown() throws SQLException {
    if (this.connection != null) {
      final java.sql.Statement s = this.connection.createStatement();
      assertNotNull(s);
      s.executeUpdate("SHUTDOWN IMMEDIATELY");
      s.close();
    }
  }

  public void init() throws SQLException {

  }

  public void cleanup() throws Exception {

  }  

}