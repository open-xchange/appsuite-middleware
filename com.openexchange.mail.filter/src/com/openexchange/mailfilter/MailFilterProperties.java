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
package com.openexchange.mailfilter;

import java.util.Map;
import com.google.common.collect.ImmutableMap;

public class MailFilterProperties {

    public static enum LoginTypes {
        GLOBAL("global"),
        USER("user");

        public final String name;

        private LoginTypes(final String name) {
            this.name = name;
        }
    }

    public static enum Values {
        SIEVE_CREDSRC("SIEVE_CREDSRC", CredSrc.SESSION.name),
        SIEVE_LOGIN_TYPE("SIEVE_LOGIN_TYPE", LoginTypes.GLOBAL.name),
        SIEVE_SERVER("SIEVE_SERVER", "localhost"),
        SIEVE_PORT("SIEVE_PORT", "2000"),
        SCRIPT_NAME("SCRIPT_NAME", "Open-Xchange"),
        SIEVE_AUTH_ENC("SIEVE_AUTH_ENC", "UTF-8"),
        NON_RFC_COMPLIANT_TLS_REGEX("NON_RFC_COMPLIANT_TLS_REGEX", "^Cyrus.*v([0-1]\\.[0-9].*|2\\.[0-2].*|2\\.3\\.[0-9]|2\\.3\\.[0-9][^0-9].*)$"),
        TLS("TLS", "true"),
        VACATION_DOMAINS("VACATION_DOMAINS", ""),
        SIEVE_CONNECTION_TIMEOUT("com.openexchange.mail.filter.connectionTimeout", "30000"),
        SIEVE_PASSWORDSRC("com.openexchange.mail.filter.passwordSource", PasswordSource.SESSION.name),
        SIEVE_MASTERPASSWORD("com.openexchange.mail.filter.masterPassword", ""),
        USE_UTF7_FOLDER_ENCODING("com.openexchange.mail.filter.useUTF7FolderEncoding", "false"),
        PUNYCODE("com.openexchange.mail.filter.punycode", "false"),
        USE_SIEVE_RESPONSE_CODES("com.openexchange.mail.filter.useSIEVEResponseCodes","false");

        public final String property;

        public final String def;

        private Values(final String property, final String def) {
            this.property = property;
            this.def = def;
        }

    }

    public static enum CredSrc {
        SESSION_FULL_LOGIN("session-full-login"),
        SESSION("session"),
        IMAP_LOGIN("imapLogin"),
        MAIL("mail");

        public final String name;

        private CredSrc(final String name) {
            this.name = name;
        }

        private static final Map<String, CredSrc> MAP;
        static {
            ImmutableMap.Builder<String, CredSrc> b = ImmutableMap.builder();
            for (CredSrc credSrc : CredSrc.values()) {
                b.put(credSrc.name, credSrc);
            }
            MAP = b.build();
        }

        public static CredSrc credSrcFor(String name) {
            return null == name ? null : MAP.get(name);
        }
    }

    public static enum PasswordSource {
        SESSION("session"),
        GLOBAL("global");

        public final String name;

        private PasswordSource(final String name) {
            this.name = name;
        }

        private static final Map<String, PasswordSource> MAP;
        static {
            ImmutableMap.Builder<String, PasswordSource> b = ImmutableMap.builder();
            for (PasswordSource pwSrc : PasswordSource.values()) {
                b.put(pwSrc.name, pwSrc);
            }
            MAP = b.build();
        }

        public static PasswordSource passwordSourceFor(String name) {
            return null == name ? null : MAP.get(name);
        }
    }

}
