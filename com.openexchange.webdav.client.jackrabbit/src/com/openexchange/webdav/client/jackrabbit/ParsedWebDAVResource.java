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

package com.openexchange.webdav.client.jackrabbit;

import static com.openexchange.webdav.client.PropertyName.DAV_CREATIONDATE;
import static com.openexchange.webdav.client.PropertyName.DAV_DISPLAYNAME;
import static com.openexchange.webdav.client.PropertyName.DAV_GETCONTENTLANGUAGE;
import static com.openexchange.webdav.client.PropertyName.DAV_GETCONTENTLENGTH;
import static com.openexchange.webdav.client.PropertyName.DAV_GETCONTENTTYPE;
import static com.openexchange.webdav.client.PropertyName.DAV_GETETAG;
import static com.openexchange.webdav.client.PropertyName.DAV_GETLASTMODIFIED;
import static com.openexchange.webdav.client.PropertyName.DAV_RESOURCETYPE;
import static com.openexchange.webdav.client.jackrabbit.Utils.getPropertyName;
import static com.openexchange.webdav.client.jackrabbit.Utils.getPropertyValue;
import java.util.Date;
import javax.xml.namespace.QName;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.openexchange.webdav.client.WebDAVResource;

/**
 * {@link WebDAVResource2}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class ParsedWebDAVResource implements WebDAVResource {

    private final String href;
    private final DavPropertySet propertySet;

    /**
     * Initializes a new {@link ParsedWebDAVResource}.
     *
     * @param href The resource's URI
     * @param propertySet The resource's property set
     */
    public ParsedWebDAVResource(String href, DavPropertySet propertySet) {
        super();
        this.href = href;
        this.propertySet = propertySet;
    }

    @Override
    public String getHref() {
        return href;
    }

    @Override
    public boolean isCollection() {
        Element property = getProperty(DAV_RESOURCETYPE);
        if (null != property) {
            NodeList nodes = property.getElementsByTagNameNS("DAV:", "collection");
            return null != nodes && 0 < nodes.getLength();
        }
        return getHref().endsWith("/");
    };

    @Override
    public Element getProperty(QName name) {
        DavProperty<?> property = propertySet.get(getPropertyName(name));
        if (null == property) {
            return null;
        }
        return Utils.toXml(property);
    }

    @Override
    public <T> T getProperty(QName name, Class<T> clazz) {
        DavProperty<?> property = propertySet.get(getPropertyName(name));
        if (null == property) {
            return null;
        }
        return getPropertyValue(property, clazz);
    }

    @Override
    public String getDisplayName() {
        return getProperty(DAV_DISPLAYNAME, String.class);
    }

    @Override
    public Date getCreationDate() {
        return getProperty(DAV_CREATIONDATE, Date.class);
    }

    @Override
    public Date getModifiedDate() {
        return getProperty(DAV_GETLASTMODIFIED, Date.class);
    }

    @Override
    public String getEtag() {
        return getProperty(DAV_GETETAG, String.class);
    }

    @Override
    public String getContentType() {
        return getProperty(DAV_GETCONTENTTYPE, String.class);
    }

    @Override
    public Long getContentLength() {
        return getProperty(DAV_GETCONTENTLENGTH, Long.class);
    }

    @Override
    public String getContentLanguage() {
        return getProperty(DAV_GETCONTENTLANGUAGE, String.class);
    }

    @Override
    public String toString() {
        return getHref();
    }

}
