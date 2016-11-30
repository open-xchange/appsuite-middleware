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

package com.openexchange.dav.push.subscribe;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.mixins.AddressbookHomeSet;
import com.openexchange.dav.mixins.CalendarHomeSet;
import com.openexchange.dav.mixins.CurrentUserPrincipal;
import com.openexchange.dav.mixins.PrincipalCollectionSet;
import com.openexchange.dav.push.DAVPushUtility;
import com.openexchange.dav.resources.DAVResource;
import com.openexchange.dav.resources.DAVRootCollection;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link RootPushSubscribeCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class RootPushSubscribeCollection extends DAVRootCollection {

    /**
     * Initializes a new {@link RootPushSubscribeCollection}.
     *
     * @param factory The factory
     */
    public RootPushSubscribeCollection(PushSubscribeFactory factory) {
        super(factory, "Push Subscribe");
        includeProperties(new CurrentUserPrincipal(factory), new PrincipalCollectionSet(), new CalendarHomeSet(), new AddressbookHomeSet());
    }

    @Override
    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    public DAVResource getChild(String name) throws WebdavProtocolException {
        String clientId = DAVPushUtility.getClientId(name);
        String transportId = DAVPushUtility.getTransportId(name);
        if (null != clientId && null != transportId) {
            return new PushSubscribeResource(getFactory(), transportId, clientId, constructPathForChildResource(name));
        }
        throw DAVProtocol.protocolException(getUrl(), HttpServletResponse.SC_NOT_FOUND);
    }

}
