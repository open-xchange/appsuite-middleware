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

package com.openexchange.aws.osgi;

import java.security.MessageDigest;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.logging.Log;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.openexchange.aws.exceptions.OXAWSExceptionCodes;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link AWSActivator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class AWSActivator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(AWSActivator.class);

    /**
     * Initializes a new {@link AWSActivator}.
     */
    public AWSActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            LOG.info("Starting bundle: com.openexchange.aws");
            final ConfigurationService configService = getService(ConfigurationService.class);
            final String accessKey = configService.getProperty("com.openexchange.aws.accessKey");
            final String secretKey = configService.getProperty("com.openexchange.aws.secretKey");
            final String ec2Region = configService.getProperty("com.openexchange.aws.ec2region");
            final String lbRegion = configService.getProperty("com.openexchange.aws.lbregion");
            final String autoscalingRegion = configService.getProperty("com.openexchange.aws.autoscalingregion");
            final String cloudwatchRegion = configService.getProperty("com.openexchange.aws.cloudwatchregion");
            final String amazonS3Region = configService.getProperty("com.openexchange.aws.s3region");
            final boolean s3encryption = configService.getBoolProperty("com.openexchange.aws.s3encryption", false);
            if (accessKey == null) {
                throw OXAWSExceptionCodes.AWS_NO_ACCESSKEY.create();
            }
            if (secretKey == null) {
                throw OXAWSExceptionCodes.AWS_NO_SECRETKEY.create();
            }
            if (ec2Region == null) {
                throw OXAWSExceptionCodes.AWS_NO_EC2_REGION.create();
            }
            if (lbRegion == null) {
                throw OXAWSExceptionCodes.AWS_NO_LB_REGION.create();
            }
            if (amazonS3Region == null) {
                throw OXAWSExceptionCodes.AWS_NO_S3_REGION.create();
            }
            final AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            final AmazonEC2 amazonEC2 = new AmazonEC2Client(credentials);
            amazonEC2.setEndpoint(ec2Region);
            final AmazonElasticLoadBalancing amazonLoadBalancing = new AmazonElasticLoadBalancingClient(credentials);
            amazonLoadBalancing.setEndpoint(lbRegion);
            final AmazonAutoScaling amazonAutoScaling = new AmazonAutoScalingClient(credentials);
            amazonAutoScaling.setEndpoint(autoscalingRegion);
            final AmazonCloudWatch amazonCloudWatch = new AmazonCloudWatchClient(credentials);
            amazonCloudWatch.setEndpoint(cloudwatchRegion);
            AmazonS3 amazonS3 = null;
            if (s3encryption) {
                SecretKey sKey = null;
                try {
                    final String aesKey = configService.getProperty("com.openexchange.aws.aeskey");
                    final String aesSalt = configService.getProperty("com.openexchange.aws.aessalt");
                    final String aesIV = configService.getProperty("com.openexchange.aws.aesiv");
                    byte[] key = (aesIV + aesKey + aesSalt).getBytes("UTF-8");
                    final MessageDigest sha = MessageDigest.getInstance("SHA-256");
                    key = sha.digest();
                    sKey = new SecretKeySpec(key, "AES");
                } catch (final Exception e) {
                    throw OXAWSExceptionCodes.AWS_S3_ENCRYPTION_ERROR.create(e.getMessage());
                }
                final EncryptionMaterials encryptionMaterials = new EncryptionMaterials(sKey);
                amazonS3 = new AmazonS3EncryptionClient(credentials, encryptionMaterials);
            } else {
                amazonS3 = new AmazonS3Client(credentials);
            }
            amazonS3.setEndpoint(amazonS3Region);
            registerService(AmazonEC2.class, amazonEC2);
            registerService(AmazonElasticLoadBalancing.class, amazonLoadBalancing);
            registerService(AmazonAutoScaling.class, amazonAutoScaling);
            registerService(AmazonCloudWatch.class, amazonCloudWatch);
            registerService(AmazonS3.class, amazonS3);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final Log log = com.openexchange.log.Log.loggerFor(AWSActivator.class);
        log.info("Stopping bundle: com.openexchange.aws");
        unregisterServices();
        cleanUp();
    }
}
