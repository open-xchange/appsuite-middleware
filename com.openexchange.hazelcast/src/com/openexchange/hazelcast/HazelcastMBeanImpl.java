/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.Member;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MemberGroupConfig;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.config.PartitionGroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.util.AddressUtil;
import com.hazelcast.partition.Partition;
import com.hazelcast.partition.PartitionService;

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
