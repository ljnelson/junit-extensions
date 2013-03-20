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

import com.edugility.junit.db.DBRule;
import com.edugility.junit.db.ConnectionDescriptor;

import com.edugility.throwables.ThrowableChain;

import org.junit.rules.TestRule;

import org.junit.runner.Description;

import org.junit.runners.model.Statement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class H2Rule extends DBRule {

  private final boolean initialShutdownValue;

  private final boolean inMemory;

  private ConnectionDescriptor cd;

  private transient Connection connection;

  private transient boolean shutdown;

  public H2Rule(final String catalog, final String username, final String password, final boolean shutdown) {
    this(buildConnectionDescriptor(true, -1, catalog, null, false, username, password), shutdown);
  }

  public H2Rule(final String catalog, final String schema, final String username, final String password, final boolean shutdown) {
    this(buildConnectionDescriptor(true, -1, catalog, schema, false, username, password), shutdown);
  }

  public H2Rule(final boolean inMemory, final int dbCloseDelay, final String catalog, final String schema, final boolean setSchemaAsDefault, final String username, final String password, final boolean shutdown) {
    this(buildConnectionDescriptor(inMemory, dbCloseDelay, catalog, schema, setSchemaAsDefault, username, password), shutdown);
  }

  private static final ConnectionDescriptor buildConnectionDescriptor(final boolean inMemory, final int dbCloseDelay, final String catalog, final String schema, final boolean setSchemaAsDefault, final String username, final String password) {
    final ConnectionDescriptor cd = new ConnectionDescriptor();
    final StringBuilder url = new StringBuilder("jdbc:h2:");
    if (inMemory) {
      url.append("mem:");
    }
    if (catalog != null && !catalog.isEmpty()) {
      cd.setProperty(ConnectionDescriptor.CATALOG, catalog);
      url.append(catalog);
    }
    url.append(";DB_CLOSE_DELAY=");
    url.append(dbCloseDelay);
    if (schema != null && !schema.isEmpty()) {
      cd.setProperty(ConnectionDescriptor.SCHEMA, schema);
      url.append(";INIT=CREATE SCHEMA IF NOT EXISTS ");
      url.append(schema);
      if (setSchemaAsDefault) {
        url.append("\\;SET SCHEMA ");
        url.append(schema);
        url.append("\\;");
      }
    }
    cd.setProperty(ConnectionDescriptor.CONNECTION_URL, url.toString());
    if (username != null) {
      cd.setProperty(ConnectionDescriptor.USERNAME, username);
    }
    if (password != null) {
      cd.setProperty(ConnectionDescriptor.PASSWORD, password);
    }
    return cd;
  }

  public H2Rule(final ConnectionDescriptor descriptor) {
    this(descriptor, false);
  }

  public H2Rule(final ConnectionDescriptor descriptor, final boolean shutdown) {
    super(descriptor);
    this.cd = descriptor;
    this.initialShutdownValue = shutdown;
    if (descriptor != null) {
      final String url = descriptor.getConnectionURL();
      this.inMemory = url == null || url.startsWith("jdbc:h2:mem:");
    } else {
      this.inMemory = true;
    }
    this.setShutdown(shutdown);
  }

  @Override
  public void create() throws Exception {
    super.create();
    this.setShutdown(this.initialShutdownValue);
  }

  @Override
  public void connect() throws Exception {
    super.connect();
    if (this.cd != null) {
      final Connection connection = this.cd.getConnection();
      this.validateConnection(connection);
      this.connection = connection;
    } else {
      this.connection = null;
    }
  }

  public Connection getAllocatedConnection() {
    return this.connection;
  }

  @Override
  public void destroy() throws Exception {
    if (this.getShutdown()) {
      Connection connection = this.getAllocatedConnection();
      if (connection == null || connection.isClosed()) {
        this.connect();
        connection = this.getAllocatedConnection();
      }
      if (connection != null) {
        final java.sql.Statement s = connection.createStatement();
        assertNotNull(s);
        s.executeUpdate("SHUTDOWN");
        s.close();
      }
      if (connection != null) {
        connection.close();
      }
    }
    super.destroy();
  }

  public void validateConnection(final Connection connection) throws SQLException {
    assertNotNull(connection);
    final DatabaseMetaData dmd = connection.getMetaData();
    assertNotNull(dmd);
    final String url = dmd.getURL();
    assertNotNull(url);
    if (this.inMemory) {
      assertTrue(url.startsWith("jdbc:h2:mem:"));
    } else {
      assertTrue(url.startsWith("jdbc:h2:"));
    }
  }

  private boolean getShutdown() {
    return this.shutdown;
  }

  private void setShutdown(final boolean shutdown) {
    this.shutdown = shutdown;
  }

}
