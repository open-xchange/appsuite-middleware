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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPBEEncryptedData;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBEDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;

/**
 * {@link PGPSymmetricDecrypter} offers PGP based, symmetric decryption
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class PGPSymmetricDecrypter {

    private static final int BUFFERSIZE = 256;

    /**
     * Decrypts, pgp based, symmetric encrypted data
     *
     * @param input The data to decrypt
     * @param output The output stream to write the decrypted data to
     * @param key The secret symmetric key required for decryption
     * @throws IOException
     * @throws PGPException
     */
    public void decrypt(InputStream input, OutputStream output, char[] key) throws IOException, PGPException {
        try (InputStream decoderStream = PGPUtil.getDecoderStream(input)) {
            PGPObjectFactory objectFact = new PGPObjectFactory(decoderStream, new BcKeyFingerprintCalculator());
            Object pgpObject = objectFact.nextObject();
            if (pgpObject instanceof PGPEncryptedDataList) {
                Object nextObj = ((PGPEncryptedDataList) pgpObject).get(0);
                if (nextObj instanceof PGPPBEEncryptedData) {
                    PGPPBEEncryptedData encrypted = (PGPPBEEncryptedData) nextObj;
                    InputStream decrypted = encrypted
                        .getDataStream(new BcPBEDataDecryptorFactory(key, new BcPGPDigestCalculatorProvider()));
                    PGPObjectFactory decrFactor = new PGPObjectFactory(decrypted, new BcKeyFingerprintCalculator());
                    Object decrKey = decrFactor.nextObject();
                    // Check for compressed data
                    if (decrKey instanceof PGPCompressedData) {
                        PGPCompressedData compressedData = (PGPCompressedData) decrKey;
                        objectFact = new PGPObjectFactory(compressedData.getDataStream(), new BcKeyFingerprintCalculator());
                        decrKey = objectFact.nextObject();
                    }
                    // Handle literal data
                    if (decrKey instanceof PGPLiteralData) {
                        PGPLiteralData decrData = (PGPLiteralData) decrKey;
                        byte[] buffer = new byte[BUFFERSIZE];
                        int len = 0;
                        while ((len = decrData.getDataStream().read(buffer)) > -1) {
                            output.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }
}
