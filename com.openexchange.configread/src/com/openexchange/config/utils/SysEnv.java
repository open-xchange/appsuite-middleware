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

/**
 * {@link SysEnv} - Provides access to already obtained unmodifiable string map view of the current system environment.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class SysEnv {

    /**
     * Initializes a new {@link SysEnv}.
     */
    private SysEnv() {
        super();
    }

    private static final Map<String, String> SYS_ENV = System.getenv();


    /**
     * Gets already obtained unmodifiable string map view of the current system environment.
     * <p>
     * Avoids the need to pass security manager on every invocation of {@link System#getenv()}.
     *
     * @return The unmodifiable string map view of the current system environment
     */
    public static Map<String, String> getSystemEnvironment() {
        return SYS_ENV;
    }

}
