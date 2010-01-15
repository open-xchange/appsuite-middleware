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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import org.apache.axis2.AxisFault;
import org.json.JSONException;
import org.json.JSONObject;
import com._4psa.common_xsd._2_0_4.PositiveInteger;
import com._4psa.reportdata_xsd._2_0_4.IncomingCalls_type0;
import com._4psa.reportdata_xsd._2_0_4.OutgoingCalls_type0;
import com._4psa.reportmessages_xsd._2_0_4.CallReportRequest;
import com._4psa.reportmessages_xsd._2_0_4.CallReportRequestChoice_type0;
import com._4psa.reportmessages_xsd._2_0_4.Disposion_type1;
import com._4psa.reportmessages_xsd._2_0_4.Interval_type0;
import com._4psa.reportmessagesinfo_xsd._2_0_4.CallReportResponseType;
import com._4psa.voipnowservice._2_0_4.ReportPortStub;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.voipnow.json.VoipNowExceptionCodes;

/**
 * {@link CallReportAction} - Maps the action to a <tt>callreport</tt> action.
 * <p>
 * A call report is initiated using VoipNow's SOAP API.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CallReportAction extends AbstractVoipNowSOAPAction<ReportPortStub> {

    /**
     * The SOAP path.
     */
    private static final String SOAP_PATH = "/soap2/report_agent.php";

    /**
     * The <tt>callreport</tt> action string.
     */
    public static final String ACTION = "callreport";

    /**
     * Initializes a new {@link CallReportAction}.
     */
    public CallReportAction() {
        super();
    }

    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws AbstractOXException {
        try {
            /*
             * Parse parameters
             */
            final long start = checkLongParameter(request, "start");
            final long end = checkLongParameter(request, "end");
            final String disposion = "answered";
            // TODO: What about disposion??? "ANSWERED", "BUSY", "FAILED", "NO ANSWER", "UNKNOWN", or "NOT ALLOWED"
            /*
             * Get session user's main extension identifier
             */
            final String userId = String.valueOf(getMainExtensionIDOfSessionUser(session.getUser(), session.getContextId()));
            /*
             * Get setting
             */
            final VoipNowServerSetting setting = getSOAPVoipNowServerSetting(session);
            /*
             * Perform a SOAP request
             */
            final ReportPortStub stub = configureStub(setting);
            /*
             * Call report request
             */
            final CallReportRequest callReportRequest = new CallReportRequest();
            {
                /*
                 * Set choice 0: user ID, identifier OR login
                 */
                {
                    final CallReportRequestChoice_type0 type0 = new CallReportRequestChoice_type0();
                    final PositiveInteger userIdParam = new PositiveInteger();
                    userIdParam.setPositiveInteger(new org.apache.axis2.databinding.types.PositiveInteger(userId));
                    type0.setUserID(userIdParam);
                    callReportRequest.setCallReportRequestChoice_type0(type0);
                }
                /*
                 * Set interval
                 */
                {
                    final Interval_type0 interval = new Interval_type0();
                    interval.setStartDate(new java.util.Date(start));
                    interval.setEndDate(new java.util.Date(end));
                    callReportRequest.setInterval(interval);
                }
                /*
                 * Set disposion
                 */
                {
                    if ("answered".equalsIgnoreCase(disposion)) {
                        callReportRequest.setDisposion(Disposion_type1.value1);
                    } else if ("busy".equalsIgnoreCase(disposion)) {
                        callReportRequest.setDisposion(Disposion_type1.value2);
                    } else if ("failed".equalsIgnoreCase(disposion)) {
                        callReportRequest.setDisposion(Disposion_type1.value3);
                    } else if ("no answer".equalsIgnoreCase(disposion)) {
                        callReportRequest.setDisposion(Disposion_type1.value4);
                    } else if ("unknown".equalsIgnoreCase(disposion)) {
                        callReportRequest.setDisposion(Disposion_type1.value5);
                    } else if ("not allowed".equalsIgnoreCase(disposion)) {
                        callReportRequest.setDisposion(Disposion_type1.value6);
                    } else {
                        throw new AjaxException(AjaxException.Code.InvalidParameterValue, "disposion", disposion);
                    }
                }
            }
            /*
             * Get response type
             */
            final CallReportResponseType callReportResponseType =
                stub.callReport(callReportRequest, getUserCredentials(setting)).getCallReportResponse();
            /*
             * Incoming calls
             */
            final JSONObject calls = new JSONObject();
            {
                final IncomingCalls_type0 incomingCalls = callReportResponseType.getIncomingCalls();
                final JSONObject incomingCallsObject = new JSONObject();
                incomingCallsObject.put("total", incomingCalls.getTotal().getInteger().intValue());
                incomingCallsObject.put("answered", incomingCalls.getAnswered().getInteger().intValue());
                incomingCallsObject.put("busy", incomingCalls.getBusy().getInteger().intValue());
                incomingCallsObject.put("failed", incomingCalls.getFailed().getInteger().intValue());
                incomingCallsObject.put("unallowed", incomingCalls.getUnallowed().getInteger().intValue());
                incomingCallsObject.put("unanswered", incomingCalls.getUnanswered().getInteger().intValue());
                incomingCallsObject.put("unknown", incomingCalls.getUnknown().getInteger().intValue());
                /*
                 * Add to object
                 */
                calls.put("incoming", incomingCallsObject);
            }
            /*
             * Outgoing calls
             */
            {
                final OutgoingCalls_type0 outgoingCalls = callReportResponseType.getOutgoingCalls();
                final JSONObject outgoingCallsObject = new JSONObject();
                outgoingCallsObject.put("total", outgoingCalls.getTotal().getInteger().intValue());
                outgoingCallsObject.put("answered", outgoingCalls.getAnswered().getInteger().intValue());
                outgoingCallsObject.put("busy", outgoingCalls.getBusy().getInteger().intValue());
                outgoingCallsObject.put("failed", outgoingCalls.getFailed().getInteger().intValue());
                outgoingCallsObject.put("unallowed", outgoingCalls.getUnallowed().getInteger().intValue());
                outgoingCallsObject.put("unanswered", outgoingCalls.getUnanswered().getInteger().intValue());
                outgoingCallsObject.put("unknown", outgoingCalls.getUnknown().getInteger().intValue());
                /*
                 * Add to object
                 */
                calls.put("outgoing", outgoingCallsObject);
            }
            /*
             * Return
             */
            return new AJAXRequestResult(calls);
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
    protected ReportPortStub newSOAPStub() throws AxisFault {
        return new ReportPortStub();
    }

}
