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



package com.openexchange.groupware.reminder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.java.Streams;

/**
 * ReminderConfigImpl
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */

public class ReminderConfigImpl extends AbstractConfigWrapper implements ReminderConfig {

	private boolean isReminderEnabled = false;

	private int reminderInterval = 3600000;

	private boolean isInit = false;

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReminderConfigImpl.class);

	public ReminderConfigImpl(final String propfile) {
		if (isInit) {
			return ;
		}

		if (propfile == null) {
			LOG.error("missing propfile");
			return ;
		}
		Properties prop = null;

		try {
			LOG.debug("try to load propfile: {}", propfile);

			prop = new Properties();
			FileInputStream fis = null;
			try {
			    fis = new FileInputStream(propfile);
			    prop.load(fis);
			} finally {
			    Streams.close(fis);
			}
		} catch (final FileNotFoundException exc) {
			LOG.error("Cannot find propfile: {}", propfile, exc);
		} catch (final IOException exc) {
			LOG.error("Cannot read propfile: {}", propfile, exc);
		}

		isReminderEnabled = parseProperty(prop, "com.openexchange.groupware.reminder.isReminderEnabled", isReminderEnabled);
		LOG.debug("Reminder property: com.openexchange.groupware.reminder.isReminderEnabled={}", isReminderEnabled);

		reminderInterval = parseProperty(prop, "com.openexchange.groupware.reminder.reminderInterval", reminderInterval);
		LOG.debug("Reminder property: com.openexchange.groupware.reminder.reminderInterval={}", reminderInterval);

		isInit = true;
	}

	@Override
    public boolean isReminderEnabled() {
		return isReminderEnabled;
	}

	@Override
    public int getReminderInterval() {
		return reminderInterval;
	}
}





