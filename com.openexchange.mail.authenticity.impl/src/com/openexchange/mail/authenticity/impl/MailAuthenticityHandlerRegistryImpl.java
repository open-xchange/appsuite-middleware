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

package com.openexchange.mail.authenticity.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.mail.authenticity.MailAuthenticityHandler;
import com.openexchange.mail.authenticity.MailAuthenticityHandlerRegistry;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.session.Session;


/**
 * {@link MailAuthenticityHandlerRegistryImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MailAuthenticityHandlerRegistryImpl implements MailAuthenticityHandlerRegistry {

    private final ServiceListing<MailAuthenticityHandler> listing;
    private final Comparator<MailAuthenticityHandler> comparator;
    private final ConfigViewFactory viewFactory;

    /**
     * Initializes a new {@link MailAuthenticityHandlerRegistryImpl}.
     */
    public MailAuthenticityHandlerRegistryImpl(ServiceListing<MailAuthenticityHandler> listing, ConfigViewFactory viewFactory) {
        super();
        this.listing = listing;
        this.viewFactory = viewFactory;
        comparator = new Comparator<MailAuthenticityHandler>() {

            @Override
            public int compare(MailAuthenticityHandler o1, MailAuthenticityHandler o2) {
                int r1 = o1.getRanking();
                int r2 = o2.getRanking();
                return (r1 < r2) ? 1 : ((r1 == r2) ? 0 : -1);
            }
        };
    }

    @Override
    public boolean isNotEnabledFor(Session session) throws OXException {
        return false == isEnabledFor(session);
    }

    @Override
    public boolean isEnabledFor(Session session) throws OXException {
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        boolean def = false;
        return ConfigViews.getDefinedBoolPropertyFrom("com.openexchange.mail.authenticity.enabled", def, view); // authenticity enabled?
    }

    @Override
    public long getDateThreshold(Session session) throws OXException {
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        long def = 0L;
        return ConfigViews.getDefinedLongPropertyFrom("com.openexchange.mail.authenticity.threshold", def, view);
    }

    @Override
    public List<MailAuthenticityHandler> getSortedApplicableHandlersFor(Session session) throws OXException {
        if (isNotEnabledFor(session)) {
            // Disabled per configuration
            return Collections.emptyList();
        }

        List<MailAuthenticityHandler> snapshot = listing.getServiceList();
        if (snapshot == null || snapshot.isEmpty()) {
            // None registered
            return Collections.emptyList();
        }

        long threshold = getDateThreshold(session);
        List<MailAuthenticityHandler> applicableHandlers = new ArrayList<>(snapshot.size());
        for (MailAuthenticityHandler handler : snapshot) {
            if (handler.isEnabled(session)) {
                applicableHandlers.add(new ThresholdAwareAuthenticityHandler(handler, threshold));
            }
        }

        if (applicableHandlers.isEmpty()) {
            // No suitable handler found
            return Collections.emptyList();
        }

        Collections.sort(applicableHandlers, comparator);
        return applicableHandlers;
    }

}
