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

import java.util.LinkedList;
import java.util.List;

import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.importexport.csv.CSVParser;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.formats.csv.ContactFieldMapper;
import com.openexchange.importexport.formats.csv.PropertyDrivenMapper;
import com.openexchange.tools.Collections;

public class AllKindsOfCsvImporter extends CSVContactImporter {
	
    private LinkedList<ContactFieldMapper> mappers;
    private ContactFieldMapper currentMapper;
    
    @Override
	public boolean checkFields(final List<String> fields) {
        int highestAmountOfMappedFields = 0;

        for (ContactFieldMapper mapper : mappers) {
            int mappedFields = 0;
            for (final String name : fields) {
                if (mapper.getFieldByName(name) != null) {
                    mappedFields++;
                }
            }
            if (mappedFields > highestAmountOfMappedFields) {
                currentMapper = mapper;
                highestAmountOfMappedFields = mappedFields;
            }

        }
        return currentMapper != null;
    }
    
    @Override
    protected boolean passesSanityTestForDisplayName(List<String> headers) {
        return Collections.any(
            headers,
            currentMapper.getNameOfField(ContactField.DISPLAY_NAME),
            currentMapper.getNameOfField(ContactField.SUR_NAME),
            currentMapper.getNameOfField(ContactField.GIVEN_NAME),
            currentMapper.getNameOfField(ContactField.EMAIL1),
            currentMapper.getNameOfField(ContactField.EMAIL2),
            currentMapper.getNameOfField(ContactField.EMAIL3),
            currentMapper.getNameOfField(ContactField.COMPANY),
            currentMapper.getNameOfField(ContactField.NICKNAME),
            currentMapper.getNameOfField(ContactField.MIDDLE_NAME));
    }

	public void addFieldMapper(PropertyDrivenMapper mapper) {
		if (mappers == null) {
			mappers = new LinkedList<ContactFieldMapper>();
		}
		mappers.add(mapper);
	}
	
    @Override
    protected ContactField getRelevantField(final String name) {
        return currentMapper.getFieldByName(name);
    }

    @Override
    protected CSVParser getCSVParser() {
        final CSVParser result = super.getCSVParser();
        result.setTolerant(true);
        return result;
    }

    @Override
    protected boolean isResponsibleFor(Format f) {
        return Format.CSV == f || Format.OUTLOOK_CSV == f;
    }

    @Override
    public String getEncoding() {
        return "cp1252";
    }
    
}
