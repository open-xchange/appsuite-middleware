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

package com.openexchange.pop3;

import com.openexchange.exception.OXException;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.pop3.config.POP3Properties;
import com.openexchange.session.Session;

/**
 * {@link POP3Provider} - The provider for POP3 protocol.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3Provider extends MailProvider {

    /**
     * POP3 protocol.
     */
    public static final Protocol PROTOCOL_POP3 = new Protocol("pop3", "pop3s");

    private static final POP3Provider instance = new POP3Provider();

    /**
     * Gets the singleton instance of POP3 provider.
     *
     * @return The singleton instance of POP3 provider
     */
    public static POP3Provider getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link POP3Provider}.
     */
    private POP3Provider() {
        super();
    }

    @Override
    public MailAccess<?, ?> createNewMailAccess(final Session session) throws OXException {
        return POP3Access.newInstance(session);
    }

    @Override
    public MailAccess<?, ?> createNewMailAccess(final Session session, final int accountId) throws OXException {
        return POP3Access.newInstance(session, accountId);
    }

    @Override
    public Protocol getProtocol() {
        return PROTOCOL_POP3;
    }

    @Override
    protected AbstractProtocolProperties getProtocolProperties() {
        return POP3Properties.getInstance();
    }

    @Override
    protected String getSpamHandlerName() {
        return POP3Properties.getInstance().getSpamHandlerName();
    }

}
