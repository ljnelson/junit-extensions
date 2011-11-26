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

import com.edugility.junit.dbunit.JdbcDatabaseTesterRule;

import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestCaseIntegration {

  @Rule
  public final TestRule rule = RuleChain.outerRule(new DataSourceLiquibaseRule("jdbc:h2:mem:test;INIT=CREATE SCHEMA IF NOT EXISTS test;DB_CLOSE_DELAY=-1", "sa", "", "test", "changelog.xml")).around(new JdbcDatabaseTesterRule(org.h2.Driver.class.getName(), "jdbc:h2:mem:test;INIT=CREATE SCHEMA IF NOT EXISTS test;DB_CLOSE_DELAY=-1", "sa", "", "test", null, null, null, null));

  public TestCaseIntegration() throws Exception {
    super();
  }

  @Test
  public void testIntegration() throws Exception {

  }
  
}