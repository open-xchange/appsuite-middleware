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

package com.openexchange.share.impl.groupware;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.modules.Module;

/**
 * {@link ShareModuleMapping}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class ShareModuleMapping {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareModuleMapping.class);
    private static final Map<Integer, String> moduleMapping2String = new HashMap<Integer, String>();
    private static final Map<String, Integer> moduleMapping2Int = new HashMap<String, Integer>();
    private static final Module[] GROUPWARE_MODULES = { Module.CALENDAR, Module.TASK, Module.CONTACTS, Module.INFOSTORE, Module.MAIL };

    private static boolean initizialed = false;

    /**
     * Initializes a new {@link ShareModuleMapping}.
     */
    private ShareModuleMapping() {
        super();
    }

    public static void init(ConfigurationService configService) {
        if (!initizialed) {
            /*
             * map custom modules
             */
            String mapping = configService.getProperty("com.openexchange.share.modulemapping");
            try {
                if (null != mapping && !"".equals(mapping) && !mapping.isEmpty()) {
                    for (String module : mapping.split(",")) {
                        String moduleName = module.split("=")[0];
                        String moduleId = module.split("=")[1];
                        moduleMapping2Int.put(moduleName, Integer.valueOf(moduleId));
                        moduleMapping2String.put(Integer.valueOf(moduleId), moduleName);
                    }
                }
            } catch (RuntimeException e) {
                LOG.error("Invalid value for property \"com.openexchange.share.modulemapping\": {}", e);
            }
            /*
             * map available groupware modules
             */
            for (Module module : GROUPWARE_MODULES) {
                moduleMapping2Int.put(module.getName(), I(module.getFolderConstant()));
                moduleMapping2String.put(I(module.getFolderConstant()), module.getName());
            }
            initizialed = true;
        }
    }

    /**
     * Gets a collection of the available share module identifiers.
     *
     * @return The module identifiers
     */
    public static Set<Integer> getModuleIDs() {
        return Collections.unmodifiableSet(moduleMapping2String.keySet());
    }

    public static int moduleMapping2int(String moduleName) {
        if (!initizialed) {
            LOG.warn("share module mapping has not been initialized!");
        }
        if (moduleMapping2Int.containsKey(moduleName)) {
            return i(moduleMapping2Int.get(moduleName));
        }
        return -1;
    }

    public static String moduleMapping2String(int module) {
        if (!initizialed) {
            LOG.warn("share module mapping has not been initialized!");
        }
        if (moduleMapping2String.containsKey(I(module))) {
            return moduleMapping2String.get(I(module));
        }
        return Module.UNBOUND.name();
    }
}
