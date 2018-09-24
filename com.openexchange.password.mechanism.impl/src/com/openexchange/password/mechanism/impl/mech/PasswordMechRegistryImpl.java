/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import com.openexchange.password.mechanism.IPasswordMech;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.password.mechanism.impl.algorithm.SHACrypt;
import com.openexchange.password.mechanism.impl.osgi.Services;

/**
 * {@link PasswordMechRegistryImpl}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class PasswordMechRegistryImpl implements PasswordMechRegistry, Reloadable {

    private final static Logger LOG = LoggerFactory.getLogger(PasswordMechRegistryImpl.class);

    private final Map<String, IPasswordMech> registeredPasswordMechs = new ConcurrentSkipListMap<String, IPasswordMech>(String.CASE_INSENSITIVE_ORDER);

    private static final String COM_OPENEXCHANGE_PASSWORD_MECHANISM_ENHANCED_ENABLED = "com.openexchange.password.mechanism.enhanced.enabled";

    // -----------------------------------------------------------------------------------

    // Dirty hack to keep 'GenerateMasterPasswordCLT' working

    private final static PasswordMechRegistryImpl INSTANCE = new PasswordMechRegistryImpl();

    public static PasswordMechRegistry getInstance() {
        return INSTANCE;
    }

    private PasswordMechRegistryImpl() {
        super();
        ConfigurationService configurationService = Services.getService(ConfigurationService.class);
        if (configurationService != null) {
            if (configurationService.getBoolProperty(COM_OPENEXCHANGE_PASSWORD_MECHANISM_ENHANCED_ENABLED, false)) {
                register(new SHAMech(SHACrypt.SHA256), defaultMech, new SHAMech(SHACrypt.SHA512), new BCryptMech(), new CryptMech());
                return;
            }
        }
        register(defaultMech, new BCryptMech(), new CryptMech());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(IPasswordMech... passwordMech) {
        for (IPasswordMech mech : passwordMech) {
            String id = adaptIdentifier(mech.getIdentifier());
            registeredPasswordMechs.put(id, mech);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPasswordMech get(String identifier) {
        String id = adaptIdentifier(identifier);
        return registeredPasswordMechs.get(id);
    }

    private IPasswordMech defaultMech = new SHAMech(SHACrypt.SHA1);

    @Override
    public IPasswordMech getDefault() {
        return defaultMech;
    }

    public void setDefaultMech(String identifier) {
        if (Strings.isNotEmpty(identifier)) {
            String mech = adaptIdentifier(identifier);
            if (registeredPasswordMechs.containsKey(mech)) {
                defaultMech = registeredPasswordMechs.get(mech);
                return;
            }
        }
        LOG.warn("Unable to find a registered implementation for the provided password mechanism '{}'. Will use the default {}. Available password mechanisms are: {}", identifier, defaultMech.getIdentifier(), String.join(",", getIdentifiers()));
        defaultMech = new SHAMech(SHACrypt.SHA256);
    }

    /**
     * Adapts the given identifier to the expected (internal) format.
     *
     * @param identifier The given identifier
     * @return the expected identifier that looks like {UPPER_CASE_MECHANISM}
     */
    private static String adaptIdentifier(String identifier) {
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
        return registeredPasswordMechs.entrySet().stream().filter(x -> x.getValue().expose()).map(x -> x.getKey()).collect(Collectors.toList());
    }

    @Override
    public void unregister(IPasswordMech passwordMech) {
        String id = adaptIdentifier(passwordMech.getIdentifier());
        registeredPasswordMechs.remove(id);
    }
}
