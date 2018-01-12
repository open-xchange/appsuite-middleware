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

package com.openexchange.passwordchange.history.impl.groupware;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.ldap.User;
import com.openexchange.passwordchange.history.PasswordChangeRecorderRegistryService;
import com.openexchange.passwordchange.history.PasswordChangeRecorder;
import com.openexchange.passwordchange.history.impl.events.PasswordChangeHelper;
import com.openexchange.user.UserService;

/**
 *
 * {@link PasswordHistoryDeleteListener} - Listener to delete all password change history entries on deletion of user or context
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordHistoryDeleteListener implements DeleteListener {

    private final PasswordChangeRecorderRegistryService registry;
    private final UserService userService;

    /**
     * Initializes a new {@link PasswordHistoryDeleteListener}.
     *
     * @param registry The {@link PasswordChangeRecorderRegistryService} to get the {@link PasswordChangeRecorder} from
     */
    public PasswordHistoryDeleteListener(PasswordChangeRecorderRegistryService registry, UserService userService) {
        super();
        this.registry = registry;
        this.userService = userService;
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        // Only context and user are relevant
        switch (event.getType()) {
            case DeleteEvent.TYPE_CONTEXT:
                {
                    // Get users in context and remove password for them
                    for (User user : userService.getUser(event.getContext())) {
                        if (false == user.isGuest()) {
                            PasswordChangeHelper.clearSafeFor(event.getContext().getContextId(), user.getId(), -1, registry);
                        }
                    }
                }

                break;
            case DeleteEvent.TYPE_USER:
                {
                    User user = userService.getUser(event.getId(), event.getContext());
                    if (false == user.isGuest()) {
                        PasswordChangeHelper.clearSafeFor(event.getContext().getContextId(), event.getId(), -1, registry);
                    }
                }

                break;
            default:
                // Ignore all other
                return;
        }
    }

}
