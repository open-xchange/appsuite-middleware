/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.report.internal;

import java.util.Set;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.google.common.collect.ImmutableSet;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.user.User;
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
        ImmutableSet.Builder<String> set = ImmutableSet.builder();
        set.add("USM-EAS");
        acceptedClients = set.build();
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
                } catch (Exception e) {
                    LOG.warn("Couldn''t check/update last-accessed time stamp for client \"{}\" of user {} in context {}", session.getClient(), session.getUserId(), session.getContextId(), e);
                }
            } else {
                final AbstractTask<Void> task = new AbstractTask<Void>() {

                    @Override
                    public Void call() throws Exception {
                        try {
                            handleSessionTouched(session);
                        } catch (Exception e) {
                            LOG.warn("Couldn''t check/update last-accessed time stamp for client \"{}\" of user {} in context {}", session.getClient(), session.getUserId(), session.getContextId(), e);
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
            Context context = contextService.getContext(session.getContextId());
            User user = userService.getUser(session.getUserId(), context);

            // Check last-accessed time stamp for client
            String value = user.getAttributes().get("client:" + client);
            if (null != value) {
                try {
                    final long lastAccessed = Long.parseLong(value);
                    final long now = System.currentTimeMillis();
                    if ((now - lastAccessed) >= MILLIS_DAY) {
                        // Need to update
                        LastLoginRecorder.updateLastLogin(userService, client, user, context);
                    }
                } catch (NumberFormatException e) {
                    // Continue...
                }
            }
        }
    }
}
