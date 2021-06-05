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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link DynamicFormDescription}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DynamicFormDescription implements Iterable<FormElement> {

    private final List<FormElement> formElements;

    private final Map<String, FormElement> namedElements = new HashMap<String, FormElement>();

    public DynamicFormDescription() {
        formElements = new ArrayList<FormElement>();
    }

    @Override
    public Iterator<FormElement> iterator() {
        return formElements.iterator();
    }

    public List<FormElement> getFormElements() {
        return Collections.unmodifiableList(formElements);
    }

    public void addFormElement(FormElement formElement) {
        formElements.add(formElement);
        namedElements.put(formElement.getName(), formElement);
    }

    public void removeFormElement(FormElement formElement) {
        formElements.remove(formElement);
        namedElements.remove(formElement.getName());
    }

    public DynamicFormDescription add(FormElement formElement) {
        addFormElement(formElement);
        return this;
    }

    public List<Object> doSwitch(WidgetSwitcher switcher, Object... args) {
        List<Object> retvals = new ArrayList<Object>(formElements.size());
        for (FormElement element : formElements) {
            retvals.add(element.doSwitch(switcher, args));
        }
        return retvals;
    }

    public void iterate(DynamicFormIterator iterator, Map<String, Object> content) {
        for (FormElement element : formElements) {
            try {
                String name = element.getName();
                if (content.containsKey(name)) {
                    iterator.handle(element, content.get(name));
                }
            } catch (IterationBreak e) {
                return;
            }
        }
    }

    public Set<String> getMissingMandatoryFields(Map<String, Object> content) {
        Set<String> missing = new HashSet<String>();
        for (FormElement element : formElements) {
            if (element.isMandatory() && !content.containsKey(element.getName())) {
                missing.add(element.getName());
            }
        }
        return missing;
    }

    public FormElement getField(String col) {
        return namedElements.get(col);
    }

}
