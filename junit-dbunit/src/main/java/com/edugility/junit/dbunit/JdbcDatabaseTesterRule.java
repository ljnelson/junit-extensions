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
package com.edugility.junit.dbunit;

import java.net.URL;

import com.edugility.junit.db.ConnectionDescriptor;

import com.edugility.throwables.ThrowableChain;

import org.dbunit.DefaultOperationListener;
import org.dbunit.IOperationListener;
import org.dbunit.JdbcDatabaseTester;

import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.IDataSet;

import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

import org.dbunit.operation.DatabaseOperation;

import org.junit.rules.TestRule;

import org.junit.runner.Description;

import org.junit.runners.model.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JdbcDatabaseTesterRule extends JdbcDatabaseTester implements TestRule {

  private static final Logger logger = LoggerFactory.getLogger(JdbcDatabaseTester.class);

  private DataSetLocator locator;

  public JdbcDatabaseTesterRule(final ConnectionDescriptor descriptor) throws Exception {
    this(descriptor, null, DatabaseOperation.CLEAN_INSERT, DatabaseOperation.NONE, new DefaultOperationListener());
  }

  public JdbcDatabaseTesterRule(final ConnectionDescriptor descriptor, final IDataSet dataSet) throws Exception {
    this(descriptor, dataSet, DatabaseOperation.CLEAN_INSERT, DatabaseOperation.NONE, new DefaultOperationListener());
  }

  public JdbcDatabaseTesterRule(final ConnectionDescriptor descriptor, final IDataSet dataSet, final DatabaseOperation setUpOperation, final DatabaseOperation tearDownOperation) throws Exception {
    this("java.lang.Object",
         descriptor == null ? null : descriptor.getConnectionURL(),
         descriptor == null ? null : descriptor.getUsername(),
         descriptor == null ? null : descriptor.getPassword(),
         descriptor == null ? null : descriptor.getSchema(),
         dataSet,
         setUpOperation,
         tearDownOperation,
         new DefaultOperationListener());
  }

  public JdbcDatabaseTesterRule(final ConnectionDescriptor descriptor, final IDataSet dataSet, final DatabaseOperation setUpOperation, final DatabaseOperation tearDownOperation, final IOperationListener listener) throws Exception {
    this("java.lang.Object",
         descriptor == null ? null : descriptor.getConnectionURL(),
         descriptor == null ? null : descriptor.getUsername(),
         descriptor == null ? null : descriptor.getPassword(),
         descriptor == null ? null : descriptor.getSchema(),
         dataSet,
         setUpOperation,
         tearDownOperation,
         listener);
  }

  public JdbcDatabaseTesterRule(final String connectionURL, final String username, final String password, final String schema) throws Exception {
    this("java.lang.Object", connectionURL, username, password, schema, null, DatabaseOperation.CLEAN_INSERT, DatabaseOperation.NONE, new DefaultOperationListener());
  }

  public JdbcDatabaseTesterRule(final String connectionURL, final String username, final String password, final String schema, final IDataSet dataSet) throws Exception {
    this("java.lang.Object", connectionURL, username, password, schema, dataSet, DatabaseOperation.CLEAN_INSERT, DatabaseOperation.NONE, new DefaultOperationListener());
  }

  public JdbcDatabaseTesterRule(final String connectionURL, final String username, final String password, final String schema, IDataSet dataSet, final DatabaseOperation setUpOperation, final DatabaseOperation tearDownOperation) throws Exception {
    this("java.lang.Object", connectionURL, username, password, schema, dataSet, setUpOperation, tearDownOperation, new DefaultOperationListener());
  }

  public JdbcDatabaseTesterRule(final String connectionURL, final String username, final String password, final String schema, IDataSet dataSet, final DatabaseOperation setUpOperation, final DatabaseOperation tearDownOperation, final IOperationListener listener) throws Exception {
    this("java.lang.Object", connectionURL, username, password, schema, dataSet, setUpOperation, tearDownOperation, listener);
  }

  public JdbcDatabaseTesterRule(final String driverClassName, final String connectionURL, final String username, final String password, final String schema, IDataSet dataSet, final DatabaseOperation setUpOperation, final DatabaseOperation tearDownOperation, final IOperationListener listener) throws Exception {
    super(driverClassName, connectionURL, username, password, schema);
    logger.info("Entering root constructor");
    if (dataSet != null) {
      this.setDataSet(dataSet);
    }
    if (setUpOperation == null) {
      this.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
    } else {
      this.setSetUpOperation(setUpOperation);
    }
    if (tearDownOperation == null) {
      this.setTearDownOperation(DatabaseOperation.NONE);
    } else {
      this.setTearDownOperation(tearDownOperation);
    }
    if (listener == null) {
      this.setOperationListener(new DefaultOperationListener());
    } else {
      this.setOperationListener(listener);
    }
  }

  public DataSetLocator getDataSetLocator() {
    return this.locator;
  }

  protected IDataSet findDataSet(final Description description) throws Exception {
    DataSetLocator locator = this.getDataSetLocator();
    if (locator == null) {
      locator = new DataSetLocator();
    }
    return locator.findDataSet(description);
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    final Statement returnValue;
    if (base == null) {
      returnValue = null;
    } else {
      returnValue = new Statement() {
          @Override
          public final void evaluate() throws Throwable {
            final boolean dataSetWasNull = getDataSet() == null;
            if (dataSetWasNull) {
              setDataSet(findDataSet(description));
            }
            onSetup();
            final ThrowableChain chain = new ThrowableChain();
            try {
              base.evaluate();
            } catch (final ThrowableChain notThrown) {
              throw notThrown;
            } catch (final Throwable everythingElse) {
              chain.add(everythingElse);
            } finally {
              try {
                onTearDown();
              } catch (final Throwable boom) {
                chain.add(boom);
              }
              if (dataSetWasNull) {
                setDataSet(null);
              }
              if (chain.size() > 1) {
                throw chain;
              }
            }
          }
        };
    }
    return returnValue;
  }

}