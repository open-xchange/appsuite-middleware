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

package com.openexchange.groupware.importexport;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;

/**
 * This class contains basic helper methods needed by all importers.
 *  
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
@OXExceptionSource(
		classId=ImportExportExceptionClasses.ABSTRACTIMPORTER, 
		component=Component.IMPORT_EXPORT)
	@OXThrowsMultiple(
		category={
			Category.TRUNCATED}, 
		desc={""}, 
		exceptionId={0}, 
		msg={
			"The following field(s) are too long to be imported: %s"})
public abstract class AbstractImporter implements Importer {
	private static final ImportExportExceptionFactory EXCEPTIONS = new ImportExportExceptionFactory(AbstractImporter.class);
	
	protected OXException handleDataTruncation(OXException oxEx){
		if(oxEx.getCategory() == Category.TRUNCATED){
			final String separator = ", ";
			final StringBuilder bob = new StringBuilder();
			final int[] ids = oxEx.getTruncatedIds();			
			if(oxEx.getComponent() == Component.CONTACT){
				for(int id : ids){
					final ContactField field = ContactField.getByValue(id);
					bob.append( field == null ? id : field.getReadableName() );
					bob.append(separator);
				}
			} else {
				for(int id : ids){
					bob.append(id);
					bob.append(separator);
				}
			}
			bob.setLength(bob.length() - separator.length());
			oxEx = EXCEPTIONS.create(0, bob.toString());
		}
		return oxEx;
	}


}
