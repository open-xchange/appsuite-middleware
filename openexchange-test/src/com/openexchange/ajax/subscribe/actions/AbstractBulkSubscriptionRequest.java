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

package com.openexchange.ajax.subscribe.actions;

import java.util.List;
import java.util.Map;
import com.openexchange.ajax.framework.Params;
import com.openexchange.java.Strings;

/**
 * A superclass for the bulk request type subscriptions that can request different kinds of fields
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractBulkSubscriptionRequest<T extends AbstractSubscriptionResponse> extends AbstractSubscriptionRequest<T> {

    protected List<String> columns;

    protected Map<String, List<String>> dynamicColumns;

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getColumns() {
        return columns;
    }

    public Parameter getColumnsAsParameter() {
        if (getColumns() == null) {
            return null;
        }
        return new Parameter("columns", Strings.join(getColumns(), ","));
    }

    public void setDynamicColumns(Map<String, List<String>> dynamicColumns) {
        this.dynamicColumns = dynamicColumns;
    }

    public Map<String, List<String>> getDynamicColumns() {
        return dynamicColumns;
    }

    public Params getDynamicColumnsAsParameter() {
        if (getDynamicColumns() == null) {
            return null;
        }

        Params params = new Params();
        for (String plugin : getDynamicColumns().keySet()) {
            params.add(plugin, Strings.join(getDynamicColumns().get(plugin), ","));
        }
        return params;
    }

}
