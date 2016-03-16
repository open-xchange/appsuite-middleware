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

package com.openexchange.tools.encoding;


/**
 * QuotedPrintable
 *
 * @author <a href="mailto:martin.kauss@open-xchange.com">Martin Kauss</a>
 */

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
            final byte b[] = s.getBytes();

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
        } catch (final Exception exc) {
            LOG.error("encode error: {}", exc, exc);
        }

        return sb.toString();
    }

    public static String decode(final String s) {
        final StringBuilder sb = new StringBuilder();

        int i = 0;

        String x = "";

        try {
            final byte b[] = s.getBytes();

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
        } catch (final Exception exc) {
            LOG.error("", exc);
        }

        return sb.toString();
    }
}
