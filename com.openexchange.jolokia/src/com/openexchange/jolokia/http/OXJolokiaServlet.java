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

        // Properties
        //-------------------------------------------------------------------------

        /**
         *  Returns the underlying iterator.
         *
         *  @return the underlying iterator
         */
        public Iterator<E> getIterator() {
            return iterator;
        }
    }

}
