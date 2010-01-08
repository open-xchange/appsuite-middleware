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
import com.openexchange.mail.api.AllMailProvider;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.session.Session;

/**
 * {@link HeaderCacheProvider} - The provider for header cache protocol.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HeaderCacheProvider extends AllMailProvider {

    /**
     * Header cache protocol.
     */
    public static final Protocol PROTOCOL_HEADER_CACHE = Protocol.PROTOCOL_ALL;

    private static final HeaderCacheProvider instance = new HeaderCacheProvider();

    /**
     * Gets the singleton instance of header cache provider.
     * 
     * @return The singleton instance of header cache provider
     */
    public static HeaderCacheProvider getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link HeaderCacheProvider}.
     */
    private HeaderCacheProvider() {
        super();
    }

    @Override
    public MailProvider getDelegatingProvider(final MailProvider realProvider) {
        return new DelegatingMailProvider(realProvider);
    }

    @Override
    public MailAccess<?, ?> createNewMailAccess(final Session session) throws MailException {
        if (null == session) {
            // A dummy access for start-up/shut-down purpose
            return null;
        }
        throw new UnsupportedOperationException("HeaderCacheProvider.createNewMailAccess()");
    }

    @Override
    public MailAccess<?, ?> createNewMailAccess(final Session session, final int accountId) throws MailException {
        if (null == session) {
            // A dummy access for start-up/shut-down purpose
            return null;
        }
        throw new UnsupportedOperationException("HeaderCacheProvider.createNewMailAccess()");
    }

    @Override
    public MailPermission createNewMailPermission() {
        throw new UnsupportedOperationException("HeaderCacheProvider.createNewMailPermission()");
    }

    @Override
    public Protocol getProtocol() {
        return PROTOCOL_HEADER_CACHE;
    }

    @Override
    protected AbstractProtocolProperties getProtocolProperties() {
        return HeaderCacheStaticProperties.getInstance();
    }

    @Override
    protected String getSpamHandlerName() {
        throw new UnsupportedOperationException("HeaderCacheProvider.getSpamHandlerName()");
    }

}
