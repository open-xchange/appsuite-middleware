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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.ical.biweekly.impl.mapping;

import java.util.List;
import biweekly.component.ICalComponent;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.exception.OXException;

/**
 * {@link ICalTextMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ICalTextMapping<T extends ICalComponent, U> extends AbstractICalMapping<T, U> {

    private final Class<? extends biweekly.property.TextProperty> clazz;

    /**
     * Initializes a new {@link ICalTextMapping}.
     * 
     * @param clazz The class of the mapping's text property
     */
    protected ICalTextMapping(Class<? extends biweekly.property.TextProperty> clazz) {
        super();
        this.clazz = clazz;
    }

    protected abstract String getValue(U object);

    protected abstract void setValue(U object, String value);

    protected abstract biweekly.property.TextProperty createProperty();

    @Override
    public void export(U object, T component, ICalParameters parameters, List<OXException> warnings) {
        String value = getValue(object);
        if (null == value) {
            component.removeProperties(clazz);
        } else {
            biweekly.property.TextProperty property = component.getProperty(clazz);
            if (null == property) {
                property = createProperty();
                component.setProperty(property);
            }
            property.setValue(value);
        }
    }

    @Override
    public void importICal(T component, U object, ICalParameters parameters, List<OXException> warnings) {
        biweekly.property.TextProperty property = component.getProperty(clazz);
        setValue(object, null == property ? null : property.getValue());
    }

}
