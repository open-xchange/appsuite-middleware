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

package com.openexchange.antivirus.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import com.openexchange.antivirus.AntiVirusResponseHeader;
import com.openexchange.antivirus.AntiVirusResult;
import com.openexchange.antivirus.exceptions.AntiVirusServiceExceptionCodes;
import com.openexchange.antivirus.impl.AntiVirusResultImpl.Builder;
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
                    builder.withInfected(Boolean.TRUE);
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
        AntiVirusResultImpl.Builder builder = AntiVirusResultImpl.builder();
        builder.withScanTimestamp(System.currentTimeMillis());
        builder.withStreamScanned(true);
        parseHeaders(response, builder);
        switch (response.getStatusCode()) {
            case 200:
                // Check the encapsulated response and override 'infected' flag if necessary.
                return builder.withInfected(Boolean.valueOf(!checkHttpResponse(response))).build();
            case 204:
                return builder.withInfected(Boolean.FALSE).build();
            case 418:
                throw AntiVirusServiceExceptionCodes.REMOTE_SERVER_ERROR.create(response.getStatusLine());
            case 500:
                throw AntiVirusServiceExceptionCodes.REMOTE_INTERNAL_SERVER_ERROR.create(response.getStatusLine());
            default:
                throw AntiVirusServiceExceptionCodes.UNEXPECTED_ERROR.create(String.format("Unexpected response code: %s - %s", I(response.getStatusCode()), response.getStatusLine()));
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
            case 418:
                throw AntiVirusServiceExceptionCodes.REMOTE_SERVER_ERROR.create(response.getStatusLine());
            case 500:
                throw AntiVirusServiceExceptionCodes.REMOTE_INTERNAL_SERVER_ERROR.create(response.getStatusLine());
            default:
                return true;
        }
    }
}
