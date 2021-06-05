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

package com.openexchange.chronos.scheduling.common;

import static com.openexchange.chronos.scheduling.common.MailUtils.saveChangesSafe;
import java.util.Locale;
import java.util.Map;
import javax.mail.internet.MimeMessage;
import com.openexchange.annotation.NonNull;
import com.openexchange.authentication.application.AppPasswordUtils;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.itip.ContextSensitiveMessages.Context;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.scheduling.ChangeNotification;
import com.openexchange.chronos.scheduling.ScheduleStatus;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.TransportProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AbstractMailTransportProvider}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public abstract class AbstractMailTransportProvider implements TransportProvider {

    protected final @NonNull ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link AbstractMailTransportProvider}.
     *
     * @param serviceLookup The {@link ServiceLookup}
     */
    public AbstractMailTransportProvider(@NonNull ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    protected @NonNull ScheduleStatus transportMail(Session session, MimeMessage mime) throws OXException {
        saveChangesSafe(serviceLookup.getOptionalService(HostnameService.class), mime, session.getContextId(), session.getUserId());
        MailTransport transport;
        com.openexchange.mail.transport.TransportProvider provider = TransportProviderRegistry.getTransportProvider("smtp");
        if (preferNoReplyAccount(session)) {
            transport = provider.createNewNoReplyTransport(session.getContextId(), false);
        } else {
            transport = provider.createNewMailTransport(session);
        }

        try {
            transport.sendMailMessage(new ContentAwareComposedMailMessage(mime, session, null), ComposeType.NEW);
        } finally {
            transport.close();
        }

        return ScheduleStatus.SENT;
    }

    /**
     * Gets a value indicating whether to prefer the <i>no-reply</i> transport account when sending notification mails, or to stick to
     * the user's primary mail transport account instead.
     * <p/>
     * By default, the decisions is made based on the user's and session's capabilities. Override if applicable.
     *
     * @param session The session to decide the preference for
     * @return <code>true</code> if the no-reply account should be used, <code>false</code>, otherwise
     */
    protected boolean preferNoReplyAccount(Session session) throws OXException {
        /*
         * use no-reply if user has no mail module permission or is a guest
         */
        if (null == session) {
            return true;
        }
        UserConfiguration userConfiguration = ServerSessionAdapter.valueOf(session).getUserConfiguration();
        if (userConfiguration.isGuest() || false == userConfiguration.hasWebMail()) {
            return true;
        }
        /*
         * otherwise use no-reply only if session is restricted and has no required scope
         */
        return false == AppPasswordUtils.isNotRestrictedOrHasScopes(session, "write_mail");
    }

    protected Map<String, String> getAdditionalHeaders(ChangeNotification notification) {
        return notification.getAdditional(Constants.ADDITIONAL_HEADER_MAIL_HEADERS, Map.class);
    }

    protected Map<String, String> getAdditionalHeaders(SchedulingMessage message) {
        return message.getAdditional(Constants.ADDITIONAL_HEADER_MAIL_HEADERS, Map.class);
    }

    /**
     * Get the subject for an changed participant status
     *
     * @param originator The originator of the message
     * @param partStat The participant status of the originator
     * @param locale The locale
     * @param summary The summary of the event
     * @return The constructed and translated String {@link Messages#SUBJECT_STATE_CHANGED}
     */
    protected String getPartStatSubject(CalendarUser originator, ParticipationStatus partStat, Locale locale, String summary) {
        //@formatter:off
        StringHelper helper = StringHelper.valueOf(locale);
        return String.format(
            helper.getString(Messages.SUBJECT_STATE_CHANGED),
            Utils.getDisplayName(originator),
            com.openexchange.chronos.itip.ContextSensitiveMessages.partStat(partStat, locale, Context.VERB),
            summary);
        //@formatter:on
    }

}
