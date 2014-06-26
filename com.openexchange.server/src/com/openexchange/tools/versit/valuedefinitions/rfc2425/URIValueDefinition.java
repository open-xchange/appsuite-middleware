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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.tools.versit.valuedefinitions.rfc2425;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.regex.Pattern;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.StringScanner;
import com.openexchange.tools.versit.ValueDefinition;
import com.openexchange.tools.versit.VersitException;

/**
 * @author Viktor Pracht
 */
public class URIValueDefinition extends ValueDefinition {

    public static final URIValueDefinition Default = new URIValueDefinition();

    private static final Pattern URIPattern = Pattern.compile("[^,]+");

    /** Turn off URI validation (bug #23046) */
    private static final boolean RFC_2396_CONFORMANCE = false;

    @Override
    public Object createValue(final StringScanner s, final Property property) throws IOException {

        if (s.length() == 0) {
            // Return null on empty URL
            return null;
        }

        StringScanner scanner = deescapeColons(s);
        //StringScanner scanner = s;

        final String value = scanner.regex(URIPattern);
        if (value == null) {
            throw new VersitException(scanner, "URI expected");
        }
        try {
            return new URI(value);
        } catch (final URISyntaxException e) {
            if (RFC_2396_CONFORMANCE) {
                final VersitException ve = new VersitException(scanner, e.getMessage());
                ve.initCause(e);
                throw ve;
            } else {
                return value; // fallback
            }
        }
    }

    private StringScanner deescapeColons(StringScanner s) {
        String str = s.getRest();
        StringBuilder strBuilder = new StringBuilder();
        StringScanner result;

        if (str.contains("\\:")) {
            StringCharacterIterator it = new StringCharacterIterator(str);
            char c = it.current();
            while (c != CharacterIterator.DONE) {
                if (c == '\\') {
                    if ((c = it.next()) == ':') {
                        strBuilder.append(':');
                        c = it.next();
                    } else {
                        if (c == CharacterIterator.DONE) {
                            strBuilder.append('\\');
                        } else {
                            strBuilder.append('\\');
                            strBuilder.append(c);
                            c = it.next();
                        }
                    }
                } else {
                    strBuilder.append(c);
                    c = it.next();
                }

            }
            str = strBuilder.toString();
        }
        result = new StringScanner(s.getScanner(), str);
        return result;
    }

}
