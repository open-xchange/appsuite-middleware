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

package com.openexchange.filestore.s3.osgi;

import com.amazonaws.metrics.AwsSdkMetrics;
import com.openexchange.config.Reloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.filestore.FileStorageProvider;
import com.openexchange.filestore.s3.internal.S3FileStorageFactory;
import com.openexchange.filestore.s3.internal.client.S3ClientFactory;
import com.openexchange.filestore.s3.internal.client.S3ClientRegistry;
import com.openexchange.filestore.s3.metrics.S3FileStorageMetricCollector;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.osgi.HousekeepingActivator;

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
        return new Class<?>[] { LeanConfigurationService.class, SSLSocketFactoryProvider.class, SSLConfigurationService.class };
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

        // Enable service metric collection (overall byte throughput)
        AwsSdkMetrics.setMetricCollector(new S3FileStorageMetricCollector(config));

        S3ClientRegistry registry = new S3ClientRegistry(new S3ClientFactory(), this);
        S3FileStorageFactory factory = new S3FileStorageFactory(registry, this);
        registerService(Reloadable.class, registry);
        registerService(FileStorageProvider.class, factory);
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(S3Activator.class);
        logger.info("Stopping bundle: com.openexchange.filestore.s3");

        AwsSdkMetrics.setMetricCollector(null);

        super.stopBundle();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }
}
