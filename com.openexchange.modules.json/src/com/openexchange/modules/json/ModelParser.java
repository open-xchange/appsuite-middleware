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

package com.openexchange.modules.json;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.modules.model.Attribute;
import com.openexchange.modules.model.AttributeHandler;
import com.openexchange.modules.model.Metadata;
import com.openexchange.modules.model.Model;

/**
 * {@link ModelParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ModelParser<T extends Model<T>> {

    private Metadata<T> metadata;
    private AttributeHandler<T> overrides;

    public void setMetadata(Metadata<T> metadata) {
        this.metadata = metadata;
    }

    public void setOverrides(AttributeHandler<T> overrides) {
        this.overrides = overrides;
    }


    public T parse(JSONObject json, List<Attribute<T>> attributesToParse) throws JSONException {
        T thing = metadata.create();
        for (Attribute<T> attribute : attributesToParse) {
            if (!json.has(attribute.getName())) {
                continue;
            }
            Object value = json.get(attribute.getName());
            Object overidden = overrides.handle(attribute, value, thing, json);
            thing.set(attribute, (overidden != null) ? overidden : value);
        }
        return thing;
    }

    public T parse(JSONObject json) throws JSONException {
        return parse(json, metadata.getAllFields());
    }

    public List<Attribute<T>> getFields(JSONObject json, List<Attribute<T>> attributes) {
        List<Attribute<T>> retval = new ArrayList<Attribute<T>>(attributes.size());
        for (Attribute<T> attribute : attributes) {
            if (json.has(attribute.getName())) {
                retval.add(attribute);
            }
        }
        return retval;
    }

    public List<Attribute<T>> getFields(JSONObject json) {
        return getFields(json, metadata.getAllFields());
    }
}
