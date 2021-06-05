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

package com.openexchange.carddav.osgi;

import static com.openexchange.dav.DAVTools.getExternalPath;
import static com.openexchange.dav.DAVTools.getInternalPath;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.carddav.Tools;
import com.openexchange.carddav.photos.PhotoPerformer;
import com.openexchange.carddav.servlet.CardDAV;
import com.openexchange.carddav.servlet.CarddavPerformer;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.similarity.ContactSimilarityService;
import com.openexchange.contact.storage.ContactTombstoneStorage;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.dav.DAVServlet;
import com.openexchange.dav.WellKnownServlet;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.login.Interface;
import com.openexchange.oauth.provider.resourceserver.scope.AbstractScopeProvider;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.service.http.HttpServices;
import com.openexchange.resource.ResourceService;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.osgi.OSGiPropertyMixin;

/**
 * {@link CarddavActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CarddavActivator extends HousekeepingActivator {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CarddavActivator.class);

    private OSGiPropertyMixin mixin;

    private String httpAliasCardDAV;
    private String httpAliasPhotos;

    /**
     * Initializes a new {@link CarddavActivator}.
     */
    public CarddavActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {
            HttpService.class, FolderService.class, ConfigViewFactory.class, UserService.class, ContactService.class,
            ResourceService.class, VCardService.class, GroupService.class, CapabilityService.class, LeanConfigurationService.class
        };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class[] { ContactTombstoneStorage.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        try {
            LOG.info("starting bundle: \"com.openexchange.carddav\"");
            /*
             * prepare CardDAV performer & initialize global OSGi property mixin
             */
            CarddavPerformer performer = new CarddavPerformer(this);
            OSGiPropertyMixin mixin = new OSGiPropertyMixin(context, performer);
            performer.setGlobalMixins(mixin);
            this.mixin = mixin;
            /*
             * register CardDAV servlet & WebDAV path
             */
            ConfigViewFactory configViewFactory = getServiceSafe(ConfigViewFactory.class);
            httpAliasCardDAV = getInternalPath(configViewFactory, "/carddav");
            getService(HttpService.class).registerServlet(httpAliasCardDAV, new CardDAV(performer), null, null);
            getService(HttpService.class).registerServlet("/.well-known/carddav", new WellKnownServlet(getExternalPath(configViewFactory, "/carddav"), Interface.CARDDAV), null, null);
            registerService(OAuthScopeProvider.class, new AbstractScopeProvider(Tools.OAUTH_SCOPE, OAuthStrings.SYNC_CONTACTS) {

                @Override
                public boolean canBeGranted(CapabilitySet capabilities) {
                    return capabilities.contains(Permission.CARDDAV.getCapabilityName());
                }
            });
            /*
             * register Photo performer for referenced contact images in vCards
             */
            httpAliasPhotos = getInternalPath(configViewFactory, "/photos");
            getService(HttpService.class).registerServlet(httpAliasPhotos, new DAVServlet(new PhotoPerformer(this), Interface.CARDDAV), null, null);
            /*
             * track optional services
             */
            trackService(VCardStorageFactory.class);
            trackService(ContactSimilarityService.class);
            trackService(ImageTransformationService.class);
            openTrackers();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(CarddavActivator.class).error("", e);
            throw e;
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        LOG.info("stopping bundle: \"com.openexchange.carddav\"");
        /*
         * close OSGi property mixin
         */
        OSGiPropertyMixin mixin = this.mixin;
        if (null != mixin) {
            mixin.close();
            this.mixin = null;
        }
        /*
         * unregister servlet
         */
        HttpService httpService = getService(HttpService.class);
        if (null != httpService) {
            String alias = httpAliasCardDAV;
            if (alias != null) {
                httpAliasCardDAV = null;
                HttpServices.unregister(alias, httpService);
            }
            alias = httpAliasPhotos;
            if (alias != null) {
                httpAliasPhotos = null;
                HttpServices.unregister(alias, httpService);
            }
        }
        super.stopBundle();
    }
}
