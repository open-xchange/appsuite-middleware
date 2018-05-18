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

package com.openexchange.ajax.chronos;

import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.exception.OXException;
import com.openexchange.test.pool.TestUser;

/**
 * {@link AbstractEnhancedApiClientSession}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public abstract class AbstractEnhancedApiClientSession extends AbstractConfigAwareAPIClientSession {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractEnhancedApiClientSession.class);

    private EnhancedApiClient enhancedApiClient;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        enhancedApiClient = generateEnhancedClient(testUser);
        rememberClient(enhancedApiClient);
    }

    /**
     * Gets the enhancedApiClient
     *
     * @return The enhancedApiClient
     */
    public EnhancedApiClient getEnhancedApiClient() {
        return enhancedApiClient;
    }

    /**
     * Sets the enhancedApiClient
     *
     * @param enhancedApiClient The enhancedApiClient to set
     */
    public void setEnhancedApiClient(EnhancedApiClient enhancedApiClient) {
        this.enhancedApiClient = enhancedApiClient;
    }

    /**
     * Generates a new {@link EnhancedApiClient} for the {@link TestUser}.
     * Generated client needs a <b>logout in tearDown()</b>
     *
     * @param client The client identifier to use when performing a login
     * @param user The {@link TestUser} to create a client for
     * @return The new {@link EnhancedApiClient}
     * @throws OXException In case no client could be created
     */
    protected final EnhancedApiClient generateEnhancedClient(TestUser user) throws OXException {
        if (null == user) {
            LOG.error("Can only create a client for an valid user");
            throw new OXException();
        }
        EnhancedApiClient newClient;
        try {
            newClient = new EnhancedApiClient();
            setBasePath(newClient);
            newClient.setUserAgent("ox-test-client");
            newClient.login(user.getLogin(), user.getPassword());
        } catch (Exception e) {
            LOG.error("Could not generate new client for user {} in context {} ", user.getUser(), user.getContext());
            throw new OXException();
        }
        return newClient;
    }

}
