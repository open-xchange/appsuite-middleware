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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.groupware.tools.mappings.DefaultMapper;

/**
 * {@link AbstractCollectionUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractCollectionUpdate<O, E extends Enum<E>> extends AbstractSimpleCollectionUpdate<O> implements CollectionUpdate<O, E> {

    /**
     * Creates an empty collection update.
     *
     * @return A new, empty collection update
     */
    public static <O, E extends Enum<E>> CollectionUpdate<O, E> emptyUpdate() {
        return new CollectionUpdate<O, E>() {

            @Override
            public List<O> getAddedItems() {
                return Collections.emptyList();
            }

            @Override
            public List<O> getRemovedItems() {
                return Collections.emptyList();
            }

            @Override
            public List<ItemUpdate<O, E>> getUpdatedItems() {
                return Collections.emptyList();
            }

            @Override
            public boolean isEmpty() {
                return true;
            }
        };
    }

    protected final List<ItemUpdate<O, E>> updatedItems;

    /**
     * Initializes a new {@link AbstractCollectionUpdate}.
     *
     * @param mapper A suitable mapper
     * @param originalItems The collection of items, or <code>null</code> if there is none
     * @param newItems The new collection of items, or <code>null</code> if there is none
     */
    public AbstractCollectionUpdate(DefaultMapper<O, E> mapper, List<O> originalItems, List<O> newItems) {
        this(mapper, originalItems, newItems, true, (E[]) null);
    }

    /**
     * Initializes a new {@link AbstractCollectionUpdate}.
     *
     * @param mapper A suitable mapper
     * @param originalItems The collection of items, or <code>null</code> if there is none
     * @param newItems The new collection of items, or <code>null</code> if there is none
     * @param considerUnset <code>true</code> to also consider comparison with not <i>set</i> fields of the original, <code>false</code>, otherwise
     * @param ignoredFields Fields to ignore when determining the differences between updated items
     */
    public AbstractCollectionUpdate(DefaultMapper<O, E> mapper, List<O> originalItems, List<O> newItems, boolean considerUnset, E[] ignoredFields) {
        super(originalItems, newItems);
        if (null != originalItems && null != newItems) {
            updatedItems = new ArrayList<ItemUpdate<O, E>>();
            for (O newItem : newItems) {
                O originalItem = find(originalItems, newItem);
                if (null != originalItem) {
                    Set<E> differentFields = mapper.getDifferentFields(originalItem, newItem, considerUnset, ignoredFields);
                    if (0 < differentFields.size()) {
                        updatedItems.add(new DefaultItemUpdate<O, E>(originalItem, newItem, differentFields));
                    }
                }
            }
        } else {
            updatedItems = Collections.emptyList();
        }
    }

    @Override
    public List<ItemUpdate<O, E>> getUpdatedItems() {
        return updatedItems;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && updatedItems.isEmpty();
    }

    @Override
    public String toString() {
        return "CollectionUpdate [" + removedItems.size() + " removed, " + addedItems.size() + " added, " + updatedItems.size() + " updated]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(updatedItems, super.hashCode());
    }

    @Override
    public boolean equals(Object other) {
        if (super.equals(other) && null != other && AbstractCollectionUpdate.class.isInstance(other)) {
            AbstractCollectionUpdate otherAbstractCollectionUpdate = (AbstractCollectionUpdate) other;
            return Objects.equals(updatedItems, otherAbstractCollectionUpdate.getUpdatedItems());
        }
        return false;
    }
}
