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

package com.openexchange.tools.service;

import junit.framework.TestCase;


/**
 * {@link SpecificServiceChooserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SpecificServiceChooserTest extends TestCase {

    private SpecificServiceChooser<TestService> specificServiceChooser;

    @Override
    public void setUp() {
        specificServiceChooser = new SpecificServiceChooser<TestService>();
    }

    public void testChooseMostSpecificService() throws ServicePriorityConflictException {
        specificServiceChooser.registerForEverything(new TestServiceImpl(1), 1);

        specificServiceChooser.registerForEverything(new TestServiceImpl(80), 0);

        specificServiceChooser.registerForContext(new TestServiceImpl(2), 0, 1337);
        specificServiceChooser.registerForContextAndFolder(new TestServiceImpl(3), 0, 1337, 12);
        specificServiceChooser.registerForFolder(new TestServiceImpl(4), 2, 13);

        assertChooses(1, 1338, 2);
        assertChooses(2, 1337, 2);
        assertChooses(3, 1337, 12);
        assertChooses(4, 1338, 13);
        assertChooses(4, 1337, 13);

    }

    public void testChooseMostSpecificServiceWithStringIDs() throws ServicePriorityConflictException {
        specificServiceChooser.registerForEverything(new TestServiceImpl(1), 1);
        specificServiceChooser.registerForEverything(new TestServiceImpl(80), 0);

        specificServiceChooser.registerForContext(new TestServiceImpl(2), 0, 1337);
        specificServiceChooser.registerForContextAndFolder(new TestServiceImpl(3), 0, 1337, "gnitz");
        specificServiceChooser.registerForFolder(new TestServiceImpl(4), 2, "batz");

        assertChooses(1, 1338, "2");
        assertChooses(2, 1337, "2");
        assertChooses(3, 1337, "gnitz");
        assertChooses(4, 1338, "batz");
        assertChooses(4, 1337, "batz");

    }


    // Test removing services

    public void testRemovingServices() throws ServicePriorityConflictException {
        TestServiceImpl serviceInstance = new TestServiceImpl(1);
        specificServiceChooser.registerForEverything(serviceInstance, 1);
        specificServiceChooser.registerForEverything(new TestServiceImpl(80), 0);
        specificServiceChooser.removeForEverything(serviceInstance);

        assertChooses(80, 1338, "2");

        specificServiceChooser = new SpecificServiceChooser<TestService>();
        specificServiceChooser.registerForContext(new TestServiceImpl(2), 2, 1337);
        specificServiceChooser.registerForContext(new TestServiceImpl(82), 0, 1337);
        specificServiceChooser.removeForContext(new TestServiceImpl(2), 1337);

        assertChooses(82, 1337, "bla");

        specificServiceChooser = new SpecificServiceChooser<TestService>();
        specificServiceChooser.registerForContextAndFolder(new TestServiceImpl(2), 2, 1337, 12);
        specificServiceChooser.registerForContextAndFolder(new TestServiceImpl(84), 0, 1337, 12);
        specificServiceChooser.removeForContextAndFolder(new TestServiceImpl(2), 1337, 12);

        assertChooses(84, 1337, 12);

        specificServiceChooser = new SpecificServiceChooser<TestService>();
        specificServiceChooser.registerForFolder(new TestServiceImpl(2), 2, 12);
        specificServiceChooser.registerForFolder(new TestServiceImpl(84), 0, 12);
        specificServiceChooser.removeForFolder(new TestServiceImpl(2), 12);

        assertChooses(84, 1337, 12);

    }

    public void testConflictingServices() throws ServicePriorityConflictException {
        specificServiceChooser.registerForContext(new TestServiceImpl(2), 0, 1337);
        specificServiceChooser.registerForFolder(new TestServiceImpl(4), 0, 13);

        try {
            specificServiceChooser.choose(1337, 13);
            fail("Should have thrown Exception");
        } catch (ServicePriorityConflictException x) {
            // Hooray!
        }

        try {
            specificServiceChooser.registerForFolder(new TestServiceImpl(5), 0, 13);
            fail("Should have thrown Exception");
        } catch (ServicePriorityConflictException x) {
            // Hooray!
        }


    }

    private void assertChooses(int expected, int cid, int folderId) throws ServicePriorityConflictException {
        TestService chosen = specificServiceChooser.choose(cid, folderId);
        assertEquals("Wrong service chosen for "+cid+" : "+folderId, expected, chosen.getId());

    }

    private void assertChooses(int expected, int cid, String folderId) throws ServicePriorityConflictException {
        TestService chosen = specificServiceChooser.choose(cid, folderId);
        assertEquals("Wrong service chosen for "+cid+" : "+folderId, expected, chosen.getId());

    }

    private static interface TestService {
        public int getId();
    }

    private static final class TestServiceImpl implements TestService {

        private final int id;

        public TestServiceImpl(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            return id == ((TestServiceImpl)obj).id;
        }

    }

}

