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

package com.openexchange.login.internal;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginJsonEnhancer;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link AbstractJsonEnhancingLoginResult}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public abstract class AbstractJsonEnhancingLoginResult extends LoginResultImpl implements LoginJsonEnhancer {

    /**
     * Initializes a new {@link AbstractJsonEnhancingLoginResult}.
     */
    protected AbstractJsonEnhancingLoginResult() {
        super();
    }

    @Override
    public void enhanceJson(JSONObject jLoginResult) throws OXException {
        try {
            doEnhanceJson(jLoginResult);
        } catch (JSONException e) {
            AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Enhances given JSON login result.
     *
     * @param jLoginResult The JSON login result
     * @throws OXException If operation fails
     * @throws JSONException If a JSON error occurs
     */
    protected abstract void doEnhanceJson(JSONObject jLoginResult) throws OXException, JSONException;

}
