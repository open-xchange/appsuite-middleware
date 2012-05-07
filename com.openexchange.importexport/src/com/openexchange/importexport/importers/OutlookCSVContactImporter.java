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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.importexport.importers;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactSetter;
import com.openexchange.groupware.contact.helpers.ContactSwitcher;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForBooleans;
import com.openexchange.groupware.contact.helpers.ContactSwitcherForSimpleDateFormat;
import com.openexchange.groupware.contact.helpers.SplitBirthdayFieldsSetter;
import com.openexchange.importexport.formats.Format;
import com.openexchange.groupware.importexport.csv.CSVParser;
import com.openexchange.importexport.formats.csv.ContactFieldMapper;
import com.openexchange.importexport.formats.csv.DutchOutlookMapper;
import com.openexchange.importexport.formats.csv.EnglishOutlookMapper;
import com.openexchange.importexport.formats.csv.FrenchOutlookMapper;
import com.openexchange.importexport.formats.csv.GermanOutlookMapper;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.tools.Collections;

/**
 * Imports the CSV format of Outlook, regardless of the file being written with an English, French or German version of Outlook.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class OutlookCSVContactImporter extends CSVContactImporter {

    private List<ContactFieldMapper> fieldMappers;

    private ContactFieldMapper fieldMapper;

    private boolean isUsingFallback = false;

    public boolean isUsingFallback() {
        return isUsingFallback;
    }

    public List<ContactFieldMapper> getFieldMappers() {
        if (fieldMappers == null) { // default
            fieldMappers = new LinkedList<ContactFieldMapper>();
            fieldMappers.add(new GermanOutlookMapper());
            fieldMappers.add(new FrenchOutlookMapper());
            fieldMappers.add(new EnglishOutlookMapper());
            fieldMappers.add(new DutchOutlookMapper());
            isUsingFallback = true;
        }
        return fieldMappers;
    }

    public void setFieldMappers(List<ContactFieldMapper> fieldMappers) {
        this.fieldMappers = fieldMappers;
    }

    public ContactFieldMapper getFieldMapper() {
        return fieldMapper;
    }

    public void setFieldMapper(ContactFieldMapper fieldMapper) {
        this.fieldMapper = fieldMapper;
    }

    public void addFieldMappers(ContactFieldMapper newMapper) {
        if (fieldMappers == null)
            fieldMappers = new LinkedList<ContactFieldMapper>();
        fieldMappers.add(newMapper);
    }

    @Override
    protected ContactField getRelevantField(final String name) {
        return getFieldMapper().getFieldByName(name);
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

    /**
     * This importers assumes the encoding CP-1252 for files uploaded, since this format is mainly used on European and American Windows
     * systems.
     */
    @Override
    public String getEncoding() {
        return "cp1252";
    }

    /**
     * Opposed to the basic CSV importer, this method probes different language mappers and sets the correct ContactFieldMapper for this
     * class.
     */
    @Override
	public boolean checkFields(final List<String> fields) {
        int highestAmountOfMappedFields = 0;

        for (ContactFieldMapper mapper : getFieldMappers()) {
            int mappedFields = 0;
            for (final String name : fields) {
                if (mapper.getFieldByName(name) != null) {
                    mappedFields++;
                }
            }
            if (mappedFields > highestAmountOfMappedFields) {
                fieldMapper = mapper;
                highestAmountOfMappedFields = mappedFields;
            }

        }
        return fieldMapper != null;
    }

    @Override
	public ContactSwitcher getContactSwitcher() {
        final ContactSwitcherForSimpleDateFormat dateSwitcher = new ContactSwitcherForSimpleDateFormat();
        dateSwitcher.addDateFormat(getGermanDateNotation());
        dateSwitcher.addDateFormat(getAmericanDateNotation());
        dateSwitcher.setDelegate(new ContactSetter());

        final ContactSwitcherForBooleans boolSwitcher = new ContactSwitcherForBooleans();
        boolSwitcher.setDelegate(dateSwitcher);

        final SplitBirthdayFieldsSetter bdaySwitcher = new SplitBirthdayFieldsSetter();
        bdaySwitcher.setDelegate(boolSwitcher);

        return bdaySwitcher;
    }

    public static final SimpleDateFormat getGermanDateNotation() {
        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        sdf.setTimeZone(ImportExportServices.getCalendarCollectionService().getTimeZone("UTC"));
        return sdf;
    }

    public static final SimpleDateFormat getAmericanDateNotation() {
        final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        sdf.setTimeZone(ImportExportServices.getCalendarCollectionService().getTimeZone("UTC"));
        return sdf;
    }

    @Override
    protected String getNameForFieldInTruncationError(final int id, final OXException notused) {
        final ContactField field = ContactField.getByValue(id);
        if (field == null) {
            return String.valueOf(id);
        }
        return getFieldMapper().getNameOfField(field);
    }

    @Override
    protected boolean passesSanityTestForDisplayName(List<String> headers) {
        ContactFieldMapper mpr = getFieldMapper();

        return Collections.any(
            headers,
            mpr.getNameOfField(ContactField.DISPLAY_NAME),
            mpr.getNameOfField(ContactField.SUR_NAME),
            mpr.getNameOfField(ContactField.GIVEN_NAME),
            mpr.getNameOfField(ContactField.EMAIL1),
            mpr.getNameOfField(ContactField.EMAIL2),
            mpr.getNameOfField(ContactField.EMAIL3),
            mpr.getNameOfField(ContactField.COMPANY),
            mpr.getNameOfField(ContactField.NICKNAME),
            mpr.getNameOfField(ContactField.MIDDLE_NAME));
    }

}
