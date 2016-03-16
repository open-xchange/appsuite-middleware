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

package com.openexchange.mail.messaging;

import java.util.List;
import java.util.Set;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAction;
import com.openexchange.messaging.MessagingService;
import com.openexchange.session.Session;

/**
 * {@link MailMessagingService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailMessagingService implements MessagingService {

    private static final MailMessagingService INSTANCE = new MailMessagingService();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static MailMessagingService getInstance() {
        return INSTANCE;
    }

    /**
     * The identifier of mail messaging service.
     */
    public static final String ID = "com.openexchange.messaging.mail";

    /**
     * Initializes a new {@link MailMessagingService}.
     */
    private MailMessagingService() {
        super();
    }

    @Override
    public MessagingAccountAccess getAccountAccess(final int accountId, final Session session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public MessagingAccountManager getAccountManager() {
        // Nothing to do
        return null;
    }

    @Override
    public MessagingAccountTransport getAccountTransport(final int accountId, final Session session) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public String getDisplayName() {
        // Nothing to do
        return null;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<MessagingAction> getMessageActions() {
        // Nothing to do
        return null;
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        // Nothing to do
        return null;
    }

    @Override
    public Set<String> getSecretProperties() {
        // Nothing to do
        return null;
    }

    @Override
    public int[] getStaticRootPermissions() {
        // Nothing to do
        return null;
    }

}
