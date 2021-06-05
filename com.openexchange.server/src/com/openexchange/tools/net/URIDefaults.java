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
