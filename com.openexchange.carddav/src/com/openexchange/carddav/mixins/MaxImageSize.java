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

package com.openexchange.carddav.mixins;


import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.l;
import com.openexchange.carddav.CarddavProtocol;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link MaxImageSize}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class MaxImageSize extends SingleXMLPropertyMixin {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MaxImageSize.class);
    private static final Property MAX_IMAGE_SIZE_PROPERTY = DefaultProperty.valueOf("max_image_size", L(4194304));

	private final GroupwareCarddavFactory factory;

    /**
     * Initializes a new {@link MaxImageSize}.
     *
     * @param factory A reference to the CardDAV factory
     */
    public MaxImageSize(GroupwareCarddavFactory factory) {
        super(CarddavProtocol.CARD_NS.getURI(), "max-image-size");
        this.factory = factory;
    }

    @Override
    protected String getValue() {
        long maxSize;
        try {
            maxSize = factory.getServiceSafe(LeanConfigurationService.class).getLongProperty(MAX_IMAGE_SIZE_PROPERTY);
        } catch (OXException e) {
            maxSize = l(MAX_IMAGE_SIZE_PROPERTY.getDefaultValue(Long.class));
            LOG.warn("error reading value for \"{}\", falling back to {}.", MAX_IMAGE_SIZE_PROPERTY, L(maxSize), e);
        }
        return String.valueOf(maxSize);
    }

}
