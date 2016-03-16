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

package com.openexchange.webdav.xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.session.Session;
import com.openexchange.webdav.WebdavExceptionCode;
import com.openexchange.webdav.xml.fields.CalendarFields;
import com.openexchange.webdav.xml.fields.TaskFields;

/**
 * TaskParser
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class TaskParser extends CalendarParser {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TaskParser.class);

    public TaskParser(final Session sessionObj) {
        this.sessionObj = sessionObj;
    }

    public void parse(final XmlPullParser parser, final Task taskobject) throws OXException, XmlPullParserException {
        try {
            while (true) {
                if (parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals("prop")) {
                    break;
                }

                parseElementTask(taskobject, parser);
                parser.nextTag();
            }
        } catch (final XmlPullParserException exc) {
            throw exc;
        } catch (final Exception exc) {
            throw new OXException(exc);
        }
    }

    protected void parseElementTask(final Task taskobject, final XmlPullParser parser) throws Exception {
        if (!hasCorrectNamespace(parser)) {
            LOG.trace("unknown namespace in tag: {}", parser.getName());
            parser.nextText();
            return ;
        }

        if (isTag(parser, TaskFields.STATUS)) {
            try {
                taskobject.setStatus(getValueAsInt(parser));
            } catch (final NumberFormatException exc) {
                throw WebdavExceptionCode.IO_ERROR.create(TaskFields.STATUS + " is not an integer");
            }

            return ;
        } else if (isTag(parser, TaskFields.PERCENT_COMPLETED)) {
            try {
                taskobject.setPercentComplete(getValueAsInt(parser));
            } catch (final NumberFormatException exc) {
                throw WebdavExceptionCode.IO_ERROR.create(TaskFields.PERCENT_COMPLETED + " is not an integer");
            }

            return ;
        } else if (isTag(parser, TaskFields.PRIORITY)) {
            try {
                taskobject.setPriority(getValueAsInt(parser));
            } catch (final NumberFormatException exc) {
                throw WebdavExceptionCode.IO_ERROR.create(TaskFields.PRIORITY + " is not an integer");
            }

            return ;
        } else if (isTag(parser, TaskFields.TARGET_DURATION)) {
            try {
                taskobject.setTargetDuration(getValueAsLong(parser));
            } catch (final NumberFormatException exc) {
                throw WebdavExceptionCode.IO_ERROR.create(TaskFields.TARGET_DURATION + " is not a long");
            }

            return ;
        } else if (isTag(parser, TaskFields.TARGET_COSTS)) {
            try {
                taskobject.setTargetCosts(getValueAsBigDecimal(parser));
            } catch (final NumberFormatException exc) {
                throw WebdavExceptionCode.IO_ERROR.create(TaskFields.TARGET_COSTS + " is not a float");
            }

            return ;
        } else if (isTag(parser, TaskFields.ACTUAL_DURATION)) {
            try {
                taskobject.setActualDuration(getValueAsLong(parser));
            } catch (final NumberFormatException exc) {
                throw WebdavExceptionCode.IO_ERROR.create(TaskFields.ACTUAL_DURATION + " is not a long");
            }

            return ;
        } else if (isTag(parser, TaskFields.ACTUAL_COSTS)) {
            try {
                taskobject.setActualCosts(getValueAsBigDecimal(parser));
            } catch (final NumberFormatException exc) {
                throw WebdavExceptionCode.IO_ERROR.create(TaskFields.ACTUAL_COSTS + " is not a float");
            }

            return ;
        } else if (isTag(parser, TaskFields.DATE_COMPLETED)) {
            try {
                taskobject.setDateCompleted(getValueAsDate(parser));
            } catch (final NumberFormatException exc) {
                throw WebdavExceptionCode.IO_ERROR.create(TaskFields.DATE_COMPLETED + " is not a long");
            }

            return ;
        } else if (isTag(parser, CalendarFields.ALARM)) {
            taskobject.setAlarm(getValueAsDate(parser));

            return ;
        } else if (isTag(parser, CalendarFields.ALARM_FLAG)) {
            taskobject.setAlarmFlag(getValueAsBoolean(parser));

            return ;
        } else if (isTag(parser, TaskFields.BILLING_INFORMATION)) {
            taskobject.setBillingInformation(getValue(parser));

            return ;
        } else if (isTag(parser, TaskFields.CURRENCY)) {
            taskobject.setCurrency(getValue(parser));

            return ;
        } else if (isTag(parser, TaskFields.TRIP_METER)) {
            taskobject.setTripMeter(getValue(parser));

            return ;
        } else if (isTag(parser, TaskFields.COMPANIES)) {
            taskobject.setCompanies(getValue(parser));

            return ;
        } else {
            parseElementCalendar(taskobject, parser);
        }
    }
}
