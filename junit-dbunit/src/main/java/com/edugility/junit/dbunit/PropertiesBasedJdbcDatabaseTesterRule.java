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

import org.dbunit.DefaultOperationListener;
import org.dbunit.IOperationListener;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;

import org.dbunit.dataset.IDataSet;

import org.dbunit.operation.DatabaseOperation;

public class PropertiesBasedJdbcDatabaseTesterRule extends JdbcDatabaseTesterRule {

  public PropertiesBasedJdbcDatabaseTesterRule() throws Exception {
    this(null, DatabaseOperation.CLEAN_INSERT, DatabaseOperation.NONE, new DefaultOperationListener());
  }

  public PropertiesBasedJdbcDatabaseTesterRule(final IDataSet dataSet) throws Exception {
    this(dataSet, DatabaseOperation.CLEAN_INSERT, DatabaseOperation.NONE, new DefaultOperationListener());
  }

  public PropertiesBasedJdbcDatabaseTesterRule(IDataSet dataSet, final DatabaseOperation setUpOperation, final DatabaseOperation tearDownOperation) throws Exception {
    this(dataSet, setUpOperation, tearDownOperation, new DefaultOperationListener());
  }

  public PropertiesBasedJdbcDatabaseTesterRule(IDataSet dataSet, final DatabaseOperation setUpOperation, final DatabaseOperation tearDownOperation, final IOperationListener listener) throws Exception {
    super(System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL),
          System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME),
          System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD),
          System.getProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_SCHEMA),
          dataSet,
          setUpOperation,
          tearDownOperation,
          listener);
  }

}
