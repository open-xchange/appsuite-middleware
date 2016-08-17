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

package com.openexchange.guest.impl.internal;

import java.util.Collections;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.guest.GuestAssignment;
import com.openexchange.guest.GuestService;

/**
 * Delegate implementation of {@link GuestService} if cross context handling is enabled.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class DelegateGuestService implements GuestService {

    private static final String CROSS_CONTEXT_ENABLED = "com.openexchange.share.crossContextGuests";

    private final GuestService delegate;

    private final ConfigurationService configurationService;

    /**
     * Initializes a new {@link DelegateGuestService}.
     *
     * @param guestService - service to delegate to
     */
    public DelegateGuestService(GuestService guestService, ConfigurationService configurationService) {
        this.delegate = guestService;
        this.configurationService = configurationService;
    }

    @Override
    public boolean isCrossContextGuestHandlingEnabled() {
        return configurationService.getBoolProperty(CROSS_CONTEXT_ENABLED, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean authenticate(final User user, int contextId, final String password) throws OXException {
        if (configurationService.getBoolProperty(CROSS_CONTEXT_ENABLED, false) && user.isGuest()) {
            return this.delegate.authenticate(user, contextId, password);
        }
        return false;
    }

    @Override
    public User alignUserWithGuest(User user,  int contextId) throws OXException {
        if (configurationService.getBoolProperty(CROSS_CONTEXT_ENABLED, false)) {
            return this.delegate.alignUserWithGuest(user, contextId);
        }
        return user;
    }

    @Override
    public void addGuest(String mailAddress, String groupId, int contextId, int userId, String password, String passwordMech) throws OXException {
        if (configurationService.getBoolProperty(CROSS_CONTEXT_ENABLED, false)) {
            this.delegate.addGuest(mailAddress, groupId, contextId, userId, password, passwordMech);
        }
    }

    @Override
    public void removeGuest(int contextId, int userId) throws OXException {
        if (configurationService.getBoolProperty(CROSS_CONTEXT_ENABLED, false)) {
            this.delegate.removeGuest(contextId, userId);
        }
    }

    @Override
    public void removeGuests(int contextId) throws OXException {
        if (configurationService.getBoolProperty(CROSS_CONTEXT_ENABLED, false)) {
            this.delegate.removeGuests(contextId);
        }
    }

    @Override
    public void removeGuests(String groupId) throws OXException {
        if (configurationService.getBoolProperty(CROSS_CONTEXT_ENABLED, false)) {
            this.delegate.removeGuests(groupId);
        }
    }

    @Override
    public void updateGuestContact(Contact contact, int contextId) throws OXException {
        if (configurationService.getBoolProperty(CROSS_CONTEXT_ENABLED, false)) {
            this.delegate.updateGuestContact(contact, contextId);
        }
    }

    @Override
    public void updateGuestUser(User user, int contextId) throws OXException {
        if (configurationService.getBoolProperty(CROSS_CONTEXT_ENABLED, false)) {
            this.delegate.updateGuestUser(user, contextId);
        }
    }

    @Override
    public UserImpl createUserCopy(String mailAddress, String groupId, int contextId) throws OXException {
        if (configurationService.getBoolProperty(CROSS_CONTEXT_ENABLED, false)) {
            return this.delegate.createUserCopy(mailAddress, groupId, contextId);
        }
        return null;
    }

    @Override
    public Contact createContactCopy(String mailAddress, String groupId, int contextId, int createdById) throws OXException {
        if (configurationService.getBoolProperty(CROSS_CONTEXT_ENABLED, false)) {
            return this.delegate.createContactCopy(mailAddress, groupId, contextId, createdById);
        }
        return null;
    }

    @Override
    public List<GuestAssignment> getExistingAssignments(String mailAddress, String groupId) throws OXException {
        if (configurationService.getBoolProperty(CROSS_CONTEXT_ENABLED, false)) {
            return this.delegate.getExistingAssignments(mailAddress, groupId);
        }
        return Collections.emptyList();
    }
}
