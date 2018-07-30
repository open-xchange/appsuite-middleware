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
import java.io.UnsupportedEncodingException;
import javax.mail.internet.AddressException;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.alarm.mail.exception.MailAlarmExceptionCodes;
import com.openexchange.chronos.alarm.mail.notification.ExtendedNotificationMail;
import com.openexchange.chronos.alarm.mail.notification.MailAlarmNotificationGenerator;
import com.openexchange.chronos.alarm.mail.notification.MailAlarmNotificationParticipantResolver;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.notification.mail.MailData;
import com.openexchange.notification.mail.MailData.Builder;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.resource.ResourceService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.user.UserService;

/**
 * 
 * {@link MailAlarmMailGenerator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class MailAlarmMailGenerator {

    private final ServiceLookup services;
    private final MailData mailData;

    protected MailAlarmMailGenerator(MailData mailData, ServiceLookup services) {
        super();
        this.services = services;
        this.mailData = mailData;
    }

    public static MailAlarmMailGenerator init(Event event, User user, int contextId, ServiceLookup services) throws OXException {
        ContextService contextService = requireService(ContextService.class, services);
        Context context = contextService.getContext(contextId);

        MailAlarmNotificationParticipantResolver participantResolver = new MailAlarmNotificationParticipantResolver(requireService(ConfigurationService.class, services), requireService(UserService.class, services), requireService(ResourceService.class, services), contextService);
        MailAlarmNotificationGenerator notificationMailGenerator = new MailAlarmNotificationGenerator(services, participantResolver, event, user, context);

        ExtendedNotificationMail mail = notificationMailGenerator.create("notify.mail.alarm.mail");

        ServerConfigService serverConfigService = requireService(ServerConfigService.class, services);
        ServerConfig serverConfig = serverConfigService.getServerConfig(null, user.getId(), contextId);

        try {
            Builder mailData = MailData.newBuilder()
                .setRecipient(new QuotedInternetAddress(user.getMail(), user.getDisplayName()))
                .setHtmlTemplate(mail.getTemplateName() + ".html.tmpl")
                .setTextContent(mail.getText())
                .setTemplateVars(mail.getEnvironment())
                .setMailConfig(serverConfig.getNotificationMailConfig())
                .setContext(context)
                .addMailHeader("X-Open-Xchange-Alarm-Type", "EMAIL")
                .setSubject(mail.getSubject())
                ;
            return new MailAlarmMailGenerator(mailData.build(), services);
        } catch (AddressException | UnsupportedEncodingException e) {
            throw MailAlarmExceptionCodes.INVALID_MAIL_ADDRESS.create(e, user.getMail());
        }
    }

    public ComposedMailMessage compose() throws OXException {
        NotificationMailFactory notificationMailFactory = requireService(NotificationMailFactory.class, services);
        return notificationMailFactory.createMail(mailData);
    }
}
