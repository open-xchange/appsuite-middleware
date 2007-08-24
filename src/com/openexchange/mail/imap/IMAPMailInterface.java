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

package com.openexchange.mail.imap;

import javax.mail.Message;

import com.openexchange.api2.MailInterface;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.MailFolderObject;
import com.openexchange.groupware.container.mail.JSONMessageAttachmentObject;
import com.openexchange.groupware.container.mail.JSONMessageObject;
import com.openexchange.groupware.upload.UploadEvent;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * IMAPMailInterface
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class IMAPMailInterface implements MailInterface {

	/**
	 * 
	 */
	public IMAPMailInterface() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#checkDefaultFolders(java.lang.String[])
	 */
	public void checkDefaultFolders(String[] defaultFolderNames) throws OXException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#clearFolder(java.lang.String)
	 */
	public boolean clearFolder(String folder) throws OXException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#close(boolean)
	 */
	public void close(boolean putIntoCache) throws OXException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#copyMessages(java.lang.String, java.lang.String, long[], boolean)
	 */
	public long[] copyMessages(String sourceFolder, String destFolder, long[] msgUIDs, boolean move) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#deleteFolder(java.lang.String)
	 */
	public String deleteFolder(String folder) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#deleteMessages(java.lang.String, long[], boolean)
	 */
	public boolean deleteMessages(String folder, long[] msgUIDs, boolean hardDelete) throws OXException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getAllFolders()
	 */
	public SearchIterator getAllFolders() throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getAllMessageCount(java.lang.String)
	 */
	public int[] getAllMessageCount(String folder) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getAllMessages(java.lang.String, int, int, int[])
	 */
	public SearchIterator getAllMessages(String folder, int sortCol, int order, int[] fields) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getAllThreadedMessages(java.lang.String, int[])
	 */
	public SearchIterator getAllThreadedMessages(String folder, int[] fields) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getChildFolders(java.lang.String, boolean)
	 */
	public SearchIterator getChildFolders(String parentFolder, boolean all) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getConfirmedHamFolder()
	 */
	public String getConfirmedHamFolder() throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getConfirmedSpamFolder()
	 */
	public String getConfirmedSpamFolder() throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getDeletedMessageCount(java.lang.String)
	 */
	public int getDeletedMessageCount(String folder) throws OXException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getDraftsFolder()
	 */
	public String getDraftsFolder() throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getFolder(java.lang.String, boolean)
	 */
	public MailFolderObject getFolder(String folder, boolean checkFolder) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getForwardMessageForDisplay(java.lang.String, long)
	 */
	public JSONMessageObject getForwardMessageForDisplay(String folder, long fowardMsgUID) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getInboxFolder()
	 */
	public String getInboxFolder() throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getMessage(java.lang.String, long)
	 */
	public Message getMessage(String folder, long msgUID) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getMessageAttachment(java.lang.String, long, java.lang.String, boolean)
	 */
	public JSONMessageAttachmentObject getMessageAttachment(String folder, long msgUID, String attachmentPosition,
			boolean displayVersion) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getMessageCount(java.lang.String)
	 */
	public int getMessageCount(String folder) throws OXException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getMessageImage(java.lang.String, long, java.lang.String)
	 */
	public JSONMessageAttachmentObject getMessageImage(String folder, long msgUID, String cid) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getMessageList(java.lang.String, long[], int[])
	 */
	public Message[] getMessageList(String folder, long[] uids, int[] fields) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getMessageUID(javax.mail.Message)
	 */
	public long getMessageUID(Message msg) throws OXException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getMessages(java.lang.String, int[], int, int, int[], java.lang.String[], boolean, int[])
	 */
	public SearchIterator getMessages(String folder, int[] fromToIndices, int sortCol, int order, int[] searchCols,
			String[] searchPatterns, boolean linkSearchTermsWithOR, int[] fields) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getNewMessageCount(java.lang.String)
	 */
	public int getNewMessageCount(String folder) throws OXException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getNewMessages(java.lang.String, int, int, int[], int)
	 */
	public SearchIterator getNewMessages(String folder, int sortCol, int order, int[] fields, int limit)
			throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getPathToDefaultFolder(java.lang.String)
	 */
	public SearchIterator getPathToDefaultFolder(String folder) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getQuota()
	 */
	public long[] getQuota() throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getQuotaLimit()
	 */
	public long getQuotaLimit() throws OXException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getQuotaUsage()
	 */
	public long getQuotaUsage() throws OXException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getReplyMessageForDisplay(java.lang.String, long, boolean)
	 */
	public JSONMessageObject getReplyMessageForDisplay(String folder, long replyMsgUID, boolean replyToAll)
			throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getRootFolders()
	 */
	public SearchIterator getRootFolders() throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getSentFolder()
	 */
	public String getSentFolder() throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getSpamFolder()
	 */
	public String getSpamFolder() throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getThreadedMessages(java.lang.String, int[], int[], java.lang.String[], boolean, int[])
	 */
	public SearchIterator getThreadedMessages(String folder, int[] fromToIndices, int[] searchCols,
			String[] searchPatterns, boolean linkSearchTermsWithOR, int[] fields) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getTrashFolder()
	 */
	public String getTrashFolder() throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#getUnreadMessageCount(java.lang.String)
	 */
	public int getUnreadMessageCount(String folder) throws OXException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#saveFolder(com.openexchange.groupware.container.MailFolderObject)
	 */
	public String saveFolder(MailFolderObject folderObj) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#saveVersitAttachment(java.lang.String, long, java.lang.String)
	 */
	public CommonObject[] saveVersitAttachment(String folder, long msgUID, String partIdentifier) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#searchMessages(java.lang.String, int[], java.lang.String[], boolean, int[])
	 */
	public SearchIterator searchMessages(String folder, int[] searchCols, String[] searchPatterns, boolean linkWithOR,
			int[] fields) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#sendMessage(com.openexchange.groupware.container.mail.JSONMessageObject, com.openexchange.groupware.upload.UploadEvent, int)
	 */
	public String sendMessage(JSONMessageObject msgObj, UploadEvent uploadEvent, int sendType) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#sendReceiptAck(java.lang.String, long, java.lang.String)
	 */
	public void sendReceiptAck(String folder, long msgUID, String fromAddr) throws OXException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#updateMessageColorLabel(java.lang.String, long[], int)
	 */
	public Message[] updateMessageColorLabel(String folder, long[] msgUID, int newColorLabel) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.openexchange.api2.MailInterface#updateMessageFlags(java.lang.String, long[], int, boolean)
	 */
	public Message[] updateMessageFlags(String folder, long[] msgUID, int flagBits, boolean flagVal) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

}
