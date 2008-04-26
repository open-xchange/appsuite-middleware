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

package com.openexchange.mail.mime;

import java.util.NoSuchElementException;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;

/**
 * {@link FullnameFolder} - A {@link Folder} implementation whose only purpose
 * is to provide fullname, separator character and UIDs.
 * <p>
 * All other methods will throw an {@link UnsupportedOperationException}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class FullnameFolder extends Folder implements UIDFolder {

	private final char separator;

	private final String fullname;

	private final boolean hasAll;

	private final long[] uids;

	/**
	 * Initializes a new {@link FullnameFolder}
	 * 
	 * @param fullname
	 *            The folder's fullname
	 * @param separator
	 *            The folder's separator character
	 * @param uids
	 *            The UIDs corresponding to appropriate message numbers such
	 *            that uids[0] is the UID of message numbered with 1 and so on.
	 */
	public FullnameFolder(final String fullname, final char separator, final long[] uids) {
		super(null);
		if (null == uids) {
			throw new IllegalArgumentException("uids is null");
		}
		this.fullname = fullname;
		this.separator = separator;
		this.uids = uids;
		hasAll = true;
	}

	/**
	 * Initializes a new {@link FullnameFolder}
	 * 
	 * @param fullname
	 *            The folder's fullname
	 * @param separator
	 *            The folder's separator character
	 * @param uid
	 *            The UID corresponding to appropriate message.
	 */
	public FullnameFolder(final String fullname, final char separator, final long uid) {
		super(null);
		if (-1 == uid) {
			throw new IllegalArgumentException("uid is invalid");
		}
		this.fullname = fullname;
		this.separator = separator;
		this.uids = new long[] { uid };
		hasAll = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#appendMessages(javax.mail.Message[])
	 */
	@Override
	public void appendMessages(final Message[] msgs) throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.appendMessages()");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#close(boolean)
	 */
	@Override
	public void close(final boolean expunge) throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.close()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#create(int)
	 */
	@Override
	public boolean create(final int type) throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.create()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#delete(boolean)
	 */
	@Override
	public boolean delete(final boolean recurse) throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.delete()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#exists()
	 */
	@Override
	public boolean exists() throws MessagingException {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#expunge()
	 */
	@Override
	public Message[] expunge() throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.expunge()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getFolder(java.lang.String)
	 */
	@Override
	public Folder getFolder(final String name) throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.getFolder()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getFullName()
	 */
	@Override
	public String getFullName() {
		return fullname;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getMessage(int)
	 */
	@Override
	public Message getMessage(final int msgnum) throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.getMessage()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getMessageCount()
	 */
	@Override
	public int getMessageCount() throws MessagingException {
		if (hasAll) {
			return uids.length;
		}
		throw new UnsupportedOperationException("FullnameFolder.getMessageCount()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getName()
	 */
	@Override
	public String getName() {
		throw new UnsupportedOperationException("FullnameFolder.getName()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getParent()
	 */
	@Override
	public Folder getParent() throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.getParent()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getPermanentFlags()
	 */
	@Override
	public Flags getPermanentFlags() {
		throw new UnsupportedOperationException("FullnameFolder.getPermanentFlags()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getSeparator()
	 */
	@Override
	public char getSeparator() throws MessagingException {
		return separator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#getType()
	 */
	@Override
	public int getType() throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.getType()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#hasNewMessages()
	 */
	@Override
	public boolean hasNewMessages() throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.hasNewMessages()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#isOpen()
	 */
	@Override
	public boolean isOpen() {
		throw new UnsupportedOperationException("FullnameFolder.isOpen()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#list(java.lang.String)
	 */
	@Override
	public Folder[] list(final String pattern) throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.list()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#open(int)
	 */
	@Override
	public void open(final int mode) throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.open()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.mail.Folder#renameTo(javax.mail.Folder)
	 */
	@Override
	public boolean renameTo(final Folder f) throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.renameTo()");
	}

	public Message getMessageByUID(final long uid) throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.getMessageByUID()");
	}

	public Message[] getMessagesByUID(final long[] uids) throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.getMessagesByUID()");
	}

	public Message[] getMessagesByUID(final long start, final long end) throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.getMessagesByUID()");
	}

	public long getUID(final Message message) throws MessagingException {
		if (hasAll) {
			if (uids.length < message.getMessageNumber()) {
				throw new NoSuchElementException("Message does not belong to folder");
			}
			return uids[message.getMessageNumber() - 1];
		}
		return uids[0];
	}

	public long getUIDValidity() throws MessagingException {
		throw new UnsupportedOperationException("FullnameFolder.getUIDValidity()");
	}

}
