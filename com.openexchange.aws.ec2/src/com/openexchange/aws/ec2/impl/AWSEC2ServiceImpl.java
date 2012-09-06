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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.aws.ec2.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.elasticloadbalancing.model.HealthCheck;
import com.openexchange.aws.ec2.AWSEC2Configuration;
import com.openexchange.aws.ec2.AWSEC2Service;
import com.openexchange.aws.ec2.osgi.AWSEC2ServiceRegistry;
import com.openexchange.aws.loadbalancing.AWSLoadbalancingService;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;

/**
 * {@link AWSEC2ServiceImpl}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AWSEC2ServiceImpl implements AWSEC2Service {

    private final static Log LOG = LogFactory.getLog(AWSEC2ServiceImpl.class);

    private final AmazonEC2 ec2client;

    private final AWSEC2Configuration config;

    private final AWSLoadbalancingService lbService;

    private final List<String> instances;

    /**
     * Initializes a new {@link AWSEC2ServiceImpl}.
     */
    public AWSEC2ServiceImpl(AmazonEC2 ec2client, AWSEC2Configuration config) {
        super();
        this.ec2client = ec2client;
        this.config = config;
        this.lbService = AWSEC2ServiceRegistry.getRegistry().getService(AWSLoadbalancingService.class);
        this.instances = new ArrayList<String>();
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.aws.ec2.AWSEC2Service#startInstance()
     */
    @Override
    public String startInstance() throws OXException {
        RunInstancesRequest runReq = new RunInstancesRequest();
        runReq.setImageId(config.getImageId());
        runReq.setInstanceType(config.getInstanceType());
        runReq.setKeyName(config.getKeyPair());
        runReq.setEbsOptimized(false);
        String securityGroupId = config.getSecurityGroupId();
        List<String> securityGroups = new ArrayList<String>();
        securityGroups.add(securityGroupId);
        runReq.setSecurityGroupIds(securityGroups);
        runReq.setMinCount(1);
        runReq.setMaxCount(1);
        runReq.setPlacement(new Placement(config.getPlacement()));
        runReq.setMonitoring(true);
        RunInstancesResult result = ec2client.runInstances(runReq);
        String instanceId = result.getReservation().getInstances().get(0).getInstanceId();
        lbService.registerInstance(instanceId);
        instances.add(instanceId);
        LOG.info("Instance " + instanceId + " started.");
        return instanceId;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.aws.ec2.AWSEC2Service#stopInstances(java.util.List<InstanceStateChange>)
     */
    @Override
    public List<InstanceStateChange> stopInstances(List<String> instanceIds) throws OXException {
        for (String instanceId : instanceIds) {
            lbService.removeInstance(instanceId);
        }
        StopInstancesRequest stopReq = new StopInstancesRequest(instanceIds);
        StopInstancesResult result = ec2client.stopInstances(stopReq);
        instances.removeAll(instanceIds);
        StringBuilder sb = new StringBuilder().append("Instances ");
        for (String instance : instanceIds) {
            sb.append(instance).append(", ");
        }
        sb.deleteCharAt(sb.length() - 2);
        sb.append("stopped.");
        LOG.info(sb.toString());
        return result.getStoppingInstances();
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.aws.ec2.AWSEC2Service#checkHealth(java.lang.String)
     */
    @Override
    public HealthCheck checkHealth(String instanceId) {
        List<String> instancesToCheck = new ArrayList<String>();
        instancesToCheck.add(instanceId);
        DescribeInstancesRequest instanceReq = new DescribeInstancesRequest();
        instanceReq.setInstanceIds(instancesToCheck);
        DescribeInstancesResult result = ec2client.describeInstances(instanceReq);
        Instance instance = result.getReservations().get(0).getInstances().get(0);
        String dnsName = instance.getPublicDnsName();
        HealthCheck hc = new HealthCheck();
        hc.setTarget(dnsName);
        return null;
    }

    @Override
    public List<InstanceStateChange> stopInstance(String instanceId) throws OXException {
        List<String> instancesToStop = new ArrayList<String>();
        instancesToStop.add(instanceId);
        return stopInstances(instancesToStop);
    }

    @Override
    public void stopAllInstances() throws OXException {
        stopInstances(instances);
    }

}
