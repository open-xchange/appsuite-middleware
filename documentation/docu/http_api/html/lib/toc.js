
function createLink(href, innerHTML) {
	var a = document.createElement("a");
	a.setAttribute("href", href);
	a.innerHTML = innerHTML;
	return a;
}

function generateRequestTOC() {

	// get the OX HTTP API item
	var toc = document.getElementById("API_ITEM");
	var tocList = document.querySelectorAll(".toggleEndpointList1");
	var subtoc = toc.appendChild(document.createElement("ul"));
	subtoc.className = 'tocList2';
	for (var i = 0; i < tocList.length; ++i) {
		
		var tagName = $(tocList[i]).data("id");
		var node = document.createElement("li");
		node.className = 'hideItem';
		var aTag = createLink("#resource_" + tagName, tagName);
		aTag.className = 'tocATag';
		subtoc.appendChild(node).appendChild(aTag);
		
	}
	document.getElementById("expandRequestToc").style.display="inline";
}

function generateTOC(toc) {
	
	var mainToc = toc.appendChild(document.createElement("ul"));
	mainToc.className = 'tocList';
	
	var headers = $('#before_insert_div h1');

	for (var i = 0; i < headers.length; ++i) {
		
		var tocName = headers[i].innerHTML;
		var tocID = headers[i].id;
		var node = document.createElement("li");
		node.className = 'tocItem';
		var aTag = createLink("#" + tocID, tocName);
		aTag.className = 'tocATag';
		mainToc.appendChild(node).appendChild(aTag);	
	}

	// insert OX HTTP API Node
	var node = document.createElement("li");
	node.className = 'tocItem';
	node.id = "API_ITEM"
	var aTag = createLink("#API-Title" , "OX HTTP API");
	aTag.className = 'tocATag';
	var tmpNode = mainToc.appendChild(node);
	tmpNode.appendChild(aTag);	

	var plusTag = createLink("#" , "[+]");
	plusTag.id = "expandRequestToc";
	plusTag.onclick = function(){toggleRequestTOC()};
	plusTag.className = 'tocPlusTag';
	tmpNode.appendChild(plusTag);
	
	var headers2 = $('#after_insert_div h1');

	for (var i = 0; i < headers2.length; ++i) {
		
		var tocName = headers2[i].innerHTML;
		var tocID = headers2[i].id;
		var node = document.createElement("li");
		node.className = 'tocItem';
		var aTag = createLink("#" + tocID, tocName);
		aTag.className = 'tocATag';
		mainToc.appendChild(node).appendChild(aTag);	
	}

}

function addToggleAllButton(){
	var parent = document.getElementById('searchDiv');
	/* var button = "<a href=\"#\" onclick=\"toggleAll();\">Show all</a>"; */

	var divWrapper = document.createElement('div');
	divWrapper.className="oxShowButtonDiv";

	// add hide all	
	var button = document.createElement('button');
	var buttonText = document.createTextNode("Hide all");
	button.appendChild(buttonText);
	button.onclick = function(){toggleAll(false)};
	button.className='oxButton';
	divWrapper.appendChild(button);

	// add show all
	var button2 = document.createElement('button');
	var buttonText2 = document.createTextNode("Show all");
	button2.appendChild(buttonText2);
	button2.onclick = function(){toggleAll(true)};
	button2.className='oxButton';
	divWrapper.appendChild(button2);
	
	parent.insertBefore(divWrapper, null);
}

function toggleAll(show) {
	
	var itemSelector=".endpoints,.content";
	var items =	$(itemSelector);
	items.each(function() {
		var item = $(this);
		item.toggle(show);
	});
}

var show=true;
function toggleRequestTOC(){
	var items = document.querySelectorAll(".hideItem");
	if(show){
		for(var i=0; i<items.length; ++i){
			items[i].style.display = 'list-item';
		}

		var aTag = document.getElementById("expandRequestToc");
		aTag.innerHTML = "[-]";
		show=false;
	} else {
		for(var i=0; i<items.length; ++i){
			items[i].style.display = 'none';
		}
		var aTag = document.getElementById("expandRequestToc");
		aTag.innerHTML = "[+]";
		show=true;
	}
}


function addATagHandler() {

$('a').on('click', function (e) { 
	var node = $(e.target);
	var href = node.attr('href');
	if (href.indexOf('#') !== 0) return;

	debugger;
	e.preventDefault();

	var target = document.getElementById(href.substr(1));

	var parent = target.closest('.endpoints');

	$(parent).show();

	target.scrollIntoView();});

}