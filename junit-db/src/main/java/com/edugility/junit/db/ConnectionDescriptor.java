/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright (c) 2010-2013 Edugility LLC.
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
package com.edugility.junit.db;

import java.io.PrintWriter;
import java.io.Serializable;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Properties;

import javax.sql.DataSource;

public class ConnectionDescriptor extends Properties implements DataSource, Serializable {

  public static final String CATALOG = "com.edugility.junit.db.catalog";

  public static final String CONNECTION_URL = "javax.persistence.jdbc.url";

  public static final String DATA_SOURCE = "com.edugility.junit.db.datasource";

  public static final String DRIVER_CLASS_NAME = "javax.persistence.jdbc.driver";

  public static final String PASSWORD = "javax.persistence.jdbc.password";

  public static final String SCHEMA = "com.edugility.junit.db.schema";

  public static final String USERNAME = "javax.persistence.jdbc.user";

  private static final long serialVersionUID = 1L;

  public ConnectionDescriptor(final String connectionURL, final String catalog, final String schema, final String username, final String password) {
    super();
    this.setProperty(CONNECTION_URL, connectionURL);
    this.setProperty(CATALOG, catalog);
    this.setProperty(SCHEMA, schema);
    this.setProperty(USERNAME, username);
    this.setProperty(PASSWORD, password);
  }

  public final DataSource getDataSource() {
    DataSource dataSource = (DataSource)this.get(DATA_SOURCE);
    if (dataSource == null) {
      final String connectionURL = this.getConnectionURL();
      if (connectionURL != null) {
        dataSource = new DriverManagerDataSource(this);
        this.put(DATA_SOURCE, dataSource);
      }
    }
    return dataSource;
  }

  public String getDriverClassName() {
    return this.getProperty(DRIVER_CLASS_NAME);
  }

  public String getConnectionURL() {
    return this.getProperty(CONNECTION_URL);
  }

  public String getCatalog() {
    return this.getProperty(CATALOG);
  }

  public String getSchema() {
    return this.getProperty(SCHEMA);
  }

  public String getUsername() {
    return this.getProperty(USERNAME);
  }

  public String getPassword() {
    return this.getProperty(PASSWORD);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    int timeout = 0;
    final DataSource dataSource = this.getDataSource();
    if (dataSource != null) {
      timeout = dataSource.getLoginTimeout();
    }
    return timeout;
  }

  @Override
  public void setLoginTimeout(final int timeout) throws SQLException {
    final DataSource ds = this.getDataSource();
    if (ds != null) {
      ds.setLoginTimeout(timeout);
    }
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    PrintWriter writer = null;
    final DataSource ds = this.getDataSource();
    if (ds != null) {
      writer = ds.getLogWriter();
    } else {
      writer = new PrintWriter(System.out);
    }
    return writer;
  }

  @Override
  public void setLogWriter(final PrintWriter writer) throws SQLException {
    final DataSource ds = this.getDataSource();
    if (ds != null) {
      ds.setLogWriter(writer);
    }
  }

  @Override
  public boolean isWrapperFor(final Class<?> cls) {
    return cls != null && cls.isInstance(this);
  }

  @Override
  public <T> T unwrap(final Class<T> cls) {
    return cls.cast(this);
  }

  @Override
  public final Connection getConnection() throws SQLException {
    return this.getConnection(this.getDataSource(), this.getUsername(), this.getPassword());
  }

  @Override
  public final Connection getConnection(final String username, final String password) throws SQLException {
    return this.getConnection(this.getDataSource(), username, password);
  }

  public Connection getConnection(final DataSource dataSource) throws SQLException {
    return this.getConnection(dataSource, this.getUsername(), this.getPassword());
  }

  public Connection getConnection(final DataSource dataSource, String username, String password) throws SQLException {
    Connection connection = null;
    if (dataSource != null) {
      if (username == null) {
        connection = dataSource.getConnection();
      } else {
        connection = dataSource.getConnection(username, password);
      }
    }
    return connection;
  }

}
