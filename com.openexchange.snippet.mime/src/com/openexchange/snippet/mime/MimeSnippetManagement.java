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

import static com.openexchange.mail.mime.MimeDefaultSession.getDefaultSession;
import static com.openexchange.snippet.mime.Services.getService;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.session.Session;
import com.openexchange.snippet.Attachment;
import com.openexchange.snippet.DefaultAttachment;
import com.openexchange.snippet.DefaultAttachment.InputStreamProvider;
import com.openexchange.snippet.DefaultSnippet;
import com.openexchange.snippet.Property;
import com.openexchange.snippet.ReferenceType;
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

    private static DatabaseService getDatabaseService() {
        return getService(DatabaseService.class);
    }

    private static Context getContext(final int contextId) throws OXException {
        return getService(ContextService.class).getContext(contextId);
    }

    private static Context getContext(final Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getContext();
        }
        return getService(ContextService.class).getContext(session.getContextId());
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

    private final int contextId;
    private final int userId;
    private final Session session;

    /**
     * Initializes a new {@link MimeSnippetManagement}.
     */
    public MimeSnippetManagement(final Session session) {
        super();
        this.session = session;
        this.userId = session.getUserId();
        this.contextId = session.getContextId();
    }

    @Override
    public List<Snippet> getSnippets(final String... types) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getReadOnly(contextId);
        final List<String> ids;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final StringBuilder sql = new StringBuilder("SELECT id FROM snippet WHERE cid=? AND (user=? OR shared>0) AND refType=").append(ReferenceType.FILE_STORAGE.getType());
            final boolean hasTypes = (null != types) && (types.length > 0);
            if (hasTypes) {
                sql.append(" AND (");
                sql.append("type=?");
                for (int i = 1; i < types.length; i++) {
                    sql.append(" OR type=?");
                }
                sql.append(')');
            }
            stmt = con.prepareStatement(sql.toString());
            int pos = 0;
            stmt.setInt(++pos, contextId);
            stmt.setInt(++pos, userId);
            if (hasTypes) {
                for (final String type : types) {
                    stmt.setString(++pos, type);
                }
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            ids = new LinkedList<String>();
            do {
                ids.add(rs.getString(1));
            } while (rs.next());
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Snippet> list = new ArrayList<Snippet>(ids.size());
        for (final String id : ids) {
            list.add(getSnippet(id));
        }
        return list;
    }

    @Override
    public List<Snippet> getOwnSnippets() throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getReadOnly(contextId);
        final List<String> ids;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final StringBuilder sql = new StringBuilder("SELECT id FROM snippet WHERE cid=? AND user=? AND refType=").append(ReferenceType.FILE_STORAGE.getType());
            stmt = con.prepareStatement(sql.toString());
            int pos = 0;
            stmt.setInt(++pos, contextId);
            stmt.setInt(++pos, userId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            ids = new LinkedList<String>();
            do {
                ids.add(rs.getString(1));
            } while (rs.next());
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Snippet> list = new ArrayList<Snippet>(ids.size());
        for (final String id : ids) {
            list.add(getSnippet(id));
        }
        return list;
    }

    @Override
    public Snippet getSnippet(final String id) throws OXException {
        try {
            final QuotaFileStorage fileStorage = getFileStorage(getContext(session));
            final MimeMessage mimeMessage = new MimeMessage(getDefaultSession(), fileStorage.getFile(id));
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
            header = part.getHeader("AttachmentId", null);
            if (null != header) {
                attachment.setId(header);
            }
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

    private static final Set<String> IGNORABLES = new HashSet<String>(Arrays.asList(Snippet.PROP_MISC));

    @Override
    public String createSnippet(final Snippet snippet) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            final MimeMessage mimeMessage = new MimeMessage(getDefaultSession());
            mimeMessage.setHeader(Property.CREATED_BY.getPropName(), Integer.toString(userId));
            // Set headers
            for (final Map.Entry<String, Object> entry : snippet.getProperties().entrySet()) {
                final String name = entry.getKey();
                if (!IGNORABLES.contains(name)) {
                    mimeMessage.setHeader(name, entry.getValue().toString());
                }
            }
            // Set other stuff
            final List<Attachment> attachments = snippet.getAttachments();
            final Object misc = snippet.getMisc();
            if (notEmpty(attachments) || (null != misc)) {
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
                if (notEmpty(attachments)) {
                    for (final Attachment attachment : attachments) {
                        if (null == attachment.getId()) {
                            if (!(attachment instanceof DefaultAttachment)) {
                                throw SnippetExceptionCodes.ILLEGAL_STATE.create("Missing attachment identifier");
                            }
                            ((DefaultAttachment) attachment).setId(UUID.randomUUID().toString());
                        }
                        multipart.addBodyPart(attachment2MimePart(attachment));
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
            String file = fileStorage.saveNewFile(Streams.newByteArrayInputStream(byteArray), byteArray.length);
            // Store in DB, too
            boolean error = true;
            try {
                stmt =
                    con.prepareStatement("INSERT INTO snippet (cid, user, id, accountId, displayName, module, type, shared, lastModified, refId, refType) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + ReferenceType.FILE_STORAGE.getType() + ")");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, file);
                {
                    final int accountId = snippet.getAccountId();
                    if (accountId >= 0) {
                        stmt.setInt(4, accountId);
                    } else {
                        stmt.setNull(4, Types.INTEGER);
                    }
                }
                stmt.setString(5, snippet.getDisplayName());
                stmt.setString(6, snippet.getModule());
                stmt.setString(7, snippet.getType());
                stmt.setInt(8, snippet.isShared() ? 1 : 0);
                stmt.setLong(9, System.currentTimeMillis());
                stmt.setString(10, file);
                stmt.executeUpdate();
                error = false;
            } finally {
                if (error) {
                    // Delete file on error
                    fileStorage.deleteFile(file);
                    file = null;
                }
            }
            // Return identifier
            return file;
        } catch (final MessagingException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SnippetExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            databaseService.backWritable(contextId, con);
        }
    }

    @Override
    public String updateSnippet(final String id, final Snippet snippet, final Set<Property> properties, final Collection<Attachment> addAttachments, final Collection<Attachment> removeAttachments) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            final QuotaFileStorage fileStorage = getFileStorage(getContext(session));
            final MimeMessage storageMessage = new MimeMessage(getDefaultSession(), fileStorage.getFile(id));
            final ContentType storageContentType;
            {
                final String header = storageMessage.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null);
                storageContentType = isEmpty(header) ? ContentType.DEFAULT_CONTENT_TYPE : new ContentType(header);
            }

            final MimeMessage updateMessage = new MimeMessage(getDefaultSession());
            // Update properties
            {
                final List<String> propNames = new LinkedList<String>();
                for (final Property property : properties) {
                    switch (property) {
                    case ACCOUNT_ID:
                        updateMessage.setHeader(Property.ACCOUNT_ID.getPropName(), Integer.toString(snippet.getAccountId()));
                        propNames.add(Property.ACCOUNT_ID.getPropName());
                        break;
                    case CREATED_BY:
                        updateMessage.setHeader(Property.CREATED_BY.getPropName(), Integer.toString(snippet.getCreatedBy()));
                        propNames.add(Property.CREATED_BY.getPropName());
                        break;
                    case DISPLAY_NAME:
                        updateMessage.setHeader(Property.DISPLAY_NAME.getPropName(), snippet.getDisplayName());
                        propNames.add(Property.DISPLAY_NAME.getPropName());
                        break;
                    case MODULE:
                        updateMessage.setHeader(Property.MODULE.getPropName(), snippet.getModule());
                        propNames.add(Property.MODULE.getPropName());
                        break;
                    case SHARED:
                        updateMessage.setHeader(Property.SHARED.getPropName(), Boolean.toString(snippet.isShared()));
                        propNames.add(Property.SHARED.getPropName());
                        break;
                    case TYPE:
                        updateMessage.setHeader(Property.TYPE.getPropName(), snippet.getType());
                        propNames.add(Property.TYPE.getPropName());
                        break;
                    default:
                        break;
                    }
                }
                // Copy remaining to updateMessage; this action included unnamed properties
                @SuppressWarnings("unchecked")
                final Enumeration<Header> nonMatchingHeaders = storageMessage.getNonMatchingHeaders(propNames.toArray(new String[0]));
                while (nonMatchingHeaders.hasMoreElements()) {
                    final Header hdr = nonMatchingHeaders.nextElement();
                    updateMessage.setHeader(hdr.getName(), hdr.getValue());
                }
            }
            // Check for content
            final String content;
            if (properties.contains(Property.CONTENT)) {
                content = snippet.getContent();
            } else {
                final MimePart textPart;
                final ContentType ct;
                if (storageContentType.startsWith("multipart/")) {
                    textPart = (MimePart) ((Multipart) storageMessage.getContent()).getBodyPart(0);
                    final String header = textPart.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null);
                    ct = isEmpty(header) ? ContentType.DEFAULT_CONTENT_TYPE : new ContentType(header);
                } else {
                    textPart = storageMessage;
                    ct = storageContentType;
                }
                content = MessageUtility.readMimePart(textPart, ct);
            }
            // Check for misc
            final MimePart miscPart;
            if (properties.contains(Property.MISC)) {
                final Object misc = snippet.getMisc();
                if (null == misc) {
                    miscPart = null;
                } else {
                    miscPart = new MimeBodyPart();
                    miscPart.setDataHandler(new DataHandler(new MessageDataSource(misc.toString(), "text/javascript; charset=UTF-8")));
                    miscPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                    miscPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType("text/javascript; charset=UTF-8"));
                }
            } else {
                if (storageContentType.startsWith("multipart/")) {
                    final Multipart multipart = (Multipart) storageMessage.getContent();
                    final int length = multipart.getCount();
                    MimePart mp = null;
                    for (int i = 1; null == mp && i < length; i++) { // skip first
                        final BodyPart bodyPart = multipart.getBodyPart(i);
                        final String header = storageMessage.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null);
                        if (null != header && header.toLowerCase(Locale.US).startsWith("text/javascript")) {
                            mp = (MimePart) bodyPart;
                        }
                    }
                    miscPart = mp;
                } else {
                    miscPart = null;
                }
            }
            // Check for attachments
            final List<MimeBodyPart> attachmentParts = new ArrayList<MimeBodyPart>();
            // Add existing
            if (storageContentType.startsWith("multipart/")) {
                final Multipart multipart = (Multipart) storageMessage.getContent();
                final int length = multipart.getCount();
                for (int i = 1; i < length; i++) { // skip first
                    final String header = storageMessage.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null);
                    if (null == header || !header.toLowerCase(Locale.US).startsWith("text/javascript")) {
                        attachmentParts.add((MimeBodyPart) multipart.getBodyPart(i));
                    }
                }
            }
            // Removed
            if (notEmpty(removeAttachments)) {
                for (final Attachment attachment : removeAttachments) {
                    for (final Iterator<MimeBodyPart> iterator = attachmentParts.iterator(); iterator.hasNext();) {
                        final String header = iterator.next().getHeader("AttachmentId", null);
                        if (null != header && header.equals(attachment.getId())) {
                            iterator.remove();
                        }
                    }
                }
            }
            // New ones
            if (notEmpty(addAttachments)) {
                for (final Attachment attachment : addAttachments) {
                    attachmentParts.add(attachment2MimePart(attachment));
                }
            }
            // Check gathered parts
            if (null != miscPart || notEmpty(attachmentParts)) {
                // Create a multipart message
                final Multipart primaryMultipart = new MimeMultipart();
                // Add text part
                final MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(content, "UTF-8", "plain");
                textPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                primaryMultipart.addBodyPart(textPart);
                // Add attachment parts
                if (notEmpty(attachmentParts)) {
                    for (final MimeBodyPart mimePart : attachmentParts) {
                        primaryMultipart.addBodyPart(mimePart);
                    }
                }
                // Add misc part
                if (null != miscPart) {
                    primaryMultipart.addBodyPart((BodyPart) miscPart);
                }
                // Apply to message
                updateMessage.setContent(primaryMultipart);
            } else {
                updateMessage.setText(content, "UTF-8", "plain");
                updateMessage.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
            }
            // Save
            updateMessage.saveChanges();
            final byte[] byteArray;
            {
                final ByteArrayOutputStream outputStream = Streams.newByteArrayOutputStream(8192);
                updateMessage.writeTo(outputStream);
                byteArray = outputStream.toByteArray();
            }
            final String file = fileStorage.saveNewFile(Streams.newByteArrayInputStream(byteArray), byteArray.length);
            /*
             * Update DB, too
             */
            stmt = con.prepareStatement("DELETE FROM snippet WHERE cid=? AND user=? AND id=? AND refType="+ReferenceType.FILE_STORAGE.getType());
            int pos = 0;
            stmt.setLong(++pos, contextId);
            stmt.setLong(++pos, userId);
            stmt.setString(++pos, id);
            stmt.executeUpdate();
            closeSQLStuff(stmt);
            stmt = con.prepareStatement("INSERT INTO snippet (cid, user, id, accountId, displayName, module, type, shared, lastModified, refId, refType) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "+ReferenceType.FILE_STORAGE.getType()+")");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, file);
            {
                final String sAccountId = updateMessage.getHeader(Property.ACCOUNT_ID.getPropName(), null);
                if (sAccountId != null) {
                    stmt.setInt(4, Integer.parseInt(sAccountId));
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
            }
            stmt.setString(5, updateMessage.getHeader(Property.DISPLAY_NAME.getPropName(), null));
            stmt.setString(6, updateMessage.getHeader(Property.MODULE.getPropName(), null));
            stmt.setString(7, updateMessage.getHeader(Property.TYPE.getPropName(), null));
            stmt.setInt(8, Boolean.parseBoolean(updateMessage.getHeader(Property.SHARED.getPropName(), null)) ? 1 : 0);
            stmt.setLong(9, System.currentTimeMillis());
            stmt.setString(10, file);
            stmt.executeUpdate();
            return file;
        } catch (final MessagingException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SnippetExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            databaseService.backWritable(contextId, con);
        }
    }

    @Override
    public void deleteSnippet(final String id) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getWritable(contextId);
        try {
            deleteSnippet(id, userId, contextId, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    /**
     * Deletes specified snippet
     *
     * @param id The snippet identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @throws OXException If delete attempt fails
     */
    public static void deleteSnippet(final String id, final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            final QuotaFileStorage fileStorage = getFileStorage(getContext(contextId));
            fileStorage.deleteFile(id);
            stmt = con.prepareStatement("DELETE FROM snippet WHERE cid=? AND user=? AND id=? AND refType="+ReferenceType.FILE_STORAGE.getType());
            int pos = 0;
            stmt.setLong(++pos, contextId);
            stmt.setLong(++pos, userId);
            stmt.setString(++pos, id);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SnippetExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static MimeBodyPart attachment2MimePart(final Attachment attachment) throws MessagingException, IOException {
        /*
         * Content-Type
         */
        String header = attachment.getContentType();
        final String contentType = isEmpty(header) ? "text/plain; charset=UTF-8" : header;
        final MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setDataHandler(new DataHandler(new MessageDataSource(attachment.getInputStream(), contentType)));
        bodyPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(contentType));
        final String attachmentId = attachment.getId();
        if (null != attachmentId) {
            bodyPart.setHeader("AttachmentId", attachmentId);
        }
        /*
         * Force base64 encoding
         */
        bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, "base64");
        /*
         * Content-Disposition
         */
        header = attachment.getContentDisposition();
        if (null != header) {
            bodyPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(header));
        }
        return bodyPart;
    }

    private static <E> boolean notEmpty(final Collection<E> col) {
        return null != col && !col.isEmpty();
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
