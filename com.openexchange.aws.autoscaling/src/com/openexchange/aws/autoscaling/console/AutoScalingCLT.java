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

package com.openexchange.aws.autoscaling.console;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AlreadyExistsException;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.DeleteAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.DeleteLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.DeletePolicyRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.DescribeLaunchConfigurationsRequest;
import com.amazonaws.services.autoscaling.model.DescribeLaunchConfigurationsResult;
import com.amazonaws.services.autoscaling.model.DescribePoliciesRequest;
import com.amazonaws.services.autoscaling.model.DescribePoliciesResult;
import com.amazonaws.services.autoscaling.model.Instance;
import com.amazonaws.services.autoscaling.model.LaunchConfiguration;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyResult;
import com.amazonaws.services.autoscaling.model.ScalingPolicy;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.Statistic;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;

/**
 * {@link AutoScalingCLT}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AutoScalingCLT {

    private static final Options toolkitOptions;

    private static AmazonAutoScaling autoScalingClient;

    private static AmazonEC2 ec2Client;

    private static AmazonElasticLoadBalancing elbClient;

    private static AmazonCloudWatch cloudWatchClient;

    private static Properties props;

    private static Map<String, String[]> arrays;

    static {
        toolkitOptions = new Options();
        toolkitOptions.addOption("h", "help", false, "Prints a help text");
        toolkitOptions.addOption("A", "access-key", true, "The AWS access key");
        toolkitOptions.addOption("S", "secret-key", true, "The AWS secret key");
        toolkitOptions.addOption("R", "region", true, "The AWS region");
        toolkitOptions.addOption("d", "delete-only", false, "Only delete existing autoscaling groups");
    }

    private static void printHelp() {
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("setupautoscaling", toolkitOptions);
    }

    private static String getEC2Endpoint(String region) {
        if (region.equals("us-east-1")) {
            return "ec2.us-east-1.amazonaws.com";
        }
        if (region.equals("us-west-1")) {
            return "ec2.us-west-1.amazonaws.com";
        }
        if (region.equals("us-west-2")) {
            return "ec2.us-west-2.amazonaws.com";
        }
        if (region.equals("eu-west-1")) {
            return "ec2.eu-west-1.amazonaws.com";
        }
        if (region.equals("ap-southeast-1")) {
            return "ec2.ap-southeast-1.amazonaws.com";
        }
        if (region.equals("ap-northeast-1")) {
            return "ec2.ap-northeast-1.amazonaws.com";
        }
        if (region.equals("sa-east-1")) {
            return "ec2.sa-east-1.amazonaws.com";
        }
        return null;
    }

    private static String getELBEndpoint(String region) {
        if (region.equals("us-east-1")) {
            return "us-east-1.elasticloadbalancing.amazonaws.com";
        }
        if (region.equals("us-west-1")) {
            return "us-west-1.elasticloadbalancing.amazonaws.com";
        }
        if (region.equals("us-west-2")) {
            return "us-west-2.elasticloadbalancing.amazonaws.com";
        }
        if (region.equals("eu-west-1")) {
            return "eu-west-1.elasticloadbalancing.amazonaws.com";
        }
        if (region.equals("ap-southeast-1")) {
            return "ap-southeast-1.elasticloadbalancing.amazonaws.com";
        }
        if (region.equals("ap-northeast-1")) {
            return "ap-northeast-1.elasticloadbalancing.amazonaws.com";
        }
        if (region.equals("sa-east-1")) {
            return "sa-east-1.elasticloadbalancing.amazonaws.com";
        }
        return null;
    }

    private static String getASEndpoint(String region) {
        if (region.equals("us-east-1")) {
            return "autoscaling.us-east-1.amazonaws.com";
        }
        if (region.equals("us-west-1")) {
            return "autoscaling.us-west-1.amazonaws.com";
        }
        if (region.equals("us-west-2")) {
            return "autoscaling.us-west-2.amazonaws.com";
        }
        if (region.equals("eu-west-1")) {
            return "autoscaling.eu-west-1.amazonaws.com";
        }
        if (region.equals("ap-southeast-1")) {
            return "autoscaling.ap-southeast-1.amazonaws.com";
        }
        if (region.equals("ap-northeast-1")) {
            return "autoscaling.ap-northeast-1.amazonaws.com";
        }
        if (region.equals("sa-east-1")) {
            return "autoscaling.sa-east-1.amazonaws.com";
        }
        return null;
    }

    private static String getCWEndpoint(String region) {
        if (region.equals("us-east-1")) {
            return "monitoring.us-east-1.amazonaws.com";
        }
        if (region.equals("us-west-1")) {
            return "monitoring.us-west-1.amazonaws.com";
        }
        if (region.equals("us-west-2")) {
            return "monitoring.us-west-2.amazonaws.com";
        }
        if (region.equals("eu-west-1")) {
            return "monitoring.eu-west-1.amazonaws.com";
        }
        if (region.equals("ap-southeast-1")) {
            return "monitoring.ap-southeast-1.amazonaws.com";
        }
        if (region.equals("ap-northeast-1")) {
            return "monitoring.ap-northeast-1.amazonaws.com";
        }
        if (region.equals("sa-east-1")) {
            return "monitoring.sa-east-1.amazonaws.com";
        }
        return null;
    }

    /**
     * Initializes a new {@link AutoScalingCLT}.
     */
    public AutoScalingCLT() {
        super();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        final CommandLineParser parser = new PosixParser();
        String accessKey = null;
        String secretKey = null;
        String region = null;
        boolean deleteOnly = false;
        props = new Properties();
        try {
            props.load(new FileInputStream("conf/awsautoscaling.properties"));
            final CommandLine cmd = parser.parse(toolkitOptions, args);
            if (cmd.hasOption('h')) {
                printHelp();
                System.exit(0);
            }
            if (cmd.hasOption('A')) {
                accessKey = cmd.getOptionValue('A');
            }
            if (cmd.hasOption('S')) {
                secretKey = cmd.getOptionValue('S');
            }
            if (cmd.hasOption('R')) {
                region = cmd.getOptionValue('R');
            }
            if (cmd.hasOption('d')) {
                deleteOnly = true;
            }
            if (accessKey == null || secretKey == null || region == null) {
                System.err.println("No access key and/or secret key and/or region");
                printHelp();
                System.exit(1);
            }
            AWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
            autoScalingClient = new AmazonAutoScalingClient(creds);
            ec2Client = new AmazonEC2Client(creds);
            elbClient = new AmazonElasticLoadBalancingClient(creds);
            cloudWatchClient = new AmazonCloudWatchClient(creds);
            String asEndpoint = getASEndpoint(region);
            if (asEndpoint == null) {
                System.err.println("Invalid region");
                printHelp();
                System.exit(1);
            }
            String ec2Endpoint = getEC2Endpoint(region);
            if (ec2Endpoint == null) {
                System.err.println("Invalid region");
                printHelp();
                System.exit(1);
            }
            String elbEndpoint = getELBEndpoint(region);
            if (elbEndpoint == null) {
                System.err.println("Invalid region");
                printHelp();
                System.exit(1);
            }
            String cwEndpoint = getCWEndpoint(region);
            if (cwEndpoint == null) {
                System.err.println("Invalid region");
                printHelp();
                System.exit(1);
            }
            autoScalingClient.setEndpoint(asEndpoint);
            ec2Client.setEndpoint(ec2Endpoint);
            elbClient.setEndpoint(elbEndpoint);
            cloudWatchClient.setEndpoint(cwEndpoint);
            deletePolicies();
            deleteASGroups();
            deleteLaunchConfigurations();
            if (deleteOnly) {
                System.out.println("Autoscaling configuration deleted");
                System.exit(0);
            }
            arrays = checkProperties();
            createLaunchConfiguration();
            createAutoScalingGroup();
            createAlarms();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
        System.out.println("Autoscaling configuration created and started");
        System.exit(0);
    }

    private static void deletePolicies() {
        DescribePoliciesRequest policiesRequest = new DescribePoliciesRequest();
        DescribePoliciesResult policiesResult = autoScalingClient.describePolicies(policiesRequest);
        for (ScalingPolicy policy : policiesResult.getScalingPolicies()) {
            DeletePolicyRequest deleteRequest = new DeletePolicyRequest();
            deleteRequest.setPolicyName(policy.getPolicyName());
            deleteRequest.setAutoScalingGroupName(policy.getAutoScalingGroupName());
            autoScalingClient.deletePolicy(deleteRequest);
        }
    }

    private static Map<String, String[]> checkProperties() {
        for (Object o : props.keySet()) {
            String propName = (String) o;
            if (props.get(propName) == null) {
                System.err.println("Config file has no value for " + propName);
                System.exit(3);
            }
        }
        Map<String, String[]> retval = new HashMap<String, String[]>();
        String[] secGroups = props.getProperty("securitygroups").split(",");
        String[] regions = props.getProperty("regions").split(",");
        String[] loadbalancers = props.getProperty("loadbalancers").split(",");
        String[] metrics = props.getProperty("metrics").split(",");
        String[] gracetimes = props.getProperty("gracetimes").split(",");
        String[] thresholds = props.getProperty("thresholds").split(",");
        String[] evalPeriods = props.getProperty("evalPeriods").split(",");
        String prop = "min";
        try {
            Integer.parseInt(props.getProperty(prop));
            prop = "max";
            Integer.parseInt(props.getProperty(prop));
            prop = "scaleup";
            Integer.parseInt(props.getProperty(prop));
            prop = "scaledown";
            Integer.parseInt(props.getProperty(prop));
            prop = "gracetimes";
            for (String s : gracetimes) {
                Integer.parseInt(s);
            }
            prop = "thresholds";
            for (String s : thresholds) {
                Integer.parseInt(s);
            }
            prop = "evalPeriods";
            for (String s : evalPeriods) {
                Integer.parseInt(s);
            }
        } catch (NumberFormatException e) {
            System.err.println("Number format error in property " + prop);
        }
        if (metrics.length != gracetimes.length && metrics.length != evalPeriods.length && metrics.length * 2 != thresholds.length) {
            System.err.println("Invalid values for metrics, gracetimes, thresholds or evalPeriods in config file");
            System.exit(3);
        }
        retval.put("secGroups", secGroups);
        retval.put("regions", regions);
        retval.put("loadbalancers", loadbalancers);
        retval.put("metrics", metrics);
        retval.put("gracetimes", gracetimes);
        retval.put("thresholds", thresholds);
        retval.put("evalPeriods", evalPeriods);
        return retval;
    }

    private static void deleteASGroups() {
        DescribeAutoScalingGroupsRequest asRequest = new DescribeAutoScalingGroupsRequest();
        DescribeAutoScalingGroupsResult asResult = autoScalingClient.describeAutoScalingGroups(asRequest);
        for (AutoScalingGroup asGroup : asResult.getAutoScalingGroups()) {
            DeleteAutoScalingGroupRequest deleteRequest = new DeleteAutoScalingGroupRequest();
            List<String> instances = new ArrayList<String>();
            for (Instance instance : asGroup.getInstances()) {
                instances.add(instance.getInstanceId());
            }
            StopInstancesRequest stopRequest = new StopInstancesRequest();
            stopRequest.setInstanceIds(instances);
            ec2Client.stopInstances(stopRequest);
            deleteRequest.setForceDelete(true);
            deleteRequest.setAutoScalingGroupName(asGroup.getAutoScalingGroupName());
            autoScalingClient.deleteAutoScalingGroup(deleteRequest);
        }
    }

    private static void deleteLaunchConfigurations() {
        DescribeLaunchConfigurationsRequest launchRequest = new DescribeLaunchConfigurationsRequest();
        DescribeLaunchConfigurationsResult launchResult = autoScalingClient.describeLaunchConfigurations(launchRequest);
        for (LaunchConfiguration launchConfiguration : launchResult.getLaunchConfigurations()) {
            DeleteLaunchConfigurationRequest deleteRequest = new DeleteLaunchConfigurationRequest();
            deleteRequest.setLaunchConfigurationName(launchConfiguration.getLaunchConfigurationName());
            autoScalingClient.deleteLaunchConfiguration(deleteRequest);
        }
    }

    private static void createLaunchConfiguration() {
        CreateLaunchConfigurationRequest launchReq = new CreateLaunchConfigurationRequest();
        launchReq.setLaunchConfigurationName(props.getProperty("launchconfiguration"));
        launchReq.setImageId(props.getProperty("ami"));
        launchReq.setInstanceType(props.getProperty("type"));
        launchReq.setKeyName(props.getProperty("keyPair"));
        List<String> securityGroups = new ArrayList<String>();
        for (String secGroup : arrays.get("secGroups")) {
            securityGroups.add(secGroup);
        }
        launchReq.setSecurityGroups(securityGroups);
        try {
            autoScalingClient.createLaunchConfiguration(launchReq);
        } catch (AlreadyExistsException e) {
            // ignore
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(4);
        }
    }

    private static void createAutoScalingGroup() {
        CreateAutoScalingGroupRequest createReq = new CreateAutoScalingGroupRequest();
        createReq.setAutoScalingGroupName(props.getProperty("autoscaling"));
        List<String> regions = new ArrayList<String>();
        for (String region : arrays.get("regions")) {
            regions.add(region);
        }
        createReq.setAvailabilityZones(regions);
        createReq.setLaunchConfigurationName(props.getProperty("launchconfiguration"));
        List<String> loadbalancers = new ArrayList<String>();
        for (String loadbalancer : arrays.get("loadbalancers")) {
            loadbalancers.add(loadbalancer);
        }
        createReq.setLoadBalancerNames(loadbalancers);
        createReq.setHealthCheckGracePeriod(100);
        createReq.setHealthCheckType("ELB");
        createReq.setMinSize(Integer.parseInt(props.getProperty("min")));
        createReq.setMaxSize(Integer.parseInt(props.getProperty("max")));
        autoScalingClient.createAutoScalingGroup(createReq);
    }

    private static void createAlarms() {
        String[] metrics = arrays.get("metrics");
        String[] gracetimes = arrays.get("gracetimes");
        String[] thresholds = arrays.get("thresholds");
        String[] evalPeriods = arrays.get("evalPeriods");
        try {
            for (int i = 0; i < metrics.length; i++) {
                PutScalingPolicyRequest scaleUpReq = new PutScalingPolicyRequest();
                scaleUpReq.setAutoScalingGroupName(props.getProperty("autoscaling"));
                scaleUpReq.setAdjustmentType("ChangeInCapacity");
                scaleUpReq.setCooldown(Integer.parseInt(gracetimes[i]));
                scaleUpReq.setPolicyName("ox-aws-scale-up");
                scaleUpReq.setScalingAdjustment(Integer.parseInt(props.getProperty("scaleup")));
                PutScalingPolicyResult resultUp = autoScalingClient.putScalingPolicy(scaleUpReq);
                PutMetricAlarmRequest scaleUpAlarmRequest = new PutMetricAlarmRequest();
                StandardUnit unit = null;
                if (metrics[i].equals("cpu")) {
                    scaleUpAlarmRequest.setMetricName("CPUUtilization");
                    unit = StandardUnit.Percent;
                } else if (metrics[i].equals("diskread")) {
                    scaleUpAlarmRequest.setMetricName("DiskReadBytes");
                    unit = StandardUnit.KilobitsSecond;
                } else if (metrics[i].equals("diskreadops")) {
                    scaleUpAlarmRequest.setMetricName("DiskReadOps");
                    unit = StandardUnit.None;
                } else if (metrics[i].equals("diskwrite")) {
                    scaleUpAlarmRequest.setMetricName("DiskwriteBytes");
                    unit = StandardUnit.KilobitsSecond;
                } else if (metrics[i].equals("diskwriteops")) {
                    scaleUpAlarmRequest.setMetricName("DiskWriteOps");
                    unit = StandardUnit.None;
                } else if (metrics[i].equals("networkin")) {
                    scaleUpAlarmRequest.setMetricName("NetworkIn");
                    unit = StandardUnit.KilobitsSecond;
                } else if (metrics[i].equals("networkout")) {
                    scaleUpAlarmRequest.setMetricName("NetworkOut");
                    unit = StandardUnit.KilobitsSecond;
                } else {
                    System.err.println("Invalid metric");
                    System.exit(1);
                }
                scaleUpAlarmRequest.setAlarmDescription("Scale up " + metrics[i]);
                scaleUpAlarmRequest.setAlarmName("ox-aws-scale-up-" + metrics[i]);
                scaleUpAlarmRequest.setNamespace("AWS/EC2");
                scaleUpAlarmRequest.setStatistic(Statistic.Average);
                scaleUpAlarmRequest.setComparisonOperator(ComparisonOperator.GreaterThanThreshold);
                scaleUpAlarmRequest.setEvaluationPeriods(Integer.parseInt(evalPeriods[i]));
                scaleUpAlarmRequest.setPeriod(Integer.parseInt(gracetimes[i]));
                scaleUpAlarmRequest.setThreshold(Double.parseDouble(thresholds[i * 2]));
                List<String> scaleUpAlarms = new ArrayList<String>();
                scaleUpAlarms.add(resultUp.getPolicyARN());
                scaleUpAlarmRequest.setAlarmActions(scaleUpAlarms);
                Dimension scaleUpDimension = new Dimension();
                scaleUpDimension.setName("AutoScalingGroupName");
                scaleUpDimension.setValue(props.getProperty("autoscaling"));
                List<Dimension> dimensions = new ArrayList<Dimension>();
                dimensions.add(scaleUpDimension);
                scaleUpAlarmRequest.setDimensions(dimensions);
                scaleUpAlarmRequest.setUnit(unit);
                cloudWatchClient.putMetricAlarm(scaleUpAlarmRequest);

                PutScalingPolicyRequest scaleDownReq = new PutScalingPolicyRequest();
                scaleDownReq.setAutoScalingGroupName(props.getProperty("autoscaling"));
                scaleDownReq.setAdjustmentType("ChangeInCapacity");
                scaleDownReq.setCooldown(Integer.parseInt(gracetimes[i]));
                scaleDownReq.setPolicyName("ox-aws-scale-down");
                scaleDownReq.setScalingAdjustment(Integer.parseInt(props.getProperty("scaledown")));
                PutScalingPolicyResult resultDown = autoScalingClient.putScalingPolicy(scaleDownReq);
                PutMetricAlarmRequest scaleDownAlarmRequest = new PutMetricAlarmRequest();
                unit = null;
                if (metrics[i].equals("cpu")) {
                    scaleDownAlarmRequest.setMetricName("CPUUtilization");
                    unit = StandardUnit.Percent;
                } else if (metrics[i].equals("diskread")) {
                    scaleDownAlarmRequest.setMetricName("DiskReadBytes");
                    unit = StandardUnit.KilobitsSecond;
                } else if (metrics[i].equals("diskreadops")) {
                    scaleDownAlarmRequest.setMetricName("DiskReadOps");
                    unit = StandardUnit.None;
                } else if (metrics[i].equals("diskwrite")) {
                    scaleDownAlarmRequest.setMetricName("DiskwriteBytes");
                    unit = StandardUnit.KilobitsSecond;
                } else if (metrics[i].equals("diskwriteops")) {
                    scaleDownAlarmRequest.setMetricName("DiskWriteOps");
                    unit = StandardUnit.None;
                } else if (metrics[i].equals("networkin")) {
                    scaleDownAlarmRequest.setMetricName("NetworkIn");
                    unit = StandardUnit.KilobitsSecond;
                } else if (metrics[i].equals("networkout")) {
                    scaleDownAlarmRequest.setMetricName("NetworkOut");
                    unit = StandardUnit.KilobitsSecond;
                } else {
                    System.err.println("Invalid metric");
                    System.exit(1);
                }
                scaleDownAlarmRequest.setAlarmDescription("Scale down " + metrics[i]);
                scaleDownAlarmRequest.setAlarmName("ox-aws-scale-down-" + metrics[i]);
                scaleDownAlarmRequest.setNamespace("AWS/EC2");
                scaleDownAlarmRequest.setStatistic(Statistic.Average);
                scaleDownAlarmRequest.setComparisonOperator(ComparisonOperator.LessThanThreshold);
                scaleDownAlarmRequest.setEvaluationPeriods(Integer.parseInt(evalPeriods[i]));
                scaleDownAlarmRequest.setPeriod(Integer.parseInt(gracetimes[i]));
                scaleDownAlarmRequest.setThreshold(Double.parseDouble(thresholds[i * 2 + 1]));
                List<String> scaleDownAlarms = new ArrayList<String>();
                scaleDownAlarms.add(resultDown.getPolicyARN());
                scaleDownAlarmRequest.setAlarmActions(scaleDownAlarms);
                Dimension scaleDownDimension = new Dimension();
                scaleDownDimension.setName("AutoScalingGroupName");
                scaleDownDimension.setValue(props.getProperty("autoscaling"));
                List<Dimension> dimensionsDown = new ArrayList<Dimension>();
                dimensionsDown.add(scaleDownDimension);
                scaleDownAlarmRequest.setDimensions(dimensionsDown);
                scaleDownAlarmRequest.setUnit(unit);
                cloudWatchClient.putMetricAlarm(scaleDownAlarmRequest);
            }
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
            System.exit(4);
        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());
            System.exit(4);
        } catch (AmazonClientException e) {
            System.err.println(e.getMessage());
            System.exit(4);
        }

    }

}
