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
import com.openexchange.folderstorage.internal.FolderI18nNamesServiceImpl;
import com.openexchange.group.internal.GroupI18nNamesService;
import com.openexchange.i18n.I18nService;

/**
 * Tracks {@link I18nService} instances.
 */
public class I18nServiceListener implements ServiceTrackerCustomizer<I18nService,I18nService>{

    private final BundleContext context;

    public I18nServiceListener(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public I18nService addingService(final ServiceReference<I18nService> reference) {
        I18nService i18n = context.getService(reference);

        FolderI18nNamesServiceImpl.getInstance().addService(i18n);
        GroupI18nNamesService.getInstance().addService(i18n);
        return i18n;
    }

    @Override
    public void modifiedService(final ServiceReference<I18nService> reference, final I18nService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(final ServiceReference<I18nService> reference, final I18nService service) {
        try {
            FolderI18nNamesServiceImpl.getInstance().removeService(service);
            GroupI18nNamesService.getInstance().removeService(service);
        } finally {
            context.ungetService(reference);
        }
    }
}
