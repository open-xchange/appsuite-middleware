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

package com.openexchange.api2;

import javax.mail.Message;

import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.MailFolderObject;
import com.openexchange.groupware.container.mail.JSONMessageAttachmentObject;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.groupware.upload.UploadEvent;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * MailInterface
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public interface MailInterface {

	/**
	 * Returns all message counts in specified folder in an <code>int</code>
	 * array as follows: <code>0</code>: Message Count, <code>1</code>:
	 * New Message Count, <code>2</code>: Unread MessageCount, <code>3</code>:
	 * Deleted Message Count
	 */
	public int[] getAllMessageCount(String folder) throws OXException;

	/**
	 * Returns the number of messages in folder
	 */
	public int getMessageCount(String folder) throws OXException;

	/**
	 * Returns the number of new messages in folder
	 */
	public int getNewMessageCount(String folder) throws OXException;

	/**
	 * Returns the number of unread messages in folder
	 */
	public int getUnreadMessageCount(String folder) throws OXException;

	/**
	 * Returns the number messages which are marked for deletion in folder
	 */
	public int getDeletedMessageCount(String folder) throws OXException;

	/**
	 * 
	 * @return both quota limit and quota usage in an array with length set to 2
	 * @throws OXException
	 */
	public long[] getQuota() throws OXException;

	/**
	 * Returns the quota limit
	 */
	public long getQuotaLimit() throws OXException;

	/**
	 * Returns the current quota usage
	 */
	public long getQuotaUsage() throws OXException;

	/**
	 * Returns an instance of <code>SearchIterator</code> containing max.
	 * <code>limit</code> new (unseen) messages located in given folder.
	 */
	public SearchIterator getNewMessages(String folder, int sortCol, int order, int[] fields, int limit)
			throws OXException;

	/**
	 * Returns an instance of <code>SearchIterator</code> containing all
	 * messages located in given folder.
	 */
	public SearchIterator getAllMessages(String folder, int sortCol, int order, int[] fields) throws OXException;

	/**
	 * Returns an instance of <code>SearchIterator</code> containing a
	 * selection of messages located in given folder. <code>fromToIndices</code>
	 * can define a range of messages that should be returned. Moreover
	 * <code>searchCols</code> and <code>searchPatterns</code> defines a
	 * search pattern to further confine returned messages.
	 */
	public SearchIterator getMessages(String folder, int[] fromToIndices, int sortCol, int order, int[] searchCols,
			String[] searchPatterns, boolean linkSearchTermsWithOR, int[] fields) throws OXException;

	/**
	 * Returns a thread-view-sorted instance of <code>SearchIterator</code>
	 * containing all messages located in given folder.
	 */
	public SearchIterator getAllThreadedMessages(String folder, int[] fields) throws OXException;

	/**
	 * Returns a thread-view-sorted instance of <code>SearchIterator</code>
	 * containing a selection of messages located in given folder.
	 * <code>fromToIndices</code> can define a range of messages that should
	 * be returned. Moreover <code>searchCols</code> and
	 * <code>searchPatterns</code> defines a search pattern to further confine
	 * returned messages.
	 */
	public SearchIterator getThreadedMessages(String folder, int[] fromToIndices, int[] searchCols,
			String[] searchPatterns, boolean linkSearchTermsWithOR, int[] fields) throws OXException;

	/**
	 * Resturn an instance of <code>SearchIterator</code> containing all
	 * messages located in given folder which match the given search pattern(s).
	 */
	public SearchIterator searchMessages(final String folder, final int[] searchCols, final String[] searchPatterns,
			final boolean linkWithOR, int[] fields) throws OXException;

	/**
	 * Returns the an array of messages located in given folder. If
	 * <code>fromToUID</code> is not <code>null</code> only messages fitting
	 * into uid range will be returned.
	 */
	public Message[] getMessageList(String folder, long[] uids, int[] fields) throws OXException;

	/**
	 * Fetches the message identified through given uid from store located in
	 * given folder. <code>seen</code> specifies whether to mark the message
	 * as seen or not.
	 * 
	 */
	public Message getMessage(String folder, long msgUID) throws OXException;

	/**
	 * Returns a message's attachment located at given
	 * <code>attachmentPosition</code> wrapped by an instance of
	 * <code>JSONMessageAttachmentObject</code> for a convenient access to its
	 * attributes and content.
	 * 
	 * @param displayVersion
	 *            <code>true</code> if returned object is for display purpose;
	 *            otherwise <code>false</code>
	 */
	public JSONMessageAttachmentObject getMessageAttachment(String folder, long msgUID, String attachmentPosition,
			boolean displayVersion) throws OXException;

	/**
	 * Returns a message's inline image located identified with given
	 * <code>cid</code> wrapped by an instance of
	 * <code>JSONMessageAttachmentObject</code> for a convenient access to its
	 * attributes and content.
	 */
	public JSONMessageAttachmentObject getMessageImage(String folder, long msgUID, String cid) throws OXException;

	/**
	 * Saves the versit object specified through given
	 * <code>partIdentifier</code> and returns its corresponding array of
	 * <code>CommonObject</code> instances which are either of type
	 * <code>AppointmentObject</code>, <code>Task</code> or
	 * <code>ContactObject </code>
	 */
	public CommonObject[] saveVersitAttachment(String folder, long msgUID, String partIdentifier) throws OXException;

	/**
	 * Returns message's uid in associated folder
	 */
	public long getMessageUID(Message msg) throws OXException;

	/**
	 * Sends a read acknowledgement to given message
	 */
	public void sendReceiptAck(String folder, long msgUID, String fromAddr) throws OXException;

	/**
	 * Sends a message described through given instance of <code>msgObj</code>
	 * and its possible file attachments contained in given instance of
	 * <code>uploadEvent</code>.
	 */
	public String sendMessage(JSONMessageObject msgObj, UploadEvent uploadEvent, int sendType) throws OXException;

	/**
	 * Creates an instance of <code>JSONMessageObject</code> which contains
	 * the initial reply content of the message identifed through
	 * <code>replyMsgUID</code>. <code>replyToAll</code> defines whether to
	 * reply to all involved entities or just to main sender. <b>NOTE:</b>This
	 * method is intended to support Open-Xchange GUI's display onyl and does
	 * not really send the reply.
	 */
	public JSONMessageObject getReplyMessageForDisplay(String folder, long replyMsgUID, boolean replyToAll)
			throws OXException;

	/**
	 * Creates an instance of <code>JSONMessageObject</code> which contains
	 * the initial forward content of the message identifed through
	 * <code>fowardMsgUID</code>. <b>NOTE:</b>This method is intended to
	 * support Open-Xchange GUI's display onyl and does not really send the
	 * forward.
	 */
	public JSONMessageObject getForwardMessageForDisplay(String folder, long fowardMsgUID) throws OXException;

	/**
	 * Deletes the message located in given folder corresponding to given
	 * <code>msgUID</code>
	 */
	public boolean deleteMessages(String folder, long[] msgUIDs, boolean hardDelete) throws OXException;

	/**
	 * Clears all messages out of given folder. <b>NOTE</b> this is a hard
	 * delete, thus no copies are created
	 */
	public boolean clearFolder(final String folderArg) throws OXException;

	/**
	 * Copies or moves (if <code>move</code> is set) the defined message from
	 * source folder to destination folder.
	 */
	public long[] copyMessages(String sourceFolder, String destFolder, long[] msgUIDs, boolean move) throws OXException;

	/**
	 * Updates the color label stored in message's user flags
	 */
	public Message[] updateMessageColorLabel(String folder, long[] msgUID, int newColorLabel) throws OXException;

	/**
	 * Updates message's client-alterable system flags (e.g. //SEEN or
	 * //ANSWERED). <code>flagVal</code> determines whether the affected flags
	 * are set (<code>true</code>) or unset (<code>false</code>).
	 */
	public Message[] updateMessageFlags(String folder, long[] msgUID, int flagBits, boolean flagVal) throws OXException;

	/**
	 * Checks if user-defines default folder exist
	 */
	public void checkDefaultFolders(String[] defaultFolderNames) throws OXException;

	/**
	 * Returns an instance of <code>SearchIterator</code> containing the
	 * mailbox's default folder
	 */
	public SearchIterator getRootFolders() throws OXException;

	/**
	 * Returns an instance of <code>SearchIterator</code> containing the
	 * subfolders of given folder
	 */
	public SearchIterator getChildFolders(String parentFolder, boolean all) throws OXException;

	/**
	 * Returns an instance of <code>SearchIterator</code> containing all
	 * folders located beneath user's default folder regardless of their
	 * subscription status
	 */
	public SearchIterator getAllFolders() throws OXException;

	/**
	 * Returns the store's folder identfied through given <code>String</code>
	 * instance
	 */
	public MailFolderObject getFolder(String folder, boolean checkFolder) throws OXException;

	/**
	 * Returns an instance of <code>SearchIterator</code> containing all
	 * antecessor folders on path to mailbox's default folder
	 */
	public SearchIterator getPathToDefaultFolder(final String folder) throws OXException;

	/**
	 * Closes the interface and releases all resources
	 * 
	 * @param putIntoCache -
	 *            whether or not to put associated conenction into pool
	 */
	public void close(boolean putIntoCache) throws OXException;

	/**
	 * Creates a new imap folder described by given
	 * <code>MailFolderObject</code> instance
	 */
	public String saveFolder(MailFolderObject folderObj) throws OXException;

	/**
	 * Deletes given folder
	 */
	public String deleteFolder(String folder) throws OXException;

	/**
	 * Returns user-defined inbox folder
	 */
	public String getInboxFolder() throws OXException;

	/**
	 * Returns user-defined drafts folder
	 */
	public String getDraftsFolder() throws OXException;

	/**
	 * Returns user-defined sent folder
	 */
	public String getSentFolder() throws OXException;

	/**
	 * Returns user-defined spam folder
	 */
	public String getSpamFolder() throws OXException;

	/**
	 * Returns user-defined trash folder
	 */
	public String getTrashFolder() throws OXException;

	/**
	 * Returns user-defined confirmed spam folder
	 */
	public String getConfirmedSpamFolder() throws OXException;

	/**
	 * Returns user-defined confirmed ham folder
	 */
	public String getConfirmedHamFolder() throws OXException;

}
