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

package com.openexchange.mail.autoconfig.sources.staticsource;

import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.autoconfig.DefaultAutoconfig;

/**
 * {@link GmxConfigSource} - The static config source for <code>gmx.com</code> and <code>gmx.de</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.10.5
 */
public class GmxConfigSource extends AbstractStaticConfigSource {

    /**
     * Initializes a new {@link GmxConfigSource}.
     */
    GmxConfigSource() {
        super();
    }

    @Override
    protected boolean accept(String emailDomain) {
        if (Strings.isEmpty(emailDomain)) {
            return false;
        }

        String lcd = Strings.toLowerCase(emailDomain.trim());
        return "gmx.com".equals(lcd) || "gmx.de".equals(lcd);
    }

    @Override
    protected DefaultAutoconfig getStaticAutoconfig(String emailLocalPart, String emailDomain, String password, int userId, int contextId, boolean forceSecure) throws OXException {
        DefaultAutoconfig autoconfig = new DefaultAutoconfig();

        String lcd = Strings.toLowerCase(emailDomain.trim());
        if ("gmx.com".equals(lcd)) {
            // IMAP
            autoconfig.setMailPort(993);
            autoconfig.setMailProtocol("imap");
            autoconfig.setMailSecure(true);
            autoconfig.setMailStartTls(forceSecure);
            autoconfig.setMailServer("imap.gmx.com");
            // Transport
            autoconfig.setTransportPort(587);
            autoconfig.setTransportProtocol("smtp");
            autoconfig.setTransportSecure(false);
            autoconfig.setTransportStartTls(true);
            autoconfig.setTransportServer("mail.gmx.com");
        } else if ("gmx.de".equals(lcd)) {
            // IMAP
            autoconfig.setMailPort(993);
            autoconfig.setMailProtocol("imap");
            autoconfig.setMailSecure(true);
            autoconfig.setMailStartTls(forceSecure);
            autoconfig.setMailServer("imap.gmx.net");
            // Transport
            autoconfig.setTransportPort(587);
            autoconfig.setTransportProtocol("smtp");
            autoconfig.setTransportSecure(false);
            autoconfig.setTransportStartTls(true);
            autoconfig.setTransportServer("mail.gmx.net");
        } else {
            throw new IllegalStateException("Unsupported domain: " + emailDomain);
        }

        autoconfig.setUsername(emailLocalPart + '@' + emailDomain);
        return autoconfig;
    }

}
