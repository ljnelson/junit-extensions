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
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;

import static org.junit.Assert.*;

public class DataSourceH2Rule extends AbstractH2Rule {

  private String dataSourceName;

  private Context context;

  public DataSourceH2Rule(final String dataSourceName, final String catalog, final String schema, final String username, final String password, final boolean shutdown) {
    super(catalog, schema, username, password, shutdown);
    this.setDataSourceName(dataSourceName);
  }

  protected Context getContext() throws NamingException {
    return this.context;
  }

  private void setContext(final Context context) throws NamingException {
    this.context = context;
  }

  @Override
  protected Connection createConnection() throws NamingException, SQLException {
    Connection returnValue = null;
    final String dataSourceName = this.getDataSourceName();
    if (dataSourceName == null) {
      throw new IllegalStateException("getDataSourceName() == null");
    }
    Context context = this.getContext();
    if (context == null) {
      this.setContext(new InitialContext());
      context = this.getContext();
    }
    assertNotNull(context);
    final DataSource dataSource = (DataSource)context.lookup(dataSourceName);
    assertNotNull(dataSource);
    final String user = this.getUsername();
    if (user != null) {
      returnValue = dataSource.getConnection(user, this.getPassword());
    } else {
      returnValue = dataSource.getConnection();
    }
    return returnValue;
  }

  @Override
  public void reset() throws Exception {
    try {
      final Context context = this.getContext();
      if (context != null) {
        context.close();
        this.setContext(null);
      }
    } finally {
      super.reset();
    }
  }
  
  protected String getDataSourceName() {
    return this.dataSourceName;
  }

  private final void setDataSourceName(final String dataSourceName) {
    this.dataSourceName = dataSourceName;
  }

}