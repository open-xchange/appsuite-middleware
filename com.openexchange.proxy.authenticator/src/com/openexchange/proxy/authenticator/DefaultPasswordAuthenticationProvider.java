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

package com.openexchange.proxy.authenticator;

import java.net.PasswordAuthentication;

/**
*
* {@link DefaultPasswordAuthenticationProvider} is a default implementation of the {@link PasswordAuthenticationProvider}
* which only return a {@link PasswordAuthentication} in case the given host and port match.
*
* @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
* @since v7.10.0
*/
public class DefaultPasswordAuthenticationProvider implements PasswordAuthenticationProvider {

   private final String host;
   private final int port;
   private final String user;
   private final String pw;
   private final String protocol;

   /**
    * Initializes a new {@link ProxyAuthenticatorActivator.DefaultPasswordAuthenticationProvider}.
    */
   public DefaultPasswordAuthenticationProvider(String protocol, String host, int port, String user, String password) {
       super();
       this.host = host;
       this.port = port;
       this.user = user;
       this.pw = password;
       this.protocol = protocol;
   }

   @Override
   public PasswordAuthentication getPasswordAuthentication(String requestingHost, int requestingPort) {
       if (port == requestingPort && host.equalsIgnoreCase(requestingHost)) {
           // Seems to be OK.
           return new PasswordAuthentication(user, pw.toCharArray());
       }

       // Does not match configured host/port
       return null;
   }

   @Override
   public String getProtocol() {
       return protocol;
   }

}
