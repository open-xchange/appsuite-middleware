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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mailaccount;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.session.Session;

/**
 * {@link CredentialsProviderRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class CredentialsProviderRegistry {

    private static final CredentialsProviderRegistry INSTANCE = new CredentialsProviderRegistry();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static CredentialsProviderRegistry getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------

    private final AtomicReference<ServiceListing<CredentialsProviderService>> providers;

    /**
     * Initializes a new {@link CredentialsProviderRegistry}.
     */
    private CredentialsProviderRegistry() {
        super();
        providers = new AtomicReference<ServiceListing<CredentialsProviderService>>(null);
    }

    /**
     * Determines the appropriate credentials provider for specified arguments.
     *
     * @param forMailAccess <code>true</code> if credentials are supposed to be determined for mail access; otherwise <code>false</code> for transport
     * @param accountId The account identifier
     * @param session The session
     * @return The appropriate credentials provider or <code>null</code>
     * @throws OXException If appropriate credentials provider cannot be returned due to an error
     */
    public CredentialsProviderService optCredentialsProviderFor(boolean forMailAccess, int accountId, Session session) throws OXException {
        ServiceListing<CredentialsProviderService> serviceListing = providers.get();
        if (null == serviceListing) {
            return null;
        }

        for (CredentialsProviderService credentialsProvider : serviceListing) {
            if (credentialsProvider.isApplicableFor(forMailAccess, accountId, session)) {
                return credentialsProvider;
            }
        }
        return null;
    }

    /**
     * Applies given service listing.
     *
     * @param listing The service listing
     */
    public void applyListing(ServiceListing<CredentialsProviderService> listing) {
        providers.set(listing);
    }

}
