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

package com.openexchange.groupware.notify;

import java.text.DateFormat;
import java.util.Locale;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.mail.MailObject;
import com.openexchange.i18n.tools.RenderMap;
import com.openexchange.i18n.tools.Template;
import com.openexchange.i18n.tools.TemplateReplacement;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link State} - Reflects the notification state for a calendar object.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco
 *         Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public interface State {

	public static enum Type {
		NEW("New"), MODIFIED("Modified"), DELETED("Deleted"), REMINDER("Reminder"), ACCEPTED("Accepted"), DECLINED(
				"Declined"), TENTATIVELY_ACCEPTED("Tentatively accepted"), NONE_ACCEPTED("Not yet accepted"), DECLINE_COUNTER("Counter declined"), REFRESH("Refresh");

		private final String str;

		private Type(final String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	/**
	 * Indicates if specified settings enable notification
	 *
	 * @param userSettingMail
	 *            The user's mail settings
	 * @param owner
	 *            The owner's ID
	 * @param participant
	 *            Current participant's ID
	 * @param modificationUser
	 *            The modifying user's ID
	 * @return <code>true</code> if specified settings enable notification;
	 *         otherwise <code>false</code>
	 */
	public boolean sendMail(UserSettingMail userSettingMail, int owner, int participant, int modificationUser);

	/**
	 * Gets the date/time formatter with default formatting styles for the given
	 * locale.
	 *
	 * @param locale
	 *            The locale
	 * @return The date/time formatter with default formatting styles for the
	 *         given locale
	 */
	public DateFormat getDateFormat(Locale locale);

	/**
	 * Adds special replacements to render map.
	 *
	 * @param obj
	 *            The calendar object
	 * @param oldObj
	 *            The obsolete calendar object
	 * @param renderMap
	 *            The render map
	 * @param p
	 *            The participant to notify
	 */
	public void addSpecial(CalendarObject obj, CalendarObject oldObj, RenderMap renderMap, EmailableParticipant p);

	/**
	 * Gets the calendar object's module.
	 *
	 * @return The calendar object's module (one of the constants defined in
	 *         {@link com.openexchange.groupware.Types})
	 */
	public int getModule();

	public void modifyInternal(MailObject mail, CalendarObject obj, ServerSession sessObj);

	public void modifyExternal(MailObject mail, CalendarObject obj, ServerSession sessObj);

	/**
	 * Gets the notification template appropriate for this state
	 *
	 * @return The notification template appropriate for this state
	 */
	public Template getTemplate();

	/**
	 * Gets the action replacement
	 *
	 * @return The action replacement
	 */
	public TemplateReplacement getAction();

	/**
	 * Gets the confirmation action replacement
	 *
	 * @return The confirmation action replacement or <code>null</code> if not
	 *         applicable to this state
	 */
	public TemplateReplacement getConfirmationAction();

	/**
	 * Gets this state's type
	 *
	 * @return This state's type
	 */
	public Type getType();

	/**
	 * Tries to find out whether anything else but irrelevant fields (for notification purposes) have changed
	 * @return true if only the alarm settings or nothing was changed, false otherwise.
	 */
    public boolean onlyIrrelevantFieldsChanged(CalendarObject oldObj, CalendarObject newObj);
}
