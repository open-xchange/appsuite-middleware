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
 *    trademarks of the OX Software GmbH. group of companies.
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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.slf4j.Logger;

/**
 * {@link AnnotatedStandardMBean} - The extension of {@link StandardMBean} that will automatically provide JMX meta-data through annotations.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 * @see MBeanMethodAnnotation
 */
public class AnnotatedStandardMBean extends StandardMBean {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AnnotatedStandardMBean.class);

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
        if (interfaces.length == 1) { // just in case, should always be equals to 1
            Method[] methods = interfaces[0].getMethods();
            for (Method m : methods) {
                if (m.isAnnotationPresent(MBeanMethodAnnotation.class)) {
                    MBeanMethodAnnotation a = m.getAnnotation(MBeanMethodAnnotation.class);
                    methodParameters.put(m.getName(), a.parameters());
                    methodDescriptions.put(m.getName(), a.description());
                    methodParameterDescriptions.put(m.getName(), a.parameterDescriptions());
                }
            }
        } else {
            LOG.error("Cannot initialize annotations");
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
