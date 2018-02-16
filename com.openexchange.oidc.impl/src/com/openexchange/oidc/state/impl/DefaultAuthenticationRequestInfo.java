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
package com.openexchange.oidc.state.impl;

import java.util.Collections;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.oidc.state.AuthenticationRequestInfo;

/**
 * Default implementation of the AuthenticationRequestInfo.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class DefaultAuthenticationRequestInfo implements AuthenticationRequestInfo{

    private final String state;
    private final String domainName;
    private final String deepLink;
    private final String nonce;
    private final Map<String, String> additionalClientInformation;
    private final String uiClientID;

    public DefaultAuthenticationRequestInfo(String state, String domainName, String deepLink, String nonce, Map<String, String> additionalClientInformation, String uiClientID) {
        super();
        this.state = state;
        this.domainName = domainName;
        this.deepLink = deepLink;
        this.nonce = nonce;
        this.additionalClientInformation = null == additionalClientInformation ? Collections.<String, String> emptyMap() : ImmutableMap.copyOf(additionalClientInformation);
        this.uiClientID = uiClientID;
    }

    @Override
    public String getState() {
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
    public String getNonce() {
        return this.nonce;
    }

    @Override
    public Map<String, String> getAdditionalClientInformation() {
        return this.additionalClientInformation;
    }

    @Override
    public String getUiClientID() {
        return this.uiClientID;
    }

    public static class Builder {

        private final String state;
        private String domainName;
        private String deepLink;
        private String nonce;
        private Map<String, String> additionalClientInformation;
        private String uiClientID;

        public Builder(String state) {
            this.state = state;
        }

        public Builder domainName(String domainName) {
            this.domainName = domainName;
            return this;
        }

        public Builder deepLink(String deepLink) {
            this.deepLink = deepLink;
            return this;
        }

        public Builder nonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        public Builder additionalClientInformation(Map<String, String> additionalClientInformation) {
            this.additionalClientInformation = additionalClientInformation;
            return this;
        }

        public Builder uiClientID(String uiClientID) {
            this.uiClientID = uiClientID;
            return this;
        }

        public DefaultAuthenticationRequestInfo build() {
            return new DefaultAuthenticationRequestInfo(
                this.state,
                this.domainName,
                this.deepLink,
                this.nonce,
                this.additionalClientInformation,
                this.uiClientID);
        }
    }

    @Override
    public String toString() {
        return "DefaultAuthenticationRequestInfo [state=" + state + ", domainName=" + domainName + ", deepLink=" + deepLink + ", nonce=" + nonce + ", additionalClientInformation=" + additionalClientInformation + ", uiClientID=" + uiClientID + "]";
    }

}
