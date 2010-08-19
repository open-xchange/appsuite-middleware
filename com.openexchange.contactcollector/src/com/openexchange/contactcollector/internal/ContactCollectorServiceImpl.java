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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.contactcollector.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import javax.mail.internet.InternetAddress;
import com.openexchange.concurrent.TimeoutConcurrentMap;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.contactcollector.folder.ContactCollectorFolderCreator;
import com.openexchange.contactcollector.osgi.CCServiceRegistry;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link ContactCollectorServiceImpl}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContactCollectorServiceImpl implements ContactCollectorService {

    private TimeoutConcurrentMap<Integer, Future<Set<InternetAddress>>> aliasesMap;

    /**
     * Initializes a new {@link ContactCollectorServiceImpl}.
     */
    public ContactCollectorServiceImpl() {
        super();
    }

    public void memorizeAddresses(final List<InternetAddress> addresses, final Session session) {
        memorizeAddresses(addresses, session, true);
    }

    public void memorizeAddresses(final List<InternetAddress> addresses, final Session session, boolean background) {
        /*
         * Delegate to thread pool if available
         */
        final ThreadPoolService threadPoolService = CCServiceRegistry.getInstance().getService(ThreadPoolService.class);
        if (!background || null == threadPoolService) {
            // Run in calling thread
            new Memorizer(addresses, session, aliasesMap).run();
        } else {
            threadPoolService.submit(
                ThreadPools.task(new Memorizer(addresses, session, aliasesMap), "ContactCollector"),
                CallerRunsBehavior.getInstance());
        }
    }

    /**
     * Starts this contact collector service implementation.
     * 
     * @throws ServiceException If a needed service is missing
     */
    public void start() throws ServiceException {
        aliasesMap = new TimeoutConcurrentMap<Integer, Future<Set<InternetAddress>>>(60, true);
    }

    /**
     * Stops this contact collector service implementation.
     */
    public void stop() {
        if (null != aliasesMap) {
            aliasesMap.dispose();
            aliasesMap = null;
        }
    }

    public void createCollectFolder(Session session, Context ctx, String folderName, Connection con) throws AbstractOXException, SQLException {
        new ContactCollectorFolderCreator().create(session, ctx, folderName, con);
    }

}
