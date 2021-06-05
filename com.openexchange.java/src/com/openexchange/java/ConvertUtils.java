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

package com.openexchange.java;

/**
 * {@link ConvertUtils} - Static methods that help to convert.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ConvertUtils {

    /**
     * Initializes a new {@link ConvertUtils}.
     */
    private ConvertUtils() {
        super();
    }

    /**
     * Converts unicodes to encoded &#92;uXXXX and escapes special characters with a preceding slash
     *
     * @param arg The argument to convert
     * @return The converted argument
     */
    public static String saveConvert(String arg, boolean escapeSpace, boolean escapeUnicode) {
        if (Strings.isEmpty(arg)) {
            return arg;
        }

        int len = arg.length();

        StringBuilder sb = null;
        for (int k = 0; k < len; k++) {
            char aChar = arg.charAt(k);
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    sb = initBuilderIfNeeded(sb, len, arg, k);
                    sb.append('\\').append('\\');
                    continue;
                }
                if (null != sb) {
                    sb.append(aChar);
                }
                continue;
            }

            switch (aChar) {
                case ' ':
                    if (k == 0 || escapeSpace) {
                        sb = initBuilderIfNeeded(sb, len, arg, k);
                        sb.append('\\');

                    }
                    if (null != sb) {
                        sb.append(' ');
                    }
                    break;
                case '\t':
                    sb = initBuilderIfNeeded(sb, len, arg, k);
                    sb.append('\\').append('t');
                    break;
                case '\n':
                    sb = initBuilderIfNeeded(sb, len, arg, k);
                    sb.append('\\').append('n');
                    break;
                case '\r':
                    sb = initBuilderIfNeeded(sb, len, arg, k);
                    sb.append('\\').append('r');
                    break;
                case '\f':
                    sb = initBuilderIfNeeded(sb, len, arg, k);
                    sb.append('\\').append('f');
                    break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    sb = initBuilderIfNeeded(sb, len, arg, k);
                    sb.append('\\').append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) && escapeUnicode) {
                        sb = initBuilderIfNeeded(sb, len, arg, k);
                        sb.append('\\');
                        sb.append('u');
                        sb.append(toHex((aChar >> 12) & 0xF));
                        sb.append(toHex((aChar >>  8) & 0xF));
                        sb.append(toHex((aChar >>  4) & 0xF));
                        sb.append(toHex( aChar        & 0xF));
                    } else {
                        if (null != sb) {
                            sb.append(aChar);
                        }
                    }
            } // End of switch statement
        }

        return null == sb ? arg : sb.toString();
    }

    private static final char[] hexDigit = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    /**
     * Convert a nibble to a hex character
     * @param   nibble  the nibble to convert.
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /**
     * Converts encoded &#92;uXXXX to unicode chars and changes special saved chars to their original forms
     *
     * @param arg The argument to convert
     * @return The converted argument
     */
    public static String loadConvert(String arg) {
        if (Strings.isEmpty(arg)) {
            return arg;
        }

        int len = arg.length();

        StringBuilder sb = null;
        for (int k = 0; k < len; k++) {
            char aChar = arg.charAt(k);
            if (aChar == '\\') {
                int st = k;
                if (k < len -1) {
                    aChar = arg.charAt(++k);
                    if (aChar == 'u' && k < len - 4) {
                        int value = 0;
                        for (int i = 0; i < 4; i++) {
                            aChar = arg.charAt(++k);
                            switch (aChar) {
                              case '0': case '1': case '2': case '3': case '4':
                              case '5': case '6': case '7': case '8': case '9':
                                 value = (value << 4) + aChar - '0';
                                 break;
                              case 'a': case 'b': case 'c':
                              case 'd': case 'e': case 'f':
                                 value = (value << 4) + 10 + aChar - 'a';
                                 break;
                              case 'A': case 'B': case 'C':
                              case 'D': case 'E': case 'F':
                                 value = (value << 4) + 10 + aChar - 'A';
                                 break;
                              default:
                                  throw new IllegalArgumentException("Malformed \\uXXXX encoding.");
                            }
                        }
                        sb = initBuilderIfNeeded(sb, len, arg, st);
                        sb.append((char) value);
                    } else {
                        if (aChar == 't') {
                            aChar = '\t';
                            sb = initBuilderIfNeeded(sb, len, arg, st);
                            sb.append(aChar);
                        } else if (aChar == 'r') {
                            aChar = '\r';
                            sb = initBuilderIfNeeded(sb, len, arg, st);
                            sb.append(aChar);
                        } else if (aChar == 'n') {
                            aChar = '\n';
                            sb = initBuilderIfNeeded(sb, len, arg, st);
                            sb.append(aChar);
                        } else if (aChar == 'f') {
                            aChar = '\f';
                            sb = initBuilderIfNeeded(sb, len, arg, st);
                            sb.append(aChar);
                        } else {
                            sb = initBuilderIfNeeded(sb, len, arg, st);
                            sb.append(aChar);
                        }
                    }
                } else {
                    if (null != sb) {
                        sb.append(aChar);
                    }
                }
            } else {
                if (null != sb) {
                    sb.append(aChar);
                }
            }
        }

        return null == sb ? arg : sb.toString();
    }

    private static StringBuilder initBuilderIfNeeded(StringBuilder builder, int len, String arg, int st) {
        if (null != builder) {
            // Already initialized
            return builder;
        }

        return st <= 0 ? new StringBuilder(len) : new StringBuilder(len).append(arg, 0, st);
    }

}
