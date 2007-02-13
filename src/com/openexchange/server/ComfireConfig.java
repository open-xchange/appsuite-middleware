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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



package com.openexchange.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * ComfireConfig
 * 
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 * @deprecated use the new configuration system. Especially
 * {@link com.openexchange.configuration.SystemConfig}.
 */
public class ComfireConfig {

	/**
     * @deprecated use the new configuration system. Especially
     * {@link com.openexchange.configuration.SystemConfig}.
	 */
    public static Properties properties;

	public static void loadProperties(String propfile) {
		try {
			properties = new Properties();
			properties.load(new FileInputStream(propfile));
			final Enumeration en = properties.propertyNames();
			String key = "";
			String val = "";
			while (en.hasMoreElements()) {
				key = (String) en.nextElement();
				val = (String) properties.getProperty(key);
				if (val != null) {
					if (val.endsWith("/")) {
						val = val.substring(0, val.length() - 1);
						properties.put(key, val);
					}
				}
			}
		} catch (IOException e) {
			ComfireLogger.log("ERROR: Can't load propfile " + propfile + "! Abort.", 0);
			System.exit(4);
		}
        try {
            if (properties.getProperty("LOGLEVEL") != null) {
                int ll = Integer.valueOf(properties.getProperty("LOGLEVEL")).intValue();
                if (ll >= 0) {
                    ComfireLogger.setLogLevel(ll);
                    ComfireLogger.log("Loglevel set to: " + properties.getProperty("LOGLEVEL"), 0);
                }
            }
        } catch (Exception ell) {
            ell.printStackTrace();
        }
	}
}
