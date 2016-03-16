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

package com.openexchange.textxtraction.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.textxtraction.DelegateTextXtraction;
import com.openexchange.textxtraction.TextXtractService;
import com.openexchange.textxtraction.internal.TikaTextXtractService;

/**
 * {@link TextXtractionActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class TextXtractionActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link TextXtractionActivator}.
     */
    public TextXtractionActivator() {
        super();
    }

    @Override
    public void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TextXtractionActivator.class);
        final String name = "com.openexchange.textxtraction";
        log.info("Starting bundle: {}", name);
        try {
            final TikaTextXtractService tikaTextXtractService = new TikaTextXtractService();
            final BundleContext context = this.context;
            track(DelegateTextXtraction.class, new ServiceTrackerCustomizer<DelegateTextXtraction, DelegateTextXtraction>() {

                @Override
                public DelegateTextXtraction addingService(final ServiceReference<DelegateTextXtraction> reference) {
                    final DelegateTextXtraction service = context.getService(reference);
                    if (tikaTextXtractService.addDelegateTextXtraction(service)) {
                        return service;
                    }
                    context.ungetService(reference);
                    return null;
                }

                @Override
                public void modifiedService(final ServiceReference<DelegateTextXtraction> reference, final DelegateTextXtraction service) {
                    // Nothing
                }

                @Override
                public void removedService(final ServiceReference<DelegateTextXtraction> reference, final DelegateTextXtraction service) {
                    if (null != service) {
                        tikaTextXtractService.removeDelegateTextXtraction(service);
                        context.ungetService(reference);
                    }
                }
            });
            openTrackers();
            registerService(TextXtractService.class, tikaTextXtractService);
        } catch (final Exception e) {
            log.info("Starting bundle failed: {}", name, e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TextXtractionActivator.class);
        final String name = "com.openexchange.textxtraction";
        log.info("Stopping bundle: {}", name);
        try {
            cleanUp();
        } catch (final Exception e) {
            log.info("Stopping bundle failed: {}", name, e);
            throw e;
        }
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[0];
    }

}
