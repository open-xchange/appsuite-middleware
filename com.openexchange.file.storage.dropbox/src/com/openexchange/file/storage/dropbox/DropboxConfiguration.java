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
