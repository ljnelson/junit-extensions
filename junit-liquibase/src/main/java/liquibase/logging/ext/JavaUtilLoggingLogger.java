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
package liquibase.logging.ext;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;

import liquibase.logging.LogLevel;

import liquibase.logging.core.AbstractLogger;

public class JavaUtilLoggingLogger extends AbstractLogger {

  public static final Map<LogLevel, Level> levels = new EnumMap<LogLevel, Level>(LogLevel.class);

  static {
    levels.put(LogLevel.OFF, Level.OFF);
    levels.put(LogLevel.DEBUG, Level.FINER);
    levels.put(LogLevel.INFO, Level.INFO);
    levels.put(LogLevel.WARNING, Level.WARNING);
    levels.put(LogLevel.SEVERE, Level.SEVERE);
  }

  private transient Logger logger;
  
  public JavaUtilLoggingLogger() {
    super();
  }
  
  @Override
  public final int getPriority() {
    return 7; // arbitrary number greater than 1
  }
  
  @Override
  public void setLogLevel(final LogLevel level) {
    super.setLogLevel(level);
    if (this.logger != null && level != null) {
      final Level l = levels.get(level);
      if (l == null) {
        this.logger.setLevel(Level.OFF);
      } else {
        this.logger.setLevel(l);
      }
    }
  }

  @Override
  public void setLogLevel(final String logLevel, final String fileName) {
    this.setLogLevel(logLevel); // emulates DefaultLogger behavior
  }
  
  @Override
  public LogLevel getLogLevel() {
    if (this.logger == null) {
      return super.getLogLevel();
    }
    final Level level = this.logger.getLevel();
    if (level == null) {
      return LogLevel.OFF;
    }
    final Set<Entry<LogLevel, Level>> entrySet = levels.entrySet();
    assert entrySet != null;
    assert !entrySet.isEmpty();
    for (final Entry<LogLevel, Level> entry : entrySet) {
      assert entry != null;
      if (level.equals(entry.getValue())) {
        return entry.getKey();
      }
    }
    throw new IllegalStateException();
  }
  
  @Override
  public void setName(final String name) {
    this.logger = Logger.getLogger(name);
  }
  
  @Override
  public void info(final String message) {
    this.info(message, null);
  }
  
  @Override
  public void info(final String message, final Throwable throwable) {
    if (this.logger != null && this.logger.isLoggable(Level.INFO)) {
      if (throwable == null) {
        this.logger.log(Level.INFO, message);
      } else {
        this.logger.log(Level.INFO, message, throwable);
      }
    }
  }
  
  @Override
  public void warning(final String message) {
    this.warning(message, null);
  }
  
  @Override
  public void warning(final String message, final Throwable throwable) {
    if (this.logger != null && this.logger.isLoggable(Level.WARNING)) {
      if (throwable == null) {
        this.logger.log(Level.WARNING, message);
      } else {
        this.logger.log(Level.WARNING, message, throwable);
      }
    }
  }

  @Override
  public void severe(final String message) {
    this.severe(message, null);
  }

  @Override
  public void severe(final String message, final Throwable throwable) {
    if (this.logger != null && this.logger.isLoggable(Level.SEVERE)) {
      if (throwable == null) {
        this.logger.log(Level.SEVERE, message);
      } else {
        this.logger.log(Level.SEVERE, message, throwable);
      }
    }
  }

  @Override
  public void debug(final String message) {
    this.debug(message, null);
  }

  @Override
  public void debug(final String message, final Throwable throwable) {
    if (this.logger != null && this.logger.isLoggable(Level.FINER)) {
      if (throwable == null) {
        this.logger.log(Level.FINER, message);
      } else {
        this.logger.log(Level.FINER, message, throwable);
      }
    }
  }
  

}