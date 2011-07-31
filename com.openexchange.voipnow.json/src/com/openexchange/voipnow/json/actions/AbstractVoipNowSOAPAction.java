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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import com._4psa.headerdata_xsd._2_5.UserCredentials;

/**
 * {@link AbstractVoipNowSOAPAction} - The abstract SOAP action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractVoipNowSOAPAction<S extends Object> extends AbstractVoipNowAction {

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
	protected abstract S newSOAPStub();

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
	protected final S configureStub(final VoipNowServerSetting setting){
		final S soapStub = newSOAPStub();

		final StringBuilder url = new StringBuilder(64);
		if (setting.isSecure()) {
			int port = setting.getPort();
			if (port == -1) {
				port = 443;
			}
			url.append("https://").append(setting.getHost()).append(':').append(port).append(getSOAPPath());
		} else {
			int port = setting.getPort();
			if (port == -1) {
				port = 80;
			}
			url.append("http://").append(setting.getHost()).append(':').append(port).append(getSOAPPath());
		}
		((BindingProvider)soapStub).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url.toString());

		List<Header> commonHeaders = new ArrayList<Header>(1);

		try {
			Header header = new Header(new QName("http://4psa.com/HeaderData.xsd/2.5.1", "userCredentials"), getUserCredentials(setting), new JAXBDataBinding(UserCredentials.class));
			commonHeaders.add(header);
			((BindingProvider)soapStub).getRequestContext().put(Header.HEADER_LIST, commonHeaders);
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		//TODO: Implement those features somehow
		//        final int soapTimeout = getSOAPTimeout();
		//        final Integer timeout = Integer.valueOf(soapTimeout);
		//        options.setProperty(HTTPConstants.SO_TIMEOUT, timeout);
		//        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, timeout);
		//        options.setTimeOutInMilliSeconds(soapTimeout);
		//        options.setProperty(HTTPConstants.CHUNKED, Boolean.FALSE);
		//        options.setCallTransportCleanup(true);
		//        options.setExceptionToBeThrownOnSOAPFault(false);
		return soapStub;
	}

	protected URL getWsdlLocation() {
		return getClass().getResource("/source/wsdl/voipnowservice.wsdl");
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
		userCredentials.setPassword(setting.getPassword());
		userCredentials.setUsername(setting.getLogin());

		return userCredentials;
	}

}
