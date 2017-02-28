
package com.openexchange.pgp.keys.parsing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;

/**
 * {@link PGPPublicKeyRingFactory} a simple factory for creating instances of PGPPublicKeyRing
 */
public class PGPPublicKeyRingFactory {

    /**
     * Creates a PGPPublicKeyRing object from some ASCII-armored data
     *
     * @param asciiKeyData the ASCII-armored data to create the key from
     * @return the key created from the ASCII-armored data
     * @throws IOException due an error
     * @throws IllegalArgumentException if asciiKeyData does not contain a valid ASCII-armored key
     */
    public static PGPPublicKeyRing create(String asciiKeyData) throws IOException {
        // PGPUtil.getDecoderStream() will detect ASCII-armor automatically and decode it,
        // the PGPObject factory then knows how to read all the data in the encoded stream
        PGPObjectFactory factory = new PGPObjectFactory(PGPUtil.getDecoderStream(new ByteArrayInputStream(asciiKeyData.getBytes("UTF-8"))), new BcKeyFingerprintCalculator());
        // these files should really just have one object in them,
        // and that object should be a PGPPublicKeyRing.
        Object o = factory.nextObject();
        if (o instanceof PGPPublicKeyRing) {
            return (PGPPublicKeyRing) o;
        }
        throw new IllegalArgumentException("Input text does not contain a PGP Public Key");
    }

    /**
     * Creates a set of PGPPublicKeyRing objects from a parsing result
     *
     * @param parserResult the result to create the keys from
     * @return a list of public key rings for the parsed keys data
     * @throws IOException due an error
     * @throws IllegalArgumentException if some data from the parserResult does not contain a valid ASCII-armored key
     */
    public static List<PGPPublicKeyRing> create(KeyRingParserResult parserResult) throws IOException {
        List<PGPPublicKeyRing> ret = new ArrayList<PGPPublicKeyRing>();
        for (String publicKey : parserResult.getPublicKeysData()) {
            ret.add(create(publicKey));
        }
        return ret;
    }
}
