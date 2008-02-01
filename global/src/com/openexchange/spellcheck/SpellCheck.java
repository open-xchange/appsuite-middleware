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

package com.openexchange.spellcheck;

import java.util.List;

import javax.swing.text.Document;

/**
 * {@link SpellCheck} - Offers several methods for spell checking.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public interface SpellCheck {

	/**
	 * Checks the spelling of the words contained in specified text.
	 * <p>
	 * For each invalid word an instance of {@link SpellCheckError} is
	 * generated; meaning an array with length <code>0</code> is returned if
	 * no misspelt words are found.
	 * </p>
	 * 
	 * @param text
	 *            The text to check
	 * @return An array of {@link SpellCheckError} for each invalid word
	 */
	public SpellCheckError[] checkSpelling(String text);

	/**
	 * Checks the spelling of the words contained in specified document's text.
	 * <p>
	 * For each invalid word an instance of {@link SpellCheckError} is
	 * generated; meaning an array with length <code>0</code> is returned if
	 * no misspelt words are found.
	 * </p>
	 * 
	 * @param document
	 *            The document whose text shall be checked
	 * @return An array of {@link SpellCheckError} for each invalid word
	 */
	public SpellCheckError[] checkSpelling(Document document);

	/**
	 * Adds words to the user dictionary
	 * 
	 * @param words
	 *            The words to add
	 */
	public void addWord(String... words);

	/**
	 * Removes words from the user dictionary
	 * 
	 * @param words
	 *            The words to remove
	 */
	public void removeWord(String... words);

	/**
	 * Returns a list containing all user words
	 * 
	 * @return A list containing all user words
	 */
	public List<String> getUserWords();

	/**
	 * Verifies if the word to analyze is contained in dictionaries. The order
	 * of dictionary lookup is:
	 * <ul>
	 * <li>The user dictionary </li>
	 * <li>The locale-specific global dictionary </li>
	 * </ul>
	 * 
	 * @param word
	 *            The word to verify that it's spelling is known.
	 * @return <code>true</code> if the word is in a dictionary; otherwise
	 *         <code>false</code>
	 */
	public boolean isCorrect(String word);

	/**
	 * Produces a list of suggested word after looking for suggestions in
	 * various dictionaries. The order of dictionary lookup is:
	 * <ul>
	 * <li>The user dictionary </li>
	 * <li>The locale-specific global dictionary </li>
	 * </ul>
	 * If the word is correctly spelled, then this method could return just that
	 * one word, or it could still return a list of words with similar
	 * spellings.
	 * <p>
	 * Each suggested word has a score, which is an <code>int</code> that
	 * represents how different the suggested word is from the source word. If
	 * the words are exactly the same, then the score is <code>0</code>. You
	 * can get the dictionary to only return the most similar words by setting
	 * an appropriately low threshold value. If you set the threshold value too
	 * low, you may get no suggestions for a given word.
	 * </p>
	 * 
	 * @param word
	 *            The word for which we want to gather suggestions
	 * @param threshold
	 *            The cost value above which any suggestions are thrown away
	 * @return The list of suggested words
	 */
	public List<String> getSuggestions(String word, int threshold);
}
