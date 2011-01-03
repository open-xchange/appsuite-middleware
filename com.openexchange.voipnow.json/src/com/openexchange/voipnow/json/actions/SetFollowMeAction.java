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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.axis2.AxisFault;
import org.json.JSONArray;
import org.json.JSONException;
import com._4psa.common_xsd._2_0_4.DelObject;
import com._4psa.common_xsd._2_0_4.PositiveInteger;
import com._4psa.common_xsd._2_0_4.Rule;
import com._4psa.common_xsd._2_0_4.UpdateObject;
import com._4psa.extensiondata_xsd._2_0_4.CallRuleInfo;
import com._4psa.extensiondata_xsd._2_0_4.Match_type1;
import com._4psa.extensionmessages_xsd._2_0_4.AddCallRulesInRequest;
import com._4psa.extensionmessages_xsd._2_0_4.AddCallRulesInRequestChoice_type0;
import com._4psa.extensionmessages_xsd._2_0_4.AddCallRulesInResponse;
import com._4psa.extensionmessages_xsd._2_0_4.DelCallRulesInRequest;
import com._4psa.extensionmessages_xsd._2_0_4.DelCallRulesInRequestChoice_type0;
import com._4psa.extensionmessages_xsd._2_0_4.DelCallRulesInResponse;
import com._4psa.extensionmessages_xsd._2_0_4.GetCallRulesInRequest;
import com._4psa.extensionmessages_xsd._2_0_4.GetCallRulesInResponse;
import com._4psa.extensionmessagesinfo_xsd._2_0_4.GetCallRulesInResponseType;
import com._4psa.extensionmessagesinfo_xsd._2_0_4.GetCallRulesInResponseTypeSequence_type0;
import com._4psa.extensionmessagesinfo_xsd._2_0_4.Rules_type1;
import com._4psa.headerdata_xsd._2_0_4.UserCredentials;
import com._4psa.voipnowservice._2_0_4.ExtensionPortStub;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.voipnow.json.VoipNowExceptionCodes;

/**
 * {@link SetFollowMeAction} - The action to set followers.
 * <p>
 * Using VoipNow's SOAP API.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SetFollowMeAction extends AbstractVoipNowSOAPAction<ExtensionPortStub> {

    /**
     * The SOAP path.
     */
    private static final String SOAP_PATH = "/soap2/extension_agent.php";

    /**
     * The <tt>followme</tt> action string.
     */
    public static final String ACTION = "followme";

    /**
     * Initializes a new {@link SetFollowMeAction}.
     */
    public SetFollowMeAction() {
        super();
    }

    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws AbstractOXException {
        try {
            /*
             * Parse parameters
             */
            final String[] transferTo;
            {
                final Object data = request.getData();
                if (null == data) {
                    transferTo = new String[0];
                } else {
                    transferTo = json2StringArr((JSONArray) request.getData());
                }
            }
            final VoipNowServerSetting setting = getSOAPVoipNowServerSetting(session);
            /*
             * Get session user's main extension identifier
             */
            final String userId = String.valueOf(getMainExtensionIDOfSessionUser(session.getUser(), session.getContextId()));
            /*
             * The SOAP stub
             */
            final ExtensionPortStub stub = configureStub(setting);
            /*
             * The credentials
             */
            final UserCredentials userCredentials = getUserCredentials(setting);
            /*
             * The user ID integer
             */
            final PositiveInteger userIdInteger = new PositiveInteger();
            userIdInteger.setPositiveInteger(new org.apache.axis2.databinding.types.PositiveInteger(userId));
            /*
             * Detect existing follow-me rules
             */
            final List<Integer> followMeRulesIDs;
            final String followMeStr = "followme";
            {
                final GetCallRulesInRequest callRulesInRequest = new GetCallRulesInRequest();
                callRulesInRequest.setUserID(userIdInteger);
                final GetCallRulesInResponse getCallRulesInResponse = stub.getCallRulesIn(callRulesInRequest, userCredentials);
                final GetCallRulesInResponseType getCallRulesInResponseType = getCallRulesInResponse.getGetCallRulesInResponse();
                final GetCallRulesInResponseTypeSequence_type0 sequenceType0 =
                    getCallRulesInResponseType.getGetCallRulesInResponseTypeSequence_type0();
                final Rules_type1[] rules = sequenceType0.getRules();
                if (null != rules) {
                    followMeRulesIDs = new ArrayList<Integer>(rules.length);
                    for (final Rules_type1 rule : rules) {
                        if (followMeStr.equals(rule.getAction().getValue())) {
                            followMeRulesIDs.add(Integer.valueOf(rule.getRuleID().getPositiveInteger().intValue()));
                        }
                    }
                } else {
                    followMeRulesIDs = Collections.emptyList();
                }
            }
            /*
             * Delete existing follow-me rules
             */
            final String successStr = "success";
            if (!followMeRulesIDs.isEmpty()) {
                final int size = followMeRulesIDs.size();
                final PositiveInteger[] ids = new PositiveInteger[size];
                for (int i = 0; i < size; i++) {
                    final Integer ruleId = followMeRulesIDs.get(i);
                    final PositiveInteger ruleIdInteger = new PositiveInteger();
                    ruleIdInteger.setPositiveInteger(new org.apache.axis2.databinding.types.PositiveInteger(ruleId.toString()));
                    ids[i] = ruleIdInteger;
                }
                final DelCallRulesInRequest delRequest = new DelCallRulesInRequest();
                delRequest.setID(ids);

                final DelCallRulesInRequestChoice_type0 cType0 = new DelCallRulesInRequestChoice_type0();
                cType0.setUserID(userIdInteger);
                delRequest.setDelCallRulesInRequestChoice_type0(cType0);

                final DelCallRulesInResponse delCallRulesInResponse = stub.delCallRulesIn(delRequest, userCredentials);
                final DelObject delObject = delCallRulesInResponse.getDelCallRulesInResponse();
                final String success = delObject.getResult().getValue();
                if (!successStr.equalsIgnoreCase(success)) {
                    throw VoipNowExceptionCodes.SOAP_FAULT.create("DelCallRulesInRequest failed with: " + success);
                }
            }
            /*
             * Add new follow-me rule
             */
            final Integer id;
            final int transferToLen = transferTo.length;
            if (transferToLen > 0) {
                final AddCallRulesInRequest addCallRulesInRequest = new AddCallRulesInRequest();

                {
                    final AddCallRulesInRequestChoice_type0 type0 = new AddCallRulesInRequestChoice_type0();
                    type0.setUserID(userIdInteger);
                    addCallRulesInRequest.setAddCallRulesInRequestChoice_type0(type0);
                }

                {
                    final CallRuleInfo callRuleInfo = new CallRuleInfo();
                    /*
                     * "followme"
                     */
                    callRuleInfo.setAction(com._4psa.extensiondata_xsd._2_0_4.Action_type5.followme);
                    /*
                     * 1
                     */
                    callRuleInfo.setMatch(Match_type1.value1);
                    /*
                     * "."
                     */
                    final Rule rule = new Rule();
                    rule.setRule(".");
                    callRuleInfo.setNumber(rule);
                    /*
                     * transferTo
                     */
                    final com._4psa.common_xsd._2_0_4.String transferToString = new com._4psa.common_xsd._2_0_4.String();
                    final StringBuilder sb = new StringBuilder(transferToLen * 8);
                    sb.append(transferTo[0]);
                    for (int i = 1; i < transferToLen; i++) {
                        sb.append(' ').append(transferTo[i]);
                    }
                    transferToString.setString(sb.toString());
                    callRuleInfo.setTransferTo(transferToString);
                    final boolean requestInterval = true;
                    /*
                     * interval
                     */
                    if (requestInterval) {
                        /*
                         * Get the interval
                         */
                        final int interval;
                        {
                            final AJAXRequestResult innerResult = new GetTimeIntervalAction().perform(new AJAXRequestData(), session);
                            interval = ((java.math.BigInteger) innerResult.getResultObject()).intValue();
                        }
                        final PositiveInteger intervalInteger = new PositiveInteger();
                        intervalInteger.setPositiveInteger(new org.apache.axis2.databinding.types.PositiveInteger(String.valueOf(interval)));
                        callRuleInfo.setIntervalID(intervalInteger);
                    } else {
                        final PositiveInteger intervalInteger = new PositiveInteger();
                        intervalInteger.setPositiveInteger(new org.apache.axis2.databinding.types.PositiveInteger("1"));
                        callRuleInfo.setIntervalID(intervalInteger);
                    }
                    /*
                     * Dummy values for event type and transfer type
                     */
                    {
                        final com._4psa.extensiondata_xsd._2_0_4.CallRuleInfoSequence_type0 sequenceType0 =
                            new com._4psa.extensiondata_xsd._2_0_4.CallRuleInfoSequence_type0();

                        {
                            final com._4psa.extensiondata_xsd._2_0_4.Event_type1 eventType =
                                com._4psa.extensiondata_xsd._2_0_4.Event_type1.BUSY;
                            sequenceType0.setEvent(eventType);
                        }

                        {
                            final com._4psa.extensiondata_xsd._2_0_4.TransferType_type1 transferType =
                                com._4psa.extensiondata_xsd._2_0_4.TransferType_type1.internal;
                            sequenceType0.setTransferType(transferType);
                        }

                        callRuleInfo.setCallRuleInfoSequence_type0(sequenceType0);
                    }
                    /*
                     * Dummy values for call priority
                     */
                    {
                        final com._4psa.common_xsd._2_0_4.Integer prio = new com._4psa.common_xsd._2_0_4.Integer();
                        prio.setInteger(java.math.BigInteger.ONE);
                        callRuleInfo.setCallPriority(prio);
                    }
                    /*
                     * Dummy values for key
                     */
                    // callRuleInfo.setKey(java.math.BigDecimal.ONE);

                    addCallRulesInRequest.setRule(new CallRuleInfo[] { callRuleInfo });
                }
                /*
                 * Execute request
                 */
                final AddCallRulesInResponse addCallRulesInResponse = stub.addCallRulesIn(addCallRulesInRequest, userCredentials);
                final UpdateObject callRulesInResponse = addCallRulesInResponse.getAddCallRulesInResponse();
                final String success = callRulesInResponse.getResult().getValue();
                if (!successStr.equalsIgnoreCase(success)) {
                    throw VoipNowExceptionCodes.SOAP_FAULT.create("AddCallRulesInRequest failed with: " + success);
                }
                id = Integer.valueOf(callRulesInResponse.getID()[0].getPositiveInteger().intValue());
            } else {
                id = Integer.valueOf(-1);
            }
            /*
             * Return ID
             */
            return new AJAXRequestResult(id);
        } catch (final AxisFault e) {
            throw VoipNowExceptionCodes.SOAP_FAULT.create(e, e.getMessage());
        } catch (final RemoteException e) {
            throw VoipNowExceptionCodes.REMOTE_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw new AjaxException(AjaxException.Code.JSONError, e, e.getMessage());
        }
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
    protected ExtensionPortStub newSOAPStub() throws AxisFault {
        return new ExtensionPortStub();
    }

    private static String[] json2StringArr(final JSONArray jsonArray) throws JSONException {
        final int len = jsonArray.length();
        final String[] ret = new String[len];
        for (int i = 0; i < len; i++) {
            ret[i] = jsonArray.getString(i);
        }
        return ret;
    }

}
