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

package com.openexchange.notification.mail.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.html.HtmlService;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.notification.mail.MailAttachment;
import com.openexchange.notification.mail.MailData;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.notification.service.CommonNotificationVariables;
import com.openexchange.serverconfig.NotificationMailConfig;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;


/**
 * {@link NotificationMailFactoryImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class NotificationMailFactoryImpl implements NotificationMailFactory {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationMailFactoryImpl.class);

    private final ConfigurationService configService;
    private final TemplateService templateService;
    private final HtmlService htmlService;

    /**
     * Initializes a new {@link NotificationMailFactoryImpl}.
     *
     * @param configService The configuration service to use
     * @param templateService The template service to use
     * @param htmlService The HTML service to use
     */
    public NotificationMailFactoryImpl(ConfigurationService configService, TemplateService templateService, HtmlService htmlService) {
        super();
        this.configService = configService;
        this.templateService = templateService;
        this.htmlService = htmlService;
    }

    @Override
    public ComposedMailMessage createMail(MailData data) throws OXException {
        return createMail(data, Collections.<MailAttachment> emptyList());
    }

    @Override
    public ComposedMailMessage createMail(MailData data, Collection<MailAttachment> attachments) throws OXException {
        try {
            NotificationMailConfig mailConfig = data.getMailConfig();
            Map<String, Object> templateVars = getMutableTemplateVars(data);
            applyStyle(mailConfig, templateVars);
            FooterImage footerImage = applyFooter(mailConfig, templateVars);

            String htmlContent = compileTemplate(data.getTemplateName(), templateVars);
            String textContent = data.getTextContent();
            if (textContent == null) {
                textContent = htmlService.html2text(htmlContent, true);
            }

            ContentType textContentType = new ContentType().setPrimaryType("text").setSubType("plain").setCharsetParameter("UTF-8");
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setDataHandler(new DataHandler(new MessageDataSource(textContent, textContentType)));
            textPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, textContentType.toString());

            ContentType htmlContentType = new ContentType().setPrimaryType("text").setSubType("html").setCharsetParameter("UTF-8");
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setDataHandler(new DataHandler(new MessageDataSource(htmlContent, htmlContentType)));
            htmlPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, htmlContentType.toString());

            if (footerImage != null) {
                if (!mailConfig.embedFooterImage()) {
                    MimeBodyPart imagePart = new MimeBodyPart();
                    imagePart.setDisposition("inline; filename=\"" + footerImage.getFileName() + "\"");
                    imagePart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, footerImage.getContentType() + "; name=\"" + footerImage.getFileName() + "\"");
                    imagePart.setContentID("<" + footerImage.getContentId() + ">");
                    imagePart.setHeader("X-Attachment-Id", footerImage.getContentId());
                    imagePart.setDataHandler(new DataHandler(new MessageDataSource(footerImage.getData(), footerImage.getContentType())));

                    MimeMultipart relatedMultipart = new MimeMultipart("related");
                    relatedMultipart.addBodyPart(htmlPart);
                    relatedMultipart.addBodyPart(imagePart);
                    MimeBodyPart tmp = new MimeBodyPart();
                    MessageUtility.setContent(relatedMultipart, tmp);
                    htmlPart = tmp;
                }
            }

            MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());
            mimeMessage.addRecipient(javax.mail.internet.MimeMessage.RecipientType.TO, data.getRecipient());
            if (data.getSender() != null) {
                mimeMessage.addFrom(new Address[] { data.getSender() });
            } else if (data.getSendingUser() != null) {
                mimeMessage.addFrom(getSenderAddress(data.getSendingUser(), data.getContext()));
            }
            mimeMessage.setSubject(data.getSubject(), "UTF-8");
            mimeMessage.setHeader("Auto-Submitted", "auto-generated");
            for (Entry<String, String> header : data.getMailHeaders().entrySet()) {
                mimeMessage.addHeader(header.getKey(), header.getValue());
            }

            MimeMultipart multipart;
            if (null == attachments || attachments.isEmpty()) {
                // No attachment specified
                multipart = new MimeMultipart("alternative");
                multipart.addBodyPart(textPart);
                multipart.addBodyPart(htmlPart);
            } else {
                // Compose multipart for "alternative" content
                MimeMultipart alternativeMultipart = new MimeMultipart("alternative");
                alternativeMultipart.addBodyPart(textPart);
                alternativeMultipart.addBodyPart(htmlPart);
                MimeBodyPart tmp = new MimeBodyPart();
                MessageUtility.setContent(alternativeMultipart, tmp);

                // Create primary multipart and append "alternative" multipart as well as attachments to it
                multipart = new MimeMultipart("mixed");
                multipart.addBodyPart(tmp);

                for (MailAttachment attachment : attachments) {
                    String contentType = attachment.getContentType();
                    MimeBodyPart bodyPart = new MimeBodyPart();

                    // Set MIME part's DataHandler
                    if (attachment instanceof AbstractMailAttachment) {
                        AbstractMailAttachment ama = (AbstractMailAttachment) attachment;
                        bodyPart.setDataHandler(new DataHandler(ama.asDataHandler()));
                    } else {
                        bodyPart.setDataHandler(new DataHandler(new MessageDataSource(attachment.getStream(), new ContentType(contentType).getBaseType())));
                    }

                    // Basic headers
                    bodyPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                    bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(contentType));
                    bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentType(attachment.getDisposition()));

                    // Other headers (if available)
                    Map<String, String> headers = attachment.getHeaders();
                    if (null != headers) {
                        for (Map.Entry<String,String> entry : headers.entrySet()) {
                            bodyPart.setHeader(entry.getKey(), entry.getValue());
                        }
                    }

                    multipart.addBodyPart(bodyPart);
                }
            }

            mimeMessage.setContent(multipart);
            mimeMessage.saveChanges();
            return new ContentAwareComposedMailMessage(mimeMessage, data.getContext().getContextId());
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (IOException e) {
            throw MimeMailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private Map<String, Object> getMutableTemplateVars(MailData data) {
        Map<String, Object> templateVars = data.getTemplateVars();
        try {
            templateVars.put(CommonNotificationVariables.FOOTER_TEXT, CommonNotificationVariables.FOOTER_TEXT);
            templateVars.remove(CommonNotificationVariables.FOOTER_TEXT);
            return templateVars;
        } catch (UnsupportedOperationException e) {
            return new HashMap<>(templateVars);
        }
    }

    /**
     * Helper method to compile a given template file into conform HTML.
     *
     * @param templateFile Name of the template file
     * @param vars The template root object as map, containing all necessary variables
     */
    private String compileTemplate(String templateFile, Map<String, Object> vars) throws OXException {
        OXTemplate template = templateService.loadTemplate(templateFile);
        StringWriter writer = new StringWriter();
        template.process(vars, writer);
        return writer.toString();
    }

    /**
     * Helper method to get the sender address of a user.
     *
     * @param user The user
     * @param context The context
     * @return The address or <code>null</code> if no valid address is configured for that user
     * @throws AddressException
     */
    private InternetAddress[] getSenderAddress(User user, Context context) throws AddressException {
        String fromSource = configService.getProperty("com.openexchange.notification.fromSource", "primaryMail");
        String from = null;
        if ("defaultSenderAddress".equals(fromSource)) {
            UserSettingMail mailSettings = UserSettingMailStorage.getInstance().getUserSettingMail(user.getId(), context);
            if (mailSettings != null) {
                from = mailSettings.getSendAddr();
            }
        }

        if (from == null) {
            from = user.getMail();
        }

        InternetAddress[] parsed = MimeMessageUtility.parseAddressList(from, true, false);
        InternetAddress senderAddress = parsed[0];
        if (senderAddress.getPersonal() == null) {
            try {
                senderAddress.setPersonal(user.getDisplayName());
            } catch (UnsupportedEncodingException e) {
                // try to send nevertheless
            }
        }

        return parsed;
    }

    /**
     * Injects the variable substitutions for button styles and the footer into the passed
     * map.
     *
     * @param vars The map containing the variables for template processing
     */
    private void applyStyle(NotificationMailConfig mailConfig, Map<String, Object> vars) {
        vars.put(CommonNotificationVariables.BUTTON_COLOR, mailConfig.getButtonTextColor());
        vars.put(CommonNotificationVariables.BUTTON_BACKGROUND_COLOR, mailConfig.getButtonBackgroundColor());
        vars.put(CommonNotificationVariables.BUTTON_BORDER_COLOR, mailConfig.getButtonBorderColor());
    }

    /**
     * Applies the footer text and image src substitutions to the passed
     * map.
     *
     * @param vars The map containing the variables for template processing
     */
    private FooterImage applyFooter(NotificationMailConfig mailConfig, Map<String, Object> vars) {
        String footerText = mailConfig.getFooterText();
        FooterImage footerImage = loadFooterImage(mailConfig);
        if (Strings.isNotEmpty(footerText)) {
            vars.put(CommonNotificationVariables.FOOTER_TEXT, footerText);
        }

        if (footerImage != null) {
            vars.put(CommonNotificationVariables.FOOTER_IMAGE_ALT, mailConfig.getFooterImageAltText());
            if (mailConfig.embedFooterImage()) {
                vars.put(CommonNotificationVariables.FOOTER_IMAGE_SRC, "data:" + footerImage.getContentType() + ";base64," + footerImage.getB64Data());
            } else {
                String cid = UUID.randomUUID().toString();
                vars.put(CommonNotificationVariables.FOOTER_IMAGE_SRC, "cid:" + cid);
                footerImage.setContentId(cid);
            }
            return footerImage;
        }

        return null;
    }

    /**
     * Loads the footer image specified in the given mail config and returns it.
     *
     * @param mailConfig The mail notification config
     * @return The footer image or <code>null</code> if none is specified or loading it failed.
     */
    private FooterImage loadFooterImage(NotificationMailConfig mailConfig) {
        String imageName = mailConfig.getFooterImage();
        if (Strings.isEmpty(imageName)) {
            return null;
        }

        String mimeType = MimeType2ExtMap.getContentType(imageName);
        if (mimeType == null || !mimeType.startsWith("image/")) {
            LOG.warn("Footer image {} seems not to be a valid image file. Ensure its file ending matches a common 'image/*' mime type.", imageName);
            return null;
        }

        String templatesPath = getTemplatesPath();
        if (Strings.isEmpty(templatesPath)) {
            return null;
        }

        File image = new File(new File(templatesPath), imageName);
        FileInputStream in = null;
        try {
            in = new FileInputStream(image);
            byte[] imageBytes = ByteStreams.toByteArray(in);
            return new FooterImage(mimeType, "footer_image." + MimeType2ExtMap.getFileExtension(mimeType), imageBytes);
        } catch (IOException e) {
            LOG.warn("Could not load and convert footer image {} from path {}.", imageName, templatesPath, e);
            return null;
        } finally {
            Streams.close(in);
        }
    }

    private String getTemplatesPath() {
        return configService.getProperty("com.openexchange.templating.path");
    }

    private static final class FooterImage {

        private final String contentType;

        private final String fileName;

        private final byte[] data;

        private String cid;


        private FooterImage(String contentType, String fileName, byte[] data) {
            super();
            this.contentType = contentType;
            this.fileName = fileName;
            this.data = data;
        }

        /**
         * Gets the content type, e.g. <code>image/png</code>
         *
         * @return The content type
         */
        public String getContentType() {
            return contentType;
        }

        /**
         * Gets the content id that has been set as the <code>src</code> attributes
         * value.
         *
         * @return The plain content id without any cid-prefix, brackets or applied encoding.
         */
        public String getContentId() {
            return cid;
        }

        /**
         * Gets the image data as byte array
         *
         * @return The data
         */
        public byte[] getData() {
            return data;
        }

        /**
         * Gets the image data as base64 encoded string
         *
         * @return The data
         */
        public String getB64Data() {
            return BaseEncoding.base64().encode(data);
        }

        /**
         * Gets a sane file name including the correct file extension for the image.
         *
         * @return The file name, e.g. <code>footer_logo.png</code>
         */
        public String getFileName() {
            return fileName;
        }

        void setContentId(String cid) {
            this.cid = cid;
        }
    }

}
