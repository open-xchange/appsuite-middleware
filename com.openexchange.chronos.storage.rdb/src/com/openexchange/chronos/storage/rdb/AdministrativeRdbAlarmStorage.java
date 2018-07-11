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

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.google.common.io.BaseEncoding;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.AdministrativeAlarmStorage;
import com.openexchange.chronos.storage.rdb.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * {@link AdministrativeRdbAlarmStorage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class AdministrativeRdbAlarmStorage implements AdministrativeAlarmStorage {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AdministrativeRdbAlarmStorage.class);
    private static final AlarmMapper MAPPER = AlarmMapper.getInstance();

      /**
     * Logs & executes a prepared statement's SQL query.
     *
     * @param stmt The statement to execute the SQL query from
     * @return The result set
     */
    protected static ResultSet logExecuteQuery(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeQuery();
        } else {
            String statementString = String.valueOf(stmt);
            long start = System.currentTimeMillis();
            ResultSet resultSet = stmt.executeQuery();
            LOG.debug("executeQuery: {} - {} ms elapsed.", statementString, L(System.currentTimeMillis() - start));
            return resultSet;
        }
    }

    /**
     * Logs & executes a prepared statement's SQL update.
     *
     * @param stmt The statement to execute the SQL update from
     * @return The number of affected rows
     */
    protected static int logExecuteUpdate(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeUpdate();
        } else {
            String statementString = String.valueOf(stmt);
            long start = System.currentTimeMillis();
            int rowCount = stmt.executeUpdate();
            LOG.debug("executeUpdate: {} - {} rows affected, {} ms elapsed.", statementString, I(rowCount), L(System.currentTimeMillis() - start));
            return rowCount;
        }
    }

    @Override
    public Alarm getAlarm(Connection con, int cid, int accountId, int alarmId) throws OXException {
        AlarmField[] mappedFields = MAPPER.getMappedFields(MAPPER.getMappedFields());
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT event").append(MAPPER.getColumns(mappedFields))
            .append(" FROM calendar_alarm WHERE cid=? AND account=? AND alarmId=?")
        ;
        stringBuilder.append(';');
        try (PreparedStatement stmt = con.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, accountId);
            stmt.setInt(parameterIndex++, alarmId);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                String eventId = resultSet.getString(1);
                return readAlarm(cid, eventId, resultSet, mappedFields);
            }
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e.getMessage());
        }
    }

    private Alarm readAlarm(int cid, String eventId, ResultSet resultSet, AlarmField[] fields) throws SQLException, OXException {
        return adjustAfterLoad(cid, eventId, MAPPER.fromResultSet(resultSet, fields));
    }

    /**
     * Adjusts certain properties of an alarm after loading it from the database.
     *
     * @param eventId The identifier of the associated event
     * @param alarm The alarm to adjust
     * @return The (possibly adjusted) alarm reference
     */
    private Alarm adjustAfterLoad(int cid, String eventId, Alarm alarm) {
        ExtendedProperties extendedProperties = alarm.getExtendedProperties();
        if (null == extendedProperties) {
            return alarm;
        }
        /*
         * move specific properties from container into alarm object
         */
        ExtendedProperty summaryProperty = extendedProperties.get("SUMMARY");
        if (null != summaryProperty) {
            alarm.setSummary((String) summaryProperty.getValue());
            extendedProperties.remove(summaryProperty);
        }
        ExtendedProperty descriptionProperty = extendedProperties.get("DESCRIPTION");
        if (null != descriptionProperty) {
            alarm.setDescription((String) descriptionProperty.getValue());
            extendedProperties.remove(descriptionProperty);
        }
        List<ExtendedProperty> attendeeProperties = extendedProperties.getAll("ATTENDEE");
        if (null != attendeeProperties && 0 < attendeeProperties.size()) {
            alarm.setAttendees(decodeAttendees(cid, eventId, attendeeProperties));
            extendedProperties.removeAll(attendeeProperties);
        }
        List<ExtendedProperty> attachmentProperties = extendedProperties.getAll("ATTACH");
        if (null != attachmentProperties && 0 < attachmentProperties.size()) {
            alarm.setAttachments(decodeAttachments(eventId, attachmentProperties));
            extendedProperties.removeAll(attachmentProperties);
        }

        if (extendedProperties.size() == 0) {
            alarm.removeExtendedProperties();
        }
        return alarm;
    }

    /**
     * Decodes a list of extended properties into a valid list of attendees.
     *
     * @param eventId The identifier of the associated event
     * @param attendeeProperties The extended attendee properties to decode
     * @return The decoded attendees, or an empty list if there are none
     */
    private List<Attendee> decodeAttendees(int cid, String eventId, List<ExtendedProperty> attendeeProperties) {
        List<Attendee> attendees = new ArrayList<Attendee>(attendeeProperties.size());
        for (ExtendedProperty attendeeProperty : attendeeProperties) {
            Attendee attendee = new Attendee();
            attendee.setUri((String) attendeeProperty.getValue());
            ExtendedPropertyParameter cnParameter = attendeeProperty.getParameter("CN");
            if (null != cnParameter) {
                attendee.setCn(cnParameter.getValue());
            }
            try {
                attendee = new EntityProcessor(cid, optEntityResolver(cid)).adjustAfterLoad(attendee);
            } catch (OXException e) {
                LOG.debug("Error processing " + attendee+ ": "+e.getMessage(), e);
            }
            attendees.add(attendee);
        }
        return attendees;
    }

    /**
     * Optionally gets an entity resolver for the supplied context.
     *
     * @param contextId The identifier of the context to get the entity resolver for
     * @return The entity resolver, or <code>null</code> if not available
     */
    private static EntityResolver optEntityResolver(int contextId) {
        CalendarUtilities calendarUtilities = Services.getOptionalService(CalendarUtilities.class);
        if (null != calendarUtilities) {
            try {
                return calendarUtilities.getEntityResolver(contextId);
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(RdbCalendarStorage.class).warn(
                    "Error getting entity resolver for context {}: {}", I(contextId), e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Decodes a list of extended properties into a valid list of attachments.
     *
     * @param eventId The identifier of the associated event
     * @param attachmentProperties The extended attachment properties to decode
     * @return The decoded attendees, or an empty list if there are none
     */
    private List<Attachment> decodeAttachments(String eventId, List<ExtendedProperty> attachmentProperties) {
        List<Attachment> attachments = new ArrayList<Attachment>(attachmentProperties.size());
        for (ExtendedProperty attachmentProperty : attachmentProperties) {
            Attachment attachment = new Attachment();
            ExtendedPropertyParameter fmtTypeParameter = attachmentProperty.getParameter("FMTTYPE");
            if (null != fmtTypeParameter) {
                attachment.setFormatType(fmtTypeParameter.getValue());
            }
            ExtendedPropertyParameter filenameParameter = attachmentProperty.getParameter("FILENAME");
            if (null != filenameParameter) {
                attachment.setFilename(filenameParameter.getValue());
            }
            ExtendedPropertyParameter sizeParameter = attachmentProperty.getParameter("SIZE");
            if (null != sizeParameter) {
                try {
                    attachment.setSize(Long.parseLong(sizeParameter.getValue()));
                } catch (NumberFormatException e) {
                    LOG.debug("Error parsing attachment size parameter: "+e.getMessage(), e);
                }
            }
            ExtendedPropertyParameter valueParameter = attachmentProperty.getParameter("VALUE");
            if (null != valueParameter && "BINARY".equals(valueParameter.getValue())) {
                ThresholdFileHolder fileHolder = new ThresholdFileHolder();
                try {
                    fileHolder.write(BaseEncoding.base64().decode((String) attachmentProperty.getValue()));
                    attachment.setData(fileHolder);
                } catch (IllegalArgumentException | OXException e) {
                    LOG.debug("Error processing binary alarm data: "+e.getMessage(), e);
                    Streams.close(fileHolder);
                }
            } else {
                attachment.setUri((String) attachmentProperty.getValue());
            }
            attachments.add(attachment);
        }
        return attachments;
    }
}
