package com.openexchange.groupware.importexport;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
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
		"Cannot find an exporter for folder %s to format %s" })


/** 
 * Meant for the import and export of data in OX folders.
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
	 */
	public List<ImportResult> importData(final SessionObject sessObj, final Format format, final InputStream is, final String... folders) throws ImportExportException{
		for(Importer imp : importers){
			if(imp.canImport(sessObj, format, folders)){
				return imp.importData(sessObj, format, is, folders);
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
	 * @return InputStream containing the exported data in given format
	 * @throws ImportExportException in case of a missing exporter for that kind of data 
	 */
	public InputStream exportData(final SessionObject sessObj, final Format format, final String folder, final String...fieldsToBeExported) throws ImportExportException{
		for(Exporter exp: exporters){
			if(exp.canExport(sessObj, format, folder)){
				return exp.exportData(sessObj, format, folder, fieldsToBeExported);
			}
		}
		throw EXCEPTIONS.create(1, folder, format);
	}

	/**
	 * Exports data of one object in one folder
	 * 
	 * @param sessObj: Session object used to determine access rights
	 * @param objectId: Identifier for one object in a folder
	 */
	public InputStream exportData(final SessionObject sessObj, final Format format, final String folder, final int objectId, final String...fieldsToBeExported) throws ImportExportException{
		for(Exporter exp: exporters){
			if(exp.canExport(sessObj, format, folder)){
				return exp.exportData(sessObj, format, folder, objectId, fieldsToBeExported);
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
	 * @return A set of possible formats this folder can be exported to
	 */
	public Set<Format> getPossibleExportFormats(final SessionObject sessObj, final String folder){
		Set<Format> res = new HashSet<Format>();
		
		for(Format format: Format.values()){
			for(Exporter exp: exporters){
				if(exp.canExport(sessObj, format, folder)){
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
	 * @param folder: Identifier of a certain folder within the OX
	 * @return A set of possible formats this folder can import
	 */
	public Set<Format> getPossibleImportFormats(final SessionObject sessObj, final String folder){
		Set<Format> res = new HashSet<Format>();
		
		for(Format format: Format.values()){
			for(Importer imp: importers){
				if(imp.canImport(sessObj, format, folder)){
					res.add(format);
					break;
				}
			}
		}
		return res;
	}
}
