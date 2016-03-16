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

package com.openexchange.file.storage.fs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AbstractFileStorageFolderAccess;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.session.Session;

/**
 * {@link FSFolderAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FSFolderAccess extends AbstractFileStorageFolderAccess {

    private final File directory;
    private final Session session;

    /**
     * Initialises a new {@link FSFolderAccess}.
     * 
     * @param file
     * @param session
     */
    public FSFolderAccess(File file, Session session) {
        super();
        this.directory = file;
        this.session = session;
    }

    private java.io.File toDirectory(String folderId) throws OXException {
        if (folderId.equals(FileStorageFolder.ROOT_FULLNAME)) {
            return directory;
        }
        java.io.File dir = new java.io.File(directory, folderId);
        if (!dir.getParentFile().equals(directory)) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create("No directory traversal, please");
        }

        return dir;
    }

    private FileStorageFolder initFolder(String folderId, File dir) {
        if (dir.equals(directory)) {
            return new RootFolder(dir);
        }
        DefaultFileStorageFolder folder = new DefaultFileStorageFolder();
        folder.setId(folderId);
        folder.setCreationDate(new Date(0));
        folder.setLastModifiedDate(new Date(dir.lastModified()));

        folder.setExists(dir.exists() && dir.canRead() && dir.canExecute());

        File[] contents = dir.listFiles(new OnlyFiles());

        File[] directories = dir.listFiles(new OnlyDirectories());

        if (contents == null) {
            contents = new File[0];
        }

        folder.setFileCount(contents.length);
        folder.setHoldsFiles(contents.length > 0);
        folder.setHoldsFolders(directories.length > 0);
        folder.setName(dir.getName());
        folder.setSubfolders(directories.length > 0);
        folder.setSubscribed(true);
        folder.setSubscribedSubfolders(true);
        folder.setRootFolder(dir.equals(directory));
        folder.setDefaultFolder(folder.isRootFolder());
        folder.setOwnPermission(DefaultFileStoragePermission.newInstance());
        folder.setPermissions(Arrays.asList((FileStoragePermission) DefaultFileStoragePermission.newInstance()));
        if (folder.isRootFolder()) {
            folder.setParentId(null);
        } else {
            folder.setParentId(getFolderId(dir.getParentFile()));
        }

        return folder;
    }

    @Override
    public boolean exists(String folderId) throws OXException {
        File dir = toDirectory(folderId);
        return dir.exists() && dir.canRead() && dir.canExecute();
    }

    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        return initFolder(folderId, toDirectory(folderId));

    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        File dir = toDirectory(session.getUserlogin());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return getFolder(session.getUserlogin());
    }

    @Override
    public FileStorageFolder getTrashFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    @Override
    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        File[] dirs = toDirectory(parentIdentifier).listFiles(new OnlyDirectories());
        if (dirs == null) {
            return new FileStorageFolder[0];
        }
        FileStorageFolder[] subfolders = new FileStorageFolder[dirs.length];

        int i = 0;
        for (File file : dirs) {
            subfolders[i++] = initFolder(parentIdentifier + "/" + file.getName(), file);
        }
        return subfolders;
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        return getFolder(FileStorageFolder.ROOT_FULLNAME);
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        new File(toDirectory(toCreate.getParentId()), toCreate.getName()).mkdirs();
        return toCreate.getParentId() + "/" + toCreate.getName();
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        File dir = toDirectory(identifier);
        File dest = new File(dir.getParentFile(), toUpdate.getName());
        if (!dir.getName().equals(toUpdate.getName())) {
            // Move
            dir.renameTo(dest);
        }

        return identifier + "/" + toUpdate.getName();
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        File dir = toDirectory(folderId);
        File dest = toDirectory(newParentId);

        dir.renameTo(new File(dest, null != newName ? newName : dir.getName()));

        return newParentId + "/" + (null != newName ? newName : dir.getName());
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        File dir = toDirectory(folderId);
        File dest = new File(dir.getParentFile(), newName);
        dir.renameTo(dest);

        return dir.getParent() + "/" + newName;
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        toDirectory(folderId).delete();
        return null;
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        toDirectory(folderId).delete();
        return null;
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        File[] files = toDirectory(folderId).listFiles(new OnlyFiles());
        if (files == null) {
            return;
        }
        for (File file : files) {
            file.delete();
        }
    }

    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        clearFolder(folderId);
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        File dir = toDirectory(folderId);
        List<FileStorageFolder> path = new ArrayList<FileStorageFolder>();

        while (!dir.equals(directory)) {
            path.add(initFolder(getFolderId(dir), dir));
            dir = dir.getParentFile();
        }
        return null;
    }

    private String getFolderId(File dir) {
        List<String> components = new ArrayList<String>();
        while (!dir.equals(directory)) {
            components.add(dir.getName());
            dir = dir.getParentFile();
        }
        Collections.reverse(components);
        StringBuilder b = new StringBuilder();

        for (String comp : components) {
            b.append(comp).append('/');
        }
        if (b.length() > 0) {
            b.setLength(b.length() - 1);
        }

        return b.toString();
    }

}
