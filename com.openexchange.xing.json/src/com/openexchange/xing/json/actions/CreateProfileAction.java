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
        } catch (final AddressException e) {
            throw MimeMailException.handleMessagingException(e);
        }

        leadDescription.setTandcCheck(Boolean.parseBoolean(tandc));
        leadDescription.setFirstName(firstName);
        leadDescription.setLastName(lastName);
        leadDescription.setLanguage(Language.valueOf((language != null ? language : Language.DE.getLangId()).toUpperCase()));

        OAuthService oauthService = getService(OAuthService.class);
        OAuthServiceMetaData m = oauthService.getMetaDataRegistry().getService("com.openexchange.oauth.xing", req.getSession().getUserId(), req.getSession().getContextId());
        WebAuthSession session = new WebAuthSession(new AppKeyPair(m.getAPIKey(req.getSession()), m.getAPISecret(req.getSession())), new ConsumerPair(m.getConsumerKey(), m.getConsumerSecret()));

        try {
            XingAPI<WebAuthSession> xingAPI = new XingAPI<WebAuthSession>(session);
            Map<String, Object> lead = xingAPI.signUpLead(leadDescription);
            return new AJAXRequestResult(JSONCoercion.coerceToJSON(lead));
        } catch (XingLeadAlreadyExistsException e) {
            throw XingExceptionCodes.LEAD_ALREADY_EXISTS.create(e, e.getEmail());
        }
    }

}
