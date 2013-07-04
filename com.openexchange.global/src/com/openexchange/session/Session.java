/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.session;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link Session} - Represents an Open-Xchange session.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Session {

    /**
     * The empty lock, doing nothing on invocations.
     */
    public static final Lock EMPTY_LOCK = new Lock() {

        @Override
        public void unlock() {
            // ignore
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public boolean tryLock() {
            return true;
        }

        /**
         * Empty lock provides no condition. Therefore a <code>UnsupportedOperationException</code> is thrown on invocation attempt.
         */
        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException("Empty lock provides no condition.");
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            // ignore
        }

        @Override
        public void lock() {
            // ignore
        }
    };

    /**
     * The parameter name for session lock. The parameter value is an instance of <code>java.uitl.concurrent.locks.Lock</code>.
     * <p>
     * Usage for locking session might look like:
     *
     * <pre>
     * import java.util.concurrent.locks.Lock
     * ...
     * final Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
     * if (null == lock) {
     *  synchronized (session) {
     *   ...
     *  }
     * } else {
     *  lock.lock();
     *  try {
     *   ...
     *  } finally {
     *   lock.unlock();
     *  }
     * }
     * </pre>
     */
    public static final String PARAM_LOCK = "session.lock".intern();

    /**
     * The parameter name for request counter currently active on session. The parameter value is an instance of
     * <code>java.util.concurrent.atomic.AtomicInteger</code>.
     */
    public static final String PARAM_COUNTER = "com.openexchange.session.counter".intern();

    /**
     * The parameter for client capabilities.
     *
     * @type <code>java.util.List&lt;String&gt;</code>
     */
    public static final String PARAM_CAPABILITIES = "session.clientCapabilities".intern();

    /**
     * The parameter for optional alternative identifier.
     *
     * @type <code>java.lang.String</code>
     */
    public static final String PARAM_ALTERNATIVE_ID = "__session.altId".intern();

    /**
     * The parameter for optional token identifier.
     *
     * @type <code>java.lang.String</code>
     */
    public static final String PARAM_TOKEN = "__session.token".intern();

    /**
     * @return the context identifier.
     */
    public int getContextId();

    /**
     * Gets the local IP address
     *
     * @return The local IP address
     */
    public String getLocalIp();

    /**
     * Updates the local IP address.
     *
     * @param ip The new IP address associated with this session
     * @deprecated Use {@link SessiondService#setLocalIp(String, String)} instead
     */
    @Deprecated
    public void setLocalIp(String ip);

    /**
     * Gets the login name
     *
     * @return The login name
     */
    public String getLoginName();

    /**
     * Checks if there is a parameter bound to specified name.
     *
     * @param name The parameter name
     * @return <code>true</code> if there is a parameter bound to specified name; otherwise <code>false</code>
     */
    public boolean containsParameter(String name);

    /**
     * Gets the parameter bound to specified name or <code>null</code> if no such parameter is present
     *
     * @param name The parameter name
     * @return The parameter or <code>null</code>
     */
    public Object getParameter(String name);

    /**
     * Gets the password
     *
     * @return The password
     */
    public String getPassword();

    /**
     * Gets the random token
     *
     * @return The random token
     */
    public String getRandomToken();

    /**
     * Gets the secret
     *
     * @return
     */
    public String getSecret();

    /**
     * Gets the session ID
     *
     * @return The session ID
     */
    public String getSessionID();

    /**
     * Gets the user ID
     *
     * @return The user ID
     */
    public int getUserId();

    /**
     * Gets the user login
     *
     * @return The user login
     */
    public String getUserlogin();

    /**
     * Gets the full login incl. context information; e.g <code>test@foo</code>
     *
     * @return The full login
     */
    public String getLogin();

    /**
     * Sets the parameter. Any existing parameters bound to specified name are replaced with given value.
     * <p>
     * A <code>null</code> value removes the parameter.
     * <p>
     * <code>Note</code>: To ensure the parameter will reside in session on remote distribution the <code>Serializable</code> interface
     * should be implemented for specified value.
     *
     * @param name The parameter name
     * @param value The parameter value
     */
    public void setParameter(String name, Object value);

    /**
     * @return the authentication identifier that is used to trace the login request across different systems.
     */
    String getAuthId();

    /**
     * @return The HashCode distinguishing this session from others in the same store.
     */
    String getHash();

    /**
     * Updates the hash value of this session.
     *
     * @param hash The new hash value
     * @deprecated Use {@link SessiondService#setHash(String, String)} instead
     */
    @Deprecated
    void setHash(String hash);

    /**
     * The client is remembered through the whole session. It should identify what client uses the back-end. Normally this is the web
     * front-end but there may be other clients especially those that synchronize their data with OX. The client is a parameter passed to the
     * back-end during the login request.
     * @return the client identifier of the client using the back-end.
     */
    String getClient();

    /**
     * Should only be used to update the client on a redirect request.
     *
     * @param client The new client identifier.
     * @deprecated Use {@link SessiondService#setClient(String, String)} instead
     */
    @Deprecated
    void setClient(String client);

    /**
     * Gets a value indicating whether the session is transient or not, i.e. the session is distributed to other nodes in the cluster
     * or has been put into another persistent storage. Typically, very short living sessions like those created for CalDAV-/CardDAV-
     * or InfoStore access via WebDAV are marked transient.
     *
     * @return <code>true</code> if the session is transient, <code>false</code>, otherwise
     */
    boolean isTransient();

}
