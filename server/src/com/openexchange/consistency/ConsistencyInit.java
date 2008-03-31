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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.consistency;

import com.openexchange.server.Initialization;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXThrows;
import com.openexchange.management.ManagementService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;


/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
@OXExceptionSource(
        classId = ConsistencyClasses.CONSISTENCY_INIT,
        component = Component.CONSISTENCY
        )
public class ConsistencyInit implements Initialization {

    private static final Log LOG = LogFactory.getLog(ConsistencyInit.class);
    private static final ConsistencyExceptionFactory EXCEPTIONS = new ConsistencyExceptionFactory(ConsistencyInit.class);

    private ObjectName name;


    @OXThrows(category = AbstractOXException.Category.INTERNAL_ERROR, desc = "", exceptionId = 1, msg = "Could not register Consistency MBean. Internal Error: %s")
    public void start() throws AbstractOXException {
        ConsistencyMBean bean = new OsgiOXConsistency();
        try {
            registerBean(getName(), bean);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw EXCEPTIONS.createException(1, e, e.getLocalizedMessage());
        }
    }


    @OXThrows(category = AbstractOXException.Category.INTERNAL_ERROR, desc = "", exceptionId = 2, msg = "Could not unregister Consistency MBean. Internal Error: %s")    
    public void stop() throws AbstractOXException {
        try {
            unregisterBean(getName());
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw EXCEPTIONS.createException(2, e, e.getLocalizedMessage());
        }
    }

    private ObjectName getName() {
        return JMXToolkit.getObjectName();
    }


    private void registerBean(ObjectName name, ConsistencyMBean bean) throws Exception {
        ManagementService management = discoverManagementService();
        if(management == null) {
            LOG.error("Can not find JMX management service, skipping export of consistency mbean. The consistency tool will not be available on this server instance.");
            return;            
        }
        management.registerMBean(name, bean);

    }

    private void unregisterBean(ObjectName name) throws Exception {
        ManagementService management = discoverManagementService();
        if(management == null) {
            LOG.info("It seems like the management service has gone away. Skipping unregistration of the consistency mbean.");
        }
        management.unregisterMBean(name);
    }

    private ManagementService discoverManagementService() {
        ManagementService management =  ServerServiceRegistry.getInstance().getService(ManagementService.class);
        return management;
    }
}
