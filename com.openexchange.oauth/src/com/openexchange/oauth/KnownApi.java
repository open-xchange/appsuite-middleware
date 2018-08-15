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

package com.openexchange.oauth;

import java.util.Collection;
import java.util.Collections;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.BoxApi;
import org.scribe.builder.api.CopyApi;
import org.scribe.builder.api.DropBoxApi;
import org.scribe.builder.api.FlickrApi;
import org.scribe.builder.api.Google2Api;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.builder.api.MsLiveConnectApi;
import org.scribe.builder.api.TumblrApi;
import org.scribe.builder.api.TwitterApi;
import org.scribe.builder.api.VkontakteApi;
import org.scribe.builder.api.XingApi;
import org.scribe.builder.api.YahooApi;
import com.google.common.collect.ImmutableList;

/**
 * {@link KnownApi} - An enumeration for available APIs.
 */
public enum KnownApi implements API {
    /**
     * Twitter
     */
    TWITTER("Twitter", "com.openexchange.oauth.twitter", new String[] { "twitter" }, TwitterApi.class),
    /**
     * LinkedIn
     */
    LINKEDIN("LinkedIn", "com.openexchange.oauth.linkedin", new String[] { "linkedin" }, LinkedInApi.class),
    /**
     * Other/unknown
     */
    OTHER("Other", "com.openexchange.oauth.other", new String[] { "other" }, Api.class),
    /**
     * MSN
     */
    MSN("MSN", "com.openexchange.oauth.msn", new String[] { "msn" }, MsLiveConnectApi.class),
    /**
     * Yahoo
     */
    YAHOO("Yahoo", "com.openexchange.oauth.yahoo", new String[] { "yahoo" }, YahooApi.class),
    /**
     * Tumblr
     */
    TUMBLR("Tumblr", "com.openexchange.oauth.tumblr", new String[] { "tumblr" }, TumblrApi.class),
    /**
     * Flickr
     */
    FLICKR("Flickr", "com.openexchange.oauth.flickr", new String[] { "flickr" }, FlickrApi.class),
    /**
     * Dropbox
     */
    DROPBOX("Dropbox", "com.openexchange.oauth.dropbox", new String[] { "dropbox" }, DropBoxApi.class),
    /**
     * XING
     */
    XING("XING", "com.openexchange.oauth.xing", new String[] { "xing" }, XingApi.class),
    /**
     * vkontakte
     */
    VKONTAKTE("Vkontakte.ru", "com.openexchange.oauth.vkontakte", new String[] { "vkontakte" }, VkontakteApi.class),
    /**
     * Google
     */
    GOOGLE("Google", "com.openexchange.oauth.google", new String[] { "google" }, Google2Api.class),
    /**
     * Box.com
     */
    BOX_COM("Box.com", "com.openexchange.oauth.boxcom", new String[] { "boxcom" }, BoxApi.class),
    /**
     * Microsoft Live Connect
     * 
     * @deprecated Use {@link #MICROSOFT_GRAPH} instead
     */
    MS_LIVE_CONNECT("MS Live", "Microsoft Live Connect", "com.openexchange.oauth.msliveconnect", new String[] { "msliveconnect" }, MsLiveConnectApi.class),
    /**
     * Copy.com
     */
    COPY_COM("Copy.com", "com.openexchange.oauth.copycom", new String[] { "copycom" }, CopyApi.class),
    /**
     * Microsoft Graph
     * 
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/overview">Microsoft Graph</a>
     */
    MICROSOFT_GRAPH("Microsoft", "com.openexchange.oauth.microsoft.graph", new String[] { "microsoftgraph" }, Api.class);
    ;

    private final String serviceId;
    private final String name;
    private final String displayName;
    private final Collection<String> aliases;
    private final Class<? extends Api> apiClass;

    /**
     * Initializes a new {@link KnownApi}.
     *
     * @param shortName The short name of the API
     * @param fullName The full name of the API
     */
    private KnownApi(String shortName, String fullName, String[] aliases, Class<? extends Api> apiClass) {
        name = shortName;
        serviceId = fullName;
        this.apiClass = apiClass;
        this.displayName = null;
        this.aliases = (null == aliases || aliases.length == 0) ? Collections.emptyList() : ImmutableList.copyOf(aliases);
    }

    /**
     * Initializes a new {@link KnownApi}.
     *
     * @param displayName An optional display name
     * @param shortName The short name of the API
     * @param fullName The full name of the API
     */
    private KnownApi(String displayName, String shortName, String fullName, String[] aliases, Class<? extends Api> apiClass) {
        this.displayName = displayName;
        name = shortName;
        serviceId = fullName;
        this.apiClass = apiClass;
        this.aliases = (null == aliases || aliases.length == 0) ? Collections.emptyList() : ImmutableList.copyOf(aliases);
    }

    /**
     * Gets possible alias identifiers
     *
     * @return The alias identifiers or an empty collection
     */
    public Collection<String> getAliases() {
        return aliases;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Gets the short name for displaying purposes.
     *
     * @return The short name
     */
    @Override
    public String getShortName() {
        if (displayName != null) {
            return displayName;
        }
        return name;
    }

    /**
     * Gets the full name aka. service identifier
     *
     * @return The full name
     */
    public String getFullName() {
        return serviceId;
    }

    /**
     * Gets the apiClass
     *
     * @return The apiClass
     */
    public Class<? extends Api> getApiClass() {
        return apiClass;
    }

    /**
     * Gets the known API for specified service identifier
     *
     * @param serviceId The service identifier to look-up
     * @return The known API or <code>null</code>
     */
    public static KnownApi getApiByServiceId(String serviceId) {
        if (null == serviceId) {
            return null;
        }

        for (KnownApi api : KnownApi.values()) {
            if (api.getFullName().equals(serviceId)) {
                return api;
            }
        }
        return null;
    }
}
