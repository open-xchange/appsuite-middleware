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

package com.openexchange.file.storage.cifs.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.cifs.CIFSServices;
import com.openexchange.file.storage.cifs.cache.SmbFileMapManagement;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.TimerService;

/**
 * {@link CIFSActivator} - Activator for CIFS bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CIFSActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link CIFSActivator}.
     */
    public CIFSActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { FileStorageAccountManagerLookupService.class, SessiondService.class, MimeTypeMap.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            CIFSServices.setServices(this);
            /*
             * Some initialization stuff
             */
            final BundleContext context = this.context;
            /*
             * Register tracker
             */
            rememberTracker(new ServiceTracker<FileStorageAccountManagerProvider, FileStorageAccountManagerProvider>(context, FileStorageAccountManagerProvider.class, new CIFSServiceRegisterer(context)));
            openTrackers();
            SmbFileMapManagement.getInstance().startShrinker();
            /*
             * Event handler
             */
            {
                final EventHandler eventHandler = new EventHandler() {

                    @Override
                    public void handleEvent(final Event event) {
                        final String topic = event.getTopic();
                        if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
                            handleDroppedSession((Session) event.getProperty(SessiondEventConstants.PROP_SESSION));
                        } else if (SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic) || SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic)) {
                            @SuppressWarnings("unchecked")
                            final Map<String, Session> map = (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                            for (final Session session : map.values()) {
                                handleDroppedSession(session);
                            }
                        }
                    }

                    private void handleDroppedSession(final Session session) {
                        if (session.isTransient()) {
                            return;
                        }
                        if (null == getService(SessiondService.class).getAnyActiveSessionForUser(session.getUserId(), session.getContextId())) {
                            SmbFileMapManagement.getInstance().dropFor(session);
                        }
                    }
                };
                final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
                dict.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
                registerService(EventHandler.class, eventHandler, dict);
            }
        } catch (final Exception e) {
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CIFSActivator.class)).error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public <S> void registerService(final Class<S> clazz, final S service) {
        super.registerService(clazz, service);
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            SmbFileMapManagement.getInstance().stopShrinker();
            // Clean-up
            cleanUp();
            // Clear service registry
            CIFSServices.setServices(null);
        } catch (final Exception e) {
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CIFSActivator.class)).error(e.getMessage(), e);
            throw e;
        }
    }

}
