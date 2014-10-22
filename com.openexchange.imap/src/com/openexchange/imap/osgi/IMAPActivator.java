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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.imap.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import javax.activation.MailcapCommandMap;
import net.htmlparser.jericho.Config;
import net.htmlparser.jericho.LoggerProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.database.DatabaseService;
import com.openexchange.imap.IMAPProvider;
import com.openexchange.imap.cache.ListLsubCache;
import com.openexchange.imap.config.IMAPProperties;
import com.openexchange.imap.config.IMAPReloadable;
import com.openexchange.imap.notify.IMAPNotifierRegistryService;
import com.openexchange.imap.notify.internal.IMAPNotifierRegistry;
import com.openexchange.imap.services.Services;
import com.openexchange.imap.storecache.IMAPStoreCache;
import com.openexchange.imap.threader.ThreadableCache;
import com.openexchange.imap.threader.ThreadableLoginHandler;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.push.PushEventConstants;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.textxtraction.TextXtractService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link IMAPActivator} - The {@link BundleActivator activator} for IMAP bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPActivator extends HousekeepingActivator {

    protected static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(IMAPActivator.class);

    private WhiteboardSecretService secretService;

    /**
     * Initializes a new {@link IMAPActivator}
     */
    public IMAPActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            ConfigurationService.class, CacheService.class, CacheEventService.class, UserService.class, MailAccountStorageService.class,
            ThreadPoolService.class, TimerService.class, SessiondService.class, DatabaseService.class, TextXtractService.class,
            EventAdmin.class };
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        LOG.warn("Absent service: {}", clazz.getName());
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        LOG.info("Re-available service: {}", clazz.getName());
    }

    @Override
    public void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);
            Config.LoggerProvider = LoggerProvider.DISABLED;
            IMAPStoreCache.initInstance();
            /*
             * Register IMAP mail provider
             */
            final Dictionary<String, String> dictionary = new Hashtable<String, String>(1);
            dictionary.put("protocol", IMAPProvider.PROTOCOL_IMAP.toString());
            registerService(MailProvider.class, IMAPProvider.getInstance(), dictionary);
            /*
             * Register IMAP notifier registry
             */
            if (IMAPProperties.getInstance().notifyRecent()) {
                final ConfigurationService service = getService(ConfigurationService.class);
                final boolean register = service.getBoolProperty("com.openexchange.imap.registerIMAPNotifierRegistryService", false);
                if (register) {
                    registerService(IMAPNotifierRegistryService.class, IMAPNotifierRegistry.getInstance());
                }
            }
            /*
             * Trackers
             */
            track(MailcapCommandMap.class, new MailcapServiceTracker(context));
            track(CacheEventService.class, new ListLsubInvalidator(context));
            openTrackers();
            /*
             * Register login handler
             */
            registerService(LoginHandlerService.class, new ThreadableLoginHandler(this));
            registerService(Reloadable.class, IMAPReloadable.getInstance());
            /*
             * Register event handler
             */
            {
                final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
                serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
                final EventHandler eventHandler = new EventHandler() {

                    @Override
                    public void handleEvent(final Event event) {
                        final ThreadPoolService threadPool = getService(ThreadPoolService.class);
                        if (null == threadPool) {
                            doHandleEvent(event);
                        } else {
                            final AbstractTask<Void> t = new AbstractTask<Void>() {

                                @Override
                                public Void call() throws Exception {
                                    try {
                                        doHandleEvent(event);
                                    } catch (final Exception e) {
                                        LOG.warn("Handling event {} failed.", event.getTopic(), e);
                                    }
                                    return null;
                                }
                            };
                            threadPool.submit(t, CallerRunsBehavior.<Void> getInstance());
                        }
                    }

                    /**
                     * Handles given event.
                     *
                     * @param event The event
                     */
                    protected void doHandleEvent(final Event event) {
                        final String topic = event.getTopic();
                        if (SessiondEventConstants.TOPIC_LAST_SESSION.equals(topic)) {
                            Integer contextId = (Integer) event.getProperty(SessiondEventConstants.PROP_CONTEXT_ID);
                            if (null != contextId) {
                                Integer userId = (Integer) event.getProperty(SessiondEventConstants.PROP_USER_ID);
                                if (null != userId) {
                                    ListLsubCache.dropFor(userId, contextId);
                                    IMAPStoreCache.getInstance().dropFor(userId, contextId);
                                    ThreadableCache.dropFor(userId, contextId);

                                    IMAPNotifierRegistry.getInstance().handleRemovedSession(userId, contextId);
                                }
                            }
                        }
                    }
                };
                registerService(EventHandler.class, eventHandler, serviceProperties);
            }
            {
                final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
                serviceProperties.put(EventConstants.EVENT_TOPIC, "com/openexchange/passwordchange");
                final EventHandler eventHandler = new EventHandler() {

                    @Override
                    public void handleEvent(final Event event) {
                        final int contextId = ((Integer) event.getProperty("com.openexchange.passwordchange.contextId")).intValue();
                        final int userId = ((Integer) event.getProperty("com.openexchange.passwordchange.userId")).intValue();
                        final Session session = (Session) event.getProperty("com.openexchange.passwordchange.session");
                        ListLsubCache.dropFor(session);
                        IMAPStoreCache.getInstance().dropFor(userId, contextId);
                        ThreadableCache.dropFor(session);
                    }

                };
                registerService(EventHandler.class, eventHandler, serviceProperties);
            }
            {
                final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
                serviceProperties.put(EventConstants.EVENT_TOPIC, PushEventConstants.getAllTopics());
                final EventHandler eventHandler = new EventHandler() {

                    @Override
                    public void handleEvent(final Event event) {
                        if (Boolean.TRUE.equals(event.getProperty("__isRemoteEvent"))) { // Remotely received
                            final Session session = ((Session) event.getProperty(PushEventConstants.PROPERTY_SESSION));
                            if (null != session) {
                                try {
                                    final String folderId = (String) event.getProperty(PushEventConstants.PROPERTY_FOLDER);
                                    final FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(folderId);
                                    final Boolean contentRelated = (Boolean) event.getProperty(PushEventConstants.PROPERTY_CONTENT_RELATED);
                                    if (null == contentRelated || false == contentRelated.booleanValue()) {
                                        ListLsubCache.clearCache(fa.getAccountId(), session);
                                    }
                                } catch (final Exception e) {
                                    LOG.error("Failed to handle event: {}", event.getTopic(), e);
                                }
                            }
                        }
                    }
                };
                registerService(EventHandler.class, eventHandler, serviceProperties);
            }
        } catch (final Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        try {
            cleanUp();
            /*
             * Clear service registry
             */
            IMAPStoreCache.shutDownInstance();
            ThreadableCache.getInstance().clear();
            Services.setServiceLookup(null);
            if (secretService != null) {
                secretService.close();
                secretService = null;
            }
        } catch (final Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

}
