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

package com.openexchange.groupware.notify;

import java.text.DateFormat;
import java.util.Locale;

import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.mail.MailObject;
import com.openexchange.i18n.tools.StringTemplate;
import com.openexchange.i18n.tools.Template;
import com.openexchange.i18n.tools.TemplateReplacement;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.tools.session.ServerSession;

public class TaskState extends LinkableState {

	private final TemplateReplacement actionRepl;

	private final TemplateReplacement confirmationActionRepl;

	private final String messageTemplate;

	public TaskState(final TemplateReplacement actionRepl, final String messageTemplate) {
		this(actionRepl, null, messageTemplate);
	}

	public TaskState(final TemplateReplacement actionRepl, final TemplateReplacement confirmationActionRepl,
			final String messageTemplate) {
		super();
		this.actionRepl = actionRepl;
		this.confirmationActionRepl = confirmationActionRepl;
		this.messageTemplate = messageTemplate;
	}

	public boolean sendMail(final UserSettingMail userSettingMail) {
		return userSettingMail.isNotifyTasks();
	}

	public int getModule() {
		return Types.TASK;
	}

	public void modifyInternal(final MailObject mail, final CalendarObject obj, final ServerSession sessObj) {
		// Nothing to do
	}

	public void modifyExternal(final MailObject mail, final CalendarObject obj, final ServerSession sessObj) {
		// Nothing to do
	}

	public DateFormat getDateFormat(final Locale locale) {
		return DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
	}

	public Template getTemplate() {
		return new StringTemplate(messageTemplate);
	}

	public TemplateReplacement getAction() {
		return actionRepl;
	}

	public TemplateReplacement getConfirmationAction() {
		return confirmationActionRepl;
	}

}