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

package com.openexchange.aws.loadbalancing.osgi;

import org.apache.commons.logging.Log;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.openexchange.aws.loadbalancing.AWSLoadbalancingConfiguration;
import com.openexchange.aws.loadbalancing.AWSLoadbalancingService;
import com.openexchange.aws.loadbalancing.exceptions.OXAWSLoadBalancingExceptionCodes;
import com.openexchange.aws.loadbalancing.impl.AWSLoadbalancingServiceImpl;
import com.openexchange.config.ConfigurationService;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link AWSLoadBalancingActivator}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AWSLoadBalancingActivator extends HousekeepingActivator {

    private static Log LOG = LogFactory.getLog(AWSLoadBalancingActivator.class);

    /**
     * Initializes a new {@link AWSLoadBalancingActivator}.
     */
    public AWSLoadBalancingActivator() {
        super();
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.osgi.DeferredActivator#getNeededServices()
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, AmazonElasticLoadBalancing.class };
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.osgi.DeferredActivator#startBundle()
     */
    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: com.openexchange.aws.loadbalancing");
        ConfigurationService configService = getService(ConfigurationService.class);
        String lbName = configService.getProperty("com.openexchange.aws.loadbalancing.name");
        boolean useHttps = configService.getBoolProperty("com.openexchange.aws.loadbalancing.name", false);
        String zone = configService.getProperty("com.openexchange.aws.loadbalancing.zone");
        if (lbName == null) {
            throw OXAWSLoadBalancingExceptionCodes.AWS_LB_NO_LB_NAME.create();
        }
        if (zone == null) {
            throw OXAWSLoadBalancingExceptionCodes.AWS_LB_NO_ZONE.create();
        }
        AWSLoadbalancingConfiguration config = new AWSLoadbalancingConfiguration(lbName, useHttps, zone);
        AmazonElasticLoadBalancing loadBalancer = getService(AmazonElasticLoadBalancing.class);
        AWSLoadbalancingService service = new AWSLoadbalancingServiceImpl(loadBalancer, config);
        registerService(AWSLoadbalancingService.class, service);
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle: com.openexchange.aws.loadbalancing");
        unregisterServices();
        cleanUp();
    }

}
