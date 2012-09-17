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

package com.openexchange.aws.autoscaling.osgi;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.model.AlreadyExistsException;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.DescribeLaunchConfigurationsResult;
import com.amazonaws.services.autoscaling.model.LaunchConfiguration;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyResult;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.Statistic;
import com.openexchange.config.ConfigurationService;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link AWSAutoScalingActivator}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AWSAutoScalingActivator extends HousekeepingActivator {

    private AmazonAutoScaling autoScalingClient;

    private AmazonCloudWatch cloudWatchClient;

    private String launchConfigurationName;

    private String autoScalingGroupName;

    private String amiId;

    private String instanceType;

    private String secGroup;

    private String keyPair;

    private String zone;

    private String loadBalancer;

    private int minInstances;

    private int maxInstances;

    private int gracePeriod;

    private int scaleUp;

    private int scaleDown;

    private PutScalingPolicyResult resultUp;

    private PutScalingPolicyResult resultDown;

    private static final Log LOG = LogFactory.getLog(AWSAutoScalingActivator.class);

    /**
     * Initializes a new {@link AWSAutoScalingActivator}.
     */
    public AWSAutoScalingActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, AmazonAutoScaling.class, AmazonCloudWatch.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle: com.openexchange.aws.autoscaling");
        autoScalingClient = getService(AmazonAutoScaling.class);
        cloudWatchClient = getService(AmazonCloudWatch.class);
        ConfigurationService configService = getService(ConfigurationService.class);
        launchConfigurationName = configService.getProperty("com.openexchange.aws.autoscaling.launchconfiguration");
        autoScalingGroupName = configService.getProperty("com.openexchange.aws.autoscaling.autoscaling");
        amiId = configService.getProperty("com.openexchange.aws.autoscaling.ami");
        instanceType = configService.getProperty("com.openexchange.aws.autoscaling.type");
        secGroup = configService.getProperty("com.openexchange.aws.autoscaling.securitygroup");
        keyPair = configService.getProperty("com.openexchange.aws.autoscaling.keypair");
        zone = configService.getProperty("com.openexchange.aws.autoscaling.zone");
        loadBalancer = configService.getProperty("com.openexchange.aws.autoscaling.loadbalancer");
        minInstances = configService.getIntProperty("com.openexchange.aws.autoscaling.min", 0);
        maxInstances = configService.getIntProperty("com.openexchange.aws.autoscaling.max", 0);
        gracePeriod = configService.getIntProperty("com.openexchange.aws.autoscaling.grace", 0);
        scaleUp = configService.getIntProperty("com.openexchange.aws.autoscaling.scaleup", 0);
        scaleDown = configService.getIntProperty("com.openexchange.aws.autoscaling.scaledown", 0);
        createLaunchConfiguration();
        createAutoScalingGroup();
        createPolicies();
        createMetricAlarms();
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("Stopping bundle: com.openexchange.aws.autoscaling");
        cleanUp();
    }

    private void createLaunchConfiguration() throws Exception {
        DescribeLaunchConfigurationsResult descResult = autoScalingClient.describeLaunchConfigurations();
        for (LaunchConfiguration l : descResult.getLaunchConfigurations()) {
            if (l.getLaunchConfigurationName().equals(launchConfigurationName)) {
                return;
            }
        }
        CreateLaunchConfigurationRequest launchReq = new CreateLaunchConfigurationRequest();
        launchReq.setLaunchConfigurationName(launchConfigurationName);
        launchReq.setImageId(amiId);
        launchReq.setInstanceType(instanceType);
        launchReq.setKeyName(keyPair);
        List<String> securityGroups = new ArrayList<String>();
        securityGroups.add(secGroup);
        launchReq.setSecurityGroups(securityGroups);
        try {
            autoScalingClient.createLaunchConfiguration(launchReq);
        } catch (AlreadyExistsException e) {
            // ignore
        } catch (Exception e) {
            throw e;
        }
    }

    private void createAutoScalingGroup() {
        DescribeAutoScalingGroupsResult descResult = autoScalingClient.describeAutoScalingGroups();
        for (AutoScalingGroup as : descResult.getAutoScalingGroups()) {
            if (as.getAutoScalingGroupName().equals(autoScalingGroupName)) {
                return;
            }
        }
        CreateAutoScalingGroupRequest createReq = new CreateAutoScalingGroupRequest();
        createReq.setAutoScalingGroupName(autoScalingGroupName);
        List<String> zones = new ArrayList<String>();
        zones.add(zone);
        createReq.setAvailabilityZones(zones);
        createReq.setLaunchConfigurationName(launchConfigurationName);
        List<String> loadbalancers = new ArrayList<String>();
        loadbalancers.add(loadBalancer);
        createReq.setLoadBalancerNames(loadbalancers);
        createReq.setHealthCheckGracePeriod(gracePeriod);
        createReq.setHealthCheckType("ELB");
        createReq.setMaxSize(maxInstances);
        createReq.setMinSize(minInstances);
        autoScalingClient.createAutoScalingGroup(createReq);
    }

    private void createPolicies() {
        PutScalingPolicyRequest scaleUpReq = new PutScalingPolicyRequest();
        scaleUpReq.setAutoScalingGroupName(autoScalingGroupName);
        scaleUpReq.setAdjustmentType("ChangeInCapacity");
        scaleUpReq.setCooldown(gracePeriod);
        scaleUpReq.setPolicyName("ox-aws-scale-up");
        scaleUpReq.setScalingAdjustment(scaleUp);
        resultUp = autoScalingClient.putScalingPolicy(scaleUpReq);

        PutScalingPolicyRequest scaleDownReq = new PutScalingPolicyRequest();
        scaleDownReq.setAutoScalingGroupName(autoScalingGroupName);
        scaleDownReq.setAdjustmentType("ChangeInCapacity");
        scaleDownReq.setCooldown(gracePeriod);
        scaleDownReq.setPolicyName("ox-aws-scale-down");
        scaleDownReq.setScalingAdjustment(scaleDown);
        resultDown = autoScalingClient.putScalingPolicy(scaleDownReq);
    }

    private void createMetricAlarms() {
        PutMetricAlarmRequest scaleUpAlarm = new PutMetricAlarmRequest();
        scaleUpAlarm.setAlarmDescription("Scale Up at 80% load");
        scaleUpAlarm.setAlarmName("ox-aws-scale-up-alarm");
        scaleUpAlarm.setMetricName("CPUUtilization");
        scaleUpAlarm.setNamespace("AWS/EC2");
        scaleUpAlarm.setStatistic(Statistic.Average);
        scaleUpAlarm.setPeriod(60);
        scaleUpAlarm.setThreshold(80.0);
        scaleUpAlarm.setComparisonOperator(ComparisonOperator.GreaterThanThreshold);
        Dimension dimensionUp = new Dimension();
        dimensionUp.setName("AutoScalingGroupName");
        dimensionUp.setValue(autoScalingGroupName);
        List<Dimension> dimensionsUp = new ArrayList<Dimension>();
        dimensionsUp.add(dimensionUp);
        scaleUpAlarm.setDimensions(dimensionsUp);
        scaleUpAlarm.setEvaluationPeriods(3);
        scaleUpAlarm.setUnit(StandardUnit.Percent);
        List<String> actionsUp = new ArrayList<String>();
        actionsUp.add(resultUp.getPolicyARN());
        scaleUpAlarm.setAlarmActions(actionsUp);
        cloudWatchClient.putMetricAlarm(scaleUpAlarm);
        PutMetricAlarmRequest scaleDownAlarm = new PutMetricAlarmRequest();
        scaleDownAlarm.setAlarmDescription("Scale Down at 20% load");
        scaleDownAlarm.setAlarmName("ox-aws-scale-down-alarm");
        scaleDownAlarm.setMetricName("CPUUtilization");
        scaleDownAlarm.setNamespace("AWS/EC2");
        scaleDownAlarm.setStatistic(Statistic.Average);
        scaleDownAlarm.setPeriod(60);
        scaleDownAlarm.setThreshold(20.0);
        scaleDownAlarm.setComparisonOperator(ComparisonOperator.LessThanThreshold);
        Dimension dimensionDown = new Dimension();
        dimensionDown.setName("AutoScalingGroupName");
        dimensionDown.setValue(autoScalingGroupName);
        List<Dimension> dimensionsDown = new ArrayList<Dimension>();
        dimensionsDown.add(dimensionDown);
        scaleDownAlarm.setDimensions(dimensionsDown);
        scaleDownAlarm.setEvaluationPeriods(3);
        scaleDownAlarm.setUnit(StandardUnit.Percent);
        List<String> actionsDown = new ArrayList<String>();
        actionsDown.add(resultDown.getPolicyARN());
        scaleDownAlarm.setAlarmActions(actionsDown);
        cloudWatchClient.putMetricAlarm(scaleDownAlarm);
    }

}
