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

package com.openexchange.imagetransformation.osgi;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.imagetransformation.ImageTransformationIdler;
import com.openexchange.imagetransformation.ImageTransformationProvider;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.imagetransformation.ImageTransformations;
import com.openexchange.java.ConcurrentPriorityQueue;
import com.openexchange.osgi.util.RankedService;

/**
 * {@link ImageTransformationServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ImageTransformationServiceImpl extends ServiceTracker<ImageTransformationProvider, ImageTransformationProvider> implements ImageTransformationService {

    private final ConcurrentPriorityQueue<RankedService<ImageTransformationProvider>> trackedProviders;
    private ServiceRegistration<ImageTransformationService> registration; // non-volatile, protected by synchronized blocks
    private ImageTransformationProvider activeProvider;

    /**
     * Initializes a new {@link ImageTransformationServiceImpl}.
     */
    public ImageTransformationServiceImpl(BundleContext context) {
        super(context, ImageTransformationProvider.class, null);
        trackedProviders = new ConcurrentPriorityQueue<RankedService<ImageTransformationProvider>>();
    }

    @Override
    public synchronized ImageTransformationProvider addingService(ServiceReference<ImageTransformationProvider> reference) {
        ImageTransformationProvider provider = context.getService(reference);
        trackedProviders.offer(new RankedService<ImageTransformationProvider>(provider, RankedService.getRanking(reference)));

        // Check what provider is now active after offering to queue
        RankedService<ImageTransformationProvider> rankedService = trackedProviders.peek();
        if (null != rankedService && !rankedService.service.equals(activeProvider)) {
            // Switch "active" provider after offering to queue
            if (activeProvider instanceof ImageTransformationIdler) {
                ((ImageTransformationIdler) activeProvider).idle();
            }
            activeProvider = rankedService.service;
        }

        if (null == registration) {
            registration = context.registerService(ImageTransformationService.class, this, null);
        }

        return provider;
    }

    @Override
    public void modifiedService(ServiceReference<ImageTransformationProvider> reference, ImageTransformationProvider service) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<ImageTransformationProvider> reference, ImageTransformationProvider provider) {
        trackedProviders.remove(new RankedService<ImageTransformationProvider>(provider, RankedService.getRanking(reference)));

        // Check what provider is now active after removing from queue
        RankedService<ImageTransformationProvider> rankedService = trackedProviders.peek();
        if (null != rankedService && !rankedService.service.equals(activeProvider)) {
            // Switch "active" provider after removing from queue
            if (activeProvider instanceof ImageTransformationIdler) {
                ((ImageTransformationIdler) activeProvider).idle();
            }
            // Check whether to "idle" currently disappearing provider
            if (!activeProvider.equals(provider) && (provider instanceof ImageTransformationIdler)) {
                ((ImageTransformationIdler) provider).idle();
            }
            activeProvider = rankedService.service;
        }

        if (trackedProviders.isEmpty() && null != registration) {
            registration.unregister();
            registration = null;
        }

        context.ungetService(reference);
    }

    /**
     * Gets the currently available {@code ImageTransformationProvider} instance having the highest rank.
     *
     * @return The highest-ranked {@code ImageTransformationProvider} instance or <code>null</code>
     */
    private ImageTransformationProvider getHighestRankedImageTransformationProvider() {
        RankedService<ImageTransformationProvider> rankedService = trackedProviders.peek();
        return null == rankedService ? null : rankedService.service;
    }

    // ----------------------------------------------------------------------------------------------------------------------


    @Override
    public ImageTransformations transfom(BufferedImage sourceImage) throws IOException {
        ImageTransformationProvider provider = getHighestRankedImageTransformationProvider();
        if (null == provider) {
            // About to shut-down
            throw new IllegalStateException("Image transformation service is about to shut-down");
        }
        return provider.transfom(sourceImage);
    }

    @Override
    public ImageTransformations transfom(BufferedImage sourceImage, Object source) throws IOException {
        ImageTransformationProvider provider = getHighestRankedImageTransformationProvider();
        if (null == provider) {
            // About to shut-down
            throw new IllegalStateException("Image transformation service is about to shut-down");
        }
        return provider.transfom(sourceImage, source);
    }

    @Override
    public ImageTransformations transfom(InputStream imageStream) throws IOException {
        ImageTransformationProvider provider = getHighestRankedImageTransformationProvider();
        if (null == provider) {
            // About to shut-down
            throw new IOException("Image transformation service is about to shut-down");
        }
        return provider.transfom(imageStream);
    }

    @Override
    public ImageTransformations transfom(InputStream imageStream, Object source) throws IOException {
        ImageTransformationProvider provider = getHighestRankedImageTransformationProvider();
        if (null == provider) {
            // About to shut-down
            throw new IOException("Image transformation service is about to shut-down");
        }
        return provider.transfom(imageStream, source);
    }

    @Override
    public ImageTransformations transfom(IFileHolder imageFile, Object source) throws IOException {
        ImageTransformationProvider provider = getHighestRankedImageTransformationProvider();
        if (null == provider) {
            // About to shut-down
            throw new IOException("Image transformation service is about to shut-down");
        }
        return provider.transfom(imageFile, source);
    }

    @Override
    public ImageTransformations transfom(byte[] imageData) throws IOException {
        ImageTransformationProvider provider = getHighestRankedImageTransformationProvider();
        if (null == provider) {
            // About to shut-down
            throw new IOException("Image transformation service is about to shut-down");
        }
        return provider.transfom(imageData);
    }

    @Override
    public ImageTransformations transfom(byte[] imageData, Object source) throws IOException {
        ImageTransformationProvider provider = getHighestRankedImageTransformationProvider();
        if (null == provider) {
            // About to shut-down
            throw new IOException("Image transformation service is about to shut-down");
        }
        return provider.transfom(imageData, source);
    }

}
