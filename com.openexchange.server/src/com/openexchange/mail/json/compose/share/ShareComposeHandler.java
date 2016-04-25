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

import static com.openexchange.mail.json.compose.share.ShareComposeConstants.HEADER_SHARE_MAIL;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.idn.IDNA;
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
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.json.compose.AbstractComposeHandler;
import com.openexchange.mail.json.compose.ComposeDraftResult;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.ComposeTransportResult;
import com.openexchange.mail.json.compose.DefaultComposeDraftResult;
import com.openexchange.mail.json.compose.DefaultComposeTransportResult;
import com.openexchange.mail.json.compose.share.internal.MessageGeneratorRegistry;
import com.openexchange.mail.json.compose.share.internal.ShareComposeLinkGenerator;
import com.openexchange.mail.json.compose.share.spi.MessageGenerator;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareLink;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tx.TransactionAware;
import com.openexchange.user.UserService;


/**
 * {@link ShareComposeHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareComposeHandler extends AbstractComposeHandler<ShareTransportComposeContext, ShareDraftComposeContext> {

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
    protected ShareDraftComposeContext createDraftComposeContext(ComposeRequest request) throws OXException {
        return new ShareDraftComposeContext(request);
    }

    @Override
    protected ShareTransportComposeContext createTransportComposeContext(ComposeRequest request) throws OXException {
        return new ShareTransportComposeContext(request);
    }

    @Override
    protected ComposeDraftResult doCreateDraftResult(ComposeRequest request, ShareDraftComposeContext context) throws OXException {
        ComposedMailMessage composeMessage = createRegularComposeMessage(context);
        return new DefaultComposeDraftResult(composeMessage);
    }

    @Override
    protected ComposeTransportResult doCreateTransportResult(ComposeRequest request, ShareTransportComposeContext context) throws OXException {
        if (false == context.isCreateShares()) {
            ComposedMailMessage composeMessage = createRegularComposeMessage(context);
            return new DefaultComposeTransportResult(Collections.singletonList(composeMessage), composeMessage);
        }

        if (context.isAddWarning()) {
            List<OXException> warnings = request.getWarnings();
            if (null != warnings) {
                warnings.add(MailExceptionCode.USED_SHARING_FEATURE.create());
            }
        }

        // Get the basic source message
        ComposedMailMessage source = context.getSourceMessage();

        // Collect recipients
        Set<Recipient> recipients;
        {
            Set<InternetAddress> addresses = new HashSet<InternetAddress>();
            addresses.addAll(Arrays.asList(source.getTo()));
            addresses.addAll(Arrays.asList(source.getCc()));
            addresses.addAll(Arrays.asList(source.getBcc()));

            UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class);
            Context ctx = request.getContext();

            recipients = new LinkedHashSet<>(addresses.size());
            for (InternetAddress address : addresses) {
                User user = resolveToUser(address, ctx, userService);
                String personal = address.getPersonal();
                String sAddress = address.getAddress();
                recipients.add(null == user ? Recipient.createExternalRecipient(personal, sAddress) : Recipient.createInternalRecipient(personal, sAddress, user));
            }
        }

        // Optional password and expiration date
        String password = getPassword(request);
        Date expirationDate = getExpirationDate(request);

        // Get folder identifier
        String folderID = createFolder(source, password, expirationDate, context.getSession());

        // Save attachments into that folder
        List<String> savedAttachments = saveAttachments(folderID, context.getAllParts(), context.getSession());

        // Create share target for that folder for an anonymous user
        ShareTarget folderTarget = new ShareTarget(FileStorageContentType.getInstance().getModule(), folderID);
        ShareLink folderLink = ServerServiceRegistry.getServize(ShareService.class).getLink(context.getSession(), folderTarget);

        // Create share compose reference
        ShareReference shareReference;
        {
            String basicShareUrl = folderLink.getShareURL(request.getRequest().getHostData());
            shareReference = new ShareReference(basicShareUrl, savedAttachments, folderID, context.getSession().getUserId(), context.getSession().getContextId());
        }

        // Create share link(s) for recipients
        Map<ShareComposeLink, Set<Recipient>> links = new LinkedHashMap<>(recipients.size());
        {
            GuestInfo guest = folderLink.getGuest();
            HostData hostData = request.getRequest().getHostData();
            for (Recipient recipient : recipients) {
                ShareComposeLink linkedAttachment = ShareComposeLinkGenerator.getInstance().createShareLink(recipient, folderTarget, guest, hostData, null, context.getSession());
                Set<Recipient> associatedRecipients = links.get(linkedAttachment);
                if (null == associatedRecipients) {
                    associatedRecipients = new LinkedHashSet<>(recipients.size());
                    links.put(linkedAttachment, associatedRecipients);
                }
                associatedRecipients.add(recipient);
            }
        }

        // Create personal share link
        ShareComposeLink personalLink;
        {
            personalLink = ShareComposeLinkGenerator.getInstance().createPersonalShareLink(folderTarget, request.getRequest().getHostData(), null, context.getSession());
        }

        // Generate messages from links
        List<ComposedMailMessage> transportMessages = new LinkedList<>();
        ComposedMailMessage sentMessage;
        {
            String referenceString = shareReference.generateReferenceString();

            MessageGeneratorRegistry generatorRegistry = ServerServiceRegistry.getInstance().getService(MessageGeneratorRegistry.class);
            MessageGenerator messageGenerator = generatorRegistry.getMessageGeneratorFor(context.getSession());
            for (Map.Entry<ShareComposeLink, Set<Recipient>> entry : links.entrySet()) {
                ShareComposeMessageInfo messageInfo = new ShareComposeMessageInfo(entry.getKey(), new ArrayList<Recipient>(entry.getValue()), password, expirationDate, source, context);
                List<ComposedMailMessage> messages = messageGenerator.generateTransportMessagesFor(messageInfo);
                for (ComposedMailMessage transportMessage : messages) {
                    transportMessage.setHeader(HEADER_SHARE_MAIL, referenceString);
                    transportMessages.add(transportMessage);
                }
            }

            String sendAddr = request.getSession().getUserSettingMail().getSendAddr();
            User user = request.getUser();
            Recipient userRecipient = Recipient.createInternalRecipient(user.getDisplayName(), sendAddr, user);
            sentMessage = messageGenerator.generateSentMessageFor(new ShareComposeMessageInfo(personalLink, Collections.singletonList(userRecipient), password, expirationDate, source, context));
            sentMessage.setHeader(HEADER_SHARE_MAIL, referenceString);
        }

        return new DefaultComposeTransportResult(transportMessages, sentMessage);
    }

    protected String getPassword(ComposeRequest request) {
        String value = request.getRequest().getParameter("password");
        return Strings.isEmpty(value) ? null : value;
    }

    protected Date getExpirationDate(ComposeRequest request) throws OXException {
        String value = request.getRequest().getParameter("expires");
        if (Strings.isEmpty(value)) {
            return null;
        }

        try {
            long millis = Long.parseLong(value);
            int offset = TimeZoneUtils.getTimeZone(request.getSession().getUser().getTimeZone()).getOffset(millis);
            return new Date(millis - offset);
        } catch (NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "expires", value);
        }
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

    private List<String> saveAttachments(final String folderID, final List<MailPart> attachments, Session session) throws OXException {
        IDBasedFileAccess fileAccess = getFileAccess(session);
        final List<String> createdFiles = new ArrayList<String>(attachments.size());

        Action<IDBasedFileAccess, Void> action = new Action<IDBasedFileAccess, Void>() {

            @Override
            public Void doAction(IDBasedFileAccess fileAccess) throws OXException {
                for (MailPart attachment : attachments) {
                    createdFiles.add(saveAttachment(fileAccess, folderID, attachment));
                }
                return null;
            }
        };
        performWithinTransaction(action, fileAccess);

        return createdFiles;
    }

    String saveAttachment(IDBasedFileAccess fileAccess, String folderID, MailPart attachment) throws OXException {
        File file = prepareMetadata(attachment, folderID);
        InputStream inputStream = null;
        try {
            inputStream = attachment.getInputStream();
            return fileAccess.saveDocument(file, inputStream, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);
        } finally {
            Streams.close(inputStream);
        }
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
        Action<IDBasedFolderAccess, String> createFolderAction = new Action<IDBasedFolderAccess, String>() {

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
        Action<IDBasedFolderAccess, String> createFolderAction = new Action<IDBasedFolderAccess, String>() {

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

    private static interface Action<S extends TransactionAware, R> {

        R doAction(S store) throws OXException;
    }

    private <S extends TransactionAware, R> R performWithinTransaction(Action<S, R> action, S store) throws OXException {
        boolean rollback = false;
        try {
            store.startTransaction();
            rollback = true;

            R retval = action.doAction(store);

            store.commit();
            rollback = false;
            return retval;
        } finally {
            if (rollback) {
                store.rollback();
            }
            store.finish();
        }
    }

    private User resolveToUser(InternetAddress address, Context ctx, UserService userService) throws OXException {
        User user;
        try {
            user = userService.searchUser(IDNA.toIDN(address.getAddress()), ctx);
        } catch (final OXException e) {
            /*
             * Unfortunately UserService.searchUser() throws an exception if no user could be found matching given email address.
             * Therefore check for this special error code and throw an exception if it is not equal.
             */
            if (!LdapExceptionCode.NO_USER_BY_MAIL.equals(e)) {
                throw e;
            }
            user = null;
        }
        return user;
    }

}
