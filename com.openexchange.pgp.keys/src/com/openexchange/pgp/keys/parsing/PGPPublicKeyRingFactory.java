
package com.openexchange.pgp.keys.parsing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.slf4j.Logger;

/**
 * {@link PGPPublicKeyRingFactory} a simple factory for creating instances of PGPPublicKeyRing
 */
public class PGPPublicKeyRingFactory {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PGPPublicKeyRingFactory.class);
    }

    /**
     * Creates a collection for PGPPublicKeyRing objects from the given data
     *
     * @param keyData The data
     * @return A collection of PGPPublicKeyRing objects or an empty collection if the data do not contain any valid PGPPublicKeyRing objects.
     * @throws IOException if the keyData contain other/invalid PGP objects
     */
    public static List<PGPPublicKeyRing> createAll(InputStream keyData) throws IOException {
        List<PGPPublicKeyRing> ret = new ArrayList<PGPPublicKeyRing>();
        // PGPUtil.getDecoderStream() will detect ASCII-armor automatically and decode it,
        // the PGPObject factory then knows how to read all the data in the encoded stream
        PGPObjectFactory factory = new PGPObjectFactory(PGPUtil.getDecoderStream(keyData), new BcKeyFingerprintCalculator());
        Object object = null;
        while ((object = factory.nextObject()) != null) {
            if (object instanceof PGPPublicKeyRing) {
                ret.add((PGPPublicKeyRing) object);
            } else {
                LoggerHolder.LOG.error("Input data does not contain a PGP Public Key");
            }
        }
        return ret;
    }

    /**
     * Creates a single PGPPublicKeyRing object from the given data.
     * <br>
     * <br>
     * If the data contain more than one key ring, the first one is returned.
     * Use {@link PGPPublicKeyRingFactory#createAll(InputStream)} to parse all keys.
     *
     * @param keyData The data to create the key from
     * @return the first key created from the data, or null if the PGP data do not contain at least one valid PGPPublicKeyRing
     * @throws IOException due an error
     */
    public static PGPPublicKeyRing create(InputStream keyData) throws IOException {
        List<PGPPublicKeyRing> keyRings = createAll(keyData);
        if (!keyRings.isEmpty()) {
            return keyRings.iterator().next();
        }
        return null;
    }

    /**
     * Creates a single PGPPublicKeyRing object from the given, ASCII armored, data.
     * <br>
     * <br>
     * If the data contain more than one key ring, the first one is returned.
     * Use {@link PGPPublicKeyRingFactory#createAll(InputStream)} to parse all keys.
     *
     * @param keyData The data to create the key from
     * @return the first key created from the data, or null if the PGP data do not contain at least one valid PGPPublicKeyRing
     * @throws IOException
     */
    public static PGPPublicKeyRing create(String keyData) throws IOException {
        return create(new ByteArrayInputStream(keyData.getBytes("UTF-8")));
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
            PGPPublicKeyRing parsedKey = create(publicKey);
            if (parsedKey != null) {
                ret.add(parsedKey);
            }
        }
        return ret;
    }
}
