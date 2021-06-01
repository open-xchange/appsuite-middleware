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

package com.openexchange.imagetransformation.java.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.imagetransformation.ImageMetadataService;
import com.openexchange.imagetransformation.ImageTransformationProvider;
import com.openexchange.imagetransformation.java.impl.JavaImageTransformationProvider;
import com.openexchange.imagetransformation.java.scheduler.Scheduler;
import com.openexchange.imagetransformation.java.services.JavaImageMetadataService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.processing.ProcessorService;
import com.openexchange.timer.TimerService;


/**
 * {@link ImageTransformationActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ImageTransformationActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ProcessorService.class, TimerService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        registerService(ImageTransformationProvider.class, new JavaImageTransformationProvider(), 0);
        registerService(ImageMetadataService.class, new JavaImageMetadataService());
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);
        Scheduler.shutDown();
        super.stopBundle();
    }

}
