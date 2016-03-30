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

package com.openexchange.xing;

import java.util.EnumMap;
import org.json.JSONObject;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.session.Session;

/**
 * User profile data includes URLs linking to different versions of the
 * user's profile photo as part of the photo_urls attribute.
 * These are:
 * <ul>
 *   <li>large (140x185 pixels)</li>
 *   <li>mini_thumb (18x24 pixels)</li>
 *   <li>thumb (30x40 pixels)</li>
 *   <li>medium_thumb (57x75 pixels)</li>
 *   <li>maxi_thumb (70x93 pixels)</li>
 * </ul>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class PhotoUrls {

    public static enum Type {
        /*
         * Always order from smallest to largest
         */
        MINI_THUMB("mini_thumb"),
        THUMB("thumb"),
        MEDIUM_THUMB("medium_thumb"),
        MAXI_THUMB("maxi_thumb"),
        LARGE("large");

        private final String key;
        private Type(String key) {
            this.key = key;
        }

        public String getJsonKey() {
            return key;
        }
    }

    private final EnumMap<Type, String> urls = new EnumMap<Type, String>(Type.class);

    public PhotoUrls() {
        super();
    }

    public PhotoUrls(JSONObject jPhotoUrls) {
        super();
        for (Type type : Type.values()) {
            String url = jPhotoUrls.optString(type.getJsonKey());
            if (url != null) {
                urls.put(type, url);
            }
        }
    }

    /**
     * Gets the photo url by its type.
     *
     * @param type The {@link Type}
     * @return The url or <code>null</code> if not available
     */
    public String getUrl(Type type) {
        return urls.get(type);
    }

    /**
     * Gets the photo url for type {@link Type#MINI_THUMB}.
     *
     * @return The url or <code>null</code> if not available
     */
    public String getMiniThumbUrl() {
        return urls.get(Type.MINI_THUMB);
    }

    /**
     * Gets the photo url for type {@link Type#THUMB}.
     *
     * @return The url or <code>null</code> if not available
     */
    public String getThumbUrl() {
        return urls.get(Type.THUMB);
    }

    /**
     * Gets the photo url for type {@link Type#MEDIUM_THUMB}.
     *
     * @return The url or <code>null</code> if not available
     */
    public String getMediumThumbUrl() {
        return urls.get(Type.MEDIUM_THUMB);
    }

    /**
     * Gets the photo url for type {@link Type#MAXI_THUMB}.
     *
     * @return The url or <code>null</code> if not available
     */
    public String getMaxiThumbUrl() {
        return urls.get(Type.MAXI_THUMB);
    }

    /**
     * Gets the photo url for type {@link Type#LARGE}.
     *
     * @return The url or <code>null</code> if not available
     */
    public String getLargeUrl() {
        return urls.get(Type.LARGE);
    }

    /**
     * Gets the url for the smallest available photo.
     *
     * @return The url or <code>null</code> if no photo is available
     */
    public String getSmallestAvailableUrl() {
        for (Type type : Type.values()) {
            String url = urls.get(type);
            if (url != null) {
                return url;
            }
        }

        return null;
    }

    /**
     * Gets the url for the largest available photo.
     *
     * @return The url or <code>null</code> if no photo is available
     */
    public String getLargestAvailableUrl() {
        for (Type type : reverse(Type.values())) {
            String url = urls.get(type);
            if (url != null) {
                return url;
            }
        }

        return null;
    }

    /**
     * Sets the url for the given type.
     *
     * @param type The {@link Type}
     * @param url The url
     */
    public void setUrl(Type type, String url) {
        urls.put(type, url);
    }

    /**
     * Convenience method to load a users photo via {@link XingAPI}.
     * <pre>
     * User user = api.userInfo(userId);
     * IFileHolder photo = user.getPhotoUrls.load(api, Type.THUMB);
     * </pre>
     *
     * @param api A valid {@link XingAPI} object; never <code>null</code;
     * @param type The photo type; never <code>null</code;
     * @return A {@link IFileHolder} containing the photo or <code>null</code>,
     * if the photo url was not available for the given type.
     * @throws XingException if an error occurs while loading the photo from XING.
     */
    public IFileHolder load(XingAPI<? extends Session> api, Type type) throws XingException {
        String url = urls.get(type);
        if (url == null) {
            return null;
        }

        return api.getPhoto(url);
    }

    /**
     * Convenience method to load the smallest available photo of a user via {@link XingAPI}.
     *
     * @param api A valid {@link XingAPI} object; never <code>null</code;
     * @return A {@link IFileHolder} containing the photo or <code>null</code>,
     * if no photo url was available.
     * @throws XingException if an error occurs while loading the photo from XING.
     */
    public IFileHolder loadSmallest(XingAPI<? extends Session> api) throws XingException {
        String url = getSmallestAvailableUrl();
        if (url == null) {
            return null;
        }

        return api.getPhoto(url);
    }

    /**
     * Convenience method to load the largest available photo of a user via {@link XingAPI}.
     *
     * @param api A valid {@link XingAPI} object; never <code>null</code;
     * @return A {@link IFileHolder} containing the photo or <code>null</code>,
     * if no photo url was available.
     * @throws XingException if an error occurs while loading the photo from XING.
     */
    public IFileHolder loadLargest(XingAPI<? extends Session> api) throws XingException {
        String url = getLargestAvailableUrl();
        if (url == null) {
            return null;
        }

        return api.getPhoto(url);
    }

    private static Type[] reverse(Type[] values) {
        Type[] reverse = new Type[values.length];
        for (int i = 0; i < values.length; i++) {
            reverse[(values.length - 1) - i] = values[i];
        }

        return reverse;
    }

}
