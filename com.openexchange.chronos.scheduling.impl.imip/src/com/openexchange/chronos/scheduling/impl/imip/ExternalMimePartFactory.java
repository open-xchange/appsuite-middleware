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

package com.openexchange.chronos.scheduling.impl.imip;

import static com.openexchange.chronos.scheduling.common.Constants.ALTERNATIVE;
import static com.openexchange.chronos.scheduling.common.Constants.CANCLE_FILE_NAME;
import static com.openexchange.chronos.scheduling.common.Constants.INVITE_FILE_NAME;
import static com.openexchange.chronos.scheduling.common.Constants.MIXED;
import static com.openexchange.chronos.scheduling.common.Constants.RESPONSE_FILE_NAME;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.ical.CalendarExport;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.scheduling.SchedulingMessage;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.scheduling.common.AbstractMimePartFactory;
import com.openexchange.chronos.scheduling.common.AttachmentDataSource;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ExternalMimePartFactory} - Generates an iMIP mail
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 * @see <a href="https://tools.ietf.org/html/rfc6047">RFC6047 - iCalendar Message-Based Interoperability Protocol (<b>iMIP</b>)</a>
 */
public class ExternalMimePartFactory extends AbstractMimePartFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalMimePartFactory.class);

    private static final String CHARSET = MailProperties.getInstance().getDefaultMimeCharset();

    private ICalService iCalService;

    private final SchedulingMessage message;

    /**
     * Initializes a new {@link ExternalMimePartFactory}.
     * 
     * @param message The {@link SchedulingMessage}
     * @param serviceLookup The {@link ServiceLookup}
     * @throws OXException In case services are missing
     */
    public ExternalMimePartFactory(ServiceLookup serviceLookup, SchedulingMessage message) throws OXException {
        super(serviceLookup, message.getScheduleChange(), message.getRecipientSettings());
        this.iCalService = serviceLookup.getServiceSafe(ICalService.class);
        this.message = message;
    }

    /**
     * Adds the mime parts based on the given message
     * 
     * @return The {@link MimeMultipart} to send
     * 
     * @throws OXException
     * @throws MessagingException
     */
    @Override
    public MimeMultipart create() throws OXException, MessagingException {
        boolean addAttachments = addAttachments();
        MimeMultipart multipart = new MimeMultipart(MIXED);
        /*
         * Add event's attachments
         */
        if (addAttachments) {
            multipart = generateAttachmentPart(multipart);
        }

        /*
         * Set text, HTML and embedded iCal part
         */
        MimeBodyPart part = new MimeBodyPart();
        {
            MimeMultipart alternative = new MimeMultipart(ALTERNATIVE);
            alternative.addBodyPart(generateTextPart());
            alternative.addBodyPart(generateHtmlPart());
            alternative.addBodyPart(generateIcalPart(addAttachments));
            MessageUtility.setContent(alternative, part);
        }
        multipart.addBodyPart(part);

        /*
         * Add the iCal file as attachment
         */
        multipart.addBodyPart(generateIcalAttachmentPart(addAttachments));

        return multipart;
    }

    /*
     * ----------------------------- HELPERS -----------------------------
     */

    /**
     * Check if the {@link CalendarObjectResource} has any attachments to add to the mail.
     * 
     * @return <code>true</code> if at least one internal attachment needs to be added to the mail, <code>false</code> otherwise
     */
    private boolean addAttachments() {
        switch (message.getMethod()) {
            case CANCEL:
            case REPLY:
            case DECLINE_COUNTER:
                return false;
            default:
                break;
        }
        for (Event e : message.getResource().getEvents()) {
            if (e.containsAttachments() && null != e.getAttachments() && false == e.getAttachments().isEmpty()) {
                for (Attachment a : e.getAttachments()) {
                    if (a.getManagedId() > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private MimeBodyPart generateIcalAttachmentPart(boolean addAttachments) throws MessagingException, OXException {
        String fileName = determineFileName();
        ContentType ct = new ContentType();
        ct.setPrimaryType("application");
        ct.setSubType("ics");
        ct.setNameParameter(fileName);
        ct.setParameter("method", message.getMethod().name());
        ct.setCharsetParameter(CHARSET);

        MimeBodyPart part = generateIcal(ct, addAttachments, false);
        /*
         * Content-Disposition & Content-Transfer-Encoding
         */
        ContentDisposition cd = new ContentDisposition();
        cd.setAttachment();
        cd.setFilenameParameter(fileName);
        part.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, cd.toString());
        part.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
        return part;

    }

    private MimeBodyPart generateIcalPart(boolean addAttachments) throws MessagingException, OXException {
        ContentType ct = new ContentType();
        ct.setPrimaryType(TEXT);
        ct.setSubType("calendar");
        ct.setParameter("method", message.getMethod().name());
        ct.setCharsetParameter(CHARSET);

        return generateIcal(ct, addAttachments, true);
    }

    private MimeBodyPart generateIcal(ContentType contentType, boolean addAttachments, boolean checkASCII) throws MessagingException, OXException {
        MimeBodyPart icalPart = new MimeBodyPart();

        CalendarExport export = iCalService.exportICal(iCalService.initParameters());
        export.setMethod(message.getMethod().name());

        for (Event e : message.getResource().getEvents()) {
            Event event = EventMapper.getInstance().copy(e, new Event(), (EventField[]) null);
            switch (message.getMethod()) {
                case REPLY:
                    event = adjustAttendees(event);
                    break;
                case CANCEL:
                    adjustSequence(event);
                    break;
                default:
                    // Do nothing special
                    break;
            }
            export.add(addAttachments ? adjustAttchmentReference(event) : event);
        }

        byte[] icalFile = export.toByteArray();
        logWarning(export.getWarnings());

        final String type = contentType.toString();
        icalPart.setDataHandler(new DataHandler(new MessageDataSource(icalFile, type)));
        icalPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(type));
        if (checkASCII) {
            icalPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, isAscii(icalFile) ? "7bit" : "quoted-printable");
        }

        return icalPart;
    }

    /**
     * Adjusts (increments) the sequence for the CANCEL method
     *
     * @param event The event to adjust
     * @return The adjusted event
     * @see <a href="https://tools.ietf.org/html/rfc5546#section-2.1.4">RFC 5546</a>
     */
    private Event adjustSequence(Event event) {
        event.setSequence(event.getSequence() + 1);
        return event;
    }

    /**
     * Adjusts the attendee property for a REPLY method
     *
     * @param event The event to adjust
     * @return The adjusted event
     * @see <a href="https://tools.ietf.org/html/rfc5546#section-3.2.3">RFC 5546</a>
     */
    private Event adjustAttendees(Event event) {
        event.setAttendees(Collections.singletonList(CalendarUtils.find(event.getAttendees(), message.getOriginator())));
        return event;
    }

    /**
     * Adjust the URI parameter of attachments. New URI will be the reference
     * to the attachments added to the mail.
     * 
     * @param event The {@link Event} to get the attachments from
     * @return The modified event with adjusted attachments URIs
     */
    private Event adjustAttchmentReference(Event event) {
        if (false == event.containsAttachments() || null == event.getAttachments()) {
            return event;
        }
        List<Attachment> attachments = new LinkedList<Attachment>();
        for (Attachment attachment : event.getAttachments()) {
            if (attachment.getManagedId() > 0) {
                Attachment a = new Attachment();
                a.setCreated(attachment.getCreated());
                a.setFilename(attachment.getFilename());
                a.setFormatType(attachment.getFormatType());
                a.setSize(attachment.getSize());
                a.setUri("cid:" + getAttachmentUri(attachment.getManagedId(), event.getUid()));
                a.setManagedId(0);
                attachments.add(a);
            }
            // Skip files not hosted by us
        }
        event.setAttachments(attachments);
        return event;
    }

    /**
     * Generates the {@link MimeBodyPart} for attachments related to {@link CalendarObjectResource#getEvent()}
     * Implicit assumption is that all attachments set in {@link CalendarObjectResource#getAttachemnts()} exclusively must
     * be added to the mail and are referenced in the event.
     * 
     * @return {@link MimeBodyPart} Containing the attachments
     * @throws OXException In case of error
     */
    private MimeMultipart generateAttachmentPart(MimeMultipart multipart) throws OXException {
        for (Event event : message.getResource().getEvents()) {
            if (false == event.containsAttachments() || event.getAttachments().isEmpty()) {
                continue;
            }
            for (Attachment attachment : event.getAttachments()) {
                if (attachment.getManagedId() > 0) {
                    try {
                        generateAttachmentPart(multipart, attachment, event.getUid());
                    } catch (MessagingException e) {
                        LOGGER.error("Unexpected error while attaching attachments to iMIP mail", e);
                    }
                }
            }
        }
        return multipart;
    }

    /**
     * Creates an {@link MimeBodyPart} with the given attachment and appends it to the mail
     * 
     * @param multipart The mail as {@link MimeMultipart}
     * @param holder The attachment as {@link IFileHolder}
     * @param managedId The identifier of the attachment
     * @param uid The unique identifier of the event the attachment belongs to
     * @throws OXException If setting or getting attachment fails
     * @throws MessagingException If attachment can#t be set
     * @throws IOException If attachments stream can't be read
     */
    private void generateAttachmentPart(MimeMultipart multipart, Attachment attachment, String uid) throws OXException, MessagingException {
        MimeBodyPart bodyPart = new MimeBodyPart();
        ContentType ct;
        String mimeType = attachment.getFormatType();
        if (Strings.isEmpty(mimeType)) {
            mimeType = "application/octet-stream";
        }
        ct = new ContentType(mimeType);

        bodyPart.setDataHandler(new DataHandler(new AttachmentDataSource(message, attachment)));

        final String fileName = attachment.getFilename();
        if (Strings.isNotEmpty(fileName)) {
            ct.setNameParameter(fileName);
            final ContentDisposition cd = new ContentDisposition(Part.ATTACHMENT);
            cd.setFilenameParameter(fileName);
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(cd.toString()));
        }

        bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
        bodyPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");

        bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
        bodyPart.setHeader(MessageHeaders.HDR_CONTENT_ID, "<" + getAttachmentUri(attachment.getManagedId(), uid) + ">");

        multipart.addBodyPart(bodyPart);
    }

    /**
     * Logs all given warnings
     * 
     * @param warnings The warnings to log
     */
    private void logWarning(List<OXException> warnings) {
        if (warnings != null && !warnings.isEmpty()) {
            for (OXException warning : warnings) {
                LOGGER.warn(warning.getMessage(), warning);
            }
        }
    }

    /**
     * Get the file name for the .ics file
     * 
     * @return The file name based on the {@link SchedulingMethod}
     */
    private String determineFileName() {
        switch (message.getMethod()) {
            case REQUEST:
                return INVITE_FILE_NAME;
            case CANCEL:
                return CANCLE_FILE_NAME;
            default:
                return RESPONSE_FILE_NAME;
        }
    }

    private boolean isAscii(final byte[] bytes) {
        boolean isAscci = true;
        for (int i = 0; isAscci && (i < bytes.length); i++) {
            isAscci = (bytes[i] >= 0);
        }
        return isAscci;
    }

    /**
     * Gets a unique identifier for attachments
     * 
     * @param managedId The managed ID of the attachment
     * @param uid The unique ID of the event
     * @return A unique identifier for the attachment
     */
    private String getAttachmentUri(int managedId, String uid) {
        return new StringBuilder().append(managedId).append('@').append(uid).toString();
    }
}
