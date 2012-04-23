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

import static com.openexchange.voipnow.json.actions.ActionUtility.urlEncode;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.voipnow.json.Utility;
import com.openexchange.voipnow.json.VoipNowExceptionCodes;

/**
 * {@link NewCallAction} - Maps the action to a <tt>newcall</tt> action.
 * <p>
 * A new call is initiated using VoipNow's HTTP API.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NewCallAction extends AbstractVoipNowHTTPAction<GetMethod> {

    /**
     * The <tt>call</tt> action string.
     */
    public static final String ACTION = "newcall";

    /**
     * Initializes a new {@link NewCallAction}.
     */
    public NewCallAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            /*
             * Parse parameters
             */
            final String receiverNumber = checkStringParameter(request, "phone");
            final String receiverDisplayName = checkStringParameter(request, "callerid");
            final int timeout = parseIntParameter(request, "waitforpickup", 25);
            /*
             * Get main extension
             */
            final String callerNumber = getMainExtensionNumberOfSessionUser(session.getUser(), session.getContextId());
            /*
             * Check for JSON array in body
             */
            JSONArray jsonArray = null;
            {
                final Object data = request.getData();
                if (null != data) {
                    try {
                        jsonArray = (JSONArray) data;
                    } catch (final ClassCastException e) {
                        final org.apache.commons.logging.Log log = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(NewCallAction.class));
                        log.warn(e.getMessage(), e);
                        try {
                            jsonArray = new JSONArray(data.toString());
                        } catch (final JSONException je) {
                            log.error("Request data is not a JSON array.", je);
                            // throw new OXException(OXException.Code.JSONError, je, je.getMessage());
                        }
                    }
                }
            }
            final VoipNowServerSetting setting = getVoipNowServerSetting(session, true);
            /*
             * Compose and apply query string without starting '?' character
             */
            final StringBuilder builder = new StringBuilder(256);
            builder.append("PhoneNumberToCall=").append(urlEncode(receiverNumber));
            if (null == jsonArray) {
                /*
                 * Single "FromExtension" parameter
                 */
                builder.append('&').append("FromNumber=").append(urlEncode(callerNumber));
            } else {
                final int len = jsonArray.length();
                if (len > 0) {
                    /*
                     * Multiple "FromExtension" parameters; add brackets
                     */
                    builder.append('&').append(urlEncode("FromNumber")).append('=').append(urlEncode(callerNumber));
                    for (int i = 0; i < len; i++) {
                        builder.append('&').append(urlEncode("FromNumber")).append('=').append(urlEncode(jsonArray.getString(i)));
                    }
                } else {
                    /*
                     * Single "FromExtension" parameter
                     */
                    builder.append('&').append("FromNumber=").append(urlEncode(callerNumber));
                }
            }
            builder.append('&').append("CallerID=").append(urlEncode(receiverDisplayName));
            builder.append('&').append("WaitForPickup=").append(timeout);
            builder.append('&').append("Account=").append(urlEncode(setting.getLogin()));
            builder.append('&').append("PassSHA256=").append(Utility.getSha256(setting.getPassword(), "hex"));
            builder.append('&').append("AnswerFormat=xml");
            /*
             * Perform GET request
             */
            final GetMethod getMethod = configure(setting, builder.toString());
            try {
                return parseXML(getMethod);
            } finally {
                closeResponse(getMethod);
                getMethod.releaseConnection();
            }
        } catch (final UnsupportedEncodingException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create( e, e.getMessage());
        } catch (final HttpException e) {
            throw VoipNowExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw VoipNowExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

    /**
     * The pattern to find the VoipNow response text in XML output.
     */
    private static final Pattern PATTERN_TEXT = Pattern.compile("<StatusMessage[^>]*>(.*?)</StatusMessage>", Pattern.CASE_INSENSITIVE);

    /**
     * The pattern to find the VoipNow status response code in XML output.
     */
    private static final Pattern PATTERN_STATUS = Pattern.compile("<Status[^>]*>([0-9]+)</Status>", Pattern.CASE_INSENSITIVE);

    /**
     * The pattern to find the VoipNow response code in XML output.
     */
    private static final Pattern PATTERN_CODE = Pattern.compile("<Code[^>]*>([0-9]+)</Code>", Pattern.CASE_INSENSITIVE);

    private AJAXRequestResult parseXML(final GetMethod getMethod) throws IOException, OXException {
        /*
         * Check response body
         */
        final String responseBody = getMethod.getResponseBodyAsString(1024);
        final Matcher matcher = PATTERN_STATUS.matcher(responseBody);
        int voipnowResponseCode = 0;
        if (matcher.find()) {
            voipnowResponseCode = ActionUtility.getUnsignedInteger(matcher.group(1));
            if (voipnowResponseCode != 0 && (voipnowResponseCode < 200 || voipnowResponseCode >= 400)) {
                final Matcher codeMatcher = PATTERN_CODE.matcher(responseBody);
                if (codeMatcher.find()) {
                    final Matcher m2 = PATTERN_TEXT.matcher(responseBody);
                    throw newRequestFailedException(codeMatcher.group(1), m2.find() ? m2.group(1) : null);
                }
                throw VoipNowExceptionCodes.UNPARSEABLE_HTTP_RESPONSE.create("\n" + responseBody);
            }
        }
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(Integer.valueOf(voipnowResponseCode));
    }

    @Override
    protected String getPath() {
        return "/callapi/204/Calls/MakeCall";
    }

    @Override
    protected int getTimeout() {
        return 3000;
    }

    @Override
    protected GetMethod newHttpMethod() {
        return new GetMethod();
    }

}
