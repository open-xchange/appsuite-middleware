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
import com.openexchange.groupware.ldap.User;
import com.openexchange.osgi.Tools;
import com.openexchange.server.ServiceLookup;
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
    public int getAmount(int userId, int contextId) throws OXException {
        LeanConfigurationService leanConfig = Tools.requireService(LeanConfigurationService.class, services);
        return leanConfig.getIntProperty(userId, contextId, MailAlarmConfig.MAIL_LIMIT_AMOUNT);
    }

    @Override
    public long getTimeframe(int userId, int contextId) throws OXException {
        LeanConfigurationService leanConfig = Tools.requireService(LeanConfigurationService.class, services);
        return leanConfig.getLongProperty(userId, contextId, MailAlarmConfig.MAIL_LIMIT_TIME_FRAME);
    }
}
