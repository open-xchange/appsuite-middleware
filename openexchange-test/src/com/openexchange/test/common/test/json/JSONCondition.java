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

package com.openexchange.test.common.test.json;

/**
 * {@link JSONCondition} - A Condition that can be calidated against a JSONObject. If validation fails the complaint can be retrieved.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface JSONCondition {

    /**
     * Validate a JSONObject against this condition.
     * 
     * @param o The JSONObject to validate
     * @return true if the JSONObject conforms to this condition, else false
     */
    public boolean validate(Object o);

    /**
     * Get the complaint that caused the validation to fail.
     * 
     * @return The complaint that caused the validation to fail.
     */
    public String getComplaint();
}
