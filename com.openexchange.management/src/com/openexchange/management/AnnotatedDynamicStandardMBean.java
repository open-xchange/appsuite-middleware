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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.ReflectionException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AnnotatedDynamicStandardMBean}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AnnotatedDynamicStandardMBean extends AnnotatedStandardMBean implements DynamicMBean {

    private final ServiceLookup services;

    private final AtomicLong refreshTime;
    private static final long REFRESH_INTERVAL = TimeUnit.MINUTES.toMillis(5);

    /**
     * Initialises a new {@link AnnotatedDynamicStandardMBean}.
     * 
     * @param services The {@link ServiceLookup} instance
     * @param description The description of this MBean
     * @param mbeanInterface The Class
     * @throws NotCompliantMBeanException
     */
    public AnnotatedDynamicStandardMBean(ServiceLookup services, String description, Class<?> mbeanInterface) throws NotCompliantMBeanException {
        super(description, mbeanInterface);
        this.services = services;
        refreshTime = new AtomicLong(0);
    }

    /**
     * Performs a refresh of the metrics/statistics if necessary.
     */
    protected abstract void refresh();

    /*
     * (non-Javadoc)
     * 
     * @see javax.management.StandardMBean#getAttributes(java.lang.String[])
     */
    @Override
    public AttributeList getAttributes(String[] attributes) {
        // Refresh information (if necessary)
        AttributeList list = new AttributeList(attributes.length);
        try {
            for (String attribute : attributes) {
                list.add(new Attribute(attribute, getAttribute(attribute)));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return list;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.management.StandardMBean#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        long now = System.currentTimeMillis();
        if (now - refreshTime.get() >= REFRESH_INTERVAL) {
            refreshTime.set(now);
            refresh();
        }
        return super.getAttribute(attribute);
    }

    /**
     * Get the specified service
     * 
     * @param clazz The service
     * @return The service
     */
    protected <S> S getService(Class<? extends S> clazz) {
        return services.getService(clazz);
    }
}
