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

package com.openexchange.jslob.config;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;

/**
 * {@link Reloadable} implementation for appsuite related properties files
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class ConfigJSlobReloadable implements Reloadable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigJSlobReloadable.class);

    private final ConfigJSlobService configJSlobService;

    public ConfigJSlobReloadable(ConfigJSlobService configJSlobService) {
        this.configJSlobService = configJSlobService;
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            configJSlobService.initPreferenceItems();
            configJSlobService.initConfigTree(configService);
        } catch (OXException oxException) {
            LOG.error("Error while configuration reload!", oxException);
        }
    }

    @Override
    public Interests getInterests() {
        return Reloadables.getInterestsForAll();
    }

}
