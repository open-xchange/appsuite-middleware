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

package com.openexchange.pgp.keys.parsing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(PGPSecretKeyRingFactory.class);

    /**
     * Creates a PGPSecretKeyRing object from the given ASCII-Armored data.
     *
     * @param asciiKeyData The ASCII-armored data to create the key from.
     * @return The secret key ring created from the given data
     * @throws IOException
     * @throws IllegalArgumentException if asciiKeyData does not contain a valid ASCII-armored key
     */
    public static PGPSecretKeyRing create(String asciiKeyData) throws IOException {
        PGPObjectFactory factory = new PGPObjectFactory(PGPUtil.getDecoderStream(new ByteArrayInputStream(asciiKeyData.getBytes("UTF-8"))), new BcKeyFingerprintCalculator());
        Object o = factory.nextObject();
        if (o instanceof PGPSecretKeyRing) {
            return (PGPSecretKeyRing) o;
        }
        else {
            logger.error("Input text does not contain a PGP Secret Key");
            return null;
        }
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
            if(parsedKey != null) {
                ret.add(parsedKey);
            }
        }
        return ret;
    }
}
