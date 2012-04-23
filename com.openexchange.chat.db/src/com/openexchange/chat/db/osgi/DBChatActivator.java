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

package com.openexchange.chat.db.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.chat.ChatService;
import com.openexchange.chat.MessageListener;
import com.openexchange.chat.Presence;
import com.openexchange.chat.db.DBChat;
import com.openexchange.chat.db.DBChatAccess;
import com.openexchange.chat.db.DBChatService;
import com.openexchange.chat.db.DBChatServiceLookup;
import com.openexchange.chat.db.DBRoster;
import com.openexchange.chat.db.groupware.DBChatCreateTableService;
import com.openexchange.chat.db.groupware.DBChatCreateTableTask;
import com.openexchange.chat.db.groupware.DBChatDeleteListener;
import com.openexchange.chat.util.ChatUserImpl;
import com.openexchange.chat.util.PresenceImpl;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link DBChatActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBChatActivator extends HousekeepingActivator {

    protected static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DBChatActivator.class));

    /**
     * Initializes a new {@link DBChatActivator}.
     */
    public DBChatActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            ThreadPoolService.class, TimerService.class, DatabaseService.class, UserService.class, ContextService.class,
            IDGeneratorService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        DBChatServiceLookup.set(this);
        DBChat.startUp();
        /*
         * Register service
         */
        registerService(ChatService.class, DBChatService.newDbChatService());
        /*
         * Add tracker
         */
        track(MessageListener.class, new SimpleRegistryListener<MessageListener>() {

            @Override
            public void added(final ServiceReference<MessageListener> ref, final MessageListener messageListener) {
                DBChat.addMessageListenerStatic(messageListener);
            }

            @Override
            public void removed(final ServiceReference<MessageListener> ref, final MessageListener messageListener) {
                DBChat.removeMessageListenerStatic(messageListener);
            }
        });
        track(SessiondService.class, new SimpleRegistryListener<SessiondService>() {

            @Override
            public void added(final ServiceReference<SessiondService> ref, final SessiondService service) {
                DBRoster.set(service);
                addService(SessiondService.class, service);
            }

            @Override
            public void removed(final ServiceReference<SessiondService> ref, final SessiondService service) {
                DBRoster.set(null);
                removeService(SessiondService.class);
            }
        });
        track(CryptoService.class, new SimpleRegistryListener<CryptoService>() {

            @Override
            public void added(final ServiceReference<CryptoService> ref, final CryptoService service) {
                DBChat.setCryptoService(service);
                addService(CryptoService.class, service);
            }

            @Override
            public void removed(final ServiceReference<CryptoService> ref, final CryptoService service) {
                DBChat.setCryptoService(null);
                removeService(CryptoService.class);
            }
        });
        openTrackers();
        /*
         * Register update task, create table job and delete listener
         */
        {
            registerService(CreateTableService.class, new DBChatCreateTableService());
            registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new DBChatCreateTableTask()));
            registerService(DeleteListener.class, new DBChatDeleteListener());
        }
        /*
         * Register event handler
         */
        {
            final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
            registerService(EventHandler.class, new EventHandler() {

                @Override
                public void handleEvent(final Event event) {
                    final String topic = event.getTopic();
                    if (SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic)) {
                        @SuppressWarnings("unchecked") final Map<String, Session> container =
                            (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                        for (final Session session : container.values()) {
                            handleRemovedSession(session);
                        }
                    } else if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
                        final Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                        handleRemovedSession(session);
                    } else if (SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic)) {
                        @SuppressWarnings("unchecked") final Map<String, Session> container =
                            (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                        for (final Session session : container.values()) {
                            handleRemovedSession(session);
                        }
                    } else if (SessiondEventConstants.TOPIC_ADD_SESSION.equals(topic)) {
                        final Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                        handleAddedSession(session);
                    } else if (SessiondEventConstants.TOPIC_REACTIVATE_SESSION.equals(topic)) {
                        @SuppressWarnings("unchecked") final Map<String, Session> container =
                            (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                        for (final Session session : container.values()) {
                            handleAddedSession(session);
                        }
                    }
                }

                private void handleAddedSession(final Session session) {
                    try {
                        final DBRoster dbRoster = DBRoster.optRosterFor(session.getContextId());
                        if (null != dbRoster) {
                            final PresenceImpl presence = new PresenceImpl();
                            final int userId = session.getUserId();
                            presence.setFrom(new ChatUserImpl(Integer.toString(userId), getUserName(userId, session.getContextId())));
                            dbRoster.notifyRosterListeners(presence);
                        }
                    } catch (final Exception e) {
                        // Failed handling session
                        LOG.warn("Failed handling tracked added session for DB chat.", e);
                    }
                }

                private void handleRemovedSession(final Session session) {
                    try {
                        if (null != DBRoster.getAnyActiveSessionForUser(session.getUserId(), session.getContextId())) {
                            // Other active session present
                            return;
                        }
                        /*-
                         * Last session gone: clean up
                         *
                         * 1. Mark as unavailable in roster
                         * 2. Remove associated chat access
                         */
                        final DBRoster dbRoster = DBRoster.optRosterFor(session.getContextId());
                        if (null != dbRoster) {
                            final PresenceImpl presence = new PresenceImpl(Presence.Type.UNAVAILABLE);
                            final int userId = session.getUserId();
                            presence.setFrom(new ChatUserImpl(Integer.toString(userId), getUserName(userId, session.getContextId())));
                            dbRoster.notifyRosterListeners(presence);
                        }
                        // Drop chat access for associated user
                        DBChatAccess.removeDbChatAccess(session);
                    } catch (final Exception e) {
                        // Failed handling session
                        LOG.warn("Failed handling tracked removed session for DB chat.", e);
                    }
                }

                private String getUserName(final int userId, final int cid) throws OXException {
                    final Context ctx = getService(ContextService.class).getContext(cid);
                    return getService(UserService.class).getUser(userId, ctx).getDisplayName();
                }

            },
                serviceProperties);
        }
    }

    @Override
    protected void cleanUp() {
        super.cleanUp();
        DBChat.shutDown();
        DBChatServiceLookup.set(null);
    }

}
