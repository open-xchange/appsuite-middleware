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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.operator.PGPDataDecryptor;
import org.bouncycastle.openpgp.operator.PublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;

/**
 * {@link SymmetricKeyDataDecryptorFactory} is a factory which decrypts PGP data with a known symmetric session key.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.0
 */
public class SymmetricKeyDataDecryptorFactory implements PublicKeyDataDecryptorFactory {

    private static final PGPPrivateKey          NULL_KEY = null;
    private final byte[]                        sessionKey;
    private final PublicKeyDataDecryptorFactory delegate;

    /**
     * Initializes a new {@link SymmetricKeyDataDecryptorFactory} using the Bouncy Castle ("BC") JCE provider.
     *
     * @param sessionKey The symmetric session key for decrypting the PGP data.
     */
    public SymmetricKeyDataDecryptorFactory(byte[] sessionKey) {
        this(BouncyCastleProvider.PROVIDER_NAME, sessionKey);
    }

    /**
     * Initializes a new {@link SymmetricKeyDataDecryptorFactory}.
     *
     * @param providerName The name of the JCE provider to use
     * @param sessionKey The symmetric session key for decrypting the PGP data
     *
     */
    public SymmetricKeyDataDecryptorFactory(String providerName, byte[] sessionKey) {
        super();
        this.sessionKey = sessionKey;
        this.delegate = new JcePublicKeyDataDecryptorFactoryBuilder().setProvider(providerName).build(NULL_KEY);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.bouncycastle.openpgp.operator.PGPDataDecryptorFactory#createDataDecryptor(boolean, int, byte[])
     */
    @Override
    public PGPDataDecryptor createDataDecryptor(boolean withIntegrityPacket, int encAlgorithm, byte[] key) throws PGPException {
        return delegate.createDataDecryptor(withIntegrityPacket, encAlgorithm, key);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.bouncycastle.openpgp.operator.PublicKeyDataDecryptorFactory#recoverSessionData(int, byte[][])
     */
    @Override
    public byte[] recoverSessionData(int keyAlgorithm, byte[][] secKeyData) throws PGPException {
        return this.sessionKey;
    }

}
