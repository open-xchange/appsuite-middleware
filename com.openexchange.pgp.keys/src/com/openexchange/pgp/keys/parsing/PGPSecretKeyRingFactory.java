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

package com.openexchange.pgp.keys.parsing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.slf4j.Logger;

/**
 * {@link PGPSecretKeyRingFactory} is a simple factory for creating instances of PGPSecretKeyRing.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.3
 */
public class PGPSecretKeyRingFactory {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PGPSecretKeyRingFactory.class);
    }

    public static PGPSecretKeyRing create(String keyData) throws  IOException {
        return create(new ByteArrayInputStream(keyData.getBytes("UTF-8")));
    }

    /**
     * Creates a PGPSecretKeyRing object from the given ASCII-Armored data.
     *
     * @param asciiKeyData The ASCII-armored data to create the key from.
     * @return The secret key ring created from the given data
     * @throws IOException
     * @throws IllegalArgumentException if asciiKeyData does not contain a valid ASCII-armored key
     */
    public static PGPSecretKeyRing create(InputStream keyData) throws IOException {
        PGPObjectFactory factory = new PGPObjectFactory(PGPUtil.getDecoderStream(keyData), new BcKeyFingerprintCalculator());
        Object o = factory.nextObject();
        if (o instanceof PGPSecretKeyRing) {
            return (PGPSecretKeyRing) o;
        }
        
        LoggerHolder.LOG.error("Input text does not contain a PGP Secret Key");
        return null;
    }

    /**
     * Creates a set of PGPSecretKeyRing objects from a parsing result
     * @param parserResult the result to create the keys from
     * @return a list of private key rings for the parsed keys data
     * @throws IOException
     * @throws IllegalArgumentException if some data from the parserResult does not contain a valid ASCII-armored key
     */
    public static List<PGPSecretKeyRing> create(KeyRingParserResult parserResult) throws IOException {
        List<PGPSecretKeyRing> ret = new ArrayList<PGPSecretKeyRing>();
        for(String secretKeyData : parserResult.getSecretKeysData()) {
            PGPSecretKeyRing parsedKey = create(secretKeyData);
            if (parsedKey != null) {
                ret.add(parsedKey);
            }
        }
        return ret;
    }
}
