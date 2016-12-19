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

package com.openexchange.imap;

import static com.openexchange.java.Strings.quoteReplacement;
import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TCharHashSet;
import gnu.trove.set.hash.TIntHashSet;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Quota;
import javax.mail.Quota.Resource;
import javax.mail.StoreClosedException;
import javax.mail.search.FlagTerm;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.imap.OperationKey.Type;
import com.openexchange.imap.acl.ACLExtension;
import com.openexchange.imap.cache.ListLsubCache;
import com.openexchange.imap.cache.ListLsubEntry;
import com.openexchange.imap.cache.ListLsubRuntimeException;
import com.openexchange.imap.cache.MBoxEnabledCache;
import com.openexchange.imap.cache.NamespaceFoldersCache;
import com.openexchange.imap.cache.RightsCache;
import com.openexchange.imap.cache.RootSubfoldersEnabledCache;
import com.openexchange.imap.cache.UserFlagsCache;
import com.openexchange.imap.command.CopyIMAPCommand;
import com.openexchange.imap.command.FlagsIMAPCommand;
import com.openexchange.imap.command.MoveIMAPCommand;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.config.IMAPReloadable;
import com.openexchange.imap.converters.IMAPFolderConverter;
import com.openexchange.imap.dataobjects.IMAPMailFolder;
import com.openexchange.imap.entity2acl.Entity2ACL;
import com.openexchange.imap.entity2acl.Entity2ACLArgs;
import com.openexchange.imap.entity2acl.Entity2ACLExceptionCode;
import com.openexchange.imap.entity2acl.UserGroupID;
import com.openexchange.imap.notify.internal.IMAPNotifierMessageRecentListener;
import com.openexchange.imap.services.Services;
import com.openexchange.imap.util.FolderUtility;
import com.openexchange.imap.util.IMAPSessionStorageAccess;
import com.openexchange.java.Collators;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.api.IMailFolderStorageDefaultFolderAware;
import com.openexchange.mail.api.IMailFolderStorageEnhanced2;
import com.openexchange.mail.api.IMailFolderStorageInfoSupport;
import com.openexchange.mail.api.IMailFolderStorageStatusSupport;
import com.openexchange.mail.api.IMailSharedFolderPathResolver;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailFolderInfo;
import com.openexchange.mail.dataobjects.MailFolderStatus;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ParsingException;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.protocol.ListInfo;

/**
 * {@link IMAPFolderStorage} - The IMAP folder storage implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPFolderStorage extends MailFolderStorage implements IMailFolderStorageEnhanced2, IMailFolderStorageInfoSupport, IMailFolderStorageDefaultFolderAware, IMailSharedFolderPathResolver, IMailFolderStorageStatusSupport {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IMAPFolderStorage.class);

    /**
     * Gets the max. length for a mailbox name
     */
    private static volatile Integer maxMailboxNameLength;
    private static int maxMailboxNameLength() {
        Integer tmp = maxMailboxNameLength;
        if (null == tmp) {
            synchronized (IMAPFolderStorage.class) {
                tmp = maxMailboxNameLength;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    tmp = Integer.valueOf(null == service ? 60 : service.getIntProperty("com.openexchange.imap.maxMailboxNameLength", 60));
                    maxMailboxNameLength = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    static {
        IMAPReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(final ConfigurationService configService) {
                maxMailboxNameLength = null;
                invalidChars = null;
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.imap.maxMailboxNameLength", "com.openexchange.imap.invalidMailboxNameCharacters");
            }
        });
    }

    private static final String STR_INBOX = "INBOX";

    private final IMAPStore imapStore;
    private final IMAPAccess imapAccess;
    private final int accountId;
    private final Session session;
    private final Context ctx;
    private final IMAPConfig imapConfig;
    private final boolean ignoreSubscriptions;

    private Character separator;
    private IMAPDefaultFolderChecker checker;

    /**
     * Initializes a new {@link IMAPFolderStorage}
     *
     * @param imapStore The IMAP store
     * @param imapAccess The IMAP access
     * @param session The session providing needed user data
     * @throws OXException If context loading fails
     */
    public IMAPFolderStorage(final IMAPStore imapStore, final IMAPAccess imapAccess, final Session session) throws OXException {
        super();
        this.imapStore = imapStore;
        this.imapAccess = imapAccess;
        accountId = imapAccess.getAccountId();
        this.session = session;
        ctx = session instanceof ServerSession ? ((ServerSession) session).getContext() : ContextStorage.getStorageContext(session.getContextId());
        imapConfig = imapAccess.getIMAPConfig();
        ignoreSubscriptions = imapConfig.getIMAPProperties().isIgnoreSubscription();
    }

    /**
     * Handles specified {@link MessagingException} instance.
     *
     * @param e The {@link MessagingException} instance
     * @return The appropriate {@link OXException} instance
     */
    public OXException handleMessagingException(MessagingException e) {
        return handleMessagingException(null, e);
    }

    /**
     * Handles specified {@link MessagingException} instance.
     *
     * @param optFullName The optional full name
     * @param e The {@link MessagingException} instance
     * @return The appropriate {@link OXException} instance
     */
    public OXException handleMessagingException(String optFullName, MessagingException e) {
        if (null != optFullName && MimeMailException.isInUseException(e)) {
            IMAPFolderWorker.markForFailFast(imapStore, optFullName, e);
        }
        return IMAPException.handleMessagingException(e, imapConfig, session, accountId, null == optFullName ? null : mapFor("fullName", optFullName));
    }

    /**
     * Gets the associated session.
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets the associated context.
     *
     * @return The context
     */
    public Context getContext() {
        return ctx;
    }

    /**
     * Gets the associated IMAP configuration.
     *
     * @return The IMAP configuration
     */
    public IMAPConfig getImapConfig() {
        return imapConfig;
    }

    /**
     * Gets the IMAP access.
     *
     * @return The IMAP access
     */
    public IMAPAccess getImapAccess() {
        return imapAccess;
    }

    /**
     * Gets the associated IMAP store.
     *
     * @return The IMAP store
     */
    public IMAPStore getImapStore() {
        return imapStore;
    }

    /**
     * Gets the associated account identifier.
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
    }

    private IMAPDefaultFolderChecker getChecker() {
        if (null == checker) {
            Map<String, String> caps = imapConfig.asMap();
            boolean hasMetadata = caps.containsKey("METADATA"); // http://tools.ietf.org/html/rfc5464#section-1
            if (caps.containsKey("SPECIAL-USE")) {
                // Supports SPECIAL-USE capability
                boolean hasCreateSpecialUse = caps.containsKey("CREATE-SPECIAL-USE"); // http://tools.ietf.org/html/rfc6154#section-3
                checker = new SpecialUseDefaultFolderChecker(accountId, session, ctx, imapStore, imapAccess, hasCreateSpecialUse, hasMetadata);
            } else {
                checker = new IMAPDefaultFolderChecker(accountId, session, ctx, imapStore, imapAccess, hasMetadata);
            }
        }
        return checker;
    }

    private char getSeparator() throws MessagingException {
        if (null == separator) {
            try {
                separator = Character.valueOf(ListLsubCache.getSeparator(accountId, (DefaultFolder) imapStore.getDefaultFolder(), session, this.ignoreSubscriptions));
            } catch (OXException e) {
                Throwable cause = e.getCause();
                if (cause instanceof MessagingException) {
                    throw (MessagingException) cause;
                }
                throw (cause instanceof Exception) ? new MessagingException(cause.getMessage(), (Exception) cause) : new MessagingException(e.getMessage(), e);
            }
        }
        return separator.charValue();
    }

    private char getSeparator(char sep) {
        if (null == separator) {
            separator = Character.valueOf(sep);
        }
        return separator.charValue();
    }

    @Override
    public boolean isStatusSupported() throws OXException {
    	return true;
    }

    @Override
    public MailFolderStatus getFolderStatus(String fullName) throws OXException {
    	try {
            final String fn = (DEFAULT_FOLDER_ID.equals(fullName) ? "" : fullName);
            // Retrieve folder...
            IMAPFolderWorker.checkFailFast(imapStore, fullName);
            IMAPFolder f;
            if (0 == fn.length()) {
                f = (IMAPFolder) imapStore.getDefaultFolder();
            } else {
                f = (IMAPFolder) imapStore.getFolder(fullName);
            }

            // ... and check existence
            boolean exists = f.exists();
            if (!exists) {

                try {
                    f.open(IMAPFolder.READ_ONLY);
                    exists = true;
                } catch (javax.mail.FolderNotFoundException e) {
                    exists = false;
                } finally {
                    if (exists) {
                        f.close(false);
                    }
                }

                if (!exists) {
                    f = checkForNamespaceFolder(fn);
                    if (null == f) {
                        throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                    }
                }
            }

            return MailFolderStatus.builder()
                .nextId(Long.toString(f.getUIDNext()))
                .total(f.getMessageCount())
                .unread(f.getUnreadMessageCount())
                .validity(Long.toString(f.getUIDValidity()))
                .build();
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public boolean isResolvingSharedFolderPathSupported(String folder) {
        return imapConfig.getCapabilities().hasPermissions();
    }

    @Override
    public String resolveSharedFolderPath(String fullName, int targetUserId) throws OXException {
        if (!imapConfig.getCapabilities().hasPermissions()) {
            return fullName;
        }

        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw MailExceptionCode.NO_ROOT_FOLDER_MODIFY_DELETE.create();
        }

        try {
            IMAPFolderWorker.checkFailFast(imapStore, fullName);
            IMAPFolder imapFolder = getIMAPFolder(fullName);
            if (!doesExist(imapFolder, true)) {
                imapFolder = checkForNamespaceFolder(fullName);
                if (null == imapFolder) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                }
            }

            // Check for user namespace
            String[] userNamespaces = NamespaceFoldersCache.getUserNamespaces(imapStore, true, session, accountId);
            if (null == userNamespaces || userNamespaces.length == 0) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create("IMAP account has no user namespace");
            }
            String namespace = userNamespaces[0];
            if (Strings.isEmpty(namespace)) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create("IMAP account has no user namespace");
            }
            char separator = getSeparator();
            if (namespace.charAt(namespace.length() - 1) ==  separator) {
                namespace = namespace.substring(0, namespace.length() - 1);
            }

            // Determines session user's ACL name
            Entity2ACLArgs entity2AclArgs = IMAPFolderConverter.getEntity2AclArgs(targetUserId, session, imapFolder, imapConfig);
            String aclName = Entity2ACL.getInstance(imapStore, imapConfig).getACLName(session.getUserId(), ctx, entity2AclArgs);

            // Compose the full name from target user point of view
            StringBuilder fullNameBuilder = new StringBuilder(namespace).append(separator).append(aclName);
            if (STR_INBOX.equals(fullName)) {
                return fullNameBuilder.toString();
            }

            String inboxPrefix = new StringBuilder(STR_INBOX).append(separator).toString();
            if (fullName.startsWith(inboxPrefix)) {
                fullName = fullName.substring(inboxPrefix.length());
            }

            return fullNameBuilder.append(separator).append(fullName).toString();
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final IMAPException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public boolean isInfoSupported() throws OXException {
        return true;
    }

    @Override
    public MailFolderInfo getFolderInfo(final String fullName) throws OXException {
        try {
            final String fn = (DEFAULT_FOLDER_ID.equals(fullName) ? "" : fullName);
            boolean ignoreSubscription = this.ignoreSubscriptions;
            final ListLsubEntry entry = ListLsubCache.getCachedLISTEntry(fn, accountId, imapStore, session, ignoreSubscription);
            if (entry.exists()) {
                return toFolderInfo(entry);
            }

            // Retrieve folder...
            IMAPFolderWorker.checkFailFast(imapStore, fullName);
            IMAPFolder f;
            if (0 == fn.length()) {
                f = (IMAPFolder) imapStore.getDefaultFolder();
            } else {
                f = (IMAPFolder) imapStore.getFolder(fullName);
            }

            // ... and check existence
            ListLsubEntry listEntry = ListLsubCache.tryCachedLISTEntry(fullName, accountId, session);
            if (null != listEntry && listEntry.exists()) {
                return toFolderInfo(listEntry);
            }

            ListInfo listInfo = IMAPCommandsCollection.getListInfo(fullName, f);
            if (null == listInfo) {
                f = checkForNamespaceFolder(fn);
                if (null == f) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                }
            }

            return IMAPFolderConverter.convertFolder(f, session, imapAccess, ctx).asMailFolderInfo(accountId);
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public List<MailFolderInfo> getAllFolderInfos(final boolean subscribedOnly) throws OXException {
        return getFolderInfos(null, subscribedOnly);
    }

    @Override
    public List<MailFolderInfo> getFolderInfos(final String optParentFullName, final boolean subscribedOnly) throws OXException {
        try {
            final String pfn = null == optParentFullName ? null : (DEFAULT_FOLDER_ID.equals(optParentFullName) ? "" : optParentFullName);
            final List<ListLsubEntry> allEntries = ListLsubCache.getAllEntries(pfn, accountId, subscribedOnly, imapStore, session, this.ignoreSubscriptions);

            // User's locale
            final Locale locale = (session instanceof ServerSession ? ((ServerSession) session).getUser() : UserStorage.getInstance().getUser(session.getUserId(), session.getContextId())).getLocale();

            // Check whether to consider standard folders
            if (!considerStandardFolders(pfn)) {
                final List<MailFolderInfo> retval = new ArrayList<MailFolderInfo>(allEntries.size());

                // Fill list
                for (final ListLsubEntry entry : allEntries) {
                    final MailFolderInfo mfi = toFolderInfo(entry);
                    retval.add(mfi);
                }

                // Sort & return
                Collections.sort(retval, new FullDisplayNameComparator(locale));
                return retval;
            }

            // ------------------------------------------------------------------------------------------------------ //
            // Fill map
            final int size = allEntries.size();
            final Map<String, MailFolderInfo> map = new HashMap<String, MailFolderInfo>(size);
            for (final ListLsubEntry entry : allEntries) {
                final MailFolderInfo mfi = toFolderInfo(entry);
                map.put(mfi.getFullname(), mfi);
            }

            // Determine standard folders
            {
                StringHelper stringHelper = StringHelper.valueOf(locale);

                {
                    MailFolderInfo mfi = map.get("INBOX");
                    if (null != mfi) {
                        mfi.setDefaultFolder(true);
                        mfi.setDefaultFolderType(DefaultFolderType.INBOX);
                        mfi.setDisplayName(stringHelper.getString(MailStrings.INBOX));
                    }
                }

                int len;
                {
                    // Detect if spam option is enabled
                    UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
                    len = usm.isSpamOptionEnabled() ? 6 : 4;
                }
                for (int index = 0; index < len; index++) {
                    String fn;
                    try {
                        fn = getChecker().getDefaultFolder(index);
                    } catch (Exception e) {
                        LOG.warn("Failed to get standard folder full name for {}", IMAPDefaultFolderChecker.getFallbackName(index), e);
                        fn = null;
                    }
                    if (null != fn) {
                        MailFolderInfo mfi = map.get(fn);
                        if (null != mfi) {
                            mfi.setDefaultFolder(true);

                            switch (index) {
                            case StorageUtility.INDEX_CONFIRMED_HAM:
                                mfi.setDefaultFolderType(DefaultFolderType.CONFIRMED_HAM);
                                mfi.setDisplayName(stringHelper.getString(MailStrings.CONFIRMED_HAM));
                                break;
                            case StorageUtility.INDEX_CONFIRMED_SPAM:
                                mfi.setDefaultFolderType(DefaultFolderType.CONFIRMED_SPAM);
                                mfi.setDisplayName(stringHelper.getString(MailStrings.CONFIRMED_SPAM));
                                break;
                            case StorageUtility.INDEX_DRAFTS:
                                mfi.setDefaultFolderType(DefaultFolderType.DRAFTS);
                                mfi.setDisplayName(stringHelper.getString(MailStrings.DRAFTS));
                                break;
                            case StorageUtility.INDEX_SENT:
                                mfi.setDefaultFolderType(DefaultFolderType.SENT);
                                mfi.setDisplayName(stringHelper.getString(MailStrings.SENT));
                                break;
                            case StorageUtility.INDEX_SPAM:
                                mfi.setDefaultFolderType(DefaultFolderType.SPAM);
                                mfi.setDisplayName(stringHelper.getString(MailStrings.SPAM));
                                break;
                            case StorageUtility.INDEX_TRASH:
                                mfi.setDefaultFolderType(DefaultFolderType.TRASH);
                                mfi.setDisplayName(stringHelper.getString(MailStrings.TRASH));
                                break;
                            default:
                                break;
                            }

                        }
                    }
                }
            }

            // Sort & return
            final List<MailFolderInfo> retval = new ArrayList<MailFolderInfo>(map.values());
            Collections.sort(retval, new FullDisplayNameComparator(locale));
            return retval;
        } catch (final MessagingException e) {
            throw handleMessagingException(e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private boolean considerStandardFolders(final String parentFullName) throws OXException {
        final boolean considerStandardFolders;
        if (null == parentFullName) {
            considerStandardFolders = true;
        } else {
            final String prefix = getDefaultFolderPrefix();
            final int pLength = prefix.length();
            considerStandardFolders = 0 == pLength ? 0 == parentFullName.length() : (parentFullName.equals(prefix.substring(0, pLength - 1)));
        }
        return considerStandardFolders;
    }

    private MailFolderInfo toFolderInfo(final ListLsubEntry entry) {
        final MailFolderInfo mfi = new MailFolderInfo();
        mfi.setAccountId(accountId);
        mfi.setSeparator(entry.getSeparator());
        final String fullName = entry.getFullName();

        mfi.setName(entry.getName());
        mfi.setHoldsFolders(entry.hasInferiors());
        mfi.setHoldsMessages(entry.canOpen());
        mfi.setSubscribed(true);

        if (entry.hasChildren()) {
            mfi.setSubfolders(true);
            mfi.setNumSubfolders(entry.getChildren().size());
        } else {
            mfi.setSubfolders(false);
            mfi.setNumSubfolders(0);
        }

        mfi.setSubscribedSubfolders(entry.hasChildren());

        if (0 == fullName.length()) {
            mfi.setRootFolder(true);
            mfi.setParentFullname(null);
            mfi.setFullname(DEFAULT_FOLDER_ID);
        } else {
            mfi.setDefaultFolder(false);
            mfi.setDefaultFolderType(DefaultFolderType.NONE);
            mfi.setParentFullname(entry.getParent() == null ? null : entry.getParent().getFullName());
            mfi.setFullname(fullName);
        }

        return mfi;
    }

    @Override
    public int[] getTotalAndUnreadCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return new int[] { 0, 0 };
        }
        try {
            IMAPFolderWorker.checkFailFast(imapStore, fullName);

            ListLsubEntry listEntry = ListLsubCache.tryCachedLISTEntry(fullName, accountId, session);
            if (null != listEntry && listEntry.exists()) {
                try {
                    return listEntry.canOpen() ? IMAPCommandsCollection.getTotalAndUnread(imapStore, fullName) : new int[] { 0, 0 };
                } catch (MessagingException e) {
                    return new int[] { 0, 0 };
                }
            }

            ListInfo listInfo = IMAPCommandsCollection.getListInfo(fullName, (IMAPFolder) imapStore.getDefaultFolder());
            if (null == listInfo) {
                IMAPFolder f = checkForNamespaceFolder(fullName);
                if (null == f) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                }
                return new int[] { 0, 0 };
            }

            try {
                return listInfo.canOpen ? IMAPCommandsCollection.getTotalAndUnread(imapStore, fullName) : new int[] { 0, 0 };
            } catch (MessagingException e) {
                return new int[] { 0, 0 };
            }
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public int getUnreadCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return 0;
        }
        try {
            IMAPFolderWorker.checkFailFast(imapStore, fullName);

            ListLsubEntry listEntry = ListLsubCache.tryCachedLISTEntry(fullName, accountId, session);
            if (null != listEntry && listEntry.exists()) {
                try {
                    return listEntry.canOpen() ? IMAPCommandsCollection.getTotalAndUnread(imapStore, fullName)[1] : 0;
                } catch (MessagingException e) {
                    return 0;
                }
            }

            ListInfo listInfo = IMAPCommandsCollection.getListInfo(fullName, (IMAPFolder) imapStore.getDefaultFolder());
            if (null == listInfo) {
                IMAPFolder f = checkForNamespaceFolder(fullName);
                if (null == f) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                }
                return 0;
            }

            try {
                return listInfo.canOpen ? IMAPCommandsCollection.getTotalAndUnread(imapStore, fullName)[1] : 0;
            } catch (MessagingException e) {
                return 0;
            }
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public int getNewCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return 0;
        }
        try {
            IMAPFolderWorker.checkFailFast(imapStore, fullName);

            ListLsubEntry listEntry = ListLsubCache.tryCachedLISTEntry(fullName, accountId, session);
            if (null != listEntry && listEntry.exists()) {
                try {
                    return listEntry.canOpen() ? IMAPCommandsCollection.getRecent(imapStore, fullName) : 0;
                } catch (MessagingException e) {
                    return 0;
                }
            }

            ListInfo listInfo = IMAPCommandsCollection.getListInfo(fullName, (IMAPFolder) imapStore.getDefaultFolder());
            if (null == listInfo) {
                IMAPFolder f = checkForNamespaceFolder(fullName);
                if (null == f) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                }
                return 0;
            }

            try {
                return listInfo.canOpen ? IMAPCommandsCollection.getRecent(imapStore, fullName) : 0;
            } catch (MessagingException e) {
                return 0;
            }
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public int getTotalCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return 0;
        }
        try {
            IMAPFolderWorker.checkFailFast(imapStore, fullName);

            ListLsubEntry listEntry = ListLsubCache.tryCachedLISTEntry(fullName, accountId, session);
            if (null != listEntry && listEntry.exists()) {
                try {
                    return listEntry.canOpen() ? IMAPCommandsCollection.getTotalAndUnread(imapStore, fullName)[0] : 0;
                } catch (MessagingException e) {
                    return 0;
                }
            }

            ListInfo listInfo = IMAPCommandsCollection.getListInfo(fullName, (IMAPFolder) imapStore.getDefaultFolder());
            if (null == listInfo) {
                IMAPFolder f = checkForNamespaceFolder(fullName);
                if (null == f) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                }
                return 0;
            }

            try {
                return listInfo.canOpen ? IMAPCommandsCollection.getTotalAndUnread(imapStore, fullName)[0] : 0;
            } catch (MessagingException e) {
                return 0;
            }
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public boolean exists(final String fullName) throws OXException {
        try {
            if (DEFAULT_FOLDER_ID.equals(fullName) || STR_INBOX.equals(fullName)) {
                return true;
            }

            ListLsubEntry listEntry = ListLsubCache.tryCachedLISTEntry(fullName, accountId, session);
            if (null != listEntry && listEntry.exists()) {
                return true;
            }

            if (null != IMAPCommandsCollection.getListInfo(fullName, (IMAPFolder) imapStore.getDefaultFolder())) {
                return true;
            }

            if (checkForNamespaceFolder(fullName) != null) {
                return true;
            }

            return false;
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public MailFolder getFolder(final String fullName) throws OXException {
        return FolderUtility.loadFolder(fullName, this);
    }

    /**
     * Gets the personal namespace folder.
     *
     * @return The personal namespace folder or <code>null</code>
     * @throws OXException If operation fails
     */
    public String getPersonalNamespace() throws OXException {
        try {
            if (!imapConfig.getImapCapabilities().hasNamespace()) {
                return null;
            }

            String[] personalNamespaces = NamespaceFoldersCache.getPersonalNamespaces(imapStore, true, session, accountId);
            return null == personalNamespaces || 0 == personalNamespaces.length ? null : personalNamespaces[0];
        } catch (final MessagingException e) {
            throw handleMessagingException(e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public MailFolder[] getSubfolders(final String parentFullName, final boolean all) throws OXException {
        try {
            IMAPFolderWorker.checkFailFast(imapStore, parentFullName);
            if (DEFAULT_FOLDER_ID.equals(parentFullName)) {
                final IMAPFolder parent = (IMAPFolder) imapStore.getDefaultFolder();
                final boolean subscribed = (!MailProperties.getInstance().isIgnoreSubscription() && !all);
                /*
                 * Request subfolders the usual way
                 */
                final List<ListLsubEntry> subfolders;
                /*
                 * Obtain folder lock once to avoid multiple acquire/releases when invoking folder's getXXX() methods
                 */
                final List<String> additionalFullNames = new ArrayList<String>(4);
                synchronized (parent) {
                    subfolders = new ArrayList<ListLsubEntry>();
                    {
                        List<ListLsubEntry> children;
                        if (subscribed) {
                            children = getLSUBEntry("", parent).getChildren();
                        } else {
                            children = getLISTEntry("", parent).getChildren();
                        }
                        subfolders.addAll(children);
                        boolean containsInbox = false;
                        for (int i = 0; i < children.size() && !containsInbox; i++) {
                            containsInbox = STR_INBOX.equals(children.get(i).getFullName());
                        }
                        if (!containsInbox) {
                            /*
                             * Add folder INBOX manually
                             */
                            subfolders.add(0, getLISTEntry(STR_INBOX, parent));
                        }
                    }
                    if (imapConfig.getImapCapabilities().hasNamespace()) {
                        /*
                         * Merge with namespace folders
                         */
                        {
                            final String[] personalNamespaces =
                                NamespaceFoldersCache.getPersonalNamespaces(imapStore, true, session, accountId);
                            if (null == personalNamespaces || 1 != personalNamespaces.length || !STR_INBOX.equals(personalNamespaces[0])) {
                                /*
                                 * Personal namespace(s) does not only consist of INBOX folder
                                 */
                                mergeWithNamespaceFolders(subfolders, personalNamespaces, subscribed, parent, additionalFullNames);
                            }
                        }
                        {
                            mergeWithNamespaceFolders(
                                subfolders,
                                NamespaceFoldersCache.getUserNamespaces(imapStore, true, session, accountId),
                                subscribed,
                                parent,
                                additionalFullNames);
                        }
                        {
                            mergeWithNamespaceFolders(
                                subfolders,
                                NamespaceFoldersCache.getSharedNamespaces(imapStore, true, session, accountId),
                                subscribed,
                                parent,
                                additionalFullNames);
                        }
                    }
                }
                /*
                 * Output subfolders
                 */
                final List<MailFolder> list =
                    new ArrayList<MailFolder>(subfolders.size() + (additionalFullNames.isEmpty() ? 0 : additionalFullNames.size()));
                for (final ListLsubEntry subfolder : subfolders) {
                    final String subfolderFullName = subfolder.getFullName();
                    try {
                        list.add(FolderUtility.loadFolder(subfolderFullName, this));
                    } catch (final OXException e) {
                        if (MimeMailExceptionCode.FOLDER_NOT_FOUND.getNumber() != e.getCode()) {
                            throw e;
                        }
                        /*
                         * Obviously folder does (no more) exist
                         */
                        ListLsubCache.removeCachedEntry(subfolderFullName, accountId, session);
                        RightsCache.removeCachedRights(subfolderFullName, session, accountId);
                        UserFlagsCache.removeUserFlags(subfolderFullName, session, accountId);
                    }
                }
                if (!additionalFullNames.isEmpty()) {
                    for (final String fn : additionalFullNames) {
                        final MailFolder namespaceFolder = namespaceFolderFor(fn, parent, subscribed);
                        if (null != namespaceFolder) {
                            list.add(namespaceFolder);
                        }
                    }
                }
                return list.toArray(new MailFolder[list.size()]);
            }
            IMAPFolder parent = getIMAPFolder(parentFullName);
            final ListLsubEntry parentEntry = getLISTEntry(parentFullName, parent);
            /*
             * Obtain folder lock once to avoid multiple acquire/releases when invoking folder's getXXX() methods
             */
            if (doesExist(parentEntry)) {
                /*
                 * Holds LOOK-UP right?
                 */
                if (imapConfig.isSupportsACLs() && parentEntry.canOpen()) {
                    try {
                        if (!imapConfig.getACLExtension().canLookUp(RightsCache.getCachedRights(parent, true, session, accountId))) {
                            throw IMAPException.create(IMAPException.Code.NO_LOOKUP_ACCESS, imapConfig, session, parentFullName);
                        }
                    } catch (final MessagingException e) {
                        if (!startsWithNamespaceFolder(parentFullName, parentEntry.getSeparator())) {
                            throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, parentFullName);
                        }
                    }
                }
                return getSubfolderArray(all, parent);
            }
            /*
             * Check for namespace folder
             */
            parent = checkForNamespaceFolder(parentFullName);
            if (null != parent) {
                return getSubfolderArray(all, parent);
            }
            return EMPTY_PATH;
        } catch (final MessagingException e) {
            throw handleMessagingException(parentFullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private MailFolder namespaceFolderFor(final String fn, final IMAPFolder parent, final boolean subscribed) throws OXException, MessagingException {
        final ListLsubEntry listEntry = getLISTEntry(fn, parent);
        if (null == listEntry || !listEntry.exists() || (subscribed ? !listEntry.isSubscribed() : false)) {
            return null;
        }
        final IMAPMailFolder mailFolder = new IMAPMailFolder();
        mailFolder.setRootFolder(false);
        mailFolder.setExists(true);
        mailFolder.setSeparator(listEntry.getSeparator());
        mailFolder.setFullname(fn);
        mailFolder.setName(listEntry.getName());
        mailFolder.setHoldsMessages(listEntry.canOpen());
        mailFolder.setHoldsFolders(listEntry.hasInferiors());
        mailFolder.setNonExistent(false);
        mailFolder.setShared(true);
        mailFolder.setSubfolders(listEntry.hasChildren());
        mailFolder.setSubscribed(listEntry.isSubscribed());
        mailFolder.setSubscribedSubfolders(ListLsubCache.hasAnySubscribedSubfolder(fn, accountId, parent, session, this.ignoreSubscriptions));
        mailFolder.setParentFullname(null);
        mailFolder.setDefaultFolder(false);
        mailFolder.setDefaultFolderType(DefaultFolderType.NONE);
        {
            final DefaultMailPermission perm = new DefaultMailPermission();
            perm.setAllPermission(0,0,0,0);
            perm.setFolderAdmin(false);
            perm.setEntity(session.getUserId());
            perm.setGroupPermission(false);
            mailFolder.setOwnPermission(perm);
            mailFolder.addPermission(perm);
        }
        mailFolder.setMessageCount(-1);
        mailFolder.setNewMessageCount(-1);
        mailFolder.setUnreadMessageCount(-1);
        mailFolder.setDeletedMessageCount(-1);
        mailFolder.setSupportsUserFlags(false);
        return mailFolder;
    }

    private MailFolder[] getSubfolderArray(final boolean all, final IMAPFolder parent) throws MessagingException, OXException {
        final boolean subscribed = !MailProperties.getInstance().isIgnoreSubscription() && !all;
        final List<ListLsubEntry> subfolders;
        {
            ListLsubEntry entry = subscribed ? getLSUBEntry(parent) : getLISTEntry(parent);
            subfolders = new ArrayList<ListLsubEntry>(entry.getChildren());
        }
        /*
         * Merge with namespace folders if NAMESPACE capability is present
         */
        final List<String> additionalFullNames = new ArrayList<String>(4);
        if (imapConfig.getImapCapabilities().hasNamespace()) {

            mergeWithNamespaceFolders(subfolders, NamespaceFoldersCache.getPersonalNamespaces(imapStore, true, session, accountId), subscribed, parent, additionalFullNames);

            mergeWithNamespaceFolders(subfolders, NamespaceFoldersCache.getUserNamespaces(imapStore, true, session, accountId), subscribed, parent, additionalFullNames);

            mergeWithNamespaceFolders(subfolders, NamespaceFoldersCache.getSharedNamespaces(imapStore, true, session, accountId), subscribed, parent, additionalFullNames);

        }
        /*
         * Convert to MailFolder instances
         */
        final List<MailFolder> list = new ArrayList<MailFolder>(subfolders.size() + (additionalFullNames.isEmpty() ? 0 : additionalFullNames.size()));
        for (final ListLsubEntry current : subfolders) {
            final MailFolder mailFolder = FolderUtility.loadFolder(current.getFullName(), this);
            if (mailFolder.exists()) {
                list.add(mailFolder);
            }
        }
        if (!additionalFullNames.isEmpty()) {
            for (final String fn : additionalFullNames) {
                list.add(FolderUtility.loadFolder(fn, this));
            }
        }
        return list.toArray(new MailFolder[list.size()]);
    }

    private void mergeWithNamespaceFolders(final List<ListLsubEntry> subfolders, final String[] namespaces, final boolean subscribed, final IMAPFolder parent, final List<String> additionalFullNames) throws MessagingException, OXException {
        if (null == namespaces || namespaces.length == 0) {
            return;
        }
        final String[] namespaceFolders = new String[namespaces.length];
        System.arraycopy(namespaces, 0, namespaceFolders, 0, namespaces.length);
        final char sep = getSeparator(parent);
        final String parentFullname = parent.getFullName();
        final boolean isRoot = (0 == parentFullname.length());
        NextNSFolder: for (int i = 0; i < namespaceFolders.length; i++) {
            final String nsFullname = namespaceFolders[i];
            if ((nsFullname == null) || (nsFullname.length() == 0)) {
                namespaceFolders[i] = null;
                continue NextNSFolder;
            }
            /*
             * Check if namespace folder's prefix matches parent full name ; e.g "INBOX" or "INBOX/#shared"
             */
            final int pos = nsFullname.lastIndexOf(sep);
            if (pos > 0) { // Located below other folder than root
                if (!nsFullname.substring(0, pos).equals(parentFullname)) {
                    namespaceFolders[i] = null;
                    continue NextNSFolder;
                }
            } else if (!isRoot) { // Should be located below root
                namespaceFolders[i] = null;
                continue NextNSFolder;
            }
            /*
             * Check if already contained in passed list
             */
            for (final ListLsubEntry subfolder : subfolders) {
                if (nsFullname.equals(subfolder.getFullName())) {
                    /*
                     * Namespace folder already contained in subfolder list
                     */
                    namespaceFolders[i] = null;
                    continue NextNSFolder;
                }
            }
        }
        if (subscribed) {
            /*
             * Remove not-subscribed namespace folders
             */
            for (int i = 0; i < namespaceFolders.length; i++) {
                final String nsFullname = namespaceFolders[i];
                if (nsFullname != null && !IMAPCommandsCollection.isSubscribed(nsFullname, sep, true, parent)) {
                    namespaceFolders[i] = null;
                }
            }
        }
        /*
         * Add remaining namespace folders to subfolder list
         */
        for (final String fullName : namespaceFolders) {
            if (fullName != null) {
                additionalFullNames.add(fullName);
                // subfolders.add(new NamespaceFolder(imapStore, fullName, sep));
            }
        }
    }

    /**
     * Checks if given full name matches a namespace folder
     *
     * @param fullName The folder's full name
     * @return The corresponding namespace folder or <code>null</code>
     * @throws MessagingException If operation fails
     */
    public IMAPFolder checkForNamespaceFolder(final String fullName) throws MessagingException {
        return checkForNamespaceFolder0(fullName, null);
    }

    /**
     * Checks if given full name matches a namespace folder or one of its subfolders.
     *
     * @param fullName The folder's full name
     * @param folder The folder associated with the full name
     * @return The corresponding namespace folder or <code>null</code>
     * @throws MessagingException If operation fails
     */
    public IMAPFolder checkForNamespaceFolder(final String fullName, IMAPFolder folder) throws MessagingException {
        if (null == folder) {
            throw new MessagingException("IMAP folder must not be null");
        }
        return checkForNamespaceFolder0(fullName, folder);
    }

    private IMAPFolder checkForNamespaceFolder0(final String fullName, IMAPFolder folder) throws MessagingException {
        if (NamespaceFoldersCache.containedInPersonalNamespaces(fullName, imapStore, true, session, accountId)) {
            return new NamespaceFolder(imapStore, fullName, getSeparator());
        }
        if (NamespaceFoldersCache.containedInUserNamespaces(fullName, imapStore, true, session, accountId)) {
            return new NamespaceFolder(imapStore, fullName, getSeparator());
        }
        if (NamespaceFoldersCache.containedInSharedNamespaces(fullName, imapStore, true, session, accountId)) {
            return new NamespaceFolder(imapStore, fullName, getSeparator());
        }
        return null == folder ? null : startsWithNamespaceFolder(fullName, getSeparator()) ? folder : null;
    }

    @Override
    public MailFolder getRootFolder() throws OXException {
        return FolderUtility.loadFolder(MailFolder.DEFAULT_FOLDER_ID, this);
    }

    @Override
    public void checkDefaultFolders() throws OXException {
        getChecker().checkDefaultFolders();
    }

    @Override
    public Map<String, String> getSpecialUseFolder() throws OXException {
        try {
            Map<String, String> retval = new HashMap<String, String>(5);
            IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(IMAPDefaultFolderChecker.INBOX);
            Collection<ListLsubEntry> entries = null;
            entries = ListLsubCache.getSentEntry(-1, imapFolder, session, imapAccess.getIMAPConfig().getIMAPProperties().isIgnoreSubscription());
            if (!entries.isEmpty()) {
                retval.put(SENT, entries.toArray(new ListLsubEntry[entries.size()])[0].getName());
            }
            entries = ListLsubCache.getDraftsEntry(-1, imapFolder, session, imapAccess.getIMAPConfig().getIMAPProperties().isIgnoreSubscription());
            if (!entries.isEmpty()) {
                retval.put(DRAFTS, entries.toArray(new ListLsubEntry[entries.size()])[0].getName());
            }
            entries = ListLsubCache.getJunkEntry(-1, imapFolder, session, imapAccess.getIMAPConfig().getIMAPProperties().isIgnoreSubscription());
            if (!entries.isEmpty()) {
                retval.put(SPAM, entries.toArray(new ListLsubEntry[entries.size()])[0].getName());
            }
            entries = ListLsubCache.getTrashEntry(-1, imapFolder, session, imapAccess.getIMAPConfig().getIMAPProperties().isIgnoreSubscription());
            if (!entries.isEmpty()) {
                retval.put(TRASH, entries.toArray(new ListLsubEntry[entries.size()])[0].getName());
            }
            entries = ListLsubCache.getArchiveEntry(-1, imapFolder, session, imapAccess.getIMAPConfig().getIMAPProperties().isIgnoreSubscription());
            if (!entries.isEmpty()) {
                retval.put(ARCHIVE, entries.toArray(new ListLsubEntry[entries.size()])[0].getName());
            }
            return retval;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e, imapConfig, session);
        }
    }

    private static final int FOLDER_TYPE = (Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS);

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws OXException {
        final String name = toCreate.getName();
        if (isEmpty(name)) {
            throw MailExceptionCode.INVALID_FOLDER_NAME_EMPTY.create();
        }
        if (name.length() > maxMailboxNameLength()) {
            throw MailExceptionCode.INVALID_FOLDER_NAME_TOO_LONG.create(Integer.valueOf(maxMailboxNameLength()));
        }
        {
            char sep = toCreate.getSeparator();
            if (sep <= '\0') {
                sep = getSeparator(imapStore);
                toCreate.setSeparator(sep);
            }
        }
        boolean created = false;
        IMAPFolder createMe = null;
        boolean subscribed = false;
        try {
            /*
             * Insert
             */
            String parentFullname = toCreate.getParentFullname();
            final boolean isParentDefault;
            IMAPFolder parent;
            if (DEFAULT_FOLDER_ID.equals(parentFullname)) {
                parent = (IMAPFolder) imapStore.getDefaultFolder();
                parentFullname = "";
                isParentDefault = true;
            } else {
                if (toCreate.containsSeparator() && !checkFolderPathValidity(parentFullname, toCreate.getSeparator())) {
                    throw IMAPException.create(IMAPException.Code.INVALID_FOLDER_NAME, imapConfig, session, toCreate.getName(), invalidCharsString(toCreate.getSeparator()));
                }
                parent = getIMAPFolder(parentFullname);
                isParentDefault = false;
            }
            /*
             * Obtain folder lock once to avoid multiple acquire/releases when invoking folder's getXXX() methods
             */
            synchronized (parent) {
                ListLsubEntry parentEntry = ListLsubCache.getCachedLISTEntry(parentFullname, accountId, parent, session, ignoreSubscriptions);
                if (parentEntry.exists()) {
                    /*
                     * Check if parent holds folders
                     */
                    if (false == parentEntry.hasInferiors()) {
                        throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS, imapConfig, session, isParentDefault ? DEFAULT_FOLDER_ID : parentFullname);
                    }
                } else {
                    if (!doesExist(parent, true)) {
                        ListInfo listInfo = IMAPCommandsCollection.getListInfo(parentFullname, parent);
                        if (null == listInfo) {
                            parent = checkForNamespaceFolder(parentFullname);
                            if (null == parent) {
                                throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, parentFullname);
                            }
                        } else if (false == listInfo.hasInferiors) {
                            throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS, imapConfig, session, isParentDefault ? DEFAULT_FOLDER_ID : parentFullname);
                        }
                    }
                }
                /*
                 * Check ACLs if enabled
                 */
                if (imapConfig.isSupportsACLs()) {
                    try {
                        if (isParentDefault) {
                            if (!(RootSubfoldersEnabledCache.isRootSubfoldersEnabled(imapConfig, (DefaultFolder) parent))) {
                                throw IMAPException.create(IMAPException.Code.NO_CREATE_ACCESS, imapConfig, session, DEFAULT_FOLDER_ID);
                            }
                        } else {
                            if (!imapConfig.getACLExtension().canCreate(RightsCache.getCachedRights(parent, true, session, accountId))) {
                                throw IMAPException.create(IMAPException.Code.NO_CREATE_ACCESS, imapConfig, session, parentFullname);
                            }
                        }
                    } catch (final MessagingException e) {
                        /*
                         * MYRIGHTS command failed for given mailbox
                         */
                        if (!imapConfig.getImapCapabilities().hasNamespace() || !NamespaceFoldersCache.containedInPersonalNamespaces(parentFullname, imapStore, true, session, accountId)) {
                            /*
                             * No namespace support or given parent is NOT covered by user's personal namespaces.
                             */
                            throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, isParentDefault ? DEFAULT_FOLDER_ID : parentFullname);
                        }
                        LOG.debug("MYRIGHTS command failed on namespace folder", e);
                    }
                }
                /*
                 * Check if IMAP server is in MBox format; meaning folder either hold messages or subfolders but not both
                 */
                final char separator = getSeparator(parent);
                final boolean mboxEnabled =
                    MBoxEnabledCache.isMBoxEnabled(imapConfig, parent, new StringBuilder(parent.getFullName()).append(separator).toString());
                if (!checkFolderNameValidity(name, separator, mboxEnabled)) {
                    throw IMAPException.create(IMAPException.Code.INVALID_FOLDER_NAME, imapConfig, session, name, invalidCharsString(separator));
                }
                if (isParentDefault) {
                    /*
                     * Below default folder
                     */
                    createMe = getIMAPFolder(name);
                } else {
                    createMe =
                        (IMAPFolder) imapStore.getFolder(new StringBuilder(parent.getFullName()).append(separator).append(name).toString());
                }
                /*
                 * Obtain folder lock once to avoid multiple acquire/releases when invoking folder's getXXX() methods
                 */
                synchronized (createMe) {
                    int ftype = mboxEnabled ? getNameOf(createMe).endsWith(String.valueOf(separator)) ? Folder.HOLDS_FOLDERS : Folder.HOLDS_MESSAGES : FOLDER_TYPE;
                    try {
                        if (!(created = createMe.create(ftype))) {
                            throw IMAPException.create(IMAPException.Code.FOLDER_CREATION_FAILED, imapConfig, session, createMe.getFullName(), isParentDefault ? DEFAULT_FOLDER_ID : parent.getFullName());
                        }
                    } catch (final MessagingException e) {
                        if (!"Unsupported type".equals(e.getMessage())) {
                            if (e.getNextException() instanceof com.sun.mail.iap.BadCommandException) {
                                // Bad input for associated IMAP server
                                throw IMAPException.create(IMAPException.Code.FOLDER_CREATION_FAILED, imapConfig, session, e, createMe.getFullName(), isParentDefault ? DEFAULT_FOLDER_ID : parent.getFullName());
                            }

                            throw handleMessagingException(createMe.getFullName(), e);
                        }
                        LOG.warn("IMAP folder creation failed due to unsupported type. Going to retry with fallback type HOLDS-MESSAGES.", e);
                        if (!(created = createMe.create(Folder.HOLDS_MESSAGES))) {
                            throw IMAPException.create(IMAPException.Code.FOLDER_CREATION_FAILED, imapConfig, session, e, createMe.getFullName(), isParentDefault ? DEFAULT_FOLDER_ID : parent.getFullName());
                        }
                        LOG.info("IMAP folder created with fallback type HOLDS_MESSAGES");
                    }
                    /*
                     * Subscribe
                     */
                    if (!MailProperties.getInstance().isSupportSubscription()) {
                        subscribed = true;
                    } else if (toCreate.containsSubscribed()) {
                        subscribed = toCreate.isSubscribed();
                    } else {
                        subscribed = true;
                    }
                    IMAPCommandsCollection.forceSetSubscribed(imapStore, createMe.getFullName(), subscribed);
                    /*
                     * Apply ACLs if supported by IMAP server
                     */
                    if (imapConfig.isSupportsACLs() && toCreate.containsPermissions()) {
                        final ACL[] initialACLs = getACLSafe(createMe);
                        if (initialACLs != null) {
                            ACL[] newACLs = permissions2ACL(toCreate.getPermissions(), createMe);
                            Entity2ACL entity2ACL = getEntity2ACL();
                            Entity2ACLArgs args = IMAPFolderConverter.getEntity2AclArgs(session, createMe, imapConfig);
                            Map<String, ACL> m = acl2map(newACLs);
                            if (!equals(initialACLs, m, entity2ACL, args)) {
                                ACLExtension aclExtension = imapConfig.getACLExtension();
                                if (aclExtension.canSetACL(createMe.myRights())) {
                                    // Check for admin
                                    {
                                        boolean adminFound = false;
                                        for (int i = 0; (i < newACLs.length) && !adminFound; i++) {
                                            if (aclExtension.canSetACL(newACLs[i].getRights())) {
                                                adminFound = true;
                                            }
                                        }
                                        if (!adminFound) {
                                            throw IMAPException.create(IMAPException.Code.NO_ADMIN_ACL, imapConfig, session, createMe.getFullName());
                                        }
                                    }
                                    /*
                                     * Apply new ACLs
                                     */
                                    List<ACL> validatedACL = new ArrayList<ACL>(initialACLs.length);
                                    final Map<String, ACL> om = acl2map(initialACLs);
                                    for (int i = 0; i < newACLs.length; i++) {
                                        ACL validated = validate(newACLs[i], om);
                                        if (null != validated) {
                                            createMe.addACL(validated);
                                            validatedACL.add(validated);
                                        }
                                    }
                                    /*
                                     * Remove other ACLs
                                     */
                                    ACL[] removedACLs = getRemovedACLs(m, initialACLs);
                                    if (removedACLs.length > 0) {
                                        for (int i = 0; i < removedACLs.length; i++) {
                                            if (isKnownEntity(removedACLs[i].getName(), entity2ACL, ctx, args)) {
                                                createMe.removeACL(removedACLs[i].getName());
                                            }
                                        }
                                    }
                                    // Affected users, too
                                    dropListLsubCachesForOther(validatedACL.toArray(new ACL[validatedACL.size()]));
                                } else {
                                    /*
                                     * Add a warning
                                     */
                                    imapAccess.addWarnings(Collections.<OXException> singletonList(IMAPException.create(IMAPException.Code.NO_ADMINISTER_ACCESS_ON_INITIAL, imapConfig, session, createMe.getFullName())));
                                    // Affected users, too
                                    dropListLsubCachesForOther(initialACLs);
                                }
                            } else {
                                // Affected users, too
                                dropListLsubCachesForOther(initialACLs);
                            }
                        }
                    }
                    return createMe.getFullName();
                }
            }
        } catch (final MessagingException e) {
            if (createMe != null && created) {
                try {
                    if (doesExist(createMe, false)) {
                        createMe.delete(true);
                        created = false;
                    }
                } catch (final Throwable e2) {
                    LOG.error("Temporary created IMAP folder \"{}could not be deleted", createMe.getFullName(),
                        e2);
                }
            }

            if (MimeMailException.isExistsException(e)) {
                // Assume outdated cache as client expected that such a folder does not exist
                ListLsubCache.clearCache(accountId, session);
                throw IMAPException.create(IMAPException.Code.DUPLICATE_FOLDER, imapConfig, session, name);
            }

            throw handleMessagingException(e);
        } catch (OXException e) {
            /*
             * No folder deletion on IMAP error "NO_ADMINISTER_ACCESS_ON_INITIAL"
             */
            if (e.isPrefix("MSG") && IMAPException.Code.NO_ADMINISTER_ACCESS_ON_INITIAL.getNumber() != e.getCode()) {
                if (createMe != null && created) {
                    try {
                        if (doesExist(createMe, false)) {
                            createMe.delete(true);
                            created = false;
                        }
                    } catch (final Throwable e2) {
                        LOG.error("Temporary created IMAP folder \"{}\" could not be deleted", createMe.getFullName(),
                            e2);
                    }
                }
            }
            throw e;
        } catch (Exception e) {
            if (createMe != null && created) {
                try {
                    if (doesExist(createMe, false)) {
                        createMe.delete(true);
                        created = false;
                    }
                } catch (final Throwable e2) {
                    LOG.error("Temporary created IMAP folder \"{}\" could not be deleted", createMe.getFullName(),
                        e2);
                }
            }
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (createMe != null) {
                if (created) {
                    try {
                        boolean ignoreSubscription = this.ignoreSubscriptions;
                        ListLsubCache.addSingle(createMe, subscribed, accountId, session, ignoreSubscription);
                    } catch (Exception e) {
                        // Updating LIST/LSUB cache failed
                        ListLsubCache.clearCache(accountId, session);
                    } finally {
                        closeSafe(createMe);
                    }
                } else {
                    closeSafe(createMe);
                }
            }
        }
    }

    private Entity2ACL getEntity2ACL() throws OXException {
        return Entity2ACL.getInstance(imapStore, imapConfig);
    }

    @Override
    public String renameFolder(final String fullName, final String newName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            final String name;
            if (accountId == MailAccount.DEFAULT_ID) {
                name = MailFolder.DEFAULT_FOLDER_NAME;
            } else {
                final MailAccountStorageService mass = Services.getService(MailAccountStorageService.class);
                if (null == mass) {
                    name = MailFolder.DEFAULT_FOLDER_NAME;
                } else {
                    name = mass.getMailAccount(accountId, session.getUserId(), session.getContextId()).getName();
                }

            }
            throw IMAPException.create(IMAPException.Code.NO_RENAME_ACCESS, imapConfig, session, name);
        }
        try {
            IMAPFolder renameMe = getIMAPFolder(fullName);
            char separator;
            boolean canOpen;
            ListLsubEntry renameEntry = ListLsubCache.tryCachedLISTEntry(fullName, accountId, session);
            if (null == renameEntry || !renameEntry.exists()) {
                ListInfo listInfo = IMAPCommandsCollection.getListInfo(fullName, renameMe);
                if (null == listInfo) {
                    renameMe = checkForNamespaceFolder(fullName);
                    if (null == renameMe) {
                        throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                    }
                    throw IMAPException.create(IMAPException.Code.NO_ADMINISTER_ACCESS, imapConfig, session, fullName);
                }
                separator = listInfo.separator;
                canOpen = listInfo.canOpen;
            } else {
                separator = renameEntry.getSeparator();
                canOpen = renameEntry.canOpen();
            }
            /*
             * Obtain folder lock once to avoid multiple acquire/releases when invoking folder's getXXX() methods
             */
            synchronized (renameMe) {
                if (imapConfig.isSupportsACLs() && canOpen) {
                    try {
                        if (!imapConfig.getACLExtension().canCreate(RightsCache.getCachedRights(renameMe, true, session, accountId))) {
                            throw IMAPException.create(IMAPException.Code.NO_RENAME_ACCESS, imapConfig, session, fullName);
                        }
                    } catch (final MessagingException e) {
                        /*
                         * MYRIGHTS command failed for given mailbox
                         */
                        throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, fullName);
                    }
                }
                if (getChecker().isDefaultFolder(fullName, true)) {
                    throw IMAPException.create(IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE, imapConfig, session, fullName);
                }
                /*
                 * Notify message storage about outstanding rename
                 */
                imapAccess.getMessageStorage().notifyIMAPFolderModification(fullName);
                if (isEmpty(newName)) {
                    throw MailExceptionCode.INVALID_FOLDER_NAME_EMPTY.create();
                } else if (newName.indexOf(separator) != -1) {
                    throw MailExceptionCode.INVALID_FOLDER_NAME2.create(newName, Character.toString(separator));
                } else if (newName.length() > maxMailboxNameLength()) {
                    throw MailExceptionCode.INVALID_FOLDER_NAME_TOO_LONG.create(Integer.valueOf(maxMailboxNameLength()));
                }
                /*-
                 * Perform rename operation
                 *
                 * Rename can only be invoked on a closed folder
                 */
                if (renameMe.isOpen()) {
                    renameMe.close(false);
                }
                final boolean mboxEnabled;
                final IMAPFolder renameFolder;
                {
                    ListLsubEntry parentEntry = null == renameEntry ? null : renameEntry.getParent();
                    final String parentFullName = null == parentEntry ? renameMe.getParent().getFullName() : parentEntry.getFullName();
                    final StringBuilder tmp = new StringBuilder();
                    if (parentFullName.length() > 0) {
                        tmp.append(parentFullName).append(separator);
                    }
                    tmp.append(newName);
                    renameFolder = (IMAPFolder) imapStore.getFolder(tmp.toString());
                    /*
                     * Check for MBox
                     */
                    mboxEnabled = MBoxEnabledCache.isMBoxEnabled(imapConfig, renameFolder, new StringBuilder(parentFullName).append(separator).toString());
                }
                ListLsubEntry testEntry = ListLsubCache.optCachedLISTEntry(renameFolder.getFullName(), accountId, renameFolder, session, ignoreSubscriptions);
                if (testEntry.exists()) {
                    throw IMAPException.create(IMAPException.Code.DUPLICATE_FOLDER, imapConfig, session, renameFolder.getFullName());
                }
                if (!checkFolderNameValidity(newName, separator, mboxEnabled)) {
                    throw IMAPException.create(IMAPException.Code.INVALID_FOLDER_NAME, imapConfig, session, newName, invalidCharsString(separator));
                }
                /*
                 * Remember subscription status
                 */
                Map<String, Boolean> subscriptionStatus;
                final String newFullName = renameFolder.getFullName();
                final String oldFullName = renameMe.getFullName();
                try {
                    subscriptionStatus = getSubscriptionStatus(renameMe, separator, oldFullName, newFullName);
                } catch (final MessagingException e) {
                    LOG.warn("Subscription status of folder \"{}\" and its subfolders could not be stored prior to rename operation", renameMe.getFullName());
                    subscriptionStatus = null;
                }
                removeSessionData(renameMe.getFullName(), separator, renameMe);
                /*
                 * Unsubscribe sub-tree
                 */
                if (null != subscriptionStatus && subscriptionStatus.size() > 1) {
                    setFolderSubscription(renameMe.getFullName(), renameMe, false);
                } else {
                    IMAPCommandsCollection.forceSetSubscribed(imapStore, renameMe.getFullName(), false);
                }
                /*
                 * Rename
                 */
                boolean success = false;
                try {
                    if (renameMe.isOpen()) {
                        renameMe.close(false);
                    }

                    long start = System.currentTimeMillis();
                    IMAPCommandsCollection.renameFolder(renameMe, separator, renameFolder);
                    long duration = System.currentTimeMillis() - start;
                    success = true;
                    mailInterfaceMonitor.addUseTime(duration);
                } catch (final MessagingException e) {
                    /*
                     * Rename failed
                     */
                    throw IMAPException.create(IMAPException.Code.RENAME_FAILED, imapConfig, session, e, renameMe.getFullName(), newFullName, e.getMessage());
                } finally {
                    if (!success) {
                        setFolderSubscription(renameMe.getFullName(), renameMe, true);
                    }
                }
                /*
                 * Success?
                 */
                if (!success) {
                    throw IMAPException.create(IMAPException.Code.RENAME_FAILED, imapConfig, session, renameMe.getFullName(), newFullName, "<not-available>");
                }
                renameMe = getIMAPFolder(newFullName);
                /*
                 * Apply remembered subscription status
                 */
                if (subscriptionStatus == null) {
                    /*
                     * At least subscribe to renamed folder
                     */
                    IMAPCommandsCollection.forceSetSubscribed(imapStore, renameMe.getFullName(), true);
                } else {
                    if (subscriptionStatus.size() > 1) {
                        applySubscriptionStatus(renameMe.getFullName(), separator, renameMe, subscriptionStatus);
                    } else {
                        boolean subscribe = subscriptionStatus.values().iterator().next().booleanValue();
                        IMAPCommandsCollection.forceSetSubscribed(imapStore, renameMe.getFullName(), subscribe);
                    }
                }
                /*
                 * Return new full name
                 */
                return renameMe.getFullName();
            }
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final IMAPException e) {
            throw e;
        } catch (final OXException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        } finally {
            ListLsubCache.clearCache(accountId, session);
        }
    }

    @Override
    public String moveFolder(final String fullName, final String newFullname) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName) || DEFAULT_FOLDER_ID.equals(newFullname)) {
            throw IMAPException.create(IMAPException.Code.NO_ROOT_MOVE, imapConfig, session, new Object[0]);
        }
        try {
            if (DEFAULT_FOLDER_ID.equals(fullName)) {
                throw MailExceptionCode.NO_ROOT_FOLDER_MODIFY_DELETE.create();
            }
            IMAPFolderWorker.checkFailFast(imapStore, fullName);
            IMAPFolder moveMe = getIMAPFolder(fullName);
            char separator;
            ListLsubEntry moveMeEntry = ListLsubCache.tryCachedLISTEntry(fullName, accountId, session);
            if (null == moveMeEntry || !moveMeEntry.exists()) {
                ListInfo listInfo = IMAPCommandsCollection.getListInfo(fullName, moveMe);
                if (null == listInfo) {
                    moveMe = checkForNamespaceFolder(fullName);
                    if (null == moveMe) {
                        throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                    }
                    throw IMAPException.create(IMAPException.Code.NO_ADMINISTER_ACCESS, imapConfig, session, fullName);
                }
                separator = listInfo.separator;
            } else {
                separator = moveMeEntry.getSeparator();
            }
            ListLsubCache.clearCache(accountId, session);
            /*
             * Obtain folder lock once to avoid multiple acquire/releases when invoking folder's getXXX() methods
             */
            synchronized (moveMe) {
                /*
                 * Notify message storage about outstanding move
                 */
                imapAccess.getMessageStorage().notifyIMAPFolderModification(fullName);
                final String oldParent = moveMe.getParent().getFullName();
                final String newParent;
                final String newName;
                {
                    final int pos = newFullname.lastIndexOf(separator);
                    if (pos == -1) {
                        newParent = "";
                        newName = newFullname;
                    } else {
                        if (pos == newFullname.length() - 1) {
                            throw IMAPException.create(IMAPException.Code.INVALID_FOLDER_NAME, imapConfig, session, newFullname, invalidCharsString(separator));
                        }
                        newParent = newFullname.substring(0, pos);
                        newName = newFullname.substring(pos + 1);
                        if (!checkFolderPathValidity(newParent, separator)) {
                            throw IMAPException.create(IMAPException.Code.INVALID_FOLDER_NAME, imapConfig, session, newName, invalidCharsString(separator));
                        }
                    }
                }
                if (newName.length() > maxMailboxNameLength()) {
                    throw MailExceptionCode.INVALID_FOLDER_NAME_TOO_LONG.create(Integer.valueOf(maxMailboxNameLength()));
                }
                /*
                 * Check for move
                 */
                final boolean move = !newParent.equals(oldParent);
                /*
                 * Check for rename. Rename must not be performed if a move has already been done
                 */
                final boolean rename = (!move && !newName.equals(getNameOf(moveMe)));
                if (move) {
                    /*
                     * Perform move operation
                     */
                    final String oldFullname = moveMe.getFullName();
                    if (getChecker().isDefaultFolder(oldFullname, true)) {
                        throw IMAPException.create(IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE, imapConfig, session, oldFullname);
                    }
                    IMAPFolder destFolder;
                    final boolean isDestRoot;
                    if ("".equals(newParent)) {
                        destFolder = (IMAPFolder) imapStore.getDefaultFolder();
                        isDestRoot = true;
                    } else {
                        destFolder = getIMAPFolder(newParent);
                        isDestRoot = false;
                    }
                    if (!doesExist(destFolder, false)) {
                        destFolder = checkForNamespaceFolder(newParent);
                        if (null == destFolder) {
                            /*
                             * Destination folder could not be found, thus an invalid name was specified by user
                             */
                            throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, isDestRoot ? DEFAULT_FOLDER_ID : newParent);
                        }
                    }
                    synchronized (destFolder) {
                        if ((destFolder.getType() & Folder.HOLDS_FOLDERS) == 0) {
                            throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS, imapConfig, session, isDestRoot ? DEFAULT_FOLDER_ID : destFolder.getFullName());
                        }
                        if (imapConfig.isSupportsACLs() && ((destFolder.getType() & Folder.HOLDS_MESSAGES) > 0)) {
                            try {
                                if (isDestRoot) {
                                    if (!(RootSubfoldersEnabledCache.isRootSubfoldersEnabled(imapConfig, (DefaultFolder) destFolder))) {
                                        throw IMAPException.create(IMAPException.Code.NO_CREATE_ACCESS, imapConfig, session, DEFAULT_FOLDER_ID);
                                    }
                                } else {
                                    if (!imapConfig.getACLExtension().canCreate(RightsCache.getCachedRights(destFolder, true, session, accountId))) {
                                        throw IMAPException.create(IMAPException.Code.NO_CREATE_ACCESS, imapConfig, session, newParent);
                                    }
                                }
                            } catch (final MessagingException e) {
                                /*
                                 * MYRIGHTS command failed for given mailbox
                                 */
                                if (!imapConfig.getImapCapabilities().hasNamespace() || !NamespaceFoldersCache.containedInPersonalNamespaces(newParent, imapStore, true, session, accountId)) {
                                    /*
                                     * No namespace support or given parent is NOT covered by user's personal namespaces.
                                     */
                                    throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, isDestRoot ? DEFAULT_FOLDER_ID : newParent);
                                }
                                LOG.debug("MYRIGHTS command failed on namespace folder", e);
                            }
                        }
                        final boolean mboxEnabled = MBoxEnabledCache.isMBoxEnabled(imapConfig, destFolder, new StringBuilder(destFolder.getFullName()).append(separator).toString());
                        if (!checkFolderNameValidity(newName, separator, mboxEnabled)) {
                            throw IMAPException.create(IMAPException.Code.INVALID_FOLDER_NAME, imapConfig, session, newName, invalidCharsString(separator));
                        }
                        if (isSubfolderOf(destFolder.getFullName(), oldFullname, separator)) {
                            throw IMAPException.create(IMAPException.Code.NO_MOVE_TO_SUBFLD, imapConfig, session, getNameOf(moveMe), getNameOf(destFolder));
                        }
                        moveMe = moveFolder(moveMe, destFolder, newName);
                    }
                }
                /*
                 * Is rename operation?
                 */
                if (rename) {
                    /*
                     * Perform rename operation
                     */
                    if (getChecker().isDefaultFolder(moveMe.getFullName(), true)) {
                        throw IMAPException.create(IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE, imapConfig, session, moveMe.getFullName());
                    } else if (imapConfig.isSupportsACLs() && ((moveMe.getType() & Folder.HOLDS_MESSAGES) > 0)) {
                        try {
                            if (!imapConfig.getACLExtension().canCreate(RightsCache.getCachedRights(moveMe, true, session, accountId))) {
                                throw IMAPException.create(IMAPException.Code.NO_CREATE_ACCESS, imapConfig, session, moveMe.getFullName());
                            }
                        } catch (final MessagingException e) {
                            throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, moveMe.getFullName());
                        }
                    }
                    /*
                     * Rename can only be invoked on a closed folder
                     */
                    if (moveMe.isOpen()) {
                        moveMe.close(false);
                    }
                    final boolean mboxEnabled;
                    final IMAPFolder renameFolder;
                    {
                        final IMAPFolder par = (IMAPFolder) moveMe.getParent();
                        final String parentFullName = par.getFullName();
                        final StringBuilder tmp = new StringBuilder();
                        if (parentFullName.length() > 0) {
                            tmp.append(parentFullName).append(separator);
                        }
                        tmp.append(newName);
                        renameFolder = (IMAPFolder) imapStore.getFolder(tmp.toString());
                        /*
                         * Check for MBox
                         */
                        mboxEnabled = MBoxEnabledCache.isMBoxEnabled(imapConfig, par, new StringBuilder(par.getFullName()).append(separator).toString());
                    }
                    if (doesExist(renameFolder, false)) {
                        throw IMAPException.create(IMAPException.Code.DUPLICATE_FOLDER, imapConfig, session, renameFolder.getFullName());
                    }
                    if (!checkFolderNameValidity(newName, separator, mboxEnabled)) {
                        throw IMAPException.create(IMAPException.Code.INVALID_FOLDER_NAME, imapConfig, session, newName, invalidCharsString(separator));
                    }
                    /*
                     * Remember subscription status
                     */
                    Map<String, Boolean> subscriptionStatus;
                    final String newFullName = renameFolder.getFullName();
                    final String oldFullName = moveMe.getFullName();
                    try {
                        subscriptionStatus = getSubscriptionStatus(moveMe, separator, oldFullName, newFullName);
                    } catch (final MessagingException e) {
                        LOG.warn("Subscription status of folder \"{}\" and its subfolders could not be stored prior to rename operation", moveMe.getFullName());
                        subscriptionStatus = null;
                    }
                    removeSessionData(moveMe.getFullName(), separator, moveMe);
                    /*
                     * Unsubscribe sub-tree
                     */
                    if (null != subscriptionStatus && subscriptionStatus.size() > 1) {
                        setFolderSubscription(moveMe.getFullName(), moveMe, false);
                    } else {
                        IMAPCommandsCollection.forceSetSubscribed(imapStore, moveMe.getFullName(), false);
                    }
                    /*
                     * Rename
                     */
                    boolean success = false;
                    try {
                        if (moveMe.isOpen()) {
                            moveMe.close(false);
                        } else {
                            // Enforce close
                            IMAPCommandsCollection.forceCloseCommand(moveMe);
                        }
                        final long start = System.currentTimeMillis();
                        IMAPCommandsCollection.renameFolder(moveMe, separator, renameFolder);
                        success = true;
                        // success = moveMe.renameTo(renameFolder);
                        mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                    } catch (final MessagingException e) {
                        /*
                         * Rename failed
                         */
                        throw IMAPException.create(IMAPException.Code.RENAME_FAILED, imapConfig, session, e, moveMe.getFullName(), newFullName, e.getMessage());

                    } finally {
                        if (!success) {
                            setFolderSubscription(moveMe.getFullName(), moveMe, true);
                        }
                    }
                    /*
                     * Success?
                     */
                    if (!success) {
                        throw IMAPException.create(IMAPException.Code.RENAME_FAILED, imapConfig, session, moveMe.getFullName(), newFullName, "<not-available>");
                    }
                    moveMe = getIMAPFolder(oldFullName);
                    if (doesExist(moveMe, false)) {
                        deleteFolder(moveMe, separator);
                    }
                    moveMe = getIMAPFolder(newFullName);
                    /*
                     * Apply remembered subscription status
                     */
                    if (subscriptionStatus == null) {
                        /*
                         * At least subscribe to renamed folder
                         */
                        IMAPCommandsCollection.forceSetSubscribed(imapStore, moveMe.getFullName(), true);
                    } else {
                        if (subscriptionStatus.size() > 1) {
                            applySubscriptionStatus(moveMe.getFullName(), separator, moveMe, subscriptionStatus);
                        } else {
                            boolean subscribe = subscriptionStatus.values().iterator().next().booleanValue();
                            IMAPCommandsCollection.forceSetSubscribed(imapStore, moveMe.getFullName(), subscribe);
                        }
                    }
                }
                return moveMe.getFullName();
            }
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final IMAPException e) {
            throw e;
        } catch (final OXException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        } finally {
            ListLsubCache.clearCache(accountId, session);
        }
    }

    @Override
    public String updateFolder(final String fullName, final MailFolderDescription toUpdate) throws OXException {
        boolean changed = false;
        try {
            if (DEFAULT_FOLDER_ID.equals(fullName)) {
                throw MailExceptionCode.NO_ROOT_FOLDER_MODIFY_DELETE.create();
            }
            IMAPFolderWorker.checkFailFast(imapStore, fullName);
            IMAPFolder updateMe = getIMAPFolder(fullName);
            ListLsubEntry updateMeEntry = ListLsubCache.getCachedLISTEntry(fullName, accountId, updateMe, session, ignoreSubscriptions);
            if (!updateMeEntry.exists()) {
                ListInfo listInfo = IMAPCommandsCollection.getListInfo(fullName, updateMe);
                if (null == listInfo) {
                    updateMe = checkForNamespaceFolder(fullName);
                    if (null == updateMe) {
                        throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                    }
                }
            }
            /*
             * Obtain folder lock once to avoid multiple acquire/releases when invoking folder's getXXX() methods
             */
            synchronized (updateMe) {
                /*
                 * Check for standard folder & possible subscribe operation
                 */
                final IMAPDefaultFolderChecker checker = getChecker();
                boolean defaultFolder = false;
                if ("INBOX".equals(fullName)) {
                    defaultFolder = true;
                } else if (fullName.equals(checker.getDefaultFolder(StorageUtility.INDEX_TRASH)) ) {
                    defaultFolder = true;
                } else if (fullName.equals(checker.getDefaultFolder(StorageUtility.INDEX_DRAFTS)) ) {
                    defaultFolder = true;
                } else if (fullName.equals(checker.getDefaultFolder(StorageUtility.INDEX_SENT)) ) {
                    defaultFolder = true;
                } else if (fullName.equals(checker.getDefaultFolder(StorageUtility.INDEX_SPAM)) ) {
                    defaultFolder = true;
                }
                boolean performSubscription = MailProperties.getInstance().isIgnoreSubscription() && defaultFolder ? false : performSubscribe(toUpdate, updateMeEntry);
                if (performSubscription && defaultFolder && !toUpdate.isSubscribed()) {
                    OXException warning = IMAPException.create(IMAPException.Code.NO_DEFAULT_FOLDER_UNSUBSCRIBE, imapConfig, session, fullName);
                    warning.setCategory(OXException.CATEGORY_WARNING);
                    imapAccess.addWarnings(Collections.singletonList(warning));
                    performSubscription = false;
                }
                /*
                 * Notify message storage
                 */
                imapAccess.getMessageStorage().notifyIMAPFolderModification(fullName);
                /*
                 * Proceed update
                 */
                if (imapConfig.isSupportsACLs() && toUpdate.containsPermissions()) {
                    final ACL[] oldACLs = getACLSafe(updateMe);
                    if (oldACLs != null) {
                        final ACL[] newACLs = permissions2ACL(toUpdate.getPermissions(), updateMe);
                        final Entity2ACL entity2ACL = getEntity2ACL();
                        final Entity2ACLArgs args = IMAPFolderConverter.getEntity2AclArgs(session, updateMe, imapConfig);
                        final Map<String, ACL> m = acl2map(newACLs);
                        if (!equals(oldACLs, m, entity2ACL, args)) {
                            /*
                             * Default folder is affected, check if owner still holds full rights
                             */
                            final ACLExtension aclExtension = imapConfig.getACLExtension();
                            if (getChecker().isDefaultFolder(updateMe.getFullName()) && !stillHoldsFullRights(updateMe, newACLs, aclExtension)) {
                                throw IMAPException.create(IMAPException.Code.NO_DEFAULT_FOLDER_UPDATE, imapConfig, session, updateMe.getFullName());
                            }
                            if (!aclExtension.canSetACL(RightsCache.getCachedRights(updateMe, true, session, accountId))) {
                                throw IMAPException.create(IMAPException.Code.NO_ADMINISTER_ACCESS, imapConfig, session, updateMe.getFullName());
                            }
                            /*
                             * Check new ACLs
                             */
                            if (newACLs.length == 0) {
                                throw IMAPException.create(IMAPException.Code.NO_ADMIN_ACL, imapConfig, session, updateMe.getFullName());
                            }
                            {
                                boolean adminFound = false;
                                for (int i = 0; (i < newACLs.length) && !adminFound; i++) {
                                    if (aclExtension.canSetACL(newACLs[i].getRights())) {
                                        adminFound = true;
                                    }
                                }
                                if (!adminFound) {
                                    throw IMAPException.create(IMAPException.Code.NO_ADMIN_ACL, imapConfig, session, updateMe.getFullName());
                                }
                            }
                            final List<ACL> entities = new LinkedList<ACL>();
                            /*
                             * Remove deleted ACLs
                             */
                            final ACL[] removedACLs = getRemovedACLs(m, oldACLs);
                            if (removedACLs.length > 0) {
                                for (int i = 0; i < removedACLs.length; i++) {
                                    final String entityName = removedACLs[i].getName();
                                    if (isKnownEntity(entityName, entity2ACL, ctx, args)) {
                                        updateMe.removeACL(entityName);
                                        entities.add(removedACLs[i]);
                                        changed = true;
                                    }
                                }
                            }
                            /*
                             * Change existing ACLs according to new ACLs
                             */
                            final Map<String, ACL> om = acl2map(oldACLs);
                            for (int i = 0; i < newACLs.length; i++) {
                                ACL newACL = newACLs[i];
                                ACL validated = validate(newACL, om);
                                if (null != validated) {
                                    updateMe.addACL(validated);
                                    entities.add(newACL);
                                    changed = true;
                                }
                            }
                            /*
                             * Since the ACLs have changed remove cached rights
                             */
                            RightsCache.removeCachedRights(updateMe, session, accountId);
                            /*
                             * Does affect ListLsubCache of other users, too
                             */
                            dropListLsubCachesForOther(entities.toArray(new ACL[entities.size()]));
                        }
                    }
                }
                if (performSubscription) {
                    /*
                     * Check read permission
                     */
                    if (imapConfig.isSupportsACLs()) {
                        if ((updateMe.getType() & Folder.HOLDS_MESSAGES) > 0) {
                            try {
                                if (!imapConfig.getACLExtension().canLookUp(RightsCache.getCachedRights(updateMe, true, session, accountId))) {
                                    throw IMAPException.create(IMAPException.Code.NO_LOOKUP_ACCESS, imapConfig, session, fullName);
                                }
                            } catch (final MessagingException e) {
                                throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, fullName);
                            }
                        } else {
                            throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, fullName);
                        }
                    }
                    boolean subscribe = toUpdate.isSubscribed();
                    setSubscribed(subscribe, updateMe);
                    changed = true;
                }
                return updateMe.getFullName();
            }
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final IMAPException e) {
            throw e;
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        } finally {
            if (changed) {
                ListLsubCache.clearCache(accountId, session);
            }
        }
    }

    private void setSubscribed(boolean subscribe, IMAPFolder imapFolder) throws MessagingException {
        imapFolder.setSubscribed(subscribe);
        IMAPCommandsCollection.forceSetSubscribed(imapStore, imapFolder.getFullName(), subscribe);

        if (subscribe) {
            // Ensure parent gets subscribed, too
            try {
                IMAPFolder parent = (IMAPFolder) imapFolder.getParent();
                if ((null != parent) && (parent.getFullName().length() > 0)) {
                    setSubscribed(subscribe, parent);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private static boolean performSubscribe(MailFolderDescription toUpdate, ListLsubEntry updateMe) {
        return toUpdate.containsSubscribed() && (toUpdate.isSubscribed() != updateMe.isSubscribed());
    }

    private void deleteTemporaryCreatedFolder(final IMAPFolder temporaryFolder) throws OXException, MessagingException {
        if (doesExist(temporaryFolder, false)) {
            try {
                temporaryFolder.delete(true);
            } catch (final MessagingException e1) {
                LOG.error("Temporary created folder could not be deleted: {}", temporaryFolder.getFullName(), e1);
            }
        }
    }

    @Override
    public String deleteFolder(final String fullName, final boolean hardDelete) throws OXException {
        boolean clearListLsubCache = false;
        try {
            if (DEFAULT_FOLDER_ID.equals(fullName)) {
                throw MailExceptionCode.NO_ROOT_FOLDER_MODIFY_DELETE.create();
            }
            IMAPFolderWorker.checkFailFast(imapStore, fullName);
            IMAPFolder deleteMe = getIMAPFolder(fullName);
            char separator;
            {
                ListLsubEntry deleteMeEntry = ListLsubCache.tryCachedLISTEntry(fullName, accountId, session);
                if (null != deleteMeEntry) {
                    if (!deleteMeEntry.exists()) {
                        deleteMe = checkForNamespaceFolder(fullName);
                        if (null == deleteMe) {
                            throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                        }
                    }
                    separator = deleteMeEntry.getSeparator();
                } else {
                    ListInfo listInfo = IMAPCommandsCollection.getListInfo(fullName, deleteMe);
                    if (null == listInfo) {
                        throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                    }
                    separator = listInfo.separator;
                }
            }
            clearListLsubCache = true;
            synchronized (deleteMe) {
                imapAccess.getMessageStorage().notifyIMAPFolderModification(fullName);
                if (hardDelete) {
                    /*
                     * Delete permanently
                     */
                    deleteFolder(deleteMe, separator);
                } else {
                    String trashFullName = getTrashFolder();
                    if (isSubfolderOf(getParentFullName(fullName, separator), trashFullName, separator) || !inferiors(trashFullName, deleteMe)) {
                        /*
                         * Delete permanently
                         */
                        deleteFolder(deleteMe, separator);
                    } else {
                        /*
                         * Just move this folder to trash
                         */
                        imapAccess.getMessageStorage().notifyIMAPFolderModification(trashFullName);
                        final String name = getNameOf(deleteMe);
                        int appendix = 1;
                        final StringBuilder sb = new StringBuilder();
                        sb.append(trashFullName).append(separator).append(name);
                        while (ListLsubCache.getCachedLISTEntry(sb.toString(), accountId, deleteMe, session, ignoreSubscriptions).exists()) {
                            /*
                             * A folder of the same name already exists. Append appropriate appendix to folder name and check existence again.
                             */
                            sb.setLength(0);
                            sb.append(trashFullName).append(separator).append(name).append('_').append(++appendix);
                        }
                        IMAPFolder trashFolder = (IMAPFolder) imapStore.getFolder(trashFullName);
                        IMAPFolder newFolder = (IMAPFolder) imapStore.getFolder(sb.toString());
                        synchronized (newFolder) {
                            try {
                                moveFolder(deleteMe, trashFolder, newFolder, false);
                            } catch (final OXException e) {
                                deleteTemporaryCreatedFolder(newFolder);
                                throw e;
                            } catch (final MessagingException e) {
                                deleteTemporaryCreatedFolder(newFolder);
                                throw e;
                            }
                        }
                    }
                }
                return fullName;
            }
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        } finally {
            if (clearListLsubCache) {
                ListLsubCache.clearCache(accountId, session);
            }
        }
    }

    private boolean inferiors(String fullName, IMAPFolder imapFolder) throws OXException, MessagingException {
        ListLsubEntry entry = ListLsubCache.tryCachedLISTEntry(fullName, accountId, session);
        if (null != entry) {
            return entry.hasInferiors();
        }

        ListInfo listInfo = IMAPCommandsCollection.getListInfo(fullName, imapFolder);
        return null == listInfo ? false : listInfo.hasInferiors;
    }

    private String getParentFullName(String fullName, char separator) {
        int pos = fullName.indexOf(separator);
        return pos < 0 || pos == fullName.length() - 1 ? "" : fullName.substring(0, pos);

    }

    private static final Flags FLAGS_DELETED = new Flags(Flags.Flag.DELETED);

    @Override
    public void expungeFolder(final String fullName) throws OXException {
        expungeFolder(fullName, false);
    }

    @Override
    public void expungeFolder(final String fullName, final boolean hardDelete) throws OXException {
        try {
            if (DEFAULT_FOLDER_ID.equals(fullName)) {
                return;
            }
            IMAPFolderWorker.checkFailFast(imapStore, fullName);
            IMAPFolder f = getIMAPFolderWithRecentListener(fullName);
            if (!doesExist(f, true)) {
                f = checkForNamespaceFolder(fullName);
                if (null == f) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                }
            }
            // ListLsubCache.removeCachedEntry(fullName, accountId, session);
            synchronized (f) {
                imapAccess.getMessageStorage().notifyIMAPFolderModification(fullName);
                try {
                    if (!isSelectable(f)) {
                        throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapConfig, session, f.getFullName());
                    }
                    if (imapConfig.isSupportsACLs()) {
                        final Rights myrights = RightsCache.getCachedRights(f, true, session, accountId);
                        if (!imapConfig.getACLExtension().canRead(myrights)) {
                            throw IMAPException.create(IMAPException.Code.NO_READ_ACCESS, imapConfig, session, f.getFullName());
                        }
                        if (!imapConfig.getACLExtension().canDeleteMessages(myrights)) {
                            throw IMAPException.create(IMAPException.Code.NO_DELETE_ACCESS, imapConfig, session, f.getFullName());
                        }
                    }
                } catch (final MessagingException e) {
                    throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, f.getFullName());
                }
                /*
                 * Remove from session storage
                 */
                if (IMAPSessionStorageAccess.isEnabled()) {
                    IMAPSessionStorageAccess.removeDeletedFolder(accountId, session, fullName);
                }
                final OperationKey opKey = new OperationKey(Type.MSG_DELETE, accountId, new Object[] { fullName });
                boolean marked = false;
                f.open(Folder.READ_WRITE);
                try {
                    final int msgCount = f.getMessageCount();
                    if (msgCount <= 0) {
                        /*
                         * Empty folder
                         */
                        return;
                    }

                    marked = setMarker(opKey, f);

                    String trashFullname = null;
                    final boolean hardDeleteMsgsByConfig = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isHardDeleteMsgs();
                    final boolean backup = (!hardDelete && !hardDeleteMsgsByConfig && !isSubfolderOf(
                        f.getFullName(),
                        (trashFullname = getTrashFolder()),
                        getSeparator()));
                    if (backup) {
                        imapAccess.getMessageStorage().notifyIMAPFolderModification(trashFullname);
                        final Message[] candidates = f.search(new FlagTerm(FLAGS_DELETED, true));
                        if (null != candidates && candidates.length > 0) {
                            f.copyMessages(candidates, imapStore.getFolder(trashFullname));
                        }
                    }
                }  finally {
                    if (marked) {
                        unsetMarker(opKey);
                    }
                    f.close(true);
                }
            }
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        } finally {
            ListLsubCache.clearCache(accountId, session);
        }
    }

    @Override
    public void clearFolder(final String fullName, final boolean hardDelete) throws OXException {
        try {
            if (DEFAULT_FOLDER_ID.equals(fullName)) {
                throw MailExceptionCode.NO_ROOT_FOLDER_MODIFY_DELETE.create();
            }
            IMAPFolderWorker.checkFailFast(imapStore, fullName);
            IMAPFolder f = getIMAPFolderWithRecentListener(fullName);
            if (!doesExist(f, true)) {
                f = checkForNamespaceFolder(fullName);
                if (null == f) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                }
            }
            // ListLsubCache.removeCachedEntry(fullName, accountId, session);
            synchronized (f) {
                imapAccess.getMessageStorage().notifyIMAPFolderModification(fullName);
                try {
                    if (!isSelectable(f)) {
                        throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapConfig, session, f.getFullName());
                    }
                    if (imapConfig.isSupportsACLs()) {
                        final Rights myrights = RightsCache.getCachedRights(f, true, session, accountId);
                        if (!imapConfig.getACLExtension().canRead(myrights)) {
                            throw IMAPException.create(IMAPException.Code.NO_READ_ACCESS, imapConfig, session, f.getFullName());
                        }
                        if (!imapConfig.getACLExtension().canDeleteMessages(myrights)) {
                            throw IMAPException.create(IMAPException.Code.NO_DELETE_ACCESS, imapConfig, session, f.getFullName());
                        }
                    }
                } catch (final MessagingException e) {
                    throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, f.getFullName());
                }
                /*
                 * Remove from session storage
                 */
                if (IMAPSessionStorageAccess.isEnabled()) {
                    IMAPSessionStorageAccess.removeDeletedFolder(accountId, session, fullName);
                }
                final OperationKey opKey = new OperationKey(Type.MSG_DELETE, accountId, new Object[] { fullName });
                boolean marked = false;
                f.open(Folder.READ_WRITE);
                try {
                    int msgCount = f.getMessageCount();
                    if (msgCount <= 0) {
                        /*
                         * Empty folder
                         */
                        return;
                    }

                    marked = setMarker(opKey, f);

                    String trashFullname = null;
                    final boolean hardDeleteMsgsByConfig = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isHardDeleteMsgs();
                    final boolean backup =
                        (!hardDelete && !hardDeleteMsgsByConfig && !isSubfolderOf(f.getFullName(), (trashFullname = getTrashFolder()), getSeparator()));
                    if (backup) {
                        imapAccess.getMessageStorage().notifyIMAPFolderModification(trashFullname);
                    }
                    final int blockSize = imapConfig.getIMAPProperties().getBlockSize();
                    if (blockSize > 0) {
                        final boolean supportsMove = imapConfig.asMap().containsKey("MOVE");
                        /*
                         * Block-wise deletion
                         */
                        while (msgCount > blockSize) {
                            /*
                             * Don't adapt sequence number since folder expunge already resets message numbering
                             */
                            if (backup) {
                                try {
                                    if(supportsMove){
                                        new MoveIMAPCommand(f, 1, blockSize, trashFullname).doCommand();
                                        msgCount -= blockSize;
                                        continue;
                                    } else {
                                        new CopyIMAPCommand(f, 1, blockSize, trashFullname).doCommand();
                                    }
                                } catch (final MessagingException e) {
                                    if (e.getMessage().indexOf("Over quota") > -1) {
                                        /*
                                         * We face an Over-Quota-Exception
                                         */
                                        throw MailExceptionCode.DELETE_FAILED_OVER_QUOTA.create(e, new Object[0]);
                                    }
                                    final Exception nestedExc = e.getNextException();
                                    if (nestedExc != null && nestedExc.getMessage().indexOf("Over quota") > -1) {
                                        /*
                                         * We face an Over-Quota-Exception
                                         */
                                        throw MailExceptionCode.DELETE_FAILED_OVER_QUOTA.create(e, new Object[0]);
                                    }
                                    throw IMAPException.create(
                                        IMAPException.Code.MOVE_ON_DELETE_FAILED,
                                        imapConfig,
                                        session,
                                        e,
                                        new Object[0]);
                                }
                            }
                            /*
                             * Delete through storing \Deleted flag...
                             */
                            new FlagsIMAPCommand(f, 1, blockSize, FLAGS_DELETED, true, true).doCommand();
                            /*
                             * ... and perform EXPUNGE
                             */
                            try {
                                IMAPCommandsCollection.fastExpunge(f);
                            } catch (final FolderClosedException e) {
                                /*
                                 * Not possible to retry since connection is broken
                                 */
                                throw IMAPException.create(
                                    IMAPException.Code.CONNECT_ERROR,
                                    imapConfig,
                                    session,
                                    e,
                                    imapConfig.getServer(),
                                    imapConfig.getLogin());
                            } catch (final StoreClosedException e) {
                                /*
                                 * Not possible to retry since connection is broken
                                 */
                                throw IMAPException.create(
                                    IMAPException.Code.CONNECT_ERROR,
                                    imapConfig,
                                    session,
                                    e,
                                    imapConfig.getServer(),
                                    imapConfig.getLogin());
                            }
                            /*
                             * Decrement
                             */
                            msgCount -= blockSize;
                        }
                    }
                    if (msgCount == 0) {
                        /*
                         * All messages already cleared through previous block-wise deletion
                         */
                        return;
                    }
                    if (backup) {
                        try {
                            final boolean supportsMove = imapConfig.asMap().containsKey("MOVE");
                            if(supportsMove){
                                new MoveIMAPCommand(f, trashFullname).doCommand();
                                return;
                            }
                            new CopyIMAPCommand(f, trashFullname).doCommand();
                        } catch (final MessagingException e) {
                            if (e.getNextException() instanceof CommandFailedException) {
                                final CommandFailedException exc = (CommandFailedException) e.getNextException();
                                if (exc.getMessage().indexOf("Over quota") > -1) {
                                    /*
                                     * We face an Over-Quota-Exception
                                     */
                                    throw MailExceptionCode.DELETE_FAILED_OVER_QUOTA.create(e, new Object[0]);
                                }
                            }
                            throw IMAPException.create(IMAPException.Code.MOVE_ON_DELETE_FAILED, imapConfig, session, e, new Object[0]);
                        }
                    }
                    /*
                     * Delete through storing \Deleted flag...
                     */
                    new FlagsIMAPCommand(f, FLAGS_DELETED, true, true).doCommand();
                    /*
                     * ... and perform EXPUNGE
                     */
                    final long start = System.currentTimeMillis();
                    IMAPCommandsCollection.fastExpunge(f);
                    mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                } finally {
                    closeSafe(f);
                    if (marked) {
                        unsetMarker(opKey);
                    }
                }
            }
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private boolean isSelectable(final IMAPFolder imapFolder) throws OXException, MessagingException {
        return getLISTEntry(imapFolder).canOpen();
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullName) throws OXException {
        try {
            if (fullName.equals(DEFAULT_FOLDER_ID)) {
                return EMPTY_PATH;
            }
            IMAPFolder f = getIMAPFolder(fullName);
            if (!doesExist(f, true)) {
                f = checkForNamespaceFolder(fullName);
                if (null == f) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                }
            }
            /*
             * Obtain folder lock once to avoid multiple acquire/releases when invoking folder's getXXX() methods
             */
            if (imapConfig.isSupportsACLs() && isSelectable(f)) {
                try {
                    if (!imapConfig.getACLExtension().canLookUp(RightsCache.getCachedRights(f, true, session, accountId))) {
                        throw IMAPException.create(IMAPException.Code.NO_LOOKUP_ACCESS, imapConfig, session, fullName);
                    }
                } catch (final MessagingException e) {
                    throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, fullName);
                }
            }
            final List<MailFolder> list = new ArrayList<MailFolder>();
            final String defaultFolder = "";
            String fn;
            while (!(fn = f.getFullName()).equals(defaultFolder)) {
                list.add(FolderUtility.loadFolder(fn, this));
                f = (IMAPFolder) f.getParent();
            }
            return list.toArray(new MailFolder[list.size()]);
        } catch (final MessagingException e) {
            throw handleMessagingException(fullName, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public String getDefaultFolderPrefix() throws OXException {
        final String urlName = urlName("ns-", imapConfig.getServer(), imapConfig.getPort(), imapConfig.getLogin(), imapConfig.getPassword());
        String defaultFolderPrefix = (String) session.getParameter(urlName);
        if (null == defaultFolderPrefix) {
            defaultFolderPrefix = getDefaultFolderPrefix0();
            session.setParameter(urlName, defaultFolderPrefix);
        }
        return defaultFolderPrefix;
    }

    private String getDefaultFolderPrefix0() throws OXException {
        try {
            // Special handling for GMail...
            {
                final String server = imapConfig.getServer().toLowerCase(Locale.US);
                if ("imap.gmail.com".equals(server) || "imap.googlemail.com".equals(server)) {
                    /*
                     * Look-up special GMail folder: [GMail], [Google Mail], ...
                     */
                    final ListLsubEntry rootEntry = ListLsubCache.getCachedLISTEntry("", accountId, imapStore, session, this.ignoreSubscriptions);
                    final List<ListLsubEntry> children = rootEntry.getChildren();
                    final String prefix = "[G";
                    for (final ListLsubEntry child : children) {
                        final String fullName = child.getFullName();
                        if (fullName.startsWith(prefix)) {
                            return fullName + child.getSeparator();
                        }
                    }
                }
            }

            // Determine separator character
            char separator = ListLsubCache.getSeparator(accountId, (DefaultFolder) imapStore.getDefaultFolder(), session, this.ignoreSubscriptions);
            getSeparator(separator);

            // Try NAMESPACE command
            boolean detectedByNamespace = false;
            String prefix = null;
            try {
                final String[] namespaces = NamespaceFoldersCache.getPersonalNamespaces(imapStore, true, session, accountId);
                if (null == namespaces || 0 == namespaces.length) {
                    // No namespaces available
                    String prefixByInferiors = prefixByInferiors(separator);
                    LOG.info("IMAP server {} does not provide a personal namespace for login {}. Using fall-back \"by inferiors\" detection: \"{}\" (user={}, context={})", imapConfig.getServer(), imapConfig.getLogin(), prefixByInferiors, Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
                    return prefixByInferiors;
                }
                prefix = namespaces[0];
                detectedByNamespace = true;
            } catch (final MessagingException e) {
                String prefixByInferiors = prefixByInferiors(separator);
                LOG.info("NAMESPACE command failed for any reason on IMAP server {} for login {}. Using fall-back \"by inferiors\" detection: \"{}\" (user={}, context={})", imapConfig.getServer(), imapConfig.getLogin(), prefixByInferiors, Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), e);
                return prefixByInferiors;
            }

            if (prefix.length() != 0) {
                return new StringBuilder(prefix).append(separator).toString();
            }

            // The empty prefix so far; verify against root-folder capability
            DefaultFolder defaultFolder = (DefaultFolder) imapStore.getDefaultFolder();
            if (!RootSubfoldersEnabledCache.isRootSubfoldersEnabled(imapConfig, defaultFolder)) {
                // Impossible to create folders on root level; hence adjust prefix to be: "INBOX" + <separator>
                if (detectedByNamespace) {
                    // Strange... Since NAMESPACE tells to use root level, but IMAP server denies to create such folders.
                    LOG.warn("\n\n\tNAMESPACE from IMAP server {} indicates to use root level for login {}, but IMAP server denies to create such folders!\n", imapConfig.getServer(), imapConfig.getLogin());
                }
                return new StringBuilder(STR_INBOX).append(separator).toString();
            }

            // Grant empty prefix as standard folder prefix
            return prefix;
        } catch (final MessagingException e) {
            throw handleMessagingException(e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private String prefixByInferiors(char separator) throws OXException {
        try {
            final DefaultFolder defaultFolder = (DefaultFolder) imapStore.getDefaultFolder();
            if (!RootSubfoldersEnabledCache.isRootSubfoldersEnabled(imapConfig, defaultFolder) || MailProperties.getInstance().isAllowNestedDefaultFolderOnAltNamespace()) {
                return new StringBuilder(STR_INBOX).append(separator).toString();
            }
            return "";
        } catch (final MessagingException e) {
            throw handleMessagingException(e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        return getChecker().getDefaultFolder(StorageUtility.INDEX_CONFIRMED_HAM);
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        return getChecker().getDefaultFolder(StorageUtility.INDEX_CONFIRMED_SPAM);
    }

    @Override
    public String getDraftsFolder() throws OXException {
        return getChecker().getDefaultFolder(StorageUtility.INDEX_DRAFTS);
    }

    @Override
    public String getSentFolder() throws OXException {
        return getChecker().getDefaultFolder(StorageUtility.INDEX_SENT);
    }

    @Override
    public String getSpamFolder() throws OXException {
        return getChecker().getDefaultFolder(StorageUtility.INDEX_SPAM);
    }

    @Override
    public String getTrashFolder() throws OXException {
        return getChecker().getDefaultFolder(StorageUtility.INDEX_TRASH);
    }

    @Override
    public void releaseResources() throws IMAPException {
        // Nothing to release
    }

    @Override
    public com.openexchange.mail.Quota[] getQuotas(final String folder, final com.openexchange.mail.Quota.Type[] types) throws OXException {
        try {
            final String fullName = folder == null ? STR_INBOX : folder;
            final boolean isDefaultFolder = fullName.equals(DEFAULT_FOLDER_ID);
            final IMAPFolder f = (IMAPFolder) (isDefaultFolder ? imapStore.getDefaultFolder() : imapStore.getFolder(fullName));
            /*
             * Obtain folder lock once to avoid multiple acquire/releases when invoking folder's getXXX() methods
             */
            synchronized (f) {
                if (!isDefaultFolder && !doesExist(f, true)) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
                }
                try {
                    if (!isSelectable(f)) {
                        throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, imapConfig, session, fullName);
                    }
                    if (imapConfig.isSupportsACLs()) {
                        final Rights myrights = RightsCache.getCachedRights(f, true, session, accountId);
                        if (!imapConfig.getACLExtension().canRead(myrights)) {
                            throw IMAPException.create(IMAPException.Code.NO_READ_ACCESS, imapConfig, session, fullName);
                        }
                        /*-
                         * TODO: Why check DELETE access when requesting quota?
                         *
                        if (!imapConfig.getACLExtension().canDeleteMailbox(myrights)) {
                            throw IMAPException.create(IMAPException.Code.NO_DELETE_ACCESS, imapConfig, session, fullName);
                        }
                         */
                    }
                } catch (final MessagingException e) {
                    throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, fullName);
                }
                // f.open(Folder.READ_ONLY);
                if (!imapConfig.getImapCapabilities().hasQuota()) {
                    return com.openexchange.mail.Quota.getUnlimitedQuotas(types);
                }
                Quota[] folderQuota = null;
                try {
                    final long start = System.currentTimeMillis();
                    folderQuota = f.getQuota();
                    mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                } catch (final MessagingException mexc) {
                    if (mexc.getNextException() instanceof ParsingException) {
                        try {
                            final long start = System.currentTimeMillis();
                            folderQuota = IMAPCommandsCollection.getQuotaRoot(f);
                            mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                        } catch (final MessagingException inner) {
                            LOG.warn("", inner);
                            return com.openexchange.mail.Quota.getUnlimitedQuotas(types);
                        }
                    } else {
                        throw mexc;
                    }
                }
                if (folderQuota == null || folderQuota.length == 0) {
                    return com.openexchange.mail.Quota.getUnlimitedQuotas(types);
                }
                final Quota.Resource[] resources = folderQuota[0].resources;
                if (resources.length == 0) {
                    return com.openexchange.mail.Quota.getUnlimitedQuotas(types);
                }
                final com.openexchange.mail.Quota[] quotas = new com.openexchange.mail.Quota[types.length];
                for (int i = 0; i < types.length; i++) {
                    final String typeStr = types[i].toString();
                    /*
                     * Find corresponding resource to current type
                     */
                    Resource resource = null;
                    for (int k = 0; k < resources.length && resource == null; k++) {
                        if (typeStr.equalsIgnoreCase(resources[k].name)) {
                            resource = resources[k];
                        }
                    }
                    if (resource == null) {
                        /*
                         * No quota limitation found that applies to current resource type
                         */
                        quotas[i] = com.openexchange.mail.Quota.getUnlimitedQuota(types[i]);
                    } else {
                        quotas[i] = new com.openexchange.mail.Quota(resource.limit, resource.usage, types[i]);
                    }
                }
                return quotas;
            }
        } catch (final MessagingException e) {
            throw handleMessagingException(folder, e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    /*
     * ++++++++++++++++++ Helper methods ++++++++++++++++++
     */

    private void unsetMarker(final OperationKey key) {
        OperationKey.unsetMarker(key, session);
    }

    private boolean setMarker(final OperationKey key, final Folder imapFolder) throws OXException {
        final int result = OperationKey.setMarker(key, session);
        if (result < 0) {
            // In use...
            throw MimeMailExceptionCode.IN_USE_ERROR_EXT.create(
                imapConfig.getServer(),
                imapConfig.getLogin(),
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(session.getContextId()),
                MimeMailException.appendInfo("Mailbox is currently in use.", imapFolder));
        }
        return result > 0;
    }

    private static void closeSafe(final Folder folder) {
        if (null != folder) {
            try {
                folder.close(false);
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    /*-
     * Get the QUOTA resource with the highest usage-per-limitation value
     *
     * @param resources The QUOTA resources
     * @return The QUOTA resource with the highest usage to limitation relation
     *
     *
     * private static Resource getMaxUsageResource(final Quota.Resource[] resources) {
     *     final Resource maxUsageResource;
     *     {
     *         int index = 0;
     *         long maxUsage = resources[0].usage / resources[0].limit;
     *         for (int i = 1; i &lt; resources.length; i++) {
     *             final long tmp = resources[i].usage / resources[i].limit;
     *             if (tmp &gt; maxUsage) {
     *                 maxUsage = tmp;
     *                 index = i;
     *             }
     *         }
     *         maxUsageResource = resources[index];
     *     }
     *     return maxUsageResource;
     * }
     */

    /**
     * Get the ACL list of specified folder
     *
     * @param imapFolder The IMAP folder
     * @return The ACL list or <code>null</code> if any error occurred
     */
    private static ACL[] getACLSafe(final IMAPFolder imapFolder) {
        try {
            return imapFolder.getACL();
        } catch (final MessagingException e) {
            LOG.debug("", e);
            return null;
        }
    }

    /**
     * Get the ACL list of specified folder
     *
     * @param fullName The full name denoting the folder
     * @param imapFolder The IMAP folder provding the protocol to use
     * @return The ACL list or <code>null</code> if any error occurred
     */
    private static ACL[] getACLSafe(String fullName, IMAPFolder imapFolder) {
        try {
            return IMAPCommandsCollection.getACL(fullName, imapFolder);
        } catch (final MessagingException e) {
            LOG.debug("", e);
            return null;
        }
    }

    private void deleteFolder(IMAPFolder deleteMe, char separator) throws OXException, MessagingException {
        final String fullName = deleteMe.getFullName();
        if (getChecker().isDefaultFolder(fullName, true)) {
            throw IMAPException.create(IMAPException.Code.NO_DEFAULT_FOLDER_DELETE, imapConfig, session, fullName);
        } else if (!doesExist(deleteMe, false)) {
            throw IMAPException.create(IMAPException.Code.FOLDER_NOT_FOUND, imapConfig, session, fullName);
        }
        try {
            if (imapConfig.isSupportsACLs() && ((deleteMe.getType() & Folder.HOLDS_MESSAGES) > 0) && !imapConfig.getACLExtension().canDeleteMailbox(
                RightsCache.getCachedRights(deleteMe, true, session, accountId))) {
                throw IMAPException.create(IMAPException.Code.NO_CREATE_ACCESS, imapConfig, session, fullName);
            }
        } catch (final MessagingException e) {
            throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, fullName);
        }
        if (deleteMe.isOpen()) {
            deleteMe.close(false);
        }
        final ACL[] oldACLs = imapConfig.isSupportsACLs() ? getACLSafe(deleteMe) : null;
        /*
         * Unsubscribe prior to deletion
         */
        IMAPCommandsCollection.forceSetSubscribed(imapStore, fullName, false);
        removeSessionData(deleteMe.getFullName(), separator, deleteMe);
        final long start = System.currentTimeMillis();
        if (!deleteMe.delete(true)) {
            throw IMAPException.create(IMAPException.Code.DELETE_FAILED, imapConfig, session, fullName);
        }
        mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
        /*
         * Remove cache entries
         */
        // ListLsubCache.clearCache(accountId, session);
        RightsCache.removeCachedRights(deleteMe, session, accountId);
        UserFlagsCache.removeUserFlags(deleteMe, session, accountId);
        // Affected users, too
        dropListLsubCachesForOther(oldACLs);
    }

    private boolean stillHoldsFullRights(final IMAPFolder defaultFolder, final ACL[] newACLs, final ACLExtension aclExtension) throws OXException {
        /*
         * Ensure that owner still holds full rights
         */
        final String ownerACLName =
            getEntity2ACL().getACLName(session.getUserId(), ctx, IMAPFolderConverter.getEntity2AclArgs(session, defaultFolder, imapConfig));
        final Rights fullRights = aclExtension.getFullRights();
        for (final ACL newACL : newACLs) {
            if (newACL.getName().equals(ownerACLName) && newACL.getRights().contains(fullRights)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Boolean> getSubscriptionStatus(IMAPFolder f, char separator, String oldFullName, String newFullName) throws MessagingException, OXException {
        final Map<String, Boolean> retval = new HashMap<String, Boolean>();
        getSubscriptionStatus(retval, f.getFullName(), separator, f, oldFullName, newFullName);
        return retval;
    }

    private void getSubscriptionStatus(Map<String, Boolean> m, String fullName, char separator, IMAPFolder f, String oldFullName, String newFullName) throws MessagingException, OXException {
        for (ListInfo listInfo : IMAPCommandsCollection.listSubfolders(fullName, separator, f)) {
            getSubscriptionStatus(m, listInfo.name, separator, f, oldFullName, newFullName);
        }

        ListLsubEntry testEntry = ListLsubCache.getCachedLISTEntry(fullName, accountId, f, session, ignoreSubscriptions);
        m.put(fullName.replaceFirst(Pattern.quote(oldFullName), quoteReplacement(newFullName)), Boolean.valueOf(testEntry.isSubscribed()));
    }

    private void setFolderSubscription(String fullName, IMAPFolder f, boolean subscribed) throws MessagingException, OXException {
        ListLsubEntry testEntry = ListLsubCache.getCachedLISTEntry(fullName, accountId, f, session, ignoreSubscriptions);
        if (testEntry.hasInferiors()) {
            for (ListInfo listInfo : IMAPCommandsCollection.listSubfolders(fullName, testEntry.getSeparator(), f)) {
                setFolderSubscription(listInfo.name, f, subscribed);
            }
        }
        IMAPCommandsCollection.forceSetSubscribed(imapStore, fullName, subscribed);
    }

    private void applySubscriptionStatus(String fullName, char separator, IMAPFolder f, Map<String, Boolean> m) throws MessagingException, OXException {
        for (ListInfo listInfo : IMAPCommandsCollection.listSubfolders(fullName, separator, f)) {
            applySubscriptionStatus(listInfo.name, separator, f, m);
        }

        Boolean b = m.get(fullName);
        if (b == null) {
            LOG.warn("No stored subscription status found for {}", fullName);
            b = Boolean.TRUE;
        }
        IMAPCommandsCollection.forceSetSubscribed(imapStore, fullName, b.booleanValue());
    }

    private IMAPFolder moveFolder(final IMAPFolder toMove, final IMAPFolder destFolder, final String folderName) throws MessagingException, OXException {
        String name = folderName;
        if (name == null) {
            name = getNameOf(toMove);
        }
        final String destFullname = destFolder.getFullName();
        final IMAPFolder newFolder;
        {
            final StringBuilder sb = new StringBuilder();
            if (destFullname.length() > 0) {
                sb.append(destFullname).append(getSeparator(destFolder));
            }
            sb.append(name);
            newFolder = (IMAPFolder) imapStore.getFolder(sb.toString());
        }
        return moveFolder(toMove, destFolder, newFolder, true);
    }

    private IMAPFolder moveFolder(final IMAPFolder toMove, final IMAPFolder destFolder, final IMAPFolder newFolder, final boolean checkForDuplicate) throws MessagingException, OXException {
        String destFullName = destFolder.getFullName();
        char separator;
        {
            ListLsubEntry listEntry = getLISTEntry(destFullName, destFolder);
            if (!listEntry.hasInferiors()) {
                throw IMAPException.create(IMAPException.Code.FOLDER_DOES_NOT_HOLD_FOLDERS, imapConfig, session, destFullName);
            }
            separator = listEntry.getSeparator();
        }

        String moveFullname = toMove.getFullName();
        if (imapConfig.isSupportsACLs() && (getLISTEntry(moveFullname, toMove).hasInferiors())) {
            try {
                if (!imapConfig.getACLExtension().canRead(RightsCache.getCachedRights(toMove, true, session, accountId))) {
                    throw IMAPException.create(IMAPException.Code.NO_READ_ACCESS, imapConfig, session, moveFullname);
                }
            } catch (final MessagingException e) {
                throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, moveFullname);
            }
            try {
                if (!imapConfig.getACLExtension().canCreate(RightsCache.getCachedRights(toMove, true, session, accountId))) {
                    throw IMAPException.create(IMAPException.Code.NO_CREATE_ACCESS, imapConfig, session, moveFullname);
                }
            } catch (final MessagingException e) {
                /*
                 * MYRIGHTS command failed for given mailbox
                 */
                if (!imapConfig.getImapCapabilities().hasNamespace() || !NamespaceFoldersCache.containedInPersonalNamespaces(moveFullname, imapStore, true, session, accountId)) {
                    /*
                     * No namespace support or given parent is NOT covered by user's personal namespaces.
                     */
                    throw IMAPException.create(IMAPException.Code.NO_ACCESS, imapConfig, session, e, moveFullname);
                }
                LOG.debug("MYRIGHTS command failed on namespace folder", e);
            }
        }
        /*
         * Move by creating a new folder, copying all messages and deleting old folder
         */
        if (checkForDuplicate && newFolder.exists()) {
            throw IMAPException.create(IMAPException.Code.DUPLICATE_FOLDER, imapConfig, session, getNameOf(newFolder));
        }
        /*
         * Examine
         */
        Set<String> entityNames = imapConfig.isSupportsACLs() ? new HashSet<String>(8) : null;
        Set<String> oldFullNames = new HashSet<String>(8);
        Map<String, Boolean> subscriptions = new HashMap<String, Boolean>(8);
        gatherFolderInfo(toMove.getFullName(), toMove, moveFullname.length(), newFolder.getFullName(), subscriptions, oldFullNames, entityNames, new StringBuilder(32));
        /*-
         * Check if move operation may be executed through a RENAME command
         * (requires target and destination reside in the same namespace)
         */
        if (isSameNamespace(toMove.getFullName(), newFolder.getFullName())) {
            /*
             * Perform RENAME
             */
            boolean throwException = true;
            try {
                long start = System.currentTimeMillis();
                IMAPCommandsCollection.renameFolder(toMove, separator, newFolder);
                long duration = System.currentTimeMillis() - start;
                throwException = false;
                mailInterfaceMonitor.addUseTime(duration);
            } catch (final MessagingException e) {
                // Rename failed
                throwException = false;
                throw IMAPException.create(IMAPException.Code.FOLDER_CREATION_FAILED, imapConfig, session, e, newFolder.getFullName(), destFolder instanceof DefaultFolder ? DEFAULT_FOLDER_ID : destFullName);
            } finally {
                if (throwException) {
                    throw IMAPException.create(IMAPException.Code.FOLDER_CREATION_FAILED, imapConfig, session, newFolder.getFullName(), destFolder instanceof DefaultFolder ? DEFAULT_FOLDER_ID : destFullName);
                }
            }
        } else {
            /*
             * Perform CREATE for each folder in line, copy its messages and DELETE afterwards
             */
            LinkedList<IMAPFolder> createdOnes = new LinkedList<IMAPFolder>();
            LinkedList<IMAPFolder> copiedOnes = new LinkedList<IMAPFolder>();
            boolean error = true;
            try {
                doRecursiveCopy(toMove, newFolder, getSeparator(toMove.getSeparator()), copiedOnes, createdOnes);
                error = false;

                for (IMAPFolder iapf : copiedOnes) {
                    deleteFolderSafe(iapf);
                }
            } finally {
                if (error) {
                    for (IMAPFolder iapf : createdOnes) {
                        deleteFolderSafe(iapf);
                    }
                }
            }
        }
        /*
         * Apply original subscription status
         */
        for (Entry<String, Boolean> entry : subscriptions.entrySet()) {
            String fullName = entry.getKey();
            try {
                imapStore.getFolder(fullName).setSubscribed(entry.getValue().booleanValue());
            } catch (Exception e) {
                // Restoring subscription status failed
                LOG.warn("Could not restore subscription status for folder {} during IMAP folder move operation", fullName, e);
            }
        }
        /*
         * Delete/unsubscribe old folder
         */
        for (String oldFullName : oldFullNames) {
            IMAPCommandsCollection.forceSetSubscribed(imapStore, oldFullName, false);
        }
        /*
         * Notify message storage
         */
        imapAccess.getMessageStorage().notifyIMAPFolderModification(moveFullname);
        /*
         * Remove cache entries
         */
        // ListLsubCache.clearCache(accountId, session);
        RightsCache.removeCachedRights(moveFullname, session, accountId);
        UserFlagsCache.removeUserFlags(moveFullname, session, accountId);
        if (IMAPSessionStorageAccess.isEnabled()) {
            IMAPSessionStorageAccess.removeDeletedFolder(accountId, session, moveFullname);
        }
        // Affected users, too
        dropListLsubCachesForOther(entityNames);

        return newFolder;
    }

    private void doRecursiveCopy(IMAPFolder toCopy, IMAPFolder newFolder, char separator, LinkedList<IMAPFolder> copiedOnes, LinkedList<IMAPFolder> createdOnes) throws MessagingException, OXException {
        if (!newFolder.create(toCopy.getType())) {
            Folder destFolder = newFolder.getParent();
            throw IMAPException.create(IMAPException.Code.FOLDER_CREATION_FAILED, imapConfig, session, newFolder.getFullName(), destFolder instanceof DefaultFolder ? DEFAULT_FOLDER_ID : destFolder.getFullName());
        }

        // Remember as created
        createdOnes.addFirst(newFolder);

        // Align ACLs (if not moved to trash)
        if (false == isSubfolderOf(newFolder.getFullName(), getTrashFolder(), separator)) {
            ACL[] acls = getACLSafe(toCopy);
            if (null != acls) {
                Entity2ACL entity2ACL = getEntity2ACL();

                for (ACL acl : acls) {
                    String name = acl.getName();
                    Entity2ACLArgs args = IMAPFolderConverter.getEntity2AclArgs(session, toCopy, imapConfig);
                    try {
                        int entityId = entity2ACL.getEntityID(name, ctx, args).getId();

                        args = IMAPFolderConverter.getEntity2AclArgs(session, newFolder, imapConfig);
                        name = entity2ACL.getACLName(entityId, ctx, args);

                        ACL newAcl = new ACL(name, acl.getRights());
                        newFolder.addACL(newAcl);
                    } catch (OXException e) {
                        if (!Entity2ACLExceptionCode.RESOLVE_USER_FAILED.equals(e)) {
                            throw e;
                        }

                        // Take over as-is
                        newFolder.addACL(acl);
                    }
                }
            }
        }

        // Copy all messages
        if (toCopy.getMessageCount() > 0) {
            toCopy.open(Folder.READ_ONLY);
            try {
                new CopyIMAPCommand(toCopy, newFolder.getFullName()).doCommand();
            } finally {
                toCopy.close(false);
            }
        }

        // Remember as copied
        copiedOnes.addFirst(toCopy);

        // Check for subfolders
        for (Folder subfolder : toCopy.list()) {
            IMAPFolder newSubfolder = (IMAPFolder) imapStore.getFolder(newFolder.getFullName() + separator + subfolder.getName());
            doRecursiveCopy((IMAPFolder) subfolder, newSubfolder, separator, copiedOnes, createdOnes);
        }
    }

    private void deleteFolderSafe(final IMAPFolder imapFolder) {
        if (null != imapFolder) {
            try {
                imapFolder.delete(false);
            } catch (Exception x) {
                LOG.error("Temporary created folder could not be deleted: {}", imapFolder.getFullName(), x);
            }
        }
    }

    private void gatherFolderInfo(String fullName, IMAPFolder f, int oldPathLen, String newPath, Map<String, Boolean> subscriptions, Set<String> oldFullNames, Set<String> entityNames, StringBuilder sb) throws MessagingException, OXException {
        if (null != entityNames) {
            extractEntityNames(getACLSafe(fullName, f), entityNames);
        }
        ListLsubEntry testEntry = ListLsubCache.getCachedLISTEntry(fullName, accountId, f, session, ignoreSubscriptions);
        {
            oldFullNames.add(fullName);
            sb.setLength(0);
            String nFullName = sb.append(newPath).append(fullName.substring(oldPathLen)).toString();
            subscriptions.put(nFullName, Boolean.valueOf(testEntry.isSubscribed()));
        }
        for (ListInfo listInfo : IMAPCommandsCollection.listSubfolders(fullName, testEntry.getSeparator(), f)) {
            gatherFolderInfo(listInfo.name, f, oldPathLen, newPath, subscriptions, oldFullNames, entityNames, sb);
        }
    }

    private ACL[] permissions2ACL(OCLPermission[] perms, IMAPFolder imapFolder) throws OXException {
        List<ACL> acls = new ArrayList<ACL>(perms.length);
        Entity2ACLArgs entity2AclArgs = IMAPFolderConverter.getEntity2AclArgs(session, imapFolder, imapConfig);
        for (int i = 0; i < perms.length; i++) {
            ACLPermission aclPermission = getACLPermission(perms[i]);
            try {
                acls.add(aclPermission.getPermissionACL(entity2AclArgs, imapConfig, imapStore, ctx));
            } catch (OXException e) {
                if (Entity2ACLExceptionCode.UNKNOWN_USER.equals(e)) {
                    // Obviously the user is not known, skip
                    LOG.debug("User {} is not known on IMAP server \"{}\"", Integer.valueOf(aclPermission.getEntity()), imapConfig.getImapServerAddress());
                } else if (UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                    // Obviously the user is not known, skip
                    LOG.debug("User 0} is not known on IMAP server \"{}\"", Integer.valueOf(aclPermission.getEntity()), imapConfig.getImapServerAddress());
                } else {
                    throw e;
                }
            }
        }
        return acls.toArray(new ACL[acls.size()]);
    }

    private ACLPermission getACLPermission(OCLPermission permission) {
        if (permission instanceof ACLPermission) {
            return (ACLPermission) permission;
        }
        ACLPermission retval = new ACLPermission();
        retval.setEntity(permission.getEntity());
        retval.setDeleteObjectPermission(permission.getDeletePermission());
        retval.setFolderAdmin(permission.isFolderAdmin());
        retval.setFolderPermission(permission.getFolderPermission());
        retval.setGroupPermission(permission.isGroupPermission());
        retval.setName(permission.getName());
        retval.setReadObjectPermission(permission.getReadPermission());
        retval.setSystem(permission.getSystem());
        retval.setWriteObjectPermission(permission.getWritePermission());
        return retval;
    }

    private static ACL[] getRemovedACLs(final Map<String, ACL> newACLs, final ACL[] oldACLs) {
        final List<ACL> retval = new ArrayList<ACL>();
        for (final ACL oldACL : oldACLs) {
            final ACL newACL = newACLs.get(oldACL.getName());
            if (null == newACL) {
                retval.add(oldACL);
            }
        }
        return retval.toArray(new ACL[retval.size()]);
    }

    private static boolean isKnownEntity(final String entity, final Entity2ACL entity2ACL, final Context ctx, final Entity2ACLArgs args) {
        try {
            return !UserGroupID.NULL.equals(entity2ACL.getEntityID(entity, ctx, args));
        } catch (final OXException e) {
            return false;
        }
    }

    private boolean equals(final ACL[] oldACLs, final Map<String, ACL> newACLs, final Entity2ACL entity2ACL, final Entity2ACLArgs args) {
        int examined = 0;
        for (final ACL oldACL : oldACLs) {
            final String oldName = oldACL.getName();
            if (isKnownEntity(oldName, entity2ACL, ctx, args)) {
                final ACL newACL = newACLs.get(oldName/* .toLowerCase(Locale.ENGLISH) */);
                if (null == newACL) {
                    // No corresponding entity in new ACLs
                    return false;
                }
                // Remember number of corresponding entities
                examined++;
                // Check ACLS' rights ignoring POST right
                if (!equalRights(oldACL.getRights().toString(), newACL.getRights().toString(), true)) {
                    return false;
                }
            }
        }
        return (examined == newACLs.size());
    }

    private void dropListLsubCachesForOther(ACL[] acls) {
        if (null == acls || 0 == acls.length) {
            return;
        }

        UserStorage us = UserStorage.getInstance();
        String myLogin = imapConfig.getLogin();
        int myUserId = session.getUserId();
        for (ACL acl : acls) {
            if (null != acl) {
                String entityName = acl.getName();
                if (!myLogin.equals(entityName)) {
                    try {
                        User[] users = us.searchUserByMailLogin(entityName, ctx);
                        for (User user : users) {
                            int userId = user.getId();
                            if (userId != myUserId) {
                                ListLsubCache.dropFor(userId, ctx.getContextId());
                                MailSessionCache.clearFor(userId, ctx.getContextId());
                            }
                        }
                    } catch (OXException e) {
                        LOG.debug("Could not resolve users for entity name {}", entityName, e);
                    }
                }
            }
        }
    }

    private void dropListLsubCachesForOther(Collection<String> entityNames) {
        if (null == entityNames || entityNames.isEmpty()) {
            return;
        }

        UserStorage us = UserStorage.getInstance();
        String myLogin = imapConfig.getLogin();
        int myUserId = session.getUserId();
        for (String entityName : entityNames) {
            if (null != entityName) {
                if (!myLogin.equals(entityName)) {
                    try {
                        User[] users = us.searchUserByMailLogin(entityName, ctx);
                        for (User user : users) {
                            int userId = user.getId();
                            if (userId != myUserId) {
                                ListLsubCache.dropFor(userId, ctx.getContextId());
                                MailSessionCache.clearFor(userId, ctx.getContextId());
                            }
                        }
                    } catch (OXException e) {
                        LOG.debug("Could not resolve users for entity name {}", entityName, e);
                    }
                }
            }
        }
    }

    private void extractEntityNames(final ACL[] acls, final Collection<String> entityNames) {
        if (null == acls || 0 == acls.length) {
            return;
        }
        for (final ACL acl : acls) {
            if (null != acl) {
                entityNames.add(acl.getName());
            }
        }
    }

    private static char[] stripPOSTRight(final String rights) {
        StringBuilder sb = new StringBuilder(rights.length());
        int length = rights.length();
        for (int i = 0; i < length; i++) {
            char c = rights.charAt(i);
            if ('p' != c && 'P' != c) {
                sb.append(c);
            }
        }

        int len = sb.length();
        char[] retval = new char[len];
        sb.getChars(0, len, retval, 0);
        return retval;
    }

    private static boolean equalRights(final String rights1, final String rights2, final boolean ignorePOST) {
        char[] r1 = ignorePOST ? stripPOSTRight(rights1) : rights1.toCharArray();
        char[] r2 = ignorePOST ? stripPOSTRight(rights2) : rights2.toCharArray();
        if (r1.length != r2.length) {
            return false;
        }
        return new TCharHashSet(r1).containsAll(r2);
    }

    private static Map<String, ACL> acl2map(final ACL[] acls) {
        final Map<String, ACL> m = new HashMap<String, ACL>(acls.length);
        for (final ACL acl : acls) {
            m.put(acl.getName()/* .toLowerCase(Locale.ENGLISH) */, acl);
        }
        return m;
    }

    private static ACL validate(final ACL newACL, final Map<String, ACL> oldACLs) {
        ACL oldACL = oldACLs.get(newACL.getName());
        if (null == oldACL) {
            // Either no corresponding old ACL or old ACL's rights is not equal to "p"
            return newACL;
        }

        Rights newRights = newACL.getRights();
        Rights oldRights = oldACL.getRights();
        if (equalRights(newRights.toString(), oldRights.toString(), true)) {
            // Nothing to do...
            return null;
        }

        // Handle the POST-to-NOT-MAPPABLE problem
        if (oldRights.contains(Rights.Right.POST) && !newRights.contains(Rights.Right.POST)) {
            newRights.add(Rights.Right.POST);
        }

        // Handle the READ-KEEP_SEEN-to-READ problem
        if (oldRights.contains(Rights.Right.READ) && newRights.contains(Rights.Right.READ)) {
            // Both allow READ access
            if (!oldRights.contains(Rights.Right.KEEP_SEEN) && newRights.contains(Rights.Right.KEEP_SEEN)) {
                newRights.remove(Rights.Right.KEEP_SEEN);
            }
        }

        return newACL;
    }

    /*-
     * Determines if <i>altNamespace</i> is enabled for mailbox. If <i>altNamespace</i> is enabled all folder which are logically located
     * below INBOX folder are represented as INBOX's siblings in IMAP folder tree. Dependent on IMAP server's implementation the INBOX
     * folder is then marked with attribute <code>\NoInferiors</code> meaning it no longer allows subfolders.
     *
     * @param imapStore - the IMAP store (mailbox)
     * @return <code>true</code> if altNamespace is enabled; otherwise <code>false</code>
     * @throws MessagingException - if IMAP's NAMESPACE command fails
    private static boolean isPersonalNamespaceEmpty(final IMAPStore imapStore) throws MessagingException {
        boolean altnamespace = false;
        final Folder[] pn = imapStore.getPersonalNamespaces();
        if ((pn.length != 0) && (pn[0].getFullName().trim().length() == 0)) {
            altnamespace = true;
        }
        return altnamespace;
    }*/

    private static final TIntSet WILDCARDS = new TIntHashSet(Arrays.asList(Integer.valueOf('%'), Integer.valueOf('*')));

    private static volatile TIntSet invalidChars;
    private static TIntSet invalidChars() {
        TIntSet tmp = invalidChars;
        if (null == tmp) {
            synchronized (IMAPFolderStorage.class) {
                tmp = invalidChars;
                if (null == tmp) {
                    final ConfigurationService service = Services.getService(ConfigurationService.class);
                    if (null == service) {
                        return new TIntHashSet(0);
                    }
                    final String invalids = service.getProperty("com.openexchange.imap.invalidMailboxNameCharacters");
                    if (isEmpty(invalids)) {
                        tmp = new TIntHashSet(0);
                    } else {
                        final String[] sa = Strings.splitByWhitespaces(Strings.unquote(invalids));
                        final int length = sa.length;
                        tmp = new TIntHashSet(length);
                        for (int i = 0; i < length; i++) {
                            tmp.add(sa[i].charAt(0));
                        }
                    }
                    invalidChars = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Gets a <code>String</code> containing those characters considered as invalid for a mailbox name.
     *
     * @return A <code>String</code> containing invalid characters
     */
    public static String invalidCharsString(final char separator) {
        final TIntSet invalidChars = new TIntHashSet(invalidChars());
        invalidChars.addAll(WILDCARDS);
        invalidChars.add(separator);
        final StringBuilder sb = new StringBuilder(invalidChars.size());
        invalidChars.forEach(new TIntProcedure() {

            @Override
            public boolean execute(int c) {
                sb.append((char) c);
                return true;
            }
        });
        return sb.toString();
    }

    /**
     * Checks id specified folder name is allowed to be used on folder creation. The folder name is valid if the separator character does
     * not appear or provided that MBox format is enabled may only appear at name's end.
     *
     * @param name The folder name to check.
     * @param separator The separator character.
     * @param mboxEnabled <code>true</code> If MBox format is enabled; otherwise <code>false</code>
     * @return <code>true</code> if folder name is valid; otherwise <code>false</code>
     */
    private static boolean checkFolderNameValidity(final String name, final char separator, final boolean mboxEnabled) {
        final TIntProcedure procedure = new TIntProcedure() {

            @Override
            public boolean execute(final int value) {
                return name.indexOf(value) < 0;
            }
        };

        // Check for possibly contained wild-cards
        if (!WILDCARDS.forEach(procedure)) {
            return false;
        }

        // Check for possibly contained invalid characters (as per configuration)
        final TIntSet invalidChars = invalidChars();
        if (null != invalidChars && !invalidChars.isEmpty()) {
            if (!invalidChars.forEach(procedure)) {
                return false;
            }
        }

        // Check for possibly contained separator character (dependent on mbox format)
        final int pos = name.indexOf(separator);
        return mboxEnabled ? (pos < 0) || (pos == name.length() - 1) : (pos < 0);
    }

    private static final String REGEX_TEMPL = "[\\S\\p{Blank}&&[^\\p{Cntrl}#SEP#]]+(?:\\Q#SEP#\\E[\\S\\p{Blank}&&[^\\p{Cntrl}#SEP#]]+)*";

    private static final Pattern PAT_SEP = Pattern.compile("#SEP#");

    private static boolean checkFolderPathValidity(final String path, final char separator) {
        if ((path != null) && (path.length() > 0)) {
            return Pattern.compile(PAT_SEP.matcher(REGEX_TEMPL).replaceAll(String.valueOf(separator))).matcher(path).matches();
        }
        return false;
    }

    private void removeSessionData(String fullName, char sep, IMAPFolder f) {
        if (!IMAPSessionStorageAccess.isEnabled()) {
            return;
        }
        try {
            for (ListInfo listInfo : IMAPCommandsCollection.listSubfolders(fullName, sep, f)) {
                removeSessionData(listInfo.name, sep, f);
            }
            IMAPSessionStorageAccess.removeDeletedFolder(accountId, session, fullName);
        } catch (final MessagingException e) {
            LOG.error("", e);
        }
    }

    private IMAPFolder getIMAPFolder(String fullName) throws MessagingException {
        return DEFAULT_FOLDER_ID.equals(fullName) ? (IMAPFolder) imapStore.getDefaultFolder() : (IMAPFolder) imapStore.getFolder(fullName);
    }

    private IMAPFolder getIMAPFolderWithRecentListener(String fullName) throws MessagingException {
        IMAPFolder ret = DEFAULT_FOLDER_ID.equals(fullName) ? (IMAPFolder) imapStore.getDefaultFolder() : (IMAPFolder) imapStore.getFolder(fullName);
        if (MailAccount.DEFAULT_ID == accountId && imapConfig.getIMAPProperties().notifyRecent()) {
            IMAPNotifierMessageRecentListener.addNotifierFor(ret, fullName, accountId, session, true);
        }
        return ret;
    }

    private boolean doesExist(IMAPFolder imapFolder, boolean mayCheckCache) throws OXException, MessagingException {
        String fullName = imapFolder.getFullName();
        if (STR_INBOX.equals(fullName)) {
            return true;
        }

        if (!mayCheckCache) {
            return imapFolder.exists();
        }

        boolean exists = getLISTEntry(fullName, imapFolder).exists();
        if (!exists) {
            exists = imapFolder.exists();
        }
        return exists;
    }

    private static boolean doesExist(final ListLsubEntry entry) {
        return STR_INBOX.equals(entry.getFullName()) || (entry.exists());
    }

    private ListLsubEntry getLISTEntry(final IMAPFolder imapFolder) throws OXException, MessagingException {
        return getLISTEntry(imapFolder.getFullName(), imapFolder);
    }

    private ListLsubEntry getLISTEntry(final String fullName, final IMAPFolder imapFolder) throws OXException, MessagingException {
        return ListLsubCache.getCachedLISTEntry(fullName, accountId, imapFolder, session, this.ignoreSubscriptions);
    }

    private ListLsubEntry getLSUBEntry(final IMAPFolder imapFolder) throws OXException, MessagingException {
        return getLSUBEntry(imapFolder.getFullName(), imapFolder);
    }

    private ListLsubEntry getLSUBEntry(final String fullName, final IMAPFolder imapFolder) throws OXException, MessagingException {
        return ListLsubCache.getCachedLSUBEntry(fullName, accountId, imapFolder, session, this.ignoreSubscriptions);
    }

    /**
     * Gets the separator character.
     *
     * @param imapStore The IMAP store
     * @return The separator character
     * @throws OXException If an error occurs
     */
    public char getSeparator(final IMAPStore imapStore) throws OXException {
        try {
            return getLISTEntry(STR_INBOX, (IMAPFolder) imapStore.getDefaultFolder()).getSeparator();
        } catch (final MessagingException e) {
            throw handleMessagingException(e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    /**
     * Gets the separator character.
     *
     * @param imapFolder The IMAP folder
     * @return The separator character
     * @throws OXException If an error occurs
     */
    public char getSeparator(final IMAPFolder imapFolder) throws OXException {
        try {
            return getLISTEntry(STR_INBOX, imapFolder).getSeparator();
        } catch (final MessagingException e) {
            throw handleMessagingException(imapFolder.getFullName(), e);
        } catch (final RuntimeException e) {
            throw handleRuntimeException(e);
        }
    }

    private String getNameOf(final IMAPFolder imapFolder) throws OXException {
        final String fullName = imapFolder.getFullName();
        return fullName.substring(fullName.lastIndexOf(getSeparator(imapFolder)) + 1);
    }

    private OXException handleRuntimeException(final RuntimeException e) {
        if (e instanceof ListLsubRuntimeException) {
            ListLsubCache.clearCache(accountId, session);
            return MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        }
        return MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
    }

    private static boolean isSubfolderOf(final String fullName, final String possibleParent, final char separator) {
        if (!fullName.startsWith(possibleParent)) {
            return false;
        }
        final int length = possibleParent.length();
        if (length >= fullName.length()) {
            return true;
        }
        return fullName.charAt(length) == separator;
    }

    private boolean isSameNamespace(String fullName1, String fullName2) throws MessagingException {
        if (NamespaceFoldersCache.startsWithAnyOfUserNamespaces(fullName1, imapStore, true, session, accountId)) {
            return NamespaceFoldersCache.startsWithAnyOfUserNamespaces(fullName2, imapStore, true, session, accountId);
        } else if (NamespaceFoldersCache.startsWithAnyOfUserNamespaces(fullName2, imapStore, true, session, accountId)) {
            return false;
        }

        if (NamespaceFoldersCache.startsWithAnyOfSharedNamespaces(fullName1, imapStore, true, session, accountId)) {
            return NamespaceFoldersCache.startsWithAnyOfSharedNamespaces(fullName2, imapStore, true, session, accountId);
        } else if (NamespaceFoldersCache.startsWithAnyOfSharedNamespaces(fullName2, imapStore, true, session, accountId)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if specified full name starts with either a user or a shared namespace path prefix.
     *
     * @param fullName The full name to check
     * @return <code>true</code> if denoted full name is a namespace subfolder; otherwise <code>false</code>
     * @throws MessagingException If operation fails
     */
    public boolean startsWithNamespaceFolder(String fullName) throws MessagingException {
        return startsWithNamespaceFolder(fullName, getSeparator());
    }

    private boolean startsWithNamespaceFolder(String fullName, char separator) throws MessagingException {
        for (final String nsFullName : NamespaceFoldersCache.getUserNamespaces(imapStore, true, session, accountId)) {
            if (isSubfolderOf(fullName, nsFullName, separator)) {
                return true;
            }
        }
        for (final String nsFullName : NamespaceFoldersCache.getSharedNamespaces(imapStore, true, session, accountId)) {
            if (isSubfolderOf(fullName, nsFullName, separator)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, Object> mapFor(final String... pairs) {
        if (null == pairs) {
            return null;
        }
        final int length = pairs.length;
        if (0 == length || (length % 2) != 0) {
            return null;
        }
        final Map<String, Object> map = new HashMap<String, Object>(length >> 1);
        for (int i = 0; i < length; i+=2) {
            map.put(pairs[i], pairs[i+1]);
        }
        return map;
    }

    /** Check for an empty string */
    private static boolean isEmpty(final String string) {
        return Strings.isEmpty(string);
    }

    private static final class FullDisplayNameComparator implements Comparator<MailFolderInfo> {

        private final Collator collator;

        FullDisplayNameComparator(final Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale);
        }

        @Override
        public int compare(final MailFolderInfo o1, final MailFolderInfo o2) {
            /*
             * Compare by full name
             */
            return collator.compare(o1.getFullDisplayName(), o2.getFullDisplayName());
        }

    } // End of FullNameComparator

    private static String urlName(final String prefix, final String host, final int port, final String username, final String pw) {
        // Start with "protocol:"
        final StringBuilder tempURL = new StringBuilder(128);

        if (null != prefix) {
            tempURL.append(prefix);
        }

        tempURL.append("imap://");

        // Add the user:password@
        if (username != null) {
            tempURL.append(username);
            if (pw != null) {
                tempURL.append(':');
                tempURL.append(pw);
            }
            tempURL.append('@');
        }

        // Add host
        if (host != null) {
            tempURL.append(host);
        }

        // Add port
        if (port > 0) {
            tempURL.append(':');
            tempURL.append(Integer.toString(port));
        }

        return tempURL.toString();
    }

}
