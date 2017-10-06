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

package com.openexchange.folderstorage.mail;

import static com.openexchange.folderstorage.mail.MailFolderStorage.closeMailAccess;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import gnu.trove.map.hash.TIntIntHashMap;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderExtension;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.SystemContentType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.mail.contentType.DraftsContentType;
import com.openexchange.folderstorage.mail.contentType.MailContentType;
import com.openexchange.folderstorage.mail.contentType.SentContentType;
import com.openexchange.folderstorage.mail.contentType.SpamContentType;
import com.openexchange.folderstorage.mail.contentType.TrashContentType;
import com.openexchange.folderstorage.type.MailType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageEnhanced;
import com.openexchange.mail.api.IMailFolderStorageEnhanced2;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link MailFolderImpl} - A mail folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailFolderImpl extends AbstractFolder implements FolderExtension {

    private static final long serialVersionUID = 6445442372690458946L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailFolderImpl.class);

    private static final String PROTOCOL_UNIFIED_INBOX = UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX;
    private static final String CAPABILITY_COUNT_TOTAL = Strings.asciiLowerCase(FileStorageCapability.COUNT_TOTAL.name());
    private static final String CAPABILITY_CASE_INSENSITIVE = Strings.asciiLowerCase(FileStorageCapability.CASE_INSENSITIVE.name());
    private static final String CAPABILITY_STORE_SEEN = "STORE_SEEN";
    private static final String CAPABILITY_FOLDER_VALIDITY = "FOLDER_VALIDITY";
    private static final String CAPABILITY_FILENAME_SEARCH = "FILENAME_SEARCH";

    /**
     * The mail folder content type.
     */
    public static enum MailFolderType {
        NONE(MailContentType.getInstance(), 0),
        ROOT(SystemContentType.getInstance(), 0),
        INBOX(MailContentType.getInstance(), 7), // FolderObject.MAIL
        DRAFTS(DraftsContentType.getInstance(), 9),
        SENT(SentContentType.getInstance(), 10),
        SPAM(SpamContentType.getInstance(), 11),
        TRASH(TrashContentType.getInstance(), 12);

        private final ContentType contentType;

        private final int type;

        private MailFolderType(final ContentType contentType, final int type) {
            this.contentType = contentType;
            this.type = type;
        }

        /**
         * Gets the content type associated with this mail folder type.
         *
         * @return The content type
         */
        public ContentType getContentType() {
            return contentType;
        }

        /**
         * Gets the type.
         *
         * @return The type
         */
        public int getType() {
            return type;
        }

    }

    private static Set<MailFolderType> STANDARD_FOLDER_TYPES = EnumSet.of(MailFolderType.INBOX, MailFolderType.TRASH, MailFolderType.DRAFTS, MailFolderType.SENT, MailFolderType.SPAM);

    private static boolean isNoDefaultFolder(MailFolderType mailFolderType, String fullName, MailAccess<?, ?> mailAccess, MailAccount mailAccount) throws OXException {
        return !STANDARD_FOLDER_TYPES.contains(mailFolderType) && !MailFolderStorage.isArchiveFolder(fullName, mailAccess, mailAccount);
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private final AtomicReference<MailFolderType> mailFolderTypeRef;
    private final boolean cacheable;
    private final String fullName;
    private final int mailAccountId;
    private final int userId;
    private final int contextId;
    private final LocalizedNameProvider localizedNameProvider;

    private final int m_total;
    private final int m_unread;

    /** The special bit for user flag support */
    public static final int BIT_USER_FLAG = (1 << 29);

    /** The special bit for rename grant */
    public static final int BIT_RENAME_FLAG = (1 << 30);

    /**
     * Initializes a new {@link MailFolderImpl} from given mail folder.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor.
     *
     * @param mailFolder The underlying mail folder
     * @param accountId The account identifier
     * @param mailConfig The mail configuration
     * @param user The user
     * @param context The context
     * @param fullnameProvider The (optional) full name provider
     * @param mailAccess The associated {@link MailAccess} instance
     * @param mailAccount The mail account
     * @param translatePrimaryAccountDefaultFolders Whether to translate names of default folders from the primary account
     * @throws OXException If creation fails
     */
    public MailFolderImpl(MailFolder mailFolder, int accountId, MailConfig mailConfig, StorageParameters params, DefaultFolderFullnameProvider fullnameProvider, MailAccess<?, ?> mailAccess, MailAccount mailAccount, boolean translatePrimaryAccountDefaultFolders) throws OXException {
        this(mailFolder, accountId, mailConfig, params.getUser(), optLocaleFrom(params), params.getContextId(), fullnameProvider, mailAccess, mailAccount, translatePrimaryAccountDefaultFolders);
    }

    private static Locale optLocaleFrom(StorageParameters params) {
        FolderServiceDecorator decorator = params.getDecorator();
        return null == decorator ? null : decorator.getLocale();
    }

    /**
     * Initializes a new {@link MailFolderImpl} from given mail folder.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor.
     *
     * @param mailFolder The underlying mail folder
     * @param accountId The account identifier
     * @param mailConfig The mail configuration
     * @param user The user
     * @param locale The optional locale to use; if <code>null</code> user's locale is used
     * @param contextId The context identifier
     * @param fullnameProvider The (optional) full name provider
     * @param mailAccess The associated {@link MailAccess} instance
     * @param mailAccount The mail account
     * @param translatePrimaryAccountDefaultFolders Whether to translate names of default folders from the primary account
     * @throws OXException If creation fails
     */
    public MailFolderImpl(MailFolder mailFolder, int accountId, MailConfig mailConfig, User user, Locale locale, int contextId, DefaultFolderFullnameProvider fullnameProvider, MailAccess<?, ?> mailAccess, MailAccount mailAccount, boolean translatePrimaryAccountDefaultFolders) throws OXException {
        super();
        this.mailAccountId = accountId;
        this.accountId = MailFolderUtility.prepareFullname(accountId, MailFolder.DEFAULT_FOLDER_ID);
        userId = user.getId();
        this.contextId = contextId;
        fullName = mailFolder.getFullname();
        id = MailFolderUtility.prepareFullname(accountId, fullName);
        String folderName = mailFolder.getName();
        name = folderName;
        Locale effectiveLocale = null == locale ? (null == user.getLocale() ? Locale.US : user.getLocale()) : locale;
        LocalizedNameProvider localizedNameProvider;
        if ("INBOX".equals(fullName)) {
            localizedNameProvider = new CommonLocalizedNameProvider(MailStrings.INBOX);
        } else if (mailFolder.isRootFolder() && isUnifiedMail(mailFolder)) {
            localizedNameProvider = new CommonLocalizedNameProvider(MailStrings.UNIFIED_MAIL);
        } else {
            localizedNameProvider = new StaticLocalizedNameProvider(folderName);
        }
        // Determine the parent...
        if (mailFolder.isRootFolder()) {
            parent = FolderStorage.PRIVATE_ID;
        } else {
            String parentFullName = MailFolderUtility.prepareFullname(accountId, mailFolder.getParentFullname());
            if (parentFullName != null && parentFullName.equals(id)) {
                parent = determineParentFrom(accountId, mailFolder.getParentFullname(), mailFolder.containsSeparator() ? mailFolder.getSeparator() : mailAccess.getRootFolder().getSeparator());
                LOG.warn("Mail folder \"{}\" references itself as parent. Assuming \"{}\" instead... (user={}, context={})", id, parent, I(userId), I(contextId));
            } else {
                parent = parentFullName;
            }
        }
        final MailPermission[] mailPermissions = mailFolder.getPermissions();
        permissions = new Permission[mailPermissions.length];
        for (int i = 0; i < mailPermissions.length; i++) {
            permissions[i] = new MailPermissionImpl(mailPermissions[i]);
        }
        type = MailType.getInstance();
        final boolean ignoreSubscription = mailConfig.getMailProperties().isIgnoreSubscription();
        subscribed = ignoreSubscription ? true : mailFolder.isSubscribed(); // || mailFolder.hasSubscribedSubfolders();
        subscribedSubfolders = ignoreSubscription ? mailFolder.hasSubfolders() : mailFolder.hasSubscribedSubfolders();
        summary = mailFolder.isRootFolder() ? "" : new StringBuilder(16).append('(').append(mailFolder.getMessageCount()).append('/').append(mailFolder.getUnreadMessageCount()).append(')').toString();
        deefault = 0 == accountId && mailFolder.isDefaultFolder();
        total = mailFolder.getMessageCount();
        nu = mailFolder.getNewMessageCount();
        unread = mailFolder.getUnreadMessageCount();
        deleted = mailFolder.getDeletedMessageCount();
        MailPermission mp;
        MailFolderType mailFolderType;
        if (mailFolder.isRootFolder()) {
            mailFolderType = MailFolderType.ROOT;
            mp = mailFolder.getOwnPermission();
            if (mp == null) {
                mp = new DefaultMailPermission();
                mp.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
                mp.setFolderAdmin(false);
            }
        } else {
            mp = mailFolder.getOwnPermission();

            // Check if entity's permission allows to read the folder: Every mail folder listed is at least visible to user
            for (Permission pe : permissions) {
                if ((pe.getEntity() == mp.getEntity()) && (pe.getFolderPermission() <= Permission.NO_PERMISSIONS)) {
                    pe.setFolderPermission(Permission.READ_FOLDER);
                }
            }

            // Determine the mail folder type (None, Trash, Sent, ...) and set localized name
            MailFolderTypeRetval retval = determineMailFolderType(folderName, mailFolder, mailAccess, mailAccount, effectiveLocale, fullnameProvider, translatePrimaryAccountDefaultFolders);
            mailFolderType = retval.mailFolderType;
            if (null != retval.localizedNameProvider) {
                localizedNameProvider = retval.localizedNameProvider;
            }
        }
        this.mailFolderTypeRef = new AtomicReference<MailFolderType>(mailFolderType);
        this.localizedNameProvider = localizedNameProvider;

        {
            String client = Strings.asciiLowerCase(mailAccess.getSession().getClient());
            if (null == client || !client.startsWith("usm-")) {
                if (MailFolderType.NONE.equals(mailFolderType)) {
                    if (mailFolder.containsShared() && mailFolder.isShared()) {
                        type = SharedType.getInstance();
                    } else if (mailFolder.containsPublic() && mailFolder.isPublic()) {
                        type = PublicType.getInstance();
                    }
                }
            }
        }

        this.capabilities = mailConfig.getCapabilities().getCapabilities();
        if (mailConfig.getCapabilities().hasFileNameSearch()) {
            addSupportedCapabilities(CAPABILITY_FILENAME_SEARCH);
        }
        if (mailConfig.getCapabilities().hasFolderValidity()) {
            addSupportedCapabilities(CAPABILITY_FOLDER_VALIDITY);
        }
        if (!mailFolder.isHoldsFolders() && mp.canCreateSubfolders()) {
            // Cannot contain subfolders; therefore deny subfolder creation
            mp.setFolderPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER);
        }
        if (mp.canReadOwnObjects()) {
            if (mailFolder.isHoldsMessages()) {
                addSupportedCapabilities(CAPABILITY_COUNT_TOTAL);
            } else {
                // Cannot contain messages; therefore deny read access. Folder is not selectable.
                mp.setReadObjectPermission(OCLPermission.NO_PERMISSIONS);
            }
        }
        addSupportedCapabilities(CAPABILITY_CASE_INSENSITIVE);

        final int canStoreSeenFlag = mp.canStoreSeenFlag();
        if (canStoreSeenFlag > 0 || ((canStoreSeenFlag < 0) && (mp.getReadPermission() > MailPermission.NO_PERMISSIONS))) {
            addSupportedCapabilities(CAPABILITY_STORE_SEEN);
        }
        // Permission bits
        int permissionBits = createPermissionBits(mp.getFolderPermission(), mp.getReadPermission(), mp.getWritePermission(), mp.getDeletePermission(), mp.isFolderAdmin());
        if (mailFolder.isSupportsUserFlags()) {
            permissionBits |= BIT_USER_FLAG;
        }
        final int canRename = mp.canRename();
        if ((canRename > 0 || (mp.isFolderAdmin() && canRename < 0)) && isNoDefaultFolder(mailFolderType, fullName, mailAccess, mailAccount)) {
            // Rename only allowed for non-standard folders
            permissionBits |= BIT_RENAME_FLAG;
        }
        bits = permissionBits;
        // Check if folder is cacheable
        boolean cache = true;
        if (mailFolder.liveAccess()) {
            if (mailFolder.containsShared() && mailFolder.isShared()) { // A shared mail folder must not be cacheable
                cache = false;
            } else if (mailFolder.isTrash()) { // Trash folder must not be cacheable
                cache = false;
            } else if (isUnifiedMail(mailFolder)) { // Unified mail must not be cacheable
                cache = false;
            }
        } else {
            // Already cached in MAL API layer
            cache = false;
        }
        // If not cached, we can obtain total/unread counter here
        int m_total = -1;
        int m_unread = -1;
        if (!cache) {
            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();

            IMailFolderStorageEnhanced2 storageEnhanced2 = folderStorage.supports(IMailFolderStorageEnhanced2.class);
            if (null != storageEnhanced2) {
                int[] tu = storageEnhanced2.getTotalAndUnreadCounter(ensureFullName(fullName));
                m_total = null == tu ? -1 : tu[0];
                m_unread = null == tu ? -1 : tu[1];
            } else {
                IMailFolderStorageEnhanced storageEnhanced = folderStorage.supports(IMailFolderStorageEnhanced.class);
                if (null != storageEnhanced) {
                    m_total = storageEnhanced.getTotalCounter(ensureFullName(fullName));
                    m_unread = storageEnhanced.getUnreadCounter(ensureFullName(fullName));
                } else {
                    m_total = mailAccess.getMessageStorage().searchMessages(ensureFullName(fullName), IndexRange.NULL, MailSortField.RECEIVED_DATE, OrderDirection.ASC, null, FIELDS_ID).length;
                    m_unread = mailAccess.getMessageStorage().getUnreadMessages(ensureFullName(fullName), MailSortField.RECEIVED_DATE, OrderDirection.DESC, FIELDS_ID, -1).length;
                }
            }
        }
        this.m_total = m_total;
        this.m_unread = m_unread;
        cacheable = cache;
    }

    /**
     * Determines the parent from specified full name path.
     *
     * @param accountId The account identifier
     * @param fullName The full name to determine the parent from
     * @return The parent full name
     */
    private String determineParentFrom(int accountId, String fullName, char separator) {
        int index = fullName.lastIndexOf(separator);

        if (index >= 0) {
            return MailFolderUtility.prepareFullname(accountId, fullName.substring(0, index));
        }

        return MailFolder.DEFAULT_FOLDER_ID.equals(fullName) ? FolderStorage.PRIVATE_ID : MailFolderUtility.prepareFullname(accountId, MailFolder.DEFAULT_FOLDER_ID);
    }

    private LocalizedNameProvider getLocalizedNameProviderForPrimaryStandard(String key, boolean translatePrimaryAccountDefaultFolders, String folderName) {
        return translatePrimaryAccountDefaultFolders ? new CommonLocalizedNameProvider(key) : new StaticLocalizedNameProvider(folderName);
    }

    private MailFolderTypeRetval determineMailFolderType(String folderName, MailFolder mailFolder, MailAccess<?, ?> mailAccess, MailAccount mailAccount, Locale effectiveLocale, DefaultFolderFullnameProvider fullnameProvider, boolean translatePrimaryAccountDefaultFolders) throws OXException {
        MailFolderType mailFolderType = MailFolderType.NONE;
        LocalizedNameProvider localizedNameProvider = null;
        boolean isPrimaryAccount = MailAccount.DEFAULT_ID == mailAccount.getId();
        if (mailFolder.containsDefaultFolderType()) {
            switch (mailFolder.getDefaultFolderType()) {
            case INBOX:
                mailFolderType = MailFolderType.INBOX;
                break;
            case TRASH:
                if (isPrimaryAccount) {
                    localizedNameProvider = getLocalizedNameProviderForPrimaryStandard(MailStrings.TRASH, translatePrimaryAccountDefaultFolders, folderName);
                }
                mailFolderType = MailFolderType.TRASH;
                break;
            case SENT:
                if (isPrimaryAccount) {
                    localizedNameProvider = getLocalizedNameProviderForPrimaryStandard(MailStrings.SENT, translatePrimaryAccountDefaultFolders, folderName);
                }
                mailFolderType = MailFolderType.SENT;
                break;
            case SPAM:
                if (isPrimaryAccount) {
                    localizedNameProvider = getLocalizedNameProviderForPrimaryStandard(MailStrings.SPAM, translatePrimaryAccountDefaultFolders, folderName);
                }
                mailFolderType = MailFolderType.SPAM;
                break;
            case DRAFTS:
                if (isPrimaryAccount) {
                    localizedNameProvider = getLocalizedNameProviderForPrimaryStandard(MailStrings.DRAFTS, translatePrimaryAccountDefaultFolders, folderName);
                }
                mailFolderType = MailFolderType.DRAFTS;
                break;
            case CONFIRMED_SPAM:
                if (isPrimaryAccount) {
                    localizedNameProvider = new CommonLocalizedNameProvider(MailStrings.CONFIRMED_SPAM);
                }
                break;
            case CONFIRMED_HAM:
                if (isPrimaryAccount) {
                    localizedNameProvider = new CommonLocalizedNameProvider(MailStrings.CONFIRMED_HAM);
                }
                break;
            default:
                if (isPrimaryAccount && translatePrimaryAccountDefaultFolders && MailFolderStorage.isArchiveFolder(fullName, mailAccess, mailAccount)) {
                    localizedNameProvider = new CommonLocalizedNameProvider(MailStrings.ARCHIVE);
                }
                // Nope
            }
        } else if (null != fullName) {
            if (null == fullnameProvider) {
                mailFolderType = MailFolderType.ROOT;
            } else {
                try {
                    if (fullName.equals(fullnameProvider.getDraftsFolder())) {
                        if (isPrimaryAccount) {
                            localizedNameProvider = getLocalizedNameProviderForPrimaryStandard(MailStrings.DRAFTS, translatePrimaryAccountDefaultFolders, folderName);
                        }
                        mailFolderType = MailFolderType.DRAFTS;
                    } else if (fullName.equals(fullnameProvider.getINBOXFolder())) {
                        mailFolderType = MailFolderType.INBOX;
                    } else if (fullName.equals(fullnameProvider.getSentFolder())) {
                        if (isPrimaryAccount) {
                            localizedNameProvider = getLocalizedNameProviderForPrimaryStandard(MailStrings.SENT, translatePrimaryAccountDefaultFolders, folderName);
                        }
                        mailFolderType = MailFolderType.SENT;
                    } else if (fullName.equals(fullnameProvider.getSpamFolder())) {
                        if (isPrimaryAccount) {
                            localizedNameProvider = getLocalizedNameProviderForPrimaryStandard(MailStrings.SPAM, translatePrimaryAccountDefaultFolders, folderName);
                        }
                        mailFolderType = MailFolderType.SPAM;
                    } else if (fullName.equals(fullnameProvider.getTrashFolder())) {
                        if (isPrimaryAccount) {
                            localizedNameProvider = getLocalizedNameProviderForPrimaryStandard(MailStrings.TRASH, translatePrimaryAccountDefaultFolders, folderName);
                        }
                        mailFolderType = MailFolderType.TRASH;
                    } else {
                        if (isPrimaryAccount && translatePrimaryAccountDefaultFolders) {
                            if (fullName.equals(fullnameProvider.getConfirmedSpamFolder())) {
                                localizedNameProvider = new CommonLocalizedNameProvider(MailStrings.CONFIRMED_SPAM);
                            } else if (fullName.equals(fullnameProvider.getConfirmedHamFolder())) {
                                localizedNameProvider = new CommonLocalizedNameProvider(MailStrings.CONFIRMED_HAM);
                            } else if (MailFolderStorage.isArchiveFolder(fullName, mailAccess, mailAccount)) {
                                localizedNameProvider = new CommonLocalizedNameProvider(MailStrings.ARCHIVE);
                            }
                        }
                    }
                } catch (final OXException e) {
                    org.slf4j.LoggerFactory.getLogger(MailFolderImpl.class).error("", e);
                    mailFolderType = MailFolderType.NONE;
                }
            }
        } else {
            mailFolderType = MailFolderType.NONE;
        }
        return new MailFolderTypeRetval(mailFolderType, localizedNameProvider);
    }

    private boolean isUnifiedMail(final MailFolder mailFolder) {
        return PROTOCOL_UNIFIED_INBOX.equals(mailFolder.getProperty("protocol"));
    }

    /**
     * The actual max permission that can be transfered in field 'bits' or JSON's permission object
     */
    private static final int MAX_PERMISSION = 64;

    private static final TIntIntHashMap MAPPING = new TIntIntHashMap(6) {
        { //Unnamed Block.
            put(Permission.MAX_PERMISSION, MAX_PERMISSION);
            put(MAX_PERMISSION, MAX_PERMISSION);
            put(0, 0);
            put(2, 1);
            put(4, 2);
            put(8, 4);
        }
    };

    static int createPermissionBits(final Permission perm) {
        return createPermissionBits(
            perm.getFolderPermission(),
            perm.getReadPermission(),
            perm.getWritePermission(),
            perm.getDeletePermission(),
            perm.isAdmin());
    }

    static int createPermissionBits(final int fp, final int rp, final int wp, final int dp, final boolean adminFlag) {
        int retval = 0;
        int i = 4;
        retval += (adminFlag ? 1 : 0) << (i-- * 7)/*Number of bits to be shifted*/;
        retval += MAPPING.get(dp) << (i-- * 7);
        retval += MAPPING.get(wp) << (i-- * 7);
        retval += MAPPING.get(rp) << (i-- * 7);
        retval += MAPPING.get(fp) << (i * 7);
        return retval;
    }

    private static final MailField[] FIELDS_ID = new MailField[] { MailField.ID };

    @Override
    public int getUnread() {
        final int unread = m_unread;
        if (unread >= 0) {
            return unread;
        }

        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(userId, contextId, mailAccountId);
            mailAccess.connect(false);
            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();

            IMailFolderStorageEnhanced storageEnhanced = folderStorage.supports(IMailFolderStorageEnhanced.class);
            if (null != storageEnhanced) {
                return storageEnhanced.getUnreadCounter(ensureFullName(fullName));
            }

            return mailAccess.getMessageStorage().getUnreadMessages(ensureFullName(fullName), MailSortField.RECEIVED_DATE, OrderDirection.DESC, FIELDS_ID, -1).length;
        } catch (final OXException e) {
            LOG.debug("Cannot return up-to-date unread counter.", e);
            return super.getUnread();
        } catch (final Exception e) {
            LOG.debug("Cannot return up-to-date unread counter.", e);
            return super.getUnread();
        } finally {
            closeMailAccess(mailAccess);
        }
    }

    @Override
    public int getTotal() {
        final int total = m_total;
        if (total >= 0) {
            return total;
        }

        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(userId, contextId, mailAccountId);
            mailAccess.connect(false);
            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();

            IMailFolderStorageEnhanced storageEnhanced = folderStorage.supports(IMailFolderStorageEnhanced.class);
            if (null != storageEnhanced) {
                return storageEnhanced.getTotalCounter(ensureFullName(fullName));
            }

            return mailAccess.getMessageStorage().searchMessages(ensureFullName(fullName), IndexRange.NULL, MailSortField.RECEIVED_DATE, OrderDirection.ASC, null, FIELDS_ID).length;
        } catch (final OXException e) {
            LOG.debug("Cannot return up-to-date total counter.", e);
            return super.getTotal();
        } catch (final Exception e) {
            LOG.debug("Cannot return up-to-date total counter.", e);
            return super.getTotal();
        } finally {
            closeMailAccess(mailAccess);
        }
    }

    @Override
    public int[] getTotalAndUnread(final ConcurrentMap<String, Object> optParams) {
        final int total = m_total;
        final int unread = m_unread;
        if (total >= 0 && unread >= 0) {
            return new int[] { total, unread };
        }

        // Live look-up of total/unread count
        if (null == optParams) {
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                mailAccess = MailAccess.getInstance(userId, contextId, mailAccountId);
                mailAccess.connect(false);
                return totalAndUnread(mailAccess);
            } catch (final OXException e) {
                LOG.debug("Cannot return up-to-date total counter.", e);
                return null;
            } catch (final Exception e) {
                LOG.debug("Cannot return up-to-date total counter.", e);
                return null;
            } finally {
                closeMailAccess(mailAccess);
            }
        }

        // Look-up provided parameters
        try {
            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = mailAccess(optParams);
            return totalAndUnread(mailAccess);
        } catch (final OXException e) {
            LOG.debug("Cannot return up-to-date total counter.", e);
            return null;
        } catch (final Exception e) {
            LOG.debug("Cannot return up-to-date total counter.", e);
            return null;
        }
    }

    private int[] totalAndUnread(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
        String ensuredFullName = ensureFullName(fullName);

        IMailFolderStorageEnhanced2 storageEnhanced2 = folderStorage.supports(IMailFolderStorageEnhanced2.class);
        if (null != storageEnhanced2) {
            return storageEnhanced2.getTotalAndUnreadCounter(ensuredFullName);
        }

        int unread, total;
        {
            IMailFolderStorageEnhanced storageEnhanced = folderStorage.supports(IMailFolderStorageEnhanced.class);
            if (null != storageEnhanced) {
                unread = storageEnhanced.getUnreadCounter(ensuredFullName);
                total = storageEnhanced.getTotalCounter(ensuredFullName);
            } else {
                unread = mailAccess.getMessageStorage().getUnreadMessages(ensuredFullName, MailSortField.RECEIVED_DATE, OrderDirection.DESC, FIELDS_ID, -1).length;
                total = mailAccess.getMessageStorage().searchMessages(ensuredFullName, IndexRange.NULL, MailSortField.RECEIVED_DATE, OrderDirection.ASC, null, FIELDS_ID).length;
            }
        }

        return new int[] { total, unread };
    }

    private MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess(final ConcurrentMap<String, Object> optParams) throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = (MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>) optParams.get("__macc__");
        if (null == mailAccess) {
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> nu = MailAccess.getInstance(userId, contextId, mailAccountId);
            mailAccess = (MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>) optParams.putIfAbsent("__macc__", nu);
            if (null == mailAccess) {
                // Put into map
                mailAccess = nu;
                mailAccess.connect(false);
            } else {
                // Discard "new" instance
                closeMailAccess(nu);
            }
        }
        return mailAccess;
    }

    private static String ensureFullName(final String fullName) {
        return prepareMailFolderParam(fullName).getFullname();
    }

    @Override
    public Object clone() {
        return super.clone();
    }

    @Override
    public boolean isCacheable() {
        return cacheable;
    }

    @Override
    public ContentType getContentType() {
        return mailFolderTypeRef.get().getContentType();
    }

    @Override
    public int getDefaultType() {
        return mailFolderTypeRef.get().getType();
    }

    @Override
    public void setDefaultType(final int defaultType) {
        // Nothing to do
    }

    /**
     * Sets the mail folder type
     *
     * @param mailFolderType The type to set
     */
    public void setMailFolderType(final MailFolderType mailFolderType) {
        mailFolderTypeRef.set(mailFolderType);
    }

    @Override
    public void setContentType(final ContentType contentType) {
        // Nothing to do
    }

    @Override
    public void setType(final Type type) {
        // Nothing to do
    }

    @Override
    public boolean isGlobalID() {
        return false;
    }

    @Override
    public String getLocalizedName(final Locale locale) {
        LocalizedNameProvider localizedNameProvider = this.localizedNameProvider;
        return null == localizedNameProvider ? name : localizedNameProvider.getLocalizedName(locale);
    }

    // --------------------------------------------------- i18n stuff ------------------------------------------------------------------------

    private static final class MailFolderTypeRetval {

        final MailFolderType mailFolderType;
        final LocalizedNameProvider localizedNameProvider;

        MailFolderTypeRetval(MailFolderType mailFolderType, LocalizedNameProvider localizedNameProvider) {
            super();
            this.mailFolderType = mailFolderType;
            this.localizedNameProvider = localizedNameProvider;
        }
    }

    private static interface LocalizedNameProvider extends Serializable {

        String getLocalizedName(Locale locale);
    }

    private static final class StaticLocalizedNameProvider implements LocalizedNameProvider {

        private static final long serialVersionUID = 7258965483623767051L;

        private final String name;

        StaticLocalizedNameProvider(String name) {
            super();
            this.name = name;
        }

        @Override
        public String getLocalizedName(Locale locale) {
            return name;
        }
    }

    private static final class CommonLocalizedNameProvider implements LocalizedNameProvider {

        private static final long serialVersionUID = -2733154469307477060L;

        private final String key;

        CommonLocalizedNameProvider(String key) {
            super();
            this.key = key;
        }

        @Override
        public String getLocalizedName(Locale locale) {
            return StringHelper.valueOf(locale).getString(key);
        }
    }

}
