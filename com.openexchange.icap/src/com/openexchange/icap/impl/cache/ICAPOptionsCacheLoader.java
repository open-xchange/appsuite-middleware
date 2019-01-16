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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

    /*
     * (non-Javadoc)
     * 
     * @see com.google.common.cache.CacheLoader#reload(java.lang.Object, java.lang.Object)
     */
    @Override
    public ListenableFuture<ICAPOptions> reload(GenericICAPCacheKey key, ICAPOptions oldValue) throws Exception {
        if (null != oldValue && false == isExpired(oldValue)) {
            return Futures.immediateFuture(oldValue);
        }
        return super.reload(key, oldValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.common.cache.CacheLoader#load(java.lang.Object)
     */
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
            } catch (IllegalArgumentException e) {
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
