package com.openexchange.publish.tools;

import static com.openexchange.publish.Asserts.assertDoesNotKnow;
import static com.openexchange.publish.Asserts.assertGettable;
import static com.openexchange.publish.Asserts.assertKnows;
import static com.openexchange.publish.Asserts.assertNotGettable;
import static com.openexchange.publish.Asserts.assertTargets;
import java.util.Collection;
import java.util.List;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.SimPublicationService;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;

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

/**
 * {@link CompositePublicationTargetDiscovererTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class CompositePublicationTargetDiscovererTest extends TestCase {

    private CompositePublicationTargetDiscoveryService composite;

    @Override
    public void setUp() throws Exception {
        SimPublicationTargetDiscoveryService discovery1 = new SimPublicationTargetDiscoveryService();
        SimPublicationTargetDiscoveryService discovery2 = new SimPublicationTargetDiscoveryService();

        discovery1.addTarget(target("com.openexchange.publish.test1"));
        discovery1.addTarget(target("com.openexchange.publish.test2"));
        discovery1.addTarget(target("com.openexchange.publish.test3"));

        discovery2.addTarget(target("com.openexchange.publish.test4"));
        discovery2.addTarget(target("com.openexchange.publish.test5"));

        PublicationTarget publicationTarget = new PublicationTarget();
        publicationTarget.setId("com.openexchange.publish.knowAll");
        publicationTarget.setModule("not infostore");
        publicationTarget.setPublicationService(new SimPublicationService() {
            @Override
            public boolean knows(Context ctx, int publicationId) {
                return true;
            }
        });
        discovery2.addTarget(publicationTarget);

        composite = new CompositePublicationTargetDiscoveryService();

        composite.addDiscoveryService(discovery1);
        composite.addDiscoveryService(discovery2);
    }

    public void testCompositeList() throws OXException {
        List<PublicationTarget> targets = composite.listTargets();

        assertTargets(targets, "com.openexchange.publish.test1", "com.openexchange.publish.test2", "com.openexchange.publish.test3", "com.openexchange.publish.test4", "com.openexchange.publish.test5", "com.openexchange.publish.knowAll");
    }

    public void testCompositeKnows() throws OXException {
        assertKnows(composite, "com.openexchange.publish.test1");
        assertKnows(composite, "com.openexchange.publish.test2");
        assertKnows(composite, "com.openexchange.publish.test3");
        assertKnows(composite, "com.openexchange.publish.test4");
        assertKnows(composite, "com.openexchange.publish.test5");

        assertDoesNotKnow(composite, "com.openexchange.publish.unknown");
    }

    public void testCompositeGet() throws OXException {
        assertGettable(composite, "com.openexchange.publish.test1");
        assertGettable(composite, "com.openexchange.publish.test2");
        assertGettable(composite, "com.openexchange.publish.test3");
        assertGettable(composite, "com.openexchange.publish.test4");
        assertGettable(composite, "com.openexchange.publish.test5");

        assertNotGettable(composite, "com.openexchange.publish.unknown");
    }

    public void testGetTarget() throws OXException {
        PublicationTarget target = composite.getTarget(null, -1);
        assertNotNull(target);
        assertEquals("com.openexchange.publish.knowAll", target.getId());
    }

    public void testGetResponsibleTargets() throws OXException {
        Collection<PublicationTarget> targets = composite.getTargetsForEntityType("infostore");
        assertTargets(targets, "com.openexchange.publish.test1", "com.openexchange.publish.test2", "com.openexchange.publish.test3", "com.openexchange.publish.test4", "com.openexchange.publish.test5");

    }


    private PublicationTarget target(String id) {
        PublicationTarget publicationTarget = new PublicationTarget();
        publicationTarget.setId(id);
        publicationTarget.setPublicationService(new SimPublicationService());
        publicationTarget.setModule("infostore");
        return publicationTarget;
    }

}
