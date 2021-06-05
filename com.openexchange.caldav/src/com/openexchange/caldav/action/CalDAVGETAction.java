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

package com.openexchange.caldav.action;

import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.resources.EventResource;
import com.openexchange.dav.actions.GETAction;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link CalDAVGETAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalDAVGETAction extends GETAction {

    /**
     * Initializes a new {@link CalDAVGETAction}.
     *
     * @param factory The underlying factory
     */
    public CalDAVGETAction(GroupwareCaldavFactory factory) {
        super(factory.getProtocol());
    }

    @Override
    protected void setResponseHeaders(WebdavResource resource, WebdavResponse response) throws WebdavProtocolException {
        super.setResponseHeaders(resource, response);
        if (EventResource.class.isInstance(resource)) {
            setHeaderOpt("Schedule-Tag", ((EventResource) resource).getScheduleTag(), true, response);
        }
    }

}
