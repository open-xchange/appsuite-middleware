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

package com.openexchange.ajax.session.actions;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link AutologinRequest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.6.2
 */
public class AutologinRequest extends AbstractRequest<AutologinResponse> {

    private final boolean failOnError;

    /**
     * Initializes a new {@link AutologinRequest}.
     * 
     * @param parameters
     */
    public AutologinRequest(Parameter[] parameters, boolean failOnError) {
        super(parameters);
        this.failOnError = failOnError;
    }

    public AutologinRequest(AutologinParameters parameters, boolean failOnError) {
        this(new Parameter[] { new URLParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_AUTOLOGIN), new URLParameter(LoginFields.CLIENT_PARAM, parameters.getClient())
        }, failOnError);
    }

    @Override
    public AbstractAJAXParser<? extends AutologinResponse> getParser() {
        return new AutologinResponseParser(failOnError);
    }

    public static class AutologinParameters {

        String authId, client, version;

        public AutologinParameters(String authId, String client, String version) {
            super();
            this.authId = authId;
            this.client = client;
            this.version = version;
        }

        public String getAuthId() {
            return authId;
        }

        public void setAuthId(String authId) {
            this.authId = authId;
        }

        public String getClient() {
            return client;
        }

        public void setClient(String client) {
            this.client = client;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

    }

}
