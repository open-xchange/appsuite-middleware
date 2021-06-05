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

package com.openexchange.mail.autoconfig.sources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.DefaultAutoconfig;
import com.openexchange.mail.autoconfig.xmlparser.AutoconfigParser;
import com.openexchange.mail.autoconfig.xmlparser.ClientConfig;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ConfigurationFile}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ConfigurationFile extends AbstractConfigSource {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigurationFile.class);

    private static final String locationProperty = "com.openexchange.mail.autoconfig.path";

    private final ServiceLookup services;

    public ConfigurationFile(ServiceLookup services) {
        this.services = services;
    }

    @Override
    public Autoconfig getAutoconfig(String emailLocalPart, final String emailDomain, String password, int userId, int contextId) throws OXException {
        return getAutoconfig(emailLocalPart, emailDomain, password, userId, contextId, true);
    }

    @Override
    public DefaultAutoconfig getAutoconfig(String emailLocalPart, final String emailDomain, String password, int userId, int contextId, boolean forceSecure) throws OXException {
        ConfigViewFactory configViewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = configViewFactory.getView(userId, contextId);
        String fileLocation = view.get(locationProperty, String.class);
        if (fileLocation == null) {
            return null;
        }
        File configFolder = new File(fileLocation);

        File[] files = configFolder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.equalsIgnoreCase(emailDomain + ".xml");
            }
        });

        if (files == null || files.length == 0) {
            return null;
        }

        FileInputStream fis;
        try {
            fis = new FileInputStream(files[0]);
        } catch (FileNotFoundException e) {
            LOG.warn("Unable to find file: {}", files[0], e);
            return null;
        }
        ClientConfig clientConfig = new AutoconfigParser().getConfig(fis);

        DefaultAutoconfig autoconfig = getBestConfiguration(clientConfig, emailDomain);
        if (null == autoconfig) {
            return null;
        }

        // If 'forceSecure' is true, ensure that both - mail and transport settings - either support SSL or STARTTLS
        if (skipDueToForcedSecure(forceSecure, autoconfig)) {
            // Either mail or transport do not support a secure connection (or neither of them)
            return null;
        }

        replaceUsername(autoconfig, emailLocalPart, emailDomain);
        autoconfig.setMailStartTls(forceSecure);
        autoconfig.setTransportStartTls(forceSecure);
        return autoconfig;
    }

}
