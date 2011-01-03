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

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.Stub;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import com._4psa.headerdata_xsd._2_0_4.UserCredentials;
import com._4psa.headerdata_xsd._2_0_4.UserCredentialsSequence_type0;
import com.openexchange.voipnow.json.http.TrustAllAdapter;

/**
 * {@link AbstractVoipNowSOAPAction} - The abstract SOAP action.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractVoipNowSOAPAction<S extends Stub> extends AbstractVoipNowAction {

    /**
     * Initializes a new {@link AbstractVoipNowSOAPAction}.
     */
    protected AbstractVoipNowSOAPAction() {
        super();
    }

    /**
     * Creates a new the SOAP stub.
     * 
     * @return A new SOAP stub
     * @throws AxisFault If creating a new stub fails
     */
    protected abstract S newSOAPStub() throws AxisFault;

    /**
     * Gets the SOAP path with starting "/" character.
     * 
     * @return The SOAP path
     */
    protected abstract String getSOAPPath();

    /**
     * Gets the SOAP timeout in milliseconds.
     * 
     * @return The SOAP timeout in milliseconds
     */
    protected abstract int getSOAPTimeout();

    /**
     * Configures a new stub according to given setting.
     * 
     * @param setting The VoipNow server setting
     * @return The new configured stub
     * @throws AxisFault If returning a new stub fails
     */
    protected final S configureStub(final VoipNowServerSetting setting) throws AxisFault {
        final S soapStub = newSOAPStub();
        final Options options = soapStub._getServiceClient().getOptions();
        final EndpointReference endpointReference;
        if (setting.isSecure()) {
            int port = setting.getPort();
            if (port == -1) {
                port = 443;
            }
            final StringBuilder sb = new StringBuilder(128);
            sb.append("https://").append(setting.getHost()).append(':').append(port).append(getSOAPPath());
            /*
             * Create stub from custom URI and assign its options
             */
            endpointReference = new EndpointReference(sb.toString());
            /*
             * Custom SSL socket factory
             */
            final Protocol httpsProtocol = new Protocol(HTTPS, ((ProtocolSocketFactory) new TrustAllAdapter()), port);
            options.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, httpsProtocol);
        } else {
            int port = setting.getPort();
            if (port == -1) {
                port = 80;
            }
            final StringBuilder sb = new StringBuilder(128);
            sb.append("http://").append(setting.getHost()).append(':').append(port).append(getSOAPPath());
            /*
             * Create stub from custom URI
             */
            endpointReference = new EndpointReference(sb.toString());
        }
        /*
         * Set end-point reference
         */
        options.setTo(endpointReference);
        /*
         * Set timeout and other options
         */
        final int soapTimeout = getSOAPTimeout();
        final Integer timeout = Integer.valueOf(soapTimeout);
        options.setProperty(HTTPConstants.SO_TIMEOUT, timeout);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, timeout);
        options.setTimeOutInMilliSeconds(soapTimeout);
        options.setProperty(HTTPConstants.CHUNKED, Boolean.FALSE);
        options.setCallTransportCleanup(true);
        options.setExceptionToBeThrownOnSOAPFault(false);
        return soapStub;
    }

    /**
     * Gets the appropriate SOAP {@link UserCredentials} instance from specified {@link VoipNowServerSetting setting}.
     * 
     * @param setting The VoipNow server setting
     * @return The appropriate SOAP {@link UserCredentials} instance
     */
    protected static final UserCredentials getUserCredentials(final VoipNowServerSetting setting) {
        /*
         * Create user credentials
         */
        final UserCredentials userCredentials = new UserCredentials();
        {
            final UserCredentialsSequence_type0 sequenceType0 = new UserCredentialsSequence_type0();
            final com._4psa.common_xsd._2_0_4.Password pw = new com._4psa.common_xsd._2_0_4.Password();
            pw.setPassword(setting.getPassword());
            sequenceType0.setPassword(pw);

            final com._4psa.common_xsd._2_0_4.String login = new com._4psa.common_xsd._2_0_4.String();
            login.setString(setting.getLogin());
            sequenceType0.setUsername(login);
            userCredentials.setUserCredentialsSequence_type0(sequenceType0);
        }
        return userCredentials;
    }

}
