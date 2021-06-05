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

import javax.management.ObjectName;
import javax.management.StandardMBean;


/**
 * ManagementObject to use it in the {@link ManagementService}. As a subclass of StandardMBean it allows to create MBeans and MXBeans.
 * Furthermore it provides the ObjectName used to register this Instance in the {@link ManagementService}. Subclasses are enforced to
 * implement the interface T and provide it as Argument to the constructor call {@link ManagementObject#ManagementObject(Class)}. Not
 * implementing the interface T will result in an IllegalArgumentException.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public abstract class ManagementObject<T> extends StandardMBean {

    /**
     * Initializes a new {@link ManagementObject} MBean
     * 
     * @param mbeanInterface The implemented management interface
     * @throws IllegalArgumentException if the mbeanInterface does not follow JMX design patterns for management interfaces, or if this does
     *             not implement the specified interface.
     */
    protected ManagementObject(Class<T> mbeanInterface) throws IllegalArgumentException {
        super(mbeanInterface, false);
    }

    /**
     * Initializes a new {@link ManagementObject} which is either a MBean or a MxBean
     * 
     * @param mbeanInterface The implemented management interface
     * @param isMxBean If true, the mbeanInterface parameter names an MXBean interface and the resultant MBean is an MXBean.
     * @throws IllegalArgumentException if the mbeanInterface does not follow JMX design patterns for Management Interfaces, or if this does
     *             not implement the specified interface.
     */
    protected ManagementObject(Class<T> mbeanInterface, boolean isMxBean) throws IllegalArgumentException {
        super(mbeanInterface, isMxBean);
    }

    /**
     * Get the ObjectName used to register this ManagementObject
     * @return The ObjectName used to register this ManagementObject
     */
    public abstract ObjectName getObjectName();

}
