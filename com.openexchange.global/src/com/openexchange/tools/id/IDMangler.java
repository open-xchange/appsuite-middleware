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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.tools.id;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link IDMangler}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class IDMangler {

    public static final String PRIMARY_DELIM = "://";

    public static final String SECONDARY_DELIM = "/";

    public static String mangle(String... components) {
        StringBuilder id = new StringBuilder(50);
        boolean first = true;
        for (String string : components) {
            string = escape(string);
            id.append(string);
            String delim = first ? PRIMARY_DELIM : SECONDARY_DELIM;
            id.append(delim);
            first = false;
        }
        id.setLength(id.length()-1);
        return id.toString();
    }

    private static String escape(String string) {
        if(string == null) {
            return null;
        }
        StringBuilder buffer = new StringBuilder(string.length() * 3);
        for (char c : string.toCharArray()) {
            switch (c) {
            case '/':
                buffer.append("[/]");
                break;
            case '[':
                buffer.append("[[]");
                break;
            case ':':
                buffer.append("[:]");
                break;
            default:
                buffer.append(c);
            }
        }
        return buffer.toString();
    }

    private static enum ParserState {
        APPEND, APPEND_PREFIX, PRIMARY_DELIM1, PRIMARY_DELIM2, ESCAPED;
    }

    public static List<String> unmangle(String mangled) {
        ArrayList<String> list = new ArrayList<String>(5);
        StringBuilder buffer = new StringBuilder(50);
        ParserState state = ParserState.APPEND_PREFIX;
        ParserState unescapedState = null;

        for (char c : mangled.toCharArray()) {
            switch (c) {
            case '[': {
                if (state == ParserState.ESCAPED) {
                    buffer.append(c);
                } else {
                    unescapedState = state;
                    state = ParserState.ESCAPED;
                }
                break;
            }
            case ']': {
                if (state == ParserState.ESCAPED) {
                    state = unescapedState;
                } else {
                    buffer.append(c);
                }
                break;
            }
            case ':': {
                switch (state) {
                case APPEND:
                case ESCAPED:
                    buffer.append(c);
                    break;
                case APPEND_PREFIX:
                    state = ParserState.PRIMARY_DELIM1;
                    break;
                }
                break;
            }
            case '/': {
                switch (state) {
                case APPEND:
                    list.add(buffer.toString());
                    buffer.setLength(0);
                    break;
                case APPEND_PREFIX:
                case ESCAPED:
                    buffer.append(c);
                    break;
                case PRIMARY_DELIM1:
                    state = ParserState.PRIMARY_DELIM2;
                    break;
                case PRIMARY_DELIM2:
                    list.add(buffer.toString());
                    buffer.setLength(0);
                    state = ParserState.APPEND;
                    break;
                }
                break;
            }
            default: {
                switch(state) {
                case PRIMARY_DELIM1: buffer.append(':'); state = ParserState.APPEND_PREFIX; break;
                case PRIMARY_DELIM2: buffer.append(":/"); state = ParserState.APPEND_PREFIX; break;
                }
                buffer.append(c);
                break;
            }
            }
        }
        if (buffer.length() != 0) {
            list.add(buffer.toString());
        }
        return list;
    }
}
