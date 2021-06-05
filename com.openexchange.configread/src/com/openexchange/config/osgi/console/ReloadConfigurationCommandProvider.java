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

package com.openexchange.config.osgi.console;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import com.openexchange.config.internal.ConfigurationImpl;
import com.openexchange.java.Strings;

/**
 * {@link ReloadConfigurationCommandProvider}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @since 7.10.3
 */
public class ReloadConfigurationCommandProvider implements CommandProvider {

    private final ConfigurationImpl configService;

    public ReloadConfigurationCommandProvider(ConfigurationImpl configService) {
        super();
        this.configService = configService;
    }

    @Override
    public String getHelp() {
        return "reload - Reloads the configuration." + Strings.getLineSeparator();
    }

    public void _reload(CommandInterpreter commandInterpreter) {
        configService.reloadConfiguration();
        commandInterpreter.println("Configuration reloaded.");
    }
}
