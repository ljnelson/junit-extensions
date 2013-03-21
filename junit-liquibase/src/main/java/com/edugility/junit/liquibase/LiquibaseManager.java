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
package com.edugility.junit.liquibase;

import java.io.InputStream;
import java.io.IOException;

import java.sql.Connection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

import com.edugility.junit.db.ConnectionDescriptor;
import com.edugility.junit.db.DBRule.SingleDBManager;

import liquibase.Liquibase;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;

import liquibase.database.jvm.JdbcConnection;

import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;

import liquibase.resource.ResourceAccessor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LiquibaseManager extends SingleDBManager {

  private final String changeLogResourceName;

  private final ResourceAccessor accessor;

  private final String changeLogContext;

  private transient Database database;

  public LiquibaseManager(final ConnectionDescriptor cd, final ResourceAccessor accessor, final String... changeLogContexts) {
    this(cd, "changelog.xml", accessor, changeLogContexts);
  }

  public LiquibaseManager(final ConnectionDescriptor cd, final String changeLogResourceName, final ResourceAccessor accessor, final String... changeLogContexts) {
    super(cd);
    this.changeLogResourceName = changeLogResourceName;
    this.accessor = accessor;
    this.changeLogContext = joinChangeLogContexts(Arrays.asList(changeLogContexts));
  }

  @Override
  public void connect() throws Exception {
    super.connect();
    final Connection c = this.getAllocatedConnection();
    assertNotNull(c);
    this.database = findCorrectDatabaseImplementation(c);
    assertNotNull(this.database);
    final ConnectionDescriptor cd = this.getConnectionDescriptor();
    if (cd != null) {
      final String schema = cd.getSchema();
      if (schema != null) {
        this.database.setDefaultSchemaName(schema);
      }
    }
  }

  @Override
  public void initialize() throws Exception {
    assertNotNull(this.changeLogResourceName);
    assertNotNull(this.accessor);
    assertNotNull(this.database);
    assertTrue(changeLogExists(this.changeLogResourceName, this.accessor));
    final Liquibase liquibase = new Liquibase(this.changeLogResourceName, this.accessor, this.database);
    try {
      liquibase.update(this.changeLogContext);
    } finally {
      liquibase.forceReleaseLocks();
    }
  }

  private static final String joinChangeLogContexts(final Iterable<? extends String> changeLogContexts) {
    final String changeLogContext;
    if (changeLogContexts == null) {
      changeLogContext = null;
    } else {
      final Iterator<? extends String> iterator = changeLogContexts.iterator();
      if (iterator == null || !iterator.hasNext()) {
        changeLogContext = null;
      } else {
        final StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
          final String context = iterator.next();
          if (context != null) {
            sb.append(context);
            if (iterator.hasNext()) {
              sb.append(",");
            }
          }
        }
        if (sb.length() > 0) {
          changeLogContext = sb.toString();
        } else {
          changeLogContext = null;
        }
      }
    }
    return changeLogContext;
  }

  public static Database findCorrectDatabaseImplementation(final Connection connection) throws DatabaseException {
    Database database = null;
    if (connection != null) {
      final DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
      assert databaseFactory != null;
      database = databaseFactory.findCorrectDatabaseImplementation(new JdbcConnection(connection));
    }
    return database;
  }

  /**
   * Returns {@code true} if the specified {@code
   * changeLogResourceName} can be opened using the supplied {@link
   * ResourceAccessor}.
   *
   * <p>Any {@link IOException}s encountered by the supplied {@link
   * ResourceAccessor} will be {@linkplain Logger#log(LogRecord)
   * logged} by this {@link AbstractLiquibaseRule}'s {@link #logger
   * Logger} at the {@link Level#SEVERE SEVERE} level.</p>
   *
   * @param changeLogResourceName the name of a changelog resource;
   * may be {@code null} in which case {@code false} will be returned
   *
   * @param resourceAccessor the {@link ResourceAccessor} to use to
   * attempt to {@linkplain
   * ResourceAccessor#getResourceAsStream(String) open and immediately
   * close an <tt>InputStream</tt>} to the supplied {@code
   * changeLogResourceName}; may be {@code null} in which case {@code
   * false} will be returned
   *
   * @return {@code true} if the supplied {@code
   * changeLogResourceName} can be opened with the supplied {@link
   * ResourceAccessor}; {@code false} in all other cases
   */
  public final boolean changeLogExists(final String changeLogResourceName, final ResourceAccessor resourceAccessor) {
    final Logger logger = Logger.getLogger(this.getClass().getName());
    if (logger != null && logger.isLoggable(Level.FINER)) {
      logger.entering(this.getClass().getName(), "changeLogExists", new Object[] { changeLogResourceName, resourceAccessor });
    }
    boolean returnValue = false;
    if (changeLogResourceName != null && resourceAccessor != null) {
      InputStream stream = null;
      try {
        stream = resourceAccessor.getResourceAsStream(changeLogResourceName);
        returnValue = stream != null;
      } catch (final IOException logMe) {
        returnValue = false;
        if (logger != null && logger.isLoggable(Level.SEVERE)) {
          final LogRecord logRecord = new LogRecord(Level.SEVERE, "The changelog resource named {0} could not be opened as an InputStream.");
          logRecord.setThrown(logMe);
          logRecord.setParameters(new Object[] { changeLogResourceName, resourceAccessor });
          logRecord.setSourceClassName(this.getClass().getName());
          logRecord.setSourceMethodName("changeLogExists");
          logger.log(logRecord);
        }
      } finally {
        if (stream != null) {
          try {
            stream.close();
          } catch (final IOException ignore) {
            // ignore
          }
        }
      }
    }
    if (logger != null && logger.isLoggable(Level.FINER)) {
      logger.exiting(this.getClass().getName(), "changeLogExists", Boolean.valueOf(returnValue));
    }
    return returnValue;
  }

}
