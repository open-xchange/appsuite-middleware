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

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.ComparisonTerm;

import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MIMEMailException;

/**
 * {@link SizeTerm}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SizeTerm extends SearchTerm<int[]> {

	private static final long serialVersionUID = 6011159685554702125L;

	private final int size;

	private final ComparisonType comparisonType;

	/**
	 * Initializes a new {@link SizeTerm}
	 */
	public SizeTerm(final ComparisonType comparisonType, final int size) {
		super();
		this.comparisonType = comparisonType;
		this.size = size;
	}

	/**
	 * @return The size of bytes to match
	 */
	@Override
	public int[] getPattern() {
		switch (comparisonType) {
		case LESS_THAN:
			return new int[] { ComparisonType.LESS_THAN.getType(), size };
		case EQUALS:
			return new int[] { ComparisonType.EQUALS.getType(), size };
		case GREATER_THAN:
			return new int[] { ComparisonType.GREATER_THAN.getType(), size };
		default:
			return null;
		}
	}

	@Override
	public void addMailField(final Collection<MailField> col) {
		col.add(MailField.SIZE);
	}

	@Override
	public boolean matches(final Message msg) throws MailException {
		final int size;
		try {
			size = msg.getSize();
		} catch (final MessagingException e) {
			throw MIMEMailException.handleMessagingException(e);
		}
		final int[] dat = getPattern();
		if (dat[0] == com.openexchange.mail.search.ComparisonType.EQUALS.getType()) {
			return size == dat[1];
		} else if (dat[0] == com.openexchange.mail.search.ComparisonType.LESS_THAN.getType()) {
			return size < dat[1];
		} else if (dat[0] == com.openexchange.mail.search.ComparisonType.GREATER_THAN.getType()) {
			return size > dat[1];
		} else {
			return size == dat[1];
		}
	}

	@Override
	public boolean matches(final MailMessage mailMessage) {
		final long size = mailMessage.getSize();
		final int[] dat = getPattern();
		if (dat[0] == com.openexchange.mail.search.ComparisonType.EQUALS.getType()) {
			return size == dat[1];
		} else if (dat[0] == com.openexchange.mail.search.ComparisonType.LESS_THAN.getType()) {
			return size < dat[1];
		} else if (dat[0] == com.openexchange.mail.search.ComparisonType.GREATER_THAN.getType()) {
			return size > dat[1];
		} else {
			return size == dat[1];
		}
	}

	@Override
	public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
		final int[] dat = getPattern();
		final int ct;
		if (dat[0] == com.openexchange.mail.search.ComparisonType.EQUALS.getType()) {
			ct = ComparisonTerm.EQ;
		} else if (dat[0] == com.openexchange.mail.search.ComparisonType.LESS_THAN.getType()) {
			ct = ComparisonTerm.LT;
		} else if (dat[0] == com.openexchange.mail.search.ComparisonType.GREATER_THAN.getType()) {
			ct = ComparisonTerm.GT;
		} else {
			ct = ComparisonTerm.EQ;
		}
		return new javax.mail.search.SizeTerm(ct, dat[1]);
	}
}
