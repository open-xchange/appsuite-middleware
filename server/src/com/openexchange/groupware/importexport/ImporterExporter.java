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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;
import com.openexchange.sessiond.SessionObject;

@OXThrowsMultiple(
	category = { 
		Category.SUBSYSTEM_OR_SERVICE_DOWN,
		Category.SUBSYSTEM_OR_SERVICE_DOWN
	}, 
	desc = { "" , ""}, 
	exceptionId = { 0 , 1 }, 
	msg = { 
		"Cannot find an importer for format %s into folders %s",
		"Cannot find an exporter for folder %s to format %s" 
	}
)
@OXExceptionSource(
	classId=ImportExportExceptionClasses.IMPORTEREXPORTER, 
	component=Component.IMPORT_EXPORT)


/** 
 * Selects the appropriate importer or exporter for an import or export.
 * 
 * The list of importers and exports is usually loaded via Spring in the
 * ImportServlet or ExportServlet, but this might be changed in the future. 
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class ImporterExporter {
	

	private final static ImportExportExceptionFactory EXCEPTIONS = new ImportExportExceptionFactory(ImporterExporter.class);
	private List <Importer> importers;
	private List <Exporter> exporters;


	public ImporterExporter(){
		importers = new ArrayList<Importer>();
		exporters = new ArrayList<Exporter>();
	}


	//--- SETTER & GETTER ---//
	public List<Exporter> getExporters() {
		return exporters;
	}
	public void setExporters(final List<Exporter> exporters) {
		this.exporters = exporters;
	}
	public void addExporter(final Exporter exp){
		exporters.add(exp);
	}
	public void removeExporter(final Exporter exp){
		exporters.remove(exp);
	}

	public List<Importer> getImporters() {
		return importers;
	}
	public void setImporters(final List<Importer> importers) {
		this.importers = importers;
	}
	public void addImporter(final Importer exp){
		importers.add(exp);
	}
	public void removeImporter(final Importer exp){
		importers.remove(exp);
	}

	//--- PROPER METHODS ---//-
	
	
	/**
	 * Imports data to one or more folders
	 * 
	 * @param sessObj: Session object used to determine access rights
	 * @param format: Format the imported data is in
	 * @param is: InputStream containing the data to be imported
	 * @param folders: One or more folders the data is to be imported to (usually one, but iCal may contain both tasks and appointments and future formats might contain even more)
	 * @param optionalParams: Params that might be needed by a specific implementor of this interface. Note: The format was chosen to be congruent with HTTP-GET
	 */
	public List<ImportResult> importData(final SessionObject sessObj, final Format format, final InputStream is, final List<String> folders, final Map<String, String[]> optionalParams) throws ImportExportException{
		for(final Importer imp : getImporters()){
			if(imp.canImport(sessObj, format, folders, optionalParams)){
				return imp.importData(sessObj, format, is, folders, optionalParams);
			}
		}
		throw EXCEPTIONS.create(0, format, folders);
	}

	/**
	 * Exports data of one folder
	 * 
	 * @param sessObj: Session object used to determine access rights
	 * @param format: Format the exported data should be in
	 * @param folder: Folder that is to be exported
	 * @param fieldsToBeExported: Fields of certain data that are to be exported. Convention: If this is empty, all fields are exported
	 * @param optionalParams: Params that might be needed by a specific implementor of this interface. Note: The format was chosen to be congruent with HTTP-GET
	 * @return InputStream containing the exported data in given format
	 * @throws ImportExportException in case of a missing exporter for that kind of data 
	 */
	public SizedInputStream exportData(final SessionObject sessObj, final Format format, final String folder, final int[] fieldsToBeExported, final Map<String, String[]> optionalParams) throws ImportExportException{
		for(final Exporter exp: getExporters()){
			if(exp.canExport(sessObj, format, folder, optionalParams)){
				return exp.exportData(sessObj, format, folder, fieldsToBeExported, optionalParams);
			}
		}
		throw EXCEPTIONS.create(1, folder, format);
	}

	/**
	 * Exports the data of one object in one folder
	 * 
	 * @param sessObj
	 * @param format
	 * @param folder
	 * @param type
	 * @param objectId
	 * @param fieldsToBeExported
	 * @param optionalParams: Params that might be needed by a specific implementor of this interface. Note: The format was chosen to be congruent with HTTP-GET
	 * @return
	 * @throws ImportExportException
	 */
	public SizedInputStream exportData(final SessionObject sessObj, final Format format, final String folder, final int objectId, final int[] fieldsToBeExported, final Map<String, String[]> optionalParams) throws ImportExportException{
		for(final Exporter exp: exporters){
			if(exp.canExport(sessObj, format, folder,  optionalParams)){
				return exp.exportData(sessObj, format, folder, objectId, fieldsToBeExported, optionalParams);
			}
		}
		throw EXCEPTIONS.create(1, folder, format);
	}

	/**
	 * Lists all formats a folder (which in OX is always of a certain type) 
	 * can be converted into.
	 * 
	 * @param sessObj: Session object used to determine access rights
	 * @param folder: Identifier of a certain folder within the OX
	 * @param type: Type of the folder as defined in class Types
	 * @param optionalParams: Params that might be needed by a specific implementor of this interface. Note: The format was chosen to be congruent with HTTP-GET
	 * @return A set of possible formats this folder can be exported to
	 * @throws ImportExportException 
	 */
	public Set<Format> getPossibleExportFormats(final SessionObject sessObj, final String folder, final Map<String, String[]> optionalParams) throws ImportExportException{
		final Set<Format> res = new HashSet<Format>();
		
		for(final Format format: Format.values()){
			for(final Exporter exp: exporters){
				if(exp.canExport(sessObj, format, folder, optionalParams)){
					res.add(format);
					break;
				}
			}
		}
		return res;
	}
	
	/**
	 * Lists all formats a folder can import data from
	 *
	 * @param sessObj: Session object used to determine access rights
	 * @param folderMapping: Identifier of a certain folder (plus its type as defined in class Type) within the OX
	 * @param optionalParams: Params that might be needed by a specific implementor of this interface. Note: The format was chosen to be congruent with HTTP-GET
	 * @return A set of possible formats this folder can import
	 * @throws ImportExportException 
	 */
	public Set<Format> getPossibleImportFormats(final SessionObject sessObj, final List<String> folders, final Map<String, String[]> optionalParams) throws ImportExportException{
		final Set<Format> res = new HashSet<Format>();
		
		for(final Format format: Format.values()){
			for(final Importer imp: importers){
				if(imp.canImport(sessObj, format, folders, optionalParams)){
					res.add(format);
					break;
				}
			}
		}
		return res;
	}
}
