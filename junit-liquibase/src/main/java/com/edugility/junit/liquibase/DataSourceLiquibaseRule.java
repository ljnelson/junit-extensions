/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
 *
 * $Id$
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
package com.edugility.junit.liquibase;

import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

public class DataSourceLiquibaseRule extends AbstractLiquibaseRule {

  private DataSource dataSource;

  public DataSourceLiquibaseRule(final DataSource dataSource, final String username, final String password, final String schema, final String changeLogResourceName, final String... changeLogContexts) {
    super(username, password, schema, changeLogResourceName, changeLogContexts);
    this.setDataSource(dataSource);
  }

  public DataSourceLiquibaseRule(final String url, final String username, final String password, final String schema, final String changeLogResourceName, final String... changeLogContexts) {
    super(username, password, schema, changeLogResourceName, changeLogContexts);
    this.setDataSource(new DriverManagerDataSource(url));
  }

  public void setDataSource(final DataSource dataSource) {
    if (dataSource == null) {
      throw new IllegalArgumentException("dataSource", new NullPointerException("dataSource == null"));
    }
    this.dataSource = dataSource;
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

  @Override
  public Connection getConnection() throws SQLException {
    final DataSource dataSource = this.getDataSource();
    if (dataSource == null) {
      throw new IllegalStateException("getDataSource() == null");
    }
    final String username = this.getUsername();
    if (username == null) {
      return dataSource.getConnection();
    } else {
      return dataSource.getConnection(username, this.getPassword());
    }
  }

  public static final class DriverManagerDataSource implements DataSource {
    
    private final String url;

    public DriverManagerDataSource(final String url) {
      super();
      this.url = url;
      if (url == null) {
        throw new IllegalArgumentException("url", new NullPointerException("url == null"));
      }
    }

    @Override
    public final int getLoginTimeout() {
      return DriverManager.getLoginTimeout();
    }

    @Override
    public final void setLoginTimeout(final int timeout) {
      DriverManager.setLoginTimeout(timeout);
    }

    @Override
    public final PrintWriter getLogWriter() {
      return DriverManager.getLogWriter();
    }

    @Override
    public final void setLogWriter(final PrintWriter writer) {
      DriverManager.setLogWriter(writer);
    }

    @Override
    public final Connection getConnection() throws SQLException {
      return DriverManager.getConnection(url);
    }

    @Override
    public final Connection getConnection(final String username, final String password) throws SQLException {
      return DriverManager.getConnection(url, username, password);
    }

    @Override
    public final boolean isWrapperFor(final Class<?> cls) {
      return false;
    }

    @Override
    public final <T> T unwrap(final Class<T> cls) throws SQLException {
      throw new SQLException(new UnsupportedOperationException("unwrap"));
    }

  }
  
}