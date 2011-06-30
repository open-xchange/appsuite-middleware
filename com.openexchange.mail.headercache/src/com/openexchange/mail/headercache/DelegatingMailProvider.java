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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail.headercache;

import com.openexchange.mail.MailException;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.SpamHandler;

/**
 * {@link DelegatingMailProvider}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DelegatingMailProvider extends MailProvider {

    private final MailProvider mailProvider;

    /**
     * Initializes a new {@link DelegatingMailProvider}.
     * 
     * @param mailProvider The mail provider to delegate to
     */
    public DelegatingMailProvider(final MailProvider mailProvider) {
        super();
        this.mailProvider = mailProvider;
    }

    @Override
    public MailAccess<?, ?> createNewMailAccess(final Session session, final int accountId) throws MailException {
        return new HeaderCacheMailAccess(session, accountId, mailProvider.createNewMailAccess(session, accountId));
    }

    @Override
    public MailAccess<?, ?> createNewMailAccess(final Session session) throws MailException {
        return new HeaderCacheMailAccess(session, MailAccount.DEFAULT_ID, mailProvider.createNewMailAccess(session, MailAccount.DEFAULT_ID));
    }

    @Override
    public MailPermission createNewMailPermission() {
        return mailProvider.createNewMailPermission();
    }

    @Override
    public Protocol getProtocol() {
        return mailProvider.getProtocol();
    }

    @Override
    public boolean isDeprecated() {
        return mailProvider.isDeprecated();
    }

    @Override
    public SpamHandler getSpamHandler() {
        return mailProvider.getSpamHandler();
    }

    @Override
    public void setDeprecated(final boolean deprecated) {
        mailProvider.setDeprecated(deprecated);
    }

    @Override
    public void shutDown() throws MailException {
        mailProvider.shutDown();
    }

    @Override
    public void startUp() throws MailException {
        mailProvider.startUp();
    }

    @Override
    public String toString() {
        return mailProvider.toString();
    }

    @Override
    protected AbstractProtocolProperties getProtocolProperties() {
        return null;
    }

    @Override
    public AbstractProtocolProperties getProtocolProps() {
        return mailProvider.getProtocolProps();
    }

}
