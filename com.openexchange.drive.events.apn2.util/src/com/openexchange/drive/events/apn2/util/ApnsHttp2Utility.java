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

package com.openexchange.drive.events.apn2.util;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.events.apn2.util.ApnsHttp2Options.AuthType;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.auth.ApnsSigningKey;

/**
 * {@link ApnsHttp2Utility} - APNS HTTP/2 utility class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class ApnsHttp2Utility {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ApnsHttp2Utility.class);

    /**
     * Initializes a new {@link ApnsHttp2Utility}.
     */
    private ApnsHttp2Utility() {
        super();
    }

    private static ConcurrentMap<ApnsHttp2Options, ApnsClient> CLIENTS = new ConcurrentHashMap<ApnsHttp2Options, ApnsClient>(4, 0.9F, 1);

    /**
     * Gets the appropriate APNS HTTP/2 client for specified options instance.
     *
     * @param options The options instance to create the client for
     * @return The APNS HTTP/2 client
     * @throws OXException If creation of APNS HTTP/2 client fails
     */
    public static ApnsClient getApnsClient(ApnsHttp2Options options) throws OXException {
        ApnsClient client = CLIENTS.get(options);
        if (null == client) {
            ApnsClient newClient = createNewApnsClient(options);
            client = CLIENTS.putIfAbsent(options, newClient);
            if (null == client) {
                client = newClient;
            }
        }
        return client;
    }

    /**
     * Creates a new APNS HTTP/2 client for specified options instance.
     *
     * @param options The options instance to create the client for
     * @return The newly created APNS HTTP/2 client
     * @throws OXException If creation of APNS HTTP/2 client fails
     */
    public static ApnsClient createNewApnsClient(ApnsHttp2Options options) throws OXException {
        try {
            ApnsClientBuilder clientBuilder;
            if (AuthType.CERTIFICATE == options.getAuthType()) {
                if (null != options.getKeystore()) {
                    clientBuilder = new ApnsClientBuilder().setApnsServer(options.isProduction() ? ApnsClientBuilder.PRODUCTION_APNS_HOST : ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                        .setClientCredentials(options.getKeystore(), options.getPassword());
                } else {
                    clientBuilder = new ApnsClientBuilder().setApnsServer(options.isProduction() ? ApnsClientBuilder.PRODUCTION_APNS_HOST : ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                        .setClientCredentials(new ByteArrayInputStream(options.getKeystoreBytes()), options.getPassword());
                }
            } else {
                clientBuilder = new ApnsClientBuilder()
                    .setApnsServer(options.isProduction() ? ApnsClientBuilder.PRODUCTION_APNS_HOST : ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                    .setSigningKey(ApnsSigningKey.loadFromInputStream(Streams.newByteArrayInputStream(options.getPrivateKey()), options.getTeamId(), options.getKeyId()));
            }
            return clientBuilder.build();
        } catch (FileNotFoundException e) {
            throw DriveExceptionCodes.UNEXPECTED_ERROR.create(e, "No such APNS HTTP/2 keystore file for drive events: " + options.getKeystore());
        } catch (NoSuchAlgorithmException e) {
            throw DriveExceptionCodes.UNEXPECTED_ERROR.create(e, "The algorithm used to check the integrity of the APNS HTTP/2 keystore for drive events cannot be found");
        } catch (IOException e) {
            throw DriveExceptionCodes.UNEXPECTED_ERROR.create(e, "There is an I/O or format problem with the APNS HTTP/2 keystore data of the keystore for drive events or specified password is invalid");
        } catch (InvalidKeyException e) {
            throw DriveExceptionCodes.UNEXPECTED_ERROR.create(e, "Invalid APNS HTTP/2 private key specified for drive events");
        }
    }

    /**
     * Gets the APNS HTTP/2 client for specified options if it is available.
     *
     * @param options The APNS HTTP/2 options
     * @return The optional {@link ApnsClient}
     */
    public static Optional<ApnsClient> getClient(ApnsHttp2Options options) {
        try {
            return options == null ? Optional.empty() : Optional.of(ApnsHttp2Utility.getApnsClient(options));
        } catch (Exception e) {
            LOG.error("Unable to create APNS HTTP/2 client for service {}", "apn/apn2", e);
        }
        return Optional.empty();
    }

}
