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

package com.openexchange.passwordchange.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Set;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.guest.GuestService;
import com.openexchange.guest.osgi.GuestServiceServiceTracker;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.passwordchange.BasicPasswordChangeService;
import com.openexchange.passwordchange.DefaultBasicPasswordChangeService;
import com.openexchange.passwordchange.EditPasswordCapabilityChecker;
import com.openexchange.user.UserService;

/**
 * {@link PasswordChangeActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class PasswordChangeActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link PasswordChangeActivator}.
     */
    public PasswordChangeActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        Set<Class<?>> classes = new LinkedHashSet<>(8);
        classes.add(UserService.class);
        classes.add(CapabilityService.class);

        Class<?>[] neededServices = EditPasswordCapabilityChecker.getNeededServices();
        if (null != neededServices && neededServices.length > 0) {
            for (Class<?> clazz : neededServices) {
                if (null != clazz) {
                    classes.add(clazz);
                }
            }
        }

        return classes.toArray(new Class<?>[classes.size()]);
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(BasicPasswordChangeService.class, new DefaultBasicPasswordChangeService());

        // Register CapabilityChecker
        {
            final Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
            properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, EditPasswordCapabilityChecker.EDIT_PASSWORD_CAP);
            registerService(CapabilityChecker.class, new EditPasswordCapabilityChecker(this), properties);

            getService(CapabilityService.class).declareCapability(EditPasswordCapabilityChecker.EDIT_PASSWORD_CAP);
        }

        track(GuestService.class, new GuestServiceServiceTracker(this.context));
        openTrackers();
    }
}
