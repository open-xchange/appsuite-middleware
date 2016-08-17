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
