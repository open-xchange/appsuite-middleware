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

package com.openexchange.http.grizzly.service.http;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * {@link ServletFilterRegistrationTest}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public class ServletFilterRegistrationTest {

    private OSGiMainHandler handlerMock;

    ServletFilterRegistration instance;

    List<FilterProxy> addedProxies;

    @Before
    public void setUp() {
        ServletFilterRegistration.initInstance();
        handlerMock = Mockito.mock(OSGiMainHandler.class);
        ServletFilterRegistration.getInstance().setOSGiMainHandler(handlerMock);
        instance = ServletFilterRegistration.getInstance();
        addedProxies = new ArrayList<FilterProxy>();
    }

    @After
    public void tearDown() {
        ServletFilterRegistration.dropInstance();
    }

     @Test
    public void testPutAllMatch() {
        FilterProxy proxy0 = generateFilterProxy(new String[] { "/*" }, 0);
        instance.put(proxy0);
        addedProxies.add(proxy0);

        FilterProxy proxy1 = generateFilterProxy(null, 1);
        instance.put(proxy1);
        addedProxies.add(proxy1);

        List<FilterProxy> filterProxies = instance.getFilters();
        assertTrue(filterProxies.containsAll(addedProxies));
        assertEquals(proxy0, filterProxies.get(0));
        assertEquals(proxy1, filterProxies.get(1));
    }

    @Test
    public void testPutWildcardMatch() {
        FilterProxy proxy0 = generateFilterProxy(new String[] { "/a/*" }, 0);
        instance.put(proxy0);
        addedProxies.add(proxy0);

        FilterProxy proxy1 = generateFilterProxy(new String[] { "/a/b/*" }, 1);
        instance.put(proxy1);
        addedProxies.add(proxy1);

        FilterProxy proxy2 = generateFilterProxy(new String[] { "/a/b/c/*" }, 2);
        instance.put(proxy2);
        addedProxies.add(proxy2);

        FilterProxy proxy3 = generateFilterProxy(new String[] { "/a/b/c/d/*" }, 3);
        instance.put(proxy3);
        addedProxies.add(proxy3);

        // get all
        List<FilterProxy> filterProxies = instance.getFilters();
        assertTrue(filterProxies.containsAll(addedProxies));
        assertEquals(proxy0, filterProxies.get(0));
        assertEquals(proxy1, filterProxies.get(1));
        assertEquals(proxy2, filterProxies.get(2));
        assertEquals(proxy3, filterProxies.get(3));

        // get by path
        filterProxies = instance.getFilterProxies("/a/b/c/d/test");
        assertEquals("All four added filters should match", 4, filterProxies.size());
        assertEquals(proxy0, filterProxies.get(0));
        assertEquals(proxy1, filterProxies.get(1));
        assertEquals(proxy2, filterProxies.get(2));
        assertEquals(proxy3, filterProxies.get(3));

        filterProxies = instance.getFilterProxies("/a/b/c/test");
        assertEquals("Three added filters should match", 3, filterProxies.size());
        assertEquals(proxy0, filterProxies.get(0));
        assertEquals(proxy1, filterProxies.get(1));
        assertEquals(proxy2, filterProxies.get(2));

        filterProxies = instance.getFilterProxies("/a/b/test");
        assertEquals("Two added filters should match", 2, filterProxies.size());
        assertEquals(proxy0, filterProxies.get(0));
        assertEquals(proxy1, filterProxies.get(1));

        filterProxies = instance.getFilterProxies("/a/test");
        assertEquals("One added filters should match", 1, filterProxies.size());
        assertEquals(proxy0, filterProxies.get(0));

        filterProxies = instance.getFilterProxies("/a");
        assertEquals("No filters should match", 0, filterProxies.size());

    }

    @Test
    public void testPutExactMatch() {
        FilterProxy proxy0 = generateFilterProxy(new String[] { "/" }, 0);
        instance.put(proxy0);
        addedProxies.add(proxy0);

        FilterProxy proxy1 = generateFilterProxy(new String[] { "/a" }, 1);
        instance.put(proxy1);
        addedProxies.add(proxy1);

        FilterProxy proxy2 = generateFilterProxy(new String[] { "/a/b" }, 2);
        instance.put(proxy2);
        addedProxies.add(proxy2);

        FilterProxy proxy3 = generateFilterProxy(new String[] { "/a/b/c" }, 3);
        instance.put(proxy3);
        addedProxies.add(proxy3);

        FilterProxy proxy4 = generateFilterProxy(new String[] { "/a/b/c/d" }, 4);
        instance.put(proxy4);
        addedProxies.add(proxy4);

        // get all
        List<FilterProxy> filterProxies = instance.getFilters();
        assertTrue(filterProxies.containsAll(addedProxies));
        assertEquals(proxy0, filterProxies.get(0));
        assertEquals(proxy1, filterProxies.get(1));
        assertEquals(proxy2, filterProxies.get(2));
        assertEquals(proxy3, filterProxies.get(3));
        assertEquals(proxy4, filterProxies.get(4));

        // get by path
        filterProxies = instance.getFilterProxies("/a/b/c/d");
        assertEquals("Only one filter should match", 1, filterProxies.size());
        assertEquals(proxy4, filterProxies.get(0));

        filterProxies = instance.getFilterProxies("/a/b/c");
        assertEquals("Only one filter should match", 1, filterProxies.size());
        assertEquals(proxy3, filterProxies.get(0));
        
        filterProxies = instance.getFilterProxies("/a/b");
        assertEquals("Only one filter should match", 1, filterProxies.size());
        assertEquals(proxy2, filterProxies.get(0));

        filterProxies = instance.getFilterProxies("/a");
        assertEquals("Only one filter should match", 1, filterProxies.size());
        assertEquals(proxy1, filterProxies.get(0));

        filterProxies = instance.getFilterProxies("/");
        assertEquals("Only one filter should match", 1, filterProxies.size());
        assertEquals(proxy0, filterProxies.get(0));

        filterProxies = instance.getFilterProxies("/a/b/c/d/test");
        assertEquals("No filters should match prefix path", 0, filterProxies.size());

    }

    @Test
    public void testRemoveAllMatch() {
        FilterProxy proxy0 = generateFilterProxy(new String[] { "/*" }, 0);
        instance.put(proxy0);
        addedProxies.add(proxy0);
        
        FilterProxy proxy1 = generateFilterProxy(null, 1);
        instance.put(proxy1);
        addedProxies.add(proxy1);

        List<FilterProxy> filterProxies = instance.getFilters();
        assertTrue(filterProxies.containsAll(addedProxies));
        assertEquals(proxy0, filterProxies.get(0));
        assertEquals(proxy1, filterProxies.get(1));

        instance.remove(proxy0);
        filterProxies = instance.getFilters();
        assertEquals(1, filterProxies.size());
        assertEquals("Only one filter should remain", proxy1, filterProxies.get(0));

        instance.remove(proxy1);
        filterProxies = instance.getFilters();
        assertEquals("All filters should have been removed", 0, filterProxies.size());
    }

    @Test
    public void testRemoveWildcardMatch() {
        FilterProxy proxy0 = generateFilterProxy(new String[] { "/a/*" }, 0);
        instance.put(proxy0);
        addedProxies.add(proxy0);

        FilterProxy proxy1 = generateFilterProxy(new String[] { "/a/b/*" }, 1);
        instance.put(proxy1);
        addedProxies.add(proxy1);

        FilterProxy proxy2 = generateFilterProxy(new String[] { "/a/b/c/*" }, 2);
        instance.put(proxy2);
        addedProxies.add(proxy2);

        FilterProxy proxy3 = generateFilterProxy(new String[] { "/a/b/c/d/*" }, 3);
        instance.put(proxy3);
        addedProxies.add(proxy3);

        List<FilterProxy> filterProxies = instance.getFilters();
        assertTrue(filterProxies.containsAll(addedProxies));
        assertEquals(proxy0, filterProxies.get(0));
        assertEquals(proxy1, filterProxies.get(1));
        assertEquals(proxy2, filterProxies.get(2));
        assertEquals(proxy3, filterProxies.get(3));

        instance.remove(proxy0);
        filterProxies = instance.getFilters();
        assertEquals(3, filterProxies.size());
        assertEquals(proxy1, filterProxies.get(0));
        assertEquals(proxy2, filterProxies.get(1));
        assertEquals(proxy3, filterProxies.get(2));
        
        instance.remove(proxy2);
        filterProxies = instance.getFilters();
        assertEquals(2, filterProxies.size());
        assertEquals(proxy1, filterProxies.get(0));
        assertEquals(proxy3, filterProxies.get(1));
    }

    @Test
    public void testRemoveExactMatch() {
        FilterProxy proxy0 = generateFilterProxy(new String[] { "/" }, 0);
        instance.put(proxy0);
        addedProxies.add(proxy0);

        FilterProxy proxy1 = generateFilterProxy(new String[] { "/a" }, 1);
        instance.put(proxy1);
        addedProxies.add(proxy1);

        FilterProxy proxy2 = generateFilterProxy(new String[] { "/a/b" }, 2);
        instance.put(proxy2);
        addedProxies.add(proxy2);

        FilterProxy proxy3 = generateFilterProxy(new String[] { "/a/b/c" }, 3);
        instance.put(proxy3);
        addedProxies.add(proxy3);

        FilterProxy proxy4 = generateFilterProxy(new String[] { "/a/b/c/d" }, 4);
        instance.put(proxy4);
        addedProxies.add(proxy4);

        List<FilterProxy> filterProxies = instance.getFilters();
        assertTrue(filterProxies.containsAll(addedProxies));
        assertEquals(proxy0, filterProxies.get(0));
        assertEquals(proxy1, filterProxies.get(1));
        assertEquals(proxy2, filterProxies.get(2));
        assertEquals(proxy3, filterProxies.get(3));
        assertEquals(proxy4, filterProxies.get(4));

        instance.remove(proxy0);
        filterProxies = instance.getFilters();
        assertEquals(4, filterProxies.size());
        assertEquals(proxy1, filterProxies.get(0));
        assertEquals(proxy2, filterProxies.get(1));
        assertEquals(proxy3, filterProxies.get(2));
        assertEquals(proxy4, filterProxies.get(3));
        
        instance.remove(proxy2);
        filterProxies = instance.getFilters();
        assertEquals(3, filterProxies.size());
        assertEquals(proxy1, filterProxies.get(0));
        assertEquals(proxy3, filterProxies.get(1));
        assertEquals(proxy4, filterProxies.get(2));
        
        
    }

    private static FilterProxy generateFilterProxy(final String[] paths, final int ranking) {
        return new FilterProxy(new javax.servlet.Filter() {

            @Override
            public void destroy() {
            }

            @Override
            public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {
            }

            @Override
            public void init(FilterConfig arg0) throws ServletException {
            }

            @Override
            public String toString() {
                return paths + ", " + ranking;
            }

        }, paths, ranking);
    }

}
