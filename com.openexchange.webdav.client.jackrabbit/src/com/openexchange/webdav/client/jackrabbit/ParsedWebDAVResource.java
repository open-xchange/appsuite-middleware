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
