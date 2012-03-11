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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.webdav.protocol.osgi;

import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.helpers.PropertyMixin;
import com.openexchange.webdav.protocol.helpers.PropertyMixinFactory;

/**
 * {@link OSGiPropertyMixin}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OSGiPropertyMixin implements PropertyMixin {

    private ServiceTracker mixinTracker = null;

    private final ServiceTracker factoryTracker;

    private final SessionHolder sessionHolder;

    public OSGiPropertyMixin(BundleContext context, SessionHolder sessionHolder) {
        this.sessionHolder = sessionHolder;

        this.mixinTracker = new ServiceTracker(context, PropertyMixin.class.getName(), null);
        mixinTracker.open();

        this.factoryTracker = new ServiceTracker(context, PropertyMixinFactory.class.getName(), null);
        factoryTracker.open();
    }

    public void close() {
        mixinTracker.close();
        factoryTracker.close();
    }

    @Override
    public List<WebdavProperty> getAllProperties() throws OXException {
        Object[] mixins = mixinTracker.getServices();
        List<WebdavProperty> allProperties = new ArrayList<WebdavProperty>();
        if (mixins != null) {
            for (Object object : mixins) {
                PropertyMixin mixin = (PropertyMixin) object;
                allProperties.addAll(mixin.getAllProperties());
            }
        }

        Object[] factories = factoryTracker.getServices();
        if (factories != null) {
            for (Object object : factories) {
                PropertyMixinFactory factory = (PropertyMixinFactory) object;
                PropertyMixin mixin = factory.create(sessionHolder);
                allProperties.addAll(mixin.getAllProperties());
            }
        }

        return allProperties;
    }

    @Override
    public WebdavProperty getProperty(String namespace, String name) throws OXException {
        Object[] mixins = mixinTracker.getServices();
        if (mixins != null) {
            for (Object object : mixins) {
                PropertyMixin mixin = (PropertyMixin) object;
                WebdavProperty property = mixin.getProperty(namespace, name);
                if (property != null) {
                    return property;
                }
            }
        }

        Object[] factories = factoryTracker.getServices();
        if (factories != null) {
            for (Object object : factories) {
                PropertyMixinFactory factory = (PropertyMixinFactory) object;
                PropertyMixin mixin = factory.create(sessionHolder);
                WebdavProperty property = mixin.getProperty(namespace, name);
                if (property != null) {
                    return property;
                }
            }
        }
        return null;
    }

}
