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
 * {@link GMailConfigSource} - The static config source for <code>gmail.com</code>.
 * <p>
 * See <a href="https://support.google.com/mail/answer/7126229">https://support.google.com/mail/answer/7126229<a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.10.5
 */
public class GMailConfigSource extends AbstractStaticConfigSource {

    /**
     * Initializes a new {@link GMailConfigSource}.
     */
    GMailConfigSource() {
        super();
    }

    @Override
    protected boolean accept(String emailDomain) {
        if (Strings.isEmpty(emailDomain)) {
            return false;
        }

        String lcd = Strings.toLowerCase(emailDomain.trim());
        return "gmail.com".equals(lcd) || "googlemail.com".equals(lcd);
    }

    @Override
    protected DefaultAutoconfig getStaticAutoconfig(String emailLocalPart, String emailDomain, String password, int userId, int contextId, boolean forceSecure) throws OXException {
        final DefaultAutoconfig autoconfig = new DefaultAutoconfig();
        // IMAP
        autoconfig.setMailPort(993);
        autoconfig.setMailProtocol("imap");
        autoconfig.setMailSecure(true);
        autoconfig.setMailStartTls(forceSecure);
        autoconfig.setMailServer("imap.gmail.com");
        // Transport
        autoconfig.setTransportPort(465);
        autoconfig.setTransportProtocol("smtp");
        autoconfig.setTransportSecure(true);
        autoconfig.setTransportStartTls(forceSecure);
        autoconfig.setTransportServer("smtp.gmail.com");
        autoconfig.setUsername(emailLocalPart + '@' + emailDomain);
        return autoconfig;
    }

}
