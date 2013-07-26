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
 * @(#)TextEditor.java	1.8 05/11/16
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.sun.activation.viewers;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.beans.*;
import javax.activation.*;

public class TextEditor extends Panel implements CommandObject,
    ActionListener {
	// UI Vars...
    private TextArea text_area = null;
    private GridBagLayout panel_gb = null;
    private Panel button_panel = null;
    private Button save_button = null;
	// File Vars
    private File text_file = null;
    private String text_buffer = null;
    private InputStream data_ins = null;
    private FileInputStream fis = null;
	
    private DataHandler _dh = null;
    private boolean DEBUG = false;
	/**
	 * Constructor
	 */
    public TextEditor() {
	panel_gb = new GridBagLayout();
	setLayout(panel_gb);
	
	button_panel = new Panel();
	//	button_panel.setBackground(Color.white);
	button_panel.setLayout( new FlowLayout() );
	save_button = new Button("SAVE");
	button_panel.add(save_button);
	addGridComponent(this,
			 button_panel,
			 panel_gb,
			 0,0,
			 1,1,
			 1,0);
	
	// create the text area
	text_area = new TextArea("This is text",24, 80, 
				 TextArea.SCROLLBARS_VERTICAL_ONLY );
	//	text_area.setBackground(Color.lightGray);
	text_area.setEditable( true );
	
	addGridComponent(this,
			 text_area,
			 panel_gb,
			 0,1,
			 1,2,
			 1,1);
	
	// add listeners
	save_button.addActionListener( this );
	
    }

    ////////////////////////////////////////////////////////////////////////
	/**
	 * adds a component to our gridbag layout
	 */
    private void addGridComponent(Container cont, 
				  Component comp,
				  GridBagLayout mygb,
				  int gridx,
				  int gridy, 
				  int gridw,
				  int gridh,
				  int weightx,
				  int weighty) { 
	GridBagConstraints c = new GridBagConstraints(); 
	c.gridx = gridx; 
	c.gridy = gridy; 
	c.gridwidth = gridw; 
	c.gridheight = gridh; 
	c.fill = GridBagConstraints.BOTH;
	c.weighty = weighty;
	c.weightx = weightx;
	c.anchor =  GridBagConstraints.CENTER;
	mygb.setConstraints(comp, c); 
	cont.add(comp); 
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
	
	byte data[] = new byte[1024];
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	int bytes_read = 0;
	// check that we can actually read
	
	while((bytes_read = ins.read(data)) >0)
	    baos.write(data, 0, bytes_read);
	ins.close();

      
	// convert the buffer into a string
	// popuplate the buffer
	text_buffer = baos.toString();

	// place in the text area
	text_area.setText(text_buffer);
    }
    ///////////////////////////////////////////////////////////////////////
    private void performSaveOperation(){
	OutputStream fos = null;
	try {
	    fos = _dh.getOutputStream();
	} catch (Exception e) {}
	
	String buffer = text_area.getText();
	
	// make sure we got one
	if(fos == null) {
	    System.out.println("Invalid outputstream in TextEditor!");
	    System.out.println("not saving!");
	}
	
	try {
	    fos.write( buffer.getBytes() );
	    fos.flush(); // flush it!
	    fos.close(); // close it!
	} catch(IOException e)
	    {
		System.out.println("TextEditor Save Operation failed with: " + e);
	    }
	
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
	/////////////////////////////////////////////////////////////////////
	// for ActionListener
    public void actionPerformed(ActionEvent evt){
	if(evt.getSource() == save_button) { // save button pressed!
	    
	    // Save ourselves
	    this.performSaveOperation();
	}
    }
	
}
