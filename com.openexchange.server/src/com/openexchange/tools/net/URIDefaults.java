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

package com.openexchange.tools.net;

/**
 * This interface defines the defaults for the {@link URIParser}. If no protocol or port is specified in the input, this defaults are used.
 * The non SSL defaults are always preferred. The SSL defaults only help in determining the protocol or the port if the other part is parsed.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface URIDefaults {

    String getProtocol();

    String getSSLProtocol();

    int getPort();

    int getSSLPort();

    static URIDefaults IMAP = new URIDefaults() {
        @Override
        public String getProtocol() {
            return "imap";
        }
        @Override
        public String getSSLProtocol() {
            return "imaps";
        }
        @Override
        public int getPort() {
            return 143;
        }
        @Override
        public int getSSLPort() {
            return 993;
        }
    };

    static URIDefaults NULL = new URIDefaults() {
        @Override
        public String getProtocol() {
            return null;
        }
        @Override
        public String getSSLProtocol() {
            return null;
        }
        @Override
        public int getPort() {
            return -1;
        }
        @Override
        public int getSSLPort() {
            return -1;
        }
    };

    static URIDefaults SMTP = new URIDefaults() {
        @Override
        public String getProtocol() {
            return "smtp";
        }
        @Override
        public String getSSLProtocol() {
            return "smtps";
        }
        @Override
        public int getPort() {
            return 25;
        }
        @Override
        public int getSSLPort() {
            return 465;
        }
    };

    static URIDefaults POP3 = new URIDefaults() {

        @Override
        public String getProtocol() {
            return "pop3";
        }

        @Override
        public String getSSLProtocol() {
            return "pop3s";
        }

        @Override
        public int getPort() {
            return 110;
        }

        @Override
        public int getSSLPort() {
            return 995;
        }
    };

}
