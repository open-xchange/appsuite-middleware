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

package com.openexchange.snippet.mime;

import static com.openexchange.mail.mime.MimeDefaultSession.getDefaultSession;
import static com.openexchange.snippet.SnippetUtils.sanitizeContent;
import static com.openexchange.snippet.mime.Services.getService;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import java.util.regex.Matcher;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;
import org.slf4j.Logger;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.image.ImageLocation;
import com.openexchange.image.ImageUtility;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaType;
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
import com.openexchange.snippet.SnippetUtils;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link MimeSnippetManagement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeSnippetManagement implements SnippetManagement {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MimeSnippetManagement.class);

    /**
     * The file storage reference type identifier: <b><code>1</code></b>.
     */
    private static final int FS_TYPE = ReferenceType.FILE_STORAGE.getType();

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

    private static IDGeneratorService getIdGeneratorService() {
        return getService(IDGeneratorService.class);
    }

    private static QuotaFileStorage getFileStorage(int contextId) throws OXException {
        return FileStorages.getQuotaFileStorageService().getQuotaFileStorage(contextId);
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    private final int contextId;
    private final int userId;
    private final Session session;
    private final QuotaProvider quotaProvider;

    /**
     * Initializes a new {@link MimeSnippetManagement}.
     */
    public MimeSnippetManagement(Session session, QuotaProvider quotaProvider) {
        super();
        this.session = session;
        this.userId = session.getUserId();
        this.contextId = session.getContextId();
        this.quotaProvider = quotaProvider;
    }

    private AccountQuota getQuota() throws OXException {
        return null == quotaProvider ? null : quotaProvider.getFor(session, "0");
    }

    @Override
    public List<Snippet> getSnippets(String... types) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final StringBuilder sql = new StringBuilder("SELECT id FROM snippet WHERE cid=? AND (user=? OR shared>0) AND refType=").append(FS_TYPE);
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
            final List<String> ids = new LinkedList<String>();
            do {
                ids.add(rs.getString(1));
            } while (rs.next());
            closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
            if (ids.isEmpty()) {
                return Collections.emptyList();
            }
            final List<Snippet> list = new ArrayList<Snippet>(ids.size());
            for (final String id : ids) {
                try {
                    list.add(getSnippet0(id, con));
                } catch (OXException e) {
                    if (!FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                        throw e;
                    }

                    LOGGER.warn("Missing file for snippet {} for user {} in context {}. Maybe file storage is (temporary) not available.", id, userId, contextId, e);
                }
            }
            return list;
        } catch (SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
    }

    @Override
    public int getOwnSnippetsCount() throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final StringBuilder sql = new StringBuilder("SELECT COUNT(id) FROM snippet WHERE cid=? AND user=? AND refType=").append(FS_TYPE);
            stmt = con.prepareStatement(sql.toString());
            int pos = 0;
            stmt.setInt(++pos, contextId);
            stmt.setInt(++pos, userId);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
    }

    @Override
    public List<Snippet> getOwnSnippets() throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final StringBuilder sql = new StringBuilder("SELECT id FROM snippet WHERE cid=? AND user=? AND refType=").append(FS_TYPE);
            stmt = con.prepareStatement(sql.toString());
            int pos = 0;
            stmt.setInt(++pos, contextId);
            stmt.setInt(++pos, userId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            final List<String> ids = new LinkedList<String>();
            do {
                ids.add(rs.getString(1));
            } while (rs.next());
            closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
            if (ids.isEmpty()) {
                return Collections.emptyList();
            }
            final List<Snippet> list = new ArrayList<Snippet>(ids.size());
            for (final String id : ids) {
                try {
                    list.add(getSnippet0(id, con));
                } catch (OXException e) {
                    if (!FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                        throw e;
                    }
                }
            }
            return list;
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
    }

    @Override
    public Snippet getSnippet(final String id) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final Connection con = databaseService.getReadOnly(contextId);
        try {
            return getSnippet0(id, con);
        } catch (OXException e) {
            if (!FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                throw e;
            }
            throw SnippetExceptionCodes.SNIPPET_NOT_FOUND.create(e, id);
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    private Snippet getSnippet0(String identifier, Connection con) throws OXException {
        if (null == identifier) {
            return null;
        }
        if (null == con) {
            return getSnippet(identifier);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final String file;
            final int creator;
            final String displayName;
            final String module;
            final String type;
            final boolean shared;
            {
                stmt = con.prepareStatement("SELECT refId, user, displayName, module, type, shared FROM snippet WHERE cid=? AND id=? AND refType=" + FS_TYPE);
                int pos = 0;
                stmt.setInt(++pos, contextId);
                stmt.setString(++pos, identifier);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    throw SnippetExceptionCodes.SNIPPET_NOT_FOUND.create(identifier);
                }
                file = rs.getString(1);
                creator = rs.getInt(2);
                displayName = rs.getString(3);
                module = rs.getString(4);
                type = rs.getString(5);
                shared = rs.getInt(6) > 0;
                closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }
            MimeMessage mimeMessage;
            {
                InputStream in = null;
                try {
                    QuotaFileStorage fileStorage = getFileStorage(session.getContextId());
                    in = fileStorage.getFile(file);
                    mimeMessage = new MimeMessage(getDefaultSession(), in);
                } catch (OXException e) {
                    if (!FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                        throw e;
                    }
                    throw SnippetExceptionCodes.SNIPPET_NOT_FOUND.create(e, identifier);
                } finally {
                    Streams.close(in);
                }
            }
            com.openexchange.mail.mime.converters.MimeMessageConverter.saveChanges(mimeMessage);
            final DefaultSnippet snippet = new DefaultSnippet().setId(identifier).setCreatedBy(creator);
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
                final Multipart multipart = MimeMessageUtility.getMultipartContentFrom(mimeMessage);
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
            snippet.setDisplayName(displayName).setModule(module).setType(type).setShared(shared);
            return snippet;
        } catch (SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw SnippetExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static void parsePart(MimePart part, DefaultSnippet snippet) throws OXException, MessagingException, IOException {
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

            header = part.getHeader(MessageHeaders.HDR_CONTENT_ID, null);
            if (null != header) {
                if (header.startsWith("<")) {
                    header = header.substring(1, header.length() - 1);
                }
                attachment.setContentId(header);
            }

            attachment.setSize(part.getSize());
            header = part.getHeader("attachmentid", null);
            if (null != header) {
                attachment.setId(header);
            }
            attachment.setStreamProvider(new InputStreamProviderImpl(part));
            snippet.addAttachment(attachment);
        }
    }

    private static void parseSnippet(MimeMessage mimeMessage, MimePart part, DefaultSnippet snippet) throws OXException, MessagingException {
        // Read content from part
        final String header = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null);
        final ContentType contentType = isEmpty(header) ? ContentType.DEFAULT_CONTENT_TYPE : new ContentType(header);
        snippet.setContent(MessageUtility.readMimePart(part, contentType));
        // Read message's headers
        @SuppressWarnings("unchecked") final Enumeration<Header> others = mimeMessage.getAllHeaders();
        while (others.hasMoreElements()) {
            final Header hdr = others.nextElement();
            snippet.put(hdr.getName(), MimeMessageUtility.decodeMultiEncodedHeader(hdr.getValue()));
        }
    }

    private static final Set<String> IGNORABLES = new HashSet<String>(Arrays.asList(Snippet.PROP_MISC));

    private static String encode(String value) {
        try {
            return MimeUtility.encodeText(value, "UTF-8", "Q");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    @Override
    public String createSnippet(Snippet snippet) throws OXException {
        AccountQuota quota = getQuota();
        if (null != quota && quota.hasQuota(QuotaType.AMOUNT)) {
            Quota amountQuota = quota.getQuota(QuotaType.AMOUNT);
            if (amountQuota.isExceeded() || amountQuota.willExceed(1)) {
                throw QuotaExceptionCodes.QUOTA_EXCEEDED_SNIPPETS.create(amountQuota.getUsage(), amountQuota.getLimit());
            }
        }

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
                    mimeMessage.setHeader(name, encode(entry.getValue().toString()));
                }
            }

            final Object misc = snippet.getMisc();
            final String contentSubType = determineContentSubtype(misc);

            // Set other stuff
            final List<Attachment> attachments = snippet.getAttachments();
            if (notEmpty(attachments) || (null != misc)) {
                final MimeMultipart multipart = new MimeMultipart("mixed");
                // Content part
                {
                    final MimeBodyPart textPart = new MimeBodyPart();
                    final String content = snippet.getContent();
                    MessageUtility.setText(sanitizeContent(content), "UTF-8", null == misc ? "plain" : contentSubType, textPart);
                    // textPart.setText(sanitizeContent(snippet.getContent()), "UTF-8", "plain");
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
                MessageUtility.setContent(multipart, mimeMessage);
            } else {
                // The variable "misc" can only be null at this location
                MessageUtility.setText(sanitizeContent(snippet.getContent()), "UTF-8", "plain", mimeMessage);
            }
            // Save
            mimeMessage.saveChanges();
            mimeMessage.removeHeader("Message-ID");
            mimeMessage.removeHeader("MIME-Version");
            // Save MIME content to file storage
            QuotaFileStorage fileStorage = getFileStorage(session.getContextId());
            String file;
            {
                InputStream mimeStream = null;
                try {
                    mimeStream = MimeMessageUtility.getStreamFromPart(mimeMessage);
                    file = fileStorage.saveNewFile(mimeStream);
                } finally {
                    Streams.close(mimeStream);
                }
            }
            // Store in DB, too
            String newId = Integer.toString(getIdGeneratorService().getId("com.openexchange.snippet.mime", contextId));
            boolean error = true;
            try {
                stmt = con.prepareStatement("INSERT INTO snippet (cid, user, id, accountId, displayName, module, type, shared, lastModified, refId, refType) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + FS_TYPE + ")");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, newId);
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
                    newId = null;
                }
            }
            // Return identifier
            return newId;
        } catch (MessagingException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw SnippetExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            databaseService.backWritable(contextId, con);
        }
    }

    @Override
    public String updateSnippet(String identifier, Snippet snippet, Set<Property> properties, Collection<Attachment> addAttachments, Collection<Attachment> removeAttachments) throws OXException {
        if (null == identifier) {
            return null;
        }
        final DatabaseService databaseService = getDatabaseService();
        final int contextId = this.contextId;
        final QuotaFileStorage fileStorage = getFileStorage(session.getContextId());
        boolean error = true;
        String oldFile = null;
        String newFile = null;
        try {
            // Obtain file identifier
            final String displayName;
            final String module;
            final String type;
            final boolean shared;
            {
                final Connection con = databaseService.getReadOnly(contextId);
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = con.prepareStatement("SELECT refId, displayName, module, type, shared FROM snippet WHERE cid=? AND id=? AND refType=" + FS_TYPE);
                    int pos = 0;
                    stmt.setInt(++pos, contextId);
                    stmt.setString(++pos, identifier);
                    rs = stmt.executeQuery();
                    if (!rs.next()) {
                        throw SnippetExceptionCodes.SNIPPET_NOT_FOUND.create(identifier);
                    }
                    oldFile = rs.getString(1);
                    if (null == oldFile) {
                        throw SnippetExceptionCodes.SNIPPET_NOT_FOUND.create(identifier);
                    }
                    displayName = rs.getString(2);
                    module = rs.getString(3);
                    type = rs.getString(4);
                    shared = rs.getInt(5) > 0;
                } finally {
                    closeSQLStuff(rs, stmt);
                    databaseService.backReadOnly(contextId, con);
                }
            }
            // Create MIME message from existing file
            MimeMessage storageMessage;
            {
                InputStream in = null;
                try {
                    in = fileStorage.getFile(oldFile);
                    storageMessage = new MimeMessage(getDefaultSession(), in);
                } catch (OXException e) {
                    if (!FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                        throw e;
                    }
                    throw SnippetExceptionCodes.SNIPPET_NOT_FOUND.create(e, identifier);
                } finally {
                    Streams.close(in);
                }
            }
            final ContentType storageContentType;
            {
                final String header = storageMessage.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null);
                storageContentType = isEmpty(header) ? ContentType.DEFAULT_CONTENT_TYPE : new ContentType(header);
            }
            // New MIME message for changes
            final MimeMessage updateMessage = new MimeMessage(getDefaultSession());
            // Update properties
            {
                final List<String> propNames = new LinkedList<String>();
                for (final Property property : properties) {
                    switch (property) {
                        case ACCOUNT_ID:
                            updateMessage.setHeader(Property.ACCOUNT_ID.getPropName(), Integer.toString(snippet.getAccountId()));
                            propNames.add(Snippet.PROP_ACCOUNT_ID);
                            break;
                        case CREATED_BY:
                            updateMessage.setHeader(Property.CREATED_BY.getPropName(), Integer.toString(snippet.getCreatedBy()));
                            propNames.add(Snippet.PROP_CREATED_BY);
                            break;
                        case DISPLAY_NAME:
                            updateMessage.setHeader(Property.DISPLAY_NAME.getPropName(), encode(snippet.getDisplayName()));
                            propNames.add(Snippet.PROP_DISPLAY_NAME);
                            break;
                        case MODULE:
                            updateMessage.setHeader(Property.MODULE.getPropName(), snippet.getModule());
                            propNames.add(Snippet.PROP_MODULE);
                            break;
                        case SHARED:
                            updateMessage.setHeader(Property.SHARED.getPropName(), Boolean.toString(snippet.isShared()));
                            propNames.add(Snippet.PROP_SHARED);
                            break;
                        case TYPE:
                            updateMessage.setHeader(Property.TYPE.getPropName(), snippet.getType());
                            propNames.add(Snippet.PROP_TYPE);
                            break;
                        default:
                            break;
                    }
                }

                // Copy remaining to updateMessage; this action includes unnamed properties
                @SuppressWarnings("unchecked") final Enumeration<Header> nonMatchingHeaders = storageMessage.getNonMatchingHeaders(propNames.toArray(new String[0]));
                final Set<String> propertyNames = Property.getPropertyNames();
                while (nonMatchingHeaders.hasMoreElements()) {
                    final Header hdr = nonMatchingHeaders.nextElement();
                    if (propertyNames.contains(hdr.getName())) {
                        updateMessage.setHeader(hdr.getName(), encode(hdr.getValue()));
                    }
                }
            }

            // Check for content
            String content;
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

            // Extract image identifiers
            Set<String> contentIds = new HashSet<String>(MimeMessageUtility.getContentIDs(content));
            contentIds.addAll(extractContentIDs(content));

            // Check for misc
            MimePart miscPart;
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
                    Multipart multipart = (Multipart) storageMessage.getContent();
                    int length = multipart.getCount();
                    MimePart mp = null;
                    for (int i = 1; null == mp && i < length; i++) { // skip first
                        BodyPart bodyPart = multipart.getBodyPart(i);
                        String header = Strings.asciiLowerCase(MimeMessageUtility.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null, bodyPart));
                        if (null != header && header.startsWith("text/javascript")) {
                            mp = (MimePart) bodyPart;
                        }
                    }
                    miscPart = mp;
                } else {
                    miscPart = null;
                }
            }

            // Check for attachments
            List<MimeBodyPart> attachmentParts = new ArrayList<MimeBodyPart>();

            // Add existing
            if (storageContentType.startsWith("multipart/")) {
                Multipart storageMultipart = (Multipart) storageMessage.getContent();
                int length = storageMultipart.getCount();
                for (int i = 1; i < length; i++) { // skip first
                    MimeBodyPart bodyPart = (MimeBodyPart) storageMultipart.getBodyPart(i);
                    String header = Strings.asciiLowerCase(MimeMessageUtility.getHeader(MessageHeaders.HDR_CONTENT_TYPE, null, bodyPart));
                    if (null == header) {
                        attachmentParts.add(bodyPart);
                    } else if (!header.startsWith("text/javascript")) {
                        // Check for inline image attachment
                        if (header.startsWith("image/")) {
                            String optContentId = MimeMessageUtility.getHeader(MessageHeaders.HDR_CONTENT_ID, null, bodyPart);
                            if (null == content) {
                                attachmentParts.add(bodyPart);
                            } else {
                                String disp = MimeMessageUtility.getHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, null, bodyPart);
                                if (null != disp && Strings.asciiLowerCase(disp).trim().startsWith("inline")) {
                                    // Still referenced in HTML content
                                    if (contentIds.contains(MimeMessageUtility.trimContentId(optContentId))) {
                                        attachmentParts.add(bodyPart);
                                    }
                                } else {
                                    attachmentParts.add(bodyPart);
                                }
                            }
                        } else {
                            attachmentParts.add(bodyPart);
                        }
                    }
                }
            }

            // Removed
            if (notEmpty(removeAttachments)) {
                for (Attachment attachment : removeAttachments) {
                    for (Iterator<MimeBodyPart> iterator = attachmentParts.iterator(); iterator.hasNext();) {
                        final String header = iterator.next().getHeader("attachmentid", null);
                        if (null != header && header.equals(attachment.getId())) {
                            iterator.remove();
                        }
                    }
                }
            }

            // New ones
            if (notEmpty(addAttachments)) {
                for (Attachment attachment : addAttachments) {
                    attachmentParts.add(attachment2MimePart(attachment));
                }
            }
            // Check gathered parts
            if (null != miscPart || notEmpty(attachmentParts)) {
                // Create a multipart message
                Multipart primaryMultipart = new MimeMultipart();

                // Add text part
                MimeBodyPart textPart = new MimeBodyPart();
                String subType = determineContentSubtype(snippet.getMisc());
                // MessageUtility.setText(sanitizeContent(snippet.getContent()), "UTF-8", null == miscPart ? "plain" :
                // determineContentSubtype(MessageUtility.readMimePart(miscPart, "UTF-8")), textPart);
                textPart.setText(sanitizeContent(content), "UTF-8", subType);
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

                // MessageUtility.setContent(primaryMultipart, updateMessage);
                // updateMessage.setContent(primaryMultipart);
            } else {
                MessageUtility.setText(sanitizeContent(content), "UTF-8", "plain", updateMessage);
                // updateMessage.setText(sanitizeContent(content), "UTF-8", "plain");
                updateMessage.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
            }

            // Save to MIME structure...
            updateMessage.saveChanges();
            updateMessage.removeHeader("Message-ID");
            updateMessage.removeHeader("MIME-Version");

            // ... and write to byte array
            byte[] byteArray;
            {
                final ByteArrayOutputStream outputStream = Streams.newByteArrayOutputStream(8192);
                updateMessage.writeTo(outputStream);
                byteArray = outputStream.toByteArray();
            }

            // Create file carrying new MIME data
            newFile = fileStorage.saveNewFile(Streams.newByteArrayInputStream(byteArray), byteArray.length);
            byteArray = null; // Drop immediately
            {
                Connection con = databaseService.getWritable(contextId);
                PreparedStatement stmt = null;
                boolean rollback = false;
                try {
                    /*-
                     * Update DB, too
                     *
                     * 1. Create dummy entry to check DB schema consistency
                     * 2. Delete existing
                     * 3. Make dummy entry the real entry
                     */
                    final String dummyId = "--" + identifier;
                    con.setAutoCommit(false); // BEGIN
                    rollback = true;
                    stmt = con.prepareStatement("INSERT INTO snippet (cid, user, id, accountId, displayName, module, type, shared, lastModified, refId, refType) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + FS_TYPE + ")");
                    int pos = 0;
                    stmt.setInt(++pos, contextId);
                    stmt.setInt(++pos, userId);
                    stmt.setString(++pos, dummyId);
                    {
                        final String sAccountId = updateMessage.getHeader(Property.ACCOUNT_ID.getPropName(), null);
                        if (sAccountId != null) {
                            stmt.setInt(++pos, Integer.parseInt(sAccountId));
                        } else {
                            stmt.setNull(++pos, Types.INTEGER);
                        }
                    }
                    stmt.setString(++pos, properties.contains(Property.DISPLAY_NAME) ? getObject(snippet.getDisplayName(), displayName) : displayName);
                    stmt.setString(++pos, properties.contains(Property.MODULE) ? getObject(snippet.getModule(), module) : module);
                    stmt.setString(++pos, properties.contains(Property.TYPE) ? getObject(snippet.getType(), type) : type);
                    stmt.setInt(++pos, properties.contains(Property.SHARED) ? (snippet.isShared() ? 1 : 0) : (shared ? 1 : 0));
                    stmt.setLong(++pos, System.currentTimeMillis());
                    stmt.setString(++pos, newFile);
                    stmt.executeUpdate();
                    closeSQLStuff(stmt);
                    stmt = con.prepareStatement("DELETE FROM snippet WHERE cid=? AND user=? AND id=? AND refType=" + FS_TYPE);
                    pos = 0;
                    stmt.setLong(++pos, contextId);
                    stmt.setLong(++pos, userId);
                    stmt.setString(++pos, identifier);
                    stmt.executeUpdate();
                    closeSQLStuff(stmt);
                    stmt = con.prepareStatement("UPDATE snippet SET id=? WHERE cid=? AND user=? AND id=? AND refType=" + FS_TYPE);
                    pos = 0;
                    stmt.setString(++pos, identifier);
                    stmt.setLong(++pos, contextId);
                    stmt.setLong(++pos, userId);
                    stmt.setString(++pos, dummyId);
                    stmt.executeUpdate();
                    con.commit(); // COMMIT
                    rollback = false;
                } finally {
                    if (rollback) {
                        DBUtils.rollback(con);
                    }
                    closeSQLStuff(stmt);
                    DBUtils.autocommit(con);
                    databaseService.backWritable(contextId, con);
                }
            }

            // Mark as successfully processed
            error = false;
            return identifier;
        } catch (MessagingException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw SnippetExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw SnippetExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (error) {
                // Delete newly created file
                deleteSafe(newFile, fileStorage);
            } else {
                // Delete obsolete file
                deleteSafe(oldFile, fileStorage);
            }
        }
    }

    /**
     * Extracts image identifiers from given HTML content
     *
     * @param htmlContent The HTML content
     * @return The extracted image identifiers
     */
    protected static Set<String> extractContentIDs(String htmlContent) {
        Matcher matcher = MimeMessageUtility.PATTERN_SRC.matcher(htmlContent);
        if (!matcher.find()) {
            return Collections.emptySet();
        }

        Set<String> set = new HashSet<String>(2);
        do {
            String imageUri = matcher.group(1);
            if (!imageUri.startsWith("cid:")) {
                ImageLocation imageLocation = ImageUtility.parseImageLocationFrom(imageUri);
                if (null != imageLocation) {
                    String imageId = imageLocation.getImageId();
                    if (null != imageId) {
                        set.add(imageId);
                    }
                }
            }
        } while (matcher.find());
        return set;
    }

    private static void deleteSafe(String file, QuotaFileStorage fileStorage) {
        if (null == file) {
            return;
        }
        try {
            fileStorage.deleteFile(file);
        } catch (final Exception e) {
            // Ignore any regular exception
        }
    }

    private static <V> V getObject(V o1, V o2) {
        return o1 == null ? (o2 == null ? o1 : o2) : o1;
    }

    @Override
    public void deleteSnippet(final String id) throws OXException {
        DatabaseService databaseService = getDatabaseService();

        int contextId = this.contextId;
        Connection con = databaseService.getWritable(contextId);
        try {
            deleteSnippet(id, userId, contextId, con);
        } finally {
            databaseService.backWritable(contextId, con);
        }
    }

    /**
     * Safely deletes specified snippet
     *
     * @param identifier The snippet identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If delete attempt fails
     */
    public static void deleteSnippetSafe(String identifier, int userId, int contextId) {
        DatabaseService databaseService = getDatabaseService();

        Connection con = null;
        try {
            con = databaseService.getWritable(contextId);
            deleteSnippetSafe(identifier, userId, contextId, con);
        } catch (Exception e) {
            // Ignore
        } finally {
            if (null != con) {
                databaseService.backWritable(contextId, con);
            }
        }
    }

    /**
     * Safely deletes specified snippet
     *
     * @param identifier The snippet identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @throws OXException If delete attempt fails
     */
    public static void deleteSnippetSafe(String identifier, int userId, int contextId, Connection con) {
        if (null == con) {
            deleteSnippetSafe(identifier, userId, contextId);
            return;
        }
        try {
            deleteSnippet(identifier, userId, contextId, con);
        } catch (Exception e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(MimeSnippetManagement.class);
            logger.warn("Failed to delete snippet {} for user {} in context {}", identifier, userId, contextId, e);
        }
    }

    /**
     * Deletes specified snippet
     *
     * @param identifier The snippet identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @throws OXException If delete attempt fails
     */
    public static void deleteSnippet(String identifier, int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            final String file;
            {
                stmt = con.prepareStatement("SELECT refId FROM snippet WHERE cid=? AND id=? AND refType=" + FS_TYPE);
                int pos = 0;
                stmt.setInt(++pos, contextId);
                stmt.setString(++pos, identifier);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    throw SnippetExceptionCodes.SNIPPET_NOT_FOUND.create(identifier);
                }
                file = rs.getString(1);
                closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }

            final QuotaFileStorage fileStorage = getFileStorage(contextId);
            deleteSafe(file, fileStorage);
            stmt = con.prepareStatement("DELETE FROM snippet WHERE cid=? AND user=? AND id=? AND refType=" + FS_TYPE);
            int pos = 0;
            stmt.setLong(++pos, contextId);
            stmt.setLong(++pos, userId);
            stmt.setString(++pos, identifier);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw SnippetExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static MimeBodyPart attachment2MimePart(Attachment attachment) throws MessagingException, IOException {
        /*
         * Content-Type
         */
        String header = attachment.getContentType();
        final String contentType = isEmpty(header) ? "text/plain; charset=UTF-8" : header;
        final MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setDataHandler(new DataHandler(new MessageDataSource(attachment.getInputStream(), contentType)));
        bodyPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        bodyPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(contentType));
        String attachmentId = attachment.getId();
        if (null == attachmentId) {
            attachmentId = UUID.randomUUID().toString();
        }
        bodyPart.setHeader("attachmentid", attachmentId);
        header = attachment.getContentId();
        if (null == header) {
            header = "<" + UUIDs.getUnformattedString(UUID.randomUUID()) + ">";
        }
        bodyPart.setHeader(MessageHeaders.HDR_CONTENT_ID, header.startsWith("<") ? header : "<" + header + ">");
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
        return com.openexchange.java.Strings.isEmpty(string);
    }

    private static String determineContentSubtype(final Object misc) throws OXException {
        if (misc == null) {
            return "plain";
        }
        final String ct = SnippetUtils.parseContentTypeFromMisc(misc);
        return new ContentType(ct).getSubType();
    }
}
