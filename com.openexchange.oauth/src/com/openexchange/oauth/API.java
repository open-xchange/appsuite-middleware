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

/**
 * {@link API} - An enumeration for available APIs.
 */
public enum API {
    /**
     * Twitter
     */
    TWITTER("Twitter", "com.openexchange.oauth.twitter"),
    /**
     * LinkedIn
     */
    LINKEDIN("LinkedIn", "com.openexchange.oauth.linkedin"),
    /**
     * Other/unknown
     */
    OTHER("Other", "com.openexchange.oauth.other"),
    /**
     * MSN
     */
    MSN("MSN", "com.openexchange.oauth.msn"),
    /**
     * Yahoo
     */
    YAHOO("Yahoo", "com.openexchange.oauth.yahoo"),
    /**
     * Tumblr
     */
    TUMBLR("Tumblr", "com.openexchange.oauth.tumblr"),
    /**
     * Flickr
     */
    FLICKR("Flickr", "com.openexchange.oauth.flickr"),
    /**
     * Dropbox
     */
    DROPBOX("Dropbox", "com.openexchange.oauth.dropbox"),
    /**
     * XING
     */
    XING("XING", "com.openexchange.oauth.xing"),
    /**
     * vkontakte
     */
    VKONTAKTE("Vkontakte.ru", "com.openexchange.oauth.vkontakte"),
    /**
     * Google
     */
    GOOGLE("Google", "com.openexchange.oauth.google"),
    /**
     * Box.com
     */
    BOX_COM("Box.com", "com.openexchange.oauth.boxcom"),
    /**
     * Microsoft Live Connect
     */
    MS_LIVE_CONNECT("Microsoft Live Connect", "com.openexchange.oauth.msliveconnect"),
    /**
     * Copy.com
     */
    COPY_COM("Copy.com", "com.openexchange.oauth.copycom"),
    /**
     * SurDoc
     */
    SURDOC("SurDoc", "com.openexchange.oauth.surdoc"),
    ;

    private final String shortName;
    private final String fullName;

    /**
     * Initialises a new {@link API}.
     * 
     * @param shortName The short name of the API
     * @param fullName The full name of the API
     */
    private API(String shortName, String fullName) {
        this.shortName = shortName;
        this.fullName = fullName;
    }

    /**
     * Gets the shortName
     *
     * @return The shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Gets the fullName
     *
     * @return The fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Resolves the specified service identifier to a known OAuth {@link API}
     * 
     * @param serviceId The service identifier to resolve
     * @return The resolved OAuth {@link API}
     * @throws IllegalArgumentException if the specified service identifier cannot be resolved to any known OAuth {@link API}
     */
    public static API resolveFromServiceId(String serviceId) {
        for (API api : values()) {
            if (api.fullName.equals(serviceId)) {
                return api;
            }
        }
        throw new IllegalArgumentException("The serviceId '" + serviceId + "' cannot be resolved to any known OAuth API");
    }
}
