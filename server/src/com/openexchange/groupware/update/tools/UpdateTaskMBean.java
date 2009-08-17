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

package com.openexchange.groupware.update.tools;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import com.openexchange.groupware.update.exception.UpdateException;

/**
 * {@link UpdateTaskMBean} - MBean for update task toolkit.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateTaskMBean implements DynamicMBean {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(UpdateTaskMBean.class);

    private final MBeanInfo mbeanInfo;

    /**
     * Initializes a new {@link UpdateTaskMBean}.
     */
    public UpdateTaskMBean() {
        super();
        mbeanInfo = buildMBeanInfo();
    }

    private MBeanInfo buildMBeanInfo() {
        final MBeanParameterInfo[] params = new MBeanParameterInfo[] {
            new MBeanParameterInfo("versionNumber", "java.lang.Integer", "The version number to set"),
            new MBeanParameterInfo("contextId", "java.lang.Integer", "A valid context identifier contained in target schema") };
        final MBeanOperationInfo resetOperation = new MBeanOperationInfo(
            "resetVersion",
            "Resets the schema's version number to given value.",
            params,
            "void",
            MBeanOperationInfo.ACTION);

        final MBeanOperationInfo[] operations = new MBeanOperationInfo[] { resetOperation };

        return new MBeanInfo(UpdateTaskMBean.class.getName(), "Update task toolkit", null, null, operations, null);
    }

    public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        throw new AttributeNotFoundException("No attribute can be obtained in this MBean");
    }

    public AttributeList getAttributes(final String[] attributes) {
        return new AttributeList();
    }

    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        if (actionName.equals("resetVersion")) {
            try {
                UpdateTaskToolkit.resetVersion(((Integer) params[0]).intValue(), ((Integer) params[1]).intValue());
            } catch (final UpdateException e) {
                LOG.error(e.getMessage(), e);
                final Exception wrapMe = new Exception(e.getMessage());
                throw new MBeanException(wrapMe);
            }
            // Void
            return null;
        }
        throw new ReflectionException(new NoSuchMethodException(actionName));

    }

    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        throw new AttributeNotFoundException("No attribute can be set in this MBean");
    }

    public AttributeList setAttributes(final AttributeList attributes) {
        return new AttributeList();
    }

}
