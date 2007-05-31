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



package com.openexchange.tools.conf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import com.openexchange.groupware.calendar.CalendarRecurringCollection;
import com.openexchange.server.ComfireLogger;

/**
 * GlobalConfig
 * @author <a href="mailto:martin.kauss@open-xchange.com">Martin Kauss</a>
 * @author <a href="mailto:stefan.preuss@open-xchange.com">Stefan Preuss</a>
 * @author <a href="mailto:ben.pahne@open-xchange.com">Benjamin Frederic Pahne</a>
 * @deprecated Use specialized config classes.
 */
public class GlobalConfig {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(GlobalConfig.class);

    static final String parameterfile = "intranet.conf";
    static final String servletmappingfile = "servletmapping.properties";
    static String CalendarSQLImpl;

    private static final HashMap datePattern = new HashMap();
    private static final HashMap parameters = new HashMap();
    private static final HashMap categories = new HashMap();

    static {
//	datePattern.put("DEFAULT", "dd.MM.yyyy HH:mm");
//	datePattern.put("DATABASE", "yyyy-MM-dd HH:mm");
//	datePattern.put("DE", "dd.MM.yyyy HH:mm");
//	datePattern.put("EN", "MM/dd/yy h:mm a");
	
	// teamview 
//	parameters.put("teamview_max_users", "4");

	// database sequence standard for postgres
//	parameters.put("seq-fid","SELECT nextval ('fid')");
//	parameters.put("seq-import_id","SELECT nextval ('import_id')");
//	parameters.put("seq-insert_id","SELECT nextval ('insert_id')");
//	parameters.put("seq-profile_id","SELECT nextval ('profile_id')");
//	parameters.put("seq-serial_id","SELECT nextval ('serial_id')");
	
	// database format for sysdate - now	
//	parameters.put("SYSDATE", "'now'");	

	// database format for today
//	parameters.put("SQL_TODAY", "'today'");	
	
	// the image scaleing size for contact images x,y	
//	parameters.put("image-scale", "72,72");	

	// week start day
//	parameters.put("WEEK_START_DAY", ""+Calendar.MONDAY);	

	// sql field mapping in webmail (fallback) 
//	parameters.put("sql-fieldmapname-usrdata_delete","delete");
    
	// first letter field for the contact list page
	parameters.put("contact_first_letter_field", "field02");

	// validate the email of a new or changed contacts, true/false
	parameters.put("validate_contact_email", "true");	

//	parameters.put("faxEnabled", "false");
//	parameters.put("mail_check_time", "300");
//	parameters.put("mail_check_includes_subfolder", "false");
//	parameters.put("default_mail_encoding", "UTF-8");
    }

	 
    public static void loadConf(final String propertyfile) {
	if (propertyfile != null) {
	    //ComfireConfig.loadProperties(propertyfile);
	    loadConf();
	} else {
	    ComfireLogger.log("No property file given. Interface will not work!", 0);
	}
    }
    
    public static void loadConf() {
	//loadParametersFromFile(ComfireConfig.properties.getProperty("CONFIGPATH")+"/"+parameterfile);
	//checkReloadTimes();
	//checkCalendarConfig();
	//loadServletMapping(ComfireConfig.properties.getProperty("CONFIGPATH")+"/"+servletmappingfile);
    }


    // This is small copy of loading langCodes method
    public static void loadLangCategories(final String file) {
	final File list = new File(file);
	if ((!(list.exists())) || (!(list.isDirectory()))) {
	    return;
	}

	final String[] allFilesInDir = list.list();
	for (int i = 0; i < allFilesInDir.length; i++) {
	    if ((new File(file + System.getProperty("file.separator") + allFilesInDir[i]).isFile()) &&
		((allFilesInDir[i]).endsWith(".cat")) &&
		((allFilesInDir[i]).length() > 4)) {

		final String language = allFilesInDir[i].substring(0, allFilesInDir[i].lastIndexOf("."));		
		final Vector tp = new Vector();
		boolean encodingerror = false;
		try {
		    final FileInputStream fis = new FileInputStream(file+System.getProperty("file.separator")+allFilesInDir[i]);
		    final InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		    final BufferedReader br = new BufferedReader(isr);
		    
		    String line = "";
		    
		    while ((line = br.readLine()) != null) {
			line = line.trim();			
			if (line.charAt(0) != '#') {
			    tp.addElement(line);
			}		
		    }
		    br.close();
		    isr.close();
		    fis.close();
		} catch (final UnsupportedEncodingException uce) {
		    System.out.println("UnsupportedEncodingException at " + file + System.getProperty("file.separator") + allFilesInDir[i] + " (file is not UTF-8 encoded!)");
		    encodingerror = true;
		} catch (final Exception e) {
		    System.out.println("Exception while parsing dlc file : " + file + System.getProperty("file.separator") + allFilesInDir[i]);
		    e.printStackTrace();
		}

		if (encodingerror) {
		    System.out.println("Starting fallback mode (reading " + file + System.getProperty("file.separator") + allFilesInDir[i] + " with standard encoding)");

		    try {
			final FileReader fr = new FileReader(file+System.getProperty("file.separator")+allFilesInDir[i]);
			final BufferedReader br = new BufferedReader(fr);
			
			String line = "";
			
			while ((line = br.readLine()) != null) {
			    line = line.trim();
			    if (!line.startsWith("#")) {
				tp.addElement(line);
			    }
			}
			br.close();
			fr.close();
		    } catch (final Exception e) {
			System.out.println("Exception while parsing dlc file : " + file + System.getProperty("file.separator") + allFilesInDir[i]);
			e.printStackTrace();
		    }
		}
		Collections.sort(tp);	
		categories.put(language,tp);
	    }
	}
    }

    // loading user layouts
    public static Vector loadUserLayouts(final String file, final String user) {
        final File list = new File(file);
        if ((!(list.exists())) || (!(list.isDirectory()))) {
            return null;
        }

        Vector tp = new Vector();
        final File f = new File(file + System.getProperty("file.separator") + user + ".layout");

        if (f.isFile() && f.length() > 1) {
            boolean encodingerror = false;
            FileInputStream fis = null;
            InputStreamReader isr = null;
            try {
                fis = new FileInputStream(f);
                isr = new InputStreamReader(fis, "UTF-8");
                final BufferedReader br = new BufferedReader(isr);
                String line = "";
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.startsWith("#")) {
                        tp.addElement(line);
                    }
                }
                br.close();
            } catch (final UnsupportedEncodingException uce) {
                ComfireLogger.log("ERROR: LoadUserLayout(...): UnsupportedEncodingException at " + file + System.getProperty("file.separator") + user + ".layout (file is not UTF-8 encoded!)", ComfireLogger.ERROR);
                encodingerror = true;
            } catch (final Exception e) {
                ComfireLogger.log("ERROR: LoadUserLayout(...): Exception while parsing layout file : " + file + System.getProperty("file.separator") + user + ".layout", ComfireLogger.ERROR);
                e.printStackTrace();
            } finally {
                try {
                    if (isr != null) {
						isr.close();
					}
                    if (fis != null) {
						fis.close();
					}
                } catch (final IOException e) {}
            }

            if (encodingerror) {
                ComfireLogger.log("ERROR: LoadUserLayout(...): Starting fallback mode (reading " + file + System.getProperty("file.separator") + user + ".layout with standard encoding)", ComfireLogger.ERROR);
                tp = new Vector();
                FileReader fr = null;
                BufferedReader br = null;
                try {
                    fr = new FileReader(f);
                    br = new BufferedReader(fr);
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (!line.startsWith("#")) {
                            tp.addElement(line);
                        }
                    }
                } catch (final Exception e) {
                    ComfireLogger.log("ERROR: LoadUserLayout(...): Exception while parsing layout file : " + file + System.getProperty("file.separator") + user + ".layout", ComfireLogger.ERROR);
                    e.printStackTrace();
                } finally {
                    try {
                        if (br != null) {
							br.close();
						}
                        if (fr != null) {
							fr.close();
						}
                    } catch (final IOException e) { }
                }
            }
        }

        // if there are no layouts add defaults
        if (tp.size() < 1) {

            tp.addElement("Default Layout#colaystr#field02,field03,field18,field43,field53,field65");
            tp.addElement("Communication#colaystr#field02,field03,field43,field49,field53,field45,field65");
            tp.addElement("Company#colaystr#field18,field48,field23,field24,field25,field65");
            tp.addElement("Privat#colaystr#field02,field03,timestampfield01,field09,field49,field53,field65");
            tp.addElement("Categories#colaystr#field02,field03,field18,field69");
            tp.addElement("Birthday#colaystr#timestampfield01,field02,field03,field09,field49,field53,field65");
            tp.addElement("Business#colaystr#field18,field02,field03,field43,field45,field65");
            tp.addElement("Default Layout#talaystr#subject,priority,start_date,end_date,percent_complete");
            tp.addElement("default_task_layout:Default Layout");
            tp.addElement("default_contact_layout:Default Layout");
        }

        return tp;
    }
    

    public static void loadLangCodes(final String file) {
	final File list = new File(file);
	if ((!(list.exists())) || (!(list.isDirectory()))) {
	    return;
	}

	final String[] allFilesInDir = list.list();
	for (int i = 0; i < allFilesInDir.length; i++) {
	    if ((new File(file + System.getProperty("file.separator") + allFilesInDir[i]).isFile()) &&
		((allFilesInDir[i]).endsWith(".dlc")) &&
		((allFilesInDir[i]).length() > 4)) {
		final String language = allFilesInDir[i].substring(0, allFilesInDir[i].lastIndexOf("."));		

		boolean encodingerror = false;
		try {
		    final FileInputStream fis = new FileInputStream(file+System.getProperty("file.separator")+allFilesInDir[i]);
		    final InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		    final BufferedReader br = new BufferedReader(isr);
		    
		    boolean found = false;
		    String line = "";
		    String sepVal = "";
		    String[] data;
		    
		    while ((line = br.readLine()) != null) {
			line = line.trim();
			
			if (line.startsWith("<text id=\"")) {
			    if (found) {
				sepVal = "";
			    }
			    found = true;
			    
			    if (line.endsWith("</text>")) {
				sepVal += line;
				data = seperateHTMLData(sepVal);
				found = false;
				sepVal = "";
				
			    }
			} else if (line.endsWith("</text>")) {
			    sepVal += line;
			    data = seperateHTMLData(sepVal);
			    found = false;
			    sepVal = "";
			    
			}
			
			if (found) {
			    sepVal += line+"\n";
			}		
		    }
		    br.close();
		    isr.close();
		    fis.close();
		} catch (final UnsupportedEncodingException uce) {
		    System.out.println("UnsupportedEncodingException at " + file + System.getProperty("file.separator") + allFilesInDir[i] + " (file is not UTF-8 encoded!)");
		    encodingerror = true;
		} catch (final Exception e) {
		    System.out.println("Exception while parsing dlc file : "
				       + file + System.getProperty("file.separator") + allFilesInDir[i]);
		    e.printStackTrace();
		}

		if (encodingerror) {
		    System.out.println("Starting fallback mode (reading " + file + System.getProperty("file.separator") + allFilesInDir[i] + " with standard encoding)");

		    try {
			final FileReader fr = new FileReader(file+System.getProperty("file.separator")+allFilesInDir[i]);
			final BufferedReader br = new BufferedReader(fr);
			
			boolean found = false;
			String line = "";
			String sepVal = "";
			String[] data;
			
			while ((line = br.readLine()) != null) {
			    line = line.trim();
			    
			    if (line.startsWith("<text id=\"")) {
				if (found) {
				    sepVal = "";
				}
				found = true;
				
				if (line.endsWith("</text>")) {
				    sepVal += line;
				    data = seperateHTMLData(sepVal);
				    found = false;
				    sepVal = "";
				    
				}
			    } else if (line.endsWith("</text>")) {
				sepVal += line;
				data = seperateHTMLData(sepVal);
				found = false;
				sepVal = "";
				
			    }
			    
			    if (found) {
				sepVal += line+"\n";
			    }		
			}
			br.close();
			fr.close();
		    } catch (final Exception e) {
			System.out.println("Exception while parsing dlc file : "
					   + file + System.getProperty("file.separator") + allFilesInDir[i]);
			e.printStackTrace();
		    }
		}
	    }
	}
    }

    public static void checkReloadTimes() {
	
	int time_to_check = 300000;
	
	try {
	    time_to_check = Integer.parseInt(getParameter("mail_check_time"));
	    time_to_check = time_to_check * 1000;
	    if (time_to_check < 30000) {
		time_to_check = 30000;
	    }
	} catch (final Exception e) { }
	
	setParameter("mail_check_time", "" + time_to_check);

	if (getParameter("portal_reload_time") != null) {
	    time_to_check = 300000;
	    try {
		time_to_check = Integer.parseInt(getParameter("portal_reload_time"));
		time_to_check = time_to_check * 1000;
		if (time_to_check < 300000) {
		    time_to_check = 300000;
		}
	    } catch (final Exception e) { }
	    
	    setParameter("portal_reload_time", "" + time_to_check);
	}
    }
    
    public static void loadParametersFromFile(final String filename) {
	if (!new File(filename).exists()) {
	    return;
	}
	try {
	    final FileInputStream fis = new FileInputStream(filename);
	    final BufferedReader br = new BufferedReader(new InputStreamReader(fis));
	    String line = null;
	    while ((line = br.readLine()) != null) {
		line = line.trim();
		if (line.startsWith("#") || line.startsWith("!")) {
		    continue;
		}
		final int separator = line.indexOf("=");
		if (separator == -1) {
		    continue;
		}
		final String name = line.substring(0, separator);
		final String value = line.substring(separator+1);

	    }
	    br.close();
	    fis.close();
	} catch (final IOException ie) {
	    System.out.println("Error while reading parameter file \""+filename+"\".");
	    System.out.println(ie.getMessage());
	}
    }
   
    public static String getDatePattern(final String country) {
	try {
	    final String returnPattern = datePattern.get(country.toUpperCase()).toString();
	    return returnPattern.substring(0, returnPattern.indexOf(" "));
	} catch(final NullPointerException npe) {
	    final String defaultPattern = datePattern.get("DEFAULT").toString();
	    return defaultPattern.substring(0, defaultPattern.indexOf(" "));
	}
    }

    public static String getDateTimePattern(final String country) {
	try {
	    return datePattern.get(country.toUpperCase()).toString();
	} catch(final NullPointerException npe) {
	    return datePattern.get("DEFAULT").toString();
	}
    }

    /**
     * Reading external configuration files we need and store paramters global ...
     * /etc/imap/globals.conf
     * /etc/openldap/ldap.conf
     */
    public static void loadExternalConfigs(final String imapfile) {
	try {
	    final File f = new File(imapfile);
	    if (f.exists()) {
		final FileReader fr = new FileReader(f);
		final BufferedReader br = new BufferedReader(fr);
		String line = "";
		while ((line = br.readLine()) != null) {
		    if (line.startsWith("AdministrativeHostname=")) {
			line = line.substring(line.indexOf("=")+1, line.length()).trim();
			if (line.length() != 0) {
			    setParameter("administrativeHostname", line);
			}
		    } else if (line.startsWith("WebmailHostname=")) {
			line = line.substring(line.indexOf("=")+1, line.length()).trim();
			if (line.length() != 0) {
			    setParameter("webmailHostname", line);
			}
		    } else if (line.startsWith("GroupwareHostname=")) {
			line = line.substring(line.indexOf("=")+1, line.length()).trim();
			if (line.length() != 0) {
			    setParameter("groupwareHostname", line);
			}
		    } else if (line.startsWith("MessagingHostname=")) {
			line = line.substring(line.indexOf("=")+1, line.length()).trim();
			if (line.length() != 0) {
			    setParameter("messagingHostname", line);
			}
		    } else if (line.startsWith("EnableFaxParamFrontend=")) {
			line = line.substring(line.indexOf("=")+1, line.length()).trim();
			if (line.length() != 0 && line.trim().equals("true")) {
			    setParameter("faxEnabled", "true");
			}
		    }
		}
		fr.close();
	    }
	} catch (final Exception e) {  
	    System.out.println("config file \"" + imapfile + "\" read error! --> " + e);
	}

   }

    /**
     * Laden der Datenbankanbindung aus der nasi.con
     */
    public static void loadDBParameters(final String serverconf) {
	
	try {
	    
	    final File f = new File(serverconf);
	    
	    final FileReader fr = new FileReader(f);
	    final BufferedReader br = new BufferedReader(fr);
	    String line = "";
	    
	    while ((line = br.readLine()) != null) {
		
		line = line.trim();
		
		if (line.startsWith("NAS_CON_CLASS_NAME:")) {
		    line = line.substring(line.indexOf(":")+1, line.length()).trim();
		    if (line.length() != 0) {
			setParameter("db_connection", line);
		    }
		    
		} else if (line.startsWith("NAS_CON_USER:")) {
		    line = line.substring(line.indexOf(":")+1, line.length()).trim();
		    if (line.length() != 0) {
			setParameter("db_user", line);
		    }
		    
		} else if (line.startsWith("NAS_CON_PASS:")) {
		    line = line.substring(line.indexOf(":")+1, line.length()).trim();
		    if (line.length() != 0) {
			setParameter("db_pass", line);
		    }
		    
		} else if (line.startsWith("NAS_CON_DRIVER:")) {
		    line = line.substring(line.indexOf(":")+1, line.length()).trim();
		    if (line.length() != 0) {
			setParameter("db_driver", line);
		    }
		}
	    }
	    
	    br.close();
	    fr.close();
	
	} catch (final Exception e) {
	    System.out.println("config file \"" + serverconf + "\" read error!");
	}
	
    }

    /**
     * Einen Parameter aus der Parameterliste auslesen
     */
    public static String getParameter(final String name) {
	return (String)parameters.get(name);
    }
    
    /**
     * Einen Parameter in der Parameterliste spezifizieren
     */
    public static void setParameter(final String name, final String value) {
    parameters.put(name, value);
    }


    private static String[] seperateHTMLData(final String data) {
	final String[] retval = new String[2];
	
	final int start = data.indexOf("=\"")+2;
	final int end = data.indexOf("\">", start);
	
	retval[0] = data.substring(start, end);
	retval[1] = data.substring(end+2, data.indexOf("</text>"));
	
	return (retval);
    }
		
	private static final void checkCalendarConfig() {
		if (GlobalConfig.getParameter("set_max_end_years_for_sequences") != null) {
			try {
				final int test = Integer.parseInt(GlobalConfig.getParameter("set_max_end_years_for_sequences"));
				 CalendarRecurringCollection.setMAX_END_YEARS(test);
			} catch(final NumberFormatException nfe) { 
				ComfireLogger.log("set_max_end_years_for_sequences not set!", ComfireLogger.DEBUG);
			}
		}
	}
	
	 private static void loadServletMapping(final String file) {
		final File f = new File(file);
		FileInputStream fis = null;
		try {
			if (f.exists()) {
				fis = new FileInputStream(f);
				
				final Properties properties = new Properties();		
				properties.load(fis);
				
				fis.close();
			
				String name = null;
				String value = null;
				
				final Map<String,Constructor> servletConstructorMap = new HashMap<String,Constructor>();
				
				final Iterator it = properties.keySet().iterator();
				
				while (it.hasNext()) {
					try {
						name = it.next().toString();
						value = properties.get(name).toString();

						servletConstructorMap.put(name, Class.forName(value).getConstructor(new Class[] {}));
					} catch (final SecurityException e) {
						if (LOG.isWarnEnabled()) {
							LOG.warn("Couldn't find class " + value, e);
						}
					} catch (final ClassNotFoundException e) {
						if (LOG.isWarnEnabled()) {
							LOG.warn("Couldn't find class " + value, e);
						}
					}
				}
				
				//HttpServletManager.setServletConstructorMap(servletConstructorMap);
			} else {
				ComfireLogger.log("Cannot find property file: " + file, ComfireLogger.WARN);
			}
		} catch (final Exception exc) {
			ComfireLogger.log("Cannot load property file: " + file + " " + exc, ComfireLogger.WARN);
			exc.printStackTrace();
		}
	 }

}
