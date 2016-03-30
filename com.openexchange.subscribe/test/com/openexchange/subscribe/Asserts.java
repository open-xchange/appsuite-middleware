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

package com.openexchange.subscribe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * {@link Asserts}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class Asserts {
    public static void assertKnows(SubscriptionSourceDiscoveryService discoverer, String id) {
        assertTrue("Did not know: "+id, discoverer.knowsSource(id));
    }

    public static void assertDoesNotKnow(SubscriptionSourceDiscoveryService discoverer, String id) {
        assertFalse("Did know: "+id, discoverer.knowsSource(id));
    }

    public static void assertSources(List<SubscriptionSource> sources, String...expectedIdentifiers) {
        List<String> identifier = new ArrayList<String>();
        for(SubscriptionSource source : sources) {
            identifier.add(source.getId());
        }
        List<String> expectedList = Arrays.asList(expectedIdentifiers);
        assertEquals("Expected: "+expectedList+" Got: "+identifier, sources.size(), expectedIdentifiers.length);

        Set<String> actual = new HashSet<String>(identifier);
        for(String expected : expectedIdentifiers) {
            assertTrue("Expected: "+expectedList+" Got: "+identifier +" Missing: "+expected, actual.remove(expected));
        }
        assertTrue("Expected: "+expectedList+" Got: "+identifier, actual.isEmpty());

    }

    public static void assertPriority(List<SubscriptionSource> sources, String identifier, int priority) {
        for (SubscriptionSource subscriptionSource : sources) {
            if(subscriptionSource.getId().equals(identifier)) {
                assertEquals(priority, subscriptionSource.getPriority());
                return;
            }
        }
        fail("Did not found subscription source with identifier "+identifier);
    }

    public static void assertPriority(SubscriptionSource source, int priority) {
        assertEquals(priority, source.getPriority());
    }
}
