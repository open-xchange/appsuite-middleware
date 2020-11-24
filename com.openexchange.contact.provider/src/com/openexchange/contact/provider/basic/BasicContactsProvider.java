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

package com.openexchange.contact.provider.basic;

import org.json.JSONObject;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.provider.ContactsProvider;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link BasicContactsProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public interface BasicContactsProvider extends ContactsProvider {

    /**
     * Probes specific client-supplied, possibly erroneous and/or incomplete contacts settings by checking if they are valid or further
     * configuration settings are required. This step is typically performed prior creating a new account.
     * <p/>
     * In case the settings are valid and can be used to create a new contacts account, the result will contain the proposed contact
     * settings, which may be enhanced by additional default values for certain properties of the contacts account. The client is encouraged to
     * create the account with these settings, then.
     * <p/>
     * In case the settings are invalid or incomplete, an appropriate exception is thrown providing further details about the root cause.
     *
     * @param session The user's session
     * @param settings The contacts settings to be probed for the new account as supplied by the client
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     * @return The proposed contacts settings, enhanced by additional default values
     */
    ContactsSettings probe(Session session, ContactsSettings settings, ContactsParameters parameters) throws OXException;

    /**
     * Initializes the configuration prior creating a new contacts account.
     * <p/>
     * Any permission or data validation checks are performed during this initialization phase. In case of erroneous or incomplete
     * configuration data, an appropriate exception will be thrown. Upon success, any <i>internal</i> configuration data is returned for
     * persisting along with the newly created contacts account.
     * <p/>
     * <b>Note: </b>Since this method is usually invoked within a running database transaction, refrain from performing possibly
     * long-running checks, especially not over the network. Those checks have usually been executed before (during the {@link #probe}
     * -phase of the settings).
     *
     * @param session The user's session
     * @param settings Contact settings for the new account as supplied by the client
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     * @return A JSON object holding the <i>internal</i> configuration to store along with the new account
     */
    JSONObject configureAccount(Session session, ContactsSettings settings, ContactsParameters parameters) throws OXException;

    /**
     * Re-initializes the configuration prior updating an existing contacts account.
     * <p/>
     * Any permission- or data validation checks are performed during this initialization phase. In case of erroneous or incomplete
     * configuration data, an appropriate exception will be thrown. Upon success, any updated <i>internal</i> configuration data is
     * returned for persisting along with the updated contacts account.
     *
     * @param session The user's session
     * @param account The currently stored contacts account holding the obsolete user and current <i>internal</i> configuration
     * @param settings The updated settings for the updated account as supplied by the client
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     * @return A JSON object holding the updated <i>internal</i> configuration to store along with update, or <code>null</code> if unchanged
     */
    JSONObject reconfigureAccount(Session session, ContactsAccount account, ContactsSettings settings, ContactsParameters parameters) throws OXException;

    /**
     * Initializes the connection to a specific contacts account.
     *
     * @param session The user's session
     * @param account The contacts account to connect to
     * @param parameters Additional contacts parameters
     * @return The connected contacts access
     */
    @Override
    BasicContactsAccess connect(Session session, ContactsAccount account, ContactsParameters parameters) throws OXException;
}
