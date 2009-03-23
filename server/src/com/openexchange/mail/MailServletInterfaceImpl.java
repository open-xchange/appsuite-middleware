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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.cache.OXCachingException;
import com.openexchange.dataretention.DataRetentionException;
import com.openexchange.dataretention.DataRetentionService;
import com.openexchange.dataretention.RetentionData;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
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
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.NonInlineForwardPartHandler;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.SearchUtility;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.StorageUtility;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.SpamHandlerRegistry;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorDelegator;
import com.openexchange.user.UserService;

/**
 * {@link MailServletInterfaceImpl} - The mail servlet interface implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class MailServletInterfaceImpl extends MailServletInterface {

    private static final MailField[] FIELDS_ID_INFO = new MailField[] { MailField.ID, MailField.FOLDER_ID };

    private static final String INBOX_ID = "INBOX";

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailServletInterfaceImpl.class);

    /*-
     * ++++++++++++++ Fields ++++++++++++++
     */

    private final Context ctx;

    private boolean init;

    private MailConfig mailConfig;

    private MailAccess<?, ?> mailAccess;

    private final Session session;

    private final UserSettingMail usm;

    private Locale locale;

    /**
     * Initializes a new {@link MailServletInterfaceImpl}.
     * 
     * @throws MailException If user has no mail access or properties cannot be successfully loaded
     */
    MailServletInterfaceImpl(final Session session) throws MailException {
        super();
        {
            Context ctx;
            try {
                ctx = (Context) session.getParameter(MailSessionParameterNames.PARAM_CONTEXT);
            } catch (final ClassCastException e1) {
                ctx = null;
            }
            if (ctx == null) {
                try {
                    ctx = ContextStorage.getStorageContext(session.getContextId());
                } catch (final ContextException e) {
                    throw new MailException(e);
                }
            }
            this.ctx = ctx;
        }
        try {
            if (!UserConfigurationStorage.getInstance().getUserConfiguration(session.getUserId(), ctx).hasWebMail()) {
                throw new MailException(MailException.Code.NO_MAIL_ACCESS);
            }
        } catch (final UserConfigurationException e) {
            throw new MailException(e);
        }
        session.setParameter(MailSessionParameterNames.PARAM_CONTEXT, ctx);
        this.session = session;
        usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
    }

    private Locale getUserLocale() {
        if (null == locale) {
            final UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class);
            if (null == userService) {
                return Locale.ENGLISH;
            }
            try {
                locale = userService.getUser(session.getUserId(), ctx).getLocale();
            } catch (final com.openexchange.groupware.ldap.UserException e) {
                LOG.warn(e.getMessage(), e);
                return Locale.ENGLISH;
            }
        }
        return locale;
    }

    @Override
    public void checkDefaultFolders(final String[] defaultFolderNames) throws MailException {
        initConnection();
        mailAccess.getFolderStorage().checkDefaultFolders();
    }

    @Override
    public boolean clearFolder(final String folder) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        /*
         * Only backup if no hard-delete is set in user's mail configuration and fullname does not denote trash (sub)folder
         */
        final boolean backup = (!UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isHardDeleteMsgs() && !(fullname.startsWith(mailAccess.getFolderStorage().getTrashFolder())));
        mailAccess.getFolderStorage().clearFolder(fullname, !backup);
        try {
            /*
             * Update message cache
             */
            MailMessageCache.getInstance().removeFolderMessages(fullname, session.getUserId(), ctx);
        } catch (final OXCachingException e) {
            LOG.error(e.getMessage(), e);
        }
        final String trashFullname = prepareMailFolderParam(getTrashFolder());
        if (fullname.startsWith(trashFullname)) {
            // Special handling
            final MailFolder[] subf = mailAccess.getFolderStorage().getSubfolders(fullname, true);
            for (int i = 0; i < subf.length; i++) {
                mailAccess.getFolderStorage().deleteFolder(subf[i].getFullname(), true);
            }
        }
        return true;
    }

    @Override
    public void close(final boolean putIntoCache) throws MailException {
        try {
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
    public long[] copyMessages(final String sourceFolder, final String destFolder, final long[] msgUIDs, final boolean move) throws MailException {
        initConnection();
        final String sourceFullname = prepareMailFolderParam(sourceFolder);
        final String destFullname = prepareMailFolderParam(destFolder);
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
            final boolean locatedInSpamFolder = (SPAM_HAM == spamAction) || spamFullname.equals(sourceFullname);
            if (spamAction == SPAM_SPAM) {
                if (!locatedInSpamFolder) {
                    /*
                     * Handle spam
                     */
                    SpamHandlerRegistry.getSpamHandlerBySession(session).handleSpam(sourceFullname, msgUIDs, false, session);
                }
            } else {
                if (locatedInSpamFolder) {
                    /*
                     * Handle ham.
                     */
                    SpamHandlerRegistry.getSpamHandlerBySession(session).handleHam(sourceFullname, msgUIDs, false, session);
                }
            }
        }
        final long[] maildIds;
        if (move) {
            maildIds = mailAccess.getMessageStorage().moveMessages(sourceFullname, destFullname, msgUIDs, false);
        } else {
            maildIds = mailAccess.getMessageStorage().copyMessages(sourceFullname, destFullname, msgUIDs, false);
        }
        try {
            /*
             * Update message cache
             */
            MailMessageCache.getInstance().removeFolderMessages(sourceFullname, session.getUserId(), ctx);
            MailMessageCache.getInstance().removeFolderMessages(destFullname, session.getUserId(), ctx);
        } catch (final OXCachingException e) {
            LOG.error(e.getMessage(), e);
        }
        return maildIds;
    }

    @Override
    public String deleteFolder(final String folder) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        /*
         * Only backup if fullname does not denote trash (sub)folder
         */
        final String retval = prepareFullname(mailAccess.getFolderStorage().deleteFolder(
            fullname,
            (fullname.startsWith(mailAccess.getFolderStorage().getTrashFolder()))));
        try {
            /*
             * Update message cache
             */
            MailMessageCache.getInstance().removeFolderMessages(fullname, session.getUserId(), ctx);
        } catch (final OXCachingException e) {
            LOG.error(e.getMessage(), e);
        }
        return retval;
    }

    @Override
    public boolean deleteMessages(final String folder, final long[] msgUIDs, final boolean hardDelete) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        /*
         * Hard-delete if hard-delete is set in user's mail configuration or fullname denotes trash (sub)folder
         */
        final boolean hd = (hardDelete || UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isHardDeleteMsgs() || (fullname.startsWith(mailAccess.getFolderStorage().getTrashFolder())));
        mailAccess.getMessageStorage().deleteMessages(fullname, msgUIDs, hd);
        try {
            /*
             * Update message cache
             */
            MailMessageCache.getInstance().removeFolderMessages(fullname, session.getUserId(), ctx);
        } catch (final OXCachingException e) {
            LOG.error(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public int[] getAllMessageCount(final String folder) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        final MailFolder f = mailAccess.getFolderStorage().getFolder(fullname);
        return new int[] { f.getMessageCount(), f.getNewMessageCount(), f.getUnreadMessageCount(), f.getDeletedMessageCount() };
    }

    @Override
    public SearchIterator<?> getAllMessages(final String folder, final int sortCol, final int order, final int[] fields, final int[] fromToIndices) throws MailException {
        return getMessages(folder, fromToIndices, sortCol, order, null, null, false, fields);
    }

    @Override
    public SearchIterator<?> getAllThreadedMessages(final String folder, final int[] fields, final int[] fromToIndices) throws MailException {
        return getThreadedMessages(folder, fromToIndices, null, null, false, fields);
    }

    @Override
    public SearchIterator<?> getChildFolders(final String parentFolder, final boolean all) throws MailException {
        initConnection();
        final String parentFullname = prepareMailFolderParam(parentFolder);
        final List<MailFolder> children = Arrays.asList(mailAccess.getFolderStorage().getSubfolders(parentFullname, all));
        if (children.isEmpty()) {
            return SearchIteratorAdapter.createEmptyIterator();
        }
        /*
         * Check if denoted parent can hold default folders like Trash, Sent, etc.
         */
        if (!MailFolder.DEFAULT_FOLDER_ID.equals(parentFullname) && !prepareMailFolderParam(getInboxFolder()).equals(parentFullname)) {
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
        if (isDefaultFoldersChecked()) {
            names = getSortedDefaultMailFolders();
        } else {
            names = new String[] {
                prepareMailFolderParam(getInboxFolder()), prepareMailFolderParam(getDraftsFolder()),
                prepareMailFolderParam(getSentFolder()), prepareMailFolderParam(getSpamFolder()), prepareMailFolderParam(getTrashFolder()) };
        }
        /*
         * Sort them
         */
        Collections.sort(children, new MailFolderComparator(names, getUserLocale()));
        return new SearchIteratorDelegator<MailFolder>(children.iterator(), children.size());
    }

    @Override
    public String getConfirmedHamFolder() throws MailException {
        if (isDefaultFoldersChecked()) {
            return prepareFullname(getDefaultMailFolder(StorageUtility.INDEX_CONFIRMED_HAM));
        }
        initConnection();
        return prepareFullname(mailAccess.getFolderStorage().getConfirmedHamFolder());
    }

    @Override
    public String getConfirmedSpamFolder() throws MailException {
        if (isDefaultFoldersChecked()) {
            return prepareFullname(getDefaultMailFolder(StorageUtility.INDEX_CONFIRMED_SPAM));
        }
        initConnection();
        return prepareFullname(mailAccess.getFolderStorage().getConfirmedSpamFolder());
    }

    private String getDefaultMailFolder(final int index) {
        final String[] arr = (String[]) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_ARR);
        return arr == null ? null : arr[index];
    }

    private String[] getSortedDefaultMailFolders() {
        final String[] arr = (String[]) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_ARR);
        if (arr == null) {
            return new String[0];
        }
        return new String[] {
            INBOX_ID, arr[StorageUtility.INDEX_DRAFTS], arr[StorageUtility.INDEX_SENT], arr[StorageUtility.INDEX_SPAM],
            arr[StorageUtility.INDEX_TRASH] };
    }

    @Override
    public int getDeletedMessageCount(final String folder) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        return mailAccess.getFolderStorage().getFolder(fullname).getDeletedMessageCount();
    }

    @Override
    public String getDraftsFolder() throws MailException {
        if (isDefaultFoldersChecked()) {
            return prepareFullname(getDefaultMailFolder(StorageUtility.INDEX_DRAFTS));
        }
        initConnection();
        return prepareFullname(mailAccess.getFolderStorage().getDraftsFolder());
    }

    @Override
    public MailFolder getFolder(final String folder, final boolean checkFolder) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        return mailAccess.getFolderStorage().getFolder(fullname);
    }

    @Override
    public MailMessage getForwardMessageForDisplay(final String[] folders, final long[] fowardMsgUIDs, final UserSettingMail usm) throws MailException {
        if ((null == folders) || (null == fowardMsgUIDs) || (folders.length != fowardMsgUIDs.length)) {
            throw new IllegalArgumentException("Illegal arguments");
        }
        initConnection();
        final MailMessage[] originalMails = new MailMessage[folders.length];
        for (int i = 0; i < folders.length; i++) {
            originalMails[i] = mailAccess.getMessageStorage().getMessage(prepareMailFolderParam(folders[i]), fowardMsgUIDs[i], false);
        }
        return mailAccess.getLogicTools().getFowardMessage(originalMails, usm);
    }

    @Override
    public String getInboxFolder() throws MailException {
        if (isDefaultFoldersChecked()) {
            return prepareFullname(INBOX_ID);
        }
        initConnection();
        return prepareFullname(mailAccess.getFolderStorage().getFolder(INBOX_ID).getFullname());
    }

    @Override
    public MailConfig getMailConfig() throws MailException {
        return mailConfig;
    }

    private static final MailListField[] FIELDS_FLAGS = new MailListField[] { MailListField.FLAGS };

    private static final transient Object[] ARGS_FLAG_SEEN_SET = new Object[] { Integer.valueOf(MailMessage.FLAG_SEEN) };

    private static final transient Object[] ARGS_FLAG_SEEN_UNSET = new Object[] { Integer.valueOf(-1 * MailMessage.FLAG_SEEN) };

    @Override
    public MailMessage getMessage(final String folder, final long msgUID) throws MailException {
        initConnection();
        if (MailFolder.DEFAULT_FOLDER_ID.equals(folder)) {
            throw new MailException(MailException.Code.FOLDER_DOES_NOT_HOLD_MESSAGES, MailFolder.DEFAULT_FOLDER_ID);
        }
        final String fullname = prepareMailFolderParam(folder);
        final MailMessage mail = mailAccess.getMessageStorage().getMessage(fullname, msgUID, true);
        if (mail != null) {
            /*
             * Update cache since \Seen flag is possibly changed
             */
            try {
                if (MailMessageCache.getInstance().containsFolderMessages(fullname, session.getUserId(), ctx)) {
                    /*
                     * Update cache entry
                     */
                    MailMessageCache.getInstance().updateCachedMessages(
                        new long[] { mail.getMailId() },
                        fullname,
                        session.getUserId(),
                        ctx,
                        FIELDS_FLAGS,
                        mail.isSeen() ? ARGS_FLAG_SEEN_SET : ARGS_FLAG_SEEN_UNSET);

                }
            } catch (final OXCachingException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return mail;
    }

    @Override
    public MailPart getMessageAttachment(final String folder, final long msgUID, final String attachmentPosition, final boolean displayVersion) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        return mailAccess.getMessageStorage().getAttachment(fullname, msgUID, attachmentPosition);
    }

    @Override
    public int getMessageCount(final String folder) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        return mailAccess.getFolderStorage().getFolder(fullname).getMessageCount();
    }

    @Override
    public MailPart getMessageImage(final String folder, final long msgUID, final String cid) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        return mailAccess.getMessageStorage().getImageAttachment(fullname, msgUID, cid);
    }

    @Override
    public MailMessage[] getMessageList(final String folder, final long[] uids, final int[] fields) throws MailException {
        /*
         * Although message cache is only used within mail implementation, we have to examine if cache already holds desired messages. If
         * the cache holds the desired messages no connection has to be fetched/established. This avoids a lot of overhead.
         */
        final String fullname = prepareMailFolderParam(folder);
        try {
            final MailMessage[] mails = MailMessageCache.getInstance().getMessages(uids, fullname, session.getUserId(), ctx);
            if (null != mails) {
                return mails;
            }
        } catch (final OXCachingException e) {
            LOG.error(e.getMessage(), e);
        }
        initConnection();
        final MailMessage[] mails = mailAccess.getMessageStorage().getMessages(
            fullname,
            uids,
            MailField.toFields(MailListField.getFields(fields)));
        try {
            if (MailMessageCache.getInstance().containsFolderMessages(fullname, session.getUserId(), ctx)) {
                MailMessageCache.getInstance().putMessages(mails, session.getUserId(), ctx);
            }
        } catch (final OXCachingException e) {
            LOG.error(e.getMessage(), e);
        }
        return mails;
    }

    @Override
    public SearchIterator<?> getMessages(final String folder, final int[] fromToIndices, final int sortCol, final int order, final int[] searchCols, final String[] searchPatterns, final boolean linkSearchTermsWithOR, final int[] fields) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        final SearchTerm<?> searchTerm = (searchCols == null) || (searchCols.length == 0) ? null : SearchUtility.parseFields(
            searchCols,
            searchPatterns,
            linkSearchTermsWithOR);
        /*
         * Identify and sort messages according to search term and sort criteria while only fetching their IDs
         */
        MailMessage[] mails = mailAccess.getMessageStorage().searchMessages(
            fullname,
            null == fromToIndices ? IndexRange.NULL : new IndexRange(fromToIndices[0], fromToIndices[1]),
            MailSortField.getField(sortCol),
            OrderDirection.getOrderDirection(order),
            searchTerm,
            FIELDS_ID_INFO);
        if ((mails == null) || (mails.length == 0)) {
            return SearchIterator.EMPTY_ITERATOR;
        }
        final boolean cachable = (mails.length < MailProperties.getInstance().getMailFetchLimit());
        final MailField[] useFields;
        final boolean onlyFolderAndID;
        if (cachable) {
            /*
             * Selection fits into cache: Prepare for caching
             */
            useFields = com.openexchange.mail.mime.utils.MIMEStorageUtility.getCacheFieldsArray();
            onlyFolderAndID = false;
        } else {
            useFields = MailField.getFields(fields);
            onlyFolderAndID = onlyFolderAndID(useFields);
        }
        if (!onlyFolderAndID) {
            /*
             * Extract IDs
             */
            final long[] mailIds = new long[mails.length];
            for (int i = 0; i < mailIds.length; i++) {
                mailIds[i] = mails[i].getMailId();
            }
            /*
             * Fetch identified messages by their IDs and pre-fill them according to specified fields
             */
            mails = mailAccess.getMessageStorage().getMessages(fullname, mailIds, useFields);
        }
        try {
            /*
             * Remove old user cache entries
             */
            MailMessageCache.getInstance().removeUserMessages(session.getUserId(), ctx);
            if ((mails != null) && (mails.length > 0) && (cachable)) {
                /*
                 * ... and put new ones
                 */
                MailMessageCache.getInstance().putMessages(mails, session.getUserId(), ctx);
            }
        } catch (final OXCachingException e) {
            LOG.error(e.getMessage(), e);
        }
        return SearchIteratorAdapter.createArrayIterator(mails);
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
    public int getNewMessageCount(final String folder) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        return mailAccess.getFolderStorage().getFolder(fullname).getNewMessageCount();
    }

    @Override
    public SearchIterator<?> getNewMessages(final String folder, final int sortCol, final int order, final int[] fields, final int limit) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        return SearchIteratorAdapter.createArrayIterator(mailAccess.getMessageStorage().getUnreadMessages(
            fullname,
            MailSortField.getField(sortCol),
            OrderDirection.getOrderDirection(order),
            MailField.toFields(MailListField.getFields(fields)),
            limit));
    }

    @Override
    public SearchIterator<?> getPathToDefaultFolder(final String folder) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        return SearchIteratorAdapter.createArrayIterator(mailAccess.getFolderStorage().getPath2DefaultFolder(fullname));
    }

    @Override
    public long[][] getQuotas(final int[] types) throws MailException {
        initConnection();
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
    public long getQuotaLimit(final int type) throws MailException {
        initConnection();
        if (QUOTA_RESOURCE_STORAGE == type) {
            return mailAccess.getFolderStorage().getStorageQuota(INBOX_ID).getLimit();
        } else if (QUOTA_RESOURCE_MESSAGE == type) {
            return mailAccess.getFolderStorage().getMessageQuota(INBOX_ID).getLimit();
        }
        throw new IllegalArgumentException("Unknown quota resource type: " + type);
    }

    @Override
    public long getQuotaUsage(final int type) throws MailException {
        initConnection();
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
    public MailMessage getReplyMessageForDisplay(final String folder, final long replyMsgUID, final boolean replyToAll, final UserSettingMail usm) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        final MailMessage originalMail = mailAccess.getMessageStorage().getMessage(fullname, replyMsgUID, false);
        return mailAccess.getLogicTools().getReplyMessage(originalMail, replyToAll, usm);
    }

    @Override
    public SearchIterator<?> getRootFolders() throws MailException {
        initConnection();
        return SearchIteratorAdapter.createArrayIterator(new MailFolder[] { mailAccess.getFolderStorage().getRootFolder() });
    }

    @Override
    public String getSentFolder() throws MailException {
        if (isDefaultFoldersChecked()) {
            return prepareFullname(getDefaultMailFolder(StorageUtility.INDEX_SENT));
        }
        initConnection();
        return prepareFullname(mailAccess.getFolderStorage().getSentFolder());
    }

    @Override
    public String getSpamFolder() throws MailException {
        if (isDefaultFoldersChecked()) {
            return prepareFullname(getDefaultMailFolder(StorageUtility.INDEX_SPAM));
        }
        initConnection();
        return prepareFullname(mailAccess.getFolderStorage().getSpamFolder());
    }

    @Override
    public SearchIterator<?> getThreadedMessages(final String folder, final int[] fromToIndices, final int[] searchCols, final String[] searchPatterns, final boolean linkSearchTermsWithOR, final int[] fields) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        final SearchTerm<?> searchTerm = (searchCols == null) || (searchCols.length == 0) ? null : SearchUtility.parseFields(
            searchCols,
            searchPatterns,
            linkSearchTermsWithOR);
        /*
         * Identify and thread-sort messages according to search term while only fetching their IDs
         */
        MailMessage[] mails = mailAccess.getMessageStorage().getThreadSortedMessages(
            fullname,
            fromToIndices == null ? IndexRange.NULL : new IndexRange(fromToIndices[0], fromToIndices[1]),
            searchTerm,
            FIELDS_ID_INFO);
        if ((mails == null) || (mails.length == 0)) {
            return SearchIterator.EMPTY_ITERATOR;
        }
        final MailField[] useFields;
        final boolean onlyFolderAndID;
        if (mails.length < MailProperties.getInstance().getMailFetchLimit()) {
            /*
             * Selection fits into cache: Prepare for caching
             */
            useFields = com.openexchange.mail.mime.utils.MIMEStorageUtility.getCacheFieldsArray();
            onlyFolderAndID = false;
        } else {
            useFields = MailField.toFields(MailListField.getFields(fields));
            onlyFolderAndID = onlyFolderAndID(useFields);
        }
        if (!onlyFolderAndID) {
            /*
             * Extract IDs
             */
            final long[] mailIds = new long[mails.length];
            for (int i = 0; i < mailIds.length; i++) {
                mailIds[i] = mails[i].getMailId();
            }
            /*
             * Fetch identified messages by their IDs and pre-fill them according to specified fields
             */
            mails = mailAccess.getMessageStorage().getMessages(fullname, mailIds, useFields);
        }
        try {
            /*
             * Remove old user cache entries
             */
            MailMessageCache.getInstance().removeFolderMessages(fullname, session.getUserId(), ctx);
            if ((mails != null) && (mails.length > 0) && (mails.length < MailProperties.getInstance().getMailFetchLimit())) {
                /*
                 * ... and put new ones
                 */
                MailMessageCache.getInstance().putMessages(mails, session.getUserId(), ctx);
            }
        } catch (final OXCachingException e) {
            LOG.error(e.getMessage(), e);
        }
        return SearchIteratorAdapter.createArrayIterator(mails);
    }

    @Override
    public String getTrashFolder() throws MailException {
        if (isDefaultFoldersChecked()) {
            return prepareFullname(getDefaultMailFolder(StorageUtility.INDEX_TRASH));
        }
        initConnection();
        return prepareFullname(mailAccess.getFolderStorage().getTrashFolder());
    }

    @Override
    public int getUnreadMessageCount(final String folder) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        return mailAccess.getFolderStorage().getFolder(fullname).getUnreadMessageCount();
    }

    private void initConnection() throws MailException {
        if (init) {
            return;
        }
        /*
         * Fetch a mail access (either from cache or a new instance)
         */
        mailAccess = MailAccess.getInstance(session);
        if (!mailAccess.isConnected()) {
            /*
             * Get new mail configuration
             */
            final long start = System.currentTimeMillis();
            try {
                mailAccess.connect();
                MailServletInterface.mailInterfaceMonitor.addUseTime(System.currentTimeMillis() - start);
                MailServletInterface.mailInterfaceMonitor.changeNumSuccessfulLogins(true);
            } catch (final MailException e) {
                if (e.getDetailNumber() == 2) {
                    MailServletInterface.mailInterfaceMonitor.changeNumFailedLogins(true);
                }
                throw e;
            }
        }
        mailConfig = mailAccess.getMailConfig();
        mailAccess.getFolderStorage().checkDefaultFolders();
        init = true;
    }

    private boolean isDefaultFoldersChecked() {
        final Boolean b = (Boolean) session.getParameter(MailSessionParameterNames.PARAM_DEF_FLD_FLAG);
        return (b != null) && b.booleanValue();
    }

    @Override
    public String saveDraft(final ComposedMailMessage draftMail, final boolean autosave) throws MailException {
        if (autosave) {
            return autosaveDraft(draftMail);
        }
        initConnection();
        return mailAccess.getMessageStorage().saveDraft(
            (prepareMailFolderParam(mailAccess.getFolderStorage().getDraftsFolder())),
            draftMail).getMailPath().toString();
    }

    private String autosaveDraft(final ComposedMailMessage draftMail) throws MailException {
        initConnection();
        final String draftFullname = prepareMailFolderParam(mailAccess.getFolderStorage().getDraftsFolder());
        /*
         * Auto-save draft
         */
        if (!draftMail.isDraft()) {
            draftMail.setFlag(MailMessage.FLAG_DRAFT, true);
        }
        final MailPath msgref = draftMail.getMsgref();
        final MailMessage origMail;
        if (null == msgref) {
            origMail = null;
        } else {
            origMail = mailAccess.getMessageStorage().getMessage(msgref.getFolder(), msgref.getUid(), false);
            if (origMail != null) {
                /*
                 * Check for attachments and add them
                 */
                final NonInlineForwardPartHandler handler = new NonInlineForwardPartHandler();
                new MailMessageParser().parseMailMessage(origMail, handler);
                final List<MailPart> parts = handler.getNonInlineParts();
                if (!parts.isEmpty()) {
                    final TransportProvider tp = TransportProviderRegistry.getTransportProviderBySession(session);
                    for (final MailPart mailPart : parts) {
                        /*
                         * Create and add a referenced part from original draft mail
                         */
                        draftMail.addEnclosedPart(tp.getNewReferencedPart(mailPart, session));
                    }
                }
            }
        }
        final long uid;
        {
            final MailMessage filledMail = MIMEMessageConverter.fillComposedMailMessage(draftMail);
            filledMail.setFlag(MailMessage.FLAG_DRAFT, true);
            /*
             * Append message to draft folder without invoking draftMail.cleanUp() afterwards to avoid loss of possibly uploaded images
             */
            uid = mailAccess.getMessageStorage().appendMessages(draftFullname, new MailMessage[] { filledMail })[0];
        }
        /*
         * Check for draft-edit operation: Delete old version
         */
        if (origMail != null) {
            if (origMail.isDraft()) {
                deleteMessages(origMail.getFolder(), new long[] { origMail.getMailId() }, true);
            }
            draftMail.setMsgref(null);
        }
        /*
         * Return draft mail
         */
        final MailMessage m = mailAccess.getMessageStorage().getMessage(draftFullname, uid, true);
        if (null == m) {
            throw new MailException(MailException.Code.MAIL_NOT_FOUND, Long.valueOf(uid), draftFullname);
        }
        return m.getMailPath().toString();
    }

    @Override
    public String saveFolder(final MailFolderDescription mailFolder) throws MailException {
        initConnection();
        if (!mailFolder.containsExists() && !mailFolder.containsFullname()) {
            throw new MailException(MailException.Code.INSUFFICIENT_FOLDER_ATTR);
        }
        if ((mailFolder.containsExists() && mailFolder.exists()) || ((mailFolder.getFullname() != null) && mailAccess.getFolderStorage().exists(
            mailFolder.getFullname()))) {
            /*
             * Update
             */
            String fullname = prepareMailFolderParam(mailFolder.getFullname());
            final char separator = mailFolder.getSeparator();
            final String oldParent;
            final String oldName;
            {
                final int pos = fullname.lastIndexOf(separator);
                if (pos == -1) {
                    oldParent = "";
                    oldName = fullname;
                } else {
                    oldParent = fullname.substring(0, pos);
                    oldName = fullname.substring(pos + 1);
                }
            }
            boolean movePerformed = false;
            /*
             * Check if a move shall be performed
             */
            if (mailFolder.containsParentFullname()) {
                final String newParent = prepareMailFolderParam(mailFolder.getParentFullname());
                final StringBuilder newFullname = new StringBuilder(newParent).append(mailFolder.getSeparator());
                if (mailFolder.containsName()) {
                    newFullname.append(mailFolder.getName());
                } else {
                    newFullname.append(oldName);
                }
                if (!newParent.equals(oldParent)) { // move & rename
                    fullname = mailAccess.getFolderStorage().moveFolder(fullname, newFullname.toString());
                    movePerformed = true;
                }
            }
            /*
             * Check if a rename shall be performed
             */
            if (!movePerformed && mailFolder.containsName()) {
                final String newName = mailFolder.getName();
                if (!newName.equals(oldName)) { // rename
                    fullname = mailAccess.getFolderStorage().renameFolder(fullname, newName);
                }
            }
            /*
             * Handle update of permission or subscription
             */
            return prepareFullname(mailAccess.getFolderStorage().updateFolder(fullname, mailFolder));
        }
        /*
         * Insert
         */
        return prepareFullname(mailAccess.getFolderStorage().createFolder(mailFolder));
    }

    @Override
    public String sendMessage(final ComposedMailMessage composedMail, final ComposeType type) throws MailException {
        /*
         * Check for valid from address
         */
        try {
            final Set<InternetAddress> validAddrs = new HashSet<InternetAddress>(4);
            if (usm.getSendAddr() != null && usm.getSendAddr().length() > 0) {
                validAddrs.add(new InternetAddress(usm.getSendAddr()));
            }
            final User user = UserStorage.getStorageUser(session.getUserId(), session.getContextId());
            validAddrs.add(new InternetAddress(user.getMail()));
            final String[] aliases = user.getAliases();
            for (final String alias : aliases) {
                validAddrs.add(new InternetAddress(alias));
            }
            final List<InternetAddress> from = Arrays.asList(composedMail.getFrom());
            if (!validAddrs.containsAll(from)) {
                throw new MailException(
                    MailException.Code.INVALID_SENDER,
                    from.size() == 1 ? from.get(0).toString() : Arrays.toString(composedMail.getFrom()));
            }
        } catch (final AddressException e) {
            throw MIMEMailException.handleMessagingException(e, mailConfig);
        }
        /*
         * Initialize
         */
        initConnection();
        final MailTransport transport = MailTransport.getInstance(session);
        try {
            /*
             * Send mail
             */
            final long startTransport = System.currentTimeMillis();
            final MailMessage sentMail = transport.sendMailMessage(composedMail, type);
            /*
             * Email successfully sent, trigger data retention
             */
            final DataRetentionService retentionService = ServerServiceRegistry.getInstance().getService(DataRetentionService.class);
            if (null != retentionService) {
                // TODO: Delegate runnable to thread pool
                new Runnable() {

                    public void run() {
                        try {
                            final RetentionData retentionData = retentionService.newInstance();
                            retentionData.setStartTime(new Date(startTransport));
                            retentionData.setIdentifier(transport.getTransportConfig().getLogin());
                            retentionData.setIPAddress(session.getLocalIp());
                            retentionData.setSenderAddress(sentMail.getFrom()[0].getAddress());
                            final Set<InternetAddress> recipients = new HashSet<InternetAddress>(Arrays.asList(sentMail.getTo()));
                            recipients.addAll(Arrays.asList(sentMail.getCc()));
                            recipients.addAll(Arrays.asList(sentMail.getBcc()));
                            final int size = recipients.size();
                            final String[] recipientsArr = new String[size];
                            final Iterator<InternetAddress> it = recipients.iterator();
                            for (int i = 0; i < size; i++) {
                                recipientsArr[i] = it.next().getAddress();
                            }
                            retentionData.setRecipientAddresses(recipientsArr);
                            /*
                             * Finally store it
                             */
                            retentionService.storeOnTransport(retentionData);
                        } catch (final MailException e) {
                            LOG.error(e.getMessage(), e);
                        } catch (final DataRetentionException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                }.run();
            }
            /*
             * Check for a reply/forward
             */
            if (ComposeType.REPLY.equals(type)) {
                final MailPath path = composedMail.getMsgref();
                if (null == path) {
                    LOG.warn("Missing msgref on reply. Corresponding mail cannot be marked as answered.", new Throwable());
                } else {
                    /*
                     * Mark referenced mail as answered
                     */
                    final String fullname = path.getFolder();
                    final long[] uids = new long[] { path.getUid() };
                    mailAccess.getMessageStorage().updateMessageFlags(fullname, uids, MailMessage.FLAG_ANSWERED, true);
                    try {
                        if (MailMessageCache.getInstance().containsFolderMessages(fullname, session.getUserId(), ctx)) {
                            /*
                             * Update cache entries
                             */
                            MailMessageCache.getInstance().updateCachedMessages(
                                uids,
                                fullname,
                                session.getUserId(),
                                ctx,
                                FIELDS_FLAGS,
                                new Object[] { Integer.valueOf(MailMessage.FLAG_ANSWERED) });
                        }
                    } catch (final OXCachingException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            } else if (ComposeType.FORWARD.equals(type)) {
                final MailPath supPath = composedMail.getMsgref();
                if (null == supPath) {
                    final int count = composedMail.getEnclosedCount();
                    final long[] ids = new long[1];
                    for (int i = 0; i < count; i++) {
                        final MailPart part = composedMail.getEnclosedMailPart(i);
                        final MailPath path = part.getMsgref();
                        if ((path != null) && part.getContentType().isMimeType(MIMETypes.MIME_MESSAGE_RFC822)) {
                            /*
                             * Mark referenced mail as forwarded
                             */
                            ids[0] = path.getUid();
                            mailAccess.getMessageStorage().updateMessageFlags(path.getFolder(), ids, MailMessage.FLAG_FORWARDED, true);
                            try {
                                if (MailMessageCache.getInstance().containsFolderMessages(path.getFolder(), session.getUserId(), ctx)) {
                                    /*
                                     * Update cache entries
                                     */
                                    MailMessageCache.getInstance().updateCachedMessages(
                                        ids,
                                        path.getFolder(),
                                        session.getUserId(),
                                        ctx,
                                        FIELDS_FLAGS,
                                        new Object[] { Integer.valueOf(MailMessage.FLAG_FORWARDED) });
                                }
                            } catch (final OXCachingException e) {
                                LOG.error(e.getMessage(), e);
                            }
                        }
                    }
                } else {
                    /*
                     * Mark referenced mail as forwarded
                     */
                    final String fullname = supPath.getFolder();
                    final long[] uids = new long[] { supPath.getUid() };
                    mailAccess.getMessageStorage().updateMessageFlags(fullname, uids, MailMessage.FLAG_FORWARDED, true);
                    try {
                        if (MailMessageCache.getInstance().containsFolderMessages(fullname, session.getUserId(), ctx)) {
                            /*
                             * Update cache entries
                             */
                            MailMessageCache.getInstance().updateCachedMessages(
                                uids,
                                fullname,
                                session.getUserId(),
                                ctx,
                                FIELDS_FLAGS,
                                new Object[] { Integer.valueOf(MailMessage.FLAG_FORWARDED) });
                        }
                    } catch (final OXCachingException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
            if (UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx).isNoCopyIntoStandardSentFolder()) {
                /*
                 * No copy in sent folder
                 */
                return null;
            }
            /*
             * Append to Sent folder
             */
            final long start = System.currentTimeMillis();
            final String sentFullname = prepareMailFolderParam(mailAccess.getFolderStorage().getSentFolder());
            final long[] uidArr;
            try {
                uidArr = mailAccess.getMessageStorage().appendMessages(sentFullname, new MailMessage[] { sentMail });
                try {
                    /*
                     * Update cache
                     */
                    MailMessageCache.getInstance().removeFolderMessages(sentFullname, session.getUserId(), ctx);
                } catch (final OXCachingException e) {
                    LOG.error(e.getMessage(), e);
                }
            } catch (final MailException e) {
                if (e.getMessage().indexOf("quota") != -1) {
                    throw new MailException(MailException.Code.COPY_TO_SENT_FOLDER_FAILED_QUOTA, e, new Object[0]);
                }
                throw new MailException(MailException.Code.COPY_TO_SENT_FOLDER_FAILED, e, new Object[0]);
            }
            if ((uidArr != null) && (uidArr[0] != -1)) {
                /*
                 * Mark appended sent mail as seen
                 */
                mailAccess.getMessageStorage().updateMessageFlags(sentFullname, uidArr, MailMessage.FLAG_SEEN, true);
            }
            final MailPath retval = new MailPath(sentFullname, uidArr[0]);
            if (LOG.isDebugEnabled()) {
                LOG.debug(new StringBuilder(128).append("Mail copy (").append(retval.toString()).append(") appended in ").append(
                    System.currentTimeMillis() - start).append("msec").toString());
            }
            return retval.toString();
        } finally {
            transport.close();
        }
    }

    @Override
    public void sendReceiptAck(final String folder, final long msgUID, final String fromAddr) throws MailException {
        /*
         * Check for valid from address
         */
        try {
            final Set<InternetAddress> validAddrs = new HashSet<InternetAddress>(4);
            if (usm.getSendAddr() != null && usm.getSendAddr().length() > 0) {
                validAddrs.add(new InternetAddress(usm.getSendAddr()));
            }
            final User user = UserStorage.getStorageUser(session.getUserId(), session.getContextId());
            validAddrs.add(new InternetAddress(user.getMail()));
            final String[] aliases = user.getAliases();
            for (final String alias : aliases) {
                validAddrs.add(new InternetAddress(alias));
            }
            if (!validAddrs.contains(new InternetAddress(fromAddr))) {
                throw new MailException(MailException.Code.INVALID_SENDER, fromAddr);
            }
        } catch (final AddressException e) {
            throw MIMEMailException.handleMessagingException(e);
        }
        /*
         * Initialize
         */
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        final MailTransport transport = MailTransport.getInstance(session);
        try {
            transport.sendReceiptAck(mailAccess.getMessageStorage().getMessage(fullname, msgUID, false), fromAddr);
        } finally {
            transport.close();
        }
        mailAccess.getMessageStorage().updateMessageFlags(fullname, new long[] { msgUID }, MailMessage.FLAG_READ_ACK, true);
    }

    private static final MailListField[] FIELDS_COLOR_LABEL = new MailListField[] { MailListField.COLOR_LABEL };

    @Override
    public void updateMessageColorLabel(final String folder, final long[] msgUID, final int newColorLabel) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        mailAccess.getMessageStorage().updateMessageColorLabel(fullname, msgUID, newColorLabel);
        try {
            if (MailMessageCache.getInstance().containsFolderMessages(fullname, session.getUserId(), ctx)) {
                /*
                 * Update cache entries
                 */
                MailMessageCache.getInstance().updateCachedMessages(
                    msgUID,
                    fullname,
                    session.getUserId(),
                    ctx,
                    FIELDS_COLOR_LABEL,
                    new Object[] { Integer.valueOf(newColorLabel) });
            }
        } catch (final OXCachingException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public void updateMessageFlags(final String folder, final long[] msgUID, final int flagBits, final boolean flagVal) throws MailException {
        initConnection();
        final String fullname = prepareMailFolderParam(folder);
        mailAccess.getMessageStorage().updateMessageFlags(fullname, msgUID, flagBits, flagVal);
        if (usm.isSpamEnabled() && ((flagBits & MailMessage.FLAG_SPAM) > 0)) {
            /*
             * Remove from cache
             */
            try {
                if (MailMessageCache.getInstance().containsFolderMessages(fullname, session.getUserId(), ctx)) {
                    MailMessageCache.getInstance().removeMessages(msgUID, fullname, session.getUserId(), ctx);

                }
            } catch (final OXCachingException e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            try {
                if (MailMessageCache.getInstance().containsFolderMessages(fullname, session.getUserId(), ctx)) {
                    /*
                     * Update cache entries
                     */
                    MailMessageCache.getInstance().updateCachedMessages(
                        msgUID,
                        fullname,
                        session.getUserId(),
                        ctx,
                        FIELDS_FLAGS,
                        new Object[] { Integer.valueOf(flagVal ? flagBits : (flagBits * -1)) });
                }
            } catch (final OXCachingException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /*-
     * ################################################################################
     * #############################   HELPER CLASSES   ######h########################
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

        public int compare(final MailFolder o1, final MailFolder o2) {
            return collator.compare(o1.getName(), o2.getName());
        }
    }
}
