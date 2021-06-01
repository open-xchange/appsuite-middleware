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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.tools.strings.BasicTypesStringParser;
import com.openexchange.tools.strings.StringParser;
import com.openexchange.webdav.client.WebDAVClientException;

/**
 * {@link Utils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class Utils {

    private static final StringParser DATE_PARSER = new WebDAVDateParser();
    private static final StringParser BASIC_PARSER = new BasicTypesStringParser();

    public static WebDAVClientException asOXException(Exception e) {
        if (WebDAVClientException.class.isInstance(e)) {
            return (WebDAVClientException) e;
        }
        if (DavException.class.isInstance(e)) {
            DavException davException = (DavException) e;
            return new WebDAVClientException(davException.getErrorCode(), davException.getStatusPhrase(), davException.getCause());
        }
        return new WebDAVClientException(e);
    }

    public static DavPropertyName getPropertyName(QName name) {
        return DavPropertyName.create(name.getLocalPart(), Namespace.getNamespace(name.getPrefix(), name.getNamespaceURI()));
    }

    public static DavPropertySet getPropertySet(Map<QName, Object> props) {
        DavPropertySet propertySet = new DavPropertySet();
        for (Entry<QName, Object> entry : props.entrySet()) {
            propertySet.add(getProperty(entry.getKey(), entry.getValue()));
        }
        return propertySet;
    }

    public static DavProperty<?> getProperty(QName name, Object value) {
        return serializeProperty(name, value);
    }

    public static DavPropertyNameSet getPropertyNameSet(Set<QName> props) {
        DavPropertyNameSet propertyNameSet = new DavPropertyNameSet();
        for (QName prop : props) {
            propertyNameSet.add(getPropertyName(prop));
        }
        return propertyNameSet;
    }

    public static Element toXml(DavProperty<?> property) {
        try {
            return property.toXml(DomUtil.createDocument());
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <R extends HttpRequestBase> R addHeaders(R request, Map<String, String> headers) {
        if (null != headers) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.addHeader(header.getKey(), header.getValue());
            }
        }
        return request;
    }

    public static <T> T getPropertyValue(DavProperty<?> property, Class<T> t) {
        Object value = property.getValue();
        if (null == value) {
            return null;
        }
        if (value.getClass() == t) {
            return (T) value;
        }
        String stringValue = String.class.isInstance(value) ? (String) value : String.valueOf(value);
        if (Date.class == t) {
            return DATE_PARSER.parse(stringValue, t);
        }
        return BASIC_PARSER.parse(stringValue, t);
    }

    public static DavProperty<?> serializeProperty(QName name, Object value) {
        if (null == value || String.class.isInstance(value)) {
            return new DefaultDavProperty<String>(getPropertyName(name), (String) value);
        }
        if (Date.class.isInstance(value)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return new DefaultDavProperty<String>(getPropertyName(name), dateFormat.format((Date) value));
        }
        if (Element.class.isInstance(value)) {
            return new DavProperty<Object>() {

                @Override
                public Element toXml(Document document) {
                    return (Element) value;
                }

                @Override
                public DavPropertyName getName() {
                    return getPropertyName(name);
                }

                @Override
                public Object getValue() {
                    return value;
                }

                @Override
                public boolean isInvisibleInAllprop() {
                    return false;
                }
            };
        }
        return serializeProperty(name, String.valueOf(value));
    }

}
