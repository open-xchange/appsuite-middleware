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

package com.openexchange.chronos.ical.ical4j.mapping.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ical4j.extensions.Conference;
import com.openexchange.chronos.ical.ical4j.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.parameter.XParameter;

/**
 * {@link ConferencesMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class ConferencesMapping extends AbstractICalMapping<VEvent, Event> {

    /**
     * Initializes a new {@link ConferencesMapping}.
     */
    public ConferencesMapping() {
        super();
    }

	@Override
	public void export(Event object, VEvent component, ICalParameters parameters, List<OXException> warnings) {
        List<com.openexchange.chronos.Conference> conferences = object.getConferences();
        if (null == conferences || 0 == conferences.size()) {
            removeProperties(component, Conference.PROPERTY_NAME);
		} else {
            removeProperties(component, Conference.PROPERTY_NAME);
            for (com.openexchange.chronos.Conference conference : conferences) {
                component.getProperties().add(exportConference(conference));
            }
		}
	}

	@Override
	public void importICal(VEvent component, Event object, ICalParameters parameters, List<OXException> warnings) {
        PropertyList properties = component.getProperties(Conference.PROPERTY_NAME);
        if (null != properties && 0 < properties.size()) {
            List<com.openexchange.chronos.Conference> conferences = new ArrayList<com.openexchange.chronos.Conference>(properties.size());
            for (Iterator<?> iterator = properties.iterator(); iterator.hasNext();) {
                conferences.add(importConference(((Conference) iterator.next())));
            }
            object.setConferences(conferences);
        } else if (false == isIgnoreUnsetProperties(parameters)) {
            object.setConferences(null);
		}
	}

    private Conference exportConference(com.openexchange.chronos.Conference conference) {
        Conference property = new Conference(Conference.FACTORY);
        property.getParameters().add(Value.URI);
        property.setValue(conference.getUri());
        if (Strings.isNotEmpty(conference.getLabel())) {
            property.getParameters().add(new XParameter("LABEL", conference.getLabel()));
        }
        if (null != conference.getFeatures() && 0 < conference.getFeatures().size()) {
            property.getParameters().add(new XParameter("FEATURE", Strings.join(conference.getFeatures(), ",")));
        }
        if (null != conference.getExtendedParameters() && 0 < conference.getExtendedParameters().size()) {
            for (ExtendedPropertyParameter parameter : conference.getExtendedParameters()) {
                property.getParameters().add(new XParameter(parameter.getName(), parameter.getValue()));
            }
        }
        return property;
    }

    private com.openexchange.chronos.Conference importConference(Conference property) {
        com.openexchange.chronos.Conference conference = new com.openexchange.chronos.Conference();
        conference.setUri(property.getValue());
        String label = null;
        List<String> features = new ArrayList<String>();
        List<ExtendedPropertyParameter> extendedParameters = new ArrayList<ExtendedPropertyParameter>();
        ParameterList parameterList = property.getParameters();
        if (null != parameterList && 0 < parameterList.size()) {
            for (Iterator<?> iterator = parameterList.iterator(); iterator.hasNext();) {
                Parameter parameter = (Parameter) iterator.next();
                String value = parameter.getValue();
                switch (parameter.getName()) {
                    case Parameter.VALUE:
                        // skip
                        break;
                    case "LABEL":
                        label = value;
                        break;
                    case "FEATURE":
                        String[] splitted = Strings.splitByCommaNotInQuotes(value);
                        if (null != splitted) {
                            features.addAll(Arrays.asList(splitted));
                        }
                        break;
                    default:
                        extendedParameters.add(new ExtendedPropertyParameter(parameter.getName(), parameter.getValue()));
                        break;
                }
            }
        }
        conference.setLabel(label);
        conference.setFeatures(features.isEmpty() ? null : features);
        conference.setExtendedParameters(extendedParameters.isEmpty() ? null : extendedParameters);
        return conference;
    }

}
