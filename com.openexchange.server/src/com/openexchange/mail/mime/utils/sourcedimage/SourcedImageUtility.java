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

package com.openexchange.mail.mime.utils.sourcedimage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.regex.MatcherReplacer;
import com.openexchange.version.VersionService;


/**
 * {@link SourcedImageUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SourcedImageUtility {

    /**
     * Initializes a new {@link SourcedImageUtility}.
     */
    private SourcedImageUtility() {
        super();
    }

    private static final Pattern PATTERN_SOURCED_IMG = Pattern.compile(
        "(<img[^>]+src=\")data:([a-zA-Z]+/[a-zA-Z-.]+);([a-zA-Z0-9-_]+),([^\"]+)(\"[^>]*/?>)",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Parses possible existing with-source images from specified HTML content and replaces such occurrences with referenced images.
     * <p>
     * <b>Note</b>: Specified {@link StringBuilder}'s content is changed to hold referenced images instead.
     *
     * @param htmlContent The HTML content
     * @return The parsed with-source images
     */
    public static Map<String, SourcedImage> hasSourcedImages(StringBuilder htmlContent) {
        final String toParse = htmlContent.toString();
        final Matcher m = PATTERN_SOURCED_IMG.matcher(toParse);
        if (!m.find()) {
            return Collections.emptyMap();
        }
        htmlContent.setLength(0);
        final MatcherReplacer mr = new MatcherReplacer(m, toParse);
        final Map<String, SourcedImage> map = new HashMap<String, SourcedImage>(4);
        final StringBuilder tmp = new StringBuilder(48);
        do {
            final String cid = tmp.append(UUIDs.getUnformattedString(UUID.randomUUID())).append('@').append(VersionService.NAME).toString();
            tmp.setLength(0);
            final String prefix = m.group(1);
            final String appendix = m.group(5);
            map.put(cid, new SourcedImage(m.group(2), m.group(3), cid, m.group(4)));
            mr.appendLiteralReplacement(htmlContent, tmp.append(prefix).append("cid:").append(cid).append(appendix).toString());
            tmp.setLength(0);
        } while (m.find());
        mr.appendTail(htmlContent);
        return map;
    }

}
