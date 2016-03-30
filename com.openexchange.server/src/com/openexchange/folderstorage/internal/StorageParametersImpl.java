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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.folderstorage.internal;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link StorageParametersImpl} - Implementation of {@link StorageParameters}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class StorageParametersImpl implements StorageParameters {

    private static final Object PRESENT = new Object();

    private final ServerSession session;

    private FolderServiceDecorator decorator;

    private final User user;

    private final int userId;

    private final Context context;

    private final int contextId;

    private final ConcurrentMap<FolderType, ConcurrentMap<String, Object>> parameters;

    private Date timeStamp;

    private Thread usingThread;

    private StackTraceElement[] trace;

    private final Map<OXException, Object> warnings;

    private Boolean ignoreCache;

    /**
     * Initializes a new {@link StorageParametersImpl} from given session.
     *
     * @param session The session
     */
    public StorageParametersImpl(final ServerSession session) {
        this(session, null, null);
    }

    /**
     * Initializes a new {@link StorageParametersImpl} from given session.
     *
     * @param session The session
     * @param user The session user
     * @param context The session context
     */
    public StorageParametersImpl(final ServerSession session, final User user, final Context context) {
        super();
        this.session = session;
        this.user = null == user ? session.getUser() : user;
        userId = null == this.user ? -1 : this.user.getId();
        this.context = null == context ? session.getContext() : context;
        contextId = null == this.context ? -1 : this.context.getContextId();
        parameters = new ConcurrentHashMap<FolderType, ConcurrentMap<String, Object>>();
        warnings = new ConcurrentHashMap<OXException, Object>(2, 0.9f, 1);
    }

    /**
     * Initializes a new {@link StorageParametersImpl} from given user-context-pair.
     *
     * @param user The user
     * @param context The context
     */
    public StorageParametersImpl(final User user, final Context context) {
        super();
        session = null;
        this.user = user;
        userId = user.getId();
        this.context = context;
        contextId = context.getContextId();
        parameters = new ConcurrentHashMap<FolderType, ConcurrentMap<String, Object>>();
        warnings = new ConcurrentHashMap<OXException, Object>(2, 0.9f, 1);
    }

    /**
     * Initializes a new {@link StorageParametersImpl} from specified storage parameters.
     *
     * @param source The source parameters
     */
    public StorageParametersImpl(final StorageParameters source) {
        super();
        final Session s = source.getSession();
        if (null == s) {
            user = source.getUser();
            session = null;
            userId = user.getId();
            context = source.getContext();
            contextId = context.getContextId();
        } else {
            try {
                session = s instanceof ServerSession ? (ServerSession) s : ServerSessionAdapter.valueOf(s);
                user = this.session.getUser();
                userId = user.getId();
                context = this.session.getContext();
                contextId = context.getContextId();
            } catch (final OXException e) {
                throw new IllegalStateException(e);
            }
        }
        parameters = new ConcurrentHashMap<FolderType, ConcurrentMap<String, Object>>();
        warnings = new ConcurrentHashMap<OXException, Object>(2, 0.9f, 1);
    }

    private ConcurrentMap<String, Object> getFolderTypeMap(final FolderType folderType, final boolean createIfAbsent) {
        ConcurrentMap<String, Object> m = parameters.get(folderType);
        if (createIfAbsent && null == m) {
            final ConcurrentMap<String, Object> inst = new ConcurrentHashMap<String, Object>();
            m = parameters.putIfAbsent(folderType, inst);
            if (null == m) {
                m = inst;
            }
        }
        return m;
    }

    @Override
    public void addWarning(OXException warning) {
        if (false == Category.CATEGORY_WARNING.equals(warning.getCategory()) &&
            (null == warning.getCategories() || false == warning.getCategories().contains(Category.CATEGORY_WARNING))) {
            warning.addCategory(Category.CATEGORY_WARNING);
        }
        warnings.put(warning, PRESENT);
    }

    @Override
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    @Override
    public Set<OXException> getWarnings() {
        return Collections.unmodifiableSet(warnings.keySet());
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public <P> P getParameter(final FolderType folderType, final String name) {
        final Map<String, Object> m = getFolderTypeMap(folderType, false);
        if (null == m) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked") final P retval = (P) m.get(name);
            return retval;
        } catch (final ClassCastException e) {
            /*
             * Wrong type
             */
            return null;
        }
    }

    @Override
    public <P> P removeParameter(final FolderType folderType, final String name) {
        final Map<String, Object> m = getFolderTypeMap(folderType, false);
        if (null == m) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked") final P retval = (P) m.remove(name);
            return retval;
        } catch (final ClassCastException e) {
            /*
             * Wrong type
             */
            return null;
        }
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Object putParameter(final FolderType folderType, final String name, final Object value) {
        if (null == value) {
            final Map<String, Object> m = getFolderTypeMap(folderType, false);
            if (null == m) {
                return null;
            }
            return m.remove(name);
        } else {
            final Map<String, Object> m = getFolderTypeMap(folderType, true);
            return m.put(name, value);
        }
    }

    @Override
    public boolean putParameterIfAbsent(final FolderType folderType, final String name, final Object value) {
        if (null == value) {
            throw new IllegalArgumentException("value is null");
        }
        final ConcurrentMap<String, Object> m = getFolderTypeMap(folderType, true);
        return (null == m.putIfAbsent(name, value));
    }

    @Override
    public Date getTimeStamp() {
        return null == timeStamp ? null : new Date(timeStamp.getTime());
    }

    @Override
    public void setTimeStamp(final Date timeStamp) {
        this.timeStamp = null == timeStamp ? null : new Date(timeStamp.getTime());
    }

    @Override
    public FolderServiceDecorator getDecorator() {
        return decorator;
    }

    @Override
    public void setDecorator(final FolderServiceDecorator decorator) {
        this.decorator = decorator;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public void markCommitted() {
        usingThread = Thread.currentThread();
        /*
         * This is faster than Thread.getStackTrace() since a native method is used to fill thread's stack trace
         */
        trace = new Throwable().getStackTrace();
    }

    /**
     * Gets the trace of the thread that lastly obtained this access.
     * <p>
     * This is useful to detect certain threads which uses an access for a long time
     *
     * @return the trace of the thread that lastly obtained this access
     */
    @Override
    public String getCommittedTrace() {
        final StringBuilder sBuilder = new StringBuilder(512);
        sBuilder.append(toString());
        sBuilder.append("\nStorage parameters committed at: ").append('\n');
        /*
         * Start at index 2
         */
        final String delim = "\tat ";
        for (int i = 2; i < trace.length; i++) {
            sBuilder.append(delim).append(trace[i]).append('\n');
        }
        if ((null != usingThread) && usingThread.isAlive()) {
            sBuilder.append("Currently using thread: ").append(usingThread.getName()).append('\n');
            /*
             * Only possibility to get the current working position of a thread.
             */
            final StackTraceElement[] trace = usingThread.getStackTrace();
            sBuilder.append(delim).append(trace[0]);
            final String prefix = "\n\tat ";
            for (int i = 1; i < trace.length; i++) {
                sBuilder.append(prefix).append(trace[i]);
            }
        }
        return sBuilder.toString();
    }

    @Override
    public Boolean getIgnoreCache() {
        return ignoreCache;
    }

    @Override
    public void setIgnoreCache(Boolean ignoreCache) {
        this.ignoreCache = ignoreCache;
    }

}
