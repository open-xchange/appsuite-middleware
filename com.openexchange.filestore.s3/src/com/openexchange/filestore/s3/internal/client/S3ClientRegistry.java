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

package com.openexchange.filestore.s3.internal.client;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.s3.internal.S3FileStorage;
import com.openexchange.filestore.s3.internal.config.S3ClientConfig;
import com.openexchange.filestore.s3.internal.config.S3ClientProperty;
import com.openexchange.filestore.s3.internal.config.S3Property;
import com.openexchange.server.ServiceLookup;

/**
 * {@link S3ClientRegistry}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class S3ClientRegistry implements Reloadable {

    private static class LoggerHolder {
        static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(S3ClientRegistry.class);
    }

    private final S3ClientFactory clientFactory;
    private final ServiceLookup services;
    private final Cache<String, S3FileStorageClient> clients;

    /**
     * Initializes a new {@link S3ClientRegistry}.
     *
     * @param clientFactory The client factory
     * @param services The {@link ServiceLookup} containing a {@link LeanConfigurationService}
     */
    public S3ClientRegistry(S3ClientFactory clientFactory, ServiceLookup services) {
        super();
        this.clientFactory = clientFactory;
        this.services = services;
        clients = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
    }

    /**
     * Get an {@link S3FileStorageClient} for the given client configuration. If none exists yet a new instance is created
     * and put into the registry. Instances are weakly referenced by the registry and vanish after the last strong reference
     * to them was garbage collected. Make sure to include the returned instance as member to the respective {@link S3FileStorage}
     * instance.
     *
     * @param clientConfig The client configuration
     * @return The already existing or new {@link S3FilestoreClient}
     * @throws OXException If S3 storage client cannot be created
     */
    public S3FileStorageClient getOrCreate(S3ClientConfig clientConfig) throws OXException {
        String key = clientConfig.getClientID().orElse(clientConfig.getFilestoreID());
        S3FileStorageClient client = clients.getIfPresent(key);
        if (client == null) {
            try {
                client = clients.get(key, createLoaderFor(clientConfig));
            } catch (ExecutionException | UncheckedExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof OXException) {
                    throw (OXException) cause;
                }
                throw OXException.general("Failed initializing S3 client.", cause);
            }
        }
        return client;
    }

    private Callable<? extends S3FileStorageClient> createLoaderFor(S3ClientConfig clientConfig) {
        return new S3ClientLoaderCallable(clientConfig, this.clientFactory);
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().propertiesOfInterest(
            S3ClientProperty.getInterestsWildcard(),
            S3Property.getInterestsWildcard())
            .build();
    }

    @Override
    public void reloadConfiguration(ConfigurationService _unused) {
        try {
            LeanConfigurationService configService = services.getServiceSafe(LeanConfigurationService.class);
            List<S3FileStorageClient> toRemove = new LinkedList<>();
            ConcurrentMap<String, S3FileStorageClient> clientsMap = clients.asMap();
            for (Map.Entry<String, S3FileStorageClient> entry : clientsMap.entrySet()) {
                S3FileStorageClient client = entry.getValue();
                int recentFingerprint = S3ClientConfig.getFingerprint(configService, client.getScope(), entry.getKey());
                if (recentFingerprint != client.getConfigFingerprint()) {
                    toRemove.add(client);
                }
            }

            for (S3FileStorageClient client : toRemove) {
                clientsMap.remove(client.getKey(), client);
            }
        } catch (Exception e) {
            LoggerHolder.LOG.error("Configuration reload failed for S3 clients", e);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class S3ClientLoaderCallable implements Callable<S3FileStorageClient> {

        private final S3ClientConfig clientConfig;
        private final S3ClientFactory clientFactory;

        S3ClientLoaderCallable(S3ClientConfig clientConfig, S3ClientFactory clientFactory) {
            this.clientConfig = clientConfig;
            this.clientFactory = clientFactory;
        }

        @Override
        public S3FileStorageClient call() throws Exception {
            return clientFactory.initS3Client(clientConfig);
        }
    }

}
