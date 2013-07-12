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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mail.autoconfig.internal;

import java.util.LinkedList;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.idn.IDNA;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.AutoconfigException;
import com.openexchange.mail.autoconfig.AutoconfigService;
import com.openexchange.mail.autoconfig.sources.ConfigServer;
import com.openexchange.mail.autoconfig.sources.ConfigSource;
import com.openexchange.mail.autoconfig.sources.ConfigurationFile;
import com.openexchange.mail.autoconfig.sources.Guess;
import com.openexchange.mail.autoconfig.sources.ISPDB;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AutoconfigServiceImpl}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AutoconfigServiceImpl implements AutoconfigService {

    private final List<ConfigSource> sources;

    public AutoconfigServiceImpl(final ServiceLookup services) {
        sources = new LinkedList<ConfigSource>();
        sources.add(new ConfigurationFile(services));
        sources.add(new ConfigServer());
        sources.add(new ISPDB(services));
        // Deactivated because of Bug 27473
        // sources.add(new Database(services));
        sources.add(new Guess());
    }

    @Override
    public Autoconfig getConfig(final String email, final String password, final User user, final Context context) throws OXException {
        QuotedInternetAddress internetAddress;
        try {
            internetAddress = new QuotedInternetAddress(email);
        } catch (final AddressException e) {
            throw AutoconfigException.invalidMail(email);
        }

        String mailLocalPart = null;
        String mailDomain = null;
        try {
            mailLocalPart = getLocalPart(internetAddress);
            mailDomain = getDomain(internetAddress);
        } catch (final ArrayIndexOutOfBoundsException e) {
            return null;
        }

        for (final ConfigSource source : sources) {
            final Autoconfig config = source.getAutoconfig(mailLocalPart, mailDomain, password, user, context);
            if (config != null) {
                return config;
            }
        }

        return null;
    }

    private String getDomain(final QuotedInternetAddress internetAddress) {
        return IDNA.toIDN(internetAddress.getAddress()).split("@")[1];
    }

    private String getLocalPart(final QuotedInternetAddress internetAddress) {
        return internetAddress.getAddress().split("@")[0];
    }

}
