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

package com.openexchange.chronos.itip.sender;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ical.CalendarExport;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.generators.NotificationConfiguration;
import com.openexchange.chronos.itip.generators.NotificationMail;
import com.openexchange.chronos.itip.generators.NotificationParticipant;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.itip.sender.datasources.MessageDataSource;
import com.openexchange.chronos.itip.tools.ITipUtils;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.mail.MailObject;
import com.openexchange.groupware.notify.State;
import com.openexchange.html.HtmlService;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.session.Session;

/**
 * {@link DefaultMailSenderService}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 */
public class DefaultMailSenderService implements MailSenderService {

    private static final String COMMENT = "COMMENT";

    private static final String MIXED = "mixed";

    private static final String MULTIPART_MIXED = "multipart/mixed";

    private static final String ALTERNATIVE = "alternative";

    private static final String MULTIPART_ALTERNATIVE = "multipart/alternative";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultMailSenderService.class);

    private final ICalService icalService;

    private final HtmlService htmlService;

    public DefaultMailSenderService() {
        this.icalService = Services.getService(ICalService.class);
        this.htmlService = Services.getService(HtmlService.class);
    }

    @Override
    public void sendMail(NotificationMail mail, Session session, CalendarUser principal, String comment) throws OXException {
        if (!mail.shouldBeSent()) {
            return;
        }

        send(mail, session, mail.getStateType());
    }

    private void send(NotificationMail mail, Session session, State.Type type) {
        Event event = (mail.getOriginal() == null || mail.getEvent().getId() != null) ? mail.getEvent() : mail.getOriginal();
        boolean addAttachments = !mail.getAttachments().isEmpty() && mail.getRecipient().isExternal();
        try {
            prepareAndSend(mail, session, type, event, addAttachments);
        } catch (OXException | MessagingException e) {
            if (addAttachments) {
                LOG.warn("Couldn't send message with attachments. Try to send without attachments", e);
                // Try to resend mail without attachments
                try {
                    prepareAndSend(mail, session, type, event, false);
                    return;
                } catch (MessagingException | OXException e2) {
                    LOG.error("Unable to compose message", e2);
                }
            } else {
                LOG.error("Unable to compose message", e);
            }
        }
    }

    private void prepareAndSend(NotificationMail mail, Session session, State.Type type, Event event, boolean addAttachments) throws MessagingException, OXException {
        MailObject message = new MailObject(session, event.getId(), mail.getRecipient().getFolderId(), Types.APPOINTMENT, type != null ? type.toString() : null);
        message.setAdditionalHeaders(mail.getAdditionalHeaders());
        message.setInternalRecipient(!mail.getRecipient().isExternal() && !mail.getRecipient().isResource());
        message.setFromAddr(getAddress(mail.getSender()));
        message.addToAddr(getAddress(mail.getRecipient()));
        message.setSubject(mail.getSubject());
        message.setUid(event.getUid());
        message.setMessageId(ITipUtils.generateHeaderValue(event.getUid(), true));
        message.setInReplyTo(ITipUtils.generateHeaderValue(event.getUid(), false));
        message.setReference(ITipUtils.generateHeaderValue(event.getUid(), false));
        message.setAutoGenerated(true);
        if (event.containsRecurrenceId()) {
            message.setRecurrenceDatePosition(event.getRecurrenceId().getValue().getTimestamp());
        }
        addBody(mail, message, session, addAttachments);
        message.send();
    }

    private void addBody(NotificationMail mail, MailObject message, Session session, boolean addAttachments) throws MessagingException, OXException {
        NotificationConfiguration recipientConfig = mail.getRecipient().getConfiguration();

        String charset = MailProperties.getInstance().getDefaultMimeCharset();

        if (recipientConfig.sendITIP()) {
            recipientConfig = recipientConfig.clone();
            recipientConfig.setSendITIP(mail.getMessage() != null);
        }

        Object text = null;

        if (!recipientConfig.sendITIP() && !recipientConfig.includeHTML()) { // text only
            if (!addAttachments) {
                message.setContentType("text/plain; charset=" + charset);
                text = mail.getText();
            } else {
                message.setContentType(MULTIPART_MIXED);
                MimeMultipart multipart = new MimeMultipart(MIXED);
                addAttachments(mail, multipart, session);
                multipart.addBodyPart(generateTextPart(mail, charset));
                text = multipart;
            }
        } else if (!recipientConfig.sendITIP()) { // text + html
            if (!addAttachments) {
                message.setContentType(MULTIPART_ALTERNATIVE);
                text = generateTextAndHtmlMultipart(mail, charset);
            } else {
                message.setContentType(MULTIPART_MIXED);
                MimeMultipart multipart = new MimeMultipart(MIXED);
                addAttachments(mail, multipart, session);
                multipart.addBodyPart(toPart(generateTextAndHtmlMultipart(mail, charset)));
                text = multipart;
            }
        } else if (!recipientConfig.includeHTML()) { // text + iCal
            if (!addAttachments) {
                message.setContentType(MULTIPART_ALTERNATIVE);
                text = generateTextAndIcalMultipart(mail, charset, session);
            } else {
                message.setContentType(MULTIPART_MIXED);
                MimeMultipart multipart = new MimeMultipart(MIXED);
                addAttachments(mail, multipart, session);
                multipart.addBodyPart(toPart(generateTextAndIcalMultipart(mail, charset, session)));
                text = multipart;
            }
        } else { // (text + html) + iCal
            message.setContentType(MULTIPART_MIXED);
            Multipart multipart = new MimeMultipart(MIXED);
            if (addAttachments) {
                addAttachments(mail, multipart, session);
            }
            generateTextAndHtmlAndIcalAndIcalAttachment(multipart, mail, charset, session, false);
            text = multipart;
        }

        message.setText(text);
    }

    private BodyPart toPart(Multipart multipart) throws MessagingException {
        MimeBodyPart part = new MimeBodyPart();
        MessageUtility.setContent(multipart, part);
        // part.setContent(multipart);
        return part;
    }

    private void addAttachments(NotificationMail mail, Multipart multipart, Session session) throws OXException {
        CalendarService calendarService = Services.getService(CalendarService.class, true);
        CalendarSession init = calendarService.init(session);
        Attendee actor = mail.getEvent().getAttendees().stream().filter(a -> mail.getActor().getIdentifier() == a.getEntity()).findFirst().orElse(null);
        EventID eventID = new EventID(null == actor ? mail.getActor().getFolderId() : actor.getFolderId(), mail.getEvent().getId());
        try {
            for (Attachment attachment : mail.getAttachments()) {
                // Skip all attachments that are not hosted by us
                if (0 >= attachment.getManagedId()) {
                    continue;
                }
                /*
                 * Create appropriate MIME body part
                 */
                final MimeBodyPart bodyPart = new MimeBodyPart();
                final ContentType ct;
                {
                    String mimeType = attachment.getFormatType();
                    if (Strings.isEmpty(mimeType)) {
                        mimeType = "application/octet-stream";
                    }
                    ct = new ContentType(mimeType);
                }
                /*
                 * Set content through a DataHandler
                 */
                IFileHolder holder = null;
                InputStream attachedFile = null;
                try {
                    holder = calendarService.getAttachment(init, eventID, attachment.getManagedId());
                    attachedFile = holder.getStream();
                    bodyPart.setDataHandler(new DataHandler(new MessageDataSource(attachedFile, ct)));
                    final String fileName = attachment.getFilename();
                    if (fileName != null && !ct.containsNameParameter()) {
                        ct.setNameParameter(fileName);
                    }
                    bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
                    bodyPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                    if (fileName != null) {
                        final ContentDisposition cd = new ContentDisposition(Part.ATTACHMENT);
                        cd.setFilenameParameter(fileName);
                        bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(cd.toString()));
                    }
                    bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
                    String contentId = attachment.getManagedId() + "@" + mail.getEvent().getUid();
                    bodyPart.setHeader(MessageHeaders.HDR_CONTENT_ID, contentId);

                    // Dirty hack... Attachment reference on event is updated implicit 
                    attachment.setManagedId(0);
                    attachment.setUri("CID:" + contentId);
                    /*
                     * Append body part
                     */
                    multipart.addBodyPart(bodyPart);
                } finally {
                    Streams.close(attachedFile);
                    Streams.close(holder);
                }
            }
        } catch (IOException x) {
            LOG.error("", x);
        } catch (MessagingException x) {
            LOG.error("", x);
        }
    }

    private Multipart generateTextAndHtmlAndIcalAndIcalAttachment(Multipart multipart, NotificationMail mail, String charset, Session session, boolean iCalAsAttachment) throws MessagingException, OXException {
        BodyPart textAndHtml = new MimeBodyPart();
        Multipart textAndHtmlAndIcalMultipart = generateTextAndIcalAndHtmlMultipart(mail, charset, session);
        MessageUtility.setContent(textAndHtmlAndIcalMultipart, textAndHtml);
        // textAndHtml.setContent(textAndHtmlAndIcalMultipart);
        BodyPart iCalAttachment = generateIcalAttachmentPart(mail, charset, session);

        multipart.addBodyPart(textAndHtml);
        multipart.addBodyPart(iCalAttachment);
        return multipart;
    }

    private Multipart generateTextAndIcalAndHtmlMultipart(NotificationMail mail, String charset, Session session) throws MessagingException, OXException {
        BodyPart textPart = generateTextPart(mail, charset);
        BodyPart htmlPart = generateHtmlPart(mail, charset);
        BodyPart iCalPart = generateIcalPart(mail, charset, session);

        MimeMultipart multipart = new MimeMultipart(ALTERNATIVE);
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(htmlPart);
        multipart.addBodyPart(iCalPart);
        return multipart;
    }

    private Multipart generateTextAndIcalMultipart(NotificationMail mail, String charset, Session session) throws MessagingException, OXException {
        BodyPart textPart = generateTextPart(mail, charset);
        BodyPart iCalPart = generateIcalPart(mail, charset, session);

        MimeMultipart multipart = new MimeMultipart(ALTERNATIVE);
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(iCalPart);
        return multipart;
    }

    private MimeBodyPart generateIcal(NotificationMail mail, String charset, Session session, boolean checkASCII, Consumer<ContentType> processContent) throws MessagingException, OXException {
        MimeBodyPart icalPart = new MimeBodyPart();
        /*
         * Compose Content-Type
         */
        final ContentType ct = new ContentType();
        processContent.accept(ct);
        String method = null;

        if (mail.getMessage().getMethod() != ITipMethod.NO_METHOD) {
            method = mail.getMessage().getMethod().getKeyword().toUpperCase(Locale.US);
            ct.setParameter("method", method);
        }
        ct.setCharsetParameter(charset);

        /*
         * Generate ICal text
         */
        CalendarExport export = icalService.exportICal(icalService.initParameters());
        if (method != null) {
            export.setMethod(method);
        }
        Event event = mail.getMessage().getEvent();
        if (null != event) {
            ITipMethod iTipMethod = mail.getMessage().getMethod();
            String comment = mail.getMessage().getEvent().getAttendees().get(0).getComment();
            if (null != iTipMethod && ITipMethod.REPLY.equals(iTipMethod) && Strings.isNotEmpty(comment)) {
                // Add attendee comment as event comment
                addComment(event, comment);
            } else if (iTipMethod != null && EnumSet.of(ITipMethod.REQUEST, ITipMethod.CANCEL).contains(iTipMethod) && Strings.isNotEmpty(mail.getMessage().getComment())) {
                addComment(event, mail.getMessage().getComment());
            }
            export.add(event);
        }
        for (Event excpetion : mail.getMessage().exceptions()) {
            export.add(excpetion);
        }

        byte[] icalFile = export.toByteArray();
        List<OXException> warnings = export.getWarnings();
        if (warnings != null && !warnings.isEmpty()) {
            for (OXException warning : warnings) {
                LOG.warn(warning.getMessage(), warning);
            }
        }

        final String contentType = ct.toString();
        icalPart.setDataHandler(new DataHandler(new MessageDataSource(icalFile, contentType)));
        icalPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(contentType));
        if (checkASCII) {
            icalPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, isAscii(icalFile) ? "7bit" : "quoted-printable");
        }
        return icalPart;
    }

    private void addComment(Event event, String comment) {
        ExtendedProperties props = event.getExtendedProperties();
        if (null == props) {
            props = new ExtendedProperties();
        }
        props.add(new ExtendedProperty(COMMENT, comment));
        event.setExtendedProperties(props);
    }

    private BodyPart generateIcalPart(NotificationMail mail, String charset, Session session) throws MessagingException, OXException {
        return generateIcal(mail, charset, session, true, new Consumer<ContentType>() {

            @Override
            public void accept(ContentType ct) {
                ct.setPrimaryType("text");
                ct.setSubType("calendar");
            }
        });
    }

    private BodyPart generateIcalAttachmentPart(NotificationMail mail, String charset, Session session) throws MessagingException, OXException {
        /*
         * Determine file name
         */
        final String fileName;
        switch (mail.getMessage().getMethod()) {
            case REQUEST:
                fileName = "invite.ics";
                break;
            case CANCEL:
                fileName = "cancel.ics";
                break;
            default:
                fileName = "response.ics";
                break;
        }
        MimeBodyPart icalPart = generateIcal(mail, charset, session, false, new Consumer<ContentType>() {

            @Override
            public void accept(ContentType ct) {
                ct.setPrimaryType("application");
                ct.setSubType("ics");
                ct.setNameParameter(fileName);
            }
        });
        /*
         * Content-Disposition & Content-Transfer-Encoding
         */
        ContentDisposition cd = new ContentDisposition();
        cd.setAttachment();
        cd.setFilenameParameter(fileName);
        icalPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, cd.toString());
        icalPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
        return icalPart;

    }

    private Multipart generateTextAndHtmlMultipart(NotificationMail mail, String charset) throws MessagingException, OXException {
        BodyPart textPart = generateTextPart(mail, charset);
        BodyPart htmlPart = generateHtmlPart(mail, charset);

        MimeMultipart multipart = new MimeMultipart(ALTERNATIVE);
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(htmlPart);

        return multipart;
    }

    private BodyPart generateHtmlPart(NotificationMail mail, String charset) throws MessagingException, OXException {
        try {
            MimeBodyPart htmlPart = new MimeBodyPart();
            final ContentType ct = new ContentType();
            ct.setPrimaryType("text");
            ct.setSubType("html");
            ct.setCharsetParameter(charset);
            String contentType = ct.toString();
            final String wellFormedHTMLContent = htmlService.getConformHTML(mail.getHtml(), charset);
            htmlPart.setDataHandler(new DataHandler(new MessageDataSource(wellFormedHTMLContent, ct)));
            // htmlPart.setContent(wellFormedHTMLContent, contentType);
            htmlPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
            htmlPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType);
            return htmlPart;
        } catch (final UnsupportedEncodingException e) {
            throw new MessagingException("Unsupported encoding", e);
        }
    }

    private BodyPart generateTextPart(NotificationMail mail, String charset) throws MessagingException {
        final MimeBodyPart textPart = new MimeBodyPart();
        MessageUtility.setText(mail.getText(), charset, textPart);
        // textPart.setText(mail.getText(), charset);
        textPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        final ContentType ct = new ContentType();
        ct.setPrimaryType("text");
        ct.setSubType("plain");
        ct.setCharsetParameter(charset);
        textPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, ct.toString());
        return textPart;
    }

    private String getAddress(NotificationParticipant participant) {
        final String displayName = participant.getDisplayName();
        if (displayName != null) {
            try {
                return new QuotedInternetAddress(participant.getEmail(), displayName, "UTF-8").toUnicodeString();
            } catch (AddressException | UnsupportedEncodingException e) {
                LOG.warn("Interned address could not be generated. Returning fall-back instead.", e);
                return "\"" + displayName + "\"" + " <" + participant.getEmail() + ">";
            }
        }
        // Without personal part
        return participant.getEmail();
    }

    private static boolean isAscii(final byte[] bytes) {
        boolean isAscci = true;
        for (int i = 0; isAscci && (i < bytes.length); i++) {
            isAscci = (bytes[i] >= 0);
        }
        return isAscci;
    }
}
