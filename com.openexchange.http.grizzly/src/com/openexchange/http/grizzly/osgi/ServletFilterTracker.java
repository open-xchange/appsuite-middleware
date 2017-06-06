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

package com.openexchange.http.grizzly.osgi;

import static com.openexchange.servlet.Constants.FILTER_PATHS;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.http.grizzly.service.http.HttpServiceFactory;
import com.openexchange.http.grizzly.service.http.OSGiMainHandler;

/**
 * {@link ServletFilterTracker} - Tracks services with the type {@link Filter} and updates the central {@link OSGiMainHandler}
 * accordingly so the filters are applied to new incoming requests/outgoing responses.
 *
 * <p>
 * A Filter service may be registered with an additional <strong>com.openexchange.servlet.Constants.FILTER_PATHS</strong> property. This
 * property may consist of path expressions including wildcards. The path property should be provided as:
 *
 * <ol>
 *   <li>A single String for a single path</li>
 *   <li>An array of Strings</li>
 *   <li>A Collection of of Objects that provides the path via invocation of <cod>toString()</code></li>
 * </ol>
 *
 * if the filter.path property is missing/null the filter will be used for every incoming request.
 * </p>
 *
 * <p>
 * The form of a path must be one of:
 * <ol>
 *   <li>
 *     <strong>*</strong>: This filter will be applied to all request
 *   </li>
 *   <li>
 *     The path starts with <strong>/</strong> and ends with the <strong>/*</strong> wildcard but doesn't equal <strong>/*</strong> e.g.
 *     <strong>/a/b/*</strong>: This filter will be used for requests to all URLs starting with <strong>/a/b</strong> e.g
 *     <strong>/a/b/c</strong>, <strong>/a/b/c/d</strong> and so on
 *   </li>
 *   <li>
 *     The path starts with <strong>/</strong> but doesn't end with the <strong>/*</strong> wildcard: This filter will only be used for
 *     requests that match this path exactly
 *   </li>
 * </ol>
 * </p>
 *
 * <h4>Example:</h4>
 * <pre>
 * {@code
 * public class ServletFilterActivator extends HousekeepingActivator {
 *
 *  {@literal @}Override
 *  protected Class<?>[] getNeededServices() {
 *      return new Class[] { HttpService.class };
 *  }
 *
 *  {@literal @}Override
 *  protected void startBundle() throws Exception {
 *      Filter yourFilter = new Filter() {
 *
 *          {@literal @}Override
 *          public void destroy() {
 *          }
 *
 *          {@literal @}Override
 *          public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
 *              String world = request.getParameter("hello");
 *              filterChain.doFilter(request, response);
 *          }
 *
 *          {@literal @}Override
 *          public void init(FilterConfig config) throws ServletException {
 *          }
 *      };
 *
 *      Hashtable<String, Object> serviceProperties = new Hashtable<String, Object>();
 *      serviceProperties.put(Constants.SERVICE_RANKING, 0);
 *      serviceProperties.put(FILTER_PATHS, "*");
 *
 *      registerService(Filter.class, yourFilter, serviceProperties);
 *}
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.6.1
 */
public class ServletFilterTracker implements ServiceTrackerCustomizer<Filter, Filter> {

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
    private final HttpServiceFactory httpServiceFactory;
    private final Map<Filter, List<Filter>> wrappersFor;

    /**
     * Initializes a new {@link ServletFilterTracker}.
     */
    public ServletFilterTracker(HttpServiceFactory httpServiceFactory, BundleContext context) {
        super();
        this.httpServiceFactory = httpServiceFactory;
        this.context = context;
        wrappersFor = new HashMap<>(8, 0.9F);
    }

    @Override
    public synchronized Filter addingService(ServiceReference<Filter> reference) {
        try {
            Filter filter = context.getService(reference);

            String[] paths = getPathsFrom(reference);
            if (null == paths) {
                httpServiceFactory.addServletFilter(filter, "/*");
            } else {
                int len = paths.length;
                if (len == 0) {
                    httpServiceFactory.addServletFilter(filter, "/*");
                } else if (len == 1) {
                    httpServiceFactory.addServletFilter(filter, paths[0]);
                } else {
                    List<Filter> wrappers = new LinkedList<>();
                    boolean error = true;
                    try {
                        httpServiceFactory.addServletFilter(filter, paths[0]);
                        wrappers.add(filter);

                        // Create new wrapper instances for additional paths to not mess up Grizzly
                        for (int i = 1; i < len; i++) {
                            Filter wrapper = new WrappingFilter(filter);
                            httpServiceFactory.addServletFilter(wrapper, paths[i]);
                            wrappers.add(wrapper);
                        }

                        // Everything went well... Only keep real wrapper instances in list
                        wrappers.remove(0);
                        wrappersFor.put(filter, wrappers);

                        error = false;
                    } finally {
                        if (error) {
                            for (Filter toRemove : wrappers) {
                                removeFilterSafe(toRemove);
                            }
                        }
                    }
                }
            }

            return filter;
        } catch (InvalidFilterPathsException e) {
            LOG.error("Not adding servlet filter because of malformed path.info", e);
            context.ungetService(reference);
            return null;
        } catch (Exception e) {
            LOG.error("Failed to register filter", e);
            context.ungetService(reference);
            return null;
        }
    }

    @Override
    public void modifiedService(ServiceReference<Filter> reference, Filter filter) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<Filter> reference, Filter filter) {
        removeFilterSafe(filter);

        List<Filter> wrappers = wrappersFor.remove(filter);
        if (null != wrappers) {
            for (Filter wrapper : wrappers) {
                removeFilterSafe(wrapper);
            }
        }

        context.ungetService(reference);
    }

    private void removeFilterSafe(Filter toRemove) {
        try {
            httpServiceFactory.removeServletFilter(toRemove);
        } catch (Exception e) {
            // Ignore
        }
    }

    private String[] getPathsFrom(ServiceReference<Filter> reference) throws InvalidFilterPathsException {
        final Object filterPathObj = reference.getProperty(FILTER_PATHS);
        if (filterPathObj instanceof String) {
            return filterPathObj.toString().equals("*") ? null : new String[] { filterPathObj.toString() };
        } else if (filterPathObj instanceof String[]) {
            // check if one value matches '*'
            final String[] values = (String[]) filterPathObj;
            boolean matchAll = false;
            for (int i = 0; i < values.length; i++) {
                if ("*".equals(values[i])) {
                    matchAll = true;
                }
            }
            return matchAll ? null : values;
        } else if (filterPathObj instanceof Collection) {
            final Collection<?> col = (Collection<?>) filterPathObj;
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
        } else if (filterPathObj == null) {
            return null;
        } else {
            LOG.warn("Invalid filter paths : Neither of type String nor String[] : {} - Ignoring ServiceReference [{} | Bundle({})]", filterPathObj.getClass().getName(), reference, reference.getBundle());
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
                if (oRanking instanceof Integer) {
                    ranking = ((Integer) oRanking).intValue();
                } else {
                    try {
                        ranking = Integer.parseInt(oRanking.toString().trim());
                    } catch (final NumberFormatException e) {
                        ranking = defaultRanking;
                    }
                }
            }
        }
        return ranking;
    }

    // ------------------------------------------------------------------------------------------------------------------------------- //

    private static class WrappingFilter implements Filter {

        private final Filter delegate;

        WrappingFilter(Filter delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            delegate.init(filterConfig);
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            delegate.doFilter(request, response, chain);
        }

        @Override
        public void destroy() {
            delegate.destroy();
        }
    }

}
