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

package com.openexchange.guest.impl.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.delete.contextgroup.DeleteContextGroupListener;
import com.openexchange.guest.GuestService;
import com.openexchange.guest.impl.internal.DefaultGuestService;
import com.openexchange.guest.impl.internal.DelegateGuestService;
import com.openexchange.guest.impl.internal.GuestDeleteContextGroupListener;
import com.openexchange.guest.impl.internal.GuestDeleteListenerImpl;
import com.openexchange.guest.impl.internal.GuestStorageServiceLookup;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.passwordmechs.PasswordMechFactory;
import com.openexchange.user.UserService;

/**
 * {@link GuestImplActivator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class GuestImplActivator extends HousekeepingActivator {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            UserService.class, DatabaseService.class, ConfigViewFactory.class, ContactUserStorage.class, ConfigurationService.class, ContextService.class, PasswordMechFactory.class
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GuestImplActivator.class);
        logger.info("Starting bundle: {}", this.context.getBundle().getSymbolicName());

        GuestStorageServiceLookup.set(this);

        GuestService guestService = new DefaultGuestService(getService(UserService.class), getService(ContextService.class), getService(ContactUserStorage.class), getService(ConfigViewFactory.class), getService(PasswordMechFactory.class));
        GuestService delegateGuestService = new DelegateGuestService(guestService, getService(ConfigurationService.class));
        registerService(GuestService.class, delegateGuestService);

        registerService(DeleteListener.class, new GuestDeleteListenerImpl(delegateGuestService));
        registerService(DeleteContextGroupListener.class, new GuestDeleteContextGroupListener(delegateGuestService));
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GuestImplActivator.class);
        logger.info("Stopping bundle: {}", this.context.getBundle().getSymbolicName());

        GuestStorageServiceLookup.set(null);
        super.stopBundle();
    }
}
