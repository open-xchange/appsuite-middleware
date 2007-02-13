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

import com.openexchange.api2.OXException;
import com.openexchange.groupware.imap.IMAPProperties;
import com.openexchange.groupware.imap.SpellCheckConfig;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.text.spelling.OXAspellCheck;
import com.openexchange.tools.text.spelling.OXSpellCheckResult;

/**
 * AJAXSpellCheck
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class AJAXSpellCheck {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AJAXSpellCheck.class);
	
	private SessionObject session;

	public AJAXSpellCheck(SessionObject session) {
		super();
		this.session = session;
	}
	
	private String getCommand() throws OXException {
		try {
			final String lang = session.getLanguage().toUpperCase();
			final SpellCheckConfig scc = IMAPProperties.getSpellCheckConfig();
			if (scc == null)
				throw new Exception("No SpellCheckConfig loaded!");
			final SpellCheckConfig.DictionaryConfig dicConfig = scc.getDictionary(lang);
			if (dicConfig == null)
				throw new Exception("No dictionary could be found for language: " + lang);
			if (dicConfig.getCommand() == null)
				throw new Exception("Dictionary (id=" + dicConfig.getId() + ") does not hold a command."
						+ " Please specify a command in corresponding \"spellcheck.cfg\" file");
			return dicConfig.getCommand();
		} catch (Exception e) {
			throw new OXException(e);
		}
	}
	
	public AJAXSpellCheckResult[] checkText(final String text) throws OXException {
		try {
			if (IMAPProperties.getSpellCheckConfig() == null) {
				throw new OXException("Missing SpellCheckConfig object");
			}
			if (!IMAPProperties.getSpellCheckConfig().isEnabled()) {
				return new AJAXSpellCheckResult[] {};
			}
			final String command = getCommand();
			final AJAXUserDictionary userDic = session.getUserConfiguration().getUserDictionary();
			if (userDic == null)
				throw new OXException("User dictionary could not be found!");
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
					final List results = oac.parseLine(line);
					final int size = results.size();
					for (int a = 0; a < size; a++) {
						final AJAXSpellCheckResult ascr = new AJAXSpellCheckResult();
						final OXSpellCheckResult oscr = (OXSpellCheckResult) results.get(a);
						ascr.setLineNumber(lineCount);
						ascr.setOffset(oscr.getOffset());
						ascr.setOriginalWord(oscr.getOriginalWord());
						ascr.addAllSuggestion(oscr.getSuggestions());
						ascr.setType(userDic.containsWord(oscr.getOriginalWord()) ? AJAXSpellCheckResult.TYPE_SUGGESTION
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
		} catch (OXException e) {
			throw e;
		} catch (Exception exc) {
			throw new OXException("Error while running spell check", exc);
		}
	}
	
	public JSONArray getSpellCheckResultsAsJSONArray(final String text) throws IOException, OXException {
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
