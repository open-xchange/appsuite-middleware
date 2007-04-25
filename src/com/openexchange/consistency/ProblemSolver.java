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


package com.openexchange.consistency;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.SortedSet;

import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.FileStorageException;

/**
 * ProblemSolver
 * 
 * This class defines the interface for all the different function which help 
 * solving consistency problems
 * 
 * @author d7
 *
 */
public abstract class ProblemSolver {
	
    /**
     * For the ProblemSolver we need a database object, so that we needn't
     * create a new Object with a DBPool
     */
    protected DatabaseImpl database;
    
    /**
     * For the ProblemSolver we need a FileStorage object, so that we needn't
     * create a new Object every time
     */
    protected FileStorage storage;

    /**
     * For the ProblemSolver we need a AttachmentBase object, so that we needn't
     * create a new Object every time
     */
    protected AttachmentBase attachments;

    /**
	 * @param database The database on which the solver works
	 */
	public ProblemSolver(DatabaseImpl database, FileStorage storage,
			AttachmentBase attachments) {
		super();
		this.database = database;
		this.storage = storage;
		this.attachments = attachments;
	}
    
	/**
	 * @return The storage object stored within
	 */
	public FileStorage getStorage() {
		return storage;
	}
	
	/**
	 * This method create a dummy file a returns its name
	 * @return The name of the dummy file
	 * @throws FileStorageException 
	 */
	protected String createDummyFile() throws FileStorageException {
		final String filetext = "This is just a dummy file";
		final InputStream input = new ByteArrayInputStream(filetext.getBytes());
		
		return storage.saveNewFile(input);
	}
	
	/**
	 * This method is used for plain deletion of the superfluous entry
	 */
	public abstract void deleteEntries(SortedSet<String> set, Context ctx);
	
	/**
	 * This method is used for solving the problem with a dummy entry
	 *
	 */
	public abstract void dummyEntries(SortedSet<String> set, Context ctx);
}
