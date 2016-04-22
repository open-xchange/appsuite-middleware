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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStorageGuestPermission;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.json.compose.AbstractComposeHandler;
import com.openexchange.mail.json.compose.ComposeDraftResult;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.ComposeTransportResult;
import com.openexchange.mail.json.compose.DefaultComposeDraftResult;
import com.openexchange.mail.json.compose.DefaultComposeTransportResult;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.ShareRecipient;


/**
 * {@link ShareComposeHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareComposeHandler extends AbstractComposeHandler<ShareComposeContext> {

    /**
     * Initializes a new {@link ShareComposeHandler}.
     */
    public ShareComposeHandler() {
        super();
    }

    @Override
    public String getId() {
        return "share";
    }

    @Override
    public boolean serves(Session session) throws OXException {
        return Utilities.getBoolFromProperty("com.openexchange.mail.compose.share.enabled", true, session);
    }

    @Override
    protected ShareComposeContext createComposeContext(ComposeRequest request) throws OXException {
        return new ShareComposeContext(request);
    }

    @Override
    protected ComposeDraftResult doCreateDraftResult(ComposeRequest request, ShareComposeContext context) throws OXException {
        if (false == context.isCreateShares()) {
            ComposedMailMessage composeMessage = createRegularComposeMessage(context);
            return new DefaultComposeDraftResult(composeMessage);
        }

        return null;
    }

    @Override
    protected ComposeTransportResult doCreateTransportResult(ComposeRequest request, ShareComposeContext context) throws OXException {
        if (false == context.isCreateShares()) {
            ComposedMailMessage composeMessage = createRegularComposeMessage(context);
            return new DefaultComposeTransportResult(Collections.singletonList(composeMessage), composeMessage);
        }

        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

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

    private String createFolder(ComposedMailMessage mail, String password, Date expiry, Session session) throws OXException {
        // Get or create base share attachments folder
        IDBasedFolderAccess folderAccess = getFolderAccess(session);
        String parentFolderID;
        {
            String paramterName = MailSessionParameterNames.getParamSharingDriveFolderID();
            Object parameter = session.getParameter(paramterName);
            if (null != parameter) {
                parentFolderID = String.valueOf(parameter);
            } else {
                parentFolderID = discoverEMailAttachmentsFolderID(folderAccess, session);
                session.setParameter(paramterName, parentFolderID);
            }
        }

        // Create folder, pre-shared to an anonymous recipient, for this message
        final DefaultFileStorageFolder folder = prepareFolder(mail, parentFolderID, password, expiry, session);
        Action<String> createFolderAction = new Action<String>() {

            @Override
            public String doAction(IDBasedFolderAccess folderAccess) throws OXException {
                return folderAccess.createFolder(folder);
            }
        };
        int counter = 1;
        do {
            try {
                return performWithinTransaction(createFolderAction, folderAccess);
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

    private String discoverEMailAttachmentsFolderID(IDBasedFolderAccess folderAccess, Session session) throws OXException {
        String name = TransportProperties.getInstance().getPublishingInfostoreFolder();
        if ("i18n-defined".equals(name)) {
            name = ShareComposeStrings.FOLDER_NAME_SHARED_MAIL_ATTACHMENTS;
        }

        FileStorageAccount defaultAccount = getDefaultAccount(session);
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
        final DefaultFileStorageFolder folder = new DefaultFileStorageFolder();
        folder.setName(name);
        DefaultFileStoragePermission permission = DefaultFileStoragePermission.newInstance();
        permission.setEntity(session.getUserId());
        folder.setPermissions(Collections.<FileStoragePermission>singletonList(permission));
        folder.setParentId(personalFolder.getId());
        Action<String> createFolderAction = new Action<String>() {

            @Override
            public String doAction(IDBasedFolderAccess folderAccess) throws OXException {
                return folderAccess.createFolder(folder);
            }
        };
        return performWithinTransaction(createFolderAction, folderAccess);
    }

    private FileStorageAccount getDefaultAccount(Session session) throws OXException {
        FileStorageAccountManagerLookupService lookupService = ServerServiceRegistry.getInstance().getService(FileStorageAccountManagerLookupService.class);
        if (null == lookupService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(FileStorageAccountManagerLookupService.class.getName());
        }
        FileStorageAccountManager accountManager = lookupService.getAccountManagerFor("com.openexchange.infostore");
        return accountManager.getAccount("infostore", session);
    }

    private DefaultFileStorageFolder prepareFolder(ComposedMailMessage mail, String parent, String password, Date expiry, Session session) {
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

    private static ShareRecipient prepareRecipient(String password, Date expiryDate) {
        int bits = Permissions.createPermissionBits(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, false);
        return new AnonymousRecipient(bits, password, expiryDate);
    }

    private static interface Action<R> {

        R doAction(IDBasedFolderAccess folderAccess) throws OXException;
    }

    private <R> R performWithinTransaction(Action<R> action, IDBasedFolderAccess folderAccess) throws OXException {
        boolean rollback = false;
        try {
            folderAccess.startTransaction();
            rollback = true;

            R retval = action.doAction(folderAccess);

            folderAccess.commit();
            rollback = false;
            return retval;
        } finally {
            if (rollback) {
                folderAccess.rollback();
            }
            folderAccess.finish();
        }
    }

}
