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
 * @(#)ImageViewerCanvas.java	1.3 05/11/16
 *
 * Copyright 1997-2005 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.sun.activation.viewers;

import java.awt.*;

public class ImageViewerCanvas extends Canvas
{
  private Image canvas_image = null;
  
  /**
   * The constructor
   */
  public ImageViewerCanvas()
    {
	
    }

  /**
   * set the image
   */
  public void setImage(Image new_image)
    {
      canvas_image = new_image;
      this.invalidate();
      this.repaint();
    }
  
  /**
   * getPreferredSize
   */
  public Dimension getPreferredSize()
    {
      Dimension d = null;
      
      if(canvas_image == null)
	{
	  d = new Dimension(200, 200);
	}
      else
	d = new Dimension(canvas_image.getWidth(this), 
			  canvas_image.getHeight(this));

      return d;
    }
  /**
   * paint method
   */
  public void paint(Graphics g)
    {

      if(canvas_image != null)
	g.drawImage(canvas_image, 0, 0, this);

    }
  
}
