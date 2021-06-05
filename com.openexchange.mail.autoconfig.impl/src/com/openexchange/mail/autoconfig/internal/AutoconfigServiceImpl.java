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

package com.openexchange.mail.autoconfig.internal;

import java.util.List;
import javax.mail.internet.AddressException;
import com.google.common.collect.ImmutableList;
import com.openexchange.exception.OXException;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.AutoconfigException;
import com.openexchange.mail.autoconfig.AutoconfigService;
import com.openexchange.mail.autoconfig.DefaultAutoconfig;
import com.openexchange.mail.autoconfig.sources.ConfigServer;
import com.openexchange.mail.autoconfig.sources.ConfigSource;
import com.openexchange.mail.autoconfig.sources.ConfigurationFile;
import com.openexchange.mail.autoconfig.sources.Guess;
import com.openexchange.mail.autoconfig.sources.ISPDB;
import com.openexchange.mail.autoconfig.sources.staticsource.KnownStaticConfigSource;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AutoconfigServiceImpl}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AutoconfigServiceImpl implements AutoconfigService {

    private final List<ConfigSource> sources;

    /**
     * Initializes a new {@link AutoconfigServiceImpl}.
     *
     * @param services THe service look-up
     */
    public AutoconfigServiceImpl(final ServiceLookup services) {
        super();
        KnownStaticConfigSource[] staticConfigSources = KnownStaticConfigSource.values();

        ImmutableList.Builder<ConfigSource> sources = ImmutableList.builderWithExpectedSize(staticConfigSources.length + 4);
        sources.add(new ConfigurationFile(services));
        sources.add(new ConfigServer(services));
        sources.add(new ISPDB(services));
        for (KnownStaticConfigSource staticConfigSource : staticConfigSources) {
            sources.add(staticConfigSource);
        }
        sources.add(new Guess(services));

        this.sources = sources.build();
    }

    @Override
    public Autoconfig getConfig(final String email, final String password, final int userId, final int contextId) throws OXException {
        return getConfig(email, password, userId, contextId, true);
    }

    @Override
    public Autoconfig getConfig(final String email, final String password, final int userId, final int contextId, boolean forceSecure) throws OXException {
        QuotedInternetAddress internetAddress;
        try {
            internetAddress = new QuotedInternetAddress(email);
        } catch (AddressException e) {
            throw AutoconfigException.invalidMail(email);
        }

        final String mailLocalPart;
        final String mailDomain;
        try {
            mailLocalPart = getLocalPart(internetAddress);
            mailDomain = getDomain(internetAddress);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }

        for (ConfigSource source : sources) {
            DefaultAutoconfig config = source.getAutoconfig(mailLocalPart, mailDomain, password, userId, contextId, forceSecure);
            if (config != null) {
                config.setSource(source.getClass().getSimpleName());
                return config;
            }
        }

        return null;
    }

    protected String getDomain(final QuotedInternetAddress internetAddress) {
        String address = internetAddress.getAddress();
        int pos = address.indexOf('@');
        return pos > 0 ? address.substring(pos + 1) : address;
    }

    private String getLocalPart(final QuotedInternetAddress internetAddress) {
        String address = internetAddress.getAddress();
        int pos = address.indexOf('@');
        return pos > 0 ? address.substring(0, pos) : address;
    }

}
