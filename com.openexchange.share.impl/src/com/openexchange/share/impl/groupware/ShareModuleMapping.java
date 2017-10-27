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
                        moduleMapping2Int.put(moduleName, Integer.parseInt(moduleId));
                        moduleMapping2String.put(Integer.parseInt(moduleId), moduleName);
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
