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

package com.openexchange.pgp.keys.tools;

import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.bcpg.sig.RevocationReasonTags;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPSignatureSubpacketVector;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

/**
 * {@link PGPUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 */
public final class PGPKeysUtil {

    /**
     * Convert byte array fingerprint into standard String format with spacing
     *
     * @param fingerprint
     * @return
     */
    public static String getFingerPrintInBlocks(byte[] fingerprint) {
        StringBuffer fpstring = new StringBuffer();
        for (int i = 0; i < fingerprint.length; ++i) {
            String hex = Integer.toHexString((char) fingerprint[i]);
            hex = hex.toUpperCase();
            while (hex.length() < 2) {
                hex = '0' + hex;
            }
            if (hex.length() > 2) {
                hex = hex.substring(hex.length() - 2);
            }
            fpstring.append(hex);
            if (i % 2 == 1) {
                fpstring.append(" ");
            }
        }
        return fpstring.toString().trim();
    }

    /**
     * Converts byte array fingerprint into standard HEX String format
     *
     * @param fingerprint The fingerprint to convert
     * @return The HEX fingerprint as String
     */
    public static String getFingerPrint(byte[] fingerprint) {
        StringBuffer fpstring = new StringBuffer();
        for (int i = 0; i < fingerprint.length; ++i) {
            String hex = Integer.toHexString((char) fingerprint[i]).toUpperCase();
            while (hex.length() < 2) {
                hex = '0' + hex;
            }
            if (hex.length() > 2) {
                hex = hex.substring(hex.length() - 2);
            }
            fpstring.append(hex);
        }
        return fpstring.toString().trim();
    }

    public static int getHashAlgorithmTags() {
        return HashAlgorithmTags.SHA256;
    }



    /**
     * Check if a public key is expired
     *
     * @param key
     * @return
     */
    public static boolean isExpired(PGPPublicKey key) {
        if (key == null) {
            return false;
        }
        if (key.getValidSeconds() == 0) {
            return false;
        }
        Date now = new Date();
        return key.getCreationTime().getTime() + (key.getValidSeconds() * 1000) - now.getTime() < 0;
    }

    /**
     * Check if all keys are expired
     */
    public static boolean checkAllExpired(PGPPublicKeyRing ring) {
        boolean allExpired = true;
        Iterator<PGPPublicKey> it = ring.getPublicKeys();
        while (it.hasNext()) {
            PGPPublicKey key = it.next();
            allExpired = allExpired && isExpired(key);
        }
        return (allExpired);
    }

    /**
     * Duplicates the specified secret key ring. This method can also be used to create a new secret key ring with a different password
     *
     * @param secretKeyRing The secret key ring to duplicate
     * @param decryptorPasswordHash The hashed password for the decryptor
     * @param encryptorPasswordHash The hashed password for the encryptor
     * @param symmetricKeyAlgorithmTag The symmetric key algorithm tag (see PGPEncryptedData.AES_256 and PGPEncryptedData.AES_128)
     * @return The duplicated {@link PGPSecretKeyRing}
     * @throws PGPException
     */
    public static PGPSecretKeyRing duplicateSecretKeyRing(PGPSecretKeyRing secretKeyRing, String decryptorPasswordHash, String encryptorPasswordHash, int symmetricKeyAlgorithmTag) throws PGPException {
        PGPDigestCalculator sha256Calc = new BcPGPDigestCalculatorProvider().get(getHashAlgorithmTags());
        PBESecretKeyDecryptor oldEncryptor = new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(decryptorPasswordHash.toCharArray());
        PBESecretKeyEncryptor newEncryptor = new BcPBESecretKeyEncryptorBuilder(symmetricKeyAlgorithmTag, sha256Calc, 0x60).build(encryptorPasswordHash.toCharArray());

        return PGPSecretKeyRing.copyWithNewPassword(secretKeyRing, oldEncryptor, newEncryptor);
    }



    /**
     * Gets the flags of a given key
     *
     * @param key the key to get the flags for
     * @return the flags for the given key
     */
    public static int getKeyFlags(PGPPublicKey key) {
        Iterator<PGPSignature> signatures = key.getSignatures();
        while (signatures.hasNext()) {
            PGPSignature signature = signatures.next();
            PGPSignatureSubpacketVector packet = signature.getHashedSubPackets();
            if(packet != null) {
                return packet.getKeyFlags();
            }
        }
        return 0;
    }

    /**
     * Checks whether a key has the given flags or not
     *
     * @param key the key
     * @param flag the flag to check
     * @see https://tools.ietf.org/html/rfc4880#section-5.2.3.21
     * @return True if the key has the given flag
     */
    public static boolean keyHasFlag(PGPPublicKey key, int flag) {
        int existingFlags = getKeyFlags(key);
        return (existingFlags & flag) > 0;
    }

    /**
     * Checks whether a key is meant to be an encryption key.
     *
     * Note: This method checks if the preferred usage of the key is encrypting.
     * Use {@link org.bouncycastle.openpgp.PGPPublicKey#isEncryptionKey} for checking if a key is technical able to encrypt
     *
     * @param key the key to check
     * @return true, if the key is meant to be an encryption key, false otherwise
     */
    public static boolean isEncryptionKey(PGPPublicKey key) {
        //Check if the key has flags
        if (getKeyFlags(key) > 0) {
            //Check for encryption Flags
            return keyHasFlag(key, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);
        } else {
            //Fallback if flags do not exist (for older keys or if the key creation software did not create them)
            return key.isEncryptionKey();
        }
    }

    /**
     * Returns the ring's singing key
     * @param keyRing The key ring
     * @return the ring's signing sub key or the master key, if no sub key is marked for singing
     */
    public static PGPSecretKey getSigningKey(PGPSecretKeyRing keyRing){
        PGPSecretKey ret = getSigningSubKey(keyRing);
        if(ret == null){
            //If no signing subkey was found we are using the master key
            ret = keyRing.getSecretKey();
        }
        return ret;
    }

    /**
     * Returns a ring's subkey which is meant to be used as signing key
     * @param keyRing The ring to get the signing sub key for
     * @return the signing sub key, or null if no signing sub key was found
     */
    public static PGPSecretKey getSigningSubKey(PGPSecretKeyRing keyRing){
        Iterator<PGPSecretKey> iter = keyRing.getSecretKeys();
        while(iter.hasNext()){
            PGPSecretKey secretKey = iter.next();
            if(!secretKey.isMasterKey() /*only check sub keys*/){
                PGPPublicKey publicKey = secretKey.getPublicKey();
                boolean isSigningKey = keyHasFlag(publicKey,KeyFlags.SIGN_DATA);
                if(isSigningKey && !publicKey.hasRevocation()){
                    return secretKey;
                }
            }
        }
        return null;
    }

    /**
     * Gets the public key from the key ring which is suitable for encrypting data.
     *
     * @return The key which can be used for encrypting data
     */
    public static PGPPublicKey getEncryptionKey(PGPPublicKeyRing keyRing) {
        PGPPublicKey found = null;
        if (keyRing != null) {
            Iterator it = keyRing.getPublicKeys();
            while (it.hasNext()) {
                PGPPublicKey key = (PGPPublicKey) it.next();
                if (PGPKeysUtil.isEncryptionKey(key)) {
                    if (key.isMasterKey() && !isExpired(key) && !key.hasRevocation()) {  // If master key, we will use only if we don't have another encryption key
                        if (found == null) {
                            found = key;
                        }
                    } else {
                        if (!key.hasRevocation()) {
                            if (!isExpired(key)) {
                                return (key);
                            }
                        }
                    }
                }
            }
        }
        return found;
    }

    /**
     * Gets the master key from the given public key ring, or null if no master key was found.
     *
     * @param publicKeyRing The key ring.
     * @return The master key, or null if no master key was found.
     */
    public static PGPPublicKey getPublicMasterKey(PGPPublicKeyRing publicKeyRing) {
        for (PGPPublicKey publicKey : publicKeyRing) {
            if (publicKey.isMasterKey()) {
                return publicKey;
            }
        }
        return null;
    }

    /**
     * Adds a new User ID to a {@link PGPPublicKeyRing}
     *
     * @param publicKeyRing The public key ring to add the user ID to
     * @param privateKey The private key used for signing
     * @param userId The new user ID
     * @return The public key ring containing the new user ID
     * @throws PGPException
     */
    public static PGPPublicKeyRing addUID(PGPPublicKeyRing publicKeyRing, PGPPrivateKey privateKey, String userId) throws PGPException {
        PGPPublicKey pub = publicKeyRing.getPublicKey();
        PGPSignatureGenerator generator = new PGPSignatureGenerator(new BcPGPContentSignerBuilder(PGPPublicKey.RSA_GENERAL, org.bouncycastle.openpgp.PGPUtil.SHA1));
        generator.init(PGPSignature.POSITIVE_CERTIFICATION, privateKey);
        PGPSignatureSubpacketGenerator signhashgen = new PGPSignatureSubpacketGenerator();
        generator.setHashedSubpackets(signhashgen.generate());
        PGPSignature certification = generator.generateCertification(userId, pub);
        PGPPublicKey newPubKey = PGPPublicKey.addCertification(pub, userId, certification);
        publicKeyRing = PGPPublicKeyRing.removePublicKey(publicKeyRing, pub);
        publicKeyRing = PGPPublicKeyRing.insertPublicKey(publicKeyRing, newPubKey);
        return publicKeyRing;
    }

    /**
     * Convert the specified reason string a byte representation. See {@link RevocationReasonTags}
     *
     * @param reason The reason in string
     * @return The byte representation
     */
    private static byte revokeReason(String reason) {
        switch (reason) {
            case "NO_REASON":
                return RevocationReasonTags.NO_REASON;
            case "KEY_SUPERSEDED":
                return RevocationReasonTags.KEY_SUPERSEDED;
            case "KEY_COMPROMISED":
                return RevocationReasonTags.KEY_COMPROMISED;
            case "KEY_RETIRED":
                return RevocationReasonTags.KEY_RETIRED;
            case "USER_NO_LONGER_VALID":
                return RevocationReasonTags.USER_NO_LONGER_VALID;
        }
        return RevocationReasonTags.NO_REASON;
    }

    /**
     * Revokes a public key ring
     *
     * @param privateKey The private key which is used for revocation.
     * @param publicKeyRing The public key ring to be revoked
     * @param revocationReason The reason why the key is being revoked.
     * @return The new key ring with the recovation certificate set
     * @throws PGPException
     */
    public static PGPPublicKeyRing revokeKey(PGPPrivateKey privateKey, PGPPublicKeyRing publicKeyRing, String revocationReason) throws PGPException {
        privateKey = Objects.requireNonNull(privateKey, "privateKey must not be null");
        publicKeyRing = Objects.requireNonNull(publicKeyRing, "publicKeyRing must not be null");
        PGPPublicKeyRing ret = publicKeyRing;
        Iterator<PGPPublicKey> pkeys = publicKeyRing.getPublicKeys();
        PGPPublicKey master = getPublicMasterKey(publicKeyRing);
        while (pkeys.hasNext()) {
            PGPPublicKey pub = pkeys.next();
            ret = PGPPublicKeyRing.removePublicKey(ret, pub);
            PGPSignatureSubpacketGenerator subHashGenerator = new PGPSignatureSubpacketGenerator();
            PGPSignatureSubpacketGenerator subUnHashGenerator = new PGPSignatureSubpacketGenerator();
            PGPSignatureGenerator generator = new PGPSignatureGenerator(new BcPGPContentSignerBuilder(pub.getAlgorithm(), org.bouncycastle.openpgp.PGPUtil.SHA1));
            if (pub.isMasterKey()) {
                generator.init(PGPSignature.KEY_REVOCATION, privateKey);
                master = pub;
            } else {
                generator.init(PGPSignature.SUBKEY_REVOCATION, privateKey);
            }
            subHashGenerator.setSignatureCreationTime(false, new Date());
            subHashGenerator.setRevocationReason(false, revokeReason(revocationReason), revocationReason);
            subUnHashGenerator.setRevocationKey(false, pub.getAlgorithm(), pub.getFingerprint());
            generator.setHashedSubpackets(subHashGenerator.generate());
            generator.setUnhashedSubpackets(subUnHashGenerator.generate());
            if (pub.isMasterKey()) {
                PGPSignature signature = generator.generateCertification(pub);
                pub = PGPPublicKey.addCertification(pub, signature);
            } else {
                PGPSignature signature = generator.generateCertification(master, pub);
                pub = PGPPublicKey.addCertification(pub, signature);
            }

            ret = PGPPublicKeyRing.insertPublicKey(ret, pub);
        }

        return ret;
    }
}
