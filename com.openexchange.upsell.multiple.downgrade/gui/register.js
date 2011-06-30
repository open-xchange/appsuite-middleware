// the downgrade page for demo purposes
var innode = new ox.Configuration.InnerNode("configuration/com.openexchange.upsell.multiple.downgrade", _("OX Access-Level"));

var downgradepanel = new ox.Configuration.LeafNode("configuration/com.openexchange.upsell.multiple.downgrade/downgrade_panel", _("Downgrade"));

var downpage = new ox.Configuration.Page(downgradepanel, _("Change Access Permissions for your OX Context"),false);

var intro = new ox.UI.Text(_("The Downgrade Premium function will downgrade an Open-Xchange Premium Groupware context (tenant) to a simple Open-Xchange PIM context (tenant). Please re-login to the context to use the downgraded version. At any time you can UPGRADE again to the Premium Groupware by simply clicking the upsell triggers (e.g. InfoStore icon in the main module bar) and follow the upsell process."));
downpage.addWidget(intro);

var button = new ox.UI.Button(_("Downgrade"));

button.click = function (){

// method which is executed after succesfull http call to change context
function dwnsuc(data) {
   ox.Configuration.info(_("Your context has been downgraded."));
}


// downgrade context via http call
if(this.enabled){
        ox.JSON.get("/ajax/upsell/multiple?session="+parent.session+"&action=change_context_permissions", dwnsuc);
}       

// disable button after upgrade
this.disable();

}

// add button widget to page
downpage.addWidget(button);

