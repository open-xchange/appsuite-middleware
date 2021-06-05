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

package com.openexchange.chronos.alarm.mail.impl;

import static com.openexchange.osgi.Tools.requireService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.alarm.message.AlarmNotificationService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.osgi.Tools;
import com.openexchange.ratelimit.Rate;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link MailAlarmNotificationServiceImpl}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class MailAlarmNotificationServiceImpl implements AlarmNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(MailAlarmNotificationServiceImpl.class.getName());

    private final MailAlarmMailHandler mailAlarmNotificationHandler;

    private final ServiceLookup services;

    public MailAlarmNotificationServiceImpl(ServiceLookup services) {
        this.services = services;
        this.mailAlarmNotificationHandler = new MailAlarmMailHandler(services);
    }

    @Override
    public void send(Event event, Alarm alarm, int contextId, int accountId, int userId, long trigger) throws OXException {
        if (event == null || contextId < 1 || userId < 1) {
            LOG.debug("Event is null or contextId/userId invalid. Cannot send alarm notification via mail.");
            return;
        }

        ContextService contextService = requireService(ContextService.class, services);
        Context context = contextService.getContext(contextId);
        UserService userService = requireService(UserService.class, services);
        User targetUser = userService.getUser(userId, context);

        this.mailAlarmNotificationHandler.send(event, targetUser, contextId, accountId, trigger);
    }

    @Override
    public AlarmAction getAction() {
        return AlarmAction.EMAIL;
    }

    @Override
    public int getShift() throws OXException {
        LeanConfigurationService leanConfig = Tools.requireService(LeanConfigurationService.class, services);
        return leanConfig.getIntProperty(MailAlarmConfig.MAIL_SHIFT);
    }

    @Override
    public boolean isEnabled(int userId, int contextId) throws OXException {
        LeanConfigurationService leanConfig = Tools.requireService(LeanConfigurationService.class, services);
        return leanConfig.getBooleanProperty(userId, contextId, MailAlarmConfig.MAIL_ENABLED);
    }

    @Override
    public Rate getRate(int userId, int contextId) throws OXException {
        LeanConfigurationService leanConfig = Tools.requireService(LeanConfigurationService.class, services);
        return Rate.create(leanConfig.getIntProperty(userId, contextId, MailAlarmConfig.MAIL_LIMIT_AMOUNT), leanConfig.getLongProperty(userId, contextId, MailAlarmConfig.MAIL_LIMIT_TIME_FRAME));
    }
}
