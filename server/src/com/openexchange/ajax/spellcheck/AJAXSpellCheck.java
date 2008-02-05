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

package com.openexchange.ajax.spellcheck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.spellcheck.SpellCheckConfig;
import com.openexchange.session.Session;
import com.openexchange.tools.text.spelling.OXAspellCheck;
import com.openexchange.tools.text.spelling.OXSpellCheckResult;

/**
 * AJAXSpellCheck
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
@OXExceptionSource(classId = AJAXSpellCheckExceptionClasses.AJAX_SPELL_CHECK, component = Component.SPELLCHECK)
public class AJAXSpellCheck {

	private final Session session;

	private final Context ctx;

	private static final AJAXSpellCheckExceptionFactory EXCEPTIONS = new AJAXSpellCheckExceptionFactory(
			AJAXSpellCheck.class);

	private static final Object[] EMPTY_MSG_ARGS = new Object[0];

	public AJAXSpellCheck(final Session session, final Context ctx) {
		super();
		this.session = session;
		this.ctx = ctx;
	}

	@OXThrowsMultiple(category = { Category.SETUP_ERROR, Category.SETUP_ERROR, Category.SETUP_ERROR }, desc = { "", "",
			"" }, exceptionId = { 1, 2, 3 }, msg = { "No SpellCheckConfig loaded.",
			"No dictionary could be found for language: %1$s.",
			"Dictionary (id=%1$s) does not hold a command. Please specify a command in corresponding \"spellcheck.cfg\" file." })
	private String getCommand() throws AbstractOXException {
		final String lang = UserStorage.getStorageUser(session.getUserId(), ctx).getLocale().getLanguage().toUpperCase();
		final SpellCheckConfig scc = MailConfig.getSpellCheckConfig();
		if (scc == null) {
			throw EXCEPTIONS.createException(1, EMPTY_MSG_ARGS);
		}
		final SpellCheckConfig.DictionaryConfig dicConfig = scc.getDictionary(lang);
		if (dicConfig == null) {
			throw EXCEPTIONS.createException(2, lang);
		}
		if (dicConfig.getCommand() == null) {
			throw EXCEPTIONS.createException(3, dicConfig.getId());
		}
		return dicConfig.getCommand();
	}

	@OXThrowsMultiple(category = { Category.SETUP_ERROR, Category.SETUP_ERROR, Category.CODE_ERROR }, desc = { "", "",
			"" }, exceptionId = { 4, 5, 6 }, msg = { "Missing SpellCheckConfig object.",
			"User dictionary could not be found.", "An I/O error occurred: %1$s." })
	public AJAXSpellCheckResult[] checkText(final String text) throws AbstractOXException {
		try {
			if (MailConfig.getSpellCheckConfig() == null) {
				throw EXCEPTIONS.createException(4, EMPTY_MSG_ARGS);
			}
			if (!MailConfig.getSpellCheckConfig().isEnabled()) {
				return new AJAXSpellCheckResult[] {};
			}
			final String command = getCommand();
			final AJAXUserDictionary userDic = UserConfigurationStorage.getInstance().getUserConfigurationSafe(
					session.getUserId(), ctx).getUserDictionary();
			if (userDic == null) {
				throw EXCEPTIONS.createException(5, EMPTY_MSG_ARGS);
			}
			StringReader sr = null;
			BufferedReader br = null;
			OXAspellCheck oac = null;
			try {
				final ArrayList<AJAXSpellCheckResult> resultList = new ArrayList<AJAXSpellCheckResult>();
				/*
				 * Execute ASpell command
				 */
				oac = new OXAspellCheck(command);
				sr = new StringReader(text.replaceAll("<br/?>", "\n"));
				br = new BufferedReader(sr);
				String line = null;
				int lineCount = 0;
				while ((line = br.readLine()) != null) {
					lineCount++;
					final List<OXSpellCheckResult> results = oac.parseLine(line);
					final int size = results.size();
					for (int a = 0; a < size; a++) {
						final AJAXSpellCheckResult ascr = new AJAXSpellCheckResult();
						final OXSpellCheckResult oscr = results.get(a);
						ascr.setLineNumber(lineCount);
						ascr.setOffset(oscr.getOffset());
						ascr.setOriginalWord(oscr.getOriginalWord());
						ascr.addAllSuggestion(oscr.getSuggestions());
						ascr
								.setType(userDic.containsWord(oscr.getOriginalWord()) ? AJAXSpellCheckResult.TYPE_SUGGESTION
										: AJAXSpellCheckResult.TYPE_ERROR);
						resultList.add(ascr);
					}
				}
				final Object[] objs = resultList.toArray();
				final AJAXSpellCheckResult[] retval = new AJAXSpellCheckResult[objs.length];
				System.arraycopy(objs, 0, retval, 0, objs.length);
				return retval;
			} finally {
				if (br != null) {
					br.close();
				}
				if (sr != null) {
					sr.close();
				}
				if (oac != null) {
					oac.destroy();
				}
			}
		} catch (IOException e) {
			throw EXCEPTIONS.createException(6, e, e.getMessage());
		}
	}

	public JSONArray getSpellCheckResultsAsJSONArray(final String text) throws AbstractOXException {
		return createJSONArrayFromSpellCheckResults(checkText(text));
	}

	private JSONArray createJSONArrayFromSpellCheckResults(final AJAXSpellCheckResult[] results) {
		final JSONArray retvalArr = new JSONArray();
		for (int i = 0; i < results.length; i++) {
			final AJAXSpellCheckResult ascr = results[i];
			final JSONArray resultArr = new JSONArray();
			resultArr.put(ascr.getOriginalWord());
			resultArr.put(ascr.getLineNumber());
			resultArr.put(ascr.getOffset());
			resultArr.put(ascr.getOffsetEnd());
			resultArr.put(ascr.getType());
			final JSONArray suggestionsArr = new JSONArray();
			final int size = ascr.getSuggestionsSize();
			final Iterator<String> iter = ascr.getSuggestionsIterator();
			for (int k = 0; k < size; k++) {
				suggestionsArr.put(iter.next());
			}
			resultArr.put(suggestionsArr);
			retvalArr.put(resultArr);
		}
		return retvalArr;
	}

}