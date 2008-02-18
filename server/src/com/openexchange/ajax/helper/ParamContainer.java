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

package com.openexchange.ajax.helper;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * ParamContainer
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public abstract class ParamContainer {

	private static interface ErrorInfo {

		public String getMissingParamMsg();

		public Category getMissingParamCategory();

		public int getMissingParamNum();

		public String getBadParamMsg();

		public Category getBadParamCategory();

		public int getBadParamNum();
	}

	private static final ErrorInfo FOLDER_ERR_INFO = new ErrorInfo() {
		public Category getBadParamCategory() {
			return FolderCode.BAD_PARAM_VALUE.getCategory();
		}

		public String getBadParamMsg() {
			return FolderCode.BAD_PARAM_VALUE.getMessage();
		}

		public int getBadParamNum() {
			return FolderCode.BAD_PARAM_VALUE.getNumber();
		}

		public Category getMissingParamCategory() {
			return FolderCode.MISSING_PARAMETER.getCategory();
		}

		public String getMissingParamMsg() {
			return FolderCode.MISSING_PARAMETER.getMessage();
		}

		public int getMissingParamNum() {
			return FolderCode.MISSING_PARAMETER.getNumber();
		}
	};

	private static final ErrorInfo MAIL_ERR_INFO = new ErrorInfo() {
		public Category getBadParamCategory() {
			return com.openexchange.mail.MailException.Code.BAD_PARAM_VALUE.getCategory();
		}

		public String getBadParamMsg() {
			return com.openexchange.mail.MailException.Code.BAD_PARAM_VALUE.getMessage();
		}

		public int getBadParamNum() {
			return com.openexchange.mail.MailException.Code.BAD_PARAM_VALUE.getNumber();
		}

		public Category getMissingParamCategory() {
			return com.openexchange.mail.MailException.Code.MISSING_PARAM.getCategory();
		}

		public String getMissingParamMsg() {
			return com.openexchange.mail.MailException.Code.MISSING_PARAM.getMessage();
		}

		public int getMissingParamNum() {
			return com.openexchange.mail.MailException.Code.MISSING_PARAM.getNumber();
		}
	};

	private static final ErrorInfo DEFAULT_ERR_INFO = new ErrorInfo() {
		public Category getBadParamCategory() {
			return ParamContainerException.Code.BAD_PARAM_VALUE.getCategory();
		}

		public String getBadParamMsg() {
			return ParamContainerException.Code.BAD_PARAM_VALUE.getMessage();
		}

		public int getBadParamNum() {
			return ParamContainerException.Code.BAD_PARAM_VALUE.getNumber();
		}

		public Category getMissingParamCategory() {
			return ParamContainerException.Code.MISSING_PARAMETER.getCategory();
		}

		public String getMissingParamMsg() {
			return ParamContainerException.Code.MISSING_PARAMETER.getMessage();
		}

		public int getMissingParamNum() {
			return ParamContainerException.Code.MISSING_PARAMETER.getNumber();
		}
	};

	private static final ErrorInfo getErrorInfo(final Component component) {
		switch (component) {
		case FOLDER:
			return FOLDER_ERR_INFO;
		case MAIL:
			return MAIL_ERR_INFO;
		default:
			return DEFAULT_ERR_INFO;
		}
	}

	private static class HttpParamContainer extends ParamContainer {

		private final HttpServletRequest req;

		private final Component component;

		private final HttpServletResponse resp;

		private final ErrorInfo errorInfo;

		/**
		 * @param req
		 * @param component
		 * @param resp
		 */
		public HttpParamContainer(final HttpServletRequest req, final Component component,
				final HttpServletResponse resp) {
			this.req = req;
			this.component = component;
			this.resp = resp;
			errorInfo = getErrorInfo(component);
		}

		@Override
		public Date checkDateParam(final String paramName) throws AbstractOXException {
			final String tmp = req.getParameter(paramName);
			if (tmp == null) {
				throw new ParamContainerException(component, errorInfo.getMissingParamCategory(), errorInfo
						.getMissingParamNum(), errorInfo.getMissingParamMsg(), null, paramName);
			}
			try {
				return new Date(Long.parseLong(tmp));
			} catch (final NumberFormatException e) {
				throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
						.getBadParamNum(), errorInfo.getBadParamMsg(), null, tmp, paramName);
			}
		}

		@Override
		public int[] checkIntArrayParam(final String paramName) throws AbstractOXException {
			String tmp = req.getParameter(paramName);
			if (tmp == null) {
				throw new ParamContainerException(component, errorInfo.getMissingParamCategory(), errorInfo
						.getMissingParamNum(), errorInfo.getMissingParamMsg(), null, paramName);
			}
			final String[] sa = tmp.split(SPLIT_PAT);
			tmp = null;
			final int intArray[] = new int[sa.length];
			for (int a = 0; a < sa.length; a++) {
				try {
					intArray[a] = Integer.parseInt(sa[a]);
				} catch (final NumberFormatException e) {
					throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
							.getBadParamNum(), errorInfo.getBadParamMsg(), null, tmp, paramName);
				}
			}
			return intArray;
		}

		@Override
		public int checkIntParam(final String paramName) throws AbstractOXException {
			final String tmp = req.getParameter(paramName);
			if (tmp == null) {
				throw new ParamContainerException(component, errorInfo.getMissingParamCategory(), errorInfo
						.getMissingParamNum(), errorInfo.getMissingParamMsg(), null, paramName);
			}
			try {
				return Integer.parseInt(tmp);
			} catch (final NumberFormatException e) {
				throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
						.getBadParamNum(), errorInfo.getBadParamMsg(), null, tmp, paramName);
			}
		}

		@Override
		public String checkStringParam(final String paramName) throws AbstractOXException {
			final String tmp = req.getParameter(paramName);
			if (tmp == null) {
				throw new ParamContainerException(component, errorInfo.getMissingParamCategory(), errorInfo
						.getMissingParamNum(), errorInfo.getMissingParamMsg(), null, paramName);
			}
			return tmp;
		}

		@Override
		public Date getDateParam(final String paramName) throws AbstractOXException {
			final String tmp = req.getParameter(paramName);
			if (tmp == null) {
				return null;
			}
			try {
				return new Date(Long.parseLong(tmp));
			} catch (final NumberFormatException e) {
				throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
						.getBadParamNum(), errorInfo.getBadParamMsg(), null, tmp, paramName);
			}
		}

		@Override
		public String getHeader(final String hdrName) {
			return req.getHeader(hdrName);
		}

		@Override
		public int[] getIntArrayParam(final String paramName) throws AbstractOXException {
			final String tmp = req.getParameter(paramName);
			if (tmp == null) {
				return null;
			}
			final String[] sa = tmp.split(SPLIT_PAT);
			final int intArray[] = new int[sa.length];
			for (int a = 0; a < sa.length; a++) {
				try {
					intArray[a] = Integer.parseInt(sa[a]);
				} catch (final NumberFormatException e) {
					throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
							.getBadParamNum(), errorInfo.getBadParamMsg(), null, tmp, paramName);
				}
			}
			return intArray;
		}

		@Override
		public int getIntParam(final String paramName) throws AbstractOXException {
			final String tmp = req.getParameter(paramName);
			if (tmp == null) {
				return NOT_FOUND;
			}
			try {
				return Integer.parseInt(tmp);
			} catch (final NumberFormatException e) {
				throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
						.getBadParamNum(), errorInfo.getBadParamMsg(), null, tmp, paramName);
			}
		}

		@Override
		public String getStringParam(final String paramName) {
			return req.getParameter(paramName);
		}

		@Override
		public HttpServletResponse getHttpServletResponse() {
			return resp;
		}
	}

	private static class JSONParamContainer extends ParamContainer {

		private final JSONObject jo;

		private final Component component;

		private final ErrorInfo errorInfo;

		/**
		 * @param jo
		 * @param component
		 */
		public JSONParamContainer(final JSONObject jo, final Component component) {
			this.jo = jo;
			this.component = component;
			errorInfo = getErrorInfo(component);
		}

		@Override
		public Date checkDateParam(final String paramName) throws AbstractOXException {
			if (!jo.has(paramName) || jo.isNull(paramName)) {
				throw new ParamContainerException(component, errorInfo.getMissingParamCategory(), errorInfo
						.getMissingParamNum(), errorInfo.getMissingParamMsg(), null, paramName);
			}
			try {
				return new Date(jo.getLong(paramName));
			} catch (final JSONException e) {
				throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
						.getBadParamNum(), errorInfo.getBadParamMsg(), null, jo.opt(paramName), paramName);
			}
		}

		@Override
		public int[] checkIntArrayParam(final String paramName) throws AbstractOXException {
			if (!jo.has(paramName) || jo.isNull(paramName)) {
				throw new ParamContainerException(component, errorInfo.getMissingParamCategory(), errorInfo
						.getMissingParamNum(), errorInfo.getMissingParamMsg(), null, paramName);
			}
			String[] tmp;
			try {
				tmp = jo.getString(paramName).split(SPLIT_PAT);
			} catch (final JSONException e1) {
				throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
						.getBadParamNum(), errorInfo.getBadParamMsg(), null, jo.opt(paramName), paramName);
			}
			final int[] intArray = new int[tmp.length];
			for (int i = 0; i < tmp.length; i++) {
				try {
					intArray[i] = Integer.parseInt(tmp[i]);
				} catch (final NumberFormatException e) {
					throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
							.getBadParamNum(), errorInfo.getBadParamMsg(), null, jo.opt(paramName), paramName);
				}
			}
			return intArray;
		}

		@Override
		public int checkIntParam(final String paramName) throws AbstractOXException {
			if (!jo.has(paramName) || jo.isNull(paramName)) {
				throw new ParamContainerException(component, errorInfo.getMissingParamCategory(), errorInfo
						.getMissingParamNum(), errorInfo.getMissingParamMsg(), null, paramName);
			}
			try {
				return jo.getInt(paramName);
			} catch (final JSONException e) {
				throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
						.getBadParamNum(), errorInfo.getBadParamMsg(), null, jo.opt(paramName), paramName);
			}
		}

		@Override
		public String checkStringParam(final String paramName) throws AbstractOXException {
			if (!jo.has(paramName) || jo.isNull(paramName)) {
				throw new ParamContainerException(component, errorInfo.getMissingParamCategory(), errorInfo
						.getMissingParamNum(), errorInfo.getMissingParamMsg(), null, paramName);
			}
			try {
				return jo.getString(paramName);
			} catch (final JSONException e) {
				throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
						.getBadParamNum(), errorInfo.getBadParamMsg(), null, jo.opt(paramName), paramName);
			}
		}

		@Override
		public Date getDateParam(final String paramName) throws AbstractOXException {
			if (!jo.has(paramName) || jo.isNull(paramName)) {
				return null;
			}
			try {
				return new Date(jo.getLong(paramName));
			} catch (final JSONException e) {
				throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
						.getBadParamNum(), errorInfo.getBadParamMsg(), null, jo.opt(paramName), paramName);
			}
		}

		@Override
		public String getHeader(final String hdrName) {
			return null;
		}

		@Override
		public int[] getIntArrayParam(final String paramName) throws AbstractOXException {
			if (!jo.has(paramName) || jo.isNull(paramName)) {
				return null;
			}
			String[] tmp;
			try {
				tmp = jo.getString(paramName).split(SPLIT_PAT);
			} catch (final JSONException e1) {
				throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
						.getBadParamNum(), errorInfo.getBadParamMsg(), null, jo.opt(paramName), paramName);
			}
			final int[] intArray = new int[tmp.length];
			for (int i = 0; i < tmp.length; i++) {
				try {
					intArray[i] = Integer.parseInt(tmp[i]);
				} catch (final NumberFormatException e) {
					throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
							.getBadParamNum(), errorInfo.getBadParamMsg(), null, jo.opt(paramName), paramName);
				}
			}
			return intArray;
		}

		@Override
		public int getIntParam(final String paramName) throws AbstractOXException {
			if (!jo.has(paramName) || jo.isNull(paramName)) {
				return NOT_FOUND;
			}
			try {
				return jo.getInt(paramName);
			} catch (final JSONException e) {
				throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
						.getBadParamNum(), errorInfo.getBadParamMsg(), null, jo.opt(paramName), paramName);
			}
		}

		@Override
		public String getStringParam(final String paramName) throws AbstractOXException {
			if (!jo.has(paramName) || jo.isNull(paramName)) {
				return null;
			}
			try {
				return jo.getString(paramName);
			} catch (final JSONException e) {
				throw new ParamContainerException(component, errorInfo.getBadParamCategory(), errorInfo
						.getBadParamNum(), errorInfo.getBadParamMsg(), null, jo.opt(paramName), paramName);
			}
		}

		@Override
		public HttpServletResponse getHttpServletResponse() {
			return null;
		}
	}

	private static final String SPLIT_PAT = " *, *";

	public static final int NOT_FOUND = -9999;

	public static ParamContainer getInstance(final HttpServletRequest req, final Component component,
			final HttpServletResponse resp) {
		return new HttpParamContainer(req, component, resp);
	}

	public static ParamContainer getInstance(final JSONObject jo, final Component component) {
		return new JSONParamContainer(jo, component);
	}

	/**
	 * Gets a parameter as String
	 * 
	 * @param paramName -
	 *            the parameter name
	 * @return parameter value as <code>String</code> or <code>null</code>
	 *         if not found
	 * @throws AbstractOXException
	 */
	public abstract String getStringParam(String paramName) throws AbstractOXException;

	/**
	 * Requires a parameter as <code>String</code>
	 * 
	 * @param paramName -
	 *            the parameter name
	 * @return parameter value as <code>String</code>
	 * @throws AbstractOXException
	 *             if parameter could not be found
	 */
	public abstract String checkStringParam(String paramName) throws AbstractOXException;

	/**
	 * Gets a parameter as <code>int</code>
	 * 
	 * @param paramName -
	 *            the parameter name
	 * @return parameter value as <code>int</code> or constant
	 *         <code>NOT_FOUND</code> if not found
	 * @throws AbstractOXException
	 */
	public abstract int getIntParam(String paramName) throws AbstractOXException;

	/**
	 * Requires a paramater as <code>int</code>
	 * 
	 * @param paramName -
	 *            the parameter name
	 * @return parameter value as <code>int</code>
	 * @throws AbstractOXException
	 *             if parameter could not be found
	 */
	public abstract int checkIntParam(String paramName) throws AbstractOXException;

	/**
	 * Gets a parameter as an array of <code>int</code>
	 * 
	 * @param paramName -
	 *            the parameter name
	 * @return parameter value as an array of <code>int</code> or
	 *         <code>null</code> if not found
	 * @throws AbstractOXException
	 */
	public abstract int[] getIntArrayParam(String paramName) throws AbstractOXException;

	/**
	 * Requires a parameter as an array of <code>int</code>
	 * 
	 * @param paramName -
	 *            the parameter name
	 * @return parameter value as an array of <code>int</code>
	 * @throws AbstractOXException
	 *             if parameter could not be found
	 */
	public abstract int[] checkIntArrayParam(String paramName) throws AbstractOXException;

	/**
	 * Gets a parameter as a <code>java.util.Date</code>
	 * 
	 * @param paramName -
	 *            the parameter name
	 * @return parameter value as an array of <code>java.util.Date</code> or
	 *         <code>null</code> if not found
	 * @throws AbstractOXException
	 */
	public abstract Date getDateParam(String paramName) throws AbstractOXException;

	/**
	 * Requires a parameter as a <code>java.util.Date</code>
	 * 
	 * @param paramName -
	 *            the parameter name
	 * @return parameter value as <code>java.util.Date</code>
	 * @throws AbstractOXException
	 *             if parameter could not be found
	 */
	public abstract Date checkDateParam(String paramName) throws AbstractOXException;

	/**
	 * Gets a header
	 * 
	 * @param hdrName -
	 *            the header name
	 * @return the header as <code>String</code> or <code>null</code> if not
	 *         found
	 */
	public abstract String getHeader(String hdrName);

	/**
	 * Gets the <code>javax.servlet.http.HttpServletResponse</code> instance
	 * 
	 * @return the <code>javax.servlet.http.HttpServletResponse</code>
	 *         instance if present; otherwise <code>null</code>
	 */
	public abstract HttpServletResponse getHttpServletResponse();

}
