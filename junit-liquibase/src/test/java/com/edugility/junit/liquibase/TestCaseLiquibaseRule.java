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

import java.io.InputStream;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.logging.LogManager;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestCaseLiquibaseRule {

  static {
    final LogManager logManager = LogManager.getLogManager();
    assertNotNull(logManager);
    try {
      final InputStream loggingProperties = TestCaseLiquibaseRule.class.getResourceAsStream("/logging.properties");
      assertNotNull(loggingProperties);
      logManager.readConfiguration(loggingProperties);
      loggingProperties.close();
    } catch (final Exception everything) {
      everything.printStackTrace(System.err);
    }
  }

  @Rule
  public final LiquibaseRule liquibaseRule;

  public TestCaseLiquibaseRule() {
    super();
    this.liquibaseRule = new LiquibaseRule("jdbc:h2:mem:test;INIT=CREATE SCHEMA IF NOT EXISTS test;DB_CLOSE_DELAY=-1", "sa", "", "test", "changelog.xml", "test");
  }

  @Test
  public void testRuleFiring() throws Exception {
    final Connection connection = DriverManager.getConnection(this.liquibaseRule.getConnectionURL(), this.liquibaseRule.getUsername(), this.liquibaseRule.getPassword());
    assertNotNull(connection);
    ResultSet rs = null;
    try {

      final DatabaseMetaData dmd = connection.getMetaData();
      assertNotNull(dmd);

      rs = dmd.getTables("TEST", "TEST", "ROCK", null);
      assertNotNull(rs);
      assertTrue(rs.next());

      assertEquals("ROCK", rs.getString(3));

      assertFalse(rs.next());


    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (final SQLException ignore) {

        }
      }
      if (connection != null) {
        try {
          connection.close();
        } catch (final SQLException ignore) {

        }
      }
    }
  }

}
