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

package com.openexchange.config.utils;

import java.util.Map;
import com.openexchange.config.VariablesProvider;

/**
 * {@link SysEnvVariablesProvider} - The variables provider using system environment for look-up.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class SysEnvVariablesProvider implements VariablesProvider {

    private static final SysEnvVariablesProvider INSTANCE = new SysEnvVariablesProvider();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static SysEnvVariablesProvider getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Map<String, String> variables;

    /**
     * Initializes a new {@link SysEnvVariablesProvider}.
     */
    private SysEnvVariablesProvider() {
        super();
        variables = SysEnv.getSystemEnvironment();
    }

    @Override
    public String getForKey(String variableKey) {
        return variableKey == null ? null : variables.get(variableKey);
    }

    @Override
    public String getName() {
        return "env";
    }

}
