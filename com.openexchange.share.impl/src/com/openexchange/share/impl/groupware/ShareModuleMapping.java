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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.util.HashMap;
import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.modules.Module;


/**
 * {@link ShareModuleMapping}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.6.1
 */
public class ShareModuleMapping {

    private static final ShareModuleMapping INSTANCE = new ShareModuleMapping();

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ShareModuleMapping.class);

    private static final Map<Integer, String> moduleMapping2String = new HashMap<Integer, String>();
    private static final Map<String, Integer> moduleMapping2Int = new HashMap<String, Integer>();
    private static boolean initizialed = false;

    /**
     * Initializes a new {@link ShareModuleMapping}.
     */
    private ShareModuleMapping() {
    }

    public static void init(ConfigurationService configService) {
        if (!initizialed) {
            String mapping = configService.getProperty("com.openexchange.share.modulemapping");
            try {
                if (null != mapping && !"".equals(mapping) && !mapping.isEmpty()) {
                    mapping = mapping.replace("\"", "");
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
            initizialed = true;
        }
    }

    public static ShareModuleMapping getShareModuleMapping() {
        return INSTANCE;
    }

    public static int moduleMapping2int(String moduleName) {
        if (!initizialed) {
            LOG.warn("share module mapping has not been initialized!");
        }
        int mod = Module.getModuleInteger(moduleName);
        if (-1 != mod) {
            return mod;
        }
        if (moduleMapping2Int.containsKey(moduleName)) {
            return moduleMapping2Int.get(moduleName);
        }
        return -1;
    }

    public static String moduleMapping2String(int module) {
        if (!initizialed) {
            LOG.warn("share module mapping has not been initialized!");
        }
        Module mod = Module.getForFolderConstant(module);
        if (null != mod) {
            return mod.getName();
        }
        if (moduleMapping2String.containsKey(module)) {
            return moduleMapping2String.get(module);
        }
        return Module.UNBOUND.name();
    }
}
