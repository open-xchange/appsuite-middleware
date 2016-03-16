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

package com.openexchange.admin.rmi.exceptions;

import java.util.ArrayList;

/**
 * Is thrown when user sends invalid data to the server.
 *
 * @author cutmasta
 *
 */
public class InvalidDataException extends Exception {

    /**
     * IF we need more granular exceptions for invalid filestore url or invalid
     * username for example, the new Exception must extend this exception.
     */

    /**
     * Contains the name of the object which is affected by this exception
     */
    private String objectname = null;

    /**
     * Contains the fieldnames in the object (if available) which are not correct
     */
    private ArrayList<String> fieldnames = null;
    /**
     * For serialization
     */
    private static final long serialVersionUID = 5803502090025698411L;

    /**
     *
     */
    public InvalidDataException() {
        super("Invalid data sent!");
    }

    /**
     * @param message
     */
    public InvalidDataException(String message) {
        super(message);

    }

    /**
     * @param cause
     */
    public InvalidDataException(Throwable cause) {
        super(cause);

    }

    /**
     * @param message
     * @param cause
     */
    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Set the fieldname which are not correct in the object
     *
     * @return
     */
    public final ArrayList<String> getFieldnames() {
        return fieldnames;
    }

    /**
     * Get the fieldnames of the object which aren't correct (maybe null if no fieldnames can be specified)
     *
     * @param fieldnames
     */
    public final void setFieldnames(ArrayList<String> fieldnames) {
        this.fieldnames = fieldnames;
    }

    /**
     * Get the Name of the object which is affected by this exception
     *
     * @return
     */
    public final String getObjectname() {
        return objectname;
    }

    /**
     *
     *
     * @param objectname
     */
    public final void setObjectname(String objectname) {
        this.objectname = objectname;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        if (null != fieldnames) {
            sb.append("The following field are invalid:\n");
            sb.append(fieldnames);
        }
        return super.toString();
    }

}
