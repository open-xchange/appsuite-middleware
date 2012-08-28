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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.snippet.mime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.activation.DataHandler;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.session.Session;
import com.openexchange.snippet.Attachment;
import com.openexchange.snippet.DefaultAttachment;
import com.openexchange.snippet.DefaultAttachment.InputStreamProvider;
import com.openexchange.snippet.DefaultSnippet;
import com.openexchange.snippet.Property;
import com.openexchange.snippet.Snippet;
import com.openexchange.snippet.SnippetExceptionCodes;
import com.openexchange.snippet.SnippetManagement;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link MimeSnippetManagement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeSnippetManagement implements SnippetManagement {

    private static final class InputStreamProviderImpl implements InputStreamProvider {

        private final MimePart part;

        protected InputStreamProviderImpl(final MimePart part) {
            this.part = part;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            try {
                return part.getInputStream();
            } catch (final MessagingException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
    }

    private static Context getContext(final Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getContext();
        }
        return Services.getService(ContextService.class).getContext(session.getContextId());
    }

    private static final ConcurrentMap<Integer, QuotaFileStorage> FILE_STORE_CACHE = new ConcurrentHashMap<Integer, QuotaFileStorage>();

    private static QuotaFileStorage getFileStorage(final Context ctx) throws OXException {
        final Integer key = Integer.valueOf(ctx.getContextId());
        QuotaFileStorage qfs = FILE_STORE_CACHE.get(key);
        if (null == qfs) {
            final QuotaFileStorage quotaFileStorage = QuotaFileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx);
            qfs = FILE_STORE_CACHE.putIfAbsent(key, quotaFileStorage);
            if (null == qfs) {
                qfs = quotaFileStorage;
            }
        }
        return qfs;
    }

    private final Session session;

    /**
     * Initializes a new {@link MimeSnippetManagement}.
     */
    public MimeSnippetManagement(final Session session) {
        super();
        this.session = session;
    }

    @Override
    public Snippet getSnippet(final String id) throws OXException {
        try {
            final QuotaFileStorage fileStorage = getFileStorage(getContext(session));
            final MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), fileStorage.getFile(id));
            final DefaultSnippet snippet = new DefaultSnippet();
            final String lcct;
            {
                final String tmp = mimeMessage.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null);
                if (!isEmpty(tmp)) {
                    lcct = tmp.trim().toLowerCase(Locale.US);
                } else {
                    lcct = "text/plain; charset=us-ascii";
                }
            }
            if (lcct.startsWith("multipart/", 0)) {
                final Multipart multipart = (Multipart) mimeMessage.getContent();
                parseSnippet(mimeMessage, (MimePart) multipart.getBodyPart(0), snippet);
                final int count = multipart.getCount();
                if (count > 1) {
                    for (int i = 1; i < count; i++) {
                        parsePart((MimePart) multipart.getBodyPart(i), snippet);
                    }
                }
            } else {
                parseSnippet(mimeMessage, mimeMessage, snippet);
            }
            return snippet;
        } catch (final MessagingException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SnippetExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static void parsePart(final MimePart part, final DefaultSnippet snippet) throws OXException, MessagingException {
        String header = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null);
        final ContentType contentType = isEmpty(header) ? ContentType.DEFAULT_CONTENT_TYPE : new ContentType(header);
        if (contentType.startsWith("text/javascript")) {
            snippet.setMisc(MessageUtility.readMimePart(part, contentType));
        } else {
            header = part.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, null);
            final ContentDisposition contentDisposition = isEmpty(header) ? null : new ContentDisposition(header);
            final DefaultAttachment attachment = new DefaultAttachment();
            attachment.setContentDisposition(contentDisposition == null ? null : contentDisposition.toString());
            attachment.setContentType(contentType.toString());
            attachment.setSize(part.getSize());
            attachment.setStreamProvider(new InputStreamProviderImpl(part));
        }
    }

    private static void parseSnippet(final MimeMessage mimeMessage, final MimePart part, final DefaultSnippet snippet) throws OXException, MessagingException {
        // Read content from part
        final String header = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null);
        final ContentType contentType = isEmpty(header) ? ContentType.DEFAULT_CONTENT_TYPE : new ContentType(header);
        snippet.setContent(MessageUtility.readMimePart(part, contentType));
        // Read message's headers
        @SuppressWarnings("unchecked")
        final Enumeration<Header> others = mimeMessage.getAllHeaders();
        while (others.hasMoreElements()) {
            final Header hdr = others.nextElement();
            snippet.put(hdr.getName(), hdr.getValue());
        }
    }

    @Override
    public String createSnippet(final Snippet snippet) throws OXException {
        try {
            final MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());
            // Set headers
            for (final Map.Entry<String, Object> entry : snippet.getProperties().entrySet()) {
                final String name = entry.getKey();
                if (!Snippet.PROP_MISC.equals(name)) {
                    mimeMessage.setHeader(name, entry.getValue().toString());
                }
            }
            // Set other stuff
            List<Attachment> attachments = snippet.getAttachments();
            Object misc = snippet.getMisc();
            if (((null != attachments) && !attachments.isEmpty()) || (null != misc)) {
                final MimeMultipart multipart = new MimeMultipart("mixed");
                // Content part
                {
                    final MimeBodyPart textPart = new MimeBodyPart();
                    textPart.setText(snippet.getContent(), "UTF-8", "plain");
                    multipart.addBodyPart(textPart);
                }
                // Misc
                if (null != misc) {
                    final MimeBodyPart miscPart = new MimeBodyPart();
                    miscPart.setDataHandler(new DataHandler(new MessageDataSource(misc.toString(), "text/javascript; charset=UTF-8")));
                    miscPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                    miscPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType("text/javascript; charset=UTF-8"));
                    multipart.addBodyPart(miscPart);
                }
                // Attachments
                if ((null != attachments) && !attachments.isEmpty()) {
                    for (final Attachment attachment : attachments) {
                        String header = attachment.getContentType();
                        final String contentType = isEmpty(header) ? "text/plain; charset=UTF-8" : header;
                        final MimeBodyPart bodyPart = new MimeBodyPart();
                        bodyPart.setDataHandler(new DataHandler(new MessageDataSource(attachment.getInputStream(), contentType)));
                        bodyPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                        bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(contentType));
                        header = attachment.getContentDisposition();
                        if (null != header) {
                            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(header));
                        }
                        multipart.addBodyPart(bodyPart);
                    }
                }
                // Apply multipart
                mimeMessage.setContent(multipart);
            } else {
                mimeMessage.setText(snippet.getContent(), "UTF-8", "plain");
            }
            // Save
            mimeMessage.saveChanges();
            final byte[] byteArray;
            {
                final ByteArrayOutputStream outputStream = Streams.newByteArrayOutputStream(8192);
                mimeMessage.writeTo(outputStream);
                byteArray = outputStream.toByteArray();
            }
            final QuotaFileStorage fileStorage = getFileStorage(getContext(session));
            return fileStorage.saveNewFile(Streams.newByteArrayInputStream(byteArray), byteArray.length);
        } catch (final MessagingException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SnippetExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void updateSnippet(final String id, final Snippet snippet, final Set<Property> properties) throws OXException {
        
        final QuotaFileStorage fileStorage = getFileStorage(getContext(session));
        final MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession(), fileStorage.getFile(id));
        
    }

    @Override
    public void deleteSnippet(final String id) throws OXException {
        // TODO Auto-generated method stub
        
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }
}
