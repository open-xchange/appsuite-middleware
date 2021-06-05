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

import static com.openexchange.java.Autoboxing.I;
import java.text.DateFormat;
import java.util.Locale;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.mail.MailObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.i18n.tools.StringTemplate;
import com.openexchange.i18n.tools.Template;
import com.openexchange.i18n.tools.TemplateReplacement;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link TaskState} - The state for task notifications
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class TaskState extends LinkableState {

	private final TemplateReplacement actionRepl;

	private final TemplateReplacement confirmationActionRepl;

	private final String messageTemplate;

	private final Type type;

	/**
     * Initializes a new {@link TaskState}
     *
     * @param actionRepl The action replacement
     * @param messageTemplate The message template
     * @param type The notification type
     */
	public TaskState(final TemplateReplacement actionRepl, final String messageTemplate, final Type type) {
		this(actionRepl, null, messageTemplate, type);
	}

	 /**
     * Initializes a new {@link TaskState}
     *
     * @param actionRepl The action replacement
     * @param confirmationActionRepl The confirmation action replacement (optional)
     * @param messageTemplate The message template
     * @param type The notification type
     */
	public TaskState(final TemplateReplacement actionRepl, final TemplateReplacement confirmationActionRepl,
			final String messageTemplate, final Type type) {
		super();
		this.actionRepl = actionRepl;
		this.confirmationActionRepl = confirmationActionRepl;
		this.messageTemplate = messageTemplate;
		this.type = type;
	}

	@Override
    public boolean sendMail(final UserSettingMail userSettingMail, int owner, int participant, int modificationUser) {
        if (participant == modificationUser) { return false; }

         switch (type) {
            case ACCEPTED: case DECLINED: case TENTATIVELY_ACCEPTED:
                return (participant == owner) ? userSettingMail.isNotifyTasksConfirmOwner() : userSettingMail.isNotifyTasksConfirmParticipant();
            case REMINDER:
                return false;
            default:
                return userSettingMail.isNotifyTasks();
        }
	}

	@Override
    public int getModule() {
		return Types.TASK;
	}

	@Override
    public void modifyInternal(final MailObject mail, final CalendarObject obj, final ServerSession sessObj) {
		// Nothing to do
	}

	@Override
    public void modifyExternal(final MailObject mail, final CalendarObject obj, final ServerSession sessObj) {
		// Nothing to do
	}

	@Override
    public DateFormat getDateFormat(final Locale locale) {
		return DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
	}

	@Override
    public Template getTemplate() {
		return new StringTemplate(messageTemplate);
	}

	@Override
    public TemplateReplacement getAction() {
		return actionRepl;
	}

	@Override
    public TemplateReplacement getConfirmationAction() {
		return confirmationActionRepl;
	}

	@Override
    public Type getType() {
		return type;
	}

	private static final Set<Integer> FIELDS_TO_IGNORE = ImmutableSet.of(
	    I(Task.ALARM),
	    I(Task.LAST_MODIFIED)
	);

    @Override
    public boolean onlyIrrelevantFieldsChanged(CalendarObject oldObj, CalendarObject newObj) {
        if (Task.class.isInstance(oldObj) && Task.class.isInstance(newObj)) {
            Set<Integer> differingFields = ((Task)oldObj).findDifferingFields((Task)newObj);
            differingFields.removeAll(FIELDS_TO_IGNORE);
            return differingFields.isEmpty();
        }
        return false;
    }
}
