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

package com.openexchange.mail.mime.utils.sourcedimage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.java.util.UUIDs;
import com.openexchange.version.Version;
import com.openexchange.tools.regex.MatcherReplacer;


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
    public static Map<String, SourcedImage> hasSourcedImages(final StringBuilder htmlContent) {
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
            final String cid = tmp.append(UUIDs.getUnformattedString(UUID.randomUUID())).append('@').append(Version.NAME).toString();
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
