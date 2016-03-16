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
package com.openexchange.oauth.linkedin;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Filter;
import com.openexchange.config.PropertyFilter;
import com.openexchange.config.PropertyListener;
import com.openexchange.exception.OXException;

public class MockConfigurationService implements ConfigurationService {

	private final Map<String,String> props;

	public MockConfigurationService(String apiKey, String apiSecret){
		props = new HashMap<String,String>();
		props.put("com.openexchange.socialplugin.linkedin.apisecret", apiSecret);
		props.put("com.openexchange.socialplugin.linkedin.apikey", apiKey);
	}

	@Override
    public Map<String, String> getProperties(final PropertyFilter filter) throws OXException {
        return Collections.emptyMap();
    }

	@Override
	public String getProperty(String name) {
		return props.get(name);
	}

	@Override
	public String getProperty(String name, String defaultValue) {
		if(props.containsKey(name)) {
            return props.get(name);
        }
		return defaultValue;
	}

	@Override
	public String getProperty(String name, PropertyListener listener) {
		// Nothing to do
		return null;
	}

	@Override
	public String getProperty(String name, String defaultValue,
			PropertyListener listener) {
		// Nothing to do
		return null;
	}

	@Override
	public void removePropertyListener(String name, PropertyListener listener) {
		// Nothing to do

	}

	@Override
    public File getDirectory(String directoryName) {
        // Nothing to do
        return null;
    }

    @Override
    public File getFileByName(String fileName) {
        // Nothing to do
        return null;
    }

	@Override
	public Properties getFile(String fileName) {
		// Nothing to do
		return null;
	}

	@Override
	public String getText(String fileName) {
		// Nothing to do
		return null;
	}

	@Override
	public Properties getPropertiesInFolder(String folderName) {
		// Nothing to do
		return null;
	}

	@Override
	public boolean getBoolProperty(String name, boolean defaultValue) {
		// Nothing to do
		return false;
	}

	@Override
	public int getIntProperty(String name, int defaultValue) {
		// Nothing to do
		return 0;
	}

	@Override
	public Iterator<String> propertyNames() {
		// Nothing to do
		return null;
	}

	@Override
	public int size() {
		// Nothing to do
		return 0;
	}

	@Override
	public Object getYaml(String filename) {
		// Nothing to do
		return null;
	}

	@Override
	public Map<String, Object> getYamlInFolder(String dirName) {
		// Nothing to do
		return null;
	}

    @Override
    public Filter getFilterFromProperty(String name) {
        // Nothing to do
        return null;
    }

    @Override
    public boolean getBoolProperty(String name, boolean defaultValue, PropertyListener propertyListener) {
        return false;
    }

    @Override
    public int getIntProperty(String name, int defaultValue, PropertyListener propertyListener) {
        return 0;
    }

    @Override
    public List<String> getProperty(String name, String defaultValue, String separator) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getProperty(String name, String defaultValue, PropertyListener listener, String separator) {
        return Collections.emptyList();
    }

}
