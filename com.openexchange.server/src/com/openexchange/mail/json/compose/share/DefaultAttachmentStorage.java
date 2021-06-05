/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.json.compose.share;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.cleanup.CleanUpInfo;
import com.openexchange.database.cleanup.DatabaseCleanUpService;
import com.openexchange.database.cleanup.DefaultCleanUpJob;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCodeSet;
import com.openexchange.exception.OXExceptions;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStorageGuestPermission;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.NameBuilder;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FilenameValidationUtils;
import com.openexchange.file.storage.composition.FilenameValidationUtils.ValidityResult;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.folderstorage.filestorage.contentType.FileStorageContentType;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.json.compose.ComposeContext;
import com.openexchange.mail.json.compose.Utilities;
import com.openexchange.mail.json.compose.share.spi.AttachmentStorage;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tx.TransactionAware;
import com.openexchange.tx.TransactionAwares;
import com.openexchange.user.User;


/**
 * {@link DefaultAttachmentStorage} - The default attachment storage using Drive module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class DefaultAttachmentStorage implements AttachmentStorage {

    private static final OXExceptionCodeSet CODES_DUPLICATE = new OXExceptionCodeSet(OXFolderExceptionCode.NO_DUPLICATE_FOLDER, OXFolderExceptionCode.DUPLICATE_NAME, OXExceptions.codeFor(1014, "FLD"));

    private static volatile DefaultAttachmentStorage instance;

    /**
     * Gets the instance.
     *
     * @return The instance or <code>null</code> if not yet initialized
     */
    public static DefaultAttachmentStorage getInstance() {
        return instance;
    }

    /**
     * Initializes this attachment storage.
     *
     * @return The initialized instance
     */
    public static synchronized void startInstance() {
        DefaultAttachmentStorage tmp = instance;
        if (null == tmp) {
            tmp = new DefaultAttachmentStorage("default");
            instance = tmp;
        }
    }

    /**
     * Initializes the periodic cleaner.
     *
     * @param configService The configuration service
     * @param cleanUpService The clean-up service
     * @throws OXException If cleaner cannot be initialized
     */
    public static synchronized void initiateCleaner(ConfigurationService configService, DatabaseCleanUpService cleanUpService) throws OXException {
        DefaultAttachmentStorage tmp = instance;
        if (null == tmp) {
            throw new IllegalStateException("DefaultAttachmentStorage not yet started");
        }

        long cleanerInterval = Utilities.parseTimespanProperty("com.openexchange.mail.compose.share.periodicCleanerInterval", DAYS.toMillis(1), HOURS.toMillis(1), true, configService);
        if (0 < cleanerInterval) {
            DefaultAttachmentStoragePeriodicCleaner cleaner = new DefaultAttachmentStoragePeriodicCleaner(tmp.id);
            long shiftMillis = TimeUnit.MILLISECONDS.convert((long)(Math.random() * 100), TimeUnit.MINUTES);
            DefaultCleanUpJob job = DefaultCleanUpJob.builder()
                .withId(DefaultAttachmentStoragePeriodicCleaner.class)
                .withRunsExclusive(true)
                .withDelay(Duration.ofMillis(cleanerInterval))
                .withInitialDelay(Duration.ofMillis(shiftMillis))
                .withExecution(cleaner)
                .build();
            CleanUpInfo cleanUpInfo = cleanUpService.scheduleCleanUpJob(job);
            tmp.setCleanerInfo(cleaner, cleanUpInfo);
        }
    }

    /**
     * Drops the periodic cleaner.
     *
     * @throws OXException If cleaner cannot be dropped
     */
    public static synchronized void dropCleaner() {
        DefaultAttachmentStorage tmp = instance;
        if (null == tmp) {
            return;
        }

        tmp.halt();
    }

    /**
     * Shuts down this attachment storage.
     */
    public static synchronized void shutDown() {
        DefaultAttachmentStorage tmp = instance;
        if (null != tmp) {
            instance = null;
            tmp.halt();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private final String id;
    private volatile CleanUpInfo cleanUpInfo;
    private volatile DefaultAttachmentStoragePeriodicCleaner cleaner;

    /**
     * Initializes a new {@link DefaultAttachmentStorage}.
     */
    protected DefaultAttachmentStorage(String id) {
        super();
        this.id = id;
    }

    private void setCleanerInfo(DefaultAttachmentStoragePeriodicCleaner cleaner, CleanUpInfo cleanUpInfo) {
        this.cleaner = cleaner;
        this.cleanUpInfo = cleanUpInfo;
    }

    private void halt() {
        DefaultAttachmentStoragePeriodicCleaner cleaner = this.cleaner;
        if (null != cleaner) {
            this.cleaner = null;
            cleaner.stop();
        }

        CleanUpInfo cleanUpInfo = this.cleanUpInfo;
        if (null != cleanUpInfo) {
            this.cleanUpInfo = null;
            cleanUpInfo.cancel(true);
        }
    }

    /**
     * Gets the identifier.
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    @Override
    public boolean applicableFor(Session session) {
        return true;
    }

    @Override
    public StoredAttachments storeAttachments(List<MailPart> attachments, String subject, ServerSession session) throws OXException {
        DefaultAttachmentStorageContext storageContext = new DefaultAttachmentStorageContext(getFileAccess(session), getFolderAccess(session), session);
        boolean rollback = false;
        try {
            storageContext.startTransaction();
            rollback = true;

            Locale locale = session.getUser().getLocale();

            // Get folder identifier
            Item folder = createFolder(subject, null, null, false, false, locale, storageContext);

            // Save attachments into that folder
            List<Item> files = saveAttachments(attachments, folder.getId(), null, locale, storageContext);

            StoredAttachments storedAttachments = new StoredAttachments(folder, files);
            storageContext.commit();
            rollback = false;
            return storedAttachments;
        } finally {
            if (rollback) {
                rollback(storageContext);
            }
            storageContext.finish();
        }
    }

    @Override
    public StoredAttachments storeAttachments(FileItems attachments, String subject, ServerSession session) throws OXException {
        DefaultAttachmentStorageContext storageContext = new DefaultAttachmentStorageContext(getFileAccess(session), getFolderAccess(session), session);
        boolean rollback = false;
        try {
            storageContext.startTransaction();
            rollback = true;

            Locale locale = session.getUser().getLocale();

            // Get folder identifier
            Item folder = createFolder(subject, null, null, false, false, locale, storageContext);

            // Save attachments into that folder
            List<Item> files = saveAttachments(attachments, folder.getId(), null, locale, storageContext);

            StoredAttachments storedAttachments = new StoredAttachments(folder, files);
            storageContext.commit();
            rollback = false;
            return storedAttachments;
        } finally {
            if (rollback) {
                rollback(storageContext);
            }
            storageContext.finish();
        }
    }

    @Override
    public Item appendAttachment(MailPart attachment, String folderId, ServerSession session) throws OXException {
        DefaultAttachmentStorageContext storageContext = new DefaultAttachmentStorageContext(getFileAccess(session), getFolderAccess(session), session);
        boolean rollback = false;
        try {
            storageContext.startTransaction();
            rollback = true;

            Locale locale = session.getUser().getLocale();

            // Save attachments into folder
            Item file = saveAttachment(attachment, folderId, null, locale, storageContext);

            storageContext.commit();
            rollback = false;
            return file;
        } finally {
            if (rollback) {
                rollback(storageContext);
            }
            storageContext.finish();
        }
    }

    @Override
    public Item appendAttachment(FileItem attachment, String folderId, ServerSession session) throws OXException {
        DefaultAttachmentStorageContext storageContext = new DefaultAttachmentStorageContext(getFileAccess(session), getFolderAccess(session), session);
        boolean rollback = false;
        try {
            storageContext.startTransaction();
            rollback = true;

            Locale locale = session.getUser().getLocale();

            // Save attachments into folder
            Item file = saveAttachment(attachment, folderId, null, locale, storageContext);

            storageContext.commit();
            rollback = false;
            return file;
        } finally {
            if (rollback) {
                rollback(storageContext);
            }
            storageContext.finish();
        }
    }

    @Override
    public List<Item> getAttachments(String folderId, ServerSession session) throws OXException {
        IDBasedFileAccess fileAccess = getFileAccess(session);

        SearchIterator<File> results = null;
        try {
            TimedResult<File> documents = fileAccess.getDocuments(folderId, Arrays.asList(File.Field.ID, File.Field.FILENAME));
            results = documents.results();

            if (false == results.hasNext()) {
                return Collections.emptyList();
            }

            List<Item> items = new ArrayList<>();
            do {
                File file = results.next();
                items.add(new Item(file.getId(), file.getFileName()));
            } while (results.hasNext());
            return items;
        } finally {
            SearchIterators.close(results);
        }
    }

    @Override
    public FileItem getAttachment(String attachmentId, String folderId, ServerSession session) throws OXException {
        IDBasedFileAccess fileAccess = getFileAccess(session);
        File metadata = fileAccess.getFileMetadata(attachmentId, FileStorageFileAccess.CURRENT_VERSION);
        FileItem.DataProvider dataProvider = new FileAccessDataProvider(attachmentId, fileAccess);
        return new FileItem(attachmentId, metadata.getFileName(), metadata.getFileSize(), metadata.getFileMIMEType(), dataProvider);
    }

    @Override
    public void deleteAttachment(String attachmentId, String folderId, ServerSession session) throws OXException {
        DefaultAttachmentStorageContext storageContext = new DefaultAttachmentStorageContext(getFileAccess(session), getFolderAccess(session), session);
        boolean rollback = false;
        try {
            storageContext.startTransaction();
            rollback = true;

            // Delete attachments from folder
            deleteAttachment(attachmentId, storageContext);

            storageContext.commit();
            rollback = false;
        } finally {
            if (rollback) {
                rollback(storageContext);
            }
            storageContext.finish();
        }
    }

    @Override
    public boolean existsFolder(String folderId, ServerSession session) throws OXException {
        IDBasedFolderAccess folderAccess = getFolderAccess(session);
        return folderAccess.exists(folderId);
    }

    @Override
    public void deleteFolder(String folderId, ServerSession session) throws OXException {
        DefaultAttachmentStorageContext storageContext = new DefaultAttachmentStorageContext(getFileAccess(session), getFolderAccess(session), session);
        boolean rollback = false;
        try {
            storageContext.startTransaction();
            rollback = true;

            // Delete attachments from folder
            deleteFolder(folderId, storageContext);

            storageContext.commit();
            rollback = false;
        } finally {
            if (rollback) {
                rollback(storageContext);
            }
            storageContext.finish();
        }
    }

    @Override
    public void renameFolder(String subject, String folderId, ServerSession session) throws OXException {
        DefaultAttachmentStorageContext storageContext = new DefaultAttachmentStorageContext(getFileAccess(session), getFolderAccess(session), session);
        boolean rollback = false;
        try {
            storageContext.startTransaction();
            rollback = true;

            Locale locale = session.getUser().getLocale();

            // Rename folder
            renameFolder(subject, folderId, locale, storageContext);

            storageContext.commit();
            rollback = false;
        } finally {
            if (rollback) {
                rollback(storageContext);
            }
            storageContext.finish();
        }
    }

    @Override
    public StoredAttachmentsControl createShareTarget(ComposedMailMessage sourceMessage, String folderId, Item folderItem, List<Item> attachmentItems, String password, Date expiry, boolean autoDelete, ServerSession session, ComposeContext context) throws OXException {
        DefaultAttachmentStorageContext storageContext = new DefaultAttachmentStorageContext(getFileAccess(session), getFolderAccess(session), session);
        boolean rollback = false;
        try {
            storageContext.startTransaction();
            rollback = true;

            // Update meta
            applyMetaData(folderId, password, expiry, autoDelete, session.getUserId(), storageContext);

            // Create share target for that folder for an anonymous user
            ShareTarget folderTarget = new ShareTarget(FileStorageContentType.getInstance().getModule(), folderId);

            storageContext.commit();
            rollback = false;
            return new NoopStoredAttachmentsControl(attachmentItems, folderItem, folderTarget);
        } finally {
            if (rollback) {
                rollback(storageContext);
            }
            storageContext.finish();
        }
    }

    @Override
    public StoredAttachmentsControl storeAttachments(ComposedMailMessage sourceMessage, String password, Date expiry, boolean autoDelete, ComposeContext context) throws OXException {
        ServerSession session = context.getSession();

        // Generate transaction instance providing folder and file access
        DefaultAttachmentStorageContext storageContext = new DefaultAttachmentStorageContext(getFileAccess(session), getFolderAccess(session), session);
        boolean rollback = false;
        try {
            storageContext.startTransaction();
            rollback = true;

            Locale locale = session.getUser().getLocale();

            // Get folder identifier
            Item folder = createFolder(sourceMessage.getSubject(), password, expiry, autoDelete, true, locale, storageContext);

            // Save attachments into that folder
            List<Item> files = saveAttachments(context.getAllParts(), folder.getId(), autoDelete ? expiry : null, locale, storageContext);

            // Create share target for that folder for an anonymous user
            ShareTarget folderTarget = new ShareTarget(FileStorageContentType.getInstance().getModule(), folder.getId());

            DefaultStoredAttachmentsControl attachmentsControl = new DefaultStoredAttachmentsControl(files, folder, folderTarget, storageContext);
            rollback = false;
            return attachmentsControl;
        } finally {
            if (rollback) {
                rollback(storageContext);
            }
        }
    }

    @Override
    public StorageQuota getStorageQuota(ServerSession session) throws OXException {
        IDBasedFolderAccess folderAccess = getFolderAccess(session);
        DefaultAttachmentStorageContext storageContext = new DefaultAttachmentStorageContext(getFileAccess(session), folderAccess, session);
        String attachmentsFolderID = discoverEMailAttachmentsFolderID(storageContext);
        Quota quota = folderAccess.getStorageQuota(attachmentsFolderID);
        if (quota.getLimit() < 0) {
            return StorageQuota.UNLIMITED;
        }

        return new StorageQuota(quota.getUsage(), quota.getLimit());
    }

    /**
     * Gets the folder access
     *
     * @param session The session
     * @return The folder access
     * @throws OXException If folder access cannot be returned
     */
    protected IDBasedFolderAccess getFolderAccess(Session session) throws OXException {
        IDBasedFolderAccessFactory factory = ServerServiceRegistry.getServize(IDBasedFolderAccessFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(IDBasedFolderAccessFactory.class);
        }
        return factory.createAccess(session);
    }

    /**
     * Gets the file access
     *
     * @param session The session
     * @return The file access
     * @throws OXException If file access cannot be returned
     */
    protected IDBasedFileAccess getFileAccess(Session session) throws OXException {
        IDBasedFileAccessFactory factory = ServerServiceRegistry.getServize(IDBasedFileAccessFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(IDBasedFileAccessFactory.class);
        }
        return factory.createAccess(session);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Deletes a single attachment.
     *
     * @param attachmentId The identifier of the attachment to delete
     * @param storageContext The associated storage context
     * @throws OXException If delete attempt fails
     */
    protected void deleteAttachment(String attachmentId, DefaultAttachmentStorageContext storageContext) throws OXException {
        storageContext.fileAccess.removeDocument(Collections.singletonList(attachmentId), FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, true);
    }

    /**
     * Deletes a folder.
     *
     * @param folderId The folder to delete
     * @param storageContext The associated storage context
     * @throws OXException If delete attempt fails
     */
    protected void deleteFolder(String folderId, DefaultAttachmentStorageContext storageContext) throws OXException {
        storageContext.folderAccess.deleteFolder(folderId, true);
    }

    /**
     * Renames a folder.
     *
     * @param folderName The folder name to rename to
     * @param folderId The folder to rename
     * @param locale The locale of session-associated user
     * @param storageContext The associated storage context
     * @throws OXException If delete attempt fails
     */
    protected void renameFolder(String folderName, String folderId, Locale locale, DefaultAttachmentStorageContext storageContext) throws OXException {
        String fn = sanitizeName(folderName, StringHelper.valueOf(locale).getString(ShareComposeStrings.DEFAULT_NAME_FOLDER));
        NameBuilder name = null;
        do {
            try {
                storageContext.folderAccess.renameFolder(folderId, fn);
                return;
            } catch (OXException e) {
                if (CODES_DUPLICATE.contains(e)) {
                    // A duplicate folder exists
                    if (null == name) {
                        name = new NameBuilder(fn);
                    }
                    fn = name.advance().toString();
                    continue;
                }
                throw e;
            }
        } while (true);
    }

    /**
     * Saves specified attachments into denoted folder.
     *
     * @param attachments The attachments to save
     * @param folderId The folder to save to
     * @param expiry The optional expiration date or <code>null</code>
     * @param locale The locale of session-associated user
     * @param storageContext The associated storage context
     * @return The identifiers of the saved attachments
     * @throws OXException If save attempt fails
     */
    protected List<Item> saveAttachments(List<MailPart> attachments, String folderId, Date expiry, Locale locale, DefaultAttachmentStorageContext storageContext) throws OXException {
        List<Item> createdFiles = new ArrayList<Item>(attachments.size());
        for (MailPart attachment : attachments) {
            createdFiles.add(saveAttachment(attachment, folderId, expiry, locale, storageContext));
        }
        return createdFiles;
    }

    /**
     * Saves a single attachment into specified folder.
     *
     * @param attachment The attachment to save
     * @param folderId The folder
     * @param expiry The optional expiration date or <code>null</code>
     * @param locale The locale of session-associated user
     * @param storageContext The associated storage context
     * @return The identifier of the saved attachment
     * @throws OXException If save attempt fails
     */
    protected Item saveAttachment(MailPart attachment, String folderId, Date expiry, Locale locale, DefaultAttachmentStorageContext storageContext) throws OXException {
        File file = prepareMetadata(attachment, folderId, expiry, locale);
        InputStream inputStream = null;
        try {
            inputStream = attachment.getInputStream();
            return new Item(storageContext.fileAccess.saveDocument(file, inputStream, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER), file.getFileName());
        } finally {
            Streams.close(inputStream);
        }
    }

    /**
     * Saves specified attachments into denoted folder.
     *
     * @param attachments The attachments to save
     * @param folderId The folder to save to
     * @param expiry The optional expiration date or <code>null</code>
     * @param locale The locale of session-associated user
     * @param storageContext The associated storage context
     * @return The identifiers of the saved attachments
     * @throws OXException If save attempt fails
     */
    protected List<Item> saveAttachments(FileItems attachments, String folderId, Date expiry, Locale locale, DefaultAttachmentStorageContext storageContext) throws OXException {
        List<Item> createdFiles = new ArrayList<Item>(attachments.size());
        for (FileItem attachment : attachments) {
            createdFiles.add(saveAttachment(attachment, folderId, expiry, locale, storageContext));
        }
        return createdFiles;
    }

    /**
     * Saves a single attachment into specified folder.
     *
     * @param attachment The attachment to save
     * @param folderId The folder
     * @param expiry The optional expiration date or <code>null</code>
     * @param locale The locale of session-associated user
     * @param storageContext The associated storage context
     * @return The identifier of the saved attachment
     * @throws OXException If save attempt fails
     */
    protected Item saveAttachment(FileItem attachment, String folderId, Date expiry, Locale locale, DefaultAttachmentStorageContext storageContext) throws OXException {
        File file = prepareMetadata(attachment, folderId, expiry, locale);
        InputStream inputStream = null;
        try {
            inputStream = attachment.getData();
            return new Item(storageContext.fileAccess.saveDocument(file, inputStream, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER), file.getFileName());
        } finally {
            Streams.close(inputStream);
        }
    }

    protected void applyMetaData(String folderId, String password, Date expiry, boolean autoDelete, int userId, DefaultAttachmentStorageContext storageContext) throws OXException {
        SearchIterator<File> iterator = null;
        try {
            if (autoDelete && expiry != null) {
                TimedResult<File> documents = storageContext.fileAccess.getDocuments(folderId);
                iterator = documents.results();
                while (iterator.hasNext()) {
                    File existingFile = iterator.next();
                    // Compile the file to update
                    File file = new DefaultFile();
                    file.setId(existingFile.getId());
                    file.setMeta(mapFor("expiration-date-" + getId(), Long.valueOf(expiry.getTime())));
                    storageContext.fileAccess.saveDocument(file, null, existingFile.getSequenceNumber(), Collections.singletonList(Field.META));
                }
                SearchIterators.close(iterator);
                iterator = null;
            }

            if (password != null || expiry != null) {
                DefaultFileStorageFolder f = new DefaultFileStorageFolder();
                f.setId(folderId);
                List<FileStoragePermission> permissions = new ArrayList<FileStoragePermission>(2);
                DefaultFileStoragePermission userPermission = DefaultFileStoragePermission.newInstance();
                userPermission.setMaxPermissions();
                userPermission.setEntity(userId);
                permissions.add(userPermission);
                DefaultFileStorageGuestPermission guestPermission = new DefaultFileStorageGuestPermission(prepareRecipient(password, expiry));
                guestPermission.setAllPermissions(FileStoragePermission.READ_FOLDER, FileStoragePermission.READ_ALL_OBJECTS, FileStoragePermission.NO_PERMISSIONS, FileStoragePermission.NO_PERMISSIONS);
                permissions.add(guestPermission);
                f.setPermissions(permissions);
                if (autoDelete && null != expiry) {
                    f.setMeta(mapFor("expiration-date-" + getId(), Long.valueOf(expiry.getTime())));
                }
                storageContext.folderAccess.updateFolder(folderId, f);
            }
        } finally {
            SearchIterators.close(iterator);
        }
    }

    /**
     * Creates an appropriate <code>File</code> instance for given attachment.
     *
     * @param attachment The attachment
     * @param folderId The folder
     * @param expiry The optional expiration date or <code>null</code>
     * @param locale The locale of session-associated user
     * @return The resulting <code>File</code> instance
     */
    protected File prepareMetadata(MailPart attachment, String folderId, Date expiry, Locale locale) {
        // Determine & (possibly) decode attachment file name
        String fileName = attachment.getFileName();
        if (Strings.isEmpty(fileName)) {
            fileName = StringHelper.valueOf(locale).getString(ShareComposeStrings.DEFAULT_NAME_FILE);
        } else {
            fileName = MimeMessageUtility.decodeMultiEncodedHeader(fileName);
            fileName = sanitizeName(fileName, StringHelper.valueOf(locale).getString(ShareComposeStrings.DEFAULT_NAME_FILE));
        }

        // Create a file instance for it
        File file = new DefaultFile();
        file.setId(FileStorageFileAccess.NEW);
        file.setFolderId(folderId);
        file.setFileName(fileName);
        file.setFileMIMEType(attachment.getContentType().getBaseType());
        file.setTitle(fileName);
        file.setFileSize(attachment.getSize());
        if (null != expiry) {
            file.setMeta(mapFor("expiration-date-" + getId(), Long.valueOf(expiry.getTime())));
        }
        return file;
    }

    /**
     * Creates an appropriate <code>File</code> instance for given attachment.
     *
     * @param attachment The attachment
     * @param folderId The folder
     * @param expiry The optional expiration date or <code>null</code>
     * @param locale The locale of session-associated user
     * @return The resulting <code>File</code> instance
     */
    protected File prepareMetadata(FileItem attachment, String folderId, Date expiry, Locale locale) {
        // Determine & (possibly) decode attachment file name
        String fileName = attachment.getName();
        if (Strings.isEmpty(fileName)) {
            fileName = StringHelper.valueOf(locale).getString(ShareComposeStrings.DEFAULT_NAME_FILE);
        } else {
            fileName = MimeMessageUtility.decodeMultiEncodedHeader(fileName);
            fileName = sanitizeName(fileName, StringHelper.valueOf(locale).getString(ShareComposeStrings.DEFAULT_NAME_FILE));
        }

        // Create a file instance for it
        File file = new DefaultFile();
        file.setId(FileStorageFileAccess.NEW);
        file.setFolderId(folderId);
        file.setFileName(fileName);
        file.setFileMIMEType(attachment.getMimeType());
        file.setTitle(fileName);
        file.setFileSize(attachment.getSize());
        if (null != expiry) {
            file.setMeta(mapFor("expiration-date-" + getId(), Long.valueOf(expiry.getTime())));
        }
        return file;
    }

    /**
     * Utility method to sanitize file/folder name.
     *
     * @param name The name to sanitize
     * @return The sanitized name
     */
    protected String sanitizeName(String name, String defaultName) {
        String toSanitize = name;
        if (Strings.isEmpty(toSanitize)) {
            toSanitize = defaultName;
        } else {
            boolean sanitize = true;
            while (sanitize) {
                toSanitize = toSanitize.trim();
                sanitize = false;

                String illegalCharacters = FilenameValidationUtils.getIllegalCharacters(toSanitize);
                if (illegalCharacters != null) {
                    sanitize = true;
                    int length = illegalCharacters.length();
                    for (int i = length; i-- > 0;) {
                        toSanitize = toSanitize.replace(illegalCharacters.charAt(i), '_');
                    }
                } else {
                    ValidityResult validity = FilenameValidationUtils.getValidityFor(toSanitize);
                    switch (validity.getViolation()) {
                        case ONLY_DOTS:
                            sanitize = true;
                            toSanitize = defaultName;
                            break;
                        case OTHER_ILLEGAL:
                            sanitize = true;
                            toSanitize = toSanitize.length() > 1 ? toSanitize.substring(0, toSanitize.length() - 1) : defaultName;
                            break;
                        case RESERVED_NAME:
                            sanitize = true;
                            toSanitize = defaultName;
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return toSanitize;
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the context associated with specified session
     *
     * @param session The session
     * @return The context
     * @throws OXException If context cannot be returned
     */
    protected Context getContext(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getContext();
        }
        return ContextStorage.getStorageContext(session.getContextId());
    }

    /**
     * Gets the user associated with specified session
     *
     * @param session The session
     * @return The user
     * @throws OXException If user cannot be returned
     */
    protected User getSessionUser(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser();
        }
        return UserStorage.getInstance().getUser(session.getUserId(), getContext(session));
    }

    /**
     * Gets the locale associated with specified session's user.
     *
     * @param session The session
     * @return The locale of session-associated user
     * @throws OXException If locale cannot be returned
     */
    protected Locale getSessionUserLocale(Session session) throws OXException {
        return getSessionUser(session).getLocale();
    }

    /**
     * Creates the folder that is supposed to contain the attachments' files.
     *
     * @param folderName The name of the folder
     * @param password The optional password or <code>null</code>
     * @param expiry The optional expiration date or <code>null</code>
     * @param autoDelete <code>true</code> to have the files being cleansed provided that <code>expiry</code> is given; otherwise <code>false</code> to leave them
     * @param createGuestPermission <code>true</code> to create guest permission; otherwise <code>false</code>
     * @param locale The locale of session-associated user
     * @param storageContext The associated storage context
     * @return The identifier of the newly created folder
     * @throws OXException If folder cannot be created
     */
    protected Item createFolder(String folderName, String password, Date expiry, boolean autoDelete, boolean createGuestPermission, Locale locale, DefaultAttachmentStorageContext storageContext) throws OXException {
        // Get or create base share attachments folder
        Session session = storageContext.session;
        String parentFolderID;
        {
            String paramterName = MailSessionParameterNames.getParamSharingDriveFolderID();
            Object parameter = session.getParameter(paramterName);
            if (null != parameter) {
                parentFolderID = String.valueOf(parameter);
            } else {
                parentFolderID = discoverEMailAttachmentsFolderID(storageContext);
                session.setParameter(paramterName, parentFolderID);
            }
        }

        // Create folder, pre-shared to an anonymous recipient, for this message
        DefaultFileStorageFolder folder = prepareFolder(folderName, parentFolderID, password, expiry, autoDelete, createGuestPermission, session, locale);
        IDBasedFolderAccess folderAccess = storageContext.folderAccess;
        NameBuilder name = null;
        do {
            try {
                return new Item(folderAccess.createFolder(folder), folder.getName());
            } catch (OXException e) {
                if (CODES_DUPLICATE.contains(e)) {
                    // A duplicate folder exists
                    if (null == name) {
                        name = new NameBuilder(folder.getName());
                    }
                    folder.setName(name.advance().toString());
                    continue;
                }
                throw e;
            }
        } while (true);
    }

    /**
     * Discovers the standard folder for stored attachments that is supposed to hold the sub-folder.
     *
     * @param storageContext The associated storage context
     * @return The identifier of the standard folder
     * @throws OXException If standard folder cannot be discovered
     */
    protected String discoverEMailAttachmentsFolderID(DefaultAttachmentStorageContext storageContext) throws OXException {
        Session session = storageContext.session;
        String name = Utilities.getValueFromProperty("com.openexchange.mail.compose.share.name", "Drive Mail", session);

        IDBasedFolderAccess folderAccess = storageContext.folderAccess;
        FolderID placeholderID = new FolderID(FileID.INFOSTORE_SERVICE_ID, FileID.INFOSTORE_ACCOUNT_ID, "0");
        FileStorageFolder personalFolder = folderAccess.getPersonalFolder(placeholderID.toString());
        // TODO: FileStorageFolder personalFolder = folderAccess.getPersonalFolder(String serviceID, String accountID);

        // Lookup an existing folder
        FileStorageFolder[] subfolders = folderAccess.getSubfolders(personalFolder.getId(), true);
        if (null != subfolders && 0 < subfolders.length) {
            for (FileStorageFolder subfolder : subfolders) {
                if (name.equals(subfolder.getName())) {
                    if (null != subfolder.getOwnPermission() && FileStoragePermission.CREATE_SUB_FOLDERS > subfolder.getOwnPermission().getFolderPermission()) {
                        throw FileStorageExceptionCodes.NO_CREATE_ACCESS.create(subfolder.getName());
                    }
                    return subfolder.getId();
                }
            }
        }

        // Create folder if it not yet existing
        final DefaultFileStorageFolder folder = new DefaultFileStorageFolder();
        folder.setName(name);
        DefaultFileStoragePermission permission = DefaultFileStoragePermission.newInstance();
        permission.setEntity(session.getUserId());
        folder.setPermissions(Collections.<FileStoragePermission>singletonList(permission));
        folder.setParentId(personalFolder.getId());
        return folderAccess.createFolder(folder);
    }

    /**
     * Prepares a new folder holding the anonymous guest permission for the share.
     *
     * @param folderName The name of the folder
     * @param parentId The identifier of the parent folder
     * @param password The optional password for the share
     * @param expiry The optional expiration date for the share
     * @param autoDelete <code>true</code> to have the files being cleansed provided that <code>expiry</code> is given; otherwise <code>false</code> to leave them
     * @param createGuestPermission <code>true</code> to create guest permission; otherwise <code>false</code>
     * @param session The associated session
     * @param locale The locale to use
     * @return A new folder instance (not yet created)
     */
    protected DefaultFileStorageFolder prepareFolder(String folderName, String parentId, String password, Date expiry, boolean autoDelete, boolean createGuestPermission, Session session, Locale locale) {
        DefaultFileStorageFolder folder = new DefaultFileStorageFolder();
        folder.setParentId(parentId);
        folder.setName(sanitizeName(folderName, StringHelper.valueOf(locale).getString(ShareComposeStrings.DEFAULT_NAME_FOLDER)));
        List<FileStoragePermission> permissions = new ArrayList<FileStoragePermission>(2);
        {
            DefaultFileStoragePermission userPermission = DefaultFileStoragePermission.newInstance();
            userPermission.setMaxPermissions();
            userPermission.setEntity(session.getUserId());
            permissions.add(userPermission);
        }
        if (createGuestPermission) {
            DefaultFileStorageGuestPermission guestPermission = new DefaultFileStorageGuestPermission(prepareRecipient(password, expiry));
            guestPermission.setAllPermissions(FileStoragePermission.READ_FOLDER, FileStoragePermission.READ_ALL_OBJECTS, FileStoragePermission.NO_PERMISSIONS, FileStoragePermission.NO_PERMISSIONS);
            permissions.add(guestPermission);
        }
        folder.setPermissions(permissions);
        if (autoDelete && null != expiry) {
            folder.setMeta(mapFor("expiration-date-" + getId(), Long.valueOf(expiry.getTime())));
        }
        setAdditionalInfos(folder);
        return folder;
    }

    /**
     * Sets additional information in specified folder.
     *
     * @param folder The folder to enhance
     */
    protected void setAdditionalInfos(DefaultFileStorageFolder folder) {
        // Nothing by default;
    }

    private static ShareRecipient prepareRecipient(String password, Date expiryDate) {
        return new AnonymousRecipient(Permissions.createReadOnlyPermissionBits(), password, expiryDate);
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    protected static class DefaultAttachmentStorageContext implements TransactionAware {

        final IDBasedFolderAccess folderAccess;
        final IDBasedFileAccess fileAccess;
        final Session session;

        /**
         * Initializes a new {@link DefaultAttachmentStorageContext}.
         */
        protected DefaultAttachmentStorageContext(IDBasedFileAccess fileAccess, IDBasedFolderAccess folderAccess, Session session) {
            super();
            this.fileAccess = fileAccess;
            this.folderAccess = folderAccess;
            this.session = session;
        }

        @Override
        public void startTransaction() throws OXException {
            folderAccess.startTransaction();
            fileAccess.startTransaction();
        }

        @Override
        public void commit() throws OXException {
            fileAccess.commit();
            folderAccess.commit();
        }

        @Override
        public void rollback() throws OXException {
            fileAccess.rollback();
            folderAccess.rollback();
        }

        @Override
        public void finish() throws OXException {
            fileAccess.finish();
            folderAccess.finish();
        }

        @Override
        public void setTransactional(boolean transactional) {
            // Ignore
        }

        @Override
        public void setRequestTransactional(boolean transactional) {
            // Ignore
        }

        @Override
        public void setCommitsTransaction(boolean commits) {
            // Ignore
        }
    }

    private static void rollback(DefaultAttachmentStorageContext storageContext) throws OXException {
        if (null != storageContext) {
            try {
                storageContext.rollback();
            } finally {
                TransactionAwares.finishSafe(storageContext);
            }
        }
    }

    /**
     * Gets a map for specified arguments.
     *
     * @param args The arguments
     * @return The resulting map
     */
    protected static Map<String, Object> mapFor(Object... args) {
        if (null == args) {
            return null;
        }

        int length = args.length;
        if (0 == length || (length % 2) != 0) {
            return null;
        }

        Map<String, Object> map = new LinkedHashMap<String, Object>(length >> 1);
        for (int i = 0; i < length; i+=2) {
            map.put(args[i].toString(), args[i+1]);
        }
        return map;
    }

    /**
     * A data provider using file access.
     */
    protected static class FileAccessDataProvider implements FileItem.DataProvider {

        private final String attachmentId;
        private final IDBasedFileAccess fileAccess;

        /**
         * Initializes a new {@link FileAccessDataProvider}.
         *
         * @param attachment The identifier of the attachment
         * @param fileAccess The file access to use
         */
        FileAccessDataProvider(String attachmentId, IDBasedFileAccess fileAccess) {
            this.attachmentId = attachmentId;
            this.fileAccess = fileAccess;
        }

        @Override
        public InputStream getData() throws OXException {
            return fileAccess.getDocument(attachmentId, FileStorageFileAccess.CURRENT_VERSION);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append('[');
            if (attachmentId != null) {
                builder.append("attachmentId=").append(attachmentId).append(", ");
            }
            if (fileAccess != null) {
                builder.append("fileAccess=").append(fileAccess);
            }
            builder.append(']');
            return builder.toString();
        }
    } // End of class FileAccessDataProvider

}
