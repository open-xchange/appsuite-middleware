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
package com.openexchange.admin.container;

import com.openexchange.admin.dataSource.I_OXUtil;

import java.util.Hashtable;

/**
 *
 * @author cutmasta
 */
public class Database {
    
    private long databaseId = -1;    
    private int databaseMaxUnits = 5000;
    private int databasePoolInitial = 10;
    private int databasePoolHardlimit = 10;
    private int databasePoolMax = 20;
    private int databaseClusterWeight = 100;
    private String databaseUrl = null;
    private String databaseDriver = null;
    private String databaseDisplayName = null;
    private String databaseAuthenticationId = null;
    private String databaseAuthenticationPassword = null;
    boolean ismaster = true;
    
    public Database() {
    }

    public boolean isMaster(){
        return ismaster;
    }
    
    public void setMaster(boolean isitmaster){
        this.ismaster = isitmaster;
    }
    
    public long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(long databaseId) {
        this.databaseId = databaseId;
    }

    public int getDatabaseMaxUnits() {
        return databaseMaxUnits;
    }

    public void setDatabaseMaxUnits(int databaseMaxUnits) {
        this.databaseMaxUnits = databaseMaxUnits;
    }

    public int getDatabasePoolInitial() {
        return databasePoolInitial;
    }

    public void setDatabasePoolInitial(int databasePoolInitial) {
        this.databasePoolInitial = databasePoolInitial;
    }

    public int getDatabasePoolHardlimit() {
        return databasePoolHardlimit;
    }

    public void setDatabasePoolHardlimit(int databasePoolHardlimit) {
        this.databasePoolHardlimit = databasePoolHardlimit;
    }

    public int getDatabasePoolMax() {
        return databasePoolMax;
    }

    public void setDatabasePoolMax(int databasePoolMax) {
        this.databasePoolMax = databasePoolMax;
    }

    public int getDatabaseClusterWeight() {
        return databaseClusterWeight;
    }

    public void setDatabaseClusterWeight(int databaseClusterWeight) {
        this.databaseClusterWeight = databaseClusterWeight;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public String getDatabaseDriver() {
        return databaseDriver;
    }

    public void setDatabaseDriver(String databaseDriver) {
        this.databaseDriver = databaseDriver;
    }

    public String getDatabaseDisplayName() {
        return databaseDisplayName;
    }

    public void setDatabaseDisplayName(String databaseDisplayName) {
        this.databaseDisplayName = databaseDisplayName;
    }

    public String getDatabaseAuthenticationId() {
        return databaseAuthenticationId;
    }

    public void setDatabaseAuthenticationId(String databaseAuthenticationId) {
        this.databaseAuthenticationId = databaseAuthenticationId;
    }

    public String getDatabaseAuthenticationPassword() {
        return databaseAuthenticationPassword;
    }

    public void setDatabaseAuthenticationPassword(String databaseAuthenticationPassword) {
        this.databaseAuthenticationPassword = databaseAuthenticationPassword;
    }
    
    public Hashtable xform2Data(){
        Hashtable db = new Hashtable();
        db.put(I_OXUtil.DB_AUTHENTICATION_ID, this.getDatabaseAuthenticationId());
        db.put(I_OXUtil.DB_AUTHENTICATION_PASSWORD, this.getDatabaseAuthenticationPassword());
        db.put(I_OXUtil.DB_DISPLAY_NAME, this.getDatabaseDisplayName());
        db.put(I_OXUtil.DB_DRIVER, this.getDatabaseDriver());
        db.put(I_OXUtil.DB_POOL_HARDLIMIT, this.getDatabasePoolHardlimit());
        db.put(I_OXUtil.DB_URL, this.getDatabaseUrl());
        db.put(I_OXUtil.DB_POOL_MAX, this.getDatabasePoolMax());
        db.put(I_OXUtil.DB_POOL_INIT, this.getDatabasePoolInitial());
        db.put(I_OXUtil.DB_CLUSTER_WEIGHT, this.getDatabaseClusterWeight());
        db.put(I_OXUtil.DB_MAX_UNITS, this.getDatabaseMaxUnits());
        return db;
    }
    
}
