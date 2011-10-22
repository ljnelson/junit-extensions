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
package com.edugility.junit.db;

import java.sql.Connection;

import java.util.Iterator;

import com.edugility.throwables.ThrowableChain;

import org.junit.runners.model.Statement;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestCaseAbstractDBRule {

  public TestCaseAbstractDBRule() {
    super();
  }

  /**
   * Ensures that even if the lifecycle stages of {@link
   * AbstractDBRule} fail the cleanup code is always run.
   *
   * @exception Throwable if an error occurs
   */
  @Test
  public void testExceptionHandling() throws Throwable {

    final AbstractDBRule dbRule = new AbstractDBRule("test", "test", "sa", "") {
        @Override
        public final Connection getConnection() {
          return null;
        }

        @Override
        public void create() throws Exception {
          super.create();
          throw new Exception("create");
        }

        @Override
        public void connect() throws Exception {
          super.connect();
          throw new Exception("connect");
        }

        @Override
        public void initialize() throws Exception {
          super.initialize();
          throw new Exception("initialize");
        }

        @Override
        public void reset() throws Exception {
          super.reset();
          throw new Exception("reset");
        }

        @Override
        public void disconnect() throws Exception {
          super.disconnect();
          throw new Exception("disconnect");
        }

        @Override
        public void destroy() throws Exception {
          super.destroy();
          throw new Exception("destroy");
        }

      };

    Statement statement = new Statement() {
        @Override
        public final void evaluate() {

        }
      };

    statement = dbRule.apply(statement, null);
    assertNotNull(statement);
    try {
      statement.evaluate();
    } catch (final ThrowableChain expected) {
      final Iterator<Throwable> i = expected.iterator();
      assertNotNull(i);
      assertTrue(i.hasNext());
      assertSame(expected, i.next());
      assertTrue(i.hasNext());
      for (int j = 1; i.hasNext(); j++) {
        final Throwable t = i.next();
        assertNotNull(t);
        switch (j) {
        case 1:
          assertEquals("reset", t.getMessage());
          break;
        case 2:
          assertEquals("disconnect", t.getMessage());          
          break;
        case 3:
          assertEquals("destroy", t.getMessage());
          break;
        default:
          fail();
        }
      }
    }
  }

}