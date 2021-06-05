/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
            if (property.isDefined()) {
                return property.get().booleanValue();
            }
            return true;
        } catch (OXException e) {
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
