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

package com.openexchange.image.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.ajax.requesthandler.crypto.CryptographicServiceAuthenticationFactory;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.conversion.DataSource;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.filemanagement.internal.ManagedFileImageDataSource;
import com.openexchange.image.ImageActionFactory;
import com.openexchange.image.ImageUtility;
import com.openexchange.image.Mp3ImageDataSource;
import com.openexchange.mail.api.crypto.CryptographicAwareMailAccessFactory;
import com.openexchange.mail.conversion.InlineImageDataSource;
import com.openexchange.mail.mime.crypto.PGPMailRecognizer;
import com.openexchange.mail.service.EncryptedMailService;
import com.openexchange.server.ExceptionOnAbsenceServiceLookup;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ImageActivator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ImageActivator extends AJAXModuleActivator {

    /**
     * Initializes a new {@link ImageActivator}.
     */
    public ImageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ManagedFileManagement.class, DispatcherPrefixService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class<?>[] { CryptographicServiceAuthenticationFactory.class, CryptographicAwareMailAccessFactory.class, EncryptedMailService.class, PGPMailRecognizer.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final ServiceLookup serviceLookup = new ExceptionOnAbsenceServiceLookup(this);
        Services.setServiceLookup(serviceLookup);

        ImageUtility.setDispatcherPrefixService(getService(DispatcherPrefixService.class));
        {
            InlineImageDataSource inlineDataSource = InlineImageDataSource.getInstance();
            inlineDataSource.setServiceLookup(this);
            Dictionary<String, Object> inlineProps = new Hashtable<String, Object>(1);
            inlineProps.put("identifier", inlineDataSource.getRegistrationName());
            registerService(DataSource.class, inlineDataSource, inlineProps);
            ImageActionFactory.addMapping(inlineDataSource.getRegistrationName(), inlineDataSource.getAlias());
        }
        {
            Mp3ImageDataSource mp3DataSource = Mp3ImageDataSource.getInstance();
            Dictionary<String, Object> mp3Props = new Hashtable<String, Object>(1);
            mp3Props.put("identifier", mp3DataSource.getRegistrationName());
            registerService(DataSource.class, mp3DataSource, mp3Props);
            ImageActionFactory.addMapping(mp3DataSource.getRegistrationName(), mp3DataSource.getAlias());
        }
        {
            ManagedFileManagement service = getService(ManagedFileManagement.class);
            ManagedFileImageDataSource imageDataSource = new ManagedFileImageDataSource(service);
            Dictionary<String, Object> imageProps = new Hashtable<String, Object>(1);
            imageProps.put("identifier", imageDataSource.getRegistrationName());
            registerService(DataSource.class, imageDataSource, imageProps);
            ImageActionFactory.addMapping(imageDataSource.getRegistrationName(), imageDataSource.getAlias());
        }
        registerModule(new ImageActionFactory(this), "image");
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
