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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.antivirus.impl.impl;

import java.util.List;
import com.openexchange.antivirus.AntiVirusResponseHeader;
import com.openexchange.antivirus.AntiVirusResult;
import com.openexchange.antivirus.exceptions.AntiVirusServiceExceptionCodes;
import com.openexchange.antivirus.impl.impl.AntiVirusResultImpl.Builder;
import com.openexchange.exception.OXException;
import com.openexchange.icap.ICAPResponse;
import com.openexchange.icap.header.ICAPResponseHeader;
import com.openexchange.java.Strings;

/**
 * {@link ICAPResponseParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
class ICAPResponseParser {

    private enum HeaderParser {
        /**
         * Parses the {@link ICAPResponseHeader#ISTAG}
         */
        ISTAG() {

            @Override
            void parseHeader(ICAPResponse response, Builder builder) {
                builder.withIsTag(response.getHeader(ICAPResponseHeader.ISTAG));
            }
        },

        /**
         * Parses the {@link AntiVirusResponseHeader#X_INFECTION_FOUND}
         */
        X_INFECTION_FOUND() {

            @Override
            void parseHeader(ICAPResponse response, Builder builder) {
                String header = response.getHeader(AntiVirusResponseHeader.X_INFECTION_FOUND);
                if (Strings.isEmpty(header)) {
                    return;
                }
                String[] split = Strings.splitBySemiColon(header);
                for (String s : split) {
                    List<String> kv = Strings.splitAndTrim(s, "=");
                    if (kv.size() != 2) {
                        continue;
                    }
                    if (!kv.get(0).equals("Threat")) {
                        continue;
                    }
                    builder.withThreatName(kv.get(1));
                    builder.withInfected(true);
                    return;
                }
            }
        };

        /**
         * Parses the designated header from the specified {@link ICAPResponse} to
         * the specified {@link AntiVirusResultImpl}
         * 
         * @param response The {@link ICAPResponse}
         * @param resultBuilder The {@link Builder}
         */
        abstract void parseHeader(ICAPResponse response, Builder resultBuilder);
    }

    /**
     * Initialises a new {@link ICAPResponseParser}.
     */
    ICAPResponseParser() {
        super();
    }

    /**
     * Parses the specified {@link ICAPResponse} and returns the {@link AntiVirusResult}
     * 
     * @param response The {@link ICAPResponse} to parse
     * @return The {@link AntiVirusResult} to return
     * @throws OXException if a remote server error occurs
     */
    AntiVirusResult parse(ICAPResponse response) throws OXException {
        Builder builder = new AntiVirusResultImpl.Builder();
        builder.withScanTimestamp(System.currentTimeMillis());
        builder.withStreamScanned(true);
        parseHeaders(response, builder);
        switch (response.getStatusCode()) {
            case 200:
                // Check the encapsulated response and override 'infected' flag if necessary.
                return builder.withInfected(Boolean.valueOf(!checkHttpResponse(response))).build();
            case 204:
                return builder.withInfected(Boolean.FALSE).build();
            case 500:
                throw AntiVirusServiceExceptionCodes.REMOTE_INTERNAL_SERVER_ERROR.create(response.getStatusLine());
            default:
                return builder.build();
        }
    }

    /**
     * Parses the headers of the response
     * 
     * @param response The {@link ICAPResponse} to parse its headers
     * @param builder The {@link Builder} to parse the headers to
     */
    private void parseHeaders(ICAPResponse response, Builder builder) {
        for (HeaderParser parser : HeaderParser.values()) {
            parser.parseHeader(response, builder);
        }
    }

    /**
     * Checks the encapsulated HTTP response with in the specified {@link ICAPResponse}
     * 
     * @param response The {@link ICAPResponse}
     * @return <code>true</code> if no threats found, <code>false</code> otherwise
     * @throws OXException if a remote server error had occurs
     */
    private boolean checkHttpResponse(ICAPResponse response) throws OXException {
        switch (response.getEncapsulatedStatusCode()) {
            case 200:
                return true;
            case 403:
                return false;
            case 500:
                throw AntiVirusServiceExceptionCodes.REMOTE_INTERNAL_SERVER_ERROR.create(response.getStatusLine());
            default:
                return true;
        }
    }
}
