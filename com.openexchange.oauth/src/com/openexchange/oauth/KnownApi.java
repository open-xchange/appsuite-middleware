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
import com.google.common.collect.ImmutableList;

/**
 * {@link KnownApi} - An enumeration for available APIs.
 */
public enum KnownApi implements API {
    /**
     * Twitter
     */
    TWITTER("Twitter", "com.openexchange.oauth.twitter", "twitter"),
    /**
     * LinkedIn
     */
    LINKEDIN("LinkedIn", "com.openexchange.oauth.linkedin", "linkedin"),
    /**
     * Other/unknown
     */
    OTHER("Other", "com.openexchange.oauth.other", "other"),
    /**
     * MSN
     */
    MSN("MSN", "com.openexchange.oauth.msn", "msn"),
    /**
     * Yahoo
     */
    YAHOO("Yahoo", "com.openexchange.oauth.yahoo", "yahoo"),
    /**
     * Tumblr
     */
    TUMBLR("Tumblr", "com.openexchange.oauth.tumblr", "tumblr"),
    /**
     * Flickr
     */
    FLICKR("Flickr", "com.openexchange.oauth.flickr", "flickr"),
    /**
     * Dropbox
     */
    DROPBOX("Dropbox", "com.openexchange.oauth.dropbox", "dropbox"),
    /**
     * XING
     */
    XING("XING", "com.openexchange.oauth.xing", "xing"),
    /**
     * vkontakte
     */
    VKONTAKTE("Vkontakte.ru", "com.openexchange.oauth.vkontakte", "vkontakte"),
    /**
     * Google
     */
    GOOGLE("Google", "com.openexchange.oauth.google", "google"),
    /**
     * Box.com
     */
    BOX_COM("Box.com", "com.openexchange.oauth.boxcom", "boxcom"),
    /**
     * Microsoft Live Connect
     */
    MS_LIVE_CONNECT("Microsoft Live Connect", "com.openexchange.oauth.msliveconnect", "msliveconnect"),
    /**
     * Copy.com
     */
    COPY_COM("Copy.com", "com.openexchange.oauth.copycom", "copycom"),
    ;

    private final String serviceId;
    private final String name;
    private final Collection<String> aliases;

    /**
     * Initializes a new {@link KnownApi}.
     *
     * @param shortName The short name of the API
     * @param fullName The full name of the API
     */
    private KnownApi(String shortName, String fullName, String... aliases) {
        name = shortName;
        serviceId = fullName;

        if (null == aliases || aliases.length == 0) {
            this.aliases = Collections.emptyList();
        } else {
            this.aliases = ImmutableList.copyOf(aliases);
        }
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
    public String getShortName() {
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
