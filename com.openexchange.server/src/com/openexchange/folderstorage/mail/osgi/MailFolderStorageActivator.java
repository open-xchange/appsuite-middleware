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

package com.openexchange.folderstorage.mail.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.service.event.EventAdmin;
import com.openexchange.config.ConfigurationService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.mail.MailFolderStorage;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link MailFolderStorageActivator} - {@link BundleActivator Activator} for mail folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailFolderStorageActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link MailFolderStorageActivator}.
     */
    public MailFolderStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { MailAccountStorageService.class, UnifiedInboxManagement.class, EventAdmin.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            Services.setServiceLookup(this);
            // Register folder storage
            final Dictionary<String, String> dictionary = new Hashtable<String, String>(2);
            dictionary.put("tree", FolderStorage.REAL_TREE_ID);
            registerService(FolderStorage.class, new MailFolderStorage(), dictionary);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(MailFolderStorageActivator.class).error("", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
