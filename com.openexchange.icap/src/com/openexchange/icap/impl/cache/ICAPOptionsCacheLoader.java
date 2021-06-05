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

package com.openexchange.icap.impl.cache;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.openexchange.icap.ICAPClient;
import com.openexchange.icap.ICAPMethod;
import com.openexchange.icap.ICAPOptions;
import com.openexchange.icap.ICAPRequest;
import com.openexchange.icap.ICAPResponse;
import com.openexchange.icap.header.OptionsICAPResponseHeader;
import com.openexchange.java.Strings;

/**
 * {@link ICAPOptionsCacheLoader}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ICAPOptionsCacheLoader extends CacheLoader<GenericICAPCacheKey, ICAPOptions> {

    private static final Logger LOG = LoggerFactory.getLogger(ICAPOptionsCacheLoader.class);

    private final ICAPClient client;

    /**
     * Initialises a new {@link ICAPOptionsCacheLoader}.
     * 
     * @param client The {@link ICAPClient}
     */
    public ICAPOptionsCacheLoader(ICAPClient client) {
        super();
        this.client = client;
    }

    @Override
    public ListenableFuture<ICAPOptions> reload(GenericICAPCacheKey key, ICAPOptions oldValue) throws Exception {
        if (null != oldValue && false == isExpired(oldValue)) {
            return Futures.immediateFuture(oldValue);
        }
        return super.reload(key, oldValue);
    }

    @Override
    public ICAPOptions load(GenericICAPCacheKey key) throws Exception {
        ICAPResponse response = client.execute(new ICAPRequest.Builder().withServer(key.getServer()).withPort(key.getPort()).withService(key.getService()).build());

        String previewStr = response.getHeader(OptionsICAPResponseHeader.PREVIEW);
        String ttlStr = response.getHeader(OptionsICAPResponseHeader.OPTIONS_TTL);

        ICAPOptions.Builder optionsBuilder = new ICAPOptions.Builder();
        optionsBuilder.withSupportedMethods(getSupportedMethods(response));
        optionsBuilder.withAllow(Strings.isNotEmpty(response.getHeader(OptionsICAPResponseHeader.ALLOW)));
        optionsBuilder.withISTag(response.getHeader(OptionsICAPResponseHeader.ISTAG));
        optionsBuilder.withPreviewSize(Strings.isEmpty(previewStr) ? -1 : Long.parseLong(previewStr));
        optionsBuilder.withTTL(Strings.isEmpty(ttlStr) ? -1 : Long.parseLong(ttlStr));
        optionsBuilder.withService(OptionsICAPResponseHeader.SERVICE);
        optionsBuilder.withServiceId(OptionsICAPResponseHeader.SERVICE_ID);

        return optionsBuilder.build();
    }

    /**
     * Performs an <code>OPTIONS</code> request to the specified server and
     * fetches the supported methods.
     * 
     * @return An unmodifiable {@link Set} with all supported {@link ICAPMethod}s
     */
    private Set<ICAPMethod> getSupportedMethods(ICAPResponse response) {
        String methods = response.getHeader(OptionsICAPResponseHeader.METHODS);
        String[] split = Strings.splitByComma(methods);
        if (split == null) {
            return Collections.emptySet();
        }

        Set<ICAPMethod> methodSet = new HashSet<>(2);
        for (String s : split) {
            try {
                methodSet.add(ICAPMethod.valueOf(s));
            } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
                LOG.debug("Unsupported method detected '{}'. Ignoring", s);
                continue;
            }
        }
        return methodSet;
    }

    /**
     * Checks whether the specified {@link ICAPOptions} properties are due for renewal
     * 
     * @param options The {@link ICAPOptions} to check
     * @return <code>true</code> if the TTL is expired and is due to renewal; <code>false</code> otherwise
     */
    private boolean isExpired(ICAPOptions options) {
        return (System.currentTimeMillis() - options.getCreated()) / 1000 >= options.getTTL();
    }
}
