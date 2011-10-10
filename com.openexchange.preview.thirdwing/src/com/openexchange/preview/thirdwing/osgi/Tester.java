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

package com.openexchange.preview.thirdwing.osgi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Observer;
import net.thirdwing.common.ConversionJobfactory;
import net.thirdwing.common.IConversionJob;
import net.thirdwing.common.UpdateMessages;
import net.thirdwing.exception.XHTMLConversionException;
import net.thirdwing.io.IOUnit;
import net.thirdwing.io.IStreamProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * {@link Tester}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Tester implements Observer, IStreamProvider {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Tester.class));
    
    private File outputDir = null;
    
    public Tester() {
        super();
        try {
            outputDir = File.createTempFile("temp", Long.toString(System.currentTimeMillis()));
            outputDir.delete();
            outputDir.mkdir();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    
    public void perform() {
        File inputFile = new File("/home/steffen/preview/testDocs/OpenXML_text_reference_v1_2.docx");
        LOG.info("InputFile: " + inputFile.getAbsolutePath());
        
        IConversionJob transformer = ConversionJobfactory.getTransformer(inputFile);
        transformer.addObserver(this);
        
        IOUnit unit;
        try {
            unit = new IOUnit(new FileInputStream(inputFile));
            unit.setStreamProvider(this);
            transformer.transformDocument(unit);
//            transformer.createPreviewImage(unit, 300F);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XHTMLConversionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        } catch (PreviewCreationException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }        
    }
    
    @Override
    public void update(Observable o, Object arg) {        
        UpdateMessages msg = (UpdateMessages) arg;        
        LOG.info("Update: " + msg.getKey());   
    }

    @Override
    public OutputStream createFile(String name) throws XHTMLConversionException {
        try {
            LOG.info("OutputFile: " + outputDir.getAbsolutePath() + "/" + name);
            
            File file = new File(outputDir.getAbsolutePath() + "/" + name);
            file.createNewFile();
            
            return new FileOutputStream(file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // TODO Auto-generated method stub
        return null;
    }

}
