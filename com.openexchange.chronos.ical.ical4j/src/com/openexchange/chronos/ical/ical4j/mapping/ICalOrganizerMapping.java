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

package com.openexchange.chronos.ical.ical4j.mapping;

import java.net.URISyntaxException;
import java.util.List;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.SentBy;

/**
 * {@link ICalOrganizerMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ICalOrganizerMapping<T extends Component, U> extends AbstractICalMapping<T, U> {

    /**
     * Initializes a new {@link ICalOrganizerMapping}.
     */
    protected ICalOrganizerMapping() {
        super();
    }

    protected abstract Organizer getValue(U object);

    protected abstract void setValue(U object, Organizer value);

    @Override
    public void export(U object, T component, ICalParameters parameters, List<OXException> warnings) {
        Organizer organizer = getValue(object);
        if (null == organizer) {
            removeProperties(component, Property.ORGANIZER);
        } else {
            net.fortuna.ical4j.model.property.Organizer property = (net.fortuna.ical4j.model.property.Organizer) component.getProperty(Property.ORGANIZER);
            if (null == property) {
                property = new net.fortuna.ical4j.model.property.Organizer();
                component.getProperties().add(property);
            }
            try {
                exportOrganizer(organizer, property);
            } catch (URISyntaxException e) {
                addConversionWarning(warnings, e, Property.ORGANIZER, e.getMessage());
            }
        }
    }

    @Override
    public void importICal(T component, U object, ICalParameters parameters, List<OXException> warnings) {
        net.fortuna.ical4j.model.property.Organizer property = (net.fortuna.ical4j.model.property.Organizer) component.getProperty(Property.ORGANIZER);
        if (null != property) {
            setValue(object, importOrganizer(property));
        } else if (false == isIgnoreUnsetProperties(parameters)) {
            setValue(object, null);
        }
    }

    private static net.fortuna.ical4j.model.property.Organizer exportOrganizer(Organizer organizer, net.fortuna.ical4j.model.property.Organizer property) throws URISyntaxException {
        property.setValue(organizer.getUri());
        if (Strings.isNotEmpty(organizer.getCn())) {
            property.getParameters().replace(new Cn(organizer.getCn()));
        } else {
            property.getParameters().removeAll(Parameter.CN);
        }
        if (null != organizer.getSentBy() && Strings.isNotEmpty(organizer.getSentBy().getUri())) {
            property.getParameters().replace(new SentBy(organizer.getSentBy().getUri()));
        } else {
            property.getParameters().removeAll(Parameter.SENT_BY);
        }
        return property;
    }

    private Organizer importOrganizer(net.fortuna.ical4j.model.property.Organizer property) {
        Organizer organizer = new Organizer();
        if (null != property.getCalAddress()) {
            organizer.setUri(property.getCalAddress().toString());
        } else if (Strings.isNotEmpty(property.getValue())) {
            if (property.getValue().startsWith("mailto:")) {
                organizer.setUri(property.getValue());
            } else {
                organizer.setUri("mailto:" + property.getValue());
            }
        }
        Parameter cnParameter = property.getParameter(Parameter.CN);
        organizer.setCn(null != cnParameter ? cnParameter.getValue() : null);

        Parameter sentByParameter = property.getParameter(Parameter.SENT_BY);
        if (null != sentByParameter && Strings.isNotEmpty(sentByParameter.getValue())) {
            CalendarUser sentByUser = new CalendarUser();
            sentByUser.setUri(sentByParameter.getValue());
            organizer.setSentBy(sentByUser);
        }
        return organizer;
    }

}
