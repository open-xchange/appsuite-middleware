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

package com.openexchange.secret.recovery.osgi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;
import com.openexchange.secret.recovery.SecretConsistencyCheck;
import com.openexchange.secret.recovery.impl.DefaultSecretInconsistencyDetector;

/**
 * {@link WhiteboardSecretInconsistencyDetector}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class WhiteboardSecretInconsistencyDetector extends DefaultSecretInconsistencyDetector {

    private final ServiceTracker<SecretConsistencyCheck, SecretConsistencyCheck> tracker;
    private final WhiteboardSecretService secretService;
    private final AtomicReference<List<SecretConsistencyCheck>> checks;

    public WhiteboardSecretInconsistencyDetector(final BundleContext context) {
        super();
        final AtomicReference<List<SecretConsistencyCheck>> checks = new AtomicReference<List<SecretConsistencyCheck>>(
            Collections.<SecretConsistencyCheck> emptyList());
        this.checks = checks;
        final ServiceTrackerCustomizer<SecretConsistencyCheck, SecretConsistencyCheck> customizer = new ServiceTrackerCustomizer<SecretConsistencyCheck, SecretConsistencyCheck>() {

            @Override
            public void removedService(final ServiceReference<SecretConsistencyCheck> reference, final SecretConsistencyCheck service) {
                List<SecretConsistencyCheck> expected;
                List<SecretConsistencyCheck> list;
                do {
                    expected = checks.get();
                    list = new ArrayList<SecretConsistencyCheck>(expected);
                    list.remove(service);
                } while (!checks.compareAndSet(expected, list));

                context.ungetService(reference);
            }

            @Override
            public void modifiedService(final ServiceReference<SecretConsistencyCheck> reference, final SecretConsistencyCheck service) {
                // Ignore
            }

            @Override
            public SecretConsistencyCheck addingService(final ServiceReference<SecretConsistencyCheck> reference) {
                final SecretConsistencyCheck service = context.getService(reference);

                List<SecretConsistencyCheck> expected;
                List<SecretConsistencyCheck> list;
                do {
                    expected = checks.get();
                    list = new ArrayList<SecretConsistencyCheck>(expected);
                    list.add(service);
                } while (!checks.compareAndSet(expected, list));

                return service;
            }
        };
        tracker = new ServiceTracker<SecretConsistencyCheck, SecretConsistencyCheck>(context, SecretConsistencyCheck.class, customizer);
        secretService = new WhiteboardSecretService(context);
    }

    /**
     * Opens this detector.
     */
    public void open() {
        tracker.open();
        secretService.open();
    }

    /**
     * Closes this detector
     */
    public void close() {
        tracker.close();
        secretService.close();
    }

    @Override
    public List<SecretConsistencyCheck> getChecks() {
        return new ArrayList<SecretConsistencyCheck>(checks.get());
    }

    @Override
    public SecretService getSecretService() {
        return secretService;
    }

}
