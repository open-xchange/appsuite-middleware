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
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.impl.ConfigTree;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class PreferencesCustomizer implements ServiceTrackerCustomizer<PreferencesItemService,PreferencesItemService> {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PreferencesCustomizer.class);
    }

    private final BundleContext context;

    public PreferencesCustomizer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public PreferencesItemService addingService(final ServiceReference<PreferencesItemService> reference) {
        final PreferencesItemService preferencesItem = context.getService(reference);
        try {
            ConfigTree.getInstance().addPreferencesItem(preferencesItem);
            return preferencesItem;
        } catch (OXException e) {
            Object arg = new Object() {
                @Override
                public String toString() {
                    String[] path = preferencesItem.getPath();
                    int length = path.length;
                    if (length <= 0) {
                        return "";
                    }

                    StringBuilder sb = new StringBuilder(length << 2);
                    sb.append(path[0]);
                    for (int i = 1; i < length; i++) {
                        sb.append('/').append(path[i]);
                    }
                    return sb.toString();
                }
            };
            LoggerHolder.LOG.error("Can't add service for preferences item. Path: {}", arg, e);
        }
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<PreferencesItemService> reference, final PreferencesItemService preferencesItem) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<PreferencesItemService> reference, final PreferencesItemService preferencesItem) {
        ConfigTree.getInstance().removePreferencesItem(preferencesItem);
        context.ungetService(reference);
    }
}
