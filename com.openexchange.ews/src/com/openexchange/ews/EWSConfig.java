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

package com.openexchange.ews;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import com.microsoft.schemas.exchange.services._2006.types.ExchangeVersionType;
import com.openexchange.tools.ssl.TrustAllSSLSocketFactory;


/**
 * {@link EWSConfig}
 * 
 * Allows access to different configuration properties for the Exchange Web Service.
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class EWSConfig {
    
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
     * Initializes a new {@link EWSConfig}.
     * 
     * @param bindingProvier The underlying binding provider
     */
    public EWSConfig(BindingProvider bindingProvier) {
        super();
        this.bindingProvier = bindingProvier;
    }

    public void setEndpointAddress(String endpoint) {
        put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
    }
    
    public String getEndpointAddress() {
        return (String)get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
    }
    
    public void setUserName(String userName) {
        put(BindingProvider.USERNAME_PROPERTY, userName);
    }
    
    public String getUserName() {
        return (String)get(BindingProvider.USERNAME_PROPERTY);
    }
    
    public void setPassword(String password) {
        put(BindingProvider.PASSWORD_PROPERTY, password);
    }
    
    public String getPassword() {
        return (String)get(BindingProvider.PASSWORD_PROPERTY);
    }
    
    public void setTrustAllCerts(boolean trustAllCerts) {
        if (trustAllCerts) {
            put(SSL_SOCKET_FACTORY, TrustAllSSLSocketFactory.getDefault());
        } else {
            remove(SSL_SOCKET_FACTORY);
        }
    }

    public boolean isTrustAllCerts() {
        Object socketFactory = get(SSL_SOCKET_FACTORY);
        return null != socketFactory && TrustAllSSLSocketFactory.getDefault().equals(socketFactory);
    }

    public void setIgnoreHostnameValidation(boolean ignoreHostnameValidation) {
        if (ignoreHostnameValidation) {
            put(HOSTNAME_VERIFIER, IGNORING_HOSTNAME_VERIFIER);
        } else {
            remove(HOSTNAME_VERIFIER);
        }        
    }
    
    public boolean isIgnoreHostnameValidation() {
        return null != get(HOSTNAME_VERIFIER);  
    }    
    
    public ExchangeVersionType getExchangeVersion() {
        return this.exchangeVersion;
    }

    public void setExchangeVersion(ExchangeVersionType version) {
        this.exchangeVersion = version;
    }

    private void put(String key, Object value) {
        bindingProvier.getRequestContext().put(key, value);
    }

    private Object remove(String key) {
        return bindingProvier.getRequestContext().remove(key);
        
    }
    
    private Object get(String key) {
        return bindingProvier.getRequestContext().get(key);
    }
    
}

