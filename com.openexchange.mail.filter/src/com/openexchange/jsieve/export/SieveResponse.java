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
package com.openexchange.jsieve.export;


/**
 * @author choeger
 *
 */
public class SieveResponse {

    private final String message;
    private final Code code;

    /**
     * Initializes a new {@link SieveResponse}.
     */
    public SieveResponse(Code code, String message) {
        super();
        this.code = code;
        this.message = message;
    }

    /**
     * @return the message
     */
    public final String getMessage() {
        return message;
    }

    /**
     * Gets the code
     *
     * @return The code
     */
    public Code getCode() {
        return code;
    }

    public static enum Code {
        /*
         * https://tools.ietf.org/html/rfc5804#section-1.3
         */
        AUTH_TOO_WEAK("AUTH-TOO-WEAK",1),
        ENCRYPT_NEEDED("ENCRYPT-NEEDED",2),
        QUOTA("QUOTA",3),
        REFERRAL("REFERRAL",4),
        SASL("SASL",5),
        TRANSITION_NEEDED("TRANSITION-NEEDED",6),
        TRYLATER("TRYLATER",7),
        ACTIVE("ACTIVE",8),
        NONEXISTENT("NONEXISTENT",9),
        ALREADYEXISTS("ALREADYEXISTS",10),
        TAG("TAG",11),
        WARNINGS("WARNINGS",12),
        UNKNOWN("UNKNOWN",99);

        private final String sieveCode;

        private final int detailnumber;

        private Code(final String sieveCode, final int detailNumber) {
            this.sieveCode = sieveCode;
            this.detailnumber = detailNumber;
        }

        /**
         * @return the sieveCode
         */
        public final String getSieveCode() {
            return sieveCode;
        }

        /**
         * @return the detailnumber
         */
        public final int getDetailnumber() {
            return detailnumber;
        }

        public static Code getCode(final String respCode) {
            for(final Code code : Code.values() ) {
                final String codeStr = code.toString();
                if ( respCode.startsWith(codeStr) ) {
                    return code;
                }
            }
            return UNKNOWN;
        }

        public static boolean isKnownCode(final String respCode) {
            for(final Code code : Code.values() ) {
                final String codeStr = code.toString();
                if ( respCode.startsWith(codeStr) ) {
                    return true;
                }
            }
            return false;
        }
    }
}
