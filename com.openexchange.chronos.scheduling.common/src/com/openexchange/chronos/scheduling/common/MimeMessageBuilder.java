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

package com.openexchange.chronos.scheduling.common;

import static com.openexchange.chronos.scheduling.common.Constants.HEADER_AUTO_SUBMITTED;
import static com.openexchange.chronos.scheduling.common.Constants.HEADER_DATE;
import static com.openexchange.chronos.scheduling.common.Constants.HEADER_DISPNOTTO;
import static com.openexchange.chronos.scheduling.common.Constants.HEADER_ORGANIZATION;
import static com.openexchange.chronos.scheduling.common.Constants.HEADER_XPRIORITY;
import static com.openexchange.chronos.scheduling.common.Constants.HEADER_X_MAILER;
import static com.openexchange.chronos.scheduling.common.Constants.VALUE_AUTO_GENERATED;
import static com.openexchange.chronos.scheduling.common.Constants.VALUE_PRIORITYNORM;
import static com.openexchange.chronos.scheduling.common.Constants.VALUE_X_MAILER;
import static com.openexchange.chronos.scheduling.common.MailUtils.generateHeaderValue;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.parseAddressList;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;
import com.openexchange.version.VersionService;

/**
 * {@link MimeMessageBuilder} - builds the actual mail based on the given data
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class MimeMessageBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MimeMessageBuilder.class);

    private final SchedulingMessage message;

    private final int contextId;

    private final MimeMessage mime;

    /**
     * Initializes a new {@link MimeMessageBuilder}.
     * 
     * @param contextId The context identifier
     * @param message The {@link SchedulingMessage}
     */
    public MimeMessageBuilder(int contextId, SchedulingMessage message) {
        super();
        this.contextId = contextId;
        this.message = message;
        this.mime = new MimeMessage(MimeDefaultSession.getDefaultSession());
    }

    /**
     * Set the <code>FROM</code> header
     * 
     * @return This {@link MimeMessageBuilder} instance
     * @throws OXException If mail is missing
     */
    public MimeMessageBuilder setFrom() throws OXException {
        try {
            InternetAddress[] addresses = parseAddressList(getQuotedAddress(message.getOriginator()), false, false);
            checkAddress(addresses, message.getOriginator());
            mime.setFrom(addresses[0]);
            mime.setReplyTo(addresses);
        } catch (MessagingException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Originators email could not be set", e);
        }
        return this;
    }

    /**
     * Set the <code>TO</code> header
     * 
     * @return This {@link MimeMessageBuilder} instance
     * @throws OXException If mail is missing
     */
    public MimeMessageBuilder setTo() throws OXException {
        try {
            InternetAddress[] addresses = parseAddressList(getQuotedAddress(message.getRecipient()), false, false);
            checkAddress(addresses, message.getOriginator());
            mime.setRecipients(RecipientType.TO, addresses);
        } catch (MessagingException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Recipient email could not be set", e);
        }
        return this;
    }

    /**
     * Set the <code>SUBJECT</code> header
     * 
     * @param userService The {@link UserService} to get locale information from
     * @param contextId The context identifier
     * @return This {@link MimeMessageBuilder} instance
     * @throws MessagingException If subject can't be set
     * @throws OXException In case participant status can't be found when generating subject for a reply
     */
    public MimeMessageBuilder setSubject(UserService userService, int contextId) throws MessagingException, OXException {
        Locale locale = Utils.getLocale(userService, contextId, message.getOriginator(), message.getRecipient());
        StringHelper helper = StringHelper.valueOf(locale);
        String summary = MailUtils.getSummary(message.getResource().getCalendarObject());
        String subject;
        switch (message.getMethod()) {
            case ADD:
                subject = Messages.SUBJECT_CHANGED_APPOINTMENT;
                break;
            case CANCEL:
                subject = Messages.SUBJECT_CANCELLED_APPOINTMENT;
                break;
            case COUNTER:
                subject = Messages.SUBJECT_COUNTER_APPOINTMENT;
                break;
            case DECLINE_COUNTER:
                subject = Messages.SUBJECT_DECLINECOUNTER;
                break;
            case PUBLISH:
                subject = Messages.SUBJECT_NEW_APPOINTMENT;
                break;
            case REFRESH:
                subject = Messages.SUBJECT_REFRESH;
                break;
            case REPLY:
                subject = Messages.SUBJECT_STATE_CHANGED;
                //@formatter:off
                mime.setSubject(
                    String.format(
                        helper.getString(subject), 
                        message.getOriginator().getCn(), 
                        ContextSensitiveMessages.getInstance().getDescription(getOriginatorPartStat(), locale, ContextSensitiveMessages.Context.VERB),
                        summary
                        ), 
                    MailProperties.getInstance().getDefaultMimeCharset());
                //@formatter:on
                return this;
            case REQUEST:
                switch (message.getDescription().getAction()) {
                    case CREATE:
                        subject = Messages.SUBJECT_NEW_APPOINTMENT;
                        break;
                    default:
                        subject = Messages.SUBJECT_CHANGED_APPOINTMENT;
                        break;
                }
                break;
            default:
                subject = Messages.SUBJECT_NONE;
                break;
        }
        subject = String.format(helper.getString(subject), summary);
        mime.setSubject(subject, MailProperties.getInstance().getDefaultMimeCharset());
        return this;
    }

    /**
     * Creates the payload aka the content. Text, HTML and iCal part will be appended to the mail.
     * Optional attachments of the scheduling event will be added too.
     * 
     * @param factory An {@link MimePartFactory}
     * @return This {@link MimeMessageBuilder} instance
     * @throws MessagingException If appending fails
     * @throws OXException If appending fails
     */
    public MimeMessageBuilder setContent(@NonNull MimePartFactory factory) throws MessagingException, OXException {
        MessageUtility.setContent(factory.create(), mime);
        return this;
    }

    /**
     * Adds additional header.
     * <p>
     * Headers will be looked up with {@link SchedulingMessage#getAdditional(String, Class)}. Used keys:
     * <li> {@link Constants#ADDITIONAL_HEADER_READ_RECEIPT}</li>
     * <li> {@link Constants#ADDITIONAL_HEADER_MAIL_HEADERS}</li>
     * 
     * @return This {@link MimeMessageBuilder} instance
     * @throws MessagingException If setting fails
     */
    public MimeMessageBuilder setAdditionalHeader() throws MessagingException {
        Boolean readReceipt = message.getAdditional(Constants.ADDITIONAL_HEADER_READ_RECEIPT, Boolean.class);
        if (null != readReceipt && readReceipt.booleanValue()) {
            mime.setHeader(HEADER_DISPNOTTO, message.getOriginator().getEMail());
        }

        @SuppressWarnings("unchecked") Map<String, String> additionalHeaders = message.getAdditional(Constants.ADDITIONAL_HEADER_MAIL_HEADERS, Map.class);
        if (null != additionalHeaders) {
            for (Map.Entry<String, String> header : additionalHeaders.entrySet()) {
                mime.setHeader(header.getKey(), header.getValue());
            }
        }
        return this;
    }

    /**
     * Set the {@value #HEADER_XPRIORITY} header
     * 
     * @return This {@link MimeMessageBuilder} instance
     * @throws MessagingException If header can't be set
     */
    public MimeMessageBuilder setPriority() throws MessagingException {
        mime.setHeader(HEADER_XPRIORITY, VALUE_PRIORITYNORM);
        return this;
    }

    /**
     * Set the header necessary for processing notification mails in the UI
     * 
     * @return This {@link MimeMessageBuilder} instance
     * @throws MessagingException If header can't be set
     * @throws OXException In case folder can't be loaded
     */
    public MimeMessageBuilder setOXHeader() throws MessagingException, OXException {
        if (false == Utils.isInternalCalendarUser(message.getRecipient())) {
            return this;
        }

        List<Event> calendarObject = message.getResource().getCalendarObject();
        String objectId = calendarObject.get(0).getId();
        String folderId;
        if (null != calendarObject.get(0).getFolderId()) {
            folderId = calendarObject.get(0).getFolderId();
        } else {
            if (CalendarUtils.isInternal(message.getRecipient(), CalendarUserType.INDIVIDUAL)) {
                folderId = CalendarUtils.getFolderView(calendarObject.get(0), message.getRecipient().getEntity());
            } else {
                folderId = CalendarUtils.getFolderView(calendarObject.get(0), message.getOriginator().getEntity());
            }
        }

        mime.setHeader(Constants.HEADER_X_OX_REMINDER, new StringBuilder().append(objectId).append(',').append(folderId).append(',').append(1).toString());
        mime.setHeader(Constants.HEADER_X_OX_MODULE, Constants.VALUE_X_OX_MODULE);
        mime.setHeader(Constants.HEADER_X_OX_TYPE, message.getDescription().getAction().toString());
        mime.setHeader(Constants.HEADER_X_OX_OBJECT, objectId);
        mime.setHeader(Constants.HEADER_X_OX_UID, calendarObject.get(0).getUid());
        if (null != calendarObject.get(0).getRecurrenceId()) {
            mime.setHeader(Constants.HEADER_X_OX_RECURRENCE_DATE, calendarObject.get(0).getRecurrenceId().toString());
        }

        return this;
    }

    /**
     * Set the {@value #HEADER_X_MAILER} header
     * 
     * @param versionService The {@link VersionService} to obtain the server version from
     * @return This {@link MimeMessageBuilder} instance
     * @throws MessagingException If subject can't be set
     */
    public MimeMessageBuilder setMailerInfo(VersionService versionService) throws MessagingException {
        String mailerInfo;
        if (MailProperties.getInstance().isAppendVersionToMailerHeader() && null != versionService) {
            mailerInfo = new StringBuilder("Open-Xchange Mailer v").append(versionService.getVersionString()).toString();
        } else {
            mailerInfo = VALUE_X_MAILER;
        }
        mime.setHeader(HEADER_X_MAILER, mailerInfo);
        return this;
    }

    /**
     * Set <code>MESSAGE_ID</code>, <code>IN_REPLY_TO</code> and the <code>REFERENCE</code>
     * header
     * 
     * @param uid The unique ID of the calendar event
     * @return This {@link MimeMessageBuilder} instance
     * @throws MessagingException In case header can't be set
     */
    public MimeMessageBuilder setTracing(String uid) throws MessagingException {
        mime.setHeader(MessageHeaders.HDR_MESSAGE_ID, generateHeaderValue(uid, true));
        String reference = generateHeaderValue(uid, false);
        mime.setHeader(MessageHeaders.HDR_IN_REPLY_TO, reference);
        mime.setHeader(MessageHeaders.HDR_REFERENCES, reference);
        return this;
    }

    /**
     * Set the {@value #HEADER_ORGANIZATION} if the organization can be retrieved by the {@link ContactService}
     * 
     * @param contactService The {@link ContactService}
     * @return This {@link MimeMessageBuilder} instance
     * @throws MessagingException If header can't be set
     */
    public MimeMessageBuilder setOrganization(ContactService contactService, Session session) throws MessagingException {
        if (null == contactService) {
            return this;
        }
        try {
            final String organization = contactService.getOrganization(session);
            if (null != organization && 0 < organization.length()) {
                mime.setHeader(HEADER_ORGANIZATION, organization);
            }
        } catch (final OXException e) {
            LOGGER.warn("Header \"Organization\" could not be set", e);
        }
        return this;
    }

    /**
     * Set the {@value #HEADER_DATE} header.
     * 
     * @param userService The {@link UserService} to get time zone information from
     * @return This {@link MimeMessageBuilder} instance
     * @throws MessagingException If header can't be set
     * @throws OXException If date format can't be obtained
     */
    public MimeMessageBuilder setSentDate(UserService userService) throws MessagingException, OXException {
        /*
         * Set sent date in UTC time
         */
        if (mime.getSentDate() == null) {
            final MailDateFormat mdf = MimeMessageUtility.getMailDateFormat(Utils.getTimeZone(userService, contextId, message.getOriginator(), message.getRecipient()).getID());
            synchronized (mdf) {
                mime.setHeader(HEADER_DATE, mdf.format(new Date()));
            }
        }
        return this;
    }

    /**
     * Set the auto generated flag via the {@value #HEADER_AUTO_SUBMITTED} header
     * 
     * @return This {@link MimeMessageBuilder} instance
     * @throws MessagingException
     */
    public MimeMessageBuilder setAutoGenerated() throws MessagingException {
        mime.setHeader(HEADER_AUTO_SUBMITTED, VALUE_AUTO_GENERATED);
        return this;
    }

    /**
     * Builds the {@link MimeMessage}
     * 
     * @return The {@link MimeMessage}
     */
    public MimeMessage build() {
        return mime;
    }

    /*
     * ================ HELPERS ================
     */

    /**
     * 
     * Checks if the given {@link InternetAddress} are not <code>null</code> or <code>empty</code>
     *
     * @param addresses The addresses to check
     * @param calendarUser For logging purpose
     * @throws OXException If address
     */
    private void checkAddress(InternetAddress[] addresses, CalendarUser calendarUser) throws OXException {
        if (null == addresses || 1 != addresses.length) {
            throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(calendarUser.getUri(), I(calendarUser.getEntity()), CalendarUserType.INDIVIDUAL);
        }
    }

    /**
     * Get the updated {@link ParticipationStatus} of the originator based on the given events by {@link #message}
     * 
     * @return The {@link ParticipationStatus} of the originator
     * @throws OXException In case participant status can't be found
     */
    private ParticipationStatus getOriginatorPartStat() throws OXException {
        if (Attendee.class.isAssignableFrom(message.getOriginator().getClass())) {
            return ((Attendee) message.getOriginator()).getPartStat();
        }
        for (Event e : message.getResource().getCalendarObject()) {
            Attendee attendee = CalendarUtils.find(e.getAttendees(), message.getOriginator().getEntity());
            if (null != attendee) {
                return attendee.getPartStat();
            }
        }
        throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Unable to find correct participant status");
    }

    /**
     * Tries to generate a {@link QuotedInternetAddress} based on the common name of the
     * given calendar user
     *
     * @param calendarUser The calendar user to generate the address for
     * @return The address as {@link String}
     */
    private String getQuotedAddress(CalendarUser calendarUser) {
        String displayName = calendarUser.getCn();
        if (displayName != null) {
            try {
                return new QuotedInternetAddress(calendarUser.getEMail(), displayName, "UTF-8").toUnicodeString();
            } catch (AddressException | UnsupportedEncodingException e) {
                LOGGER.warn("Interned address could not be generated. Returning fall-back instead.", e);
                return "\"" + displayName + "\"" + " <" + calendarUser.getEMail() + ">";
            }
        }
        // Without personal part
        return calendarUser.getEMail();
    }

}
