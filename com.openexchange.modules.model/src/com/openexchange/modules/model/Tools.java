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

package com.openexchange.modules.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link Tools}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Tools {

    public static <T extends Model<T>> Object inject(T thing, AttributeHandler<T> handler, Object initial, List<? extends Attribute<T>> fields) {

        Object carryover = initial;
        for (Attribute<T> attribute : fields) {
            carryover = handler.handle(attribute, carryover, thing);
        }

        return carryover;
    }

    public static <T extends Model<T>> List<Object> each(T thing, AttributeHandler<T> handler, List<? extends Attribute<T>> fields) {

        List<Object> retval = new ArrayList<Object>(fields.size());

        for (Attribute<T> attribute : fields) {
            retval.add(handler.handle(attribute, thing.get(attribute)));
        }

        return retval;
    }

    public static <T extends Model<T>> List<Object> values(T thing, List<? extends Attribute<T>> fields, AttributeHandler<T> modifier) {
        List<Object> retval = new ArrayList<Object>(fields.size());

        for (Attribute<T> attribute : fields) {
            Object value = thing.get(attribute);
            Object overridden = modifier.handle(attribute, value, thing);
            retval.add((overridden != null) ? overridden : value);
        }

        return retval;
    }

    public static <T extends Model<T>> List<Object> values(T thing, List<? extends Attribute<T>> fields) {
        return values(thing, fields, AttributeHandler.DO_NOTHING);
    }

    public static <T extends Model<T>> T copy(T thing, AttributeHandler<T> modifier) {
        T copy = thing.getMetadata().create();
        for (Attribute<T> attribute : thing.getMetadata().getAllFields()) {
            Object value = thing.get(attribute);
            Object overridden = modifier.handle(attribute, value, thing);
            copy.set(attribute, (overridden != null) ? overridden : value);
        }
        return copy;
    }

    public static <T extends Model<T>> T copy(T thing) {
        return (T) copy(thing, AttributeHandler.DO_NOTHING);
    }

    public static <T extends Model<T>> List<T> copy(List<T> things, AttributeHandler<T> modifier) {
        List<T> retval = new ArrayList<T>(things.size());
        for(T thing : things) {
            retval.add(copy(thing, modifier));
        }

        return retval;
    }

    public static <T extends Model<T>> List<T> copy(List<T> things) {
        return copy(things, AttributeHandler.DO_NOTHING);
    }

    public static <T extends Model<T>> void set(Collection<T> things, Attribute<T> attribute, Object value) {
        for (T thing : things) {
            thing.set(attribute, value);
        }
    }

}
