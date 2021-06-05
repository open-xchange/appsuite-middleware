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

package com.openexchange.file.storage.limit.osgi;

import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.limit.FileLimitService;
import com.openexchange.file.storage.limit.impl.DefaultFileLimitService;
import com.openexchange.file.storage.limit.type.TypeLimitChecker;
import com.openexchange.file.storage.limit.type.impl.FileStorageLimitChecker;
import com.openexchange.file.storage.limit.type.impl.PIMLimitChecker;
import com.openexchange.file.storage.limit.type.impl.TypeLimitCheckerRegistry;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * 
 * {@link LimitActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class LimitActivator extends HousekeepingActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LimitActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { IDBasedFolderAccessFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: {}", this.context.getBundle().getSymbolicName());

        Services.setServiceLookup(this);

        TypeLimitCheckerRegistry registry = TypeLimitCheckerRegistry.getInstance();
        registry.register(new FileStorageLimitChecker(), new PIMLimitChecker());
        registerService(FileLimitService.class, new DefaultFileLimitService(registry));

        track(TypeLimitChecker.class, new TypeFileLimitServiceTracker(context, registry));

        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle: {}", this.context.getBundle().getSymbolicName());

        Services.setServiceLookup(null);
        super.stopBundle();
    }
}
