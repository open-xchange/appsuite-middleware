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

package com.openexchange.groupware.update;

import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.SchemaException;
import com.openexchange.groupware.update.exception.SchemaExceptionFactory;

/**
 * Abstract class defining the interface for reading the schema version
 * information.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@OXExceptionSource(classId = Classes.SCHEMA_STORE, component = EnumComponent.UPDATE)
public abstract class SchemaStore {

	/**
	 * For creating exceptions.
	 */
	private static final SchemaExceptionFactory EXCEPTION = new SchemaExceptionFactory(SchemaStore.class);

	/**
	 * Class implementing this abstract interface.
	 */
	private static Class<? extends SchemaStore> implementingClass;

	/**
	 * Default constructor.
	 */
	protected SchemaStore() {
		super();
	}

	/**
	 * Factory method.
	 * 
	 * @param className
	 *            Class name of the implementation.
	 * @return an implementation for this interface.
	 */
	@OXThrowsMultiple(category = { Category.SETUP_ERROR, Category.SETUP_ERROR }, desc = { "", "" }, exceptionId = { 1,
			2 }, msg = { "Class %1$s can not be loaded.", "Cannot instantiate class %1$s." })
	public static SchemaStore getInstance(final String className) throws SchemaException {
		try {
			synchronized (SchemaStore.class) {
				if (null == implementingClass) {
					implementingClass = Class.forName(className).asSubclass(SchemaStore.class);
				}
			}
			return implementingClass.newInstance();
		} catch (InstantiationException e) {
			throw EXCEPTION.create(2, e, className);
		} catch (IllegalAccessException e) {
			throw EXCEPTION.create(2, e, className);
		} catch (ClassNotFoundException e) {
			throw EXCEPTION.create(1, e, className);
		}
	}

	public abstract Schema getSchema(final int contextId) throws SchemaException;

	/**
	 * Marks given schema as locked due to a start of an update process
	 * 
	 * @param schema -
	 *            the schema
	 * @param contextId TODO
	 * 
	 * @throws SchemaException
	 */
	public abstract void lockSchema(final Schema schema, int contextId) throws SchemaException;
	
	/**
	 * Marks given schem as unlocked to release this schema from an update
	 * process
	 * 
	 * @param schema -
	 *            the schema
	 * @param contextId TODO
	 * @throws SchemaException
	 */
	public abstract void unlockSchema(final Schema schema, int contextId) throws SchemaException;

	public Schema getSchema(final Context context) throws SchemaException {
		return getSchema(context.getContextId());
	}
}
