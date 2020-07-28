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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.html.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link SaneScriptTags}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SaneScriptTags {

    /**
     * Initializes a new {@link SaneScriptTags}.
     */
    private SaneScriptTags() {
        super();
    }

    public static void main(String[] args) {
        String s = "<<SCRIPT>alert(\\\"XSS\\\");//<</SCRIPT>script/xss>x=/xss/;alert(x.source)</script>";
        boolean[] a = new boolean[] { true };
        while (a[0]) {
            a[0] = false;
            s = saneScriptTags(s, a);
        }
        System.out.println("--> "+s);
    }

    /**
     * Sanitizes specified HTML content by script tags
     *
     * @param html The HTML content
     * @param sanitized The sanitized flag
     * @return The sanitized HTML content
     */
    public static String saneScriptTags(String html, boolean[] sanitized) {
        if (com.openexchange.java.Strings.isEmpty(html)) {
            return html;
        }

        String processed = decode(html);
        processed = dropConcatenations(processed);
        processed = dropScriptTags(sanitized, processed);

        return sanitized[0] ? processed : html;
    }

    private static final Pattern PAT_URLDECODE_PERCENT = Pattern.compile("%25");

    private static final Pattern PAT_URLDECODE_ENTITIES = Pattern.compile("%([23][2ceCE])");
    private static final Set<String> REPLACEES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("3c", "3e", "22")));

    private static String decode(String html) {
        if (html.indexOf('%') < 0) {
            return html;
        }

        Matcher m = PAT_URLDECODE_PERCENT.matcher(html);
        if (m.find()) {
            StringBuffer sb = new StringBuffer(html.length());
            do {
                m.appendReplacement(sb, "%");
            } while (m.find());
            m.appendTail(sb);
            html = sb.toString();
            sb = null;
        }

        m = PAT_URLDECODE_ENTITIES.matcher(html);
        if (m.find()) {
            StringBuffer sb = new StringBuffer(html.length());
            do {
                final String entity = com.openexchange.java.Strings.toLowerCase(m.group(1));
                if (REPLACEES.contains(entity)) {
                    m.appendReplacement(sb, com.openexchange.java.Strings.quoteReplacement(Character.toString((char) Integer.parseInt(m.group(1), 16))));
                } else {
                    m.appendReplacement(sb, "$0");
                }
            } while (m.find());
            m.appendTail(sb);
            html = sb.toString();
            sb = null;
        }

        return html;
    }

    private static final Pattern PAT_CONCAT = Pattern.compile("[\"\u201d\u201c](\\+|%2b)[\"\u201d\u201c]");

    private static String dropConcatenations(String html) {
        if ((html.indexOf('+') < 0) && (html.indexOf("%2b") < 0)) {
            return html;
        }

        Matcher m = PAT_CONCAT.matcher(html);
        if (m.find()) {
            StringBuffer sb = new StringBuffer(html.length());
            do {
                m.appendReplacement(sb, "");
            } while (m.find());
            m.appendTail(sb);
            html = sb.toString();
            sb = null;
        }

        return html;
    }

    private static final Pattern PATTERN_SCRIPT_TAG;
    private static final Pattern PATTERN_SCRIPT_TAG_START;
    private static final Pattern PATTERN_SCRIPT_TAG_END;
    static {
        String regexScriptStart = "<+[\\s]*script(?:>| [^>]*>|/[^>]*>)";
        String regexScriptEnd = "<+[\\s]*/script(?:>| [^>]*>|/[^>]*>)";
        PATTERN_SCRIPT_TAG = Pattern.compile(regexScriptStart + ".*?" + regexScriptEnd, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        PATTERN_SCRIPT_TAG_START = Pattern.compile(regexScriptStart, Pattern.CASE_INSENSITIVE);
        PATTERN_SCRIPT_TAG_END = Pattern.compile(regexScriptEnd, Pattern.CASE_INSENSITIVE);
    }

    private static String dropScriptTags(boolean[] sanitized, String html) {
        Matcher m = PATTERN_SCRIPT_TAG.matcher(html);
        if (m.find()) {
            StringBuffer sb = new StringBuffer(html.length());
            do {
                m.appendReplacement(sb, "");
                sanitized[0] = true;
            } while (m.find());
            m.appendTail(sb);
            return sb.toString();
        }

        m = PATTERN_SCRIPT_TAG_START.matcher(html);
        if (m.find()) {
            StringBuffer sb = new StringBuffer(html.length());
            do {
                m.appendReplacement(sb, "");
                sanitized[0] = true;
            } while (m.find());
            m.appendTail(sb);
            html = sb.toString();
            sb = null;
        }

        m = PATTERN_SCRIPT_TAG_END.matcher(html);
        if (m.find()) {
            StringBuffer sb = new StringBuffer(html.length());
            do {
                m.appendReplacement(sb, "");
                sanitized[0] = true;
            } while (m.find());
            m.appendTail(sb);
            html = sb.toString();
            sb = null;
        }

        return html;
    }
}
