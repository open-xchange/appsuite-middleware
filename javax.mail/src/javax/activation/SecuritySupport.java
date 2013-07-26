/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * @(#)SecuritySupport.java	1.3 05/11/16
 *
 * Copyright 2002-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package javax.activation;

import java.security.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Security related methods that only work on J2SE 1.2 and newer.
 */
class SecuritySupport {

    private SecuritySupport() {
	// private constructor, can't create an instance
    }

    public static ClassLoader getContextClassLoader() {
	return (ClassLoader)
		AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
		ClassLoader cl = null;
		try {
		    cl = Thread.currentThread().getContextClassLoader();
		} catch (SecurityException ex) { }
		return cl;
	    }
	});
    }

    public static InputStream getResourceAsStream(final Class c,
				final String name) throws IOException {
	try {
	    return (InputStream)
		AccessController.doPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws IOException {
			return c.getResourceAsStream(name);
		    }
		});
	} catch (PrivilegedActionException e) {
	    throw (IOException)e.getException();
	}
    }

    public static URL[] getResources(final ClassLoader cl, final String name) {
	return (URL[])
		AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
		URL[] ret = null;
		try {
		    List v = new ArrayList();
		    Enumeration e = cl.getResources(name);
		    while (e != null && e.hasMoreElements()) {
			URL url = (URL)e.nextElement();
			if (url != null)
			    v.add(url);
		    }
		    if (v.size() > 0) {
			ret = new URL[v.size()];
			ret = (URL[])v.toArray(ret);
		    }
		} catch (IOException ioex) {
		} catch (SecurityException ex) { }
		return ret;
	    }
	});
    }

    public static URL[] getSystemResources(final String name) {
	return (URL[])
		AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
		URL[] ret = null;
		try {
		    List v = new ArrayList();
		    Enumeration e = ClassLoader.getSystemResources(name);
		    while (e != null && e.hasMoreElements()) {
			URL url = (URL)e.nextElement();
			if (url != null)
			    v.add(url);
		    }
		    if (v.size() > 0) {
			ret = new URL[v.size()];
			ret = (URL[])v.toArray(ret);
		    }
		} catch (IOException ioex) {
		} catch (SecurityException ex) { }
		return ret;
	    }
	});
    }

    public static InputStream openStream(final URL url) throws IOException {
	try {
	    return (InputStream)
		AccessController.doPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws IOException {
			return url.openStream();
		    }
		});
	} catch (PrivilegedActionException e) {
	    throw (IOException)e.getException();
	}
    }
}
