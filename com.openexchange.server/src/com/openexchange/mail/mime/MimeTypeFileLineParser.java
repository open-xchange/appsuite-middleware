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

package com.openexchange.mail.mime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link MimeTypeFileLineParser} - Parses entries in MIME type files like:
 *
 * <pre>
 * type=magnus-internal/cgi	exts=cgi,exe,bat
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeTypeFileLineParser {

    private static final Pattern PAT_VAL = Pattern.compile("(?:[^\"][\\p{L}&&[^\\s\"]]*|\"[\\p{L}&&[^\"]]+\")");

    private String type;

    private final List<String> extensions;

    /**
     * Initializes a new MIME type file's line parser
     *
     * @param entry The MIME type file entry; e.g. <code>type=magnus-internal/cgi&nbsp;&nbsp;&nbsp;&nbsp;exts=cgi,exe,bat</code>
     */
    public MimeTypeFileLineParser(String entry) {
        super();
        extensions = new ArrayList<String>();
        parse(entry);
    }

    private static final String STR_TYPE = "type=";

    private static final String STR_EXTS = "exts=";

    private void parse(String entry) {
        int pos = -1;
        if ((pos = entry.toLowerCase(Locale.ENGLISH).indexOf(STR_TYPE)) != -1) {
            final Matcher m = PAT_VAL.matcher(entry);
            final int start = pos + 5;
            if (m.find(start) && (m.start() == start)) {
                type = m.group();
            }
        }
        if ((pos = entry.toLowerCase(Locale.ENGLISH).indexOf(STR_EXTS)) != -1) {
            final Matcher m = PAT_VAL.matcher(entry);
            final int start = pos + 5;
            if (m.find(start) && (m.start() == start)) {
                final String sExts = m.group();
                final String[] exts;
                if ((sExts.charAt(0) == '"') && (sExts.charAt(sExts.length() - 1) == '"')) {
                    exts = sExts.substring(1, sExts.length() - 1).split("[ \t\n\r\f]*,[ \t\n\r\f]*");
                } else {
                    exts = m.group().split("[ \t\n\r\f]*,[ \t\n\r\f]*");
                }
                extensions.addAll(Arrays.asList(exts));
            }
        }
    }

    /**
     * Gets the extensions
     *
     * @return the extensions
     */
    public List<String> getExtensions() {
        return Collections.unmodifiableList(extensions);
    }

    /**
     * Gets the type
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

}
