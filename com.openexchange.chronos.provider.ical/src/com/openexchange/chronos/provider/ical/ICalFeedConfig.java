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

package com.openexchange.chronos.provider.ical;

import org.json.JSONObject;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.caching.CachingCalendarAccess;

/**
 *
 * {@link ICalFeedConfig}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ICalFeedConfig {

    public static final String ETAG = "etag";

    private final String feedUrl;
    private long allowedMaxSize;
    private final String etag;
    private final long lastUpdated;

    private ICalFeedConfig(String feedUrl, String etag, long lastUpdated) {
        super();
        this.feedUrl = feedUrl;
        this.etag = etag;
        this.lastUpdated = lastUpdated;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public long getAllowedMaxSize() {
        return allowedMaxSize;
    }

    public void setAllowedMaxSize(long allowedMaxSize) {
        this.allowedMaxSize = allowedMaxSize;
    }

    public String getEtag() {
        return etag;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public static class Builder {

        private final String feedUrl;
        private final String etag;
        private final long lastUpdated;

        Builder(CalendarAccount account, JSONObject folderConfig) {
            feedUrl = account.getUserConfiguration().optString("uri", null);
            etag = folderConfig.optString(ETAG, null);
            lastUpdated = folderConfig.optLong(CachingCalendarAccess.LAST_UPDATE, -1L);
        }

        public ICalFeedConfig build() {
            return new ICalFeedConfig(feedUrl, etag, lastUpdated);
        }
    }
}
