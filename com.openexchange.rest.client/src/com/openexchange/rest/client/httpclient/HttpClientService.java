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

package com.openexchange.rest.client.httpclient;

import org.apache.http.impl.client.CloseableHttpClient;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link HttpClientService} - OSGi service that manages HTTP clients.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
@SingletonService
public interface HttpClientService {

    /**
     * Get the {@link ManagedHttpClient} for the provided identifier
     * <p>
     * The HTTP client obtained by this service will be either
     * <li> Received from a cache holding the HTTP client instance</li>
     * <li> Created by this service</li>
     *
     * The client will be created from a configuration which is provided by registered service of classes
     * <li> {@link SpecificHttpClientConfigProvider}</li>
     * <li> {@link WildcardHttpClientConfigProvider}</li>
     * If none of these providers is registered for the given client identifier, default configuration
     * is applied.
     * Created clients will be put into the cache.
     * <p>
     * The client will be closed and removed if the corresponding service of above classes is removed via
     * OSGi. This will unset the HTTP client reference in the returned {@link ManagedHttpClient}, too.
     * <p>
     * Cached HTTP clients will be renewed if there are configuration changes after a reload configuration.
     * Therefore the HTTP client wrapped in the managed object will be replaces with a new instance. The old
     * instance will be closed after a short period of time, so that all operations running on the old client
     * can successfully finish, before the client is closed.
     * <p>
     * A provider for contributing configuration for a special
     * <br>
     * <code>
     * registerService(HttpClientConfigProvider.class, new DefaultHttpClientConfigProvider("MyClient", "MyClient User Agent"));
     * </code>
     *
     * @param httpClientId The identifier of named HTTP client to obtain
     * @return The {@link ManagedHttpClient} from which a {@link CloseableHttpClient} can be obtained.
     *         It is strongly recommended to fetch the client each time from the service instead of using it as
     *         class member because this service ensures that the underlying HTTP client can be used.
     * @throws IllegalArgumentException In case the given identifier is empty or creating of the HTTP client fails
     * @throws IllegalStateException In case service is shutting down, an unexpected error occurred during acquisition of the HTTP client
     * @see {@link ManagedHttpClient#getHttpClient()}
     */
    ManagedHttpClient getHttpClient(String httpClientId);

    /**
     * Removes cached client instances and closes it. This method is supposed to be called, when
     * no HTTP client instance with the given ID is needed anymore. Not needed means <b>either at all</b>
     * or for a <b>long (minutes and more) time</b>.
     * <p>
     * <strong>IMPORTANT: Do not call this regularly after every request performed!</strong>
     * <p>
     * For example bundle shutdown would be a good occasion to call this. However if you registered a
     * <li> {@link SpecificHttpClientConfigProvider}</li>
     * or
     * <li> {@link WildcardHttpClientConfigProvider}</li>
     * this method must not be called. The clients will be removed when the services is removed
     *
     * @param httpClientId The identifier of named HTTP client to close and remove from cache
     * @throws IllegalArgumentException In case the given identifier is empty
     */
    void destroyHttpClient(String httpClientId);

}
