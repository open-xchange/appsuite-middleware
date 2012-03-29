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
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.ws.Holder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com._4psa.headerdata_xsd._2_5.ServerInfo;
import com._4psa.report._2_5_1.ReportInterface;
import com._4psa.report._2_5_1.ReportPort;
import com._4psa.reportdata_xsd._2_5.CallReport.Call;
import com._4psa.reportdata_xsd._2_5.CallReport.IncomingCalls;
import com._4psa.reportdata_xsd._2_5.CallReport.OutgoingCalls;
import com._4psa.reportmessages_xsd._2_5.CallReportRequest;
import com._4psa.reportmessages_xsd._2_5.CallReportRequest.Interval;
import com._4psa.reportmessages_xsd._2_5.ObjectFactory;
import com._4psa.reportmessagesinfo_xsd._2_5.CallReportResponseType;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.voipnow.json.VoipNowExceptionCodes;
/**
 * {@link CallReportAction} - Maps the action to a <tt>callreport</tt> action.
 * <p>
 * A call report is initiated using VoipNow's SOAP API.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> -
 *         design for VoipNow 2.0.3
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> -
 *         rewrite for VoipNow 2.5.1
 */
public class CallReportAction extends AbstractVoipNowSOAPAction<ReportInterface> {

	private static final String CALLS = "calls";
	private static final String INCOMING = "incoming";
	private static final String OUTGOING = "outgoing";

	/**
	 * The SOAP path.
	 */
	private static String SOAP_PATH = "/soap2/report_agent.php";

	/**
	 * The <tt>callreport</tt> action string.
	 */
	public static String ACTION = "callreport";

	/**
	 * Initializes a new {@link CallReportAction}.
	 */
	public CallReportAction() {
		super();
	}

	@Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
		try {
			/*
			 * Parse parameters
			 */
			final long start = checkLongParameter(request, "start");
			final long end = checkLongParameter(request, "end");
			final String timeZoneID = request
			.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
			final TimeZone timeZone = TimeZoneUtils
			.getTimeZone(null == timeZoneID ? session.getUser()
					.getTimeZone() : timeZoneID);

			// TODO: What about disposion??? "ANSWERED", "BUSY", "FAILED", "NO ANSWER", "UNKNOWN", or "NOT ALLOWED"
			final String disposion = "answered";

			/*
			 * Get other parameters
			 */
			final BigInteger userId = getMainExtensionIDOfSessionUser(session.getUser(), session.getContextId());
			final VoipNowServerSetting setting = getSOAPVoipNowServerSetting(session);

			/*
			 * Perform a SOAP request
			 */
			final ReportInterface port = configureStub(setting);
			final CallReportRequest callReportRequest = prepareReportRequest(start, end, timeZone, disposion, userId);
			final CallReportResponseType callResponse = port.callReport(callReportRequest, getUserCredentials(setting), new Holder<ServerInfo>());

			/*
			 * Generate reports
			 */
			final JSONObject calls = new JSONObject();
			calls.put(INCOMING, reportIncomingCalls(callResponse));
			calls.put(OUTGOING, reportOutgoingCalls(callResponse));
			calls.put(CALLS, reportCallHistory(callResponse));

			return new AJAXRequestResult(calls);
		} catch (final JSONException e) {
			throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
		} catch (final DatatypeConfigurationException e) {
			throw VoipNowExceptionCodes.SOAP_FAULT.create(e, e.getMessage());
		}
	}

	private JSONArray reportCallHistory(final CallReportResponseType callResponse) throws JSONException {
		final List<Call> history = callResponse.getCall();

		final JSONArray historyObject = new JSONArray();
		if (null != history && ! history.isEmpty()) {
			for (final Call call : history) {
				final JSONObject callObject = new JSONObject();
				callObject.put("source", call.getSource());
				callObject.put("destination", call.getDestination());
				callObject.put("startDate", call.getStartDate().toGregorianCalendar().getTime());
				callObject.put("duration", call.getDuration());
				callObject.put("answerDate", call.getAnswerDate().toGregorianCalendar().getTime());
				callObject.put("flow", call.getFlow()); // could be null
				callObject.put("type", call.getType()); // could be null
				callObject.put("disposition", call.getDisposition()); // could be null
				historyObject.put(callObject);
			}
		}
		return historyObject;
	}

	private JSONObject reportOutgoingCalls(final CallReportResponseType callResponse) throws JSONException {
		final OutgoingCalls outgoingCalls = callResponse.getOutgoingCalls();
		final JSONObject outgoingCallsObject = new JSONObject();
		outgoingCallsObject.put("total", outgoingCalls.getTotal().intValue());
		outgoingCallsObject.put("answered", outgoingCalls.getAnswered().intValue());
		outgoingCallsObject.put("busy", outgoingCalls.getBusy().intValue());
		outgoingCallsObject.put("failed", outgoingCalls.getFailed().intValue());
		outgoingCallsObject.put("unallowed", outgoingCalls.getUnallowed().intValue());
		outgoingCallsObject.put("unanswered", outgoingCalls.getUnanswered().intValue());
		outgoingCallsObject.put("unknown", outgoingCalls.getUnknown().intValue());
		return outgoingCallsObject;
	}

	private JSONObject reportIncomingCalls(final CallReportResponseType callResponse) throws JSONException {
		final IncomingCalls incomingCalls = callResponse.getIncomingCalls();
		final JSONObject incomingCallsObject = new JSONObject();
		incomingCallsObject.put("total", incomingCalls.getTotal().intValue());
		incomingCallsObject.put("answered", incomingCalls.getAnswered().intValue());
		incomingCallsObject.put("busy", incomingCalls.getBusy().intValue());
		incomingCallsObject.put("failed", incomingCalls.getFailed().intValue());
		incomingCallsObject.put("unallowed", incomingCalls.getUnallowed().intValue());
		incomingCallsObject.put("unanswered", incomingCalls.getUnanswered().intValue());
		incomingCallsObject.put("unknown", incomingCalls.getUnknown().intValue());
		return incomingCallsObject;
	}


	private CallReportRequest prepareReportRequest(final long start, final long end,
			final TimeZone timeZone, final String disposion,
			final BigInteger userId)
	throws DatatypeConfigurationException, OXException {
        final ObjectFactory factory = new ObjectFactory();

		final CallReportRequest callReportRequest = factory.createCallReportRequest();

		// Set choice 0: user ID, identifier OR login
		//callReportRequest.setUserIdentifier(userId);
		callReportRequest.setUserID(userId);

		//Set interval

		final Interval interval = factory.createCallReportRequestInterval();
		final GregorianCalendar tempCal = new GregorianCalendar(timeZone);
		tempCal.setTime(new java.util.Date(start));
		interval.setStartDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(tempCal));
		tempCal.setTime(new java.util.Date(end));
		interval.setEndDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(tempCal));
		callReportRequest.setInterval( interval );

		// Set disposion
		final List<String> allowedDispositions = Arrays.asList("answered","busy","failed","no answer","unknown","not allowed");
		if(! allowedDispositions.contains(disposion.toLowerCase())) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create( "disposion", disposion);
        }
		callReportRequest.setDisposion(disposion.toUpperCase());

		return callReportRequest;
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
	protected ReportInterface newSOAPStub(){
		return new ReportPort(getWsdlLocation()).getReportPort();
	}

}
