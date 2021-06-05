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

package com.openexchange.file.storage.owncloud.osgi;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.HashBiMap;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.owncloud.NextCloudAccountAccess;
import com.openexchange.file.storage.owncloud.NextCloudFileStorageService;
import com.openexchange.file.storage.owncloud.OwnCloudAccountAccess;
import com.openexchange.file.storage.owncloud.OwnCloudEntityResolver;
import com.openexchange.file.storage.owncloud.OwnCloudFileStorageService;
import com.openexchange.file.storage.owncloud.internal.permissions.SimpleOwnCloudEntityResolver;
import com.openexchange.java.Strings;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.rest.client.httpclient.DefaultHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.SpecificHttpClientConfigProvider;

/**
 * {@link OwnCloudActivator}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class OwnCloudActivator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(OwnCloudActivator.class);
    private static final Property SIMPLE_RESOLVER_ENABLED = DefaultProperty.valueOf("com.openexchange.file.storage.owncloud.permissions.resolver.simple.enabled", Boolean.FALSE);
    private static final Property SIMPLE_RESOLVER_FILE = DefaultProperty.valueOf("com.openexchange.file.storage.owncloud.permissions.resolver.simple.path", null);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { FileStorageAccountManagerLookupService.class, LeanConfigurationService.class, CapabilityService.class, HttpClientService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        track(OwnCloudEntityResolver.class);
        openTrackers();

        registerService(SpecificHttpClientConfigProvider.class, new DefaultHttpClientConfigProvider(OwnCloudAccountAccess.HTTP_CLIENT_ID, "Open-Xchange OwnCloud HTTP client"));
        registerService(SpecificHttpClientConfigProvider.class, new DefaultHttpClientConfigProvider(NextCloudAccountAccess.HTTP_CLIENT_ID, "Open-Xchange NextCloud HTTP client"));

        registerSimpleResolver();

        registerService(FileStorageService.class, new OwnCloudFileStorageService(this));
        registerService(FileStorageService.class, new NextCloudFileStorageService(this));
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
    }

    /**
     * Registers a {@link SimpleOwnCloudEntityResolver} if configured
     *
     * @throws OXException
     * @throws IOException
     */
    private void registerSimpleResolver() throws OXException, IOException {
        LeanConfigurationService config = getServiceSafe(LeanConfigurationService.class);
        if (config.getBooleanProperty(SIMPLE_RESOLVER_ENABLED) == false) {
            // Not enabled
            return;
        }

        String path = config.getProperty(SIMPLE_RESOLVER_FILE);
        if (path == null) {
            LOG.error("Missing property '%s'", SIMPLE_RESOLVER_FILE);
            return;
        }

        File file = new File(path);
        if (file.exists() == false || file.isDirectory()) {
            LOG.error("Missing property '%s'", SIMPLE_RESOLVER_FILE);
            return;
        }

        List<String> mappings = Files.readAllLines(file.toPath(), Charset.forName("UTF-8"));
        HashBiMap<String, Integer> users = HashBiMap.create(mappings.size());
        HashBiMap<String, Integer> groups = HashBiMap.create();
        mappings.forEach((line) -> {
            boolean processingGroups = false;
            String[] splits = Strings.splitBy(line, ':', true);
            if (splits.length >= 2) {
                int x = 0;
                if (splits.length == 3) {
                    processingGroups = splits[x++].contentEquals("group");
                }
                try {
                    if (processingGroups) {
                        groups.put(splits[x++], Integer.valueOf(splits[x++]));
                    } else {
                        users.put(splits[x++], Integer.valueOf(splits[x++]));
                    }
                } catch (NumberFormatException e) {
                    LOG.error("Invalid mapping: %s", e.getMessage(), e);
                }
            }
        });

        registerService(OwnCloudEntityResolver.class, new SimpleOwnCloudEntityResolver(users, groups));
    }

}
