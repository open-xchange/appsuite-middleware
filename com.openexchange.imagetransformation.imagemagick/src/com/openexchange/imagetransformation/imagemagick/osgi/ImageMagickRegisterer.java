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

package com.openexchange.imagetransformation.imagemagick.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.imagetransformation.ImageTransformationProvider;
import com.openexchange.imagetransformation.TransformedImageCreator;
import com.openexchange.imagetransformation.imagemagick.ImageMagickImageTransformationProvider;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ImageMagickRegisterer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ImageMagickRegisterer implements Reloadable {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ImageMagickRegisterer.class);

    private static volatile Long maxSize;
    public static long maxSize() {
        Long tmp = maxSize;
        if (null == tmp) {
            synchronized (ImageMagickRegisterer.class) {
                tmp = maxSize;
                if (null == tmp) {
                    int defaultValue = 5242880; // 5 MB
                    ConfigurationService configService = Services.getService(ConfigurationService.class);
                    if (null == configService) {
                        return defaultValue;
                    }
                    tmp = Long.valueOf(configService.getIntProperty("com.openexchange.tools.images.transformations.maxSize", defaultValue));
                    maxSize = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    private static volatile Long maxResolution;
    public static long maxResolution() {
        Long tmp = maxResolution;
        if (null == tmp) {
            synchronized (ImageMagickRegisterer.class) {
                tmp = maxResolution;
                if (null == tmp) {
                    int defaultValue = 12087962; // 4064 x 2704 (11.1 megapixels) + 10%
                    ConfigurationService configService = Services.getService(ConfigurationService.class);
                    if (null == configService) {
                        return defaultValue;
                    }
                    tmp = Long.valueOf(configService.getIntProperty("com.openexchange.tools.images.transformations.maxResolution", defaultValue));
                    maxResolution = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    // ---------------------------------------------------------------------------------------------------------------

    private final BundleContext context;
    private final ServiceLookup services;
    private ServiceRegistration<ImageTransformationProvider> registration;
    private ImageMagickImageTransformationProvider provider;

    /**
     * Initializes a new {@link ImageMagickRegisterer}.
     */
    public ImageMagickRegisterer(BundleContext context, ServiceLookup services) {
        super();
        this.context = context;
        this.services = services;
    }

    /**
     * Performs the registration/unregistration for ImageMagick-based image transformation provider.
     *
     * @param configService The configuration service
     */
    public synchronized void perform(ConfigurationService configService) {
        boolean enabled = configService.getBoolProperty("com.openexchange.imagetransformation.imagemagick.enabled", true);
        if (enabled) {
            String searchPath = configService.getProperty("com.openexchange.imagetransformation.imagemagick.searchPath", "/usr/bin");
            boolean useGraphicsMagick = configService.getBoolProperty("com.openexchange.imagetransformation.imagemagick.useGraphicsMagick", false);
            int numThreads = configService.getIntProperty("com.openexchange.imagetransformation.imagemagick.numThreads", 10);
            int timeoutSecs = configService.getIntProperty("com.openexchange.imagetransformation.imagemagick.timeoutSecs", 0);
            register(searchPath, useGraphicsMagick, numThreads, timeoutSecs);
        } else {
            unregister();
        }
    }

    /**
     * Registers the ImageMagick-based image transformation provider
     *
     * @param searchPath The search path where "convert" command is located
     * @param useGraphicsMagick Whether GraphicsMagick is supposed to be used
     * @param numThreads The number of threads to use
     * @param timeoutSecs The timeout in seconds
     */
    private void register(String searchPath, boolean useGraphicsMagick, int numThreads, int timeoutSecs) {
        if (null == provider) {
            provider = new ImageMagickImageTransformationProvider(services.getService(TransformedImageCreator.class), searchPath, useGraphicsMagick, numThreads, timeoutSecs);

            Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
            properties.put(Constants.SERVICE_RANKING, Integer.valueOf(10));
            registration = context.registerService(ImageTransformationProvider.class, provider, properties);
            LOG.info("ImageMagick-based image transformation provider successfully registered using search path {} with GraphicsMagick utilization set to {}", searchPath, Boolean.valueOf(useGraphicsMagick));
        } else {
            provider.setSearchPath(searchPath);
            provider.setUseGraphicsMagick(useGraphicsMagick);
            provider.setNumThreads(numThreads);
            LOG.info("ImageMagick-based image transformation provider now using search path {} with GraphicsMagick utilization set to {}", searchPath, Boolean.valueOf(useGraphicsMagick));
        }
    }

    /**
     * Unregisters the ImageMagick-based image transformation provider
     */
    private void unregister() {
        if (null != registration) {
            registration.unregister();
            registration = null;
            LOG.info("ImageMagick-based image transformation provider successfully un-registered");
        }
    }

    /**
     * Closes this instance and releases all resources associated with it.
     */
    public synchronized void close() {
        unregister();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        maxSize = null;
        maxResolution = null;

        perform(configService);
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForFiles("imagemagick.properties");
    }

}
