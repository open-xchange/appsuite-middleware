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

package com.openexchange.oauth.provider.authorizationserver.spi;

import java.util.List;

/**
 * POJO implementation of {@link ValidationResponse}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.1
 */
public class DefaultValidationResponse implements ValidationResponse {

    private TokenStatus tokenStatus;
    private int contextId;
    private int userId;
    private String clientName;
    private List<String> scope;

    /**
     * Initializes a new {@link DefaultValidationResponse}.
     */
    public DefaultValidationResponse() {
        super();
    }

    @Override
    public TokenStatus getTokenStatus() {
        return tokenStatus;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public List<String> getScope() {
        return scope;
    }

    @Override
    public String getClientName() {
        return clientName;
    }

    public void setTokenStatus(TokenStatus tokenStatus) {
        this.tokenStatus = tokenStatus;
    }

    public void setContextId(int contextId) {
        this.contextId = contextId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setScope(List<String> scope) {
        this.scope = scope;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clientName == null) ? 0 : clientName.hashCode());
        result = prime * result + contextId;
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
        result = prime * result + ((tokenStatus == null) ? 0 : tokenStatus.hashCode());
        result = prime * result + userId;
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
        DefaultValidationResponse other = (DefaultValidationResponse) obj;
        if (clientName == null) {
            if (other.clientName != null)
                return false;
        } else if (!clientName.equals(other.clientName))
            return false;
        if (contextId != other.contextId)
            return false;
        if (scope == null) {
            if (other.scope != null)
                return false;
        } else if (!scope.equals(other.scope))
            return false;
        if (tokenStatus != other.tokenStatus)
            return false;
        if (userId != other.userId)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DefaultValidationResponse [tokenStatus=" + tokenStatus + ", contextId=" + contextId + ", userId=" + userId + ", clientName=" + clientName + ", scope=" + scope + "]";
    }

}
