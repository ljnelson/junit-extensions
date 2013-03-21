/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright (c) 2013 Edugility LLC.
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
package com.edugility.junit.dbunit;

import java.sql.Connection;

import java.util.Properties;

import com.edugility.junit.db.ConnectionDescriptor;
import com.edugility.junit.db.DBRule.AbstractDBManager;

import org.dbunit.AbstractDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.IOperationListener;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.JndiDatabaseTester;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;

import org.dbunit.dataset.IDataSet;

import org.dbunit.operation.DatabaseOperation;

import org.junit.runner.Description;

public class DbUnitManager extends AbstractDBManager implements IDatabaseTester {

public final IDatabaseTester delegate;

  private transient boolean dataSetWasNull;

  private DataSetLocator locator;

  public DbUnitManager() throws Exception {
    this(new PropertiesBasedJdbcDatabaseTester());
  }

  public DbUnitManager(final String lookupNameOrConnectionUrl) throws Exception {
    super();
    if (lookupNameOrConnectionUrl == null) {
      throw new IllegalArgumentException("lookupNameOrConnectionUrl", new NullPointerException("lookupNameOrConnectionUrl"));
    } else if (lookupNameOrConnectionUrl.startsWith("jdbc:")) {
      this.delegate = new JdbcDatabaseTester("java.lang.Object", lookupNameOrConnectionUrl);
    } else {
      this.delegate = new JndiDatabaseTester(lookupNameOrConnectionUrl);
    }    
  }

  public DbUnitManager(final Properties environment, final String lookupName) {
    this(new JndiDatabaseTester(environment, lookupName));
  }

  public DbUnitManager(final Properties environment, final String lookupName, final String schema) {
    this(new JndiDatabaseTester(environment, lookupName, schema));
  }

  public DbUnitManager(final String connectionUrl, final String username, final String password) throws Exception {
    this(new JdbcDatabaseTester("java.lang.Object", connectionUrl, username, password));
  }

  public DbUnitManager(final String connectionUrl, final String username, final String password, final String schema) throws Exception {
    this(new JdbcDatabaseTester("java.lang.Object", connectionUrl, username, password, schema));
  }

  public DbUnitManager(final ConnectionDescriptor cd) throws Exception {
    super();
    if (cd == null) {
      this.delegate = new PropertiesBasedJdbcDatabaseTester();
    } else {
      this.delegate = new AbstractDatabaseTester() {
          @Override
          public final IDatabaseConnection getConnection() throws Exception {
            final Connection connection = cd.getConnection();
            if (connection == null) {
              throw new IllegalStateException("cd.getConnection() == null");
            }
            return new DatabaseConnection(connection, cd.getSchema());
          }
        };
    }
  }

  public DbUnitManager(final IDatabaseTester delegate) {
    super();
    if (delegate == null) {
      throw new IllegalArgumentException("delegate", new NullPointerException("delegate"));
    }
    this.delegate = delegate;
  }

  public DataSetLocator getDataSetLocator() {
    return this.locator;
  }

  public void setDataSetLocator(final DataSetLocator locator) {
    this.locator = locator;
  }

  public IDataSet findDataSet() throws Exception {
    DataSetLocator locator = this.getDataSetLocator();
    if (locator == null) {
      locator = new DataSetLocator();
    }
    return locator.findDataSet(this.getDescription());
  }

  @Deprecated
  @Override
  public final void closeConnection(final IDatabaseConnection connection) throws Exception {
    this.delegate.closeConnection(connection);
  }

  @Override
  public IDatabaseConnection getConnection() throws Exception {
    return this.delegate.getConnection();
  }

  @Override
  public IDataSet getDataSet() {
    return this.delegate.getDataSet();
  }

  @Override
  public final void setDataSet(final IDataSet dataSet) {
    this.delegate.setDataSet(dataSet);
  }

  @Deprecated
  @Override
  public final void setSchema(final String schema) {
    this.delegate.setSchema(schema);
  }

  @Override
  public final void setSetUpOperation(final DatabaseOperation setUpOperation) {
    this.delegate.setSetUpOperation(setUpOperation);
  }

  @Override
  public final void setTearDownOperation(final DatabaseOperation tearDownOperation) {
    this.delegate.setTearDownOperation(tearDownOperation);
  }

  @Override
  public void initialize() throws Exception {
    this.dataSetWasNull = this.getDataSet() == null;
    if (this.dataSetWasNull) {
      this.setDataSet(this.findDataSet());
    }
    this.onSetup();
  }

  @Override
  public final void onSetup() throws Exception {
    this.delegate.onSetup();
  }

  @Override
  public void reset() throws Exception {
    this.onTearDown();
    if (this.dataSetWasNull) {
      this.setDataSet(null);
    }
  }

  @Override
  public final void onTearDown() throws Exception {
    this.delegate.onTearDown();
  }

  @Override
  public final void setOperationListener(final IOperationListener listener) {
    this.delegate.setOperationListener(listener);
  }

}
