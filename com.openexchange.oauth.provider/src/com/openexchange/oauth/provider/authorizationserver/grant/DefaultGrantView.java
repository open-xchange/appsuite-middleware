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

package com.openexchange.oauth.provider.authorizationserver.grant;

import java.util.Date;
import com.openexchange.oauth.provider.authorizationserver.client.Client;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;


/**
 * Default implementation of a {@link GrantView}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class DefaultGrantView implements GrantView {

    private Client client;

    private Scope scope;

    private Date grantDate;

    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public Date getLatestGrantDate() {
        return grantDate;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public void setLatestGrantDate(Date grantDate) {
        this.grantDate = grantDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((client == null) ? 0 : client.hashCode());
        result = prime * result + ((grantDate == null) ? 0 : grantDate.hashCode());
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultGrantView other = (DefaultGrantView) obj;
        if (client == null) {
            if (other.client != null) {
                return false;
            }
        } else if (!client.equals(other.client)) {
            return false;
        }
        if (grantDate == null) {
            if (other.grantDate != null) {
                return false;
            }
        } else if (!grantDate.equals(other.grantDate)) {
            return false;
        }
        if (scope == null) {
            if (other.scope != null) {
                return false;
            }
        } else if (!scope.equals(other.scope)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DefaultGrantView [client=" + client + ", scope=" + scope + ", grantDate=" + grantDate + "]";
    }

}
