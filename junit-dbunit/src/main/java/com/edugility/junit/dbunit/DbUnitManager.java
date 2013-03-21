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

import java.lang.reflect.Field;

import java.sql.Connection;

import java.util.List;
import java.util.Properties;

import com.edugility.junit.db.ConnectionDescriptor;
import com.edugility.junit.db.DBRule;
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

import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.TestClass;

import static org.junit.Assert.assertNotNull;

public class DbUnitManager extends AbstractDBManager implements IDatabaseTester {

  public final IDatabaseTester delegate;

  private transient boolean dataSetWasNull;

  private DataSetLocator locator;

  private final Object id;


  /*
   * Constructors.
   */


  public DbUnitManager() throws Exception {
    this(null, new PropertiesBasedJdbcDatabaseTester());
  }

  public DbUnitManager(final Object id) throws Exception {
    this(id, new PropertiesBasedJdbcDatabaseTester());
  }


  public DbUnitManager(final String lookupNameOrConnectionUrl) throws Exception {
    this(null, lookupNameOrConnectionUrl);
  }

  public DbUnitManager(final Object id, final String lookupNameOrConnectionUrl) throws Exception {
    super();
    this.id = id;
    if (lookupNameOrConnectionUrl == null) {
      throw new IllegalArgumentException("lookupNameOrConnectionUrl", new NullPointerException("lookupNameOrConnectionUrl"));
    } else if (lookupNameOrConnectionUrl.startsWith("jdbc:")) {
      this.delegate = new JdbcDatabaseTester("java.lang.Object", lookupNameOrConnectionUrl);
    } else {
      this.delegate = new JndiDatabaseTester(lookupNameOrConnectionUrl);
    }    
  }

  public DbUnitManager(final Properties environment, final String lookupName) {
    this(null, environment, lookupName);
  }

  public DbUnitManager(final Object id, final Properties environment, final String lookupName) {
    this(id, new JndiDatabaseTester(environment, lookupName));
  }

  public DbUnitManager(final Properties environment, final String lookupName, final String schema) {
    this(null, new JndiDatabaseTester(environment, lookupName, schema));
  }

  public DbUnitManager(final Object id, final Properties environment, final String lookupName, final String schema) {
    this(id, new JndiDatabaseTester(environment, lookupName, schema));
  }

  public DbUnitManager(final String connectionUrl, final String username, final String password) throws Exception {
    this(null, new JdbcDatabaseTester("java.lang.Object", connectionUrl, username, password));
  }

  public DbUnitManager(final Object id, final String connectionUrl, final String username, final String password) throws Exception {
    this(id, new JdbcDatabaseTester("java.lang.Object", connectionUrl, username, password));
  }

  public DbUnitManager(final String connectionUrl, final String username, final String password, final String schema) throws Exception {
    this(null, new JdbcDatabaseTester("java.lang.Object", connectionUrl, username, password, schema));
  }

  public DbUnitManager(final Object id, final String connectionUrl, final String username, final String password, final String schema) throws Exception {
    this(id, new JdbcDatabaseTester("java.lang.Object", connectionUrl, username, password, schema));
  }

  public DbUnitManager(final ConnectionDescriptor cd) throws Exception {
    this(null, cd);
  }

  public DbUnitManager(final Object id, final ConnectionDescriptor cd) throws Exception {
    super();
    this.id = id;
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
    this(null, delegate);
  }

  public DbUnitManager(final Object id, final IDatabaseTester delegate) {
    super();
    this.id = id;
    if (delegate == null) {
      throw new IllegalArgumentException("delegate", new NullPointerException("delegate"));
    }
    this.delegate = delegate;
  }


  /*
   * Instance methods.
   */

  public Object getId() {
    return this.id;
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


  /*
   * IDatabaseTester implementation.
   */


  @Deprecated
  @Override
  public final void closeConnection(final IDatabaseConnection connection) throws Exception {
    this.delegate.closeConnection(connection);
  }

  @Override
  public final IDatabaseConnection getConnection() throws Exception {
    return this.delegate.getConnection();
  }

  @Override
  public final IDataSet getDataSet() {
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
  public final void onSetup() throws Exception {
    this.delegate.onSetup();
  }

  @Override
  public final void onTearDown() throws Exception {
    this.delegate.onTearDown();
  }

  @Override
  public final void setOperationListener(final IOperationListener listener) {
    this.delegate.setOperationListener(listener);
  }


  /*
   * AbstractDBManager overrides.
   */


  @Override
  public void initialize() throws Exception {
    this.dataSetWasNull = this.getDataSet() == null;
    if (this.dataSetWasNull) {
      this.setDataSet(this.findDataSet());
    }
    this.onSetup();
  }

  @Override
  public void inject() throws Exception {
    this.inject(this.getDataSet());
  }

  @Override
  public void disconnect() throws Exception {
    this.inject(null);
  }

  private final void inject(final IDataSet dataSet) throws Exception {
    final Object testInstance = this.getTestInstance();
    if (testInstance != null) {
      final Description description = this.getDescription();
      final TestClass testClass = DBRule.getTestClass(description);
      assertNotNull(testClass);
        
      final List<FrameworkField> annotatedFields = testClass.getAnnotatedFields(DataSet.class);
      assertNotNull(annotatedFields);
        
      if (!annotatedFields.isEmpty()) {
        for (final FrameworkField ff : annotatedFields) {
          if (ff != null) {
            final Field f = ff.getField();
            if (f != null && IDataSet.class.isAssignableFrom(f.getType())) {
              final DataSet dataSetAnnotation = f.getAnnotation(DataSet.class);
              assertNotNull(dataSetAnnotation);

              boolean inject = false;
              final Object id = this.getId();
              final String value = dataSetAnnotation.value();
              if (id == null) {
                if (value == null || value.isEmpty()) {
                  inject = true;
                }
              } else if (id.equals(value)) {
                inject = true;
              }

              if (inject) {
                final boolean accessible = f.isAccessible();
                f.setAccessible(true);
                try {
                  f.set(testInstance, dataSet);
                } finally {
                  f.setAccessible(accessible);
                }
                
              }
            }
          }
        }
      }
    }
  }

  @Override
  public void reset() throws Exception {
    this.onTearDown();
    if (this.dataSetWasNull) {
      this.setDataSet(null);
    }
  }

}
