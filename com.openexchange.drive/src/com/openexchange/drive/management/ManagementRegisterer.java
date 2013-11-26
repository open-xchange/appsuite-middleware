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

package com.openexchange.drive.management;

import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.management.ManagementExceptionCode;
import com.openexchange.management.ManagementService;

/**
 * {@link ManagementRegisterer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ManagementRegisterer implements ServiceTrackerCustomizer<ManagementService, ManagementService> {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ManagementRegisterer.class));

    private final BundleContext context;
    private final ObjectName objectName;

    /**
     * Initializes a new {@link ManagementRegisterer}.
     *
     * @param context The bundle context
     */
    public ManagementRegisterer(BundleContext context) throws OXException {
        super();
        this.context = context;
        try {
            this.objectName = new ObjectName("com.openexchange.drive", "name", "Drive Configuration");
        } catch (MalformedObjectNameException e) {
            throw ManagementExceptionCode.MALFORMED_OBJECT_NAME.create(e);
        }
    }

    @Override
    public ManagementService addingService(ServiceReference<ManagementService> reference) {
        ManagementService management = context.getService(reference);
        try {
            management.registerMBean(objectName, new DriveConfigMBeanImpl());
        } catch (OXException e) {
            LOG.error("Error registering MBean", e);
        } catch (NotCompliantMBeanException e) {
            LOG.error("Error registering MBean", e);
        }
        return management;
    }

    @Override
    public void modifiedService(ServiceReference<ManagementService> reference, ManagementService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<ManagementService> reference, ManagementService service) {
        try {
            service.unregisterMBean(objectName);
        } catch (OXException e) {
            LOG.error("Error unregistering MBean", e);
        }
        context.ungetService(reference);
    }

}
