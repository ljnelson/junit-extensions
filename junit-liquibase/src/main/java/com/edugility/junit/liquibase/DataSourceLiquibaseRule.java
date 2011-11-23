/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
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

import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import liquibase.database.Database; // for javadoc only

/**
 * An {@link AbstractLiquibaseRule} that {@linkplain #getConnection()
 * obtains its <tt>Connection</tt>s} from a {@link
 * javax.sql.DataSource}.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @version 1.0
 *
 * @since 1.0
 */
public class DataSourceLiquibaseRule extends AbstractLiquibaseRule {

  /**
   * The {@link javax.sql.DataSource} to use to {@linkplain
   * DataSource#getConnection() obtain <tt>Conection</tt>s}.  This
   * field is never {@code null}.
   */
  private DataSource dataSource;

  /**
   * Creates a new {@link DataSourceLiquibaseRule}.
   *
   * @param dataSource the {@link javax.sql.DataSource} to use to
   * {@linkplain DataSource#getConnection() obtain
   * <tt>Connection</tt>s}; must not be {@code null}
   *
   * @param username the user name to use when connecting to a
   * database; may be {@code null}
   *
   * @param password the password to use when connecting to a
   * database; may be {@code null}
   *
   * @param schema the default schema name to be {@linkplain
   * Database#setDefaultSchemaName(String) supplied to the Liquibase
   * <tt>Database</tt> instance}; may be {@code null}
   *
   * @param changeLogResourceName the name of the changelog resource
   * that Liquibase will use to operate on the database; if {@code
   * null}, then this class will behave as though {@code
   * changelog.xml} had been supplied instead
   *
   * @param changeLogContexts any changelog contexts to supply to
   * Liquibase; may be {@code null} or empty
   *
   * @exception IllegalArgumentException if the {@code dataSource}
   * parameter is {@code null}
   */
  public DataSourceLiquibaseRule(final DataSource dataSource, final String username, final String password, final String schema, final String changeLogResourceName, final String... changeLogContexts) {
    super(username, password, schema, changeLogResourceName, changeLogContexts);
    this.setDataSource(dataSource);
  }

  /**
   * Creates a new {@link DataSourceLiquibaseRule}.
   *
   * @param url a {@link String} representation of a JDBC URL that
   * will be used to construct a new {@link DriverManagerDataSource};
   * must not be {@code null}
   *
   * @param username the user name to use when connecting to a
   * database; may be {@code null}
   *
   * @param password the password to use when connecting to a
   * database; may be {@code null}
   *
   * @param schema the default schema name to be {@linkplain
   * Database#setDefaultSchemaName(String) supplied to the Liquibase
   * <tt>Database</tt> instance}; may be {@code null}
   *
   * @param changeLogResourceName the name of the changelog resource
   * that Liquibase will use to operate on the database; if {@code
   * null}, then this class will behave as though {@code
   * changelog.xml} had been supplied instead
   *
   * @param changeLogContexts any changelog contexts to supply to
   * Liquibase; may be {@code null} or empty
   *
   * @exception IllegalArgumentException if the {@code url}
   * parameter is {@code null}
   */
  public DataSourceLiquibaseRule(final String url, final String username, final String password, final String schema, final String changeLogResourceName, final String... changeLogContexts) {
    super(username, password, schema, changeLogResourceName, changeLogContexts);
    this.setDataSource(new DriverManagerDataSource(url));
  }

  /**
   * Sets the {@link javax.sql.DataSource} to be used by this {@link
   * DataSourceLiquibaseRule}.
   *
   * @param dataSource the new {@link javax.sql.DataSource}; must not
   * be {@code null}
   *
   * @exception IllegalArgumentException if the supplied {@link
   * DataSource} is {@code null}
   */
  public void setDataSource(final DataSource dataSource) {
    if (dataSource == null) {
      throw new IllegalArgumentException("dataSource", new NullPointerException("dataSource == null"));
    }
    this.dataSource = dataSource;
  }

  /**
   * Returns the {@link javax.sql.DataSource} used by this {@link
   * DataSourceLiquibaseRule} to {@linkplain #getConnection() allocate
   * <tt>Connection</tt>s}.  This method never returns {@code null}.
   *
   * @return the {@link javax.sql.DataSource} used by this [@link
   * DataSourceLiquibaseRule} to {@linkplain #getConnection() allocate
   * <tt>Connection</tt>s}; never {@code null}
   */
  public DataSource getDataSource() {
    return this.dataSource;
  }

  /**
   * Returns a {@link Connection} as allocated by the {@linkplain
   * #getDataSource() underlying <tt>DataSource</tt>}.  This method
   * never returns {@code null}.
   *
   * @return a {@link Connection}; never {@code null}
   */
  @Override
  public Connection getConnection() throws SQLException {
    final DataSource dataSource = this.getDataSource();
    if (dataSource == null) {
      throw new IllegalStateException("getDataSource() == null");
    }
    final String username = this.getUsername();
    if (username == null) {
      return dataSource.getConnection();
    } else {
      return dataSource.getConnection(username, this.getPassword());
    }
  }

  /**
   * A general-purpose class that implements the {@link
   * javax.sql.DataSource} interface in terms of {@link DriverManager}
   * operations.
   *
   * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
   *
   * @version 1.0
   *
   * @since 1.0
   */
  public static final class DriverManagerDataSource implements DataSource {
    
    /**
     * The JDBC URL, in {@link String} form, that this {@link
     * DriverManagerDataSource} will use to connect to a database.
     * This field is never {@code null}.
     */
    private final String url;

    /**
     * The user name that the underlying {@link DriverManager} will
     * use to obtain a {@link Connection}.  This field may be {@code
     * null}.
     */
    private final String username;

    /**
     * The password that the underlying {@link DriverManager} will use
     * to obtain a {@link Connection}.  This field may be {@code
     * null}.
     */
    private final String password;

    /**
     * Creates a new {@link DriverManagerDataSource} with no user name
     * or password.
     *
     * @param url the JDBC URL, in {@link String} form, that this
     * {@link DriverManagerDataSource} will use to connect to a
     * database.  The value of this parameter must not be {@code
     * null}.
     *
     * @exception IllegalArgumentException if {@code url} is {@code
     * null}
     */
    public DriverManagerDataSource(final String url) {
      this(url, null, null);
    }

    /**
     * Creates a new {@link DriverManagerDataSource} that will use the
     * supplied {@code username} to obtain {@link Connection}s, but no
     * password.
     *
     * @param url the JDBC URL, in {@link String} form, that this
     * {@link DriverManagerDataSource} will use to connect to a
     * database.  The value of this parameter must not be {@code
     * null}.
     *
     * @param username the user name that this {@link
     * DriverManagerDataSource} will use to connect to a database.
     * The value of this parameter may be {@code null}.
     *
     * @exception IllegalArgumentException if {@code url} is {@code
     * null}
     */
    public DriverManagerDataSource(final String url, final String username) {
      this(url, username, null);
    }

    /**
     * Creates a new {@link DriverManagerDataSource} that will use the
     * supplied {@code username} and {@code password} to obtain {@link Connection}s, but no
     * password.
     *
     * @param url the JDBC URL, in {@link String} form, that this
     * {@link DriverManagerDataSource} will use to connect to a
     * database.  The value of this parameter must not be {@code
     * null}.
     *
     * @param username the user name that this {@link
     * DriverManagerDataSource} will use to connect to a database.
     * The value of this parameter may be {@code null}.
     *
     * @param password the password that this {@link
     * DriverManagerDataSource} will use to connect to a database.
     * The value of this parameter may be {@code null}.
     *
     * @exception IllegalArgumentException if {@code url} is {@code
     * null}
     */
    public DriverManagerDataSource(final String url, final String username, final String password) {
      super();
      this.url = url;
      this.username = username;
      this.password = password;
      if (url == null) {
        throw new IllegalArgumentException("url", new NullPointerException("url == null"));
      }
    }

    /**
     * Returns the return value of the {@link
     * DriverManager#getLoginTimeout()} method.
     *
     * @return the return value of the {@link
     * DriverManager#getLoginTimeout()} method
     */
    @Override
    public final int getLoginTimeout() {
      return DriverManager.getLoginTimeout();
    }

    /**
     * Calls the {@link DriverManager#setLoginTimeout(int)} method
     * supplying it with the value of the supplied {@code timeout}
     * parameter.
     *
     * @param timeout the timeout value
     */
    @Override
    public final void setLoginTimeout(final int timeout) {
      DriverManager.setLoginTimeout(timeout);
    }

    /**
     * Returns the return value of the {@link
     * DriverManager#getLogWriter()} method.  This method may return
     * {@code null}.
     *
     * @return the return value of the {@link
     * DriverManager#getLogWriter()} method, or {@code null}
     */
    @Override
    public final PrintWriter getLogWriter() {
      return DriverManager.getLogWriter();
    }

    /**
     * Calls the {@link DriverManager#setLogWriter(PrintWriter)}
     * method, supplying it with the value of the supplied {@code
     * writer} parameter.
     *
     * @param writer the {@link PrintWriter} to which the underlying
     * {@link DriverManager} will log; may be {@code null}
     */
    @Override
    public final void setLogWriter(final PrintWriter writer) {
      DriverManager.setLogWriter(writer);
    }

    /**
     * Returns a newly-allocated {@link Connection} by invoking either
     * the {@link DriverManager#getConnection(String)} or the {@link
     * #getConnection(String, String)} method, depending on whether a
     * non-{@code null} {@code username} was supplied to this {@link
     * DriverManagerDataSource}'s {@link
     * #DataSourceLiquibaseRule.DriverManagerDataSource(String,
     * String, String) constructor}.
     *
     * <p>This method never returns {@code null}.</p>
     *
     * @return the return value of either the {@link
     * DriverManager#getConnection(String)} method or the {@link
     * #getConnection(String, String)} method; never {@code null}
     *
     * @exception SQLException if a {@link Connection} could not be
     * allocated
     */
    @Override
    public final Connection getConnection() throws SQLException {
      if (this.username != null) {
        return this.getConnection(this.username, this.password);
      } else {
        return DriverManager.getConnection(url);
      }
    }

    /**
     * Returns the return value of the {@link
     * DriverManager#getConnection(String, String, String)} method.
     *
     * <p>This method never returns {@code null}.</p>
     *
     * @param username the user name; may be {@code null}
     *
     * @param password the password; may be {@code null}
     *
     * @return the return value of the {@link
     * DriverManager#getConnection(String, String, String)} method;
     * never {@code null}
     *
     * @exception SQLException if a {@link Connection} could not be
     * allocated
     */
    @Override
    public final Connection getConnection(final String username, final String password) throws SQLException {
      return DriverManager.getConnection(url, username, password);
    }

    /**
     * Returns {@code false} in all cases.
     *
     * @param cls the {@link Class} for which this {@link
     * DriverManagerDataSource} might be a wrapper; this parameter is
     * ignored
     *
     * @return {@code false} in all cases
     */
    @Override
    public final boolean isWrapperFor(final Class<?> cls) {
      return false;
    }

    /**
     * Throws a new {@link SQLException} if invoked.
     *
     * @param cls the {@link Class} for which this {@link
     * DriverManagerDataSource} might be a wrapper; this parameter is
     * ignored
     *
     * @return this {@link DriverManagerDataSource} as an instance of
     * the supplied {@link Class}, but this method always throws a
     * {@link SQLException}
     *
     * @exception SQLException if invoked
     */
    @Override
    public final <T> T unwrap(final Class<T> cls) throws SQLException {
      throw new SQLException(new UnsupportedOperationException("unwrap"));
    }

  }
  
}