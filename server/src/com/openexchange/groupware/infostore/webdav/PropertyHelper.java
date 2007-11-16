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

package com.openexchange.groupware.infostore.webdav;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.api2.OXException;
import com.openexchange.sessiond.Session;
import com.openexchange.sessiond.impl.SessionHolder;
import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavProperty;

public class PropertyHelper {
	
	private final Map<WebdavProperty, WebdavProperty> properties = new HashMap<WebdavProperty, WebdavProperty>();
	
	private final Set<WebdavProperty> removedProperties = new HashSet<WebdavProperty>();
	private boolean loadedAllProps;
	private final List<WebdavProperty> changedProps = new ArrayList<WebdavProperty>();

	private PropertyStore propertyStore;
	private SessionHolder sessionHolder;
	
	private int id;
	private String url;

	private boolean changed;
	
	
	public PropertyHelper(final PropertyStore props, final SessionHolder sessionHolder, final String url) {
		this.propertyStore = props;
		this.sessionHolder = sessionHolder;
		this.url = url;
	}

	public List<WebdavProperty> getAllProps() throws WebdavException {
		loadAllProperties();
		return new ArrayList<WebdavProperty>(properties.values());
	}

	public WebdavProperty getProperty(final String namespace, final String name) throws WebdavException {
		loadProperty(namespace, name);
		return properties.get(new WebdavProperty(namespace, name));
	}

	public void putProperty(final WebdavProperty prop) {
		properties.put(new WebdavProperty(prop.getNamespace(), prop.getName()), prop);
		markSetProperty(prop);
		markChanged();
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void removeProperty(final String namespace, final String name) {
		if (properties.remove(new WebdavProperty(namespace, name)) != null) {
			markRemovedProperty(new WebdavProperty(namespace, name));
		}
	}
	
	public boolean isRemoved(final WebdavProperty property) {
		return removedProperties.contains(property);
	}

	private void markRemovedProperty(final WebdavProperty property) {
		removedProperties.add(property);
		changedProps.remove(properties.get(property));
	}

	private void markSetProperty(final WebdavProperty property) {
		removedProperties.remove(property);
		changedProps.add(property);
	}
	
	private void markChanged() {
		changed = true;
	}
	
	private void loadProperty(final String namespace, final String name) throws WebdavException {
		if(removedProperties.contains(new WebdavProperty(namespace, name))) {
			return;
		}
		if(loadedAllProps) {
			return;
		}
		final Session session = sessionHolder.getSessionObject();
		try {
			final List<WebdavProperty> list = propertyStore.loadProperties(id, Arrays.asList(new WebdavProperty(namespace,name)), session.getContext());
			if(list.isEmpty()) {
				return;
			}
			final WebdavProperty prop = list.get(0);
			properties.put(new WebdavProperty(prop.getNamespace(), prop.getName()), prop);
			
		} catch (final OXException e) {
			throw new WebdavException(e.getMessage(), e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void loadAllProperties() throws WebdavException {
		if(loadedAllProps) {
			return;
		}
		loadedAllProps = true;
		final Session session = sessionHolder.getSessionObject();
		try {
			final List<WebdavProperty> list = propertyStore.loadAllProperties(id, session.getContext());
			for(final WebdavProperty prop : list) {
				properties.put(new WebdavProperty(prop.getNamespace(), prop.getName()), prop);
			}
		} catch (final OXException e) {
			throw new WebdavException(e.getMessage(), e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	public void dumpPropertiesToDB() throws OXException {
		if(!changed) {
			return;
		}
		changed = false;
		final Session session = sessionHolder.getSessionObject();
		propertyStore.saveProperties(id, new ArrayList<WebdavProperty>(changedProps), session.getContext());
		changedProps.clear();
		propertyStore.removeProperties(id, new ArrayList<WebdavProperty>(removedProperties), session.getContext());
		removedProperties.clear();
	}
	
	public void deleteProperties() throws OXException {
		final Session session = sessionHolder.getSessionObject();
		propertyStore.removeAll(id, session.getContext());
	}
	
}
