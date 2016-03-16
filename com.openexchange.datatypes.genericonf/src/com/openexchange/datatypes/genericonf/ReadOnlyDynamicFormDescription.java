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
