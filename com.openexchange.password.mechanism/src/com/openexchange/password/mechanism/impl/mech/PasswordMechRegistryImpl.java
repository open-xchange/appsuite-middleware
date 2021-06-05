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

package com.openexchange.password.mechanism.impl.mech;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.java.Strings;
import com.openexchange.password.mechanism.PasswordMech;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.password.mechanism.stock.StockPasswordMechs;

/**
 * {@link PasswordMechRegistryImpl}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class PasswordMechRegistryImpl implements PasswordMechRegistry, Reloadable {

    private final static Logger LOG = LoggerFactory.getLogger(PasswordMechRegistryImpl.class);

    private final Map<String, PasswordMech> registeredPasswordMechs = new ConcurrentSkipListMap<String, PasswordMech>(String.CASE_INSENSITIVE_ORDER);

    // -----------------------------------------------------------------------------------

    /**
     * Initializes a new {@link PasswordMechRegistryImpl}.
     * 
     * @param configurationService A reference to the configuration service
     */
    public PasswordMechRegistryImpl(ConfigurationService configurationService) {
        super();
        register(
            StockPasswordMechs.BCRYPT.getPasswordMech(),
            StockPasswordMechs.CRYPT.getPasswordMech(),
            StockPasswordMechs.SHA1.getPasswordMech(),
            StockPasswordMechs.SHA256.getPasswordMech(),
            StockPasswordMechs.SHA512.getPasswordMech()
        );
        setDefaultMech(configurationService.getProperty(DEFAULT_MECH));
    }

    public void register(PasswordMech... passwordMech) {
        for (PasswordMech mech : passwordMech) {
            String id = adaptIdentifier(mech.getIdentifier());
            registeredPasswordMechs.put(id, mech);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PasswordMech get(String identifier) {
        String id = adaptIdentifier(identifier);
        return registeredPasswordMechs.get(id);
    }

    private volatile PasswordMech defaultMech = StockPasswordMechs.SHA256.getPasswordMech();

    @Override
    public PasswordMech getDefault() {
        return defaultMech;
    }

    public void setDefaultMech(String identifier) {
        if (Strings.isNotEmpty(identifier)) {
            String mech = adaptIdentifier(identifier);
            PasswordMech passwordMech = registeredPasswordMechs.get(mech);
            if (null != passwordMech) {
                defaultMech = passwordMech;
                return;
            }
        }
        PasswordMech passwordMech = StockPasswordMechs.SHA256.getPasswordMech();
        LOG.warn("Unable to find a registered implementation for the provided password mechanism '{}'. Falling back to {}. Available password mechanisms are: {}", identifier, passwordMech.getIdentifier(), String.join(",", getIdentifiers()));
        defaultMech = passwordMech;
    }

    /**
     * Adapts the given identifier to the expected (internal) format.
     *
     * @param identifier The given identifier
     * @return the expected identifier that looks like {UPPER_CASE_MECHANISM}
     */
    private static String adaptIdentifier(String identifier) {
        if (Strings.isEmpty(identifier)) {
            return identifier;
        }
        String id = Strings.toUpperCase(identifier);
        if (!id.startsWith("{")) {
            id = new StringBuilder(id.length() + 1).append('{').append(id).toString();
        }
        if (!id.endsWith("}")) {
            id = new StringBuilder(id.length() + 1).append(id).append('}').toString();
        }
        return id;
    }

    // -----------------------------------------------------------------------------------

    private final static String DEFAULT_MECH = "DEFAULT_PASSWORD_MECHANISM";

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        setDefaultMech(configService.getProperty(DEFAULT_MECH));
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(DEFAULT_MECH);
    }

    @Override
    public List<String> getIdentifiers() {
        return registeredPasswordMechs.entrySet().stream().filter(x -> x.getValue().isExposed()).map(x -> x.getKey()).collect(Collectors.toList());
    }

    public void unregister(PasswordMech passwordMech) {
        String id = adaptIdentifier(passwordMech.getIdentifier());
        registeredPasswordMechs.remove(id);
    }
}
