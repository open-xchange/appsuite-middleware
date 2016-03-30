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

package com.openexchange.image.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.conversion.DataSource;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.filemanagement.internal.ManagedFileImageDataSource;
import com.openexchange.groupware.contact.datasource.ContactImageDataSource;
import com.openexchange.groupware.contact.datasource.UserImageDataSource;
import com.openexchange.image.ImageActionFactory;
import com.openexchange.image.ImageUtility;
import com.openexchange.image.Mp3ImageDataSource;
import com.openexchange.mail.conversion.InlineImageDataSource;

/**
 * {@link ImageActivator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ImageActivator extends AJAXModuleActivator {

    public ImageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ManagedFileManagement.class, DispatcherPrefixService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        ImageUtility.setDispatcherPrefixService(getService(DispatcherPrefixService.class));
        {
            InlineImageDataSource inlineDataSource = InlineImageDataSource.getInstance();
            Dictionary<String, Object> inlineProps = new Hashtable<String, Object>(1);
            inlineProps.put("identifier", inlineDataSource.getRegistrationName());
            registerService(DataSource.class, inlineDataSource, inlineProps);
            ImageActionFactory.addMapping(inlineDataSource.getRegistrationName(), inlineDataSource.getAlias());
        }
        {
            ContactImageDataSource contactDataSource = ContactImageDataSource.getInstance();
            Dictionary<String, Object> contactProps = new Hashtable<String, Object>(1);
            contactProps.put("identifier", contactDataSource.getRegistrationName());
            registerService(DataSource.class, contactDataSource, contactProps);
            ImageActionFactory.addMapping(contactDataSource.getRegistrationName(), contactDataSource.getAlias());
        }
        {
            UserImageDataSource userDataSource = UserImageDataSource.getInstance();
            Dictionary<String, Object> contactProps = new Hashtable<String, Object>(1);
            contactProps.put("identifier", userDataSource.getRegistrationName());
            registerService(DataSource.class, userDataSource, contactProps);
            ImageActionFactory.addMapping(userDataSource.getRegistrationName(), userDataSource.getAlias());
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

}
