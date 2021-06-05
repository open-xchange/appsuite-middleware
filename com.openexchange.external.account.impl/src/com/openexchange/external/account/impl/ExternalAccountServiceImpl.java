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

package com.openexchange.external.account.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.external.account.ExternalAccount;
import com.openexchange.external.account.ExternalAccountExceptionCodes;
import com.openexchange.external.account.ExternalAccountModule;
import com.openexchange.external.account.ExternalAccountProvider;
import com.openexchange.external.account.ExternalAccountService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ExternalAccountServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class ExternalAccountServiceImpl implements ExternalAccountService {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalAccountServiceImpl.class);
    private final ServiceLookup services;
    private final ExternalAccountProviderRegistry registry;

    /**
     * Initializes a new {@link ExternalAccountServiceImpl}.
     *
     * @param registry The provider registry
     * @param services The {@link ServiceLookup} instance
     */
    public ExternalAccountServiceImpl(ExternalAccountProviderRegistry registry, ServiceLookup services) {
        super();
        this.registry = registry;
        this.services = services;
    }

    @Override
    public List<ExternalAccount> list(int contextId) throws OXException {
        List<ExternalAccount> accounts = new LinkedList<>();
        for (ExternalAccountModule module : ExternalAccountModule.values()) {
            optProviderFor(module).ifPresent(provider -> {
                try {
                    accounts.addAll(provider.list(contextId));
                } catch (OXException e) {
                    LOG.error("Failed to list external accounts from provider '{}'", module, e);
                }
            });
        }
        return accounts;
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId) throws OXException {
        List<ExternalAccount> accounts = new LinkedList<>();
        for (ExternalAccountModule module : ExternalAccountModule.values()) {
            optProviderFor(module).ifPresent(provider -> {
                try {
                    accounts.addAll(provider.list(contextId, userId));
                } catch (OXException e) {
                    LOG.error("Failed to list external accounts from provider '{}'", module, e);
                }
            });
        }
        return accounts;
    }

    @Override
    public List<ExternalAccount> list(int contextId, String providerId) throws OXException {
        List<ExternalAccount> accounts = new LinkedList<>();
        for (ExternalAccountModule module : ExternalAccountModule.values()) {
            try {
                accounts.addAll(list(contextId, providerId, module));
            } catch (OXException e) {
                LOG.error("Failed to list external accounts from provider '{}'", module, e);
            }
        }
        return accounts;
    }

    @Override
    public List<ExternalAccount> list(int contextId, ExternalAccountModule module) throws OXException {
        return getProviderFor(module).list(contextId);
    }

    @Override
    public List<ExternalAccount> list(int contextId, String providerId, ExternalAccountModule module) throws OXException {
        return getProviderFor(module).list(contextId, providerId).parallelStream().collect(Collectors.toList());
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId, ExternalAccountModule module) throws OXException {
        return getProviderFor(module).list(contextId, userId);
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId, String providerId) throws OXException {
        List<ExternalAccount> accounts = new LinkedList<>();
        for (ExternalAccountModule module : ExternalAccountModule.values()) {
            try {
                accounts.addAll(list(contextId, userId, providerId, module));
            } catch (OXException e) {
                LOG.error("Failed to list external accounts from provider '{}'", module, e);
            }
        }
        return accounts;
    }

    @Override
    public List<ExternalAccount> list(int contextId, int userId, String providerId, ExternalAccountModule module) throws OXException {
        return getProviderFor(module).list(contextId, userId, providerId);
    }

    @Override
    public boolean delete(int id, int contextId, int userId, ExternalAccountModule module) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getWritable(contextId);
        int rollback = 0;
        boolean deleted = false;
        try {
            connection.setAutoCommit(false);
            rollback = 1;

            deleted = getProviderFor(module).delete(id, contextId, userId, connection);

            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw ExternalAccountExceptionCodes.SQL_ERROR.create(e);
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
            databaseService.backWritable(contextId, connection);
        }
        return deleted;
    }

    //////////////////////////// HELPERS ////////////////////////////

    /**
     * Returns the {@link ExternalAccountProvider} for the specified module
     *
     * @param module The {@link ExternalAccountModule}
     * @return The {@link ExternalAccountProvider}
     * @throws OXException if the {@link ExternalAccountRMIServiceImpl} is absent
     *             or the provider does not exist
     */
    private ExternalAccountProvider getProviderFor(ExternalAccountModule module) throws OXException {
        return registry.getProviderFor(module);
    }

    /**
     * Returns the {@link ExternalAccountProvider} for the specified module
     *
     * @param module The {@link ExternalAccountModule}
     * @return The {@link ExternalAccountProvider}
     */
    private Optional<ExternalAccountProvider> optProviderFor(ExternalAccountModule module) {
        return registry.optProviderFor(module);
    }

    /**
     * Returns the {@link DatabaseService}
     *
     * @return The {@link DatabaseService}
     * @throws OXException if the service is absent
     */
    private DatabaseService getDatabaseService() throws OXException {
        return services.getServiceSafe(DatabaseService.class);
    }
}
