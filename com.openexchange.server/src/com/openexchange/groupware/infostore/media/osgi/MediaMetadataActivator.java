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
 *    trademarks of the OX Software GmbH. group of companies.
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
