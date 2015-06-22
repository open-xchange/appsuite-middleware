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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.share.notification.mail.impl;

import static com.openexchange.osgi.Tools.requireService;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.html.HtmlService;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.notification.BasicNotificationTemplate.FooterImage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.notification.impl.ShareNotifyExceptionCodes;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;


/**
 * {@link NotificationMail}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
abstract class NotificationMail {

    static class MailData {
        InternetAddress sender;
        InternetAddress recipient;
        String subject;
        String htmlContent;
        FooterImage footerImage;
        Context context;
        TransportProvider transportProvider;
        Map<String, String> mailHeaders;
    }


    private final MailData data;

    public NotificationMail(MailData data) {
        super();
        this.data = data;
    }

    public ComposedMailMessage compose() throws OXException {
        ComposedMailMessage mail;
        if (data.footerImage == null) {
            // no image, no multipart
            mail = data.transportProvider.getNewComposedMailMessage(null, data.context);
            mail.addRecipient(data.recipient);
            mail.addTo(data.recipient);
            mail.addFrom(data.sender);
            mail.setHeader("Auto-Submitted", "auto-generated");
            if (data.mailHeaders != null) {
                for (Entry<String, String> header : data.mailHeaders.entrySet()) {
                    mail.addHeader(header.getKey(), header.getValue());
                }
            }
            mail.setSubject(data.subject);
            mail.setBodyPart(data.transportProvider.getNewTextBodyPart(data.htmlContent));
        } else {
            try {
                MimeBodyPart htmlPart = new MimeBodyPart();
                ContentType ct = new ContentType();
                ct.setPrimaryType("text");
                ct.setSubType("html");
                ct.setCharsetParameter("UTF-8");
                String contentType = ct.toString();
                htmlPart.setDataHandler(new DataHandler(new MessageDataSource(data.htmlContent, ct)));
                htmlPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType);

                MimeBodyPart imagePart = new MimeBodyPart();
                imagePart.setDisposition("inline; filename=\"" + data.footerImage.getFileName() + "\"");
                imagePart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, data.footerImage.getContentType() + "; name=\"" + data.footerImage.getFileName() + "\"");
                imagePart.setContentID("<" + data.footerImage.getContentId() + ">");
                imagePart.setHeader("X-Attachment-Id", data.footerImage.getContentId());
                imagePart.setDataHandler(new DataHandler(new MessageDataSource(data.footerImage.getData(), data.footerImage.getContentType())));

                MimeMultipart multipart = new MimeMultipart("related");
                multipart.addBodyPart(htmlPart);
                multipart.addBodyPart(imagePart);

                MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());
                mimeMessage.addRecipient(javax.mail.internet.MimeMessage.RecipientType.TO, data.recipient);
                if (data.sender != null) {
                    mimeMessage.addFrom(new Address[] { data.sender });
                }
                mimeMessage.setSubject(data.subject, "UTF-8");
                mimeMessage.setHeader("Auto-Submitted", "auto-generated");
                if (data.mailHeaders != null) {
                    for (Entry<String, String> header : data.mailHeaders.entrySet()) {
                        mimeMessage.addHeader(header.getKey(), header.getValue());
                    }
                }
                mimeMessage.setContent(multipart);
                mimeMessage.saveChanges();
                mail = new ContentAwareComposedMailMessage(mimeMessage, data.context.getContextId());
            } catch (MessagingException e) {
                throw ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage(), data.recipient.getAddress());
            }
        }

        return mail;
    }

    protected static String compileTemplate(String templateFile, Map<String, Object> vars, ServiceLookup services) throws OXException {
        TemplateService templateService = requireService(TemplateService.class, services);
        OXTemplate template = templateService.loadTemplate(templateFile);
        StringWriter writer = new StringWriter();
        template.process(vars, writer);
        return requireService(HtmlService.class, services).getConformHTML(writer.toString(), "UTF-8");
    }

}
