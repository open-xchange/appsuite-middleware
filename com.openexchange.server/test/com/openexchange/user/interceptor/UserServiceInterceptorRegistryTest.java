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

package com.openexchange.user.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;


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
     public void testRanking() {
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
