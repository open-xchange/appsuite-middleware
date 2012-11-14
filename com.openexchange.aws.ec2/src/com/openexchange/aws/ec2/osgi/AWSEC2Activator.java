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

package com.openexchange.aws.ec2.osgi;

import org.apache.commons.logging.Log;
import com.amazonaws.services.ec2.AmazonEC2;
import com.openexchange.aws.ec2.AWSEC2Configuration;
import com.openexchange.aws.ec2.AWSEC2Service;
import com.openexchange.aws.ec2.impl.AWSEC2ServiceImpl;
import com.openexchange.aws.loadbalancing.AWSLoadbalancingService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link AWSEC2Activator}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AWSEC2Activator extends HousekeepingActivator {

    private static final Log LOG = LogFactory.getLog(AWSEC2Activator.class);

    private AWSEC2Service service;

    /**
     * Initializes a new {@link AWSEC2Activator}.
     */
    public AWSEC2Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { AmazonEC2.class, AWSLoadbalancingService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: com.openexchange.aws.ec2");
        AWSLoadbalancingService lbService = getService(AWSLoadbalancingService.class);
        AWSEC2ServiceRegistry.getRegistry().addService(AWSLoadbalancingService.class, lbService);
        ConfigurationService configService = getService(ConfigurationService.class);
        String imageId = configService.getProperty("com.openexchange.aws.ec2.imageId");
        String instanceType = configService.getProperty("com.openexchange.aws.ec2.instanceType");
        String keyPair = configService.getProperty("com.openexchange.aws.ec2.keyPair");
        String placement = configService.getProperty("com.openexchange.aws.ec2.placement");
        String securityGroupId = configService.getProperty("com.openexchange.aws.ec2.securitygroup");
        AWSEC2Configuration config = new AWSEC2Configuration(imageId, instanceType, keyPair, placement, securityGroupId);
        AmazonEC2 ec2 = getService(AmazonEC2.class);
        service = new AWSEC2ServiceImpl(ec2, config);
        registerService(AWSEC2Service.class, service);
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle: com.openexchange.aws.ec2");
        if (service != null) {
            service.stopAllInstances();
        }
        unregisterServices();
        cleanUp();
    }

}
