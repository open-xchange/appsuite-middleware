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

package com.openexchange.messaging;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.FormElement.Widget;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link SimMessagingService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SimMessagingService implements MessagingService {


    private String displayName;

    private DynamicFormDescription formDescription;

    private String id;

    private final Map<Integer, MessagingAccountAccess> accountAccessMap;

    private final Map<Integer, MessagingAccountTransport> accountTransportMap;

    private MessagingAccountManager accManager;

    private List<MessagingAction> capabilities;

    private int[] staticRootPermissions;

    public SimMessagingService() {
        super();
        accountAccessMap = new HashMap<Integer, MessagingAccountAccess>();
        accountTransportMap = new HashMap<Integer, MessagingAccountTransport>();
    }

    @Override
    public Set<String> getSecretProperties() {
        return getPasswordElementNames(formDescription);
    }

    /**
     * Gets the names of those {@link FormElement} associated with given identifier's messaging service which indicate to be of type
     * {@link Widget#PASSWORD password}.
     *
     * @param serviceId The service identifier
     * @return The password field names
     */
    private static Set<String> getPasswordElementNames(final DynamicFormDescription formDescription) {
        final Set<String> retval = new HashSet<String>(2);
        for (final FormElement formElement : formDescription) {
            if (Widget.PASSWORD.equals(formElement.getWidget())) {
                retval.add(formElement.getName());
            }
        }
        return retval;
    }

    @Override
    public MessagingAccountManager getAccountManager() {
        return accManager;
    }

    @Override
    public List<MessagingAction> getMessageActions() {
        return capabilities;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setMessageActions(final List<MessagingAction> capabilities) {
        this.capabilities = capabilities;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setFormDescription(final DynamicFormDescription formDescription) {
        this.formDescription = formDescription;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setAccountManager(final MessagingAccountManager accManager) {
        this.accManager = accManager;
    }

    @Override
    public MessagingAccountAccess getAccountAccess(final int accountId, final Session session) throws OXException {
        final MessagingAccountAccess accountAccess = accountAccessMap.get(Integer.valueOf(accountId));
        if (null == accountAccess) {
            // TODO: Throw appropriate error
        }
        return accountAccess;
    }

    public void setAccountAccess(final int accountId, final MessagingAccountAccess accountAccess) {
        accountAccessMap.put(Integer.valueOf(accountId), accountAccess);
    }

    @Override
    public MessagingAccountTransport getAccountTransport(final int accountId, final Session session) throws OXException {
        final MessagingAccountTransport accountTransport = accountTransportMap.get(Integer.valueOf(accountId));
        if (null == accountTransport) {
            // TODO: Throw appropriate error
        }
        return accountTransport;
    }

    public void setAccountTransport(final int accountId, final MessagingAccountTransport accountTransport) {
        accountTransportMap.put(Integer.valueOf(accountId), accountTransport);
    }

    @Override
    public int[] getStaticRootPermissions() {
        if (null == staticRootPermissions) {
            return null;
        }
        final int[] ret = new int[staticRootPermissions.length];
        System.arraycopy(staticRootPermissions, 0, ret, 0, ret.length);
        return ret;
    }


    /**
     * Sets the static root folder permissions.
     *
     * @param staticRootPermissions The static root folder permissions
     */
    public void setStaticRootPermissions(int[] staticRootPermissions) {
        if (null == staticRootPermissions) {
            this.staticRootPermissions = null;
        } else {
            final int[] tmp = new int[staticRootPermissions.length];
            System.arraycopy(staticRootPermissions, 0, tmp, 0, tmp.length);
            this.staticRootPermissions = tmp;
        }
    }

}
