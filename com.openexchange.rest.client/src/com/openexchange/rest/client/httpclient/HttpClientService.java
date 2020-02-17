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
import com.openexchange.exception.OXException;
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
     * Get the {@link CloseableHttpClient} for the provided identifier
     * <p>
     * The HTTP client obtained by this service will be either
     * <li> Received from a cache holding the HTTP client instance</li>
     * <li> Created by this service</li>
     * 
     * The client will be created from a configuration which is provided by registered service of classes
     * <li> {@link SpecificHttpClientConfigProvider}</li>
     * <li> {@link WildcardHttpClientConfigProvider}</li>
     * Created clients will be put into the cache.
     * <p>
     * The client will be closed and removed if the corresponding service of above classes is removed via
     * OSGi. This will unset the HTTP client reference in the returned {@link ManagedHttpClient}, too.
     * <p>
     * Further the client will be removed if a caller explicit closes the HTTP client reference in the
     * returned {@link ManagedHttpClient}. The service will then auto-clean the reference from the cache,
     * efficiently avoiding to return a HTTP client which has been closed.
     * <p>
     * Cached HTTP clients will be renewed if there are configuration changes after a reload configuration.
     * Therefore the HTTP client wrapped in the managed object will be replaces with a new instance. The old
     * instance will be closed after a short period of time, so that all operations running on the old client
     * can successfully finish, before the client is closed. 
     * <p>
     * To make sure a HTTP client can be obtained via this service, add a provider like e.g.
     * <br>
     * <code>
     * registerService(HttpClientConfigProvider.class, new DefaultHttpClientConfigProvider("MyClient", "MyClient User Agent"));
     * </code>
     *
     * @param httpClientId The identifier of named HTTP client to obtain
     * @return The {@link ManagedHttpClient} from which a {@link CloseableHttpClient} can be obtained.
     *         It is strongly recommended to fetch the client each time from the service instead of using it as
     *         class member because this service ensures that the underlying HTTP client can be used.
     * @throws OXException If no HTTP client with the provided identifier exists or client configuration is invalid
     * @see {@link ManagedHttpClient#getHttpClient()}
     */
    ManagedHttpClient getHttpClient(String httpClientId) throws OXException;

}
