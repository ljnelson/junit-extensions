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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DriverManagerLiquibaseRule extends AbstractLiquibaseRule {

  private String url;

  public DriverManagerLiquibaseRule(final String url, final String username, final String password, final String schema, final String changeLogResourceName, final String... changeLogContexts) {
    super(username, password, schema, changeLogResourceName, changeLogContexts);
    this.setConnectionURL(url);
  }

  public String getConnectionURL() {
    return this.url;
  }
  
  public void setConnectionURL(final String url) {
    this.url = url;
  }

  @Override
  public Connection getConnection() throws SQLException {
    final String connectionURL = this.getConnectionURL();
    if (connectionURL == null) {
      throw new IllegalStateException("getConnectionURL() == null");
    }
    final String username = this.getUsername();
    if (username == null) {
      return DriverManager.getConnection(connectionURL);
    } else {
      return DriverManager.getConnection(connectionURL, username, this.getPassword());
    }    
  }
  
}