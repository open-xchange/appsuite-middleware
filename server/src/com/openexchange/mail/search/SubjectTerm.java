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

package com.openexchange.mail.search;

import java.util.Collection;
import java.util.Locale;

import javax.mail.Message;
import javax.mail.MessagingException;

import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MIMEMailException;

/**
 * {@link SubjectTerm}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SubjectTerm extends SearchTerm<String> {

	private static final long serialVersionUID = 1462060457742619720L;

	private final String unicodeSubject;

	/**
	 * Initializes a new {@link SubjectTerm}
	 */
	public SubjectTerm(final String unicodeSubject) {
		super();
		this.unicodeSubject = unicodeSubject;
	}

	/**
	 * @return The unicode representation of the subject
	 */
	@Override
	public String getPattern() {
		return unicodeSubject;
	}

	@Override
	public void addMailField(final Collection<MailField> col) {
		col.add(MailField.SUBJECT);
	}

	@Override
	public boolean matches(final Message msg) throws MailException {
		final String subject;
		try {
			subject = msg.getSubject();
		} catch (final MessagingException e) {
			throw MIMEMailException.handleMessagingException(e);
		}
		if (subject != null) {
			return (subject.toLowerCase(Locale.ENGLISH).indexOf(unicodeSubject.toLowerCase(Locale.ENGLISH)) != -1);
		}
		return false;
	}

	@Override
	public boolean matches(final MailMessage mailMessage) {
		final String subject = mailMessage.getSubject();
		if (subject == null) {
			if (null == unicodeSubject) {
				return true;
			}
			return false;
		}
		if (null == unicodeSubject) {
			return false;
		}
		return (subject.toLowerCase(Locale.ENGLISH).indexOf(unicodeSubject.toLowerCase(Locale.ENGLISH)) != -1);
	}

	@Override
	public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
		return new javax.mail.search.SubjectTerm(unicodeSubject);
	}
}
