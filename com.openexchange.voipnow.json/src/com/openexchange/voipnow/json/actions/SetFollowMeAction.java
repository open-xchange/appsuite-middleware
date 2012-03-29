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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import org.json.JSONArray;
import org.json.JSONException;
import com._4psa.common_xsd._2_5.DelObject;
import com._4psa.common_xsd._2_5.UpdateObject;
import com._4psa.extension._2_5_1.ExtensionInterface;
import com._4psa.extension._2_5_1.ExtensionPort;
import com._4psa.extensiondata_xsd._2_5.CallRuleTransferInfo;
import com._4psa.extensionmessages_xsd._2_5.AddCallRulesInRequest;
import com._4psa.extensionmessages_xsd._2_5.AddCallRulesInRequest.Rule;
import com._4psa.extensionmessages_xsd._2_5.DelCallRulesInRequest;
import com._4psa.extensionmessages_xsd._2_5.GetCallRulesInRequest;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetCallRulesInResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetCallRulesInResponseType.Rules;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetCallRulesInResponseType.Rules.Transfer;
import com._4psa.headerdata_xsd._2_5.ServerInfo;
import com._4psa.headerdata_xsd._2_5.UserCredentials;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.voipnow.json.VoipNowExceptionCodes;

/**
 * {@link SetFollowMeAction} - The action to set followers.
 * <p>
 * Using VoipNow's SOAP API.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
/*
 * Advice from 4PSA to implement "follow-me" in 2.5.1
 * You can add a rule that implements follow me by adding a 'transfer' rule in incoming call rules for an extension.
 * To satisfy the follow me conditions you must set these parameters:
 *  - toNumbers - this is an array containing the settings for the transfer
 *  - transferNumber - number that will receive the transferred call
 *  - call = true - when this is set to true the extension that owns the rule will also be called.
 */
public class SetFollowMeAction extends AbstractVoipNowSOAPAction<ExtensionInterface> {

    /**
     * The SOAP path.
     */
    private static String SOAP_PATH = "/soap2/extension_agent.php";

    /**
     * The <tt>followme</tt> action string.
     */
    public static String ACTION = "followme";

    /**
     * Initializes a new {@link SetFollowMeAction}.
     */
    public SetFollowMeAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData ajaxRequest, ServerSession session) throws OXException {
        try {
            String[] transferTo;
            {
                Object data = ajaxRequest.getData();
                if (null == data) {
                    transferTo = new String[0];
                } else {
                    transferTo = json2StringArr((JSONArray) ajaxRequest.getData());
                }
            }
            VoipNowServerSetting setting = getSOAPVoipNowServerSetting(session);

            BigInteger userId = getMainExtensionIDOfSessionUser(session.getUser(), session.getContextId());

            ExtensionInterface port = configureStub(setting);

            ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://voip-prototyp.netline.de/" + getSOAPPath());

            UserCredentials userCredentials = getUserCredentials(setting);



            List<BigInteger> followMeRulesIDs = detectExistingFollowMeRules(userId, port, userCredentials);
            String successStr = deleteExistingFollowMeRules(userId, port, followMeRulesIDs, userCredentials);

            /*
             * Add new follow-me rule
             */
            if (transferTo.length == 0) {
                return new AJAXRequestResult(-1);
            }

            AddCallRulesInRequest addRequest = new AddCallRulesInRequest();
            addRequest.setUserID(userId);

            Rule followRule = new AddCallRulesInRequest.Rule();
            com._4psa.extensionmessages_xsd._2_5.AddCallRulesInRequest.Rule.Transfer transfer = new AddCallRulesInRequest.Rule.Transfer();
            CallRuleTransferInfo info = transfer.getToNumbers();
            followRule.setTransfer(transfer);
            addRequest.getRule().add(followRule);

            // number this rule is responsible for:
            //TODO

            // numbers to call on incoming call:
            List<String> transferNumber = info.getTransferNumber();
            transferNumber.addAll(Arrays.asList(transferTo));

            //call main number, too
            //TODO

            /*
             * Execute request
             */
            UpdateObject addResponse = port.addCallRulesIn(addRequest, getUserCredentials(setting), new Holder<ServerInfo>());
            String result = addResponse.getResult();

            if (!successStr.equalsIgnoreCase(result)) {
            	throw VoipNowExceptionCodes.SOAP_FAULT.create("AddCallRulesInRequest failed with: " + result);
            }
            return new AJAXRequestResult(addResponse.getID().get(0));
        } catch (JSONException e) {
        	throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

	private String deleteExistingFollowMeRules(BigInteger userId, ExtensionInterface port, List<BigInteger> followMeRulesIDs, UserCredentials userCredentials)
			throws OXException {
		String successStr = "success";
		if (!followMeRulesIDs.isEmpty()) {
			DelCallRulesInRequest delRequest = new DelCallRulesInRequest();
			List<BigInteger> deleteMe = delRequest.getID();
			deleteMe.addAll(followMeRulesIDs);
		    delRequest.setUserID(userId);

		    DelObject delResponse = port.delCallRulesIn(delRequest, userCredentials, new Holder<ServerInfo>());
		    String success = delResponse.getResult();
		    if (!successStr.equalsIgnoreCase(success)) {
		        throw VoipNowExceptionCodes.SOAP_FAULT.create("DelCallRulesInRequest failed with: " + success);
		    }
		}
		return successStr;
	}

    private boolean isFollowRule(Rules rule) {
		Transfer transfer = rule.getTransfer();
		if(transfer == null) {
            return false;
        }
		if(transfer.getNumber() == null) {
            return false;
        }
		if(transfer.getToNumbers() == null) {
            return false;
        }
		return true;
	}

    private List<BigInteger> detectExistingFollowMeRules(BigInteger userId, ExtensionInterface port, UserCredentials userCredentials){
    	List<BigInteger> followMeRulesIDs = Collections.emptyList();
	    GetCallRulesInRequest request = new GetCallRulesInRequest();
	    request.setUserID(userId);
	    GetCallRulesInResponseType response = port.getCallRulesIn(request, userCredentials, new Holder<ServerInfo>());

	    List<Rules> rules = response.getRules();
	    if (null != rules) {
	    	followMeRulesIDs = new ArrayList<BigInteger>(rules.size());
	    	for (Rules rule : rules) {
	    		if(isFollowRule(rule)){
	    			followMeRulesIDs.add(rule.getRuleID());
	    		}
	    	}
	    }
	    return followMeRulesIDs;
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
    protected ExtensionInterface newSOAPStub() {
        return new ExtensionPort(getWsdlLocation()).getExtensionPort();
    }

    private static String[] json2StringArr(JSONArray jsonArray) throws JSONException {
        int len = jsonArray.length();
        String[] ret = new String[len];
        for (int i = 0; i < len; i++) {
            ret[i] = jsonArray.getString(i);
        }
        return ret;
    }

}
