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

package com.openexchange.hazelcast;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MemberGroupConfig;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.config.PartitionGroupConfig;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.Partition;
import com.hazelcast.core.PartitionService;
import com.hazelcast.util.AddressUtil;

/**
 * {@link HazelcastMBeanImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class HazelcastMBeanImpl extends StandardMBean implements HazelcastMBean {

    /**
     * Holds the running hazelcast instance.
     */
    private static final AtomicReference<HazelcastInstance> REF_HAZELCAST_INSTANCE = new AtomicReference<HazelcastInstance>();

    /**
     * Sets the hazelcast instance.
     *
     * @param instance The hazelcast instance to use
     */
    public static void setHazelcastInstance(HazelcastInstance instance) {
        REF_HAZELCAST_INSTANCE.set(instance);
    }

    /**
     * Initializes a new {@link HazelcastMBeanImpl}.
     */
    public HazelcastMBeanImpl() throws NotCompliantMBeanException {
        super(HazelcastMBean.class);
    }

    @Override
    public boolean usesCustomPartitioning() {
        HazelcastInstance hazelcastInstance = REF_HAZELCAST_INSTANCE.get();
        if (null != hazelcastInstance) {
            Config config = hazelcastInstance.getConfig();
            if (null != config) {
                PartitionGroupConfig partitionGroupConfig = config.getPartitionGroupConfig();
                if (null != partitionGroupConfig) {
                    return partitionGroupConfig.isEnabled();
                }
            }
        }
        return false;
    }

    @Override
    public boolean supportsPartitionReplicas() {
        Set<Member> partitionOwningMembers = getPartitionOwningMembers();
        if (null != partitionOwningMembers) {
            int requiredMembers = getRequiredMembers();
            return partitionOwningMembers.size() >= requiredMembers;
        }
        return false;
    }

    @Override
    public String getPartitionOwner(String key) {
        HazelcastInstance hazelcastInstance = REF_HAZELCAST_INSTANCE.get();
        if (null != hazelcastInstance) {
            PartitionService partitionService = hazelcastInstance.getPartitionService();
            if (null != partitionService) {
                Partition partition = hazelcastInstance.getPartitionService().getPartition(key);
                if (null != partition) {
                    Member owner = partition.getOwner();
                    if (null != owner) {
                        return String.valueOf(owner.getSocketAddress());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the number of partition owning members required to serve the configured backup counts of all maps.
     *
     * @return The number of required members
     */
    private int getRequiredMembers() {
        int maxRequiredMembers = 0;
        HazelcastInstance hazelcastInstance = REF_HAZELCAST_INSTANCE.get();
        if (null != hazelcastInstance) {
            Config config = hazelcastInstance.getConfig();
            if (null != config) {
                Map<String, MapConfig> mapConfigs = config.getMapConfigs();
                if (null != mapConfigs) {
                    for (MapConfig mapConfig : mapConfigs.values()) {
                        int requiredMembers = 1 + mapConfig.getBackupCount() + mapConfig.getAsyncBackupCount();
                        if (maxRequiredMembers < requiredMembers) {
                            maxRequiredMembers = requiredMembers;
                        }
                    }
                }
                Map<String, MultiMapConfig> multiMapConfigs = config.getMultiMapConfigs();
                if (null != multiMapConfigs) {
                    for (MultiMapConfig multiMapConfig : multiMapConfigs.values()) {
                        int requiredMembers = 1 + multiMapConfig.getBackupCount() + multiMapConfig.getAsyncBackupCount();
                        if (maxRequiredMembers < requiredMembers) {
                            maxRequiredMembers = requiredMembers;
                        }
                    }
                }
            }
        }
        return maxRequiredMembers;
    }

    private Set<Member> getPartitionOwningMembers() {
        HashSet<Member> partitionOwningMembers = new HashSet<Member>();
        HazelcastInstance hazelcastInstance = REF_HAZELCAST_INSTANCE.get();
        if (null != hazelcastInstance) {
            Cluster cluster = hazelcastInstance.getCluster();
            if (null != cluster) {
                /*
                 * get all cluster members
                 */
                Set<Member> clusterMembers = cluster.getMembers();
                Config config = hazelcastInstance.getConfig();
                /*
                 * remove those members not defined in the partition group configs
                 */
                if (null != clusterMembers && null != config) {
                    PartitionGroupConfig partitionGroupConfig = config.getPartitionGroupConfig();
                    for (Member member : clusterMembers) {
                        if (isPartitionOwningMember(member, partitionGroupConfig)) {
                            partitionOwningMembers.add(member);
                        }
                    }
                }
            }
        }
        return partitionOwningMembers;
    }

    /**
     * Gets a value indicating whether the supplied member is configured to hold partition data or not, considering a custom partition
     * group configuration.
     *
     * @param member The member to check
     * @param partitionGroupConfig The configured partition group configuration
     * @return <code>true</code> if the member is configured to own partitions, <code>false</code>, otherwise
     */
    private static boolean isPartitionOwningMember(Member member, PartitionGroupConfig partitionGroupConfig) {
        if (null == member || null == member.getSocketAddress() || null == member.getSocketAddress().getAddress()) {
            return false;
        }
        InetAddress inetAddress = member.getSocketAddress().getAddress();
        if (null != partitionGroupConfig && partitionGroupConfig.isEnabled()) {
            Collection<MemberGroupConfig> memberGroupConfigs = partitionGroupConfig.getMemberGroupConfigs();
            if (null != memberGroupConfigs) {
                for (MemberGroupConfig memberGroupConfig : memberGroupConfigs) {
                    Collection<String> interfaces = memberGroupConfig.getInterfaces();
                    if (null != interfaces) {
                        if (AddressUtil.matchAnyInterface(inetAddress.getHostAddress(), interfaces)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
        return true;
    }

}
