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

package com.openexchange.realtime.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import com.openexchange.management.ManagementObject;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.synthetic.RunLoopManager;
import com.openexchange.realtime.synthetic.SyntheticChannelRunLoop;


/**
 * {@link RunLoopManagerManagement}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.2
 */
public class RunLoopManagerManagement extends ManagementObject<RunLoopManagerMBean> implements RunLoopManagerMBean {

    private ObjectName objectName;
    private final RunLoopManager runLoopManager;

    public RunLoopManagerManagement(RunLoopManager runLoopManager) {
        super(RunLoopManagerMBean.class);
        this.runLoopManager = runLoopManager;
    }

    @Override
    public Map<String, Map<String, String>> getComponentHandleMappings() {
        HashMap<String, Map<String,String>> clusterMappings = new HashMap<String, Map<String, String>>();
        Set<String> componentIds = runLoopManager.getManagedComponents();
        for (String componentId : componentIds) {
            Map<String, String> runLoopMap = new HashMap<String, String>();
            List<Entry<ID,SyntheticChannelRunLoop>> handlesInCluster = runLoopManager.getHandlesInCluster(componentId);
            for (Entry<ID, SyntheticChannelRunLoop> entry : handlesInCluster) {
                runLoopMap.put(entry.getKey().toString(), entry.getValue().getName());
            }
            clusterMappings.put(componentId, runLoopMap);
        }
        return clusterMappings;
    }

    @Override
    public ObjectName getObjectName() {
        if (objectName == null) {
            String managerName = "RunLoopManager";
            try {
                objectName = new ObjectName("com.openexchange.realtime", "name", managerName);
            } catch (MalformedObjectNameException e) {
                // can't happen: valid domain and no missing parameters
            } catch (NullPointerException e) {
                // can't happen: valid domain and no missing parameters
            }
        }
        return objectName;
    }

    @Override
    public List<String> getRunLoopFillStatus() {
        //component-runloop <=> size
        List<SyntheticChannelRunLoop> runLoopView = new ArrayList<>(runLoopManager.getRunLoopView());
        Collections.sort(runLoopView, new NaturalRunLoopComparator());
        //basic jmx clients like mission control or visualvm can't display sorted maps, so we simply use an ordered list of Strings
        List<String> sortedLoopStatus = new ArrayList<>(runLoopView.size());
        for (SyntheticChannelRunLoop runLoop : runLoopView) {
                sortedLoopStatus.add(runLoop.getName() + " = " + runLoop.getQueueSize());
        }
        return sortedLoopStatus;
    }
    
    @Override
    public Map<String, Long> getRunLoopFillSum() {
        Map<String, Collection<SyntheticChannelRunLoop>> runLoopsPerComponent = runLoopManager.getRunLoopsPerComponent();
        Map<String, Long> retVal = new HashMap<>(runLoopsPerComponent.size());
        
        for (Entry<String, Collection<SyntheticChannelRunLoop>> entry : runLoopsPerComponent.entrySet()) {
            String component = entry.getKey();
            long sum=0;
            for (SyntheticChannelRunLoop syntheticChannelRunLoop : entry.getValue()) {
                sum += syntheticChannelRunLoop.getQueueSize();
            }
            retVal.put(component, sum);
        }
        
        return retVal;
    }

}
