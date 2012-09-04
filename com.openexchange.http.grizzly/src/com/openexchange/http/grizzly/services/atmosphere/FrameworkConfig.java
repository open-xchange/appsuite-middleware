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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.http.grizzly.services.atmosphere;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * {@link FrameworkConfig} - ConfigBuilder with fluent interface. Use like: <code>
 * FrameworkConfig.with("Atmosphere Servlet", realtimeContext).and("org.atmosphere.cpr.CometSupport.maxInactiveActivity", "100000").build();
 * </code>
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class FrameworkConfig {

    private Map<String, String> parameters = null;

    private String servletName = null;

    private ServletContext servletContext = null;

    private FrameworkConfig(String servletName, ServletContext servletContext) {
        this.parameters = new HashMap<String, String>();
        this.servletContext = servletContext;
        this.servletName = servletName;
    }
    
    /**
     * Public constructor to create an empty FrameworkConfig.
     * @param servletName the name of the Atmosphere servlet
     * @param servletContext the context where the servlet is going to be destroyed
     * @return an empty FrameworkConfig that can be filled with <code>.and</code>
     */
    public static FrameworkConfig with(String servletName, ServletContext servletContext) {
        return new FrameworkConfig(servletName, servletContext);
    }

    /**
     * Add a named parameter and its value to this FrameworkConfig.
     * @param name the name of the paramter to add
     * @param value the value of the parameter to add
     * @return the FrameworkConfig with name:value pair added
     */
    public FrameworkConfig and(String name, String value) {
        this.parameters.put(name, value);
        return this;
    }

    /**
     * Build a ServletConfig from this FrameworkConfig that can be used to configure the Atmosphere framework.
     * @return a ServletConfig that can be used to configure the Atmosphere framework.
     */
    public ServletConfig build() {
        return new ServletConfig() {

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
                return parameters.get(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return Collections.enumeration(parameters.keySet());
            }
        };
    }

}
