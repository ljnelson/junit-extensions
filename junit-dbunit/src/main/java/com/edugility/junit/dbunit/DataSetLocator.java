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

import java.io.Serializable;

import java.net.URL;

import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.DataSetException;

import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

import org.junit.runner.Description;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSetLocator implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(DataSetLocator.class);

  public DataSetLocator() {
    super();
  }

  public IDataSet findDataSet(final Description description) throws DataSetException {
    if (logger.isDebugEnabled()) {
      logger.debug("findDataSet() - start"); // we follow the dubious formatting and level choices of DBUnit here
    }
    if (description == null) {
      throw new IllegalArgumentException("description", new NullPointerException("description == null"));
    }

    String classname = description.getClassName();
    if (classname == null) {
      throw new IllegalArgumentException("description", new IllegalStateException("description.getClassName() == null"));
    }
    final int lastDotIndex = classname.lastIndexOf(".");
    if (lastDotIndex >= 0 && classname.length() >= lastDotIndex + 1) {
      classname = classname.substring(lastDotIndex + 1);
    }

    String classpathResourceName = null;
    URL dataSetUrl = null;
    final String methodName = description.getMethodName();
    if (methodName == null) {
      classpathResourceName = String.format("/datasets/%s.xml", classname);
      dataSetUrl = this.getClass().getResource(classpathResourceName);
    } else {
      classpathResourceName = String.format("/datasets/%s/%s.xml", classname, methodName);
      dataSetUrl = this.getClass().getResource(classpathResourceName);
      if (dataSetUrl == null) {
        classpathResourceName = String.format("/datasets/%s.xml", classname);
        dataSetUrl = this.getClass().getResource(classpathResourceName);
      }
    }
    if (logger.isInfoEnabled()) {
      logger.info("IDataSet URL: {} (from {})", dataSetUrl, classpathResourceName);
    }

    final IDataSet returnValue = this.buildDataSet(dataSetUrl);
    return returnValue;
  }

  public IDataSet buildDataSet(final URL dataSetURL) throws DataSetException {
    final IDataSet returnValue;
    if (dataSetURL == null) {
      returnValue = new DefaultDataSet();
    } else {
      returnValue = new FlatXmlDataSetBuilder().build(dataSetURL);
    }
    return returnValue;
  }

}