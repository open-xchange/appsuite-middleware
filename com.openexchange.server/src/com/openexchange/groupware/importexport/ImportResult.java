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

package com.openexchange.groupware.importexport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.exception.OXException;

/**
 * Contains information on the result of an import as done by
 * implementors of Importer (see link).
 *
 * Usage: Usually, you only want to check whether the import
 * hasErrors or isCorrect (one is the inversion of the other,
 * no reason for having both besides that I'm lazy)
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 * @see com.openexchange.groupware.importexport.Importer
 */
public class ImportResult {

	private String objectId;
	private String folder;
	private OXException exception;
	private Date date;
	private int entryNumber;
	private String content;
    private final List<ConversionWarning> warnings = new ArrayList<ConversionWarning>();


    /**
	 * Basic constructor
	 *
	 */
	public ImportResult() {
		super();
	}

	/**
	 * Constructor for correct result
	 * @param objectId
	 * @param type
	 * @param timestamp
	 */
	public ImportResult(final String objectId, final String folder, final Date date){
		this(objectId, folder, date, null);
	}

	/**
	 * Constructor for botched result
	 * @param objectId
	 * @param type
	 * @param timestamp
	 * @param exception
	 */
	public ImportResult(final String objectId, final String folder, final Date date, final OXException exception){
		this(objectId, folder, date, exception, -1, null);
	}

	/**
	 * Constructor for botched result with additional information on the error
	 * @param objectId
	 * @param type
	 * @param timestamp
	 * @param exception
	 */
	public ImportResult(final String objectId, final String folder, final Date date, final OXException exception, final int entryNumber, final String content){
		this.objectId = objectId;
		this.folder = folder;
		this.date = date;
		this.exception = exception;
		this.content = content;
		this.entryNumber = entryNumber;
	}

	/** Constructor for correct result using timestamp instead of a Date
	 *
	 * @param objectId
	 * @param folder
	 * @param timestamp
	 */
	public ImportResult(final String objectId, final String folder, final long timestamp){
		this(objectId, folder, new Date(timestamp));
	}

	/**
	 * Constructor for a botched result using a timestamp instead of a Date
	 * @param objectId
	 * @param folder
	 * @param timestamp
	 * @param exception
	 */
	public ImportResult(final String objectId, final String folder, final long timestamp, final OXException exception){
		this(objectId, folder, new Date(timestamp), exception);
	}




	public boolean isCorrect(){
		return exception == null;
	}
	public boolean hasError(){
		return exception != null;
	}
	public OXException getException() {
		return this.exception;
	}
	public void setException(final OXException exception){
		this.exception = exception;
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(final String objectId) {
		this.objectId = objectId;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(final Date date) {
		this.date = date;
	}
	public String getFolder() {
		return folder;
	}
	public void setFolder(final String folder) {
		this.folder = folder;
	}
	public String getContent() {
		return content;
	}
	public void setContent(final String content) {
		this.content = content;
	}
	public int getEntryNumber() {
		return entryNumber;
	}
	public void setEntryNumber(final int entryNumber) {
		this.entryNumber = entryNumber;
	}


    public List<ConversionWarning> getWarnings() {
        return warnings;
    }

    public void addWarnings(final List<ConversionWarning> warningList) {
        warnings.addAll(warningList);
    }
}
