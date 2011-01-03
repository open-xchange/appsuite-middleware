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

package com.openexchange.messaging.facebook.osgi;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.google.code.facebookapi.IFacebookRestClient;
import com.google.code.facebookapi.Permission;
import com.google.code.facebookapi.schema.FqlQueryResponse;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.facebook.FacebookMessagingAccountAccess;
import com.openexchange.messaging.facebook.FacebookURLConnectionContent;
import com.openexchange.messaging.facebook.utility.FacebookMessagingUtility;
import com.openexchange.messaging.generic.Utility;
import com.openexchange.messaging.generic.internet.MimeContentDisposition;
import com.openexchange.messaging.generic.internet.MimeContentType;
import com.openexchange.messaging.generic.internet.MimeDateMessagingHeader;
import com.openexchange.messaging.generic.internet.MimeMessagingBodyPart;
import com.openexchange.messaging.generic.internet.MimeMessagingMessage;
import com.openexchange.messaging.generic.internet.MimeMultipartContent;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link StartUpTest}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class StartUpTest {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(StartUpTest.class);

    /**
     * Initializes a new {@link StartUpTest}.
     */
    public StartUpTest() {
        super();
    }

    public void test() {
        final FacebookMessagingAccountAccess fmaa =
            new FacebookMessagingAccountAccess(
                "rodeldodel@wolke7.net",
                "r0deld0del",
                "d36ebc9e274a89e3bd0c239cea4acb48",
                "903e8006dbad9204bb74c26eb3ca2310");
        try {
            fmaa.connect();
            final IFacebookRestClient<Object> client = fmaa.getFacebookRestClient();

            // {
            // final FqlQueryResponse fqr =
            // (FqlQueryResponse) client.fql_query("select first_name, last_name from user where uid = " + client.users_getLoggedInUser());
            // final List<Object> results = fqr.getResults();
            // final Element result0 = (Element) results.get(0);
            // System.out.println("\n\nFQL user query test:");
            // debug(result0);
            // }

            if (client.users_hasAppPermission(Permission.READ_STREAM)) {

                // Retrieve a user's wall posts (stories on their profile).
                final FqlQueryResponse fqr =
                    (FqlQueryResponse) client.fql_query("SELECT post_id, actor_id, message, updated_time, created_time, filter_key, attachment FROM stream WHERE source_id = " + client.users_getLoggedInUser());
                final List<Object> results = fqr.getResults();

                final int size = results.size();
                final Iterator<Object> iterator = results.iterator();
                System.out.println("\n\nRetrieve a user's wall posts (stories on their profile):");
                
                final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(8192);
                
                for (int i = 0; i < size; i++) {
                    final Element element = (Element) iterator.next();
                    /*
                     * TODO: Examine element according to FQL query
                     */
                    
                    out.write("\n\n\n\n".getBytes());
                    parseStreamDOMElement(element).writeTo(out);

                    // debug(element);
                }
                
                final FileOutputStream fos = new FileOutputStream("/home/thorben/Desktop/facebook-output.txt", false);
                fos.write(out.toByteArray());
                fos.flush();
                fos.close();
                
                System.out.println("DONE!");

            }

            // if (client.users_hasAppPermission(Permission.READ_STREAM)) {
            // // Get the visible stream of all of a user's connections (regardless of following status).
            // final FqlQueryResponse fqr =
            // (FqlQueryResponse)
            // client.fql_query("SELECT post_id, actor_id, target_id, message FROM stream WHERE source_id in (SELECT target_id FROM connection WHERE source_id="+client.users_getLoggedInUser()+") AND is_hidden = 0");
            //                
            // final List<Object> results = fqr.getResults();
            //
            // final int size = results.size();
            // final Iterator<Object> iterator = results.iterator();
            // System.out.println("\n\nGet the visible stream of all of a user's connections (regardless of following status):");
            // for (int i = 0; i < size; i++) {
            // final Element element = (Element) iterator.next();
            // /*
            // * TODO: Examine element according to FQL query
            // */
            // debug(element);
            // }
            // }
            //
            // {
            // final FqlQueryResponse fqr =
            // (FqlQueryResponse) client.fql_query("select uid,status_id,message from status where uid = " +
            // client.users_getLoggedInUser());
            // final List<Object> results = fqr.getResults();
            //
            // final int size = results.size();
            // final Iterator<Object> iterator = results.iterator();
            // System.out.println("\n\nThe statuses of logged-in user:");
            // for (int i = 0; i < size; i++) {
            // final Element element = (Element) iterator.next();
            // /*
            // * TODO: Examine element according to FQL query
            // */
            // debug(element);
            // }
            // }
            //
            // {
            // try {
            // final FqlQueryResponse fqr =
            // (FqlQueryResponse)
            // client.fql_query("SELECT message_id, author_id, body, created_time, attachmen, action_linkst FROM message WHERE thread_id = 0 AND viewer_id = "
            // + client.users_getLoggedInUser());
            // final List<Object> results = fqr.getResults();
            //
            // final int size = results.size();
            // final Iterator<Object> iterator = results.iterator();
            // System.out.println("\n\nFQL message query test:");
            // for (int i = 0; i < size; i++) {
            // final Element element = (Element) iterator.next();
            // /*
            // * TODO: Examine element according to FQL query
            // */
            // debug(element);
            // }
            // } catch (final FacebookException e) {
            // System.out.println("Message query failed: " + e.getMessage());
            // }
            // }

        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            fmaa.close();
        }
    }

    private static interface ItemHandler {

        public void handleItem(Node item, MimeMessagingMessage message) throws MessagingException;
    }

    private static ItemHandler IGNORE_ITEM_HANDLER = new ItemHandler() {

        public void handleItem(final Node item, final MimeMessagingMessage message) throws MessagingException {
            // DO nothing
        }
    };

    private static Map<String, ItemHandler> HANDLERS;

    static {
        final Map<String, ItemHandler> m = new HashMap(8);
        m.put("post_id", new ItemHandler() {

            public void handleItem(final Node item, final MimeMessagingMessage message) {
                message.setId(item.getTextContent());
            }
        });
        m.put("actor_id", new ItemHandler() {

            public void handleItem(final Node item, final MimeMessagingMessage message) throws MessagingException {
                message.setHeader(MessagingHeader.KnownHeader.FROM.toString(), item.getTextContent());
            }
        });
        m.put("created_time", new ItemHandler() {

            public void handleItem(final Node item, final MimeMessagingMessage message) throws MessagingException {
                final long time = FacebookMessagingUtility.parseUnsignedLong(item.getTextContent()) * 1000L;
                message.setHeader(new MimeDateMessagingHeader(MessagingHeader.KnownHeader.DATE.toString(), time));
                message.setReceivedDate(time);
            }
        });
        m.put("updated_time", new ItemHandler() {

            public void handleItem(final Node item, final MimeMessagingMessage message) throws MessagingException {
                final long time = FacebookMessagingUtility.parseUnsignedLong(item.getTextContent()) * 1000L;
                message.setHeader(new MimeDateMessagingHeader("X-Facebook-Updated-Time", time));
            }
        });
        m.put("filter_key", new ItemHandler() {

            public void handleItem(final Node item, final MimeMessagingMessage message) throws MessagingException {
                if (item.hasChildNodes()) {
                    message.setHeader("X-Facebook-Filter-Key", item.getTextContent());
                }
            }
        });
        /*
         * Dummy handlers for special items
         */
        // m.put("message", IGNORE_ITEM_HANDLER);
        // m.put("attachment", IGNORE_ITEM_HANDLER);

        HANDLERS = Collections.unmodifiableMap(m);
    }

    public static MimeMessagingMessage parseStreamDOMElement(final Element streamElement) throws MessagingException {
        if (!streamElement.hasChildNodes()) {
            return null;
        }
        final MimeMessagingMessage message = new MimeMessagingMessage();
        /*
         * Iterate child nodes
         */
        Node attachmentNode = null;
        final StringBuilder messageText = new StringBuilder(256);
        {
            final NodeList childNodes = streamElement.getChildNodes();
            final int len = childNodes.getLength();
            for (int i = 0; i < len; i++) {
                final Node item = childNodes.item(i);
                final String localName = item.getLocalName();
                if (null != localName) {
                    final ItemHandler itemHandler = HANDLERS.get(localName);
                    if (null == itemHandler) {
                        if ("message".equals(localName)) {
                            /*-
                             * Text is already HTML-escaped: '<' ==> &lt;
                             */
                            messageText.append(item.getTextContent());
                        } else if ("attachment".equals(localName)) {
                            attachmentNode = item;
                        } else {
                            System.out.println("UN-HANDLED ITEM: " + localName);
                        }
                    } else {
                        itemHandler.handleItem(item, message);
                    }
                }
            }
        }
        /*
         * Check attachment node
         */
        final MultipartProvider multipartProvider = new MultipartProvider();
        if (null != attachmentNode && attachmentNode.hasChildNodes()) {
            final NodeList attachmentChildNodes = attachmentNode.getChildNodes();
            final int len = attachmentChildNodes.getLength();
            for (int i = 0; i < len; i++) {
                final Node item = attachmentChildNodes.item(i);
                if ("fb_object_type".equals(item.getLocalName())) {
                    if (item.hasChildNodes()) {
                        /*
                         * A file attachment is present
                         */
                        final String type = item.getTextContent();
                        final AttachmentHandler attachmentHandler = AH.get(type);
                        if (null == attachmentHandler) {
                            LOG.warn("Unknown attachment type: " + type);
                        } else {
                            attachmentHandler.handleAttachment(attachmentChildNodes, len, messageText, multipartProvider);
                        }
                        break;
                    }
                    /*
                     * A link attachment; append it to message
                     */
                    final AttachmentHandler attachmentHandler = AH.get("link");
                    attachmentHandler.handleAttachment(attachmentChildNodes, len, messageText, multipartProvider);
                    break;
                }
            }
        }

        final MimeMultipartContent multipartContent = multipartProvider.getMultipartContent();
        if (null == multipartContent) {
            message.setContent(createAlternative(messageText.toString()));
        } else {
            final MimeMessagingBodyPart altBodyPart = new MimeMessagingBodyPart();
            altBodyPart.setContent(createAlternative(messageText.toString()));
            multipartContent.addBodyPart(altBodyPart, 0);
            message.setContent(multipartContent);
        }

        return message;
    }

    private static final class MultipartProvider {

        private MimeMultipartContent multipartContent;

        MultipartProvider() {
            super();
        }

        MimeMultipartContent getMultipartContent() {
            return multipartContent;
        }

        void setMultipartContent(final MimeMultipartContent multipartContent) {
            this.multipartContent = multipartContent;
        }

    }

    private static interface AttachmentHandler {

        public void handleAttachment(NodeList attachmentChildNodes, int len, StringBuilder messageText, MultipartProvider multipartProvider) throws MessagingException;
    }

    private static final Map<String, AttachmentHandler> AH;

    static {
        final Map<String, AttachmentHandler> m2 = new HashMap<String, AttachmentHandler>(4);

        m2.put("link", new AttachmentHandler() {

            public void handleAttachment(final NodeList attachmentChildNodes, final int len, final StringBuilder messageText, final MultipartProvider multipartProvider) {
                String name = null;
                String href = null;
                int flag = 0;
                for (int i = 0; flag < 2 && i < len; i++) {
                    final Node item = attachmentChildNodes.item(i);
                    if ("name".equals(item.getLocalName())) {
                        name = item.getTextContent();
                        flag++;
                    } else if ("href".equals(item.getLocalName())) {
                        href = item.getTextContent();
                        flag++;
                    }
                }
                if (null != href && null != name) {
                    messageText.append("<br />");
                    final int pos = href.indexOf('?');
                    if (pos < 0) {
                        messageText.append("<a href='").append(href).append("'>").append(name).append("</a>");
                    } else {
                        final int spos = href.indexOf("u=");
                        final int epos = href.indexOf("&h=", spos + 2);
                        if (epos < 0 || spos < 0) {
                            messageText.append("<a href='").append(href).append("'>").append(name).append("</a>");
                        } else {
                            /*-
                             * Extract real URL from:
                             * http://www.facebook.com/l.php?u=http%253A%252F%252Fwww.open-xchange.com&amp;h=f60781c95f3b41983168bbd0d0f52c95
                             */
                            try {
                                messageText.append("<a href='").append(
                                    URLDecoder.decode(URLDecoder.decode(href.substring(spos + 2, epos), "ISO-8859-1"), "ISO-8859-1")).append(
                                    "'>").append(name).append("</a>");
                            } catch (final UnsupportedEncodingException e) {
                                messageText.append("<a href='").append(href).append("'>").append(name).append("</a>");
                            }
                        }
                    }
                }
            } // End of handleAttachment
        });
        m2.put("video", new AttachmentHandler() {

            public void handleAttachment(final NodeList attachmentChildNodes, final int len, final StringBuilder messageText, final MultipartProvider multipartProvider) throws MessagingException {
                String sourceURL = null;
                String ext = null;
                /*
                 * "media" node
                 */
                final Node media = getNodeByName("media", attachmentChildNodes, len);
                if (null != media) {
                    final Node streamMedia = getNodeByName("stream_media", media);
                    if (null != streamMedia) {
                        /*
                         * "video" node
                         */
                        final Node video = getNodeByName("video", streamMedia);
                        if (null != video) {
                            final Node sourceUrlNode = getNodeByName("source_url", video);
                            if (null != sourceUrlNode) {
                                sourceURL = sourceUrlNode.getTextContent();

                                final int pos = sourceURL.lastIndexOf('.');
                                if (pos >= 0) {
                                    final String extension = sourceURL.substring(pos + 1);
                                    if (!"application/octet-stream".equals(Utility.getContentTypeByExtension(extension))) {
                                        /*
                                         * A known extension
                                         */
                                        ext = extension;
                                    }
                                }
                            }
                        }

                    }
                }
                /*
                 * Source URL present?
                 */
                if (null != sourceURL) {
                    final FacebookURLConnectionContent content = new FacebookURLConnectionContent(sourceURL, true);
                    /*
                     * Create part
                     */
                    final MimeMessagingBodyPart part = new MimeMessagingBodyPart();
                    final String ct;
                    final String filename;
                    {
                        final MimeContentType mct = new MimeContentType(content.getMimeType());
                        if (null == ext) {
                            filename = new StringBuilder("video.").append(Utility.getFileExtensions(mct.getBaseType()).get(0)).toString();
                        } else {
                            filename = new StringBuilder("video.").append(ext).toString();
                        }
                        mct.setNameParameter(filename);
                        ct = mct.toString();
                    }
                    part.setContent(content, ct);
                    part.setHeader("Content-Type", ct);
                    /*
                     * Force base64 encoding to keep data as it is
                     */
                    part.setHeader("Content-Transfer-Encoding", "base64");
                    {
                        final MimeContentDisposition mcd = new MimeContentDisposition();
                        mcd.setDisposition("attachment");
                        mcd.setFilenameParameter(filename);
                        part.setHeader("Content-Disposition", mcd.toString());
                    }
                    /*
                     * Add to multipart
                     */
                    final MimeMultipartContent multipartContent = new MimeMultipartContent();
                    multipartContent.addBodyPart(part);
                    multipartProvider.setMultipartContent(multipartContent);
                }

            } // End of handleAttachment
        });
        m2.put("photo", new AttachmentHandler() {

            public void handleAttachment(final NodeList attachmentChildNodes, final int len, final StringBuilder messageText, final MultipartProvider multipartProvider) throws MessagingException {
                String sourceURL = null;
                String ext = null;
                /*
                 * "media" node
                 */
                final Node media = getNodeByName("media", attachmentChildNodes, len);
                if (null != media) {
                    final Node streamMedia = getNodeByName("stream_media", media);
                    if (null != streamMedia) {
                        /*
                         * "src" node
                         */
                        final Node src = getNodeByName("src", streamMedia);
                        if (null != src) {
                            sourceURL = src.getTextContent();
                            final int pos = sourceURL.lastIndexOf('.');
                            if (pos >= 0) {
                                final String extension = sourceURL.substring(pos + 1);
                                if (!"application/octet-stream".equals(Utility.getContentTypeByExtension(extension))) {
                                    /*
                                     * A known extension
                                     */
                                    ext = extension;
                                }
                            }
                        }
                    }
                }
                /*
                 * Source URL present?
                 */
                if (null != sourceURL) {
                    /*
                     * TODO: Add as multipart/related?!?!
                     */
                    messageText.append("<br />");
                    messageText.append("<img src='").append(sourceURL);
                    if (null != ext) {
                        messageText.append("' alt=\"photo.").append(ext).append("\" />");
                    } else {
                        messageText.append("' />");
                    }
                }
            }
        });
        m2.put("event", new AttachmentHandler() {

            public void handleAttachment(final NodeList attachmentChildNodes, final int len, final StringBuilder messageText, final MultipartProvider multipartProvider) throws MessagingException {

                final Node name = getNodeByName("name", attachmentChildNodes, len);
                if (null != name) {
                    final Node href = getNodeByName("href", attachmentChildNodes, len);
                    if (null != href) {
                        messageText.append("<br /><a href='").append(href.getTextContent()).append("'>").append(name.getTextContent()).append("</a>");
                    } else {
                        messageText.append("<br />").append(name.getTextContent());
                    }

                    final Node properties = getNodeByName("properties", attachmentChildNodes, len);
                    if (null != properties) {
                        final List<Node> streamPropertyNodes = getNodesByName("stream_property", properties);
                        for (final Node streamProperty : streamPropertyNodes) {
                            messageText.append("<br />").append(getNodeByName("name", streamProperty).getTextContent());
                            messageText.append(": ").append(getNodeByName("text", streamProperty).getTextContent());
                        }
                    }
                }
            }
        });

        AH = Collections.unmodifiableMap(m2);
    }

    /**
     * Gets the first node with specified name occurring as child below given node.
     * 
     * @param name The node name to look-up
     * @param node The parent node
     * @return The appropriate node or <code>null</code> if none found
     */
    static Node getNodeByName(final String name, final Node node) {
        if (null == name || null == node || !node.hasChildNodes()) {
            return null;
        }
        final NodeList nodes = node.getChildNodes();
        return getNodeByName(name, nodes, nodes.getLength());
    }

    /**
     * Gets the first node with specified name occurring in given node list.
     * 
     * @param name The node name to look-up
     * @param nodes The node list
     * @param len The length of the node list
     * @return The appropriate node or <code>null</code> if none found
     */
    static Node getNodeByName(final String name, final NodeList nodes, final int len) {
        for (int i = 0; i < len; i++) {
            final Node item = nodes.item(i);
            if (name.equals(item.getLocalName())) {
                return item;
            }
        }
        return null;
    }

    /**
     * Gets all nodes with specified name occurring as child below given node.
     * 
     * @param name The node name to look-up
     * @param node The parent node
     * @return The appropriate nodes
     */
    static List<Node> getNodesByName(final String name, final Node node) {
        if (null == name || null == node || !node.hasChildNodes()) {
            return Collections.emptyList();
        }
        final NodeList nodes = node.getChildNodes();
        final int len = nodes.getLength();
        final List<Node> ret = new ArrayList<Node>(len);
        for (int i = 0; i < len; i++) {
            final Node item = nodes.item(i);
            if (name.equals(item.getLocalName())) {
                ret.add(item);
            }
        }
        return ret;
    }

    private static final String HTML_SPACE = "&#160;";

    private static final String UTF_8 = "UTF-8";

    private static MimeMultipartContent createAlternative(final String messageText) throws MessagingException {
        final MimeMultipartContent alt = new MimeMultipartContent("alternative");
        {
            final MimeMessagingBodyPart text = new MimeMessagingBodyPart();
            text.setText(Utility.textFormat(messageText), UTF_8, "plain");
            text.setHeader("MIME-Version", "1.0");
            text.setHeader("Content-Type", "text/plain; charset=UTF-8");
            alt.addBodyPart(text);
        }
        {
            final MimeMessagingBodyPart html = new MimeMessagingBodyPart();
            final String contentType = "text/html; charset=UTF-8";
            if (messageText == null || messageText.length() == 0) {
                html.setContent(new StringContent(Utility.getConformHTML(HTML_SPACE, UTF_8).replaceFirst(HTML_SPACE, "")), contentType);
            } else {
                html.setContent(new StringContent(Utility.getConformHTML(messageText, UTF_8)), contentType);
            }
            html.setHeader("MIME-Version", "1.0");
            html.setHeader("Content-Type", contentType);
            alt.addBodyPart(html);
        }
        return alt;
    }

    private static void debug(final Node dom) {

        System.out.println(Utility.prettyPrintXML(dom));

    }

}
