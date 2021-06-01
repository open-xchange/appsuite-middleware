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

package com.openexchange.oauth;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.BoxApi;
import org.scribe.builder.api.CopyApi;
import org.scribe.builder.api.FlickrApi;
import org.scribe.builder.api.Google2Api;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.builder.api.MsLiveConnectApi;
import org.scribe.builder.api.TumblrApi;
import org.scribe.builder.api.TwitterApi;
import org.scribe.builder.api.VkontakteApi;
import org.scribe.builder.api.XingApi;
import com.openexchange.oauth.api.DropboxApi2;
import com.openexchange.oauth.api.MicrosoftGraphApi;
import com.openexchange.oauth.api.YahooApi2;

/**
 * {@link KnownApi} - An enumeration for available APIs.
 */
public enum KnownApi implements API {
    /**
     * Twitter
     */
    TWITTER("Twitter", "com.openexchange.oauth.twitter", "twitter", "api.twitter.com", TwitterApi.class),
    /**
     * LinkedIn
     */
    LINKEDIN("LinkedIn", "com.openexchange.oauth.linkedin", "linkedin", "api.linkedin.com", LinkedInApi.class),
    /**
     * Other/unknown
     */
    OTHER("Other", "com.openexchange.oauth.other", "other", "", Api.class),
    /**
     * MSN
     */
    MSN("MSN", "com.openexchange.oauth.msn", "msn", "apis.live.net", MsLiveConnectApi.class),
    /**
     * Yahoo
     */
    YAHOO("Yahoo", "com.openexchange.oauth.yahoo", "yahoo", "social.yahooapis.com", YahooApi2.class),
    /**
     * Tumblr
     */
    TUMBLR("Tumblr", "com.openexchange.oauth.tumblr", "tumblr", "www.tumblr.com", TumblrApi.class),
    /**
     * Flickr
     */
    FLICKR("Flickr", "com.openexchange.oauth.flickr", "flickr", "api.flickr.com", FlickrApi.class),
    /**
     * Dropbox
     */
    DROPBOX("Dropbox", "com.openexchange.oauth.dropbox", "dropbox", "api.dropbox.com", DropboxApi2.class),
    /**
     * XING
     */
    XING("XING", "com.openexchange.oauth.xing", "xing", "api.xing.com", XingApi.class),
    /**
     * vkontakte
     */
    VKONTAKTE("Vkontakte.ru", "com.openexchange.oauth.vkontakte", "vkontakte", "api.vkontakte.ru", VkontakteApi.class),
    /**
     * Google
     */
    GOOGLE("Google", "com.openexchange.oauth.google", "google", "www.googleapis.com", Google2Api.class),
    /**
     * Box.com
     */
    BOX_COM("Box.com", "com.openexchange.oauth.boxcom", "boxcom", "app.box.com", BoxApi.class),
    /**
     * Copy.com
     */
    COPY_COM("Copy.com", "com.openexchange.oauth.copycom", "copycom", "api.copy.com", CopyApi.class),
    /**
     * Microsoft Graph
     *
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/overview">Microsoft Graph</a>
     */
    MICROSOFT_GRAPH("Microsoft", "com.openexchange.oauth.microsoft.graph", "microsoftgraph", "graph.microsoft.com", MicrosoftGraphApi.class),
    ;

    private final String serviceId;
    private final String displayName;
    private final String capability;
    private final String url;
    private final Class<? extends Api> apiClass;

    /**
     * Initializes a new {@link KnownApi}.
     *
     * @param displayName The display name
     * @param serviceId The service identifier
     * @param capability The capability name
     * @param url The API's URL
     * @param apiClass The api class
     */
    private KnownApi(String displayName, String serviceId, String capability, String url, Class<? extends Api> apiClass) {
        this.displayName = displayName;
        this.serviceId = serviceId;
        this.capability = capability;
        this.url = url;
        this.apiClass = apiClass;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getCapability() {
        return capability;
    }

    @Override
    public String getURL() {
        return url;
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
            if (api.getServiceId().equals(serviceId)) {
                return api;
            }
        }
        return null;
    }
}
