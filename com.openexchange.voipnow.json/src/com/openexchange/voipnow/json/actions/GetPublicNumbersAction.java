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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.math.BigInteger;
import java.util.List;
import javax.xml.ws.Holder;
import org.json.JSONArray;
import com._4psa.channel._2_5_1.ChannelInterface;
import com._4psa.channel._2_5_1.ChannelPort;
import com._4psa.channeldata_xsd._2_5.PublicNoSelection.Assigned;
import com._4psa.channelmessages_xsd._2_5.GetPublicNoPollRequest;
import com._4psa.channelmessagesinfo_xsd._2_5.GetNoSelectionResponseType;
import com._4psa.headerdata_xsd._2_5.ServerInfo;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetPublicNumbersAction} - The action to set followers.
 * <p>
 * Using VoipNow's SOAP API.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GetPublicNumbersAction extends AbstractVoipNowSOAPAction<ChannelInterface> {

    /**
     * The SOAP path.
     */
    private static String SOAP_PATH = "/soap2/channel_agent.php";

    /**
     * The <tt>publicnums</tt> action string.
     */
    public static String ACTION = "publicnums";

    /**
     * Initializes a new {@link GetPublicNumbersAction}.
     */
    public GetPublicNumbersAction() {
        super();
    }

    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        String userId = String.valueOf(getMainExtensionIDOfSessionUser(session.getUser(), session.getContextId()));
		VoipNowServerSetting setting = getSOAPVoipNowServerSetting(session);

		GetPublicNoPollRequest getPublicNoPollRequest = new GetPublicNoPollRequest();
		getPublicNoPollRequest.setUserID(new BigInteger(userId));

		ChannelInterface port = configureStub(setting);

		GetNoSelectionResponseType response = port.getPublicNoPoll(getPublicNoPollRequest, getUserCredentials(setting), new Holder<ServerInfo>());

		JSONArray ja = new JSONArray();
		List<Assigned> assigneds = response.getPublicNo().getAssigned();
		if (null != assigneds) {
		    for (Assigned assigned : assigneds) {
		        ja.put(assigned.getExternalNo());
		    }
		}
		return new AJAXRequestResult(ja);
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
    protected ChannelInterface newSOAPStub(){
        return new ChannelPort(getWsdlLocation()).getChannelPort();
    }


}
