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

package com.openexchange.webdav.protocol.helpers;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link SingleResourcePropertyMixin}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public abstract class SingleResourcePropertyMixin extends SinglePropertyMixin implements ResourcePropertyMixin {

    protected final String namespace;
    protected final String name;

    /**
     * Initializes a new {@link SingleResourcePropertyMixin}.
     *
     * @param namespace The namespace of the property
     * @param name The name of the property
     */
    public SingleResourcePropertyMixin(String namespace, String name) {
        super(namespace, name);
        this.namespace = namespace;
        this.name = name;
    }

    @Override
    public List<WebdavProperty> getAllProperties(WebdavResource resource) throws OXException {
        return super.getAllProperties();
    }

    @Override
    public WebdavProperty getProperty(WebdavResource resource, String namespace, String name) throws OXException {
        if (this.namespace.equals(namespace) && this.name.equals(name)) {
            return getProperty(resource);
        }
        return null;
    }

    @Override
    protected void configureProperty(WebdavProperty property) {
        throw new UnsupportedOperationException("unable to configure '" + namespace + '.' + name + "' without underlying WebDAV resource");
    }

    /**
     * Prepares a new, empty property with the target namespace and name.
     *
     * @param xml <code>true</code> to mark the property as xml content, <code>false</code>, otherwise
     * @return The prepared property
     */
    protected WebdavProperty prepareProperty(boolean xml) {
        WebdavProperty property = new WebdavProperty(namespace, name);
        property.setXML(xml);
        return property;
    }

    /**
     * Gets the WebdAV property.
     *
     * @param resource The WebDAV resource to get the property for
     * @return The property, or <code>null</code> if not available
     */
    protected abstract WebdavProperty getProperty(WebdavResource resource) throws OXException;

}
