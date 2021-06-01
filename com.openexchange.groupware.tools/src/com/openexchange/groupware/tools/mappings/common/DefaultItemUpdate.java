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

package com.openexchange.groupware.tools.mappings.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import com.openexchange.groupware.tools.mappings.DefaultMapper;
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.java.EnumeratedProperty;

/**
 * {@link DefaultItemUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultItemUpdate<O, E extends Enum<E>> implements ItemUpdate<O, E> {

    protected final O originalItem;
    protected final O updatedItem;
    protected final Set<E> updatedFields;

    /**
     * Initializes a new {@link DefaultItemUpdate}.
     *
     * @param mapper A suitable mapper
     * @param originalItem The original item
     * @param updatedItem The updated item
     */
    public DefaultItemUpdate(DefaultMapper<O, E> mapper, O originalItem, O updatedItem) {
        this(mapper, originalItem, updatedItem, true, false, (E[]) null);
    }

    /**
     * Initializes a new {@link DefaultItemUpdate}.
     *
     * @param mapper A suitable mapper
     * @param originalItem The original item
     * @param updatedItem The updated item
     * @param considerUnset <code>true</code> to also consider comparison with not <i>set</i> fields of the original, <code>false</code>, otherwise
     * @param ignoreDefaults <code>true</code> to also consider default values of enumerated properties for not <i>set</i> fields of the update, <code>false</code>, otherwise
     * @param ignoredFields Fields to ignore when determining the differences
     */
    public DefaultItemUpdate(DefaultMapper<O, E> mapper, O originalItem, O updatedItem, boolean considerUnset, boolean ignoreDefaults, E[] ignoredFields) {
        this(originalItem, updatedItem, getDifferentFields(mapper, originalItem, updatedItem, considerUnset, ignoreDefaults, ignoredFields));
    }

    /**
     * Initializes a new {@link DefaultItemUpdate}.
     *
     * @param originalItem The original item
     * @param updatedItem The updated item
     * @param updatedFields The updated fields
     */
    public DefaultItemUpdate(O originalItem, O updatedItem, Set<E> updatedFields) {
        super();
        this.originalItem = originalItem;
        this.updatedItem = updatedItem;
        this.updatedFields = updatedFields;
    }

    @Override
    public O getOriginal() {
        return originalItem;
    }

    @Override
    public O getUpdate() {
        return updatedItem;
    }

    @Override
    public Set<E> getUpdatedFields() {
        return updatedFields;
    }

    @Override
    public boolean containsAnyChangeOf(E[] fields) {
        if (null != fields) {
            Set<E> updatedFields = getUpdatedFields();
            for (E field : fields) {
                if (updatedFields.contains(field)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ItemUpdate [originalItem=" + originalItem + ", updatedItem=" + updatedItem + ", updatedFields=" + updatedFields + "]";
    }

    protected static <O, E extends Enum<E>> Set<E> getDifferentFields(DefaultMapper<O, E> mapper, O original, O update, boolean considerUnset, boolean ignoreDefaults, E[] ignoredFields) {
        if (null == original) {
            if (null == update) {
                return Collections.emptySet();
            }
            return new HashSet<E>(Arrays.asList(mapper.getAssignedFields(update)));
        }
        if (null == update) {
            return new HashSet<E>(Arrays.asList(mapper.getAssignedFields(original)));
        }
        Set<E> differentFields = mapper.getDifferentFields(original, update, considerUnset, ignoredFields);
        if (ignoreDefaults && 0 < differentFields.size()) {
            for (Iterator<E> iterator = differentFields.iterator(); iterator.hasNext();) {
                Mapping<? extends Object, O> mapping = mapper.opt(iterator.next());
                if (null != mapping && matches(mapping, original, update)) {
                    iterator.remove();
                }
            }
        }
        return differentFields;
    }

    private static <O, E extends Enum<E>> boolean matches(Mapping<? extends Object, O> mapping, O original, O update) {
        if (mapping.equals(original, update)) {
            return true;
        }
        Object originalValue = mapping.get(original);
        Object updatedValue = mapping.get(update);
        if (null != originalValue && EnumeratedProperty.class.isInstance(originalValue) && ((EnumeratedProperty) originalValue).matches(updatedValue)) {
            return true;
        }
        if (null != updatedValue && EnumeratedProperty.class.isInstance(updatedValue) && ((EnumeratedProperty) updatedValue).matches(originalValue)) {
            return true;
        }
        return false;
    }

}
