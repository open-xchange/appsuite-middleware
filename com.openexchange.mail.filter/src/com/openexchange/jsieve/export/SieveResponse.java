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
 *    trademarks of the OX Software GmbH. group of companies.
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
package com.openexchange.jsieve.export;


/**
 * @author choeger
 *
 */
public class SieveResponse {

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

        private String message;

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
         * @return the message
         */
        public final String getMessage() {
            return message;
        }

        /**
         * @param message the message to set
         */
        public final void setMessage(String message) {
            this.message = message;
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
                if( respCode.startsWith(codeStr) ) {
                    return code;
                }
            }
            return UNKNOWN;
        }

        public static boolean isKnownCode(final String respCode) {
            for(final Code code : Code.values() ) {
                final String codeStr = code.toString();
                if( respCode.startsWith(codeStr) ) {
                    return true;
                }
            }
            return false;
        }
    }
}
