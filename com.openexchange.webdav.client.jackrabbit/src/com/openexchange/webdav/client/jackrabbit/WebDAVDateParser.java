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
