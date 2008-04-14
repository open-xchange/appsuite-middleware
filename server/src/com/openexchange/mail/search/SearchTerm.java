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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link SearchTerm}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class SearchTerm<T> {

	private static final String UNCHECKED = "unchecked";

	/**
	 * Initializes a new {@link SearchTerm}
	 */
	public SearchTerm() {
		super();
	}

	/**
	 * Gets the pattern to which the expression should match.
	 * 
	 * @return The pattern
	 */
	public abstract T getPattern();

	/**
	 * Generates a search term with the unsupported search terms specified
	 * through <code>filter</code> removed.
	 * <p>
	 * For each search term contained in this search term the following rule is
	 * applied:
	 * <ol>
	 * <li>If search term is an instance of {@link ORTerm} or {@link ANDTerm}
	 * replace the unsupported with:
	 * <ul>
	 * <li>the neutral element if it is the first element that has to be
	 * replaced: {@link BooleanTerm#FALSE} for {@link ORTerm} and
	 * {@link BooleanTerm#TRUE} for {@link ANDTerm}</li>
	 * <li>the failing element if term's other element has already been
	 * replaced to let the whole search term fail: {@link BooleanTerm#FALSE} for
	 * both {@link ORTerm} and {@link ANDTerm}</li>
	 * </ul>
	 * </li>
	 * <li>If search term is supported, return the search term itself</li>
	 * <li>Otherwise replace with {@link BooleanTerm#FALSE}</li>
	 * </ol>
	 * <p>
	 * <b>Note</b>: Only a shallow copy is generated; meaning further working
	 * on this search term may influence return value's search term.
	 * 
	 * @param filter
	 *            An array containing unsupported classes of {@link SearchTerm}
	 *            to filter against
	 * @return A new search term with the unsupported search terms removed
	 */
	@SuppressWarnings(UNCHECKED)
	public SearchTerm<?> filter(final Class<? extends SearchTerm>[] filter) {
		return handleTerm(this, new HashSet<Class<? extends SearchTerm>>(Arrays.asList(filter)));
	}

	@SuppressWarnings(UNCHECKED)
	private static final SearchTerm<?> handleTerm(final SearchTerm<?> searchTerm,
			final Set<Class<? extends SearchTerm>> filterSet) {
		if (searchTerm instanceof ORTerm) {
			return handleORTerm(((ORTerm) searchTerm).getPattern(), filterSet);
		} else if (searchTerm instanceof ANDTerm) {
			return handleANDTerm(((ANDTerm) searchTerm).getPattern(), filterSet);
		} else if (filterSet.contains(searchTerm.getClass())) {
			return BooleanTerm.FALSE;
		}
		return searchTerm;
	}

	@SuppressWarnings(UNCHECKED)
	private static final SearchTerm<?> handleORTerm(final SearchTerm<?>[] terms,
			final Set<Class<? extends SearchTerm>> filterSet) {
		final ORTerm orTerm = new ORTerm();
		if (filterSet.contains(terms[0].getClass())) {
			/*
			 * Replace with neutral element
			 */
			orTerm.setFirstTerm(BooleanTerm.FALSE);
		} else {
			orTerm.setSecondTerm(handleTerm(terms[1], filterSet));
		}
		if (filterSet.contains(terms[1].getClass())) {
			/*
			 * Replace with neutral element which fits in any case no matter if
			 * first element has already been replaced or not.
			 */
			orTerm.setSecondTerm(BooleanTerm.FALSE);
		} else {
			orTerm.setSecondTerm(handleTerm(terms[1], filterSet));
		}
		return orTerm;
	}

	@SuppressWarnings(UNCHECKED)
	private static final SearchTerm<?> handleANDTerm(final SearchTerm<?>[] terms,
			final Set<Class<? extends SearchTerm>> filterSet) {
		final ANDTerm andTerm = new ANDTerm();
		final boolean replaceFirst = filterSet.contains(terms[0].getClass());
		if (replaceFirst) {
			/*
			 * Replace with neutral element
			 */
			andTerm.setFirstTerm(BooleanTerm.TRUE);
		} else {
			andTerm.setSecondTerm(handleTerm(terms[1], filterSet));
		}
		if (filterSet.contains(terms[1].getClass())) {
			if (replaceFirst) {
				/*
				 * Replace with fail element since the first element has already
				 * been replaced with neutral element.
				 */
				andTerm.setSecondTerm(BooleanTerm.FALSE);
			} else {
				/*
				 * Replace with neutral element
				 */
				andTerm.setSecondTerm(BooleanTerm.TRUE);
			}
		} else {
			andTerm.setSecondTerm(handleTerm(terms[1], filterSet));
		}
		return andTerm;
	}
}
