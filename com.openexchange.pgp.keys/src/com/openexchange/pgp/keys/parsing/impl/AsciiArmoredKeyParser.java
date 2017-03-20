
package com.openexchange.pgp.keys.parsing.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.pgp.keys.parsing.KeyRingParserResult;
import com.openexchange.tools.io.IOUtils;

/**
 * {@link AsciiArmoredKeyParser} A parser which reads ASCII-Armored keys from an input stream
 */
public class AsciiArmoredKeyParser implements KeyParser {

    private final String REGEX_PUBLIC_ASC = "(-----BEGIN PGP PUBLIC KEY BLOCK-----.*?-----END PGP PUBLIC KEY BLOCK-----)";
    private final String REGEX_PRIVATE_ASC = "(-----BEGIN PGP PRIVATE KEY BLOCK-----.*?-----END PGP PRIVATE KEY BLOCK-----)";

    private List<String> getPublicKeysFromString(String s) {
        ArrayList<String> ret = new ArrayList<String>();
        Pattern pattern = Pattern.compile(REGEX_PUBLIC_ASC, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            ret.add(matcher.group(1));
        }
        return ret;
    }

    private List<String> getPrivateKeysFromString(String s) {
        ArrayList<String> ret = new ArrayList<String>();
        Pattern pattern = Pattern.compile(REGEX_PRIVATE_ASC, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            ret.add(matcher.group(1));
        }
        return ret;
    }

    @Override
    public KeyRingParserResult parse(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            //Getting the data from the stream
            IOUtils.transfer(inputStream, out);
            String data = new String(out.toByteArray());

            if (data != null && !data.isEmpty()) {
                //Parsing
                return new KeyRingParserResult(getPublicKeysFromString(data), getPrivateKeysFromString(data));
            }
            return null;
        } finally {
            out.close();
            inputStream.close();
        }
    }
}
