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

package com.openexchange.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;

import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.RootRequest;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.folder.actions.UpdatesRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllRequest;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.framework.CommonListRequest;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.ajax.framework.CommonUpdatesResponse;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.ajax.writer.FolderWriter;

import org.json.JSONObject;

import junit.framework.TestCase;

/**
 * This class and FolderObject should be all that is needed to write folder-related tests. 
 * If multiple users are needed use multiple instances of this class. Examples of tests using this class can be found in ExemplaryFolderTestManagerTest.java.
 * @author <a href="mailto:karsten.will@open-xchange.org">Karsten Will</a>
*/
public class FolderTestManager extends TestCase {
	private Vector<FolderObject> insertedOrUpdatedFolders;
	private AJAXClient client;
	private FolderParser folderParser;
	
	public FolderTestManager(AJAXClient client) {
		this.client = client;
		insertedOrUpdatedFolders = new Vector<FolderObject>();
		folderParser = new FolderParser();
	}

	/**
	 * Creates a folder via HTTP-API and updates it with new id,
	 * timestamp and all other information that is updated after
	 * such requests. Remembers this folder for cleanup later.
	 *
	 */
	public FolderObject insertFolderOnServer(FolderObject folderToCreate){
		InsertRequest request = new InsertRequest(folderToCreate);
		CommonInsertResponse response = null;
		try {
			response = client.execute(request);
		} catch (AjaxException e) {
			fail("AjaxException during folder creation: "+e.getMessage());
		} catch (IOException e) {
			fail("IOException during folder creation: "+e.getMessage());
		} catch (SAXException e) {
			fail("SAXException during folder creation: "+e.getMessage());
		} catch (JSONException e) {
			fail("JSONException during folder creation: "+e.getMessage());
		}
		response.fillObject(folderToCreate);
		insertedOrUpdatedFolders.add(folderToCreate);
		return folderToCreate;
	}
	
	/**
	 * Create multiple folders via the HTTP-API at once
	 */
	public void insertFoldersOnServer(FolderObject[] folders) {
		for (int i=0; i<folders.length; i++) {
			this.insertFolderOnServer(folders[i]);
		}
	}
	
	/**
	 * Updates a folder via HTTP-API
	 * and returns the same folder for convenience
	 */
	public FolderObject updateFolderOnServer(FolderObject folder){
		UpdateRequest request = new UpdateRequest(folder);
		try {
			client.execute(request);
			remember(folder);
		} catch (AjaxException e) {
			fail("AjaxException while updating folder with ID " + folder.getObjectID()+ ": " + e.getMessage());
		} catch (IOException e) {
			fail("IOException while updating folder with ID " + folder.getObjectID()+ ": " + e.getMessage());
		} catch (SAXException e) {
			fail("SAXException while updating folder with ID " + folder.getObjectID()+ ": " + e.getMessage());
		} catch (JSONException e) {
			fail("JSONException while updating folder with ID " + folder.getObjectID()+ ": " + e.getMessage());
		}
		return folder;
	}
	
	/**
	 * Deletes a folder via HTTP-API
	 * 
	 */
	public void deleteFolderOnServer(FolderObject folderToDelete) throws AjaxException, IOException, SAXException, JSONException{
		DeleteRequest request = new DeleteRequest(folderToDelete);
		client.execute(request);
	}
	
	public void deleteFolderOnServer(FolderObject folderToDelete, boolean failOnError){
		try {
			deleteFolderOnServer(folderToDelete);
		} catch (AjaxException e) {
			if (failOnError)
				fail("AjaxException while deleting folder with ID " + folderToDelete.getObjectID()+ ": " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			if (failOnError)
				fail("IOException while deleting folder with ID " + folderToDelete.getObjectID()+ ": " + e.getMessage());
			e.printStackTrace();
		} catch (SAXException e) {
			if (failOnError)
				fail("SAXException while deleting folder with ID " + folderToDelete.getObjectID()+ ": " + e.getMessage());
			e.printStackTrace();
		} catch (JSONException e) {
			if (failOnError)
				fail("JSONException while deleting folder with ID " + folderToDelete.getObjectID()+ ": " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Get a folder via HTTP-API with an existing FolderObject
	 */
	public FolderObject getFolderFromServer(FolderObject folder){
	    if(folder.getObjectID() == 0) {
	        return getFolderFromServer(folder.getFullName(), true);
	    } else {
	        return getFolderFromServer(folder.getObjectID(), true);
	    }
	}
	
	public FolderObject getFolderFromServer(FolderObject folder, boolean failOnError){
		if(folder.getObjectID() == 0) {
            return getFolderFromServer(folder.getFullName(), failOnError);
        } else {
            return getFolderFromServer(folder.getObjectID(), failOnError);
        }
	}
	
	public FolderObject getFolderFromServer(String name) {
	    return getFolderFromServer(name, true);
	}
	
	/**
	 * Get a folder via HTTP-API with no existing FolderObject and the folders name as identifier
	 */
	public FolderObject getFolderFromServer(String name, boolean failOnError) {
		FolderObject returnedFolder = null;
		GetRequest request = new GetRequest(name, failOnError);
		GetResponse response = null;
		try {
			response = (GetResponse) client.execute(request);
			returnedFolder = response.getFolder();
		} catch (AjaxException e) {
			fail("AjaxException while getting folder with name " + name + ": " + e.getMessage());
		} catch (IOException e) {
			fail("IOException while getting folder with name " + name + ": " + e.getMessage());
		} catch (SAXException e) {
			fail("SAXException while getting folder with name " + name + ": " + e.getMessage());
		} catch (JSONException e) {
			fail("JSONException while getting folder with name " + name + ": " + e.getMessage());
		} catch (OXException e) {
		    e.printStackTrace();
			fail("OXException while getting folder with name " + name + ": " + e.getMessage());
		} catch (OXJSONException e) {
			fail("OXJSONException while getting folder with name " + name + ": " + e.getMessage());
		}
		return returnedFolder;
	}
	
	/**
	 * Get a folder via HTTP-API with no existing FolderObject and the folders id as identifier
	 */
	public FolderObject getFolderFromServer(final int folderId, boolean failOnError) {
		FolderObject returnedFolder = null;
		GetRequest request = new GetRequest(folderId, FolderObject.ALL_COLUMNS);
		GetResponse response = null;
		try {
			response = (GetResponse) client.execute(request);
			returnedFolder = response.getFolder();
		} catch (AjaxException e) {
			if (failOnError)
				fail("AjaxException while getting folder with id " + Integer.toString(folderId) + ": " + e.getMessage());
		} catch (IOException e) {
			if (failOnError)
				fail("IOException while getting folder with id " + Integer.toString(folderId) + ": " + e.getMessage());
		} catch (SAXException e) {
			if (failOnError)
				fail("SAXException while getting folder with id " + Integer.toString(folderId) + ": " + e.getMessage());
		} catch (JSONException e) {
			if (failOnError)
				fail("JSONException while getting folder with id " + Integer.toString(folderId) + ": " + e.getMessage());
		} catch (OXException e) {
			if (failOnError)
				fail("OXException while getting folder with id " + Integer.toString(folderId) + ": " + e.getMessage());
		} catch (OXJSONException e) {
			if (failOnError)
				fail("OXJSONException while getting folder with id " + Integer.toString(folderId) + ": " + e.getMessage());
		}
		return returnedFolder;
	}
	
	public FolderObject getFolderFromServer(final int folderId) {
		return getFolderFromServer(folderId, true);
	}

	/**
	 * removes all folders inserted or updated by this Manager
	 */
	public void cleanUp(){
		try {
			for(FolderObject folder: insertedOrUpdatedFolders){
				deleteFolderOnServer(folder);
			}
		} catch (AjaxException e) {
			fail("AjaxException occured during clean-up: " + e.getMessage());
		} catch (IOException e) {
			fail("IOException occured during clean-up: " + e.getMessage());
		} catch (SAXException e) {
			fail("SAXException occured during clean-up: " + e.getMessage());
		} catch (JSONException e) {
			fail("JSONException occured during clean-up: " + e.getMessage());
		}
	}

	/**
	 * get all folders in one parent folder via the HTTP-API
	 */
	public FolderObject[] listFoldersOnServer (int parentFolderId) {
		Vector <FolderObject> allFolders = new Vector<FolderObject>();
		//FolderObject parentFolder = this.getFolderFromServer(parentFolderId);
		ListRequest request = new ListRequest (Integer.toString(parentFolderId), new int [] {FolderObject.OBJECT_ID}, true);
		try {
			ListResponse response = client.execute(request);
			Iterator<FolderObject> iterator = response.getFolder();
			while (iterator.hasNext()) {
				allFolders.add(iterator.next());
			}
		} catch (AjaxException e) {
			fail("AjaxException occured while getting all folders for parent folder with id: " + Integer.toString(parentFolderId) + ": " + e.getMessage());
		} catch (IOException e) {
			fail("IOException occured while getting all folders for parent folder with id: " + Integer.toString(parentFolderId) + ": " + e.getMessage());
		} catch (SAXException e) {
			fail("SAXException occured while getting all folders for parent folder with id: " + Integer.toString(parentFolderId) + ": " + e.getMessage());
		} catch (JSONException e) {
			fail("JSONException occured while getting all folders for parent folder with id: " + Integer.toString(parentFolderId) + ": " + e.getMessage());
		} catch (OXException e) {
			fail("OXException occured while getting all folders for parent folder with id: " + Integer.toString(parentFolderId) + ": " + e.getMessage());
		}
		FolderObject[] folderArray = new FolderObject[allFolders.size()];
		allFolders.copyInto(folderArray);
		return folderArray;
	}
	
	/**
     * get all folders in one parent folder via the HTTP-API
     */
    public FolderObject[] listFoldersOnServer (FolderObject folder) {
        if(folder.getObjectID() != 0) {
            return listFoldersOnServer(folder.getObjectID());
        }
        Vector <FolderObject> allFolders = new Vector<FolderObject>();
        //FolderObject parentFolder = this.getFolderFromServer(parentFolderId);
        ListRequest request = new ListRequest (folder.getFullName(), new int [] {FolderObject.OBJECT_ID}, true);
        try {
            ListResponse response = client.execute(request);
            Iterator<FolderObject> iterator = response.getFolder();
            while (iterator.hasNext()) {
                allFolders.add(iterator.next());
            }
        } catch (AjaxException e) {
            fail("AjaxException occured while getting all folders for parent folder with id: " + folder.getFullName() + ": " + e.getMessage());
        } catch (IOException e) {
            fail("IOException occured while getting all folders for parent folder with id: " + folder.getFullName() + ": " + e.getMessage());
        } catch (SAXException e) {
            fail("SAXException occured while getting all folders for parent folder with id: " + folder.getFullName() + ": " + e.getMessage());
        } catch (JSONException e) {
            fail("JSONException occured while getting all folders for parent folder with id: " + folder.getFullName() + ": " + e.getMessage());
        } catch (OXException e) {
            fail("OXException occured while getting all folders for parent folder with id: " + folder.getFullName() + ": " + e.getMessage());
        }
        FolderObject[] folderArray = new FolderObject[allFolders.size()];
        allFolders.copyInto(folderArray);
        return folderArray;
    }
	
	public FolderObject[] listRootFoldersOnServer() {
	    Vector <FolderObject> allFolders = new Vector<FolderObject>();
        //FolderObject parentFolder = this.getFolderFromServer(parentFolderId);
        RootRequest request = new RootRequest (new int [] {FolderObject.OBJECT_ID}, true);
        try {
            ListResponse response = client.execute(request);
            Iterator<FolderObject> iterator = response.getFolder();
            while (iterator.hasNext()) {
                allFolders.add(iterator.next());
            }
        } catch (AjaxException e) {
            fail("AjaxException occured while getting all root folders." + e.getMessage());
        } catch (IOException e) {
            fail("IOException occured while getting all root folders." + e.getMessage());
        } catch (SAXException e) {
            fail("SAXException occured while getting all root folders." + e.getMessage());
        } catch (JSONException e) {
            fail("JSONException occured while getting all root folders." + e.getMessage());
        } catch (OXException e) {
            fail("OXException occured while getting all root folders." + e.getMessage());
        }
        FolderObject[] folderArray = new FolderObject[allFolders.size()];
        allFolders.copyInto(folderArray);
        return folderArray;
	}
	

	/**
	 * Get folders in a parent folder that were updated since a specific date via the HTTP-API 
	 */
	public FolderObject [] getUpdatedFoldersOnServer (int folderId, Date lastModified) {
		Vector <FolderObject> allFolders = new Vector<FolderObject>();
		UpdatesRequest request = new UpdatesRequest(folderId, new int [] {FolderObject.OBJECT_ID}, -1, null, lastModified);
		try {
			CommonUpdatesResponse response = (CommonUpdatesResponse) client.execute(request);
			final JSONArray data = (JSONArray) response.getResponse().getData();
			FolderObject fo = new FolderObject();
			for (int i=0; i<data.length(); i++) {
				JSONArray tempArray = data.getJSONArray(i);
				fo = this.getFolderFromServer(tempArray.getInt(0), true);
				allFolders.add(fo);
			}
		} catch (AjaxException e) {
			fail("AjaxException occured while getting folders updated since date: "+ lastModified + ", in parent folder: " + Integer.toString(folderId) + e.getMessage());
		} catch (IOException e) {
			fail("IOException occured while getting folders updated since date: "+ lastModified + ", in parent folder: " + Integer.toString(folderId) + e.getMessage());
		} catch (SAXException e) {
			fail("SAXException occured while getting folders updated since date: "+ lastModified + ", in parent folder: " + Integer.toString(folderId) + e.getMessage());
		} catch (JSONException e) {
			fail("JSONException occured while getting folders updated since date: "+ lastModified + ", in parent folder: " + Integer.toString(folderId) + e.getMessage());
		} catch (Exception e) {
			fail("Exception occured while getting folders updated since date: "+ lastModified + ", in parent folder: " + Integer.toString(folderId) + e.getMessage());
		}
		FolderObject[] folderArray = new FolderObject[allFolders.size()];
		allFolders.copyInto(folderArray);
		return folderArray;
	}
	
	private void remember (FolderObject folder) {
		for (FolderObject tempFolder: insertedOrUpdatedFolders) {
			if (tempFolder.getObjectID() == folder.getObjectID()) {
				insertedOrUpdatedFolders.set(insertedOrUpdatedFolders.indexOf(tempFolder), folder);
			}
			else {
				insertedOrUpdatedFolders.add(folder);
			}
		}
	}

//	/**
//	 * get all folders specified by multiple int-arrays with 2 slots each (1st slot: folderId, 2nd slot objectId) via the HTTP-API
//	 */
//	public FolderObject[] listFoldersOnServer (final int[]... folderAndObjectIds) {
//		Vector <FolderObject> allFolders = new Vector<FolderObject>();
//		CommonListRequest request = new CommonListRequest(AbstractFolderRequest.FOLDER_URL, folderAndObjectIds, FolderObject.ALL_COLUMNS);
//		try {
//			CommonListResponse response = client.execute(request);
//			final JSONArray data = (JSONArray) response.getResponse().getData();
//			this.convertJSONArray2Vector(data, allFolders);
//		} catch (AjaxException e) {
//			fail("AjaxException occured while getting a list of folders : " + e.getMessage());
//		} catch (IOException e) {
//			fail("IOException occured while getting a list of folders : " + e.getMessage());
//		} catch (SAXException e) {
//			fail("SAXException occured while getting a list of folders : " + e.getMessage());
//		} catch (JSONException e) {
//			fail("JSONException occured while getting a list of folders : " + e.getMessage());
//		} catch (Exception e) {
//			fail("Exception occured while getting a list of folders : " + e.getMessage());
//		}
//		FolderObject[] folderArray = new FolderObject[allFolders.size()];
//		allFolders.copyInto(folderArray);
//		return folderArray;
//	}	
	
//	/**
//	 * get all folders in one parent folder via the HTTP-API
//	 */
//	public FolderObject[] getAllFoldersOnServer (int folderId) {
//		Vector <FolderObject> allFolders = new Vector<FolderObject>();
//		CommonAllRequest request = new CommonAllRequest (AbstractFolderRequest.FOLDER_URL, folderId, new int [] {FolderObject.OBJECT_ID}, 0, null, true);
//		try {
//			CommonAllResponse response = client.execute(request);
//			final JSONArray data = (JSONArray) response.getResponse().getData();
//			for (int i=0; i < data.length(); i++) {
//				JSONArray temp = (JSONArray) data.optJSONArray(i);
//				int tempFolderId = temp.getInt(0);
//				FolderObject tempFolder = getFolderFromServer(tempFolderId);
//				allFolders.add(tempFolder);
//			}
//		} catch (AjaxException e) {
//			fail("AjaxException occured while getting all folders for parent folder with id: " + folderId + ": " + e.getMessage());
//		} catch (IOException e) {
//			fail("IOException occured while getting all folders for parent folder with id: " + folderId + ": " + e.getMessage());
//		} catch (SAXException e) {
//			fail("SAXException occured while getting all folders for parent folder with id: " + folderId + ": " + e.getMessage());
//		} catch (JSONException e) {
//			fail("JSONException occured while getting all folders for parent folder with id: " + folderId + ": " + e.getMessage());
//		}
//		FolderObject[] folderArray = new FolderObject[allFolders.size()];
//		allFolders.copyInto(folderArray);
//		return folderArray;
//	}	
	
//	private void convertJSONArray2Vector(JSONArray data, Vector allFolders) throws JSONException, OXException {
//		for (int i=0; i < data.length(); i++) {
//			final JSONArray jsonArray = data.getJSONArray(i);
//			JSONObject jsonObject = new JSONObject();
//			for (int a=0; a < jsonArray.length(); a++){
//				if (!"null".equals(jsonArray.getString(a))){
//					String fieldname = FolderMapping.columnToFieldName(FolderObject.ALL_COLUMNS[a]);
//					jsonObject.put(fieldname, jsonArray.getString(a));
//				}	
//			}
//			FolderObject folderObject = new FolderObject();
//			folderParser.parse(folderObject, jsonObject);
//			allFolders.add(folderObject);	
//		}	
//	}
//	
//	
//}
//final class FolderMapping extends TestCase{
//	
//	private static HashMap columns2fields;
//	private static HashMap fields2columns;
//	
//	static {
//		fields2columns = new HashMap();
//		columns2fields = new HashMap();
//		
//		try {
//			put(FolderFields.TITLE, FolderObject.FOLDER_NAME);
//			put(FolderFields.MODULE, FolderObject.MODULE);
//			put(FolderFields.TYPE, FolderObject.TYPE);
//			put(FolderFields.SUBFOLDERS, FolderObject.SUBFOLDERS);
//			put(FolderFields.OWN_RIGHTS, FolderObject.OWN_RIGHTS);
//			put(FolderFields.PERMISSIONS, FolderObject.PERMISSIONS_BITS);
//			put(FolderFields.SUMMARY, FolderObject.SUMMARY);
//			put(FolderFields.STANDARD_FOLDER, FolderObject.STANDARD_FOLDER);
//			put(FolderFields.TOTAL, FolderObject.TOTAL);
//			put(FolderFields.NEW, FolderObject.NEW);
//			put(FolderFields.UNREAD, FolderObject.UNREAD);
//			put(FolderFields.DELETED, FolderObject.DELETED);
//			put(FolderFields.CAPABILITIES, FolderObject.CAPABILITIES);
//			put(FolderFields.SUBSCRIBED, FolderObject.SUBSCRIBED);
//			put(FolderFields.FOLDER_ID, FolderObject.FOLDER_ID);
//			put(FolderFields.ID, FolderObject.OBJECT_ID);
//			put(FolderFields.CREATED_BY, FolderObject.CREATED_BY);
//			put(FolderFields.MODIFIED_BY, FolderObject.MODIFIED_BY);
//			put(FolderFields.CREATION_DATE, FolderObject.CREATION_DATE);
//			put(FolderFields.LAST_MODIFIED, FolderObject.LAST_MODIFIED);
//			put(FolderFields.LAST_MODIFIED_UTC, FolderObject.LAST_MODIFIED_UTC);
//			
//			
//			
//		} catch (Exception e) {
//			fail(e.getMessage());
//		}
//	}
//	
//	private static void put(String fieldname, int column) throws Exception {
//		if (!fields2columns.containsKey(fieldname) && !columns2fields.containsKey(column)) {
//			fields2columns.put(fieldname, column);
//			columns2fields.put(column, fieldname);
//		}
//		else throw (new Exception("One Part of this combination is also mapped to something else!"));
//	}
//	
//	public static String columnToFieldName (int column) {
//		return (String)columns2fields.get(column);
//	}
//	
//	public static int fieldNameToColumn (String fieldname) {
//		return Integer.valueOf((Integer)fields2columns.get(fieldname));
//	}
}
