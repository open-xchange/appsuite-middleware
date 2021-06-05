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

package com.openexchange.dav.principals.reports;

import static com.openexchange.webdav.protocol.Protocol.DAV_NS;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Element;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.actions.PROPFINDAction;
import com.openexchange.dav.mixins.PrincipalURL;
import com.openexchange.dav.principals.PrincipalFactory;
import com.openexchange.dav.principals.users.UserPrincipalResource;
import com.openexchange.exception.OXException;
import com.openexchange.user.User;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.xml.resources.ResourceMarshaller;

/**
 * {@link PrincipalMatchReport}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PrincipalMatchReport extends PROPFINDAction {

    public static final String NAMESPACE = DAV_NS.getURI();
    public static final String NAME = "principal-match";

    /**
     * Initializes a new {@link PrincipalMatchReport}.
     *
     * @param protocol The protocol
     */
    public PrincipalMatchReport(DAVProtocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        if (0 != request.getDepth(0) || false == request.getResource().isCollection()) {
            throw WebdavProtocolException.generalError(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        /*
         * always marshal properties from current user: matches both if "self" is requested, and also any requested
         * properties via the "principal-property" for the root principal collection of users.
         */
        PrincipalFactory factory = (PrincipalFactory) request.getFactory();
        User user = factory.getUser();
        WebdavPath url;
        try {
            url = new WebdavPath(PrincipalURL.forUser(user.getId(), factory.requireService(ConfigViewFactory.class)));
        } catch (OXException e) {
            throw DAVProtocol.protocolException(request.getUrl(), e);
        }
        UserPrincipalResource resource = new UserPrincipalResource(factory, user, url);
        Element multistatusElement = prepareMultistatusElement();
        ResourceMarshaller marshaller = getMarshaller(request, optRequestBody(request));
        multistatusElement.addContent(marshaller.marshal(resource));
        sendMultistatusResponse(response, multistatusElement);
    }

}
