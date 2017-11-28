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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.provider.ical.result;

import java.util.Collections;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 *
 * {@link GetResponse}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class GetResponse {

    private ImportedCalendar importedCalendar;

    private final Header[] headers;

    private final GetResponseState state;

    public GetResponse(GetResponseState state, Header[] headers) {
        this.headers = headers;
        this.state = state;
    }

    public void setCalendar(ImportedCalendar calendar) {
        this.importedCalendar = calendar;
    }

    /**
     * Returns the imported calendar if there have been updates. Otherwise <code>null</code> will be returned.
     *
     * @return {@link ImportedCalendar} with the available {@link Event}s or <code>null</code> if there haven't been updates.
     */
    public ImportedCalendar getCalendar() {
        return importedCalendar;
    }

    public List<OXException> getWarnings() {
        return importedCalendar != null && importedCalendar.getWarnings() != null ? importedCalendar.getWarnings() : Collections.emptyList();
    }

    public String getRefreshInterval() {
        return getProperty("REFRESH-INTERVAL");
    }

    public String getFeedName() {
        String property = getProperty("X-WR-CALNAME");
        if (Strings.isNotEmpty(property)) {
            return property;
        }
        return importedCalendar.getName();
    }

    public String getFeedDescription() {
        String property = getProperty("X-WR-CALDESC");
        if (Strings.isNotEmpty(property)) {
            return property;
        }
        return getProperty("DESCRIPTION");
    }

    public String getETag() {
        return getHeader(HttpHeaders.ETAG);
    }

    public String getLastModified() {
        return getHeader(HttpHeaders.LAST_MODIFIED);
    }

    public String getDate() {
        return getHeader(HttpHeaders.DATE);
    }

    public String getContentLength() {
        return getHeader(HttpHeaders.CONTENT_LENGTH);
    }

    private String getHeader(String name) {
        if (null != headers && 0 < headers.length) {
            for (Header header : headers) {
                if (name.equalsIgnoreCase(header.getName())) {
                    return header.getValue();
                }
            }
        }
        return null;
    }

    private String getProperty(String property) {
        ExtendedProperty extendedProperty = CalendarUtils.optExtendedProperty(this.importedCalendar, property);
        if (extendedProperty == null) {
            return null;
        }
        return String.valueOf(extendedProperty.getValue());
    }

    public GetResponseState getState() {
        return state;
    }
}
