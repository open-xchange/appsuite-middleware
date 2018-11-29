/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.chronos.common.mapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import com.openexchange.chronos.EnumeratedProperty;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.groupware.tools.mappings.DefaultMapper;
import com.openexchange.groupware.tools.mappings.Mapping;

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
