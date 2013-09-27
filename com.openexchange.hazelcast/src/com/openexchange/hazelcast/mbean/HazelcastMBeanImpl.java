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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.hazelcast.mbean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.openexchange.hazelcast.osgi.HazelcastActivator;


/**
 * {@link HazelcastMBeanImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HazelcastMBeanImpl extends StandardMBean implements HazelcastMBean {

    /**
     * Initializes a new {@link HazelcastMBeanImpl}.
     *
     * @throws NotCompliantMBeanException
     */
    public HazelcastMBeanImpl() throws NotCompliantMBeanException {
        super(HazelcastMBean.class);
    }

    @Override
    public List<String> listMembers() throws MBeanException {
        return getHazelcastInstance().getConfig().getNetworkConfig().getJoin().getTcpIpConfig().getMembers();
    }

    @Override
    public void addMember(String member) throws MBeanException {
        getHazelcastInstance().getConfig().getNetworkConfig().getJoin().getTcpIpConfig().addMember(member);
    }

    @Override
    public void removeMember(String member) throws MBeanException {
        List<String> members = listMembers();
        if (null != members) {
            if (members.remove(member)) {
                getHazelcastInstance().getConfig().getNetworkConfig().getJoin().getTcpIpConfig().clear();
                getHazelcastInstance().getConfig().getNetworkConfig().getJoin().getTcpIpConfig().setMembers(members);
            }
        }
    }

    @Override
    public List<String> listClusterMembers() throws MBeanException {
        Set<Member> members = getHazelcastInstance().getCluster().getMembers();
        List<String> clusterMembers = new ArrayList<String>();
        if (null != members) {
            for (Member member : members) {
                clusterMembers.add(member.getInetSocketAddress().toString());
            }
        }
        return clusterMembers;
    }

    /**
     * Gets the current Hazelcast instance, throwing an exception if there is none.
     *
     * @return The Hazelcast instance
     * @throws MBeanException If there's no Hazelcast instance
     */
    private static HazelcastInstance getHazelcastInstance() throws MBeanException {
        HazelcastInstance hazelcastInstance = HazelcastActivator.REF_HAZELCAST_INSTANCE.get();
        if (null == hazelcastInstance) {
            throw new MBeanException(null, "HazelcastInstance is absent.");
        }
        return hazelcastInstance;
    }

}
