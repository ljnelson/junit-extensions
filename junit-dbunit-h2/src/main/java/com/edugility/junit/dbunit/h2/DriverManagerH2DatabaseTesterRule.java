/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
 *
 * Copyright (c) 2011-2011 Edugility LLC.
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
package com.edugility.junit.dbunit.h2;

import java.sql.Connection;
import java.sql.SQLException;

import com.edugility.junit.db.ConnectionDescriptor;

import com.edugility.junit.h2.DriverManagerH2Rule;

import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.IDatabaseTester;
import org.dbunit.IOperationListener;

import org.dbunit.operation.DatabaseOperation;

import org.dbunit.dataset.IDataSet;

import org.h2.Driver;

public class DriverManagerH2DatabaseTesterRule extends DriverManagerH2Rule {

  private final IDatabaseTester tester;

  public DriverManagerH2DatabaseTesterRule(final IDatabaseTester tester) {
    super(null, null, null, null, false);
    this.tester = tester;
    if (tester == null) {
      throw new IllegalArgumentException("tester == null");
    }
  }

  public DriverManagerH2DatabaseTesterRule(final String catalog, final String schema, final String username, final String password, final boolean shutdown, final IDataSet dataSet, final DatabaseOperation setUpOperation, final DatabaseOperation tearDownOperation, final IOperationListener listener) {
    super(catalog, schema, username, password, shutdown);
    this.tester = this.createDatabaseTester(dataSet, setUpOperation, tearDownOperation, listener);
    if (this.tester == null) {
      throw new IllegalStateException("createDatabaseTester() == null");
    }
  }

  public DriverManagerH2DatabaseTesterRule(final ConnectionDescriptor descriptor, final boolean shutDown, final IDataSet dataSet, final DatabaseOperation setUpOperation, final DatabaseOperation tearDownOperation, final IOperationListener listener) {
    this(descriptor == null ? null : descriptor.getCatalog(),
         descriptor == null ? null : descriptor.getSchema(),
         descriptor == null ? null : descriptor.getUsername(),
         descriptor == null ? null : descriptor.getPassword(),
         shutDown,
         dataSet,
         setUpOperation,
         tearDownOperation,
         listener);
  }

  protected IDatabaseTester createDatabaseTester(final IDataSet dataSet, final DatabaseOperation setUpOperation, final DatabaseOperation tearDownOperation, final IOperationListener listener) {
    IDatabaseTester tester = null;
    try {
      tester = new JdbcDatabaseTester(Driver.class.getName(), super.getURL(), this.getUsername(), this.getPassword(), this.getSchema());
      if (dataSet != null) {
        tester.setDataSet(dataSet);
      }
      if (setUpOperation != null) {
        tester.setSetUpOperation(setUpOperation);
      }
      if (tearDownOperation != null) {
        tester.setTearDownOperation(tearDownOperation);
      }
      if (listener != null) {
        tester.setOperationListener(listener);
      }
    } catch (final ClassNotFoundException cannotHappen) {
      throw new IllegalStateException(cannotHappen);
    }
    return tester;
  }

  @Override
  public Connection getConnection() throws SQLException {
    IDatabaseConnection connection = null;
    try {
      connection = this.tester.getConnection();
    } catch (final RuntimeException throwMe) {
      throw throwMe;
    } catch (final SQLException throwMe) {
      throw throwMe;
    } catch (final Exception everythingElse) {
      throw new SQLException(everythingElse);
    }
    if (connection == null) {
      return null;
    }
    this.configureConnection(connection);
    return connection.getConnection();
  }

  protected void configureConnection(final IDatabaseConnection c) throws SQLException {

  }

  protected final IDatabaseTester getDatabaseTester() {
    return this.tester;
  }

  @Override
  public void initialize() throws Exception {
    this.tester.onSetup();
  }

  @Override
  public void reset() throws Exception {
    this.tester.onTearDown();
  }

}