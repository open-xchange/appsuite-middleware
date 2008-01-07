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

package com.openexchange.admin.rmi.dataobjects;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Object for setting/getting access informations to the different ox modules
 *
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class UserModuleAccess implements Serializable {

    /**
     * For serialization
     */
    private static final long serialVersionUID = -5336341908204911967L;
    
    // ALL ACCESS MODULES; 
    // MAKE SURE YOU REWRITE THE "equals" METHOD
    // IF YOU CHANGE SOMETHING HERE!!!
    private boolean calendar = true;

    private boolean contacts = true;

    private boolean delegateTask = true;

    private boolean editPublicFolders = true;

    private boolean forum = true;

    private boolean ical = true;

    private boolean infostore = true;

    private boolean PinboardWrite = true;

    private boolean Projects = true;

    private boolean readCreateSharedFolders = true;

    private boolean RssBookmarks = true;

    private boolean RssPortal = true;

    private boolean Syncml = true;

    private boolean Tasks = true;

    private boolean Vcard = true;

    private boolean Webdav = true;

    private boolean WebdavXml = true;

    private boolean Webmail = true;

    /**
     * Creates a new instance of UserModuleAccess
     */
    public UserModuleAccess() {
        super();
    }

    /**
     * @param calendar
     * @param contacts
     * @param delegateTask
     * @param editPublicFolders
     * @param forum
     * @param ical
     * @param infostore
     * @param pinboardWrite
     * @param projects
     * @param readCreateSharedFolders
     * @param rssBookmarks
     * @param rssPortal
     * @param syncml
     * @param tasks
     * @param vcard
     * @param webdav
     * @param webdavXml
     * @param webmail
     */
    public UserModuleAccess(boolean calendar, boolean contacts, boolean delegateTask, boolean editPublicFolders, boolean forum, boolean ical, boolean infostore, boolean pinboardWrite, boolean projects, boolean readCreateSharedFolders, boolean rssBookmarks, boolean rssPortal, boolean syncml, boolean tasks, boolean vcard, boolean webdav, boolean webdavXml, boolean webmail) {
        super();
        this.calendar = calendar;
        this.contacts = contacts;
        this.delegateTask = delegateTask;
        this.editPublicFolders = editPublicFolders;
        this.forum = forum;
        this.ical = ical;
        this.infostore = infostore;
        this.PinboardWrite = pinboardWrite;
        this.Projects = projects;
        this.readCreateSharedFolders = readCreateSharedFolders;
        this.RssBookmarks = rssBookmarks;
        this.RssPortal = rssPortal;
        this.Syncml = syncml;
        this.Tasks = tasks;
        this.Vcard = vcard;
        this.Webdav = webdav;
        this.WebdavXml = webdavXml;
        this.Webmail = webmail;
    }

    /**
     * Enable all modules  
     */
    public void enableAll() {
    	this.calendar = true;
    	this.contacts = true;
    	this.delegateTask = true;
    	this.editPublicFolders = true;
    	this.forum = true;
    	this.ical = true;
    	this.infostore = true;
    	this.PinboardWrite = true;
    	this.Projects = true;
    	this.readCreateSharedFolders = true;
    	this.RssBookmarks = true;
    	this.RssPortal = true;
    	this.Syncml = true;
    	this.Tasks = true;
    	this.Vcard = true;
    	this.Webdav = true;
    	this.WebdavXml = true;
    	this.Webmail = true;
    }
    
    /**
     * Disable all modules  
     */
    public void disableAll() {
    	this.calendar = false;
    	this.contacts = false;
    	this.delegateTask = false;
    	this.editPublicFolders = false;
    	this.forum = false;
    	this.ical = false;
    	this.infostore = false;
    	this.PinboardWrite = false;
    	this.Projects = false;
    	this.readCreateSharedFolders = false;
    	this.RssBookmarks = false;
    	this.RssPortal = false;
    	this.Syncml = false;
    	this.Tasks = false;
    	this.Vcard = false;
    	this.Webdav = false;
    	this.WebdavXml = false;
    	this.Webmail = false;
    }
    
    /**
     * Shows if a user has access to the calendar module of ox.
     * 
     * @return Returns <CODE>true</CODE> if user has access to calendar module
     *         or <CODE>false</CODE> if he has now access!
     */
    public boolean getCalendar() {
        return calendar;
    }

    /**
     * Defines if a user has access to the calendar module of ox.
     * 
     * @param val
     *            Set to <CODE>true</CODE> if user should be able to access
     *            the calendar module!
     */
    public void setCalendar(boolean val) {
        this.calendar = val;
    }

    /**
     * Shows if a user has access to the contact module of ox.
     * 
     * @return Returns <CODE>true</CODE> if user has access to contact module
     *         or <CODE>false</CODE> if he has now access!
     */
    public boolean getContacts() {
        return contacts;
    }

    /**
     * Defines if a user has access to the contact module of ox.
     * 
     * @param val
     *            Set to <CODE>true</CODE> if user should be able to access
     *            the contact module!
     */
    public void setContacts(boolean val) {
        this.contacts = val;
    }

    /**
     * Shows if a user has the right to delegate tasks in the ox groupware.
     * 
     * @return Returns <CODE>true</CODE> if user has the right to delegate
     *         tasks in the ox groupware. Or <CODE>false</CODE> if he has no
     *         right to delegate tasks!
     */
    public boolean getDelegateTask() {
        return delegateTask;
    }

    /**
     * Defines if a user has the right to delegate tasks in the ox groupware.
     * 
     * @param val
     *            Set to <CODE>true</CODE> if user should be able to delegate
     *            tasks in the ox groupware.
     */
    public void setDelegateTask(boolean val) {
        this.delegateTask = val;
    }

    public boolean getEditPublicFolders() {
        return editPublicFolders;
    }

    public void setEditPublicFolders(boolean val) {
        this.editPublicFolders = val;
    }

    public boolean getForum() {
        return forum;
    }

    public void setForum(boolean val) {
        this.forum = val;
    }

    public boolean getIcal() {
        return ical;
    }

    public void setIcal(boolean val) {
        this.ical = val;
    }

    public boolean getInfostore() {
        return infostore;
    }

    public void setInfostore(boolean val) {
        this.infostore = val;
    }

    public boolean getPinboardWrite() {
        return PinboardWrite;
    }

    public void setPinboardWrite(boolean val) {
        this.PinboardWrite = val;
    }

    public boolean getProjects() {
        return Projects;
    }

    public void setProjects(boolean val) {
        this.Projects = val;
    }

    public boolean getReadCreateSharedFolders() {
        return readCreateSharedFolders;
    }

    public void setReadCreateSharedFolders(boolean val) {
        this.readCreateSharedFolders = val;
    }

    public boolean getRssBookmarks() {
        return RssBookmarks;
    }

    public void setRssBookmarks(boolean val) {
        this.RssBookmarks = val;
    }

    public boolean getRssPortal() {
        return RssPortal;
    }

    public void setRssPortal(boolean val) {
        this.RssPortal = val;
    }

    public boolean getSyncml() {
        return Syncml;
    }

    public void setSyncml(boolean val) {
        this.Syncml = val;
    }

    public boolean getTasks() {
        return Tasks;
    }

    public void setTasks(boolean val) {
        this.Tasks = val;
    }

    public boolean getVcard() {
        return Vcard;
    }

    public void setVcard(boolean val) {
        this.Vcard = val;
    }

    public boolean getWebdav() {
        return Webdav;
    }

    public void setWebdav(boolean val) {
        this.Webdav = val;
    }

    public boolean getWebdavXml() {
        return WebdavXml;
    }

    public void setWebdavXml(boolean val) {
        this.WebdavXml = val;
    }

    public boolean getWebmail() {
        return Webmail;
    }

    public void setWebmail(boolean val) {
        this.Webmail = val;
    }
    
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("[ \n");
        for (final Field f : this.getClass().getDeclaredFields()) {
            try {
                Object ob = f.get(this);
                String tname = f.getName();
                if (ob != null && !tname.equals("serialVersionUID")) {
                    ret.append("  ");
                    ret.append(tname);
                    ret.append(": ");
                    ret.append(ob);
                    ret.append("\n");
                }
            } catch (IllegalArgumentException e) {
                ret.append("IllegalArgument\n");
            } catch (IllegalAccessException e) {
                ret.append("IllegalAccessException\n");
            }
        }
        ret.append("]");
        return ret.toString();
    }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final UserModuleAccess other = (UserModuleAccess) obj;
		if (PinboardWrite != other.PinboardWrite)
			return false;
		if (Projects != other.Projects)
			return false;
		if (RssBookmarks != other.RssBookmarks)
			return false;
		if (RssPortal != other.RssPortal)
			return false;
		if (Syncml != other.Syncml)
			return false;
		if (Tasks != other.Tasks)
			return false;
		if (Vcard != other.Vcard)
			return false;
		if (Webdav != other.Webdav)
			return false;
		if (WebdavXml != other.WebdavXml)
			return false;
		if (Webmail != other.Webmail)
			return false;
		if (calendar != other.calendar)
			return false;
		if (contacts != other.contacts)
			return false;
		if (delegateTask != other.delegateTask)
			return false;
		if (editPublicFolders != other.editPublicFolders)
			return false;
		if (forum != other.forum)
			return false;
		if (ical != other.ical)
			return false;
		if (infostore != other.infostore)
			return false;
		if (readCreateSharedFolders != other.readCreateSharedFolders)
			return false;
		return true;
	}
}
