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

package com.openexchange.html.internal.jsoup;

import static com.openexchange.java.Strings.toLowerCase;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.java.Strings;

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
        if (Strings.isEmpty(src)) {
            return false;
        }
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
