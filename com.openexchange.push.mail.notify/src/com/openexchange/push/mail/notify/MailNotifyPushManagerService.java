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

package com.openexchange.push.mail.notify;

import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushManagerExtendedService;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.PushUser;
import com.openexchange.push.PushUserInfo;
import com.openexchange.push.mail.notify.osgi.Services;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;

/**
 * {@link MailNotifyPushManagerService} - The {@link PushManagerService} for primary mail account.
 */
public final class MailNotifyPushManagerService implements PushManagerExtendedService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MailNotifyPushManagerService.class);

    private final String name;
    private final MailNotifyPushListenerRegistry registry;

    /**
     * Initializes a new {@link MailNotifyPushManagerService}.
     */
    public MailNotifyPushManagerService(MailNotifyPushListenerRegistry registry) {
        super();
        name = "Mail Push Manager";
        this.registry = registry;
    }

    /**
     * Checks if Mail Notify Push is enabled for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    private boolean isMailNotifyPushEnabledFor(int userId, int contextId) throws OXException {
        ConfigViewFactory factory = Services.optService(ConfigViewFactory.class);
        if (factory == null) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = factory.getView(userId, contextId);
        return ConfigViews.getDefinedBoolPropertyFrom("com.openexchange.push.mail.notify.enabled", true, view);
    }

    private Session generateSessionFor(PushUser pushUser) throws OXException {
        PushListenerService pushListenerService = Services.getService(PushListenerService.class, true);
        return pushListenerService.generateSessionFor(pushUser);
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    @Override
    public PushListener startListener(Session session) throws OXException {
        if (null == session) {
            return null;
        }

        int contextId = session.getContextId();
        int userId = session.getUserId();
        if (false == isMailNotifyPushEnabledFor(userId, contextId)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("Denied starting Mail Notify listener for user {} in context {} with session {} since disabled via configuration", I(userId), I(contextId), session.getSessionID(), new Throwable("Mail Notify start listener trace"));
            } else {
                LOGGER.info("Denied starting Mail Notify listener for user {} in context {} with session {} since disabled via configuration", I(userId), I(contextId), session.getSessionID());
            }
            return null;
        }

        MailNotifyPushListener pushListener = MailNotifyPushListener.newInstance(session, false);
        if (registry.addPushListener(session.getUserId(), session.getContextId(), pushListener)) {
            return pushListener;
        }
        return null;
    }

    @Override
    public boolean stopListener(Session session) throws OXException {
        if (null == session) {
            return false;
        }

        return registry.stopPushListener(true, false, session.getUserId(), session.getContextId());
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    @Override
    public List<PushUserInfo> getAvailablePushUsers() throws OXException {
        return registry.getAvailablePushUsers();
    }

    @Override
    public boolean supportsPermanentListeners() {
        return true;
    }

    @Override
    public PushListener startPermanentListener(PushUser pushUser) throws OXException {
        if (null == pushUser) {
            return null;
        }

        int contextId = pushUser.getContextId();
        int userId = pushUser.getUserId();
        if (false == isMailNotifyPushEnabledFor(userId, contextId)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("Denied starting permanent Mail Notify listener for user {} in context {} since disabled via configuration", I(userId), I(contextId), new Throwable("Mail Notify start permanent listener trace"));
            } else {
                LOGGER.info("Denied starting permanent Mail Notify listener for user {} in context {} since disabled via configuration", I(userId), I(contextId));
            }
            return null;
        }

        MailNotifyPushListener pushListener = MailNotifyPushListener.newInstance(generateSessionFor(pushUser), true);
        if (registry.addPushListener(pushUser.getUserId(), pushUser.getContextId(), pushListener)) {
            return pushListener;
        }
        return null;
    }

    @Override
    public boolean stopPermanentListener(PushUser pushUser, boolean tryToReconnect) throws OXException {
        if (null == pushUser) {
            return false;
        }

        return registry.stopPushListener(tryToReconnect, true, pushUser.getUserId(), pushUser.getContextId());
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return name;
    }

}
