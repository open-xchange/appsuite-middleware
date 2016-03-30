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

package com.openexchange.ajax.appointment.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.AbstractUpdatesRequest.Ignore;

/**
 * Request to get updated appointments.
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class UpdatesRequest extends AbstractAppointmentRequest<AppointmentUpdatesResponse> {

    private final int folderId;
    private final int[] columns;
    private final Date timestamp;
    private final boolean recurrenceMaster;
    private final boolean showPrivates;
    private final Ignore ignore;

    /**
     * Initializes a new {@link UpdatesRequest} that doesn't show private appointments and ignores updates for deleted. 
     * 
     * @param folderId Folder id to use for the request
     * @param columns  Columns to use for the request
     * @param timestamp Timestamp to use for the request
     * @param recurrenceMaster if true a recurring appointment isn't split into single occurrances but kept as one object
     */
    public UpdatesRequest(final int folderId, final int[] columns, final Date timestamp, final boolean recurrenceMaster) {
        this(folderId, columns, timestamp, recurrenceMaster, false, Ignore.DELETED);
    }

    /**
     * Initializes a new {@link UpdatesRequest} that ignores updates for deleted.
     * 
     * @param folderId Folder id to use for the request
     * @param columns  Columns to use for the request
     * @param timestamp Timestamp to use for the request
     * @param recurrenceMaster if true a recurring appointment isn't split into single occurrances but kept as one object
     * @param showPrivates When true, shows private appointments of the folder owner (Only works in shared folders)
     */
    public UpdatesRequest(final int folderId, final int[] columns, final Date timestamp, final boolean recurrenceMaster, final boolean showPrivates) {
        this(folderId, columns, timestamp, recurrenceMaster, showPrivates, Ignore.DELETED);
    }

    /**
     * Initializes a new {@link UpdatesRequest}. Ignores private appointments by default.
     * 
     * @param folderId Folder id to use for the request
     * @param columns  Columns to use for the request
     * @param timestamp Timestamp to use for the request
     * @param recurrenceMaster if true a recurring appointment isn't split into single occurrances but kept as one object
     * @param ignore What kind of updates should be ignored
     */
    public UpdatesRequest(final int folderId, final int[] columns, final Date timestamp, final boolean recurrenceMaster, Ignore ignore) {
        this(folderId, columns, timestamp, recurrenceMaster, false, ignore);
    }

    /**
     * Initializes a new {@link UpdatesRequest}.
     * 
     * @param folderId Folder id to use for the request
     * @param columns  Columns to use for the request
     * @param timestamp Timestamp to use for the request
     * @param recurrenceMaster if true a recurring appointment isn't split into single occurrances but kept as one object
     * @param showPrivates When true, shows private appointments of the folder owner (Only works in shared folders)
     * @param ignore What kind of updates should be ignored
     */
    public UpdatesRequest(final int folderId, final int[] columns, final Date timestamp, final boolean recurrenceMaster, final boolean showPrivates, Ignore ignore) {
        this.folderId = folderId;
        this.columns = columns;
        this.timestamp = timestamp;
        this.recurrenceMaster = recurrenceMaster;
        this.showPrivates = showPrivates;
        this.ignore = ignore;
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() {
        final List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_INFOLDER, String.valueOf(folderId)));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, timestamp));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_IGNORE, ignore.getValue()));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_RECURRENCE_MASTER, recurrenceMaster));
        parameterList.add(new Parameter(AJAXServlet.PARAMETER_SHOW_PRIVATE_APPOINTMENTS, showPrivates));
        return parameterList.toArray(new Parameter[parameterList.size()]);
    }

    @Override
    public AbstractAJAXParser<AppointmentUpdatesResponse> getParser() {
        return new AppointmentUpdatesParser(columns);
    }
}
