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

package com.openexchange.aws.loadbalancing.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;
import com.openexchange.aws.loadbalancing.AWSLoadbalancingConfiguration;
import com.openexchange.aws.loadbalancing.AWSLoadbalancingService;
import com.openexchange.aws.loadbalancing.exceptions.OXAWSLoadBalancingExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * {@link AWSLoadbalancingServiceImpl}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AWSLoadbalancingServiceImpl implements AWSLoadbalancingService {

    private static final Log LOG = LogFactory.getLog(AWSLoadbalancingServiceImpl.class);

    private final AmazonElasticLoadBalancing lbClient;

    private LoadBalancerDescription lbDesc;

    private final AWSLoadbalancingConfiguration config;

    private List<Instance> registeredInstances;

    /**
     * Initializes a new {@link AWSLoadbalancingServiceImpl}.
     */
    public AWSLoadbalancingServiceImpl(AmazonElasticLoadBalancing loadBalancer, AWSLoadbalancingConfiguration config) {
        super();
        this.lbClient = loadBalancer;
        this.config = config;
        this.registeredInstances = new ArrayList<Instance>();
        List<LoadBalancerDescription> loadbalancers = lbClient.describeLoadBalancers().getLoadBalancerDescriptions();
        for (LoadBalancerDescription desc : loadbalancers) {
            if (desc.getLoadBalancerName().equals(config.getLbName())) {
                this.lbDesc = desc;
                return;
            }
        }
        try {
            this.lbDesc = createLoadBalancer();
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public void registerInstance(String instanceId) throws OXException {
        try {
            Instance instance = new Instance(instanceId);
            List<Instance> instances = new ArrayList<Instance>();
            instances.add(instance);
            RegisterInstancesWithLoadBalancerRequest registerReq = new RegisterInstancesWithLoadBalancerRequest(
                config.getLbName(),
                instances);
            RegisterInstancesWithLoadBalancerResult result = lbClient.registerInstancesWithLoadBalancer(registerReq);
            registeredInstances = result.getInstances();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSLoadBalancingExceptionCodes.AWS_LB_REGISTER_FAILED.create(instanceId);
        }
    }

    @Override
    public void removeInstance(String instanceId) throws OXException {
        try {
            Instance instance = new Instance(instanceId);
            List<Instance> instances = new ArrayList<Instance>();
            instances.add(instance);
            DeregisterInstancesFromLoadBalancerRequest deregisterReq = new DeregisterInstancesFromLoadBalancerRequest(
                config.getLbName(),
                instances);
            DeregisterInstancesFromLoadBalancerResult result = lbClient.deregisterInstancesFromLoadBalancer(deregisterReq);
            registeredInstances = result.getInstances();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSLoadBalancingExceptionCodes.AWS_LB_DEREGISTER_FAILED.create(instanceId);
        }
    }

    @Override
    public List<String> getInstances() {
        List<String> instances = new ArrayList<String>();
        for (Instance instance : registeredInstances) {
            instances.add(instance.getInstanceId());
        }
        return instances;
    }

    @Override
    public String getLoadbalancerUrl() {
        return lbDesc.getDNSName();
    }

    private LoadBalancerDescription createLoadBalancer() throws OXException {
        try {
            CreateLoadBalancerRequest req = new CreateLoadBalancerRequest();
            req.setLoadBalancerName(config.getLbName());
            List<Listener> listeners = new ArrayList<Listener>();
            Listener httpListener = new Listener("HTTP", 80, 80);
            listeners.add(httpListener);
            Listener httpsListener;
            if (config.isUseHttps()) {
                httpsListener = new Listener("HTTPS", 443, 443);
                listeners.add(httpsListener);
            }
            req.setListeners(listeners);
            String zone = config.getZone();
            List<String> availabilityZones = new ArrayList<String>();
            availabilityZones.add(zone);
            req.setAvailabilityZones(availabilityZones);
            lbClient.createLoadBalancer(req);
            List<LoadBalancerDescription> loadbalancers = lbClient.describeLoadBalancers().getLoadBalancerDescriptions();
            for (LoadBalancerDescription desc : loadbalancers) {
                if (desc.getLoadBalancerName().equals(config.getLbName())) {
                    return desc;
                }
            }
            return null;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw OXAWSLoadBalancingExceptionCodes.AWS_LB_CREATE_FAILED.create();
        }
    }

}
