/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright (c) 2010-2013 Edugility LLC.
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

import java.lang.reflect.Field;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.edugility.throwables.ThrowableChain;

import org.junit.internal.runners.statements.ExpectException;

import org.junit.rules.TestRule;

import org.junit.runner.Description;

import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import static org.junit.Assert.*;

public class DBRule implements TestRule {

  private static final Map<Class<?>, TestClass> testClasses = Collections.synchronizedMap(new HashMap<Class<?>, TestClass>());

  private transient Connection c;

  private transient final ConnectionDescriptor cd;

  private transient TestClass testClass;

  private transient Object testInstance;

  private transient Description description;

  private transient Collection<DBManager> managers;

  private transient DBManager failedManager;

  public DBRule() {
    this(null);
  }

  public DBRule(final ConnectionDescriptor cd) {
    this(cd, null);
  }

  public DBRule(final ConnectionDescriptor cd, final Collection<? extends DBManager> managers) {
    super();
    this.cd = cd;
    if (managers != null && !managers.isEmpty()) {
      this.managers = new ArrayList<DBManager>(managers);
    } else {
      this.managers = Collections.emptyList();
    }
  }

  public TestClass getTestClass() {
    return this.testClass;
  }

  public Object getTestInstance() {
    return this.testInstance;
  }

  public Description getDescription() {
    return this.description;
  }

  public Connection getAllocatedConnection() {
    return this.c;
  }

  public ConnectionDescriptor getConnectionDescriptor() {
    return this.cd;
  }

  public String getConnectionURL() {
    if (this.cd == null) {
      return null;
    }
    return this.cd.getConnectionURL();
  }

  public void create() throws Exception {
    this.failedManager = null;
    if (this.managers != null && !this.managers.isEmpty()) {
      for (final DBManager manager : this.managers) {
        if (manager != null) {
          try {
            manager.create(this.cd);
          } catch (final Throwable boom) {
            this.failedManager = manager;
            if (boom instanceof Exception) {
              throw (Exception)boom;
            } else {
              throw (Error)boom;
            }
          }
        }
      }
    }
  }

  public void createFailed(final Throwable createFailed) throws Exception {
    if (this.failedManager != null) {
      this.failedManager.createFailed(createFailed);
    }
  }

  public void connect() throws Exception {
    this.failedManager = null;
    if (this.managers != null && !this.managers.isEmpty()) {
      for (final DBManager manager : this.managers) {
        if (manager != null) {
          try {
            manager.connect();
          } catch (final Throwable boom) {
            this.failedManager = manager;
            if (boom instanceof Exception) {
              throw (Exception)boom;
            } else {
              throw (Error)boom;
            }
          }
        }
      }
    }
  }

  protected void inject() throws Exception {
    this.failedManager = null;
    if (this.managers != null && !this.managers.isEmpty()) {
      for (final DBManager manager : this.managers) {
        if (manager != null) {
          try {
            manager.inject(this.testInstance, this.getDescription());
          } catch (final Throwable boom) {
            this.failedManager = manager;
            if (boom instanceof Exception) {
              throw (Exception)boom;
            } else {
              throw (Error)boom;
            }
          }
        }
      }
    }

    /*
    if (this.testInstance != null) {
      final TestClass testClass = this.getTestClass();
      assertNotNull(testClass);

      final List<FrameworkField> annotatedFields = testClass.getAnnotatedFields(DBConnection.class);
      assertNotNull(annotatedFields);

      if (!annotatedFields.isEmpty()) {
        for (final FrameworkField ff : annotatedFields) {
          if (ff != null) {
            final Field f = ff.getField();
            if (f != null && Connection.class.isAssignableFrom(f.getType())) {
              final boolean accessible = f.isAccessible();
              f.setAccessible(true);
              try {
                f.set(this.testInstance, connection);
              } finally {
                f.setAccessible(accessible);
              }
            }
          }
        }
      }
    }
    */

  }

  public void connectFailed(final Throwable connectFailed) throws Exception {
    if (this.failedManager != null) {
      this.failedManager.connectFailed(connectFailed);
    }
    if (this.c != null) {
      this.c.close();
    }
  }

  public void initialize() throws Exception {
    this.failedManager = null;
    if (this.managers != null && !this.managers.isEmpty()) {
      for (final DBManager manager : this.managers) {
        if (manager != null) {
          try {
            manager.initialize();
          } catch (final Throwable boom) {
            this.failedManager = manager;
            if (boom instanceof Exception) {
              throw (Exception)boom;
            } else {
              throw (Error)boom;
            }
          }
        }
      }
    }
  }

  public void initializeFailed(final Throwable initializeFailed) throws Exception {
    if (this.failedManager != null) {
      this.failedManager.initializeFailed(initializeFailed);
    }
  }

  public void evaluateSucceeded() throws Exception {
    this.failedManager = null;
    if (this.managers != null && !this.managers.isEmpty()) {
      for (final DBManager manager : this.managers) {
        if (manager != null) {
          try {
            manager.evaluateSucceeded();
          } catch (final Throwable boom) {
            this.failedManager = manager;
            if (boom instanceof Exception) {
              throw (Exception)boom;
            } else {
              throw (Error)boom;
            }
          }
        }
      }
    }
  }

  public void evaluateFailed(final Throwable evaluateFailed) throws Exception {
    if (this.failedManager != null) {
      this.failedManager.evaluateFailed(evaluateFailed);
    }
  }

  public void reset() throws Exception {
    this.failedManager = null;
    if (this.managers != null && !this.managers.isEmpty()) {
      for (final DBManager manager : this.managers) {
        if (manager != null) {
          try {
            manager.reset();
          } catch (final Throwable boom) {
            this.failedManager = manager;
            if (boom instanceof Exception) {
              throw (Exception)boom;
            } else {
              throw (Error)boom;
            }
          }
        }
      }
    }
  }

  public void resetFailed(final Throwable resetFailed) throws Exception {
    if (this.failedManager != null) {
      this.failedManager.resetFailed(resetFailed);
    }
  }

  public void disconnect() throws Exception {
    this.failedManager = null;
    if (this.managers != null && !this.managers.isEmpty()) {
      for (final DBManager manager : this.managers) {
        if (manager != null) {
          try {
            manager.disconnect();
          } catch (final Throwable boom) {
            this.failedManager = manager;
            if (boom instanceof Exception) {
              throw (Exception)boom;
            } else {
              throw (Error)boom;
            }
          }
        }
      }
    }
    final Connection c = this.getAllocatedConnection();
    if (c != null) {
      try {
        c.close();
      } catch (final SQLException ignoreOnPurpose) {
        
      }
    }
    if (this.testInstance != null) {
      final TestClass testClass = this.getTestClass();
      assertNotNull(testClass);

      final List<FrameworkField> annotatedFields = testClass.getAnnotatedFields(DBConnection.class);
      assertNotNull(annotatedFields);

      if (!annotatedFields.isEmpty()) {
        for (final FrameworkField ff : annotatedFields) {
          if (ff != null) {
            final Field f = ff.getField();
            if (f != null && Connection.class.isAssignableFrom(f.getType())) {
              final boolean accessible = f.isAccessible();
              try {
                f.setAccessible(true);
                f.set(this.testInstance, null);
              } finally {
                f.setAccessible(accessible);
              }
            }
          }
        }
      }
    }

  }

  public void disconnectFailed(final Throwable disconnectFailed) throws Exception {
    if (this.failedManager != null) {
      this.failedManager.disconnectFailed(disconnectFailed);
    }
  }

  public void destroy() throws Exception {
    this.failedManager = null;
    if (this.managers != null && !this.managers.isEmpty()) {
      for (final DBManager manager : this.managers) {
        if (manager != null) {
          try {
            manager.destroy();
          } catch (final Throwable boom) {
            this.failedManager = manager;
            if (boom instanceof Exception) {
              throw (Exception)boom;
            } else {
              throw (Error)boom;
            }
          }
        }
      }
    }
  }

  public void destroyFailed(final Throwable destroyFailed) throws Exception {
    if (this.failedManager != null) {
      this.failedManager.destroyFailed(destroyFailed);
    }
    this.failedManager = null;
  }

  public static final TestClass getTestClass(final Description description) {
    TestClass testClass = null;
    if (description != null) {
      final Class<?> c = description.getTestClass();
      if (c != null) {
        testClass = testClasses.get(c);
        if (testClass == null) {
          testClass = new TestClass(c);
          testClasses.put(c, testClass);
        }
      }
    }
    return testClass;
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    final Statement returnValue;
    if (base == null || description == null || !description.isTest()) {
      returnValue = base;
    } else {
      returnValue = new Statement() {

          @Override
          public final void evaluate() throws ThrowableChain {
            DBRule.this.description = description;
            testClass = getTestClass(description);
            assertNotNull("testClass == null", testClass);
            final ThrowableChain chain = new ThrowableChain();
            assert chain.size() == 1;
            try {
              testInstance = getTest(base);

              create();

              try {
                connect();
                inject();

                try {
                  initialize();

                  try {
                    base.evaluate();
                    evaluateSucceeded();
                  } catch (final Throwable evaluateFailed) {
                    chain.add(evaluateFailed);
                    try {
                      evaluateFailed(evaluateFailed);
                    } catch (final Throwable evaluateFailedFailed) {
                      chain.add(evaluateFailedFailed);
                    }
                  }

                  try {
                    reset(); // compensates for initialize()
                  } catch (final Throwable resetFailed) {
                    chain.add(resetFailed);
                    try {
                      resetFailed(resetFailed);
                    } catch (final Throwable resetFailedFailed) {
                      chain.add(resetFailedFailed);
                    }
                  }

                } catch (final Throwable initializeFailed) {
                  chain.add(initializeFailed);
                  try {
                    initializeFailed(initializeFailed);
                  } catch (final Throwable initializeFailedFailed) {
                    chain.add(initializeFailedFailed);
                  }
                }

                try {
                  disconnect(); // compensates for connect()
                } catch (final Throwable disconnectFailed) {
                  chain.add(disconnectFailed);
                  try {
                    disconnectFailed(disconnectFailed);
                  } catch (final Throwable disconnectFailedFailed) {
                    chain.add(disconnectFailedFailed);
                  }
                }

              } catch (final Throwable connectFailed) {
                chain.add(connectFailed);
                try {
                  connectFailed(connectFailed);
                } catch (final Throwable connectFailedFailed) {
                  chain.add(connectFailedFailed);
                }
              }

              try {
                destroy(); // compensates for create()
              } catch (final Throwable destroyFailed) {
                chain.add(destroyFailed);
                try {
                  destroyFailed(destroyFailed);
                } catch (final Throwable destroyFailedFailed) {
                  chain.add(destroyFailedFailed);
                }
              }

            } catch (final Throwable createFailed) {
              chain.add(createFailed);
              try {
                createFailed(createFailed);
              } catch (final Throwable createFailedFailed) {
                chain.add(createFailedFailed);
              }
            } finally {
              testInstance = null;
              testClass = null;
              DBRule.this.description = null;
              if (chain.getCause() != null || chain.size() > 1) {
                throw chain;
              }
            }
          }
        };
    }
    return returnValue;
  }

  private static final Object getTest(Statement statement) throws Exception {
    Object test = null;
    if (statement != null) {
      if (statement instanceof ExpectException) {
        final Field fNext = ExpectException.class.getDeclaredField("fNext");
        assertNotNull(fNext);
        fNext.setAccessible(true);
        statement = (Statement)fNext.get(statement);
        fNext.setAccessible(false);
      }
      try {
        final Field fTarget = statement.getClass().getDeclaredField("fTarget");
        assertNotNull(fTarget);
        fTarget.setAccessible(true);
        test = fTarget.get(statement);
        fTarget.setAccessible(false);
      } catch (final Exception ohWell) {

      }
    }
    return test;
  }

  public static interface DBManager {

    public void create(final ConnectionDescriptor cd) throws Exception;

    public void createFailed(final Throwable createFailed) throws Exception;

    public void connect() throws Exception;

    public void inject(final Object testInstance, final Description description) throws Exception;

    public void connectFailed(final Throwable connectFailed) throws Exception;

    public void initialize() throws Exception;

    public void initializeFailed(final Throwable initializeFailed) throws Exception;

    public void evaluateSucceeded() throws Exception;

    public void evaluateFailed(final Throwable evaluateFailed) throws Exception;

    public void reset() throws Exception;

    public void resetFailed(final Throwable resetFailed) throws Exception;

    public void disconnect() throws Exception;

    public void disconnectFailed(final Throwable disconnectFailed) throws Exception;

    public void destroy() throws Exception;

    public void destroyFailed(final Throwable destroyFailed) throws Exception;

  }

  public static class AbstractDBManager implements DBManager {
    
    public AbstractDBManager() {
      super();
    }

    @Override
    public void create(final ConnectionDescriptor cd) throws Exception {

    }

    @Override
    public void createFailed(final Throwable createFailed) throws Exception {

    }

    @Override
    public void connect() throws Exception {

    }

    @Override
    public void inject(final Object testInstance, final Description description) {

    }

    @Override
    public void connectFailed(final Throwable connectFailed) throws Exception {

    }

    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void initializeFailed(final Throwable initializeFailed) throws Exception {

    }

    @Override
    public void evaluateSucceeded() throws Exception {

    }

    @Override
    public void evaluateFailed(final Throwable evaluateFailed) throws Exception {

    }

    @Override
    public void reset() throws Exception {

    }

    @Override
    public void resetFailed(final Throwable resetFailed) throws Exception {

    }

    @Override
    public void disconnect() throws Exception {

    }

    @Override
    public void disconnectFailed(final Throwable disconnectFailed) throws Exception {

    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void destroyFailed(final Throwable destroyFailed) throws Exception {

    }

  }

}
