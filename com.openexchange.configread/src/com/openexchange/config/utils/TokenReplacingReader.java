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

package com.openexchange.config.utils;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * {@link TokenReplacingReader} - A reader which interpolates keyword values into a character stream.
 * <p>
 * Tokens are recognized when enclosed between starting and ending delimiter strings. The token values are fetched from a map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class TokenReplacingReader extends FilterReader {

    /** Default begin token. */
    private static final String DEFAULT_BEGIN_TOKEN = "{{";

    /** Default end token. */
    private static final String DEFAULT_END_TOKEN = "}}";

    // -------------------------------------------------------------------------------------------------------------------------------------

    /** replacement text from a token */
    private String replaceData;

    /** Index into replacement data */
    private int replaceIndex = -1;

    /** Index into previous data */
    private int previousIndex = -1;

    /** Map for the name/value pairs */
    private final Map<String, String> variables;

    /** Character marking the beginning of a token. */
    private final String beginToken;

    /** Character marking the end of a token. */
    private final String endToken;

    /** Length of begin token. */
    private final int beginTokenLength;

    /** Length of end token. */
    private final int endTokenLength;

    /**
     * Initializes a new {@link TokenReplacingReader}.
     *
     * @param in The reader to be wrapped for interpolation
     * @param variables The name/value pairs to be interpolated into the character stream
     * @param beginToken An interpolation target begins with this
     * @param endToken An interpolation target ends with this
     */
    public TokenReplacingReader(Reader in) {
        this(in, System.getenv(), DEFAULT_BEGIN_TOKEN, DEFAULT_END_TOKEN);
    }

    /**
     * Initializes a new {@link TokenReplacingReader}.
     *
     * @param in The reader to be wrapped for interpolation
     * @param variables The name/value pairs to be interpolated into the character stream
     * @param beginToken An interpolation target begins with this
     * @param endToken An interpolation target ends with this
     */
    public TokenReplacingReader(Reader in, Map<String, String> variables, String beginToken, String endToken) {
        super(in);
        this.variables = variables;
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

            String variableKey = key.substring(beginTokenLength - 1, key.length() - endTokenLength).trim();
            String defaultValue;
            {
                int colonPos = variableKey.indexOf(':');
                if (colonPos > 0) {
                    defaultValue = variableKey.substring(colonPos + 1);
                    variableKey = variableKey.substring(0, colonPos);
                } else {
                    defaultValue = null;
                }
            }
            String value = variables.get(variableKey);
            if (value == null && defaultValue != null) {
                value = defaultValue;
            }
            if (value != null) {
                if (value.length() != 0) {
                    replaceData = value;
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

}
