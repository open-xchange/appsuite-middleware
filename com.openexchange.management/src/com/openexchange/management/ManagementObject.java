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
