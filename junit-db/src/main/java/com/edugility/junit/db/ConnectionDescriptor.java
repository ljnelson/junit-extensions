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
package com.edugility.junit.db;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public class ConnectionDescriptor implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String connectionURL;

  private final String catalog;

  private final String schema;

  private final String username;

  private final String password;

  private transient String hashCodeString;

  public ConnectionDescriptor(final String connectionURL, final String catalog, final String schema, final String username, final String password) {
    super();
    this.connectionURL = connectionURL;
    this.catalog = catalog;
    this.schema = schema;
    this.username = username;
    this.password = password;
    if (connectionURL == null) {
      throw new IllegalArgumentException("connectionURL", new NullPointerException("connectionURL"));
    }
    this.hashCodeString = String.valueOf(this.getConnectionURL() + this.getCatalog() + this.getSchema() + this.getUsername() + this.getPassword());
  }

  public String getConnectionURL() {
    return this.connectionURL;
  }

  public String getCatalog() {
    return this.catalog;
  }

  public String getSchema() {
    return this.schema;
  }

  public String getUsername() {
    return this.username;
  }

  public String getPassword() {
    return this.password;
  }

  public Connection getConnection() throws SQLException {
    return this.getConnection(new DriverManagerDataSource(this.getConnectionURL(), this.getUsername(), this.getPassword()));
  }

  public Connection getConnection(final DataSource dataSource) throws SQLException {
    Connection connection = null;
    if (dataSource != null) {
      final String username = this.getUsername();
      if (username == null) {
        connection = dataSource.getConnection();
      } else {
        connection = dataSource.getConnection(username, this.getPassword());
      }
    }
    return connection;
  }

  @Override
  public int hashCode() {
    assert this.hashCodeString != null;
    return this.hashCodeString.hashCode();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && other.getClass().equals(this.getClass())) {
      assert this.hashCodeString != null;
      return this.hashCodeString.equals(((ConnectionDescriptor)other).hashCodeString);
    } else {
      return false;
    }
  }

  private final void readObject(final ObjectInputStream stream) throws ClassNotFoundException, IOException {
    if (stream != null) {
      stream.defaultReadObject();
      if (this.hashCodeString == null) {
        this.hashCodeString = String.valueOf(this.getConnectionURL() + this.getCatalog() + this.getSchema() + this.getUsername() + this.getPassword());
      }
    }
  }

}