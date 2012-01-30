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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.calendar.itip.sender;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.calendar.itip.generators.NotificationConfiguration;
import com.openexchange.calendar.itip.generators.NotificationMail;
import com.openexchange.calendar.itip.generators.NotificationParticipant;
import com.openexchange.calendar.itip.sender.datasources.MessageDataSource;
import com.openexchange.data.conversion.ical.itip.ITipEmitter;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.mail.MailObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.notify.State;
import com.openexchange.html.HTMLService;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.utils.MIMEMessageUtility;
import com.openexchange.session.Session;

/**
 * {@link DefaultMailSenderService}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class DefaultMailSenderService implements MailSenderService {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DefaultMailSenderService.class));

    private final ITipEmitter iTipEmitter;

    private final HTMLService htmlService;

    public DefaultMailSenderService(final ITipEmitter iTipEmitter, final HTMLService htmlService) {
        this.iTipEmitter = iTipEmitter;
        this.htmlService = htmlService;
    }

    public void sendMail(NotificationMail mail, Session session) {
        if (!mail.shouldBeSent()) {
            return;
        }
        
        send(mail, session, mail.getStateType());
    }

    private void send(NotificationMail mail, Session session, State.Type type) {
        Appointment app = (mail.getOriginal() == null || mail.getAppointment().getObjectID() > 0) ? mail.getAppointment() : mail.getOriginal();
        MailObject message = new MailObject(session, app.getObjectID(), mail.getRecipient().getFolderId(), Types.APPOINTMENT, type.toString());
        message.setInternalRecipient(!mail.getRecipient().isExternal() && !mail.getRecipient().isResource());

        message.setFromAddr(getAddress(mail.getSender()));
        message.addToAddr(getAddress(mail.getRecipient()));
        message.setSubject(mail.getSubject());

        try {
            addBody(mail, message, session);
            message.send();
        } catch (OXException e) {
            LOG.error("Unable to compose message", e);
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unable to compose message", e);
        } catch (MessagingException e) {
            LOG.error("Unable to compose message", e);
        }

    }

    private void addBody(NotificationMail mail, MailObject message, Session session) throws MessagingException, OXException, UnsupportedEncodingException {
        NotificationConfiguration recipientConfig = mail.getRecipient().getConfiguration();

        String charset = MailProperties.getInstance().getDefaultMimeCharset();
        
        if (recipientConfig.sendITIP()) {
        	recipientConfig = recipientConfig.clone();
            recipientConfig.setSendITIP(mail.getMessage() != null);
        }
        
        if (!recipientConfig.sendITIP() && !recipientConfig.includeHTML()) { // text only
            message.setContentType("text/plain; charset=" + charset);
            addTextBody(mail, message);
        } else if (!recipientConfig.sendITIP()) { // text + html
            message.setContentType("multipart/alternative");
            Multipart textAndHtml = generateTextAndHtmlMultipart(mail, charset);
            message.setText(textAndHtml);
        } else if (!recipientConfig.includeHTML()) { // text + iCal
            message.setContentType("multipart/alternative");
            Multipart textAndIcal = generateTextAndIcalMultipart(mail, charset, session);
            message.setText(textAndIcal);
        } else { // (text + html) + iCal
            message.setContentType("multipart/mixed");
            Multipart textAndIcalAndHtml = generateTextAndHtmlAndIcalAndIcalAttachment(mail, charset, session, false);
            message.setText(textAndIcalAndHtml);
        }
    }

    private Multipart generateTextAndHtmlAndIcalAndIcalAttachment(NotificationMail mail, String charset, Session session, boolean iCalAsAttachment) throws MessagingException, OXException, UnsupportedEncodingException {
        BodyPart textAndHtml = new MimeBodyPart();
        Multipart textAndHtmlAndIcalMultipart = generateTextAndIcalAndHtmlMultipart(mail, charset, session);
        textAndHtml.setContent(textAndHtmlAndIcalMultipart);
        BodyPart iCalAttachment = generateIcalAttachmentPart(mail, charset, session);
        
        MimeMultipart multipart = new MimeMultipart("mixed");
        multipart.addBodyPart(textAndHtml);
        multipart.addBodyPart(iCalAttachment);
        return multipart;
    }
    
    private Multipart generateTextAndIcalAndHtmlMultipart(NotificationMail mail, String charset, Session session) throws MessagingException, OXException, UnsupportedEncodingException {
        BodyPart textPart = generateTextPart(mail, charset);
        BodyPart htmlPart = generateHtmlPart(mail, charset);
        BodyPart iCalPart = generateIcalPart(mail, charset, session);

        MimeMultipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(htmlPart);
        multipart.addBodyPart(iCalPart);
        return multipart;
    }

    private Multipart generateTextAndIcalMultipart(NotificationMail mail, String charset, Session session) throws MessagingException, OXException, UnsupportedEncodingException {
        BodyPart textPart = generateTextPart(mail, charset);
        BodyPart iCalPart = generateIcalPart(mail, charset, session);

        MimeMultipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(iCalPart);
        return multipart;
    }

    private BodyPart generateIcalPart(NotificationMail mail, String charset, Session session) throws MessagingException, OXException, UnsupportedEncodingException {
    	MimeBodyPart icalPart = new MimeBodyPart();
        Context ctx = ContextStorage.getStorageContext(session.getContextId());
		final ContentType ct = new ContentType();
		ct.setPrimaryType("text");
		ct.setSubType("calendar");
		if (mail.getMessage().getMethod() != ITipMethod.NO_METHOD) {
			ct.setParameter("method", mail.getMessage().getMethod()
					.getKeyword().toUpperCase(Locale.US));
		}
		ct.setCharsetParameter(charset);
		/*
		 * Generate ICal text
		 */
		final byte[] icalFile;
		final boolean isAscii;
		{
			String message = iTipEmitter.writeMessage(mail.getMessage(), ctx,
					null, null);
			message = trimICal(message);
			icalFile = message.getBytes(charset);
			isAscii = isAscii(icalFile);
		}
		final String contentType = ct.toString();
		icalPart.setDataHandler(new DataHandler(new MessageDataSource(icalFile,
				contentType)));
		icalPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE,
				MIMEMessageUtility.foldContentType(contentType));
		icalPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC,
				isAscii ? "7bit" : "quoted-printable");
		return icalPart;
    }

    private BodyPart generateIcalAttachmentPart(NotificationMail mail, String charset, Session session) throws MessagingException, OXException, UnsupportedEncodingException {
		MimeBodyPart icalPart = new MimeBodyPart();
		Context ctx = ContextStorage.getStorageContext(session.getContextId());
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
		/*
		 * Compose Content-Type
		 */
		final ContentType ct = new ContentType();
		ct.setPrimaryType("application");
		ct.setSubType("ics");
		ct.setNameParameter(fileName);
		/*
		 * Generate ICal text
		 */
		final byte[] icalFile;
		{
			String message = iTipEmitter.writeMessage(mail.getMessage(), ctx,
					null, null);
			message = trimICal(message);
			icalFile = message.getBytes(charset);
		}
		final String contentType = ct.toString();
		icalPart.setDataHandler(new DataHandler(new MessageDataSource(icalFile,
				contentType)));
		icalPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE,
				MIMEMessageUtility.foldContentType(contentType));
		/*
		 * Content-Disposition & Content-Transfer-Encoding
		 */
		ContentDisposition cd = new ContentDisposition();
		cd.setAttachment();
		cd.setFilenameParameter(fileName);
		icalPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION,
				cd.toString());
		icalPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
		return icalPart;

    }

    private Multipart generateTextAndHtmlMultipart(NotificationMail mail, String charset) throws MessagingException {
        BodyPart textPart = generateTextPart(mail, charset);
        BodyPart htmlPart = generateHtmlPart(mail, charset);

        MimeMultipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(htmlPart);

        return multipart;
    }

    private BodyPart generateHtmlPart(NotificationMail mail, String charset) throws MessagingException {
        MimeBodyPart htmlPart = new MimeBodyPart();
        final ContentType ct = new ContentType();
        ct.setPrimaryType("text");
        ct.setSubType("html");
        ct.setCharsetParameter(charset);
        String contentType = ct.toString();
        final String wellFormedHTMLContent = htmlService.getConformHTML(mail.getHtml(), charset);
        htmlPart.setContent(wellFormedHTMLContent, contentType);
        htmlPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        htmlPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType);
        return htmlPart;
    }

    private BodyPart generateTextPart(NotificationMail mail, String charset) throws MessagingException {
        final MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(mail.getText(), charset);
        textPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        final ContentType ct = new ContentType();
        ct.setPrimaryType("text");
        ct.setSubType("plain");
        ct.setCharsetParameter(charset);
        textPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, ct.toString());
        return textPart;
    }

    private void addTextBody(NotificationMail mail, MailObject message) {
        message.setText(mail.getText());
    }

    private String getAddress(NotificationParticipant participant) {
        final String displayName = participant.getDisplayName();
        if (displayName != null) {
            try {
                return new QuotedInternetAddress(participant.getEmail(), displayName, "UTF-8").toUnicodeString();
            } catch (AddressException e) {
                LOG.warn("Interned address could not be generated. Returning fall-back instead.", e);
                return "\"" + displayName + "\"" + " <" + participant.getEmail() + ">";
            } catch (UnsupportedEncodingException e) {
                // Cannot occur
                LOG.warn("Interned address could not be generated. Returning fall-back instead.", e);
                return "\"" + displayName + "\"" + " <" + participant.getEmail() + ">";
            }
        }
        // Without personal part
        return participant.getEmail();
    }

    private static final Pattern P_TRIM = Pattern.compile("[a-zA-Z-_]+:\r?\n");

    private static String trimICal(final String ical) {
        return P_TRIM.matcher(ical).replaceAll("");
    }

    private static boolean isAscii(final byte[] bytes) {
        boolean isAscci = true;
        for (int i = 0; isAscci && (i < bytes.length); i++) {
            final byte b = bytes[i];
            isAscci = (b < 128 && b >= 0);
        }
        return isAscci;
    }

}
