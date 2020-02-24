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
 *    trademarks of the OX Software GmbH. group of companies.
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
