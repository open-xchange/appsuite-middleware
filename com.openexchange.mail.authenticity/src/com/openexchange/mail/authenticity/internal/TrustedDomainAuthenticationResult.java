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

package com.openexchange.mail.authenticity.internal;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link TrustedDomainAuthenticationResult}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class TrustedDomainAuthenticationResult {


    private final TrustedDomain trustedDomain;
    private final String IMAGE_KEY = "image";
    private final String STATUS_KEY = "passed";

    /**
     * Initializes a new {@link TrustedDomainAuthenticationResult}.
     */
    public TrustedDomainAuthenticationResult(TrustedDomain trustedDomain) {
        super();
        this.trustedDomain = trustedDomain;
    }

    public String toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        if(trustedDomain != null){
            json.put(STATUS_KEY, true);
            json.put(IMAGE_KEY, trustedDomain.getImage());
        } else {
            json.put(STATUS_KEY, false);
        }
        return json.toString();
    }

    public boolean hasPassed(){
        return trustedDomain != null;
    }

}
