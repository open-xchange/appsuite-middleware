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

package com.openexchange.chronos.alarm.message.impl;

import java.util.concurrent.ConcurrentHashMap;
import org.osgi.framework.ServiceReference;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.alarm.message.AlarmNotificationService;
import com.openexchange.osgi.SimpleRegistryListener;

/**
 * {@link AlarmNotificationServiceRegistry}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class AlarmNotificationServiceRegistry implements SimpleRegistryListener<AlarmNotificationService>{

    ConcurrentHashMap<AlarmAction, AlarmNotificationService> services = new ConcurrentHashMap<>(2);

    @Override
    public void added(ServiceReference<AlarmNotificationService> ref, AlarmNotificationService service) {
        services.put(service.getAction(), service);
    }

    @Override
    public void removed(ServiceReference<AlarmNotificationService> ref, AlarmNotificationService service) {
        services.remove(service.getAction());
    }

    public AlarmNotificationService getService(AlarmAction action){
        return services.get(action);
    }

    /**
     * Returns the current registered {@link AlarmAction}s
     *
     * @return The registered {@link AlarmAction}s
     */
    public AlarmAction[] getActions() {
        return services.keySet().toArray(new AlarmAction[services.size()]);
    }


}
