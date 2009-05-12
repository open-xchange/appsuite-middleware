package com.openexchange.publish;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;
import junit.framework.TestCase;


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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

/**
 * {@link CompositePublicationTargetDiscovererTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class CompositePublicationTargetDiscovererTest extends TestCase {
 
    private CompositePublicationTargetDiscoveryService composite;

    public void setUp() throws Exception {
        SimPublicationTargetDiscoveryService discovery1 = new SimPublicationTargetDiscoveryService();
        SimPublicationTargetDiscoveryService discovery2 = new SimPublicationTargetDiscoveryService();
        
        discovery1.addTarget(target("com.openexchange.publish.test1"));
        discovery1.addTarget(target("com.openexchange.publish.test2"));
        discovery1.addTarget(target("com.openexchange.publish.test3"));
    
        discovery2.addTarget(target("com.openexchange.publish.test4"));
        discovery2.addTarget(target("com.openexchange.publish.test5"));
    
        composite = new CompositePublicationTargetDiscoveryService();
        
        composite.addDiscoveryService(discovery1);
        composite.addDiscoveryService(discovery2);
    }
    
    public void testCompositeList() {
        List<PublicationTarget> targets = composite.listTargets();
        
        assertTargets(targets, "com.openexchange.publish.test1", "com.openexchange.publish.test2", "com.openexchange.publish.test3", "com.openexchange.publish.test4", "com.openexchange.publish.test5");
    }
    
    public void testCompositeKnows() {
        assertKnows(composite, "com.openexchange.publish.test1");
        assertKnows(composite, "com.openexchange.publish.test2");
        assertKnows(composite, "com.openexchange.publish.test3");
        assertKnows(composite, "com.openexchange.publish.test4");
        assertKnows(composite, "com.openexchange.publish.test5");
        
        assertDoesNotKnow(composite, "com.openexchange.publish.unknown");
    }
    
    public void testCompositeGet() {
        assertGettable(composite, "com.openexchange.publish.test1");
        assertGettable(composite, "com.openexchange.publish.test2");
        assertGettable(composite, "com.openexchange.publish.test3");
        assertGettable(composite, "com.openexchange.publish.test4");
        assertGettable(composite, "com.openexchange.publish.test5");
    
        assertNotGettable(composite, "com.openexchange.publish.unknown");
    }

    private void assertNotGettable(PublicationTargetDiscoveryService discovery, String id) {
        assertFalse("Did not expect to find "+id+" in discovery source", discovery.getTarget(id) != null);
    }

    private void assertGettable(PublicationTargetDiscoveryService discovery, String id) {
        assertNotNull("Could not find "+id+" in discovery source", discovery.getTarget(id));
    }

    private void assertKnows(PublicationTargetDiscoveryService discovery, String id) {
        assertTrue("Did not know: "+id, discovery.knows(id));
    }

    private void assertDoesNotKnow(PublicationTargetDiscoveryService discovery, String id) {
        assertFalse("Did know: "+id, discovery.knows(id));
    }

    private void assertTargets(List<PublicationTarget> targets, String...ids) {
        assertNotNull("Target list was null", targets);
        
        Set<String> actualIds = new HashSet<String>();
        for(PublicationTarget target : targets) {
            actualIds.add(target.getId());
        }
        
        Set<String> expectedIds = new HashSet<String>();
        for(String id : ids) {
            expectedIds.add(id);
        }
        
        String error = "Expected: "+expectedIds+" Got: "+actualIds;
        
        assertEquals(error, actualIds.size(), expectedIds.size());
        
        for(String expectedId : expectedIds) {
            assertTrue(error, actualIds.remove(expectedId));
        }
    }

    private PublicationTarget target(String id) {
        PublicationTarget publicationTarget = new PublicationTarget();
        publicationTarget.setId(id);
        return publicationTarget;
    }
    
}
 