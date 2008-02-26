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

import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.processing.MimeForward;
import com.openexchange.mail.mime.processing.MimeReply;

/**
 * {@link MailLogicTools} - Extends the mail message/folder storage
 * functionality by replying to/forwarding a mail message.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public interface MailLogicTools {

	/**
	 * Creates a reply message for the message specified by given original UID
	 * which is located in given folder.
	 * <p>
	 * If mailing system deals with common RFC822 messages, this method only
	 * delegates its request to
	 * {@link MimeReply#getReplyMail(javax.mail.internet.MimeMessage, boolean, com.openexchange.session.Session, javax.mail.Session)}.
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
	 * Creates a forward message for the messages specified by given original
	 * UIDs which are located in given folder. If multiple messages are
	 * specified then these messages are forwarded as <b>attachment</b> since
	 * no inline forward is possible.
	 * <p>
	 * If mailing system deals with common RFC822 messages, this method only
	 * delegates its request to
	 * {@link MimeForward#getFowardMail(javax.mail.internet.MimeMessage[], com.openexchange.session.Session)}.
	 * 
	 * @param originalUIDs
	 *            The original messages' UIDs which should be forwarded.
	 * @param folder
	 *            The folder fullname
	 * @return An instance of {@link MailMessage} representing the forward
	 *         message
	 * @throws MailException
	 *             If forward message cannot be generated
	 */
	public MailMessage getFowardMessage(long[] originalUIDs, String folder) throws MailException;

	/**
	 * Releases all resources when closing parental {@link MailConnection}
	 * 
	 * @throws MailException
	 */
	public void releaseResources() throws MailException;
}
