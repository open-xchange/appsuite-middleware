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

package com.openexchange.groupware.infostore.webdav;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;
import com.openexchange.session.SessionHolder;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavProtocolException;

public class PropertyHelper {

	private final Map<WebdavProperty, WebdavProperty> properties = new HashMap<WebdavProperty, WebdavProperty>();

	private final Set<WebdavProperty> removedProperties = new HashSet<WebdavProperty>();
	private boolean loadedAllProps;
    private final Map<WebdavProperty, WebdavProperty> changedProps = new HashMap<WebdavProperty, WebdavProperty>();

	private final PropertyStore propertyStore;
	private final SessionHolder sessionHolder;

	private int id;
	private final WebdavPath url;

	private boolean changed;


	public PropertyHelper(final PropertyStore props, final SessionHolder sessionHolder, final WebdavPath url) {
		this.propertyStore = props;
		this.sessionHolder = sessionHolder;
		this.url = url;
	}

	public List<WebdavProperty> getAllProps() throws WebdavProtocolException {
		loadAllProperties();
		return new ArrayList<WebdavProperty>(properties.values());
	}

	public WebdavProperty getProperty(final String namespace, final String name) throws WebdavProtocolException {
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

    public void removeProperty(final String namespace, final String name) throws WebdavProtocolException {
        loadProperty(namespace, name);
		if (properties.remove(new WebdavProperty(namespace, name)) != null) {
			markRemovedProperty(new WebdavProperty(namespace, name));
		}
	}

	public boolean isRemoved(final WebdavProperty property) {
        return removedProperties.contains(new WebdavProperty(property.getNamespace(), property.getName()));
	}

	private void markRemovedProperty(final WebdavProperty property) {
        WebdavProperty key = new WebdavProperty(property.getNamespace(), property.getName());
        removedProperties.add(key);
        changedProps.remove(key);
	}

	private void markSetProperty(final WebdavProperty property) {
        WebdavProperty key = new WebdavProperty(property.getNamespace(), property.getName());
        removedProperties.remove(key);
        changedProps.put(key, property);
	}

	private void markChanged() {
		changed = true;
	}

    public boolean mustWrite(){
        return changed;
    }

    private void loadProperty(final String namespace, final String name) throws WebdavProtocolException {
		if (removedProperties.contains(new WebdavProperty(namespace, name))) {
			return;
		}
		if (loadedAllProps) {
			return;
		}
		try {
            final ServerSession session = getSession();
			final List<WebdavProperty> list = propertyStore.loadProperties(id, Arrays.asList(new WebdavProperty(namespace,name)), session.getContext());
			if (list.isEmpty()) {
				return;
			}
			final WebdavProperty prop = list.get(0);
			properties.put(new WebdavProperty(prop.getNamespace(), prop.getName()), prop);

		} catch (OXException e) {
			throw WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void loadAllProperties() throws WebdavProtocolException {
		if (loadedAllProps) {
			return;
		}
		loadedAllProps = true;
		try {
            final ServerSession session = getSession();
		    final List<WebdavProperty> list = propertyStore.loadAllProperties(id, session.getContext());
			for(final WebdavProperty prop : list) {
				properties.put(new WebdavProperty(prop.getNamespace(), prop.getName()), prop);
			}
		} catch (OXException e) {
		    throw WebdavProtocolException.Code.GENERAL_ERROR.create(url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
		}
	}

	public void dumpPropertiesToDB() throws OXException {
		if (!changed) {
			return;
		}
		changed = false;
		final ServerSession session = getSession();
        if (false == changedProps.isEmpty()) {
            propertyStore.saveProperties(id, new ArrayList<WebdavProperty>(changedProps.values()), session.getContext());
            changedProps.clear();
        }
        if (false == removedProperties.isEmpty()) {
            propertyStore.removeProperties(id, new ArrayList<WebdavProperty>(removedProperties), session.getContext());
            removedProperties.clear();
        }
	}

	public void deleteProperties() throws OXException {
		final ServerSession session = getSession();
		propertyStore.removeAll(id, session.getContext());
	}

    private ServerSession getSession() throws OXException {
        try {
            return ServerSessionAdapter.valueOf(sessionHolder.getSessionObject());
        } catch (OXException e) {
            throw e;
        }
    }

}
