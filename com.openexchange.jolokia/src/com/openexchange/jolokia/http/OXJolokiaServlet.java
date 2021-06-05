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

package com.openexchange.jolokia.http;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.jolokia.config.ConfigKey;
import org.jolokia.osgi.servlet.JolokiaServlet;
import org.jolokia.restrictor.Restrictor;
import org.osgi.framework.BundleContext;
import com.openexchange.jolokia.log.OXJolokiaLogHandler;


/**
 *
 * Extends {@link JolokiaServlet} to add {@link OXJolokiaLogHandler} as Jolokias default logger
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.8.3
 */
public class OXJolokiaServlet extends JolokiaServlet {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4743473900278611090L;

    public OXJolokiaServlet() {
        this(null);
    }

    public OXJolokiaServlet(BundleContext pContext) {
        this(pContext, null);
    }

    public OXJolokiaServlet(BundleContext pContext, Restrictor pRestrictor) {
        super(pContext, pRestrictor);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void init(ServletConfig pServletConfig) throws ServletException {
        Map<String, String> map = new HashMap<String, String>();
        Enumeration e = pServletConfig.getInitParameterNames(); // Get servlet config
        while (e.hasMoreElements()) {
            String keyS = (String) e.nextElement();
            ConfigKey key = ConfigKey.getGlobalConfigKey(keyS);
            if (key != null) {
                map.put(keyS, pServletConfig.getInitParameter(keyS)); // Put known attributes into map
            }
        }

        // Add the OXJolokiaLogHandler to map
        map.put(ConfigKey.LOGHANDLER_CLASS.toString(), OXJolokiaLogHandler.class.getName());

        // Generate new servlet config and let Jolokia do its work
        SimpleServletConfig servletConfigWithOXLogger = new SimpleServletConfig(pServletConfig.getServletName(), pServletConfig.getServletContext(), map);
        super.init(servletConfigWithOXLogger);
    }

    private static final class SimpleServletConfig implements ServletConfig {

        private final String servletName;
        private final ServletContext servletContext;
        private final Map<String, String> initParameters;

        /**
         * Initializes a new {@link SimpleServletConfig}.
         */
        SimpleServletConfig(String servletName, ServletContext servletContext, Map<String, String> initParameters) {
            super();
            this.servletName = servletName;
            this.servletContext = servletContext;
            this.initParameters = initParameters;
        }

        @Override
        public String getServletName() {
            return servletName;
        }

        @Override
        public ServletContext getServletContext() {
            return servletContext;
        }

        @Override
        public String getInitParameter(String name) {
            return null == name ? null : initParameters.get(name);
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return new IteratorEnumeration<String>(initParameters.keySet().iterator());
        }

    }

    private static class IteratorEnumeration<E> implements Enumeration<E> {

        /** The iterator being decorated. */
        private final Iterator<E> iterator;

        /**
         * Constructs a new <code>IteratorEnumeration</code> that will use the given iterator.
         *
         * @param iterator  the iterator to use
         */
        IteratorEnumeration(Iterator<E> iterator ) {
            super();
            this.iterator = iterator;
        }

        // Iterator interface
        //-------------------------------------------------------------------------

        /**
         *  Returns true if the underlying iterator has more elements.
         *
         *  @return true if the underlying iterator has more elements
         */
        @Override
        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        /**
         *  Returns the next element from the underlying iterator.
         *
         *  @return the next element from the underlying iterator.
         *  @throws java.util.NoSuchElementException  if the underlying iterator has no
         *    more elements
         */
        @Override
        public E nextElement() {
            return iterator.next();
        }
    }
}
