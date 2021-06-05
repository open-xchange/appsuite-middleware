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

package com.openexchange.groupware.infostore.media.osgi;

import org.osgi.framework.ServiceReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.infostore.media.MediaMetadataExtractorService;
import com.openexchange.groupware.infostore.media.image.ImageMediaMetadataExtractor;
import com.openexchange.groupware.infostore.media.impl.MediaMetadataExtractorRegistry;
import com.openexchange.groupware.infostore.media.metadata.MetadataExtractorMetadataService;
import com.openexchange.imagetransformation.ImageMetadataService;
import com.openexchange.metadata.MetadataService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link MediaMetadataActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class MediaMetadataActivator extends HousekeepingActivator {

    private MediaMetadataExtractorRegistry extractorRegistry;

    /**
     * Initializes a new {@link MediaMetadataActivator}.
     */
    public MediaMetadataActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        ConfigurationService configService = getService(ConfigurationService.class);
        int defaultValue = 60;
        int extractTimeOutSec = null == configService ? defaultValue : configService.getIntProperty("com.openexchange.groupware.infostore.media.extract.timeout", defaultValue);

        MediaMetadataExtractorRegistry extractorRegistry = new MediaMetadataExtractorRegistry(extractTimeOutSec);
        this.extractorRegistry = extractorRegistry;
        extractorRegistry.start();

        extractorRegistry.addExtractor(ImageMediaMetadataExtractor.getInstance());
        // Do not extract media metadata from video files for now
        //extractorRegistry.addExtractor(VideoMediaMetadataExtractor.getInstance());

        track(ImageMetadataService.class, new SimpleRegistryListener<ImageMetadataService>() {

            @Override
            public void added(ServiceReference<ImageMetadataService> ref, ImageMetadataService service) {
                ServerServiceRegistry.getInstance().addService(ImageMetadataService.class, service);
            }

            @Override
            public void removed(ServiceReference<ImageMetadataService> ref, ImageMetadataService service) {
                ServerServiceRegistry.getInstance().removeService(ImageMetadataService.class);
            }

        });
        openTrackers();

        registerService(MediaMetadataExtractorService.class, extractorRegistry);
        registerService(MetadataService.class, new MetadataExtractorMetadataService());
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        ServerServiceRegistry.getInstance().removeService(MediaMetadataExtractorService.class);

        MediaMetadataExtractorRegistry extractorRegistry = this.extractorRegistry;
        if (null != extractorRegistry) {
            this.extractorRegistry = null;
            // Do not extract media metadata from video files for now
            //extractorRegistry.removeExtractor(VideoMediaMetadataExtractor.getInstance());
            extractorRegistry.removeExtractor(ImageMediaMetadataExtractor.getInstance());

            extractorRegistry.stop();
        }

        super.stopBundle();
    }

}
