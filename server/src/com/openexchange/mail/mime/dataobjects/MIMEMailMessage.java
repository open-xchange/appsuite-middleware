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

package com.openexchange.mail.mime.dataobjects;

import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.mail.internet.MimeMessage;

import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.converters.MIMEMessageConverter;

/**
 * {@link MIMEMailMessage} - A subclass of {@link MailMessage} to support MIME
 * messages (as per RFC822).
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MIMEMailMessage extends MailMessage {

	private static final long serialVersionUID = 4593386724062676753L;

	private MailPart mailPart;

	private long uid = -1L;

	private int unreadMessages;

	/**
	 * Constructor - Constructs an empty mail message
	 */
	public MIMEMailMessage() {
		super();
		this.mailPart = new MIMEMailPart(null);
	}

	/**
	 * Constructor - Only applies specified message, but does not set any
	 * attributes
	 * 
	 * @throws MailException
	 *             If message's content cannot be applied
	 */
	public MIMEMailMessage(final MimeMessage msg) throws MailException {
		super();
		this.mailPart = MIMEMessageConverter.convertPart(msg);
	}

	/**
	 * Sets this mail message's content
	 * 
	 * @param msg
	 *            The MIME message
	 * @throws MailException
	 *             If parsing mime message fails
	 */
	public void setContent(final MimeMessage msg) throws MailException {
		this.mailPart = msg == null ? new MIMEMailPart(null) : MIMEMessageConverter.convertPart(msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailMessage#getMailId()
	 */
	@Override
	public long getMailId() {
		/*
		 * Mail ID is equal to UID in IMAP
		 */
		return uid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailMessage#setMailId(long)
	 */
	@Override
	public void setMailId(final long id) {
		/*
		 * Mail ID is equal to UID in IMAP
		 */
		this.uid = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#getContent()
	 */
	@Override
	public Object getContent() throws MailException {
		return mailPart.getContent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#getDataHandler()
	 */
	@Override
	public DataHandler getDataHandler() throws MailException {
		return mailPart.getDataHandler();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#getEnclosedCount()
	 */
	@Override
	public int getEnclosedCount() throws MailException {
		return mailPart.getEnclosedCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#getEnclosedMailContent(int)
	 */
	@Override
	public MailPart getEnclosedMailPart(final int index) throws MailException {
		return mailPart.getEnclosedMailPart(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws MailException {
		return mailPart.getInputStream();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailPart#writeTo(java.io.OutputStream)
	 */
	@Override
	public void writeTo(final OutputStream out) throws MailException {
		mailPart.writeTo(out);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailPart#loadContent()
	 */
	@Override
	public void loadContent() throws MailException {
		mailPart.loadContent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.dataobjects.MailContent#prepareForCaching()
	 */
	@Override
	public void prepareForCaching() {
		mailPart.prepareForCaching();
	}

	@Override
	public void setUnreadMessages(final int unreadMessages) {
		this.unreadMessages = unreadMessages;
	}

	@Override
	public int getUnreadMessages() {
		return unreadMessages;
	}

}
