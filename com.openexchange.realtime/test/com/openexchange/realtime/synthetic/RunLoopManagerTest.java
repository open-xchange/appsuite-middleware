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

package com.openexchange.realtime.synthetic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Before;
import org.junit.Test;
import com.google.common.base.Optional;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import com.openexchange.realtime.Component;
import com.openexchange.realtime.ComponentHandle;
import com.openexchange.realtime.LoadFactorCalculator;
import com.openexchange.realtime.SimServiceLookup;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;

/**
 * {@link RunLoopManagerTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.2
 */
public class RunLoopManagerTest {

    private RunLoopManager runLoopManager;
    private ManagerTestComponent1 component1;
    private ManagerTestComponent2 component2;
    private final int LOOP_QUANTITY = 5;
    private final int HANDLER_QUANTITY = 20;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        runLoopManager = new RunLoopManager(new SimServiceLookup());
        component1 = new ManagerTestComponent1();
        component2 = new ManagerTestComponent2();
        runLoopManager.createRunLoops(component1, LOOP_QUANTITY);
        runLoopManager.createRunLoops(component2, LOOP_QUANTITY);
    }

    /**
     * Test method for {@link com.openexchange.realtime.synthetic.RunLoopManager#createRunLoops(com.openexchange.realtime.Component, int)}.
     *
     * @throws Exception
     */
    @Test
    public void testCreateRunLoops() throws Exception {
        ListMultimap<String, SyntheticChannelRunLoop> loopClusters = getloopClusters(runLoopManager);
        assertEquals(LOOP_QUANTITY * 2, loopClusters.size());
        List<SyntheticChannelRunLoop> cluster1 = loopClusters.get(component1.getId());
        List<SyntheticChannelRunLoop> cluster2 = loopClusters.get(component2.getId());
        assertEquals(LOOP_QUANTITY, cluster1.size());
        assertEquals(LOOP_QUANTITY, cluster2.size());
        assertTrue(Sets.intersection(Sets.newHashSet(cluster1), Sets.newHashSet(cluster2)).isEmpty());
    }

    /**
     * Test method for {@link com.openexchange.realtime.synthetic.RunLoopManager#getRunLoopForID(com.openexchange.realtime.packet.ID, boolean)}.
     *
     * @throws Exception
     */
    @Test
    public void testGetRunLoopForIDIDBoolean() throws Exception {
        ComponentHandle handle1 = component1.create();
        Optional<SyntheticChannelRunLoop> runLoopForHandle1 = runLoopManager.getRunLoopForID(handle1.getId());
        assertFalse(runLoopForHandle1.isPresent());
        runLoopForHandle1 = runLoopManager.getRunLoopForID(handle1.getId(), true);
        assertTrue(runLoopForHandle1.isPresent());
        assertTrue(getRunLoopsForComponent(runLoopManager, component1).contains(runLoopForHandle1.get()));
    }

    /**
     * Test method for {@link com.openexchange.realtime.synthetic.RunLoopManager#removeIDFromRunLoop(com.openexchange.realtime.packet.ID)}.
     *
     * @throws Exception
     */
    @Test
    public void testRemoveIDFromRunLoop() throws Exception {
        ComponentHandle handle1 = component1.create();
        Optional<SyntheticChannelRunLoop> runLoopForHandle1 = runLoopManager.getRunLoopForID(handle1.getId(), true);
        assertTrue(runLoopForHandle1.isPresent());
        assertTrue(getRunLoopsForComponent(runLoopManager, component1).contains(runLoopForHandle1.get()));
        assertTrue(getloopMap(runLoopManager).contains(runLoopForHandle1.get()));
        runLoopManager.removeIDFromRunLoop(handle1.getId());
        assertFalse(getloopMap(runLoopManager).contains(runLoopForHandle1.get()));
    }

    /**
     * Test method for {@link com.openexchange.realtime.synthetic.RunLoopManager#destroyRunLoops(com.openexchange.realtime.Component)}.
     *
     * @throws Exception
     */
    @Test
    public void testDestroyRunLoops() throws Exception {
        for (int x = 0; x < HANDLER_QUANTITY; x++) {
            runLoopManager.getRunLoopForID(component1.create().getId(), true);
        }

        assertEquals(HANDLER_QUANTITY, runLoopManager.getNumberOfHandlesInCluster(component1));
        assertEquals(0, runLoopManager.getNumberOfHandlesInCluster(component2));

        for (int x = 0; x < 20; x++) {
            runLoopManager.getRunLoopForID(component2.create().getId(), true);
        }

        assertEquals(HANDLER_QUANTITY, runLoopManager.getNumberOfHandlesInCluster(component1));
        assertEquals(HANDLER_QUANTITY, runLoopManager.getNumberOfHandlesInCluster(component2));

        runLoopManager.destroyRunLoops(component1);
        assertEquals(0, runLoopManager.getNumberOfHandlesInCluster(component1));
        assertEquals(HANDLER_QUANTITY, runLoopManager.getNumberOfHandlesInCluster(component2));

        runLoopManager.destroyRunLoops(component2);
        assertEquals(0, runLoopManager.getNumberOfHandlesInCluster(component2));
    }

    //===== helper classes ================================================================================================================
    private abstract class ManagerTestComponent implements Component {

        String id;

        public ComponentHandle create() throws RealtimeException {
            return create(new ID("synthetic", id, "operations", "1", UUID.randomUUID().toString()));
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public EvictionPolicy getEvictionPolicy() {
            return null;
        }

        @Override
        public void setLoadFactorCalculator(LoadFactorCalculator loadFactorCalculator) {
            //not interested
        }

    }

    private class ManagerTestComponent1 extends ManagerTestComponent {

        public ManagerTestComponent1() {
            this.id = "ManagerTestComponent1";
        }

        @Override
        public ComponentHandle create(ID id) {
            return new ManagerTestComponentHandle1(id);
        }

    }

    private class ManagerTestComponent2 extends ManagerTestComponent {

        public ManagerTestComponent2() {
            this.id = "ManagerTestComponent2";
        }

        @Override
        public ComponentHandle create(ID id) {
            return new ManagerTestComponentHandle2(id);
        }
    }

    private abstract class ManagerTestComponentHandle implements ComponentHandle {

        protected ID id;

        public ManagerTestComponentHandle(ID id) {
            this.id = id;
        }

        @Override
        public ID getId() {
            return id;
        }

        @Override
        public void process(Stanza stanza) {}

        @Override
        public boolean shouldBeDoneInGlobalThread(Stanza stanza) {
            return false;
        }

        @Override
        public void dispose() {}

    }

    private class ManagerTestComponentHandle1 extends ManagerTestComponentHandle {

        public ManagerTestComponentHandle1(ID id) {
            super(id);
        }
    }

    private class ManagerTestComponentHandle2 extends ManagerTestComponentHandle {

        public ManagerTestComponentHandle2(ID id) {
            super(id);
        }
    }

    //===== helper methods ================================================================================================================
    private ConcurrentHashMap<ID, SyntheticChannelRunLoop> getloopMap(RunLoopManager runLoopManager) throws Exception {
        Field loopMap = RunLoopManager.class.getDeclaredField("loopMap");
        loopMap.setAccessible(true);
        return (ConcurrentHashMap<ID, SyntheticChannelRunLoop>) loopMap.get(runLoopManager);
    }

    private ListMultimap<String, SyntheticChannelRunLoop> getloopClusters(RunLoopManager runLoopManager) throws Exception {
        Field loopClusters = RunLoopManager.class.getDeclaredField("loopClusters");
        loopClusters.setAccessible(true);
        return (ListMultimap<String, SyntheticChannelRunLoop>) loopClusters.get(runLoopManager);
    }

    private List<SyntheticChannelRunLoop> getRunLoopsForComponent(RunLoopManager runLoopManager, Component component) throws Exception {
        ListMultimap<String, SyntheticChannelRunLoop> loopCluster = getloopClusters(runLoopManager);
        return loopCluster.get(component.getId());
    }

}
