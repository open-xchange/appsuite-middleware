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

package com.openexchange.saml;

import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.saml.spi.DefaultConfig;

/**
 * {@link AbstractDefaultConfigAwareSAMLConfig} is a {@link SAMLConfig} which is also aware of the {@link DefaultConfig}.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public abstract class AbstractDefaultConfigAwareSAMLConfig implements SAMLConfig {

    private final DefaultConfig config;

    /**
     * Initializes a new {@link AbstractDefaultConfigAwareSAMLConfig}.
     *
     * @param configService The {@link LeanConfigurationService}
     * @throws OXException if the initialization of the {@link DefaultConfig} fails
     */
    public AbstractDefaultConfigAwareSAMLConfig(LeanConfigurationService configService) throws OXException {
        super();
        config = DefaultConfig.init(configService);
    }

    /**
     * Gets the default {@link SAMLConfig}. See {@link DefaultConfig} for more infos.
     *
     * @return The default {@link SAMLConfig}
     */
    protected SAMLConfig getDefaultConfig() {
        return config;
    }

}
