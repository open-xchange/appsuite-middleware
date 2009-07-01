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

package com.openexchange.easylogin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.configuration.ConfigurationException;



/**
 * {@link EasyLogin}
 * TODO: Fix configuration loading with ConfigurationService
 *       Put javascript line in external file and load it from file instead hardcoded in servlet
 *       
 * @author <a href="mailto:info@open-xchange.com">Holger Achtziger</a>
 * 
 */
public class EasyLogin extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7233346063627500582L;
	private static final Log LOG = LogFactory.getLog(EasyLogin.class);
	private static Properties props;
    private final static String EASYLOGIN_PROPERTY_FILE = "/opt/open-xchange/etc/groupware/easylogin.properties";
	
	private static final String RESPONSE1 = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" +
			"	\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
			"<head>\n" +
			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
			"<script type=\"text/javascript\">\n" +
			"var emptyFunction = function (){};\n" +
			"var _=function (ss){return ss;};\n" +
			"var format = function (ss){return ss;};\n" +
			"var AjaxRoot = \"";
	
	private static final String RESPONSE2 = "\";\n" +
			"\n" +
			"function JSON() {\n" +
			"	/**\n" +
			"	 * @private\n" +
			"	 */\n" +
			"	this.first = null;\n" +
			"	\n" +
			"	/**\n" +
			"	 * @private\n" +
			"	 */\n" +
			"	this.last = null;\n" +
			"	\n" +
			"	/**\n" +
			"	 * @private\n" +
			"	 */\n" +
			"	this.processing = false;\n" +
			"}\n" +
			"\n" +
			"JSON.serialize = function(data) {\n" +
			"	if (typeof(data) == \"string\")\n" +
			"		return \"\\\"\" + data.replace(/[\\x00-\\x1f\\\\\"]/g, function(c) {\n" +
			"			var n = Number(c.charCodeAt(0)).toString(16);\n" +
			"			return \"\\\\u00\" + (n.length < 2 ? \"0\" + n : n);\n" +
			"		}) + \"\\\"\";\n" +
			"	if (typeof(data) == \"function\") return \"function\";\n" +
			"	if (!data || typeof(data) !== \"object\") return String(data);\n" +
			"	var strings = new Array(data.length);\n" +
			"	if (data.constructor == Array) {\n" +
			"		for (var i in data) strings[i] = JSON.serialize(data[i]);\n" +
			"		return \"[\" + strings.join() + \"]\";\n" +
			"	}\n" +
			"	var j = 0;\n" +
			"	for (var i in data) strings[j++] = \"\\\"\" + i + \"\\\":\" + JSON.serialize(data[i]);\n" +
			"	return \"{\" + strings.join() + \"}\";\n" +
			"}\n" +
			"\n" +
			"JSON.count = 0;\n" +
			"\n" +
			"JSON.prototype = {\n" +
			"	\n" +
			"	get: function(uri, meta, cb, errorHandler, raw) {\n" +
			"		var request = {\n" +
			"			method: \"GET\",\n" +
			"			uri: uri,\n" +
			"			data: \"\",\n" +
			"			cb: cb,\n" +
			"			errorHandler: errorHandler,\n" +
			"			raw: raw,\n" +
			"			next: null\n" +
			"		};\n" +
			"		this.add(request);\n" +
			"		return request;\n" +
			"	},\n" +
			"	\n" +
			"	\n" +
			"	post: function(uri, data, meta, cb, errorHandler, raw) {\n" +
			"		var encoded = new Array(), n = 0;\n" +
			"		for (var i in data)\n" +
			"			encoded[n++] = i + \"=\" + encodeURIComponent(data[i]);\n" +
			"		var request = {\n" +
			"			method: \"POST\",\n" +
			"			uri: uri,\n" +
			"			data: encoded.join(\"&\"),\n" +
			"			contenttype: \"application/x-www-form-urlencoded\",\n" +
			"			cb: cb,\n" +
			"			errorHandler: errorHandler,\n" +
			"			raw: raw,\n" +
			"			next: null\n" +
			"		};\n" +
			"		this.add(request);\n" +
			"		return request;\n" +
			"	},\n" +
			"	\n" +
			"	\n" +
			"	put: function(uri, data, meta, cb, errorHandler, raw) {\n" +
			"		var request = {\n" +
			"			method: \"PUT\",\n" +
			"			uri: uri,\n" +
			"			contenttype: \"text/javascript; charset=UTF-8\",\n" +
			"			data: JSON.serialize(data),\n" +
			"			cb: cb,\n" +
			"			errorHandler: errorHandler,\n" +
			"			raw: raw,\n" +
			"			next: null\n" +
			"		};\n" +
			"		this.add(request);\n" +
			"		return request;\n" +
			"	},\n" +
			"	\n" +
			"	\n" +
			"	cancel: function(request) {\n" +
			"		if (request == this.first) {\n" +
			"			request.cancelled = true;\n" +
			"			return false;\n" +
			"		}\n" +
			"		for (var r = this.first; r; r = r.next)\n" +
			"			if (request == r.next) {\n" +
			"				r.next = request.next;\n" +
			"				return true;\n" +
			"			}\n" +
			"		return false;\n" +
			"	},\n" +
			"\n" +
			"	/**\n" +
			"	 * @private\n" +
			"	 */\n" +
			"	remove: function() {\n" +
			"		if (this.first) {\n" +
			"			if (this.last == this.first) this.last = null;\n" +
			"			this.first = this.first.next;\n" +
			"		}\n" +
			"	},\n" +
			"\n" +
			"	/**\n" +
			"	 * @private\n" +
			"	 */\n" +
			"	add: function(request) {\n" +
			"		if (!this.first)\n" +
			"			this.last = this.first = request;\n" +
			"		else\n" +
			"			this.last = this.last.next = request;\n" +
			"		if (!this.processing) this.process();\n" +
			"	},\n" +
			"\n" +
			"	/**\n" +
			"	 * @private\n" +
			"	 */\n" +
			"	process: function() {\n" +
			"		JSON.count++;\n" +
			"		if (!(this.processing = this.first != null)) {\n" +
			"			return;\n" +
			"		}\n" +
			"		var xmlhttp = this.getXmlHttp();\n" +
			"		var Self = this;\n" +
			"		xmlhttp.onreadystatechange = callback;\n" +
			"		xmlhttp.open(this.first.method, this.first.uri, true);\n" +
			"		if (this.first.contenttype)\n" +
			"			xmlhttp.setRequestHeader(\"Content-Type\", this.first.contenttype);\n" +
			"		xmlhttp.send(this.first.data);\n" +
			"		\n" +
			"		function callback() {\n" +
			"			if (xmlhttp.readyState != 4) return;\n" +
			"			JSON.count--;\n" +
			"			xmlhttp.onreadystatechange = emptyFunction; // fixes IE memory leak\n" +
			"			var cb = Self.first.cb;\n" +
			"			var originalErrorHandler = Self.first.errorHandler;\n" +
			"			var errorHandler = originalErrorHandler ? function(result, status) {\n" +
			"				if (!originalErrorHandler(result, status))\n" +
			"					JSON.errorHandler(result, status);\n" +
			"			} : JSON.errorHandler;\n" +
			"			var raw = Self.first.raw;\n" +
			"			Self.remove();\n" +
			"			var result = {};\n" +
			"			if (xmlhttp.status != 200) {\n" +
			"				errorHandler(xmlhttp.statusText, xmlhttp.status);\n" +
			"				Self.process();\n" +
			"				return;\n" +
			"			}\n" +
			"			if (raw)\n" +
			"				result = xmlhttp.responseText;\n" +
			"			else {\n" +
			"				var s = \"return \" + xmlhttp.responseText;\n" +
			"				try {\n" +
			"					result = Function(s)();\n" +
			"				} catch (e) {\n" +
			"					//#. %s is the JavaScript error message.\n" +
			"					//#, c-format\n" +
			"					alert(format(_(\"Syntax error in server reply:\\n%s\"), e.message, s));\n" +
			"					Self.process();\n" +
			"					return;\n" +
			"				}\n" +
			"				if (result && typeof(result) == \"object\" && result.error) {\n" +
			"					errorHandler(result);\n" +
			"					Self.process();\n" +
			"					return;\n" +
			"				}\n" +
			"			}\n" +
			"			//try {\n" +
			"				cb(result);\n" +
			"			/*} catch(e) {\n" +
			"				Self.process();\n" +
			"				throw e;\n" +
			"			}*/\n" +
			"			Self.process();\n" +
			"		};\n" +
			"	},\n" +
			"	\n" +
			"	/**\n" +
			"	 * @private\n" +
			"	 */\n" +
			"	getXmlHttp: function() {\n" +
			"		alert(_(\"Your browser does not support AJAX.\"));\n" +
			"	}\n" +
			"};\n" +
			"\n" +
			"JSON.errorHandler = function(result, status) {\n" +
			"	if (status)\n" +
			"		//#. HTTP Errors from the server\n" +
			"		//#. %1$s is the numeric HTTP status code\n" +
			"		//#. %2$s is the corresponding HTTP status text\n" +
			"		alert(\"Error: \"+status+\" - \"+result);\n" +
			"	else\n" +
			"		alert(formatError(result));\n" +
			"};\n" +
			"\n" +
			"(function() {\n" +
			"	var xmlhttp = null;\n" +
			"	try {\n" +
			"		xmlhttp = new XMLHttpRequest();\n" +
			"		if (xmlhttp) {\n" +
			"			xmlhttp = null;\n" +
			"			JSON.prototype.getXmlHttp = function() { return new XMLHttpRequest(); };\n" +
			"		}\n" +
			"	} catch (e) {\n" +
			"		try {\n" +
			"			xmlhttp = new ActiveXObject(\"Msxml2.XMLHTTP\");\n" +
			"			if (xmlhttp) {\n" +
			"				xmlhttp = null;\n" +
			"				JSON.prototype.getXmlHttp = function() {\n" +
			"					return new ActiveXObject(\"Msxml2.XMLHTTP\");\n" +
			"				};\n" +
			"			}\n" +
			"		} catch (e) {\n" +
			"			try {\n" +
			"				xmlhttp = new ActiveXObject(\"Microsoft.XMLHTTP\");\n" +
			"				if (xmlhttp) {\n" +
			"					xmlhttp = null;\n" +
			"					JSON.prototype.getXmlHttp = function() {\n" +
			"						return new ActiveXObject(\"Microsoft.XMLHTTP\");\n" +
			"					};\n" +
			"				}\n" +
			"			} catch (e) {\n" +
			"				JSON.prototype.getXmlHttp();\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"})();\n" +
			"\n" +
			"function login(u,p) {	\n" +
			"	var form = document.getElementById(\"login\");	\n" +
			"	new JSON().post(\n" +
			"		AjaxRoot + \"/login?action=login\", \n" +
			"		{ name: u, password: p },\n" +
			"		null,\n" +
			"		function(result) { dologin(result); },\n" +
			"		function(result, status) {\n" +
			"			if (!status) {\n" +
			"				if (result.code == \"LGI-0006\"){\n";
			
	private static final String RESPONSE23 =	"alert(_(\"Login failed. Please check your user name and password and try again.\"));\n";
			
	private static final String RESPONSE24 =
			"			    } else\n" +
			"				//#. HTTP Errors from the server\n" +
			"				//#. %1$s is the numeric HTTP status code\n" +
			"				//#. %2$s is the corresponding HTTP status text\n";
			
	private static final String RESPONSE25 =	"alert(\"Error: \"+status+\" - \"+result);\n";
		
	private static final String REDIRECT_BY_REFERRER =	
			"			window.location.href = document.referrer + \"?login=failed&user=\" + u;\n" +
			"			return true;\n}\n" ;
	
	private static final String REDIRECT_BASE =	
		"			window.location.href = location.protocol+\"//\"+location.host;\n" +
		"			return true;\n}\n" ;
		
		
	private String getCustomRedirectURL(String url){
		return "window.location.href = \""+url+"\";\n" +
				"return true;\n}\n";
	}
			
	private static final String RESPONSE28 =			
			"		},\n" +
			"		null\n" +
			"	);\n" +
			"	return false;\n" +
			"}\n" +
			" function authenticate (code,user,pass,method){\n" +
			// remove all cookies first browser session to not mix up session
			" // get all cookies from ox\n"+
			" var cookies = document.cookie.match(/open-xchange-session-\\w+/g);\n"+
			" // check if we have ox cookies\n"+
			" if(cookies){\n"+
			" // get date object\n" + 
			" var mydate = new Date();\n"+
			" mydate.setTime(mydate.getTime() - 100000);\n"+ 
			" for (var i = 0 ; i < cookies.length; i++) {\n" +
			"  // invalidate all ox cookies\n"+
			"  document.cookie = cookies[i]+\"=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/\" \n"+  
			" }\n"+
		    "}\n"+
		    "\n"+
			"	if(code != 0)\n" +
			"	{\n" +
			"		alert(_(\"Login failed. Please check your user name and password and try again.\"));\n" +
			"		return;\n" +
			"	}\n" +
			"	login(user,pass);\n" +
			"}\n" +
			"\n" +
			"//redirect to ox\n" +
			"function dologin(result) {\n" +
			"	document.location.href=\"";
	
	private static final String RESPONSE3 = "\";\n}\n" +
			"\n" +
			"function onload_fn()\n" +
			"{\n";
	
	private static final String RESPONSE4 = "\n}\n" +
			"</script>\n" +
			"</head>\n" +
			"<html>\n" +
			"<body onload=\"onload_fn()\">\n" +
			"</body>\n" +
			"</html>\n" +
			"\n";
	
	private static String AJAX_ROOT = "/ajax";
	private static String passwordPara = "password";
	private static String loginPara ="login";
	private static String redirPara ="redirect"; // param for what should be done after error on login
	private static String directLinkPara ="direct_link";
	private static  String OX_PATH_RELATIVE = "../";
	private static boolean doGetEnabled = false;
	private static boolean popUpOnError = true;
	
	/**
	 * Initializes a new {@link EasyLogin}
	 */
	public EasyLogin() {
		super();
	}
	
	protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
	IOException {
		
		processLoginRequest(req,resp);
		
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		
		try {
			if (props == null) {
				initConfig();
			}
		}catch (ConfigurationException e) {
            LOG.error("Error processing easylogin configuration" + EASYLOGIN_PROPERTY_FILE + " ", e);
        }
		
		if( !doGetEnabled ){
			// show error to user
			resp.sendError( HttpServletResponse.SC_METHOD_NOT_ALLOWED , "GET not supported");
		}else{
			processLoginRequest(req,resp);
		}	
		
	}
	
	private void processLoginRequest(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,IOException{
		
		try {
			if (props == null) {
				initConfig();
			}
		} catch (ConfigurationException e) {
            LOG.error("Error processing easylogin configuration" + EASYLOGIN_PROPERTY_FILE + " ", e);
        }
		
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		
		String login = null;
		String password = null;
		
		
				
		if (req.getParameter(passwordPara)==null || req.getParameter(passwordPara).trim().length()==0){
			resp.sendError( HttpServletResponse.SC_BAD_REQUEST , "parameter " + passwordPara + " missing");
			LOG.error("Got request without password");
		} else if (req.getParameter(loginPara)==null || req.getParameter(loginPara).trim().length()==0){
			resp.sendError( HttpServletResponse.SC_BAD_REQUEST , "parameter " + loginPara + " missing");
			LOG.error("Got request without login");
		} else{
			
			password = req.getParameter(passwordPara);
			login = req.getParameter(loginPara).trim().toLowerCase();
			
			out.print(RESPONSE1);
			out.print(AJAX_ROOT);
			out.print(RESPONSE2);
			
			if( popUpOnError ) {
				out.print(RESPONSE23);
				
				// for normal errors we must also redirect
				if(req.getParameter(redirPara)!=null && req.getParameter(redirPara).trim().length()>0){
					
					// redir param was sent, now check what action is requested
					if(req.getParameter(redirPara).equals("_BASE_")){
						// send javascript redirect to /
						out.print(REDIRECT_BASE);
					}else{
						// custom redirect url was requested, send this URL in javascript to redirect
						out.print(getCustomRedirectURL(req.getParameter(redirPara).toString()));
					}
				}else{
					// no special redirect was requested, do it via referrer
					out.print(REDIRECT_BY_REFERRER); // send redirect via referrer 
				}
			}
			
			out.print(RESPONSE24);
			out.print(RESPONSE25);
			
			// redirect to given action
			if(req.getParameter(redirPara)!=null && req.getParameter(redirPara).trim().length()>0){
				// redir param was sent, now check what action is requested
				if(req.getParameter(redirPara).equals("_BASE_")){
					// send javascript redirect to / 
					out.print(REDIRECT_BASE);
				}else{
					// custom redirect url was requested, send this URL in javascript to redirect
					out.print(getCustomRedirectURL(req.getParameter(redirPara).toString()));
				}
			}else{
				// no special redirect was requested, do it via referrer
				out.print(REDIRECT_BY_REFERRER); // send redirect via referrer 
			}
			
			
			
			
			out.print(RESPONSE28);  
			
			// direct links redirecting
			if(req.getParameter(directLinkPara)!=null && req.getParameter(directLinkPara).trim().length()>0){
				out.print(OX_PATH_RELATIVE+req.getParameter(directLinkPara));
			}else{
				out.print(OX_PATH_RELATIVE);
			}			
			out.print(RESPONSE3);
			out.print("authenticate(0,\"" +
					login +
					"\",\"" +
					password +
					"\",\"" +  
					"get\"" +
					");return;");
			out.print(RESPONSE4);
		}
		
	}
	
	
	private static void initConfig() throws ConfigurationException {
	    synchronized (EasyLogin.class) {
	        if (null == props) {
	            final File file = new File(EASYLOGIN_PROPERTY_FILE);
	            if (!file.exists()) {
	            	LOG.error("Error file not found: " + EASYLOGIN_PROPERTY_FILE);
	            	throw new ConfigurationException(com.openexchange.configuration.ConfigurationException.Code.FILE_NOT_FOUND, file.getAbsolutePath());
	            }
	            FileInputStream fis = null;
	            try {
	                fis = new FileInputStream(file);
	                props = new Properties();
	                props.load(fis);
	                
	                if (props.get("com.openexchange.easylogin.passwordPara") != null) {
	                	passwordPara  = (String) props.get("com.openexchange.easylogin.passwordPara");
	                	LOG.info("Set passwordPara to " + passwordPara );
	                } else {
	                	LOG.error("Could not find passwordPara in " + EASYLOGIN_PROPERTY_FILE + " using default: " + 
	                			passwordPara );
	                }
	                
	                if (props.get("com.openexchange.easylogin.loginPara") != null) {
	                	loginPara = (String) props.get("com.openexchange.easylogin.loginPara");
	                	LOG.info("Set loginPara to " +  loginPara);
	                } else {
	                	LOG.error("Could not find loginPara in " + EASYLOGIN_PROPERTY_FILE + " using default: " + 
	                			loginPara );
	                }
	                
	                if (props.get("com.openexchange.easylogin.AJAX_ROOT") != null) {
	                	AJAX_ROOT = (String) props.get("com.openexchange.easylogin.AJAX_ROOT");
	                	LOG.info("Set AJAX_ROOT to " +  AJAX_ROOT);
	                } else {
	                	LOG.error("Could not find AJAX_ROOT in " + EASYLOGIN_PROPERTY_FILE + " using default: " + 
	                			AJAX_ROOT );
	                }
	                
	                if (props.get("com.openexchange.easylogin.OX_PATH_RELATIVE") != null) {
	                	OX_PATH_RELATIVE = (String) props.get("com.openexchange.easylogin.OX_PATH_RELATIVE");
	                	LOG.info("Set OX_PATH_RELATIVE to " +  OX_PATH_RELATIVE);
	                } else {
	                	LOG.error("Could not find OX_PATH_RELATIVE in " + EASYLOGIN_PROPERTY_FILE + " using default: " + 
	                			OX_PATH_RELATIVE );
	                }
	                
	                if (props.get("com.openexchange.easylogin.doGetEnabled") != null) {
	                	String property = props.getProperty("com.openexchange.easylogin.doGetEnabled","").trim();
	                	doGetEnabled = Boolean.parseBoolean(property);
	                	LOG.info("Set doGetEnabled to " + doGetEnabled );
	                } else {
	                	LOG.error("Could not find doGetEnabled in " + EASYLOGIN_PROPERTY_FILE + " using default: " + 
	                			doGetEnabled );
	                }
	                
	                if (props.get("com.openexchange.easylogin.popUpOnError") != null) {
	                	String property = props.getProperty("com.openexchange.easylogin.popUpOnError","").trim();
	                	popUpOnError = Boolean.parseBoolean(property);
	                	LOG.info("Set popUpOnError to " +  popUpOnError);
	                } else {
	                	LOG.error("Could not find popUpOnError in " + EASYLOGIN_PROPERTY_FILE + " using default: " + 
	                			popUpOnError );
	                }
	                
	            } catch (IOException e) {
	            	LOG.error("Error can't read file: " + EASYLOGIN_PROPERTY_FILE);
	            	throw new ConfigurationException(com.openexchange.configuration.ConfigurationException.Code.NOT_READABLE, file.getAbsolutePath());
	            } finally {
	                try {
	                    fis.close();
	                } catch (IOException e) {
	                    LOG.error("Error closing file inputstream for file " + EASYLOGIN_PROPERTY_FILE + 
	                    		" ", e);
	                }
	            }
	        }
	   	}
	}

}
