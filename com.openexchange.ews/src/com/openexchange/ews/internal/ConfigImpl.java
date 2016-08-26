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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.ews.internal;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import com.microsoft.schemas.exchange.services._2006.types.ExchangeVersionType;
import com.openexchange.ews.Config;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;


/**
 * {@link ConfigImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ConfigImpl implements Config {

    private static final String SSL_SOCKET_FACTORY = "com.sun.xml.internal.ws.transport.https.client.SSLSocketFactory";
    private static final String HOSTNAME_VERIFIER = "com.sun.xml.internal.ws.transport.https.client.hostname.verifier";

    private ExchangeVersionType exchangeVersion = ExchangeVersionType.EXCHANGE_2010;

    private static final HostnameVerifier IGNORING_HOSTNAME_VERIFIER = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private final BindingProvider bindingProvier;

    /**
     * Initializes a new {@link ConfigImpl}.
     *
     * @param bindingProvier The underlying binding provider
     */
    public ConfigImpl(BindingProvider bindingProvier) {
        super();
        this.bindingProvier = bindingProvier;
    }

    /* (non-Javadoc)
     * @see com.openexchange.ews.Config#setEndpointAddress(java.lang.String)
     */
    @Override
    public void setEndpointAddress(String endpoint) {
        put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
    }

    /* (non-Javadoc)
     * @see com.openexchange.ews.Config#getEndpointAddress()
     */
    @Override
    public String getEndpointAddress() {
        return (String)get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
    }

    /* (non-Javadoc)
     * @see com.openexchange.ews.Config#setUserName(java.lang.String)
     */
    @Override
    public void setUserName(String userName) {
        put(BindingProvider.USERNAME_PROPERTY, userName);
    }

    /* (non-Javadoc)
     * @see com.openexchange.ews.Config#getUserName()
     */
    @Override
    public String getUserName() {
        return (String)get(BindingProvider.USERNAME_PROPERTY);
    }

    /* (non-Javadoc)
     * @see com.openexchange.ews.Config#setPassword(java.lang.String)
     */
    @Override
    public void setPassword(String password) {
        put(BindingProvider.PASSWORD_PROPERTY, password);
    }

    /* (non-Javadoc)
     * @see com.openexchange.ews.Config#getPassword()
     */
    @Override
    public String getPassword() {
        return (String)get(BindingProvider.PASSWORD_PROPERTY);
    }

    /* (non-Javadoc)
     * @see com.openexchange.ews.Config#setTrustAllCerts(boolean)
     */
    @Override
    public void setTrustAllCerts(boolean trustAllCerts) {
        if (trustAllCerts) {
            put(SSL_SOCKET_FACTORY, SSLSocketFactoryProvider.getDefault());
        } else {
            remove(SSL_SOCKET_FACTORY);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.ews.Config#isTrustAllCerts()
     */
    @Override
    public boolean isTrustAllCerts() {
        Object socketFactory = get(SSL_SOCKET_FACTORY);
        return null != socketFactory && SSLSocketFactoryProvider.getDefault().equals(socketFactory);
    }

    /* (non-Javadoc)
     * @see com.openexchange.ews.Config#setIgnoreHostnameValidation(boolean)
     */
    @Override
    public void setIgnoreHostnameValidation(boolean ignoreHostnameValidation) {
        if (ignoreHostnameValidation) {
            put(HOSTNAME_VERIFIER, IGNORING_HOSTNAME_VERIFIER);
        } else {
            remove(HOSTNAME_VERIFIER);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.ews.Config#isIgnoreHostnameValidation()
     */
    @Override
    public boolean isIgnoreHostnameValidation() {
        return null != get(HOSTNAME_VERIFIER);
    }

    /* (non-Javadoc)
     * @see com.openexchange.ews.Config#getExchangeVersion()
     */
    @Override
    public ExchangeVersionType getExchangeVersion() {
        return this.exchangeVersion;
    }

    /* (non-Javadoc)
     * @see com.openexchange.ews.Config#setExchangeVersion(com.microsoft.schemas.exchange.services._2006.types.ExchangeVersionType)
     */
    @Override
    public void setExchangeVersion(ExchangeVersionType version) {
        this.exchangeVersion = version;
    }

    private void put(String key, Object value) {
        bindingProvier.getRequestContext().put(key, value);
    }

    private Object remove(String key) {
        if (bindingProvier.getRequestContext().containsKey(key)) {
            return bindingProvier.getRequestContext().remove(key);
        } else {
            return null;
        }
    }

    private Object get(String key) {
        return bindingProvier.getRequestContext().get(key);
    }

}

