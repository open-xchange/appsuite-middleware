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

package com.openexchange.secret.recovery.json.osgi;

import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.SecretUsesPasswordChecker;
import com.openexchange.secret.recovery.EncryptedItemCleanUpService;
import com.openexchange.secret.recovery.SecretInconsistencyDetector;
import com.openexchange.secret.recovery.SecretMigrator;
import com.openexchange.secret.recovery.json.SecretRecoveryActionFactory;
import com.openexchange.secret.recovery.json.preferences.Enabled;
import com.openexchange.server.ExceptionOnAbsenceServiceLookup;

public class SecretRecoveryJSONActivator extends AJAXModuleActivator {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SecretRecoveryJSONActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { SecretInconsistencyDetector.class, SecretService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            ServiceSet<SecretMigrator> secretMigrators = new ServiceSet<SecretMigrator>();
            ServiceSet<EncryptedItemCleanUpService> cleanUpServices = new ServiceSet<EncryptedItemCleanUpService>();

            track(SecretMigrator.class, secretMigrators);
            track(EncryptedItemCleanUpService.class, cleanUpServices);
            trackService(SecretUsesPasswordChecker.class);

            openTrackers();

            registerModule(new SecretRecoveryActionFactory(new ExceptionOnAbsenceServiceLookup(this), secretMigrators, cleanUpServices), "recovery/secret");
            registerService(PreferencesItemService.class, new Enabled());
        } catch (Exception x) {
            LOG.error("", x);
            throw x;
        }

    }

}
