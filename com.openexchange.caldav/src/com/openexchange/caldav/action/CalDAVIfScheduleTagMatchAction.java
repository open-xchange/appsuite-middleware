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

import java.util.Objects;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.caldav.resources.EventResource;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.actions.DAVAction;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link CalDAVIfScheduleTagMatchAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalDAVIfScheduleTagMatchAction extends DAVAction {

    /**
     * Initializes a new {@link CalDAVIfScheduleTagMatchAction}.
     *
     * @param protocol The underlying protocol
     */
    public CalDAVIfScheduleTagMatchAction(Protocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        /*
         * get & check If-Schedule-Tag-Match header if supplied by client
         */
        String expectedScheduleTag = request.getHeader("If-Schedule-Tag-Match");
        if (Strings.isNotEmpty(expectedScheduleTag)) {
            WebdavResource resource = request.getResource();
            if (null == resource || false == resource.exists()) {
                throw DAVProtocol.protocolException(request.getUrl(), OXException.notFound(String.valueOf(request.getUrl())), HttpServletResponse.SC_NOT_FOUND);
            }
            expectedScheduleTag = Strings.unquote(expectedScheduleTag);
            if (false == EventResource.class.isInstance(resource) || false == Objects.equals(expectedScheduleTag, ((EventResource) resource).getScheduleTag())) {
                throw DAVProtocol.protocolException(request.getUrl(), OXException.conflict(), HttpServletResponse.SC_PRECONDITION_FAILED);
            }
        }
        /*
         * continue processing
         */
        yield(request, response);
    }

}
