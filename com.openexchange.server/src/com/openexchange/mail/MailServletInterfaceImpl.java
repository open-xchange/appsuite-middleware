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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.idn.IDNA;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
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
import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.groupware.upload.quotachecker.MailUploadQuotaChecker;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Collators;
import com.openexchange.java.Streams;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageEnhanced;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageBatch;
import com.openexchange.mail.api.IMailMessageStorageExt;
import com.openexchange.mail.api.IMailMessageStorageMimeSupport;
import com.openexchange.mail.api.ISimplifiedThreadStructure;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.unified.UnifiedFullName;
import com.openexchange.mail.api.unified.UnifiedViewService;
import com.openexchange.mail.cache.MailMessageCache;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.config.MailReloadable;
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
import com.openexchange.mail.mime.dataobjects.MimeRawSource;
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
import com.openexchange.mail.transport.MtaStatusInfo;
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
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.mailaccount.internal.RdbMailAccountStorage;
import com.openexchange.push.PushEventConstants;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.openexchange.threadpool.AbstractTask;
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

    private static final String INBOX_ID = "INBOX";

    private static final int MAX_NUMBER_OF_MESSAGES_2_CACHE = 50;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailServletInterfaceImpl.class);

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
    MailServletInterfaceImpl(Session session) throws OXException {
        super();
        warnings = new ArrayList<OXException>(2);
        mailImportResults = new ArrayList<MailImportResult>();
        if (session instanceof ServerSession) {
            ServerSession serverSession = (ServerSession) session;
            ctx = serverSession.getContext();
            usm = serverSession.getUserSettingMail();
            if (!serverSession.getUserPermissionBits().hasWebMail()) {
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

    private User getUser() throws OXException {
        if (null == user) {
            user = UserStorage.getInstance().getUser(session.getUserId(), session.getContextId());
        }
        return user;
    }

    private Locale getUserLocale() {
        if (null == locale) {
            if (session instanceof ServerSession) {
                locale = ((ServerSession) session).getUser().getLocale();
            } else {
                UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class);
                if (null == userService) {
                    return Locale.ENGLISH;
                }
                try {
                    locale = userService.getUser(session.getUserId(), ctx).getLocale();
                } catch (OXException e) {
                    LOG.warn("", e);
                    return Locale.ENGLISH;
                }
            }
        }
        return locale;
    }

    private MailAccount getMailAccount() throws OXException {
        if (mailAccount == null) {
            try {
                MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
                mailAccount = storageService.getMailAccount(accountId, session.getUserId(), session.getContextId());
            } catch (RuntimeException e) {
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
    public boolean expungeFolder(String folder, boolean hardDelete) throws OXException {
        FullnameArgument fullnameArgument = prepareMailFolderParam(folder);
        int accountId = fullnameArgument.getAccountId();
        initConnection(accountId);
        String fullName = fullnameArgument.getFullname();
        IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
        if (folderStorage instanceof IMailFolderStorageEnhanced) {
            ((IMailFolderStorageEnhanced) folderStorage).expungeFolder(fullName, hardDelete);
        } else {
            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            MailMessage[] messages = messageStorage.searchMessages(
                fullName,
                IndexRange.NULL,
                MailSortField.RECEIVED_DATE,
                OrderDirection.ASC,
                new FlagTerm(MailMessage.FLAG_DELETED, true),
                FIELDS_ID);
            List<String> mailIds = new LinkedList<String>();
            for (MailMessage mailMessage : messages) {
                if (null != mailMessage) {
                    mailIds.add(mailMessage.getMailId());
                }
            }
            if (hardDelete) {
                messageStorage.deleteMessages(fullName, mailIds.toArray(new String[mailIds.size()]), true);
            } else {
                String trashFolder = folderStorage.getTrashFolder();
                if (fullName.equals(trashFolder)) {
                    // Also perform hard-delete when compacting trash folder
                    messageStorage.deleteMessages(fullName, mailIds.toArray(new String[mailIds.size()]), true);
                } else {
                    messageStorage.moveMessages(fullName, trashFolder, mailIds.toArray(new String[mailIds.size()]), true);
                }
            }
        }
        postEvent(accountId, fullName, true);
        String trashFullname = prepareMailFolderParam(getTrashFolder(accountId)).getFullname();
        if (!hardDelete) {
            postEvent(accountId, trashFullname, true);
        }
        return true;
    }

    @Override
    public boolean clearFolder(String folder) throws OXException {
        FullnameArgument fullnameArgument = prepareMailFolderParam(folder);
        int accountId = fullnameArgument.getAccountId();
        initConnection(accountId);
        String fullName = fullnameArgument.getFullname();
        /*
         * Only backup if no hard-delete is set in user's mail configuration and fullName does not denote trash (sub)folder
         */
        boolean backup = (!UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isHardDeleteMsgs() && !(fullName.startsWith(mailAccess.getFolderStorage().getTrashFolder())));
        mailAccess.getFolderStorage().clearFolder(fullName, !backup);
        postEvent(accountId, fullName, true);
        String trashFullname = prepareMailFolderParam(getTrashFolder(accountId)).getFullname();
        if (backup) {
            postEvent(accountId, trashFullname, true);
        }
        try {
            /*
             * Update message cache
             */
            MailMessageCache.getInstance().removeFolderMessages(accountId, fullName, session.getUserId(), contextId);
        } catch (OXException e) {
            LOG.error("", e);
        }
        if (fullName.startsWith(trashFullname)) {
            // Special handling
            MailFolder[] subf = mailAccess.getFolderStorage().getSubfolders(fullName, true);
            for (MailFolder element : subf) {
                String subFullname = element.getFullname();
                mailAccess.getFolderStorage().deleteFolder(subFullname, true);
                postEvent(accountId, subFullname, false);
            }
            postEvent(accountId, trashFullname, false);
        }
        return true;
    }

    @Override
    public boolean clearFolder(String folder, boolean hardDelete) throws OXException {
        FullnameArgument fullnameArgument = prepareMailFolderParam(folder);
        int accountId = fullnameArgument.getAccountId();
        initConnection(accountId);
        String fullName = fullnameArgument.getFullname();
        /*
         * Only backup if no hard-delete is set in user's mail configuration and fullName does not denote trash (sub)folder
         */
        boolean backup;
        if (hardDelete) {
            backup = false;
        } else {
            backup = (!UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isHardDeleteMsgs() && !(fullName.startsWith(mailAccess.getFolderStorage().getTrashFolder())));
        }
        mailAccess.getFolderStorage().clearFolder(fullName, !backup);
        postEvent(accountId, fullName, true);
        String trashFullname = prepareMailFolderParam(getTrashFolder(accountId)).getFullname();
        if (backup) {
            postEvent(accountId, trashFullname, true);
        }
        try {
            /*
             * Update message cache
             */
            MailMessageCache.getInstance().removeFolderMessages(accountId, fullName, session.getUserId(), contextId);
        } catch (OXException e) {
            LOG.error("", e);
        }
        if (fullName.startsWith(trashFullname)) {
            // Special handling
            MailFolder[] subf = mailAccess.getFolderStorage().getSubfolders(fullName, true);
            for (MailFolder element : subf) {
                String subFullname = element.getFullname();
                mailAccess.getFolderStorage().deleteFolder(subFullname, true);
                postEvent(accountId, subFullname, false);
            }
            postEvent(accountId, trashFullname, false);
        }
        return true;
    }

    @Override
    public void close(boolean putIntoCache) throws OXException {
        try {
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = this.mailAccess;
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
    public String[] copyMessages(String sourceFolder, String destFolder, String[] msgUIDs, boolean move) throws OXException {
        FullnameArgument source = prepareMailFolderParam(sourceFolder);
        FullnameArgument dest = prepareMailFolderParam(destFolder);
        String sourceFullname = source.getFullname();
        String destFullname = dest.getFullname();
        int sourceAccountId = source.getAccountId();
        initConnection(sourceAccountId);
        int destAccountId = dest.getAccountId();
        if (sourceAccountId == destAccountId) {
            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            MailMessage[] flagInfo = null;
            if (move) {
                /*
                 * Check for spam action; meaning a move/copy from/to spam folder
                 */
                String spamFullname = mailAccess.getFolderStorage().getSpamFolder();
                int spamAction;
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
            String[] maildIds;
            if (move) {
                maildIds = messageStorage.moveMessages(sourceFullname, destFullname, msgUIDs, false);
                postEvent(sourceAccountId, sourceFullname, true, true);
            } else {
                maildIds = messageStorage.copyMessages(sourceFullname, destFullname, msgUIDs, false);
            }
            /*
             * Restore \Seen flags
             */
            if (null != flagInfo) {
                List<String> list = new LinkedList<String>();
                for (int i = 0; i < maildIds.length; i++) {
                    if (!flagInfo[i].isSeen()) {
                        list.add(maildIds[i]);
                    }
                }
                messageStorage.updateMessageFlags(destFullname, list.toArray(new String[list.size()]), MailMessage.FLAG_SEEN, false);
            }
            postEvent(sourceAccountId, destFullname, true, true);
            try {
                /*
                 * Update message cache
                 */
                if (move) {
                    MailMessageCache.getInstance().removeFolderMessages(sourceAccountId, sourceFullname, session.getUserId(), contextId);
                }
                MailMessageCache.getInstance().removeFolderMessages(destAccountId, destFullname, session.getUserId(), contextId);
            } catch (OXException e) {
                LOG.error("", e);
            }
            return maildIds;
        }
        /*
         * Differing accounts...
         */
        MailAccess<?, ?> destAccess = initMailAccess(destAccountId);
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
            // Chunk wise copy
            int chunkSize;
            {
                ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                chunkSize = null == service ? 50 : service.getIntProperty("com.openexchange.mail.externalChunkSize", 50);
            }
            // Iterate chunks
            int length = msgUIDs.length;
            List<String> retval = new LinkedList<String>();
            for (int start = 0; start < length;) {
                int end = start + chunkSize;
                String[] ids;
                {
                    int len;
                    if (end > length) {
                        end = length;
                        len = end - start;
                    } else {
                        len = chunkSize;
                    }
                    ids = new String[len];
                    System.arraycopy(msgUIDs, start, ids, 0, len);
                }
                // Fetch messages from source folder
                MailMessage[] messages = mailAccess.getMessageStorage().getMessages(sourceFullname, ids, FIELDS_FULL);
                // Append them to destination folder
                String[] destIds = destAccess.getMessageStorage().appendMessages(destFullname, messages);
                if (null == destIds || 0 == destIds.length) {
                    return new String[0];
                }
                // Delete source messages if a move shall be performed
                if (move) {
                    mailAccess.getMessageStorage().deleteMessages(sourceFullname, messages2ids(messages), true);
                    postEvent(sourceAccountId, sourceFullname, true, true);
                }
                // Restore \Seen flags
                if (null != flagInfo) {
                    List<String> list = new LinkedList<String>();
                    for (int i = 0; i < destIds.length; i++) {
                        if (!flagInfo[i].isSeen()) {
                            list.add(destIds[i]);
                        }
                    }
                    destAccess.getMessageStorage().updateMessageFlags(
                        destFullname,
                        list.toArray(new String[list.size()]),
                        MailMessage.FLAG_SEEN,
                        false);
                }
                postEvent(destAccountId, destFullname, true, true);
                try {
                    if (move) {
                        /*
                         * Update message cache
                         */
                        MailMessageCache.getInstance().removeFolderMessages(sourceAccountId, sourceFullname, session.getUserId(), contextId);
                    }
                    MailMessageCache.getInstance().removeFolderMessages(destAccountId, destFullname, session.getUserId(), contextId);
                } catch (OXException e) {
                    LOG.error("", e);
                }
                // Prepare for next iteration
                retval.addAll(Arrays.asList(destIds));
                start = end;
            }
            // Return destination identifiers
            return retval.toArray(new String[retval.size()]);
        } finally {
            destAccess.close(true);
        }
    }

    @Override
    public String deleteFolder(String folder) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        /*
         * Only backup if fullName does not denote trash (sub)folder
         */
        IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
        String trashFullname = folderStorage.getTrashFolder();
        boolean hardDelete = fullName.startsWith(trashFullname);
        /*
         * Remember subfolder tree
         */
        Map<String, Map<?, ?>> subfolders = subfolders(fullName);
        String retval = prepareFullname(accountId, folderStorage.deleteFolder(fullName, hardDelete));
        postEvent(accountId, fullName, false, true, false);
        try {
            /*
             * Update message cache
             */
            MailMessageCache.getInstance().removeFolderMessages(accountId, fullName, session.getUserId(), contextId);
        } catch (OXException e) {
            LOG.error("", e);
        }
        if (!hardDelete) {
            // New folder in trash folder
            postEventRemote(accountId, trashFullname, false);
        }
        postEvent4Subfolders(accountId, subfolders);
        return retval;
    }

    private void postEvent4Subfolders(int accountId, Map<String, Map<?, ?>> subfolders) {
        int size = subfolders.size();
        Iterator<Entry<String, Map<?, ?>>> iter = subfolders.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            Entry<String, Map<?, ?>> entry = iter.next();
            @SuppressWarnings("unchecked") Map<String, Map<?, ?>> m = (Map<String, Map<?, ?>>) entry.getValue();
            if (!m.isEmpty()) {
                postEvent4Subfolders(accountId, m);
            }
            postEventRemote(accountId, entry.getKey(), false);
        }
    }

    private Map<String, Map<?, ?>> subfolders(String fullName) throws OXException {
        Map<String, Map<?, ?>> m = new HashMap<String, Map<?, ?>>();
        subfoldersRecursively(fullName, m);
        return m;
    }

    private void subfoldersRecursively(String parent, Map<String, Map<?, ?>> m) throws OXException {
        MailFolder[] mailFolders = mailAccess.getFolderStorage().getSubfolders(parent, true);
        if (null == mailFolders || 0 == mailFolders.length) {
            Map<String, Map<?, ?>> emptyMap = Collections.emptyMap();
            m.put(parent, emptyMap);
        } else {
            Map<String, Map<?, ?>> subMap = new HashMap<String, Map<?, ?>>();
            int size = mailFolders.length;
            for (int i = 0; i < size; i++) {
                String fullName = mailFolders[i].getFullname();
                subfoldersRecursively(fullName, subMap);
            }
            m.put(parent, subMap);
        }
    }

    @Override
    public boolean deleteMessages(String folder, String[] msgUIDs, boolean hardDelete) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        /*
         * Hard-delete if hard-delete is set in user's mail configuration or fullName denotes trash (sub)folder
         */
        String trashFullname = mailAccess.getFolderStorage().getTrashFolder();
        boolean hd = (hardDelete || UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isHardDeleteMsgs() || (null != trashFullname && fullName.startsWith(trashFullname)));
        mailAccess.getMessageStorage().deleteMessages(fullName, msgUIDs, hd);
        try {
            /*
             * Update message cache
             */
            MailMessageCache.getInstance().removeFolderMessages(accountId, fullName, session.getUserId(), contextId);
        } catch (OXException e) {
            LOG.error("", e);
        }
        postEvent(accountId, fullName, true, true, false);
        if (!hd) {
            postEvent(accountId, trashFullname, true, true, false);
        }
        return true;
    }

    @Override
    public int[] getAllMessageCount(String folder) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        initConnection(argument.getAccountId());
        String fullName = argument.getFullname();
        IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
        MailFolder f = folderStorage.getFolder(fullName);
        if (folderStorage instanceof IMailFolderStorageEnhanced) {
            IMailFolderStorageEnhanced storageEnhanced = (IMailFolderStorageEnhanced) folderStorage;
            int totalCounter = storageEnhanced.getTotalCounter(fullName);
            int unreadCounter = storageEnhanced.getUnreadCounter(fullName);
            int newCounter = storageEnhanced.getNewCounter(fullName);
            return new int[] { totalCounter, newCounter, unreadCounter, f.getDeletedMessageCount() };
        }
        int totalCounter = mailAccess.getMessageStorage().searchMessages(fullName, IndexRange.NULL, MailSortField.RECEIVED_DATE, OrderDirection.ASC, null, FIELDS_ID).length;
        int unreadCounter =  mailAccess.getMessageStorage().getUnreadMessages(fullName, MailSortField.RECEIVED_DATE, OrderDirection.DESC, FIELDS_ID, -1).length;
        return new int[] { totalCounter, f.getNewMessageCount(), unreadCounter, f.getDeletedMessageCount() };
    }

    @Override
    public SearchIterator<MailMessage> getAllMessages(String folder, int sortCol, int order, int[] fields, String[] headerFields, int[] fromToIndices, boolean supportsContinuation) throws OXException {
        return getMessages(folder, fromToIndices, sortCol, order, null, null, false, fields, headerFields, supportsContinuation);
    }

    private static final MailMessageComparator COMPARATOR_DESC = new MailMessageComparator(MailSortField.RECEIVED_DATE, true, null);

    @Override
    public List<List<MailMessage>> getAllSimpleThreadStructuredMessages(String folder, boolean includeSent, boolean cache, int sortCol, int order, int[] fields, int[] fromToIndices, final long lookAhead) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        boolean mergeWithSent = includeSent && !mailAccess.getFolderStorage().getSentFolder().equals(fullName);
        final MailFields mailFields = new MailFields(MailField.getFields(fields));
        mailFields.add(MailField.toField(MailListField.getField(sortCol)));
        // Check message storage
        final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
        if (messageStorage instanceof ISimplifiedThreadStructure) {
            ISimplifiedThreadStructure simplifiedThreadStructure = (ISimplifiedThreadStructure) messageStorage;
            // Effective fields
            // Perform operation
            try {
                return simplifiedThreadStructure.getThreadSortedMessages(
                    fullName,
                    mergeWithSent,
                    cache,
                    null == fromToIndices ? IndexRange.NULL : new IndexRange(fromToIndices[0], fromToIndices[1]),
                        lookAhead,
                        MailSortField.getField(sortCol),
                        OrderDirection.getOrderDirection(order),
                        mailFields.toArray());
            } catch (OXException e) {
                // Check for missing "THREAD=REFERENCES" capability
                if ((2046 != e.getCode() || (!"MSG".equals(e.getPrefix()) && !"IMAP".equals(e.getPrefix()))) && !MailExceptionCode.UNSUPPORTED_OPERATION.equals(e)) {
                    throw e;
                }
            }
        }
        /*
         * Sort by references
         */
        Future<List<MailMessage>> messagesFromSentFolder;
        if (mergeWithSent) {
            final String sentFolder = mailAccess.getFolderStorage().getSentFolder();
            messagesFromSentFolder = ThreadPools.getThreadPool().submit(new AbstractTask<List<MailMessage>>() {

                @Override
                public List<MailMessage> call() throws Exception {
                    return Conversations.messagesFor(sentFolder, (int) lookAhead, mailFields, messageStorage);
                }
            });
        } else {
            messagesFromSentFolder = null;
        }
        // For actual folder
        List<Conversation> conversations = Conversations.conversationsFor(fullName, (int) lookAhead, mailFields, messageStorage);
        // Retrieve from sent folder
        if (null != messagesFromSentFolder) {
            List<MailMessage> sentMessages = getFrom(messagesFromSentFolder);
            for (Conversation conversation : conversations) {
                for (MailMessage sentMessage : sentMessages) {
                    if (conversation.referencesOrIsReferencedBy(sentMessage)) {
                        conversation.addMessage(sentMessage);
                    }
                }
            }
        }
        // Fold it
        Conversations.fold(conversations);
        // Comparator
        MailMessageComparator threadComparator = COMPARATOR_DESC;
        // Sort
        List<List<MailMessage>> list = new LinkedList<List<MailMessage>>();
        for (Conversation conversation : conversations) {
            list.add(conversation.getMessages(threadComparator));
        }
        // Sort root elements
        {
            MailSortField sortField = MailSortField.getField(sortCol);
            MailSortField effectiveSortField = null == sortField ? MailSortField.RECEIVED_DATE : sortField;
            Comparator<List<MailMessage>> listComparator = getListComparator(effectiveSortField, OrderDirection.getOrderDirection(order), getUserLocale());
            Collections.sort(list, listComparator);
        }
        // Check for index range
        IndexRange indexRange = null == fromToIndices ? IndexRange.NULL : new IndexRange(fromToIndices[0], fromToIndices[1]);
        if (null != indexRange) {
            int fromIndex = indexRange.start;
            int toIndex = indexRange.end;
            int size = list.size();
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
        Task<ThreadableMapping> task = new AbstractTrackableTask<ThreadableMapping>() {

            @Override
            public ThreadableMapping call() throws Exception {
                List<MailMessage> mails = Conversations.messagesFor(sentFolder, limit, mailFields, messageStorage);
                return new ThreadableMapping(64).initWith(mails);
            }

        };
        return ThreadPools.getThreadPool().submit(task, CallerRunsBehavior.<ThreadableMapping> getInstance());
    }

    private Comparator<List<MailMessage>> getListComparator(final MailSortField sortField, final OrderDirection order, Locale locale) {
        final MailMessageComparator comparator = new MailMessageComparator(sortField, OrderDirection.DESC.equals(order), locale);
        Comparator<List<MailMessage>> listComparator = new Comparator<List<MailMessage>>() {

            @Override
            public int compare(List<MailMessage> o1, List<MailMessage> o2) {
                int result = comparator.compare(o1.get(0), o2.get(0));
                if ((0 != result) || (MailSortField.RECEIVED_DATE != sortField)) {
                    return result;
                }
                // Zero as comparison result AND primarily sorted by received-date
                MailMessage msg1 = o1.get(0);
                MailMessage msg2 = o2.get(0);
                String inReplyTo1 = msg1.getInReplyTo();
                String inReplyTo2 = msg2.getInReplyTo();
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

    private static <T> T getFrom(Future<T> f) throws OXException {
        if (null == f) {
            return null;
        }
        try {
            return f.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Keep interrupted state
            throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
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
    private <C extends Collection<MailMessage>, W extends Collection<C>> W setAccountInfo2(W col) throws OXException {
        MailAccount account = getMailAccount();
        String name = account.getName();
        int id = account.getId();
        for (C mailMessages : col) {
            for (MailMessage mailMessage : mailMessages) {
                if (null != mailMessage) {
                    mailMessage.setAccountId(id);
                    mailMessage.setAccountName(name);
                }
            }
        }
        return col;
    }

    @Override
    public SearchIterator<MailMessage> getAllThreadedMessages(String folder, int sortCol, int order, int[] fields, int[] fromToIndices) throws OXException {
        return getThreadedMessages(folder, fromToIndices, sortCol, order, null, null, false, fields);
    }

    @Override
    public SearchIterator<MailFolder> getChildFolders(String parentFolder, boolean all) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(parentFolder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String parentFullname = argument.getFullname();
        List<MailFolder> children = new LinkedList<MailFolder>(Arrays.asList(mailAccess.getFolderStorage().getSubfolders(
            parentFullname,
            all)));
        if (children.isEmpty()) {
            return SearchIteratorAdapter.emptyIterator();
        }
        /*
         * Filter against possible POP3 storage folders
         */
        if (MailAccount.DEFAULT_ID == accountId && MailProperties.getInstance().isHidePOP3StorageFolders()) {
            Set<String> pop3StorageFolders = RdbMailAccountStorage.getPOP3StorageFolders(session);
            for (Iterator<MailFolder> it = children.iterator(); it.hasNext();) {
                MailFolder mailFolder = it.next();
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
        String[] names;
        if (isDefaultFoldersChecked(accountId)) {
            names = getSortedDefaultMailFolders(accountId);
        } else {
            List<String> tmp = new LinkedList<String>();

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
    public String getConfirmedHamFolder(int accountId) throws OXException {
        if (isDefaultFoldersChecked(accountId)) {
            return prepareFullname(accountId, getDefaultMailFolder(StorageUtility.INDEX_CONFIRMED_HAM, accountId));
        }
        initConnection(accountId);
        return prepareFullname(accountId, mailAccess.getFolderStorage().getConfirmedHamFolder());
    }

    @Override
    public String getConfirmedSpamFolder(int accountId) throws OXException {
        if (isDefaultFoldersChecked(accountId)) {
            return prepareFullname(accountId, getDefaultMailFolder(StorageUtility.INDEX_CONFIRMED_SPAM, accountId));
        }
        initConnection(accountId);
        return prepareFullname(accountId, mailAccess.getFolderStorage().getConfirmedSpamFolder());
    }

    private String getDefaultMailFolder(int index, int accountId) {
        String[] arr = MailSessionCache.getInstance(session).getParameter(
            accountId,
            MailSessionParameterNames.getParamDefaultFolderArray());
        return arr == null ? null : arr[index];
    }

    private String[] getSortedDefaultMailFolders(int accountId) {
        String[] arr = MailSessionCache.getInstance(session).getParameter(
            accountId,
            MailSessionParameterNames.getParamDefaultFolderArray());
        if (arr == null) {
            return new String[0];
        }
        return new String[] {
            INBOX_ID, arr[StorageUtility.INDEX_DRAFTS], arr[StorageUtility.INDEX_SENT], arr[StorageUtility.INDEX_SPAM],
            arr[StorageUtility.INDEX_TRASH] };
    }

    @Override
    public int getDeletedMessageCount(String folder) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        initConnection(argument.getAccountId());
        String fullName = argument.getFullname();
        return mailAccess.getFolderStorage().getFolder(fullName).getDeletedMessageCount();
    }

    @Override
    public String getDraftsFolder(int accountId) throws OXException {
        if (isDefaultFoldersChecked(accountId)) {
            return prepareFullname(accountId, getDefaultMailFolder(StorageUtility.INDEX_DRAFTS, accountId));
        }
        initConnection(accountId);
        return prepareFullname(accountId, mailAccess.getFolderStorage().getDraftsFolder());
    }

    @Override
    public MailFolder getFolder(String folder, boolean checkFolder) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        initConnection(argument.getAccountId());
        String fullName = argument.getFullname();
        return mailAccess.getFolderStorage().getFolder(fullName);
    }

    private static volatile Integer maxForwardCount;
    private static int maxForwardCount() {
        Integer tmp = maxForwardCount;
        if (null == tmp) {
            synchronized (MailServletInterfaceImpl.class) {
                tmp = maxForwardCount;
                if (null == tmp) {
                    ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        return 8;
                    }
                    tmp = Integer.valueOf(service.getIntProperty("com.openexchange.mail.maxForwardCount", 8));
                    maxForwardCount = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    static {
        MailReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                maxForwardCount = null;
            }

            @Override
            public Map<String, String[]> getConfigFileNames() {
                return null;
            }
        });
    }

    @Override
    public MailMessage getForwardMessageForDisplay(String[] folders, String[] fowardMsgUIDs, UserSettingMail usm, boolean setFrom) throws OXException {
        if ((null == folders) || (null == fowardMsgUIDs) || (folders.length != fowardMsgUIDs.length)) {
            throw new IllegalArgumentException("Illegal arguments");
        }
        int maxForwardCount = maxForwardCount();
        if (maxForwardCount > 0 && folders.length > maxForwardCount) {
            throw MailExceptionCode.TOO_MANY_FORWARD_MAILS.create(Integer.valueOf(maxForwardCount));
        }
        FullnameArgument[] arguments = new FullnameArgument[folders.length];
        for (int i = 0; i < folders.length; i++) {
            arguments[i] = prepareMailFolderParam(folders[i]);
        }
        boolean sameAccount = true;
        int accountId = arguments[0].getAccountId();
        int length = arguments.length;
        for (int i = 1; sameAccount && i < length; i++) {
            sameAccount = accountId == arguments[i].getAccountId();
        }
        TransportProperties transportProperties = TransportProperties.getInstance();
        MailUploadQuotaChecker checker = new MailUploadQuotaChecker(usm);
        long maxPerMsg = checker.getFileQuotaMax();
        long max = checker.getQuotaMax();
        if (sameAccount) {
            initConnection(accountId);
            MailMessage[] originalMails = new MailMessage[folders.length];
            int mlength = length - 1;
            if (transportProperties.isPublishOnExceededQuota() && (!transportProperties.isPublishPrimaryAccountOnly() || MailAccount.DEFAULT_ID == accountId)) {
                for (int i = 0; i < length; i++) {
                    String fullName = arguments[i].getFullname();
                    MailMessage origMail = mailAccess.getMessageStorage().getMessage(fullName, fowardMsgUIDs[i], false);
                    if (null == origMail) {
                        throw MailExceptionCode.MAIL_NOT_FOUND.create(fowardMsgUIDs[i], fullName);
                    }
                    if (i < mlength && !fullName.equals(arguments[i + 1].getFullname())) {
                        origMail.loadContent();
                    }
                    originalMails[i] = origMail;
                }
            } else {
                long total = 0;
                for (int i = 0; i < length; i++) {
                    String fullName = arguments[i].getFullname();
                    MailMessage origMail = mailAccess.getMessageStorage().getMessage(fullName, fowardMsgUIDs[i], false);
                    if (null == origMail) {
                        throw MailExceptionCode.MAIL_NOT_FOUND.create(fowardMsgUIDs[i], fullName);
                    }
                    long size = origMail.getSize();
                    if (size <= 0) {
                        size = 2048; // Avg size
                    }
                    if (maxPerMsg > 0 && size > maxPerMsg) {
                        String fileName = origMail.getSubject();
                        throw MailExceptionCode.UPLOAD_QUOTA_EXCEEDED_FOR_FILE.create(UploadUtility.getSize(maxPerMsg), null == fileName ? "" : fileName, UploadUtility.getSize(size));
                    }
                    total += size;
                    if (max > 0 && total > max) {
                        throw MailExceptionCode.UPLOAD_QUOTA_EXCEEDED.create(UploadUtility.getSize(max));
                    }
                    if (i < mlength && !fullName.equals(arguments[i + 1].getFullname())) {
                        origMail.loadContent();
                    }
                    originalMails[i] = origMail;
                }
            }
            return mailAccess.getLogicTools().getFowardMessage(originalMails, usm, setFrom);
        }
        MailMessage[] originalMails = new MailMessage[folders.length];
        if (transportProperties.isPublishOnExceededQuota() && (!transportProperties.isPublishPrimaryAccountOnly() || MailAccount.DEFAULT_ID == accountId)) {
            for (int i = 0; i < length && sameAccount; i++) {
                MailAccess<?, ?> ma = initMailAccess(arguments[i].getAccountId());
                try {
                    MailMessage origMail = ma.getMessageStorage().getMessage(arguments[i].getFullname(), fowardMsgUIDs[i], false);
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
            for (int i = 0; i < length && sameAccount; i++) {
                MailAccess<?, ?> ma = initMailAccess(arguments[i].getAccountId());
                try {
                    MailMessage origMail = ma.getMessageStorage().getMessage(arguments[i].getFullname(), fowardMsgUIDs[i], false);
                    if (null == origMail) {
                        throw MailExceptionCode.MAIL_NOT_FOUND.create(fowardMsgUIDs[i], arguments[i].getFullname());
                    }
                    long size = origMail.getSize();
                    if (size <= 0) {
                        size = 2048; // Avg size
                    }
                    if (maxPerMsg > 0 && size > maxPerMsg) {
                        String fileName = origMail.getSubject();
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
        int[] accountIDs = new int[originalMails.length];
        for (int i = 0; i < accountIDs.length; i++) {
            accountIDs[i] = arguments[i].getAccountId();
        }
        return MimeForward.getFowardMail(originalMails, session, accountIDs, usm, setFrom);
    }

    @Override
    public String getInboxFolder(int accountId) throws OXException {
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
    public MailMessage getMessage(String folder, String msgUID) throws OXException {
        return getMessage(folder, msgUID, true);
    }

    @Override
    public MailMessage getMessage(String folder, String msgUID, boolean markAsSeen) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        if (MailFolder.DEFAULT_FOLDER_ID.equals(folder)) {
            throw MailExceptionCode.FOLDER_DOES_NOT_HOLD_MESSAGES.create(MailFolder.DEFAULT_FOLDER_ID);
        }
        String fullName = argument.getFullname();
        MailMessage mail = mailAccess.getMessageStorage().getMessage(fullName, msgUID, markAsSeen);
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
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
        return mail;
    }

    @Override
    public MailPart getMessageAttachment(String folder, String msgUID, String attachmentPosition, boolean displayVersion) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        return mailAccess.getMessageStorage().getAttachment(fullName, msgUID, attachmentPosition);
    }

    @Override
    public List<MailPart> getAllMessageAttachments(String folder, String msgUID) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();

        MailMessage message = mailAccess.getMessageStorage().getMessage(folder, fullName, false);
        if (null == message) {
            throw MailExceptionCode.MAIL_NOT_FOUND.create(msgUID, fullName);
        }

        NonInlineForwardPartHandler handler = new NonInlineForwardPartHandler();
        new MailMessageParser().setInlineDetectorBehavior(true).parseMailMessage(message, handler);
        return handler.getNonInlineParts();
    }

    @Override
    public ManagedFile getMessages(String folder, String[] msgIds) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        /*
         * Get parts
         */
        MailMessage[] mails = new MailMessage[msgIds.length];
        for (int i = 0; i < msgIds.length; i++) {
            mails[i] = mailAccess.getMessageStorage().getMessage(fullName, msgIds[i], false);
        }
        /*
         * Store them temporary to files
         */
        ManagedFileManagement mfm = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class, true);

        ManagedFile[] files = new ManagedFile[mails.length];
        try {
            ByteArrayOutputStream bout = new UnsynchronizedByteArrayOutputStream(8192);
            for (int i = 0; i < files.length; i++) {
                MailMessage mail = mails[i];
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
                File tempFile = mfm.newTempFile();
                ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(new FileOutputStream(tempFile));
                zipOutput.setEncoding("UTF-8");
                zipOutput.setUseLanguageEncodingFlag(true);
                try {
                    byte[] buf = new byte[8192];
                    Set<String> names = new HashSet<String>(files.length);
                    for (int i = 0; i < files.length; i++) {
                        ManagedFile file = files[i];
                        File tmpFile = null == file ? null : file.getFile();
                        if (null != tmpFile) {
                            FileInputStream in = new FileInputStream(tmpFile);
                            try {
                                /*
                                 * Add ZIP entry to output stream
                                 */
                                String subject = mails[i].getSubject();
                                String ext = ".eml";
                                String name = (com.openexchange.java.Strings.isEmpty(subject) ? "mail" + (i + 1) : saneForFileName(subject)) + ext;
                                int reslen = name.lastIndexOf('.');
                                int count = 1;
                                while (false == names.add(name)) {
                                    // Name already contained
                                    name = name.substring(0, reslen);
                                    name = new StringBuilder(name).append("_(").append(count++).append(')').append(ext).toString();
                                }
                                ZipArchiveEntry entry;
                                int num = 1;
                                while (true) {
                                    try {
                                        int pos = name.indexOf(ext);
                                        String entryName = name.substring(0, pos) + (num > 1 ? "_(" + num + ")" : "") + ext;
                                        entry = new ZipArchiveEntry(entryName);
                                        zipOutput.putArchiveEntry(entry);
                                        break;
                                    } catch (java.util.zip.ZipException e) {
                                        String message = e.getMessage();
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
                                } catch (IOException e) {
                                    LOG.error("", e);
                                }
                            }
                        }
                    }
                } finally {
                    // Complete the ZIP file
                    try {
                        zipOutput.close();
                    } catch (IOException e) {
                        LOG.error("", e);
                    }
                }
                /*
                 * Return managed file
                 */
                return mfm.createManagedFile(tempFile);
            } catch (IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                }
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        } catch (OXException e) {
            throw e;
        } finally {
            for (ManagedFile file : files) {
                if (null != file) {
                    file.delete();
                }
            }
        }
    }

    @Override
    public ManagedFile getMessageAttachments(String folder, String msgUID, String[] attachmentPositions) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        /*
         * Get parts
         */
        MailPart[] parts;
        if (null == attachmentPositions) {
            List<MailPart> l = getAllMessageAttachments(folder, msgUID);
            parts = l.toArray(new MailPart[l.size()]);
        } else {
            parts = new MailPart[attachmentPositions.length];
            for (int i = 0; i < parts.length; i++) {
                parts[i] = mailAccess.getMessageStorage().getAttachment(fullName, msgUID, attachmentPositions[i]);
            }
        }
        /*
         * Store them temporary to files
         */
        ManagedFileManagement mfm = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class, true);
        ManagedFile[] files = new ManagedFile[parts.length];
        try {
            for (int i = 0; i < files.length; i++) {
                MailPart part = parts[i];
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
                File tempFile = mfm.newTempFile();
                ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(new FileOutputStream(tempFile));
                zipOutput.setEncoding("UTF-8");
                zipOutput.setUseLanguageEncodingFlag(true);
                try {
                    byte[] buf = new byte[8192];
                    for (int i = 0; i < files.length; i++) {
                        ManagedFile file = files[i];
                        File tmpFile = null == file ? null : file.getFile();
                        if (null != tmpFile) {
                            FileInputStream in = new FileInputStream(tmpFile);
                            try {
                                /*
                                 * Add ZIP entry to output stream
                                 */
                                String name = parts[i].getFileName();
                                if (null == name) {
                                    List<String> extensions = MimeType2ExtMap.getFileExtensions(parts[i].getContentType().getBaseType());
                                    name = extensions == null || extensions.isEmpty() ? "part.dat" : "part." + extensions.get(0);
                                }
                                int num = 1;
                                ZipArchiveEntry entry;
                                while (true) {
                                    try {
                                        String entryName;
                                        {
                                            int pos = name.indexOf('.');
                                            if (pos < 0) {
                                                entryName = name + (num > 1 ? "_(" + num + ")" : "");
                                            } else {
                                                entryName = name.substring(0, pos) + (num > 1 ? "_(" + num + ")" : "") + name.substring(pos);
                                            }
                                        }
                                        entry = new ZipArchiveEntry(entryName);
                                        zipOutput.putArchiveEntry(entry);
                                        break;
                                    } catch (java.util.zip.ZipException e) {
                                        String message = e.getMessage();
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
            } catch (IOException e) {
                if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
                }
                throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        } finally {
            for (ManagedFile file : files) {
                if (null != file) {
                    file.delete();
                }
            }
        }
    }

    @Override
    public int getMessageCount(String folder) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
        if (folderStorage instanceof IMailFolderStorageEnhanced) {
            return ((IMailFolderStorageEnhanced) folderStorage).getTotalCounter(fullName);
        }
        return folderStorage.getFolder(fullName).getMessageCount();
    }

    @Override
    public MailPart getMessageImage(String folder, String msgUID, String cid) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        return mailAccess.getMessageStorage().getImageAttachment(fullName, msgUID, cid);
    }

    @Override
    public MailMessage[] getMessageList(String folder, String[] uids, int[] fields, String[] headerFields) throws OXException {
        /*
         * Although message cache is only used within mail implementation, we have to examine if cache already holds desired messages. If
         * the cache holds the desired messages no connection has to be fetched/established. This avoids a lot of overhead.
         */
        int accountId;
        String fullName;
        {
            FullnameArgument argument = prepareMailFolderParam(folder);
            accountId = argument.getAccountId();
            fullName = argument.getFullname();
        }
        boolean loadHeaders = (null != headerFields && 0 < headerFields.length);
        /*-
         * Check for presence in cache
         * TODO: Think about switching to live-fetch if loadHeaders is true. Loading all data once may be faster than
         * first loading from cache then loading missing headers in next step
         */
        try {
            MailMessage[] mails = MailMessageCache.getInstance().getMessages(
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
                    List<String> loadMe = new LinkedList<String>();
                    Map<String, MailMessage> finder = new HashMap<String, MailMessage>(mails.length);
                    for (MailMessage mail : mails) {
                        String mailId = mail.getMailId();
                        finder.put(mailId, mail);
                        if (!mail.hasHeaders(headerFields)) {
                            loadMe.add(mailId);
                        }
                    }
                    if (!loadMe.isEmpty()) {
                        initConnection(accountId);
                        IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                        if (messageStorage instanceof IMailMessageStorageExt) {
                            IMailMessageStorageExt messageStorageExt = (IMailMessageStorageExt) messageStorage;
                            for (MailMessage header : messageStorageExt.getMessages(
                                fullName,
                                loadMe.toArray(new String[loadMe.size()]),
                                FIELDS_ID_INFO,
                                headerFields)) {
                                if (null != header) {
                                    MailMessage mailMessage = finder.get(header.getMailId());
                                    if (null != mailMessage) {
                                        mailMessage.addHeaders(header.getHeaders());
                                    }
                                }
                            }
                        } else {
                            for (MailMessage header : messageStorage.getMessages(fullName, loadMe.toArray(new String[loadMe.size()]), HEADERS)) {
                                if (null != header) {
                                    MailMessage mailMessage = finder.get(header.getMailId());
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
        } catch (OXException e) {
            LOG.error("", e);
        }
        /*
         * Live-Fetch from mail storage
         */
        initConnection(accountId);
        MailMessage[] mails;
        IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
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
            MailField[] mailFields;
            if (loadHeaders) {
                /*
                 * Ensure MailField.HEADERS is contained
                 */
                MailFields col = new MailFields(MailField.toFields(MailListField.getFields(fields)));
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
        } catch (OXException e) {
            LOG.error("", e);
        }
        return mails;
    }

    @Override
    public SearchIterator<MailMessage> getMessages(String folder, int[] fromToIndices, int sortCol, int order, com.openexchange.search.SearchTerm<?> searchTerm, boolean linkSearchTermsWithOR, int[] fields, String[] headerFields, boolean supportsContinuation) throws OXException {
        return getMessagesInternal(prepareMailFolderParam(folder), SearchTermMapper.map(searchTerm), fromToIndices, sortCol, order, fields, headerFields, supportsContinuation);
    }

    @Override
    public SearchIterator<MailMessage> getMessages(String folder, int[] fromToIndices, int sortCol, int order, int[] searchCols, String[] searchPatterns, boolean linkSearchTermsWithOR, int[] fields, String[] headerFields, boolean supportsContinuation) throws OXException {
        checkPatternLength(searchPatterns);
        SearchTerm<?> searchTerm = (searchCols == null) || (searchCols.length == 0) ? null : SearchUtility.parseFields(
            searchCols,
            searchPatterns,
            linkSearchTermsWithOR);
        return getMessagesInternal(prepareMailFolderParam(folder), searchTerm, fromToIndices, sortCol, order, fields, headerFields, supportsContinuation);
    }

    private SearchIterator<MailMessage> getMessagesInternal(FullnameArgument argument, SearchTerm<?> searchTerm, int[] fromToIndices, int sortCol, int order, int[] fields, String[] headerNames, boolean supportsContinuation) throws OXException {
        /*
         * Identify and sort messages according to search term and sort criteria while only fetching their IDs
         */
        String fullName = argument.getFullname();
        MailMessage[] mails;
        {
            IndexRange indexRange = null == fromToIndices ? IndexRange.NULL : new IndexRange(fromToIndices[0], fromToIndices[1]);
            MailSortField sortField = MailSortField.getField(sortCol);
            OrderDirection orderDir = OrderDirection.getOrderDirection(order);
            if ("unified/inbox".equalsIgnoreCase(fullName)) {
                UnifiedViewService unifiedView = ServerServiceRegistry.getInstance().getService(UnifiedViewService.class);
                if (null == unifiedView) {
                    throw MailExceptionCode.FOLDER_NOT_FOUND.create(fullName);
                }
                mails = unifiedView.searchMessages(UnifiedFullName.INBOX, indexRange, sortField, orderDir, searchTerm, FIELDS_ID_INFO, session);
                int accountId = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class).getUnifiedINBOXAccountID(session);
                initConnection(accountId);
                fullName = UnifiedFullName.INBOX.getFullName();
            } else {
                int accountId = argument.getAccountId();
                initConnection(accountId);
                mails = mailAccess.getMessageStorage().searchMessages(fullName, indexRange, sortField, orderDir, searchTerm, FIELDS_ID_INFO);
            }
        }
        /*
         * Proceed
         */
        if ((mails == null) || (mails.length == 0) || onlyNull(mails)) {
            return SearchIteratorAdapter.<MailMessage> emptyIterator();
        }
        boolean cachable = (mails.length < mailAccess.getMailConfig().getMailProperties().getMailFetchLimit());

        MailField[] useFields;
        boolean onlyFolderAndID;
        if (cachable) {
            /*
             * Selection fits into cache: Prepare for caching
             */
            useFields = com.openexchange.mail.mime.utils.MimeStorageUtility.getCacheFieldsArray();
            onlyFolderAndID = false;
        } else {
            useFields = MailField.getFields(fields);
            onlyFolderAndID = (null != headerNames && 0 < headerNames.length) ? false : onlyFolderAndID(useFields);
        }
        if (supportsContinuation) {
            MailFields mfs = new MailFields(useFields);
            if (!mfs.contains(MailField.SUPPORTS_CONTINUATION)) {
                mfs.add(MailField.SUPPORTS_CONTINUATION);
                useFields = mfs.toArray();
            }
        }
        /*-
         * More than ID and folder requested?
         *  AND
         * Messages do not already contain requested fields although only IDs were requested
         */
        if (!onlyFolderAndID && !containsAll(firstNotNull(mails), useFields)) {
            /*
             * Extract IDs
             */
            String[] mailIds = new String[mails.length];
            for (int i = 0; i < mailIds.length; i++) {
                MailMessage m = mails[i];
                if (null != m) {
                    mailIds[i] = m.getMailId();
                }
            }
            /*
             * Fetch identified messages by their IDs and pre-fill them according to specified fields
             */
            if (null != headerNames && 0 < headerNames.length) {
                IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                if (messageStorage instanceof IMailMessageStorageExt) {
                    mails = ((IMailMessageStorageExt) messageStorage).getMessages(fullName, mailIds, useFields, headerNames);
                } else {
                    useFields = MailFields.addIfAbsent(useFields, MailField.ID);
                    mails = messageStorage.getMessages(fullName, mailIds, useFields);

                    int length = mails.length;
                    MailMessage[] headers;
                    {
                        String[] ids = new String[length];
                        for (int i = ids.length; i-- > 0;) {
                            MailMessage m = mails[i];
                            ids[i] = null == m ? null : m.getMailId();
                        }
                        headers = messageStorage.getMessages(fullName, ids, MailFields.toArray(MailField.HEADERS));
                    }

                    for (int i = length; i-- > 0;) {
                        MailMessage mailMessage = mails[i];
                        if (null != mailMessage) {
                            MailMessage header = headers[i];
                            if (null != header) {
                                for (String headerName : headerNames) {
                                    String[] values = header.getHeader(headerName);
                                    if (null != values) {
                                        for (String value : values) {
                                            mailMessage.addHeader(headerName, value);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                mails = mailAccess.getMessageStorage().getMessages(fullName, mailIds, useFields);
            }
            if ((mails == null) || (mails.length == 0) || onlyNull(mails)) {
                return SearchIteratorAdapter.emptyIterator();
            }
        }
        /*
         * Set account information
         */
        for (MailMessage mail : mails) {
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
        } catch (OXException e) {
            LOG.error("", e);
        }
        List<MailMessage> l = new LinkedList<MailMessage>();
        for (MailMessage mm : mails) {
            if (null != mm) {
                l.add(mm);
            }
        }
        return new SearchIteratorDelegator<MailMessage>(l);
    }

    private static boolean onlyNull(MailMessage[] mails) {
        boolean ret = true;
        for (int i = mails.length; ret && i-- > 0;) {
            ret = (null == mails[i]);
        }
        return ret;
    }

    private static MailMessage firstNotNull(MailMessage[] mails) {
        for (int i = mails.length; i-- > 0;) {
            MailMessage m = mails[i];
            if (null != m) {
                return m;
            }
        }
        return null;
    }

    private static boolean containsAll(MailMessage candidate, MailField[] fields) {
        if (null == candidate) {
            return false;
        }
        boolean contained = true;
        int length = fields.length;
        for (int i = 0; contained && i < length; i++) {
            MailField field = fields[i];
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
    private static boolean onlyFolderAndID(MailField[] fields) {
        if (fields.length != 2) {
            return false;
        }
        int i = 0;
        for (MailField field : fields) {
            if (MailField.ID.equals(field)) {
                i |= 1;
            } else if (MailField.FOLDER_ID.equals(field)) {
                i |= 2;
            }
        }
        return (i == 3);
    }

    @Override
    public String[] appendMessages(String destFolder, MailMessage[] mails, boolean force) throws OXException {
        return appendMessages(destFolder, mails, force, false);
    }

    @Override
    public String[] importMessages(String destFolder, MailMessage[] mails, boolean force) throws OXException {
        return appendMessages(destFolder, mails, force, true);
    }

    public String[] appendMessages(String destFolder, MailMessage[] mails, boolean force, boolean isImport) throws OXException {
        if ((mails == null) || (mails.length == 0)) {
            return new String[0];
        }
        if (!force) {
            /*
             * Check for valid from address
             */
            try {
                Set<InternetAddress> validAddrs = new HashSet<InternetAddress>(4);
                if (usm.getSendAddr() != null && usm.getSendAddr().length() > 0) {
                    validAddrs.add(new QuotedInternetAddress(usm.getSendAddr()));
                }
                User user = getUser();
                validAddrs.add(new QuotedInternetAddress(user.getMail()));
                for (String alias : user.getAliases()) {
                    validAddrs.add(new QuotedInternetAddress(alias));
                }
                boolean supportMsisdnAddresses = MailProperties.getInstance().isSupportMsisdnAddresses();
                if (supportMsisdnAddresses) {
                    MsisdnUtility.addMsisdnAddress(validAddrs, this.session);
                }
                for (MailMessage mail : mails) {
                    InternetAddress[] from = mail.getFrom();
                    List<InternetAddress> froms = Arrays.asList(from);
                    if (supportMsisdnAddresses) {
                        for (InternetAddress internetAddress : froms) {
                            String address = internetAddress.getAddress();
                            int pos = address.indexOf('/');
                            if (pos > 0) {
                                internetAddress.setAddress(address.substring(0, pos));
                            }
                        }
                    }
                    if (!validAddrs.containsAll(froms)) {
                        throw MailExceptionCode.INVALID_SENDER.create(froms.size() == 1 ? froms.get(0).toString() : Arrays.toString(from));
                    }
                }
            } catch (AddressException e) {
                throw MimeMailException.handleMessagingException(e);
            }
        }
        FullnameArgument argument = prepareMailFolderParam(destFolder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        if (mailAccess.getFolderStorage().getDraftsFolder().equals(fullName)) {
            /*
             * Append to Drafts folder
             */
            for (MailMessage mail : mails) {
                mail.setFlag(MailMessage.FLAG_DRAFT, true);
            }
        }

        if (!isImport) {
            return mailAccess.getMessageStorage().appendMessages(fullName, mails);
        }
        IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
        MailMessage[] tmp = new MailMessage[1];
        List<String> idList = new LinkedList<String>();
        for (MailMessage mail : mails) {
            MailImportResult mir = new MailImportResult();
            mir.setMail(mail);
            try {
                tmp[0] = mail;
                String[] idStr = messageStorage.appendMessages(fullName, tmp);
                mir.setId(idStr[0]);
                idList.add(idStr[0]);
            } catch (OXException e) {
                mir.setException(e);
            }
            mailImportResults.add(mir);
        }

        String[] ids = new String[idList.size()];
        for (int i = 0; i < idList.size(); i++) {
            ids[i] = idList.get(i);
        }

        return ids;
    }

    @Override
    public int getNewMessageCount(String folder) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        return mailAccess.getFolderStorage().getFolder(fullName).getNewMessageCount();
    }

    @Override
    public SearchIterator<MailMessage> getNewMessages(String folder, int sortCol, int order, int[] fields, int limit) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        return SearchIteratorAdapter.createArrayIterator(mailAccess.getMessageStorage().getUnreadMessages(
            fullName,
            MailSortField.getField(sortCol),
            OrderDirection.getOrderDirection(order),
            MailField.toFields(MailListField.getFields(fields)),
            limit));
    }

    @Override
    public SearchIterator<MailFolder> getPathToDefaultFolder(String folder) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        return SearchIteratorAdapter.createArrayIterator(mailAccess.getFolderStorage().getPath2DefaultFolder(fullName));
    }

    @Override
    public long[][] getQuotas(int[] types) throws OXException {
        initConnection(MailAccount.DEFAULT_ID);
        com.openexchange.mail.Quota.Type[] qtypes = new com.openexchange.mail.Quota.Type[types.length];
        for (int i = 0; i < qtypes.length; i++) {
            qtypes[i] = getType(types[i]);
        }
        com.openexchange.mail.Quota[] quotas = mailAccess.getFolderStorage().getQuotas(INBOX_ID, qtypes);
        long[][] retval = new long[quotas.length][];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = quotas[i].toLongArray();
        }
        return retval;
    }

    @Override
    public long getQuotaLimit(int type) throws OXException {
        initConnection(MailAccount.DEFAULT_ID);
        if (QUOTA_RESOURCE_STORAGE == type) {
            return mailAccess.getFolderStorage().getStorageQuota(INBOX_ID).getLimit();
        } else if (QUOTA_RESOURCE_MESSAGE == type) {
            return mailAccess.getFolderStorage().getMessageQuota(INBOX_ID).getLimit();
        }
        throw new IllegalArgumentException("Unknown quota resource type: " + type);
    }

    @Override
    public long getQuotaUsage(int type) throws OXException {
        initConnection(MailAccount.DEFAULT_ID);
        if (QUOTA_RESOURCE_STORAGE == type) {
            return mailAccess.getFolderStorage().getStorageQuota(INBOX_ID).getUsage();
        } else if (QUOTA_RESOURCE_MESSAGE == type) {
            return mailAccess.getFolderStorage().getMessageQuota(INBOX_ID).getUsage();
        }
        throw new IllegalArgumentException("Unknown quota resource type: " + type);
    }

    private static com.openexchange.mail.Quota.Type getType(int type) {
        if (QUOTA_RESOURCE_STORAGE == type) {
            return com.openexchange.mail.Quota.Type.STORAGE;
        } else if (QUOTA_RESOURCE_MESSAGE == type) {
            return com.openexchange.mail.Quota.Type.MESSAGE;
        }
        throw new IllegalArgumentException("Unknown quota resource type: " + type);
    }

    @Override
    public MailMessage getReplyMessageForDisplay(String folder, String replyMsgUID, boolean replyToAll, UserSettingMail usm, boolean setFrom) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        MailMessage originalMail = mailAccess.getMessageStorage().getMessage(fullName, replyMsgUID, false);
        if (null == originalMail) {
            throw MailExceptionCode.MAIL_NOT_FOUND.create(replyMsgUID, fullName);
        }
        return mailAccess.getLogicTools().getReplyMessage(originalMail, replyToAll, usm, setFrom);
    }

    @Override
    public SearchIterator<MailFolder> getRootFolders() throws OXException {
        initConnection(MailAccount.DEFAULT_ID);
        return SearchIteratorAdapter.createArrayIterator(new MailFolder[] { mailAccess.getFolderStorage().getRootFolder() });
    }

    @Override
    public String getSentFolder(int accountId) throws OXException {
        if (isDefaultFoldersChecked(accountId)) {
            return prepareFullname(accountId, getDefaultMailFolder(StorageUtility.INDEX_SENT, accountId));
        }
        initConnection(accountId);
        return prepareFullname(accountId, mailAccess.getFolderStorage().getSentFolder());
    }

    @Override
    public String getSpamFolder(int accountId) throws OXException {
        if (isDefaultFoldersChecked(accountId)) {
            return prepareFullname(accountId, getDefaultMailFolder(StorageUtility.INDEX_SPAM, accountId));
        }
        initConnection(accountId);
        return prepareFullname(accountId, mailAccess.getFolderStorage().getSpamFolder());
    }

    @Override
    public SearchIterator<MailMessage> getThreadedMessages(String folder, int[] fromToIndices, int sortCol, int order, int[] searchCols, String[] searchPatterns, boolean linkSearchTermsWithOR, int[] fields) throws OXException {
        checkPatternLength(searchPatterns);
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        SearchTerm<?> searchTerm = (searchCols == null) || (searchCols.length == 0) ? null : SearchUtility.parseFields(
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
        MailField[] useFields;
        boolean onlyFolderAndID;
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
            String[] mailIds = new String[mails.length];
            for (int i = 0; i < mailIds.length; i++) {
                mailIds[i] = mails[i].getMailId();
            }
            /*
             * Fetch identified messages by their IDs and pre-fill them according to specified fields
             */
            MailMessage[] fetchedMails = mailAccess.getMessageStorage().getMessages(fullName, mailIds, useFields);
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
        for (MailMessage mail : mails) {
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
        } catch (OXException e) {
            LOG.error("", e);
        }
        return SearchIteratorAdapter.createArrayIterator(mails);
    }

    private void checkPatternLength(String[] patterns) throws OXException {
        int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        if (0 == minimumSearchCharacters || null == patterns) {
            return;
        }
        for (String pattern : patterns) {
            if (null != pattern && SearchStrings.lengthWithoutWildcards(pattern) < minimumSearchCharacters) {
                throw MailExceptionCode.PATTERN_TOO_SHORT.create(I(minimumSearchCharacters));
            }
        }
    }

    @Override
    public String getTrashFolder(int accountId) throws OXException {
        if (isDefaultFoldersChecked(accountId)) {
            return prepareFullname(accountId, getDefaultMailFolder(StorageUtility.INDEX_TRASH, accountId));
        }
        initConnection(accountId);
        return prepareFullname(accountId, mailAccess.getFolderStorage().getTrashFolder());
    }

    @Override
    public int getUnreadMessageCount(String folder) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        String fullName = argument.getFullname();

        int retval;

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

    @Override
    public void openFor(String folder) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
    }

    private void initConnection(int accountId) throws OXException {
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

    private MailAccess<?, ?> initMailAccess(int accountId) throws OXException {
        /*
         * Fetch a mail access (either from cache or a new instance)
         */
        MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, accountId);
        if (!mailAccess.isConnected()) {
            /*
             * Get new mail configuration
             */
            long start = System.currentTimeMillis();
            try {
                mailAccess.connect();
                warnings.addAll(mailAccess.getWarnings());
                MailServletInterface.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                MailServletInterface.mailInterfaceMonitor.changeNumSuccessfulLogins(true);
            } catch (OXException e) {
                if (MimeMailExceptionCode.LOGIN_FAILED.equals(e) || MimeMailExceptionCode.INVALID_CREDENTIALS.equals(e)) {
                    MailServletInterface.mailInterfaceMonitor.changeNumFailedLogins(true);
                }
                throw e;
            }
        }
        return mailAccess;
    }

    private boolean isDefaultFoldersChecked(int accountId) {
        Boolean b = MailSessionCache.getInstance(session).getParameter(
            accountId,
            MailSessionParameterNames.getParamDefaultFolderChecked());
        return (b != null) && b.booleanValue();
    }

    @Override
    public MailPath saveDraft(ComposedMailMessage draftMail, boolean autosave, int accountId) throws OXException {
        if (autosave) {
            return autosaveDraft(draftMail, accountId);
        }
        initConnection(accountId);
        String draftFullname = mailAccess.getFolderStorage().getDraftsFolder();
        MailMessage draftMessage = mailAccess.getMessageStorage().saveDraft(draftFullname, draftMail);
        if (null == draftMessage) {
            return null;
        }
        MailPath mailPath = draftMessage.getMailPath();
        if (null == mailPath) {
            return null;
        }
        postEvent(accountId, draftFullname, true);
        return mailPath;
    }

    private MailPath autosaveDraft(ComposedMailMessage draftMail, int accountId) throws OXException {
        initConnection(accountId);
        String draftFullname = mailAccess.getFolderStorage().getDraftsFolder();
        /*
         * Auto-save draft
         */
        if (!draftMail.isDraft()) {
            draftMail.setFlag(MailMessage.FLAG_DRAFT, true);
        }
        MailPath msgref = draftMail.getMsgref();
        MailAccess<?, ?> otherAccess = null;
        try {
            MailMessage origMail;
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
                    NonInlineForwardPartHandler handler = new NonInlineForwardPartHandler();
                    new MailMessageParser().parseMailMessage(origMail, handler);
                    List<MailPart> parts = handler.getNonInlineParts();
                    if (!parts.isEmpty()) {
                        TransportProvider tp = TransportProviderRegistry.getTransportProviderBySession(session, accountId);
                        for (MailPart mailPart : parts) {
                            /*
                             * Create and add a referenced part from original draft mail
                             */
                            draftMail.addEnclosedPart(tp.getNewReferencedPart(mailPart, session));
                        }
                    }
                }
            }
            String uid;
            {
                MailMessage filledMail = MimeMessageConverter.fillComposedMailMessage(draftMail);
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
            MailMessage m = mailAccess.getMessageStorage().getMessage(draftFullname, uid, true);
            if (null == m) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(Long.valueOf(uid), draftFullname);
            }
            postEvent(accountId, draftFullname, true);
            return m.getMailPath();
        } finally {
            if (null != otherAccess) {
                otherAccess.close(true);
            }
        }
    }

    @Override
    public String saveFolder(MailFolderDescription mailFolder) throws OXException {
        if (!mailFolder.containsExists() && !mailFolder.containsFullname()) {
            throw MailExceptionCode.INSUFFICIENT_FOLDER_ATTR.create();
        }
        {
            String name = mailFolder.getName();
            if (null != name) {
                checkFolderName(name);
            }
        }
        if ((mailFolder.containsExists() && mailFolder.exists()) || ((mailFolder.getFullname() != null) && mailAccess.getFolderStorage().exists(
            mailFolder.getFullname()))) {
            /*
             * Update
             */
            int accountId = mailFolder.getAccountId();
            String fullName = mailFolder.getFullname();
            initConnection(accountId);
            char separator = mailFolder.getSeparator();
            String oldParent;
            String oldName;
            {
                int pos = fullName.lastIndexOf(separator);
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
                int parentAccountID = mailFolder.getParentAccountId();
                if (accountId == parentAccountID) {
                    String newParent = mailFolder.getParentFullname();
                    StringBuilder newFullname = new StringBuilder(newParent).append(mailFolder.getSeparator());
                    if (mailFolder.containsName()) {
                        newFullname.append(mailFolder.getName());
                    } else {
                        newFullname.append(oldName);
                    }
                    if (!newParent.equals(oldParent)) { // move & rename
                        Map<String, Map<?, ?>> subfolders = subfolders(fullName);
                        fullName = mailAccess.getFolderStorage().moveFolder(fullName, newFullname.toString());
                        movePerformed = true;
                        postEvent4Subfolders(accountId, subfolders);
                        postEventRemote(accountId, newParent, false, true);
                    }
                } else {
                    // Move to another account
                    MailAccess<?, ?> otherAccess = initMailAccess(parentAccountID);
                    try {
                        String newParent = mailFolder.getParentFullname();
                        // Check if parent mail folder exists
                        MailFolder p = otherAccess.getFolderStorage().getFolder(newParent);
                        // Check permission on new parent
                        MailPermission ownPermission = p.getOwnPermission();
                        if (!ownPermission.canCreateSubfolders()) {
                            throw MailExceptionCode.NO_CREATE_ACCESS.create(newParent);
                        }
                        // Check for duplicate
                        MailFolder[] tmp = otherAccess.getFolderStorage().getSubfolders(newParent, true);
                        String lookFor = mailFolder.containsName() ? mailFolder.getName() : oldName;
                        for (MailFolder sub : tmp) {
                            if (sub.getName().equals(lookFor)) {
                                throw MailExceptionCode.DUPLICATE_FOLDER.create(lookFor);
                            }
                        }
                        // Copy
                        String destFullname = fullCopy(
                            mailAccess,
                            fullName,
                            otherAccess,
                            newParent,
                            p.getSeparator(),
                            session.getUserId(),
                            otherAccess.getMailConfig().getCapabilities().hasPermissions());
                        postEventRemote(parentAccountID, newParent, false, true);
                        // Delete source
                        Map<String, Map<?, ?>> subfolders = subfolders(fullName);
                        mailAccess.getFolderStorage().deleteFolder(fullName, true);
                        // Perform other updates
                        String prepareFullname = prepareFullname(
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
                String newName = mailFolder.getName();
                if (!newName.equals(oldName)) { // rename
                    fullName = mailAccess.getFolderStorage().renameFolder(fullName, newName);
                    postEventRemote(accountId, fullName, false, true);
                }
            }
            /*
             * Handle update of permission or subscription
             */
            String prepareFullname = prepareFullname(accountId, mailAccess.getFolderStorage().updateFolder(fullName, mailFolder));
            postEventRemote(accountId, fullName, false, true);
            return prepareFullname;
        }
        /*
         * Insert
         */
        int accountId = mailFolder.getParentAccountId();
        initConnection(accountId);
        String prepareFullname = prepareFullname(accountId, mailAccess.getFolderStorage().createFolder(mailFolder));
        postEventRemote(accountId, mailFolder.getParentFullname(), false, true);
        return prepareFullname;
    }

    private static String fullCopy(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> srcAccess, String srcFullname, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> destAccess, String destParent, char destSeparator, int user, boolean hasPermissions) throws OXException {
        // Create folder
        MailFolder source = srcAccess.getFolderStorage().getFolder(srcFullname);
        MailFolderDescription mfd = new MailFolderDescription();
        mfd.setName(source.getName());
        mfd.setParentFullname(destParent);
        mfd.setSeparator(destSeparator);
        mfd.setSubscribed(source.isSubscribed());
        if (hasPermissions) {
            // Copy permissions
            MailPermission[] perms = source.getPermissions();
            try {
                for (MailPermission perm : perms) {
                    mfd.addPermission((MailPermission) perm.clone());
                }
            } catch (CloneNotSupportedException e) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        String destFullname = destAccess.getFolderStorage().createFolder(mfd);
        // Copy messages
        MailMessage[] msgs = srcAccess.getMessageStorage().getAllMessages(
            srcFullname,
            null,
            MailSortField.RECEIVED_DATE,
            OrderDirection.ASC,
            FIELDS_FULL);
        IMailMessageStorage destMessageStorage = destAccess.getMessageStorage();
        // Append messages to destination account
        /* final String[] mailIds = */destMessageStorage.appendMessages(destFullname, msgs);
        /*-
         *
        // Ensure flags
        String[] arr = new String[1];
        for (int i = 0; i < msgs.length; i++) {
            MailMessage m = msgs[i];
            String mailId = mailIds[i];
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
        MailFolder[] tmp = srcAccess.getFolderStorage().getSubfolders(srcFullname, true);
        for (MailFolder element : tmp) {
            fullCopy(srcAccess, element.getFullname(), destAccess, destFullname, destSeparator, user, hasPermissions);
        }
        return destFullname;
    }

    private final static String INVALID = "<>"; // "()<>@,;:\\\".[]";

    private static void checkFolderName(String name) throws OXException {
        if (com.openexchange.java.Strings.isEmpty(name)) {
            throw MailExceptionCode.INVALID_FOLDER_NAME_EMPTY.create();
        }
        int length = name.length();
        for (int i = 0; i < length; i++) {
            if (INVALID.indexOf(name.charAt(i)) >= 0) {
                throw MailExceptionCode.INVALID_FOLDER_NAME2.create(name, INVALID);
            }
        }
    }

    @Override
    public void sendFormMail(ComposedMailMessage composedMail, int groupId, int accountId) throws OXException {
        /*
         * Initialize
         */
        initConnection(accountId);
        MailTransport transport = MailTransport.getInstance(session, accountId);
        try {
            /*
             * Resolve group to users
             */
            GroupStorage gs = GroupStorage.getInstance();
            Group group = gs.getGroup(groupId, ctx);
            int[] members = group.getMember();
            /*
             * Get user storage/contact interface to load user and its contact
             */
            UserStorage us = UserStorage.getInstance();
            ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
            /*
             * Needed variables
             */
            String content = (String) composedMail.getContent();
            StringBuilder builder = new StringBuilder(content.length() + 64);
            TransportProvider provider = TransportProviderRegistry.getTransportProviderBySession(session, accountId);
            Map<Locale, String> greetings = new HashMap<Locale, String>(4);
            for (int userId : members) {
                User user = us.getUser(userId, ctx);
                /*
                 * Get user's contact
                 */
                Contact contact = contactService.getUser(session, userId, new ContactField[] {
                    ContactField.SUR_NAME, ContactField.GIVEN_NAME });
                /*
                 * Determine locale
                 */
                Locale locale = user.getLocale();
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
                TextBodyMailPart part = provider.getNewTextBodyPart(builder.toString());
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
                MailProperties properties = MailProperties.getInstance();
                if ((properties.getRateLimitPrimaryOnly() && MailAccount.DEFAULT_ID == accountId) || !properties.getRateLimitPrimaryOnly()) {
                    int rateLimit = properties.getRateLimit();
                    rateLimitChecks(composedMail, rateLimit, properties.getMaxToCcBcc());
                    transport.sendMailMessage(composedMail, ComposeType.NEW);
                    setRateLimitTime(rateLimit);
                } else {
                    transport.sendMailMessage(composedMail, ComposeType.NEW);
                }
            }
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        } finally {
            transport.close();
        }
    }

    @Override
    public String sendMessage(ComposedMailMessage composedMail, ComposeType type, int accountId) throws OXException {
        return sendMessage(composedMail, type, accountId, UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx));
    }

    @Override
    public String sendMessage(ComposedMailMessage composedMail, ComposeType type, int accountId, UserSettingMail optUserSetting) throws OXException {
        return sendMessage(composedMail, type, accountId, optUserSetting, null);
    }

    @Override
    public String sendMessage(ComposedMailMessage composedMail, ComposeType type, int accountId, UserSettingMail optUserSetting, MtaStatusInfo statusInfo) throws OXException {
        /*
         * Initialize
         */
        initConnection(accountId);
        MailTransport transport = MailTransport.getInstance(session, accountId);
        boolean mailSent = false;
        try {
            /*
             * Send mail
             */
            long startTransport;
            MailMessage sentMail;
            startTransport = System.currentTimeMillis();
            MailProperties properties = MailProperties.getInstance();
            if (!properties.getRateLimitPrimaryOnly() || MailAccount.DEFAULT_ID == accountId) {
                int rateLimit = properties.getRateLimit();
                rateLimitChecks(composedMail, rateLimit, properties.getMaxToCcBcc());
                sentMail = transport.sendMailMessage(composedMail, type, null, statusInfo);
                setRateLimitTime(rateLimit);
            } else {
                sentMail = transport.sendMailMessage(composedMail, type, null, statusInfo);
            }
            mailSent = true;
            /*
             * Email successfully sent, trigger data retention
             */
            DataRetentionService retentionService = ServerServiceRegistry.getInstance().getService(DataRetentionService.class);
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
                    MailPath supPath = composedMail.getMsgref();
                    if (null == supPath) {
                        int count = composedMail.getEnclosedCount();
                        List<MailPath> paths = new LinkedList<MailPath>();
                        for (int i = 0; i < count; i++) {
                            MailPart part = composedMail.getEnclosedMailPart(i);
                            MailPath path = part.getMsgref();
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
                } else if (ComposeType.DRAFT_NO_DELETE_ON_TRANSPORT.equals(type)) {
                    // Do not delete draft!
                } else if (ComposeType.DRAFT.equals(type)) {
                    ConfigViewFactory configViewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
                    if (null != configViewFactory) {
                        try {
                            ConfigView view = configViewFactory.getView(session.getUserId(), session.getContextId());
                            ComposedConfigProperty<Boolean> property = view.property(
                                "com.openexchange.mail.deleteDraftOnTransport",
                                boolean.class);
                            if (property.isDefined() && property.get().booleanValue()) {
                                deleteDraft(composedMail.getMsgref());
                            }
                        } catch (Exception e) {
                            LOG.warn("Draft mail cannot be deleted.", e);
                        }
                    }
                } else if (ComposeType.DRAFT_DELETE_ON_TRANSPORT.equals(type)) {
                    try {
                        deleteDraft(composedMail.getMsgref());
                    } catch (Exception e) {
                        LOG.warn("Draft mail cannot be deleted.", e);
                    }
                }
            } catch (OXException e) {
                mailAccess.addWarnings(Collections.singletonList(MailExceptionCode.FLAG_FAIL.create(e, new Object[0])));
            }
            UserSettingMail usm = null == optUserSetting ? UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx) : optUserSetting;
            if (usm.isNoCopyIntoStandardSentFolder()) {
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
        } catch (OXException e) {
            if (!mailSent) {
                throw e;
            }
            e.setCategory(Category.CATEGORY_WARNING);
            warnings.add(e);
            return null;
        } catch (RuntimeException e) {
            OXException oxe = MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
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
        final Session session = this.session;
        final Logger logger = LOG;
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    RetentionData retentionData = retentionService.newInstance();
                    retentionData.setStartTime(new Date(startTransport));
                    retentionData.setIdentifier(transport.getTransportConfig().getLogin());
                    retentionData.setIPAddress(session.getLocalIp());
                    retentionData.setSenderAddress(IDNA.toIDN(sentMail.getFrom()[0].getAddress()));
                    Set<InternetAddress> recipients = new HashSet<InternetAddress>(Arrays.asList(sentMail.getTo()));
                    recipients.addAll(Arrays.asList(sentMail.getCc()));
                    recipients.addAll(Arrays.asList(sentMail.getBcc()));
                    int size = recipients.size();
                    String[] recipientsArr = new String[size];
                    Iterator<InternetAddress> it = recipients.iterator();
                    for (int i = 0; i < size; i++) {
                        recipientsArr[i] = IDNA.toIDN(it.next().getAddress());
                    }
                    retentionData.setRecipientAddresses(recipientsArr);
                    /*
                     * Finally store it
                     */
                    retentionService.storeOnTransport(retentionData);
                } catch (OXException e) {
                    logger.error("", e);
                }
            }
        };
        /*
         * Check if timer service is available to delegate execution
         */
        ThreadPoolService threadPool = ThreadPools.getThreadPool();
        if (null == threadPool) {
            // Execute in this thread
            r.run();
        } else {
            // Delegate runnable to thread pool
            threadPool.submit(ThreadPools.task(r), CallerRunsBehavior.getInstance());
        }
    }

    private MailPath append2SentFolder(MailMessage sentMail) throws OXException {
        /*
         * Append to Sent folder
         */
        long start = System.currentTimeMillis();
        String sentFullname = mailAccess.getFolderStorage().getSentFolder();
        String[] uidArr;
        try {
            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            if (messageStorage instanceof IMailMessageStorageMimeSupport) {
                IMailMessageStorageMimeSupport mimeSupport = (IMailMessageStorageMimeSupport) messageStorage;
                if (mimeSupport.isMimeSupported() && (sentMail instanceof MimeRawSource)) {
                    uidArr = mimeSupport.appendMimeMessages(sentFullname, new Message[] { (Message) ((MimeRawSource) sentMail).getPart() });
                } else {
                    uidArr = messageStorage.appendMessages(sentFullname, new MailMessage[] { sentMail });
                }
            } else {
                uidArr = messageStorage.appendMessages(sentFullname, new MailMessage[] { sentMail });
            }
            try {
                /*
                 * Update caches
                 */
                MailMessageCache.getInstance().removeFolderMessages(mailAccess.getAccountId(), sentFullname, session.getUserId(), contextId);
            } catch (OXException e) {
                LOG.error("", e);
            }
        } catch (OXException e) {
            if (e.getMessage().indexOf("quota") != -1) {
                throw MailExceptionCode.COPY_TO_SENT_FOLDER_FAILED_QUOTA.create(e, new Object[0]);
            }
            LOG.warn("Mail with id {} in folder {} sent successfully, but a copy could not be placed in the sent folder.", sentMail.getMailId(), sentMail.getFolder(), e);
            throw MailExceptionCode.COPY_TO_SENT_FOLDER_FAILED.create(e, new Object[0]);
        }
        if ((uidArr != null) && (uidArr[0] != null)) {
            /*
             * Mark appended sent mail as seen
             */
            mailAccess.getMessageStorage().updateMessageFlags(sentFullname, uidArr, MailMessage.FLAG_SEEN, true);
        }
        MailPath retval = new MailPath(mailAccess.getAccountId(), sentFullname, uidArr[0]);
        LOG.debug("Mail copy ({}) appended in {}msec", retval, System.currentTimeMillis() - start);
        return retval;
    }

    private void setFlagForward(MailPath path) throws OXException {
        /*
         * Mark referenced mail as forwarded
         */
        String fullName = path.getFolder();
        String[] uids = new String[] { path.getMailID() };
        int pathAccount = path.getAccountId();
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
            } catch (OXException e) {
                LOG.error("", e);
            }
        } else {
            MailAccess<?, ?> otherAccess = MailAccess.getInstance(session, pathAccount);
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
                } catch (OXException e) {
                    LOG.error("", e);
                }
            } finally {
                otherAccess.close(false);
            }
        }
    }

    private void setFlagMultipleForward(List<MailPath> paths) throws OXException {
        String[] ids = new String[1];
        for (MailPath path : paths) {
            /*
             * Mark referenced mail as forwarded
             */
            ids[0] = path.getMailID();
            int pathAccount = path.getAccountId();
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
                } catch (OXException e) {
                    LOG.error("", e);
                }
            } else {
                MailAccess<?, ?> otherAccess = MailAccess.getInstance(session, pathAccount);
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
                    } catch (OXException e) {
                        LOG.error("", e);
                    }
                } finally {
                    otherAccess.close(false);
                }
            }
        }
    }

    private void deleteDraft(MailPath path) throws OXException {
        if (null == path) {
            LOG.warn("Missing msgref on draft-delete. Corresponding draft mail cannot be deleted.", new Throwable());
            return;
        }
        /*
         * Delete draft mail
         */
        String fullName = path.getFolder();
        String[] uids = new String[] { path.getMailID() };
        int pathAccount = path.getAccountId();
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
                } catch (OXException e) {
                    // Ignore
                }
            } finally {
                if (null != otherAccess) {
                    otherAccess.close(true);
                }
            }
        }
    }

    private void setFlagReply(MailPath path) throws OXException {
        if (null == path) {
            LOG.warn("Missing msgref on reply. Corresponding mail cannot be marked as answered.", new Throwable());
            return;
        }
        /*
         * Mark referenced mail as answered
         */
        String fullName = path.getFolder();
        String[] uids = new String[] { path.getMailID() };
        int pathAccount = path.getAccountId();
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
            } catch (OXException e) {
                LOG.error("", e);
            }
        } else {
            /*
             * Mark as \Answered in foreign account
             */
            MailAccess<?, ?> otherAccess = MailAccess.getInstance(session, pathAccount);
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
                } catch (OXException e) {
                    LOG.error("", e);
                }
            } finally {
                otherAccess.close(false);
            }
        }
    }

    private <V> V performSynchronized(Callable<V> task, Session session) throws Exception {
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

    private void setRateLimitTime(int rateLimit) {
        if (rateLimit > 0) {
            session.setParameter(LAST_SEND_TIME, Long.valueOf(System.currentTimeMillis()));
        }
    }

    private void rateLimitChecks(MailMessage composedMail, int rateLimit, int maxToCcBcc) throws OXException {
        if (rateLimit > 0) {
            Long parameter = (Long) session.getParameter(LAST_SEND_TIME);
            if (null != parameter && (parameter.longValue() + rateLimit) >= System.currentTimeMillis()) {
                NumberFormat numberInstance = NumberFormat.getNumberInstance(getUserLocale());
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
    public void sendReceiptAck(String folder, String msgUID, String fromAddr) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int acc = argument.getAccountId();
        try {
            MailAccountStorageService ss = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            MailAccount ma = ss.getMailAccount(acc, session.getUserId(), session.getContextId());
            if (ma.isDefaultAccount()) {
                /*
                 * Check for valid from address
                 */
                try {
                    Set<InternetAddress> validAddrs = new HashSet<InternetAddress>(4);
                    if (usm.getSendAddr() != null && usm.getSendAddr().length() > 0) {
                        validAddrs.add(new QuotedInternetAddress(usm.getSendAddr()));
                    }
                    User user = getUser();
                    validAddrs.add(new QuotedInternetAddress(user.getMail()));
                    for (String alias : user.getAliases()) {
                        validAddrs.add(new QuotedInternetAddress(alias));
                    }
                    QuotedInternetAddress fromAddress = new QuotedInternetAddress(fromAddr);
                    if (MailProperties.getInstance().isSupportMsisdnAddresses()) {
                        MsisdnUtility.addMsisdnAddress(validAddrs, session);
                        String address = fromAddress.getAddress();
                        int pos = address.indexOf('/');
                        if (pos > 0) {
                            fromAddress.setAddress(address.substring(0, pos));
                        }
                    }
                    if (!validAddrs.contains(fromAddress)) {
                        throw MailExceptionCode.INVALID_SENDER.create(fromAddr);
                    }
                } catch (AddressException e) {
                    throw MimeMailException.handleMessagingException(e);
                }
            } else {
                if (!new QuotedInternetAddress(ma.getPrimaryAddress()).equals(new QuotedInternetAddress(fromAddr))) {
                    throw MailExceptionCode.INVALID_SENDER.create(fromAddr);
                }
            }
        } catch (AddressException e) {
            throw MimeMailException.handleMessagingException(e);
        }
        /*
         * Initialize
         */
        initConnection(acc);
        String fullName = argument.getFullname();
        MailTransport transport = MailTransport.getInstance(session);
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
        Map<String, Object> m = new HashMap<String, Object>(1, 1f);
        m.put("operation", "updateMessageColorLabel");
        MORE_PROPS_UPDATE_LABEL = Collections.unmodifiableMap(m);
    }

    @Override
    public void updateMessageColorLabel(String folder, String[] mailIDs, int newColorLabel) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
        String[] ids;
        if (null == mailIDs) {
            if (messageStorage instanceof IMailMessageStorageBatch) {
                IMailMessageStorageBatch batch = (IMailMessageStorageBatch) messageStorage;
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
        } catch (OXException e) {
            LOG.error("", e);
        }
    }

    @Override
    public String getMailIDByMessageID(String folder, String messageID) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        MailMessage[] messages = mailAccess.getMessageStorage().searchMessages(
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
        Map<String, Object> m = new HashMap<String, Object>(1, 1f);
        m.put("operation", "updateMessageFlags");
        MORE_PROPS_UPDATE_FLAGS = Collections.unmodifiableMap(m);
    }

    @Override
    public void updateMessageFlags(String folder, String[] mailIDs, int flagBits, boolean flagVal) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
        String[] ids;
        if (null == mailIDs) {
            if (messageStorage instanceof IMailMessageStorageBatch) {
                IMailMessageStorageBatch batch = (IMailMessageStorageBatch) messageStorage;
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
        boolean spamAction = (usm.isSpamEnabled() && ((flagBits & MailMessage.FLAG_SPAM) > 0));
        if (spamAction) {
            String spamFullname = mailAccess.getFolderStorage().getSpamFolder();
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
            } catch (OXException e) {
                LOG.error("", e);
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
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
    }

    @Override
    public MailMessage[] getUpdatedMessages(String folder, int[] fields) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        return mailAccess.getMessageStorage().getNewAndModifiedMessages(fullName, MailField.getFields(fields));
    }

    @Override
    public MailMessage[] getDeletedMessages(String folder, int[] fields) throws OXException {
        FullnameArgument argument = prepareMailFolderParam(folder);
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
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

        public MailFolderComparator(String[] names, Locale locale) {
            super();
            indexMap = new HashMap<String, Integer>(names.length);
            for (int i = 0; i < names.length; i++) {
                indexMap.put(names[i], Integer.valueOf(i));
            }
            na = Integer.valueOf(names.length);
            collator = Collators.getSecondaryInstance(locale);
        }

        private Integer getNumberOf(String name) {
            Integer ret = indexMap.get(name);
            if (null == ret) {
                return na;
            }
            return ret;
        }

        @Override
        public int compare(MailFolder o1, MailFolder o2) {
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

        public SimpleMailFolderComparator(Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale);
        }

        @Override
        public int compare(MailFolder o1, MailFolder o2) {
            return collator.compare(o1.getName(), o2.getName());
        }
    }

    private static String[] messages2ids(MailMessage[] messages) {
        if (null == messages) {
            return null;
        }
        String[] retval = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            retval[i] = messages[i].getMailId();
        }
        return retval;
    }

    private void postEvent(int accountId, String fullName, boolean contentRelated) {
        postEvent(accountId, fullName, contentRelated, false);
    }

    private void postEventRemote(int accountId, String fullName, boolean contentRelated) {
        postEventRemote(accountId, fullName, contentRelated, false);
    }

    // ---------------------------------------------------------------------------------------------------------------------------------- //

    private void postEvent(int accountId, String fullName, boolean contentRelated, boolean immediateDelivery) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        EventPool.getInstance().put(new PooledEvent(contextId, session.getUserId(), accountId, prepareFullname(accountId, fullName), contentRelated, immediateDelivery, false, session));
    }

    private void postEventRemote(int accountId, String fullName, boolean contentRelated, boolean immediateDelivery) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        EventPool.getInstance().put(new PooledEvent(contextId, session.getUserId(), accountId, prepareFullname(accountId, fullName), contentRelated, immediateDelivery, true, session));
    }

    // ---------------------------------------------------------------------------------------------------------------------------------- //

    private void postEvent(int accountId, String fullName, boolean contentRelated, boolean immediateDelivery, boolean async) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        EventPool.getInstance().put(
            new PooledEvent(contextId, session.getUserId(), accountId, prepareFullname(accountId, fullName), contentRelated, immediateDelivery, false, session).setAsync(async));
    }

    private void postEventRemote(int accountId, String fullName, boolean contentRelated, boolean immediateDelivery, boolean async) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        EventPool.getInstance().put(
            new PooledEvent(contextId, session.getUserId(), accountId, prepareFullname(accountId, fullName), contentRelated, immediateDelivery, true, session).setAsync(async));
    }

    // ---------------------------------------------------------------------------------------------------------------------------------- //

    private void postEvent(String topic, int accountId, String fullName, boolean contentRelated, boolean immediateDelivery) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        EventPool.getInstance().put(new PooledEvent(topic, contextId, session.getUserId(), accountId, prepareFullname(accountId, fullName), contentRelated, immediateDelivery, false, session));
    }

    private void postEventRemote(String topic, int accountId, String fullName, boolean contentRelated, boolean immediateDelivery) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        EventPool.getInstance().put(new PooledEvent(topic, contextId, session.getUserId(), accountId, prepareFullname(accountId, fullName), contentRelated, immediateDelivery, true, session));
    }

    // ---------------------------------------------------------------------------------------------------------------------------------- //

    private void postEvent(String topic, int accountId, String fullName, boolean contentRelated, boolean immediateDelivery, boolean async) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        PooledEvent pooledEvent = new PooledEvent(topic, contextId, session.getUserId(), accountId, prepareFullname(accountId, fullName), contentRelated, immediateDelivery, false, session);
        EventPool.getInstance().put(pooledEvent.setAsync(async));
    }

    private void postEventRemote(String topic, int accountId, String fullName, boolean contentRelated, boolean immediateDelivery, boolean async) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        PooledEvent pooledEvent = new PooledEvent(topic, contextId, session.getUserId(), accountId, prepareFullname(accountId, fullName), contentRelated, immediateDelivery, true, session);
        EventPool.getInstance().put(pooledEvent.setAsync(async));
    }

    // ---------------------------------------------------------------------------------------------------------------------------------- //

    private void postEvent(String topic, int accountId, String fullName, boolean contentRelated, boolean immediateDelivery, boolean async, Map<String, Object> moreProperties) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        PooledEvent pooledEvent = new PooledEvent(topic, contextId, session.getUserId(), accountId, prepareFullname(accountId, fullName), contentRelated, immediateDelivery, false, session);
        if (null != moreProperties) {
            for (Entry<String, Object> entry : moreProperties.entrySet()) {
                pooledEvent.putProperty(entry.getKey(), entry.getValue());
            }
        }
        EventPool.getInstance().put(pooledEvent.setAsync(async));
    }

    private void postEventRemote(String topic, int accountId, String fullName, boolean contentRelated, boolean immediateDelivery, boolean async, Map<String, Object> moreProperties) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        PooledEvent pooledEvent = new PooledEvent(topic, contextId, session.getUserId(), accountId, prepareFullname(accountId, fullName), contentRelated, immediateDelivery, true, session);
        if (null != moreProperties) {
            for (Entry<String, Object> entry : moreProperties.entrySet()) {
                pooledEvent.putProperty(entry.getKey(), entry.getValue());
            }
        }
        EventPool.getInstance().put(pooledEvent.setAsync(async));
    }

    @Override
    public MailImportResult[] getMailImportResults() {
        MailImportResult[] mars = new MailImportResult[mailImportResults.size()];
        for (int i = 0; i < mars.length; i++) {
            mars[i] = mailImportResults.get(i);
        }

        return mars;
    }

    private String[] getAllMessageIDs(FullnameArgument argument) throws OXException {
        int accountId = argument.getAccountId();
        initConnection(accountId);
        String fullName = argument.getFullname();
        MailMessage[] mails = mailAccess.getMessageStorage().searchMessages(
            fullName,
            null,
            MailSortField.RECEIVED_DATE,
            OrderDirection.ASC,
            null,
            FIELDS_ID_INFO);
        if ((mails == null) || (mails.length == 0)) {
            return new String[0];
        }
        String[] ret = new String[mails.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = mails[i].getMailId();
        }
        return ret;
    }

}
