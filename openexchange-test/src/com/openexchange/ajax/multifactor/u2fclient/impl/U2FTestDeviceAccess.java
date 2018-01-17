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

package com.openexchange.ajax.multifactor.u2fclient.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.UUID;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import com.openexchange.ajax.multifactor.u2fclient.U2FClientException;
import com.openexchange.ajax.multifactor.u2fclient.U2FDeviceAccess;
import com.openexchange.ajax.multifactor.u2fclient.U2FEncodings;

/**
 * {@link U2FTestDeviceAccess} - This is a test implementation not interacting with a real U2F device.
 * <br>
 * Instead it just  uses the demo certificate and private key from  the fido U2F spec.
 * <br>
 * See https://fidoalliance.org/specs/fido-u2f-v1.2-ps-20170411/fido-u2f-raw-message-formats-v1.2-ps-20170411.html#registration-example
 * <br>
 * <br>
 * For each authentication it just creates a new KeyPair (for testing purpose!), and stores it in a map.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.1
 */
public class U2FTestDeviceAccess implements U2FDeviceAccess {

    //Example private key data from the FIDO U2F spec.
    private static String EXAMPLE_CERT_PRIVATE_KEY_HEX =
        "f3fccc0d00d8031954f90864d43c247f4bf5f0665c6b50cc17749a27d1cf7664";

    //Example certificate data from the FIDO U2F spec.
    private static String EXAMPLE_CERT_HEX =
        "3082013c3081e4a003020102020a47901280001155957352300a06082a8648ce3d0403023017311" +
        "530130603550403130c476e756262792050696c6f74301e170d3132303831343138323933325a17" +
        "0d3133303831343138323933325a3031312f302d0603550403132650696c6f74476e756262792d3" +
        "02e342e312d34373930313238303030313135353935373335323059301306072a8648ce3d020106" +
        "082a8648ce3d030107034200048d617e65c9508e64bcc5673ac82a6799da3c1446682c258c463ff" +
        "fdf58dfd2fa3e6c378b53d795c4a4dffb4199edd7862f23abaf0203b4b8911ba0569994e101300a" +
        "06082a8648ce3d0403020347003044022060cdb6061e9c22262d1aac1d96d8c70829b2366531dda" +
        "268832cb836bcd30dfa0220631b1459f09e6330055722c8d89b7f48883b9089b88d60d1d9795902" +
        "b30410df";

    private int keySize;
    private HashMap<String, U2FKeyPair> keyStore = new HashMap<String, U2FKeyPair>();

    /**
     * Initializes a new {@link U2FTestDeviceAccess}.
     */
    public U2FTestDeviceAccess() {
       this(256);
    }

    /**
     * Initializes a new {@link U2FTestDeviceAccess}.
     *
     * @param keySize The size of the KeyPairs to create for authentication.
     */
    public U2FTestDeviceAccess(int keySize) {
        this.keySize = keySize;
    }

    private U2FKeyPair put(U2FKeyPair key) {
        String base64KeyHandle = U2FEncodings.encodeBase64Url(key.getKeyHandle());
        this.keyStore.put(base64KeyHandle, key);
        return key;
    }

    private PrivateKey parsePrivateKey(String privateKeyDataHex) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory fac = KeyFactory.getInstance("ECDSA");
        X9ECParameters curve = SECNamedCurves.getByName("secp256r1");
        ECParameterSpec curveSpec = new ECParameterSpec(curve.getCurve(), curve.getG(), curve.getN(), curve.getH());
        ECPrivateKeySpec keySpec = new ECPrivateKeySpec(new BigInteger(privateKeyDataHex, 16), curveSpec);
        return fac.generatePrivate(keySpec);
    }

    private X509Certificate parseCertificate(String vertHexData) throws CertificateException {
        byte[] certData = U2FEncodings.decodeHex(EXAMPLE_CERT_HEX);
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certData));
    }

    /**
     * Internal method to create a key handle.
     * <br>
     * <br>
     * This test method just creates a random key handle.
     * <br>
     * <br>
     * A real U2F device would create key handles in a way that
     * the key can only be used for a specific APPID
     * (https://developers.yubico.com/U2F/Protocol_details/Key_generation.html)
     *
     * @return a random key handle for testing purpose only
     */
    @SuppressWarnings("unused")
    private String createKeyHandle(String appId) {
        return UUID.randomUUID().toString();
    }


    /* (non-Javadoc)
     * @see com.openexchange.ajax.multifactor.u2fclient.U2FDeviceAccess#getCertificate()
     */
    @Override
    public AttestationCertificate getCertificate() throws U2FClientException {
        try {
            return new AttestationCertificate(parsePrivateKey(EXAMPLE_CERT_PRIVATE_KEY_HEX),
                parseCertificate(EXAMPLE_CERT_HEX));
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new U2FClientException(e.getMessage(), e);
        }
    }

    @Override
    public U2FKeyPair getKeyPair(String appId, String challenge) throws U2FClientException {
        //Appid and challenge could be used to create/derive the private key instead of createing a new one
        try {
            KeyPairGenerator keyPairGenerator;
            keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
            keyPairGenerator.initialize(keySize);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            return put(new U2FKeyPair(createKeyHandle(appId).getBytes(), keyPair));
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new U2FClientException("unable to create keypair", e);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.ajax.multifactor.u2fclient.U2FKeyFactory#encodePublicKey(com.openexchange.ajax.multifactor.u2fclient.U2FKeyFactory.U2FKeyPair)
     */
    @Override
    public byte[] encodePublicKey(U2FKeyPair key) throws U2FClientException {
        BCECPublicKey publicKey = (BCECPublicKey) key.getKeyPair().getPublic();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(65);

        try {
            bos.write(0x04);
            //bos.write(publicKey.getQ().getX().getEncoded());
            //bos.write(publicKey.getQ().getY().getEncoded());           
            bos.write(publicKey.getQ().getXCoord().getEncoded());
            bos.write(publicKey.getQ().getYCoord().getEncoded());           
        } catch (IOException e) {
            throw new U2FClientException(e.getMessage(), e);
        }
        return bos.toByteArray();
    }

    /* (non-Javadoc)
     * @see com.openexchange.ajax.multifactor.u2fclient.U2FKeyFactory#get(byte[])
     */
    @Override
    public U2FKeyPair getKeyPair(byte[] keyHandle) {
        String base64KeyHandle = U2FEncodings.encodeBase64Url(keyHandle);
         U2FKeyPair key = this.keyStore.get(base64KeyHandle);
         key.incrementCounter();
         return key;
    }
}
