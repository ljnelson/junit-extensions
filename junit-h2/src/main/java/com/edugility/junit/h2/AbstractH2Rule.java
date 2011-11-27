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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.edugility.junit.db.AbstractDBRule;
import com.edugility.junit.db.ConnectionDescriptor;

import com.edugility.throwables.ThrowableChain;

import org.junit.rules.TestRule;

import org.junit.runner.Description;

import org.junit.runners.model.Statement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractH2Rule extends AbstractDBRule {

  private final boolean initialShutdownValue;

  private transient boolean shutdown;

  protected AbstractH2Rule(final String catalog, final String schema, final String username, final String password, final boolean shutdown) {
    super(catalog, schema, username, password);    
    this.setShutdown(shutdown);
    this.initialShutdownValue = this.getShutdown();
  }

  protected AbstractH2Rule(final ConnectionDescriptor descriptor, final boolean shutdown) {
    this(descriptor == null ? null : descriptor.getCatalog(),
         descriptor == null ? null : descriptor.getSchema(),
         descriptor == null ? null : descriptor.getUsername(),
         descriptor == null ? null : descriptor.getPassword(),
         shutdown);
  }

  @Override
  public String getUsername() {
    String username = super.getUsername();
    if (username == null) {
      username = System.getProperty("testDatabaseUserName");
    }
    return username;
  }

  @Override
  public String getPassword() {
    String password = super.getPassword();
    if (password == null) {
      password = System.getProperty("testDatabasePassword");
    }
    return password;
  }

  @Override
  public void create() throws Exception {
    super.create();
    this.setShutdown(this.initialShutdownValue);
    final Connection connection = this.getConnection();
    validateConnection(connection);
  }

  @Override
  public void destroy() throws Exception {
    Connection connection = null;
    ThrowableChain chain = new ThrowableChain();
    try {
      if (this.getShutdown()) {
        connection = this.getConnection();
        if (connection != null) {
          final java.sql.Statement s = connection.createStatement();
          assertNotNull(s);
          s.executeUpdate("SHUTDOWN");
          s.close();
        }
      }
    } catch (final Exception everything) {
      chain.add(everything);
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (final Exception everything) {
        chain.add(everything);
      } finally {
        try {
          super.destroy();
        } catch (final Exception everything) {
          chain.add(everything);
        } finally {
          if (chain.size() > 1) {
            throw chain;
          }
        }
      }
    }
  }

  public static final void validateConnection(final Connection connection) throws SQLException {
    assertNotNull(connection);
    final DatabaseMetaData dmd = connection.getMetaData();
    assertNotNull(dmd);
    final String url = dmd.getURL();
    assertNotNull(url);
    assertTrue(url.startsWith("jdbc:h2:mem:"));
  }

  private boolean getShutdown() {
    return this.shutdown;
  }

  private void setShutdown(final boolean shutdown) {
    this.shutdown = shutdown;
  }

}