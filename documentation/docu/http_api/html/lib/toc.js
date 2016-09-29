
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
		aTag.className = 'tocATagHide';
		subtoc.appendChild(node).appendChild(aTag);
		
	}
	document.getElementById("expandRequestToc").style.display="table-cell";
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
	node.className = 'tocItem tocTable';
	node.id = "API_ITEM"
	var aTag = createLink("#API-Title" , "OX HTTP API");
	aTag.className = 'tocATag tocATagCell';
	var tmpNode = mainToc.appendChild(node);

	var nodeTable = document.createElement("div");
	nodeTable.className = 'tocTable';
	nodeTable.id = "API_ITEM_TABLE"
	tmpNode.appendChild(nodeTable);	
	nodeTable.appendChild(aTag);	

	var plusTag = createLink("#" , "&#x25BC;");
	plusTag.id = "expandRequestToc";
	plusTag.onclick = function(){toggleRequestTOC()};
	plusTag.className = 'tocPlusTag';
	nodeTable.appendChild(plusTag);
	
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


function generateChapterTOC(chapter) {
	
	var toc = chapter.parentNode.insertBefore(document.createElement("div"), chapter.nextSibling);
	var mainToc = toc.appendChild(document.createElement("ol"));
	mainToc.className = 'tocChapterList';
	var id=chapter.id;
	
	var headers = $('#'+id).nextUntil('h1').filter('h2');

	for (var i = 0; i < headers.length; ++i) {
		
		var tocName = headers[i].innerHTML;
		var tocID = headers[i].id;
		var node = document.createElement("li");
		node.className = 'tocItemInline';
		var aTag = createLink("#" + tocID, tocName);
		aTag.className = 'tocATagInline';
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
	var parent;
	var items = document.querySelectorAll(".hideItem");
	if(show){
		for(var i=0; i<items.length; ++i){
			if(parent==null){
				parent = items[i].parentElement;
				parent.style.display = 'block';
			}
			items[i].style.display = 'list-item';
		}

		var aTag = document.getElementById("expandRequestToc");
		aTag.innerHTML = "&#x25B2;";
		show=false;
	} else {
		for(var i=0; i<items.length; ++i){
			if(parent==null){
				parent = items[i].parentElement;
				parent.style.display = 'none';
			}
			items[i].style.display = 'none';
		}
		var aTag = document.getElementById("expandRequestToc");
		aTag.innerHTML = "&#x25BC;";
		show=true;
	}
}


function addATagHandler() {

$('a').on('click', function (e) { 
	var node = $(e.target);
	var href = node.attr('href');
	if (href.indexOf('#') !== 0 || href.indexOf('#!') === 0) return;

	e.preventDefault();
	var target = document.getElementById(href.substr(1));
	if(target==null){
		return;
	}
	var endpoints = target.closest('.endpoints');
	$(endpoints).show();
	var content = $(target.parentNode.parentNode).children('.content');
	$(content).show();

	target.scrollIntoView();
});

}

$(document).ready(function(){

	// Der Button wird mit JavaScript erzeugt und vor dem Ende des body eingebunden.
	var back_to_top_button = ['<a href="#top" class="back-to-top">&uArr;</a>'].join("");
	$("body").append(back_to_top_button)

	// Der Button wird ausgeblendet
	$(".back-to-top").hide();

	// Funktion für das Scroll-Verhalten
	$(function () {
		$(window).scroll(function () {
			if ($(this).scrollTop() > 100) { // Wenn 100 Pixel gescrolled wurde
				$('.back-to-top').fadeIn();
			} else {
				$('.back-to-top').fadeOut();
			}
		});

		$('.back-to-top').click(function () { // Klick auf den Button
			$('body,html').animate({
				scrollTop: 0
			}, 800);
			return false;
		});
	});

});