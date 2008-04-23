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
import javax.mail.search.OrTerm;

import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link ORTerm}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ORTerm extends SearchTerm<SearchTerm<?>[]> {

	private final SearchTerm<?>[] terms;

	/**
	 * Initializes a new {@link ORTerm}
	 */
	protected ORTerm() {
		super();
		terms = new SearchTerm<?>[2];
	}

	/**
	 * Initializes a new {@link ORTerm}
	 */
	public ORTerm(final SearchTerm<?> firstTerm, final SearchTerm<?> secondTerm) {
		super();
		terms = new SearchTerm<?>[] { firstTerm, secondTerm };
	}

	/**
	 * Gets the search terms that should be linked with an OR as an array of
	 * {@link SearchTerm} with length <code>2</code>.
	 * 
	 * @return The terms that should be linked with an OR
	 */
	@Override
	public SearchTerm<?>[] getPattern() {
		return terms;
	}

	/**
	 * Sets the first search term
	 * 
	 * @param firstTerm
	 *            The first search term
	 */
	public void setFirstTerm(final SearchTerm<?> firstTerm) {
		terms[0] = firstTerm;
	}

	/**
	 * Sets the second search term
	 * 
	 * @param secondTerm
	 *            The second search term
	 */
	public void setSecondTerm(final SearchTerm<?> secondTerm) {
		terms[1] = secondTerm;
	}

	@Override
	public void addMailField(final Collection<MailField> col) {
		terms[0].addMailField(col);
		terms[1].addMailField(col);
	}

	@Override
	public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
		return new OrTerm(terms[0].getJavaMailSearchTerm(), terms[1].getJavaMailSearchTerm());
	}

	@Override
	public boolean matches(final Message msg) throws MailException {
		return terms[0].matches(msg) || terms[1].matches(msg);
	}

	@Override
	public boolean matches(final MailMessage mailMessage) throws MailException {
		return terms[0].matches(mailMessage) || terms[1].matches(mailMessage);
	}
}
