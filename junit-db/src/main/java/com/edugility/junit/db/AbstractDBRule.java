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
package com.edugility.junit.db;

import java.sql.Connection;

import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;

import java.util.concurrent.CopyOnWriteArrayList;

import com.edugility.throwables.ThrowableChain;

import org.junit.rules.TestRule;

import org.junit.runner.Description;

import org.junit.runners.model.Statement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractDBRule implements TestRule {

  public static final Listener[] EMPTY_LISTENER_ARRAY = new Listener[0];

  private Collection<Listener> listeners;

  private String catalog;

  private String schema;

  private String username;

  private String password;

  protected AbstractDBRule(final String catalog, final String schema, final String username, final String password) {
    super();
    this.setCatalog(catalog);
    this.setSchema(schema);
    this.setUsername(username);
    this.setPassword(password);
  }

  public void addListener(final Listener listener) {
    if (listener != null) {
      if (this.listeners == null) {
        this.listeners = new CopyOnWriteArrayList<Listener>();
      }
      this.listeners.add(listener);
    }
  }

  public void removeListener(final Listener listener) {
    if (listener != null && this.listeners != null && !this.listeners.isEmpty()) {
      this.listeners.remove(listener);
    }
  }

  public Listener[] getListeners() {
    if (listeners == null || listeners.isEmpty()) {
      return EMPTY_LISTENER_ARRAY;
    }
    return this.listeners.toArray(new Listener[this.listeners.size()]);
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getUsername() {
    return this.username;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public String getPassword() {
    return this.password;
  }

  public void setCatalog(final String catalog) {
    this.catalog = catalog;
  }
  
  public String getCatalog() {
    return this.catalog;
  }

  public void setSchema(final String schema) {
    this.schema = schema;
  }
  
  public String getSchema() {
    return this.schema;
  }

  /**
   * Returns a {@link Connection} to use to connect to the database.
   * The {@link Connection} may be new, or cached.  No guarantees are
   * made about whether closing the {@link Connection} will shut down
   * the associated database.  Careful, safe tests will not
   * {@linkplain Connection#close() close} the returned {@link
   * Connection}.
   *
   * <p>Implementations of this method must not return {@code
   * null}.</p>
   *
   * @return a non-{@code null} {@link Connection}
   *
   * @exception Exception if an error occurs
   */
  public abstract Connection getConnection() throws Exception;

  @Override
  public Statement apply(final Statement base, final Description description) {
    final Statement returnValue;
    if (base == null) {
      returnValue = null;
    } else {
      returnValue = new Statement() {
          @Override
          public final void evaluate() throws ThrowableChain {
            final ThrowableChain chain = new ThrowableChain();

            preEvaluation(chain);

            try {

              base.evaluate();

            } catch (final Throwable boom) {
              chain.add(boom);
            } finally {

              postEvaluation(chain);

            }
            
          }
        };
    }
    return returnValue;
  }

  private final void preEvaluation(final ThrowableChain chain) throws ThrowableChain {
    if (chain == null) {
      throw new IllegalArgumentException("chain", new NullPointerException("chain == null"));
    }
    this.createDatabase(chain);
    this.connectDatabase(chain);
    this.initializeDatabase(chain);
  }

  private final void postEvaluation(final ThrowableChain chain) throws ThrowableChain {
    if (chain == null) {
      throw new IllegalArgumentException("chain", new NullPointerException("chain == null"));
    }
    try {
      this.reset();
      this.fireDatabaseReset();
    } catch (final Throwable boom) {
      chain.add(boom);
    } finally {
      try {
        this.disconnect();
        this.fireDatabaseDisconnected();
      } catch (final Throwable boom) {
        chain.add(boom);
      } finally {
        try {
          this.destroy();
          this.fireDatabaseDestroyed();
        } catch (final Throwable boom) {
          chain.add(boom);
        } finally {
          if (chain.size() > 1) {
            throw chain;
          }
        }
      }
    }
  }

  private final void createDatabase(final ThrowableChain chain) throws ThrowableChain {
    try {
      this.create();
      this.fireDatabaseCreated();
    } catch (final Throwable boom) {
      chain.add(boom);
      postEvaluation(chain);
    }
  }

  public void create() throws Exception {

  }

  private final void connectDatabase(final ThrowableChain chain) throws ThrowableChain {
    try {
      this.connect();
      this.fireDatabaseConnected();
    } catch (final Throwable boom) {
      chain.add(boom);
      postEvaluation(chain);
    }
  }

  public void connect() throws Exception {

  }

  private final void initializeDatabase(final ThrowableChain chain) throws ThrowableChain {
    try {
      this.initialize();
      this.fireDatabaseInitialized();
    } catch (final Throwable boom) {
      chain.add(boom);
      postEvaluation(chain);
    }
  }

  public void initialize() throws Exception {

  }

  public void reset() throws Exception {

  }

  public void disconnect() throws Exception {

  }

  public void destroy() throws Exception {

  }

  protected void fireDatabaseCreated() {
    if (this.listeners != null && !this.listeners.isEmpty()) {
      final Event event = new Event(this);
      for (final Listener l : this.listeners) {
        if (l != null) {
          l.databaseCreated(event);
        }
      }
    }
  }

  protected void fireDatabaseConnected() {
    if (this.listeners != null && !this.listeners.isEmpty()) {
      final Event event = new Event(this);
      for (final Listener l : this.listeners) {
        if (l != null) {
          l.databaseConnected(event);
        }
      }
    }
  }

  protected void fireDatabaseInitialized() {
    if (this.listeners != null && !this.listeners.isEmpty()) {
      final Event event = new Event(this);
      for (final Listener l : this.listeners) {
        if (l != null) {
          l.databaseInitialized(event);
        }
      }
    }
  }

  protected void fireDatabaseReset() {
    if (this.listeners != null && !this.listeners.isEmpty()) {
      final Event event = new Event(this);
      for (final Listener l : this.listeners) {
        if (l != null) {
          l.databaseReset(event);
        }
      }
    }
  }

  protected void fireDatabaseDisconnected() {
    if (this.listeners != null && !this.listeners.isEmpty()) {
      final Event event = new Event(this);
      for (final Listener l : this.listeners) {
        if (l != null) {
          l.databaseDisconnected(event);
        }
      }
    }
  }

  protected void fireDatabaseDestroyed() {
    if (this.listeners != null && !this.listeners.isEmpty()) {
      final Event event = new Event(this);
      for (final Listener l : this.listeners) {
        if (l != null) {
          l.databaseDestroyed(event);
        }
      }
    }
  }

  public static class Event extends EventObject {

    private static final long serialVersionUID = 1L;

    public Event(final AbstractDBRule source) {
      super(source);
    }

    @Override
    public AbstractDBRule getSource() {
      return (AbstractDBRule)super.getSource();
    }

  }

  public static interface Listener extends EventListener {

    public void databaseCreated(final Event event);

    public void databaseConnected(final Event event);

    public void databaseInitialized(final Event event);

    public void databaseReset(final Event event);

    public void databaseDisconnected(final Event event);

    public void databaseDestroyed(final Event event);

  }

  public static class AbstractListener implements Listener {

    public AbstractListener() {
      super();
    }

    @Override
    public void databaseCreated(final Event event) {

    }

    @Override
    public void databaseConnected(final Event event) {

    }

    @Override
    public void databaseInitialized(final Event event) {

    }

    @Override
    public void databaseReset(final Event event) {

    }

    @Override
    public void databaseDisconnected(final Event event) {

    }

    @Override
    public void databaseDestroyed(final Event event) {

    }


  }


}