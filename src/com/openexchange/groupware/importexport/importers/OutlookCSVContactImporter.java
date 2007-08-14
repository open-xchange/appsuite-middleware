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

package com.openexchange.groupware.importexport.importers;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactSetter;
import com.openexchange.groupware.contact.helpers.ContactSwitcher;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForBooleans;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForSimpleDateFormat;
import com.openexchange.groupware.contact.mappers.ContactFieldMapper;
import com.openexchange.groupware.contact.mappers.EnglishOutlookMapper;
import com.openexchange.groupware.contact.mappers.FrenchOutlookMapper;
import com.openexchange.groupware.contact.mappers.GermanOutlookMapper;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.Importer;
import com.openexchange.groupware.importexport.csv.CSVParser;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;

@OXExceptionSource(
	classId=ImportExportExceptionClasses.OUTLOOKCSVCONTACTIMPORTER, 
	component=Component.IMPORT_EXPORT)
@OXThrowsMultiple(
	category={
		Category.TRUNCATED}, 
	desc={""}, 
	exceptionId={0}, 
	msg={
		"The following field(s) are too long to be imported: %s"})
/**
 * Imports the CSV format of Outlook, regardless of the file being written with an English, 
 * French or German version of Outlook.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class OutlookCSVContactImporter extends CSVContactImporter implements Importer {
	
	private static final ImportExportExceptionFactory EXCEPTIONS = new ImportExportExceptionFactory(OutlookCSVContactImporter.class);
	
	protected ContactFieldMapper fieldMapper;
	
	@Override
	protected ContactField getRelevantField(final String name) {
		return fieldMapper.getFieldByName(name);
	}

	@Override
	protected CSVParser getCSVParser() {
		final CSVParser result = super.getCSVParser();
		result.setTolerant(true);
		return result;
	}

	@Override
	protected Format getResponsibleFor() {
		return Format.OUTLOOK_CSV;
	}

	
	
	@Override
	public String getEncoding() {
		return "cp1252";
	}

	@Override
	/**
	 * Opposed to the basic CSV importer, this method probes different
	 * language mappers and sets the correct ContactFieldMapper for this
	 * class.
	 */
	protected boolean checkFields(List<String> fields) {
		int de = 0, fr = 0, en = 0;
		final ContactFieldMapper 	deMap = new GermanOutlookMapper(), 
									frMap = new FrenchOutlookMapper(), 
									enMap = new EnglishOutlookMapper();
		for(final String name: fields){
			if(deMap.getFieldByName(name) != null){
				de++;
			}
			if(enMap.getFieldByName(name) != null){
				en++;
			}
			if(frMap.getFieldByName(name) != null){
				fr++;
			}
		}
		fieldMapper = enMap;
		if(de > en){
			fieldMapper = deMap;
		}
		if(fr > de){
			fieldMapper = frMap;
		}
		return de > 0 || fr > 0 || en > 0;
	}

	@Override
	protected ContactSwitcher getContactSwitcher() {
		final ContactSwitcherForSimpleDateFormat dateSwitcher = new ContactSwitcherForSimpleDateFormat();
		dateSwitcher.addDateFormat( getGermanDateNotation());
		dateSwitcher.addDateFormat( getAmericanDateNotation());
		dateSwitcher.setDelegate(new ContactSetter());
		final ContactSwitcherForBooleans boolSwitcher = new ContactSwitcherForBooleans();
		boolSwitcher.setDelegate(dateSwitcher);
		return boolSwitcher;
	}
	
	public static final SimpleDateFormat getGermanDateNotation(){
		final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf; 
	}
	
	public static final SimpleDateFormat getAmericanDateNotation(){
		final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf; 
	}

	@Override
	protected String getNameForFieldInTruncationError(int id, OXException notused) {
		final ContactField field = ContactField.getByValue(id);
		if(field == null){
			return String.valueOf( id );
		}
		return fieldMapper.getNameOfField(field);
	}
}
