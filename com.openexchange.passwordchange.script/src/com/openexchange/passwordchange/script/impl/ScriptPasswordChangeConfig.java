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

package com.openexchange.passwordchange.script.impl;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.DefaultInterests.Builder;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.passwordchange.script.services.SPWServiceRegistry;

/**
 * {@link ScriptPasswordChangeConfig} Collects and exposes configuration
 * parameters needed by the ScriptPasswordChange.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ScriptPasswordChangeConfig implements Reloadable {

    private final static String CONFIG_FILE_NAME = "change_pwd_script.properties";
    private final static String SCRIPT_PATH_KEY = "com.openexchange.passwordchange.script.shellscript";
    private final static String BASE64_KEY = "com.openexchange.passwordchange.script.base64";

    private static String scriptPath;
    private static boolean asBase64;

    private static ScriptPasswordChangeConfig INSTANCE;

    /**
     * Prevent initialization of multiple {@link ScriptPasswordChangeConfig}.
     */
    private ScriptPasswordChangeConfig() {}

    /**
     * Get an initialized instance of {@link ScriptPasswordChangeConfig}
     * 
     * @return an initialized instance of {@link ScriptPasswordChangeConfig}
     * @throws OXException if the service needed for initialization is missing.
     */
    public static ScriptPasswordChangeConfig getInstance() throws OXException {
        if (ScriptPasswordChangeConfig.INSTANCE == null) {
            synchronized (ScriptPasswordChangeConfig.class) {
                if (ScriptPasswordChangeConfig.INSTANCE == null) {
                    ServiceRegistry serviceRegistry = SPWServiceRegistry.getServiceRegistry();
                    ConfigurationService configurationService = serviceRegistry.getService(ConfigurationService.class, true);
                    initFromConfigService(configurationService);
                    INSTANCE = new ScriptPasswordChangeConfig();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Get consistent config data in a way that
     * {@link Reloadable#reloadConfiguration(ConfigurationService)} can't
     * interfere.  
     * @return A consistent {@link ScriptPasswordChangeConfig.ConfigData} view.
     */
    public synchronized ConfigData getData() {
        return new ConfigData();
    }

    /**
     * Initialize the configuration values.
     * 
     * @param configService The {@link ConfigurationService} needed for initialization
     */
    private static void initFromConfigService(ConfigurationService configService) {
        scriptPath = configService.getProperty(SCRIPT_PATH_KEY, "");
        asBase64 = configService.getBoolProperty(BASE64_KEY, false);
    }

    @Override
    public synchronized void reloadConfiguration(ConfigurationService configService) {
        initFromConfigService(configService);
    }

    @Override
    public Interests getInterests() {
        Builder builder = DefaultInterests.builder();
        builder.propertiesOfInterest(SCRIPT_PATH_KEY, BASE64_KEY);
        builder.configFileNames(CONFIG_FILE_NAME);
        return builder.build();
    }

    /**
     * Immutable data container to guarantee data consistency during config
     * reloads and password changes.
     * {@link ConfigData}
     *
     * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
     * @since v7.8.4
     */
    final class ConfigData {

        private final String scriptPath;
        private final boolean asBase64;

        private ConfigData() {
            this.scriptPath = ScriptPasswordChangeConfig.scriptPath;
            this.asBase64 = ScriptPasswordChangeConfig.asBase64;
        }

        /**
         * Get the FS path to the password change script.
         * 
         * @return The path
         */
        public String getScriptPath() {
            return scriptPath;
        }

        /**
         * Indicates if the string based script parameters like username,
         * oldpassword and newpassword should be encoded as Base64 to circumvent
         * character encoding issues on improperly configured distributions not
         * providing an unicode environment for the process.
         * 
         * @return true if the parameters should be Base64 encoded, false otherwise.
         */
        public boolean asBase64() {
            return asBase64;
        }
    }

}
