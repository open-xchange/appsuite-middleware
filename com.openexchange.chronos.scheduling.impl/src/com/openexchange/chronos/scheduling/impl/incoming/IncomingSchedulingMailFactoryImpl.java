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

package com.openexchange.chronos.scheduling.impl.incoming;

import static com.openexchange.chronos.scheduling.impl.incoming.MailUtils.closeMailAccess;
import static com.openexchange.chronos.scheduling.impl.incoming.MailUtils.getAttachmentPart;
import static com.openexchange.chronos.scheduling.impl.incoming.MailUtils.getMailAccess;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Calendar;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.IncomingCalendarObjectResource;
import com.openexchange.chronos.common.IncomingSchedulingMessageBuilder;
import com.openexchange.chronos.common.IncomingSchedulingObjectBuilder;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.itip.IncomingSchedulingMailData;
import com.openexchange.chronos.itip.IncomingSchedulingMailFactory;
import com.openexchange.chronos.scheduling.IncomingSchedulingMessage;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.crypto.CryptographicAwareMailAccessFactory;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.arrays.Collections;
import com.openexchange.user.User;

/**
 * {@link IncomingSchedulingMailFactoryImpl}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.6
 */
public class IncomingSchedulingMailFactoryImpl implements IncomingSchedulingMailFactory {

    protected static final Logger LOGGER = LoggerFactory.getLogger(IncomingSchedulingMailFactoryImpl.class);

    protected final ServiceLookup services;

    /**
     * Initializes a new {@link IncomingSchedulingMailFactoryImpl}.
     * 
     * @param services The service lookup
     */
    public IncomingSchedulingMailFactoryImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public IncomingSchedulingMessage create(CalendarSession session, IncomingSchedulingMailData data) throws OXException {
        return create(session, data, false);
    }

    @Override
    public IncomingSchedulingMessage createPatched(CalendarSession session, IncomingSchedulingMailData data) throws OXException {
        return create(session, data, true);
    }

    private IncomingSchedulingMessage create(CalendarSession session, IncomingSchedulingMailData data, boolean patchEvents) throws OXException {
        IncomingSchedulingMessageBuilder builder = IncomingSchedulingMessageBuilder.newBuilder();
        builder.setTimeStamp(new Date(System.currentTimeMillis()));

        /*
         * Load mail and gather meta data
         */
        int accountId = data.getFullnameArgument().getAccountId();
        String folderId = data.getFullnameArgument().getFullname();
        MailAccess<?, ?> mailAccess = null;
        MailMessage mail;
        ImportedCalendar calendar;
        try {
            mailAccess = getMailAccess(services.getOptionalService(CryptographicAwareMailAccessFactory.class), session.getSession(), accountId);
            MailFolder folder = mailAccess.getFolderStorage().getFolder(folderId);
            int userId = -1;
            /*
             * Check if the session user is working on behalf of another user
             */
            if (folder.isShared()) {
                String owner = folder.getOwner();
                Context context = services.getServiceSafe(ContextService.class).getContext(session.getContextId());
                final User[] users = UserStorage.getInstance().searchUserByMailLogin(owner, context);
                if (null != users && users.length == 1) {
                    userId = users[0].getId();
                } else {
                    throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Unable to determine folder owner");
                }
            }
            builder.setTargetUser(userId <= 0 ? session.getUserId() : userId);
            /*
             * Load mail and close access
             */
            mail = mailAccess.getMessageStorage().getMessage(folderId, data.getMailId(), false);
            calendar = getCalendar(services, mailAccess, mail, folderId, data.getMailId(), data.getSequenceId());
        } finally {
            closeMailAccess(mailAccess);
        }
        /*
         * Gather meta data
         */
        builder.setSchedulingObject(IncomingSchedulingObjectBuilder.newBuilder().setOriginator(getOriginator(mail)).build());
        /*
         * Prepare calendar data and load attachments as needed
         */
        builder.setMethod(SchedulingMethod.valueOf(calendar.getMethod()));
        if (patchEvents) {
            calendar = ITipPatches.applyAll(calendar);
        }
        List<Event> events = calendar.getEvents();
        events = linkBinaryAttachments(mail, events, new ShortLivingMailAccess(services, session, folderId, data.getMailId(), accountId));
        builder.setResource(new IncomingCalendarObjectResource(events));
        return builder.build();
    }

    /*
     * ============================== HELPERS ==============================
     */

    private static CalendarUser getOriginator(MailMessage mail) throws OXException {
        InternetAddress[] addresses = mail.getFrom();
        if (null != addresses && 1 == addresses.length) {
            InternetAddress from = addresses[0];
            CalendarUser calendarUser = new CalendarUser();
            calendarUser.setEMail(from.getAddress());
            calendarUser.setUri(CalendarUtils.getURI(from.getAddress()));
            calendarUser.setCn(from.getPersonal());
            return calendarUser;
        }
        throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Can't find originator.");
    }

    /**
     * Get the iCAL file from the mail as-is and parses it into an {@link Calendar} object
     * 
     * @param services The service lookup
     * @param mailAccess The access to mails
     * @param mail The loaded mail to process
     * @param folderId The mail folder
     * @param mailId The mail identifier
     * @param sequenceId The attachments sequence identifier of the iCAL
     *
     * @return A {@link Calendar} containing the iCAL from the mail
     * @throws OXException If service is missing, iCAL can't be obtained or importing fails
     */
    private static ImportedCalendar getCalendar(ServiceLookup services, MailAccess<?,?> mailAccess, MailMessage mail, String folderId, String mailId, String sequenceId) throws OXException {
        InputStream iCal = null;
        try {
            iCal = getIcalFromMail(mailAccess, mail, folderId, mailId, sequenceId);
            ICalService iCalService = services.getServiceSafe(ICalService.class);
            ICalParameters parameters = iCalService.initParameters();
            /*
             * Don't ignore unset fields explicitly. E.g. unset attachment field in incoming REQUEST method indicates removed attachments.
             */
            parameters.set(ICalParameters.IGNORE_UNSET_PROPERTIES, Boolean.FALSE);
            parameters.set(ICalParameters.IGNORE_ALARM, Boolean.TRUE);
            return iCalService.importICal(iCal, parameters);
        } finally {
            Streams.close(iCal);
        }
    }

    /**
     * Get the iCAL file containing the calendar object resource from the given mail
     *
     * @param mailAccess The mail access to use
     * @param mail The mail to process
     * @param folderId The folder of the mail
     * @param mailId The identifier of the mail
     * @param sequenceId The sequence identifier of the .ics file, can be <code>null</code> for automatic detection
     * @return The iCAL file as {@link InputStream}
     * @throws OXException In case file can't be loaded
     */
    private static InputStream getIcalFromMail(MailAccess<?,?> mailAccess, MailMessage mail, String folderId, String mailId, String sequenceId) throws OXException {
        MailPart mailPart;
        if (Strings.isNotEmpty(sequenceId)) {
            mailPart = mailAccess.getMessageStorage().getAttachment(folderId, mailId, sequenceId);
        } else {
            mailPart = MailUtils.getIcalAttachmentPart(mail);
        }
        if (null == mailPart) {
            throw MailExceptionCode.ATTACHMENT_NOT_FOUND.create(sequenceId, mailId, folderId);
        }
        mailPart.loadContent();
        return mailPart.getInputStream();
    }

    /*
     * ============================== Attachments ==============================
     */

    /**
     * Prepares attachment data by linking binary attachments to their representation in the mail
     *
     * @param mail The mail to get the attachments from
     * @param events The calendar data as events
     * @param supplier Supplier for lazy loading attachment binaries
     * @return The calendar data enriched with the initialized attachments
     * @throws OXException in case of error
     * 
     */
    private List<Event> linkBinaryAttachments(MailMessage mail, List<Event> events, ShortLivingMailAccess access) throws OXException {
        List<Event> modified = EventMapper.getInstance().copy(events, (EventField[]) null);
        for (ListIterator<Event> iterator = modified.listIterator(); iterator.hasNext();) {
            Event event = iterator.next();
            if (false == Collections.isNullOrEmpty(event.getAttachments())) {
                try {
                    Event copy = EventMapper.getInstance().copy(event, null, (EventField[]) null);
                    copy.setAttachments(prepareBinaryAttachments(mail, copy.getAttachments(), access));
                    iterator.set(copy);
                } catch (OXException e) {
                    LOGGER.debug("Unable to copy", e);
                }
            }
        }
        return modified;
    }

    /**
     * Prepares attachments transmitted with the incoming message
     *
     * @param mail The mail
     * @param originalAttachments The attachments to find
     * @param in The object to get the (binary) attachments from
     * @return The filtered and existing attachments
     * @throws OXException
     */
    private List<Attachment> prepareBinaryAttachments(MailMessage mail, List<Attachment> originalAttachments, ShortLivingMailAccess access) throws OXException {
        List<Attachment> attachments = new ArrayList<>(originalAttachments.size());
        for (Attachment attachment : originalAttachments) {
            MailPart part = getAttachmentPart(mail, attachment.getUri());
            if (null != part) {
                attachments.add(prepareAttachment(part, attachment, access));
            } else {
                attachments.add(attachment);
            }
        }
        return attachments;
    }

    /**
     * Get an attachment based on the supplied part
     * 
     * @param part The mail part aka. the attachment
     * @param uri The attachment transmitted in the as iCAL
     * @return An adjusted attachment
     */
    private Attachment prepareAttachment(MailPart part, Attachment ref, ShortLivingMailAccess access) {
        Attachment attachment = new Attachment();
        attachment.setUri(part.getContentId()); // use part, avoids adjusting
        attachment.setChecksum(ref.getChecksum());
        if (Strings.isNotEmpty(part.getFileName())) {
            // Prefer mail announced name, see Bug65533
            attachment.setFilename(part.getFileName());
        } else {
            attachment.setFilename(ref.getFilename());
        }
        if (Strings.isNotEmpty(part.getContentType().getBaseType())) {
            attachment.setFormatType(part.getContentType().getBaseType());
        } else if (Strings.isNotEmpty(ref.getFormatType())) {
            attachment.setFormatType(ref.getFormatType());
        }
        attachment.setSize(ref.getSize());
        attachment.setData(new MailAttachmentFileHolder(access, part.getContentId(), attachment.getFilename(), attachment.getFormatType(), attachment.getSize()));
        return attachment;
    }

}
