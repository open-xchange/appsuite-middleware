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

package com.openexchange.drive.impl.storage;

import static com.openexchange.file.storage.FileStoragePermission.MAX_PERMISSION;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.management.DriveConfig;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;

/**
 * {@link DriveTemp}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveTemp {
	
	private final SyncSession session;
	
	private Boolean hasParent;
	private FileStorageFolder parentFolder;
	
    /**
     * Initializes a new {@link DriveTemp}.
     *
     * @param session The sync session
     */
	public DriveTemp(SyncSession session) {
		super();
		this.session = session;
	}
	
    /**
     * Gets a value indicating whether the special temporary ".drive" folder for uploads is or would be available or not. This method only
     * checks if such a folder would be available based on the synchronization settings, it does not yet create it.
     *
     * @return <code>true</code> if the folder would be available, <code>false</code>, otherwise
     */
    public boolean supported() throws OXException {
    	return null != getParentFolder();
    }

    /**
     * Gets a value indicating whether the special temporary ".drive" folder is available and currently exists or not. This method only
     * checks if such a folder would be available and is currently existing based on the synchronization settings, it does not yet create it.
     *
     * @return <code>true</code> if the folder is supported and already exists, <code>false</code>, otherwise
     */
    public boolean exists() throws OXException {
    	FileStorageFolder parentFolder = getParentFolder();
    	if (null != parentFolder) {
    		String parentPath = session.getStorage().getPath(parentFolder.getId());
    		String tempPath = DriveUtils.combine(parentPath, DriveConstants.TEMP_FOLDER_NAME);
    		return null != session.getStorage().optFolder(tempPath);
    	}
    	return false;
    }
    
    /**
     * Gets the path to the special temporary ".drive" folder, optionally creating it if the folder would be supported and it not yet exists.
     * 
     * @return The path to the temporary ".drive" folder, or <code>null</code> if the drive folder is not supported
     */
    public String getPath(boolean createIfNeeded) throws OXException {
        FileStorageFolder parentFolder = getParentFolder();
        if (null != parentFolder) {
            String parentPath = session.getStorage().getPath(parentFolder.getId());
            String tempPath = DriveUtils.combine(parentPath, DriveConstants.TEMP_FOLDER_NAME);
            if (createIfNeeded) {
                FileStorageFolder tempFolder = session.getStorage().getFolder(tempPath, true);
                FileStoragePermission ownPermission = tempFolder.getOwnPermission();
                if (null != ownPermission && false == checkPermissions(tempFolder)) {
                    session.trace("Permissions for temp folder at " + tempPath + " not sufficient,");
                    return null;
                }
            }
            return tempPath;
        }
        return null;
    }
    
    private boolean checkPermissions(FileStorageFolder tempFolder) throws OXException {
        FileStoragePermission ownPermission = tempFolder.getOwnPermission();
        if (null != ownPermission) {
            if (false == ownPermission.isAdmin()) {
                return false;
            }            
            if (FileStoragePermission.CREATE_SUB_FOLDERS > ownPermission.getFolderPermission() ||
                FileStoragePermission.WRITE_ALL_OBJECTS > ownPermission.getFolderPermission() ||
                FileStoragePermission.DELETE_ALL_OBJECTS > ownPermission.getDeletePermission()) {
                List<FileStoragePermission> permissions = tempFolder.getPermissions();
                List<FileStoragePermission> newPermissions = new ArrayList<FileStoragePermission>(permissions.size());
                for (FileStoragePermission permission : permissions) {
                    if (permission.getEntity() == session.getServerSession().getUserId()) {
                        DefaultFileStoragePermission newPermission = DefaultFileStoragePermission.newInstance();
                        newPermission.setAdmin(true);
                        newPermission.setAllPermissions(MAX_PERMISSION, MAX_PERMISSION, MAX_PERMISSION, MAX_PERMISSION);
                        newPermission.setEntity(session.getServerSession().getUserId());
                        newPermissions.add(newPermission);                        
                    } else {
                        newPermissions.add(permission);                        
                    }
                }
                DefaultFileStorageFolder toUpdate = new DefaultFileStorageFolder();
                toUpdate.setId(tempFolder.getId());
                toUpdate.setPermissions(newPermissions);
                session.getStorage().getFolderAccess().updateFolder(tempFolder.getId(), toUpdate);
            }
        }
        return true;
    }
    
    private FileStorageFolder getParentFolder() throws OXException {
    	if (null == parentFolder && null == hasParent) {
    		parentFolder = determineParentForTempFolder(session);
    		hasParent = null != parentFolder;
    	}
    	return parentFolder;
    }

    /**
     * Determines a suitable parent folder in which the temporary ".drive" folder might be created (or is already existing in).
     * 
     * @param session The sync session
     * @return The suitable parent folder for the temporary ".drive" folder, or <code>null</code> if there is none 
     */
    private static FileStorageFolder determineParentForTempFolder(SyncSession session) throws OXException {
        /*
         * check configuration first
         */
        if (false == DriveConfig.getInstance().isUseTempFolder()) {
            session.trace("Temporary folder for upload is disabled by configuration.");
            return null;
        }        
    	/*
    	 * determine parent folder for temp folder
    	 */
    	FileStorageFolder parentFolder = null;            	
    	if ("9".equals(session.getRootFolderID())) {
    		/*
    		 * fall back to temp folder below personal folder in case the root folder points to the root file storage folder 
    		 */
    		FileStorageFolder personalFolder = null;
    		try {
    			personalFolder = session.getStorage().getFolderAccess().getPersonalFolder(session.getRootFolderID());
    		} catch (OXException e) {
    			if (FileStorageExceptionCodes.NO_SUCH_FOLDER.equals(e)) {
    				session.trace("No personal folder found for root folder \"{}\".");
    			} else {
    				session.trace("Error checking for personal folder: " + e.getMessage());
    			}            			
    		}
    		if (null != personalFolder) {
    			if (null == session.getStorage().getPath(personalFolder.getId())) {
    				session.trace("Unable to resolve path to personal folder \"" + personalFolder.getId() + "\" + from rooot folder \"" + session.getRootFolderID());
    			} else if (null != personalFolder.getOwnPermission() &&
					FileStoragePermission.CREATE_SUB_FOLDERS > personalFolder.getOwnPermission().getFolderPermission()) {
    				session.trace("Unable to use temp folder below personal folder \"" + personalFolder.getId() + "\" due to missing permissions.");
    			} else {
    				parentFolder = personalFolder;
    				session.trace("Trying to use temp folder below " + personalFolder.getId());
    			}
    		}
    	} else {
    		/*
    		 * try and use root folder as parent by default
    		 */
    		parentFolder = session.getStorage().getFolder(DriveConstants.ROOT_PATH);            		
    	}
    	/*
    	 * check permissions & determine potential temp path
    	 */
    	if (null == parentFolder || null != parentFolder.getOwnPermission() && 
			FileStoragePermission.CREATE_SUB_FOLDERS > parentFolder.getOwnPermission().getFolderPermission()) {
    		session.trace("Temp folder not available, no suitable parent folder found");
    		return null;
    	} 
    	return parentFolder;
    }

}
