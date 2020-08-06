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
 *    trademarks of the OX Software GmbH. group of companies.
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
