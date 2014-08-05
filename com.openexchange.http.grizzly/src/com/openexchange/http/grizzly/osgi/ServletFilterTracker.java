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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.http.grizzly.osgi;

import java.util.Collection;
import java.util.Iterator;
import javax.servlet.Filter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.http.grizzly.service.http.FilterProxy;
import com.openexchange.http.grizzly.service.http.ServletFilterRegistration;

/**
 * {@link ServletFilterTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class ServletFilterTracker implements ServiceTrackerCustomizer<Filter, FilterProxy> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ServletFilterTracker.class);

    private static final class InvalidFilterPathsException extends Exception {

        private static final long serialVersionUID = 8247656654408196913L;

        InvalidFilterPathsException() {
            super();
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------- //

    private final BundleContext context;

    /**
     * Initializes a new {@link ServletFilterTracker}.
     */
    public ServletFilterTracker(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public FilterProxy addingService(ServiceReference<Filter> reference) {
        try {
            Filter filter = context.getService(reference);
            String[] paths = getPathsFrom(reference);

            FilterProxy proxy = new FilterProxy(filter, paths, getRanking(reference));
            ServletFilterRegistration.getInstance().put(proxy);

            return proxy;
        } catch (InvalidFilterPathsException e) {
            context.ungetService(reference);
            return null;
        }
    }

    @Override
    public void modifiedService(ServiceReference<Filter> reference, FilterProxy proxy) {
        // Nothing
    }

    @Override
    public void removedService(ServiceReference<Filter> reference, FilterProxy proxy) {
        ServletFilterRegistration.getInstance().remove(proxy);
        context.ungetService(reference);
    }

    private String[] getPathsFrom(ServiceReference<Filter> reference) throws InvalidFilterPathsException {
        final Object topicObj = reference.getProperty("filter.paths");
        if (topicObj instanceof String) {
            return topicObj.toString().equals("*") ? null : new String[] { topicObj.toString() };
        } else if (topicObj instanceof String[]) {
            // check if one value matches '*'
            final String[] values = (String[]) topicObj;
            boolean matchAll = false;
            for (int i = 0; i < values.length; i++) {
                if ("*".equals(values[i])) {
                    matchAll = true;
                }
            }
            return matchAll ? null : values;
        } else if (topicObj instanceof Collection) {
            final Collection<?> col = (Collection<?>) topicObj;
            final String[] values = new String[col.size()];
            int index = 0;
            // check if one value matches '*'
            final Iterator<?> i = col.iterator();
            boolean matchAll = false;
            while (i.hasNext()) {
                final String v = i.next().toString();
                values[index] = v;
                index++;
                if ("*".equals(v)) {
                    matchAll = true;
                }
            }
            return matchAll ? null : values;
        } else if (topicObj == null) {
            return null;
        } else {
            LOG.warn("Invalid filter paths : Neither of type String nor String[] : {} - Ignoring ServiceReference [{} | Bundle({})]", topicObj.getClass().getName(), reference, reference.getBundle());
            throw new InvalidFilterPathsException();
        }
    }

    /**
     * Gets the service ranking by look-up of <code>"service.ranking"</code> property.
     * <p>
     * See {@link Constants#SERVICE_RANKING}.
     *
     * @param reference The service reference providing properties Dictionary object of the service
     * @return The ranking or <code>0</code> (zero) if absent
     */
    private <S> int getRanking(final ServiceReference<S> reference) {
        return getRanking(reference, 0);
    }

    /**
     * Gets the service ranking by look-up of <code>"service.ranking"</code> property.
     * <p>
     * See {@link Constants#SERVICE_RANKING}.
     *
     * @param reference The service reference providing properties Dictionary object of the service
     * @param defaultRanking The default ranking if {@link Constants#SERVICE_RANKING} property is absent
     * @return The ranking or <code>0</code> (zero) if absent
     */
    private <S> int getRanking(final ServiceReference<S> reference, final int defaultRanking) {
        int ranking = defaultRanking;
        {
            final Object oRanking = reference.getProperty(Constants.SERVICE_RANKING);
            if (null != oRanking) {
                try {
                    ranking = Integer.parseInt(oRanking.toString().trim());
                } catch (final NumberFormatException e) {
                    ranking = defaultRanking;
                }
            }
        }
        return ranking;
    }

}
