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
package com.openexchange.oidc.state;

import java.util.Map;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.Nonce;

/**
 * Default implementation of the AuthenticationRequestInfo.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class DefaultAuthenticationRequestInfo implements AuthenticationRequestInfo {

    private State state;
    private String domainName;
    private String deepLink;
    private Nonce nonce;
    private Map<String, String> additionalClientInformation;

    public DefaultAuthenticationRequestInfo(State state, String domainName, String deepLink, Nonce nonce, Map<String, String> additionalClientInformation) {
        super();
        this.state = state;
        this.domainName = domainName;
        this.deepLink = deepLink;
        this.nonce = nonce;
        this.additionalClientInformation = additionalClientInformation;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public void setDeepLink(String deepLink) {
        this.deepLink = deepLink;
    }

    public void setNonce(Nonce nonce) {
        this.nonce = nonce;
    }

    public void setAdditionalClientInformation(Map<String, String> additionalClientInformation) {
        this.additionalClientInformation = additionalClientInformation;
    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public String getDomainName() {
        return this.domainName;
    }

    @Override
    public String getDeepLink() {
        return this.deepLink;
    }

    @Override
    public Nonce getNonce() {
        nonce.hashCode();
        return this.nonce;
    }

    @Override
    public Map<String, String> getAdditionalClientInformation() {
        return this.additionalClientInformation;
    }
    
}
