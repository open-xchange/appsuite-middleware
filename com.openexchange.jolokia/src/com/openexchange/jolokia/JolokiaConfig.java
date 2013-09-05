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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.jolokia;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import org.jolokia.config.ConfigKey;
import org.jolokia.restrictor.Restrictor;
import org.jolokia.restrictor.RestrictorFactory;
import org.jolokia.restrictor.RestrictorFactoryForLocalhost;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.jolokia.osgi.Services;
import com.openexchange.server.Initialization;

/**
 * {@link JolokiaConfig} Collects and exposes configuration parameters needed by Jolokia
 * 
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
public class JolokiaConfig implements Initialization {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(JolokiaConfig.class);

    private static final JolokiaConfig instance = new JolokiaConfig();

    public static JolokiaConfig getInstance() {
        return instance;
    }

    private final AtomicBoolean started = new AtomicBoolean();

    // Jolokia properties

    /** boolean which will start jolokia or not */
    private boolean jolokiaStart = false;

    /** The servlet name, jolokia will try to get connected to */
    private String jolokiaServletName = "/monitoring/jolokia";

    /** The user for authentication */
    private String user;

    /** The password for authentication */
    private String password;

    /** if Jolokia is restricted to localhost */
    private boolean restrictToLocalhost;

    // internal Jolokia options
    private Dictionary<String, String> pConfig = new Hashtable<String, String>();
    
    private Restrictor restrictor = null;

    @Override
    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            LOG.error(this.getClass().getName() + " already started");
            return;
        }
        init();
    }

    @Override
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            LOG.error(this.getClass().getName() + " cannot be stopped since it has no been started before");
            return;
        }
    }

    private void init() throws OXException {
        ConfigurationService configService = Services.getService(ConfigurationService.class);
        if (configService == null) {
            throw JolokiaExceptionCode.NEEDED_SERVICE_MISSING.create(ConfigurationService.class.getSimpleName());
        }

        // jolokia properties
        this.jolokiaServletName = configService.getProperty("com.openexchange.jolokia.servlet.name", "/monitoring/jolokia");
        this.jolokiaStart = configService.getBoolProperty("com.openexchange.jolokia.start", false);
        this.user = configService.getProperty("com.openexchange.jolokia.user");
        this.password = configService.getProperty("com.openexchange.jolokia.password", "secret");
        this.restrictToLocalhost = configService.getBoolProperty("com.openexchange.jolokia.restrict.to.localhost", true);
        
        File xmlConfigFile = configService.getFileByName("jolokia-access.xml");
        if (null != xmlConfigFile) {
            try {
                restrictor =  RestrictorFactory.lookupPolicyRestrictor(xmlConfigFile.toURI().toURL().toString());
            } catch (RuntimeException e) {
                LOG.warn("Error loading configuration from file " + xmlConfigFile.getAbsolutePath(), e);
            } catch (IOException e) {
                LOG.warn("Error loading configuration from file " + xmlConfigFile.getAbsolutePath(), e);
            }
        } else if (restrictToLocalhost) {
            restrictor = RestrictorFactoryForLocalhost.createPolicyRestrictor();
        }
        

        pConfig.put(ConfigKey.MAX_OBJECTS.getKeyValue(), configService.getProperty("com.openexchange.jolokia.maxObjects", "0"));
        pConfig.put(ConfigKey.MAX_DEPTH.getKeyValue(), configService.getProperty("com.openexchange.jolokia.maxDepth", "0"));

    }

    /**
     * Gets the started
     * 
     * @return The started
     */
    public AtomicBoolean getStarted() {
        return started;
    }

    /**
     * Gets the servletName
     * 
     * @return The servlteName
     */
    public String getServletName() {
        return jolokiaServletName;
    }

    /**
     * Gets the boolean, if jolokia will be run or not
     * 
     * @return true if jolokia will start and false if not
     */
    public boolean getJolokiaStart() {
        return jolokiaStart;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    // Customizer for registering servlet at a HttpService
    public Dictionary<String, String> getJolokiaConfiguration() {
        return pConfig;
    }

    /**
     * Loads a Jolokia Security configuration from a file named <code>jolokia-access.xml</code> if present.
     * 
     * @return The loaded jolokia config, or <code>null</code> if no such file exists or can't be loaded
     * @throws OXException
     */
    public Restrictor getRestrictor() throws OXException {
        return restrictor;
    }

}
