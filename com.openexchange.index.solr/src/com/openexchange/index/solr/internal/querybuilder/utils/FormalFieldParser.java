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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.index.solr.internal.querybuilder.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link FormalFieldParser}
 *
 * @author Sven Maurmann
 */
public class FormalFieldParser {

    private final Map<String, List<String>> formalFieldMap;

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FormalFieldParser.class);

    public FormalFieldParser(Map<String, String> mapping) {
        formalFieldMap = this.createMapping(mapping);
    }

    public FormalFieldParser(Map<String, List<String>> mapping, boolean mapReady) {
        formalFieldMap = mapping;
    }

    /**
     * The method creates a list of terms from a string of the following form: <br>
     * <code>field1:term1 field1:"term2 AND term3" field2:term4</code> <br>
     * The members of the list are
     *
     * @param source
     * @return
     */
    public String parse(String source) {
        StringBuffer b = new StringBuffer();
        log.debug("[parse]: Parsing \'{}\'", source);
        List<Token> termList = this.split(source);

        for (Token t : termList) {
            String s = t.getTerm().split(":")[0];
            if (formalFieldMap.containsKey(s)) {
                b.append(this.parseTerm(t.getTerm(), formalFieldMap.get(s)) + " ");
            } else {
                if (!(s.equalsIgnoreCase("AND") || s.equalsIgnoreCase("OR") || s.equalsIgnoreCase("NOT"))) {
                    log.debug("[parse]: No mapping for field \'{}\'", s);
                }
                b.append(s + " ");
            }
        }
        return b.toString().trim();
    }

    // ============================ private methods below ================================= //

    private List<Token> split(String source) {
        List<Token> termList = new ArrayList<Token>();
        boolean protectQuote = false;
        boolean escaped = false;

        StringBuilder b = new StringBuilder();
        log.trace("[split]: Starting to split \'{}\'", source);

        final int length = source.length();
        for (int i = 0; i < length; i++) {
            final char ch = source.charAt(i);
            log.trace("[split]: \'{}\'", ch);
            switch (ch) {
                case '\\':
                    escaped = true;
                    log.trace("[split]: set escaped to TRUE");
                    b.append(ch);
                    break;

                case '"':
                    if (!escaped && !protectQuote) {
                        protectQuote = true;
                        log.trace("[split]: set protectQuote to TRUE");
                    } else {
                        if (!escaped && protectQuote) {
                            protectQuote = false;
                            log.trace("[split]: set protectQuote to FALSE");
                        }
                    }
                    if (escaped) {
                        escaped = false;
                        log.trace("[split]: set escape to FALSE");
                    }
                    b.append(ch);
                    break;

                case ' ':
                    if (!protectQuote) {
                        Token t = new Token(b.toString().trim(), TokenTypes.GENERIC);
                        termList.add(t);
                        log.debug("[split]: add term \'{}\'", t.getTerm());
                        b.delete(0, b.length());
                        protectQuote = false;
                        escaped = false;
                    } else {
                        b.append(ch);
                    }
                    break;

                case '(':
                    if (!protectQuote) {
                        Token t = new Token(b.toString().trim(), TokenTypes.GENERIC);
                        if (b.toString().trim().length() > 0) {
                            termList.add(t);
                            log.debug("[split]: add term \'{}\'", t.getTerm());
                        }
                        termList.add(new Token("(", TokenTypes.BRACKET));
                        log.debug("[split]: add term \'(\'");

                        b.delete(0, b.length());
                        protectQuote = false;
                        escaped = false;
                    } else {
                        b.append(ch);
                    }
                    break;

                case ')':
                    if (!protectQuote) {
                        Token t = new Token(b.toString().trim(), TokenTypes.GENERIC);
                        if (b.toString().trim().length() > 0) {
                            termList.add(t);
                            log.debug("[split]: add term \'{}\'", t.getTerm());
                        }
                        termList.add(new Token(")", TokenTypes.BRACKET));
                        log.debug("[split]: add term \')\'");
                        b.delete(0, b.length());
                        protectQuote = false;
                        escaped = false;
                    } else {
                        b.append(ch);
                    }
                    break;

                default:
                    b.append(ch);
            }
        }

        Token t = new Token(b.toString().trim(), TokenTypes.GENERIC);
        termList.add(t);
        log.trace("[split]: add term \'{}\'", t.getTerm());
        return termList;
    }

    private String parseTerm(String term, List<String> replacements) throws RuntimeException {
        log.debug("[parseTerm]: Receiving search string \'{}\'", term);
        String searchTerm;

        int pos;
        if ((pos = term.indexOf(':')) < 0) {
            throw new RuntimeException("Term misses ':' (colon) character: " + term);
        }
        searchTerm = term.substring(pos + 1, term.length());

        final StringBuilder b = new StringBuilder(replacements.size() << 4);
        b.append('(');
        {
            boolean first = true;
            for (final String s : replacements) {
                if (first) {
                    first = false;
                } else {
                    b.append(' ');
                }
                b.append(s).append(':').append(searchTerm);
            }
        }
        b.append(')');
        if (log.isDebugEnabled()) {
            log.debug("[parseTerm]: result is \'{}\'", b);
        }

        return b.toString();
    }

    private Map<String, List<String>> createMapping(Map<String, String> rawMap) {
        if (log.isTraceEnabled()) {
            for (String s : rawMap.keySet()) {
                log.debug("[createMapping]: Received {}:{}", s, rawMap.get(s));
            }
        }
        Map<String, List<String>> fieldMappings = new HashMap<String, List<String>>();

        for (String formalField : rawMap.keySet()) {
            List<String> schemaFields = new ArrayList<String>();
            final String mf = rawMap.get(formalField);
            final int pos;
            if ((pos = mf.indexOf('{')) >= 0) {
                log.trace("[createMapping]: is multi-field");
                String prefix = mf.substring(0, pos);
                String[] locales = mf.substring(pos + 1, mf.length() - 1).split(",");
                for (String s : locales) {
                    schemaFields.add(prefix + "_" + s.trim());
                }
            } else {
                log.trace("[createMapping]: is standard field");
                schemaFields.add(mf);
            }
            fieldMappings.put(formalField, schemaFields);
        }

        if (log.isTraceEnabled()) {
            for (String s : fieldMappings.keySet()) {
                StringBuffer b = new StringBuffer();
                b.append(s + ":");
                for (String t : fieldMappings.get(s)) {
                    b.append(t + " ");
                }
                log.trace("[createMapping]: result is {}", b);
            }
        }
        return fieldMappings;
    }
}
