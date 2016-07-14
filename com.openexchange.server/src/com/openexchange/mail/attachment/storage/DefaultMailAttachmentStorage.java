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

package com.openexchange.mail.attachment.storage;

import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.datasource.DocumentDataSource;
import com.openexchange.mail.mime.processing.MimeProcessingUtility;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderSQL;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link DefaultMailAttachmentStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class DefaultMailAttachmentStorage implements MailAttachmentStorage {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultMailAttachmentStorage.class);

    private static class PublicationRefs {

        final PublicationTarget target;
        final PublicationService publisher;

        PublicationRefs(PublicationTarget target, PublicationService publisher) {
            super();
            this.target = target;
            this.publisher = publisher;
        }

    }

    // ------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link DefaultMailAttachmentStorage}.
     */
    public DefaultMailAttachmentStorage() {
        super();
    }

    private PublicationRefs getPublicationRefs() throws OXException {
        PublicationTargetDiscoveryService discoveryService = ServerServiceRegistry.getInstance().getService(PublicationTargetDiscoveryService.class, true);

        PublicationTarget target = discoveryService.getTarget("com.openexchange.publish.online.infostore.document");
        if (null == target) {
            LOG.warn("Missing publication target for ID \"com.openexchange.publish.online.infostore.document\".\nThrowing quota-exceeded exception instead.");
            throw ServiceExceptionCode.absentService(PublicationTarget.class);
        }

        PublicationService publisher = target.getPublicationService();

        return new PublicationRefs(target, publisher);
    }

    private Context getContext(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getContext();
        }
        return ContextStorage.getStorageContext(session.getContextId());
    }

    private Locale getSessionUserLocale(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser().getLocale();
        }
        final Context context = ContextStorage.getStorageContext(session.getContextId());
        return UserStorage.getInstance().getUser(session.getUserId(), context).getLocale();
    }

    private static ServerSession getServerSessionFrom(final Session session, final Context context) {
        if (session instanceof ServerSession) {
            return (ServerSession) session;
        }
        return ServerSessionAdapter.valueOf(session, context);
    }

    @Override
    public void prepareStorage(String folderName, boolean checkForExpiredAttachments, long timeToLive, Session session) throws OXException {
        prepareStorage0(folderName, checkForExpiredAttachments, timeToLive, session);
    }

    private int prepareStorage0(String folderName, boolean checkForExpiredAttachments, long timeToLive, Session session) throws OXException {
        try {
            Context ctx = getContext(session);
            ServerSession serverSession = getServerSessionFrom(session, ctx);
            UserPermissionBits permissionBits = serverSession.getUserPermissionBits();

            OXFolderAccess folderAccess = new OXFolderAccess(ctx);
            FolderObject defaultInfoStoreFolder = folderAccess.getDefaultFolder(serverSession.getUserId(), FolderObject.INFOSTORE);
            if (!defaultInfoStoreFolder.getEffectiveUserPermission(serverSession.getUserId(), permissionBits).canCreateSubfolders()) {
                throw OXFolderExceptionCode.NO_CREATE_SUBFOLDER_PERMISSION.create(session.getUserId(), defaultInfoStoreFolder.getObjectID(), ctx.getContextId());
            }

            String name = folderName;
            final int folderId;
            final int lookUpFolder = OXFolderSQL.lookUpFolder(defaultInfoStoreFolder.getObjectID(), name, FolderObject.INFOSTORE, null, ctx);
            if (-1 == lookUpFolder) {
                synchronized (DefaultMailAttachmentStorage.class) {
                    folderId = createIfAbsent(serverSession, ctx, name, defaultInfoStoreFolder);
                }
            } else {
                folderId = lookUpFolder;
            }
            serverSession.setParameter(MailSessionParameterNames.getParamPublishingInfostoreFolderID(), Integer.valueOf(folderId));
            /*
             * Check for elapsed documents inside infostore folder
             */
            if (checkForExpiredAttachments) {
                IDBasedFileAccess fileAccess = ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class).createAccess(serverSession);
                long now = System.currentTimeMillis();
                List<String> toRemove = getElapsedDocuments(folderId, fileAccess, serverSession, now, timeToLive);
                if (!toRemove.isEmpty()) {
                    /*
                     * Remove elapsed documents
                     */
                    fileAccess.startTransaction();
                    try {
                        fileAccess.removeDocument(toRemove, now);
                        fileAccess.commit();
                    } finally {
                        finishSafe(fileAccess);
                    }
                }
            }
            return folderId;
        } catch (SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String storeAttachment(MailPart attachment, StoreOperation op, Map<String, Object> storeProps, Session session) throws OXException {
        IDBasedFileAccessFactory fileAccessFactory = ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class, true);
        boolean publishStore = StoreOperation.PUBLISH_STORE.equals(op);

        // Check for folder ID
        String folderId;
        {
            String sFolderId = null == storeProps ? null : (String) storeProps.get("folder");
            if (null != sFolderId) {
                folderId = sFolderId;
            } else if (publishStore) {
                final String key = MailSessionParameterNames.getParamPublishingInfostoreFolderID();
                if (session.containsParameter(key)) {
                    folderId = ((Integer) session.getParameter(key)).toString();
                } else {
                    // Folder name
                    String name = TransportProperties.getInstance().getPublishingInfostoreFolder();
                    if ("i18n-defined".equals(name)) {
                        name = FolderStrings.DEFAULT_EMAIL_ATTACHMENTS_FOLDER_NAME;
                    }
                    int fuid = prepareStorage0(name, false, 0, session);
                    folderId = Integer.toString(fuid);
                    session.setParameter(MailSessionParameterNames.getParamPublishingInfostoreFolderID(), Integer.valueOf(fuid));
                }
            } else {
                throw MailExceptionCode.MISSING_PARAM.create("folder");
            }
        }

        // Create document meta data for current attachment
        String name = attachment.getFileName();
        if (name == null) {
            name = "attachment";
        }
        final File file = new DefaultFile();
        file.setId(FileStorageFileAccess.NEW);
        file.setFolderId(folderId);
        file.setFileName(name);
        file.setFileMIMEType(attachment.getContentType().getBaseType());
        file.setTitle(name);
        file.setFileSize(attachment.getSize());
        List<Field> modifiedColumns = new ArrayList<Field>();
        modifiedColumns.add(Field.FILENAME);
        modifiedColumns.add(Field.FILE_SIZE);
        modifiedColumns.add(Field.FILE_MIMETYPE);
        modifiedColumns.add(Field.TITLE);
        if (null != storeProps) {
            String description = (String) storeProps.get("description");
            if (null != description) {
                file.setDescription(description);
                modifiedColumns.add(Field.DESCRIPTION);
            } else if (publishStore) {
                // Description
                Locale locale = (Locale) storeProps.get("externalLocale");
                if (null == locale) {
                    locale = getSessionUserLocale(session);
                }
                final StringHelper stringHelper = StringHelper.valueOf(locale);
                String desc = stringHelper.getString(MailStrings.PUBLISHED_ATTACHMENT_INFO);
                {
                    final String subject = (String) storeProps.get("subject");
                    desc = desc.replaceFirst("#SUBJECT#", com.openexchange.java.Strings.quoteReplacement(null == subject ? stringHelper.getString(MailStrings.DEFAULT_SUBJECT) : subject));
                }
                {
                    final Date date = (Date) storeProps.get("date");
                    final String repl = date == null ? "" : com.openexchange.java.Strings.quoteReplacement(MimeProcessingUtility.getFormattedDate(date, DateFormat.LONG, locale, TimeZone.getDefault()));
                    desc = desc.replaceFirst("#DATE#", repl);
                }
                {
                    final InternetAddress[] to = (InternetAddress[]) storeProps.get("to");
                    desc = desc.replaceFirst("#TO#", com.openexchange.java.Strings.quoteReplacement(to == null || to.length == 0 ? "" : com.openexchange.java.Strings.quoteReplacement(MimeProcessingUtility.addrs2String(to))));
                }
                file.setDescription(desc);
                modifiedColumns.add(Field.DESCRIPTION);
            }
        }
        /*
         * Put attachment's document to dedicated infostore folder
         */
        final IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);
        boolean retry = true;
        int count = 1;
        final StringBuilder hlp = new StringBuilder(16);
        while (retry) {
            /*
             * Get attachment's input stream
             */
            final InputStream in = attachment.getInputStream();
            boolean rollbackNeeded = false;
            try {
                /*
                 * save attachment in storage, ignoring potential warnings due to limited storage capabilities
                 */
                fileAccess.startTransaction();
                rollbackNeeded = true;
                try {
                    fileAccess.saveDocument(file, in, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, modifiedColumns, false, true, false);
                    fileAccess.commit();
                    rollbackNeeded = false;
                    retry = false;
                } catch (final OXException x) {
                    fileAccess.rollback();
                    rollbackNeeded = false;
                    if (!x.isPrefix("IFO")) {
                        throw x;
                    }
                    if (441 != x.getCode()) {
                        throw x;
                    }
                    /*
                     * Duplicate document name, thus retry with a new name
                     */
                    hlp.setLength(0);
                    final int pos = name.lastIndexOf('.');
                    final String newName;
                    if (pos >= 0) {
                        newName = hlp.append(name.substring(0, pos)).append("_(").append(++count).append(')').append(name.substring(pos)).toString();
                    } else {
                        newName = hlp.append(name).append("_(").append(++count).append(')').toString();
                    }
                    file.setFileName(newName);
                    file.setTitle(newName);
                } catch (final RuntimeException e) {
                    throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
                } finally {
                    if (rollbackNeeded) {
                        fileAccess.rollback();
                    }
                    finishSafe(fileAccess);
                }
            } finally {
                Streams.close(in);
            }
        }
        return file.getId();
    }

    @Override
    public MailPart getAttachment(String id, Session session) throws OXException {
        IDBasedFileAccess fileAccess = null;
        try {
            IDBasedFileAccessFactory fileAccessFactory = ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class, true);

            fileAccess = fileAccessFactory.createAccess(session);
            File fileMetadata = fileAccess.getFileMetadata(id, FileStorageFileAccess.CURRENT_VERSION);

            String fileName = fileMetadata.getFileName();
            String fileMIMEType = fileMetadata.getFileMIMEType();
            if (Strings.isEmpty(fileMIMEType) || MimeTypes.MIME_APPL_OCTET.equalsIgnoreCase(fileMIMEType)) {
                fileMIMEType = MimeType2ExtMap.getContentType(fileName, MimeTypes.MIME_APPL_OCTET);
            }

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setDataHandler(new DataHandler(new DocumentDataSource(id, fileMIMEType, fileName, session)));
            mimeBodyPart.setFileName(fileName);
            mimeBodyPart.setHeader("Content-Type", fileMIMEType);

            MailPart mailPart = MimeMessageConverter.convertPart(mimeBodyPart, false);
            mailPart.setSize(fileMetadata.getFileSize());
            return mailPart;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            finishSafe(fileAccess);
        }
    }

    @Override
    public MailAttachmentInfo getAttachmentInfo(String id, Session session) throws OXException {
        IDBasedFileAccess fileAccess = null;
        try {
            IDBasedFileAccessFactory fileAccessFactory = ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class, true);

            fileAccess = fileAccessFactory.createAccess(session);
            File fileMetadata = fileAccess.getFileMetadata(id, FileStorageFileAccess.CURRENT_VERSION);

            String fileName = fileMetadata.getFileName();
            String fileMIMEType = fileMetadata.getFileMIMEType();
            if (Strings.isEmpty(fileMIMEType) || MimeTypes.MIME_APPL_OCTET.equalsIgnoreCase(fileMIMEType)) {
                fileMIMEType = MimeType2ExtMap.getContentType(fileName, MimeTypes.MIME_APPL_OCTET);
            }

            return new MailAttachmentInfo(id, fileMIMEType, fileName, fileMetadata.getFileSize());
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            finishSafe(fileAccess);
        }
    }

    @Override
    public InputStream getAttachmentStream(String id, Session session) throws OXException {
        IDBasedFileAccess fileAccess = null;
        try {
            IDBasedFileAccessFactory fileAccessFactory = ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class, true);
            fileAccess = fileAccessFactory.createAccess(session);
            return fileAccess.getDocument(id, FileStorageFileAccess.CURRENT_VERSION);
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            finishSafe(fileAccess);
        }
    }

    @Override
    public void removeAttachment(String id, Session session) throws OXException {
        IDBasedFileAccessFactory fileAccessFactory = ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class, true);
        IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);
        long timestamp = System.currentTimeMillis();

        // Delete file
        fileAccess.startTransaction();
        try {
            fileAccess.removeDocument(Collections.singletonList(id), timestamp);
            fileAccess.commit();
        } catch (final OXException x) {
            fileAccess.rollback();
            throw x;
        } finally {
            finishSafe(fileAccess);
        }
    }

    @Override
    public DownloadUri getDownloadUri(String id, Session session) throws OXException {
        PublicationRefs publicationRefs = getPublicationRefs();

        // Generate publication for current attachment
        final Publication publication = new Publication();
        publication.setModule("infostore/object");
        publication.setEntityId(new FileID(id).getFileId());
        publication.setContext(getContext(session));
        publication.setUserId(session.getUserId());

        // Set target
        publication.setTarget(publicationRefs.target);

        // ... and publish
        publicationRefs.publisher.create(publication);

        // Get associated URL
        String url = (String) publication.getConfiguration().get("url");

        // Return
        return new DownloadUri(url, Integer.toString(publication.getId()));
    }

    @Override
    public void discard(String id, DownloadUri downloadUri, Session session) throws OXException {
        IDBasedFileAccessFactory fileAccessFactory = ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class, true);
        PublicationRefs publicationRefs = getPublicationRefs();
        long timestamp = System.currentTimeMillis();

        // Delete publication
        try {
            publicationRefs.publisher.delete(getContext(session), Strings.parsePositiveInt(downloadUri.getDownloadInfo()));
        } catch (final OXException e) {
            LOG.error("Publication with ID \"{} could not be roll-backed.", downloadUri.getDownloadInfo(), e);
        }

        // Delete file
        try {
            IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);
            fileAccess.startTransaction();
            try {
                fileAccess.removeDocument(Collections.singletonList(id), timestamp);
                fileAccess.commit();
            } catch (final OXException x) {
                fileAccess.rollback();
                throw x;
            } finally {
                finishSafe(fileAccess);
            }
        } catch (final OXException e) {
            LOG.error("Transaction error while deleting file with ID \"{}\" failed.", id, e);
        }
    }

    private static final List<Field> FIELDS = Collections.unmodifiableList(new ArrayList<Field>(Arrays.asList(Field.ID, Field.CREATED, Field.CREATED_BY)));

    private List<String> getElapsedDocuments(int folderId, IDBasedFileAccess fileAccess, ServerSession serverSession, long now, long timeToLive) throws OXException {
        final SearchIterator<File> searchIterator = fileAccess.getDocuments(String.valueOf(folderId), FIELDS).results();
        try {
            final List<String> ret;
            final int userId = serverSession.getUserId();
            if (searchIterator.size() != -1) {
                final int size = searchIterator.size();
                ret = new ArrayList<String>(size);
                for (int i = 0; i < size; i++) {
                    final File file = searchIterator.next();
                    if (isOwner(userId, file.getCreatedBy()) && isElapsed(now, file.getCreated().getTime(), timeToLive)) {
                        ret.add(file.getId());
                    }
                }
            } else {
                ret = new LinkedList<String>();
                while (searchIterator.hasNext()) {
                    final File file = searchIterator.next();
                    if (isOwner(userId, file.getCreatedBy()) && isElapsed(now, file.getCreated().getTime(), timeToLive)) {
                        ret.add(file.getId());
                    }
                }
            }
            return ret;
        } finally {
            SearchIterators.close(searchIterator);
        }
    }

    private static boolean isOwner(final int sessionUser, final int createdBy) {
        return (sessionUser == createdBy);
    }

    private static boolean isElapsed(final long now, final long creationDate, final long ttl) {
        return ((now - creationDate) > ttl);
    }

    private int createIfAbsent(final Session session, final Context ctx, final String name, final FolderObject defaultInfoStoreFolder) throws SQLException, OXException {
        final int lookUpFolder = OXFolderSQL.lookUpFolder(defaultInfoStoreFolder.getObjectID(), name, FolderObject.INFOSTORE, null, ctx);
        if (-1 == lookUpFolder) {
            /*
             * Create folder
             */
            final FolderObject fo = createNewInfostoreFolder(session.getUserId(), name, defaultInfoStoreFolder.getObjectID());
            return OXFolderManager.getInstance(session).createFolder(fo, true, System.currentTimeMillis()).getObjectID();
        }
        return lookUpFolder;
    }

    private FolderObject createNewInfostoreFolder(final int adminId, final String name, final int parent) {
        final FolderObject newFolder = new FolderObject();
        newFolder.setFolderName(name);
        newFolder.setParentFolderID(parent);
        newFolder.setType(FolderObject.PUBLIC);
        newFolder.setModule(FolderObject.INFOSTORE);

        // Admin permission
        {
            final OCLPermission perm = new OCLPermission();
            perm.setEntity(adminId);
            perm.setFolderAdmin(true);
            perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
            perm.setReadObjectPermission(OCLPermission.ADMIN_PERMISSION);
            perm.setWriteObjectPermission(OCLPermission.ADMIN_PERMISSION);
            perm.setDeleteObjectPermission(OCLPermission.ADMIN_PERMISSION);
            perm.setGroupPermission(false);
            newFolder.setPermissions(Collections.singletonList(perm));
        }

        return newFolder;
    }

    private static void finishSafe(IDBasedFileAccess fileAccess) {
        if (fileAccess != null) {
            try {
                fileAccess.finish();
            } catch (final Exception e) {
                // IGNORE
            }
        }
    }

}
