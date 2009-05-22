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

package com.openexchange.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class SimConfigurationService implements ConfigurationService {
    
    public Map<String, String> stringProperties = new HashMap<String, String>();

    public boolean getBoolProperty(String name, boolean defaultValue) {
        // TODO Auto-generated method stub
        return false;
    }

    public Properties getFile(String filename) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getIntProperty(String name, int defaultValue) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Properties getPropertiesInFolder(String folderName) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getProperty(String name) {
        return stringProperties.get(name);
    }

    public String getProperty(String name, String defaultValue) {
        return stringProperties.containsKey(name) ? stringProperties.get(name) : defaultValue;
    }

    public String getProperty(String name, PropertyListener listener) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getProperty(String name, String defaultValue, PropertyListener listener) {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator<String> propertyNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public void removePropertyListener(String name, PropertyListener listener) {
        // TODO Auto-generated method stub

    }

    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

}
