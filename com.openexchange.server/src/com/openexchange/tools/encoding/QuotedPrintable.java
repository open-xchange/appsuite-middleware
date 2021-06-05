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

package com.openexchange.tools.encoding;

import java.nio.charset.StandardCharsets;

/**
 * QuotedPrintable
 *
 * @author <a href="mailto:martin.kauss@open-xchange.com">Martin Kauss</a>
 * @deprecated DOn't use this class
 */
@Deprecated
public final class QuotedPrintable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(QuotedPrintable.class);

    private QuotedPrintable() {
        super();
    }

    public static String encode(final String s) {

        final StringBuilder sb = new StringBuilder();

        int i = 0;
        String x = "";

        try {
            final byte b[] = s.getBytes(StandardCharsets.ISO_8859_1);

            for (int a = 0; a < b.length; a++) {
                final int unsignedInt = (0xff & b[a]);
                if ((unsignedInt >= 32) && (unsignedInt <= 127) && (unsignedInt != 61)) {
                    sb.append((char) b[a]);
                } else {
                    i = b[a];
                    if (i < 0) {
                        i = i + 256;
                    }

                    x = Integer.toString(i, 16).toUpperCase();

                    if (x.length() == 1) {
                        x = '0' + x;
                    }

                    sb.append('=').append(x);
                }
            }
        } catch (Exception exc) {
            LOG.error("encode error: {}", exc, exc);
        }

        return sb.toString();
    }

    public static String decode(final String s) {
        final StringBuilder sb = new StringBuilder();

        int i = 0;

        String x = "";

        try {
            final byte b[] = s.getBytes(StandardCharsets.ISO_8859_1);

            for (int a = 0; a < b.length; a++) {
                if (b[a] == 61) {
                    if ((a + 2) < b.length) {
                        x = (new StringBuilder().append((char) b[a + 1]).append((char) b[a + 2]).toString());

                        i = Integer.parseInt(x, 16);

                        sb.append((char) i);
                        a = a + 2;
                    }
                } else {
                    sb.append((char) b[a]);
                }
            }
        } catch (Exception exc) {
            LOG.error("", exc);
        }

        return sb.toString();
    }
}
