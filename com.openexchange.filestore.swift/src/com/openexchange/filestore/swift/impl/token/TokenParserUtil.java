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

package com.openexchange.filestore.swift.impl.token;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.swift.SwiftExceptionCode;

/**
 * {@link TokenParserUtil} - Utility class for token parsing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class TokenParserUtil {

    /**
     * Initializes a new {@link TokenParserUtil}.
     */
    private TokenParserUtil() {
        super();
    }

    private static final SimpleDateFormat SDF_EXPIRES_WITH_MILLIS;
    private static final SimpleDateFormat SDF_EXPIRES_WITH_MILLIS2;
    private static final SimpleDateFormat SDF_EXPIRES_WITHOUT_MILLIS;
    static {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        SDF_EXPIRES_WITH_MILLIS = sdf;

        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        SDF_EXPIRES_WITH_MILLIS2 = sdf;

        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        SDF_EXPIRES_WITHOUT_MILLIS = sdf;
    }

    /**
     * Parses the specified string representation of the expiry date.
     *
     * @param sDate The string representation of the expiry date
     * @return The parsed date
     * @throws OXException If parsing the date fails
     */
    public static Date parseExpiryDate(String sDate) throws OXException {
        if (null == sDate) {
            return null;
        }

        try {
            SimpleDateFormat sdf;
            int pos = sDate.indexOf('.');
            if (pos > 0) {
                sdf = sDate.length() - pos == 8 ? SDF_EXPIRES_WITH_MILLIS2 : SDF_EXPIRES_WITH_MILLIS;
            } else {
                sdf = SDF_EXPIRES_WITHOUT_MILLIS;
            }
            synchronized (sdf) {
                return sdf.parse(sDate);
            }
        } catch (ParseException e) {
            throw SwiftExceptionCode.UNEXPECTED_ERROR.create(e, "Invalid date format: " + sDate);
        }
    }

}
