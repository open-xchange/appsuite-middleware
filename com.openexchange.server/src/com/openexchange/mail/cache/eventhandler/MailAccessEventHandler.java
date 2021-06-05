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

package com.openexchange.mail.cache.eventhandler;

import com.openexchange.caching.CacheElement;
import com.openexchange.caching.ElementEvent;
import com.openexchange.caching.ElementEventHandler;
import com.openexchange.mail.api.MailAccess;

/**
 * {@link MailAccessEventHandler} - The mail access event handler which preludes mail access closure if an instance of {@link MailAccess} is
 * removed from mail access cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccessEventHandler implements ElementEventHandler {

    /**
	 *
	 */
    private static final long serialVersionUID = 6568843006180170658L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailAccessEventHandler.class);

    /**
     * Default constructor
     */
    public MailAccessEventHandler() {
        super();
    }

    @Override
    public void onExceededIdletimeBackground(ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        close((MailAccess<?, ?>) cacheElem.getVal());
    }

    @Override
    public void onExceededMaxlifeBackground(ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        close((MailAccess<?, ?>) cacheElem.getVal());
    }

    @Override
    public void onSpooledDiskNotAvailable(ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        close((MailAccess<?, ?>) cacheElem.getVal());
    }

    @Override
    public void onSpooledNotAllowed(ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        close((MailAccess<?, ?>) cacheElem.getVal());
    }

    /**
     * Closes given instance of {@link MailAccess}
     */
    private void close(MailAccess<?, ?> mailAccess) {
        mailAccess.close(false);
    }

    @Override
    public void handleElementEvent(ElementEvent event) {
        LOG.error("Unknown event type: {}", event.getElementEvent());
    }

    @Override
    public void onExceededIdletimeOnRequest(ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        close((MailAccess<?, ?>) cacheElem.getVal());
    }

    @Override
    public void onExceededMaxlifeOnRequest(ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        close((MailAccess<?, ?>) cacheElem.getVal());
    }

    @Override
    public void onSpooledDiskAvailable(ElementEvent event) {
        final CacheElement cacheElem = (CacheElement) event.getSource();
        close((MailAccess<?, ?>) cacheElem.getVal());
    }
}
