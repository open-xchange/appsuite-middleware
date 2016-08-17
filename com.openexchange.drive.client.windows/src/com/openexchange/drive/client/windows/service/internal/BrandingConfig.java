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

package com.openexchange.drive.client.windows.service.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.drive.BrandedDriveVersionService;
import com.openexchange.drive.client.windows.service.Constants;
import com.openexchange.java.Streams;


/**
 * {@link BrandingConfig} is a storage for branding configuration's.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class BrandingConfig {

    private final static Logger LOG = LoggerFactory.getLogger(BrandingConfig.class);

    private final static Map<String, BrandingConfig> CONFIGS = new HashMap<String, BrandingConfig>();
    private final static String[] FIELDS = new String[] { Constants.BRANDING_NAME, Constants.BRANDING_VERSION, Constants.BRANDING_RELEASE };

    // ------------------------------------------------------------------------------------------------------------------------------

    private final Properties prop;

    private BrandingConfig(File file) throws IOException {
        super();
        prop = new Properties();
        FileInputStream fis = new FileInputStream(file);
        try {
            prop.load(fis);
        } finally {
            Streams.close(fis);
        }
    }

    /**
     * Retrieves the branding properties
     *
     * @return The properties
     */
    public Properties getProperties() {
        return prop;
    }

    /**
     * Tests if this config contains the given property
     *
     * @param property The name of the property to check
     * @return true if it contains the property, false otherwise
     */
    public boolean contains(String property) {
        return prop.containsKey(property);
    }

    /**
     * Tests if the given file is a valid branding configuration and add it to the list of known configurations.
     *
     * @param file The branding configuration
     * @throws IOException if the file couldn't be found or if an error occurs while retrieving the properties
     */
    public static boolean checkFile(File file) throws IOException {
        BrandingConfig conf = new BrandingConfig(file);
        for (String field : FIELDS) {
            if (!conf.contains(field)) {
                return false;
            }
        }
        CONFIGS.put(file.getParentFile().getName(), conf);
        BrandedDriveVersionService versionService = Services.getService(BrandedDriveVersionService.class);
        if (versionService != null) {
            versionService.putBranding(file.getParentFile().getName(), conf.getProperties().getProperty(Constants.BRANDING_VERSION), conf.getProperties().getProperty(Constants.BRANDING_MINIMUM_VERSION));
        } else {
            LOG.warn("BrandedDriveVersionService is not available. Version restrictions are not applied.");
        }
        return true;
    }

    /**
     * Clear the list of all known configurations.
     */
    public static void clear() {
        CONFIGS.clear();
        BrandedDriveVersionService versionService = Services.getService(BrandedDriveVersionService.class);
        if (versionService != null) {
            versionService.clearAll();
        }
    }

    /**
     * Retrieves the configuration for the given branding.
     *
     * @param branding The branding identifier
     * @return The configuration or null
     */
    public static BrandingConfig getBranding(String branding) {
        return CONFIGS.get(branding);
    }

    /**
     * Tests if the BrandingConfig contains the given branding
     *
     * @param branding The branding name
     * @return true if it contains the branding, false otherwise
     */
    public static boolean containsBranding(String branding) {
        return CONFIGS.containsKey(branding);
    }

}
