/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.guest.impl.internal;

import java.util.Collections;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.guest.GuestAssignment;
import com.openexchange.guest.GuestService;
import com.openexchange.user.User;

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
    public void addGuest(String mailAddress, String groupId, int contextId, int userId, String password, String passwordMech, byte[] salt) throws OXException {
        if (configurationService.getBoolProperty(CROSS_CONTEXT_ENABLED, false)) {
            this.delegate.addGuest(mailAddress, groupId, contextId, userId, password, passwordMech, salt);
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
