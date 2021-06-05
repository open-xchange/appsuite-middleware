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

package com.openexchange.imageconverter.api;

import com.openexchange.management.MBeanMethodAnnotation;

/**
 * {@link IImageConverterMonitoring}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10.0
 */
public interface IImageConverterMonitoring {

    @MBeanMethodAnnotation (description="The ratio of successful image results that are retrieved from the cache against the total number of processed images as simple quotient for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public double getCacheHitRatio();

    @MBeanMethodAnnotation (description="The number of image keys that have been processed for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getKeysProcessedCount();

    @MBeanMethodAnnotation (description="The median processing time in milliseconds for one image key for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getMedianKeyProcessTimeMillis();

    @MBeanMethodAnnotation (description="The number of all WebService requests for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getRequestCount_Total();

    @MBeanMethodAnnotation (description="The number of all WebService get image requests for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getRequestCount_Get();

    @MBeanMethodAnnotation (description="The number of all WebService cache image requests for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getRequestCount_Cache();

    @MBeanMethodAnnotation (description="The number of all WebService cacheAndGet image requests for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getRequestCount_CacheAndGet();

    @MBeanMethodAnnotation (description="The number of all WebService administration requests for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getRequestCount_Admin();

    @MBeanMethodAnnotation (description="The median time in milliseconds, that a WebService request lasted, for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getMedianRequestTimeMillis_Total();

    @MBeanMethodAnnotation (description="The median time in milliseconds, that a WebService get request lasted, for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getMedianRequestTimeMillis_Get();

    @MBeanMethodAnnotation (description="The median time in milliseconds, that a WebService cache request lasted, for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getMedianRequestTimeMillis_Cache();

    @MBeanMethodAnnotation (description="The median time in milliseconds, that a WebService cacheAndGet request lasted, for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getMedianRequestTimeMillis_CacheAndGet();

    @MBeanMethodAnnotation (description="The median time in milliseconds, that a WebService administration request lasted, for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getMedianRequestTimeMillis_Admin();

    @MBeanMethodAnnotation (description="The peak number of background priority image key requests in queue, measured from the beginning of the last reset/initialization on (default period: 5 minutes)", parameterDescriptions = {}, parameters = {})
    public long getPeakKeyCountInQueue_Background();

    @MBeanMethodAnnotation (description="The peak number of medium priority image key requests in queue, measured from the beginning of the last reset/initialization on (default period: 5 minutes)", parameterDescriptions = {}, parameters = {})
    public long getPeakKeyCountInQueue_Medium();

    @MBeanMethodAnnotation (description="The peak number of instant priority image key requests in queue, measured from the beginning of the last reset/initialization on (default period: 5 minutes)", parameterDescriptions = {}, parameters = {})
    public long getPeakKeyCountInQueue_Instant();

    @MBeanMethodAnnotation (description="The median time in milliseconds, that a WebService image key request stayed in queue, for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getMedianKeyQueueTimeMillis_Total();

    @MBeanMethodAnnotation (description="The median time in milliseconds, that a WebService background priority image key request stayed in queue, for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getMedianKeyQueueTimeMillis_Background();

    @MBeanMethodAnnotation (description="The median time in milliseconds, that a WebService medium priority image key request stayed in queue, for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getMedianKeyQueueTimeMillis_Medium();

    @MBeanMethodAnnotation (description="The median time in milliseconds, that a WebService instant priority image key request stayed in queue, for the current ImageConverter server instance", parameterDescriptions = {}, parameters = {})
    public long getMedianKeyQueueTimeMillis_Instant();

    @MBeanMethodAnnotation (description="The total number of image keys cached", parameterDescriptions = {}, parameters = {})
    public long getCacheKeyCount();

    @MBeanMethodAnnotation (description="The accumulated size of all cached images in bytes", parameterDescriptions = {}, parameters = {})
    public long getCacheSize();

}
