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
     * @return The service or <code>null</code>
     */
    protected <S> S getService(Class<? extends S> clazz) {
        return services.getOptionalService(clazz);
    }
}
