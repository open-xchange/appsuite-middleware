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

package com.openexchange.webdav.client.jackrabbit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.tools.strings.StringParser;

/**
 * {@link WebDAVDateParser}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.4
 */
public class WebDAVDateParser implements StringParser {

    private final List<ThreadLocal<SimpleDateFormat>> dateFormats;

    public WebDAVDateParser() {
        super();
        this.dateFormats = initDateFormats();
    }

    @Override
    public <T> T parse(String s, Class<T> t) {
        if (Date.class != t || Strings.isEmpty(s)) {
            return null;
        }
        for (ThreadLocal<SimpleDateFormat> format : dateFormats) {
            try {
                return (T) format.get().parse(s);
            } catch (ParseException e) {
                // ignore & try next
            }
        }
        return null;
    }

    private static final List<ThreadLocal<SimpleDateFormat>> initDateFormats() {
        String[] patterns = {
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "yyyy-MM-dd'T'HH:mm:ss.sss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "EEE MMM dd HH:mm:ss zzz yyyy",
            "EEEEEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMMM d HH:mm:ss yyyy"
        };
        List<ThreadLocal<SimpleDateFormat>> threadLocalFormats = new ArrayList<ThreadLocal<SimpleDateFormat>>(patterns.length);
        for (String pattern : patterns) {
            threadLocalFormats.add(ThreadLocal.withInitial(() -> {
                SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
                dateFormat.setTimeZone(TimeZones.UTC);
                return dateFormat;
            }));
        }
        return Collections.unmodifiableList(threadLocalFormats);
    }

}
