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

package com.openexchange.management;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.ObjectName;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractManagementHouseKeeper} - Housekeeper for {@link ManagementObject}s. Should be used as bundle-wide singleton so classes can
 * register their {@link ManagementObject}s while the Activators take care of exposing or concealing those objects during bundle start and
 * stop. Activators have to initialize the instances with a ServiceLookup instance before trying to expose objects.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public abstract class AbstractManagementHouseKeeper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractManagementHouseKeeper.class);

    private ServiceLookup serviceLookup = null;

    private boolean exposed = false;

    private final ConcurrentHashMap<ObjectName, ManagementObject<?>> managementObjects;

    /**
     * Initializes a new {@link AbstractManagementHouseKeeper}.
     *
     * @param managementObjects
     */
    protected AbstractManagementHouseKeeper() {
        super();
        managementObjects = new ConcurrentHashMap<ObjectName, ManagementObject<?>>();
    }

    /**
     * Initialize the HouseKeeper by setting the needed ServiceLookup instance.
     *
     * @param serviceLookup The needed ServiceLookup instance.
     */
    public void initialize(ServiceLookup serviceLookup) {
        this.serviceLookup = serviceLookup;
    }

    /**
     * Conceal all {@link ManagementObjects} and remove them from the Housekeeper. Furthermore reset all states to uninitialized.
     */
    public void cleanup() throws OXException {
        concealManagementObjects();
        managementObjects.clear();
        this.serviceLookup = null;
    }

    /**
     * Add a new ManagementObject to the Housekeeper
     *
     * @param managementObject The object to add
     * @return true if the object was successfully added, false if an Object with the same name already exists.
     */
    public boolean addManagementObject(ManagementObject<?> managementObject) {
        ManagementObject<?> previous = managementObjects.putIfAbsent(managementObject.getObjectName(), managementObject);
        return previous == null;
    }

    /**
     * Add a new ManagementObject to the Housekeeper
     *
     * @param managementObject The object to add
     * @return true if the object was successfully added, false if an Object with the same name already exists.
     */
    public boolean removeManagementObject(ObjectName objectName) {
        ManagementObject<?> managementObject = managementObjects.remove(objectName);
        return managementObject != null;
    }

    /**
     * Expose all known {@link ManagementObject}s
     *
     * @throws OXException
     */
    public void exposeManagementObjects() throws OXException {
        if (!exposed) {
            ManagementService managementService = getManagementService();
            for (Entry<ObjectName, ManagementObject<?>> entry : managementObjects.entrySet()) {
                managementService.registerMBean(entry.getKey(), entry.getValue());
            }
            exposed = true;
        } else {
            LOG.info("ManagementObjects are already exposed.");
        }
    }

    /**
     * Conceal all known {@link ManagementObject}s
     *
     * @throws OXException
     */
    public void concealManagementObjects() throws OXException {
        if (exposed) {
            ManagementService managementService = getManagementService();
            for (ObjectName objectName : managementObjects.keySet()) {
                managementService.unregisterMBean(objectName);
            }
            exposed = false;
        } else {
            LOG.error("No ManagementObjects have been exposed, yet.");
        }
    }

    /**
     * Get an instance of the ManagementService
     *
     * @return an instance of the ManagementService
     * @throws RealtimeException if the service can't be found
     */
    private ManagementService getManagementService() throws OXException {
        if (serviceLookup == null) {
            throw new IllegalStateException("ManagementHouseKeeper wasn't initialized.");
        }
        ManagementService managementService = serviceLookup.getService(ManagementService.class);
        if (managementService == null) {
            throw ManagementExceptionCode.NEEDED_SERVICE_MISSING.create(ManagementService.class);
        }
        return managementService;
    }

}
