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

package com.openexchange.datatypes.genericonf;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link ReadOnlyDynamicFormDescription}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class ReadOnlyDynamicFormDescription extends DynamicFormDescription {

    private final DynamicFormDescription delegatee;

    /**
     * Initializes a new {@link ReadOnlyDynamicFormDescription}.
     *
     * @param delegatee The delegate form description
     */
    public ReadOnlyDynamicFormDescription(final DynamicFormDescription delegatee) {
        super();
        this.delegatee = delegatee;
    }

    @Override
    public DynamicFormDescription add(final FormElement formElement) {
        throw new UnsupportedOperationException("ReadOnlyDynamicFormDescription.add()");
    }

    @Override
    public void addFormElement(final FormElement formElement) {
        throw new UnsupportedOperationException("ReadOnlyDynamicFormDescription.addFormElement()");
    }

    @Override
    public List<Object> doSwitch(final WidgetSwitcher switcher, final Object... args) {
        return delegatee.doSwitch(switcher, args);
    }

    @Override
    public boolean equals(final Object obj) {
        return delegatee.equals(obj);
    }

    @Override
    public FormElement getField(final String col) {
        return delegatee.getField(col);
    }

    @Override
    public List<FormElement> getFormElements() {
        return delegatee.getFormElements();
    }

    @Override
    public Set<String> getMissingMandatoryFields(final Map<String, Object> content) {
        return delegatee.getMissingMandatoryFields(content);
    }

    @Override
    public int hashCode() {
        return delegatee.hashCode();
    }

    @Override
    public void iterate(final DynamicFormIterator iterator, final Map<String, Object> content) {
        delegatee.iterate(iterator, content);
    }

    @Override
    public Iterator<FormElement> iterator() {
        return delegatee.iterator();
    }

    @Override
    public void removeFormElement(final FormElement formElement) {
        throw new UnsupportedOperationException("ReadOnlyDynamicFormDescription.removeFormElement()");
    }

    @Override
    public String toString() {
        return delegatee.toString();
    }
}
