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

package com.openexchange.i18n.yaml.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.i18n.yaml.I18NYamlParserService;
import com.openexchange.i18n.yaml.internal.I18NYamlParserImpl;
import com.openexchange.osgi.HousekeepingActivator;


/**
 * {@link I18NYamlParserActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class I18NYamlParserActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link I18NYamlParserActivator}.
     */
    public I18NYamlParserActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        I18NYamlParserImpl yamlParser = new I18NYamlParserImpl(this);

        // Register as OSGi service
        registerService(I18NYamlParserService.class, yamlParser);
    }

}
