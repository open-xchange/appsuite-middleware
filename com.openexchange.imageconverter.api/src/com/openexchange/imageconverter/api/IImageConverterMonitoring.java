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
