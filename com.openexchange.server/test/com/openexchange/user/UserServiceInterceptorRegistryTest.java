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

package com.openexchange.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.user.AbstractUserServiceInterceptor;
import com.openexchange.user.UserServiceInterceptor;
import com.openexchange.user.UserServiceInterceptorRegistry;


/**
 * {@link UserServiceInterceptorRegistryTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class UserServiceInterceptorRegistryTest {

    private UserServiceInterceptorRegistry interceptorRegistry;
    private UserServiceInterceptor lowest;
    private UserServiceInterceptor middle;
    private UserServiceInterceptor highest;

    @Before
    public void before() {
        interceptorRegistry = new UserServiceInterceptorRegistry(null);
        lowest = new AbstractUserServiceInterceptor() {
            @Override
            public int getRanking() {
                return -1;
            }
        };
        middle = new AbstractUserServiceInterceptor() {
            @Override
            public int getRanking() {
                return 50;
            }
        };
        highest = new AbstractUserServiceInterceptor() {
            @Override
            public int getRanking() {
                return 100;
            }
        };
    }

    @Test
    public void testRanking() throws Exception {
        interceptorRegistry.addInterceptor(highest);
        interceptorRegistry.addInterceptor(middle);
        interceptorRegistry.addInterceptor(lowest);
        assertOrder();
        clearRegistry();

        interceptorRegistry.addInterceptor(lowest);
        interceptorRegistry.addInterceptor(middle);
        interceptorRegistry.addInterceptor(highest);
        assertOrder();
        clearRegistry();

        interceptorRegistry.addInterceptor(middle);
        interceptorRegistry.addInterceptor(lowest);
        interceptorRegistry.addInterceptor(highest);
        assertOrder();
    }

    private void assertOrder() {
        List<UserServiceInterceptor> interceptors = interceptorRegistry.getInterceptors();
        assertEquals("Wrong size", 3, interceptors.size());
        Iterator<UserServiceInterceptor> it = interceptors.iterator();
        assertTrue("Wrong order", it.next() == highest);
        assertTrue("Wrong order", it.next() == middle);
        assertTrue("Wrong order", it.next() == lowest);
    }

    private void clearRegistry() {
        interceptorRegistry.removeInterceptor(highest);
        interceptorRegistry.removeInterceptor(middle);
        interceptorRegistry.removeInterceptor(lowest);
    }

}
