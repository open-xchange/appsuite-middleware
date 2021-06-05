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

package com.openexchange.imageconverter.api;

import java.awt.Dimension;
import java.util.Collection;
import org.json.JSONObject;

/**
 * {@link IMetadata}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10
 */
public interface IMetadata {

    /**
     * Retrieving the physical extent of the image in pixels.
     *
     * @return The width and height of the image in pixels.
     */
    public Dimension getImageDimension();

    /**
     * Retrieving the lower case image format short name.
     *
     * @return The lower case image format short name.
     */
    public String getImageFormatName();


    /**
     * Retrieving the {@link IMetadataGroup} interface with the given
     *  {@link MetadataKey}.
     *
     * @param metadataGroup The type of the requested {@link IMetadataGroup}.
     * @return The requested {@link IMetadataGroup} interface.
     */
    public IMetadataGroup getMetadataGroup(final MetadataGroup metadataGroup);

    /**
     * Retrieving the {@link Collection} of all {@link IMetadataGroup} interfaces.
     *
     * @return The {@link IMetadataGroup} interface {@link Collection}.
     */
    public Collection<IMetadataGroup> getMetadataGroups();

    /**
     * Retrieving the content of all {@link IMetadataGroup}s as
     *  {@link JSONObject}.
     *
     * @return The {@link JSONObject}, containing all metadata information
     *  as textual representation.
     */
    public JSONObject getJSONObject();
}
