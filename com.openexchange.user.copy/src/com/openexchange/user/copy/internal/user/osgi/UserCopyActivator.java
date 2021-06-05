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

package com.openexchange.user.copy.internal.user.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;


/**
 * {@link UserCopyActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UserCopyActivator implements BundleActivator {

    private ServiceTracker<Object, Object> tracker;

    /**
     * Initializes a new {@link UserCopyActivator}.
     */
    public UserCopyActivator() {
        super();
    }

    @Override
    public synchronized void start(final BundleContext context) throws Exception {
        UserCopyTaskRegisterer registerer = new UserCopyTaskRegisterer(context);
        ServiceTracker<Object, Object> tracker = new ServiceTracker<Object, Object>(context, registerer.getFilter(), registerer);
        this.tracker = tracker;
        tracker.open();
    }

    @Override
    public synchronized void stop(final BundleContext context) throws Exception {
        ServiceTracker<Object, Object> tracker = this.tracker;
        if (null != tracker) {
            this.tracker = null;
            tracker.close();
        }
    }

}
