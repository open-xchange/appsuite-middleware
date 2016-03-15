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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.mail.find;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.mail.MailDriveConstants;
import com.openexchange.find.AbstractFindRequest;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.spi.ModuleSearchDriver;
import com.openexchange.find.spi.SearchConfiguration;
import com.openexchange.java.ConcurrentPriorityQueue;
import com.openexchange.java.Strings;
import com.openexchange.osgi.util.RankedService;
import com.openexchange.tools.id.IDMangler;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailDriveDriver}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class MailDriveDriver extends ServiceTracker<ModuleSearchDriver, ModuleSearchDriver> implements ModuleSearchDriver {

    private final ConcurrentPriorityQueue<RankedService<ModuleSearchDriver>> trackedDrivers;
    private final int ranking;
    private ServiceRegistration<ModuleSearchDriver> registration; // non-volatile, protected by synchronized blocks

    /**
     * Initializes a new {@link MailDriveDriver}.
     *
     * @param initialDelegate The iniial delegate instance
     */
    public MailDriveDriver(BundleContext context, int ranking) {
        super(context, ModuleSearchDriver.class, null);
        trackedDrivers = new ConcurrentPriorityQueue<RankedService<ModuleSearchDriver>>();
        this.ranking = ranking;
    }

    // ---------------------------------------------------------------------------------------------------------

    @Override
    public synchronized ModuleSearchDriver addingService(ServiceReference<ModuleSearchDriver> reference) {
        int ranking = RankedService.getRanking(reference);
        if (ranking >= this.ranking) {
            // Higher or equal ranking... ignore.
            return null;
        }

        ModuleSearchDriver driver = context.getService(reference);
        if (Module.DRIVE != driver.getModule()) {
            // Not a Drive driver...
            context.ungetService(reference);
            return null;
        }

        trackedDrivers.offer(new RankedService<ModuleSearchDriver>(driver, ranking));

        if (null == registration) {
            Dictionary<String, Object> props = new Hashtable<>(2);
            props.put(Constants.SERVICE_RANKING, Integer.valueOf(this.ranking));
            registration = context.registerService(ModuleSearchDriver.class, this, props);
        }

        return driver;
    }

    @Override
    public void modifiedService(ServiceReference<ModuleSearchDriver> reference, ModuleSearchDriver driver) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<ModuleSearchDriver> reference, ModuleSearchDriver driver) {
        if (Module.DRIVE == driver.getModule()) {
            trackedDrivers.remove(new RankedService<ModuleSearchDriver>(driver, RankedService.getRanking(reference)));

            if (trackedDrivers.isEmpty() && null != registration) {
                registration.unregister();
                registration = null;
            }
        }
        context.ungetService(reference);
    }

    /**
     * Gets the currently available {@code ModuleSearchDriver} instance having the highest rank.
     *
     * @return The highest-ranked {@code ModuleSearchDriver} instance or <code>null</code>
     * @throws OXException If no such service is currently available
     */
    private ModuleSearchDriver delegate() throws OXException {
        RankedService<ModuleSearchDriver> rankedService = trackedDrivers.peek();
        if (null == rankedService) {
            // About to shut-down
            throw FindExceptionCode.UNEXPECTED_ERROR.create("Mail Drive service is about to shut-down");
        }
        return rankedService.service;
    }

    // ---------------------------------------------------------------------------------------------------------

    @Override
    public Module getModule() {
        return Module.DRIVE;
    }

    @Override
    public boolean isValidFor(ServerSession session) throws OXException {
        return delegate().isValidFor(session);
    }

    @Override
    public boolean isValidFor(ServerSession session, AbstractFindRequest findRequest) throws OXException {
        return delegate().isValidFor(session, findRequest);
    }

    @Override
    public SearchConfiguration getSearchConfiguration(ServerSession session) throws OXException {
        return delegate().getSearchConfiguration(session);
    }

    @Override
    public AutocompleteResult autocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        String accountId = autocompleteRequest.getAccountId();
        if (Strings.isEmpty(accountId)) {
            return delegate().autocomplete(autocompleteRequest, session);
        }

        List<String> idParts = IDMangler.unmangle(accountId);
        if (idParts.size() != 2) {
            throw FindExceptionCode.INVALID_ACCOUNT_ID.create(accountId, Module.DRIVE.getIdentifier());
        }

        if (!MailDriveConstants.ID.equals(idParts.get(0)) || !MailDriveConstants.ACCOUNT_ID.equals(idParts.get(1))) {
            return delegate().autocomplete(autocompleteRequest, session);
        }


        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchResult search(SearchRequest searchRequest, ServerSession session) throws OXException {
        String accountId = searchRequest.getAccountId();
        if (Strings.isEmpty(accountId)) {
            return delegate().search(searchRequest, session);
        }

        List<String> idParts = IDMangler.unmangle(accountId);
        if (idParts.size() != 2) {
            throw FindExceptionCode.INVALID_ACCOUNT_ID.create(accountId, Module.DRIVE.getIdentifier());
        }

        if (!MailDriveConstants.ID.equals(idParts.get(0)) || !MailDriveConstants.ACCOUNT_ID.equals(idParts.get(1))) {
            return delegate().search(searchRequest, session);
        }


        return null;
    }

}
