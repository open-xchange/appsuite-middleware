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



package com.openexchange.tools.text.spelling;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * OXSpellCheckResult
 * 
 * @author Stefan Preuss <stefan.preuss@open-xchange.com>
 * @version 0.8.1, 11/11/05
 * @since 0.8.1-5
 */
public class OXSpellCheckResult {

    private int offset;
    private Type type;
    private List<String> suggestions;
    private String origWord;
    public static final Type ERROR = new Type("Error");
    public static final Type OK = new Type("OK");
    public static final Type NONE = new Type("None");
    public static final Type SUGGESTION = new Type("Suggestion");

    /**
     * @param response
     */
    public OXSpellCheckResult(final String line) {
        if (line == null || line.length() <= 0) {
			parseErr(line);
		} else if (line.charAt(0) == '*') {
			parseOk(line);
		} else if (line.charAt(0) == '&') {
			parseSuggestion(line);
		} else if (line.charAt(0) == '#') {
			parseNon(line);
		} else {
			parseErr(line);
		}
    }

    private void parseErr(final String line) {
        offset = 0;
        type = ERROR;
        suggestions = new ArrayList<String>();
        origWord = "";
    }

    private void parseOk(final String line) {
        offset = 0;
        type = OK;
        suggestions = new ArrayList<String>();
        origWord = "";
    }

    private void parseNon(final String line) {
        type = NONE;
        suggestions = new ArrayList<String>();

        final StringTokenizer st = new StringTokenizer(line);
        st.nextToken(); // skip '#'
        origWord = st.nextToken();
        offset = Integer.parseInt(st.nextToken());
    }

    private void parseSuggestion(final String line) {
        type = SUGGESTION;

        StringTokenizer st = new StringTokenizer(line);
        st.nextToken(); // skip '#'
        origWord = st.nextToken();
        final int count = Integer.parseInt(st.nextToken().trim());
        suggestions = new ArrayList<String>(count);
        offset = Integer.parseInt(st.nextToken(":").trim());

        st = new StringTokenizer(st.nextToken(":"), ",");
        while (st.hasMoreTokens()) {
            final String suggestion = st.nextToken().trim();
            suggestions.add(suggestion);
        }
    }

    public int getOffset() {
        return offset;
    }

    public Type getType() {
        return type;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public String getOriginalWord() {
        return origWord;
    }

    private static class Type {
        private final String typeName;

        Type(final String typeName) {
            this.typeName = typeName;
        }

        @Override
		public String toString() {
            return typeName;
        }
    }

}
