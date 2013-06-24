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

package com.openexchange.mail;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Collator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.idn.IDNA;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.contact.ContactService;
import com.openexchange.dataretention.DataRetentionService;
import com.openexchange.dataretention.RetentionData;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.importexport.MailImportResult;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.upload.quotachecker.MailUploadQuotaChecker;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.StringAllocator;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageEnhanced;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageBatch;
import com.openexchange.mail.api.IMailMessageStorageExt;
import com.openexchange.mail.api.ISimplifiedThreadStructure;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.cache.MailMessageCache;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.event.EventPool;
import com.openexchange.mail.event.PooledEvent;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.processing.MimeForward;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.NonInlineForwardPartHandler;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.mail.search.HeaderTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.SearchUtility;
import com.openexchange.mail.search.service.SearchTermMapper;
import com.openexchange.mail.threader.Conversation;
import com.openexchange.mail.threader.Conversations;
import com.openexchange.mail.threader.ThreadableMapping;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.MailMessageComparator;
import com.openexchange.mail.utils.MsisdnUtility;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.internal.RdbMailAccountStorage;
import com.openexchange.push.PushEventConstants;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.openexchange.threadpool.AbstractTrackableTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorDelegator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.sql.SearchStrings;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.user.UserService;

/**
 * {@link MailServletInterfaceImpl} - The mail servlet interface implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class MailServletInterfaceImpl extends MailServletInterface {

    private static final MailField[] FIELDS_FULL = new MailField[] { MailField.FULL };

    private static final MailField[] FIELDS_ID_INFO = new MailField[] { MailField.ID, MailField.FOLDER_ID };

    private static final MailField[] HEADERS = { MailField.ID, MailField.HEADERS };

    private static final String LAST_SEND_TIME = "com.openexchange.mail.lastSendTimestamp";

    private static final String[] STR_ARR = new String[0];

    private static final String INBOX_ID = "INBOX";

    private static final int MAX_NUMBER_OF_MESSAGES_2_CACHE = 50;

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailServletInterfaceImpl.class));

    private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    /*-
     * ++++++++++++++ Fields ++++++++++++++
     */

    private final Context ctx;

    private final int contextId;

    private boolean init;

    private MailConfig mailConfig;

    private MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess;

    private int accountId;

    /**
     * The session instance.
     */
    final Session session;

    private final UserSettingMail usm;

    private Locale locale;

    private User user;

    private final Collection<OXException> warnings;

    private final ArrayList<MailImportResult> mailImportResults;

    private MailAccount mailAccount;

    /**
     * Initializes a new {@link MailServletInterfaceImpl}.
     *
     * @throws OXException If user has no mail access or properties cannot be successfully loaded
     */
    MailServletInterfaceImpl(final Session session) throws OXException {
        super();
        warnings = new ArrayList<OXException>(2);
        mailImportResults = new ArrayList<MailImportResult>();
        if (session instanceof ServerSession) {
            final ServerSession serverSession = (ServerSession) session;
            ctx = serverSession.getContext();
            usm = serverSession.getUserSettingMail();
            if (!serverSession.getUserConfiguration().hasWebMail()) {
                throw MailExceptionCode.NO_MAIL_ACCESS.create();
            }
            user = serverSession.getUser();
        } else {
            ctx = ContextStorage.getInstance().getContext(session.getContextId());
            usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
            if (!UserConfigurationStorage.getInstance().getUserConfiguration(session.getUserId(), ctx).hasWebMail()) {
                throw MailExceptionCode.NO_MAIL_ACCESS.create();
            }
        }
        this.session = session;
        contextId = session.getContextId();
    }

    private User getUser() {
        if (null == user) {
            user = UserStorage.getStorageUser(session.getUserId(), session.getContextId());
        }
        return user;
    }

    private Locale getUserLocale() {
        if (null == locale) {
            final UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class);
            if (null == userService) {
                return Locale.ENGLISH;
            }
            try {
                locale = userService.getUser(session.getUserId(), ctx).getLocale();
            } catch (final OXException e) {
                LOG.warn(e.getMessage(), e);
                return Locale.ENGLISH;
            }
        }
        return locale;
    }

    private MailAccount getMailAccount() throws OXException {
        if (mailAccount == null) {
            try {
                final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
                mailAccount = storageService.getMailAccount(accountId, session.getUserId(), session.getContextId());
            } catch (final RuntimeException e) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return mailAccount;
    }

    @Override
    public Collection<OXException> getWarnings() {
        return Collections.unmodifiableCollection(warnings);
    }

    /**
     * The fields containing only the mail identifier.
     */
    private static final MailField[] FIELDS_ID = new MailField[] { MailField.ID };

    @Override
    public boolean expungeFolder(final String folder, final boolean hardDelete) throws OXException {
        final FullnameArgument fullnameArgument = prepareMailFolderParam(folder);
        final int accountId = fullnameArgument.getAccountId();
        initConnection(accountId);
        final String fullName = fullnameArgument.getFullname();
        final IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
        if (folderStorage instanceof IMailFolderStorageEnhanced) {
            ((IMailFolderStorageEnhanced) folderStorage).expungeFolder(fullName, hardDelete);
        } else {
            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            final MailMessage[] messages = messageStorage.searchMessages(
                fullName,
                IndexRange.NULL,
                MailSortField.RECEIVED_DATE,
                OrderDirection.ASC,
                new FlagTerm(MailMessage.FLAG_DELETED, true),
                FIELDS_ID);
            final List<String> mailIds = new ArrayList<String>(messages.length);
            for (final MailMessage mailMessage : messages) {
                if (null != mailMessage) {
                    mailIds.add(mailMessage.getMailId());
                }
            }
            if (hardDelete) {
                messageStorage.deleteMessages(fullName, mailIds.toArray(new String[0]), true);
            } else {
                final String trashFolder = folderStorage.getTrashFolder();
                if (fullName.equals(trashFolder)) {
                    // Also perform hard-delete when compacting trash folder
                    messageStorage.deleteMessages(fullName, mailIds.toArray(new String[0]), true);
                } else {
                    messageStorage.moveMessages(fullName, trashFolder, mailIds.toArray(new String[0]), true);
                }
            }
        }
        postEvent(accountId, fullName, true);
        final String trashFullname = prepareMailFolderParam(getTrashFolder(accountId)).getFullname();
        if (!hardDelete) {
            postEvent(accountId, trashFullname, true);
        }
        return true;
    }

    @Override
    public boolean clearFolder(final String folder) throws OXException {
        final FullnameArgument fullnameArgument = prepareMailFolderParam(folder);
        final int accountId = fullnameArgument.getAccountId();
        initConnection(accountId);
        final String fullName = fullnameArgument.getFullname();
        /*
         * Only backup if no hard-delete is set in user's mail configuration and fullName does not denote trash (sub)folder
         */
        final boolean backup = (!UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isHardDeleteMsgs() && !(fullName.startsWith(mailAccess.getFolderStorage().getTrashFolder())));
        mailAccess.getFolderStorage().clearFolder(fullName, !backup);
        postEvent(accountId, fullName, true);
        final String trashFullname = prepareMailFolderParam(getTrashFolder(accountId)).getFullname();
        if (backup) {
            postEvent(accountId, trashFullname, true);
        }
        try {
            /*
             * Update message cache
             */
            MailMessageCache.getInstance().removeFolderMessages(accountId, fullName, session.getUserId(), contextId);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
        if (fullName.startsWith(trashFullname)) {
            // Special handling
            final MailFolder[] subf = mailAccess.getFolderStorage().getSubfolders(fullName, true);
            for (MailFolder element : subf) {
                final String subFullname = element.getFullname();
                mailAccess.getFolderStorage().deleteFolder(subFullname, true);
                postEvent(accountId, subFullname, false);
            }
            postEvent(accountId, trashFullname, false);
        }
        return true;
    }

    @Override
    public boolean clearFolder(final String folder, final boolean hardDelete) throws OXException {
        final FullnameArgument fullnameArgument = prepareMailFolderParam(folder);
        final int accountId = fullnameArgument.getAccountId();
        initConnection(accountId);
        final String fullName = fullnameArgument.getFullname();
        /*
         * Only backup if no hard-delete is set in user's mail configuration and fullName does not denote trash (sub)folder
         */
        final boolean backup;
        if (hardDelete) {
            backup = false;
        } else {
            backup = (!UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isHardDeleteMsgs() && !(fullName.startsWith(mailAccess.getFolderStorage().getTrashFolder())));
        }
        mailAccess.getFolderStorage().clearFolder(fullName, !backup);
        postEvent(accountId, fullName, true);
        final String trashFullname = prepareMailFolderParam(getTrashFolder(accountId)).getFullname();
        if (backup) {
            postEvent(accountId, trashFullname, true);
        }
        try {
            /*
             * Update message cache
             */
            MailMessageCache.getInstance().removeFolderMessages(accountId, fullName, session.getUserId(), contextId);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
        if (fullName.startsWith(trashFullname)) {
            // Special handling
            final MailFolder[] subf = mailAccess.getFolderStorage().getSubfolders(fullName, true);
            for (MailFolder element : subf) {
                final String subFullname = element.getFullname();
                mailAccess.getFolderStorage().deleteFolder(subFullname, true);
                postEvent(accountId, subFullname, false);
            }
            postEvent(accountId, trashFullname, false);
        }
        return true;
    }

    @Override
    public void close(final boolean putIntoCache) throws OXException {
        try {
            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = this.mailAccess;
            if (mailAccess != null) {
                mailAccess.close(putIntoCache);
            }
        } finally {
            mailAccess = null;
            init = false;
        }
    }

    private static final int SPAM_HAM = -1;

    private static final int SPAM_NOOP = 0;

    private static final int SPAM_SPAM = 1;

    @Override
    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] msgUIDs, final boolean move) throws OXException {
        final FullnameArgument source = prepareMailFolderParam(sourceFolder);
        final FullnameArgument dest = prepareMailFolderParam(destFolder);
        final String sourceFullname = source.getFullname();
        final String destFullname = dest.getFullname();
        final int sourceAccountId = source.getAccountId();
        initConnection(sourceAccountId);
        final int destAccountId = dest.getAccountId();
        if (sourceAccountId == destAccountId) {
            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            MailMessage[] flagInfo = null;
            if (move) {
                /*
                 * Check for spam action; meaning a move/copy from/to spam folder
                 */
                final String spamFullname = mailAccess.getFolderStorage().getSpamFolder();
                final int spamAction;
                if (usm.isSpamEnabled()) {
                    spamAction = spamFullname.equals(sourceFullname) ? SPAM_HAM : (spamFullname.equals(destFullname) ? SPAM_SPAM : SPAM_NOOP);
                } else {
                    spamAction = SPAM_NOOP;
                }
                if (spamAction != SPAM_NOOP) {
                    if (spamAction == SPAM_SPAM) {
                        flagInfo = messageStorage.getMessages(sourceFullname, msgUIDs, new MailField[] { MailField.FLAGS });
                        /*
                         * Handle spam
                         */
                        SpamHandlerRegistry.getSpamHandlerBySession(session, accountId).handleSpam(
                            accountId,
                            sourceFullname,
                            msgUIDs,
                            false,
                            session);
                    } else {
                        flagInfo = messageStorage.getMessages(sourceFullname, msgUIDs, new MailField[] { MailField.FLAGS });
                        /*
                         * Handle ham.
                         */
                        SpamHandlerRegistry.getSpamHandlerBySession(session, accountId).handleHam(
                            accountId,
                            sourceFullname,
                            msgUIDs,
                            false,
                            session);
                    }
                }
            }
            final String[] maildIds;
            if (move) {
                maildIds = messageStorage.moveMessages(sourceFullname, destFullname, msgUIDs, false);
                postEvent(sourceAccountId, sourceFullname, true);
            } else {
                maildIds = messageStorage.copyMessages(sourceFullname, destFullname, msgUIDs, false);
            }
            /*
             * Restore \Seen flags
             */
            if (null != flagInfo) {
                final List<String> list = new ArrayList<String>(maildIds.length >> 1);
                for (int i = 0; i < maildIds.length; i++) {
                    if (!flagInfo[i].isSeen()) {
                        list.add(maildIds[i]);
                    }
                }
                messageStorage.updateMessageFlags(destFullname, list.toArray(new String[list.size()]), MailMessage.FLAG_SEEN, false);
            }
            postEvent(sourceAccountId, destFullname, true);
            try {
                /*
                 * Update message cache
                 */
                if (move) {
                    MailMessageCache.getInstance().removeFolderMessages(sourceAccountId, sourceFullname, session.getUserId(), contextId);
                }
                MailMessageCache.getInstance().removeFolderMessages(destAccountId, destFullname, session.getUserId(), contextId);
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
            return maildIds;
        }
        /*
         * Differing accounts...
         */
        final MailAccess<?, ?> destAccess = initMailAccess(destAccountId);
        try {
            MailMessage[] flagInfo = null;
            if (move) {
                /*
                 * Check for spam action; meaning a move/copy from/to spam folder
                 */
                int spamActionSource = SPAM_NOOP;
                int spamActionDest = SPAM_NOOP;
                if (usm.isSpamEnabled()) {
                    if (sourceFullname.equals(mailAccess.getFolderStorage().getSpamFolder())) {
                        spamActionSource = SPAM_HAM;
                    }
                    if (destFullname.equals(destAccess.getFolderStorage().getSpamFolder())) {
                        spamActionDest = SPAM_SPAM;
                    }
                }
                if (SPAM_HAM == spamActionSource) {
                    flagInfo = mailAccess.getMessageStorage().getMessages(sourceFullname, msgUIDs, new MailField[] { MailField.FLAGS });
                    /*
                     * Handle ham.
                     */
                    SpamHandlerRegistry.getSpamHandlerBySession(session, accountId).handleHam(
                        accountId,
                        sourceFullname,
                        msgUIDs,
                        false,
                        session);
                }
                if (SPAM_SPAM == spamActionDest) {
                    flagInfo = mailAccess.getMessageStorage().getMessages(sourceFullname, msgUIDs, new MailField[] { MailField.FLAGS });
                    /*
                     * Handle spam
                     */
                    SpamHandlerRegistry.getSpamHandlerBySession(session, accountId).handleSpam(
                        accountId,
                        sourceFullname,
                        msgUIDs,
                        false,
                        session);
                }
            }
            // Fetch messages from source folder
            final MailMessage[] messages = mailAccess.getMessageStorage().getMessages(sourceFullname, msgUIDs, FIELDS_FULL);
            // Append them to destination folder
            final String[] maildIds = destAccess.getMessageStorage().appendMessages(destFullname, messages);
            // Delete source messages if a move shall be performed
            if (move) {
                mailAccess.getMessageStorage().deleteMessages(sourceFullname, messages2ids(messages), true);
                postEvent(sourceAccountId, sourceFullname, true);
            }
            /*
             * Restore \Seen flags
             */
            if (null != flagInfo) {
                final List<String> list = new ArrayList<String>(maildIds.length >> 1);
                for (int i = 0; i < maildIds.length; i++) {
                    if (!flagInfo[i].isSeen()) {
                        list.add(maildIds[i]);
                    }
                }
                destAccess.getMessageStorage().updateMessageFlags(
                    destFullname,
                    list.toArray(new String[list.size()]),
                    MailMessage.FLAG_SEEN,
                    false);
            }
            postEvent(destAccountId, destFullname, true);
            try {
                if (move) {
                    /*
                     * Update message cache
                     */
                    MailMessageCache.getInstance().removeFolderMessages(sourceAccountId, sourceFullname, session.getUserId(), contextId);
                }
                MailMessageCache.getInstance().removeFolderMessages(destAccountId, destFullname, session.getUserId(), contextId);
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
            return maildIds;
        } finally {
            destAccess.close(true);
        }
    }

    @Override
    public String deleteFolder(final String folder) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        /*
         * Only backup if fullName does not denote trash (sub)folder
         */
        final IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
        final String trashFullname = folderStorage.getTrashFolder();
        final boolean hardDelete = fullName.startsWith(trashFullname);
        /*
         * Remember subfolder tree
         */
        final Map<String, Map<?, ?>> subfolders = subfolders(fullName);
        final String retval = prepareFullname(accountId, folderStorage.deleteFolder(fullName, hardDelete));
        postEvent(accountId, fullName, false, true, false);
        try {
            /*
             * Update message cache
             */
            MailMessageCache.getInstance().removeFolderMessages(accountId, fullName, session.getUserId(), contextId);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
        if (!hardDelete) {
            // New folder in trash folder
            postEvent(accountId, trashFullname, false);
        }
        postEvent4Subfolders(accountId, subfolders);
        return retval;
    }

    private void postEvent4Subfolders(final int accountId, final Map<String, Map<?, ?>> subfolders) {
        final int size = subfolders.size();
        final Iterator<Entry<String, Map<?, ?>>> iter = subfolders.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            final Entry<String, Map<?, ?>> entry = iter.next();
            final @SuppressWarnings("unchecked") Map<String, Map<?, ?>> m = (Map<String, Map<?, ?>>) entry.getValue();
            if (!m.isEmpty()) {
                postEvent4Subfolders(accountId, m);
            }
            postEvent(accountId, entry.getKey(), false);
        }
    }

    private Map<String, Map<?, ?>> subfolders(final String fullName) throws OXException {
        final Map<String, Map<?, ?>> m = new HashMap<String, Map<?, ?>>();
        subfoldersRecursively(fullName, m);
        return m;
    }

    private void subfoldersRecursively(final String parent, final Map<String, Map<?, ?>> m) throws OXException {
        final MailFolder[] mailFolders = mailAccess.getFolderStorage().getSubfolders(parent, true);
        if (null == mailFolders || 0 == mailFolders.length) {
            final Map<String, Map<?, ?>> emptyMap = Collections.emptyMap();
            m.put(parent, emptyMap);
        } else {
            final Map<String, Map<?, ?>> subMap = new HashMap<String, Map<?, ?>>();
            final int size = mailFolders.length;
            for (int i = 0; i < size; i++) {
                final String fullName = mailFolders[i].getFullname();
                subfoldersRecursively(fullName, subMap);
            }
            m.put(parent, subMap);
        }
    }

    @Override
    public boolean deleteMessages(final String folder, final String[] msgUIDs, final boolean hardDelete) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        /*
         * Hard-delete if hard-delete is set in user's mail configuration or fullName denotes trash (sub)folder
         */
        final String trashFullname = mailAccess.getFolderStorage().getTrashFolder();
        final boolean hd = (hardDelete || UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isHardDeleteMsgs() || (null != trashFullname && fullName.startsWith(trashFullname)));
        mailAccess.getMessageStorage().deleteMessages(fullName, msgUIDs, hd);
        try {
            /*
             * Update message cache
             */
            MailMessageCache.getInstance().removeFolderMessages(accountId, fullName, session.getUserId(), contextId);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
        postEvent(accountId, fullName, true, true, false);
        if (!hd) {
            postEvent(accountId, trashFullname, true, true, false);
        }
        return true;
    }

    @Override
    public int[] getAllMessageCount(final String folder) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        initConnection(argument.getAccountId());
        final String fullName = argument.getFullname();
        final IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
        final MailFolder f = folderStorage.getFolder(fullName);
        if (folderStorage instanceof IMailFolderStorageEnhanced) {
            final IMailFolderStorageEnhanced storageEnhanced = (IMailFolderStorageEnhanced) folderStorage;
            final int totalCounter = storageEnhanced.getTotalCounter(fullName);
            final int unreadCounter = storageEnhanced.getUnreadCounter(fullName);
            final int newCounter = storageEnhanced.getNewCounter(fullName);
            return new int[] { totalCounter, newCounter, unreadCounter, f.getDeletedMessageCount() };
        }
        final int totalCounter = mailAccess.getMessageStorage().searchMessages(fullName, IndexRange.NULL, MailSortField.RECEIVED_DATE, OrderDirection.ASC, null, FIELDS_ID).length;
        final int unreadCounter =  mailAccess.getMessageStorage().getUnreadMessages(fullName, MailSortField.RECEIVED_DATE, OrderDirection.DESC, FIELDS_ID, -1).length;
        return new int[] { totalCounter, f.getNewMessageCount(), unreadCounter, f.getDeletedMessageCount() };
    }

    @Override
    public SearchIterator<MailMessage> getAllMessages(final String folder, final int sortCol, final int order, final int[] fields, final int[] fromToIndices) throws OXException {
        return getMessages(folder, fromToIndices, sortCol, order, null, null, false, fields);
    }

    private static final MailMessageComparator COMPARATOR_DESC = new MailMessageComparator(MailSortField.RECEIVED_DATE, true, null);

    @Override
    public List<List<MailMessage>> getAllSimpleThreadStructuredMessages(final String folder, final boolean includeSent, final boolean cache, final int sortCol, final int order, final int[] fields, final int[] fromToIndices, final long max) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        final boolean mergeWithSent = includeSent && !mailAccess.getFolderStorage().getSentFolder().equals(fullName);
        final MailFields mailFields = new MailFields(MailField.getFields(fields));
        mailFields.add(MailField.toField(MailListField.getField(sortCol)));
        // Check message storage
        final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
        if (messageStorage instanceof ISimplifiedThreadStructure) {
            final ISimplifiedThreadStructure simplifiedThreadStructure = (ISimplifiedThreadStructure) messageStorage;
            // Effective fields
            // Perform operation
            try {
                return simplifiedThreadStructure.getThreadSortedMessages(
                    fullName,
                    mergeWithSent,
                    cache,
                    null == fromToIndices ? IndexRange.NULL : new IndexRange(fromToIndices[0], fromToIndices[1]),
                    max,
                    MailSortField.getField(sortCol),
                    OrderDirection.getOrderDirection(order),
                    mailFields.toArray());
            } catch (final OXException e) {
                // Check for missing "THREAD=REFERENCES" capability
                if ((2046 != e.getCode() || (!"MSG".equals(e.getPrefix()) && !"IMAP".equals(e.getPrefix()))) && !MailExceptionCode.UNSUPPORTED_OPERATION.equals(e)) {
                    throw e;
                }
            }
        }
        /*
         * Sort by references
         */
        final String sentFolder = mailAccess.getFolderStorage().getSentFolder();
        final Future<ThreadableMapping> submittedTask = mergeWithSent ? getThreadableMapping(sentFolder, (int) max, mailFields, messageStorage) : null;
        final List<Conversation> conversations = Conversations.conversationsFor(fullName, (int) max, mailFields, messageStorage);
        Conversations.fold(conversations);
        // Comparator
        final MailMessageComparator threadComparator = COMPARATOR_DESC;
        // Sort
        List<List<MailMessage>> list = new ArrayList<List<MailMessage>>(conversations.size());
        for (final Conversation conversation : conversations) {
            list.add(conversation.getMessages(threadComparator));
        }
        // Sort root elements
        {
            final MailSortField sortField = MailSortField.getField(sortCol);
            final MailSortField effectiveSortField = null == sortField ? MailSortField.RECEIVED_DATE : sortField;
            final Comparator<List<MailMessage>> listComparator = getListComparator(effectiveSortField, OrderDirection.getOrderDirection(order), getUserLocale());
            Collections.sort(list, listComparator);
        }
        // Check for available mapping indicating that sent folder results have to be merged
        if (null != submittedTask) {
            final ThreadableMapping threadableMapping = getFrom(submittedTask);
            for (final List<MailMessage> thread : list) {
                if (threadableMapping.checkFor(new ArrayList<MailMessage>(thread), thread)) { // Iterate over copy
                    // Re-Sort thread
                    Collections.sort(thread, threadComparator);
                }
            }
        }
        IndexRange indexRange = null == fromToIndices ? IndexRange.NULL : new IndexRange(fromToIndices[0], fromToIndices[1]);
        if (null != indexRange) {
            final int fromIndex = indexRange.start;
            int toIndex = indexRange.end;
            final int size = list.size();
            if ((fromIndex) > size) {
                // Return empty iterator if start is out of range
                return Collections.emptyList();
            }
            // Reset end index if out of range
            if (toIndex >= size) {
                toIndex = size;
            }
            list = list.subList(fromIndex, toIndex);
        }
        /*
         * Apply account identifier
         */
        setAccountInfo2(list);
        // Return list
        return list;
    }

    private static Future<ThreadableMapping> getThreadableMapping(final String sentFolder, final int limit, final MailFields mailFields, final IMailMessageStorage messageStorage) {
        final Props props = LogProperties.optLogProperties(Thread.currentThread());
        final Task<ThreadableMapping> task = new AbstractTrackableTask<ThreadableMapping>() {

            @Override
            public ThreadableMapping call() throws Exception {
                final List<MailMessage> mails = Conversations.messagesFor(sentFolder, limit, mailFields, messageStorage);
                return new ThreadableMapping(64).initWith(mails);
            }

            @Override
            public Props optLogProperties() {
                return props;
            }

        };
        return ThreadPools.getThreadPool().submit(task, CallerRunsBehavior.<ThreadableMapping> getInstance());
    }

    private Comparator<List<MailMessage>> getListComparator(final MailSortField sortField, final OrderDirection order, final Locale locale) {
        final MailMessageComparator comparator = new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), locale);
        final Comparator<List<MailMessage>> listComparator = new Comparator<List<MailMessage>>() {

            @Override
            public int compare(final List<MailMessage> o1, final List<MailMessage> o2) {
                int result = comparator.compare(o1.get(0), o2.get(0));
                if ((0 != result) || (MailSortField.RECEIVED_DATE != sortField)) {
                    return result;
                }
                // Zero as comparison result AND primarily sorted by received-date
                final MailMessage msg1 = o1.get(0);
                final MailMessage msg2 = o2.get(0);
                final String inReplyTo1 = msg1.getInReplyTo();
                final String inReplyTo2 = msg2.getInReplyTo();
                if (null == inReplyTo1) {
                    result = null == inReplyTo2 ? 0 : -1;
                } else {
                    result = null == inReplyTo2 ? 1 : 0;
                }
                return 0 == result ? new MailMessageComparator(MailSortField.SENT_DATE, OrderDirection.DESC.equals(order), null).compare(msg1, msg2) : result;
            }
        };
        return listComparator;
    }

    private static <T> T getFrom(final Future<T> f) throws OXException {
        if (null == f) {
            return null;
        }
        try {
            return f.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt(); // Keep interrupted state
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof MessagingException) {
                throw MimeMailException.handleMessagingException((MessagingException) cause);
            }
            throw ThreadPools.launderThrowable(e, OXException.class);
        }

    }

    /**
     * Sets account ID and name in given instances of {@link MailMessage}.
     *
     * @param mailMessages The {@link MailMessage} instances
     * @return The given instances of {@link MailMessage} each with account ID and name set
     * @throws OXException If mail account cannot be obtained
     */
    private <C extends Collection<MailMessage>, W extends Collection<C>> W setAccountInfo2(final W col) throws OXException {
        final MailAccount account = getMailAccount();
        final String name = account.getName();
        final int id = account.getId();
        for (final C mailMessages : col) {
            for (final MailMessage mailMessage : mailMessages) {
                if (null != mailMessage) {
                    mailMessage.setAccountId(id);
                    mailMessage.setAccountName(name);
                }
            }
        }
        return col;
    }

    @Override
    public SearchIterator<MailMessage> getAllThreadedMessages(final String folder, final int sortCol, final int order, final int[] fields, final int[] fromToIndices) throws OXException {
        return getThreadedMessages(folder, fromToIndices, sortCol, order, null, null, false, fields);
    }

    @Override
    public SearchIterator<MailFolder> getChildFolders(final String parentFolder, final boolean all) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(parentFolder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String parentFullname = argument.getFullname();
        final List<MailFolder> children = new ArrayList<MailFolder>(Arrays.asList(mailAccess.getFolderStorage().getSubfolders(
            parentFullname,
            all)));
        if (children.isEmpty()) {
            return SearchIteratorAdapter.emptyIterator();
        }
        /*
         * Filter against possible POP3 storage folders
         */
        if (MailAccount.DEFAULT_ID == accountId && MailProperties.getInstance().isHidePOP3StorageFolders()) {
            final Set<String> pop3StorageFolders = RdbMailAccountStorage.getPOP3StorageFolders(session);
            for (final Iterator<MailFolder> it = children.iterator(); it.hasNext();) {
                final MailFolder mailFolder = it.next();
                if (pop3StorageFolders.contains(mailFolder.getFullname())) {
                    it.remove();
                }
            }
        }
        /*
         * Check if denoted parent can hold default folders like Trash, Sent, etc.
         */
        if (!MailFolder.DEFAULT_FOLDER_ID.equals(parentFullname) && !INBOX_ID.equals(parentFullname)) {
            /*
             * Denoted parent is not capable to hold default folders. Therefore output as it is.
             */
            Collections.sort(children, new SimpleMailFolderComparator(getUserLocale()));
            return new SearchIteratorDelegator<MailFolder>(children.iterator(), children.size());
        }
        /*
         * Ensure default folders are at first positions
         */
        final String[] names;
        if (isDefaultFoldersChecked(accountId)) {
            names = getSortedDefaultMailFolders(accountId);
        } else {
            final List<String> tmp = new ArrayList<String>();

            FullnameArgument fa = prepareMailFolderParam(getInboxFolder(accountId));
            if (null != fa) {
                tmp.add(fa.getFullname());
            }

            fa = prepareMailFolderParam(getDraftsFolder(accountId));
            if (null != fa) {
                tmp.add(fa.getFullname());
            }

            fa = prepareMailFolderParam(getSentFolder(accountId));
            if (null != fa) {
                tmp.add(fa.getFullname());
            }

            fa = prepareMailFolderParam(getSpamFolder(accountId));
            if (null != fa) {
                tmp.add(fa.getFullname());
            }

            fa = prepareMailFolderParam(getTrashFolder(accountId));
            if (null != fa) {
                tmp.add(fa.getFullname());
            }

            names = tmp.toArray(new String[tmp.size()]);
        }
        /*
         * Sort them
         */
        Collections.sort(children, new MailFolderComparator(names, getUserLocale()));
        return new SearchIteratorDelegator<MailFolder>(children.iterator(), children.size());
    }

    @Override
    public String getConfirmedHamFolder(final int accountId) throws OXException {
        if (isDefaultFoldersChecked(accountId)) {
            return prepareFullname(accountId, getDefaultMailFolder(StorageUtility.INDEX_CONFIRMED_HAM, accountId));
        }
        initConnection(accountId);
        return prepareFullname(accountId, mailAccess.getFolderStorage().getConfirmedHamFolder());
    }

    @Override
    public String getConfirmedSpamFolder(final int accountId) throws OXException {
        if (isDefaultFoldersChecked(accountId)) {
            return prepareFullname(accountId, getDefaultMailFolder(StorageUtility.INDEX_CONFIRMED_SPAM, accountId));
        }
        initConnection(accountId);
        return prepareFullname(accountId, mailAccess.getFolderStorage().getConfirmedSpamFolder());
    }

    private String getDefaultMailFolder(final int index, final int accountId) {
        final String[] arr = MailSessionCache.getInstance(session).getParameter(
            accountId,
            MailSessionParameterNames.getParamDefaultFolderArray());
        return arr == null ? null : arr[index];
    }

    private String[] getSortedDefaultMailFolders(final int accountId) {
        final String[] arr = MailSessionCache.getInstance(session).getParameter(
            accountId,
            MailSessionParameterNames.getParamDefaultFolderArray());
        if (arr == null) {
            return STR_ARR;
        }
        return new String[] {
            INBOX_ID, arr[StorageUtility.INDEX_DRAFTS], arr[StorageUtility.INDEX_SENT], arr[StorageUtility.INDEX_SPAM],
            arr[StorageUtility.INDEX_TRASH] };
    }

    @Override
    public int getDeletedMessageCount(final String folder) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        initConnection(argument.getAccountId());
        final String fullName = argument.getFullname();
        return mailAccess.getFolderStorage().getFolder(fullName).getDeletedMessageCount();
    }

    @Override
    public String getDraftsFolder(final int accountId) throws OXException {
        if (isDefaultFoldersChecked(accountId)) {
            return prepareFullname(accountId, getDefaultMailFolder(StorageUtility.INDEX_DRAFTS, accountId));
        }
        initConnection(accountId);
        return prepareFullname(accountId, mailAccess.getFolderStorage().getDraftsFolder());
    }

    @Override
    public MailFolder getFolder(final String folder, final boolean checkFolder) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        initConnection(argument.getAccountId());
        final String fullName = argument.getFullname();
        return mailAccess.getFolderStorage().getFolder(fullName);
    }

    private static final int MAX_FORWARD_COUNT = 8;

    @Override
    public MailMessage getForwardMessageForDisplay(final String[] folders, final String[] fowardMsgUIDs, final UserSettingMail usm) throws OXException {
        if ((null == folders) || (null == fowardMsgUIDs) || (folders.length != fowardMsgUIDs.length)) {
            throw new IllegalArgumentException("Illegal arguments");
        }
        if (folders.length > MAX_FORWARD_COUNT) {
            throw MailExceptionCode.TOO_MANY_FORWARD_MAILS.create(Integer.valueOf(MAX_FORWARD_COUNT));
        }
        final FullnameArgument[] arguments = new FullnameArgument[folders.length];
        for (int i = 0; i < folders.length; i++) {
            arguments[i] = prepareMailFolderParam(folders[i]);
        }
        boolean sameAccount = true;
        final int accountId = arguments[0].getAccountId();
        for (int i = 1; sameAccount && i < arguments.length; i++) {
            sameAccount = accountId == arguments[i].getAccountId();
        }
        final TransportProperties transportProperties = TransportProperties.getInstance();
        final MailUploadQuotaChecker checker = new MailUploadQuotaChecker(usm);
        final long maxPerMsg = checker.getFileQuotaMax();
        final long max = checker.getQuotaMax();
        if (sameAccount) {
            initConnection(accountId);
            final MailMessage[] originalMails = new MailMessage[folders.length];
            if (transportProperties.isPublishOnExceededQuota() && (!transportProperties.isPublishPrimaryAccountOnly() || MailAccount.DEFAULT_ID == accountId)) {
                for (int i = 0; i < arguments.length; i++) {
                    final MailMessage origMail = mailAccess.getMessageStorage().getMessage(
                        arguments[i].getFullname(),
                        fowardMsgUIDs[i],
                        false);
                    if (null == origMail) {
                        throw MailExceptionCode.MAIL_NOT_FOUND.create(fowardMsgUIDs[i], arguments[i].getFullname());
                    }
                    originalMails[i] = origMail;
                }
            } else {
                long total = 0;
                for (int i = 0; i < arguments.length; i++) {
                    final MailMessage origMail = mailAccess.getMessageStorage().getMessage(
                        arguments[i].getFullname(),
                        fowardMsgUIDs[i],
                        false);
                    if (null == origMail) {
                        throw MailExceptionCode.MAIL_NOT_FOUND.create(fowardMsgUIDs[i], arguments[i].getFullname());
                    }
                    long size = origMail.getSize();
                    if (size <= 0) {
                        size = 2048; // Avg size
                    }
                    if (maxPerMsg > 0 && size > maxPerMsg) {
                        final String fileName = origMail.getSubject();
                        throw MailExceptionCode.UPLOAD_QUOTA_EXCEEDED_FOR_FILE.create(
                            Long.valueOf(maxPerMsg),
                            null == fileName ? "" : fileName,
                            Long.valueOf(size));
                    }
                    total += size;
                    if (max > 0 && total > max) {
                        throw MailExceptionCode.UPLOAD_QUOTA_EXCEEDED.create(Long.valueOf(max));
                    }
                    originalMails[i] = origMail;
                }
            }
            return mailAccess.getLogicTools().getFowardMessage(originalMails, usm);
        }
        final MailMessage[] originalMails = new MailMessage[folders.length];
        if (transportProperties.isPublishOnExceededQuota() && (!transportProperties.isPublishPrimaryAccountOnly() || MailAccount.DEFAULT_ID == accountId)) {
            for (int i = 0; i < arguments.length && sameAccount; i++) {
                final MailAccess<?, ?> ma = initMailAccess(arguments[i].getAccountId());
                try {
                    final MailMessage origMail = ma.getMessageStorage().getMessage(arguments[i].getFullname(), fowardMsgUIDs[i], false);
                    if (null == origMail) {
                        throw MailExceptionCode.MAIL_NOT_FOUND.create(fowardMsgUIDs[i], arguments[i].getFullname());
                    }
                    originalMails[i] = origMail;
                    origMail.loadContent();
                } finally {
                    ma.close(true);
                }
            }
        } else {
            long total = 0;
            for (int i = 0; i < arguments.length && sameAccount; i++) {
                final MailAccess<?, ?> ma = initMailAccess(arguments[i].getAccountId());
                try {
                    final MailMessage origMail = ma.getMessageStorage().getMessage(arguments[i].getFullname(), fowardMsgUIDs[i], false);
                    if (null == origMail) {
                        throw MailExceptionCode.MAIL_NOT_FOUND.create(fowardMsgUIDs[i], arguments[i].getFullname());
                    }
                    long size = origMail.getSize();
                    if (size <= 0) {
                        size = 2048; // Avg size
                    }
                    if (maxPerMsg > 0 && size > maxPerMsg) {
                        final String fileName = origMail.getSubject();
                        throw MailExceptionCode.UPLOAD_QUOTA_EXCEEDED_FOR_FILE.create(
                            Long.valueOf(maxPerMsg),
                            null == fileName ? "" : fileName,
                            Long.valueOf(size));
                    }
                    total += size;
                    if (max > 0 && total > max) {
                        throw MailExceptionCode.UPLOAD_QUOTA_EXCEEDED.create(Long.valueOf(max));
                    }
                    originalMails[i] = origMail;
                    origMail.loadContent();
                } finally {
                    ma.close(true);
                }
            }
        }
        final int[] accountIDs = new int[originalMails.length];
        for (int i = 0; i < accountIDs.length; i++) {
            accountIDs[i] = arguments[i].getAccountId();
        }
        return MimeForward.getFowardMail(originalMails, session, accountIDs, usm);
    }

    @Override
    public String getInboxFolder(final int accountId) throws OXException {
        if (isDefaultFoldersChecked(accountId)) {
            return prepareFullname(accountId, INBOX_ID);
        }
        initConnection(accountId);
        return prepareFullname(accountId, mailAccess.getFolderStorage().getFolder(INBOX_ID).getFullname());
    }

    @Override
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getMailAccess() throws OXException {
        return mailAccess;
    }

    @Override
    public MailConfig getMailConfig() throws OXException {
        return mailConfig;
    }

    @Override
    public int getAccountID() {
        return accountId;
    }

    private static final MailListField[] FIELDS_FLAGS = new MailListField[] { MailListField.FLAGS };

    private static final transient Object[] ARGS_FLAG_SEEN_SET = new Object[] { Integer.valueOf(MailMessage.FLAG_SEEN) };

    private static final transient Object[] ARGS_FLAG_SEEN_UNSET = new Object[] { Integer.valueOf(-1 * MailMessage.FLAG_SEEN) };

    @Override
    public MailMessage getMessage(final String folder, final String msgUID) throws OXException {
        return getMessage(folder, msgUID, true);
    }

    @Override
    public MailMessage getMessage(final String folder, final String msgUID, final boolean markAsSeen) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        if (MailFolder.DEFAULT_FOLDER_ID.equals(folder)) {
            throw MailExceptionCode.FOLDER_DOES_NOT_HOLD_MESSAGES.create(MailFolder.DEFAULT_FOLDER_ID);
        }
        final String fullName = argument.getFullname();
        final MailMessage mail = mailAccess.getMessageStorage().getMessage(fullName, msgUID, markAsSeen);
        if (mail != null) {
            if (!mail.containsAccountId() || mail.getAccountId() < 0) {
                mail.setAccountId(accountId);
            }
            /*
             * Post event for possibly switched \Seen flag
             */
            if (mail.containsPrevSeen() && !mail.isPrevSeen()) {
                postEvent(PushEventConstants.TOPIC_ATTR, accountId, fullName, true, true);
            }
            /*
             * Update cache since \Seen flag is possibly changed
             */
            try {
                if (MailMessageCache.getInstance().containsFolderMessages(accountId, fullName, session.getUserId(), contextId)) {
                    /*
                     * Update cache entry
                     */
                    MailMessageCache.getInstance().updateCachedMessages(
                        new String[] { mail.getMailId() },
                        accountId,
                        fullName,
                        session.getUserId(),
                        contextId,
                        FIELDS_FLAGS,
                        mail.isSeen() ? ARGS_FLAG_SEEN_SET : ARGS_FLAG_SEEN_UNSET);

                }
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return mail;
    }

    @Override
    public MailPart getMessageAttachment(final String folder, final String msgUID, final String attachmentPosition, final boolean displayVersion) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        return mailAccess.getMessageStorage().getAttachment(fullName, msgUID, attachmentPosition);
    }

    @Override
    public ManagedFile getMessages(final String folder, final String[] msgIds) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        /*
         * Get parts
         */
        final MailMessage[] mails = new MailMessage[msgIds.length];
        for (int i = 0; i < msgIds.length; i++) {
            mails[i] = mailAccess.getMessageStorage().getMessage(fullName, msgIds[i], false);
        }
        /*
         * Store them temporary to files
         */
        final ManagedFileManagement mfm = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class, true);

        final ManagedFile[] files = new ManagedFile[mails.length];
        try {
            final ByteArrayOutputStream bout = new UnsynchronizedByteArrayOutputStream(8192);
            for (int i = 0; i < files.length; i++) {
                final MailMessage mail = mails[i];
                if (null == mail) {
                    files[i] = null;
                } else {
                    bout.reset();
                    mail.writeTo(bout);
                    files[i] = mfm.createManagedFile(bout.toByteArray());
                }
            }
            /*
             * ZIP them
             */
            try {
                final File tempFile = mfm.newTempFile();
                final ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(new FileOutputStream(tempFile));
                zipOutput.setEncoding("UTF-8");
                zipOutput.setUseLanguageEncodingFlag(true);
                try {
                    final byte[] buf = new byte[8192];
                    final Set<String> names = new HashSet<String>(files.length);
                    for (int i = 0; i < files.length; i++) {
                        final ManagedFile file = files[i];
                        final File tmpFile = null == file ? null : file.getFile();
                        if (null != tmpFile) {
                            final FileInputStream in = new FileInputStream(tmpFile);
                            try {
                                /*
                                 * Add ZIP entry to output stream
                                 */
                                final String subject = mails[i].getSubject();
                                final String ext = ".eml";
                                String name = (isEmpty(subject) ? "mail" + (i+1) : saneForFileName(subject)) + ext;
                                final int reslen = name.lastIndexOf('.');
                                int count = 1;
                                while (false == names.add(name)) {
                                    // Name already contained
                                    name = name.substring(0, reslen);
                                    name = new StringAllocator(name).append("_(").append(count++).append(')').append(ext).toString();
                                }
                                ZipArchiveEntry entry;
                                int num = 1;
                                while (true) {
                                    try {
                                        final int pos = name.indexOf(ext);
                                        final String entryName = name.substring(0, pos) + (num > 1 ? "_(" + num + ")" : "") + ext;
                                        entry = new ZipArchiveEntry(entryName);
                                        zipOutput.putArchiveEntry(entry);
                                        break;
                                    } catch (final java.util.zip.ZipException e) {
                                        final String message = e.getMessage();
                                        if (message == null || !message.startsWith("duplicate entry")) {
                                            throw e;
                                        }
                                        num++;
                                    }
                                }
                                /*
                                 * Transfer bytes from the file to the ZIP file
                                 */
                                long size = 0;
                                for (int len; (len = in.read(buf)) > 0;) {
                                    zipOutput.write(buf, 0, len);
                                    size += len;
                                }
                                entry.setSize(size);
                                /*
                                 * Complete the entry
                                 */
                                zipOutput.closeArchiveEntry();
                            } finally {
                                try {
                                    in.close();
                                } catch (final IOException e) {
                                    LOG.error(e.getMessage(), e);
                                }
                            }
                        }
                    }
                } finally {
                    // Complete the ZIP file
                    try {
                        zipOutput.close();
                    } catch (final IOException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                /*
                 * Return managed file
                 */
                return mfm.createManagedFile(tempFile);
            } catch (final IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName())) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                }
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        } catch (final OXException e) {
            throw e;
        } finally {
            for (final ManagedFile file : files) {
                if (null != file) {
                    file.delete();
                }
            }
        }
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private static String saneForFileName(final String fileName) {
        if (isEmpty(fileName)) {
            return fileName;
        }
        final int len = fileName.length();
        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(len);
        char prev = '\0';
        for (int i = 0; i < len; i++) {
            final char c = fileName.charAt(i);
            if (Strings.isWhitespace(c)) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else if ('/' == c) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else if ('\\' == c) {
                if (prev != '_') {
                    prev = '_';
                    sb.append(prev);
                }
            } else {
                prev = '\0';
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public ManagedFile getMessageAttachments(final String folder, final String msgUID, final String[] attachmentPositions) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        /*
         * Get parts
         */
        final MailPart[] parts = new MailPart[attachmentPositions.length];
        for (int i = 0; i < parts.length; i++) {
            parts[i] = mailAccess.getMessageStorage().getAttachment(fullName, msgUID, attachmentPositions[i]);
        }
        /*
         * Store them temporary to files
         */
        final ManagedFileManagement mfm = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class, true);
        final ManagedFile[] files = new ManagedFile[parts.length];
        try {
            for (int i = 0; i < files.length; i++) {
                final MailPart part = parts[i];
                if (null == part) {
                    files[i] = null;
                } else {
                    files[i] = mfm.createManagedFile(part.getInputStream());
                }
            }
            /*
             * ZIP them
             */
            try {
                final File tempFile = mfm.newTempFile();
                final ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(new FileOutputStream(tempFile));
                zipOutput.setEncoding("UTF-8");
                zipOutput.setUseLanguageEncodingFlag(true);
                try {
                    final byte[] buf = new byte[8192];
                    for (int i = 0; i < files.length; i++) {
                        final ManagedFile file = files[i];
                        final File tmpFile = null == file ? null : file.getFile();
                        if (null != tmpFile) {
                            final FileInputStream in = new FileInputStream(tmpFile);
                            try {
                                /*
                                 * Add ZIP entry to output stream
                                 */
                                String name = parts[i].getFileName();
                                if (null == name) {
                                    final List<String> extensions = MimeType2ExtMap.getFileExtensions(parts[i].getContentType().getBaseType());
                                    name = extensions == null || extensions.isEmpty() ? "part.dat" : "part." + extensions.get(0);
                                }
                                int num = 1;
                                ZipArchiveEntry entry;
                                while (true) {
                                    try {
                                        final String entryName;
                                        {
                                            final int pos = name.indexOf('.');
                                            if (pos < 0) {
                                                entryName = name + (num > 1 ? "_(" + num + ")" : "");
                                            } else {
                                                entryName = name.substring(0, pos) + (num > 1 ? "_(" + num + ")" : "") + name.substring(pos);
                                            }
                                        }
                                        entry = new ZipArchiveEntry(entryName);
                                        zipOutput.putArchiveEntry(entry);
                                        break;
                                    } catch (final java.util.zip.ZipException e) {
                                        final String message = e.getMessage();
                                        if (message == null || !message.startsWith("duplicate entry")) {
                                            throw e;
                                        }
                                        num++;
                                    }
                                }
                                /*
                                 * Transfer bytes from the file to the ZIP file
                                 */
                                long size = 0;
                                for (int len; (len = in.read(buf)) > 0;) {
                                    zipOutput.write(buf, 0, len);
                                    size += len;
                                }
                                entry.setSize(size);
                                /*
                                 * Complete the entry
                                 */
                                zipOutput.closeArchiveEntry();
                            } finally {
                                Streams.close(in);
                            }
                        }
                    }
                } finally {
                    // Complete the ZIP file
                    Streams.close(zipOutput);
                }
                /*
                 * Return managed file
                 */
                return mfm.createManagedFile(tempFile);
            } catch (final IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName())) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                }
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        } finally {
            for (final ManagedFile file : files) {
                if (null != file) {
                    file.delete();
                }
            }
        }
    }

    @Override
    public int getMessageCount(final String folder) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        final IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
        if (folderStorage instanceof IMailFolderStorageEnhanced) {
            return ((IMailFolderStorageEnhanced) folderStorage).getTotalCounter(fullName);
        }
        return folderStorage.getFolder(fullName).getMessageCount();
    }

    @Override
    public MailPart getMessageImage(final String folder, final String msgUID, final String cid) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        return mailAccess.getMessageStorage().getImageAttachment(fullName, msgUID, cid);
    }

    @Override
    public MailMessage[] getMessageList(final String folder, final String[] uids, final int[] fields, final String[] headerFields) throws OXException {
        /*
         * Although message cache is only used within mail implementation, we have to examine if cache already holds desired messages. If
         * the cache holds the desired messages no connection has to be fetched/established. This avoids a lot of overhead.
         */
        final int accountId;
        final String fullName;
        {
            final FullnameArgument argument = prepareMailFolderParam(folder);
            accountId = argument.getAccountId();
            fullName = argument.getFullname();
        }
        final boolean loadHeaders = (null != headerFields && 0 < headerFields.length);
        /*-
         * Check for presence in cache
         * TODO: Think about switching to live-fetch if loadHeaders is true. Loading all data once may be faster than
         * first loading from cache then loading missing headers in next step
         */
        try {
            final MailMessage[] mails = MailMessageCache.getInstance().getMessages(
                uids,
                accountId,
                fullName,
                session.getUserId(),
                contextId);
            if (null != mails) {
                /*
                 * List request can be served from cache; apply proper account ID to (unconnected) mail servlet interface
                 */
                this.accountId = accountId;
                /*
                 * Check if headers shall be loaded
                 */
                if (loadHeaders) {
                    /*
                     * Load headers of cached mails
                     */
                    final List<String> loadMe = new ArrayList<String>(mails.length);
                    final Map<String, MailMessage> finder = new HashMap<String, MailMessage>(mails.length);
                    for (final MailMessage mail : mails) {
                        final String mailId = mail.getMailId();
                        finder.put(mailId, mail);
                        if (!mail.hasHeaders(headerFields)) {
                            loadMe.add(mailId);
                        }
                    }
                    if (!loadMe.isEmpty()) {
                        initConnection(accountId);
                        final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                        if (messageStorage instanceof IMailMessageStorageExt) {
                            final IMailMessageStorageExt messageStorageExt = (IMailMessageStorageExt) messageStorage;
                            for (final MailMessage header : messageStorageExt.getMessages(
                                fullName,
                                loadMe.toArray(STR_ARR),
                                FIELDS_ID_INFO,
                                headerFields)) {
                                if (null != header) {
                                    final MailMessage mailMessage = finder.get(header.getMailId());
                                    if (null != mailMessage) {
                                        mailMessage.addHeaders(header.getHeaders());
                                    }
                                }
                            }
                        } else {
                            for (final MailMessage header : messageStorage.getMessages(fullName, loadMe.toArray(STR_ARR), HEADERS)) {
                                if (null != header) {
                                    final MailMessage mailMessage = finder.get(header.getMailId());
                                    if (null != mailMessage) {
                                        mailMessage.addHeaders(header.getHeaders());
                                    }
                                }
                            }
                        }
                    }
                }
                return mails;
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
        /*
         * Live-Fetch from mail storage
         */
        initConnection(accountId);
        final MailMessage[] mails;
        final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
        if (messageStorage instanceof IMailMessageStorageExt) {
            mails = ((IMailMessageStorageExt) messageStorage).getMessages(
                fullName,
                uids,
                MailField.toFields(MailListField.getFields(fields)),
                headerFields);
        } else {
            /*
             * Get appropriate mail fields
             */
            final MailField[] mailFields;
            if (loadHeaders) {
                /*
                 * Ensure MailField.HEADERS is contained
                 */
                final MailFields col = new MailFields(MailField.toFields(MailListField.getFields(fields)));
                col.add(MailField.HEADERS);
                mailFields = col.toArray();
            } else {
                mailFields = MailField.toFields(MailListField.getFields(fields));
            }
            mails = messageStorage.getMessages(fullName, uids, mailFields);
        }
        try {
            if (MailMessageCache.getInstance().containsFolderMessages(accountId, fullName, session.getUserId(), contextId)) {
                MailMessageCache.getInstance().putMessages(accountId, mails, session.getUserId(), contextId);
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
        return mails;
    }

    @Override
    public SearchIterator<MailMessage> getMessages(final String folder, final int[] fromToIndices, final int sortCol, final int order, final com.openexchange.search.SearchTerm<?> searchTerm, final boolean linkSearchTermsWithOR, final int[] fields) throws OXException {
        return getMessagesInternal(prepareMailFolderParam(folder), SearchTermMapper.map(searchTerm), fromToIndices, sortCol, order, fields);
    }

    @Override
    public SearchIterator<MailMessage> getMessages(final String folder, final int[] fromToIndices, final int sortCol, final int order, final int[] searchCols, final String[] searchPatterns, final boolean linkSearchTermsWithOR, final int[] fields) throws OXException {
        checkPatternLength(searchPatterns);
        final SearchTerm<?> searchTerm = (searchCols == null) || (searchCols.length == 0) ? null : SearchUtility.parseFields(
            searchCols,
            searchPatterns,
            linkSearchTermsWithOR);
        return getMessagesInternal(prepareMailFolderParam(folder), searchTerm, fromToIndices, sortCol, order, fields);
    }

    private SearchIterator<MailMessage> getMessagesInternal(final FullnameArgument argument, final SearchTerm<?> searchTerm, final int[] fromToIndices, final int sortCol, final int order, final int[] fields) throws OXException {
        /*
         * Identify and sort messages according to search term and sort criteria while only fetching their IDs
         */
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        MailMessage[] mails =
            mailAccess.getMessageStorage().searchMessages(
                fullName,
                null == fromToIndices ? IndexRange.NULL : new IndexRange(fromToIndices[0], fromToIndices[1]),
                MailSortField.getField(sortCol),
                OrderDirection.getOrderDirection(order),
                searchTerm,
                FIELDS_ID_INFO);
        /*
         * Proceed
         */
        if ((mails == null) || (mails.length == 0)) {
            return SearchIteratorAdapter.<MailMessage> emptyIterator();
        }
        final boolean cachable = (mails.length < mailAccess.getMailConfig().getMailProperties().getMailFetchLimit());
        final MailField[] useFields;
        final boolean onlyFolderAndID;
        if (cachable) {
            /*
             * Selection fits into cache: Prepare for caching
             */
            useFields = com.openexchange.mail.mime.utils.MimeStorageUtility.getCacheFieldsArray();
            onlyFolderAndID = false;
        } else {
            useFields = MailField.getFields(fields);
            onlyFolderAndID = onlyFolderAndID(useFields);
        }
        /*-
         * More than ID and folder requested?
         *  AND
         * Messages do not already contain requested fields although only IDs were requested
         */
        if (!onlyFolderAndID && !containsAll(mails[0], useFields)) {
            /*
             * Extract IDs
             */
            final String[] mailIds = new String[mails.length];
            for (int i = 0; i < mailIds.length; i++) {
                mailIds[i] = mails[i].getMailId();
            }
            /*
             * Fetch identified messages by their IDs and pre-fill them according to specified fields
             */
            mails = mailAccess.getMessageStorage().getMessages(fullName, mailIds, useFields);
            if (null == mails) {
                return SearchIteratorAdapter.emptyIterator();
            }
        }
        /*
         * Set account information
         */
        for (final MailMessage mail : mails) {
            if (mail != null && (!mail.containsAccountId() || mail.getAccountId() < 0)) {
                mail.setAccountId(accountId);
            }
        }
        /*
         * Put message information into cache
         */
        try {
            /*
             * Remove old user cache entries
             */
            // TODO: JSONMessageCache.getInstance().removeAllFoldersExcept(accountId, fullName, session);
            MailMessageCache.getInstance().removeUserMessages(session.getUserId(), contextId);
            if ((cachable) && (mails.length > 0)) {
                /*
                 * ... and put new ones
                 */
                MailMessageCache.getInstance().putMessages(accountId, mails, session.getUserId(), contextId);
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
        final List<MailMessage> l = new ArrayList<MailMessage>(mails.length);
        for (final MailMessage mm : mails) {
            if (null != mm) {
                l.add(mm);
            }
        }
        return new SearchIteratorDelegator<MailMessage>(l);
    }

    private static boolean containsAll(final MailMessage candidate, final MailField[] fields) {
        boolean contained = true;
        final int length = fields.length;
        for (int i = 0; contained && i < length; i++) {
            final MailField field = fields[i];
            switch (field) {
            case ACCOUNT_NAME:
                contained = candidate.containsAccountId() || candidate.containsAccountName();
                break;
            case BCC:
                contained = candidate.containsBcc();
                break;
            case CC:
                contained = candidate.containsCc();
                break;
            case COLOR_LABEL:
                contained = candidate.containsColorLabel();
                break;
            case CONTENT_TYPE:
                contained = candidate.containsContentType();
                break;
            case DISPOSITION_NOTIFICATION_TO:
                contained = candidate.containsDispositionNotification();
                break;
            case FLAGS:
                contained = candidate.containsFlags();
                break;
            case FOLDER_ID:
                contained = true;
                break;
            case FROM:
                contained = candidate.containsFrom();
                break;
            case ID:
                contained = null != candidate.getMailId();
                break;
            case PRIORITY:
                contained = candidate.containsPriority();
                break;
            case RECEIVED_DATE:
                contained = candidate.containsReceivedDate();
                break;
            case SENT_DATE:
                contained = candidate.containsSentDate();
                break;
            case SIZE:
                contained = candidate.containsSize();
                break;
            case SUBJECT:
                contained = candidate.containsSubject();
                break;
            case THREAD_LEVEL:
                contained = candidate.containsThreadLevel();
                break;
            case TO:
                contained = candidate.containsTo();
                break;

            default:
                contained = false;
                break;
            }
        }
        return contained;
    }

    /**
     * Checks if specified fields only consist of mail ID and folder ID
     *
     * @param fields The fields to check
     * @return <code>true</code> if specified fields only consist of mail ID and folder ID; otherwise <code>false</code>
     */
    private static boolean onlyFolderAndID(final MailField[] fields) {
        if (fields.length != 2) {
            return false;
        }
        int i = 0;
        for (final MailField field : fields) {
            if (MailField.ID.equals(field)) {
                i |= 1;
            } else if (MailField.FOLDER_ID.equals(field)) {
                i |= 2;
            }
        }
        return (i == 3);
    }

    @Override
    public String[] appendMessages(final String destFolder, final MailMessage[] mails, final boolean force) throws OXException {
        return appendMessages(destFolder, mails, force, false);
    }

    @Override
    public String[] importMessages(final String destFolder, final MailMessage[] mails, final boolean force) throws OXException {
        return appendMessages(destFolder, mails, force, true);
    }

    public String[] appendMessages(final String destFolder, final MailMessage[] mails, final boolean force, final boolean isImport) throws OXException {
        if ((mails == null) || (mails.length == 0)) {
            return new String[0];
        }
        if (!force) {
            /*
             * Check for valid from address
             */
            try {
                final Set<InternetAddress> validAddrs = new HashSet<InternetAddress>(4);
                if (usm.getSendAddr() != null && usm.getSendAddr().length() > 0) {
                    validAddrs.add(new QuotedInternetAddress(usm.getSendAddr()));
                }
                final User user = getUser();
                validAddrs.add(new QuotedInternetAddress(user.getMail()));
                for (final String alias : user.getAliases()) {
                    validAddrs.add(new QuotedInternetAddress(alias));
                }
                if (MailProperties.getInstance().isSupportMsisdnAddresses()) {
                    MsisdnUtility.addMsisdnAddress(validAddrs, this.session);
                }
                for (final MailMessage mail : mails) {
                    final InternetAddress[] from = mail.getFrom();
                    final List<InternetAddress> froms = Arrays.asList(from);
                    if (!validAddrs.containsAll(froms)) {
                        throw MailExceptionCode.INVALID_SENDER.create(froms.size() == 1 ? froms.get(0).toString() : Arrays.toString(from));
                    }
                }
            } catch (final AddressException e) {
                throw MimeMailException.handleMessagingException(e);
            }
        }
        final FullnameArgument argument = prepareMailFolderParam(destFolder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        if (mailAccess.getFolderStorage().getDraftsFolder().equals(fullName)) {
            /*
             * Append to Drafts folder
             */
            for (final MailMessage mail : mails) {
                mail.setFlag(MailMessage.FLAG_DRAFT, true);
            }
        }

        if (!isImport) {
            return mailAccess.getMessageStorage().appendMessages(fullName, mails);
        }
        final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
        final MailMessage[] tmp = new MailMessage[1];
        final ArrayList<String> idList = new ArrayList<String>();
        for (final MailMessage mail : mails) {
            final MailImportResult mir = new MailImportResult();
            mir.setMail(mail);
            try {
                tmp[0] = mail;
                final String[] idStr = messageStorage.appendMessages(fullName, tmp);
                mir.setId(idStr[0]);
                idList.add(idStr[0]);
            } catch (final OXException e) {
                mir.setException(e);
            }
            mailImportResults.add(mir);
        }

        final String[] ids = new String[idList.size()];
        for (int i = 0; i < idList.size(); i++) {
            ids[i] = idList.get(i);
        }

        return ids;
    }

    @Override
    public int getNewMessageCount(final String folder) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        return mailAccess.getFolderStorage().getFolder(fullName).getNewMessageCount();
    }

    @Override
    public SearchIterator<MailMessage> getNewMessages(final String folder, final int sortCol, final int order, final int[] fields, final int limit) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        return SearchIteratorAdapter.createArrayIterator(mailAccess.getMessageStorage().getUnreadMessages(
            fullName,
            MailSortField.getField(sortCol),
            OrderDirection.getOrderDirection(order),
            MailField.toFields(MailListField.getFields(fields)),
            limit));
    }

    @Override
    public SearchIterator<MailFolder> getPathToDefaultFolder(final String folder) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        return SearchIteratorAdapter.createArrayIterator(mailAccess.getFolderStorage().getPath2DefaultFolder(fullName));
    }

    @Override
    public long[][] getQuotas(final int[] types) throws OXException {
        initConnection(MailAccount.DEFAULT_ID);
        final com.openexchange.mail.Quota.Type[] qtypes = new com.openexchange.mail.Quota.Type[types.length];
        for (int i = 0; i < qtypes.length; i++) {
            qtypes[i] = getType(types[i]);
        }
        final com.openexchange.mail.Quota[] quotas = mailAccess.getFolderStorage().getQuotas(INBOX_ID, qtypes);
        final long[][] retval = new long[quotas.length][];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = quotas[i].toLongArray();
        }
        return retval;
    }

    @Override
    public long getQuotaLimit(final int type) throws OXException {
        initConnection(MailAccount.DEFAULT_ID);
        if (QUOTA_RESOURCE_STORAGE == type) {
            return mailAccess.getFolderStorage().getStorageQuota(INBOX_ID).getLimit();
        } else if (QUOTA_RESOURCE_MESSAGE == type) {
            return mailAccess.getFolderStorage().getMessageQuota(INBOX_ID).getLimit();
        }
        throw new IllegalArgumentException("Unknown quota resource type: " + type);
    }

    @Override
    public long getQuotaUsage(final int type) throws OXException {
        initConnection(MailAccount.DEFAULT_ID);
        if (QUOTA_RESOURCE_STORAGE == type) {
            return mailAccess.getFolderStorage().getStorageQuota(INBOX_ID).getUsage();
        } else if (QUOTA_RESOURCE_MESSAGE == type) {
            return mailAccess.getFolderStorage().getMessageQuota(INBOX_ID).getUsage();
        }
        throw new IllegalArgumentException("Unknown quota resource type: " + type);
    }

    private static com.openexchange.mail.Quota.Type getType(final int type) {
        if (QUOTA_RESOURCE_STORAGE == type) {
            return com.openexchange.mail.Quota.Type.STORAGE;
        } else if (QUOTA_RESOURCE_MESSAGE == type) {
            return com.openexchange.mail.Quota.Type.MESSAGE;
        }
        throw new IllegalArgumentException("Unknown quota resource type: " + type);
    }

    @Override
    public MailMessage getReplyMessageForDisplay(final String folder, final String replyMsgUID, final boolean replyToAll, final UserSettingMail usm) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        final MailMessage originalMail = mailAccess.getMessageStorage().getMessage(fullName, replyMsgUID, false);
        if (null == originalMail) {
            throw MailExceptionCode.MAIL_NOT_FOUND.create(replyMsgUID, fullName);
        }
        return mailAccess.getLogicTools().getReplyMessage(originalMail, replyToAll, usm);
    }

    @Override
    public SearchIterator<MailFolder> getRootFolders() throws OXException {
        initConnection(MailAccount.DEFAULT_ID);
        return SearchIteratorAdapter.createArrayIterator(new MailFolder[] { mailAccess.getFolderStorage().getRootFolder() });
    }

    @Override
    public String getSentFolder(final int accountId) throws OXException {
        if (isDefaultFoldersChecked(accountId)) {
            return prepareFullname(accountId, getDefaultMailFolder(StorageUtility.INDEX_SENT, accountId));
        }
        initConnection(accountId);
        return prepareFullname(accountId, mailAccess.getFolderStorage().getSentFolder());
    }

    @Override
    public String getSpamFolder(final int accountId) throws OXException {
        if (isDefaultFoldersChecked(accountId)) {
            return prepareFullname(accountId, getDefaultMailFolder(StorageUtility.INDEX_SPAM, accountId));
        }
        initConnection(accountId);
        return prepareFullname(accountId, mailAccess.getFolderStorage().getSpamFolder());
    }

    @Override
    public SearchIterator<MailMessage> getThreadedMessages(final String folder, final int[] fromToIndices, final int sortCol, final int order, final int[] searchCols, final String[] searchPatterns, final boolean linkSearchTermsWithOR, final int[] fields) throws OXException {
        checkPatternLength(searchPatterns);
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        final SearchTerm<?> searchTerm = (searchCols == null) || (searchCols.length == 0) ? null : SearchUtility.parseFields(
            searchCols,
            searchPatterns,
            linkSearchTermsWithOR);
        /*
         * Identify and thread-sort messages according to search term while only fetching their IDs
         */
        MailMessage[] mails = mailAccess.getMessageStorage().getThreadSortedMessages(
            fullName,
            fromToIndices == null ? IndexRange.NULL : new IndexRange(fromToIndices[0], fromToIndices[1]),
            MailSortField.getField(sortCol),
            OrderDirection.getOrderDirection(order),
            searchTerm,
            FIELDS_ID_INFO);
        if ((mails == null) || (mails.length == 0)) {
            return SearchIteratorAdapter.<MailMessage> emptyIterator();
        }
        final MailField[] useFields;
        final boolean onlyFolderAndID;
        if (mails.length < mailAccess.getMailConfig().getMailProperties().getMailFetchLimit()) {
            /*
             * Selection fits into cache: Prepare for caching
             */
            useFields = com.openexchange.mail.mime.utils.MimeStorageUtility.getCacheFieldsArray();
            onlyFolderAndID = false;
        } else {
            useFields = MailField.toFields(MailListField.getFields(fields));
            onlyFolderAndID = onlyFolderAndID(useFields);
        }
        if (!onlyFolderAndID) {
            /*
             * Extract IDs
             */
            final String[] mailIds = new String[mails.length];
            for (int i = 0; i < mailIds.length; i++) {
                mailIds[i] = mails[i].getMailId();
            }
            /*
             * Fetch identified messages by their IDs and pre-fill them according to specified fields
             */
            final MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages(fullName, mailIds, useFields);
            /*
             * Apply thread level
             */
            for (int i = 0; i < fetchedMails.length; i++) {
                fetchedMails[i].setThreadLevel(mails[i].getThreadLevel());
            }
            mails = fetchedMails;
        }
        /*
         * Set account information
         */
        for (final MailMessage mail : mails) {
            if (mail != null && (!mail.containsAccountId() || mail.getAccountId() < 0)) {
                mail.setAccountId(accountId);
            }
        }
        try {
            /*
             * Remove old user cache entries
             */
            MailMessageCache.getInstance().removeFolderMessages(accountId, fullName, session.getUserId(), contextId);
            if ((mails.length > 0) && (mails.length < mailAccess.getMailConfig().getMailProperties().getMailFetchLimit())) {
                /*
                 * ... and put new ones
                 */
                MailMessageCache.getInstance().putMessages(accountId, mails, session.getUserId(), contextId);
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
        return SearchIteratorAdapter.createArrayIterator(mails);
    }

    private void checkPatternLength(final String[] patterns) throws OXException {
        final int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        if (0 == minimumSearchCharacters || null == patterns) {
            return;
        }
        for (final String pattern : patterns) {
            if (null != pattern && SearchStrings.lengthWithoutWildcards(pattern) < minimumSearchCharacters) {
                throw MailExceptionCode.PATTERN_TOO_SHORT.create(I(minimumSearchCharacters));
            }
        }
    }

    @Override
    public String getTrashFolder(final int accountId) throws OXException {
        if (isDefaultFoldersChecked(accountId)) {
            return prepareFullname(accountId, getDefaultMailFolder(StorageUtility.INDEX_TRASH, accountId));
        }
        initConnection(accountId);
        return prepareFullname(accountId, mailAccess.getFolderStorage().getTrashFolder());
    }

    @Override
    public int getUnreadMessageCount(final String folder) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        final String fullName = argument.getFullname();

        final int retval;

        if (!init) {
            mailAccess = MailAccess.getInstance(session, accountId);
            retval = mailAccess.getUnreadMessagesCount(fullName);
            mailConfig = mailAccess.getMailConfig();
            this.accountId = accountId;
            init = true;
        } else if (accountId != mailAccess.getAccountId()) {
            mailAccess.close(true);
            mailAccess = MailAccess.getInstance(session, accountId);
            retval = mailAccess.getUnreadMessagesCount(fullName);
            mailConfig = mailAccess.getMailConfig();
            this.accountId = accountId;
        } else {
            retval = mailAccess.getUnreadMessagesCount(fullName);
        }

        return retval;
    }

    private void initConnection(final int accountId) throws OXException {
        if (!init) {
            mailAccess = initMailAccess(accountId);
            mailConfig = mailAccess.getMailConfig();
            this.accountId = accountId;
            init = true;
        } else if (accountId != mailAccess.getAccountId()) {
            mailAccess.close(true);
            mailAccess = initMailAccess(accountId);
            mailConfig = mailAccess.getMailConfig();
            this.accountId = accountId;
        }
    }

    private MailAccess<?, ?> initMailAccess(final int accountId) throws OXException {
        /*
         * Fetch a mail access (either from cache or a new instance)
         */
        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, accountId);
        if (!mailAccess.isConnected()) {
            /*
             * Get new mail configuration
             */
            final long start = System.currentTimeMillis();
            try {
                mailAccess.connect();
                warnings.addAll(mailAccess.getWarnings());
                MailServletInterface.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                MailServletInterface.mailInterfaceMonitor.changeNumSuccessfulLogins(true);
            } catch (final OXException e) {
                if (MimeMailExceptionCode.LOGIN_FAILED.equals(e) || MimeMailExceptionCode.INVALID_CREDENTIALS.equals(e)) {
                    MailServletInterface.mailInterfaceMonitor.changeNumFailedLogins(true);
                }
                throw e;
            }
        }
        return mailAccess;
    }

    private boolean isDefaultFoldersChecked(final int accountId) {
        final Boolean b = MailSessionCache.getInstance(session).getParameter(
            accountId,
            MailSessionParameterNames.getParamDefaultFolderChecked());
        return (b != null) && b.booleanValue();
    }

    @Override
    public String saveDraft(final ComposedMailMessage draftMail, final boolean autosave, final int accountId) throws OXException {
        if (autosave) {
            return autosaveDraft(draftMail, accountId);
        }
        initConnection(accountId);
        final String draftFullname = mailAccess.getFolderStorage().getDraftsFolder();
        final MailMessage draftMessage = mailAccess.getMessageStorage().saveDraft(draftFullname, draftMail);
        if (null == draftMessage) {
            return null;
        }
        final MailPath mailPath = draftMessage.getMailPath();
        if (null == mailPath) {
            return null;
        }
        final String retval = mailPath.toString();
        postEvent(accountId, draftFullname, true);
        return retval;
    }

    private String autosaveDraft(final ComposedMailMessage draftMail, final int accountId) throws OXException {
        initConnection(accountId);
        final String draftFullname = mailAccess.getFolderStorage().getDraftsFolder();
        /*
         * Auto-save draft
         */
        if (!draftMail.isDraft()) {
            draftMail.setFlag(MailMessage.FLAG_DRAFT, true);
        }
        final MailPath msgref = draftMail.getMsgref();
        MailAccess<?, ?> otherAccess = null;
        try {
            final MailMessage origMail;
            if (null == msgref || !draftFullname.equals(msgref.getFolder())) {
                origMail = null;
            } else {
                if (msgref.getAccountId() == accountId) {
                    origMail = mailAccess.getMessageStorage().getMessage(msgref.getFolder(), msgref.getMailID(), false);
                } else {
                    otherAccess = MailAccess.getInstance(session, msgref.getAccountId());
                    otherAccess.connect(true);
                    origMail = otherAccess.getMessageStorage().getMessage(msgref.getFolder(), msgref.getMailID(), false);
                }
                if (origMail != null) {
                    /*
                     * Check for attachments and add them
                     */
                    final NonInlineForwardPartHandler handler = new NonInlineForwardPartHandler();
                    new MailMessageParser().parseMailMessage(origMail, handler);
                    final List<MailPart> parts = handler.getNonInlineParts();
                    if (!parts.isEmpty()) {
                        final TransportProvider tp = TransportProviderRegistry.getTransportProviderBySession(session, accountId);
                        for (final MailPart mailPart : parts) {
                            /*
                             * Create and add a referenced part from original draft mail
                             */
                            draftMail.addEnclosedPart(tp.getNewReferencedPart(mailPart, session));
                        }
                    }
                }
            }
            final String uid;
            {
                final MailMessage filledMail = MimeMessageConverter.fillComposedMailMessage(draftMail);
                filledMail.setFlag(MailMessage.FLAG_DRAFT, true);
                /*
                 * Append message to draft folder without invoking draftMail.cleanUp() afterwards to avoid loss of possibly uploaded images
                 */
                uid = mailAccess.getMessageStorage().appendMessages(draftFullname, new MailMessage[] { filledMail })[0];
            }
            if (null == uid) {
                return null;
            }
            /*
             * Check for draft-edit operation: Delete old version
             */
            if (origMail != null) {
                if (origMail.isDraft() && null != msgref) {
                    if (msgref.getAccountId() == accountId) {
                        mailAccess.getMessageStorage().deleteMessages(msgref.getFolder(), new String[] { msgref.getMailID() }, true);
                    } else if (null != otherAccess) {
                        otherAccess.getMessageStorage().deleteMessages(msgref.getFolder(), new String[] { msgref.getMailID() }, true);
                    }
                }
                draftMail.setMsgref(null);
            }
            /*
             * Return draft mail
             */
            final MailMessage m = mailAccess.getMessageStorage().getMessage(draftFullname, uid, true);
            if (null == m) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(Long.valueOf(uid), draftFullname);
            }
            postEvent(accountId, draftFullname, true);
            return m.getMailPath().toString();
        } finally {
            if (null != otherAccess) {
                otherAccess.close(true);
            }
        }
    }

    @Override
    public String saveFolder(final MailFolderDescription mailFolder) throws OXException {
        if (!mailFolder.containsExists() && !mailFolder.containsFullname()) {
            throw MailExceptionCode.INSUFFICIENT_FOLDER_ATTR.create();
        }
        {
            final String name = mailFolder.getName();
            if (null != name) {
                checkFolderName(name);
            }
        }
        if ((mailFolder.containsExists() && mailFolder.exists()) || ((mailFolder.getFullname() != null) && mailAccess.getFolderStorage().exists(
            mailFolder.getFullname()))) {
            /*
             * Update
             */
            final int accountId = mailFolder.getAccountId();
            String fullName = mailFolder.getFullname();
            initConnection(accountId);
            final char separator = mailFolder.getSeparator();
            final String oldParent;
            final String oldName;
            {
                final int pos = fullName.lastIndexOf(separator);
                if (pos == -1) {
                    oldParent = "";
                    oldName = fullName;
                } else {
                    oldParent = fullName.substring(0, pos);
                    oldName = fullName.substring(pos + 1);
                }
            }
            boolean movePerformed = false;
            /*
             * Check if a move shall be performed
             */
            if (mailFolder.containsParentFullname()) {
                final int parentAccountID = mailFolder.getParentAccountId();
                if (accountId == parentAccountID) {
                    final String newParent = mailFolder.getParentFullname();
                    final com.openexchange.java.StringAllocator newFullname = new com.openexchange.java.StringAllocator(newParent).append(mailFolder.getSeparator());
                    if (mailFolder.containsName()) {
                        newFullname.append(mailFolder.getName());
                    } else {
                        newFullname.append(oldName);
                    }
                    if (!newParent.equals(oldParent)) { // move & rename
                        final Map<String, Map<?, ?>> subfolders = subfolders(fullName);
                        fullName = mailAccess.getFolderStorage().moveFolder(fullName, newFullname.toString());
                        movePerformed = true;
                        postEvent4Subfolders(accountId, subfolders);
                        postEvent(accountId, newParent, false);
                    }
                } else {
                    // Move to another account
                    final MailAccess<?, ?> otherAccess = initMailAccess(parentAccountID);
                    try {
                        final String newParent = mailFolder.getParentFullname();
                        // Check if parent mail folder exists
                        final MailFolder p = otherAccess.getFolderStorage().getFolder(newParent);
                        // Check permission on new parent
                        final MailPermission ownPermission = p.getOwnPermission();
                        if (!ownPermission.canCreateSubfolders()) {
                            throw MailExceptionCode.NO_CREATE_ACCESS.create(newParent);
                        }
                        // Check for duplicate
                        final MailFolder[] tmp = otherAccess.getFolderStorage().getSubfolders(newParent, true);
                        final String lookFor = mailFolder.containsName() ? mailFolder.getName() : oldName;
                        for (final MailFolder sub : tmp) {
                            if (sub.getName().equals(lookFor)) {
                                throw MailExceptionCode.DUPLICATE_FOLDER.create(lookFor);
                            }
                        }
                        // Copy
                        final String destFullname = fullCopy(
                            mailAccess,
                            fullName,
                            otherAccess,
                            newParent,
                            p.getSeparator(),
                            session.getUserId(),
                            otherAccess.getMailConfig().getCapabilities().hasPermissions());
                        postEvent(parentAccountID, newParent, false);
                        // Delete source
                        final Map<String, Map<?, ?>> subfolders = subfolders(fullName);
                        mailAccess.getFolderStorage().deleteFolder(fullName, true);
                        // Perform other updates
                        final String prepareFullname = prepareFullname(
                            parentAccountID,
                            otherAccess.getFolderStorage().updateFolder(destFullname, mailFolder));
                        postEvent4Subfolders(accountId, subfolders);
                        return prepareFullname;
                    } finally {
                        otherAccess.close(true);
                    }
                }
            }
            /*
             * Check if a rename shall be performed
             */
            if (!movePerformed && mailFolder.containsName()) {
                final String newName = mailFolder.getName();
                if (!newName.equals(oldName)) { // rename
                    fullName = mailAccess.getFolderStorage().renameFolder(fullName, newName);
                    postEvent(accountId, fullName, false);
                }
            }
            /*
             * Handle update of permission or subscription
             */
            final String prepareFullname = prepareFullname(accountId, mailAccess.getFolderStorage().updateFolder(fullName, mailFolder));
            postEvent(accountId, fullName, false, true);
            return prepareFullname;
        }
        /*
         * Insert
         */
        final int accountId = mailFolder.getParentAccountId();
        initConnection(accountId);
        final String prepareFullname = prepareFullname(accountId, mailAccess.getFolderStorage().createFolder(mailFolder));
        postEvent(accountId, mailFolder.getParentFullname(), false, true);
        return prepareFullname;
    }

    private static String fullCopy(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> srcAccess, final String srcFullname, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> destAccess, final String destParent, final char destSeparator, final int user, final boolean hasPermissions) throws OXException {
        // Create folder
        final MailFolder source = srcAccess.getFolderStorage().getFolder(srcFullname);
        final MailFolderDescription mfd = new MailFolderDescription();
        mfd.setName(source.getName());
        mfd.setParentFullname(destParent);
        mfd.setSeparator(destSeparator);
        mfd.setSubscribed(source.isSubscribed());
        if (hasPermissions) {
            // Copy permissions
            final MailPermission[] perms = source.getPermissions();
            try {
                for (MailPermission perm : perms) {
                    mfd.addPermission((MailPermission) perm.clone());
                }
            } catch (final CloneNotSupportedException e) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        final String destFullname = destAccess.getFolderStorage().createFolder(mfd);
        // Copy messages
        final MailMessage[] msgs = srcAccess.getMessageStorage().getAllMessages(
            srcFullname,
            null,
            MailSortField.RECEIVED_DATE,
            OrderDirection.ASC,
            FIELDS_FULL);
        final IMailMessageStorage destMessageStorage = destAccess.getMessageStorage();
        // Append messages to destination account
        /* final String[] mailIds = */destMessageStorage.appendMessages(destFullname, msgs);
        /*-
         *
        // Ensure flags
        final String[] arr = new String[1];
        for (int i = 0; i < msgs.length; i++) {
            final MailMessage m = msgs[i];
            final String mailId = mailIds[i];
            if (null != m && null != mailId) {
                arr[0] = mailId;
                // System flags
                destMessageStorage.updateMessageFlags(destFullname, arr, m.getFlags(), true);
                // Color label
                if (m.containsColorLabel() && m.getColorLabel() != MailMessage.COLOR_LABEL_NONE) {
                    destMessageStorage.updateMessageColorLabel(destFullname, arr, m.getColorLabel());
                }
            }
        }
         */
        // Iterate subfolders
        final MailFolder[] tmp = srcAccess.getFolderStorage().getSubfolders(srcFullname, true);
        for (MailFolder element : tmp) {
            fullCopy(srcAccess, element.getFullname(), destAccess, destFullname, destSeparator, user, hasPermissions);
        }
        return destFullname;
    }

    private final static String INVALID = "<>"; // "()<>@,;:\\\".[]";

    private static void checkFolderName(final String name) throws OXException {
        if (isEmpty(name)) {
            throw MailExceptionCode.INVALID_FOLDER_NAME_EMPTY.create();
        }
        final int length = name.length();
        for (int i = 0; i < length; i++) {
            if (INVALID.indexOf(name.charAt(i)) >= 0) {
                throw MailExceptionCode.INVALID_FOLDER_NAME2.create(name);
            }
        }
    }

    @Override
    public void sendFormMail(final ComposedMailMessage composedMail, final int groupId, final int accountId) throws OXException {
        /*
         * Initialize
         */
        initConnection(accountId);
        final MailTransport transport = MailTransport.getInstance(session, accountId);
        try {
            /*
             * Resolve group to users
             */
            final GroupStorage gs = GroupStorage.getInstance();
            final Group group = gs.getGroup(groupId, ctx);
            final int[] members = group.getMember();
            /*
             * Get user storage/contact interface to load user and its contact
             */
            final UserStorage us = UserStorage.getInstance();
            final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
            /*
             * Needed variables
             */
            final String content = (String) composedMail.getContent();
            final StringBuilder builder = new StringBuilder(content.length() + 64);
            final TransportProvider provider = TransportProviderRegistry.getTransportProviderBySession(session, accountId);
            final Map<Locale, String> greetings = new HashMap<Locale, String>(4);
            for (final int userId : members) {
                final User user = us.getUser(userId, ctx);
                /*
                 * Get user's contact
                 */
                final Contact contact = contactService.getUser(session, userId, new ContactField[] {
                    ContactField.SUR_NAME, ContactField.GIVEN_NAME });
                /*
                 * Determine locale
                 */
                final Locale locale = user.getLocale();
                /*
                 * Compose text
                 */
                String greeting = greetings.get(locale);
                if (null == greeting) {
                    greeting = StringHelper.valueOf(locale).getString(MailStrings.GREETING);
                    greetings.put(locale, greeting);
                }
                builder.setLength(0);
                builder.append(greeting).append(' ');
                builder.append(contact.getGivenName()).append(' ').append(contact.getSurName());
                builder.append("<br><br>").append(content);
                final TextBodyMailPart part = provider.getNewTextBodyPart(builder.toString());
                /*
                 * TODO: Clone composed mail?
                 */
                composedMail.setBodyPart(part);
                composedMail.removeTo();
                composedMail.removeBcc();
                composedMail.removeCc();
                composedMail.addTo(new QuotedInternetAddress(user.getMail()));
                /*
                 * Finally send mail
                 */
                final MailProperties properties = MailProperties.getInstance();
                if ((properties.getRateLimitPrimaryOnly() && MailAccount.DEFAULT_ID == accountId) || !properties.getRateLimitPrimaryOnly()) {
                    final int rateLimit = properties.getRateLimit();
                    rateLimitChecks(composedMail, rateLimit, properties.getMaxToCcBcc());
                    transport.sendMailMessage(composedMail, ComposeType.NEW);
                    setRateLimitTime(rateLimit);
                } else {
                    transport.sendMailMessage(composedMail, ComposeType.NEW);
                }
            }
        } catch (final MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } finally {
            transport.close();
        }
    }

    @Override
    public String sendMessage(final ComposedMailMessage composedMail, final ComposeType type, final int accountId) throws OXException {
        /*
         * Initialize
         */
        initConnection(accountId);
        final MailTransport transport = MailTransport.getInstance(session, accountId);
        boolean mailSent = false;
        try {
            /*
             * Send mail
             */
            final long startTransport;
            final MailMessage sentMail;
            startTransport = System.currentTimeMillis();
            final MailProperties properties = MailProperties.getInstance();
            if ((properties.getRateLimitPrimaryOnly() && MailAccount.DEFAULT_ID == accountId) || !properties.getRateLimitPrimaryOnly()) {
                final int rateLimit = properties.getRateLimit();
                rateLimitChecks(composedMail, rateLimit, properties.getMaxToCcBcc());
                sentMail = transport.sendMailMessage(composedMail, type);
                setRateLimitTime(rateLimit);
            } else {
                sentMail = transport.sendMailMessage(composedMail, type);
            }
            mailSent = true;
            /*
             * Email successfully sent, trigger data retention
             */
            final DataRetentionService retentionService = ServerServiceRegistry.getInstance().getService(DataRetentionService.class);
            if (null != retentionService) {
                triggerDataRetention(transport, startTransport, sentMail, retentionService);
            }
            /*
             * Check for a reply/forward
             */
            try {
                if (ComposeType.REPLY.equals(type)) {
                    setFlagReply(composedMail.getMsgref());
                } else if (ComposeType.FORWARD.equals(type)) {
                    final MailPath supPath = composedMail.getMsgref();
                    if (null == supPath) {
                        final int count = composedMail.getEnclosedCount();
                        final List<MailPath> paths = new ArrayList<MailPath>(count);
                        for (int i = 0; i < count; i++) {
                            final MailPart part = composedMail.getEnclosedMailPart(i);
                            final MailPath path = part.getMsgref();
                            if ((path != null) && part.getContentType().isMimeType(MimeTypes.MIME_MESSAGE_RFC822)) {
                                paths.add(path);
                            }
                        }
                        if (!paths.isEmpty()) {
                            setFlagMultipleForward(paths);
                        }
                    } else {
                        setFlagForward(supPath);
                    }
                } else if (ComposeType.DRAFT.equals(type)) {
                    final ConfigViewFactory configViewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
                    if (null != configViewFactory) {
                        try {
                            final ConfigView view = configViewFactory.getView(session.getUserId(), session.getContextId());
                            final ComposedConfigProperty<Boolean> property = view.property(
                                "com.openexchange.mail.deleteDraftOnTransport",
                                boolean.class);
                            if (property.isDefined() && property.get().booleanValue()) {
                                deleteDraft(composedMail.getMsgref());
                            }
                        } catch (final Exception e) {
                            LOG.warn("Draft mail cannot be deleted.", e);
                        }
                    }
                } else if (ComposeType.DRAFT_DELETE_ON_TRANSPORT.equals(type)) {
                    try {
                        deleteDraft(composedMail.getMsgref());
                    } catch (final Exception e) {
                        LOG.warn("Draft mail cannot be deleted.", e);
                    }
                }
            } catch (final OXException e) {
                mailAccess.addWarnings(Collections.singletonList(MailExceptionCode.FLAG_FAIL.create(e, new Object[0])));
            }
            if (UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isNoCopyIntoStandardSentFolder()) {
                /*
                 * No copy in sent folder
                 */
                return null;
            }
            /*
             * If mail identifier and folder identifier is already available, assume is has already been stored in Sent folder
             */
            if (null != sentMail.getMailId() && null != sentMail.getFolder()) {
                return new MailPath(accountId, sentMail.getFolder(), sentMail.getMailId()).toString();
            }
            return append2SentFolder(sentMail).toString();
        } catch (final OXException e) {
            if (!mailSent) {
                throw e;
            }
            e.setCategory(Category.CATEGORY_WARNING);
            warnings.add(e);
            return null;
        } catch (final RuntimeException e) {
            final OXException oxe = MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            if (!mailSent) {
                throw oxe;
            }
            oxe.setCategory(Category.CATEGORY_WARNING);
            warnings.add(oxe);
            return null;
        } finally {
            transport.close();
        }
    }

    private void triggerDataRetention(final MailTransport transport, final long startTransport, final MailMessage sentMail, final DataRetentionService retentionService) {
        /*
         * Create runnable task
         */
        final Session s = session;
        final org.apache.commons.logging.Log l = LOG;
        final Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    final RetentionData retentionData = retentionService.newInstance();
                    retentionData.setStartTime(new Date(startTransport));
                    retentionData.setIdentifier(transport.getTransportConfig().getLogin());
                    retentionData.setIPAddress(s.getLocalIp());
                    retentionData.setSenderAddress(IDNA.toIDN(sentMail.getFrom()[0].getAddress()));
                    final Set<InternetAddress> recipients = new HashSet<InternetAddress>(Arrays.asList(sentMail.getTo()));
                    recipients.addAll(Arrays.asList(sentMail.getCc()));
                    recipients.addAll(Arrays.asList(sentMail.getBcc()));
                    final int size = recipients.size();
                    final String[] recipientsArr = new String[size];
                    final Iterator<InternetAddress> it = recipients.iterator();
                    for (int i = 0; i < size; i++) {
                        recipientsArr[i] = IDNA.toIDN(it.next().getAddress());
                    }
                    retentionData.setRecipientAddresses(recipientsArr);
                    /*
                     * Finally store it
                     */
                    retentionService.storeOnTransport(retentionData);
                } catch (final OXException e) {
                    l.error(e.getMessage(), e);
                }
            }
        };
        /*
         * Check if timer service is available to delegate execution
         */
        final ThreadPoolService threadPool = ThreadPools.getThreadPool();
        if (null == threadPool) {
            // Execute in this thread
            r.run();
        } else {
            // Delegate runnable to thread pool
            threadPool.submit(ThreadPools.task(r), CallerRunsBehavior.getInstance());
        }
    }

    private MailPath append2SentFolder(final MailMessage sentMail) throws OXException {
        /*
         * Append to Sent folder
         */
        final long start = System.currentTimeMillis();
        final String sentFullname = mailAccess.getFolderStorage().getSentFolder();
        final String[] uidArr;
        try {
            uidArr = mailAccess.getMessageStorage().appendMessages(sentFullname, new MailMessage[] { sentMail });
            try {
                /*
                 * Update caches
                 */
                MailMessageCache.getInstance().removeFolderMessages(mailAccess.getAccountId(), sentFullname, session.getUserId(), contextId);
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
        } catch (final OXException e) {
            if (e.getMessage().indexOf("quota") != -1) {
                throw MailExceptionCode.COPY_TO_SENT_FOLDER_FAILED_QUOTA.create(e, new Object[0]);
            }
            throw MailExceptionCode.COPY_TO_SENT_FOLDER_FAILED.create(e, new Object[0]);
        }
        if ((uidArr != null) && (uidArr[0] != null)) {
            /*
             * Mark appended sent mail as seen
             */
            mailAccess.getMessageStorage().updateMessageFlags(sentFullname, uidArr, MailMessage.FLAG_SEEN, true);
        }
        final MailPath retval = new MailPath(mailAccess.getAccountId(), sentFullname, uidArr[0]);
        if (DEBUG_ENABLED) {
            LOG.debug(new com.openexchange.java.StringAllocator(128).append("Mail copy (").append(retval.toString()).append(
                ") appended in ").append(System.currentTimeMillis() - start).append("msec").toString());
        }
        return retval;
    }

    private void setFlagForward(final MailPath path) throws OXException {
        /*
         * Mark referenced mail as forwarded
         */
        final String fullName = path.getFolder();
        final String[] uids = new String[] { path.getMailID() };
        final int pathAccount = path.getAccountId();
        if (mailAccess.getAccountId() == pathAccount) {
            mailAccess.getMessageStorage().updateMessageFlags(fullName, uids, MailMessage.FLAG_FORWARDED, true);
            try {
                if (MailMessageCache.getInstance().containsFolderMessages(
                    mailAccess.getAccountId(),
                    fullName,
                    session.getUserId(),
                    contextId)) {
                    /*
                     * Update cache entries
                     */
                    MailMessageCache.getInstance().updateCachedMessages(
                        uids,
                        mailAccess.getAccountId(),
                        fullName,
                        session.getUserId(),
                        contextId,
                        FIELDS_FLAGS,
                        new Object[] { Integer.valueOf(MailMessage.FLAG_FORWARDED) });
                }
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            final MailAccess<?, ?> otherAccess = MailAccess.getInstance(session, pathAccount);
            otherAccess.connect(true);
            try {
                otherAccess.getMessageStorage().updateMessageFlags(fullName, uids, MailMessage.FLAG_FORWARDED, true);
                try {
                    if (MailMessageCache.getInstance().containsFolderMessages(
                        otherAccess.getAccountId(),
                        fullName,
                        session.getUserId(),
                        contextId)) {
                        /*
                         * Update cache entries
                         */
                        MailMessageCache.getInstance().updateCachedMessages(
                            uids,
                            otherAccess.getAccountId(),
                            fullName,
                            session.getUserId(),
                            contextId,
                            FIELDS_FLAGS,
                            new Object[] { Integer.valueOf(MailMessage.FLAG_FORWARDED) });
                    }
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            } finally {
                otherAccess.close(false);
            }
        }
    }

    private void setFlagMultipleForward(final List<MailPath> paths) throws OXException {
        final String[] ids = new String[1];
        for (final MailPath path : paths) {
            /*
             * Mark referenced mail as forwarded
             */
            ids[0] = path.getMailID();
            final int pathAccount = path.getAccountId();
            if (mailAccess.getAccountId() == pathAccount) {
                mailAccess.getMessageStorage().updateMessageFlags(path.getFolder(), ids, MailMessage.FLAG_FORWARDED, true);
                try {
                    if (MailMessageCache.getInstance().containsFolderMessages(
                        mailAccess.getAccountId(),
                        path.getFolder(),
                        session.getUserId(),
                        contextId)) {
                        /*
                         * Update cache entries
                         */
                        MailMessageCache.getInstance().updateCachedMessages(
                            ids,
                            mailAccess.getAccountId(),
                            path.getFolder(),
                            session.getUserId(),
                            contextId,
                            FIELDS_FLAGS,
                            new Object[] { Integer.valueOf(MailMessage.FLAG_FORWARDED) });
                    }
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            } else {
                final MailAccess<?, ?> otherAccess = MailAccess.getInstance(session, pathAccount);
                otherAccess.connect(true);
                try {
                    otherAccess.getMessageStorage().updateMessageFlags(path.getFolder(), ids, MailMessage.FLAG_FORWARDED, true);
                    try {
                        if (MailMessageCache.getInstance().containsFolderMessages(
                            otherAccess.getAccountId(),
                            path.getFolder(),
                            session.getUserId(),
                            contextId)) {
                            /*
                             * Update cache entries
                             */
                            MailMessageCache.getInstance().updateCachedMessages(
                                ids,
                                otherAccess.getAccountId(),
                                path.getFolder(),
                                session.getUserId(),
                                contextId,
                                FIELDS_FLAGS,
                                new Object[] { Integer.valueOf(MailMessage.FLAG_FORWARDED) });
                        }
                    } catch (final OXException e) {
                        LOG.error(e.getMessage(), e);
                    }
                } finally {
                    otherAccess.close(false);
                }
            }
        }
    }

    private void deleteDraft(final MailPath path) throws OXException {
        if (null == path) {
            LOG.warn("Missing msgref on draft-delete. Corresponding draft mail cannot be deleted.", new Throwable());
            return;
        }
        /*
         * Delete draft mail
         */
        final String fullName = path.getFolder();
        final String[] uids = new String[] { path.getMailID() };
        final int pathAccount = path.getAccountId();
        if (mailAccess.getAccountId() == pathAccount) {
            mailAccess.getMessageStorage().deleteMessages(fullName, uids, true);
        } else {
            MailAccess<?, ?> otherAccess = null;
            try {
                otherAccess = MailAccess.getInstance(session, pathAccount);
                otherAccess.connect(true);
                otherAccess.getMessageStorage().deleteMessages(fullName, uids, true);
                try {
                    MailMessageCache.getInstance().removeMessages(uids, pathAccount, fullName, session.getUserId(), session.getContextId());
                } catch (final OXException e) {
                    // Ignore
                }
            } finally {
                if (null != otherAccess) {
                    otherAccess.close(true);
                }
            }
        }
    }

    private void setFlagReply(final MailPath path) throws OXException {
        if (null == path) {
            LOG.warn("Missing msgref on reply. Corresponding mail cannot be marked as answered.", new Throwable());
            return;
        }
        /*
         * Mark referenced mail as answered
         */
        final String fullName = path.getFolder();
        final String[] uids = new String[] { path.getMailID() };
        final int pathAccount = path.getAccountId();
        if (mailAccess.getAccountId() == pathAccount) {
            mailAccess.getMessageStorage().updateMessageFlags(fullName, uids, MailMessage.FLAG_ANSWERED, true);
            try {
                /*
                 * Update JSON cache
                 */
                if (MailMessageCache.getInstance().containsFolderMessages(
                    mailAccess.getAccountId(),
                    fullName,
                    session.getUserId(),
                    contextId)) {
                    /*
                     * Update cache entries
                     */
                    MailMessageCache.getInstance().updateCachedMessages(
                        uids,
                        mailAccess.getAccountId(),
                        fullName,
                        session.getUserId(),
                        contextId,
                        FIELDS_FLAGS,
                        new Object[] { Integer.valueOf(MailMessage.FLAG_ANSWERED) });
                }
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            /*
             * Mark as \Answered in foreign account
             */
            final MailAccess<?, ?> otherAccess = MailAccess.getInstance(session, pathAccount);
            otherAccess.connect(true);
            try {
                otherAccess.getMessageStorage().updateMessageFlags(fullName, uids, MailMessage.FLAG_ANSWERED, true);
                try {
                    /*
                     * Update JSON cache
                     */
                    if (MailMessageCache.getInstance().containsFolderMessages(pathAccount, fullName, session.getUserId(), contextId)) {
                        /*
                         * Update cache entries
                         */
                        MailMessageCache.getInstance().updateCachedMessages(
                            uids,
                            pathAccount,
                            fullName,
                            session.getUserId(),
                            contextId,
                            FIELDS_FLAGS,
                            new Object[] { Integer.valueOf(MailMessage.FLAG_ANSWERED) });
                    }
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            } finally {
                otherAccess.close(false);
            }
        }
    }

    private <V> V performSynchronized(final Callable<V> task, final Session session) throws Exception {
        Lock lock = (Lock) session.getParameter(Session.PARAM_LOCK);
        if (null == lock) {
            lock = Session.EMPTY_LOCK;
        }
        lock.lock();
        try {
            return task.call();
        } finally {
            lock.unlock();
        }
    }

    private void setRateLimitTime(final int rateLimit) {
        if (rateLimit > 0) {
            session.setParameter(LAST_SEND_TIME, Long.valueOf(System.currentTimeMillis()));
        }
    }

    private void rateLimitChecks(final MailMessage composedMail, final int rateLimit, final int maxToCcBcc) throws OXException {
        if (rateLimit > 0) {
            final Long parameter = (Long) session.getParameter(LAST_SEND_TIME);
            if (null != parameter && (parameter.longValue() + rateLimit) >= System.currentTimeMillis()) {
                final NumberFormat numberInstance = NumberFormat.getNumberInstance(UserStorage.getStorageUser(
                    session.getUserId(),
                    session.getContextId()).getLocale());
                throw MailExceptionCode.SENT_QUOTA_EXCEEDED.create(numberInstance.format(((double) rateLimit) / 1000));
            }
        }
        if (maxToCcBcc > 0) {
            InternetAddress[] addrs = composedMail.getTo();
            int count = (addrs == null ? 0 : addrs.length);

            addrs = composedMail.getCc();
            count += (addrs == null ? 0 : addrs.length);

            addrs = composedMail.getBcc();
            count += (addrs == null ? 0 : addrs.length);

            if (count > maxToCcBcc) {
                throw MailExceptionCode.RECIPIENTS_EXCEEDED.create(Integer.valueOf(maxToCcBcc));
            }
        }
    }

    @Override
    public void sendReceiptAck(final String folder, final String msgUID, final String fromAddr) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int acc = argument.getAccountId();
        try {
            final MailAccountStorageService ss = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            final MailAccount ma = ss.getMailAccount(acc, session.getUserId(), session.getContextId());
            if (ma.isDefaultAccount()) {
                /*
                 * Check for valid from address
                 */
                try {
                    final Set<InternetAddress> validAddrs = new HashSet<InternetAddress>(4);
                    if (usm.getSendAddr() != null && usm.getSendAddr().length() > 0) {
                        validAddrs.add(new QuotedInternetAddress(usm.getSendAddr()));
                    }
                    final User user = getUser();
                    validAddrs.add(new QuotedInternetAddress(user.getMail()));
                    for (final String alias : user.getAliases()) {
                        validAddrs.add(new QuotedInternetAddress(alias));
                    }
                    if (MailProperties.getInstance().isSupportMsisdnAddresses()) {
                        MsisdnUtility.addMsisdnAddress(validAddrs, session);
                    }
                    if (!validAddrs.contains(new QuotedInternetAddress(fromAddr))) {
                        throw MailExceptionCode.INVALID_SENDER.create(fromAddr);
                    }
                } catch (final AddressException e) {
                    throw MimeMailException.handleMessagingException(e);
                }
            } else {
                if (!new QuotedInternetAddress(ma.getPrimaryAddress()).equals(new QuotedInternetAddress(fromAddr))) {
                    throw MailExceptionCode.INVALID_SENDER.create(fromAddr);
                }
            }
        } catch (final AddressException e) {
            throw MimeMailException.handleMessagingException(e);
        }
        /*
         * Initialize
         */
        initConnection(acc);
        final String fullName = argument.getFullname();
        final MailTransport transport = MailTransport.getInstance(session);
        try {
            transport.sendReceiptAck(mailAccess.getMessageStorage().getMessage(fullName, msgUID, false), fromAddr);
        } finally {
            transport.close();
        }
        mailAccess.getMessageStorage().updateMessageFlags(fullName, new String[] { msgUID }, MailMessage.FLAG_READ_ACK, true);
    }

    private static final MailListField[] FIELDS_COLOR_LABEL = new MailListField[] { MailListField.COLOR_LABEL };

    private static final Map<String, Object> MORE_PROPS_UPDATE_LABEL;
    static {
        final Map<String, Object> m = new HashMap<String, Object>(1, 1f);
        m.put("operation", "updateMessageColorLabel");
        MORE_PROPS_UPDATE_LABEL = Collections.unmodifiableMap(m);
    }

    @Override
    public void updateMessageColorLabel(final String folder, final String[] mailIDs, final int newColorLabel) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
        final String[] ids;
        if (null == mailIDs) {
            if (messageStorage instanceof IMailMessageStorageBatch) {
                final IMailMessageStorageBatch batch = (IMailMessageStorageBatch) messageStorage;
                ids = null;
                batch.updateMessageColorLabel(fullName, newColorLabel);
            } else {
                ids = getAllMessageIDs(argument);
                messageStorage.updateMessageColorLabel(fullName, ids, newColorLabel);
            }
        } else {
            ids = mailIDs;
            messageStorage.updateMessageColorLabel(fullName, ids, newColorLabel);
        }
        postEvent(PushEventConstants.TOPIC_ATTR, accountId, fullName, true, true, false, MORE_PROPS_UPDATE_LABEL);
        /*
         * Update caches
         */
        try {
            if (MailMessageCache.getInstance().containsFolderMessages(accountId, fullName, session.getUserId(), contextId)) {
                /*
                 * Update cache entries
                 */
                MailMessageCache.getInstance().updateCachedMessages(
                    ids,
                    accountId,
                    fullName,
                    session.getUserId(),
                    contextId,
                    FIELDS_COLOR_LABEL,
                    new Object[] { Integer.valueOf(newColorLabel) });
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public String getMailIDByMessageID(final String folder, final String messageID) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        final MailMessage[] messages = mailAccess.getMessageStorage().searchMessages(
            fullName,
            null,
            MailSortField.RECEIVED_DATE,
            OrderDirection.ASC,
            new HeaderTerm("Message-Id", messageID),
            FIELDS_ID_INFO);
        if (null == messages || 1 != messages.length) {
            throw MailExceptionCode.MAIL_NOT_FOUN_BY_MESSAGE_ID.create(fullName, messageID);
        }
        return messages[0].getMailId();
    }

    private static final Map<String, Object> MORE_PROPS_UPDATE_FLAGS;
    static {
        final Map<String, Object> m = new HashMap<String, Object>(1, 1f);
        m.put("operation", "updateMessageFlags");
        MORE_PROPS_UPDATE_FLAGS = Collections.unmodifiableMap(m);
    }

    @Override
    public void updateMessageFlags(final String folder, final String[] mailIDs, final int flagBits, final boolean flagVal) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
        final String[] ids;
        if (null == mailIDs) {
            if (messageStorage instanceof IMailMessageStorageBatch) {
                final IMailMessageStorageBatch batch = (IMailMessageStorageBatch) messageStorage;
                ids = null;
                batch.updateMessageFlags(fullName, flagBits, flagVal);
            } else {
                ids = getAllMessageIDs(argument);
                messageStorage.updateMessageFlags(fullName, ids, flagBits, flagVal);
            }
        } else {
            ids = mailIDs;
            messageStorage.updateMessageFlags(fullName, ids, flagBits, flagVal);
        }
        postEvent(PushEventConstants.TOPIC_ATTR, accountId, fullName, true, true, false, MORE_PROPS_UPDATE_FLAGS);
        final boolean spamAction = (usm.isSpamEnabled() && ((flagBits & MailMessage.FLAG_SPAM) > 0));
        if (spamAction) {
            final String spamFullname = mailAccess.getFolderStorage().getSpamFolder();
            postEvent(PushEventConstants.TOPIC_ATTR, accountId, spamFullname, true, true);
        }
        /*
         * Update caches
         */
        if (spamAction) {
            /*
             * Remove from caches
             */
            try {
                if (MailMessageCache.getInstance().containsFolderMessages(accountId, fullName, session.getUserId(), contextId)) {
                    MailMessageCache.getInstance().removeMessages(ids, accountId, fullName, session.getUserId(), contextId);
                }
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            try {
                if (MailMessageCache.getInstance().containsFolderMessages(accountId, fullName, session.getUserId(), contextId)) {
                    /*
                     * Update cache entries
                     */
                    MailMessageCache.getInstance().updateCachedMessages(
                        ids,
                        accountId,
                        fullName,
                        session.getUserId(),
                        contextId,
                        FIELDS_FLAGS,
                        new Object[] { Integer.valueOf(flagVal ? flagBits : (flagBits * -1)) });
                }
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public MailMessage[] getUpdatedMessages(final String folder, final int[] fields) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        return mailAccess.getMessageStorage().getNewAndModifiedMessages(fullName, MailField.getFields(fields));
    }

    @Override
    public MailMessage[] getDeletedMessages(final String folder, final int[] fields) throws OXException {
        final FullnameArgument argument = prepareMailFolderParam(folder);
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        return mailAccess.getMessageStorage().getDeletedMessages(fullName, MailField.getFields(fields));
    }

    /*-
     * ################################################################################
     * #############################   HELPER CLASSES   ###############################
     * ################################################################################
     */

    private static final class MailFolderComparator implements Comparator<MailFolder> {

        private final Map<String, Integer> indexMap;

        private final Collator collator;

        private final Integer na;

        public MailFolderComparator(final String[] names, final Locale locale) {
            super();
            indexMap = new HashMap<String, Integer>(names.length);
            for (int i = 0; i < names.length; i++) {
                indexMap.put(names[i], Integer.valueOf(i));
            }
            na = Integer.valueOf(names.length);
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        private Integer getNumberOf(final String name) {
            final Integer ret = indexMap.get(name);
            if (null == ret) {
                return na;
            }
            return ret;
        }

        @Override
        public int compare(final MailFolder o1, final MailFolder o2) {
            if (o1.isDefaultFolder()) {
                if (o2.isDefaultFolder()) {
                    return getNumberOf(o1.getFullname()).compareTo(getNumberOf(o2.getFullname()));
                }
                return -1;
            }
            if (o2.isDefaultFolder()) {
                return 1;
            }
            return collator.compare(o1.getName(), o2.getName());
        }
    }

    private static final class SimpleMailFolderComparator implements Comparator<MailFolder> {

        private final Collator collator;

        public SimpleMailFolderComparator(final Locale locale) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        @Override
        public int compare(final MailFolder o1, final MailFolder o2) {
            return collator.compare(o1.getName(), o2.getName());
        }
    }

    private static String[] messages2ids(final MailMessage[] messages) {
        if (null == messages) {
            return null;
        }
        final String[] retval = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            retval[i] = messages[i].getMailId();
        }
        return retval;
    }

    private void postEvent(final int accountId, final String fullName, final boolean contentRelated) {
        postEvent(accountId, fullName, contentRelated, false);
    }

    private void postEvent(final int accountId, final String fullName, final boolean contentRelated, final boolean immediateDelivery) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        EventPool.getInstance().put(
            new PooledEvent(contextId, session.getUserId(), accountId, prepareFullname(accountId, fullName), contentRelated, immediateDelivery, session));
    }

    private void postEvent(final int accountId, final String fullName, final boolean contentRelated, final boolean immediateDelivery, final boolean async) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        EventPool.getInstance().put(
            new PooledEvent(contextId, session.getUserId(), accountId, prepareFullname(accountId, fullName), contentRelated, immediateDelivery, session).setAsync(async));
    }

    private void postEvent(final String topic, final int accountId, final String fullName, final boolean contentRelated, final boolean immediateDelivery) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        EventPool.getInstance().put(new PooledEvent(topic, contextId, session.getUserId(), accountId, prepareFullname(accountId, fullName), contentRelated, immediateDelivery, session));
    }

    private void postEvent(final String topic, final int accountId, final String fullName, final boolean contentRelated, final boolean immediateDelivery, final boolean async) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        final PooledEvent pooledEvent = new PooledEvent(topic, contextId, session.getUserId(), accountId, prepareFullname(accountId, fullName), contentRelated, immediateDelivery, session);
        EventPool.getInstance().put(pooledEvent.setAsync(async));
    }

    private void postEvent(final String topic, final int accountId, final String fullName, final boolean contentRelated, final boolean immediateDelivery, final boolean async, final Map<String, Object> moreProperties) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        final PooledEvent pooledEvent = new PooledEvent(topic, contextId, session.getUserId(), accountId, prepareFullname(accountId, fullName), contentRelated, immediateDelivery, session);
        if (null != moreProperties) {
            for (final Entry<String, Object> entry : moreProperties.entrySet()) {
                pooledEvent.putProperty(entry.getKey(), entry.getValue());
            }
        }
        EventPool.getInstance().put(pooledEvent.setAsync(async));
    }

    @Override
    public MailImportResult[] getMailImportResults() {
        final MailImportResult[] mars = new MailImportResult[mailImportResults.size()];
        for (int i = 0; i < mars.length; i++) {
            mars[i] = mailImportResults.get(i);
        }

        return mars;
    }

    private String[] getAllMessageIDs(final FullnameArgument argument) throws OXException {
        final int accountId = argument.getAccountId();
        initConnection(accountId);
        final String fullName = argument.getFullname();
        final MailMessage[] mails = mailAccess.getMessageStorage().searchMessages(
            fullName,
            null,
            MailSortField.RECEIVED_DATE,
            OrderDirection.ASC,
            null,
            FIELDS_ID_INFO);
        if ((mails == null) || (mails.length == 0)) {
            return new String[0];
        }
        final String[] ret = new String[mails.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = mails[i].getMailId();
        }
        return ret;
    }

}
