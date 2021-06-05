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

package com.openexchange.dav.osgi;

import static com.openexchange.dav.DAVTools.getInternalPath;
import org.osgi.service.http.HttpService;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.clientinfo.ClientInfoProvider;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.dav.DAVClientInfoProvider;
import com.openexchange.dav.DAVServlet;
import com.openexchange.dav.attachments.AttachmentPerformer;
import com.openexchange.dav.mixins.AddressbookHomeSet;
import com.openexchange.dav.mixins.CalendarHomeSet;
import com.openexchange.dav.mixins.PrincipalCollectionSet;
import com.openexchange.dav.principals.PrincipalPerformer;
import com.openexchange.dav.root.RootPerformer;
import com.openexchange.group.GroupService;
import com.openexchange.login.Interface;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.resource.ResourceService;
import com.openexchange.uadetector.UserAgentParser;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.helpers.PropertyMixin;
import com.openexchange.webdav.protocol.osgi.OSGiPropertyMixin;

/**
 * {@link DAVActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class DAVActivator extends HousekeepingActivator {

    private OSGiPropertyMixin mixin;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { UserService.class, HttpService.class, ContactService.class, GroupService.class, ResourceService.class, UserAgentParser.class, ConfigViewFactory.class, CapabilityService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        ConfigViewFactory configViewFactory = getService(ConfigViewFactory.class);
        HttpService httpService = getService(HttpService.class);
        /*
         * root
         */
        RootPerformer rootPerformer = new RootPerformer(this);
        httpService.registerServlet(getInternalPath(configViewFactory, null), new DAVServlet(rootPerformer, Interface.CALDAV), null, null);
        /*
         * attachments
         */
        AttachmentPerformer attachmentPerformer = new AttachmentPerformer(this);
        httpService.registerServlet(getInternalPath(configViewFactory, "attachments"), new DAVServlet(attachmentPerformer, Interface.CALDAV), null, null);
        /*
         * principals
         */
        PrincipalPerformer principalPerformer = new PrincipalPerformer(this);
        httpService.registerServlet(getInternalPath(configViewFactory, "principals"), new DAVServlet(principalPerformer, Interface.CARDDAV), null, null);
        OSGiPropertyMixin mixin = new OSGiPropertyMixin(context, principalPerformer);
        principalPerformer.setGlobalMixins(mixin);
        this.mixin = mixin;
        /*
         * OSGi mixins
         */
        registerService(PropertyMixin.class, new PrincipalCollectionSet(configViewFactory));
        registerService(PropertyMixin.class, new CalendarHomeSet(configViewFactory));
        registerService(PropertyMixin.class, new AddressbookHomeSet(configViewFactory));
        /*
         * DAV client info
         */
        registerService(ClientInfoProvider.class, new DAVClientInfoProvider(getService(UserAgentParser.class)), 0);
        openTrackers();
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        OSGiPropertyMixin mixin = this.mixin;
        if (null != mixin) {
            mixin.close();
            this.mixin = null;
        }
        super.stopBundle();
    }

}
