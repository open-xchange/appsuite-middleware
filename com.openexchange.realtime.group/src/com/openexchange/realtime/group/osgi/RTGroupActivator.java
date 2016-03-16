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

package com.openexchange.realtime.group.osgi;

import org.osgi.framework.BundleActivator;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.cleanup.RealtimeJanitor;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.group.DistributedGroupManager;
import com.openexchange.realtime.group.GroupCommand;
import com.openexchange.realtime.group.GroupDispatcher;
import com.openexchange.realtime.group.conversion.GroupCommand2JSON;
import com.openexchange.realtime.group.conversion.JSON2GroupCommand;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.payload.converter.PayloadTreeConverter;
import com.openexchange.realtime.util.Duration;
import com.openexchange.realtime.util.ElementPath;
import com.openexchange.threadpool.ThreadPoolService;


public class RTGroupActivator extends HousekeepingActivator implements BundleActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {
            MessageDispatcher.class, PayloadTreeConverter.class, ThreadPoolService.class, GlobalRealtimeCleanup.class,
            DistributedGroupManager.class };
    }

    @Override
    protected void startBundle() throws Exception {
        GroupServiceRegistry.SERVICES.set(this);
        GroupDispatcher.GROUPMANAGER_REF.set(GroupServiceRegistry.getInstance().getService(DistributedGroupManager.class));
        PayloadTreeConverter treeConverter = getService(PayloadTreeConverter.class);
        treeConverter.declarePreferredFormat(new ElementPath("group", "command"), GroupCommand.class.getName());
        registerService(SimplePayloadConverter.class, new GroupCommand2JSON());
        registerService(SimplePayloadConverter.class, new JSON2GroupCommand());
        treeConverter.declarePreferredFormat(new ElementPath("com.openexchange.realtime.client","inactivity"), Duration.class.getName());
        treeConverter.declarePreferredFormat(new ElementPath("com.openexchange.realtime","client"), ID.class.getName());
        for(RealtimeJanitor realtimeJanitor : RealtimeJanitors.getInstance().getJanitors()) {
            registerService(RealtimeJanitor.class, realtimeJanitor, realtimeJanitor.getServiceProperties());
        }

    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        RealtimeJanitors.getInstance().cleanup();
        GroupServiceRegistry.SERVICES.set(null);
        GroupDispatcher.GROUPMANAGER_REF.set(null);
    }

}
