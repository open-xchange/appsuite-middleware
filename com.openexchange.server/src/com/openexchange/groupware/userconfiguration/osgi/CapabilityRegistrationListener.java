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

package com.openexchange.groupware.userconfiguration.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.impl.ConfigTree;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link CapabilityRegistrationListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CapabilityRegistrationListener implements ServiceTrackerCustomizer<CapabilityService, CapabilityService> {

    private final BundleContext context;

    private volatile ServiceRegistration<CapabilityChecker> checkerRegistration1;
    private volatile ServiceRegistration<CapabilityChecker> checkerRegistration2;

    /**
     * Initializes a new {@link CapabilityRegistrationListener}.
     */
    public CapabilityRegistrationListener(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public CapabilityService addingService(ServiceReference<CapabilityService> ref) {
        CapabilityService capabilityService = context.getService(ref);
        ServerServiceRegistry.getInstance().addService(CapabilityService.class, capabilityService);

        {
            Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
            final String sCapability = Permission.EDIT_GROUP.getCapabilityName();
            properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
            checkerRegistration1 = context.registerService(CapabilityChecker.class, new CapabilityChecker() {

                @Override
                public boolean isEnabled(String capability, Session session) throws OXException {
                    if (sCapability.equals(capability)) {
                        if (session.getUserId() <= 0) {
                            return false;
                        }

                        // Currently availability is signaled through registration of associated config-tree setting
                        if (null == ConfigTree.getInstance().optSettingByPath("modules/com.openexchange.group")) {
                            // Service not availble
                            return false;
                        }

                        UserPermissionService permissionService = ServerServiceRegistry.getInstance().getService(UserPermissionService.class);
                        return permissionService.getUserPermissionBits(session.getUserId(), session.getContextId()).isEditGroup();
                    }

                    return true;
                }
            }, properties);

            capabilityService.declareCapability(sCapability);
        }

        {
            Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
            final String sCapability = Permission.EDIT_RESOURCE.getCapabilityName();
            properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, sCapability);
            checkerRegistration2 = context.registerService(CapabilityChecker.class, new CapabilityChecker() {

                @Override
                public boolean isEnabled(String capability, Session session) throws OXException {
                    if (sCapability.equals(capability)) {
                        if (session.getUserId() <= 0) {
                            return false;
                        }

                        // Currently availability is signaled through registration of associated config-tree setting
                        if (null == ConfigTree.getInstance().optSettingByPath("modules/com.openexchange.resource")) {
                            // Service not availble
                            return false;
                        }

                        UserPermissionService permissionService = ServerServiceRegistry.getInstance().getService(UserPermissionService.class);
                        return permissionService.getUserPermissionBits(session.getUserId(), session.getContextId()).isEditResource();
                    }

                    return true;
                }
            }, properties);

            capabilityService.declareCapability(sCapability);
        }

        return capabilityService;
    }

    @Override
    public void modifiedService(ServiceReference<CapabilityService> reference, CapabilityService capabilityService) {
        // Nothing to do
    }

    @Override
    public void removedService(ServiceReference<CapabilityService> reference, CapabilityService capabilityService) {
        ServiceRegistration<CapabilityChecker> registration = this.checkerRegistration1;
        if (registration != null) {
            registration.unregister();
            this.checkerRegistration1 = null;
        }

        registration = this.checkerRegistration2;
        if (registration != null) {
            registration.unregister();
            this.checkerRegistration2 = null;
        }

        capabilityService.undeclareCapability(Permission.EDIT_GROUP.getCapabilityName());
        capabilityService.undeclareCapability(Permission.EDIT_RESOURCE.getCapabilityName());

        ServerServiceRegistry.getInstance().removeService(CapabilityService.class);
        context.ungetService(reference);
    }

}
