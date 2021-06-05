/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
