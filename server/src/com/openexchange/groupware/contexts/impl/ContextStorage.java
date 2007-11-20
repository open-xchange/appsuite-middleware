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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.contexts.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.Updater;
import com.openexchange.groupware.update.exception.UpdateException;
import com.openexchange.server.impl.Starter;

/**
 * This class defines the methods for accessing the storage of contexts.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class ContextStorage {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(ContextStorage.class);

    /**
     * Singleton implementation.
     */
    private static ContextStorage impl;

    /**
     * Will be returned if a context cannot be found through its login info.
     */
    public static final int NOT_FOUND = -1;

    /**
     * Creates an instance implementing the context storage.
     * @return an instance implementing the context storage.
     */
    public static ContextStorage getInstance() {
        return impl;
    }

    /**
     * Instantiates an implementation of the context interface and fill its
     * attributes according to the needs to be able to seperate contexts.
     * @param loginContextInfo the login info for the context.
     * @return the unique identifier of the context or <code>-1</code> if no
     * matching context exists.
     * @throws ContextException if an error occurs.
     */
    public abstract int getContextId(String loginContextInfo)
        throws ContextException;

    /**
     * Creates a context implementation for the given context unique identifier.
     * @param contextId unique identifier of the context.
     * @return an implementation of the context or <code>null</code> if the
     * context with the given identifier can't be found.
     * @throws ContextException if an error occurs.
     */
    public Context getContext(final int contextId) throws ContextException {
        final Context retval = loadContext(contextId);
        // Check for update.
        try {
            final Updater updater = Updater.getInstance();
            if (updater.toUpdate(retval)) {
                updater.startUpdate(retval);
                throw new ContextException(ContextException.Code.UPDATE);
            }
            if (updater.isLocked(retval)) {
                throw new ContextException(ContextException.Code.UPDATE);
            }
        } catch (UpdateException e) {
            throw new ContextException(e);
        }
        // Lock context.
        return retval;
    }

    /**
     * Loads the context object.
     * @param contextId unique identifier of the context to load.
     * @return the context object.
     * @throws ContextException if loading the context fails.
     */
    protected abstract ContextExtended loadContext(int contextId)
        throws ContextException;

    /**
     * Invalidates the context object in cache(s).
     * @param contextId unique identifier of the context to invalidate
     * @throws ContextException if invalidating the context fails
     */
    public void invalidateContext(final int contextId) throws ContextException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("invalidateContext not implemented in " + this.getClass()
                .getCanonicalName());
        }
    }

    /**
     * Invalidates a login information in the cache.
     * @param loginContextInfo login information to invalidate.
     * @throws ContextException if invalidating the login information fails.
     */
    public void invalidateLoginInfo(final String loginContextInfo)
        throws ContextException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("invalidateLoginInfo not implemented in " + this
                .getClass().getCanonicalName());
        }
    }

    /**
     * Gives a list of all context ids which are stored in the config database.
     * @return the list of context ids
     * @throws ContextException if reading the contexts fails.
     */
    public abstract List<Integer> getAllContextIds() throws ContextException;

    /**
     * Releases all resources associated with the implementation of this context
     * storage.
     */
    public void close() {
        // Do nothing because we use a singleton implementation.
    }

    /**
     * Initialization.
     * FIXME remove this method.
     * @throws ContextException if initialization of contexts fails.
     * @deprecated use normal server startup with {@link Starter}.
     */
    public static void init() throws ContextException {
        start();
    }

    /**
     * Initialization.
     * @throws ContextException if initialization of contexts fails.
     */
    public static void start() throws ContextException {
        if (null != impl) {
            LOG.error("Duplicate initialization of ContextStorage.");
            return;
        }
        CachingContextStorage.start();
        impl = new CachingContextStorage(new RdbContextStorage());
    }

    /**
     * Shutdown.
     */
    public static void stop() {
        if (null == impl) {
            LOG.error("Duplicate shutdown of ContextStorage.");
            return;
        }
        impl = null;
        CachingContextStorage.stop();
    }
}
