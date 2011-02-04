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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigCascadeException;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;


/**
 * {@link FilteredSubscriptionSourceDiscoveryService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FilteredSubscriptionSourceDiscoveryService implements SubscriptionSourceDiscoveryService {

    private static final Log LOG = LogFactory.getLog(FilteredSubscriptionSourceDiscoveryService.class);

    public static ConfigViewFactory CONFIG_VIEW_FACTORY;
    
    public SubscriptionSourceDiscoveryService delegate = null;
    private ConfigView config;
    
    public FilteredSubscriptionSourceDiscoveryService(int user, int context, SubscriptionSourceDiscoveryService delegate) throws ConfigCascadeException {
        this.config = CONFIG_VIEW_FACTORY.getView(user, context);
        this.delegate = delegate;
    }
    
    public SubscriptionSource getSource(String identifier) {
        if (accepts(identifier)) {
            return delegate.getSource(identifier);
        }
        return null;
    }

    public SubscriptionSource getSource(Context context, int subscriptionId) throws AbstractOXException {
        SubscriptionSource source = delegate.getSource(context, subscriptionId);
        
        return filter(source);
    }

    public List<SubscriptionSource> getSources() {
        return filter(delegate.getSources());
    }


    public List<SubscriptionSource> getSources(int folderModule) {
        return filter(delegate.getSources(folderModule));
    }

    public boolean knowsSource(String identifier) {
        return accepts(identifier) ? delegate.knowsSource(identifier) : false;
    }
    
    public SubscriptionSourceDiscoveryService filter(int user, int context) throws AbstractOXException {
        return delegate.filter(user, context);
    }
    
    protected boolean accepts(String identifier) {
        return true;
        /*
        try {
            ComposedConfigProperty<Boolean> property = config.property(identifier, boolean.class);
            if(property.isDefined()) {
                return property.get();
            }
            return false;
        } catch (ConfigCascadeException e) {
            LOG.error(e.getMessage(), e);
            return false;
        }*/
    }
    
    protected SubscriptionSource filter(SubscriptionSource source) {
        return accepts(source.getId()) ? source : null;
    }
    
    protected List<SubscriptionSource> filter(List<SubscriptionSource> sources) {
        List<SubscriptionSource> filtered = new ArrayList<SubscriptionSource>(sources.size());
        for (SubscriptionSource subscriptionSource : sources) {
            if (accepts(subscriptionSource.getId())) {
                filtered.add(subscriptionSource);
            }
        }
        return filtered;
    }

}
