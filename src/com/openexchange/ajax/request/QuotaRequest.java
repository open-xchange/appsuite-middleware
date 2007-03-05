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

package com.openexchange.ajax.request;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.api2.MailInterface;
import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.filestore.FilestoreException;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.FileStorageException;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.file.FileStorageException.Code;

/**
 * FIXME replace QuotaFileStorage FileStorage
 */
public class QuotaRequest extends CommonRequest {

	private static final Log LOG = LogFactory.getLog(QuotaRequest.class);
	
	private QuotaFileStorage qfs;
	
	private AbstractOXException fsException;

	private SessionObject session;

	public QuotaRequest(SessionObject sessionObj,PrintWriter w) {
		super(w);
		try {
			this.qfs = (QuotaFileStorage) FileStorage.getInstance(FilestoreStorage.createURI(sessionObj.getContext()),sessionObj.getContext(),new DBPoolProvider());
		} catch (AbstractOXException e) {
			this.fsException = e;
		}
		
		this.session = sessionObj;
	}
	
	public boolean action(String action, SimpleRequest req){
		if (action.equals(AJAXServlet.ACTION_GET)) {
			filestore();
			return true;
		} else if (action.equals("filestore")) {
			filestore();
			return true;
		} else if (action.equals("mail")) {
			mail();
			return true;
		}
		return false;
	
	}
	
	private void exception(AbstractOXException exception){
		Response resp = new Response();
		resp.setException(exception);
		try {
			LOG.error(exception.getMessage(), exception);
			Response.write(resp, w);
		} catch (JSONException e) {
			LOG.error(e);
		}
	}

	private void filestore() {
		if(fsException != null) {
			exception(fsException);
			return;
		}
		try {
			long use = qfs.getUsage();
			long quota = qfs.getQuota();
			JSONObject res = new JSONObject();
			JSONObject data = new JSONObject();
			data.put("quota",quota);
			data.put("use",use);
			res.put("data",data);
			w.write(res.toString());
		} catch (Exception e) {
			handle(e);
		}
	}
	
	private void mail() {
		MailInterface mi = null;
		
		try {
			mi = MailInterfaceImpl.getInstance(this.session);
			long[] quotaInfo = mi.getQuota();
			long quota = quotaInfo[0];
			long use = quotaInfo[1];
			JSONObject res = new JSONObject();
			JSONObject data = new JSONObject();
			data.put("quota",quota*1024);
			data.put("use",use*1024);
			res.put("data",data);
			w.write(res.toString());
		} catch (OXException e) {
			exception(e);
		} catch (Exception e) {
			handle(e);
		} finally {
            // FIXME What about if mi == null?
			try {
				mi.close(false);
			} catch (OXException e) {
				LOG.error(e);
			}
		}
	}

}
