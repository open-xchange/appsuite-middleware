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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.capabilities.osgi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.capabilities.CapabilityChecker;

/**
 * {@link CapabilityCheckerRegistry} - A registry for CapabilityChecker.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CapabilityCheckerRegistry extends ServiceTracker<CapabilityChecker, CapabilityChecker> {

    private final AtomicReference<List<CapabilityChecker>> checkers;

    /**
     * Initializes a new {@link CapabilityCheckerRegistry}.
     *
     * @param context The bundle context
     */
    public CapabilityCheckerRegistry(final BundleContext context) {
        super(context, CapabilityChecker.class, null);
        checkers = new AtomicReference<List<CapabilityChecker>>(Collections.<CapabilityChecker> emptyList());
    }

    @Override
    public CapabilityChecker addingService(ServiceReference<CapabilityChecker> reference) {
        final CapabilityChecker checker = context.getService(reference);

        List<CapabilityChecker> expected;
        List<CapabilityChecker> list;
        do {
            expected = checkers.get();
            list = new ArrayList<CapabilityChecker>(expected);
            list.add(checker);
        } while (!checkers.compareAndSet(expected, list));

        return checker;
    }

    @Override
    public void modifiedService(org.osgi.framework.ServiceReference<CapabilityChecker> reference, CapabilityChecker service) {
        // Ignore
    }

    @Override
    public void remove(ServiceReference<CapabilityChecker> reference) {
        final CapabilityChecker checker = context.getService(reference);

        List<CapabilityChecker> expected;
        List<CapabilityChecker> list;
        do {
            expected = checkers.get();
            list = new ArrayList<CapabilityChecker>(expected);
            list.remove(checker);
        } while (!checkers.compareAndSet(expected, list));

        context.ungetService(reference);
    }

    /**
     * Gets a snapshot of currently available checkers.
     *
     * @return The available checkers
     */
    public List<CapabilityChecker> getCheckers() {
        return checkers.get();
    }

}
