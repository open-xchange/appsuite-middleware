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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.json.compose.share;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStorageGuestPermission;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.FileStorageUtility;
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
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.json.compose.ComposeContext;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.Utilities;
import com.openexchange.mail.json.compose.share.spi.AttachmentStorage;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tx.TransactionAware;
import com.openexchange.tx.TransactionAwares;


/**
 * {@link DefaultAttachmentStorage} - The default attachment storage using Drive module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class DefaultAttachmentStorage implements AttachmentStorage {

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
     * @param configService The config service
     * @param timerService The timer service
     * @return The initialized instance
     * @throws OXException If instance cannot be returned
     */
    public static synchronized void startInstance(ConfigurationService configService, TimerService timerService) throws OXException {
        DefaultAttachmentStorage tmp = instance;
        if (null == tmp) {
            tmp = new DefaultAttachmentStorage("default");

            long cleanerInterval = Utilities.parseTimespanProperty("com.openexchange.mail.compose.share.periodicCleanerInterval", DAYS.toMillis(1), HOURS.toMillis(1), true, configService);
            if (0 < cleanerInterval) {
                DefaultAttachmentStoragePeriodicCleaner cleaner = new DefaultAttachmentStoragePeriodicCleaner(tmp.id);
                long shiftMillis = TimeUnit.MILLISECONDS.convert((long)(Math.random() * 100), TimeUnit.MINUTES);
                ScheduledTimerTask timerTask = timerService.scheduleWithFixedDelay(cleaner, cleanerInterval + shiftMillis, cleanerInterval);
                tmp.setCleanerInfo(cleaner, timerTask);
            }

            instance = tmp;
        }
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
    private volatile ScheduledTimerTask timerTask;
    private volatile DefaultAttachmentStoragePeriodicCleaner cleaner;

    /**
     * Initializes a new {@link DefaultAttachmentStorage}.
     */
    protected DefaultAttachmentStorage(String id) {
        super();
        this.id = id;
    }

    private void setCleanerInfo(DefaultAttachmentStoragePeriodicCleaner cleaner, ScheduledTimerTask timerTask) {
        this.cleaner = cleaner;
        this.timerTask = timerTask;
    }

    private void halt() {
        DefaultAttachmentStoragePeriodicCleaner cleaner = this.cleaner;
        if (null != cleaner) {
            this.cleaner = null;
            cleaner.stop();
        }

        ScheduledTimerTask timerTask = this.timerTask;
        if (null != timerTask) {
            this.timerTask = null;
            timerTask.cancel(true);
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
    public boolean applicableFor(ComposeRequest composeRequest) throws OXException {
        return true;
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
            Item folder = createFolder(sourceMessage, password, expiry, autoDelete, locale, storageContext);

            // Save attachments into that folder
            List<Item> files = saveAttachments(context.getAllParts(), folder, autoDelete ? expiry : null, locale, storageContext);

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
     * Saves specified attachments into denoted folder.
     *
     * @param attachments The attachments to save
     * @param folder The folder to save to
     * @param expiry The optional expiration date or <code>null</code>
     * @param locale The locale of session-associated user
     * @param storageContext The associated storage context
     * @return The identifiers of the saved attachments
     * @throws OXException If save attempt fails
     */
    protected List<Item> saveAttachments(List<MailPart> attachments, Item folder, Date expiry, Locale locale, DefaultAttachmentStorageContext storageContext) throws OXException {
        List<Item> createdFiles = new ArrayList<Item>(attachments.size());
        for (MailPart attachment : attachments) {
            createdFiles.add(saveAttachment(attachment, folder, expiry, locale, storageContext));
        }
        return createdFiles;
    }

    /**
     * Saves a single attachment into specified folder.
     *
     * @param attachment The attachment to save
     * @param folder The folder
     * @param expiry The optional expiration date or <code>null</code>
     * @param locale The locale of session-associated user
     * @param storageContext The associated storage context
     * @return The identifier of the saved attachment
     * @throws OXException If save attempt fails
     */
    protected Item saveAttachment(MailPart attachment, Item folder, Date expiry, Locale locale, DefaultAttachmentStorageContext storageContext) throws OXException {
        File file = prepareMetadata(attachment, folder, expiry, locale);
        InputStream inputStream = null;
        try {
            inputStream = attachment.getInputStream();
            return new Item(storageContext.fileAccess.saveDocument(file, inputStream, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER), file.getFileName());
        } finally {
            Streams.close(inputStream);
        }
    }

    /**
     * Creates an appropriate <code>File</code> instance for given attachment.
     *
     * @param attachment The attachment
     * @param folder The folder
     * @param expiry The optional expiration date or <code>null</code>
     * @param locale The locale of session-associated user
     * @return The resulting <code>File</code> instance
     */
    protected File prepareMetadata(MailPart attachment, Item folder, Date expiry, Locale locale) {
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
        file.setFolderId(folder.getId());
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
            toSanitize = toSanitize.trim();

            boolean sanitize = true;
            while (sanitize) {
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
                    if (!validity.isValid()) {
                        sanitize = true;
                        toSanitize = defaultName;
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
     * @param source The source message
     * @param password The optional password or <code>null</code>
     * @param expiry The optional expiration date or <code>null</code>
     * @param autoDelete <code>true</code> to have the files being cleansed provided that <code>expiry</code> is given; otherwise <code>false</code> to leave them
     * @param locale The locale of session-associated user
     * @param storageContext The associated storage context
     * @return The identifier of the newly created folder
     * @throws OXException If folder cannot be created
     */
    protected Item createFolder(ComposedMailMessage source, String password, Date expiry, boolean autoDelete, Locale locale, DefaultAttachmentStorageContext storageContext) throws OXException {
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
        DefaultFileStorageFolder folder = prepareFolder(source, parentFolderID, password, expiry, autoDelete, session, locale);
        IDBasedFolderAccess folderAccess = storageContext.folderAccess;
        int counter = 1;
        do {
            try {
                return new Item(folderAccess.createFolder(folder), folder.getName());
            } catch (OXException e) {
                if (e.equalsCode(1014, "FLD") || e.equalsCode(12, "FLD")) {
                    // A duplicate folder exists
                    folder.setName(FileStorageUtility.enhance(folder.getName(), counter++));
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
     * @param source The source message
     * @param parentId The identifier of the parent folder
     * @param password The optional password for the share
     * @param expiry The optional expiration date for the share
     * @param autoDelete <code>true</code> to have the files being cleansed provided that <code>expiry</code> is given; otherwise <code>false</code> to leave them
     * @param session The associated session
     * @param locale The locale to use
     * @return A new folder instance (not yet created)
     */
    protected DefaultFileStorageFolder prepareFolder(ComposedMailMessage source, String parentId, String password, Date expiry, boolean autoDelete, Session session, Locale locale) {
        DefaultFileStorageFolder folder = new DefaultFileStorageFolder();
        folder.setParentId(parentId);
        folder.setName(sanitizeName(source.getSubject(), StringHelper.valueOf(locale).getString(ShareComposeStrings.DEFAULT_NAME_FOLDER)));
        List<FileStoragePermission> permissions = new ArrayList<FileStoragePermission>(2);
        DefaultFileStoragePermission userPermission = DefaultFileStoragePermission.newInstance();
        userPermission.setMaxPermissions();
        userPermission.setEntity(session.getUserId());
        permissions.add(userPermission);
        DefaultFileStorageGuestPermission guestPermission = new DefaultFileStorageGuestPermission(prepareRecipient(password, expiry));
        guestPermission.setAllPermissions(FileStoragePermission.READ_FOLDER, FileStoragePermission.READ_ALL_OBJECTS, FileStoragePermission.NO_PERMISSIONS, FileStoragePermission.NO_PERMISSIONS);
        permissions.add(guestPermission);
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

}
