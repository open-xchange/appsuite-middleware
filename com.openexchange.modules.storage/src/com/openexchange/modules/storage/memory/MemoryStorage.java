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

package com.openexchange.modules.storage.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.modules.model.Attribute;
import com.openexchange.modules.model.AttributeHandler;
import com.openexchange.modules.model.Metadata;
import com.openexchange.modules.model.Model;
import com.openexchange.modules.model.Tools;


/**
 * {@link MemoryStorage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MemoryStorage<T extends Model<T>> {

    private Map<Object, T> db;


    public MemoryStorage(Map<String, Map<String, Object>> database, Metadata<T> metadata) {
        this(database, metadata, AttributeHandler.DO_NOTHING);
    }

    public MemoryStorage(Map<String, Map<String, Object>> database, Metadata<T> metadata, AttributeHandler<T> overrides) {
        super();
        db  = new HashMap<Object, T>();
        if (database == null) {
            return;
        }
        for (Map.Entry<String, Map<String, Object>> pair : database.entrySet()) {
            T thing = metadata.create();
            Map<String, Object> simpleRep = pair.getValue();
            for(Attribute<T> attribute : metadata.getPersistentFields()) {
                Object value = simpleRep.get(attribute.getName());
                Object overridden = overrides.handle(attribute, value, thing, simpleRep);
                thing.set(attribute, (overridden != null) ? overridden : value);
            }
            thing.set(metadata.getIdField(), pair.getKey());
            db.put(thing.get(metadata.getIdField()), thing);
        }

    }

    public List<T> list() {
        ArrayList<T> list = new ArrayList<T>(db.values());
        return Tools.copy(list);
    }

    public T get(Object id) {
        return Tools.copy(db.get(id));
    }

}
