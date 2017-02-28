package com.openexchange.pgp.keys.parsing.impl;

import java.io.IOException;
import java.io.InputStream;
import com.openexchange.pgp.keys.parsing.KeyRingParserResult;

/**
 * {@link KeyParser} defines a parser for public and private keys which should be imported to OX Guard
 */
public interface KeyParser {

    /**
     * Parses public and private keys from an inputStream and returns them as ASCII-armored keys
     * @param inputStream the input stream to parse the keys from
     * @return a result set of parsed ASCII-armored keys
     * @throws IOException due an error while reading from the stream
     */
    KeyRingParserResult parse(InputStream inputStream) throws IOException;
}