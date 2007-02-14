package com.openexchange.groupware.importexport;

import java.io.InputStream;
import java.util.List;

import com.openexchange.groupware.importexport.exceptions.ImportExportException;

/**
 * This interface defines an importer, meaning a class able to
 * import one or more data formats into the OX.
 *  
 * @author Tobias Prinz, mailto:tobias.prinz@open-xchange.com
 *
 */
public interface Importer {

	/**
	 * 
	 * @param format: Format of the data that is meant to be imported
	 * @param folders: Folders the data is meant to be imported into (remember: a folder is always of a certain type, like Appointment)
	 * @return true, if this importer can import this format for this module; false otherwise
	 */
	public abstract boolean canImport(Format format, String... folders);

	/**
	 * 
	 * @param format: Format of the data to be imported
	 * @param is: InputStream containing data to be imported
	 * @param folders: Identifiers for folders - usually only one, but iCal may need two and future extensions might need even more (remember: Folders can have only one type, so type is not a necessary argument)
	 * @return
	 * @throws ImportExportException
	 */
	public abstract List<ImportResult> importData(
			Format format,
			InputStream is,
			String... folders ) throws ImportExportException;

}