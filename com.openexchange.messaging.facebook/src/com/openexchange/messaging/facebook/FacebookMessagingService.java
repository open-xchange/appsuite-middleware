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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.messaging.facebook;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAction;
import com.openexchange.messaging.MessagingPermission;
import com.openexchange.messaging.MessagingService;
import com.openexchange.session.Session;

/**
 * {@link FacebookMessagingService} - The Facebook messaging service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class FacebookMessagingService implements MessagingService {

    private static final String ID = "com.openexchange.messaging.facebook";

    private static final String DISPLAY_NAME = "Facebook";

    /**
     * Gets the service identifier for Facebook messaging service.
     *
     * @return The service identifier
     */
    public static String getServiceId() {
        return ID;
    }

    /*-
     * -------------------------------------- Member section --------------------------------------
     */

    private final MessagingAccountManager accountManager;

    private final DynamicFormDescription formDescription;

    /**
     * Initializes a new {@link FacebookMessagingService}.
     */
    public FacebookMessagingService() {
        super();
        accountManager = new FacebookMessagingAccountManager(this);
        final DynamicFormDescription tmpDescription = new DynamicFormDescription();
        /*
         * API & secret key
         */
        final FormElement oauthAccount = FormElement.custom("oauthAccount", "account", FormStrings.ACCOUNT_LABEL);
        oauthAccount.setOption("type", "com.openexchange.oauth.facebook");
        tmpDescription.add(oauthAccount);
        formDescription = new ReadOnlyDynamicFormDescription(tmpDescription);
    }

    @Override
    public Set<String> getSecretProperties() {
        return Collections.emptySet();
    }

    @Override
    public MessagingAccountAccess getAccountAccess(final int accountId, final Session session) throws OXException {
        return new FacebookMessagingAccountAccess(accountManager.getAccount(accountId, session), session);
    }

    @Override
    public MessagingAccountManager getAccountManager() {
        return accountManager;
    }

    @Override
    public MessagingAccountTransport getAccountTransport(final int accountId, final Session session) throws OXException {
        return new FacebookMessagingAccountTransport(accountManager.getAccount(accountId, session), session);
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    @Override
    public String getId() {
        return ID;
    }

    private static final List<MessagingAction> ACTIONS =
        Collections.unmodifiableList(Arrays.asList(
            new MessagingAction(FacebookConstants.TYPE_UPDATE_STATUS, MessagingAction.Type.MESSAGE),
            new MessagingAction(FacebookConstants.TYPE_POST, MessagingAction.Type.MESSAGE)));

    @Override
    public List<MessagingAction> getMessageActions() {
        /*
         * status update, wall
         */
        return ACTIONS;
    }

    public static int[] getStaticRootPerms() {
        return new int[] {MessagingPermission.READ_FOLDER,
            MessagingPermission.READ_ALL_OBJECTS,
            MessagingPermission.NO_PERMISSIONS,
            MessagingPermission.NO_PERMISSIONS};
    }

    @Override
    public int[] getStaticRootPermissions() {
        return getStaticRootPerms();
    }

}
