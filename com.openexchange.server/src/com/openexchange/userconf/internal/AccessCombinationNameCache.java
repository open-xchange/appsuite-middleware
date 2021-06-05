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

package com.openexchange.userconf.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link AccessCombinationNameCache}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class AccessCombinationNameCache {

    private static final Logger LOG = LoggerFactory.getLogger(AccessCombinationNameCache.class);

    private HashMap<String, UserModuleAccess> named_access_combinations = null;

    public synchronized void initAccessCombinations() throws OXException {
        if (named_access_combinations == null) {
                LOG.info("Processing access combinations...");
                named_access_combinations = getAccessCombinations(loadValidAccessModules(), loadAccessCombinations());
                LOG.info("Access combinations processed!");
        }

    }

    public synchronized String getNameForAccessCombination(final UserModuleAccess access_combination) {
        for (final Entry<String, UserModuleAccess> entry : named_access_combinations.entrySet()) {
            if (entry.getValue().equals(access_combination)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private HashMap<String, Method> loadValidAccessModules() throws OXException {

        // If we wanna blacklist some still unused modules, add them here!
        HashSet<String> BLACKLIST = new HashSet<>();
        // BLACKLIST.add("");

        try {

            // Load the available combinations directly from the
            // usermoduleaccess object
            Class<?> tmp = Class.forName(UserModuleAccess.class.getCanonicalName());
            Method methlist[] = tmp.getDeclaredMethods();
            HashMap<String, Method> module_method_mapping = new HashMap<>();

            LOG.debug("Listing available modules for use in access combinations...");
            for (Method method : methlist) {
                String meth_name = method.getName();
                if (meth_name.startsWith("set")) {
                    // remember all valid modules and its set methods
                    String module_name = meth_name.substring(3, meth_name.length()).toLowerCase();
                    if (!BLACKLIST.contains(module_name)) {
                        module_method_mapping.put(module_name, method);
                        LOG.debug(module_name);
                    }
                }
            }
            LOG.debug("End of list for access combinations!");
            return module_method_mapping;
        } catch (ClassNotFoundException e) {
            LOG.error("UserModuleAccess class not found!", e);
            throw new OXException(e);
        }
    }

    private static Properties loadAccessCombinations() {
        // Load properties from file , if does not exists use fallback
        // properties!
        final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null == service) {
            throw new IllegalStateException("Absent service: " + ConfigurationService.class.getName());
        }
        return service.getFile("ModuleAccessDefinitions.properties");
    }

    private HashMap<String, UserModuleAccess> getAccessCombinations(HashMap<String, Method> module_method_mapping, Properties access_props) throws OXException {
        HashMap<String, UserModuleAccess> combis = new HashMap<>();
        // Now check if predefined combinations are valid
        Enumeration<Object> predefined_access_combinations = access_props.keys();
        while (predefined_access_combinations.hasMoreElements()) {
            String predefined_combination_name = (String) predefined_access_combinations.nextElement();
            String predefined_modules = (String) access_props.get(predefined_combination_name);
            if (predefined_modules != null) {
                UserModuleAccess us = new UserModuleAccess();
                us.disableAll();
                us.setGlobalAddressBookDisabled(false); // by default this is enabled.
                String[] modules = Strings.isEmpty(predefined_modules) ? new String[0] : predefined_modules.split(" *, *");
                for (String module : modules) {
                    module = module.trim();
                    Method meth = module_method_mapping.get(module);
                    if (null == meth) {
                        LOG.error("Predefined combination \"{}\" contains invalid module \"{}\" ", predefined_combination_name, module);
                        // AS DEFINED IN THE CONTEXT WIDE ACCES SPECIFICAION ,
                        // THE SYSTEM WILL STOP IF IT FINDS AN INVALID
                        // CONFIGURATION!

                        throw new OXException(new Exception("Invalid access combinations found in config file!"));
                    }
                    try {
                        meth.invoke(us, Boolean.TRUE);
                    } catch (IllegalArgumentException e) {
                        LOG.error("Illegal argument passed to method!", e);
                    } catch (IllegalAccessException e) {
                        LOG.error("Illegal access!", e);
                    } catch (InvocationTargetException e) {
                        LOG.error("Invocation target error!", e);
                    }
                }
                // add moduleaccess object to hashmap/list identified by
                // combinations name
                combis.put(predefined_combination_name, us);
            }
        }

        return combis;
    }

}
