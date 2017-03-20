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
