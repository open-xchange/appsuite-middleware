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

package com.openexchange.share.impl.osgi;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;
import com.openexchange.share.impl.DefaultShareService;
import com.openexchange.share.impl.ShareMBeanImpl;
import com.openexchange.share.impl.mbean.ShareMBean;


/**
 * {@link ManagementServiceTracker}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public class ManagementServiceTracker implements ServiceTrackerCustomizer<ManagementService, ManagementService> {

    private final BundleContext context;

    private ObjectName objectName;

    private final DefaultShareService defaultShareService;

    /**
     * Initializes a new {@link ManagementServiceTracker}.
     *
     * @param context The OSGi bundle execution context
     * @param shareService A reference to the share service
     */
    ManagementServiceTracker(BundleContext context, DefaultShareService shareService) {
        super();
        this.context = context;
        this.defaultShareService = shareService;
    }

    @Override
    public ManagementService addingService(final ServiceReference<ManagementService> reference) {
        final ManagementService management = context.getService(reference);
        registerMBean(management);
        return management;
    }

    @Override
    public void modifiedService(final ServiceReference<ManagementService> reference, final ManagementService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<ManagementService> reference, final ManagementService service) {
        final ManagementService management = service;
        unregisterMBean(management);
        context.ungetService(reference);
    }

    private void registerMBean(final ManagementService management) {
        if (objectName == null) {
            final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ManagementServiceTracker.class);
            try {
                objectName = getObjectName(ShareMBean.class.getName(), ShareMBean.DOMAIN);
                management.registerMBean(objectName, new ShareMBeanImpl(ShareMBean.class, defaultShareService));
            } catch (final MalformedObjectNameException e) {
                logger.error("", e);
            } catch (final OXException e) {
                logger.error("", e);
            } catch (final Exception e) {
                logger.error("", e);
            }
        }
    }

    private void unregisterMBean(final ManagementService management) {
        if (objectName != null) {
            final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ManagementServiceTracker.class);
            try {
                management.unregisterMBean(objectName);
            } catch (final OXException e) {
                logger.error("", e);
            } finally {
                objectName = null;
            }
        }
    }

    /**
     * Creates an appropriate instance of {@link ObjectName} from specified class name and domain name.
     *
     * @param className The class name to use as object name
     * @param domain The domain name
     * @return An appropriate instance of {@link ObjectName}
     * @throws MalformedObjectNameException If instantiation of {@link ObjectName} fails
     */
    private static ObjectName getObjectName(final String className, final String domain) throws MalformedObjectNameException {
        final int pos = className.lastIndexOf('.');
        return new ObjectName(domain, "name", (pos == -1 ? className : className.substring(pos + 1)));
    }

}
