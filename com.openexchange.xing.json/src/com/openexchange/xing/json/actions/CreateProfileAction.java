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

package com.openexchange.xing.json.actions;

import java.util.Map;
import javax.mail.internet.AddressException;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.xing.Language;
import com.openexchange.xing.LeadDescription;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.access.XingExceptionCodes;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.exception.XingLeadAlreadyExistsException;
import com.openexchange.xing.json.XingRequest;
import com.openexchange.xing.session.AppKeyPair;
import com.openexchange.xing.session.ConsumerPair;
import com.openexchange.xing.session.WebAuthSession;


/**
 * {@link CreateProfileAction}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CreateProfileAction extends AbstractXingAction {

    /**
     * Initializes a new {@link CreateProfileAction}.
     */
    public CreateProfileAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(XingRequest req) throws OXException, JSONException, XingException {
        final String firstName = getMandatoryStringParameter(req, "first_name");
        final String lastName = getMandatoryStringParameter(req, "last_name");
        String email = getMandatoryStringParameter(req, "email");
        final String language = getMandatoryStringParameter(req, "language");
        final String tandc = getMandatoryStringParameter(req, "tandc_check");

        LeadDescription leadDescription = new LeadDescription();

        try {
            final QuotedInternetAddress addr = new QuotedInternetAddress(email, false);
            email = QuotedInternetAddress.toIDN(addr.getAddress());
            leadDescription.setEmail(email);
        } catch (AddressException e) {
            throw MimeMailException.handleMessagingException(e);
        }

        leadDescription.setTandcCheck(Boolean.parseBoolean(tandc));
        leadDescription.setFirstName(firstName);
        leadDescription.setLastName(lastName);
        leadDescription.setLanguage(Language.valueOf((language != null ? language : Language.DE.getLangId()).toUpperCase()));

        ServerSession serverSession = req.getSession();

        OAuthService oauthService = getService(OAuthService.class);
        OAuthServiceMetaData m = oauthService.getMetaDataRegistry().getService("com.openexchange.oauth.xing", serverSession.getUserId(), serverSession.getContextId());
        WebAuthSession session = new WebAuthSession(new AppKeyPair(m.getAPIKey(serverSession), m.getAPISecret(serverSession)), new ConsumerPair(m.getConsumerKey(serverSession), m.getConsumerSecret(serverSession)));

        try {
            XingAPI<WebAuthSession> xingAPI = new XingAPI<WebAuthSession>(session);
            Map<String, Object> lead = xingAPI.signUpLead(leadDescription);
            return new AJAXRequestResult(JSONCoercion.coerceToJSON(lead));
        } catch (XingLeadAlreadyExistsException e) {
            throw XingExceptionCodes.LEAD_ALREADY_EXISTS.create(e, e.getEmail());
        }
    }

}
