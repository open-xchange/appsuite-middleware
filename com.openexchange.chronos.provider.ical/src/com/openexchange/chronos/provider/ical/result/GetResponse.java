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

package com.openexchange.chronos.provider.ical.result;

import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.utils.DateUtils;
import com.openexchange.chronos.Calendar;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.ContentDisposition;

/**
 *
 * {@link GetResponse}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class GetResponse {

    private final Header[] headers;
    private final GetResponseState state;
    private final URI uri;

    private ImportedCalendar importedCalendar;

    public GetResponse(URI uri, GetResponseState state, Header[] headers) {
        super();
        this.headers = headers;
        this.state = state;
        this.uri = uri;
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
        return getFirstNonEmptyValue(importedCalendar, "REFRESH-INTERVAL", "X-PUBLISHED-TTL");
    }

    public String getFeedName() {
        String name = importedCalendar.getName();
        if (Strings.isEmpty(name)) {
            name = getFirstNonEmptyValue(importedCalendar, "NAME", "X-WR-CALNAME");
        }
        if (Strings.isEmpty(name)) {
            try {
                ContentDisposition contentDisposition = new ContentDisposition(getHeader("Content-Disposition"));
                String filenameParameter = contentDisposition.getFilenameParameter();
                if (null != filenameParameter) {
                    name = FilenameUtils.getBaseName(filenameParameter);
                }
            } catch (Exception e) {
                // best effort, so ignore
            }
        }
        if (Strings.isEmpty(name)) {
            try {
                List<String> segments = Strings.splitAndTrim(uri.getPath(), "/");
                if (null != segments && 0 < segments.size()) {
                    name = FilenameUtils.getBaseName(segments.get(segments.size() - 1));
                }
            } catch (Exception e) {
                // best effort, so ignore
            }
        }
        if (Strings.isEmpty(name) || "basic".equals(name)) {
            name = "Unnamed Feed";
        }
        return name;
    }

    public String getFeedDescription() {
        return getFirstNonEmptyValue(importedCalendar, "DESCRIPTION", "X-WR-CALDESC");
    }

    public String getFeedColor() {
        return getFirstNonEmptyValue(importedCalendar, "COLOR", "X-APPLE-CALENDAR-COLOR", "X-OUTLOOK-COLOR", "X-FUNAMBOL-COLOR");
    }

    public String getETag() {
        return getHeader(HttpHeaders.ETAG);
    }

    public String getLastModified() {
        String lastModifiedHeader = getHeader(HttpHeaders.LAST_MODIFIED);
        return getTimestampAsString(lastModifiedHeader);
    }

    public String getDate() {
        String dateHeader = getHeader(HttpHeaders.DATE);
        return getTimestampAsString(dateHeader);
    }

    private String getTimestampAsString(String dateString) {
        if (Strings.isNotEmpty(dateString)) {
            Date parseDate = DateUtils.parseDate(dateString);
            if (parseDate != null) {
                return Long.toString(parseDate.getTime());
            }
        }
        return null;
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

    public GetResponseState getState() {
        return state;
    }

    /**
     * Gets the first non-empty extended property value from a specific imported calendar.
     *
     * @param calendar The calendar to get the property from
     * @param propertyNames The property names to check
     * @return The first non-empty property value, or <code>null</code> if there is none
     */
    private static String getFirstNonEmptyValue(Calendar calendar, String... propertyNames) {
        if (null != calendar && null != propertyNames) {
            for (String propertyName : propertyNames) {
                ExtendedProperty property = CalendarUtils.optExtendedProperty(calendar, propertyName);
                if (null != property) {
                    String value = String.valueOf(property.getValue());
                    if (Strings.isNotEmpty(value)) {
                        return value;
                    }
                }
            }
        }
        return null;
    }

}
