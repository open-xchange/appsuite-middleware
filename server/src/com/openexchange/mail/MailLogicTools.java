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

import com.openexchange.groupware.container.CommonObject;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link MailLogicTools} - Extends the mail message/folder storage
 * functionality by requesting quota informations, replying to/forwarding a mail
 * message and storing attached versit (ical & vcard) objects.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public interface MailLogicTools {

	/**
	 * Constant which indicates unlimited quota
	 * 
	 * @value <code>-1</code>
	 */
	public static final int UNLIMITED_QUOTA = -1;

	/**
	 * Creates a reply message for the message specified by given original UID
	 * which is located in given folder
	 * 
	 * @param originalUID
	 *            The original message's UID
	 * @param folder
	 *            The folder fullname
	 * @param replyAll
	 *            <code>true</code> to reply to all recipients; otherwise
	 *            <code>false</code>
	 * @return An instance of {@link MailMessage} representing the reply message
	 * @throws MailException
	 *             If reply message cannot be generated
	 */
	public MailMessage getReplyMessage(long originalUID, String folder, boolean replyAll) throws MailException;

	/**
	 * Creates a forward message for the message specified by given original UID
	 * which is located in given folder
	 * 
	 * @param originalUID
	 *            The original message's UID
	 * @param folder
	 *            The folder fullname
	 * @return An instance of {@link MailMessage} representing the forward
	 *         message
	 * @throws MailException
	 *             If forward message cannot be generated
	 */
	public MailMessage getFowardMessage(long originalUID, String folder) throws MailException;

	/**
	 * Detects both quota limit and quota usage on given mailbox's folder
	 * gathered in an array of <code>long</code>. The first value is the
	 * quota limit and the second is the quota usage.
	 * 
	 * @param folder
	 *            The folder fullname (if <code>null</code> <i>"INBOX"</i> is
	 *            used)
	 * @return Both quota limit and quota usage
	 * @throws MailException
	 *             If quota limit and/or quote usage cannot be determined
	 */
	public long[] getQuota(String folder) throws MailException;

	/**
	 * Saves the versit attachment with given sequence ID in the message located
	 * in given folder with given UID.
	 * 
	 * @param folder
	 *            The folder fullname
	 * @param msgUID
	 *            The message UID
	 * @param sequenceId
	 *            The versit attachment's sequence ID inside the message
	 * @return An array of {@link CommonObject} instances representing the saved
	 *         versit objects
	 * @throws MailException
	 *             If versit attachment cannot be saved
	 */
	public CommonObject[] saveVersitAttachment(String folder, long msgUID, String sequenceId) throws MailException;

	/**
	 * Releases all resources when closing parental {@link MailConnection}
	 * 
	 * @throws MailException
	 */
	public void releaseResources() throws MailException;
}
