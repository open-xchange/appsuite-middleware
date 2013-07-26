/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * @(#)FileTypeMap.java	1.8 05/11/16
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package javax.activation;

import java.io.File;

/**
 * The FileTypeMap is an abstract class that provides a data typing
 * interface for files. Implementations of this class will
 * implement the getContentType methods which will derive a content
 * type from a file name or a File object. FileTypeMaps could use any
 * scheme to determine the data type, from examining the file extension
 * of a file (like the MimetypesFileTypeMap) to opening the file and
 * trying to derive its type from the contents of the file. The
 * FileDataSource class uses the default FileTypeMap (a MimetypesFileTypeMap
 * unless changed) to determine the content type of files.
 *
 * @see javax.activation.FileTypeMap
 * @see javax.activation.FileDataSource
 * @see javax.activation.MimetypesFileTypeMap
 */

public abstract class FileTypeMap {

    private static FileTypeMap defaultMap = null;

    /**
     * The default constructor.
     */
    public FileTypeMap() {
	super();
    }

    /**
     * Return the type of the file object. This method should
     * always return a valid MIME type.
     *
     * @param file A file to be typed.
     * @return The content type.
     */
    abstract public String getContentType(File file);

    /**
     * Return the type of the file passed in.  This method should
     * always return a valid MIME type.
     *
     * @param filename the pathname of the file.
     * @return The content type.
     */
    abstract public String getContentType(String filename);

    /**
     * Sets the default FileTypeMap for the system. This instance
     * will be returned to callers of getDefaultFileTypeMap.
     *
     * @param map The FileTypeMap.
     * @exception SecurityException if the caller doesn't have permission
     *					to change the default
     */
    public static void setDefaultFileTypeMap(FileTypeMap map) {
	SecurityManager security = System.getSecurityManager();
	if (security != null) {
	    try {
		// if it's ok with the SecurityManager, it's ok with me...
		security.checkSetFactory();
	    } catch (SecurityException ex) {
		// otherwise, we also allow it if this code and the
		// factory come from the same class loader (e.g.,
		// the JAF classes were loaded with the applet classes).
		if (FileTypeMap.class.getClassLoader() !=
			map.getClass().getClassLoader())
		    throw ex;
	    }
	}
	defaultMap = map;	
    }

    /**
     * Return the default FileTypeMap for the system.
     * If setDefaultFileTypeMap was called, return
     * that instance, otherwise return an instance of
     * <code>MimetypesFileTypeMap</code>.
     *
     * @return The default FileTypeMap
     * @see javax.activation.FileTypeMap#setDefaultFileTypeMap
     */
    public static FileTypeMap getDefaultFileTypeMap() {
	// XXX - probably should be synchronized
	if (defaultMap == null)
	    defaultMap = new MimetypesFileTypeMap();
	return defaultMap;
    }
}
