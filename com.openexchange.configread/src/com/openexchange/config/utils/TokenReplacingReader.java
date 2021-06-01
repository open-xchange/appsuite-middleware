/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.config.utils;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import com.openexchange.config.VariablesProvider;
import com.openexchange.java.Strings;
import com.openexchange.osgi.ServiceListing;

/**
 * {@link TokenReplacingReader} - A reader which interpolates token values into a character stream.
 * <p>
 * By default the <code>"{{"</code> and the <code>"}}"</code> sequences mark the start and end of a token. A token is interpreted as:
 * <pre>
 *   "{{" + ([source-id] + " ")? + [variable-name] + (":" + [default-value]) + "}}"
 *           ^^^^^^^^^^^^^^^^^                        ^^^^^^^^^^^^^^^^^^^^^
 *               optional                                 optional
 * </pre>
 * The source identifier tells from what source to look-up the given variable name and the default value defines the value to use in case no
 * such variable is available in looked-up source.
 *
 * <h3>Supported source identifiers</h3>
 * <ul>
 * <li><code>"env"</code> determines that system environment is supposed to be looked-up for a certain variable name. Furthermore,
 * it is assumed as default in case no source identifier is present.</li>
 * <li><code>"file"</code> sets that a certain .properties file is supposed to be looked-up. Moreover, this identifier expects
 * specification of the actual .properties file in surrounding arrow brackets; e.g. <code>"file<tokenvalues.properties>"</code>.
 * That .properties file is supposed to reside in the <code>/opt/open-xchange/etc</code> directory or any of its sub-directories.</li>
 * <li>Any other source identifier refers to a programmatically registered instance of <code>com.openexchange.config.VariablesProvider</code>
 * in case a custom source needs to be used.</li>
 * </ul>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class TokenReplacingReader extends FilterReader {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(TokenReplacingReader.class);
    }

    /** Default begin token. */
    private static final String DEFAULT_BEGIN_TOKEN = "{{";

    /** Default end token. */
    private static final String DEFAULT_END_TOKEN = "}}";

    private static final AtomicReference<ServiceListing<VariablesProvider>> VARIABLES_PROVIDER_LISTING = new AtomicReference<>(null);

    /**
     * Sets the variables provider listing.
     *
     * @param variablesProviderListing The listing to set
     */
    public static void setVariablesProviderListing(ServiceListing<VariablesProvider> variablesProviderListing) {
        VARIABLES_PROVIDER_LISTING.set(variablesProviderListing);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final String beginToken;
    private final String endToken;
    private final int beginTokenLength;
    private final int endTokenLength;

    private String replaceData;
    private int replaceIndex = -1;
    private int previousIndex = -1;

    /**
     * Initializes a new {@link TokenReplacingReader}.
     *
     * @param in The reader to be wrapped for interpolation
     */
    public TokenReplacingReader(Reader in) {
        this(in, DEFAULT_BEGIN_TOKEN, DEFAULT_END_TOKEN);
    }

    /**
     * Initializes a new {@link TokenReplacingReader}.
     *
     * @param in The reader to be wrapped for interpolation
     * @param beginToken An interpolation target begins with this
     * @param endToken An interpolation target ends with this
     */
    public TokenReplacingReader(Reader in, String beginToken, String endToken) {
        super(in);
        this.beginToken = beginToken;
        this.endToken = endToken;
        beginTokenLength = beginToken.length();
        endTokenLength = endToken.length();
    }

    /**
     * Skips characters. This method will block until some characters are
     * available, an I/O error occurs, or the end of the stream is reached.
     *
     * @param n The number of characters to skip
     * @return the number of characters actually skipped
     * @throws IllegalArgumentException If <code>n</code> is negative.
     * @throws IOException If an I/O error occurs
     */
    @Override
    public long skip(long n) throws IOException {
        if (n < 0L) {
            throw new IllegalArgumentException("skip value is negative");
        }

        for (long i = 0; i < n; i++) {
            if (read() == -1) {
                return i;
            }
        }
        return n;
    }

    /**
     * Reads characters into a portion of an array. This method will block
     * until some input is available, an I/O error occurs, or the end of the
     * stream is reached.
     *
     * @param cbuf Destination buffer to write characters to.
     *            Must not be <code>null</code>.
     * @param off Offset at which to start storing characters.
     * @param len Maximum number of characters to read.
     *
     * @return the number of characters read, or -1 if the end of the
     *         stream has been reached
     *
     * @throws IOException If an I/O error occurs
     */
    @Override
    public int read(char cbuf[], int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            int ch = read();
            if (ch == -1) {
                return i == 0 ? -1 : i;
            }
            cbuf[off + i] = (char) ch;
        }
        return len;
    }

    /**
     * Gets the next character in the filtered stream, replacing tokens from the original stream.
     *
     * @return The next character in the resulting stream, or <code>-1</code> if the end of the resulting stream has been reached
     * @throws IOException If the underlying stream throws an I/O error during reading
     */
    @Override
    public int read() throws IOException {
        if (replaceIndex != -1 && replaceIndex < replaceData.length()) {
            int ch = replaceData.charAt(replaceIndex++);
            if (replaceIndex >= replaceData.length()) {
                replaceIndex = -1;
            }
            return ch;
        }

        int ch;
        if (previousIndex != -1 && previousIndex < endTokenLength) {
            ch = endToken.charAt(previousIndex++);
        } else {
            ch = in.read();
        }

        if (ch == beginToken.charAt(0)) {
            StringBuilder key = new StringBuilder();
            int beginTokenMatchPos = 1;
            do {
                if (previousIndex != -1 && previousIndex < endTokenLength) {
                    ch = endToken.charAt(previousIndex++);
                } else {
                    ch = in.read();
                }
                if (ch != -1) {
                    key.append((char) ch);

                    if ((beginTokenMatchPos < beginTokenLength) && (ch != beginToken.charAt(beginTokenMatchPos++))) {
                        ch = -1; // not really EOF but to trigger code below
                        break;
                    }
                } else {
                    break;
                }
            } while (ch != endToken.charAt(0));

            // now test endToken
            if (ch != -1 && endTokenLength > 1) {
                int endTokenMatchPos = 1;

                do {
                    if (previousIndex != -1 && previousIndex < endTokenLength) {
                        ch = endToken.charAt(previousIndex++);
                    } else {
                        ch = in.read();
                    }

                    if (ch != -1) {
                        key.append((char) ch);

                        if (ch != endToken.charAt(endTokenMatchPos++)) {
                            ch = -1; // not really EOF but to trigger code below
                            break;
                        }

                    } else {
                        break;
                    }
                } while (endTokenMatchPos < endTokenLength);
            }

            // There is nothing left to read so we have the situation where the begin/end token
            // are in fact the same and as there is nothing left to read we have got ourselves
            // end of a token boundary so let it pass through.
            if (ch == -1) {
                replaceData = key.toString();
                replaceIndex = 0;
                return beginToken.charAt(0);
            }

            String optValue = determineValueFor(key);
            if (optValue != null) {
                if (optValue.length() != 0) {
                    replaceData = optValue;
                    replaceIndex = 0;
                }
                return read();
            }

            previousIndex = 0;
            replaceData = key.substring(0, key.length() - endTokenLength);
            replaceIndex = 0;
            return beginToken.charAt(0);
        }

        return ch;
    }

    /**
     * Determines the value to interpolate for given key.
     *
     * @param key The key; e.g. <code>"{{ env SOME_PROP:true }}"</code>
     * @return The value or <code>null</code> if there is value associated with given key
     */
    private String determineValueFor(StringBuilder key) {
        try {
            String variableKey = key.substring(beginTokenLength - 1, key.length() - endTokenLength).trim();
            ParseResult result = parseVariableKey(variableKey);
            return (result.value != null ? result.value : (result.defaultValue != null ? result.defaultValue : null));
        } catch (IllegalTokenException e) {
            LoggerHolder.LOG.error("", e);
            return null;
        }
    }

    /**
     * Parses the given token and resolves it to corresponding value that is supposed to be interpolated into character stream.
     * A token is interpreted as:
     * <pre>
     *   "{{" + [source-id] + " " + [variable-name] + ":" + [default-value] + "}}"
     * </pre>
     *
     * @param token The token to parse; e.g. <code>"env SOME_PROP:true"</code>
     * @return The parse result for given token
     * @throws IllegalTokenException If passed token is invalid and cannot be parsed
     */
    private static ParseResult parseVariableKey(String token) throws IllegalTokenException {
        int spacePos = token.indexOf(' ');
        if (spacePos <= 0) {
            // No space present or at illegal position
            int colonPos = token.indexOf(':');
            if (colonPos <= 0) {
                return new ParseResult(token, null, SysEnvVariablesProvider.getInstance());
            }

            String defaultValue = token.substring(colonPos + 1);
            if (defaultValue.length() > 0 && Strings.isEmpty(defaultValue)) {
                defaultValue = "";
            }
            return new ParseResult(token.substring(0, colonPos), defaultValue, SysEnvVariablesProvider.getInstance());
        }

        // <source> + " " + <variable-name> + ":" + <default-value>
        // env SOME_PROPERTY  or  env SOME_PROPERTY:true

        // file(values.properties):SOME_PROPERTY:true
        VariablesProvider variablesProvider;
        {
            String providerName = token.substring(0, spacePos).trim();
            if ("env".equals(providerName)) {
                variablesProvider = SysEnvVariablesProvider.getInstance();
            } else if (providerName.startsWith("file")) {
                int arrowStartPos = providerName.indexOf('<');
                if (arrowStartPos < 0) {
                    // Illegal identifier
                    throw new IllegalTokenException("Illegal token: \"" + token + "\". Missing .properties file specification.");
                }
                int arrowEndPos = providerName.indexOf('>', arrowStartPos + 1);
                if (arrowEndPos < 0) {
                    // Illegal identifier
                    throw new IllegalTokenException("Illegal token: \"" + token + "\". Missing .properties file specification.");
                }
                String fileName = providerName.substring(arrowStartPos + 1, arrowEndPos);
                if (Strings.isEmpty(fileName)) {
                    // Illegal identifier
                    throw new IllegalTokenException("Illegal token: \"" + token + "\". Missing .properties file specification.");
                }
                fileName = fileName.trim();
                variablesProvider = FileVariablesProvider.getInstanceFor(fileName);
                if (variablesProvider == null) {
                    // Illegal identifier
                    throw new IllegalTokenException("Illegal token: \"" + token + "\". No such .properties file: " + fileName);
                }
            } else {
                variablesProvider = null;
                ServiceListing<VariablesProvider> listing = VARIABLES_PROVIDER_LISTING.get();
                if (listing != null) {
                    for (Iterator<VariablesProvider> it = listing.iterator(); variablesProvider == null && it.hasNext();) {
                        VariablesProvider vp = it.next();
                        if (providerName.equals(vp.getName())) {
                            variablesProvider = vp;
                        }
                    }
                }
                if (variablesProvider == null) {
                    throw new IllegalTokenException("Illegal token: \"" + token + "\". No such registered provider: " + providerName);
                }
            }
        }

        String varKey = token.substring(spacePos + 1).trim();
        int colonPos = varKey.indexOf(':');
        if (colonPos <= 0) {
            return new ParseResult(varKey, null, variablesProvider);
        }

        if (colonPos + 1 >= varKey.length()) {
            return new ParseResult(varKey.substring(0, colonPos), "", variablesProvider);
        }

        String defaultValue = varKey.substring(colonPos + 1);
        if (defaultValue.length() > 0 && Strings.isEmpty(defaultValue)) {
            defaultValue = "";
        }
        return new ParseResult(varKey.substring(0, colonPos), defaultValue, variablesProvider);

    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class ParseResult {

        final String value;
        final String defaultValue;

        ParseResult(String key, String defaultValue, VariablesProvider variablesProvider) {
            this(variablesProvider.getForKey(key), defaultValue);
        }

        ParseResult(String value, String defaultValue) {
            super();
            this.value = value;
            this.defaultValue = defaultValue;
        }
    }

    private static class IllegalTokenException extends Exception {

        private static final long serialVersionUID = -4805746003498666565L;

        /**
         * Initializes a new {@link IllegalTokenException}.
         *
         * @param message The message
         */
        IllegalTokenException(String message) {
            super(message);
        }
    }

}
