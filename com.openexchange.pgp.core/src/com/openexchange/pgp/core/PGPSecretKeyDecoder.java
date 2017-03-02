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

package com.openexchange.pgp.core;

import java.util.Iterator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;

/**
 * {@link PGPSecretKeyDecoder} - Obtains the raw PGPPrivateKey from a given, password protected, PGPSecretKey.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class PGPSecretKeyDecoder {

    /**
     * Decodes the {@link PGPPrivateKey} from the given {@link PGPSecretKey} using a password.
     *
     * @param secretKey The secret key to decode the private key from
     * @param password The password
     * @return The decoded private key
     * @throws PGPException
     */
    public static PGPPrivateKey decodePrivateKey(PGPSecretKey secretKey, char[] password) throws PGPException {
        PBESecretKeyDecryptor extractor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(password);
        return secretKey.extractPrivateKey(extractor);
    }

    /**
     * Decodes a {@link PGPPrivateKey} from the given {@link PGPSecretKeyRing}.
     *
     * It is preferred to not return the master key. Only return master if no other keys found.
     *
     * @param PGPSecretKeyRing The secret key ring to decode a private key from.
     * @param password The password
     * @return The decoded private key
     * @throws PGPException
     */
    public static PGPPrivateKey decodePrivateKey(PGPSecretKeyRing secretKeyRing, char[] password) throws PGPException {
        PGPSecretKey sec_key = null;
        Iterator<PGPSecretKey> it = secretKeyRing.getSecretKeys();
        PGPSecretKey master = null;
        while (sec_key == null && it.hasNext()) {
            PGPSecretKey key = it.next();
            if (!key.isMasterKey()) { // We prefer to not return the master.  Only return master if no other encr keys found
                return decodePrivateKey(key, password);
            } else {
                master = key;
            }
        }
        if (master != null) {
            return decodePrivateKey(master, password);
        }
        return null;
    }
}
