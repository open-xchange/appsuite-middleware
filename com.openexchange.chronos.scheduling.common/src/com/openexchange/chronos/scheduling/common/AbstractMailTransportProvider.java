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
 *    trademarks of the OX Software GmbH. group of companies.
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
         * use no-reply if user has no mail module permission
         */
        if (null == session || false == ServerSessionAdapter.valueOf(session).getUserConfiguration().hasWebMail()) {
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
