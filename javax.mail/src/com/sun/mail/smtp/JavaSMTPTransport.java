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

package com.sun.mail.smtp;

import java.io.OutputStream;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;

/**
 * {@link JavaSMTPTransport}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class JavaSMTPTransport extends SMTPTransport {

    /**
     * Initializes a new {@link JavaSMTPTransport}.
     *
     * @param   session the Session
     * @param   urlname the URLName of this transport
     * @param   name    the protocol name of this transport
     * @param   isSSL   use SSL to connect?
     */
    public JavaSMTPTransport(Session session, URLName urlname, String name, boolean isSSL) {
        super(session, urlname, name, isSSL);
    }

    /**
     * Initializes a new {@link JavaSMTPTransport}.
     *
     * @param   session the Session
     * @param   urlname the URLName of this transport
     */
    public JavaSMTPTransport(Session session, URLName urlname) {
        super(session, urlname);
    }

    @Override
    protected OutputStream data() throws MessagingException {
        OutputStream data = super.data();

        String sMaxMailSize = session.getProperty("com.openexchange.mail.maxMailSize");
        if (sMaxMailSize != null) {
            try {
                long maxMailSize = Long.parseLong(sMaxMailSize);
                if (maxMailSize > 0) {
                    data = new CountingOutputStream(data, maxMailSize);
                }
            } catch (NumberFormatException e) {
                // Not a parseable number...
            }
        }

        return data;
    }
}
