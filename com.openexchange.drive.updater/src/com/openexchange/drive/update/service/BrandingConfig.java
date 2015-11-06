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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.drive.update.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * {@link BrandingConfig}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class BrandingConfig {

    private final Properties prop;
    private final static Map<String, BrandingConfig> CONFIGS = new HashMap<String, BrandingConfig>();
    private final static String[] FIELDS = new String[] { Constants.BRANDING_NAME, Constants.BRANDING_VERSION, Constants.BRANDING_CODE, Constants.BRANDING_RELEASE, Constants.BRANDING_IMPORTANCE };

    public BrandingConfig(String name) throws IOException {
        //load prop   
        prop = new Properties();
        try {
            String dir = System.getProperty("openexchange.propdir");
            prop.load(new FileInputStream(new File(dir, name)));
        } catch (IOException e) {
            throw e;
        }
    }

    public BrandingConfig(File file) throws IOException {
        //load prop   
        prop = new Properties();
        try {
            prop.load(new FileInputStream(file));
        } catch (IOException e) {
            throw e;
        }
    }

    public Properties getProperties() {
        return prop;
    }

    public boolean contains(String property) {
        return prop.containsKey(property);
    }

    /**
     * @param file
     * @throws IOException
     */
    public static boolean checkFile(File file) throws IOException {
        BrandingConfig conf = new BrandingConfig(file);
        for (String field : FIELDS) {
            if (!conf.contains(field)) {
                return false;
            }
        }
        CONFIGS.put(file.getParentFile().getName(), conf);
        return true;
    }

    /**
     * @param branding
     * @return
     */
    public static BrandingConfig getBranding(String branding) {
        return CONFIGS.get(branding);
    }

}
