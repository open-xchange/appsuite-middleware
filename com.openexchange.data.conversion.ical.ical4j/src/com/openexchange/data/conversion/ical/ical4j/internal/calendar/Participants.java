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
package com.openexchange.data.conversion.ical.ical4j.internal.calendar;

import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Resources;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.UserResolver;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ConversionError;

import java.util.List;
import java.util.TimeZone;
import java.util.LinkedList;
import java.util.Iterator;
import java.net.URI;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Participants<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T,U> {

    public static UserResolver userResolver = null;

    public boolean isSet(U cObj) {
        return cObj.containsParticipants();
    }

    public void emit(U cObj, T component, List<ConversionWarning> warnings) throws ConversionError {
        //Todo
    }

    public boolean hasProperty(T component) {
        PropertyList properties = component.getProperties("ATTENDEE");
        PropertyList resourcesList = component.getProperties("RESOURCES");
        return properties.size() > 0 || resourcesList.size() > 0;
    }

    public void parse(T component, U cObj, TimeZone timeZone, Context ctx, List<ConversionWarning> warnings) throws ConversionError {
        PropertyList properties = component.getProperties("ATTENDEE");
        List<String> mails = new LinkedList<String>();

        for(int i = 0, size = properties.size(); i < size; i++) {
            Attendee attendee = (Attendee) properties.get(i);
            URI uri = attendee.getCalAddress();
            if("mailto".equalsIgnoreCase(uri.getScheme())) {
                String mail = uri.getSchemeSpecificPart();
                mails.add( mail );
            }
        }

        List<User> users = userResolver.findUsers(mails, ctx);

        for(User user : users) {
            cObj.addParticipant( new UserParticipant(user.getId()) );
            mails.remove(user.getMail());
        }

        for(String mail : mails) {
            ExternalUserParticipant external = new ExternalUserParticipant(mail);
            external.setDisplayName(null);
            cObj.addParticipant(external);
        }

        PropertyList resourcesList = component.getProperties("RESOURCES");
        for(int i = 0, size = resourcesList.size(); i < size; i++) {
            Resources resources = (Resources) resourcesList.get(i);
            for(Iterator<Object> resObjects = resources.getResources().iterator(); resObjects.hasNext();) {
                ResourceParticipant participant = new ResourceParticipant();
                participant.setDisplayName(resObjects.next().toString());
                cObj.addParticipant(participant);
            }
        }
    }
}
