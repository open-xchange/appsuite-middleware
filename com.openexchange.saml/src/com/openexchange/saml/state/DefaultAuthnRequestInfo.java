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

package com.openexchange.saml.state;



/**
 * Default implementation of {@link AuthnRequestInfo}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class DefaultAuthnRequestInfo implements AuthnRequestInfo {

    private String requestId;

    private String domainName;

    private String loginPath;

    private String client;

    private String uriFragment;

    @Override
    public String getRequestId() {
        return requestId;
    }

    @Override
    public String getDomainName() {
        return domainName;
    }

    @Override
    public String getLoginPath() {
        return loginPath;
    }

    @Override
    public String getClientID() {
        return client;
    }

    @Override
    public String getUriFragment() {
        return uriFragment;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
    }

    public void setClientID(String client) {
        this.client = client;
    }

    public void setUriFragment(String uriFragment) {
        this.uriFragment = uriFragment;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((client == null) ? 0 : client.hashCode());
        result = prime * result + ((domainName == null) ? 0 : domainName.hashCode());
        result = prime * result + ((loginPath == null) ? 0 : loginPath.hashCode());
        result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
        result = prime * result + ((uriFragment == null) ? 0 : uriFragment.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultAuthnRequestInfo other = (DefaultAuthnRequestInfo) obj;
        if (client == null) {
            if (other.client != null)
                return false;
        } else if (!client.equals(other.client))
            return false;
        if (domainName == null) {
            if (other.domainName != null)
                return false;
        } else if (!domainName.equals(other.domainName))
            return false;
        if (loginPath == null) {
            if (other.loginPath != null)
                return false;
        } else if (!loginPath.equals(other.loginPath))
            return false;
        if (requestId == null) {
            if (other.requestId != null)
                return false;
        } else if (!requestId.equals(other.requestId))
            return false;
        if (uriFragment == null) {
            if (other.uriFragment != null)
                return false;
        } else if (!uriFragment.equals(other.uriFragment))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DefaultAuthnRequestInfo [requestId=" + requestId + ", domainName=" + domainName
            + ", loginPath=" + loginPath + ", client=" + client + ", uriFragment=" + uriFragment + "]";
    }

}
