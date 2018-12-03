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
    TWITTER("Twitter", "com.openexchange.oauth.twitter", "twitter", TwitterApi.class),
    /**
     * LinkedIn
     */
    LINKEDIN("LinkedIn", "com.openexchange.oauth.linkedin", "linkedin", LinkedInApi.class),
    /**
     * Other/unknown
     */
    OTHER("Other", "com.openexchange.oauth.other", "other", Api.class),
    /**
     * MSN
     */
    MSN("MSN", "com.openexchange.oauth.msn", "msn", MsLiveConnectApi.class),
    /**
     * Yahoo
     */
    YAHOO("Yahoo", "com.openexchange.oauth.yahoo", "yahoo", YahooApi2.class),
    /**
     * Tumblr
     */
    TUMBLR("Tumblr", "com.openexchange.oauth.tumblr", "tumblr", TumblrApi.class),
    /**
     * Flickr
     */
    FLICKR("Flickr", "com.openexchange.oauth.flickr", "flickr", FlickrApi.class),
    /**
     * Dropbox
     */
    DROPBOX("Dropbox", "com.openexchange.oauth.dropbox", "dropbox", DropboxApi2.class),
    /**
     * XING
     */
    XING("XING", "com.openexchange.oauth.xing", "xing", XingApi.class),
    /**
     * vkontakte
     */
    VKONTAKTE("Vkontakte.ru", "com.openexchange.oauth.vkontakte", "vkontakte", VkontakteApi.class),
    /**
     * Google
     */
    GOOGLE("Google", "com.openexchange.oauth.google", "google", Google2Api.class),
    /**
     * Box.com
     */
    BOX_COM("Box.com", "com.openexchange.oauth.boxcom", "boxcom", BoxApi.class),
    /**
     * Microsoft Live Connect
     * 
     * @deprecated Use {@link #MICROSOFT_GRAPH} instead
     */
    MS_LIVE_CONNECT("MS Live", "com.openexchange.oauth.msliveconnect", "msliveconnect", MsLiveConnectApi.class),
    /**
     * Copy.com
     */
    COPY_COM("Copy.com", "com.openexchange.oauth.copycom", "copycom", CopyApi.class),
    /**
     * Microsoft Graph
     * 
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/overview">Microsoft Graph</a>
     */
    MICROSOFT_GRAPH("Microsoft", "com.openexchange.oauth.microsoft.graph", "microsoftgraph", MicrosoftGraphApi.class);
    ;

    private final String serviceId;
    private final String displayName;
    private final String capability;
    private final Class<? extends Api> apiClass;

    /**
     * Initializes a new {@link KnownApi}.
     *
     * @param displayName The display name
     * @param serviceId The service identifier
     * @param capability The capability name
     * @param apiClass The api class
     */
    private KnownApi(String displayName, String serviceId, String capability, Class<? extends Api> apiClass) {
        this.displayName = displayName;
        this.serviceId = serviceId;
        this.capability = capability;
        this.apiClass = apiClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.API#getServiceId()
     */
    @Override
    public String getServiceId() {
        return serviceId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.API#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.API#getCapability()
     */
    @Override
    public String getCapability() {
        return capability;
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
