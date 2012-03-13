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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.voipnow.json.actions;

import javax.xml.ws.Holder;
import com._4psa.client._2_5_1.ClientInterface;
import com._4psa.client._2_5_1.ClientPort;
import com._4psa.clientmessages_xsd._2_5.GetClientDetailsRequest;
import com._4psa.clientmessages_xsd._2_5.GetClientDetailsResponse;
import com._4psa.headerdata_xsd._2_5.ServerInfo;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ClientDetailsAction} - Maps the action to a <tt>clientdetails</tt> action.
 * <p>
 * A client details is initiated using VoipNow's SOAP API.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ClientDetailsAction extends AbstractVoipNowSOAPAction<ClientInterface> {

    /**
     * The SOAP path.
     */
    private static String SOAP_PATH = "/soap2/client_agent.php";

    /**
     * The <tt>clientdetails</tt> action string.
     */
    public static String ACTION = "clientdetails";

    /**
     * Initializes a new {@link ClientDetailsAction}.
     */
    public ClientDetailsAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        String clientId = checkStringParameter(request, "id");

		GetClientDetailsRequest detailRequest = new GetClientDetailsRequest();

		detailRequest.setIdentifier(clientId);

		VoipNowServerSetting setting = getSOAPVoipNowServerSetting(session);
		ClientInterface port = configureStub(setting);
		GetClientDetailsResponse response = port.getClientDetails(detailRequest, getUserCredentials(setting), new Holder<ServerInfo>());


		String email = response.getEmail();

		return new AJAXRequestResult(email);
    }

    @Override
    protected String getSOAPPath() {
        return SOAP_PATH;
    }

    @Override
    protected int getSOAPTimeout() {
        return 60000;
    }

    @Override
    protected ClientInterface newSOAPStub(){
        return new ClientPort(getWsdlLocation()).getClientPort();
    }

}
