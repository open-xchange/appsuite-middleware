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

package com.openexchange.drive.events.apn2.internal;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.events.apn2.ApnsHttp2Options;
import com.openexchange.drive.events.apn2.ApnsHttp2Options.AuthType;
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

}
