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

package com.openexchange.calendar.itip.sender;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import com.openexchange.calendar.itip.generators.AttachmentMemory;
import com.openexchange.calendar.itip.generators.NotificationConfiguration;
import com.openexchange.calendar.itip.generators.NotificationMail;
import com.openexchange.calendar.itip.generators.NotificationParticipant;
import com.openexchange.calendar.itip.sender.datasources.MessageDataSource;
import com.openexchange.context.ContextService;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.itip.ITipEmitter;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.mail.MailObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.notify.NotificationConfig;
import com.openexchange.groupware.notify.NotificationConfig.NotificationProperty;
import com.openexchange.groupware.notify.State;
import com.openexchange.groupware.userconfiguration.CapabilityUserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.html.HtmlService;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;

/**
 * {@link DefaultMailSenderService}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class DefaultMailSenderService implements MailSenderService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultMailSenderService.class);

    private final ITipEmitter iTipEmitter;

    private final HtmlService htmlService;

    private final AttachmentBase attachments;

    private final ContextService contexts;

    private final UserService users;

    private final UserConfigurationStorage userConfigurations;

    public DefaultMailSenderService(final ITipEmitter iTipEmitter, final HtmlService htmlService, AttachmentBase attachments, ContextService contexts, UserService users, UserConfigurationStorage userConfigs, AttachmentMemory attachmentMemory) {
        this.iTipEmitter = iTipEmitter;
        this.htmlService = htmlService;
        this.attachments = attachments;
        this.contexts = contexts;
        this.users = users;
        this.userConfigurations = userConfigs;
    }

    @Override
    public void sendMail(NotificationMail mail, Session session) {
        if (!mail.shouldBeSent()) {
            return;
        }

        send(mail, session, mail.getStateType());
    }

    private void send(NotificationMail mail, Session session, State.Type type) {
        Appointment app = (mail.getOriginal() == null || mail.getAppointment().getObjectID() > 0) ? mail.getAppointment() : mail.getOriginal();
        MailObject message;
        {
            final int appointmentId = app.getObjectID();
            final int folderId = mail.getRecipient().getFolderId();
            final String sType = type != null ? type.toString() : null;
            message = new MailObject(session, appointmentId, folderId, Types.APPOINTMENT, sType);
        }
        try {
            message.setInternalRecipient(!mail.getRecipient().isExternal() && !mail.getRecipient().isResource());
            message.setFromAddr(getSenderAddress(mail.getSender(), session));
            message.addToAddr(getAddress(mail.getRecipient()));
            message.setSubject(mail.getSubject());
            message.setUid(app.getUid());
            message.setAutoGenerated(true);
            if (app.containsRecurrenceDatePosition()) {
                message.setRecurrenceDatePosition(app.getRecurrenceDatePosition().getTime());
            }
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

    /**
     * @param message
     * @param sender
     * @throws OXException
     */
    private String getSenderAddress(NotificationParticipant sender, Session session) throws OXException {
        if (sender.getUser() == null || sender.getUser().getId() != session.getUserId()) {
            return getAddress(sender);
        }

        ServerSession serverSession = null;
        try {
            serverSession = ServerSessionAdapter.valueOf(session);
        } catch (OXException e) {
            LOG.error("Unable to retrieve ServerSession for UserSettings", e);
            return getAddress(sender);
        }

        String fromAddr;
        final String senderSource = NotificationConfig.getProperty(NotificationProperty.FROM_SOURCE, "primaryMail");
        if (senderSource.equals("defaultSenderAddress")) {
            try {
                fromAddr = getUserSettingMail(session.getUserId(), serverSession.getContext()).getSendAddr();
            } catch (final OXException e) {
                LOG.error("", e);
                fromAddr = UserStorage.getInstance().getUser(session.getUserId(), serverSession.getContext()).getMail();
            }
        } else {
            fromAddr = UserStorage.getInstance().getUser(session.getUserId(), serverSession.getContext()).getMail();
        }

        sender.setEmail(fromAddr);

        return getAddress(sender);
    }

    private UserConfiguration getUserConfiguration(final int id, final int[] groups, final Context context) throws SQLException, OXException {
        return CapabilityUserConfigurationStorage.loadUserConfiguration(id, groups, context);
    }

    private UserSettingMail getUserSettingMail(final int id, final Context context) throws OXException {
        return UserSettingMailStorage.getInstance().loadUserSettingMail(id, context);
    }


    private void addBody(NotificationMail mail, MailObject message, Session session) throws MessagingException, OXException, UnsupportedEncodingException {
        NotificationConfiguration recipientConfig = mail.getRecipient().getConfiguration();

        String charset = MailProperties.getInstance().getDefaultMimeCharset();

        if (recipientConfig.sendITIP()) {
        	recipientConfig = recipientConfig.clone();
            recipientConfig.setSendITIP(mail.getMessage() != null);
        }

        boolean addAttachments = !mail.getAttachments().isEmpty() && mail.getRecipient().isExternal();

        if (!recipientConfig.sendITIP() && !recipientConfig.includeHTML()) { // text only
        	if (!addAttachments) {
            	message.setContentType("text/plain; charset=" + charset);
                addTextBody(mail, message);
        	} else {
        		message.setContentType("multipart/mixed");
        		MimeMultipart multipart = new MimeMultipart("mixed");
        		multipart.addBodyPart(generateTextPart(mail, charset));
        		addAttachments(mail, multipart, session);
        		message.setText(multipart);
        	}
        } else if (!recipientConfig.sendITIP()) { // text + html
            if (!addAttachments) {
            	message.setContentType("multipart/alternative");
                Multipart textAndHtml = generateTextAndHtmlMultipart(mail, charset);
                message.setText(textAndHtml);
            } else {
        		message.setContentType("multipart/mixed");
        		MimeMultipart multipart = new MimeMultipart("mixed");
        		multipart.addBodyPart(toPart(generateTextAndHtmlMultipart(mail, charset)));
        		addAttachments(mail, multipart, session);
        		message.setText(multipart);
            }
        } else if (!recipientConfig.includeHTML()) { // text + iCal
            if (!addAttachments) {
            	message.setContentType("multipart/alternative");
                Multipart textAndIcal = generateTextAndIcalMultipart(mail, charset, session);
                message.setText(textAndIcal);
            } else {
        		message.setContentType("multipart/mixed");
        		MimeMultipart multipart = new MimeMultipart("mixed");
        		multipart.addBodyPart(toPart(generateTextAndIcalMultipart(mail, charset, session)));
        		addAttachments(mail, multipart, session);
        		message.setText(multipart);
            }
        } else { // (text + html) + iCal
        	message.setContentType("multipart/mixed");
            Multipart textAndIcalAndHtml = generateTextAndHtmlAndIcalAndIcalAttachment(mail, charset, session, false);
            addAttachments(mail, textAndIcalAndHtml, session);
            message.setText(textAndIcalAndHtml);
        }
    }

    private BodyPart toPart(Multipart multipart) throws MessagingException {
    	MimeBodyPart part = new MimeBodyPart();
    	MessageUtility.setContent(multipart, part);
    	// part.setContent(multipart);
    	return part;
	}

	private void addAttachments(NotificationMail mail,
			Multipart multipart, Session session)  throws OXException {
		try {
			Context context = contexts.getContext(session.getContextId());
			User user = users.getUser(session.getUserId(), context);
			UserConfiguration config = userConfigurations.getUserConfiguration(session.getUserId(), context);

			Appointment effective = (mail.getAppointment() != null) ? mail.getAppointment() : mail.getOriginal();
			int folderId = effective.getParentFolderID();
			int attachedId = effective.getObjectID();

			for (AttachmentMetadata metadata : mail.getAttachments()) {
				/*
				 * Create appropriate MIME body part
				 */
				final MimeBodyPart bodyPart = new MimeBodyPart();
				final ContentType ct;
				{
					String mimeType = metadata.getFileMIMEType();
					if (null == mimeType) {
						mimeType = "application/octet-stream";
					}
					ct = new ContentType(mimeType);
				}
				/*
				 * Set content through a DataHandler
				 */
				bodyPart.setDataHandler(new DataHandler(new MessageDataSource(
						attachments.getAttachedFile(session, folderId, attachedId,
								Types.APPOINTMENT, metadata.getId(), context, user,
								config), ct)));
				final String fileName = metadata.getFilename();
				if (fileName != null && !ct.containsNameParameter()) {
					ct.setNameParameter(fileName);
				}
				bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE,
						MimeMessageUtility.foldContentType(ct.toString()));
				bodyPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
				if (fileName != null) {
					final ContentDisposition cd = new ContentDisposition(
							Part.ATTACHMENT);
					cd.setFilenameParameter(fileName);
					bodyPart.setHeader(
							MessageHeaders.HDR_CONTENT_DISPOSITION,
							MimeMessageUtility.foldContentDisposition(cd.toString()));
				}
				bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC,
						"base64");
				/*
				 * Append body part
				 */
				multipart.addBodyPart(bodyPart);
			}
		} catch (IOException x) {
			LOG.error("", x);
		} catch (MessagingException x) {
			LOG.error("", x);
		}
	}

    private Multipart generateTextAndHtmlAndIcalAndIcalAttachment(NotificationMail mail, String charset, Session session, boolean iCalAsAttachment) throws MessagingException, OXException, UnsupportedEncodingException {
        BodyPart textAndHtml = new MimeBodyPart();
        Multipart textAndHtmlAndIcalMultipart = generateTextAndIcalAndHtmlMultipart(mail, charset, session);
        MessageUtility.setContent(textAndHtmlAndIcalMultipart, textAndHtml);
        // textAndHtml.setContent(textAndHtmlAndIcalMultipart);
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
            List<ConversionWarning> warnings = new ArrayList<>();
            List<ConversionError> errors = new ArrayList<>();

			String message = iTipEmitter.writeMessage(mail.getMessage(), ctx,
                errors, warnings);

            if (!warnings.isEmpty()) {
                for (ConversionWarning warning : warnings) {
                    LOG.warn(warning.getMessage(), warning);
                }
            }
            if (!errors.isEmpty()) {
                for (ConversionWarning error : errors) {
                    LOG.error(error.getMessage(), error);
                }
            }

			message = trimICal(message);
			icalFile = message.getBytes(charset);
			isAscii = isAscii(icalFile);
		}
		final String contentType = ct.toString();
		icalPart.setDataHandler(new DataHandler(new MessageDataSource(icalFile,
				contentType)));
		icalPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE,
				MimeMessageUtility.foldContentType(contentType));
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
				MimeMessageUtility.foldContentType(contentType));
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
            isAscci = (bytes[i] >= 0);
        }
        return isAscci;
    }

}
