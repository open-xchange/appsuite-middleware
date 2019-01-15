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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.html.internal.jsoup;

import static com.openexchange.java.Strings.toLowerCase;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link JsoupHandlers} - Utility class for {@link JsoupHandler Jsoup handlers}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class JsoupHandlers {

    /**
     * Initializes a new {@link JsoupHandlers}.
     */
    private JsoupHandlers() {
        super();
    }

    // -------------------------------------- Image check --------------------------------------------- //

    private static final String CID = "cid:";
    private static final String DATA = "data:";
    private static final Pattern PATTERN_FILENAME = Pattern.compile("([0-9a-z&&[^.\\s>\"]]+\\.[0-9a-z&&[^.\\s>\"]]+)");

    /**
     * Checks if specified value from &lt;img&gt; tag's <code>"src"</code> attribute appears to be an inline/embedded image.
     *
     * @param src The value of the <code>"src"</code> attribute to examine
     * @param extactCheckForEmbeddedImage Whether an exact check for an embedded image should be performed
     * @return <code>true</code> for an inline/embedded image; otherwisae <code>false</code>
     */
    public static boolean isInlineImage(String src, boolean extactCheckForEmbeddedImage) {
        String tmp = toLowerCase(src);
        return tmp.startsWith(CID) || (tmp.startsWith(DATA) && (extactCheckForEmbeddedImage ? isEmbeddedImage(tmp) : true)) || PATTERN_FILENAME.matcher(tmp).matches();
    }

    /** Simple class to delay initialization until needed */
    private static class DataBase64PatternHolder {
        static final Pattern PATTERN_DATA_BASE64 = Pattern.compile("data:([\\p{L}_0-9-]+(?:/([\\p{L}_0-9-]+))?)?;base64,"); // data:image/jpeg;base64
    }

    private static boolean isEmbeddedImage(String val) {
        Matcher m = DataBase64PatternHolder.PATTERN_DATA_BASE64.matcher(val);
        return m.find() && (m.start() == 0);
    }

    // ----------------------------------------------------------------------------------------------- //

}
