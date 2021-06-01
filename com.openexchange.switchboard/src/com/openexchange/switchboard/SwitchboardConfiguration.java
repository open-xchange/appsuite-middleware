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

package com.openexchange.switchboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.lean.LeanConfigurationService;

/**
 * {@link SwitchboardConfiguration}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.4
 */
public class SwitchboardConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(SwitchboardConfiguration.class);

    private String webhookSecret;

    private String uri;

    private SwitchboardConfiguration() {
        super();
    }

    /**
     * Loads the configuration
     *
     * @param configService The LeanConfigurationService
     * @return A SwitchboardConfiguration
     */
    public static SwitchboardConfiguration getConfig(LeanConfigurationService configService, int user, int context) {
        if (configService != null) {
            SwitchboardConfiguration retval = new SwitchboardConfiguration();
            String key = configService.getProperty(user, context, SwitchboardProperties.webhookSecret);
            retval.setSharedKey(key);
            String uri = configService.getProperty(user, context, SwitchboardProperties.baseUri);
            retval.setUri(uri + "v1/webhook");

            return retval;
        }
        LOG.error("Unable to load Zoom Configuration.");

        return null;
    }

    /**
     * Gets the webhookSecret
     *
     * @return The webhookSecret
     */
    public String getWebhookSecret() {
        return webhookSecret;
    }

    /**
     * Sets the webhookSecret
     *
     * @param webhookSecret The webhookSecret to set
     */
    public void setSharedKey(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    /**
     * Gets the uri
     *
     * @return The uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the uri
     *
     * @param uri The uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
}
