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

package com.openexchange.data.conversion.ical.ical4j.internal.calendar;

import java.net.URISyntaxException;
import java.util.List;
import java.util.TimeZone;
import javax.mail.internet.AddressException;
import javax.mail.internet.idn.IDNA;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.UserResolver;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.notify.NotificationConfig;
import com.openexchange.groupware.notify.NotificationConfig.NotificationProperty;
import com.openexchange.java.Strings;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.user.User;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.property.Organizer;

/**
 * Test implementation to write the organizer.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class CreatedBy<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T, U> {

    public static UserResolver userResolver = UserResolver.EMPTY;

    @Override
    public void emit(Mode mode, int index, U calendar, T component, List<ConversionWarning> warnings, Context ctx, Object... args) {
        try {
            if (0 < calendar.getOrganizerId()) {
                component.getProperties().add(getOrganizer(ctx, calendar.getOrganizerId()));
            } else if (Strings.isNotEmpty(calendar.getOrganizer())) {
                component.getProperties().add(new Organizer("mailto:" + IDNA.toACE(calendar.getOrganizer())));
            } else if (0 < calendar.getCreatedBy()) {
                component.getProperties().add(getOrganizer(ctx, calendar.getCreatedBy()));
            } else {
                warnings.add(new ConversionWarning(index, ConversionWarning.Code.UNEXPECTED_ERROR, "Unable to determine organizer."));
            }
        } catch (URISyntaxException e) {
            warnings.add(new ConversionWarning(index, ConversionWarning.Code.UNEXPECTED_ERROR, e, "URI problem."));
        } catch (OXException e) {
            warnings.add(new ConversionWarning(index, ConversionWarning.Code.UNEXPECTED_ERROR, e));
        } catch (AddressException e) {
            warnings.add(new ConversionWarning(index, ConversionWarning.Code.UNEXPECTED_ERROR, e, "Address Problem,"));
        }
    }

    @Override
    public boolean hasProperty(final T component) {
        return null != component.getProperty(Property.ORGANIZER);
    }

    @Override
    public boolean isSet(final U calendar) {
        return calendar.containsOrganizer() || calendar.containsCreatedBy();
    }

    @Override
    public void parse(final int index, final T component, final U calendar, final TimeZone timeZone, final Context ctx, final List<ConversionWarning> warnings) {
        String organizer = component.getProperty(Property.ORGANIZER).getValue();
        organizer = organizer.toLowerCase().startsWith("mailto:") ? organizer.substring(7, organizer.length()) : organizer;
        organizer = IDNA.toIDN(organizer);
        calendar.setOrganizer(organizer);
    }

    /**
     * Constructs an {@link Organizer} property for an internal user, obeying the {@link NotificationProperty#FROM_SOURCE} setting.
     *
     * @param context The context
     * @param userID The user identifier of the organizer
     * @return The organizer
     */
    private static Organizer getOrganizer(Context context, int userID) throws AddressException, URISyntaxException, OXException {
        User user = userResolver.loadUser(userID, context);
        String displayName = user.getDisplayName();
        String mail = null;
        if ("defaultSenderAddress".equalsIgnoreCase(NotificationConfig.getProperty(NotificationProperty.FROM_SOURCE, "primaryMail"))) {
            UserSettingMail userSettingMail = UserSettingMailStorage.getInstance().getUserSettingMail(userID, context);
            if (null != userSettingMail) {
                mail = userSettingMail.getSendAddr();
            }
        } else {
            mail = UserStorage.getInstance().getUser(userID, context).getMail();
        }
        if (null == mail) {
            mail = user.getDisplayName();
        }
        Organizer organizer = new Organizer("mailto:" + IDNA.toACE(mail));
        if (Strings.isNotEmpty(displayName)) {
            organizer.getParameters().add(new Cn(displayName));
        }
        return organizer;
    }

}
