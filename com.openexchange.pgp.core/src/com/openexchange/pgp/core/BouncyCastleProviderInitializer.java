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

package com.openexchange.pgp.core;

import static com.openexchange.java.Autoboxing.I;
import java.security.Security;
import javax.crypto.Cipher;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes the Bouncy Castle provider
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class BouncyCastleProviderInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(BouncyCastleProviderInitializer.class);

    /**
     * Adds the Bouncy Castle Provider ({@link BouncyCastleProvider}).
     *
     * @throws Exception
     */
    public static void initialize() throws Exception {
        try {
            //Registering bouncy castle provider
            LOG.debug("Registering Bouncy Castle JCE Provider...");
            Security.addProvider(new BouncyCastleProvider());
            LOG.debug("Bouncy Castle JCE Provider registered successfull.");
        } catch (Exception e) {
            LOG.error("Error while registering Bouncy Castle JCE Provider", e);
            throw e;
        }

        //Checking allowed key length for AES
        int maxAllowedKeyLength = Cipher.getMaxAllowedKeyLength("AES");
        if (maxAllowedKeyLength < 256) {
            LOG.warn("The JAVA security package installed only allows AES key length {}. Key lengths < 256 will often cause PGP to fail", I(maxAllowedKeyLength));
        }
    }
}
