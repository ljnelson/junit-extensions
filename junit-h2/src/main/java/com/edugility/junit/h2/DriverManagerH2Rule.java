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
package com.edugility.junit.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.edugility.junit.db.ConnectionDescriptor;

/**
 * An {@link AbstractH2Rule} that uses {@link
 * DriverManager#getConnection(String, String, String)} to obtain a
 * {@link Connection} to an H2 in-memory database.
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @since 1.0-SNAPSHOT
 * 
 * @version 1.0-SNAPSHOT
 */
public class DriverManagerH2Rule extends AbstractH2Rule {

  /**
   * Creates a new {@link DriverManagerH2Rule}.
   *
   * @param catalog the catalog, which becomes the H2 database name;
   * may be {@code null}
   *
   * @param schema the default schema; may be {@code null}
   *
   * @param username the username to use to {@linkplain
   * DriverManager#getConnection(String, String, String) get
   * <tt>Connection</tt>s}; may be {@code null}
   *
   * @param password the password to use to {@linkplain
   * DriverManager#getConnection(String, String, String) get
   * <tt>Connection</tt>s}; may be {@code null}
   *
   * @param shutdown whether the database will be shut down during the
   * execution of the {@link #destroy()} method
   *
   * @see AbstractH2Rule#AbstractH2Rule(String, String, String,
   * String, boolean)
   */
  public DriverManagerH2Rule(final String catalog, final String schema, final String username, final String password, final boolean shutdown) {
    super(catalog, schema, username, password, shutdown);
  }

  /**
   * Creates a new {@link DriverManagerH2Rule}.
   *
   * @param descriptor the {@link ConnectionDescriptor} describing the
   * connection to the database to be established; must not be {@code
   * null}
   */
  public DriverManagerH2Rule(final ConnectionDescriptor descriptor, final boolean shutdown) {
    super(descriptor, shutdown);
  }
  
  /**
   * Returns a {@link Connection} as returned by the {@link
   * DriverManager#getConnection(String, String, String)} method.
   *
   * @return a {@link Connection}
   *
   * @exception SQLException if an error occurs
   *
   * @see #getURL()
   */
  @Override
  public Connection getConnection() throws SQLException {
    final String username = this.getUsername();
    if (username != null) {
      return DriverManager.getConnection(this.getURL(), username, this.getPassword());
    } else {
      return DriverManager.getConnection(this.getURL());
    }
  }

  /**
   * Returns a {@link String} representation of the URL used by this
   * {@link DriverManagerH2Rule} to pass to the {@link DriverManager}
   * class in order to obtain a {@link Connection} to an H2 database.
   *
   * <p>This implementation first checks for a {@linkplain
   * System#getProperties() <tt>System</tt> property} named {@code
   * testDatabaseCatalog}.  If one is found, then its {@link String}
   * value is used as the H2 database name.  If one is not found, then
   * {@code test} is used for the H2 database name.</p>
   *
   * <p>Next, this implementation checks for a {@linkplain
   * System#getProperties() <tt>System</tt> property} named {@code
   * testDatabaseInitCommand}.  If such a property is found, then
   * "{@code ;INIT=}" is prepended to it, and the resulting {@link
   * String} will be used as the H2 initialization command.</p>
   *
   * <p>If such a property is <em>not</em> found, then this {@link
   * DriverManagerH2Rule}'s {@link #getSchema()} method is called.  If
   * a non-{@code null}, non-{@linkplain String#isEmpty() empty}
   * {@link String} is returned, then a default initialization {@link
   * String} is assembled that reads:</p>
   *
   * <pre>;INIT=CREATE SCHEMA IF NOT EXISTS <em>schemaName</em></pre>
   *
   * <p>...where <em>schemaName</em> is replaced with the return value
   * of the {@link #getSchema()} method.</p>
   *
   * <p>If the return value of the {@link #getSchema()} method is
   * {@code null}, then no initialization {@link String} is produced
   * at all.</p>
   *
   * <p>Finally, a {@link String} is created out of the following
   * fragments in order:
   *
   * <ol>
   *
   * <li>The literal {@link String} "{@code jdbc:h2:mem:}"</li>
   *
   * <li>The catalog name (see above)</li>
   *
   * <li>The initialization string as produced above.</li>
   *
   * </ol>
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link String} representation of the JDBC URL used to
   * connect to the H2 database; never {@code null}
   */
  protected String getURL() {
    String url = null;
    String catalog = this.getCatalog();
    if (catalog == null) {
      catalog = System.getProperty("testDatabaseCatalog", "test").trim();
    }
    if (catalog.isEmpty()) {
      catalog = "test";
    }
    String initCommand = System.getProperty("testDatabaseInitCommand");
    if (initCommand == null) {
      String schema = this.getSchema();
      if (schema != null) {
        schema = schema.trim();
        if (!schema.isEmpty()) {
          initCommand = String.format("CREATE SCHEMA IF NOT EXISTS %s", schema);
        }
      }
    }
    if (initCommand == null) {
      url = String.format("jdbc:h2:mem:%s", catalog);
    } else {
      url = String.format("jdbc:h2:mem:%s;INIT=%s", catalog, initCommand);
    }
    return url;
  }

}