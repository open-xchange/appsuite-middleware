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

package com.openexchange.secret.recovery.json;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.secret.recovery.EncryptedItemCleanUpService;
import com.openexchange.secret.recovery.SecretMigrator;
import com.openexchange.secret.recovery.json.action.CheckAction;
import com.openexchange.secret.recovery.json.action.CleanUpAction;
import com.openexchange.secret.recovery.json.action.MigrateAction;
import com.openexchange.secret.recovery.json.action.RemoveAction;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SecretRecoveryActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SecretRecoveryActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AJAXActionService> actions;

    /**
     * Initializes a new {@link SecretRecoveryActionFactory}.
     *
     * @param services The service look-up
     * @param cleanUpServices
     * @param secretMigrators
     */
    public SecretRecoveryActionFactory(final ServiceLookup services, Set<SecretMigrator> secretMigrators, Set<EncryptedItemCleanUpService> cleanUpServices) {
        super();
        actions = new ConcurrentHashMap<String, AJAXActionService>(3, 0.9f, 1);
        actions.put("check", new CheckAction(services));
        actions.put("migrate", new MigrateAction(services, secretMigrators));
        actions.put("clean_up", new CleanUpAction(services, cleanUpServices));
        actions.put("remove", new RemoveAction(services, cleanUpServices));
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }

}
