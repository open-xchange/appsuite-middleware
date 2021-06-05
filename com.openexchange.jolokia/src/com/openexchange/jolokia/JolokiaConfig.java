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

package com.openexchange.jolokia;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import org.jolokia.config.ConfigKey;
import org.jolokia.restrictor.Restrictor;
import org.jolokia.restrictor.RestrictorFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.jolokia.restrictor.OXRestrictor;
import com.openexchange.jolokia.restrictor.OXRestrictorLocalhost;

/**
 * {@link JolokiaConfig} Collects and exposes configuration parameters needed by Jolokia
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JolokiaConfig {

    /** The logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JolokiaConfig.class);

    /**
     * Creates a new builder
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** A builder for an instance of <code>JolokiaConfig</code> */
    public static class Builder {

        private boolean jolokiaStart;
        private String jolokiaServletName;
        private String user;
        private String password;
        private boolean restrictToLocalhost;
        private final Dictionary<String, String> pConfig;
        private Restrictor restrictor;

        Builder() {
            super();
            pConfig = new Hashtable<String, String>();
            jolokiaStart = false;
            jolokiaServletName = "/monitoring/jolokia";
            restrictor = null;
        }

        public Builder init(ConfigurationService configService) throws OXException {
            // Jolokia properties
            this.jolokiaServletName = configService.getProperty("com.openexchange.jolokia.servlet.name", "/monitoring/jolokia");
            this.jolokiaStart = configService.getBoolProperty("com.openexchange.jolokia.start", false);
            this.user = configService.getProperty("com.openexchange.jolokia.user", "");
            this.password = configService.getProperty("com.openexchange.jolokia.password", "");
            this.restrictToLocalhost = configService.getBoolProperty("com.openexchange.jolokia.restrict.to.localhost", true);

            // Only allow Jolokia to be started if user and password are set
            if (jolokiaStart) {
                if (!(null != user && user.length() != 0)) {
                    LOG.warn("No user set by com.openexchange.jolokia.user, Jolokia will not start");
                    jolokiaStart = false;
                }
                if (!(null != password && password.length() != 0)) {
                    LOG.warn("No password set by com.openexchange.jolokia.password, Jolokia will not start");
                    jolokiaStart = false;
                }
            }

            File xmlConfigFile = configService.getFileByName("jolokia-access.xml");
            if (null != xmlConfigFile) {
                try {
                    restrictor = RestrictorFactory.lookupPolicyRestrictor(xmlConfigFile.toURI().toURL().toString());
                } catch (RuntimeException e) {
                    LOG.warn("Error loading configuration from file {}", xmlConfigFile.getAbsolutePath(), e);
                } catch (IOException e) {
                    LOG.warn("Error loading configuration from file {}", xmlConfigFile.getAbsolutePath(), e);
                }
            } else if (restrictToLocalhost) {
                restrictor = new OXRestrictorLocalhost();
            }

            /*
             * MW207 Instead of a default Restrictor used by Jolokia an own OXRestrictor is used.
             * In case no XML file is found/usable or service is not permitted to localhost OXRestrictor will be used
             */
            if (null == restrictor) {
                restrictor = new OXRestrictor(!restrictToLocalhost);
            }

            String maxObjects = configService.getProperty("com.openexchange.jolokia.maxObjects", "0");
            if (null != maxObjects) {
                pConfig.put(ConfigKey.MAX_OBJECTS.getKeyValue(), maxObjects);
            }

            String maxDepth = configService.getProperty("com.openexchange.jolokia.maxDepth", "0");
            if (null != maxDepth) {
                pConfig.put(ConfigKey.MAX_DEPTH.getKeyValue(), maxDepth);
            }

            return this;
        }

        /**
         * Builds the <code>JolokiaConfig</code> instance from this builder's arguments.
         *
         * @return The <code>JolokiaConfig</code> instance
         */
        public JolokiaConfig build() {
            return new JolokiaConfig(jolokiaStart, jolokiaServletName, user, password, restrictor, pConfig);
        }

    }

    // ---------------------------------------------------------------------------------------------------------------------------

    private final boolean jolokiaStart;
    private final String jolokiaServletName;
    private final String user;
    private final String password;
    private final Dictionary<String, String> pConfig;
    private final Restrictor restrictor;

    JolokiaConfig(boolean jolokiaStart, String jolokiaServletName, String user, String password, Restrictor restrictor, Dictionary<String, String> pConfig) {
        super();
        this.jolokiaStart = jolokiaStart;
        this.jolokiaServletName = jolokiaServletName;
        this.user = user;
        this.password = password;
        this.restrictor = restrictor;
        this.pConfig = pConfig;
    }

    /**
     * Gets the Servlet name; default is <code>"/monitoring/jolokia"</code>
     *
     * @return The Servlet name
     */
    public String getServletName() {
        return jolokiaServletName;
    }

    /**
     * Gets the flag, if jolokia will be run or not
     *
     * @return <code>true</code> if jolokia will start and <code>false</code> if not
     */
    public boolean getJolokiaStart() {
        return jolokiaStart;
    }

    /**
     * Gets the user for authentication
     *
     * @return The user for authentication
     */
    public String getUser() {
        return user;
    }

    /**
     * Gets the password for authentication
     *
     * @return The password for authentication
     */
    public String getPassword() {
        return password;
    }

    // Customizer for registering servlet at a HttpService
    /**
     * The internal Jolokia configuration to apply
     *
     * @return The internal Jolokia configuration
     */
    public Dictionary<String, String> getJolokiaConfiguration() {
        return pConfig;
    }

    /**
     * Gets the restrictor instance that controls access to registered MBeans
     *
     * @return The restrictor instance
     */
    public Restrictor getRestrictor() {
        return restrictor;
    }

}
