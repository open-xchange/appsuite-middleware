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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.obs.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author choeger
 */
public class BuildServiceClient {

    private static final int MAX_RETRIES = 5;

    private String bsuser;

    private String bspass;

    private String bsurl;

    private String bsarch;

    private HttpClient httpclient;

    private XPath xpath;

    /**
     * @return the bsarch
     */
    public final String getBsarch() {
        return bsarch;
    }

    /**
     * @param bsarch the bsarch to set
     */
    public final void setBsarch(final String bsarch) {
        this.bsarch = bsarch;
    }

    /**
     * @return the bsuser
     */
    public final String getBsuser() {
        return bsuser;
    }

    /**
     * @param bsuser the bsuser to set
     */
    public final void setBsuser(final String bsuser) {
        this.bsuser = bsuser;
    }

    /**
     * @return the bspass
     */
    public final String getBspass() {
        return bspass;
    }

    /**
     * @param bspass the bspass to set
     */
    public final void setBspass(final String bspass) {
        this.bspass = bspass;
    }

    /**
     * @return the bsurl
     */
    public final String getBsurl() {
        return bsurl;
    }

    /**
     * @param bsurl the bsurl to set
     */
    public final void setBsurl(final String bsurl) {
        this.bsurl = bsurl;
    }

    /**
     * requires the account name, password and url of the openSUSE buildservice host
     */
    public BuildServiceClient(final String bsuser, final String bspass, final String bsurl) {
        super();
        this.bsuser = bsuser;
        this.bspass = bspass;
        this.bsurl = bsurl;
        this.bsarch = "i586";
        this.xpath = XPathFactory.newInstance().newXPath();
        this.httpclient = new HttpClient();
        this.httpclient.getState().setCredentials(new AuthScope(null, 80, null), new UsernamePasswordCredentials(bsuser, bspass));
    }

    /**
     * set common parameters for this method like AuthScheme, etc.
     * 
     * @param method
     */
    private void setHttpMethodParams(HttpMethod method) {
        method.getHostAuthState().setAuthScheme(new BasicScheme());
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
    }
    
    /**
     * return {@link InputStream} to {@link GetMethod} response
     * @param method
     * @return
     * @throws HttpException
     * @throws IOException
     */
    private InputStream bsGetResult(GetMethod method) throws HttpException, IOException {
    	setHttpMethodParams(method);
    	int status = httpclient.executeMethod(method);
        if (status != HttpStatus.SC_OK) {
            throw new HttpException(method.getStatusLine().toString());
        }
        return method.getResponseBodyAsStream();
    }

    /**
     * Return {@link InputStream} to binary package content
     * 
     * @param project
     * @param repository
     * @param pkgname
     * @param filename
     * @return
     * @throws IOException
     * @throws HttpException
     */
    public InputStream getProjectBinaryPackageByName(String project, String repository, String pkgname, String filename) throws HttpException, IOException {
        final GetMethod method = new GetMethod(
            this.bsurl + "/build/" + project + "/" + repository + "/" + this.bsarch + "/" + pkgname + "/" + filename);
        return bsGetResult(method);
    }

    /**
     * check if specified package exists
     * returns true in case it exists, false in case of 404 and
     * throws an Exception in case of any other responses
     * 
     * @param method
     * @return
     * @throws HttpException
     * @throws IOException
     */
    private boolean packageExists(GetMethod method) throws HttpException, IOException {
    	int status = httpclient.executeMethod(method);
    	if( status == HttpStatus.SC_NOT_FOUND ) {
    		return false;
    	} else if (status != HttpStatus.SC_OK) {
            throw new HttpException(method.getStatusLine().toString());
        }
    	return true;
    }
    
    /**
     * create empty package on obs
     * 
     * @param project
     * @param pkgname
     * @throws HttpException
     * @throws IOException
     */
    private void createPackage(String project, String pkgname) throws HttpException, IOException {
    	String pkgmeta = getPackageMeta(project, pkgname);
    	final PutMethod pmethod = new PutMethod(this.bsurl + "/source/" + project + "/" + pkgname + "/_meta");
    	setHttpMethodParams(pmethod);
    	pmethod.setRequestEntity(new StringRequestEntity(pkgmeta,"text/plain","utf-8"));
    	int status = httpclient.executeMethod(pmethod);
        if (status != HttpStatus.SC_OK) {
            throw new HttpException(pmethod.getStatusLine().toString());
        }
    }
    
    /**
     * upload specified file into obs
     * 
     * @param project
     * @param pkgname
     * @param file
     * @throws HttpException
     * @throws IOException
     */
    private void uploadSource(String project, String pkgname, File file) throws HttpException, IOException {
    	final PutMethod pmethod = new PutMethod(this.bsurl + "/source/" + project + "/" + pkgname + "/" + file.getName());
    	setHttpMethodParams(pmethod);
    	pmethod.setRequestEntity(new InputStreamRequestEntity(new FileInputStream(file)));
    	int status = httpclient.executeMethod(pmethod);
        if (status != HttpStatus.SC_OK) {
            throw new HttpException(pmethod.getStatusLine().toString());
        }
    }
    
    /**
     * delete specified source file
     * 
     * @param project
     * @param pkgname
     * @param file
     * @throws HttpException
     * @throws IOException
     */
    private void deleteSource(String project, String pkgname, String file) throws HttpException, IOException {
    	final DeleteMethod dmethod = new DeleteMethod(this.bsurl + "/source/" + project + "/" + pkgname + "/" + file);
    	setHttpMethodParams(dmethod);
    	int status = httpclient.executeMethod(dmethod);
        if (status != HttpStatus.SC_OK) {
            throw new HttpException(dmethod.getStatusLine().toString());
        }
    }
    
    /**
     * generate package meta xml data
     * 
     * @param project
     * @param pkgname
     * @return
     */
    private String getPackageMeta(String project, String pkgname) {
    	return "<package project=\"" + project + "\" name=\"" + pkgname + "\">\n" +
    			"<title>" + pkgname + "</title>\n" + 
    			"<description/>\n" + 
    			"<person role=\"maintainer\" userid=\"" + this.bsuser + "\"/>\n" +
    			"</package>";
    }
    
    /**
     * upload all source files from within directory path
     * 
     * @param project
     * @param pkgname
     * @param path
     * @throws HttpException
     * @throws IOException
     * @throws BuildServiceException
     * @throws XPathExpressionException
     */
    public void uploadSourcePackage(String project, String pkgname, String path) throws HttpException, IOException, BuildServiceException, XPathExpressionException {
    	final GetMethod method = new GetMethod(this.bsurl + "/source/" + project + "/" + pkgname + "/_meta");
    	setHttpMethodParams(method);
    	if( ! packageExists(method) ) {
    		createPackage(project, pkgname);
    	}
    	File sdir = new File(path);
    	if( ! sdir.isDirectory() ) {
    		throw new BuildServiceException("path is not a directory");
    	}
    	// TODO: only upload files that have changed (use md5sum)
    	ArrayList<String> obsfiles = getPackageSourceNames(project, pkgname);
    	if( null != obsfiles && obsfiles.size() > 0 ) {
    		for(String s : obsfiles) {
    			System.out.println("Removing " + s);
    			deleteSource(project, pkgname, s);
    		}
    	}
		for(final File f : sdir.listFiles() ) {
			System.out.println("Uploading " + f.getPath());
			uploadSource(project, pkgname, f);
		}
    }
    
    /**
     * get an ArrayList of packages that belong to project / repository / package
     * 
     * @param project
     * @param repository
     * @param pkgname
     * @return
     * @throws IOException
     * @throws HttpException
     * @throws XPathExpressionException
     * @throws BuildServiceException
     */
    public ArrayList<String> getProjectBinaryPackageNames(final String project, final String repository, final String pkgname) throws XPathExpressionException, HttpException, IOException, BuildServiceException {
        final GetMethod method = new GetMethod(this.bsurl + "/build/" + project + "/" + repository + "/" + this.bsarch + "/" + pkgname);
        return getFileList(method, "/binarylist/*");
    }

    /**
     * @param project
     * @param pkgname
     * @return
     * @throws XPathExpressionException
     * @throws HttpException
     * @throws IOException
     * @throws BuildServiceException
     */
    private ArrayList<String> getPackageSourceNames(final String project, final String pkgname) throws XPathExpressionException, HttpException, IOException, BuildServiceException {
        final GetMethod method = new GetMethod(this.bsurl + "/source/" + project + "/" + pkgname);
        return getFileList(method, "/directory/*");
    }

    /**
     * Returns list of files contained in given url within {@link GetMethod}.
     * xPathExpression is needed to find the nodes within the returned xml data
     *
     * Example:
     * <verbatim>$ curl -u oxbuilduser:openxchange http://buildapi.netline.de/build/open-xchange-snapshot/DebianLenny/i586/open-xchange-imap
     *   <binarylist>
     *     <binary filename="open-xchange-imap_6.20.0.0-26.diff.gz" size="4582" mtime="1317797248" />
     *     <binary filename="open-xchange-imap_6.20.0.0-26.dsc" size="1411" mtime="1317797248" />
     *     <binary filename="open-xchange-imap_6.20.0.0-26_all.deb" size="446892" mtime="1317805378" />
     *     <binary filename="open-xchange-imap_6.20.0.0-26_i386.changes" size="1373" mtime="1317805379" />
     *     <binary filename="open-xchange-imap_6.20.0.0.orig.tar.gz" size="214223" mtime="1317797248" />
     *   </binarylist></verbatim>
     *
     * @param method
     * @param xPathExpression
     * @return
     * @throws XPathExpressionException
     * @throws HttpException
     * @throws IOException
     * @throws BuildServiceException
     */
    private ArrayList<String> getFileList(final GetMethod method, final String xPathExpression) throws XPathExpressionException, HttpException, IOException, BuildServiceException {
        final NodeList nodes = (NodeList) xpath.evaluate(xPathExpression, new InputSource(bsGetResult(method)), XPathConstants.NODESET);
        final ArrayList<String> ret = new ArrayList<String>();
        for (int n = 0; n < nodes.getLength(); n++) {
            final NamedNodeMap nmap = nodes.item(n).getAttributes();
            if (nmap == null) {
                throw new BuildServiceException("unable to fetch package names");
            }
            Node node = nmap.getNamedItem("filename");
            if( null == node ) {
            	node = nmap.getNamedItem("name");
            }
            if (node != null) {
                ret.add(node.getNodeValue());
            }
        }
        return ret;
    }
    
    /**
     * Determine status of project Status is cached internally and is overwritten with every call of this method
     * 
     * @param project
     * @param repository
     * @return the package statuses
     * @throws IOException
     * @throws HttpException
     * @throws XPathExpressionException
     * @throws BuildServiceException
     */
    public PackageStatus[] checkProjectStatus(final String project, final String repository) throws XPathExpressionException, HttpException, IOException, BuildServiceException {
        final GetMethod method = new GetMethod(this.bsurl + "/build/" + project + "/_result");
        int retries = MAX_RETRIES;
        NodeList nodes = null;
        do {
            retries--;
            try {
                nodes = (NodeList) xpath.evaluate("/resultlist/result[@repository=\"" + repository + "\"]//*", new InputSource(
                    bsGetResult(method)), XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                if (retries == 0) {
                    throw e;
                }
            }
        } while (null == nodes);
        final List<PackageStatus> statuses = new ArrayList<PackageStatus>();
        for (int n = 0; n < nodes.getLength(); n++) {
            final Node node = nodes.item(n);
            final NodeList childs = node.getChildNodes();
            // find out whether status node has any subnodes containing error details
            String errDetails = null;
            if (childs != null) {
                for (int f = 0; f < childs.getLength(); f++) {
                    final Node cn = childs.item(f);
                    if (cn.getNodeName().equals("details")) {
                        errDetails = cn.getTextContent();
                    }
                }
            }
            final NamedNodeMap nmap = node.getAttributes();
            if (nmap == null) {
                throw new BuildServiceException("unable to check project status");
            }
            // FIXME: dunno why there are nodes without any attributes and where they belong (at
            // least they are not part of the document we are parsing here -> ROTTEN!
            if (nmap.getLength() > 0) {
                final Node code = nmap.getNamedItem("code");
                if (code == null) {
                    throw new BuildServiceException("unable to check project status");
                }
                final Node pkg = nmap.getNamedItem("package");
                if (pkg == null) {
                    throw new BuildServiceException("unable to check project status");
                }
                final PackageStatus ps = new PackageStatus(pkg.getNodeValue(), code.getNodeValue());
                if (errDetails != null) {
                    ps.setDetails(errDetails);
                }
                statuses.add(ps);
            }
        }
        return statuses.toArray(new PackageStatus[statuses.size()]);
    }

    /**
     * check if project build contains errors or is in progress
     * @param statuses  the package statuses.
     * @return <code>true</code> if the project has been built.
     */
    public boolean isProjectSuccessfulBuilt(PackageStatus[] statuses) {
        for (final PackageStatus status : statuses) {
            final Code code = status.getCode();
            switch (code) {
            case SUCCEEDED:
            case DISABLED:
            case EXCLUDED:
                break;
            default:
                return false;
            }
        }
        return true;
    }

    /**
     * returns the complete project status as a string
     * @param statuses the package statuses.
     * @return a string containing all statuses.
     */
    public String getProjectStatus(final PackageStatus[] statuses) {
        final StringBuilder ret = new StringBuilder();
        for (final PackageStatus status : statuses) {
            ret.append(status.toString());
        }
        return ret.toString();
    }

    /**
     * @param statuses the package statuses
     * @return <code>true</code> if the project is still building.
     */
    public boolean isProjectBuilding(final PackageStatus[] statuses) {
        for (final PackageStatus status : statuses) {
            final Code code = status.getCode();
            switch (code) {
            case BUILDING:
            case SCHEDULED:
            case FINISHED:
            case BLOCKED:
            case DISPATCHING:
            case UNKNOWN:
                return true;
            default:
            }
        }
        return false;
    }

    public boolean somePackageFailed(final PackageStatus[] statuses) {
        for (final PackageStatus status : statuses) {
            if (Code.FAILED == status.getCode()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param args
     * @throws BuildServiceException 
     * @throws XPathExpressionException 
     */
    public static void main(String[] args) throws XPathExpressionException, BuildServiceException {
        BuildServiceClient bsc = new BuildServiceClient("oxbuilduser", "openxchange", "http://buildapi.netline.de");

//        final String project = "open-xchange-6.20-public-ptf-2011-10-06";
//        final String repository = "DebianSqueeze";

        try {
        	//bsc.uploadSourcePackage("open-xchange-snapshot", "open-xchange-imap", "/home/choeger/bsctest");
        	bsc.uploadSourcePackage("open-xchange-7-test", "open-xchange-imap", "/home/choeger/bsctest");
        	
            //bsc.getProjectBinaryPackageNames("open-xchange-snapshot", "DebianLenny", "open-xchange-imap");
            /*
            while (it.hasNext()) {
                final String pkg = it.next();
                System.out.println(pkg);
                final File file = new File("/tmp/" + pkg);
                if (file.exists()) {
                    file.delete();
                }
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                final InputStream is = bsc.getProjectBinaryPackageByName(project, repository, "open-xchange", pkg);
                byte[] buf = new byte[512];
                int length;
                while ((length = is.read(buf)) != -1) {
                    bos.write(buf, 0, length);
                }
            }
            */
        	
//            while (true) {
//        	PackageStatus[] statuses = bsc.checkProjectStatus(project, repository);
//        	for(final PackageStatus st : statuses ) {
//        		if( st.getCode() != Code.DISABLED ) {
//        			System.out.println(st.getName() + ":" + st.getCode());
//        		}
//        	}
//                if (!bsc.isProjectBuilding(statuses)) {
//                    break;
//                }
//                System.out.println("still building...");
//                Thread.sleep(5000);
//            }
//            PackageStatus[] status = bsc.checkProjectStatus(project, repository);
//            if (!bsc.isProjectSuccessfulBuilt(status)) {
//                System.out.println("Project not successfully built:");
//                System.out.println(bsc.getProjectStatus(status));
//            }
        } catch (final HttpException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
