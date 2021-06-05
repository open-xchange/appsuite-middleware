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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

/**
 * {@link AnnotatedStandardMBean} - The extension of {@link StandardMBean} that will automatically provide JMX meta-data through annotations.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 * @see MBeanMethodAnnotation
 */
public class AnnotatedStandardMBean extends StandardMBean {

    private final Map<String, String> methodDescriptions;
    private final Map<String, String[]> methodParameters;
    private final Map<String, String[]> methodParameterDescriptions;
    private final String description;

    /**
     * Initializes a new {@link AnnotatedStandardMBean}.
     *
     * @param description The MBean's description; e.g. <code>"Management Bean for the XYZ module"</code>
     * @param mbeanInterface The Management Interface exported by this MBean
     * @throws NotCompliantMBeanException If the <code>mbeanInterface</code> does not follow JMX design patterns for Management Interfaces, or if <code>this</code> does not implement the specified interface
     */
    public AnnotatedStandardMBean(String description, Class<?> mbeanInterface) throws NotCompliantMBeanException {
        super(mbeanInterface);
        this.description = description;

        methodDescriptions = new HashMap<String, String>();
        methodParameters = new HashMap<String, String[]>();
        methodParameterDescriptions = new HashMap<String, String[]>();

        Class<?>[] interfaces = this.getClass().getInterfaces();
        for (Class<?> iface : interfaces) {
            Method[] methods = iface.getMethods();
            for (Method m : methods) {
                if (m.isAnnotationPresent(MBeanMethodAnnotation.class)) {
                    MBeanMethodAnnotation a = m.getAnnotation(MBeanMethodAnnotation.class);
                    methodParameters.put(m.getName(), a.parameters());
                    methodDescriptions.put(m.getName(), a.description());
                    methodParameterDescriptions.put(m.getName(), a.parameterDescriptions());
                }
            }
        }
    }

    @Override
    protected final String getDescription(MBeanInfo info) {
        return null == description ? super.getDescription(info) : description;
    }

    @Override
    protected String getDescription(MBeanAttributeInfo info) {
        String desc = methodDescriptions.get("get" + info.getName());
        return null == desc ? super.getDescription(info) : desc;
    }

    @Override
    protected final String getDescription(MBeanOperationInfo info) {
        String desc = methodDescriptions.get(info.getName());
        return null == desc ? super.getDescription(info) : desc;
    }

    @Override
    protected final String getDescription(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        String desc = getMBeanOperationInfo(methodParameterDescriptions, op, param, sequence);
        return null == desc ? super.getDescription(op, param, sequence) : desc;
    }

    @Override
    protected final String getParameterName(MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        String paramName = getMBeanOperationInfo(methodParameters, op, param, sequence);
        return null == paramName ? super.getDescription(op, param, sequence) : paramName;
    }

    /**
     * Delegate method for MBeanOperationInfo
     */
    private final String getMBeanOperationInfo(Map<String, String[]> map, MBeanOperationInfo op, MBeanParameterInfo param, int sequence) {
        String[] v = map.get(op.getName());
        if (v == null || v.length == 0 || sequence > v.length) {
            return super.getDescription(op, param, sequence);
        }
        return v[sequence];
    }
}
