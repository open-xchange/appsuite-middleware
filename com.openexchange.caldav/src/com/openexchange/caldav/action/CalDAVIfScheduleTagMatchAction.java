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
            if (null != resource && resource.exists() && EventResource.class.isInstance(resource)) {
                String scheduleTag = ((EventResource) resource).getScheduleTag();
                if (false == Objects.equals(expectedScheduleTag, scheduleTag)) {
                    throw DAVProtocol.protocolException(request.getUrl(), OXException.conflict(), HttpServletResponse.SC_PRECONDITION_FAILED);
                }
            }
        }
        /*
         * continue processing
         */
        yield(request, response);
    }

}
