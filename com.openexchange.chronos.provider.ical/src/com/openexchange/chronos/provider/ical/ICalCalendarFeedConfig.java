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

package com.openexchange.chronos.provider.ical;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.lang3.Validate;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import com.openexchange.chronos.provider.ical.auth.AdvancedAuthInfo;
import com.openexchange.chronos.provider.ical.auth.ICalAuthParser;
import com.openexchange.chronos.provider.ical.properties.ICalCalendarProviderProperties;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 *
 * {@link ICalCalendarFeedConfig}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ICalCalendarFeedConfig {

    private final String feedUrl;
    private final String etag;
    private final long lastUpdated;
    private final AdvancedAuthInfo authInfo;

    ICalCalendarFeedConfig(String feedUrl, String etag, long lastUpdated, AdvancedAuthInfo authInfo) {
        super();
        this.feedUrl = feedUrl;
        this.etag = etag;
        this.lastUpdated = lastUpdated;
        this.authInfo = authInfo;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public String getEtag() {
        return etag;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public AdvancedAuthInfo getAuthInfo() {
        return authInfo;
    }

    private static class Builder {

        private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICalCalendarFeedConfig.Builder.class);

        protected final String etag;
        protected final long lastUpdated;
        protected String feedUrl;

        Builder(JSONObject userConfiguration, JSONObject icalConfig) {
            JSONObject userConfig = new JSONObject(userConfiguration);

            this.feedUrl = userConfig.optString(ICalCalendarConstants.URI, null);
            Validate.notNull(this.feedUrl, "Feed URL might not be null!");
            adaptScheme();

            this.etag = icalConfig.optString(ICalCalendarConstants.ETAG, null);
            this.lastUpdated = icalConfig.optLong(ICalCalendarConstants.LAST_LAST_MODIFIED, -1L);
        }

        private void adaptScheme() {
            try {
                URI uri = new URI(this.feedUrl);
                if (ICalCalendarProviderProperties.supportedSchemes().contains(uri.getScheme()) && uri.getScheme().equalsIgnoreCase("webcal")) {
                    URIBuilder newUriBuilder = new URIBuilder(uri).setScheme("http");
                    this.feedUrl = newUriBuilder.build().toString();
                }
            } catch (URISyntaxException e) {
                LOG.error("Unable to verify and adapt scheme for URL {}.", this.feedUrl);
            }
        }
    }

    public static class EncryptedBuilder extends Builder {

        private final AdvancedAuthInfo authInfo;

        EncryptedBuilder(Session session, JSONObject userConfiguration, JSONObject icalConfig) throws OXException {
            super(userConfiguration, icalConfig);

            this.authInfo = ICalAuthParser.getInstance().getEncryptedFromDecrypted(session, userConfiguration);
        }

        public ICalCalendarFeedConfig build() {
            return new ICalCalendarFeedConfig(feedUrl, etag, lastUpdated, authInfo);
        }
    }

    public static class DecryptedBuilder extends Builder {

        private final AdvancedAuthInfo authInfo;

        DecryptedBuilder(Session session, JSONObject userConfiguration, JSONObject icalConfig) throws OXException {
            super(userConfiguration, icalConfig);

            this.authInfo = ICalAuthParser.getInstance().getDecryptedFromEncyrpted(session, userConfiguration);
        }

        public ICalCalendarFeedConfig build() {
            return new ICalCalendarFeedConfig(feedUrl, etag, lastUpdated, authInfo);
        }
    }

    /**
     * Returns whether the provided {@link ICalCalendarFeedConfig} differs in a way (endpoint, auth, ....) the whole feed has to be reloaded
     *
     * @param configToCompare the new {@link ICalCalendarFeedConfig}
     * @return <code>true</code> if the configuration is different in a way we have to update the previously persisted data; otherwise <code>false</code>
     */
    public boolean mandatoryChanges(ICalCalendarFeedConfig configToCompare) {
        if (!this.feedUrl.equals(configToCompare.feedUrl)) {
            return true;
        }
        return !this.getAuthInfo().equals(configToCompare.getAuthInfo());
    }
}
