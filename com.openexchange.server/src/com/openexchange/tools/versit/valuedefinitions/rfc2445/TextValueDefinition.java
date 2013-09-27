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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.tools.versit.valuedefinitions.rfc2445;

import java.io.IOException;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.StringScanner;
import com.openexchange.tools.versit.ValueDefinition;
import com.openexchange.tools.versit.VersitException;

public class TextValueDefinition extends ValueDefinition {

    public static final ValueDefinition Default = new TextValueDefinition();

    @Override
    public Object createValue(final StringScanner s, final Property property) throws IOException {
        final StringBuilder sb = new StringBuilder();
        while (s.peek >= 0 && s.peek != ',' && s.peek != ';') {
            if (s.peek == '\\') {
                s.read();
                switch (s.peek) {
                case '\\':
                case ';':
                case ',':
                case ':':
                    sb.append((char) s.read());
                    break;
                case 'n':
                case 'N':
                    s.read();
                    sb.append('\n');
                    break;
                default:
                    throw new VersitException(s, "Invalid ecape sequence");
                }
            } else {
                sb.append((char) s.read());
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    @Override
    public String writeValue(final Object value) {
        final String str = (String) value;
        final int length = str.length();
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = str.charAt(i);
            switch (c) {
            case '\r':
                if (i+1 < length && str.charAt(i + 1) == '\n') {
                    i++;
                }
                // no break;
            case '\n':
                sb.append("\\n");
                break;
            case '\\':
            case ';':
            case ',':
                sb.append('\\');
                // no break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
