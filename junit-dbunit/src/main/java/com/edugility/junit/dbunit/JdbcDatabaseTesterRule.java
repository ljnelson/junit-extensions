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

import com.edugility.throwables.ThrowableChain;

import org.dbunit.JdbcDatabaseTester;

import org.junit.rules.TestRule;

import org.junit.runner.Description;

import org.junit.runners.model.Statement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JdbcDatabaseTesterRule extends JdbcDatabaseTester implements TestRule {

  public JdbcDatabaseTesterRule(final String driverClassName, final String connectionURL, final String username, final String password, final String schema) throws ClassNotFoundException {
    super(driverClassName, connectionURL, username, password, schema);
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