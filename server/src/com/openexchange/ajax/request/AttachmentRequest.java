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

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONWriter;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Attachment;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.parser.AttachmentParser;
import com.openexchange.ajax.parser.AttachmentParser.UnknownColumnException;
import com.openexchange.ajax.writer.AttachmentWriter;
import com.openexchange.groupware.attach.*;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.exceptions.OXAborted;
import com.openexchange.session.Session;

@OXExceptionSource(classId = Classes.COM_OPENEXCHANGE_AJAX_REQUEST_ATTACHMENTREQUEST, component = EnumComponent.ATTACHMENT)
public class AttachmentRequest extends CommonRequest {
	
	private static final AttachmentParser PARSER = new AttachmentParser();


	private static final AttachmentBase ATTACHMENT_BASE = Attachment.ATTACHMENT_BASE;
	
	private static final Log LOG = LogFactory.getLog(AttachmentRequest.class);
	private static final AttachmentExceptionFactory EXCEPTIONS = new AttachmentExceptionFactory(AttachmentRequest.class);

    private UserConfiguration userConfig;
	private final User user;
	private Context ctx;

    public AttachmentRequest(Session session, Context ctx, JSONWriter w) {
        this(new ServerSessionAdapter(session,ctx),w);
    }

    public AttachmentRequest(final ServerSession session, final JSONWriter w) {
		super(w);
		this.ctx = session.getContext();
		this.user = UserStorage.getStorageUser(session.getUserId(), session.getContext());
		this.userConfig = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
				session.getContext());
	}
	
	public static boolean hasPermission(UserConfiguration userConfig) {
		return true; // FIXME
	}
	
	public boolean action(final String action, final SimpleRequest req){
		if (LOG.isDebugEnabled()) {
			LOG.debug("Attachments: " + action + ' ' + req);
		}
		try {
			
			if (AJAXServlet.ACTION_GET.equals(action)) {
				if (!checkRequired(req, AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_ATTACHEDID, AJAXServlet.PARAMETER_MODULE, AJAXServlet.PARAMETER_ID)) {
					return true;
				}
				final int folderId = requireNumber(req, AJAXServlet.PARAMETER_FOLDERID);
				final int attachedId = requireNumber(req, AJAXServlet.PARAMETER_ATTACHEDID);
				final int moduleId = requireNumber(req, AJAXServlet.PARAMETER_MODULE);
				final int id = requireNumber(req, AJAXServlet.PARAMETER_ID);
				
				get(folderId,attachedId,moduleId,id);
				return true;
			} else if (AJAXServlet.ACTION_UPDATES.equals(action)) {
				if (!checkRequired(req, AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_MODULE, AJAXServlet.PARAMETER_ATTACHEDID, AJAXServlet.PARAMETER_TIMESTAMP)) {
					return true;
				}
                final int folderId = requireNumber(req, AJAXServlet.PARAMETER_FOLDERID);
                final int attachedId = requireNumber(req, AJAXServlet.PARAMETER_ATTACHEDID);
                final int moduleId = requireNumber(req, AJAXServlet.PARAMETER_MODULE);
				
				long timestamp = -1;
                try {
                    timestamp = Long.parseLong(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
                } catch (NumberFormatException nfe) {
                    numberError(AJAXServlet.PARAMETER_TIMESTAMP, req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
                }
				
				final AttachmentField[] columns = PARSER.getColumns(req.getParameterValues(AJAXServlet.PARAMETER_COLUMNS));
				
				AttachmentField sort = null;
				if(null != req.getParameter(AJAXServlet.PARAMETER_SORT)) {
					sort = AttachmentField.get(Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_SORT)));
				}
				
				int order = AttachmentBase.ASC;
				if("DESC".equalsIgnoreCase(req.getParameter(AJAXServlet.PARAMETER_ORDER))) {
					order = AttachmentBase.DESC;
				}
				
				final String delete = req.getParameter(AJAXServlet.PARAMETER_IGNORE);
				
				updates(folderId,attachedId,moduleId,timestamp, "deleted".equals(delete), columns, sort, order);
				return true;
			} else if (AJAXServlet.ACTION_ALL.equals(action)) {
				if (!checkRequired(req, AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_MODULE, AJAXServlet.PARAMETER_ATTACHEDID)) {
					return true;
				}
                final int folderId = requireNumber(req, AJAXServlet.PARAMETER_FOLDERID);
                final int attachedId = requireNumber(req, AJAXServlet.PARAMETER_ATTACHEDID);
                final int moduleId = requireNumber(req, AJAXServlet.PARAMETER_MODULE);

				final AttachmentField[] columns = PARSER.getColumns(req.getParameterValues(AJAXServlet.PARAMETER_COLUMNS));
				
				AttachmentField sort = null;
				if(null != req.getParameter(AJAXServlet.PARAMETER_SORT)) {
					sort = AttachmentField.get(Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_SORT)));
				}
				
				int order = AttachmentBase.ASC;
				if("DESC".equalsIgnoreCase(req.getParameter(AJAXServlet.PARAMETER_ORDER))) {
					order = AttachmentBase.DESC;
				}
				all(folderId,attachedId,moduleId,columns,sort,order);
				return true;
			} else  if (AJAXServlet.ACTION_DETACH.equals(action) || AJAXServlet.ACTION_LIST.equals(action)) {
				if (!checkRequired(req, AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_MODULE, AJAXServlet.PARAMETER_ATTACHEDID)) {
					return true;
				}
                final int folderId = requireNumber(req, AJAXServlet.PARAMETER_FOLDERID);
                final int attachedId = requireNumber(req, AJAXServlet.PARAMETER_ATTACHEDID);
                final int moduleId = requireNumber(req, AJAXServlet.PARAMETER_MODULE);

                final JSONArray idsArray = (JSONArray) req.getBody();
				
				int[] ids = new int[idsArray.length()];
				for(int i = 0; i < idsArray.length(); i++) {
					try {
						ids[i] = idsArray.getInt(i);
					} catch (JSONException e) {
						try {
							ids[i] = Integer.parseInt(idsArray.getString(i));
						} catch (NumberFormatException e1) {
							handle(e1);
						} catch (JSONException e1) {
							handle(e1);
						}
					}
				}
				
				if(AJAXServlet.ACTION_DETACH.equals(action)) {
					detach(folderId, attachedId, moduleId, ids);
				} else {
					final AttachmentField[] columns = PARSER.getColumns(req.getParameterValues(AJAXServlet.PARAMETER_COLUMNS));
					list(folderId,attachedId,moduleId,ids,columns);
				}
				return true;
			}
		}
		/*catch (IOException x) {
			LOG.info("Lost contact to client: ",x);
		}*/
		catch (UnknownColumnException e) {
			handle(e);
		} catch (OXAborted x) {
            return true;
        }
		
		return false;
	}

    private int requireNumber(SimpleRequest req, String parameter) {
        String value = req.getParameter(parameter);
        try {
            return Integer.parseInt(value);            
        } catch(NumberFormatException  nfe) {
            numberError(parameter, value);
            throw new OXAborted();
        }
    }

    @OXThrows(category = AbstractOXException.Category.CODE_ERROR, desc = "", exceptionId = 1, msg = "Invalid parameter sent in request. Parameter '%s' was '%s' which does not look like a number")
    public void numberError(String parameter, String value) {
        AttachmentException t = EXCEPTIONS.create(1, parameter, value);
        handle(t);
    }

    // Actions
	
	private void get(final int folderId, final int attachedId, final int moduleId, final int id) {
		try {
			ATTACHMENT_BASE.startTransaction();
			
			final AttachmentMetadata attachment = ATTACHMENT_BASE.getAttachment(folderId,attachedId,moduleId,id,ctx,user,userConfig);
			
			final AttachmentWriter aWriter = new AttachmentWriter(w);
			aWriter.timedResult(attachment.getCreationDate().getTime());
			aWriter.write(attachment, TimeZone.getTimeZone(user.getTimeZone()));
			aWriter.endTimedResult();
			
			ATTACHMENT_BASE.commit();
		} catch (Throwable t) {
			try {
				ATTACHMENT_BASE.rollback();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
			handle(t);
		} finally {
			try {
				ATTACHMENT_BASE.finish();
			} catch (TransactionException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
	
	
	private void updates(final int folderId, final int attachedId, final int moduleId, final long ts, final boolean ignoreDeleted, final AttachmentField[] fields, final AttachmentField sort, final int order) {
		
		SearchIterator iter = null;
		SearchIterator iter2 = null;
		
		try {
			ATTACHMENT_BASE.startTransaction();
			Delta delta;
			if(sort != null) {
				delta = ATTACHMENT_BASE.getDelta(folderId,attachedId,moduleId,ts,ignoreDeleted,fields,sort,order,ctx,user,userConfig);
			} else {
				delta = ATTACHMENT_BASE.getDelta(folderId,attachedId,moduleId,ts,ignoreDeleted,ctx,user,userConfig);
			}
			iter = delta.results();
			iter2 = delta.getDeleted();
			
			final AttachmentWriter aWriter = new AttachmentWriter(w);
			aWriter.timedResult(delta.sequenceNumber());
			aWriter.writeDelta(iter, iter2, fields,ignoreDeleted, TimeZone.getTimeZone(user.getTimeZone()));
			aWriter.endTimedResult();
			//w.flush();
			ATTACHMENT_BASE.commit();
		} catch (Throwable t) {
			try {
				ATTACHMENT_BASE.rollback();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
			handle(t);
		} finally {
			try {
				ATTACHMENT_BASE.finish();
			} catch (TransactionException e) {
				LOG.error(e.getMessage(), e);
			}
			if (iter!=null) {
				try {
					iter.close();
				} catch (SearchIteratorException e1) {
					LOG.error(e1.getMessage(), e1);
				}
			}
			if (iter2!=null) {
				try {
					iter2.close();
				} catch (SearchIteratorException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}
	
	private void all(final int folderId, final int attachedId, final int moduleId, final AttachmentField[] fields, final AttachmentField sort, final int order) {
		
		SearchIterator iter = null;
		
		try {
			ATTACHMENT_BASE.startTransaction();
			TimedResult result;
			if(sort != null) {
				result = ATTACHMENT_BASE.getAttachments(folderId,attachedId,moduleId,fields,sort,order,ctx,user,userConfig);
			} else {
				result = ATTACHMENT_BASE.getAttachments(folderId,attachedId,moduleId,ctx,user,userConfig);
			}
			iter = result.results();
			final AttachmentWriter aWriter = new AttachmentWriter(w);
			aWriter.timedResult(result.sequenceNumber());
			aWriter.writeAttachments(iter,fields,TimeZone.getTimeZone(user.getTimeZone()));
			aWriter.endTimedResult();
			//w.flush();
			ATTACHMENT_BASE.commit();
		} catch (Throwable t) {
			try {
				ATTACHMENT_BASE.rollback();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
			handle(t);
		} finally {
			try {
				ATTACHMENT_BASE.finish();
			} catch (TransactionException e) {
				LOG.error(e.getMessage(), e);
			}
			if (iter!=null) {
				try {
					iter.close();
				} catch (SearchIteratorException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}
	
	private void detach(final int folderId, final int attachedId, final int moduleId, final int[] ids) {
		long timestamp = 0;
		try {
			ATTACHMENT_BASE.startTransaction();
			
			timestamp = ATTACHMENT_BASE.detachFromObject(folderId,attachedId,moduleId,ids,ctx,user,userConfig);
			
			ATTACHMENT_BASE.commit();
		} catch (Throwable t) {
			try {
				ATTACHMENT_BASE.rollback();
			} catch (TransactionException e) {
				LOG.debug("",e);
			}
			handle(t);
			return;
		} finally {
			try {
				ATTACHMENT_BASE.finish();
			} catch (TransactionException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		
		final Response resp = new Response();
		resp.setData("");
		resp.setTimestamp(new Date(timestamp));
		try {
			Response.write(resp, w);
		} catch (final JSONException e) {
			LOG.debug("Cannot contact client",e);
		}
	}
	
	private void list(final int folderId, final int attachedId, final int moduleId, final int[] ids, final AttachmentField[] fields) {
		
		SearchIterator iter = null;
		
		try {
			ATTACHMENT_BASE.startTransaction();
			
			final TimedResult result = ATTACHMENT_BASE.getAttachments(folderId,attachedId,moduleId,ids, fields, ctx,user,userConfig);
			
			iter=result.results();
			
			final AttachmentWriter aWriter = new AttachmentWriter(w);
			aWriter.timedResult(result.sequenceNumber());
			aWriter.writeAttachments(iter,fields,TimeZone.getTimeZone(user.getTimeZone()));
			aWriter.endTimedResult();
			//w.flush();
			
			ATTACHMENT_BASE.commit();
		} catch (Throwable t) {
			try {
				ATTACHMENT_BASE.rollback();
			} catch (TransactionException e) {
				LOG.error(e.getMessage(), e);
			}
			handle(t);
		} finally {
			try {
				ATTACHMENT_BASE.finish();
			} catch (TransactionException e) {
				LOG.error(e.getMessage(), e);
			}
			
			if (iter!=null) {
				try {
					iter.close();
				} catch (SearchIteratorException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}
}
