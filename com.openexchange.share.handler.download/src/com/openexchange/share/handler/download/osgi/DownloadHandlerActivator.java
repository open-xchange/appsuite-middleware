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

package com.openexchange.share.handler.download.osgi;

import com.openexchange.antivirus.AntiVirusService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.share.handler.download.DownloadHandler;
import com.openexchange.share.handler.download.Services;
import com.openexchange.share.handler.download.limiter.ShareDownloadLimiter;
import com.openexchange.share.servlet.handler.ShareHandler;
import com.openexchange.user.UserService;

/**
 * {@link DownloadHandlerActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DownloadHandlerActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link DownloadHandlerActivator}.
     */
    public DownloadHandlerActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { IDBasedFileAccessFactory.class, ConfigurationService.class, UserService.class, ConfigViewFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        org.slf4j.LoggerFactory.getLogger(DownloadHandlerActivator.class).info("Starting bundle: \"com.openexchange.share.handler.download\"");
        /*
         * set references
         */
        Services.set(this);
        /*
         * register handler
         */
        final DownloadHandler handler = new DownloadHandler();

        // only a local limiter for DownloadHandlers FileResponseRenderer 
        final ShareDownloadLimiter shareDownloadLimiter = new ShareDownloadLimiter(getService(ConfigViewFactory.class));
        handler.addRenderListener(shareDownloadLimiter);
        trackService(AntiVirusService.class);
        registerService(ShareHandler.class, handler, handler.getRanking());
    }

    @Override
    protected void stopBundle() throws Exception {
        org.slf4j.LoggerFactory.getLogger(DownloadHandlerActivator.class).info("Stopping bundle: \"com.openexchange.share.handler.download\"");
        Services.set(null);
        super.stopBundle();
    }

}
