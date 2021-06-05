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

package com.openexchange.client.onboarding;

/**
 * {@link Link} - A link for a scenario having type set to "link".
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class Link {

    private final String url;
    private final LinkType type;
    private final String imageUrl;

    /**
     * Initializes a new {@link Link} without an image.
     *
     * @param url The URL of the link
     * @param type The link type
     */
    public Link(String url, LinkType type) {
        this(url, type, null);
    }

    /**
     * Initializes a new {@link Link} with an image.
     *
     * @param url The URL of the link
     * @param type The link type
     * @param imageUrl The image URL for this link
     */
    public Link(String url, LinkType type, String imageUrl) {
        super();
        this.url = url;
        this.type = type;
        this.imageUrl = imageUrl;
    }

    /**
     * Gets the type
     *
     * @return The type
     */
    public LinkType getType() {
        return type;
    }

    /**
     * Gets the URL of the link
     *
     * @return The URL of the link
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the image URL (if any)
     *
     * @return The image URL or <code>null</code>
     */
    public String getImageUrl() {
        return imageUrl;
    }

}
