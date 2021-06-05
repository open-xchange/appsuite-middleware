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

package com.openexchange.carddav.action;

import javax.servlet.http.HttpServletResponse;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.actions.PUTAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link CardDAVPUTAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CardDAVPUTAction extends PUTAction {

    private final GroupwareCarddavFactory factory;

    /**
     * Initializes a new {@link CardDAVPUTAction}.
     *
     * @param factory The underlying factory
     */
    public CardDAVPUTAction(GroupwareCarddavFactory factory) {
        super(factory.getProtocol());
        this.factory = factory;
    }

    @Override
    protected WebdavProtocolException getSizeExceeded(WebdavRequest request) {
        return new PreconditionException(DAVProtocol.CARD_NS.getURI(), "max-resource-size", HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
    protected long getMaxSize() {
        long maxVCardSize = factory.getState().getMaxVCardSize();
        long maxUploadSize = factory.getState().getMaxUploadSize();
        return Math.min(maxVCardSize, maxUploadSize);
    }

    @Override
    protected boolean includeResponseETag() {
        return false;
    }

}
