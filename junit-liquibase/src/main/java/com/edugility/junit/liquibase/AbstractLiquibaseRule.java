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

import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Collections;
import java.util.Enumeration;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

import com.edugility.junit.db.ConnectionDescriptor;

import liquibase.Liquibase;

import liquibase.database.jvm.JdbcConnection;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;

import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.junit.Assert;

import org.junit.rules.TestRule;

import org.junit.runner.Description;

import org.junit.runners.model.Statement;

public abstract class AbstractLiquibaseRule implements TestRule {

  protected transient Logger logger;

  private String username;

  private String password;

  private String schema;

  private String[] contexts;

  private String changeLogResourceName;

  public AbstractLiquibaseRule(final ConnectionDescriptor cd, final String changeLogResourceName, final String... changeLogContexts) {
    this(cd == null ? null : cd.getUsername(),
         cd == null ? null : cd.getPassword(),
         cd == null ? null : cd.getSchema(),
         changeLogResourceName,
         changeLogContexts);
  }

  public AbstractLiquibaseRule(final String username, final String password, final String schema, final String changeLogResourceName, final String... changeLogContexts) {
    super();
    final Logger logger = this.createLogger();
    if (logger == null) {
      this.logger = Logger.getLogger(this.getClass().getName());
    } else {
      this.logger = logger;
    }
    this.setUsername(username);
    this.setPassword(password);
    this.setSchema(schema);
    if (changeLogResourceName == null) {
      this.setChangeLogResourceName("changelog.xml");
    } else {
      this.setChangeLogResourceName(changeLogResourceName);
    }
    this.setChangeLogContexts(changeLogContexts);
  }

  protected Logger createLogger() {
    return Logger.getLogger(this.getClass().getName());
  }
  
  @Override
  public Statement apply(final Statement base, final Description description) {
    if (this.logger != null && this.logger.isLoggable(Level.FINER)) {
      this.logger.entering(this.getClass().getName(), "apply", new Object[] { base, description });
    }
    final Statement returnValue;
    if (base == null) {
      returnValue = null;
    } else {
      returnValue = new Statement() {
          @Override
          public final void evaluate() throws Throwable {
            update();
            base.evaluate();
          }
        };
    }
    if (this.logger != null && this.logger.isLoggable(Level.FINER)) {
      this.logger.exiting(this.getClass().getName(), "apply", returnValue);
    }
    return returnValue;
  }

  public String getSchema() {
    return this.schema;
  }

  public void setSchema(final String schema) {
    this.schema = schema;
  }

  public String getChangeLogResourceName() {
    return this.changeLogResourceName;
  }

  public void setChangeLogResourceName(final String changeLogResourceName) {
    this.changeLogResourceName = changeLogResourceName;
  }

  public String[] getChangeLogContexts() {
    return this.contexts;
  }

  public void setChangeLogContexts(final String... changeLogContexts) {
    this.contexts = changeLogContexts;
  }

  /**
   * Runs a <a href="http://liquibase.org/">Liquibase</a> <a
   * href="http://www.liquibase.org/manual/update">update</a>
   * operation.
   *
   * <p>This method performs the following steps in order:</p>
   *
   * <ol>
   *
   * <li>Gather data needed for the <a
   * href="http://www.liquibase.org/manual/update">Liquibase
   * update</a> operation.  Notable here is that if {@code null} was
   * originally supplied as the value for the {@link
   * #getChangeLogResourceName() changeLogResourceName} property, then
   * {@code changelog.xml} will be used instead.</li>
   *
   * <li>Create a {@link ResourceAccessor} by calling the {@link
   * #createResourceAccessor()} method.  By default, a {@link
   * CompositeResourceAccessor} will be returned that evaluates
   * resources against the filesystem, then the classpath, then as
   * URLs.</li>
   *
   * <li>Using the {@link ResourceAccessor}, attempt to open a
   * connection to the changelog resource to see if it exists.  If it
   * does not exist, this method effectively returns.</li>
   *
   * <li>Get a {@link Connection} to the appropriate {@link Database}
   * by calling the {@link #getConnection()} method.  <strong>This
   * {@link Connection} will be {@linkplain Connection#close() closed}
   * by this method in all cases.</strong></li>
   *
   * <li>Assemble the {@linkplain #getChangeLogContexts() changelog
   * contexts} into a single comma-delimited {@link String}.</li>
   *
   * <li>Create a new {@link Liquibase} instance from the data that
   * has been gathered so far.</li>
   *
   * <li>Invoke the {@link Liquibase#update(String)} method.</li>
   *
   * <li>{@linkplain Liquibase#forceReleaseLocks() Forcibly release
   * any database locks acquired by the <tt>Liquibase</tt>
   * instance}.</li>
   *
   * </ol>
   *
   * @exception DatabaseException if an error occurs while trying to
   * determine the appropriate {@link Database} class to use
   *
   * @exception LiquibaseException if an error occurs with the {@link
   * Liquibase} instance creation or the invocation of its {@link
   * Liquibase#update(String)} method
   *
   * @exception SQLException if an error occurs while allocating a
   * {@link Connection} to the database
   */
  public void update() throws DatabaseException, LiquibaseException, SQLException {
    if (this.logger != null && this.logger.isLoggable(Level.FINER)) {
      this.logger.entering(this.getClass().getName(), "update");
    }

    String changeLogResourceName = this.getChangeLogResourceName();
    if (changeLogResourceName == null || changeLogResourceName.isEmpty()) {
      changeLogResourceName = "changelog.xml";
    }

    ResourceAccessor resourceAccessor = this.createResourceAccessor();
    if (resourceAccessor == null) {
      resourceAccessor = new CompositeResourceAccessor(new FileSystemResourceAccessor(), new ClassLoaderResourceAccessor(), new URLResourceAccessor());
    }    

    if (this.changeLogExists(changeLogResourceName, resourceAccessor)) {

      final Connection connection = this.getConnection();

      try {

        final Database database = findCorrectDatabaseImplementation(connection);
        Assert.assertNotNull(database);

        final String schema = this.getSchema();
        if (schema != null) {
          database.setDefaultSchemaName(schema);
        }

        final String changeLogContext;
        final String[] changeLogContexts = this.getChangeLogContexts();
        if (changeLogContexts == null || changeLogContexts.length <= 0) {
          changeLogContext = null;
        } else {
          final StringBuilder sb = new StringBuilder();
          for (int i = 0; i < changeLogContexts.length; i++) {
            final String context = changeLogContexts[i];
            if (context != null) {
              sb.append(context);
              if (i + 1 < changeLogContexts.length) {
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

        final Liquibase liquibase = new Liquibase(changeLogResourceName, resourceAccessor, database);
        try {
          liquibase.update(changeLogContext);
        } finally {
          liquibase.forceReleaseLocks();
        }

      } finally {
        if (connection != null) {
          try {
            connection.close();
          } catch (final SQLException whatever) {
            
          }
        }
      }

    } else if (this.logger != null && this.logger.isLoggable(Level.FINE)) {
      this.logger.logp(Level.FINE, this.getClass().getName(), "update", "The changelog resource {0} does not exist", changeLogResourceName);
    }

    if (this.logger != null && this.logger.isLoggable(Level.FINER)) {
      this.logger.exiting(this.getClass().getName(), "update");
    }
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
  private final boolean changeLogExists(final String changeLogResourceName, final ResourceAccessor resourceAccessor) {
    if (this.logger != null && this.logger.isLoggable(Level.FINER)) {
      this.logger.entering(this.getClass().getName(), "changeLogExists", new Object[] { changeLogResourceName, resourceAccessor });
    }
    boolean returnValue = false;
    if (changeLogResourceName != null && resourceAccessor != null) {
      InputStream stream = null;
      try {
        stream = resourceAccessor.getResourceAsStream(changeLogResourceName);
        returnValue = stream != null;
      } catch (final IOException logMe) {
        returnValue = false;
        if (this.logger != null && this.logger.isLoggable(Level.SEVERE)) {
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
    if (this.logger != null && this.logger.isLoggable(Level.FINER)) {
      this.logger.exiting(this.getClass().getName(), "changeLogExists", Boolean.valueOf(returnValue));
    }
    return returnValue;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public abstract Connection getConnection() throws SQLException;

  public ResourceAccessor createResourceAccessor() {
    return new CompositeResourceAccessor(new FileSystemResourceAccessor(), new ClassLoaderResourceAccessor(), new URLResourceAccessor());
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

  public static class URLResourceAccessor implements ResourceAccessor {
    
    public URLResourceAccessor() {
      super();
    }
    
    @Override
    public InputStream getResourceAsStream(final String resourceName) throws IOException {
      try {
        return new URL(resourceName).openStream();
      } catch (final MalformedURLException ignore) {
        return null;
      }
    }
    
    @Override
    public Enumeration<URL> getResources(final String packageName) throws IOException {
      final ClassLoader classLoader = this.toClassLoader();
      if (classLoader != null) {
        return classLoader.getResources(packageName);
      }
      return Collections.enumeration(Collections.<URL>emptySet());
    }
    
    @Override
    public ClassLoader toClassLoader() {
      return Thread.currentThread().getContextClassLoader();
    }
    
  }
  
}
