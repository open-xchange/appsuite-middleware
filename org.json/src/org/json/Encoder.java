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

package org.json;

import com.fasterxml.jackson.core.io.JsonStringEncoder;

/**
 * {@link Encoder} - Helper class used for efficient encoding of JSON String values (including JSON field names) into Strings or UTF-8 byte
 * arrays.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Encoder {

    private static final Encoder INSTANCE = new Encoder();

    /**
     * Gets the instance
     * 
     * @return The instance
     */
    public static Encoder getInstance() {
        return INSTANCE;
    }

    private final JsonStringEncoder encoder;

    /**
     * Initializes a new {@link Encoder}.
     */
    private Encoder() {
        super();
        encoder = JsonStringEncoder.getInstance();
    }

    /**
     * Method that will quote text contents using JSON standard quoting, and return results as a character array
     */
    public char[] quoteAsString(String input) {
        return encoder.quoteAsString(input);
    }

    /**
     * Will quote given JSON String value using standard quoting, encode results as UTF-8, and return result as a byte array.
     */
    public byte[] quoteAsUTF8(String text) {
        return encoder.quoteAsUTF8(text);
    }

    /**
     * Will encode given String as UTF-8 (without any quoting), return resulting byte array.
     */
    public byte[] encodeAsUTF8(String text) {
        return encoder.encodeAsUTF8(text);
    }

}
