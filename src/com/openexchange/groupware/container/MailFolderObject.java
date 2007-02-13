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

package com.openexchange.groupware.container;

import javax.mail.MessagingException;

import com.openexchange.api2.OXException;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.IMAPFolder;

public class MailFolderObject {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailFolderObject.class);
	
	private String fullName;
	
	private String parentFullName;
	
	private String name;
	
	private ACL[] acls;
	
	private boolean b_acls;
	
	private boolean exists;
	
	private char separator = '0';
	
	private IMAPFolder imapFolder;

	public static final String DEFAULT_IMAP_FOLDER = "default";
	
	public MailFolderObject() {
		super();
	}
	
	public MailFolderObject(final String fullName, final boolean exists) {
		super();
		this.exists = exists;
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(final String fullName) {
		this.fullName = fullName;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getParentFullName() {
		return parentFullName;
	}

	public void setParentFullName(final String parentFullName) {
		this.parentFullName = parentFullName;
	}

	public ACL[] getACL() {
		return acls;
	}
	
	public boolean containsACLs() {
		return b_acls;
	}

	public void setACL(final ACL[] acls) {
		this.acls = acls;
		b_acls = true;
	}
	
	public void removeACL() {
		this.acls = null;
		b_acls = false;
	}
	
	public boolean exists() {
		return exists;
	}

	public char getSeparator() throws OXException {
		if (separator != '0')
			return separator;
		else
			throw new OXException("IMAP delimiter not specified!");
	}

	public void setSeparator(final char separator) {
		this.separator = separator;
	}

	public IMAPFolder getImapFolder() {
		return imapFolder;
	}

	public void setImapFolder(final IMAPFolder imapFolder) {
		this.imapFolder = imapFolder;
		try {
			setSeparator(imapFolder.getSeparator());
		} catch (MessagingException e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
