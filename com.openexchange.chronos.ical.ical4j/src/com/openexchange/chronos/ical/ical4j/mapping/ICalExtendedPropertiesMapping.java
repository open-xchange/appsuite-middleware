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

package com.openexchange.chronos.ical.ical4j.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.exception.OXException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.XProperty;

/**
 * {@link ICalExtendedPropertiesMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ICalExtendedPropertiesMapping<T extends Component, U> extends AbstractICalMapping<T, U> {

    private Set<String> ignoredProperties;

    /**
     * Initializes a new {@link ICalExtendedPropertiesMapping}.
     *
     * @param ignoredProperties The properties to ignore, i.e. the <i>known</i> properties covered in other mappings
     */
    protected ICalExtendedPropertiesMapping(String... ignoredProperties) {
        this(com.openexchange.tools.arrays.Collections.unmodifiableSet(ignoredProperties));
    }

    /**
     * Initializes a new {@link ICalExtendedPropertiesMapping}.
     *
     * @param ignoredProperties The properties to ignore, i.e. the <i>known</i> properties covered in other mappings
     */
    protected ICalExtendedPropertiesMapping(Set<String> ignoredProperties) {
        super();
        this.ignoredProperties = null == ignoredProperties ? Collections.<String> emptySet() : ignoredProperties;
    }

    protected abstract ExtendedProperties getValue(U object);

    protected abstract void setValue(U object, ExtendedProperties value);

    @Override
    public void export(U object, T component, ICalParameters parameters, List<OXException> warnings) {
        /*
         * remove all but ignored properties first
         */
        for (Iterator<?> iterator = component.getProperties().iterator(); iterator.hasNext();) {
            String name = ((Property) iterator.next()).getName();
            if (null != name && false == ignoredProperties.contains(name)) {
                iterator.remove();
            }
        }
        /*
         * add extended properties as specified
         */
        ExtendedProperties extendedProperties = getValue(object);
        if (null != extendedProperties && 0 < extendedProperties.size()) {
            for (ExtendedProperty property : extendedProperties) {
                component.getProperties().add(exportProperty(property));
            }
        }
    }

    @Override
    public void importICal(T component, U object, ICalParameters parameters, List<OXException> warnings) {
        /*
         * get all extended properties from component
         */
        List<ExtendedProperty> extendedProperties = new ArrayList<ExtendedProperty>();
        for (Iterator<?> iterator = component.getProperties().iterator(); iterator.hasNext();) {
            Property property = (Property) iterator.next();
            if (ignoredProperties.contains(property.getName())) {
                continue;
            }
            extendedProperties.add(importProperty(property));
        }
        /*
         * apply to object
         */
        setValue(object, extendedProperties.isEmpty() ? null : new ExtendedProperties(extendedProperties));
    }

    private static ExtendedProperty importProperty(Property property) {
        return new ExtendedProperty(property.getName(), property.getValue(), importParameters(property.getParameters()));
    }

    private static List<ExtendedPropertyParameter> importParameters(ParameterList parameterList) {
        if (null == parameterList || 0 == parameterList.size()) {
            return Collections.emptyList();
        }
        List<ExtendedPropertyParameter> parameters = new ArrayList<ExtendedPropertyParameter>(parameterList.size());
        for (Iterator<?> iterator = parameterList.iterator(); iterator.hasNext();) {
            Parameter parameter = (Parameter) iterator.next();
            parameters.add(new ExtendedPropertyParameter(parameter.getName(), parameter.getValue()));
        }
        return parameters;
    }

    private static Property exportProperty(ExtendedProperty property) {
        return new XProperty(property.getName(), exportParameters(property.getParameters()), String.valueOf(property.getValue()));
    }

    private static ParameterList exportParameters(List<ExtendedPropertyParameter> list) {
        ParameterList parameterList = new ParameterList();
        if (null != list && 0 < list.size()) {
            for (ExtendedPropertyParameter entry : list) {
                if (Parameter.VALUE.equals(entry.getName())) {
                    parameterList.add(new Value(entry.getValue()));
                } else {
                    parameterList.add(new XParameter(entry.getName(), entry.getValue()));
                }
            }
        }
        return parameterList;
    }

}
