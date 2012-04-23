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

package com.openexchange.mail.smal.impl;

import com.openexchange.exception.OXException;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.api.AllMailProvider;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;

/**
 * {@link SmalProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SmalProvider extends AllMailProvider {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(SmalProvider.class));

    /**
     * SMAL protocol.
     */
    public static final Protocol PROTOCOL_SMAL = Protocol.PROTOCOL_ALL;

    private static final SmalProvider instance = new SmalProvider();

    /**
     * Gets the singleton instance of SMAL provider.
     *
     * @return The singleton instance of SMAL provider
     */
    public static SmalProvider getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link SmalProvider}.
     */
    private SmalProvider() {
        super();
    }

    @Override
    public MailProvider getDelegatingProvider(final MailProvider realProvider) {
        return this;
    }

    @Override
    public Protocol getProtocol() {
        return PROTOCOL_SMAL;
    }

    @Override
    public MailAccess<?, ?> createNewMailAccess(final Session session) throws OXException {
        return createNewMailAccess(session, MailAccount.DEFAULT_ID);
    }

    @Override
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> createNewMailAccess(final Session session, final int accountId) throws OXException {
        if (null == session) {
            /*
             * For initialization purpose
             */
            return new SmalMailAccess(null, accountId);
        }
        return new SmalMailAccess(session, accountId);
    }

    @Override
    protected AbstractProtocolProperties getProtocolProperties() {
        return SmalStaticProperties.getInstance();
    }

    @Override
    public MailPermission createNewMailPermission(final Session session, final int accountId) {
        try {
            return SmalMailProviderRegistry.getMailProviderBySession(session, accountId).createNewMailPermission(session, accountId);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            return new DefaultMailPermission();
        }
    }

    @Override
    public String toString() {
        return PROTOCOL_SMAL.getName();
    }

}
