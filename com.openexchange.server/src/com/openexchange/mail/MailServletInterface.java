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

import java.util.Collection;
import java.util.List;
import com.openexchange.api2.MailInterfaceMonitor;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.groupware.importexport.MailImportResult;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link MailServletInterface} - The mail interface which invokes the mail layer methods.
 * <p>
 * This interface's purpose is to be conform to other interfaces used in other groupware modules which are used throughout servlet
 * instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailServletInterface {

    /**
     * The constant for quota storage resource
     */
    public static final int QUOTA_RESOURCE_STORAGE = 1;

    /**
     * The constant for quota message resource
     */
    public static final int QUOTA_RESOURCE_MESSAGE = 2;

    /**
     * Gets a proper implementation of {@link MailServletInterface}
     * <p>
     * <b>NOTE:</b> Don't forget to invoke {@link #close(boolean)} after usage
     *
     * <pre>
     * MailInterface mailInterface = MailInterface.getInstance(session);
     * try {
     *     //Do some stuff here...
     * } finally {
     *     mailInterface.close(true);
     * }
     * </pre>
     *
     * @param session The session
     * @return An instance of {@link MailServletInterface}
     * @throws OXException
     */
    public static final MailServletInterface getInstance(final Session session) throws OXException {
        return new MailServletInterfaceImpl(session);
    }

    /**
     * Mail monitor
     */
    public static final MailInterfaceMonitor mailInterfaceMonitor = new MailInterfaceMonitor();

    /**
     * Gets a mail's ID by specified "Message-Id" header.
     *
     * @param folder The folder to search in
     * @param messageID The "Message-Id" header
     * @return The ID of the mail corresponding to specified "Message-Id" header
     * @throws OXException If no mauil could be found
     */
    public abstract String getMailIDByMessageID(final String folder, final String messageID) throws OXException;

    /**
     * Returns all message counts in specified folder in an <code>int</code> array as follows: <code>0</code>: Message Count, <code>1</code>
     * : New Message Count, <code>2</code>: Unread MessageCount, <code>3</code>: Deleted Message Count
     */
    public abstract int[] getAllMessageCount(String folder) throws OXException;

    /**
     * Returns the number of messages in folder
     */
    public abstract int getMessageCount(String folder) throws OXException;

    /**
     * Returns the number of new messages in folder
     */
    public abstract int getNewMessageCount(String folder) throws OXException;

    /**
     * Returns the number of unread messages in folder
     */
    public abstract int getUnreadMessageCount(String folder) throws OXException;

    /**
     * Returns the number messages which are marked for deletion in folder
     */
    public abstract int getDeletedMessageCount(String folder) throws OXException;

    /**
     * Gets both quota limit and quota usage in an array with length set to <code>2</code> for each resource type
     *
     * @param types The resource types; {@link #QUOTA_RESOURCE_STORAGE} or {@link #QUOTA_RESOURCE_MESSAGE}
     * @return Both quota limit and quota usage in an array with length set to <code>2</code> for each resource type
     * @throws OXException If quotas cannot be retrieved
     */
    public abstract long[][] getQuotas(int[] types) throws OXException;

    /**
     * Returns the quota limit
     */
    public abstract long getQuotaLimit(int type) throws OXException;

    /**
     * Returns the current quota usage
     */
    public abstract long getQuotaUsage(int type) throws OXException;

    /**
     * Returns an instance of <code>SearchIterator</code> containing max. <code>limit</code> new (unseen) messages located in given folder.
     */
    public abstract SearchIterator<MailMessage> getNewMessages(String folder, int sortCol, int order, int[] fields, int limit) throws OXException;

    /**
     * Returns an instance of <code>SearchIterator</code> containing all messages located in given folder.
     */
    public abstract SearchIterator<MailMessage> getAllMessages(String folder, int sortCol, int order, int[] fields, int[] fromToIndices) throws OXException;

    /**
     * Returns an instance of <code>SearchIterator</code> containing a selection of messages located in given folder.
     * <code>fromToIndices</code> can define a range of messages that should be returned. Moreover <code>searchCols</code> and
     * <code>searchPatterns</code> defines a search pattern to further confine returned messages.
     */
    public abstract SearchIterator<MailMessage> getMessages(String folder, int[] fromToIndices, int sortCol, int order, int[] searchCols, String[] searchPatterns, boolean linkSearchTermsWithOR, int[] fields) throws OXException;

    /**
     * Returns an instance of <code>SearchIterator</code> containing a selection of messages located in given folder.
     * <code>fromToIndices</code> can define a range of messages that should be returned. Moreover <code>searchCols</code> and
     * <code>searchPatterns</code> defines a search pattern to further confine returned messages.
     */
    public abstract SearchIterator<MailMessage> getMessages(String folder, int[] fromToIndices, int sortCol, int order, SearchTerm<?> searchTerm, boolean linkSearchTermsWithOR, int[] fields) throws OXException;

    /**
     * Returns a thread-view-sorted instance of <code>SearchIterator</code> containing all messages located in given folder.
     */
    public abstract SearchIterator<MailMessage> getAllThreadedMessages(String folder, int sortCol, int order, int[] fields, int[] fromToIndices) throws OXException;

    /**
     * Returns a thread-view-sorted instance of <code>SearchIterator</code> containing all messages located in given folder.
     */
    public abstract List<List<MailMessage>> getAllSimpleThreadStructuredMessages(String folder, boolean includeSent, boolean cache, int sortCol, int order, int[] fields, int[] fromToIndices, long max) throws OXException;

    /**
     * Returns a thread-view-sorted instance of <code>SearchIterator</code> containing a selection of messages located in given folder.
     * <code>fromToIndices</code> can define a range of messages that should be returned. Moreover <code>searchCols</code> and
     * <code>searchPatterns</code> defines a search pattern to further confine returned messages.
     */
    public abstract SearchIterator<MailMessage> getThreadedMessages(String folder, int[] fromToIndices, int sortCol, int order, int[] searchCols, String[] searchPatterns, boolean linkSearchTermsWithOR, int[] fields) throws OXException;

    /**
     * Returns the an array of messages located in given folder. If <code>fromToUID</code> is not <code>null</code> only messages fitting
     * into uid range will be returned.
     */
    public abstract MailMessage[] getMessageList(String folder, String[] uids, int[] fields, String[] headerFields) throws OXException;

    /**
     * Gets the mail identified through given ID from store located in given folder.
     *
     * @param folder The folder path
     * @param msgUID The mail ID
     * @return The mail identified through given ID from store located in given folder.
     * @throws OXException If mail cannot be fetched from store
     */
    public MailMessage getMessage(String folder, String msgUID) throws OXException {
        return getMessage(folder, msgUID, true);
    }

    /**
     * Gets the mail identified through given ID from store located in given folder.
     *
     * @param folder The folder path
     * @param msgUID The mail ID
     * @param markAsSeen <code>true</code> to mark as seen; otherwise <code>false</code> for peek only
     * @return The mail identified through given ID from store located in given folder.
     * @throws OXException If mail cannot be fetched from store
     */
    public abstract MailMessage getMessage(String folder, String msgUID, boolean markAsSeen) throws OXException;

    /**
     * Returns a message's attachment located at given <code>attachmentPosition</code> wrapped by an instance of
     * <code>JSONMessageAttachmentObject</code> for a convenient access to its attributes and content.
     *
     * @param displayVersion <code>true</code> if returned object is for display purpose; otherwise <code>false</code>
     */
    public abstract MailPart getMessageAttachment(String folder, String msgUID, String attachmentPosition, boolean displayVersion) throws OXException;

    /**
     * Returns message's attachments as a ZIP file backed by returned managed file instance.
     *
     * @param folder The folder
     * @param msgUID The message ID
     * @param attachmentPositions The attachment positions
     * @return A ZIP file backed by returned managed file instance
     * @throws OXException If an error occurs
     */
    public abstract ManagedFile getMessageAttachments(String folder, String msgUID, String[] attachmentPositions) throws OXException;

    /**
     * Returns messages as a ZIP file backed by returned managed file instance.
     *
     * @param folder The folder
     * @param msgIds The message identifiers
     * @return A ZIP file backed by returned managed file instance
     * @throws MailException If an error occurs
     */
    public abstract ManagedFile getMessages(String folder, String[] msgIds) throws OXException;

    /**
     * Returns a message's inline image located identified with given <code>cid</code> wrapped by an instance of
     * <code>JSONMessageAttachmentObject</code> for a convenient access to its attributes and content.
     */
    public abstract MailPart getMessageImage(String folder, String msgUID, String cid) throws OXException;

    /**
     * Saves specified draft mail.
     * <p>
     * If specified draft mail holds a reference to an existing draft mail - {@link MailMessage#getMsgref()} is not <code>null</code> - then
     * the referenced draft mail shall be replaced.
     *
     * @param draftMail The draft mail
     * @param autosave <code>true</code> to indicate an auto-save operation; otherwise <code>false</code>
     * @return The stored draft's mail path
     * @throws OXException
     */
    public abstract String saveDraft(ComposedMailMessage draftMail, boolean autosave, int accountId) throws OXException;

    /**
     * Sends a read acknowledgment to given message
     */
    public abstract void sendReceiptAck(String folder, String msgUID, String fromAddr) throws OXException;

    /**
     * Sends a form mail.
     *
     * @param composedMail The form mail (without any recipients)
     * @param groupId The identifier of the group to whose members shall receive the mail
     * @param accountId The account identifier
     * @throws OXException If mail transport fails
     */
    public abstract void sendFormMail(ComposedMailMessage composedMail, int groupId, int accountId) throws OXException;

    /**
     * Sends a message described through given instance of <code>msgObj</code> and its possible file attachments contained in given instance
     * of <code>uploadEvent</code>.
     */
    public abstract String sendMessage(ComposedMailMessage transportMail, ComposeType sendType, int accountId) throws OXException;

    /**
     * Appends given messages to given folder.
     *
     * @param destFolder The destination folder
     * @param msgs - The messages to append (<b>must</b> be completely pre-filled incl. content references)
     * @param force <code>true</code> to enforce append and to omit checks; otherwise <code>false</code>
     * @return The corresponding mail IDs in destination folder
     * @throws OXException If messages cannot be appended.
     */
    public abstract String[] appendMessages(String destFolder, MailMessage[] msgs, boolean force) throws OXException;

    /**
     * Overwrite this to implement a different append behaviour for mail imports.
     *
     * @param destFolder The destination folder
     * @param msgs - The messages to append (<b>must</b> be completely pre-filled incl. content references)
     * @param force <code>true</code> to enforce append and to omit checks; otherwise <code>false</code>
     * @return The corresponding mail IDs in destination folder
     * @throws OXException If messages cannot be appended.
     */
    public String[] importMessages(final String destFolder, final MailMessage[] msgs, final boolean force) throws OXException {
        return appendMessages(destFolder, msgs, force);
    }

    /**
     * Creates an instance of <code>JSONMessageObject</code> which contains the initial reply content of the message identifed through
     * <code>replyMsgUID</code>. <code>replyToAll</code> defines whether to reply to all involved entities or just to main sender.
     * <b>NOTE:</b>This method is intended to support Open-Xchange GUI's display onyl and does not really send the reply.
     */
    public abstract MailMessage getReplyMessageForDisplay(String folder, String replyMsgUID, boolean replyToAll, UserSettingMail usm) throws OXException;

    /**
     * Creates an instance of <code>JSONMessageObject</code> which contains the initial forward content of the message identifed through
     * <code>fowardMsgUID</code>. <b>NOTE:</b>This method is intended to support Open-Xchange GUI's display onyl and does not really send
     * the forward.
     */
    public abstract MailMessage getForwardMessageForDisplay(String[] folders, String[] fowardMsgUIDs, UserSettingMail usm) throws OXException;

    /**
     * Deletes the message located in given folder corresponding to given <code>msgUID</code>
     */
    public abstract boolean deleteMessages(String folder, String[] msgUIDs, boolean hardDelete) throws OXException;

    /**
     * Expunges denoted folder.
     */
    public abstract boolean expungeFolder(final String folder, final boolean hardDelete) throws OXException;

    /**
     * Clears all messages out of given folder.
     */
    public abstract boolean clearFolder(final String folderArg) throws OXException;

    /**
     * Clears all messages out of given folder.
     */
    public abstract boolean clearFolder(final String folderArg, boolean hardDelete) throws OXException;

    /**
     * Copies or moves (if <code>move</code> is set) the defined message from source folder to destination folder.
     */
    public abstract String[] copyMessages(String sourceFolder, String destFolder, String[] msgUIDs, boolean move) throws OXException;

    /**
     * Updates the color label stored in message's user flags
     */
    public abstract void updateMessageColorLabel(String folder, String[] msgUID, int newColorLabel) throws OXException;

    /**
     * Updates message's client-alterable system flags (e.g. //SEEN or //ANSWERED). <code>flagVal</code> determines whether the affected
     * flags are set (<code>true</code>) or unset (<code>false</code>).
     */
    public abstract void updateMessageFlags(String folder, String[] msgUID, int flagBits, boolean flagVal) throws OXException;

    /**
     * Gets all updated messages in given folder
     *
     * @param folder The folder fullname
     * @param since The time stamp in UTC milliseconds
     * @param fields The desired fields to fill in returned messages
     * @return All updated messages in given folder
     * @throws OXException If updated messages cannot be returned
     */
    public abstract MailMessage[] getUpdatedMessages(String folder, int[] fields) throws OXException;

    /**
     * Gets all deleted messages in given folder
     *
     * @param folder The folder fullname
     * @param since The time stamp in UTC milliseconds
     * @param fields The desired fields to fill in returned messages
     * @return All deleted messages in given folder
     * @throws OXException If deleted messages cannot be returned
     */
    public abstract MailMessage[] getDeletedMessages(String folder, int[] fields) throws OXException;

    /**
     * Returns an instance of <code>SearchIterator</code> containing the mailbox's default folder
     */
    public abstract SearchIterator<MailFolder> getRootFolders() throws OXException;

    /**
     * Returns an instance of <code>SearchIterator</code> containing the subfolders of given folder
     */
    public abstract SearchIterator<MailFolder> getChildFolders(String parentFolder, boolean all) throws OXException;

    /**
     * Returns the store's folder identfied through given <code>String</code> instance
     */
    public abstract MailFolder getFolder(String folder, boolean checkFolder) throws OXException;

    /**
     * Returns an instance of <code>SearchIterator</code> containing all antecessor folders on path to mailbox's default folder
     */
    public abstract SearchIterator<MailFolder> getPathToDefaultFolder(final String folder) throws OXException;

    /**
     * Closes the interface and releases all resources
     *
     * @param putIntoCache - whether or not to put associated conenction into pool
     */
    public abstract void close(boolean putIntoCache) throws OXException;

    /**
     * Creates a new mail folder described by given <code>MailFolderObject</code> instance
     */
    public abstract String saveFolder(MailFolderDescription mailFolder) throws OXException;

    /**
     * Deletes given folder
     */
    public abstract String deleteFolder(String folder) throws OXException;

    /**
     * Returns user-defined inbox folder
     */
    public abstract String getInboxFolder(int accountId) throws OXException;

    /**
     * Returns user-defined drafts folder
     */
    public abstract String getDraftsFolder(int accountId) throws OXException;

    /**
     * Returns user-defined sent folder
     */
    public abstract String getSentFolder(int accountId) throws OXException;

    /**
     * Returns user-defined spam folder
     */
    public abstract String getSpamFolder(int accountId) throws OXException;

    /**
     * Returns user-defined trash folder
     */
    public abstract String getTrashFolder(int accountId) throws OXException;

    /**
     * Returns user-defined confirmed spam folder
     */
    public abstract String getConfirmedSpamFolder(int accountId) throws OXException;

    /**
     * Returns user-defined confirmed ham folder
     */
    public abstract String getConfirmedHamFolder(int accountId) throws OXException;

    /**
     * Returns user-specific mail configuration
     */
    public abstract MailConfig getMailConfig() throws OXException;

    /**
     * Returns user-specific mail access
     */
    public abstract MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getMailAccess() throws OXException;

    /**
     * Gets the account ID to which the (primary) mail access is connected
     *
     * @return The account ID
     */
    public abstract int getAccountID();

    /**
     * Gets possible warnings.
     *
     * @return Possible warnings
     */
    public abstract Collection<OXException> getWarnings();

    /**
     * Get results of imported mails.
     * Implement this if you want to return detailed informations for mail imports.
     * @return The results.
     */
    public MailImportResult[] getMailImportResults() {
        return new MailImportResult[0];
    }

}
