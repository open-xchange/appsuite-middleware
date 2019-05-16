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

package com.openexchange.icap;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.icap.conf.ICAPClientProperty;
import com.openexchange.icap.impl.cache.GenericICAPCacheKey;
import com.openexchange.icap.impl.cache.ICAPOptionsCacheLoader;
import com.openexchange.icap.impl.request.handler.ICAPRequestHandler;
import com.openexchange.icap.impl.request.handler.OptionsICAPRequestHandler;
import com.openexchange.icap.impl.request.handler.RequestModificationICAPRequestHandler;
import com.openexchange.icap.impl.request.handler.ResponseModificationICAPRequestHandler;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ICAPClient}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ICAPClient {

    /**
     * <p>Cache for the ICAP options for different ICAP servers.</p>
     * 
     * <p>Since each ICAP server can define a different amount of TTL for its options
     * and Guava does not support self-expiring entries with different TTLs,
     * we set the expiration time to the lowest possible value (i.e. 1 microsecond)
     * and we check for the expiration ourselves.</p>
     * 
     * @see ICAPOptionsCacheLoader#reload(GenericICAPCacheKey, ICAPOptions)
     */
    private final LoadingCache<GenericICAPCacheKey, ICAPOptions> optionsCache;

    /**
     * Defines all available {@link ICAPRequestHandler}s
     */
    private final ImmutableMap<ICAPMethod, ICAPRequestHandler> requestHandlers;

    private final ServiceLookup services;

    /**
     * Initialises a new {@link ICAPClient}.
     */
    public ICAPClient(ServiceLookup services) {
        super();
        this.services = services;
        this.optionsCache = CacheBuilder.newBuilder().initialCapacity(10).maximumSize(100).refreshAfterWrite(1, TimeUnit.MICROSECONDS).build(new ICAPOptionsCacheLoader(this));
        //@formatter:off
        requestHandlers = new ImmutableMap.Builder<ICAPMethod, ICAPRequestHandler>()
            .put(ICAPMethod.OPTIONS, new OptionsICAPRequestHandler())
            .put(ICAPMethod.RESPMOD, new ResponseModificationICAPRequestHandler())
            .put(ICAPMethod.REQMOD, new RequestModificationICAPRequestHandler())
            .build();
        //@formatter:on
    }

    /**
     * Performs an <code>OPTIONS</code> request to the specified server and
     * fetches the supported methods, preview size, ISTag and whether an ALLOW
     * header is useful for the server. The response is cached locally either
     * Indefinitely (i.e. if there is no TTL header present in the OPTIONS response)
     * or for the amount of time specified by the TTL response header.
     * 
     * @param server the ICAP server address
     * @param port The ICAP server's listening port
     * @param service The service for which to request the OPTIONS
     * @return The {@link ICAPOptions}
     * @throws ExecutionException if an error is occurred
     */
    public ICAPOptions getOptions(String server, int port, String service) throws ExecutionException {
        return getOptions(server, port, service, false);
    }

    /**
     * <p>
     * Performs an <code>OPTIONS</code> request to the specified server and
     * fetches the supported methods, preview size, ISTag and whether an ALLOW
     * header is useful for the server. The response is cached locally either
     * Indefinitely (i.e. if there is no TTL header present in the OPTIONS response)
     * or for the amount of time specified by the TTL response header.
     * </p>
     * <p>
     * If the <code>refresh</code> flag is enabled, then the local cached copy is
     * invalidated and a new one is fetched from the server (and cached).
     * </p>
     * 
     * @param refresh Whether to refresh the cached copy
     * @return The {@link ICAPOptions}
     * @throws ExecutionException if an error is occurred
     */
    public ICAPOptions getOptions(String server, int port, String service, boolean refresh) throws ExecutionException {
        if (refresh) {
            optionsCache.invalidate(new GenericICAPCacheKey(server, port, service));
        }
        return optionsCache.get(new GenericICAPCacheKey(server, port, service));
    }

    /**
     * Executes the specified {@link ICAPRequest}
     * 
     * @param request the request to execute
     * @return The response
     * @throws UnknownHostException if the IP address of the host of the ICAP server
     *             could not be determined
     * @throws IOException if an I/O error is occurred
     */
    public ICAPResponse execute(ICAPRequest request) throws IOException {
        ICAPRequestHandler requestHandler = requestHandlers.get(request.getMethod());
        if (requestHandler == null) {
            throw new IllegalArgumentException("No handler found for handling the '" + request.getMethod() + "' method.");
        }
        Socket socket = createSocket(request.getServer(), request.getPort());
        try {
            return requestHandler.handle(request, socket);
        } finally {
            if (request.getOperationMode().equals(OperationMode.DOUBLE_FETCH)) {
                // - Note that we close the socket right after execution only 
                //   when in 'double-fetch' operation mode. 
                // - For the 'streaming' operation mode the socket has to remain open
                //   for streaming the content back to the end-point-client.
                //   The socket will the be closed once the stream is closed.
                //   See ICAPInputStream.
                IOUtils.closeQuietly(socket);
            }
        }
    }

    /////////////////////// HELPERS ////////////////////////

    /**
     * Creates a {@link Socket} for the specified hostname and port
     * 
     * @param hostname The hostname
     * @param port the port
     * @return The newly created {@link Socket}
     * @throws UnknownHostException if the IP address of the host of the ICAP server
     *             could not be determined
     * @throws IOException if an I/O error is occurred
     */
    private Socket createSocket(String hostname, int port) throws IOException {
        Socket socket = new Socket(hostname, port);
        socket.setSoTimeout(getSocketTimeout());
        return socket;
    }

    /**
     * Retrieves the configured socket time out value from the {@link LeanConfigurationService}
     * 
     * @return The default socket timeout value
     */
    private int getSocketTimeout() {
        LeanConfigurationService leanConfigService = services.getOptionalService(LeanConfigurationService.class);
        if (leanConfigService == null) {
            return ICAPClientProperty.SOCKET_TIMEOUT.getDefaultValue(Integer.class).intValue();
        }
        return leanConfigService.getIntProperty(ICAPClientProperty.SOCKET_TIMEOUT);
    }
}
