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

import java.sql.Connection;

import java.util.Arrays;

import com.edugility.junit.db.ConnectionDescriptor;
import com.edugility.junit.db.DBConnection;
import com.edugility.junit.db.DBRule;

import com.edugility.junit.dbunit.DatabaseTesterRule;
import com.edugility.junit.dbunit.DataSet;
import com.edugility.junit.dbunit.DbUnitManager;

import com.edugility.junit.h2.H2Manager;

import org.dbunit.IDatabaseTester;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;

import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestCaseIntegration {

  static {
    try {
      CustomLoggingPropertiesLoader.loadLoggingProperties();
    } catch (final Exception everything) {
      everything.printStackTrace(System.err);
    }
  }

  @DBConnection(catalog = "test")
  private Connection connection;

  @DataSet
  private IDataSet dataSet;

  private final DbUnitManager dbUnitManager;

  @Rule
  public final DBRule rule;

  public TestCaseIntegration() throws Exception {
    super();
    final ConnectionDescriptor cd = new ConnectionDescriptor("jdbc:h2:mem:test;INIT=CREATE SCHEMA IF NOT EXISTS test;DB_CLOSE_DELAY=-1", "test", "test", "sa", "");
    final H2Manager h2Manager = new H2Manager(cd);
    dbUnitManager = new DbUnitManager(cd);
    final LiquibaseManager liquibaseManager = new LiquibaseManager(cd);
    this.rule = new DBRule(Arrays.asList(h2Manager, liquibaseManager, dbUnitManager));
  }

  @Test
  public void testIntegration() throws Exception {
    assertNotNull(this.connection);
    assertNotNull(this.dataSet);

    final IDataSet dataSet = this.dbUnitManager.getDataSet();
    assertNotNull(dataSet);
    final ITable rockTable = dataSet.getTable("rock");
    assertNotNull(rockTable);
    assertEquals(2, rockTable.getRowCount());
  }
  
}
