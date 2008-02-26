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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.text.Document;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.spellcheck.SpellCheckError;
import com.openexchange.spellcheck.SpellCheckException;
import com.openexchange.spellcheck.SpellCheckService;
import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.event.DocumentWordTokenizer;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

/**
 * {@link SpellCheckImpl}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SpellCheckImpl implements SpellCheckService {

	/**
	 * Creates a new spell check with only the user dictionary added
	 * 
	 * @param userId
	 *            The user ID
	 * @param ctx
	 *            The context
	 * @return A newly created spell check
	 * @throws SpellCheckException
	 *             If spell check creation fails
	 */
	public static SpellCheckService newSpellCheck(final int userId, final Context ctx) throws SpellCheckException {
		return new SpellCheckImpl(null, new RdbUserSpellDictionary(userId, ctx));
	}

	/**
	 * Creates a new spell check
	 * 
	 * @param userId
	 *            The user ID
	 * @param localeStr
	 *            The desired locale's string representation (determines the
	 *            locale-specific dictionary)
	 * @param ctx
	 *            The context
	 * @return A newly created spell check
	 * @throws SpellCheckException
	 *             If spell check creation fails
	 */
	public static SpellCheckService newSpellCheck(final int userId, final String localeStr, final Context ctx)
			throws SpellCheckException {
		final SpellDictionary localeDictionary = DictonaryStorage.getDictionary(localeStr);
		if (localeDictionary == null) {
			throw new SpellCheckException(SpellCheckException.Code.MISSING_LOCALE_DIC, localeStr);
		}
		return new SpellCheckImpl(localeDictionary, new RdbUserSpellDictionary(userId, ctx));
	}

	/**
	 * Creates a new spell check
	 * 
	 * @param userId
	 *            The user ID
	 * @param locale
	 *            The desired locale (determines the locale-specific dictionary)
	 * @param ctx
	 *            The context
	 * @return A newly created spell check
	 * @throws SpellCheckException
	 *             If spell check creation fails
	 */
	public static SpellCheckService newSpellCheck(final int userId, final Locale locale, final Context ctx)
			throws SpellCheckException {
		final SpellDictionary localeDictionary = DictonaryStorage.getDictionary(locale);
		if (localeDictionary == null) {
			throw new SpellCheckException(SpellCheckException.Code.MISSING_LOCALE_DIC, locale.toString());
		}
		return new SpellCheckImpl(localeDictionary, new RdbUserSpellDictionary(userId, ctx));
	}

	private static final SpellCheckError[] EMPTY_ERRORS = new SpellCheckError[0];

	private final RdbUserSpellDictionary userDictionary;

	private final SpellChecker spellChecker;

	private final _SpellCheckListener spellCheckListener;

	/**
	 * Initializes a new {@link SpellCheckImpl}
	 * 
	 * @param localeDictionary
	 *            The locale-specific dictionary
	 */
	private SpellCheckImpl(final SpellDictionary localeDictionary) {
		this(localeDictionary, null);
	}

	/**
	 * Initializes a new {@link SpellCheckImpl}
	 * 
	 * @param localeDictionary
	 *            The locale-specific dictionary
	 * @param userDictionary
	 *            The user dictionary
	 */
	private SpellCheckImpl(final SpellDictionary localeDictionary, final RdbUserSpellDictionary userDictionary) {
		super();
		spellChecker = localeDictionary == null ? new SpellChecker() : new SpellChecker(localeDictionary);
		if (null != userDictionary) {
			spellChecker.setUserDictionary(userDictionary);
		}
		spellCheckListener = new _SpellCheckListener();
		spellChecker.addSpellCheckListener(spellCheckListener);
		/*
		 * Assign to support removals
		 */
		this.userDictionary = userDictionary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.spellcheck.SpellCheck#addWord(java.lang.String[])
	 */
	public void addWord(final String... words) {
		if (userDictionary != null && words.length > 0) {
			final int mlen = words.length - 1;
			for (int i = 0; i < mlen; i++) {
				userDictionary.addWord(words[i], false);
			}
			userDictionary.addWord(words[mlen], true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.spellcheck.SpellCheck#removeWord(java.lang.String[])
	 */
	public void removeWord(final String... words) {
		if (userDictionary != null && words.length > 0) {
			final int mlen = words.length - 1;
			for (int i = 0; i < mlen; i++) {
				userDictionary.removeWord(words[i], false);
			}
			userDictionary.removeWord(words[mlen], true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.spellcheck.SpellCheck#getUserWords()
	 */
	public List<String> getUserWords() {
		if (userDictionary != null) {
			final List<String> l = userDictionary.getWords();
			Collections.sort(l);
			return l;
		}
		return Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.spellcheck.SpellCheck#checkSpelling(java.lang.String)
	 */
	public SpellCheckError[] checkSpelling(final String text) {
		spellCheckListener.clearErrors();
		spellChecker.checkSpelling(new StringWordTokenizer(text));
		return spellCheckListener.getSpellCheckErrors();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.spellcheck.SpellCheck#checkSpelling(java.lang.String)
	 */
	public SpellCheckError[] checkSpelling(final Document document) {
		spellCheckListener.clearErrors();
		spellChecker.checkSpelling(new DocumentWordTokenizer(document));
		return spellCheckListener.getSpellCheckErrors();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.spellcheck.SpellCheck#getSuggestions(java.lang.String,
	 *      int)
	 */
	public List<String> getSuggestions(final String word, final int threshold) {
		final List<?> l = spellChecker.getSuggestions(word, threshold);
		if (l != null) {
			final List<String> retval = new ArrayList<String>(l.size());
			for (final Object obj : l) {
				retval.add(obj.toString());
			}
			return retval;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.spellcheck.SpellCheck#isCorrect(java.lang.String)
	 */
	public boolean isCorrect(final String word) {
		return spellChecker.isCorrect(word);
	}

	private static final class _SpellCheckListener implements SpellCheckListener {

		private final List<SpellCheckError> errors;

		/**
		 * Initializes a new {@link _SpellCheckListener}
		 */
		public _SpellCheckListener() {
			super();
			errors = new ArrayList<SpellCheckError>();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.swabunga.spell.event.SpellCheckListener#spellingError(com.swabunga.spell.event.SpellCheckEvent)
		 */
		public void spellingError(final SpellCheckEvent event) {
			errors.add(_SpellCheckError.generateSpellCheckError(event));
		}

		public SpellCheckError[] getSpellCheckErrors() {
			if (errors.size() == 0) {
				return EMPTY_ERRORS;
			}
			return errors.toArray(new SpellCheckError[errors.size()]);
		}

		public void clearErrors() {
			errors.clear();
		}
	}

	private static final class _SpellCheckError implements SpellCheckError {

		private final String invalidWord;

		private final List<String> suggestions;

		private final int startPos;

		public static SpellCheckError generateSpellCheckError(final SpellCheckEvent event) {
			return new _SpellCheckError(event);
		}

		/**
		 * Initializes a new {@link _SpellCheckError}
		 * 
		 * @param event
		 *            The spell check event from which the error is created
		 */
		private _SpellCheckError(final SpellCheckEvent event) {
			super();
			this.invalidWord = event.getInvalidWord();
			final List<?> suggestions = event.getSuggestions();
			if (suggestions != null) {
				this.suggestions = new ArrayList<String>(suggestions.size());
				for (final Object cur : suggestions) {
					this.suggestions.add(cur.toString());
				}
			} else {
				this.suggestions = null;
			}
			this.startPos = event.getWordContextPosition();
		}

		public String getInvalidWord() {
			return invalidWord;
		}

		public List<String> getSuggestions() {
			return suggestions;
		}

		public int getWordStartingPosition() {
			return startPos;
		}

	}
}
