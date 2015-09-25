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

package com.openexchange.ajax;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.configuration.ServerConfig.Property;


/**
 * {@link ClientUtil}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class ClientUtil implements Reloadable {

    private static volatile String uiWebPath;
    private static final String PROP_FILE = "server.properties";
    private static final String[] PROPERTY_LIST = new String[] { Property.UI_WEB_PATH.getPropertyName() };

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        uiWebPath = configService.getProperty(Property.UI_WEB_PATH.getPropertyName(), Property.UI_WEB_PATH.getDefaultValue());
    }

    @Override
    public Map<String, String[]> getConfigFileNames() {
        Map<String, String[]> map = new HashMap<String, String[]>(1);
        map.put(PROP_FILE, PROPERTY_LIST);
        return map;
    }

    public static void init(ConfigurationService configService) {
        if (configService != null) {
            uiWebPath = configService.getProperty(Property.UI_WEB_PATH.getPropertyName(), Property.UI_WEB_PATH.getDefaultValue());
        }
        else
        {
            uiWebPath = Property.UI_WEB_PATH.getDefaultValue();
        }
    }

    static String getUIWebPathProperty()
    {
        return uiWebPath;
    }

}
