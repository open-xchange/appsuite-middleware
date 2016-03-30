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

package com.openexchange.subscribe.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;


/**
 * {@link FilteredSubscriptionSourceDiscoveryService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FilteredSubscriptionSourceDiscoveryService implements SubscriptionSourceDiscoveryService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FilteredSubscriptionSourceDiscoveryService.class);

    public static final AtomicReference<ConfigViewFactory> CONFIG_VIEW_FACTORY = new AtomicReference<ConfigViewFactory>();

    public SubscriptionSourceDiscoveryService delegate = null;
    private final ConfigView config;

    public FilteredSubscriptionSourceDiscoveryService(final int user, final int context, final SubscriptionSourceDiscoveryService delegate) throws OXException {
        this.config = CONFIG_VIEW_FACTORY.get().getView(user, context);
        this.delegate = delegate;
    }

    @Override
    public SubscriptionSource getSource(final String identifier) {
        if (accepts(identifier)) {
            return delegate.getSource(identifier);
        }
        return null;
    }

    @Override
    public SubscriptionSource getSource(final Context context, final int subscriptionId) throws OXException {
        final SubscriptionSource source = delegate.getSource(context, subscriptionId);

        return filter(source);
    }

    @Override
    public List<SubscriptionSource> getSources() {
        return filter(delegate.getSources());
    }


    @Override
    public List<SubscriptionSource> getSources(final int folderModule) {
        return filter(delegate.getSources(folderModule));
    }

    @Override
    public boolean knowsSource(final String identifier) {
        return accepts(identifier) ? delegate.knowsSource(identifier) : false;
    }

    @Override
    public SubscriptionSourceDiscoveryService filter(final int user, final int context) throws OXException {
        return delegate.filter(user, context);
    }

    protected boolean accepts(final String identifier) {
        try {
            final ComposedConfigProperty<Boolean> property = config.property(identifier, boolean.class);
            if(property.isDefined()) {
                return property.get().booleanValue();
            }
            return true;
        } catch (final OXException e) {
            LOG.error("", e);
            return false;
        }
    }

    protected SubscriptionSource filter(final SubscriptionSource source) {
        if (source == null) {
            return null;
        }
        return accepts(source.getId()) ? source : null;
    }

    protected List<SubscriptionSource> filter(final List<SubscriptionSource> sources) {
        final List<SubscriptionSource> filtered = new ArrayList<SubscriptionSource>(sources.size());
        for (final SubscriptionSource subscriptionSource : sources) {
            if (accepts(subscriptionSource.getId())) {
                filtered.add(subscriptionSource);
            }
        }
        return filtered;
    }

}
