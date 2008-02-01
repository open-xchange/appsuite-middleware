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

package com.openexchange.spellcheck.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.swabunga.spell.engine.SpellDictionary;

/**
 * {@link CombinedSpellDictionary}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class CombinedSpellDictionary implements SpellDictionary {

	private final List<SpellDictionary> dicts;

	/**
	 * Initializes a new {@link CombinedSpellDictionary}
	 */
	public CombinedSpellDictionary() {
		super();
		dicts = new ArrayList<SpellDictionary>();
	}

	/**
	 * Initializes a new {@link CombinedSpellDictionary}
	 * 
	 * @param dicts
	 *            The initial dictionaries to add
	 */
	public CombinedSpellDictionary(final SpellDictionary... dicts) {
		this();
		addSpellDictionaries(dicts);
	}

	/**
	 * Adds specified spell dictionaries to this combined dictionaries
	 * 
	 * @param dicts
	 *            The dictionaries to add
	 */
	public void addSpellDictionaries(final SpellDictionary... dicts) {
		for (int i = 0; i < dicts.length; i++) {
			this.dicts.add(dicts[i]);
		}
	}

	/**
	 * Checks if this combined dictionary has no added dictionaries
	 * 
	 * @return <code>true</code> if empty; otherwise <code>false</code>
	 */
	public boolean isEmpty() {
		return (dicts.isEmpty());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.swabunga.spell.engine.SpellDictionary#addWord(java.lang.String)
	 */
	public void addWord(final String word) {
		throw new UnsupportedOperationException("CombinedSpellDictionary.addWord(java.lang.String) not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.swabunga.spell.engine.SpellDictionary#getSuggestions(java.lang.String,
	 *      int)
	 */
	@SuppressWarnings("unchecked")
	public List<?> getSuggestions(final String sourceWord, final int scoreThreshold) {
		final Set<String> set = new HashSet<String>();
		for (final SpellDictionary dict : dicts) {
			set.addAll(dict.getSuggestions(sourceWord, scoreThreshold));
		}
		return new ArrayList<String>(set);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.swabunga.spell.engine.SpellDictionary#getSuggestions(java.lang.String,
	 *      int, int[][])
	 */
	@SuppressWarnings("unchecked")
	public List<?> getSuggestions(final String sourceWord, final int scoreThreshold, final int[][] matrix) {
		final Set<String> set = new HashSet<String>();
		for (final SpellDictionary dict : dicts) {
			set.addAll(dict.getSuggestions(sourceWord, scoreThreshold, matrix));
		}
		return new ArrayList<String>(set);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.swabunga.spell.engine.SpellDictionary#isCorrect(java.lang.String)
	 */
	public boolean isCorrect(final String word) {
		final int size = dicts.size();
		boolean correct = false;
		for (int i = 0; i < size && !correct; i++) {
			correct = dicts.get(i).isCorrect(word);
		}
		return correct;
	}

}
