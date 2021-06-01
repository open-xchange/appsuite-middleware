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

package com.openexchange.server.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.server.impl.Constants;


/**
 * {@link ModuleSpecificServiceTracker}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ModuleSpecificServiceTracker<T> extends ServiceTracker {

    public ModuleSpecificServiceTracker(BundleContext context, Class<T> toTrack) {
        super(context, toTrack.getName(), null);
    }

    @Override
    public Object addingService(ServiceReference reference) {
        T tracked = (T) context.getService(reference);
        Object property = reference.getProperty(Constants.OX_MODULE);
        int module = atoi(property);
        addingService(module, tracked, reference);
        return tracked;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        T tracked = (T) getService(reference);
        Object property = reference.getProperty(Constants.OX_MODULE);
        int module = atoi(property);
        modifiedService(module, tracked, reference);
    }


    @Override
    public void removedService(ServiceReference reference, Object service) {
        T tracked = (T) getService(reference);
        Object property = reference.getProperty(Constants.OX_MODULE);
        int module = atoi(property);
        removedService(module, tracked, reference);
        context.ungetService(reference);
    }

    protected int atoi(Object property) {
        if (Integer.class.isInstance(property)) {
            return (Integer) property;
        }
        return Integer.parseInt(property.toString());
    }

    public void removedService(int module, T tracked, ServiceReference reference) {
        // Nothing to do

    }

    public void addingService(int module, T tracked, ServiceReference reference) {
        // Nothing to do

    }

    public void modifiedService(int module, T tracked, ServiceReference reference) {
        // Nothing to do

    }


}
