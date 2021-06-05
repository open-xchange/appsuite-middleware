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



package com.openexchange.groupware.reminder;

import static com.openexchange.java.Autoboxing.I;
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
		} catch (FileNotFoundException exc) {
			LOG.error("Cannot find propfile: {}", propfile, exc);
		} catch (IOException exc) {
			LOG.error("Cannot read propfile: {}", propfile, exc);
		}

		isReminderEnabled = parseProperty(prop, "com.openexchange.groupware.reminder.isReminderEnabled", isReminderEnabled);
		LOG.debug("Reminder property: com.openexchange.groupware.reminder.isReminderEnabled={}", isReminderEnabled ? Boolean.TRUE : Boolean.FALSE);

		reminderInterval = parseProperty(prop, "com.openexchange.groupware.reminder.reminderInterval", reminderInterval);
		LOG.debug("Reminder property: com.openexchange.groupware.reminder.reminderInterval={}", I(reminderInterval));

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





