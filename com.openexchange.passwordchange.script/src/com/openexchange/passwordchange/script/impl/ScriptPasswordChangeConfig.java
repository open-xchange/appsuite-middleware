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

package com.openexchange.passwordchange.script.impl;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadables;

/**
 * {@link ScriptPasswordChangeConfig} Collects and exposes configuration
 * parameters needed by the ScriptPasswordChange.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ScriptPasswordChangeConfig {

    private final static String SCRIPT_PATH_KEY = "com.openexchange.passwordchange.script.shellscript";
    private final static String BASE64_KEY = "com.openexchange.passwordchange.script.base64";

    private static final Interests INTERESTS = Reloadables.interestsForProperties(SCRIPT_PATH_KEY, BASE64_KEY);

    /**
     * Gets the interests
     *
     * @return The interests
     */
    public static Interests getInterests() {
        return INTERESTS;
    }

    /**
     * Creates a new builder.
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder class */
    public static final class Builder {

        private String scriptPath;
        private boolean asBase64;

        Builder() {
            super();
            scriptPath = "";
            asBase64 = false;
        }

        /**
         * Initializes this builder using given configuration service
         *
         * @param configService The configuration service to use
         * @return This builder
         */
        public Builder init(ConfigurationService configService) {
            scriptPath = configService.getProperty(SCRIPT_PATH_KEY, "");
            asBase64 = configService.getBoolProperty(BASE64_KEY, false);
            return this;
        }

        /**
         * Sets whether to encode in base64
         *
         * @param asBase64 The flag to set
         * @return This builder
         */
        public Builder asBase64(boolean asBase64) {
            this.asBase64 = asBase64;
            return this;
        }

        /**
         * Sets the script path
         *
         * @param scriptPath The scriptPath to set
         * @return This builder
         */
        public Builder scriptPath(String scriptPath) {
            this.scriptPath = scriptPath;
            return this;
        }

        /**
         * Builds the ScriptPasswordChangeConfig from this builder's arguments
         *
         * @return The ScriptPasswordChangeConfig instance
         */
        public ScriptPasswordChangeConfig build() {
            return new ScriptPasswordChangeConfig(scriptPath, asBase64);
        }

    }

    // --------------------------------------------------------------------------------------------------------------------

    private final String scriptPath;
    private final boolean asBase64;

    ScriptPasswordChangeConfig(String scriptPath, boolean asBase64) {
        super();
        this.scriptPath = scriptPath;
        this.asBase64 = asBase64;
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
