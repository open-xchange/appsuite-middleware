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

package com.openexchange.test.common.asset;

/**
 * {@link Asset}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Asset {

    private final AssetType assetType;
    private final String absolutePath;
    private final String filename;

    /**
     * Initialises a new {@link Asset}.
     * 
     * @param assetType The asset's type
     * @param absolutePath The absolute path of the asset
     * @param filename The filename of the asset
     */
    public Asset(AssetType assetType, String absolutePath, String filename) {
        super();
        this.assetType = assetType;
        this.absolutePath = absolutePath;
        this.filename = filename;
    }

    /**
     * Gets the assetType
     *
     * @return The assetType
     */
    public AssetType getAssetType() {
        return assetType;
    }

    /**
     * Gets the absolutePath
     *
     * @return The absolutePath
     */
    public String getAbsolutePath() {
        return absolutePath;
    }

    /**
     * Gets the filename
     *
     * @return The filename
     */
    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Asset [assetType=").append(assetType).append(", absolutePath=").append(absolutePath).append(", filename=").append(filename).append("]");
        return builder.toString();
    }
}
