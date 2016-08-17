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

package com.openexchange.mail.autoconfig.sources;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.mail.autoconfig.Autoconfig;

/**
 * {@link OutlookComConfigSource} - The static config source for <code>outlook.com</code>.
 * <p>
 * See <a href="http://windows.microsoft.com/en-US/windows/outlook/send-receive-from-app">http://windows.microsoft.com/en-US/windows/outlook/send-receive-from-app<a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.0
 */
public class OutlookComConfigSource extends StaticConfigSource {

    /**
     * Initializes a new {@link OutlookComConfigSource}.
     */
    public OutlookComConfigSource() {
        super(new DomainFilter() {

            @Override
            public boolean accept(final String emailDomain) {
                return null != emailDomain && "outlook.com".equals(Strings.toLowerCase(emailDomain.trim()));
            }
        });
    }

    @Override
    protected Autoconfig getStaticAutoconfig(final String emailLocalPart, final String emailDomain, final String password, final User user, final Context context, boolean forceSecure) throws OXException {
        final Autoconfig autoconfig = new Autoconfig();
        // IMAP
        autoconfig.setMailPort(993);
        autoconfig.setMailProtocol("imap");
        autoconfig.setMailSecure(true);
        autoconfig.setMailStartTls(forceSecure);
        autoconfig.setMailServer("imap-mail.outlook.com");
        // Transport
        autoconfig.setTransportPort(25);
        autoconfig.setTransportProtocol("smtp");
        autoconfig.setTransportSecure(false);
        autoconfig.setTransportStartTls(forceSecure);
        autoconfig.setTransportServer("smtp-mail.outlook.com");
        autoconfig.setUsername(emailLocalPart + '@' + emailDomain);
        return autoconfig;
    }

}
