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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.subscribe.crawler;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.crawler.internal.AbstractStep;


/**
 * {@link TextPageByMultiselectStep}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class TextPageByMultiselectStep extends AbstractStep<TextPage, HtmlPage> {

    private String formName, formAction, selectName, selectValue, buttonName;

    private int formNumber;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TextPageByMultiselectStep.class);

    public TextPageByMultiselectStep(){

    }

    public TextPageByMultiselectStep(final String description, final String formName, final String formAction, final int formNumber, final String selectName, final String selectValue, final String buttonName){
        this.description = description;
        this.formName = formName;
        this.formAction = formAction;
        this.formNumber = formNumber;
        this.selectName = selectName;
        this.selectValue = selectValue;
        this.buttonName = buttonName;
    }

    @Override
    public void execute(final WebClient webClient) throws OXException {
        HtmlForm form = null;
        if (!formName.equals("")) {
            form = input.getFormByName(formName);
        } else if (!formAction.equals("")){
            for (final HtmlForm tempForm : input.getForms()){
                if (tempForm.getActionAttribute().matches(formAction)){
                    form = tempForm;
                }
            }
        }
        if (form != null){
            final HtmlSelect select = form.getSelectByName(selectName);
            final HtmlOption option = select.getOptionByValue(selectValue);
            select.setSelectedAttribute(option, true);
            final HtmlSubmitInput button = form.getInputByName(buttonName);

            try {
                output = (TextPage) button.click();
            }
            catch (final ClassCastException e){
                LOG.info("Instead of the expected TextPage something else was returned. Maybe the users addressbook is empty.");
                executedSuccessfully = true;
            }
            catch (final Exception e) {
                LOG.error(e.toString());
            }
            if (output != null) {
                executedSuccessfully = true;
            }
        }
    }

    public String getFormName() {
        return formName;
    }


    public void setFormName(final String formName) {
        this.formName = formName;
    }


    public String getFormAction() {
        return formAction;
    }


    public void setFormAction(final String formAction) {
        this.formAction = formAction;
    }


    public String getSelectName() {
        return selectName;
    }


    public void setSelectName(final String selectName) {
        this.selectName = selectName;
    }


    public String getSelectValue() {
        return selectValue;
    }


    public void setSelectValue(final String selectValue) {
        this.selectValue = selectValue;
    }


    public int getFormNumber() {
        return formNumber;
    }


    public void setFormNumber(final int formNumber) {
        this.formNumber = formNumber;
    }


    public String getButtonName() {
        return buttonName;
    }


    public void setButtonName(final String buttonName) {
        this.buttonName = buttonName;
    }


}
