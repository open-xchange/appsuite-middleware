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

package com.openexchange.security.conditions;

import java.util.Dictionary;
import org.osgi.service.condpermadmin.Condition;


/**
 * {@link AbstractCondition} Abstract Condition Class.  Defaults to not postponed, and not mutable
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.3
 */
public abstract class AbstractCondition implements Condition {

    @Override
    public boolean isPostponed() {
        return false;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    /**
     * This really shouldn't be called, as only called if postponed.
     * If Postponed is overridden then should probably implement differently
     *
     */
    public boolean isSatisfied(Condition[] conditions, Dictionary<Object, Object> context) {
        for (Condition condition : conditions) {
            if (!condition.isSatisfied()) {
              return false;
            }
          }
        return true;
    }

}
