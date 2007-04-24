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

package com.openexchange.groupware.tasks;

import com.openexchange.api2.OXException;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

/**
 * This class implements the new iterator for tasks that fixes problems with
 * connection timeouts if a lot of tasks are read and that improved performance
 * while reading from database.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class NewTaskIterator implements SearchIterator, Runnable {

    /**
     * Default constructor.
     */
    public NewTaskIterator() {
        // TODO Auto-generated constructor stub
    }

    /**
     * {@inheritDoc}
     */

    public void close() throws SearchIteratorException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */

    public boolean hasNext() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */

    public boolean hasSize() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */

    public Object next() throws SearchIteratorException, OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */

    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void run() {
        // TODO Auto-generated method stub
        
    }

}
