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

package com.openexchange.groupware.notify;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
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
        if(participant == modificationUser) { return false; }

         switch(type) {
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

	private static final Set<Integer> FIELDS_TO_IGNORE = new HashSet<Integer>(Arrays.asList(
	    Task.ALARM,
	    Task.LAST_MODIFIED
	));

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
