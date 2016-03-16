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

package com.openexchange.groupware.tasks;

import static com.openexchange.java.Autoboxing.L;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Create {

    /**
     * Prevent instantiation
     */
    private Create() {
        super();
    }

    /**
     * Creates a task and fills all fields.
     */
    public static Task createTask() {
        final Task task = new Task();
        task.setTitle("Private task");
        task.setPrivateFlag(false);
        task.setCreationDate(new Date());
        task.setLastModified(new Date());
        task.setStartDate(new Date(1133964000000L));
        task.setEndDate(new Date(1133967600000L));
        task.setAfterComplete(new Date(1133971200000L));
        task.setNote("Description");
        task.setStatus(Task.NOT_STARTED);
        task.setPriority(Task.NORMAL);
        task.setCategories("Categories");
        task.setTargetDuration(L(1440));
        task.setActualDuration(L(1440));
        task.setTargetCosts(new BigDecimal("1.0"));
        task.setActualCosts(new BigDecimal("1.0"));
        task.setCurrency("\u20ac");
        task.setTripMeter("trip meter");
        task.setBillingInformation("billing information");
        task.setCompanies("companies");
        return task;
    }

    /**
     * Creates an empty task.
     */
    public static Task createWithDefaults() {
        final Task task = new Task();
        task.setPrivateFlag(false);
        return task;
    }

    /**
     * Creates a task for a given folder and with a given title.
     * @param folder pre-fill parent folder field.
     * @param title title of the task.
     */
    public static final Task createWithDefaults(int folder, String title) {
        final Task task = createWithDefaults();
        task.setParentFolderID(folder);
        task.setTitle(title);
        return task;
    }

    /**
     * Creates a task with the specified title, description status and folder
     * @param title task's title
     * @param description task's description
     * @param status task's status
     * @param folder task's parent folder
     * @return the task
     */
    public static final Task createWithDefaults(String title, String description, int status, int folder) {
        Task task = createWithDefaults(folder, title);
        task.setNote(description);
        task.setStatus(status);
        return task;
    }

    public static final Task cloneForUpdate(Task task) {
        Task retval = new Task();
        retval.setObjectID(task.getObjectID());
        retval.setParentFolderID(task.getParentFolderID());
        retval.setLastModified(task.getLastModified());
        return retval;
    }
}
