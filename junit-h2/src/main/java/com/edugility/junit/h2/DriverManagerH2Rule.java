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

public class DriverManagerH2Rule extends AbstractH2Rule {

  public DriverManagerH2Rule(final String catalog, final String schema, final String username, final String password, final boolean shutdown) {
    super(catalog, schema, username, password, shutdown);
  }

  @Override
  public Connection getConnection() throws SQLException {
    String username = this.getUsername();
    if (username == null) {
      username = System.getProperty("testDatabaseUserName");
    }
    if (username != null) {
      String password = this.getPassword();
      if (password == null) {
        password = System.getProperty("testDatabasePassword");
      }
      return DriverManager.getConnection(this.getURL(), username, password);
    } else {
      return DriverManager.getConnection(this.getURL());
    }
  }

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