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

package com.openexchange.contact.provider;

import java.util.EnumSet;
import java.util.Locale;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

/**
 * {@link ContactsProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public interface ContactsProvider {

    /**
     * Gets the identifier of the contact provider.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Gets the provider's display name.
     *
     * @param locale The current locale to get the display name in
     * @return The display name
     */
    String getDisplayName(Locale locale);

    /**
     * Gets the supported capabilities for a contacts access
     * of this contacts provider, describing the usable extended feature set.
     *
     * @return The supported contacts capabilities, or an empty set if
     *         no extended functionality is available
     */
    EnumSet<ContactsAccessCapability> getCapabilities();

    /**
     * Initialises the connection to a specific contact account.
     *
     * @param session The user's session
     * @param account The contact account to connect to
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     * @return The connected contact access
     */
    ContactsAccess connect(Session session, ContactsAccount account, ContactsParameters parameters) throws OXException;

    /**
     * Callback routine that is invoked after an existing account for the contacts provider has been deleted.
     *
     * @param session The user's session
     * @param account The contacts account that was deleted
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     */
    void onAccountDeleted(Session session, ContactsAccount account, ContactsParameters parameters) throws OXException;

    /**
     * Callback routine that is invoked after an existing account for the contacts provider has been deleted (and the user's session is
     * not available).
     *
     * @param context The context
     * @param account The contacts account that was deleted
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     */
    void onAccountDeleted(Context context, ContactsAccount account, ContactsParameters parameters) throws OXException;

    /**
     * Checks whether the provider is available for the given session.
     * <p/>
     * This method is invoked from the capability checker for contact providers, and the general capability check for this contact
     * provider is already done at that stage.
     * <p/>
     * Returns <code>true</code> by default, and may be overridden to perform additional, provider-specific checks.
     *
     * @param session The session
     * @return <code>true</code> if it is available, <code>false</code> otherwise
     */
    default boolean isAvailable(Session session) {
        return true;
    }
}
