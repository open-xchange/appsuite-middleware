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

package com.openexchange.client.onboarding.internal;

import com.openexchange.client.onboarding.LinkType;
import com.openexchange.client.onboarding.internal.ConfiguredLinkImage.Type;

/**
 * {@link ConfiguredLink} - A link for a scenario having type set to "link".
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ConfiguredLink {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfiguredLink.class);

    private final String url;
    private final LinkType type;
    private final boolean property;
    private final ConfiguredLinkImage image;

    /**
     * Initializes a new {@link ConfiguredLink}.
     *
     * @param link The link
     * @param property Whether the link denotes a property
     * @param type The link type
     * @param imageInfo The optional image info
     */
    public ConfiguredLink(String link, boolean property, LinkType type, String imageInfo) {
        super();
        this.url = link;
        this.type = type;
        this.property = property;

        if (null == imageInfo) {
            image = null;
        } else {
            int pos = imageInfo.indexOf("://");
            if (pos <= 0) {
                image = new ConfiguredLinkImage(imageInfo, Type.RESOURCE);
            } else {
                String scheme = imageInfo.substring(0, pos);
                String name = imageInfo.substring(pos + 3);
                Type iType = Type.typeFor(scheme);
                if (iType == null) {
                    LOG.warn("Unknown image type: {}", scheme);
                    image = null;
                } else {
                    image = new ConfiguredLinkImage(name, iType);
                }
            }
        }
    }

    /**
     * Gets the image
     *
     * @return The image
     */
    public ConfiguredLinkImage getImage() {
        return image;
    }

    /**
     * Gets the URL or property name.
     *
     * @return The URL or property name
     */
    public String getUrl() {
        return url;
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
     * Gets the property flag
     *
     * @return The property flag
     */
    public boolean isProperty() {
        return property;
    }

}
