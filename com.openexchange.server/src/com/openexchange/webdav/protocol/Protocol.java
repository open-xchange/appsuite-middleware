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

package com.openexchange.webdav.protocol;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jdom2.Namespace;
import com.openexchange.config.ConfigurationService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.webdav.action.WebdavAction;
import com.openexchange.webdav.protocol.util.PropertySwitch;

public class Protocol {

	public static final int SC_LOCKED = 423;
	public static final int SC_MULTISTATUS = 207;

    /** HTTP/1.1 507 Insufficient Storage */
    public static final int SC_INSUFFICIENT_STORAGE = 507;

	public static final Namespace DAV_NS = Namespace.getNamespace("D","DAV:");


	public static final String DEFAULT_NAMESPACE = "DAV:";
	public static final String COLLECTION = "<D:collection />";

	public static final class Property {
		private final int id;
		private final String name;
		private final String namespace;

		Property(final int id, final String namespace, final String name) {
			this.id = id;
			this.name = name;
			this.namespace = namespace;
		}

		public Object doSwitch(final PropertySwitch sw) throws WebdavProtocolException {
			switch(id) {
			case CREATIONDATE : return sw.creationDate();
			case DISPLAYNAME : return sw.displayName();
			case GETCONTENTLANGUAGE : return sw.contentLanguage();
			case GETCONTENTLENGTH : return sw.contentLength();
			case GETCONTENTTYPE : return sw.contentType();
			case GETETAG : return sw.etag();
			case GETLASTMODIFIED : return sw.lastModified();
			case RESOURCETYPE : return sw.resourceType();
			case LOCKDISCOVERY : return sw.lockDiscovery();
			case SUPPORTEDLOCK : return sw.supportedLock();
			case SOURCE : return sw.source();
			default: return null;
			}
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getNamespace() {
			return namespace;
		}

		public WebdavProperty getWebdavProperty(){
			final WebdavProperty p = new WebdavProperty();
			p.setNamespace(namespace);
			p.setName(name);
			switch(id){
			case CREATIONDATE : case GETLASTMODIFIED : p.setDate(true); break;
			case RESOURCETYPE: p.setXML(true); break;
			}
			return p;
		}

		@Override
		public int hashCode(){
			return id;
		}

		@Override
		public boolean equals(final Object o) {
			if (o instanceof Property) {
				final Property prop = (Property) o;
				return prop.getId() == id;
			}
			return false;
		}
	}


	public static final int CREATIONDATE = 1;
	public static final int DISPLAYNAME = 2;
	public static final int GETCONTENTLANGUAGE = 3;
	public static final int GETCONTENTLENGTH = 4;
	public static final int GETCONTENTTYPE = 5;
	public static final int GETETAG = 6;
	public static final int GETLASTMODIFIED = 7;
	public static final int RESOURCETYPE = 8;
	public static final int LOCKDISCOVERY = 9;
	public static final int SUPPORTEDLOCK = 10;
	public static final int SOURCE = 11;

	public static final Property CREATIONDATE_LITERAL = new Property(CREATIONDATE, DEFAULT_NAMESPACE,"creationdate");
	public static final Property DISPLAYNAME_LITERAL = new Property(DISPLAYNAME, DEFAULT_NAMESPACE, "displayname");
	public static final Property GETCONTENTLANGUAGE_LITERAL = new Property(GETCONTENTLANGUAGE, DEFAULT_NAMESPACE,"getcontentlanguage");
	public static final Property GETCONTENTLENGTH_LITERAL = new Property(GETCONTENTLENGTH,DEFAULT_NAMESPACE,"getcontentlength");
	public static final Property GETCONTENTTYPE_LITERAL = new Property(GETCONTENTTYPE, DEFAULT_NAMESPACE,"getcontenttype");
	public static final Property GETETAG_LITERAL = new Property(GETETAG, DEFAULT_NAMESPACE,"getetag");
	public static final Property GETLASTMODIFIED_LITERAL = new Property(GETLASTMODIFIED, DEFAULT_NAMESPACE,"getlastmodified");
	public static final Property RESOURCETYPE_LITERAL = new Property(RESOURCETYPE, DEFAULT_NAMESPACE, "resourcetype");
	public static final Property LOCKDISCOVERY_LITERAL = new Property(LOCKDISCOVERY, DEFAULT_NAMESPACE, "lockdiscovery");
	public static final Property SUPPORTEDLOCK_LITERAL =  new Property(SUPPORTEDLOCK, DEFAULT_NAMESPACE, "supportedlock");
	public static final Property SOURCE_LITERAL = new Property(SOURCE,DEFAULT_NAMESPACE,"source");



	public Property[] VALUES_ARRAY = new Property[]{
			CREATIONDATE_LITERAL,
			DISPLAYNAME_LITERAL,
			GETCONTENTLANGUAGE_LITERAL,
			GETCONTENTLENGTH_LITERAL,
			GETCONTENTTYPE_LITERAL,
			GETETAG_LITERAL,
			GETLASTMODIFIED_LITERAL,
			RESOURCETYPE_LITERAL,
			LOCKDISCOVERY_LITERAL,
			SUPPORTEDLOCK_LITERAL,
			SOURCE_LITERAL
	};

	public List<Property> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	public List<Property> getKnownProperties(){
		return VALUES;
	}

	public Property get(final int i) {
		switch(i) {
		case CREATIONDATE : return CREATIONDATE_LITERAL;
		case DISPLAYNAME : return DISPLAYNAME_LITERAL;
		case GETCONTENTLANGUAGE : return GETCONTENTLANGUAGE_LITERAL;
		case GETCONTENTLENGTH : return GETCONTENTLENGTH_LITERAL;
		case GETCONTENTTYPE : return GETCONTENTTYPE_LITERAL;
		case GETETAG : return GETETAG_LITERAL;
		case GETLASTMODIFIED : return GETLASTMODIFIED_LITERAL;
		case RESOURCETYPE : return RESOURCETYPE_LITERAL;
		case LOCKDISCOVERY : return LOCKDISCOVERY_LITERAL;
		case SUPPORTEDLOCK : return SUPPORTEDLOCK_LITERAL;
		case SOURCE : return SOURCE_LITERAL;
		default : return null;
		}
	}

	public Property get(String namespace, final String name) {
		if (namespace == null) {
			namespace = DEFAULT_NAMESPACE;
		}
		final List<Property> known = getKnownProperties();
		for(final Property prop : known) {
			if (prop.getNamespace().equals(namespace) && prop.getName().equals(name)) {
				return prop;
			}
		}
		return null;
	}

	public boolean isProtected(final String namespaceURI, final String name) {
		final Property p = get(namespaceURI, name);
		if (p == null) {
			return false;
		}
		switch(p.getId()) {
		case CREATIONDATE: case GETETAG: case RESOURCETYPE: case LOCKDISCOVERY: case SUPPORTEDLOCK: case SOURCE: return true;
		default : return false;
		}
	}

    public List<Namespace> getAdditionalNamespaces() {
        return Collections.emptyList();
    }

    /**
     * Gets a value indicating which limits apply when marshalling elements in WebDAV responses recursively.
     *
     * @return The recursive marshalling limit, or <code>-1</code> for no limitations
     */
    public int getRecursiveMarshallingLimit() {
        int defaultValue = 250000;
        ConfigurationService configService = ServerServiceRegistry.getServize(ConfigurationService.class);
        return null != configService ?
            configService.getIntProperty("com.openexchange.webdav.recursiveMarshallingLimit", defaultValue) : defaultValue;
    }

    public WebdavAction getReportAction(final String ns, final String name) {
        return null;
    }
}
