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
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.internet.AddressException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.alarm.mail.exception.MailAlarmExceptionCodes;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.notification.mail.MailData;
import com.openexchange.notification.mail.MailData.Builder;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;

/**
 * 
 * {@link MailAlarmMail}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class MailAlarmMail {
    
    private static final Logger LOG = LoggerFactory.getLogger(MailAlarmMail.class.getName());

    static final String DESCRIPTION = "description";
    static final String SUMMARY = "summary";
    static final String STARTDATE = "start_date";
    static final String LOCATION = "location";
    static final String ATTENDEES = "attendees";
    static final String URL = "url";


    private final ServiceLookup services;
    private final MailData mailData;

    protected MailAlarmMail(MailData mailData, ServiceLookup services) {
        super();
        this.services = services;
        this.mailData = mailData;
    }

    public ComposedMailMessage compose() throws OXException {
        NotificationMailFactory notificationMailFactory = services.getService(NotificationMailFactory.class);
        return notificationMailFactory.createMail(mailData);
    }

    public static MailAlarmMail init(MailAlarmNotification notification, ServiceLookup services) throws OXException {
        ServerConfigService serverConfigService = requireService(ServerConfigService.class, services);
        ContextService contextService = requireService(ContextService.class, services);
        Context context = contextService.getContext(notification.getContextId());

        ServerConfig serverConfig = serverConfigService.getServerConfig(null, notification.getTargetUser().getId(), notification.getContextId());

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put(MailAlarmMail.DESCRIPTION, notification.getEvent().getDescription());
        vars.put(MailAlarmMail.SUMMARY, notification.getEvent().getSummary());
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT, notification.getTargetUser().getLocale());
        String formattedDate = df.format(new Date(notification.getEvent().getStartDate().getTimestamp()));
        DateFormat dfEnd = DateFormat.getTimeInstance(DateFormat.SHORT, notification.getTargetUser().getLocale());
        String formattedEndDate = dfEnd.format(new Date(notification.getEvent().getEndDate().getTimestamp()));
        String completeDate = formattedDate  + " - "+formattedEndDate;
        vars.put(MailAlarmMail.STARTDATE, MailAlarmMailStrings.WHEN + ": " + completeDate);
        vars.put(MailAlarmMail.LOCATION, MailAlarmMailStrings.WHERE + ": " + notification.getEvent().getLocation());
        vars.put(MailAlarmMail.ATTENDEES, MailAlarmMailStrings.WHO + ": " + prepareAttendees(notification.getEvent().getAttendees()));
        vars.put(MailAlarmMail.URL, generateInternalURL(services, notification.getEvent(), notification.getTargetUser()));

        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        Translator translator = translatorFactory.translatorFor(notification.getTargetUser().getLocale());

        try {
            Builder mailData = MailData.newBuilder()
                .setRecipient(new QuotedInternetAddress(notification.getTargetUser().getMail(), notification.getTargetUser().getDisplayName()))
                .setHtmlTemplate("notify.mail.alarm.mail.html.tmpl")
                .setTemplateVars(vars)
                .setMailConfig(serverConfig.getNotificationMailConfig())
                .setContext(context)
                .addMailHeader("X-Open-Xchange-Alarm-Type", "EMAIL")
                .setSubject(translator.translate(MailAlarmMailStrings.NOTIFICATION) + ": " + notification.getEvent().getSummary() + " - " + completeDate);
            return new MailAlarmMail(mailData.build(), services);
        } catch (AddressException | UnsupportedEncodingException e) {
            throw MailAlarmExceptionCodes.INVALID_MAIL_ADDRESS.create(e, notification.getTargetUser().getMail());
        }
    }
    
    private static String prepareAttendees(List<Attendee> attendees) {
        StringBuilder builder = new StringBuilder();
        for (Attendee attendee : attendees) {
            builder.append(attendee.getEMail() + ", ");
        }
        String string = builder.toString();
        return string.substring(0, string.length() - 2);
    }

    private static String generateInternalURL(ServiceLookup services, Event event, User user) throws OXException {
        final ConfigurationService config = requireService(ConfigurationService.class, services);
        String webpath = config.getProperty("com.openexchange.UIWebPath", "/appsuite/");
        if (webpath.startsWith("/")) {
            webpath = webpath.substring(1, webpath.length());
        }
        final String objectId = event.getId();
        final String module = "calendar";
        String folder = event.getFolderId();
        if (folder == null) {
            int recipientId = user.getId();
            try {
                folder = CalendarUtils.DEFAULT_ACCOUNT_PREFIX + CalendarUtils.getFolderView(event, recipientId);
            } catch (OXException e) {
                LOG.error("Unable to generate link to event. Folder Id for user {} can't be found.", Integer.valueOf(recipientId), e);
                return "";
            }
        }
        String url = "/[uiwebpath]#m=[module]&i=[object]&f=[folder]";
        return url.replaceAll("\\[uiwebpath\\]", webpath).replaceAll("\\[module\\]", module).replaceAll("\\[object\\]", objectId).replaceAll("\\[folder\\]", folder);
    }
}
