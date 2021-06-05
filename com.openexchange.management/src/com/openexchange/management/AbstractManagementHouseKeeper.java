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
