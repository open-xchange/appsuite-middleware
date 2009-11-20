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

import java.rmi.RemoteException;
import org.apache.axis2.AxisFault;
import com._4psa.common_xsd._2_0_3.PositiveInteger;
import com._4psa.common_xsd._2_0_3.UnsignedInt;
import com._4psa.common_xsd._2_0_3._boolean;
import com._4psa.headerdata_xsd._2_0_3.UserCredentials;
import com._4psa.headerdata_xsd._2_0_3.UserCredentialsSequence_type0;
import com._4psa.reportmessages_xsd._2_0_3.CallReportRequest;
import com._4psa.reportmessages_xsd._2_0_3.CallReportRequestChoice_type0;
import com._4psa.reportmessages_xsd._2_0_3.CallReportResponse;
import com._4psa.reportmessagesinfo_xsd._2_0_3.CallCostsResponseType;
import com._4psa.voipnowservice._2_0_3.ReportPortStub;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.groupware.AbstractOXException;
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
            final boolean answered = Boolean.parseBoolean(checkStringParameter("answered", request));
            final String userId = checkStringParameter("id", request);
            final String identifier = checkStringParameter("identifier", request);
            // final String flow = checkStringParameter("flow", request);
            /*
             * Get setting
             */
            final VoipNowServerSetting setting = getVoipNowServerSetting(session);
            /*
             * Perform a SOAP request
             */
            final ReportPortStub stub = configureStub(setting);
            /*
             * Call report request
             */
            final CallReportRequest callReportRequest = new CallReportRequest();
            /*
             * Set answered parameter
             */
            final _boolean answeredParam = new _boolean();
            answeredParam.set_boolean(answered);
            callReportRequest.setAnswered(answeredParam);
            /*
             * Set choice 0
             */
            final CallReportRequestChoice_type0 choiceType0 = new CallReportRequestChoice_type0();
            final PositiveInteger userIdParam = new PositiveInteger();
            userIdParam.setPositiveInteger(new org.apache.axis2.databinding.types.PositiveInteger(userId));
            choiceType0.setUserID(userIdParam);
            callReportRequest.setCallReportRequestChoice_type0(choiceType0);
            /*
             * Create user credentials
             */
            final UserCredentials userCredentials = new UserCredentials();
            {
                final UserCredentialsSequence_type0 sequenceType0 = new UserCredentialsSequence_type0();
                final com._4psa.common_xsd._2_0_3.Password pw = new com._4psa.common_xsd._2_0_3.Password();
                pw.setPassword("oxSecure");
                sequenceType0.setPassword(pw);

                final com._4psa.common_xsd._2_0_3.String login = new com._4psa.common_xsd._2_0_3.String();
                login.setString("admin");
                sequenceType0.setUsername(login);
                userCredentials.setUserCredentialsSequence_type0(sequenceType0);
            }
            /*
             * Get response
             */
            final CallReportResponse callReportResponse = stub.CallReport(callReportRequest, userCredentials);
            /*
             * Get response type
             */
            final CallCostsResponseType callCostsResponseType = callReportResponse.getCallReportResponse();
            /*
             * Some data from response type
             */
            final UnsignedInt totalCalls = callCostsResponseType.getTotalCalls();
            /*
             * Return
             */
            return new AJAXRequestResult(Integer.valueOf(totalCalls.getUnsignedInt().intValue()));
        } catch (final AxisFault e) {
            throw VoipNowExceptionCodes.SOAP_FAULT.create(e, e.getMessage());
        } catch (final RemoteException e) {
            throw VoipNowExceptionCodes.REMOTE_ERROR.create(e, e.getMessage());
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
