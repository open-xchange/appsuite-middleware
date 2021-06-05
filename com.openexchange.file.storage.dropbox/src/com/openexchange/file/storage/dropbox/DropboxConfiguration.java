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

package com.openexchange.file.storage.dropbox;

import com.openexchange.config.ConfigurationService;

/**
 * {@link DropboxConfiguration}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class DropboxConfiguration {

    private static final DropboxConfiguration INSTANCE = new DropboxConfiguration();

    /**
     * Gets the {@link DropboxConfiguration instance}.
     *
     * @return The instance
     */
    public static DropboxConfiguration getInstance() {
        return INSTANCE;
    }

    /*-
     * ----------------------------------------------------------------------------------
     * --------------------------------- MEMBER SECTION ---------------------------------
     * ----------------------------------------------------------------------------------
     */

    private String apiKey;
    private String secretKey;
    private String productName;

    /**
     * Initializes a new {@link DropboxConfiguration}.
     */
    private DropboxConfiguration() {
        super();
        reset();
    }

    /**
     * Resets the values
     */
    private void reset() {
        apiKey = DropboxConstants.KEY_API;
        secretKey = DropboxConstants.KEY_SECRET;
        productName = DropboxConstants.PRODUCT_NAME;
    }

    /**
     * Configures this {@link DropboxConfiguration instance} using given {@link ConfigurationService configuration service}.
     *
     * @param configurationService The configuration service
     */
    public void configure(final ConfigurationService configurationService) {
        {
            apiKey = configurationService.getProperty("com.openexchange.oauth.dropbox.apiKey", DropboxConstants.KEY_API).trim();
        }
        {
            secretKey = configurationService.getProperty("com.openexchange.oauth.dropbox.apiSecret", DropboxConstants.KEY_SECRET).trim();
        }
        {
            productName = configurationService.getProperty("com.openexchange.oauth.dropbox.productName", DropboxConstants.PRODUCT_NAME).trim();
        }
    }

    /**
     * Drops this {@link DropboxConfiguration instance}.
     */
    public void drop() {
        reset();
    }

    /**
     * Gets the API key.
     *
     * @return The API key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Gets the secret key.
     *
     * @return The secret key
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Gets the product name.
     * 
     * @return the product name
     */
    public String getProductName() {
        return productName;
    }
}
