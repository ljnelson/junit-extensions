/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
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

import java.util.Properties;

import com.edugility.throwables.ThrowableChain;

import org.dbunit.IDatabaseTester;
import org.dbunit.IOperationListener;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.JndiDatabaseTester;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;

import org.dbunit.database.IDatabaseConnection;

import org.dbunit.dataset.IDataSet;

import org.dbunit.operation.DatabaseOperation;

import org.junit.rules.TestRule;

import org.junit.runner.Description;

import org.junit.runners.model.Statement;

public class DatabaseTesterRule implements IDatabaseTester, TestRule {

  public final IDatabaseTester delegate;

  private DataSetLocator locator;

  public DatabaseTesterRule() throws Exception {
    this(new PropertiesBasedJdbcDatabaseTester());
  }

  public DatabaseTesterRule(final String lookupNameOrConnectionUrl) throws Exception {
    super();
    if (lookupNameOrConnectionUrl == null) {
      throw new IllegalArgumentException("lookupNameOrConnectionUrl", new NullPointerException("lookupNameOrConnectionUrl"));
    } else if (lookupNameOrConnectionUrl.startsWith("jdbc:")) {
      this.delegate = new JdbcDatabaseTester("java.lang.Object", lookupNameOrConnectionUrl);
    } else {
      this.delegate = new JndiDatabaseTester(lookupNameOrConnectionUrl);
    }    
  }

  public DatabaseTesterRule(final Properties environment, final String lookupName) {
    this(new JndiDatabaseTester(environment, lookupName));
  }

  public DatabaseTesterRule(final Properties environment, final String lookupName, final String schema) {
    this(new JndiDatabaseTester(environment, lookupName, schema));
  }

  public DatabaseTesterRule(final String connectionUrl, final String username, final String password, final String schema) throws Exception {
    this(new JdbcDatabaseTester("java.lang.Object", connectionUrl, username, password, schema));
  }

  public DatabaseTesterRule(final IDatabaseTester delegate) {
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

  public IDataSet findDataSet(final Description description) throws Exception {
    DataSetLocator locator = this.getDataSetLocator();
    if (locator == null) {
      locator = new DataSetLocator();
    }
    return locator.findDataSet(description);
  }

  @Deprecated
  @Override
  public void closeConnection(final IDatabaseConnection connection) throws Exception {
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
  public void setDataSet(final IDataSet dataSet) {
    this.delegate.setDataSet(dataSet);
  }

  @Deprecated
  @Override
  public void setSchema(final String schema) {
    this.delegate.setSchema(schema);
  }

  @Override
  public void setSetUpOperation(final DatabaseOperation setUpOperation) {
    this.delegate.setSetUpOperation(setUpOperation);
  }

  @Override
  public void setTearDownOperation(final DatabaseOperation tearDownOperation) {
    this.delegate.setTearDownOperation(tearDownOperation);
  }

  @Override
  public void onSetup() throws Exception {
    this.delegate.onSetup();
  }

  @Override
  public void onTearDown() throws Exception {
    this.delegate.onTearDown();
  }

  @Override
  public void setOperationListener(final IOperationListener listener) {
    this.delegate.setOperationListener(listener);
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

            // Ensure there is an IDataSet present.
            final boolean dataSetWasNull = getDataSet() == null;
            if (dataSetWasNull) {
              setDataSet(findDataSet(description));
            }

            // Run dbUnit's preparatory phase, represented by
            // DatabaseTester#onSetup().
            onSetup();

            // Attempt to run the regular JUnit test, wrapped up
            // safely in such a way that ALL errors are caught.
            final ThrowableChain chain = new ThrowableChain();
            try {
              base.evaluate();
            } catch (final ThrowableChain notThrown) {
              throw notThrown;
            } catch (final Throwable everythingElse) {
              chain.add(everythingElse);
            } finally {
              try {

                // Run dbUnit's cleanup phase, represented by
                // DatabaseTester#onTearDown().
                onTearDown();

              } catch (final Throwable boom) {
                chain.add(boom);
              }

              // We might have used an auto-discovered IDataSet
              // appropriate for this test only.  Make sure we leave
              // things as we found them.
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
