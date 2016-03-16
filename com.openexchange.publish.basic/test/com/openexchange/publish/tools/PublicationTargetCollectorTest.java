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

package com.openexchange.publish.tools;

import static com.openexchange.publish.Asserts.assertDoesNotKnow;
import static com.openexchange.publish.Asserts.assertGettable;
import static com.openexchange.publish.Asserts.assertKnows;
import static com.openexchange.publish.Asserts.assertTargets;
import java.util.Collection;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.SimPublicationService;

/**
 * {@link PublicationTargetCollectorTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class PublicationTargetCollectorTest extends TestCase {



    private PublicationTargetCollector collector;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        collector = new PublicationTargetCollector();

        PublicationTarget target1 = new PublicationTarget();
        target1.setModule("Cookies");
        target1.setId("com.openexchange.publish.test1");

        PublicationTarget target2 = new PublicationTarget();
        target2.setId("com.openexchange.publish.test2");
        target2.setModule("Vegetables");

        SimPublicationService pubService1 = new SimPublicationService();
        pubService1.setTarget(target1);
        target1.setPublicationService(pubService1);

        SimPublicationService pubService2 = new SimPublicationService() {
            @Override
            public boolean knows(Context ctx, int publicationId) {
                return publicationId == 12;
            }
        };
        pubService2.setTarget(target2);
        target2.setPublicationService(pubService2);

        collector.addPublicationService(pubService1);
        collector.addPublicationService(pubService2);

    }

    public void testList() {
        Collection<PublicationTarget> targets = collector.listTargets();
        assertNotNull("Targets was null", targets);
        assertEquals("Expected two publication targets", 2, targets.size());
        assertTargets(targets, "com.openexchange.publish.test1", "com.openexchange.publish.test2");
    }

    public void testGetTarget() throws OXException {
        assertGettable(collector, "com.openexchange.publish.test1");
    }

    public void testKnows() throws OXException {
        assertKnows(collector, "com.openexchange.publish.test1");
        assertDoesNotKnow(collector, "com.openexchange.publish.unknown");
    }

    public void testGetTargetForEntity() throws OXException {
        PublicationTarget target = collector.getTarget(new SimContext(1), 12);
        assertNotNull("Target was null!", target);
        assertEquals("com.openexchange.publish.test2", target.getId());
    }

    public void testGetTargetForModule() {
        Collection<PublicationTarget> targets = collector.getTargetsForEntityType("Cookies");
        assertNotNull("targets was null", targets);
        assertTargets(targets, "com.openexchange.publish.test1");
    }
}
