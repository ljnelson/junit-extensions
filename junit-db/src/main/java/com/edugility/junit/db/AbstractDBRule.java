/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright (c) 2010-2013 Edugility LLC.
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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.edugility.throwables.ThrowableChain;

import org.junit.rules.RuleChain; // for javadoc only
import org.junit.rules.TestRule;

import org.junit.runner.Description;

import org.junit.runners.model.Statement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * A {@link TestRule} that connects to a database, provides the
 * capability of setting up its environment, runs the test it wraps,
 * provides the capability of tearing down its environment, and
 * disconnects from the database, all while preserving {@link
 * Throwable}s encountered along the way.
 *
 * <p>The sequencing that an {@link AbstractDBRule} performs is more
 * robust than that performed by the JUnit {@link RuleChain} class,
 * and is designed so that no failure in any phase will prohibit
 * effective cleanup.</p>
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @version 1.0
 *
 * @since 1.0
 *
 * @see TestRule
 *
 * @see RuleChain
 *
 * @see <a href="http://github.com/ljnelson/edugility-throwables">the
 * <tt>edugility-throwables</tt> project on Github</a>
 */
public abstract class AbstractDBRule implements TestRule {

  /**
   * An empty array of {@link Listener}s.  This field is never {@code
   * null}.
   */
  public static final Listener[] EMPTY_LISTENER_ARRAY = new Listener[0];

  /**
   * A {@link Collection} of {@link Listener}s that will be notified
   * at certain points during this {@link AbstractDBRule}'s lifecycle.
   * This field may be {@code null} at any point.
   *
   * @see {@link #addListener(Listener)}
   */
  private Collection<Listener> listeners;

  /**
   * The database catalog to which this {@link AbstractDBRule} will
   * connect.  This field may be {@code null}.
   */
  private String catalog;

  /**
   * The database schema to which this {@link AbstractDBRule} will
   * connect.  This field may be {@code null}.
   */
  private String schema;

  /**
   * The user name used by this {@link AbstractDBRule} in order to
   * connect to a database.  This field may be {@code null}.
   */
  private String username;

  /**
   * The password used by this {@link AbstractDBRule} in order to
   * connect to a database.  This field may be {@code null}.
   */
  private String password;

  /**
   * The {@link Logger} used by this {@link AbstractDBRule} to log
   * messages.  This field is never {@code null}.
   */
  protected transient final Logger logger;

  /**
   * Creates a new {@link AbstractDBRule}.
   *
   * @param catalog the database catalog to which this {@link
   * AbstractDBRule} will connect; may be {@code null}
   *
   * @param schema the database schema to which this {@link
   * AbstractDBRule} will connect; may be {@code null}
   *
   * @param username the user name used by this {@link AbstractDBRule}
   * in order to connect to a database; may be {@code null}
   *
   * @param password the password used by this {@link AbstractDBRule}
   * in order to connect to a database; may be {@code null}
   */
  protected AbstractDBRule(final String catalog, final String schema, final String username, final String password) {
    super();
    final Logger logger = this.createLogger();
    if (logger == null) {
      this.logger = Logger.getLogger(this.getClass().getName());
    } else {
      this.logger = logger;
    }
    this.setCatalog(catalog);
    this.setSchema(schema);
    this.setUsername(username);
    this.setPassword(password);
  }

  /**
   * Creates a new {@link AbstractDBRule}.
   *
   * @param descriptor the {@link ConnectionDescriptor} providing
   * most&mdash;if not all&mdash;of the connection information; may be
   * {@code null}
   */
  protected AbstractDBRule(final ConnectionDescriptor descriptor) {
    super();
    final Logger logger = this.createLogger();
    if (logger == null) {
      this.logger = Logger.getLogger(this.getClass().getName());
    } else {
      this.logger = logger;
    }
    if (descriptor != null) {
      this.setCatalog(descriptor.getCatalog());
      this.setSchema(schema);
      this.setUsername(username);
      this.setPassword(password);
    }
  }

  /**
   * Creates a new {@link Logger} for use by this {@link
   * AbstractDBRule}.  This method never returns {@code null}, and any
   * overrides of it must not either.
   *
   * @return a new, non-{@code null} {@link Logger}
   */
  protected Logger createLogger() {
    return Logger.getLogger(this.getClass().getName());
  }

  /**
   * Adds the supplied {@link Listener} to this {@link AbstractDBRule}
   * so that it will be notified during this {@link AbstractDBRule}'s
   * lifecycle.
   *
   * @param listener the {@link Listener}; may be {@code null} in
   * which case no action will be taken
   */
  public void addListener(final Listener listener) {
    if (listener != null) {
      if (this.listeners == null) {
        this.listeners = new CopyOnWriteArrayList<Listener>();
      }
      this.listeners.add(listener);
    }
  }

  /**
   * Removes the supplied {@link Listener} from this {@link
   * AbstractDBRule}.
   *
   * @param listener the {@link Listener}; if {@code null} no action
   * will be taken
   */
  public void removeListener(final Listener listener) {
    if (listener != null && this.listeners != null && !this.listeners.isEmpty()) {
      this.listeners.remove(listener);
    }
  }

  /**
   * Returns an array of {@link Listener}s that are currently
   * monitoring this {@link AbstractDBRule}'s lifecycle events.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return an array of {@link Listener}s that are currently
   * monitoring this {@link AbstractDBRule}'s lifecycle events; never
   * {@code null}
   */
  public Listener[] getListeners() {
    if (listeners == null || listeners.isEmpty()) {
      return EMPTY_LISTENER_ARRAY;
    }
    return this.listeners.toArray(new Listener[this.listeners.size()]);
  }

  /**
   * Sets the user name that this {@link AbstractDBRule} may use in
   * connecting to a database.
   *
   * @param username the user name; may be {@code null}
   */
  public void setUsername(final String username) {
    this.username = username;
  }

  /**
   * Returns the user name that this {@link AbstractDBRule} may use in
   * connecting to a database.  This method may return {@code null}.
   *
   * @return the user name that this {@link AbstractDBRule} may use in
   * connecting to a database, or {@code null}
   */
  public String getUsername() {
    return this.username;
  }

  /**
   * Sets the password that this {@link AbstractDBRule} may use in
   * connecting to a database.
   *
   * @param password the password; may be {@code null}
   */
  public void setPassword(final String password) {
    this.password = password;
  }

  /**
   * Returns the password that this {@link AbstractDBRule} may use in
   * connecting to a database.  This method may return {@code null}.
   *
   * @return the password that this {@link AbstractDBRule} may use in
   * connecting to a database, or {@code null}
   */
  public String getPassword() {
    return this.password;
  }

  /**
   * Sets the database catalog that this {@link AbstractDBRule} may
   * use in connecting to a database.
   *
   * @param catalog the database catalog; may be {@code null}
   */
  public void setCatalog(final String catalog) {
    this.catalog = catalog;
  }
  
  /**
   * Returns the database catalog that this {@link AbstractDBRule} may
   * use in connecting to a database.  This method may return {@code
   * null}.
   *
   * @return the database catalog that this {@link AbstractDBRule} may
   * use in connecting to a database, or {@code null}
   */
  public String getCatalog() {
    return this.catalog;
  }

  /**
   * Sets the database schema that this {@link AbstractDBRule} may
   * use in connecting to a database.
   *
   * @param schema the database schema; may be {@code null}
   */
  public void setSchema(final String schema) {
    this.schema = schema;
  }
  
  /**
   * Returns the database schema that this {@link AbstractDBRule} may
   * use in connecting to a database.  This method may return {@code
   * null}.
   *
   * @return the database schema that this {@link AbstractDBRule} may
   * use in connecting to a database, or {@code null}
   */
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

  /**
   * Creates a new {@link Statement} that wraps the supplied {@link
   * Statement} and returns it.
   *
   * <p>The new {@link Statement} performs the following actions in
   * order:</p>
   *
   * <ol>
   *
   * <li>Creates a new {@link ThrowableChain} to store the results of
   * any {@link Throwable}s encountered during the test run, including
   * those otherwise shadowed by {@code finally} blocks and the
   * like.</li>
   *
   * <li>Invokes the following methods in order:</li>
   *
   * <ol>
   *
   * <li>{@link #create()}</li>
   *
   * <li>{@link #fireDatabaseCreated()}</li>
   *
   * <li>{@link #connect()}</li>
   *
   * <li>{@link #fireDatabaseConnected()}</li>
   *
   * <li>{@link #initialize()}</li>
   *
   * <li>{@link #fireDatabaseInitialized()}</li>
   *
   * <li>{@link Statement#evaluate() base.evaluate()}</li>
   *
   * </ol>
   *
   * <li>Catches any {@link Throwable}s encountered and {@linkplain
   * ThrowableChain#add(Throwable) adds them} to the {@link
   * ThrowableChain}.</li>
   *
   * <li>Invokes the following methods in order, catching any
   * encountered {@link Throwable}s along the way and adding them to
   * the {@link ThrowableChain} as well:</li>
   *
   * <ol>
   *
   * <li>{@link #reset()}</li>
   *
   * <li>{@link #fireDatabaseReset()}</li>
   *
   * <li>{@link #disconnect()}</li>
   *
   * <li>{@link #fireDatabaseDisconnected()}</li>
   *
   * <li>{@link #destroy()}</li>
   *
   * <li>{@link #fireDatabaseDestroyed()}</li>
   *
   * </ol>
   *
   * <li>If the {@link ThrowableChain} has a {@linkplain
   * ThrowableChain#size() size} greater than {@code 1}, it is thrown.
   * Otherwise the method completes normally.</li>
   *
   * </ol>
   *
   * @param base the {@link Statement} to wrap; if {@code null},
   * {@code null} is returned
   *
   * @param description currently ignored by this implementation
   */
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
            try {
              create();
              fireDatabaseCreated();
              connect();
              fireDatabaseConnected();
              initialize();
              fireDatabaseInitialized();
              base.evaluate();
            } catch (final ThrowableChain throwMeButRealisticallyNeverThrown) {
              throw throwMeButRealisticallyNeverThrown;
            } catch (final Throwable everyLastLittleThing) {
              chain.add(everyLastLittleThing);
            } finally {
              cleanup(chain);
            }
            if (chain.size() > 1) {
              throw chain;
            }
          }
        };
    }
    return returnValue;
  }

  /**
   * Cleans up after a test.
   *
   * <p>This method calls the following methods in the following order:</p>
   *
   * <ol>
   *
   * <li>{@link #reset()}</li>
   *
   * <li>{@link #fireDatabaseReset()}</li>
   *
   * <li>{@link #disconnect()}</li>
   *
   * <li>{@link #fireDatabaseDisconnected()}</li>
   *
   * <li>{@link #destroy()}</li>
   *
   * <li>{@link #fireDatabaseDestroyed}</li>
   *
   * </ol>
   *
   * <p>Any {@link Throwable}s encountered will be {@linkplain
   * ThrowableChain#add(Throwable) added} to the supplied {@link
   * ThrowableChain}.</p> No error will prevent the sequence of
   * methods above from being fully completed.</p>
   *
   * @param chain the {@link ThrowableChain} to add any encountered
   * {@link Throwable}s to; may be {@link null}
   */
  private final void cleanup(ThrowableChain chain) throws ThrowableChain {
    if (chain == null) {
      chain = new ThrowableChain();
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

  /**
   * Creates the database.
   *
   * <p>This implementation does nothing.</p>
   *
   * @exception Exception if an error occurs
   */
  public void create() throws Exception {

  }

  /**
   * Connects to the database.
   *
   * <p>This implementation does nothing.</p>
   *
   * @exception Exception if an error occurs
   */
  public void connect() throws Exception {

  }

  /**
   * Initializes the database.
   *
   * <p>This implementation does nothing.</p>
   *
   * @exception Exception if an error occurs
   */
  public void initialize() throws Exception {

  }

  /**
   * Resets the database to its pre-test state.
   *
   * <p>This implementation does nothing.</p>
   *
   * @exception Exception if an error occurs
   */
  public void reset() throws Exception {

  }

  /**
   * Disconnects from the database.
   *
   * <p>This implementation does nothing.</p>
   *
   * @exception Exception if an error occurs
   */
  public void disconnect() throws Exception {

  }

  /**
   * Destroys the database.
   *
   * <p>This implementation does nothing.</p>
   *
   * @exception Exception if an error occurs
   */
  public void destroy() throws Exception {

  }

  /**
   * Notifies {@link #getListeners() Listener}s that the database has
   * been created.
   */
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

  /**
   * Notifies {@link #getListeners() Listener}s that the database has
   * been created.
   */
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

  /**
   * Notifies {@link #getListeners() Listener}s that the database has
   * been initialized to its pre-test state.
   */
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

  /**
   * Notifies {@link #getListeners() Listener}s that the database has
   * been reset to its pre-test state.
   */
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

  /**
   * Notifies {@link #getListeners() Listener}s that this {@link
   * AbstractDBRule} has disconnected from the database.
   */
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

  /**
   * Notifies {@link #getListeners() Listener}s that the database has
   * been destroyed.
   */
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

  /**
   * An {@link EventObject} sent to {@link Listener}s representing
   * events in an {@link AbstractDBRule}'s lifecycle.
   *
   * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
   *
   * @version 1.0
   *
   * @since 1.0
   */
  public static class Event extends EventObject {

    /**
     * A version identifier for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new {@link Event} with the supplied {@link
     * AbstractDBRule} as its {@linkplain EventObject#getSource()
     * source}.
     *
     * @param source the {@link AbstractDBRule} whose lifecycle event
     * is being represented; must not be {@code null}
     */
    public Event(final AbstractDBRule source) {
      super(source);
    }

    /**
     * Returns the {@link AbstractDBRule} whose lifecycle event is
     * being represented. This method never returns {@code null}.
     */
    @Override
    public AbstractDBRule getSource() {
      return (AbstractDBRule)super.getSource();
    }

  }

  /**
   * An {@link EventListener} that contractually describes moments in
   * an {@link AbstractDBRule}'s lifecycle.
   *
   * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
   *
   * @version 1.0
   *
   * @since 1.0
   */
  public static interface Listener extends EventListener {

    /**
     * Fired when a database has been created.
     *
     * @param event the lifecycle {@link Event} in question; must not
     * be {@code null}
     */
    public void databaseCreated(final Event event);

    /**
     * Fired when a database has been connected to.
     *
     * @param event the lifecycle {@link Event} in question; must not
     * be {@code null}
     */
    public void databaseConnected(final Event event);

    /**
     * Fired when a database has been initialized to its pre-test state.
     *
     * @param event the lifecycle {@link Event} in question; must not
     * be {@code null}
     */
    public void databaseInitialized(final Event event);

    /**
     * Fired when a database has been reset to its pre-test state.
     *
     * @param event the lifecycle {@link Event} in question; must not
     * be {@code null}
     */
    public void databaseReset(final Event event);

    /**
     * Fired when a database has been disconnected from.
     *
     * @param event the lifecycle {@link Event} in question; must not
     * be {@code null}
     */
    public void databaseDisconnected(final Event event);

    /**
     * Fired when a database has been destroyed.
     *
     * @param event the lifecycle {@link Event} in question; must not
     * be {@code null}
     */
    public void databaseDestroyed(final Event event);

  }

  /**
   * An implementation of the {@link Listener} interface that does
   * nothing.  This class is provided as a convenience for
   * subclassers.
   *
   * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
   *
   * @version 1.0
   *
   * @since 1.0
   */
  public static class AbstractListener implements Listener {

    /**
     * Creates a new {@link AbstractListener}.
     */
    public AbstractListener() {
      super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void databaseCreated(final Event event) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void databaseConnected(final Event event) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void databaseInitialized(final Event event) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void databaseReset(final Event event) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void databaseDisconnected(final Event event) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void databaseDestroyed(final Event event) {

    }


  }


}
