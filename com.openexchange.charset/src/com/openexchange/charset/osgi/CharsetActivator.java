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

package com.openexchange.charset.osgi;

import java.nio.charset.spi.CharsetProvider;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.charset.CollectionCharsetProvider;
import com.openexchange.charset.ModifyCharsetExtendedProvider;
import com.openexchange.charset.ModifyCharsetStandardProvider;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link CharsetActivator} - Activator for com.openexchange.charset bundle
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CharsetActivator extends HousekeepingActivator implements ServiceTrackerCustomizer<CharsetProvider, CharsetProvider> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CharsetActivator.class);

    private CollectionCharsetProvider collectionCharsetProvider;

    private CharsetProvider backupExtendedCharsetProvider;

    private CharsetProvider backupStandardCharsetProvider;

    /**
     * Default constructor
     */
    public CharsetActivator() {
        super();
    }

    @Override
    public CharsetProvider addingService(final ServiceReference<CharsetProvider> reference) {
        final CharsetProvider addedService = context.getService(reference);
        {
            collectionCharsetProvider.addCharsetProvider(addedService);
            LOG.info("New charset provider detected and added: {}", addedService.getClass().getName());
        }
        return addedService;
    }

    @Override
    public void modifiedService(final ServiceReference<CharsetProvider> reference, final CharsetProvider service) {
        // Nope
    }

    @Override
    public void removedService(final ServiceReference<CharsetProvider> reference, final CharsetProvider service) {
        {
            collectionCharsetProvider.removeCharsetProvider(service);
            LOG.info("Charset provider removed: {}", service.getClass().getName());
        }
        context.ungetService(reference);
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {};
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: com.openexchange.charset");

        try {
            {
                final CharsetProvider[] results = ModifyCharsetExtendedProvider.modifyCharsetExtendedProvider();
                backupExtendedCharsetProvider = results[0];
                collectionCharsetProvider = (CollectionCharsetProvider) results[1];
            }
            {
                final CharsetProvider[] results = ModifyCharsetStandardProvider.modifyCharsetExtendedProvider();
                backupStandardCharsetProvider = null == results ? null : results[0];
            }
            LOG.info("Standard & external charset provider replaced with collection charset provider");
            /*
             * Initialize a service tracker to track bundle chars providers
             */
            track(CharsetProvider.class, this);
            openTrackers();
            LOG.info("Charset bundle successfully started");
        } catch (final Throwable t) {
            LOG.error("", t);
            throw t instanceof Exception ? (Exception) t : new Exception(t);
        }
    }

    @Override
    public void stopBundle() {
        LOG.info("stopping bundle: com.openexchange.charset");
        try {
            closeTrackers();
            /*
             * Restore original
             */
            if (null != backupExtendedCharsetProvider) {
                ModifyCharsetExtendedProvider.restoreCharsetExtendedProvider(backupExtendedCharsetProvider);
                backupExtendedCharsetProvider = null;
            }
            if (null != backupStandardCharsetProvider) {
                ModifyCharsetStandardProvider.restoreCharsetExtendedProvider(backupStandardCharsetProvider);
                backupStandardCharsetProvider = null;
            }
            collectionCharsetProvider = null;
            LOG.info("Collection charset provider replaced with former standard/external charset provider. Charset bundle successfully stopped");
        } catch (final Throwable t) {
            LOG.error("", t);
//            throw t instanceof Exception ? (Exception) t : new Exception(t);
        } finally {
            collectionCharsetProvider = null;
            cleanUp();
        }
    }

}
