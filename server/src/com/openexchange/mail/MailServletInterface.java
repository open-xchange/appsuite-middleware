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

import com.openexchange.api2.MailInterfaceMonitor;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.usersetting.UserSettingMail;
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
     * @throws MailException
     */
    public static final MailServletInterface getInstance(final Session session) throws MailException {
        return new MailServletInterfaceImpl(session);
    }

    public static final MailInterfaceMonitor mailInterfaceMonitor = new MailInterfaceMonitor();

    /**
     * Returns all message counts in specified folder in an <code>int</code> array as follows: <code>0</code>: Message Count, <code>1</code>
     * : New Message Count, <code>2</code>: Unread MessageCount, <code>3</code>: Deleted Message Count
     */
    public abstract int[] getAllMessageCount(String folder) throws MailException;

    /**
     * Returns the number of messages in folder
     */
    public abstract int getMessageCount(String folder) throws MailException;

    /**
     * Returns the number of new messages in folder
     */
    public abstract int getNewMessageCount(String folder) throws MailException;

    /**
     * Returns the number of unread messages in folder
     */
    public abstract int getUnreadMessageCount(String folder) throws MailException;

    /**
     * Returns the number messages which are marked for deletion in folder
     */
    public abstract int getDeletedMessageCount(String folder) throws MailException;

    /**
     * Gets both quota limit and quota usage in an array with length set to <code>2</code> for each resource type
     * 
     * @param types The resource types; {@link #QUOTA_RESOURCE_STORAGE} or {@link #QUOTA_RESOURCE_MESSAGE}
     * @return Both quota limit and quota usage in an array with length set to <code>2</code> for each resource type
     * @throws MailException If quotas cannot be retrieved
     */
    public abstract long[][] getQuotas(int[] types) throws MailException;

    /**
     * Returns the quota limit
     */
    public abstract long getQuotaLimit(int type) throws MailException;

    /**
     * Returns the current quota usage
     */
    public abstract long getQuotaUsage(int type) throws MailException;

    /**
     * Returns an instance of <code>SearchIterator</code> containing max. <code>limit</code> new (unseen) messages located in given folder.
     */
    public abstract SearchIterator<MailMessage> getNewMessages(String folder, int sortCol, int order, int[] fields, int limit) throws MailException;

    /**
     * Returns an instance of <code>SearchIterator</code> containing all messages located in given folder.
     */
    public abstract SearchIterator<MailMessage> getAllMessages(String folder, int sortCol, int order, int[] fields, int[] fromToIndices) throws MailException;

    /**
     * Returns an instance of <code>SearchIterator</code> containing a selection of messages located in given folder.
     * <code>fromToIndices</code> can define a range of messages that should be returned. Moreover <code>searchCols</code> and
     * <code>searchPatterns</code> defines a search pattern to further confine returned messages.
     */
    public abstract SearchIterator<MailMessage> getMessages(String folder, int[] fromToIndices, int sortCol, int order, int[] searchCols, String[] searchPatterns, boolean linkSearchTermsWithOR, int[] fields) throws MailException;

    /**
     * Returns a thread-view-sorted instance of <code>SearchIterator</code> containing all messages located in given folder.
     */
    public abstract SearchIterator<MailMessage> getAllThreadedMessages(String folder, int order, int[] fields, int[] fromToIndices) throws MailException;

    /**
     * Returns a thread-view-sorted instance of <code>SearchIterator</code> containing a selection of messages located in given folder.
     * <code>fromToIndices</code> can define a range of messages that should be returned. Moreover <code>searchCols</code> and
     * <code>searchPatterns</code> defines a search pattern to further confine returned messages.
     */
    public abstract SearchIterator<MailMessage> getThreadedMessages(String folder, int[] fromToIndices, int order, int[] searchCols, String[] searchPatterns, boolean linkSearchTermsWithOR, int[] fields) throws MailException;

    /**
     * Returns the an array of messages located in given folder. If <code>fromToUID</code> is not <code>null</code> only messages fitting
     * into uid range will be returned.
     */
    public abstract MailMessage[] getMessageList(String folder, String[] uids, int[] fields) throws MailException;

    /**
     * Gets the mail identified through given ID from store located in given folder.
     * 
     * @param folder The folder path
     * @param msgUID The mail ID
     * @return The mail identified through given ID from store located in given folder.
     * @throws MailException If mail cannot be fetched from store
     */
    public abstract MailMessage getMessage(String folder, String msgUID) throws MailException;

    /**
     * Returns a message's attachment located at given <code>attachmentPosition</code> wrapped by an instance of
     * <code>JSONMessageAttachmentObject</code> for a convenient access to its attributes and content.
     * 
     * @param displayVersion <code>true</code> if returned object is for display purpose; otherwise <code>false</code>
     */
    public abstract MailPart getMessageAttachment(String folder, String msgUID, String attachmentPosition, boolean displayVersion) throws MailException;

    /**
     * Returns a message's inline image located identified with given <code>cid</code> wrapped by an instance of
     * <code>JSONMessageAttachmentObject</code> for a convenient access to its attributes and content.
     */
    public abstract MailPart getMessageImage(String folder, String msgUID, String cid) throws MailException;

    /**
     * Saves specified draft mail.
     * <p>
     * If specified draft mail holds a reference to an existing draft mail - {@link MailMessage#getMsgref()} is not <code>null</code> - then
     * the referenced draft mail shall be replaced.
     * 
     * @param draftMail The draft mail
     * @param autosave <code>true</code> to indicate an auto-save operation; otherwise <code>false</code>
     * @return The stored draft's mail path
     * @throws MailException
     */
    public abstract String saveDraft(ComposedMailMessage draftMail, boolean autosave, int accountId) throws MailException;

    /**
     * Sends a read acknowledgment to given message
     */
    public abstract void sendReceiptAck(String folder, String msgUID, String fromAddr) throws MailException;

    /**
     * Sends a message described through given instance of <code>msgObj</code> and its possible file attachments contained in given instance
     * of <code>uploadEvent</code>.
     */
    public abstract String sendMessage(ComposedMailMessage transportMail, ComposeType sendType, int accountId) throws MailException;

    /**
     * Creates an instance of <code>JSONMessageObject</code> which contains the initial reply content of the message identifed through
     * <code>replyMsgUID</code>. <code>replyToAll</code> defines whether to reply to all involved entities or just to main sender.
     * <b>NOTE:</b>This method is intended to support Open-Xchange GUI's display onyl and does not really send the reply.
     */
    public abstract MailMessage getReplyMessageForDisplay(String folder, String replyMsgUID, boolean replyToAll, UserSettingMail usm) throws MailException;

    /**
     * Creates an instance of <code>JSONMessageObject</code> which contains the initial forward content of the message identifed through
     * <code>fowardMsgUID</code>. <b>NOTE:</b>This method is intended to support Open-Xchange GUI's display onyl and does not really send
     * the forward.
     */
    public abstract MailMessage getForwardMessageForDisplay(String[] folders, String[] fowardMsgUIDs, UserSettingMail usm) throws MailException;

    /**
     * Deletes the message located in given folder corresponding to given <code>msgUID</code>
     */
    public abstract boolean deleteMessages(String folder, String[] msgUIDs, boolean hardDelete) throws MailException;

    /**
     * Clears all messages out of given folder. <b>NOTE</b> this is a hard delete, thus no copies are created
     */
    public abstract boolean clearFolder(final String folderArg) throws MailException;

    /**
     * Copies or moves (if <code>move</code> is set) the defined message from source folder to destination folder.
     */
    public abstract String[] copyMessages(String sourceFolder, String destFolder, String[] msgUIDs, boolean move) throws MailException;

    /**
     * Updates the color label stored in message's user flags
     */
    public abstract void updateMessageColorLabel(String folder, String[] msgUID, int newColorLabel) throws MailException;

    /**
     * Updates message's client-alterable system flags (e.g. //SEEN or //ANSWERED). <code>flagVal</code> determines whether the affected
     * flags are set (<code>true</code>) or unset (<code>false</code>).
     */
    public abstract void updateMessageFlags(String folder, String[] msgUID, int flagBits, boolean flagVal) throws MailException;

    /**
     * Returns an instance of <code>SearchIterator</code> containing the mailbox's default folder
     */
    public abstract SearchIterator<MailFolder> getRootFolders() throws MailException;

    /**
     * Returns an instance of <code>SearchIterator</code> containing the subfolders of given folder
     */
    public abstract SearchIterator<MailFolder> getChildFolders(String parentFolder, boolean all) throws MailException;

    /**
     * Returns the store's folder identfied through given <code>String</code> instance
     */
    public abstract MailFolder getFolder(String folder, boolean checkFolder) throws MailException;

    /**
     * Returns an instance of <code>SearchIterator</code> containing all antecessor folders on path to mailbox's default folder
     */
    public abstract SearchIterator<MailFolder> getPathToDefaultFolder(final String folder) throws MailException;

    /**
     * Closes the interface and releases all resources
     * 
     * @param putIntoCache - whether or not to put associated conenction into pool
     */
    public abstract void close(boolean putIntoCache) throws MailException;

    /**
     * Creates a new mail folder described by given <code>MailFolderObject</code> instance
     */
    public abstract String saveFolder(MailFolderDescription mailFolder) throws MailException;

    /**
     * Deletes given folder
     */
    public abstract String deleteFolder(String folder) throws MailException;

    /**
     * Returns user-defined inbox folder
     */
    public abstract String getInboxFolder(int accountId) throws MailException;

    /**
     * Returns user-defined drafts folder
     */
    public abstract String getDraftsFolder(int accountId) throws MailException;

    /**
     * Returns user-defined sent folder
     */
    public abstract String getSentFolder(int accountId) throws MailException;

    /**
     * Returns user-defined spam folder
     */
    public abstract String getSpamFolder(int accountId) throws MailException;

    /**
     * Returns user-defined trash folder
     */
    public abstract String getTrashFolder(int accountId) throws MailException;

    /**
     * Returns user-defined confirmed spam folder
     */
    public abstract String getConfirmedSpamFolder(int accountId) throws MailException;

    /**
     * Returns user-defined confirmed ham folder
     */
    public abstract String getConfirmedHamFolder(int accountId) throws MailException;

    /**
     * Returns user-specific mail configuration
     */
    public abstract MailConfig getMailConfig() throws MailException;

    /**
     * Gets the account ID to which the (primary) mail access is connected
     * 
     * @return The account ID
     */
    public abstract int getAccountID();
}
