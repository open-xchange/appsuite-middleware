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

package com.openexchange.ajax.smtptest.actions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.mail.mime.MimeDefaultSession;


/**
 * {@link GetMailsResponse}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class GetMailsResponse extends AbstractAJAXResponse {

    /**
     * Initializes a new {@link GetMailsResponse}.
     * @param response
     */
    protected GetMailsResponse(Response response) {
        super(response);
    }

    public List<Message> getMessages() throws JSONException, MessagingException {
        JSONObject json = ResponseWriter.getJSON(getResponse());
        JSONArray jMessages = json.getJSONArray("data");
        List<Message> messages = new ArrayList<Message>(jMessages.length());
        for (int i = 0; i < jMessages.length(); i++) {
            JSONObject jMessage = jMessages.getJSONObject(i);
            JSONArray jHeaders = jMessage.getJSONArray("headers");
            Message message = new Message(jMessage.getString("raw_message"));
            for (int j = 0; j < jHeaders.length(); j++) {
                JSONObject jHeader = jHeaders.getJSONObject(j);
                message.setHeader(jHeader.getString("name"), jHeader.getString("value"));
            }
            messages.add(message);
        }

        return messages;
    }

    public static final class Message {

        private Document html;

        static {
            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            final Set<String> types = new HashSet<String>(java.util.Arrays.asList(mc.getMimeTypes()));
            if (!types.contains("text/html")) {
                mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            }
            if (!types.contains("text/xml")) {
                mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            }
            if (!types.contains("text/plain")) {
                mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            }
            if (!types.contains("multipart/*")) {
                mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed; x-java-fallback-entry=true");
            }
            if (!types.contains("message/rfc822")) {
                mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
            }
            CommandMap.setDefaultCommandMap(mc);
        }

        private final Map<String, String> headers = new HashMap<String, String>();

        private final String rawMessage;

        private final MimeMessage mimeMessage;

        public Message(String rawMessage) throws MessagingException {
            super();
            this.rawMessage = rawMessage;
            mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), new ByteArrayInputStream(rawMessage.getBytes()));
        }

        public String getRawMessage() {
            return rawMessage;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeader(String name, String value) {
            headers.put(name, value);
        }

        public MimeMessage getMimeMessage() {
            return mimeMessage;
        }

        public String requirePlainText() throws IOException, MessagingException {
            String text = getPlainText();
            assertNotNull("MIME message did not contain a plain text part.", text);
            return text;
        }

        public String getPlainText() throws IOException, MessagingException {
            Object content = mimeMessage.getContent();
            if (content instanceof String) {
                return (String) content;
            } else if (content instanceof InputStream) {
                return readStream((InputStream) content);
            } else if (content instanceof MimeMultipart) {
                MimeMultipart multipart = (MimeMultipart) content;
                BodyPart bodyPart = findPartByContentType(multipart, "text/plain");
                if (bodyPart != null) {
                    return readStream(bodyPart.getInputStream());
                }
            }

            return null;
        }

        public Document requireHtml() throws IOException, MessagingException {
            Document document = getHtml();
            assertNotNull("MIME message did not contain an HTML part.", document);
            return document;
        }

        public Document getHtml() throws IOException, MessagingException {
            if (html == null) {
                Object content = mimeMessage.getContent();
                if (content instanceof MimeMultipart) {
                    MimeMultipart multipart = (MimeMultipart) content;
                    BodyPart bodyPart = findPartByContentType(multipart, "text/html");
                    if (bodyPart != null) {
                        String readStream = readStream(bodyPart.getInputStream());
                        Document document = Jsoup.parse(readStream);
                        html = document;
                    }
                }
            }

            return html;
        }

        public BodyPart getBodyPartByContentType(String contentType) throws MessagingException, IOException {
            Object content = mimeMessage.getContent();
            if (content instanceof MimeMultipart) {
                return findPartByContentType((MimeMultipart) content, contentType);
            }

            return null;
        }

        public BodyPart getBodyPartByContentID(String cid) throws MessagingException, IOException {
            Object content = mimeMessage.getContent();
            if (content instanceof MimeMultipart) {
                return findPartByContentID((MimeMultipart) content, cid);
            }

            return null;
        }

        private BodyPart findPartByContentType(MimeMultipart multipart, String contentType) throws MessagingException, IOException {
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.getContentType().startsWith("multipart/")) {
                    return findPartByContentType((MimeMultipart) bodyPart.getContent(), contentType);
                } else if (bodyPart.getContentType().startsWith(contentType)) {
                    return bodyPart;
                }
            }

            return null;
        }

        private BodyPart findPartByContentID(MimeMultipart multipart, String cid) throws MessagingException, IOException {
            for (int i = 0; i < multipart.getCount(); i++) {
                MimeBodyPart bodyPart = (MimeBodyPart) multipart.getBodyPart(i);
                if (cid.equals(bodyPart.getContentID())) {
                    return bodyPart;
                } else if (bodyPart.getContentType().startsWith("multipart/")) {
                    return findPartByContentID((MimeMultipart) bodyPart.getContent(), cid);
                }
            }

            return null;
        }

        private static String readStream(InputStream is) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line).append(System.getProperty("line.separator"));
                line = br.readLine();
            }
            br.close();
            return sb.toString();
        }

    }

}
