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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mail.json.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.internet.InternetAddress;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStorageGuestPermission;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.folderstorage.filestorage.contentType.FileStorageContentType;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.notify.hostname.internal.HostDataImpl;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareLink;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tx.TransactionAware;

/**
 * {@link ShareLinkAttachmentHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class ShareLinkAttachmentHandler extends DefaultAttachmentHandler implements TransactionAware {

    private final IDBasedFolderAccess folderAccess;
    private final IDBasedFileAccess fileAccess;

    /**
     * Initializes a new {@link ShareLinkAttachmentHandler}.
     *
     * @param session The session providing needed user information
     * @param transportProvider The transport provider
     * @param protocol The server's protocol
     * @param hostName The server's host name
     * @throws OXException If initialization fails
     */
    public ShareLinkAttachmentHandler(Session session, TransportProvider transportProvider, String protocol, String hostName) throws OXException {
        super(session, transportProvider, protocol, hostName);
        fileAccess = ServerServiceRegistry.getServize(IDBasedFileAccessFactory.class).createAccess(session);
        folderAccess = ServerServiceRegistry.getServize(IDBasedFolderAccessFactory.class).createAccess(session);
    }

    @Override
    public void startTransaction() throws OXException {
        fileAccess.startTransaction();
        folderAccess.startTransaction();
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
    protected List<LinkedAttachment> publishAttachments(ComposedMailMessage source, List<OXException> warnings) throws OXException {
        /*
         * save attachments in new folder
         */
        String folderID = createFolder(source, getPassword(), getExpiratioDate());
        List<String> savedAttachments = saveAttachments(folderID, attachments);
        ShareTarget folderTarget = new ShareTarget(FileStorageContentType.getInstance().getModule(), folderID);
        ShareLink folderLink = ServerServiceRegistry.getServize(ShareService.class).getLink(session, folderTarget);
        if (useDownloadLinks()) {
            /*
             * provide individual links to each file
             */
            List<LinkedAttachment> linkedAttachments = new ArrayList<LinkedAttachment>(attachments.size());
            for (int i = 0; i < attachments.size(); i++) {
                ShareTarget fileTarget = new ShareTarget(folderTarget.getModule(), folderTarget.getFolder(), savedAttachments.get(i));
                linkedAttachments.add(new LinkedShareAttachment(attachments.get(i).getFileName(), folderLink.getGuest(), getHostData(), fileTarget, "dl=1"));
            }
            return linkedAttachments;
        } else {
            /*
             * provide a single link to the shared folder
             */
            LinkedShareAttachment linkedAttachment = new LinkedShareAttachment(null, folderLink.getGuest(), getHostData(), folderTarget, null);
            return Collections.<LinkedAttachment>singletonList(linkedAttachment);
        }
    }

    private String createFolder(ComposedMailMessage mail, String password, Date expiry) throws OXException {
        /*
         * get or create base email attachments folder as needed
         */
        String parentFolderID;
        String paramterName = MailSessionParameterNames.getParamPublishingInfostoreFolderID();
        Object parameter = session.getParameter(paramterName);
        if (null != parameter) {
            parentFolderID = String.valueOf(parameter);
        } else {
            parentFolderID = discoverEMailAttachmentsFolderID();
            session.setParameter(paramterName, parentFolderID);
        }
        /*
         * create folder, pre-shared to an anonymous recipient, for this message
         */
        DefaultFileStorageFolder folder = prepareFolder(mail, parentFolderID, password, expiry);
        int counter = 1;
        do {
            try {
                return folderAccess.createFolder(folder);
            } catch (OXException e) {
                if ("FLD-1014".equals(e.getErrorCode()) || "FLD-0012".equals(e.getErrorCode())) {
                    // A duplicate folder exists
                    folder.setName(FileStorageUtility.enhance(folder.getName(), counter++));
                    continue;
                }
                throw e;
            }
        } while (true);
    }

    private String discoverEMailAttachmentsFolderID() throws OXException {
        String name = TransportProperties.getInstance().getPublishingInfostoreFolder();
        if ("i18n-defined".equals(name)) {
            name = FolderStrings.DEFAULT_EMAIL_ATTACHMENTS_FOLDER_NAME;
        }
        FileStorageAccount defaultAccount = getDefaultAccount();
        FolderID placeholderID = new FolderID(defaultAccount.getFileStorageService().getId(), defaultAccount.getId(), "0");
        FileStorageFolder personalFolder = folderAccess.getPersonalFolder(placeholderID.toString());
        // TODO: FileStorageFolder personalFolder = folderAccess.getPersonalFolder(String serviceID, String accountID);
        /*
         * lookup an existing folder
         */
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
        /*
         * create folder if it not yet exists
         */
        DefaultFileStorageFolder folder = new DefaultFileStorageFolder();
        folder.setName(name);
        DefaultFileStoragePermission permission = DefaultFileStoragePermission.newInstance();
        permission.setEntity(session.getUserId());
        folder.setPermissions(Collections.<FileStoragePermission>singletonList(permission));
        folder.setParentId(personalFolder.getId());
        return folderAccess.createFolder(folder);
    }

    private List<String> saveAttachments(String folderID, List<MailPart> attachments) throws OXException {
        IDBasedFileAccess fileAccess = ServerServiceRegistry.getServize(IDBasedFileAccessFactory.class).createAccess(session);
        List<String> createdFiles = new ArrayList<String>(attachments.size());
        try {
            fileAccess.startTransaction();
            for (MailPart attachment : attachments) {
                createdFiles.add(saveAttachment(fileAccess, folderID, attachment));
            }
            fileAccess.commit();
        } finally {
            fileAccess.finish();
        }
        return createdFiles;
    }

    private String saveAttachment(IDBasedFileAccess fileAccess, String folderID, MailPart attachment) throws OXException {
        File file = prepareMetadata(attachment, folderID);
        InputStream inputStream = null;
        try {
            inputStream = attachment.getInputStream();
            return fileAccess.saveDocument(file, inputStream, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);
        } finally {
            Streams.close(inputStream);
        }
    }

    private DefaultFileStorageFolder prepareFolder(ComposedMailMessage mail, String parent, String password, Date expiry) {
        DefaultFileStorageFolder folder = new DefaultFileStorageFolder();
        folder.setParentId(parent);
        folder.setName(mail.getSubject());
        List<FileStoragePermission> permissions = new ArrayList<FileStoragePermission>(2);
        DefaultFileStoragePermission userPermission = DefaultFileStoragePermission.newInstance();
        userPermission.setMaxPermissions();
        userPermission.setEntity(session.getUserId());
        permissions.add(userPermission);
        DefaultFileStorageGuestPermission guestPermission = new DefaultFileStorageGuestPermission(prepareRecipient(password, expiry));
        guestPermission.setAllPermissions(FileStoragePermission.READ_FOLDER, FileStoragePermission.READ_ALL_OBJECTS, FileStoragePermission.NO_PERMISSIONS, FileStoragePermission.NO_PERMISSIONS);
        permissions.add(guestPermission);
        folder.setPermissions(permissions);
        return folder;
    }

    private File prepareMetadata(MailPart attachment, String folderID) {
        String name = attachment.getFileName();
        if (Strings.isEmpty(name)) {
            name = "attachment";
        }
        final File file = new DefaultFile();
        file.setId(FileStorageFileAccess.NEW);
        file.setFolderId(folderID);
        file.setFileName(name);
        file.setFileMIMEType(attachment.getContentType().getBaseType());
        file.setTitle(name);
        file.setFileSize(attachment.getSize());
        return file;
    }

    private static ShareRecipient prepareRecipient(String password, Date expiryDate) {
        int bits = Permissions.createPermissionBits(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, false);
        return new AnonymousRecipient(bits, password, expiryDate);
    }

    private FileStorageAccount getDefaultAccount() throws OXException {
        // TODO
        FileStorageAccountManagerLookupService lookupService = ServerServiceRegistry.getInstance().getService(FileStorageAccountManagerLookupService.class);
        if (null == lookupService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(FileStorageAccountManagerLookupService.class.getName());
        }
        FileStorageAccountManager accountManager = lookupService.getAccountManagerFor("com.openexchange.infostore");
        return accountManager.getAccount("infostore", session);
    }

    private HostData getHostData() {
        String prefix = ServerServiceRegistry.getServize(DispatcherPrefixService.class).getPrefix();
        String route = com.openexchange.tools.servlet.http.Tools.extractRoute(null);
        return new HostDataImpl("https".equalsIgnoreCase(protocol), hostName, -1, null, route, prefix);
    }

    private static boolean useDownloadLinks() {
        return true;
    }

    private static boolean isEncodeRecipients() {
        return true;
    }

    private static final class LinkedShareAttachment implements LinkedAttachment {

        private final GuestInfo guest;
        private final ShareTarget sourceTarget;
        private final HostData hostData;
        private final String name;
        private final String queryString;

        public LinkedShareAttachment(String name, GuestInfo guest, HostData hostData, ShareTarget sourceTarget, String queryString) {
            super();
            this.name = name;
            this.guest = guest;
            this.hostData = hostData;
            this.sourceTarget = sourceTarget;
            this.queryString = queryString;
        }

        @Override
        public String getName() {
            return name;
        }
        @Override
        public String getLink(InternetAddress recipient) {
            if (isEncodeRecipients()) {

            } else {

            }
            Map<String, String> additionals = new HashMap<String, String>(1);
            additionals.put("recipient", recipient.getAddress());
            ShareTargetPath targetPath = new ShareTargetPath(sourceTarget.getModule(), sourceTarget.getFolder(), sourceTarget.getItem(), additionals);
            String url = guest.generateLink(hostData, targetPath);
            if (Strings.isNotEmpty(queryString)) {
                url = url + '?' + queryString;
            }
            return url;
        }

    }

}
