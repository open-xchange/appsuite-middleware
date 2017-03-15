
package com.openexchange.pgp.keys.parsing;

import java.io.IOException;
import java.util.List;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import com.openexchange.pgp.keys.tools.PGPKeysUtil;

/**
 * {@link KeyRingParserResult} represents a ASCII-armored key parsing result
 */
public class KeyRingParserResult {

    private final List<String> publicKeys;
    private final List<String> privateKeys;

    /**
     * Initializes a new {@link KeyRingParserResult}.
     *
     * @param publicKeys a set of parsed ASCII-armored public keys
     * @param privateKeys a set of parsed ASCII-armored private keys
     */
    public KeyRingParserResult(List<String> publicKeys, List<String> privateKeys) {
        this.publicKeys = publicKeys;
        this.privateKeys = privateKeys;
    }

    /**
     * Gets the raw ASCII-armored public key data.
     *
     * @return a set of parsed raw ASCII-armored public keys
     */
    public List<String> getPublicKeysData() {
        return publicKeys;
    }

    /**
     * Gets the raw ASCII-armored secret key data.
     *
     * @return a set of parsed raw ASCII-armored secret keys
     */
    public List<String> getSecretKeysData() {
        return privateKeys;
    }

    /**
     * Gets the parsed public key rings.
     *
     * @return A set of parsed PGPPublicKeyRing instances.
     * @throws IOException
     */
    public List<PGPPublicKeyRing> toPublicKeyRings() throws IOException {
        return PGPPublicKeyRingFactory.create(this);
    }

    /**
     * Gets the parsed secret key rings.
     *
     * @return A set of parsed PGPSecretKeyRing instances.
     * @throws IOException
     */
    public List<PGPSecretKeyRing> toSecretKeyRings() throws IOException {
        return PGPSecretKeyRingFactory.create(this);
    }

    /**
     * Gets the fist public key from the parsed result which is suitable for encryption.
     *
     * @return The fist public key suitable for encryption.
     * @throws IOException
     */
    public PGPPublicKey toEncryptionKey() throws IOException {
        List<PGPPublicKeyRing> publicKeyRings = toPublicKeyRings();
        if(publicKeyRings != null && publicKeyRings.size() > 0) {
            return PGPKeysUtil.getEncryptionKey(publicKeyRings.get(0));
        }
        return null;
    }

    /**
     * Gets the first secret key from the parsed result which is suitable for signing.
     *
     * @return The first secret key suitable for signing.
     * @throws IOException
     */
    public PGPSecretKey toSigningKey() throws IOException {
        List<PGPSecretKeyRing> secretKeyRings = toSecretKeyRings();
        if (secretKeyRings != null && secretKeyRings.size() > 0) {
            return PGPKeysUtil.getSigningKey(secretKeyRings.get(0));
        }
        return null;
    }
}
