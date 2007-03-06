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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



package com.openexchange.tools.versit.old;

import java.io.IOException;
import java.util.ArrayList;

import com.openexchange.tools.versit.Parameter;
import com.openexchange.tools.versit.ParameterValue;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.VersitObject;

public class OldAlarmObjectDefinition extends OldObjectDefinition {

	public OldAlarmObjectDefinition(String[] propertyNames,
			OldPropertyDefinition[] properties) {
		super(propertyNames, properties);
	}

	public void parse(final OldScanner s, final VersitObject object) throws IOException {
		throw new VersitException(s, "Invalid element: VALARM");
	}

	public void write(final OldFoldingWriter fw, final VersitObject object)
			throws IOException {
		Property property = object.getProperty("ACTION");
		if (property == null) {
			throw new IOException("ACTION in VALARM not found");
		}
		final String action = property.getValue().toString();
		final String[] actions = { "AUDIO", "DISPLAY", "EMAIL", "PROCEDURE" };
		int alarm_type = -1;
		for (int i = 0; i < actions.length; i++) {
			if (actions[i].equalsIgnoreCase(action)) {
				alarm_type = i;
				break;
			}
		}
		if (alarm_type < 0) {
			throw new IOException("Unknown ACTION in VALARM: " + action);
		}
		final String[] propNames = { "AALARM", "DALARM", "MALARM", "PALARM" };
		final Property alarm = new Property(propNames[alarm_type]);
		if (alarm_type == 0) { // AUDIO
			property = object.getProperty("ATTACH");
			if (property != null) {
				final Parameter param = property.getParameter("FMTTYPE");
				String type = param.getValue(0).getText();
				if ("audio/basic".equalsIgnoreCase(type)) {
					type = "PCM";
				} else if ("audio/x-wav".equalsIgnoreCase(type)) {
					type = "WAVE";
				} else if ("audio/x-aiff".equalsIgnoreCase(type)) {
					type = "AIFF";
				} else {
					throw new IOException("Unknown audio format: " + type);
				}
				final Parameter type_param = new Parameter("TYPE");
				type_param.addValue(new ParameterValue(type));
				alarm.addParameter(type_param);
			}
		}
		final ArrayList<Object> value = new ArrayList<Object>();
		property = object.getProperty("TRIGGER");
		if (property != null) {
			value.add(property.getValue());
		}
		property = object.getProperty("DURATION");
		if (property != null) {
			value.add(property.getValue());
		}
		property = object.getProperty("REPEAT");
		if (property != null) {
			value.add(property.getValue());
		}
		final String[] propValueNames = { "ATTACH", "DESCRIPTION", "ATTENDEE",
				"ATTACH" };
		property = object.getProperty(propValueNames[alarm_type]);
		if (property != null) {
			value.add(property.getValue());
		}
		if (alarm_type == 2) {
			property = object.getProperty("DESCRIPTION");
			if (property != null) {
				value.add(property.getValue());
			}
		}
	}

}
