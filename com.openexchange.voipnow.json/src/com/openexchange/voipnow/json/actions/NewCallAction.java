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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.voipnow.json.VoipNowException;
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
     * The call API request to perform.
     */
    private static final String REQUEST_NEWCALL = "request=newcall";

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

    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws AbstractOXException {
        try {
            /*
             * Parse parameters
             */
            final String receiverNumber = checkStringParameter(request, "phone");
            final String receiverDisplayName = checkStringParameter(request, "callerid");
            final int timeout = parseIntParameter(request, "timeout", 10);
            /*
             * Get main extension
             */
            final String callerNumber;
            {
                final User sessionUser = session.getUser();
                final Map<String, Set<String>> attributes = sessionUser.getAttributes();
                final String attributeName = "com.4psa.voipnow/mainExtension";
                final Set<String> set = attributes.get(attributeName);
                if (null == set || set.isEmpty()) {
                    throw VoipNowExceptionCodes.MISSING_MAIN_EXTENSION.create(
                        Integer.valueOf(session.getUserId()),
                        Integer.valueOf(session.getContextId()));
                }
                callerNumber = set.iterator().next();
            }
            final boolean xml = false;
            final VoipNowServerSetting setting = getVoipNowServerSetting(session);
            /*
             * Compose and apply query string without starting '?' character
             */
            final StringBuilder builder = new StringBuilder(256);
            builder.append(REQUEST_NEWCALL);
            builder.append('&').append("user=").append(ActionUtility.urlEncode(setting.getLogin()));
            builder.append('&').append("pass=").append(ActionUtility.urlEncode(setting.getPassword()));
            builder.append('&').append("phone=").append(receiverNumber);
            builder.append('&').append("callerid=").append(ActionUtility.urlEncode(receiverDisplayName));
            builder.append('&').append("timeout=").append(timeout);
            builder.append('&').append("from=").append(callerNumber);
            if (xml) {
                builder.append('&').append("interactive=xml");
            }
            /*
             * Perform GET request
             */
            final GetMethod getMethod = configure(setting, builder.toString());
            try {
                return xml ? parseXML(getMethod) : parseHTML(getMethod);
            } finally {
                getMethod.releaseConnection();
            }
        } catch (final UnsupportedEncodingException e) {
            throw new AjaxException(AjaxException.Code.UnexpectedError, e, e.getMessage());
        }
        // catch (final NoSuchAlgorithmException e) {
        // throw new AjaxException(AjaxException.Code.UnexpectedError, e, e.getMessage());
        // }
        catch (final HttpException e) {
            throw VoipNowExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw VoipNowExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * The pattern to find the VoipNow response text in XML output.
     */
    private static final Pattern PATTERN_TEXT = Pattern.compile("<text[^>]*>(.*?)</text>", Pattern.CASE_INSENSITIVE);

    /**
     * The pattern to find the VoipNow response code in XML output.
     */
    private static final Pattern PATTERN_CODE = Pattern.compile("<code[^>]*>([0-9]+)</code>", Pattern.CASE_INSENSITIVE);

    private AJAXRequestResult parseXML(final GetMethod getMethod) throws IOException, VoipNowException {
        /*
         * Check response body
         */
        final String responseBody = getMethod.getResponseBodyAsString(1024);
        final Matcher matcher = PATTERN_CODE.matcher(responseBody);
        int voipnowResponseCode = 0;
        if (matcher.find()) {
            voipnowResponseCode = ActionUtility.getUnsignedInteger(matcher.group(1));
            if (voipnowResponseCode > 0) {
                final Matcher m2 = PATTERN_TEXT.matcher(responseBody);
                throw newRequestFailedException(voipnowResponseCode, m2.find() ? m2.group() : null);
            }
        }
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(Integer.valueOf(voipnowResponseCode));
    }

    /**
     * The pattern to find the VoipNow response text in XML output.
     */
    private static final Pattern PATTERN_ERR = Pattern.compile("error *= *([0-9]+) *errnum *= *(\\w[\\w ]*)", Pattern.CASE_INSENSITIVE);

    private AJAXRequestResult parseHTML(final GetMethod getMethod) throws IOException, VoipNowException {
        /*
         * Check response body
         */
        final String responseCharSet = getMethod.getResponseCharSet();
        final BufferedReader r =
            new BufferedReader(new InputStreamReader(
                getMethod.getResponseBodyAsStream(),
                responseCharSet == null ? "ISO-8859-1" : responseCharSet));
        try {
            final String errStr = "error";
            int voipnowResponseCode = 0;
            String line = null;
            while ((line = r.readLine()) != null) {
                if (line.indexOf(errStr) >= 0) {
                    final Matcher matcher = PATTERN_ERR.matcher(line);
                    if (matcher.find()) {
                        voipnowResponseCode = ActionUtility.getUnsignedInteger(matcher.group(1));
                        if (voipnowResponseCode > 0) {
                            throw newRequestFailedException(voipnowResponseCode, matcher.group(2));
                        }
                    }
                }
            }
            /*
             * Return appropriate result
             */
            return new AJAXRequestResult(Integer.valueOf(voipnowResponseCode));
        } finally {
            try {
                r.close();
            } catch (final IOException e) {
                org.apache.commons.logging.LogFactory.getLog(NewCallAction.class).error(e.getMessage(), e);
            }
        }
    }

    @Override
    protected String getPath() {
        return "/callapi/callapi.php";
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
