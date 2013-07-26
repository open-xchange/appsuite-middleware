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
 * @(#)ImageViewer.java	1.8 05/11/16
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.sun.activation.viewers;

import java.awt.*;
import java.io.*;
import java.beans.*;
import javax.activation.*;

public class ImageViewer extends Panel implements CommandObject {
    // UI Vars...
    private ImageViewerCanvas canvas = null;
    
    // File Vars
    //    private InputStream data_ins = null;
    private Image image = null;
    private DataHandler _dh = null;
    
    private boolean DEBUG = false;
    /**
     * Constructor
     */
    public ImageViewer(){
	
	// create the ImageViewerCanvas
	canvas = new ImageViewerCanvas();
	add(canvas);
    }
    /**
     * Set the DataHandler for this CommandObject
     * @param DataHandler the DataHandler
     */
    public void setCommandContext(String verb, DataHandler dh)	throws IOException{
	_dh = dh;
	this.setInputStream( _dh.getInputStream() );
    }
    //--------------------------------------------------------------------
    
    /**
     * Set the data stream, component to assume it is ready to
     * be read.
     */
    private void setInputStream(InputStream ins) throws IOException {
	MediaTracker mt = new MediaTracker(this);
	int bytes_read = 0;
	byte data[] = new byte[1024];
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	while((bytes_read = ins.read(data)) >0)
	    baos.write(data, 0, bytes_read);
	ins.close();
	
	// convert the buffer into an image
	image = getToolkit().createImage(baos.toByteArray());
	
	mt.addImage(image, 0);
	
	try {
	    mt.waitForID(0);
	    mt.waitForAll();
	    if(mt.statusID(0, true ) != MediaTracker.COMPLETE){
		System.out.println("Error occured in image loading = " +
				   mt.getErrorsID(0));
		
	    }
	    
	}
	catch(InterruptedException e) {
	    throw new IOException("Error reading image data");
	}
	
	canvas.setImage(image);
	if(DEBUG)
	    System.out.println("calling invalidate");
	
    }
    //--------------------------------------------------------------------
    public void addNotify(){
	super.addNotify(); // call the real one first...
	this.invalidate();
	this.validate();
	this.doLayout();
    }
    //--------------------------------------------------------------------
    public Dimension getPreferredSize(){
	return canvas.getPreferredSize();
    }

}











