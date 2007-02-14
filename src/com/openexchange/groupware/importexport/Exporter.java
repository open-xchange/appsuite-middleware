package com.openexchange.groupware.importexport;

import java.io.InputStream;

import com.openexchange.groupware.importexport.exceptions.ImportExportException;

/**
 * Defines a class able to export a certain type of OX folder as a certain format
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public interface Exporter {

	/**
	 * 
	 * @param format: Format the exported data is supposed to be in
	 * @param folder: Folder that should be exported. Note: A folder can only contain data of one type
	 * @return true, if the given folders can be exported in the given format; false otherwise
	 */
	public abstract boolean canExport(Format format, String folder);

	/**
	 * 
	 * @param format: Format the returned InputStream should be in.
	 * @param folder: Folder that should be exported. Note: A folder can only contain data of one type.
	 * @param fieldsToBeExported: A list of fields of that folder that should be exported. Convention: If the list is empty, all fields are exported.
	 * @return InputStream in requested format.
	 * @throws ImportExportException
	 */
	public abstract InputStream exportData(Format format, String folder,
			String... fieldsToBeExported) throws ImportExportException;

	/**
	 * 
	 * @param format: Format the returned InputStream should be in.
	 * @param folder: Folder that should be exported. Note: A folder can only contain data of one type.
	 * @param objectId: Id of an entry in that folder that is to be exported.
	 * @param fieldsToBeExported: A list of fields of that folder that should be exported. Convention: If the list is empty, all fields are exported.
	 * @return InputStream in requested format.
	 * @throws ImportExportException
	 */
	public abstract InputStream exportData(Format format, String folder, int objectId,
			String... fieldsToBeExported) throws ImportExportException;

}