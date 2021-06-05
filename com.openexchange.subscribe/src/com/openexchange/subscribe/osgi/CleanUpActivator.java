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

package com.openexchange.subscribe.osgi;

import com.openexchange.context.ContextService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.SubscriptionStorage;
import com.openexchange.subscribe.internal.FolderCleanUpEventHandler;


/**
 * {@link CleanUpActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class CleanUpActivator extends HousekeepingActivator {

    private FolderCleanUpEventHandler folderCleanUpEventHandler;

    @Override
    public synchronized void startBundle() throws Exception {
        final ContextService contexts = getService(ContextService.class);
        final SubscriptionStorage storage = AbstractSubscribeService.STORAGE.get();
        if (null != storage) {
            folderCleanUpEventHandler = new FolderCleanUpEventHandler(context, storage, contexts);
        }
    }

    @Override
    public synchronized void stopBundle() throws Exception {
        final FolderCleanUpEventHandler folderCleanUpEventHandler = this.folderCleanUpEventHandler;
        if (null != folderCleanUpEventHandler) {
            folderCleanUpEventHandler.close();
            this.folderCleanUpEventHandler = null;
        }
        super.stopBundle();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ContextService.class };
    }

}
