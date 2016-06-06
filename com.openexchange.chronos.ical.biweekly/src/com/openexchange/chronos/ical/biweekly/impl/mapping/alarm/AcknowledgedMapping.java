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

package com.openexchange.chronos.ical.biweekly.impl.mapping.alarm;

import java.util.Date;
import java.util.List;
import biweekly.ICalDataType;
import biweekly.component.VAlarm;
import biweekly.property.RawProperty;
import biweekly.util.DateTimeComponents;
import biweekly.util.ICalDate;
import biweekly.util.ICalDateFormat;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.biweekly.impl.mapping.AbstractICalMapping;
import com.openexchange.exception.OXException;

/**
 * {@link AcknowledgedMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AcknowledgedMapping extends AbstractICalMapping<VAlarm, Alarm> {

    /**
     * Initializes a new {@link AcknowledgedMapping}.
     */
    public AcknowledgedMapping() {
        super();
    }

    @Override
    public void export(Alarm object, VAlarm component, ICalParameters parameters, List<OXException> warnings) {
        Date value = object.getAcknowledged();
        if (null == value) {
            component.removeExperimentalProperties("ACKNOWLEDGED");
        } else {
            String formattedValue = ICalDateFormat.UTC_TIME_BASIC.format(value);
            RawProperty existingProperty = component.getExperimentalProperty("ACKNOWLEDGED");
            RawProperty property = component.setExperimentalProperty("ACKNOWLEDGED", ICalDataType.DATE_TIME, formattedValue);
            if (null != existingProperty) {
                property.setParameters(existingProperty.getParameters());
            }
        }
    }

    @Override
    public void importICal(VAlarm component, Alarm object, ICalParameters parameters, List<OXException> warnings) {
        RawProperty property = component.getExperimentalProperty("ACKNOWLEDGED");
        if (null == property) {
            object.setAcknowledged(null);
        } else {
            try {
                DateTimeComponents components = DateTimeComponents.parse(property.getValue());
                object.setAcknowledged(new ICalDate(components, true));
            } catch (IllegalArgumentException e) {
                addConversionWarning(warnings, e, "ACKNOWLEDGED", e.getMessage());
            }
        }
    }

}
