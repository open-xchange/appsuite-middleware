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
 * @(#)TextViewer.java	1.9 05/11/16
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.sun.activation.viewers;

import java.awt.*;
import java.io.*;
import java.beans.*;
import javax.activation.*;

public class TextViewer extends Panel implements CommandObject {
    // UI Vars...
    private TextArea text_area = null;
    
    // File Vars
    private File text_file = null;
    private String text_buffer = null;
    
    private DataHandler _dh = null;
    private boolean DEBUG = false;
    /**
     * Constructor
     */
    public TextViewer() {
	setLayout( new GridLayout(1,1));
	// create the text area
	text_area = new TextArea("", 24, 80, 
				 TextArea.SCROLLBARS_VERTICAL_ONLY );
	text_area.setEditable( false );
	
	add(text_area);
    }
    
    //--------------------------------------------------------------------
    public void setCommandContext(String verb, DataHandler dh) throws IOException {
	_dh = dh;
	this.setInputStream( _dh.getInputStream() );
    }
  //--------------------------------------------------------------------

  /**
   * set the data stream, component to assume it is ready to
   * be read.
   */
  public void setInputStream(InputStream ins) throws IOException {
      
      int bytes_read = 0;
      // check that we can actually read
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte data[] = new byte[1024];
      
      while((bytes_read = ins.read(data)) >0)
	  baos.write(data, 0, bytes_read);
      
      ins.close();

      // convert the buffer into a string
      // popuplate the buffer
      text_buffer = baos.toString();

      // place in the text area
      text_area.setText(text_buffer);

    }
  //--------------------------------------------------------------------
    public void addNotify() {
	super.addNotify();
	invalidate();
    }
  //--------------------------------------------------------------------
    public Dimension getPreferredSize()	{
	return text_area.getMinimumSize(24, 80);
    }

}






