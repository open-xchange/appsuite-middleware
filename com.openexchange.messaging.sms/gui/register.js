/**
 * 
 * All content on this website (including text, images, source
 * code and any other original works), unless otherwise noted,
 * is licensed under a Creative Commons License.
 * 
 * http://creativecommons.org/licenses/by-nc-sa/2.5/
 * 
 * Copyright (C) Open-Xchange Inc., 2011
 * Mail: info@open-xchange.com 
 * 
 * @author Viktor Pracht <viktor.pracht@open-xchange.com>
 * 
 */

function ManualPulldown(parent, trigger, content) {
    MousePopup.call(this, newnode("div",
        { position: "absolute", display: "none", border: "1px solid" },
        { oxPopupMenu: true, className: "background-color-content " +
            "font-weight-default font-color-default border-color-design" },
        [content]));
    this.trigger = parent;
    trigger.oxPulldownTrigger = true;
    if (!document.addEventListener) {
        this.registerHide = function() {
            if (!MousePopup.active) {
                jQuery("body").bind("click", this.clickHandler); // allows external trigger
                // addDOMEvent(body, "click", this.clickHandler);
                addDOMEvent(body, "mousedown", this.clickHandler);
                addDOMEvent(body, "mouseover", this.mouseHandler);
                addDOMEvent(body, "mouseout", this.mouseHandler);
                body.onlosecapture = MousePopup.hideActive;
                body.setCapture(false);
            }
            if (!this.activeID) this.activeID = ++MousePopup.nextID;
            MousePopup.activePopups[this.activeID] = this;
        }
        var Self = this;
        var recursion = {};
        this.clickHandler = function(e) {
            if (recursion[e.type]) return;
            if (!Self.captureEvents(e.srcElement)) Self.mouseHandler(e);
        };
        this.mouseHandler = function(e) {
            if (recursion[e.type]) return;
            recursion[e.type] = true;
            if (MousePopup.active) {
                body.onlosecapture = null;
                body.releaseCapture();
            }
            switch (e.type) {
                case "click":
                    e.srcElement.click();
                    break;
                default:
                    e.srcElement.fireEvent("on" + e.type);
            }
            if (MousePopup.active) {
                body.onlosecapture = MousePopup.hideActive;
                body.setCapture(false);
            }
            recursion[e.type] = false;
        };
    }
}
ManualPulldown.prototype = extend(MousePopup, {
    show: PulldownMenu.prototype.show,
    captureEvents: PulldownMenu.prototype.captureEvents,
    close: function() {
        PopupMenu.last = null;
        this.hide();
    }
});

// TODO: decide at runtmie
var boxSizing = "box-sizing: border-box; -moz-box-sizing: border-box;" +
                "-webkit-box-sizing: border-box;";

function ComboBox(options) {
    var self = this;
    this.options = jQuery.extend({ size: 8 }, options || {});
    
    this.count = 0;
    
    this.node = jQuery(
        '<div style="position: relative; width: 100%; height: 1.5em;' +
                    boxSizing + 'border: 1px solid;"' +
                    'class="border-color-design">' +
            '<div style="position: absolute; left: 0; top: 0;' +
                        'botom: 0; right: 0; margin-right: 2px;">' +
                '<input style="width: 100%; height: 100%; border: none;"' +
                        'type="text" id="' + this.options.id + '">' +
            '</div>' +
            '<div style="position: absolute; top: 0; bottom: 0; right: 0;' +
                        'width: 1em; height: auto; display: none;' +
                        'border: 1px outset; padding: 0;' +
                        'background-position: 50% 50%;' +
                        'background-repeat: no-repeat;"' +
                'class="expandButton ox-button"></div>' +
       '</div>');

    this.input = this.node.find("input");

    this.select = jQuery('<select style="width: 100%; height: 100%;' +
        'visibility: hidden; border: none;' + boxSizing + '"></select>');
    this.trigger = this.node.children().last();
    var trigger = this.trigger[0];
    if (this.options.readonly) {
        this.input.attr("readonly", "readonly").click(pulldownHandler);
        trigger = this.node[0];
    }
    this.setMultiple(this.options.multiple);
    var pulldown = new ManualPulldown(this.node[0], trigger, this.select[0]);

    this.select.change(function() {
        var newVal = self.select.val();
        if (self.options.multiple) {
            var v = self.input.val();
            if (/[^;,]$/.test(v)) v += ";";
            newVal = v + newVal;
        }
        self.input.val(newVal);
        if (pulldown.isVisible) pulldown.close();
    });
    this.node.find("div.ox-button").click(pulldownHandler);
    
    this.options.replace.parentNode.replaceChild(this.node[0],
                                                 this.options.replace);
    
    function pulldownHandler(e) {
        if (PopupMenu.last == pulldown) {
            PopupMenu.last = null;
        } else if (self.count > (self.options.readonly ? 1 : 0)) {
            var size = Math.max(2, Math.min(self.options.size, self.count));
            jQuery(pulldown.node).css({ width: self.node.width() + "px",
                height: (1.5 * size) + "em" });
            pulldown.show();
        }
        e.stopPropagation();
    }
}

ComboBox.prototype = {
    addEntry: function(key, value) {
        this.select.append(jQuery("<option>").val(key)
            .text(value ? expectI18n(value) : key));
        if (!this.count) this.input.val(key);
        if (this.count == (this.options.readonly ? 1 : 0)) {
            this.input.css("right", "1em");
            this.trigger.css("display", "");
            this.select.css("visibility", "visible");
        }
        this.count++;
        this.select.attr("size",
            Math.max(2, Math.min(this.options.size, this.count)));
    },
    clear: function() {
        this.count = 0;
        this.select.empty().css("visibility", "hidden").attr("size", 2);
        this.input.val("");
        this.input.css("right", "0");
        this.trigger.css("display", "none");
    },
    get: function() { return this.input.val(); },
    set: function(value) { this.input.val(value); },
    focus: function() { this.input.focus(); },
    setMultiple: function(multiple) {
        this.options.multiple = multiple;
        this.trigger.removeClass("expandButton");
        if (multiple) {
            this.trigger.css("background-image",
                "url(plugins/com.openexchange.messaging.sms/images/plus.png)");
        } else {
            this.trigger.addClass("expandButton").css("background-image", "");
        }
    }
};

var dialog = ('<div class="background-color-content" ' +
    'style="width: 25em; padding: 1em; border: 1px solid #555;' +
           'font-size: 10pt; border-radius: 10px; -moz-border-radius: 10px;' +
           '-webkit-border-radius: 10px; box-shadow: 1px 1px 5px black;' +
           '-moz-box-shadow: 1px 1px 5px black;' +
           '-webkit-box-shadow: 1px 1px 5px black">' +
    '<div style="margin: -1em -1em 0.5em; background-color: white;' +
                'border-bottom: 1px solid #aaa; padding: 1em 1em 0.5em;' +
                'border-radius-topleft: 10px; border-radius-topright: 10px;' +
                '-moz-border-radius-topleft: 10px;' +
                '-moz-border-radius-topright: 10px;' +
                '-webkit-border-radius-topleft: 10px;' +
                '-webkit-border-radius-topright: 10px;">' +
        '<span style="font-size: 14pt;"></span>' +
        '<span style="color: #555; font-weight: bold;"></span>' +
    '</div>' +
    '<div>' +
        '<table style="width: 100%; height: 4em; margin: 3px 0;" ' +
               'cellpadding="0" cellspacing="0.5em"><tbody><tr>' +
            '<td style="padding-right: 0.5em;">' +
                '<label for="~.from"></label>' +
            '</td><td style="width: 100%; position: relative;">' +
                '<div class="combobox">' +
            '</td>' +
        '</tr><tr>' +
            '<td>' +
                '<label for="~.to" style="margin-right: 0.5em;"></label>' +
            '</td><td style="width: 100%; position: relative;">' +
                '<div class="combobox">' +
            '</td>' +
        '</tr><tr>' +
            '<td colspan="2">' +
                '<div style="height: 90pt; margin: 3px 0;">' +
                    '<textarea style="width: 100%; height: 90pt;' +
                                      boxSizing + 'resize: none;">' +
                    '</textarea>' +
                '</div>' +
                '<label for="~.signature">' +
                    '<input type="checkbox" id="~.signature" ' +
                           'style="vertical-align: text-bottom;' +
                                  'margin: 0 0.5em 0 0;">' +
                    '<span></span>' +
                '</label>' + 
            '</td>' +
        '</tr></tbody></table>' +
    '</div>' +
    '<div style="height: 2em; margin-top: 1em;">' +
        '<table style="width: 100%; height: 100%;" cellpadding="0"' +
               'cellspacing="0"><tbody><tr>' +
            '<td style="font-weight: bold; white-space: nowrap;">' +
                '<span></span><br><span></span>' +
            '</td>' +
            '<td style="width: 100%;">' +
                '<img style="display: none; margin-left: 5px;"' +
                     'src="plugins/com.openexchange.messaging.sms/images/attention.png">' +
            '</td>' +
            '<td style="padding: 0 0.5em;"><button></button></td>' +
            '<td><button></button></td>' +
        '</tr></tbody></table>' +
    '</div>' +
'</div>').replace(/~/g, "com.openexchange.messaging.sms");

var dialogTop = jQuery(
    '<div style="position: absolute; width: 100%; height: 100%">' +
        '<table style="height: 100%; margin: auto;">' +
            '<tbody>' +
                '<tr style="align: center; vertical-align: center">' +
                    '<td>' + dialog + '</td>' +
                '</tr>' +
            '</tbody>' +
        '</table>' +
    '</div>');

var dialog = dialogTop.find("div").first();

var labels = [_("From"), _("To")];
dialog.find("label").each(function(i, label) {
    if (i < labels.length) label.appendChild(addTranslated(labels[i]));
});

var spans = dialog.find("span");
spans[0].appendChild(addTranslated(_("Send SMS")));
var serverStatus = jQuery(spans[1]);
var clientStatus = spans.eq(-2);
var clientStatus2 = spans.last();
var clientStatusIcon = dialog.find("img");

var buttons = [{ title: _("Send"), click: send },
               { title: _("Cancel"), click: close }];
dialog.find("button").each(function(i, button) {
    buttons[i] = jQuery.button(buttons[i]);
    button.parentNode.replaceChild(buttons[i][0], button);
});

function enableButton(button, enabled) {
    if (enabled) {
        button.removeAttr("disabled");
    } else {
        button.attr("disabled", "disabled");
    }
    button.toggleClass("ox-button-disabled", !enabled);
}

var comboboxes = dialog.find("div.combobox");
var from = new ComboBox({ replace: comboboxes[0], readonly: true,
                          id: "com.openexchange.messaging.sms.to" });
var to = new ComboBox({ replace: comboboxes[1],
                        id: "com.openexchange.messaging.sms.to" });
var content = dialog.find("textarea");
var signature = dialog.find("#com\\.openexchange\\.messaging\\.sms\\.signature")
    .click(updateStatus);

var signatureOption = false, signatureText = null;
var total = 0;
var multisms = false;
var smsLimit = 0;
var captchaKey = "", captchaDiv;
var mms = null;
var recipientLimit = 1;
var blacklist = null;
var numberFilter = /[^0-9#*+]/g;

function updateStatus() {
    if (!total && !smsLimit) return;
    var used = content.val().length;
    if (signatureText && signature[0].checked) {
        used += signatureText.length + 2; // +2 is for the newlines
    }
    if (multisms) {
        var msgs = Math.ceil(used / total);
        //#. Character count status
        //#. %d is the number of used characters
        clientStatus.text(expectI18n(format(
            ngettext("%d character", "%d characters", used), used)));
        if (smsLimit) {
            //#. Limited message count status
            //#. %1$d is the number of messages
            //#. %2$d is the maximum number of messages
            clientStatus2.text(expectI18n(format(
                _("%1$d / %2$d messages"), msgs, smsLimit)));
            setValid(msgs <= smsLimit);
        } else {
            //#. Unlimited message count status
            //#. %d is the number of messages
            clientStatus2.text(expectI18n(format(
                ngettext("%d message", "%d messages", msgs), msgs)));
        }
    } else {
        //#. Character count status
        //#. %1$d is the number of used characters
        //#. %2$d is the number of remaining characters
        //#. %3$d is the total number of available characters
        clientStatus.text(expectI18n(format(
            _("%1$d / %3$d"), used, total - used, total)));
        setValid(used <= total);
    }
    function setValid(valid) {
        clientStatus.parent().css("color", valid ? "#555" : "red");
        clientStatusIcon.toggle(!valid);
        enableButton(buttons[0], valid);
    }
}
content.bind('input', updateStatus);
content.change(updateStatus);
content.keyup(updateStatus);

ox.JSON.get(AjaxRoot + "/messaging/sms?action=getconfig&session=" + session,
    function(reply) {
        total = reply.data.length;
        multisms = reply.data.multisms;
        if (multisms) smsLimit = reply.data.smsLimit;
        signatureOption = reply.data.signatureoption;
        if (reply.data.blacklist) {
            blacklist = new RegExp("^(?:" + reply.data.blacklist + ")$");
        }
        if (reply.data.numCleanRegEx) {
            numberFilter = new RegExp(reply.data.numCleanRegEx, "gi");
        }
        recipientLimit = reply.data.recipientLimit || 1;
        if (recipientLimit > 1) {
            to.setMultiple(true);
            to.input.attr("title", expectI18n(
                _("Multiple recipients can be specified by separating them with a semicolon.")));
        }
        if (reply.data.mms) {
            mms = { ids: [] };
            var tbody = dialog.find("tbody").first();
            mms.preview = jQuery('<tr><td colspan="2">' +
                '<div style="max-height: 5em; margin: 3px 0;' +
                            'overflow-y: auto; position: relative;"></div>' +
            '</td></tr>').appendTo(tbody).find("div");
            var tr = jQuery('<tr>' +
                '<td style="padding-right: 0.5em;"></td>' +
                '<td>' +
                    '<form method="POST" enctype="multipart/form-data" ' +
                          'target="dlIframe" style="width: 100%; margin: 0" ' +
                          'action="' + AjaxRoot + '/file?action=new&session=' +
                                       session + '&module=mail&type=file">' +
                        '<input name="file" type="file" accept="image/*" ' +
                               'style="width: 100%">' +
                    '</form>' +
                '</td>' +
            '</tr>').appendTo(tbody);
            tr.find("td").first().append(addTranslated(_("MMS")));
            mms.input = tr.find("input").change(uploadMMS);
            mms.form = tr.find("form")[0];
        }
        if (reply.data.captcha) {
            jQuery('<script type="text/javascript" src="'
                    + '[protocol]://www.google.com/recaptcha/api/js/recaptcha_ajax.js'.format() 
                    + '"></script>')
                .appendTo(jQuery("head"));
            captchaDiv = jQuery('<div style="width: 318px; margin: auto;' +
                                            'position: relative;"></div>')
                .insertBefore(dialog.children().last())[0];
            ox.JSON.get(AjaxRoot + "/recaptcha?action=key&session=" + session,
                function(reply) { captchaKey = reply.data; });
        }
        var addresses = reply.data.addresses;
        if (addresses.length) {
            for (var i = 0; i < addresses.length; i++) {
                from.addEntry(addresses[i]);
            }
        } else {
            dialog.find("tr")[0].style.display = "none";
        }
    });

var phoneFields = ["cellular_telephone1", "cellular_telephone2", "telephone_ip",
                   "telephone_business1", "telephone_business2",
                   "telephone_callback", "telephone_car", "telephone_company",
                   "telephone_home1", "telephone_home2", "telephone_other",
                   "telephone_isdn", "telephone_pager", "telephone_primary",
                   "telephone_radio", "telephone_telex", "telephone_ttytdd",
                   "telephone_assistant"];
var phoneLabels = [
    //#. cellular_telephone1
    _("Mobile"),
    //#. cellular_telephone2
    _("Mobile 2"),
    //#. telephone_ip
    _("IP phone"),
    //#. telephone_business1
    _("Business"),
    //#. telephone_business2
    _("Business 2"),
    //#. telephone_callback
    _("Callback"),
    //#. telephone_car
    _("Car phone"),
    //#. telephone_company
    _("Company"),
    //#. telephone_home1
    _("Private"),
    //#. telephone_home2
    _("Private 2"),
    //#. telephone_other
    _("Other"),
    //#. telephone_isdn
    _("ISDN"),
    //#. telephone_pager
    _("Pager"),
    //#. telephone_primary
    _("Primary"),
    //#. telephone_radio
    _("Radio"),
    //#. telephone_telex
    _("Telex"),
    //#. telephone_ttytdd
    _("TTY/TDD"),
    //#. telephone_assistant
    _("Assistant")
];

function open(contact) {
    to.clear();
    signatureText = signatureOption &&
        configGetKey("modules.mail.defaultaddress");
    //#. %s is the email address used as signature
    signature.next().text(expectI18n(format(_("Add signature: %s"),
                                            noI18n(signatureText))));
    signature.parent().toggle(Boolean(signatureText));
    if (contact) {
        var hasNumbers = false;
        for (var i = 0; i < phoneFields.length; i++) {
            var field = contact[phoneFields[i]];
            if (!field) continue;
            hasNumbers = true;
            //#. %1$s is the phone number
            //#. %2$s is the type of phone number (mobile/private/...)
            to.addEntry(field, format(_("%1$s (%2$s)"), field, phoneLabels[i]));
        }
        if (!hasNumbers) {
            ox.UINotifier.warn(_("The selected contact has no phone number."));
        }
    }
    ox.JSON.get(AjaxRoot + "/messaging/sms?action=getstatus&session=" + session,
        function(reply) {
            serverStatus.text(reply.data.display_string);
            var spareHeight = reply.data.display_string ? 0 : 1.5;
            if (captchaKey) {
                Recaptcha.create(captchaKey, captchaDiv, {
                    theme: "white",
                    lang: (config.language || "en").substring(0, 2)
                });
            }
            updateStatus();
            ox.api.setModal(true);
            jQuery("#modal-dialog").append(dialogTop);
            setTimeout(function() { (to.get() ? content : to).focus(); }, 0);
        });
    return true;
}

function close() {
    ox.api.setModal(false);
    jQuery(dialogTop).detach();
    to.set("");
    content.val("");
    if (mms) {
        if (mms.interval) {
            clearInterval(mms.interval);
            mms.interval = undefined;
        }
        mms.preview.empty();
        mms.ids.length = 0;
    }
}

function addMMSImage(src, alt) {
    var index = mms.ids.length;
    var image = jQuery('<span tabindex="0" ' +
        'style="position: relative; display: inline-block; margin: 3px;">' +
        '<img style="vertical-align: middle; border: 1px solid #aaa;' +
                    'max-width: 4em; max-height: 4em;" ' +
             'src="' + src + '" alt="' + alt + '">' +
        '<img style="position: absolute; right: 0; top: 0; display: none;" ' +
             'src="' + getFullImgSrc("img/menu/delete.gif") + '">' +
    '</span>').appendTo(mms.preview)
        .keydown(function(e) { if (e.which == 46) remove(); }) // Delete key
        .focus(function() { del.show(); })
        .blur(function() { del.hide(); });
    var del = image.children().last().click(remove);
    return image.children().first();
    
    function remove() {
        image.detach();
        mms.ids[index] = null;
    }
}

function uploadMMS() {
    var loading = addMMSImage(getFullImgSrc("img/ox_animated_withoutbg.gif"),
                              mms.input.val());
    var oldHandler = window.callback_new;
    window.callback_new = function(reply) {
        if (reply.error) {
            loading.detach();
            triggerEvent("OX_New_Error", 4, formatError(reply));
        } else {
            window.callback_new = oldHandler;
            var id = reply.data[0];
            loading.attr("src", AjaxRoot + "/file?action=get&session=" +
                                session + "&id=" + id);
            mms.ids.push(id);
            mms.form.reset();
            if (mms.interval === undefined) {
                mms.interval = setInterval(function() {
                    var requests = [];
                    for (var i = 0; i < mms.ids.length; i++) {
                        if (mms.ids[i]) {
                            requests.push({ module: "file", action: "keepalive",
                                            id: mms.ids[i] });
                        }
                    }
                    if (requests.length) {
                        ox.JSON.put(AjaxRoot + "/multiple?session=" + session +
                            "&continue=true", requests, emptyFunction);
                    }
                }, 4.5*60*1000); // 4.5 minutes
            }
        }
    }
    mms.form.submit();
}

function send() {
    function err(message) { ox.Configuration.error(message); }
    var numbers = to.get().split(";");
    for (var i = 0, j = 0; i < numbers.length; i++) {
        var n = numbers[i].replace(numberFilter, "");
        if (!n) continue;
        if (blacklist && blacklist.test(n)) return err(format(
            //#. %s is the invalid phone number
            _("Invalid phone number: '%s'. Special numbers are not allowed."),
            numbers[i]));
        numbers[j++] = n;
    }
    numbers.length = j;
    if (!j) return err(_("Please specify a recipient number."));
    if (j > recipientLimit) {
        if (recipientLimit == 1) {
            return err(_("Multiple recipients are not supported."));
        } else {
            //#. %d is the maximum number of recipients.
            return err(format(
                _("More than %d recipients are not supported. Please remove some recipients."),
                recipientLimit));
        }
    }
    var msg = { from: from.get(), to: numbers.join(";"), body: content.val() };
    if (signatureText && signature[0].checked) {
        msg.body += "\n\n" + signatureText;
    }
    if (captchaKey) {
        msg.captcha = { challenge: Recaptcha.get_challenge(),
                        response: Recaptcha.get_response() };
    }
    if (mms) {
        var attachments = [];
        for (var i = 0; i < mms.ids.length; i++) {
            if (mms.ids[i]) attachments.push(mms.ids[i]);
        }
        if (attachments.length) msg.attachments = attachments;
    }
    ox.JSON.put(AjaxRoot + "/messaging/sms?action=send&session=" + session, msg,
        function() {
            triggerEvent("com.openexchange.messaging.sms/sent");
            close();
        }, function(result, status) {
            if (   !status && result.code == "MESSAGING-0017"
                && result.error_params[0] == "response")
            {
                ox.Configuration.error(_("Invalid CAPTCHA, please try again."));
                Recaptcha.reload();
                return true;
            }
            if (  !status && result.code == "MESSAGING-0001") {   
                ox.Configuration.error(result.error_params[0]
                    .replace(/.*Message=/ig,"").replace(/ exceptionID.*/ig,""));
                if ( Recaptcha && Recaptcha.reload ) Recaptcha.reload();
                return true;
            }
        });
}

register("com.openexchange.messaging.sms.open", open);

var simpleToolbar = ox.api.config.get("ui.global.toolbar.mode.value") ==
                    "simple";
jQuery.map(["calendarCreate", "contactsCreate", "infostoreCreate",
            "mailCreate", "portalCreate", "tasksCreate"],
    function(name) {
        var section = ox.widgets.toolBar.sections[name];
        var item = new ox.gui.MenuItem({
            title: _("New SMS"),
            abbr: _("SMS"),
            icons: ["plugins/com.openexchange.messaging.sms/images/sms.png"],
            pathPrefix: "",
            big: true,
            action: function () { open(); }
        });
        if (simpleToolbar) section.add(item); else section.insert(item, 1);
    });

register("PAINT_CONTACT", function(e) {
    if (e.contact.distribution_list) return;
    var img = jQuery('<img style="cursor: pointer">').attr("src", urlify(
            "plugins/com.openexchange.messaging.sms/images/sms_small.png"))
        .click(function() {
            // ensure necessary data (phone data might be missing)
            var collection = { objects: [e.contact], columns: phoneFields };
            OXCache.newRequest(null, "contacts", collection, null,
                function(data) {
                    var contact = data.objects[0];
                    open(contact);
                    // hide potential hover
                    if (e.hover) e.hover.enable(); // resets hover (includes hide)
                });
        });
    switch (e.view) {
        case "card":
            // get card & first child
            var card = e.node.firstChild, first = card.firstChild;
            // adjust image
            img.css({ align: "absmiddle" }).addClass("cardHeaderIcons")
                .appendTo(first);
            break;
            
        case "hover":
            // get target node (jquery would be great here)
            var target = e.node.childNodes[1].firstChild.firstChild.childNodes[1].firstChild;
            var div = jQuery('<div style="padding-right: 3px"' +
                             'class="cardHoverIconRight">').append(img)[0];
            target.insertBefore(div, target.firstChild || null);
            break;
    }
});
