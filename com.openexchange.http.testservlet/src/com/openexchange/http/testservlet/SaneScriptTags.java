/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.http.testservlet;

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

    /**
     * Sanitizes specified HTML content by script tags
     *
     * @param html The HTML content
     * @return The sanitized HTML content
     */
    public static String saneScriptTags(final String html) {
        if (com.openexchange.java.Strings.isEmpty(html)) {
            return html;
        }
        String s = html;
        s = decode(s);
        s = dropConcatenations(s);
        s = dropScriptTags(s);
        return s;
    }

    private static final Pattern PAT_URLDECODE_ENTITIES = Pattern.compile("%([0-9a-fA-F]{2})");
    private static final Pattern PAT_URLDECODE_PERCENT = Pattern.compile("%25");
    private static final Set<String> REPLACEES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("3c", "3e", "2b", "22")));

    private static String decode(final String html) {
        if (html.indexOf('%') < 0) {
            return html;
        }
        String ret = PAT_URLDECODE_PERCENT.matcher(html).replaceAll("%");
        final Matcher m = PAT_URLDECODE_ENTITIES.matcher(ret);
        if (!m.find()) {
            return ret;
        }
        final StringBuffer sb = new StringBuffer(ret.length());
        do {
            final String entity = com.openexchange.java.Strings.toLowerCase(m.group(1));
            if (REPLACEES.contains(entity)) {
                m.appendReplacement(sb, com.openexchange.java.Strings.quoteReplacement(Character.toString((char) Integer.parseInt(m.group(1), 16))));
            } else {
                m.appendReplacement(sb, "$0");
            }
        } while (m.find());
        m.appendTail(sb);
        return sb.toString();
    }

    private static final Pattern PAT_CONCAT = Pattern.compile("[\"\u201d\u201c]\\+[\"\u201d\u201c]");

    private static String dropConcatenations(final String html) {
        final Matcher m = PAT_CONCAT.matcher(html);
        if (!m.find()) {
            return html;
        }
        final StringBuffer sb = new StringBuffer(html.length());
        do {
            m.appendReplacement(sb, "");
        } while (m.find());
        m.appendTail(sb);
        return sb.toString();
    }

    private static final Pattern PATTERN_SCRIPT_TAG = Pattern.compile(
        "<+script[^>]*>" + ".*?" + "</script>",
        Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static String dropScriptTags(final String htmlContent) {
        final Matcher m = PATTERN_SCRIPT_TAG.matcher(htmlContent);
        if (!m.find()) {
            return htmlContent;
        }
        final StringBuffer sb = new StringBuffer(htmlContent.length());
        do {
            m.appendReplacement(sb, "");
        } while (m.find());
        m.appendTail(sb);
        return sb.toString();
    }
}
