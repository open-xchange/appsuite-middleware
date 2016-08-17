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

package com.openexchange.report.internal;

import java.util.HashSet;
import java.util.Set;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.user.UserService;

/**
 * Increases every day the login timestamp for clients able to use a session longer than several days. This is currently especially the
 * USM client maintaining a long lasting connection to some EAS client.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LastLoginUpdater implements EventHandler {

    /** The logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LastLoginUpdater.class);

    /** The constant providing the amount of milliseconds for one day */
    private static final long MILLIS_DAY = 86400000L;

    /** The accepted clients */
    private final Set<String> acceptedClients;

    private final ContextService contextService;
    private final UserService userService;

    public LastLoginUpdater(ContextService contextService, UserService userService) {
        super();
        this.contextService = contextService;
        this.userService = userService;
        final Set<String> set = new HashSet<String>(1);
        set.add("USM-EAS");
        acceptedClients = set;
    }

    @Override
    public void handleEvent(Event event) {
        if (SessiondEventConstants.TOPIC_TOUCH_SESSION.equals(event.getTopic())) {
            final Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
            // Handle session-touched event asynchronously if possible
            final ThreadPoolService threadPool = ServerServiceRegistry.getInstance().getService(ThreadPoolService.class);
            if (null == threadPool) {
                try {
                    handleSessionTouched(session);
                } catch (final Exception e) {
                    final String message = "Couldn't check/update last-accessed time stamp for client \"" + session.getClient() + "\" of user " + session.getUserId() + " in context " + session.getContextId();
                    LOG.warn(message, e);
                }
            } else {
                final AbstractTask<Void> task = new AbstractTask<Void>() {

                    @Override
                    public Void call() throws Exception {
                        try {
                            handleSessionTouched(session);
                        } catch (final Exception e) {
                            final String message = "Couldn't check/update last-accessed time stamp for client \"" + session.getClient() + "\" of user " + session.getUserId() + " in context " + session.getContextId();
                            LOG.warn(message, e);
                        }
                        return null;
                    }
                };
                threadPool.submit(task, CallerRunsBehavior.<Void> getInstance());
            }
        }
    }

    protected void handleSessionTouched(final Session session) throws OXException {
        // Determine client
        String client = session.getClient();
        if (!com.openexchange.java.Strings.isEmpty(client) && acceptedClients.contains(client)) {
            final Context context = contextService.getContext(session.getContextId());
            final User user = userService.getUser(session.getUserId(), context);
            // Check last-accessed time stamp for client
            final Set<String> values = user.getAttributes().get("client:" + client);
            if (null != values && !values.isEmpty()) {
                try {
                    final long lastAccessed = Long.parseLong(values.iterator().next());
                    final long now = System.currentTimeMillis();
                    if ((now - lastAccessed) >= MILLIS_DAY) {
                        // Need to update
                        LastLoginRecorder.updateLastLogin(userService, client, user, context);
                    }
                } catch (final NumberFormatException e) {
                    // Continue...
                }
            }
        }
    }
}
