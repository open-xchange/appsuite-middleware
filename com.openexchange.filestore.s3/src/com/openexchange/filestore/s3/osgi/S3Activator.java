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

package com.openexchange.filestore.s3.osgi;

import org.osgi.framework.ServiceReference;
import com.amazonaws.metrics.AwsSdkMetrics;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.filestore.FileStorageProvider;
import com.openexchange.filestore.s3.internal.S3FileStorageFactory;
import com.openexchange.filestore.s3.internal.S3Properties;
import com.openexchange.filestore.s3.metrics.S3FileStorageMetricCollector;
import com.openexchange.metrics.MetricService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;

/**
 * {@link S3Activator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class S3Activator extends HousekeepingActivator {

    /**
     * Initializes a new {@link S3Activator}.
     */
    public S3Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { LeanConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(S3Activator.class);
        logger.info("Starting bundle: com.openexchange.filestore.s3");

        // Disable MD5 validation...
        //
        // If enabled, requesting an object's input stream has an intermediate DigestValidationInputStream, which breaks
        // to communication a call to 'abort()' to the low-level S3AbortableInputStream as it does not inherit from SdkFilterInputStream.
        // In consequence, AbortIfNotFullyConsumedS3ObjectInputStreamWrapper is of no effect: The HTTP request is not aborted and an
        // accompanying WARN message occurs (see com.amazonaws.services.s3.internal.S3AbortableInputStream.close())
        //
        // Stack:
        //    S3AbortableInputStream.close() line: 182
        //    S3ObjectInputStream(SdkFilterInputStream).close() line: 99
        //    S3ObjectInputStream.close() line: 136
        //    ServiceClientHolderInputStream(SdkFilterInputStream).close() line: 99
        //    AmazonS3Client$2(SdkFilterInputStream).close() line: 99
        //    AmazonS3Client$2(ProgressInputStream).close() line: 211
        //    DigestValidationInputStream(FilterInputStream).close() line: 181
        //    IOUtils.closeQuietly(Closeable, Log) line: 70
        //    S3ObjectInputStream.abort() line: 98
        //    AbortIfNotFullyConsumedS3ObjectInputStreamWrapper.close() line: 111
        //     ...
        //
        System.setProperty(com.amazonaws.services.s3.internal.SkipMd5CheckStrategy.DISABLE_GET_OBJECT_MD5_VALIDATION_PROPERTY, "true");

        final LeanConfigurationService config = getService(LeanConfigurationService.class);
        track(MetricService.class, new SimpleRegistryListener<MetricService>() {
            @Override
            public void added(ServiceReference<MetricService> ref, MetricService service) {
                // Check for metric collection
                boolean metricCollection = config.getBooleanProperty(S3Properties.METRIC_COLLECTION);
                if (metricCollection) {
                    // Enable metric collection by overriding the default metrics
                    AwsSdkMetrics.setMetricCollector(new S3FileStorageMetricCollector(service, config));
                }
            }

            @Override
            public void removed(ServiceReference<MetricService> ref, MetricService service) {
                AwsSdkMetrics.setMetricCollector(null);
            }
        });
        openTrackers();

        S3FileStorageFactory factory = new S3FileStorageFactory(this);
        registerService(FileStorageProvider.class, factory);
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(S3Activator.class);
        logger.info("Stopping bundle: com.openexchange.filestore.s3");

        AwsSdkMetrics.setMetricCollector(null);

        super.stopBundle();
    }
}
