/*! jQuery v1.7.2 jquery.com | jquery.org/license */
(function(a,b){function cy(a){return f.isWindow(a)?a:a.nodeType===9?a.defaultView||a.parentWindow:!1}function cu(a){if(!cj[a]){var b=c.body,d=f("<"+a+">").appendTo(b),e=d.css("display");d.remove();if(e==="none"||e===""){ck||(ck=c.createElement("iframe"),ck.frameBorder=ck.width=ck.height=0),b.appendChild(ck);if(!cl||!ck.createElement)cl=(ck.contentWindow||ck.contentDocument).document,cl.write((f.support.boxModel?"<!doctype html>":"")+"<html><body>"),cl.close();d=cl.createElement(a),cl.body.appendChild(d),e=f.css(d,"display"),b.removeChild(ck)}cj[a]=e}return cj[a]}function ct(a,b){var c={};f.each(cp.concat.apply([],cp.slice(0,b)),function(){c[this]=a});return c}function cs(){cq=b}function cr(){setTimeout(cs,0);return cq=f.now()}function ci(){try{return new a.ActiveXObject("Microsoft.XMLHTTP")}catch(b){}}function ch(){try{return new a.XMLHttpRequest}catch(b){}}function cb(a,c){a.dataFilter&&(c=a.dataFilter(c,a.dataType));var d=a.dataTypes,e={},g,h,i=d.length,j,k=d[0],l,m,n,o,p;for(g=1;g<i;g++){if(g===1)for(h in a.converters)typeof h=="string"&&(e[h.toLowerCase()]=a.converters[h]);l=k,k=d[g];if(k==="*")k=l;else if(l!=="*"&&l!==k){m=l+" "+k,n=e[m]||e["* "+k];if(!n){p=b;for(o in e){j=o.split(" ");if(j[0]===l||j[0]==="*"){p=e[j[1]+" "+k];if(p){o=e[o],o===!0?n=p:p===!0&&(n=o);break}}}}!n&&!p&&f.error("No conversion from "+m.replace(" "," to ")),n!==!0&&(c=n?n(c):p(o(c)))}}return c}function ca(a,c,d){var e=a.contents,f=a.dataTypes,g=a.responseFields,h,i,j,k;for(i in g)i in d&&(c[g[i]]=d[i]);while(f[0]==="*")f.shift(),h===b&&(h=a.mimeType||c.getResponseHeader("content-type"));if(h)for(i in e)if(e[i]&&e[i].test(h)){f.unshift(i);break}if(f[0]in d)j=f[0];else{for(i in d){if(!f[0]||a.converters[i+" "+f[0]]){j=i;break}k||(k=i)}j=j||k}if(j){j!==f[0]&&f.unshift(j);return d[j]}}function b_(a,b,c,d){if(f.isArray(b))f.each(b,function(b,e){c||bD.test(a)?d(a,e):b_(a+"["+(typeof e=="object"?b:"")+"]",e,c,d)});else if(!c&&f.type(b)==="object")for(var e in b)b_(a+"["+e+"]",b[e],c,d);else d(a,b)}function b$(a,c){var d,e,g=f.ajaxSettings.flatOptions||{};for(d in c)c[d]!==b&&((g[d]?a:e||(e={}))[d]=c[d]);e&&f.extend(!0,a,e)}function bZ(a,c,d,e,f,g){f=f||c.dataTypes[0],g=g||{},g[f]=!0;var h=a[f],i=0,j=h?h.length:0,k=a===bS,l;for(;i<j&&(k||!l);i++)l=h[i](c,d,e),typeof l=="string"&&(!k||g[l]?l=b:(c.dataTypes.unshift(l),l=bZ(a,c,d,e,l,g)));(k||!l)&&!g["*"]&&(l=bZ(a,c,d,e,"*",g));return l}function bY(a){return function(b,c){typeof b!="string"&&(c=b,b="*");if(f.isFunction(c)){var d=b.toLowerCase().split(bO),e=0,g=d.length,h,i,j;for(;e<g;e++)h=d[e],j=/^\+/.test(h),j&&(h=h.substr(1)||"*"),i=a[h]=a[h]||[],i[j?"unshift":"push"](c)}}}function bB(a,b,c){var d=b==="width"?a.offsetWidth:a.offsetHeight,e=b==="width"?1:0,g=4;if(d>0){if(c!=="border")for(;e<g;e+=2)c||(d-=parseFloat(f.css(a,"padding"+bx[e]))||0),c==="margin"?d+=parseFloat(f.css(a,c+bx[e]))||0:d-=parseFloat(f.css(a,"border"+bx[e]+"Width"))||0;return d+"px"}d=by(a,b);if(d<0||d==null)d=a.style[b];if(bt.test(d))return d;d=parseFloat(d)||0;if(c)for(;e<g;e+=2)d+=parseFloat(f.css(a,"padding"+bx[e]))||0,c!=="padding"&&(d+=parseFloat(f.css(a,"border"+bx[e]+"Width"))||0),c==="margin"&&(d+=parseFloat(f.css(a,c+bx[e]))||0);return d+"px"}function bo(a){var b=c.createElement("div");bh.appendChild(b),b.innerHTML=a.outerHTML;return b.firstChild}function bn(a){var b=(a.nodeName||"").toLowerCase();b==="input"?bm(a):b!=="script"&&typeof a.getElementsByTagName!="undefined"&&f.grep(a.getElementsByTagName("input"),bm)}function bm(a){if(a.type==="checkbox"||a.type==="radio")a.defaultChecked=a.checked}function bl(a){return typeof a.getElementsByTagName!="undefined"?a.getElementsByTagName("*"):typeof a.querySelectorAll!="undefined"?a.querySelectorAll("*"):[]}function bk(a,b){var c;b.nodeType===1&&(b.clearAttributes&&b.clearAttributes(),b.mergeAttributes&&b.mergeAttributes(a),c=b.nodeName.toLowerCase(),c==="object"?b.outerHTML=a.outerHTML:c!=="input"||a.type!=="checkbox"&&a.type!=="radio"?c==="option"?b.selected=a.defaultSelected:c==="input"||c==="textarea"?b.defaultValue=a.defaultValue:c==="script"&&b.text!==a.text&&(b.text=a.text):(a.checked&&(b.defaultChecked=b.checked=a.checked),b.value!==a.value&&(b.value=a.value)),b.removeAttribute(f.expando),b.removeAttribute("_submit_attached"),b.removeAttribute("_change_attached"))}function bj(a,b){if(b.nodeType===1&&!!f.hasData(a)){var c,d,e,g=f._data(a),h=f._data(b,g),i=g.events;if(i){delete h.handle,h.events={};for(c in i)for(d=0,e=i[c].length;d<e;d++)f.event.add(b,c,i[c][d])}h.data&&(h.data=f.extend({},h.data))}}function bi(a,b){return f.nodeName(a,"table")?a.getElementsByTagName("tbody")[0]||a.appendChild(a.ownerDocument.createElement("tbody")):a}function U(a){var b=V.split("|"),c=a.createDocumentFragment();if(c.createElement)while(b.length)c.createElement(b.pop());return c}function T(a,b,c){b=b||0;if(f.isFunction(b))return f.grep(a,function(a,d){var e=!!b.call(a,d,a);return e===c});if(b.nodeType)return f.grep(a,function(a,d){return a===b===c});if(typeof b=="string"){var d=f.grep(a,function(a){return a.nodeType===1});if(O.test(b))return f.filter(b,d,!c);b=f.filter(b,d)}return f.grep(a,function(a,d){return f.inArray(a,b)>=0===c})}function S(a){return!a||!a.parentNode||a.parentNode.nodeType===11}function K(){return!0}function J(){return!1}function n(a,b,c){var d=b+"defer",e=b+"queue",g=b+"mark",h=f._data(a,d);h&&(c==="queue"||!f._data(a,e))&&(c==="mark"||!f._data(a,g))&&setTimeout(function(){!f._data(a,e)&&!f._data(a,g)&&(f.removeData(a,d,!0),h.fire())},0)}function m(a){for(var b in a){if(b==="data"&&f.isEmptyObject(a[b]))continue;if(b!=="toJSON")return!1}return!0}function l(a,c,d){if(d===b&&a.nodeType===1){var e="data-"+c.replace(k,"-$1").toLowerCase();d=a.getAttribute(e);if(typeof d=="string"){try{d=d==="true"?!0:d==="false"?!1:d==="null"?null:f.isNumeric(d)?+d:j.test(d)?f.parseJSON(d):d}catch(g){}f.data(a,c,d)}else d=b}return d}function h(a){var b=g[a]={},c,d;a=a.split(/\s+/);for(c=0,d=a.length;c<d;c++)b[a[c]]=!0;return b}var c=a.document,d=a.navigator,e=a.location,f=function(){function J(){if(!e.isReady){try{c.documentElement.doScroll("left")}catch(a){setTimeout(J,1);return}e.ready()}}var e=function(a,b){return new e.fn.init(a,b,h)},f=a.jQuery,g=a.$,h,i=/^(?:[^#<]*(<[\w\W]+>)[^>]*$|#([\w\-]*)$)/,j=/\S/,k=/^\s+/,l=/\s+$/,m=/^<(\w+)\s*\/?>(?:<\/\1>)?$/,n=/^[\],:{}\s]*$/,o=/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,p=/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,q=/(?:^|:|,)(?:\s*\[)+/g,r=/(webkit)[ \/]([\w.]+)/,s=/(opera)(?:.*version)?[ \/]([\w.]+)/,t=/(msie) ([\w.]+)/,u=/(mozilla)(?:.*? rv:([\w.]+))?/,v=/-([a-z]|[0-9])/ig,w=/^-ms-/,x=function(a,b){return(b+"").toUpperCase()},y=d.userAgent,z,A,B,C=Object.prototype.toString,D=Object.prototype.hasOwnProperty,E=Array.prototype.push,F=Array.prototype.slice,G=String.prototype.trim,H=Array.prototype.indexOf,I={};e.fn=e.prototype={constructor:e,init:function(a,d,f){var g,h,j,k;if(!a)return this;if(a.nodeType){this.context=this[0]=a,this.length=1;return this}if(a==="body"&&!d&&c.body){this.context=c,this[0]=c.body,this.selector=a,this.length=1;return this}if(typeof a=="string"){a.charAt(0)!=="<"||a.charAt(a.length-1)!==">"||a.length<3?g=i.exec(a):g=[null,a,null];if(g&&(g[1]||!d)){if(g[1]){d=d instanceof e?d[0]:d,k=d?d.ownerDocument||d:c,j=m.exec(a),j?e.isPlainObject(d)?(a=[c.createElement(j[1])],e.fn.attr.call(a,d,!0)):a=[k.createElement(j[1])]:(j=e.buildFragment([g[1]],[k]),a=(j.cacheable?e.clone(j.fragment):j.fragment).childNodes);return e.merge(this,a)}h=c.getElementById(g[2]);if(h&&h.parentNode){if(h.id!==g[2])return f.find(a);this.length=1,this[0]=h}this.context=c,this.selector=a;return this}return!d||d.jquery?(d||f).find(a):this.constructor(d).find(a)}if(e.isFunction(a))return f.ready(a);a.selector!==b&&(this.selector=a.selector,this.context=a.context);return e.makeArray(a,this)},selector:"",jquery:"1.7.2",length:0,size:function(){return this.length},toArray:function(){return F.call(this,0)},get:function(a){return a==null?this.toArray():a<0?this[this.length+a]:this[a]},pushStack:function(a,b,c){var d=this.constructor();e.isArray(a)?E.apply(d,a):e.merge(d,a),d.prevObject=this,d.context=this.context,b==="find"?d.selector=this.selector+(this.selector?" ":"")+c:b&&(d.selector=this.selector+"."+b+"("+c+")");return d},each:function(a,b){return e.each(this,a,b)},ready:function(a){e.bindReady(),A.add(a);return this},eq:function(a){a=+a;return a===-1?this.slice(a):this.slice(a,a+1)},first:function(){return this.eq(0)},last:function(){return this.eq(-1)},slice:function(){return this.pushStack(F.apply(this,arguments),"slice",F.call(arguments).join(","))},map:function(a){return this.pushStack(e.map(this,function(b,c){return a.call(b,c,b)}))},end:function(){return this.prevObject||this.constructor(null)},push:E,sort:[].sort,splice:[].splice},e.fn.init.prototype=e.fn,e.extend=e.fn.extend=function(){var a,c,d,f,g,h,i=arguments[0]||{},j=1,k=arguments.length,l=!1;typeof i=="boolean"&&(l=i,i=arguments[1]||{},j=2),typeof i!="object"&&!e.isFunction(i)&&(i={}),k===j&&(i=this,--j);for(;j<k;j++)if((a=arguments[j])!=null)for(c in a){d=i[c],f=a[c];if(i===f)continue;l&&f&&(e.isPlainObject(f)||(g=e.isArray(f)))?(g?(g=!1,h=d&&e.isArray(d)?d:[]):h=d&&e.isPlainObject(d)?d:{},i[c]=e.extend(l,h,f)):f!==b&&(i[c]=f)}return i},e.extend({noConflict:function(b){a.$===e&&(a.$=g),b&&a.jQuery===e&&(a.jQuery=f);return e},isReady:!1,readyWait:1,holdReady:function(a){a?e.readyWait++:e.ready(!0)},ready:function(a){if(a===!0&&!--e.readyWait||a!==!0&&!e.isReady){if(!c.body)return setTimeout(e.ready,1);e.isReady=!0;if(a!==!0&&--e.readyWait>0)return;A.fireWith(c,[e]),e.fn.trigger&&e(c).trigger("ready").off("ready")}},bindReady:function(){if(!A){A=e.Callbacks("once memory");if(c.readyState==="complete")return setTimeout(e.ready,1);if(c.addEventListener)c.addEventListener("DOMContentLoaded",B,!1),a.addEventListener("load",e.ready,!1);else if(c.attachEvent){c.attachEvent("onreadystatechange",B),a.attachEvent("onload",e.ready);var b=!1;try{b=a.frameElement==null}catch(d){}c.documentElement.doScroll&&b&&J()}}},isFunction:function(a){return e.type(a)==="function"},isArray:Array.isArray||function(a){return e.type(a)==="array"},isWindow:function(a){return a!=null&&a==a.window},isNumeric:function(a){return!isNaN(parseFloat(a))&&isFinite(a)},type:function(a){return a==null?String(a):I[C.call(a)]||"object"},isPlainObject:function(a){if(!a||e.type(a)!=="object"||a.nodeType||e.isWindow(a))return!1;try{if(a.constructor&&!D.call(a,"constructor")&&!D.call(a.constructor.prototype,"isPrototypeOf"))return!1}catch(c){return!1}var d;for(d in a);return d===b||D.call(a,d)},isEmptyObject:function(a){for(var b in a)return!1;return!0},error:function(a){throw new Error(a)},parseJSON:function(b){if(typeof b!="string"||!b)return null;b=e.trim(b);if(a.JSON&&a.JSON.parse)return a.JSON.parse(b);if(n.test(b.replace(o,"@").replace(p,"]").replace(q,"")))return(new Function("return "+b))();e.error("Invalid JSON: "+b)},parseXML:function(c){if(typeof c!="string"||!c)return null;var d,f;try{a.DOMParser?(f=new DOMParser,d=f.parseFromString(c,"text/xml")):(d=new ActiveXObject("Microsoft.XMLDOM"),d.async="false",d.loadXML(c))}catch(g){d=b}(!d||!d.documentElement||d.getElementsByTagName("parsererror").length)&&e.error("Invalid XML: "+c);return d},noop:function(){},globalEval:function(b){b&&j.test(b)&&(a.execScript||function(b){a.eval.call(a,b)})(b)},camelCase:function(a){return a.replace(w,"ms-").replace(v,x)},nodeName:function(a,b){return a.nodeName&&a.nodeName.toUpperCase()===b.toUpperCase()},each:function(a,c,d){var f,g=0,h=a.length,i=h===b||e.isFunction(a);if(d){if(i){for(f in a)if(c.apply(a[f],d)===!1)break}else for(;g<h;)if(c.apply(a[g++],d)===!1)break}else if(i){for(f in a)if(c.call(a[f],f,a[f])===!1)break}else for(;g<h;)if(c.call(a[g],g,a[g++])===!1)break;return a},trim:G?function(a){return a==null?"":G.call(a)}:function(a){return a==null?"":(a+"").replace(k,"").replace(l,"")},makeArray:function(a,b){var c=b||[];if(a!=null){var d=e.type(a);a.length==null||d==="string"||d==="function"||d==="regexp"||e.isWindow(a)?E.call(c,a):e.merge(c,a)}return c},inArray:function(a,b,c){var d;if(b){if(H)return H.call(b,a,c);d=b.length,c=c?c<0?Math.max(0,d+c):c:0;for(;c<d;c++)if(c in b&&b[c]===a)return c}return-1},merge:function(a,c){var d=a.length,e=0;if(typeof c.length=="number")for(var f=c.length;e<f;e++)a[d++]=c[e];else while(c[e]!==b)a[d++]=c[e++];a.length=d;return a},grep:function(a,b,c){var d=[],e;c=!!c;for(var f=0,g=a.length;f<g;f++)e=!!b(a[f],f),c!==e&&d.push(a[f]);return d},map:function(a,c,d){var f,g,h=[],i=0,j=a.length,k=a instanceof e||j!==b&&typeof j=="number"&&(j>0&&a[0]&&a[j-1]||j===0||e.isArray(a));if(k)for(;i<j;i++)f=c(a[i],i,d),f!=null&&(h[h.length]=f);else for(g in a)f=c(a[g],g,d),f!=null&&(h[h.length]=f);return h.concat.apply([],h)},guid:1,proxy:function(a,c){if(typeof c=="string"){var d=a[c];c=a,a=d}if(!e.isFunction(a))return b;var f=F.call(arguments,2),g=function(){return a.apply(c,f.concat(F.call(arguments)))};g.guid=a.guid=a.guid||g.guid||e.guid++;return g},access:function(a,c,d,f,g,h,i){var j,k=d==null,l=0,m=a.length;if(d&&typeof d=="object"){for(l in d)e.access(a,c,l,d[l],1,h,f);g=1}else if(f!==b){j=i===b&&e.isFunction(f),k&&(j?(j=c,c=function(a,b,c){return j.call(e(a),c)}):(c.call(a,f),c=null));if(c)for(;l<m;l++)c(a[l],d,j?f.call(a[l],l,c(a[l],d)):f,i);g=1}return g?a:k?c.call(a):m?c(a[0],d):h},now:function(){return(new Date).getTime()},uaMatch:function(a){a=a.toLowerCase();var b=r.exec(a)||s.exec(a)||t.exec(a)||a.indexOf("compatible")<0&&u.exec(a)||[];return{browser:b[1]||"",version:b[2]||"0"}},sub:function(){function a(b,c){return new a.fn.init(b,c)}e.extend(!0,a,this),a.superclass=this,a.fn=a.prototype=this(),a.fn.constructor=a,a.sub=this.sub,a.fn.init=function(d,f){f&&f instanceof e&&!(f instanceof a)&&(f=a(f));return e.fn.init.call(this,d,f,b)},a.fn.init.prototype=a.fn;var b=a(c);return a},browser:{}}),e.each("Boolean Number String Function Array Date RegExp Object".split(" "),function(a,b){I["[object "+b+"]"]=b.toLowerCase()}),z=e.uaMatch(y),z.browser&&(e.browser[z.browser]=!0,e.browser.version=z.version),e.browser.webkit&&(e.browser.safari=!0),j.test(" ")&&(k=/^[\s\xA0]+/,l=/[\s\xA0]+$/),h=e(c),c.addEventListener?B=function(){c.removeEventListener("DOMContentLoaded",B,!1),e.ready()}:c.attachEvent&&(B=function(){c.readyState==="complete"&&(c.detachEvent("onreadystatechange",B),e.ready())});return e}(),g={};f.Callbacks=function(a){a=a?g[a]||h(a):{};var c=[],d=[],e,i,j,k,l,m,n=function(b){var d,e,g,h,i;for(d=0,e=b.length;d<e;d++)g=b[d],h=f.type(g),h==="array"?n(g):h==="function"&&(!a.unique||!p.has(g))&&c.push(g)},o=function(b,f){f=f||[],e=!a.memory||[b,f],i=!0,j=!0,m=k||0,k=0,l=c.length;for(;c&&m<l;m++)if(c[m].apply(b,f)===!1&&a.stopOnFalse){e=!0;break}j=!1,c&&(a.once?e===!0?p.disable():c=[]:d&&d.length&&(e=d.shift(),p.fireWith(e[0],e[1])))},p={add:function(){if(c){var a=c.length;n(arguments),j?l=c.length:e&&e!==!0&&(k=a,o(e[0],e[1]))}return this},remove:function(){if(c){var b=arguments,d=0,e=b.length;for(;d<e;d++)for(var f=0;f<c.length;f++)if(b[d]===c[f]){j&&f<=l&&(l--,f<=m&&m--),c.splice(f--,1);if(a.unique)break}}return this},has:function(a){if(c){var b=0,d=c.length;for(;b<d;b++)if(a===c[b])return!0}return!1},empty:function(){c=[];return this},disable:function(){c=d=e=b;return this},disabled:function(){return!c},lock:function(){d=b,(!e||e===!0)&&p.disable();return this},locked:function(){return!d},fireWith:function(b,c){d&&(j?a.once||d.push([b,c]):(!a.once||!e)&&o(b,c));return this},fire:function(){p.fireWith(this,arguments);return this},fired:function(){return!!i}};return p};var i=[].slice;f.extend({Deferred:function(a){var b=f.Callbacks("once memory"),c=f.Callbacks("once memory"),d=f.Callbacks("memory"),e="pending",g={resolve:b,reject:c,notify:d},h={done:b.add,fail:c.add,progress:d.add,state:function(){return e},isResolved:b.fired,isRejected:c.fired,then:function(a,b,c){i.done(a).fail(b).progress(c);return this},always:function(){i.done.apply(i,arguments).fail.apply(i,arguments);return this},pipe:function(a,b,c){return f.Deferred(function(d){f.each({done:[a,"resolve"],fail:[b,"reject"],progress:[c,"notify"]},function(a,b){var c=b[0],e=b[1],g;f.isFunction(c)?i[a](function(){g=c.apply(this,arguments),g&&f.isFunction(g.promise)?g.promise().then(d.resolve,d.reject,d.notify):d[e+"With"](this===i?d:this,[g])}):i[a](d[e])})}).promise()},promise:function(a){if(a==null)a=h;else for(var b in h)a[b]=h[b];return a}},i=h.promise({}),j;for(j in g)i[j]=g[j].fire,i[j+"With"]=g[j].fireWith;i.done(function(){e="resolved"},c.disable,d.lock).fail(function(){e="rejected"},b.disable,d.lock),a&&a.call(i,i);return i},when:function(a){function m(a){return function(b){e[a]=arguments.length>1?i.call(arguments,0):b,j.notifyWith(k,e)}}function l(a){return function(c){b[a]=arguments.length>1?i.call(arguments,0):c,--g||j.resolveWith(j,b)}}var b=i.call(arguments,0),c=0,d=b.length,e=Array(d),g=d,h=d,j=d<=1&&a&&f.isFunction(a.promise)?a:f.Deferred(),k=j.promise();if(d>1){for(;c<d;c++)b[c]&&b[c].promise&&f.isFunction(b[c].promise)?b[c].promise().then(l(c),j.reject,m(c)):--g;g||j.resolveWith(j,b)}else j!==a&&j.resolveWith(j,d?[a]:[]);return k}}),f.support=function(){var b,d,e,g,h,i,j,k,l,m,n,o,p=c.createElement("div"),q=c.documentElement;p.setAttribute("className","t"),p.innerHTML="   <link/><table></table><a href='/a' style='top:1px;float:left;opacity:.55;'>a</a><input type='checkbox'/>",d=p.getElementsByTagName("*"),e=p.getElementsByTagName("a")[0];if(!d||!d.length||!e)return{};g=c.createElement("select"),h=g.appendChild(c.createElement("option")),i=p.getElementsByTagName("input")[0],b={leadingWhitespace:p.firstChild.nodeType===3,tbody:!p.getElementsByTagName("tbody").length,htmlSerialize:!!p.getElementsByTagName("link").length,style:/top/.test(e.getAttribute("style")),hrefNormalized:e.getAttribute("href")==="/a",opacity:/^0.55/.test(e.style.opacity),cssFloat:!!e.style.cssFloat,checkOn:i.value==="on",optSelected:h.selected,getSetAttribute:p.className!=="t",enctype:!!c.createElement("form").enctype,html5Clone:c.createElement("nav").cloneNode(!0).outerHTML!=="<:nav></:nav>",submitBubbles:!0,changeBubbles:!0,focusinBubbles:!1,deleteExpando:!0,noCloneEvent:!0,inlineBlockNeedsLayout:!1,shrinkWrapBlocks:!1,reliableMarginRight:!0,pixelMargin:!0},f.boxModel=b.boxModel=c.compatMode==="CSS1Compat",i.checked=!0,b.noCloneChecked=i.cloneNode(!0).checked,g.disabled=!0,b.optDisabled=!h.disabled;try{delete p.test}catch(r){b.deleteExpando=!1}!p.addEventListener&&p.attachEvent&&p.fireEvent&&(p.attachEvent("onclick",function(){b.noCloneEvent=!1}),p.cloneNode(!0).fireEvent("onclick")),i=c.createElement("input"),i.value="t",i.setAttribute("type","radio"),b.radioValue=i.value==="t",i.setAttribute("checked","checked"),i.setAttribute("name","t"),p.appendChild(i),j=c.createDocumentFragment(),j.appendChild(p.lastChild),b.checkClone=j.cloneNode(!0).cloneNode(!0).lastChild.checked,b.appendChecked=i.checked,j.removeChild(i),j.appendChild(p);if(p.attachEvent)for(n in{submit:1,change:1,focusin:1})m="on"+n,o=m in p,o||(p.setAttribute(m,"return;"),o=typeof p[m]=="function"),b[n+"Bubbles"]=o;j.removeChild(p),j=g=h=p=i=null,f(function(){var d,e,g,h,i,j,l,m,n,q,r,s,t,u=c.getElementsByTagName("body")[0];!u||(m=1,t="padding:0;margin:0;border:",r="position:absolute;top:0;left:0;width:1px;height:1px;",s=t+"0;visibility:hidden;",n="style='"+r+t+"5px solid #000;",q="<div "+n+"display:block;'><div style='"+t+"0;display:block;overflow:hidden;'></div></div>"+"<table "+n+"' cellpadding='0' cellspacing='0'>"+"<tr><td></td></tr></table>",d=c.createElement("div"),d.style.cssText=s+"width:0;height:0;position:static;top:0;margin-top:"+m+"px",u.insertBefore(d,u.firstChild),p=c.createElement("div"),d.appendChild(p),p.innerHTML="<table><tr><td style='"+t+"0;display:none'></td><td>t</td></tr></table>",k=p.getElementsByTagName("td"),o=k[0].offsetHeight===0,k[0].style.display="",k[1].style.display="none",b.reliableHiddenOffsets=o&&k[0].offsetHeight===0,a.getComputedStyle&&(p.innerHTML="",l=c.createElement("div"),l.style.width="0",l.style.marginRight="0",p.style.width="2px",p.appendChild(l),b.reliableMarginRight=(parseInt((a.getComputedStyle(l,null)||{marginRight:0}).marginRight,10)||0)===0),typeof p.style.zoom!="undefined"&&(p.innerHTML="",p.style.width=p.style.padding="1px",p.style.border=0,p.style.overflow="hidden",p.style.display="inline",p.style.zoom=1,b.inlineBlockNeedsLayout=p.offsetWidth===3,p.style.display="block",p.style.overflow="visible",p.innerHTML="<div style='width:5px;'></div>",b.shrinkWrapBlocks=p.offsetWidth!==3),p.style.cssText=r+s,p.innerHTML=q,e=p.firstChild,g=e.firstChild,i=e.nextSibling.firstChild.firstChild,j={doesNotAddBorder:g.offsetTop!==5,doesAddBorderForTableAndCells:i.offsetTop===5},g.style.position="fixed",g.style.top="20px",j.fixedPosition=g.offsetTop===20||g.offsetTop===15,g.style.position=g.style.top="",e.style.overflow="hidden",e.style.position="relative",j.subtractsBorderForOverflowNotVisible=g.offsetTop===-5,j.doesNotIncludeMarginInBodyOffset=u.offsetTop!==m,a.getComputedStyle&&(p.style.marginTop="1%",b.pixelMargin=(a.getComputedStyle(p,null)||{marginTop:0}).marginTop!=="1%"),typeof d.style.zoom!="undefined"&&(d.style.zoom=1),u.removeChild(d),l=p=d=null,f.extend(b,j))});return b}();var j=/^(?:\{.*\}|\[.*\])$/,k=/([A-Z])/g;f.extend({cache:{},uuid:0,expando:"jQuery"+(f.fn.jquery+Math.random()).replace(/\D/g,""),noData:{embed:!0,object:"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000",applet:!0},hasData:function(a){a=a.nodeType?f.cache[a[f.expando]]:a[f.expando];return!!a&&!m(a)},data:function(a,c,d,e){if(!!f.acceptData(a)){var g,h,i,j=f.expando,k=typeof c=="string",l=a.nodeType,m=l?f.cache:a,n=l?a[j]:a[j]&&j,o=c==="events";if((!n||!m[n]||!o&&!e&&!m[n].data)&&k&&d===b)return;n||(l?a[j]=n=++f.uuid:n=j),m[n]||(m[n]={},l||(m[n].toJSON=f.noop));if(typeof c=="object"||typeof c=="function")e?m[n]=f.extend(m[n],c):m[n].data=f.extend(m[n].data,c);g=h=m[n],e||(h.data||(h.data={}),h=h.data),d!==b&&(h[f.camelCase(c)]=d);if(o&&!h[c])return g.events;k?(i=h[c],i==null&&(i=h[f.camelCase(c)])):i=h;return i}},removeData:function(a,b,c){if(!!f.acceptData(a)){var d,e,g,h=f.expando,i=a.nodeType,j=i?f.cache:a,k=i?a[h]:h;if(!j[k])return;if(b){d=c?j[k]:j[k].data;if(d){f.isArray(b)||(b in d?b=[b]:(b=f.camelCase(b),b in d?b=[b]:b=b.split(" ")));for(e=0,g=b.length;e<g;e++)delete d[b[e]];if(!(c?m:f.isEmptyObject)(d))return}}if(!c){delete j[k].data;if(!m(j[k]))return}f.support.deleteExpando||!j.setInterval?delete j[k]:j[k]=null,i&&(f.support.deleteExpando?delete a[h]:a.removeAttribute?a.removeAttribute(h):a[h]=null)}},_data:function(a,b,c){return f.data(a,b,c,!0)},acceptData:function(a){if(a.nodeName){var b=f.noData[a.nodeName.toLowerCase()];if(b)return b!==!0&&a.getAttribute("classid")===b}return!0}}),f.fn.extend({data:function(a,c){var d,e,g,h,i,j=this[0],k=0,m=null;if(a===b){if(this.length){m=f.data(j);if(j.nodeType===1&&!f._data(j,"parsedAttrs")){g=j.attributes;for(i=g.length;k<i;k++)h=g[k].name,h.indexOf("data-")===0&&(h=f.camelCase(h.substring(5)),l(j,h,m[h]));f._data(j,"parsedAttrs",!0)}}return m}if(typeof a=="object")return this.each(function(){f.data(this,a)});d=a.split(".",2),d[1]=d[1]?"."+d[1]:"",e=d[1]+"!";return f.access(this,function(c){if(c===b){m=this.triggerHandler("getData"+e,[d[0]]),m===b&&j&&(m=f.data(j,a),m=l(j,a,m));return m===b&&d[1]?this.data(d[0]):m}d[1]=c,this.each(function(){var b=f(this);b.triggerHandler("setData"+e,d),f.data(this,a,c),b.triggerHandler("changeData"+e,d)})},null,c,arguments.length>1,null,!1)},removeData:function(a){return this.each(function(){f.removeData(this,a)})}}),f.extend({_mark:function(a,b){a&&(b=(b||"fx")+"mark",f._data(a,b,(f._data(a,b)||0)+1))},_unmark:function(a,b,c){a!==!0&&(c=b,b=a,a=!1);if(b){c=c||"fx";var d=c+"mark",e=a?0:(f._data(b,d)||1)-1;e?f._data(b,d,e):(f.removeData(b,d,!0),n(b,c,"mark"))}},queue:function(a,b,c){var d;if(a){b=(b||"fx")+"queue",d=f._data(a,b),c&&(!d||f.isArray(c)?d=f._data(a,b,f.makeArray(c)):d.push(c));return d||[]}},dequeue:function(a,b){b=b||"fx";var c=f.queue(a,b),d=c.shift(),e={};d==="inprogress"&&(d=c.shift()),d&&(b==="fx"&&c.unshift("inprogress"),f._data(a,b+".run",e),d.call(a,function(){f.dequeue(a,b)},e)),c.length||(f.removeData(a,b+"queue "+b+".run",!0),n(a,b,"queue"))}}),f.fn.extend({queue:function(a,c){var d=2;typeof a!="string"&&(c=a,a="fx",d--);if(arguments.length<d)return f.queue(this[0],a);return c===b?this:this.each(function(){var b=f.queue(this,a,c);a==="fx"&&b[0]!=="inprogress"&&f.dequeue(this,a)})},dequeue:function(a){return this.each(function(){f.dequeue(this,a)})},delay:function(a,b){a=f.fx?f.fx.speeds[a]||a:a,b=b||"fx";return this.queue(b,function(b,c){var d=setTimeout(b,a);c.stop=function(){clearTimeout(d)}})},clearQueue:function(a){return this.queue(a||"fx",[])},promise:function(a,c){function m(){--h||d.resolveWith(e,[e])}typeof a!="string"&&(c=a,a=b),a=a||"fx";var d=f.Deferred(),e=this,g=e.length,h=1,i=a+"defer",j=a+"queue",k=a+"mark",l;while(g--)if(l=f.data(e[g],i,b,!0)||(f.data(e[g],j,b,!0)||f.data(e[g],k,b,!0))&&f.data(e[g],i,f.Callbacks("once memory"),!0))h++,l.add(m);m();return d.promise(c)}});var o=/[\n\t\r]/g,p=/\s+/,q=/\r/g,r=/^(?:button|input)$/i,s=/^(?:button|input|object|select|textarea)$/i,t=/^a(?:rea)?$/i,u=/^(?:autofocus|autoplay|async|checked|controls|defer|disabled|hidden|loop|multiple|open|readonly|required|scoped|selected)$/i,v=f.support.getSetAttribute,w,x,y;f.fn.extend({attr:function(a,b){return f.access(this,f.attr,a,b,arguments.length>1)},removeAttr:function(a){return this.each(function(){f.removeAttr(this,a)})},prop:function(a,b){return f.access(this,f.prop,a,b,arguments.length>1)},removeProp:function(a){a=f.propFix[a]||a;return this.each(function(){try{this[a]=b,delete this[a]}catch(c){}})},addClass:function(a){var b,c,d,e,g,h,i;if(f.isFunction(a))return this.each(function(b){f(this).addClass(a.call(this,b,this.className))});if(a&&typeof a=="string"){b=a.split(p);for(c=0,d=this.length;c<d;c++){e=this[c];if(e.nodeType===1)if(!e.className&&b.length===1)e.className=a;else{g=" "+e.className+" ";for(h=0,i=b.length;h<i;h++)~g.indexOf(" "+b[h]+" ")||(g+=b[h]+" ");e.className=f.trim(g)}}}return this},removeClass:function(a){var c,d,e,g,h,i,j;if(f.isFunction(a))return this.each(function(b){f(this).removeClass(a.call(this,b,this.className))});if(a&&typeof a=="string"||a===b){c=(a||"").split(p);for(d=0,e=this.length;d<e;d++){g=this[d];if(g.nodeType===1&&g.className)if(a){h=(" "+g.className+" ").replace(o," ");for(i=0,j=c.length;i<j;i++)h=h.replace(" "+c[i]+" "," ");g.className=f.trim(h)}else g.className=""}}return this},toggleClass:function(a,b){var c=typeof a,d=typeof b=="boolean";if(f.isFunction(a))return this.each(function(c){f(this).toggleClass(a.call(this,c,this.className,b),b)});return this.each(function(){if(c==="string"){var e,g=0,h=f(this),i=b,j=a.split(p);while(e=j[g++])i=d?i:!h.hasClass(e),h[i?"addClass":"removeClass"](e)}else if(c==="undefined"||c==="boolean")this.className&&f._data(this,"__className__",this.className),this.className=this.className||a===!1?"":f._data(this,"__className__")||""})},hasClass:function(a){var b=" "+a+" ",c=0,d=this.length;for(;c<d;c++)if(this[c].nodeType===1&&(" "+this[c].className+" ").replace(o," ").indexOf(b)>-1)return!0;return!1},val:function(a){var c,d,e,g=this[0];{if(!!arguments.length){e=f.isFunction(a);return this.each(function(d){var g=f(this),h;if(this.nodeType===1){e?h=a.call(this,d,g.val()):h=a,h==null?h="":typeof h=="number"?h+="":f.isArray(h)&&(h=f.map(h,function(a){return a==null?"":a+""})),c=f.valHooks[this.type]||f.valHooks[this.nodeName.toLowerCase()];if(!c||!("set"in c)||c.set(this,h,"value")===b)this.value=h}})}if(g){c=f.valHooks[g.type]||f.valHooks[g.nodeName.toLowerCase()];if(c&&"get"in c&&(d=c.get(g,"value"))!==b)return d;d=g.value;return typeof d=="string"?d.replace(q,""):d==null?"":d}}}}),f.extend({valHooks:{option:{get:function(a){var b=a.attributes.value;return!b||b.specified?a.value:a.text}},select:{get:function(a){var b,c,d,e,g=a.selectedIndex,h=[],i=a.options,j=a.type==="select-one";if(g<0)return null;c=j?g:0,d=j?g+1:i.length;for(;c<d;c++){e=i[c];if(e.selected&&(f.support.optDisabled?!e.disabled:e.getAttribute("disabled")===null)&&(!e.parentNode.disabled||!f.nodeName(e.parentNode,"optgroup"))){b=f(e).val();if(j)return b;h.push(b)}}if(j&&!h.length&&i.length)return f(i[g]).val();return h},set:function(a,b){var c=f.makeArray(b);f(a).find("option").each(function(){this.selected=f.inArray(f(this).val(),c)>=0}),c.length||(a.selectedIndex=-1);return c}}},attrFn:{val:!0,css:!0,html:!0,text:!0,data:!0,width:!0,height:!0,offset:!0},attr:function(a,c,d,e){var g,h,i,j=a.nodeType;if(!!a&&j!==3&&j!==8&&j!==2){if(e&&c in f.attrFn)return f(a)[c](d);if(typeof a.getAttribute=="undefined")return f.prop(a,c,d);i=j!==1||!f.isXMLDoc(a),i&&(c=c.toLowerCase(),h=f.attrHooks[c]||(u.test(c)?x:w));if(d!==b){if(d===null){f.removeAttr(a,c);return}if(h&&"set"in h&&i&&(g=h.set(a,d,c))!==b)return g;a.setAttribute(c,""+d);return d}if(h&&"get"in h&&i&&(g=h.get(a,c))!==null)return g;g=a.getAttribute(c);return g===null?b:g}},removeAttr:function(a,b){var c,d,e,g,h,i=0;if(b&&a.nodeType===1){d=b.toLowerCase().split(p),g=d.length;for(;i<g;i++)e=d[i],e&&(c=f.propFix[e]||e,h=u.test(e),h||f.attr(a,e,""),a.removeAttribute(v?e:c),h&&c in a&&(a[c]=!1))}},attrHooks:{type:{set:function(a,b){if(r.test(a.nodeName)&&a.parentNode)f.error("type property can't be changed");else if(!f.support.radioValue&&b==="radio"&&f.nodeName(a,"input")){var c=a.value;a.setAttribute("type",b),c&&(a.value=c);return b}}},value:{get:function(a,b){if(w&&f.nodeName(a,"button"))return w.get(a,b);return b in a?a.value:null},set:function(a,b,c){if(w&&f.nodeName(a,"button"))return w.set(a,b,c);a.value=b}}},propFix:{tabindex:"tabIndex",readonly:"readOnly","for":"htmlFor","class":"className",maxlength:"maxLength",cellspacing:"cellSpacing",cellpadding:"cellPadding",rowspan:"rowSpan",colspan:"colSpan",usemap:"useMap",frameborder:"frameBorder",contenteditable:"contentEditable"},prop:function(a,c,d){var e,g,h,i=a.nodeType;if(!!a&&i!==3&&i!==8&&i!==2){h=i!==1||!f.isXMLDoc(a),h&&(c=f.propFix[c]||c,g=f.propHooks[c]);return d!==b?g&&"set"in g&&(e=g.set(a,d,c))!==b?e:a[c]=d:g&&"get"in g&&(e=g.get(a,c))!==null?e:a[c]}},propHooks:{tabIndex:{get:function(a){var c=a.getAttributeNode("tabindex");return c&&c.specified?parseInt(c.value,10):s.test(a.nodeName)||t.test(a.nodeName)&&a.href?0:b}}}}),f.attrHooks.tabindex=f.propHooks.tabIndex,x={get:function(a,c){var d,e=f.prop(a,c);return e===!0||typeof e!="boolean"&&(d=a.getAttributeNode(c))&&d.nodeValue!==!1?c.toLowerCase():b},set:function(a,b,c){var d;b===!1?f.removeAttr(a,c):(d=f.propFix[c]||c,d in a&&(a[d]=!0),a.setAttribute(c,c.toLowerCase()));return c}},v||(y={name:!0,id:!0,coords:!0},w=f.valHooks.button={get:function(a,c){var d;d=a.getAttributeNode(c);return d&&(y[c]?d.nodeValue!=="":d.specified)?d.nodeValue:b},set:function(a,b,d){var e=a.getAttributeNode(d);e||(e=c.createAttribute(d),a.setAttributeNode(e));return e.nodeValue=b+""}},f.attrHooks.tabindex.set=w.set,f.each(["width","height"],function(a,b){f.attrHooks[b]=f.extend(f.attrHooks[b],{set:function(a,c){if(c===""){a.setAttribute(b,"auto");return c}}})}),f.attrHooks.contenteditable={get:w.get,set:function(a,b,c){b===""&&(b="false"),w.set(a,b,c)}}),f.support.hrefNormalized||f.each(["href","src","width","height"],function(a,c){f.attrHooks[c]=f.extend(f.attrHooks[c],{get:function(a){var d=a.getAttribute(c,2);return d===null?b:d}})}),f.support.style||(f.attrHooks.style={get:function(a){return a.style.cssText.toLowerCase()||b},set:function(a,b){return a.style.cssText=""+b}}),f.support.optSelected||(f.propHooks.selected=f.extend(f.propHooks.selected,{get:function(a){var b=a.parentNode;b&&(b.selectedIndex,b.parentNode&&b.parentNode.selectedIndex);return null}})),f.support.enctype||(f.propFix.enctype="encoding"),f.support.checkOn||f.each(["radio","checkbox"],function(){f.valHooks[this]={get:function(a){return a.getAttribute("value")===null?"on":a.value}}}),f.each(["radio","checkbox"],function(){f.valHooks[this]=f.extend(f.valHooks[this],{set:function(a,b){if(f.isArray(b))return a.checked=f.inArray(f(a).val(),b)>=0}})});var z=/^(?:textarea|input|select)$/i,A=/^([^\.]*)?(?:\.(.+))?$/,B=/(?:^|\s)hover(\.\S+)?\b/,C=/^key/,D=/^(?:mouse|contextmenu)|click/,E=/^(?:focusinfocus|focusoutblur)$/,F=/^(\w*)(?:#([\w\-]+))?(?:\.([\w\-]+))?$/,G=function(
a){var b=F.exec(a);b&&(b[1]=(b[1]||"").toLowerCase(),b[3]=b[3]&&new RegExp("(?:^|\\s)"+b[3]+"(?:\\s|$)"));return b},H=function(a,b){var c=a.attributes||{};return(!b[1]||a.nodeName.toLowerCase()===b[1])&&(!b[2]||(c.id||{}).value===b[2])&&(!b[3]||b[3].test((c["class"]||{}).value))},I=function(a){return f.event.special.hover?a:a.replace(B,"mouseenter$1 mouseleave$1")};f.event={add:function(a,c,d,e,g){var h,i,j,k,l,m,n,o,p,q,r,s;if(!(a.nodeType===3||a.nodeType===8||!c||!d||!(h=f._data(a)))){d.handler&&(p=d,d=p.handler,g=p.selector),d.guid||(d.guid=f.guid++),j=h.events,j||(h.events=j={}),i=h.handle,i||(h.handle=i=function(a){return typeof f!="undefined"&&(!a||f.event.triggered!==a.type)?f.event.dispatch.apply(i.elem,arguments):b},i.elem=a),c=f.trim(I(c)).split(" ");for(k=0;k<c.length;k++){l=A.exec(c[k])||[],m=l[1],n=(l[2]||"").split(".").sort(),s=f.event.special[m]||{},m=(g?s.delegateType:s.bindType)||m,s=f.event.special[m]||{},o=f.extend({type:m,origType:l[1],data:e,handler:d,guid:d.guid,selector:g,quick:g&&G(g),namespace:n.join(".")},p),r=j[m];if(!r){r=j[m]=[],r.delegateCount=0;if(!s.setup||s.setup.call(a,e,n,i)===!1)a.addEventListener?a.addEventListener(m,i,!1):a.attachEvent&&a.attachEvent("on"+m,i)}s.add&&(s.add.call(a,o),o.handler.guid||(o.handler.guid=d.guid)),g?r.splice(r.delegateCount++,0,o):r.push(o),f.event.global[m]=!0}a=null}},global:{},remove:function(a,b,c,d,e){var g=f.hasData(a)&&f._data(a),h,i,j,k,l,m,n,o,p,q,r,s;if(!!g&&!!(o=g.events)){b=f.trim(I(b||"")).split(" ");for(h=0;h<b.length;h++){i=A.exec(b[h])||[],j=k=i[1],l=i[2];if(!j){for(j in o)f.event.remove(a,j+b[h],c,d,!0);continue}p=f.event.special[j]||{},j=(d?p.delegateType:p.bindType)||j,r=o[j]||[],m=r.length,l=l?new RegExp("(^|\\.)"+l.split(".").sort().join("\\.(?:.*\\.)?")+"(\\.|$)"):null;for(n=0;n<r.length;n++)s=r[n],(e||k===s.origType)&&(!c||c.guid===s.guid)&&(!l||l.test(s.namespace))&&(!d||d===s.selector||d==="**"&&s.selector)&&(r.splice(n--,1),s.selector&&r.delegateCount--,p.remove&&p.remove.call(a,s));r.length===0&&m!==r.length&&((!p.teardown||p.teardown.call(a,l)===!1)&&f.removeEvent(a,j,g.handle),delete o[j])}f.isEmptyObject(o)&&(q=g.handle,q&&(q.elem=null),f.removeData(a,["events","handle"],!0))}},customEvent:{getData:!0,setData:!0,changeData:!0},trigger:function(c,d,e,g){if(!e||e.nodeType!==3&&e.nodeType!==8){var h=c.type||c,i=[],j,k,l,m,n,o,p,q,r,s;if(E.test(h+f.event.triggered))return;h.indexOf("!")>=0&&(h=h.slice(0,-1),k=!0),h.indexOf(".")>=0&&(i=h.split("."),h=i.shift(),i.sort());if((!e||f.event.customEvent[h])&&!f.event.global[h])return;c=typeof c=="object"?c[f.expando]?c:new f.Event(h,c):new f.Event(h),c.type=h,c.isTrigger=!0,c.exclusive=k,c.namespace=i.join("."),c.namespace_re=c.namespace?new RegExp("(^|\\.)"+i.join("\\.(?:.*\\.)?")+"(\\.|$)"):null,o=h.indexOf(":")<0?"on"+h:"";if(!e){j=f.cache;for(l in j)j[l].events&&j[l].events[h]&&f.event.trigger(c,d,j[l].handle.elem,!0);return}c.result=b,c.target||(c.target=e),d=d!=null?f.makeArray(d):[],d.unshift(c),p=f.event.special[h]||{};if(p.trigger&&p.trigger.apply(e,d)===!1)return;r=[[e,p.bindType||h]];if(!g&&!p.noBubble&&!f.isWindow(e)){s=p.delegateType||h,m=E.test(s+h)?e:e.parentNode,n=null;for(;m;m=m.parentNode)r.push([m,s]),n=m;n&&n===e.ownerDocument&&r.push([n.defaultView||n.parentWindow||a,s])}for(l=0;l<r.length&&!c.isPropagationStopped();l++)m=r[l][0],c.type=r[l][1],q=(f._data(m,"events")||{})[c.type]&&f._data(m,"handle"),q&&q.apply(m,d),q=o&&m[o],q&&f.acceptData(m)&&q.apply(m,d)===!1&&c.preventDefault();c.type=h,!g&&!c.isDefaultPrevented()&&(!p._default||p._default.apply(e.ownerDocument,d)===!1)&&(h!=="click"||!f.nodeName(e,"a"))&&f.acceptData(e)&&o&&e[h]&&(h!=="focus"&&h!=="blur"||c.target.offsetWidth!==0)&&!f.isWindow(e)&&(n=e[o],n&&(e[o]=null),f.event.triggered=h,e[h](),f.event.triggered=b,n&&(e[o]=n));return c.result}},dispatch:function(c){c=f.event.fix(c||a.event);var d=(f._data(this,"events")||{})[c.type]||[],e=d.delegateCount,g=[].slice.call(arguments,0),h=!c.exclusive&&!c.namespace,i=f.event.special[c.type]||{},j=[],k,l,m,n,o,p,q,r,s,t,u;g[0]=c,c.delegateTarget=this;if(!i.preDispatch||i.preDispatch.call(this,c)!==!1){if(e&&(!c.button||c.type!=="click")){n=f(this),n.context=this.ownerDocument||this;for(m=c.target;m!=this;m=m.parentNode||this)if(m.disabled!==!0){p={},r=[],n[0]=m;for(k=0;k<e;k++)s=d[k],t=s.selector,p[t]===b&&(p[t]=s.quick?H(m,s.quick):n.is(t)),p[t]&&r.push(s);r.length&&j.push({elem:m,matches:r})}}d.length>e&&j.push({elem:this,matches:d.slice(e)});for(k=0;k<j.length&&!c.isPropagationStopped();k++){q=j[k],c.currentTarget=q.elem;for(l=0;l<q.matches.length&&!c.isImmediatePropagationStopped();l++){s=q.matches[l];if(h||!c.namespace&&!s.namespace||c.namespace_re&&c.namespace_re.test(s.namespace))c.data=s.data,c.handleObj=s,o=((f.event.special[s.origType]||{}).handle||s.handler).apply(q.elem,g),o!==b&&(c.result=o,o===!1&&(c.preventDefault(),c.stopPropagation()))}}i.postDispatch&&i.postDispatch.call(this,c);return c.result}},props:"attrChange attrName relatedNode srcElement altKey bubbles cancelable ctrlKey currentTarget eventPhase metaKey relatedTarget shiftKey target timeStamp view which".split(" "),fixHooks:{},keyHooks:{props:"char charCode key keyCode".split(" "),filter:function(a,b){a.which==null&&(a.which=b.charCode!=null?b.charCode:b.keyCode);return a}},mouseHooks:{props:"button buttons clientX clientY fromElement offsetX offsetY pageX pageY screenX screenY toElement".split(" "),filter:function(a,d){var e,f,g,h=d.button,i=d.fromElement;a.pageX==null&&d.clientX!=null&&(e=a.target.ownerDocument||c,f=e.documentElement,g=e.body,a.pageX=d.clientX+(f&&f.scrollLeft||g&&g.scrollLeft||0)-(f&&f.clientLeft||g&&g.clientLeft||0),a.pageY=d.clientY+(f&&f.scrollTop||g&&g.scrollTop||0)-(f&&f.clientTop||g&&g.clientTop||0)),!a.relatedTarget&&i&&(a.relatedTarget=i===a.target?d.toElement:i),!a.which&&h!==b&&(a.which=h&1?1:h&2?3:h&4?2:0);return a}},fix:function(a){if(a[f.expando])return a;var d,e,g=a,h=f.event.fixHooks[a.type]||{},i=h.props?this.props.concat(h.props):this.props;a=f.Event(g);for(d=i.length;d;)e=i[--d],a[e]=g[e];a.target||(a.target=g.srcElement||c),a.target.nodeType===3&&(a.target=a.target.parentNode),a.metaKey===b&&(a.metaKey=a.ctrlKey);return h.filter?h.filter(a,g):a},special:{ready:{setup:f.bindReady},load:{noBubble:!0},focus:{delegateType:"focusin"},blur:{delegateType:"focusout"},beforeunload:{setup:function(a,b,c){f.isWindow(this)&&(this.onbeforeunload=c)},teardown:function(a,b){this.onbeforeunload===b&&(this.onbeforeunload=null)}}},simulate:function(a,b,c,d){var e=f.extend(new f.Event,c,{type:a,isSimulated:!0,originalEvent:{}});d?f.event.trigger(e,null,b):f.event.dispatch.call(b,e),e.isDefaultPrevented()&&c.preventDefault()}},f.event.handle=f.event.dispatch,f.removeEvent=c.removeEventListener?function(a,b,c){a.removeEventListener&&a.removeEventListener(b,c,!1)}:function(a,b,c){a.detachEvent&&a.detachEvent("on"+b,c)},f.Event=function(a,b){if(!(this instanceof f.Event))return new f.Event(a,b);a&&a.type?(this.originalEvent=a,this.type=a.type,this.isDefaultPrevented=a.defaultPrevented||a.returnValue===!1||a.getPreventDefault&&a.getPreventDefault()?K:J):this.type=a,b&&f.extend(this,b),this.timeStamp=a&&a.timeStamp||f.now(),this[f.expando]=!0},f.Event.prototype={preventDefault:function(){this.isDefaultPrevented=K;var a=this.originalEvent;!a||(a.preventDefault?a.preventDefault():a.returnValue=!1)},stopPropagation:function(){this.isPropagationStopped=K;var a=this.originalEvent;!a||(a.stopPropagation&&a.stopPropagation(),a.cancelBubble=!0)},stopImmediatePropagation:function(){this.isImmediatePropagationStopped=K,this.stopPropagation()},isDefaultPrevented:J,isPropagationStopped:J,isImmediatePropagationStopped:J},f.each({mouseenter:"mouseover",mouseleave:"mouseout"},function(a,b){f.event.special[a]={delegateType:b,bindType:b,handle:function(a){var c=this,d=a.relatedTarget,e=a.handleObj,g=e.selector,h;if(!d||d!==c&&!f.contains(c,d))a.type=e.origType,h=e.handler.apply(this,arguments),a.type=b;return h}}}),f.support.submitBubbles||(f.event.special.submit={setup:function(){if(f.nodeName(this,"form"))return!1;f.event.add(this,"click._submit keypress._submit",function(a){var c=a.target,d=f.nodeName(c,"input")||f.nodeName(c,"button")?c.form:b;d&&!d._submit_attached&&(f.event.add(d,"submit._submit",function(a){a._submit_bubble=!0}),d._submit_attached=!0)})},postDispatch:function(a){a._submit_bubble&&(delete a._submit_bubble,this.parentNode&&!a.isTrigger&&f.event.simulate("submit",this.parentNode,a,!0))},teardown:function(){if(f.nodeName(this,"form"))return!1;f.event.remove(this,"._submit")}}),f.support.changeBubbles||(f.event.special.change={setup:function(){if(z.test(this.nodeName)){if(this.type==="checkbox"||this.type==="radio")f.event.add(this,"propertychange._change",function(a){a.originalEvent.propertyName==="checked"&&(this._just_changed=!0)}),f.event.add(this,"click._change",function(a){this._just_changed&&!a.isTrigger&&(this._just_changed=!1,f.event.simulate("change",this,a,!0))});return!1}f.event.add(this,"beforeactivate._change",function(a){var b=a.target;z.test(b.nodeName)&&!b._change_attached&&(f.event.add(b,"change._change",function(a){this.parentNode&&!a.isSimulated&&!a.isTrigger&&f.event.simulate("change",this.parentNode,a,!0)}),b._change_attached=!0)})},handle:function(a){var b=a.target;if(this!==b||a.isSimulated||a.isTrigger||b.type!=="radio"&&b.type!=="checkbox")return a.handleObj.handler.apply(this,arguments)},teardown:function(){f.event.remove(this,"._change");return z.test(this.nodeName)}}),f.support.focusinBubbles||f.each({focus:"focusin",blur:"focusout"},function(a,b){var d=0,e=function(a){f.event.simulate(b,a.target,f.event.fix(a),!0)};f.event.special[b]={setup:function(){d++===0&&c.addEventListener(a,e,!0)},teardown:function(){--d===0&&c.removeEventListener(a,e,!0)}}}),f.fn.extend({on:function(a,c,d,e,g){var h,i;if(typeof a=="object"){typeof c!="string"&&(d=d||c,c=b);for(i in a)this.on(i,c,d,a[i],g);return this}d==null&&e==null?(e=c,d=c=b):e==null&&(typeof c=="string"?(e=d,d=b):(e=d,d=c,c=b));if(e===!1)e=J;else if(!e)return this;g===1&&(h=e,e=function(a){f().off(a);return h.apply(this,arguments)},e.guid=h.guid||(h.guid=f.guid++));return this.each(function(){f.event.add(this,a,e,d,c)})},one:function(a,b,c,d){return this.on(a,b,c,d,1)},off:function(a,c,d){if(a&&a.preventDefault&&a.handleObj){var e=a.handleObj;f(a.delegateTarget).off(e.namespace?e.origType+"."+e.namespace:e.origType,e.selector,e.handler);return this}if(typeof a=="object"){for(var g in a)this.off(g,c,a[g]);return this}if(c===!1||typeof c=="function")d=c,c=b;d===!1&&(d=J);return this.each(function(){f.event.remove(this,a,d,c)})},bind:function(a,b,c){return this.on(a,null,b,c)},unbind:function(a,b){return this.off(a,null,b)},live:function(a,b,c){f(this.context).on(a,this.selector,b,c);return this},die:function(a,b){f(this.context).off(a,this.selector||"**",b);return this},delegate:function(a,b,c,d){return this.on(b,a,c,d)},undelegate:function(a,b,c){return arguments.length==1?this.off(a,"**"):this.off(b,a,c)},trigger:function(a,b){return this.each(function(){f.event.trigger(a,b,this)})},triggerHandler:function(a,b){if(this[0])return f.event.trigger(a,b,this[0],!0)},toggle:function(a){var b=arguments,c=a.guid||f.guid++,d=0,e=function(c){var e=(f._data(this,"lastToggle"+a.guid)||0)%d;f._data(this,"lastToggle"+a.guid,e+1),c.preventDefault();return b[e].apply(this,arguments)||!1};e.guid=c;while(d<b.length)b[d++].guid=c;return this.click(e)},hover:function(a,b){return this.mouseenter(a).mouseleave(b||a)}}),f.each("blur focus focusin focusout load resize scroll unload click dblclick mousedown mouseup mousemove mouseover mouseout mouseenter mouseleave change select submit keydown keypress keyup error contextmenu".split(" "),function(a,b){f.fn[b]=function(a,c){c==null&&(c=a,a=null);return arguments.length>0?this.on(b,null,a,c):this.trigger(b)},f.attrFn&&(f.attrFn[b]=!0),C.test(b)&&(f.event.fixHooks[b]=f.event.keyHooks),D.test(b)&&(f.event.fixHooks[b]=f.event.mouseHooks)}),function(){function x(a,b,c,e,f,g){for(var h=0,i=e.length;h<i;h++){var j=e[h];if(j){var k=!1;j=j[a];while(j){if(j[d]===c){k=e[j.sizset];break}if(j.nodeType===1){g||(j[d]=c,j.sizset=h);if(typeof b!="string"){if(j===b){k=!0;break}}else if(m.filter(b,[j]).length>0){k=j;break}}j=j[a]}e[h]=k}}}function w(a,b,c,e,f,g){for(var h=0,i=e.length;h<i;h++){var j=e[h];if(j){var k=!1;j=j[a];while(j){if(j[d]===c){k=e[j.sizset];break}j.nodeType===1&&!g&&(j[d]=c,j.sizset=h);if(j.nodeName.toLowerCase()===b){k=j;break}j=j[a]}e[h]=k}}}var a=/((?:\((?:\([^()]+\)|[^()]+)+\)|\[(?:\[[^\[\]]*\]|['"][^'"]*['"]|[^\[\]'"]+)+\]|\\.|[^ >+~,(\[\\]+)+|[>+~])(\s*,\s*)?((?:.|\r|\n)*)/g,d="sizcache"+(Math.random()+"").replace(".",""),e=0,g=Object.prototype.toString,h=!1,i=!0,j=/\\/g,k=/\r\n/g,l=/\W/;[0,0].sort(function(){i=!1;return 0});var m=function(b,d,e,f){e=e||[],d=d||c;var h=d;if(d.nodeType!==1&&d.nodeType!==9)return[];if(!b||typeof b!="string")return e;var i,j,k,l,n,q,r,t,u=!0,v=m.isXML(d),w=[],x=b;do{a.exec(""),i=a.exec(x);if(i){x=i[3],w.push(i[1]);if(i[2]){l=i[3];break}}}while(i);if(w.length>1&&p.exec(b))if(w.length===2&&o.relative[w[0]])j=y(w[0]+w[1],d,f);else{j=o.relative[w[0]]?[d]:m(w.shift(),d);while(w.length)b=w.shift(),o.relative[b]&&(b+=w.shift()),j=y(b,j,f)}else{!f&&w.length>1&&d.nodeType===9&&!v&&o.match.ID.test(w[0])&&!o.match.ID.test(w[w.length-1])&&(n=m.find(w.shift(),d,v),d=n.expr?m.filter(n.expr,n.set)[0]:n.set[0]);if(d){n=f?{expr:w.pop(),set:s(f)}:m.find(w.pop(),w.length===1&&(w[0]==="~"||w[0]==="+")&&d.parentNode?d.parentNode:d,v),j=n.expr?m.filter(n.expr,n.set):n.set,w.length>0?k=s(j):u=!1;while(w.length)q=w.pop(),r=q,o.relative[q]?r=w.pop():q="",r==null&&(r=d),o.relative[q](k,r,v)}else k=w=[]}k||(k=j),k||m.error(q||b);if(g.call(k)==="[object Array]")if(!u)e.push.apply(e,k);else if(d&&d.nodeType===1)for(t=0;k[t]!=null;t++)k[t]&&(k[t]===!0||k[t].nodeType===1&&m.contains(d,k[t]))&&e.push(j[t]);else for(t=0;k[t]!=null;t++)k[t]&&k[t].nodeType===1&&e.push(j[t]);else s(k,e);l&&(m(l,h,e,f),m.uniqueSort(e));return e};m.uniqueSort=function(a){if(u){h=i,a.sort(u);if(h)for(var b=1;b<a.length;b++)a[b]===a[b-1]&&a.splice(b--,1)}return a},m.matches=function(a,b){return m(a,null,null,b)},m.matchesSelector=function(a,b){return m(b,null,null,[a]).length>0},m.find=function(a,b,c){var d,e,f,g,h,i;if(!a)return[];for(e=0,f=o.order.length;e<f;e++){h=o.order[e];if(g=o.leftMatch[h].exec(a)){i=g[1],g.splice(1,1);if(i.substr(i.length-1)!=="\\"){g[1]=(g[1]||"").replace(j,""),d=o.find[h](g,b,c);if(d!=null){a=a.replace(o.match[h],"");break}}}}d||(d=typeof b.getElementsByTagName!="undefined"?b.getElementsByTagName("*"):[]);return{set:d,expr:a}},m.filter=function(a,c,d,e){var f,g,h,i,j,k,l,n,p,q=a,r=[],s=c,t=c&&c[0]&&m.isXML(c[0]);while(a&&c.length){for(h in o.filter)if((f=o.leftMatch[h].exec(a))!=null&&f[2]){k=o.filter[h],l=f[1],g=!1,f.splice(1,1);if(l.substr(l.length-1)==="\\")continue;s===r&&(r=[]);if(o.preFilter[h]){f=o.preFilter[h](f,s,d,r,e,t);if(!f)g=i=!0;else if(f===!0)continue}if(f)for(n=0;(j=s[n])!=null;n++)j&&(i=k(j,f,n,s),p=e^i,d&&i!=null?p?g=!0:s[n]=!1:p&&(r.push(j),g=!0));if(i!==b){d||(s=r),a=a.replace(o.match[h],"");if(!g)return[];break}}if(a===q)if(g==null)m.error(a);else break;q=a}return s},m.error=function(a){throw new Error("Syntax error, unrecognized expression: "+a)};var n=m.getText=function(a){var b,c,d=a.nodeType,e="";if(d){if(d===1||d===9||d===11){if(typeof a.textContent=="string")return a.textContent;if(typeof a.innerText=="string")return a.innerText.replace(k,"");for(a=a.firstChild;a;a=a.nextSibling)e+=n(a)}else if(d===3||d===4)return a.nodeValue}else for(b=0;c=a[b];b++)c.nodeType!==8&&(e+=n(c));return e},o=m.selectors={order:["ID","NAME","TAG"],match:{ID:/#((?:[\w\u00c0-\uFFFF\-]|\\.)+)/,CLASS:/\.((?:[\w\u00c0-\uFFFF\-]|\\.)+)/,NAME:/\[name=['"]*((?:[\w\u00c0-\uFFFF\-]|\\.)+)['"]*\]/,ATTR:/\[\s*((?:[\w\u00c0-\uFFFF\-]|\\.)+)\s*(?:(\S?=)\s*(?:(['"])(.*?)\3|(#?(?:[\w\u00c0-\uFFFF\-]|\\.)*)|)|)\s*\]/,TAG:/^((?:[\w\u00c0-\uFFFF\*\-]|\\.)+)/,CHILD:/:(only|nth|last|first)-child(?:\(\s*(even|odd|(?:[+\-]?\d+|(?:[+\-]?\d*)?n\s*(?:[+\-]\s*\d+)?))\s*\))?/,POS:/:(nth|eq|gt|lt|first|last|even|odd)(?:\((\d*)\))?(?=[^\-]|$)/,PSEUDO:/:((?:[\w\u00c0-\uFFFF\-]|\\.)+)(?:\((['"]?)((?:\([^\)]+\)|[^\(\)]*)+)\2\))?/},leftMatch:{},attrMap:{"class":"className","for":"htmlFor"},attrHandle:{href:function(a){return a.getAttribute("href")},type:function(a){return a.getAttribute("type")}},relative:{"+":function(a,b){var c=typeof b=="string",d=c&&!l.test(b),e=c&&!d;d&&(b=b.toLowerCase());for(var f=0,g=a.length,h;f<g;f++)if(h=a[f]){while((h=h.previousSibling)&&h.nodeType!==1);a[f]=e||h&&h.nodeName.toLowerCase()===b?h||!1:h===b}e&&m.filter(b,a,!0)},">":function(a,b){var c,d=typeof b=="string",e=0,f=a.length;if(d&&!l.test(b)){b=b.toLowerCase();for(;e<f;e++){c=a[e];if(c){var g=c.parentNode;a[e]=g.nodeName.toLowerCase()===b?g:!1}}}else{for(;e<f;e++)c=a[e],c&&(a[e]=d?c.parentNode:c.parentNode===b);d&&m.filter(b,a,!0)}},"":function(a,b,c){var d,f=e++,g=x;typeof b=="string"&&!l.test(b)&&(b=b.toLowerCase(),d=b,g=w),g("parentNode",b,f,a,d,c)},"~":function(a,b,c){var d,f=e++,g=x;typeof b=="string"&&!l.test(b)&&(b=b.toLowerCase(),d=b,g=w),g("previousSibling",b,f,a,d,c)}},find:{ID:function(a,b,c){if(typeof b.getElementById!="undefined"&&!c){var d=b.getElementById(a[1]);return d&&d.parentNode?[d]:[]}},NAME:function(a,b){if(typeof b.getElementsByName!="undefined"){var c=[],d=b.getElementsByName(a[1]);for(var e=0,f=d.length;e<f;e++)d[e].getAttribute("name")===a[1]&&c.push(d[e]);return c.length===0?null:c}},TAG:function(a,b){if(typeof b.getElementsByTagName!="undefined")return b.getElementsByTagName(a[1])}},preFilter:{CLASS:function(a,b,c,d,e,f){a=" "+a[1].replace(j,"")+" ";if(f)return a;for(var g=0,h;(h=b[g])!=null;g++)h&&(e^(h.className&&(" "+h.className+" ").replace(/[\t\n\r]/g," ").indexOf(a)>=0)?c||d.push(h):c&&(b[g]=!1));return!1},ID:function(a){return a[1].replace(j,"")},TAG:function(a,b){return a[1].replace(j,"").toLowerCase()},CHILD:function(a){if(a[1]==="nth"){a[2]||m.error(a[0]),a[2]=a[2].replace(/^\+|\s*/g,"");var b=/(-?)(\d*)(?:n([+\-]?\d*))?/.exec(a[2]==="even"&&"2n"||a[2]==="odd"&&"2n+1"||!/\D/.test(a[2])&&"0n+"+a[2]||a[2]);a[2]=b[1]+(b[2]||1)-0,a[3]=b[3]-0}else a[2]&&m.error(a[0]);a[0]=e++;return a},ATTR:function(a,b,c,d,e,f){var g=a[1]=a[1].replace(j,"");!f&&o.attrMap[g]&&(a[1]=o.attrMap[g]),a[4]=(a[4]||a[5]||"").replace(j,""),a[2]==="~="&&(a[4]=" "+a[4]+" ");return a},PSEUDO:function(b,c,d,e,f){if(b[1]==="not")if((a.exec(b[3])||"").length>1||/^\w/.test(b[3]))b[3]=m(b[3],null,null,c);else{var g=m.filter(b[3],c,d,!0^f);d||e.push.apply(e,g);return!1}else if(o.match.POS.test(b[0])||o.match.CHILD.test(b[0]))return!0;return b},POS:function(a){a.unshift(!0);return a}},filters:{enabled:function(a){return a.disabled===!1&&a.type!=="hidden"},disabled:function(a){return a.disabled===!0},checked:function(a){return a.checked===!0},selected:function(a){a.parentNode&&a.parentNode.selectedIndex;return a.selected===!0},parent:function(a){return!!a.firstChild},empty:function(a){return!a.firstChild},has:function(a,b,c){return!!m(c[3],a).length},header:function(a){return/h\d/i.test(a.nodeName)},text:function(a){var b=a.getAttribute("type"),c=a.type;return a.nodeName.toLowerCase()==="input"&&"text"===c&&(b===c||b===null)},radio:function(a){return a.nodeName.toLowerCase()==="input"&&"radio"===a.type},checkbox:function(a){return a.nodeName.toLowerCase()==="input"&&"checkbox"===a.type},file:function(a){return a.nodeName.toLowerCase()==="input"&&"file"===a.type},password:function(a){return a.nodeName.toLowerCase()==="input"&&"password"===a.type},submit:function(a){var b=a.nodeName.toLowerCase();return(b==="input"||b==="button")&&"submit"===a.type},image:function(a){return a.nodeName.toLowerCase()==="input"&&"image"===a.type},reset:function(a){var b=a.nodeName.toLowerCase();return(b==="input"||b==="button")&&"reset"===a.type},button:function(a){var b=a.nodeName.toLowerCase();return b==="input"&&"button"===a.type||b==="button"},input:function(a){return/input|select|textarea|button/i.test(a.nodeName)},focus:function(a){return a===a.ownerDocument.activeElement}},setFilters:{first:function(a,b){return b===0},last:function(a,b,c,d){return b===d.length-1},even:function(a,b){return b%2===0},odd:function(a,b){return b%2===1},lt:function(a,b,c){return b<c[3]-0},gt:function(a,b,c){return b>c[3]-0},nth:function(a,b,c){return c[3]-0===b},eq:function(a,b,c){return c[3]-0===b}},filter:{PSEUDO:function(a,b,c,d){var e=b[1],f=o.filters[e];if(f)return f(a,c,b,d);if(e==="contains")return(a.textContent||a.innerText||n([a])||"").indexOf(b[3])>=0;if(e==="not"){var g=b[3];for(var h=0,i=g.length;h<i;h++)if(g[h]===a)return!1;return!0}m.error(e)},CHILD:function(a,b){var c,e,f,g,h,i,j,k=b[1],l=a;switch(k){case"only":case"first":while(l=l.previousSibling)if(l.nodeType===1)return!1;if(k==="first")return!0;l=a;case"last":while(l=l.nextSibling)if(l.nodeType===1)return!1;return!0;case"nth":c=b[2],e=b[3];if(c===1&&e===0)return!0;f=b[0],g=a.parentNode;if(g&&(g[d]!==f||!a.nodeIndex)){i=0;for(l=g.firstChild;l;l=l.nextSibling)l.nodeType===1&&(l.nodeIndex=++i);g[d]=f}j=a.nodeIndex-e;return c===0?j===0:j%c===0&&j/c>=0}},ID:function(a,b){return a.nodeType===1&&a.getAttribute("id")===b},TAG:function(a,b){return b==="*"&&a.nodeType===1||!!a.nodeName&&a.nodeName.toLowerCase()===b},CLASS:function(a,b){return(" "+(a.className||a.getAttribute("class"))+" ").indexOf(b)>-1},ATTR:function(a,b){var c=b[1],d=m.attr?m.attr(a,c):o.attrHandle[c]?o.attrHandle[c](a):a[c]!=null?a[c]:a.getAttribute(c),e=d+"",f=b[2],g=b[4];return d==null?f==="!=":!f&&m.attr?d!=null:f==="="?e===g:f==="*="?e.indexOf(g)>=0:f==="~="?(" "+e+" ").indexOf(g)>=0:g?f==="!="?e!==g:f==="^="?e.indexOf(g)===0:f==="$="?e.substr(e.length-g.length)===g:f==="|="?e===g||e.substr(0,g.length+1)===g+"-":!1:e&&d!==!1},POS:function(a,b,c,d){var e=b[2],f=o.setFilters[e];if(f)return f(a,c,b,d)}}},p=o.match.POS,q=function(a,b){return"\\"+(b-0+1)};for(var r in o.match)o.match[r]=new RegExp(o.match[r].source+/(?![^\[]*\])(?![^\(]*\))/.source),o.leftMatch[r]=new RegExp(/(^(?:.|\r|\n)*?)/.source+o.match[r].source.replace(/\\(\d+)/g,q));o.match.globalPOS=p;var s=function(a,b){a=Array.prototype.slice.call(a,0);if(b){b.push.apply(b,a);return b}return a};try{Array.prototype.slice.call(c.documentElement.childNodes,0)[0].nodeType}catch(t){s=function(a,b){var c=0,d=b||[];if(g.call(a)==="[object Array]")Array.prototype.push.apply(d,a);else if(typeof a.length=="number")for(var e=a.length;c<e;c++)d.push(a[c]);else for(;a[c];c++)d.push(a[c]);return d}}var u,v;c.documentElement.compareDocumentPosition?u=function(a,b){if(a===b){h=!0;return 0}if(!a.compareDocumentPosition||!b.compareDocumentPosition)return a.compareDocumentPosition?-1:1;return a.compareDocumentPosition(b)&4?-1:1}:(u=function(a,b){if(a===b){h=!0;return 0}if(a.sourceIndex&&b.sourceIndex)return a.sourceIndex-b.sourceIndex;var c,d,e=[],f=[],g=a.parentNode,i=b.parentNode,j=g;if(g===i)return v(a,b);if(!g)return-1;if(!i)return 1;while(j)e.unshift(j),j=j.parentNode;j=i;while(j)f.unshift(j),j=j.parentNode;c=e.length,d=f.length;for(var k=0;k<c&&k<d;k++)if(e[k]!==f[k])return v(e[k],f[k]);return k===c?v(a,f[k],-1):v(e[k],b,1)},v=function(a,b,c){if(a===b)return c;var d=a.nextSibling;while(d){if(d===b)return-1;d=d.nextSibling}return 1}),function(){var a=c.createElement("div"),d="script"+(new Date).getTime(),e=c.documentElement;a.innerHTML="<a name='"+d+"'/>",e.insertBefore(a,e.firstChild),c.getElementById(d)&&(o.find.ID=function(a,c,d){if(typeof c.getElementById!="undefined"&&!d){var e=c.getElementById(a[1]);return e?e.id===a[1]||typeof e.getAttributeNode!="undefined"&&e.getAttributeNode("id").nodeValue===a[1]?[e]:b:[]}},o.filter.ID=function(a,b){var c=typeof a.getAttributeNode!="undefined"&&a.getAttributeNode("id");return a.nodeType===1&&c&&c.nodeValue===b}),e.removeChild(a),e=a=null}(),function(){var a=c.createElement("div");a.appendChild(c.createComment("")),a.getElementsByTagName("*").length>0&&(o.find.TAG=function(a,b){var c=b.getElementsByTagName(a[1]);if(a[1]==="*"){var d=[];for(var e=0;c[e];e++)c[e].nodeType===1&&d.push(c[e]);c=d}return c}),a.innerHTML="<a href='#'></a>",a.firstChild&&typeof a.firstChild.getAttribute!="undefined"&&a.firstChild.getAttribute("href")!=="#"&&(o.attrHandle.href=function(a){return a.getAttribute("href",2)}),a=null}(),c.querySelectorAll&&function(){var a=m,b=c.createElement("div"),d="__sizzle__";b.innerHTML="<p class='TEST'></p>";if(!b.querySelectorAll||b.querySelectorAll(".TEST").length!==0){m=function(b,e,f,g){e=e||c;if(!g&&!m.isXML(e)){var h=/^(\w+$)|^\.([\w\-]+$)|^#([\w\-]+$)/.exec(b);if(h&&(e.nodeType===1||e.nodeType===9)){if(h[1])return s(e.getElementsByTagName(b),f);if(h[2]&&o.find.CLASS&&e.getElementsByClassName)return s(e.getElementsByClassName(h[2]),f)}if(e.nodeType===9){if(b==="body"&&e.body)return s([e.body],f);if(h&&h[3]){var i=e.getElementById(h[3]);if(!i||!i.parentNode)return s([],f);if(i.id===h[3])return s([i],f)}try{return s(e.querySelectorAll(b),f)}catch(j){}}else if(e.nodeType===1&&e.nodeName.toLowerCase()!=="object"){var k=e,l=e.getAttribute("id"),n=l||d,p=e.parentNode,q=/^\s*[+~]/.test(b);l?n=n.replace(/'/g,"\\$&"):e.setAttribute("id",n),q&&p&&(e=e.parentNode);try{if(!q||p)return s(e.querySelectorAll("[id='"+n+"'] "+b),f)}catch(r){}finally{l||k.removeAttribute("id")}}}return a(b,e,f,g)};for(var e in a)m[e]=a[e];b=null}}(),function(){var a=c.documentElement,b=a.matchesSelector||a.mozMatchesSelector||a.webkitMatchesSelector||a.msMatchesSelector;if(b){var d=!b.call(c.createElement("div"),"div"),e=!1;try{b.call(c.documentElement,"[test!='']:sizzle")}catch(f){e=!0}m.matchesSelector=function(a,c){c=c.replace(/\=\s*([^'"\]]*)\s*\]/g,"='$1']");if(!m.isXML(a))try{if(e||!o.match.PSEUDO.test(c)&&!/!=/.test(c)){var f=b.call(a,c);if(f||!d||a.document&&a.document.nodeType!==11)return f}}catch(g){}return m(c,null,null,[a]).length>0}}}(),function(){var a=c.createElement("div");a.innerHTML="<div class='test e'></div><div class='test'></div>";if(!!a.getElementsByClassName&&a.getElementsByClassName("e").length!==0){a.lastChild.className="e";if(a.getElementsByClassName("e").length===1)return;o.order.splice(1,0,"CLASS"),o.find.CLASS=function(a,b,c){if(typeof b.getElementsByClassName!="undefined"&&!c)return b.getElementsByClassName(a[1])},a=null}}(),c.documentElement.contains?m.contains=function(a,b){return a!==b&&(a.contains?a.contains(b):!0)}:c.documentElement.compareDocumentPosition?m.contains=function(a,b){return!!(a.compareDocumentPosition(b)&16)}:m.contains=function(){return!1},m.isXML=function(a){var b=(a?a.ownerDocument||a:0).documentElement;return b?b.nodeName!=="HTML":!1};var y=function(a,b,c){var d,e=[],f="",g=b.nodeType?[b]:b;while(d=o.match.PSEUDO.exec(a))f+=d[0],a=a.replace(o.match.PSEUDO,"");a=o.relative[a]?a+"*":a;for(var h=0,i=g.length;h<i;h++)m(a,g[h],e,c);return m.filter(f,e)};m.attr=f.attr,m.selectors.attrMap={},f.find=m,f.expr=m.selectors,f.expr[":"]=f.expr.filters,f.unique=m.uniqueSort,f.text=m.getText,f.isXMLDoc=m.isXML,f.contains=m.contains}();var L=/Until$/,M=/^(?:parents|prevUntil|prevAll)/,N=/,/,O=/^.[^:#\[\.,]*$/,P=Array.prototype.slice,Q=f.expr.match.globalPOS,R={children:!0,contents:!0,next:!0,prev:!0};f.fn.extend({find:function(a){var b=this,c,d;if(typeof a!="string")return f(a).filter(function(){for(c=0,d=b.length;c<d;c++)if(f.contains(b[c],this))return!0});var e=this.pushStack("","find",a),g,h,i;for(c=0,d=this.length;c<d;c++){g=e.length,f.find(a,this[c],e);if(c>0)for(h=g;h<e.length;h++)for(i=0;i<g;i++)if(e[i]===e[h]){e.splice(h--,1);break}}return e},has:function(a){var b=f(a);return this.filter(function(){for(var a=0,c=b.length;a<c;a++)if(f.contains(this,b[a]))return!0})},not:function(a){return this.pushStack(T(this,a,!1),"not",a)},filter:function(a){return this.pushStack(T(this,a,!0),"filter",a)},is:function(a){return!!a&&(typeof a=="string"?Q.test(a)?f(a,this.context).index(this[0])>=0:f.filter(a,this).length>0:this.filter(a).length>0)},closest:function(a,b){var c=[],d,e,g=this[0];if(f.isArray(a)){var h=1;while(g&&g.ownerDocument&&g!==b){for(d=0;d<a.length;d++)f(g).is(a[d])&&c.push({selector:a[d],elem:g,level:h});g=g.parentNode,h++}return c}var i=Q.test(a)||typeof a!="string"?f(a,b||this.context):0;for(d=0,e=this.length;d<e;d++){g=this[d];while(g){if(i?i.index(g)>-1:f.find.matchesSelector(g,a)){c.push(g);break}g=g.parentNode;if(!g||!g.ownerDocument||g===b||g.nodeType===11)break}}c=c.length>1?f.unique(c):c;return this.pushStack(c,"closest",a)},index:function(a){if(!a)return this[0]&&this[0].parentNode?this.prevAll().length:-1;if(typeof a=="string")return f.inArray(this[0],f(a));return f.inArray(a.jquery?a[0]:a,this)},add:function(a,b){var c=typeof a=="string"?f(a,b):f.makeArray(a&&a.nodeType?[a]:a),d=f.merge(this.get(),c);return this.pushStack(S(c[0])||S(d[0])?d:f.unique(d))},andSelf:function(){return this.add(this.prevObject)}}),f.each({parent:function(a){var b=a.parentNode;return b&&b.nodeType!==11?b:null},parents:function(a){return f.dir(a,"parentNode")},parentsUntil:function(a,b,c){return f.dir(a,"parentNode",c)},next:function(a){return f.nth(a,2,"nextSibling")},prev:function(a){return f.nth(a,2,"previousSibling")},nextAll:function(a){return f.dir(a,"nextSibling")},prevAll:function(a){return f.dir(a,"previousSibling")},nextUntil:function(a,b,c){return f.dir(a,"nextSibling",c)},prevUntil:function(a,b,c){return f.dir(a,"previousSibling",c)},siblings:function(a){return f.sibling((a.parentNode||{}).firstChild,a)},children:function(a){return f.sibling(a.firstChild)},contents:function(a){return f.nodeName(a,"iframe")?a.contentDocument||a.contentWindow.document:f.makeArray(a.childNodes)}},function(a,b){f.fn[a]=function(c,d){var e=f.map(this,b,c);L.test(a)||(d=c),d&&typeof d=="string"&&(e=f.filter(d,e)),e=this.length>1&&!R[a]?f.unique(e):e,(this.length>1||N.test(d))&&M.test(a)&&(e=e.reverse());return this.pushStack(e,a,P.call(arguments).join(","))}}),f.extend({filter:function(a,b,c){c&&(a=":not("+a+")");return b.length===1?f.find.matchesSelector(b[0],a)?[b[0]]:[]:f.find.matches(a,b)},dir:function(a,c,d){var e=[],g=a[c];while(g&&g.nodeType!==9&&(d===b||g.nodeType!==1||!f(g).is(d)))g.nodeType===1&&e.push(g),g=g[c];return e},nth:function(a,b,c,d){b=b||1;var e=0;for(;a;a=a[c])if(a.nodeType===1&&++e===b)break;return a},sibling:function(a,b){var c=[];for(;a;a=a.nextSibling)a.nodeType===1&&a!==b&&c.push(a);return c}});var V="abbr|article|aside|audio|bdi|canvas|data|datalist|details|figcaption|figure|footer|header|hgroup|mark|meter|nav|output|progress|section|summary|time|video",W=/ jQuery\d+="(?:\d+|null)"/g,X=/^\s+/,Y=/<(?!area|br|col|embed|hr|img|input|link|meta|param)(([\w:]+)[^>]*)\/>/ig,Z=/<([\w:]+)/,$=/<tbody/i,_=/<|&#?\w+;/,ba=/<(?:script|style)/i,bb=/<(?:script|object|embed|option|style)/i,bc=new RegExp("<(?:"+V+")[\\s/>]","i"),bd=/checked\s*(?:[^=]|=\s*.checked.)/i,be=/\/(java|ecma)script/i,bf=/^\s*<!(?:\[CDATA\[|\-\-)/,bg={option:[1,"<select multiple='multiple'>","</select>"],legend:[1,"<fieldset>","</fieldset>"],thead:[1,"<table>","</table>"],tr:[2,"<table><tbody>","</tbody></table>"],td:[3,"<table><tbody><tr>","</tr></tbody></table>"],col:[2,"<table><tbody></tbody><colgroup>","</colgroup></table>"],area:[1,"<map>","</map>"],_default:[0,"",""]},bh=U(c);bg.optgroup=bg.option,bg.tbody=bg.tfoot=bg.colgroup=bg.caption=bg.thead,bg.th=bg.td,f.support.htmlSerialize||(bg._default=[1,"div<div>","</div>"]),f.fn.extend({text:function(a){return f.access(this,function(a){return a===b?f.text(this):this.empty().append((this[0]&&this[0].ownerDocument||c).createTextNode(a))},null,a,arguments.length)},wrapAll:function(a){if(f.isFunction(a))return this.each(function(b){f(this).wrapAll(a.call(this,b))});if(this[0]){var b=f(a,this[0].ownerDocument).eq(0).clone(!0);this[0].parentNode&&b.insertBefore(this[0]),b.map(function(){var a=this;while(a.firstChild&&a.firstChild.nodeType===1)a=a.firstChild;return a}).append(this)}return this},wrapInner:function(a){if(f.isFunction(a))return this.each(function(b){f(this).wrapInner(a.call(this,b))});return this.each(function(){var b=f(this),c=b.contents();c.length?c.wrapAll(a):b.append(a)})},wrap:function(a){var b=f.isFunction(a);return this.each(function(c){f(this).wrapAll(b?a.call(this,c):a)})},unwrap:function(){return this.parent().each(function(){f.nodeName(this,"body")||f(this).replaceWith(this.childNodes)}).end()},append:function(){return this.domManip(arguments,!0,function(a){this.nodeType===1&&this.appendChild(a)})},prepend:function(){return this.domManip(arguments,!0,function(a){this.nodeType===1&&this.insertBefore(a,this.firstChild)})},before:function(){if(this[0]&&this[0].parentNode)return this.domManip(arguments,!1,function(a){this.parentNode.insertBefore(a,this)});if(arguments.length){var a=f
.clean(arguments);a.push.apply(a,this.toArray());return this.pushStack(a,"before",arguments)}},after:function(){if(this[0]&&this[0].parentNode)return this.domManip(arguments,!1,function(a){this.parentNode.insertBefore(a,this.nextSibling)});if(arguments.length){var a=this.pushStack(this,"after",arguments);a.push.apply(a,f.clean(arguments));return a}},remove:function(a,b){for(var c=0,d;(d=this[c])!=null;c++)if(!a||f.filter(a,[d]).length)!b&&d.nodeType===1&&(f.cleanData(d.getElementsByTagName("*")),f.cleanData([d])),d.parentNode&&d.parentNode.removeChild(d);return this},empty:function(){for(var a=0,b;(b=this[a])!=null;a++){b.nodeType===1&&f.cleanData(b.getElementsByTagName("*"));while(b.firstChild)b.removeChild(b.firstChild)}return this},clone:function(a,b){a=a==null?!1:a,b=b==null?a:b;return this.map(function(){return f.clone(this,a,b)})},html:function(a){return f.access(this,function(a){var c=this[0]||{},d=0,e=this.length;if(a===b)return c.nodeType===1?c.innerHTML.replace(W,""):null;if(typeof a=="string"&&!ba.test(a)&&(f.support.leadingWhitespace||!X.test(a))&&!bg[(Z.exec(a)||["",""])[1].toLowerCase()]){a=a.replace(Y,"<$1></$2>");try{for(;d<e;d++)c=this[d]||{},c.nodeType===1&&(f.cleanData(c.getElementsByTagName("*")),c.innerHTML=a);c=0}catch(g){}}c&&this.empty().append(a)},null,a,arguments.length)},replaceWith:function(a){if(this[0]&&this[0].parentNode){if(f.isFunction(a))return this.each(function(b){var c=f(this),d=c.html();c.replaceWith(a.call(this,b,d))});typeof a!="string"&&(a=f(a).detach());return this.each(function(){var b=this.nextSibling,c=this.parentNode;f(this).remove(),b?f(b).before(a):f(c).append(a)})}return this.length?this.pushStack(f(f.isFunction(a)?a():a),"replaceWith",a):this},detach:function(a){return this.remove(a,!0)},domManip:function(a,c,d){var e,g,h,i,j=a[0],k=[];if(!f.support.checkClone&&arguments.length===3&&typeof j=="string"&&bd.test(j))return this.each(function(){f(this).domManip(a,c,d,!0)});if(f.isFunction(j))return this.each(function(e){var g=f(this);a[0]=j.call(this,e,c?g.html():b),g.domManip(a,c,d)});if(this[0]){i=j&&j.parentNode,f.support.parentNode&&i&&i.nodeType===11&&i.childNodes.length===this.length?e={fragment:i}:e=f.buildFragment(a,this,k),h=e.fragment,h.childNodes.length===1?g=h=h.firstChild:g=h.firstChild;if(g){c=c&&f.nodeName(g,"tr");for(var l=0,m=this.length,n=m-1;l<m;l++)d.call(c?bi(this[l],g):this[l],e.cacheable||m>1&&l<n?f.clone(h,!0,!0):h)}k.length&&f.each(k,function(a,b){b.src?f.ajax({type:"GET",global:!1,url:b.src,async:!1,dataType:"script"}):f.globalEval((b.text||b.textContent||b.innerHTML||"").replace(bf,"/*$0*/")),b.parentNode&&b.parentNode.removeChild(b)})}return this}}),f.buildFragment=function(a,b,d){var e,g,h,i,j=a[0];b&&b[0]&&(i=b[0].ownerDocument||b[0]),i.createDocumentFragment||(i=c),a.length===1&&typeof j=="string"&&j.length<512&&i===c&&j.charAt(0)==="<"&&!bb.test(j)&&(f.support.checkClone||!bd.test(j))&&(f.support.html5Clone||!bc.test(j))&&(g=!0,h=f.fragments[j],h&&h!==1&&(e=h)),e||(e=i.createDocumentFragment(),f.clean(a,i,e,d)),g&&(f.fragments[j]=h?e:1);return{fragment:e,cacheable:g}},f.fragments={},f.each({appendTo:"append",prependTo:"prepend",insertBefore:"before",insertAfter:"after",replaceAll:"replaceWith"},function(a,b){f.fn[a]=function(c){var d=[],e=f(c),g=this.length===1&&this[0].parentNode;if(g&&g.nodeType===11&&g.childNodes.length===1&&e.length===1){e[b](this[0]);return this}for(var h=0,i=e.length;h<i;h++){var j=(h>0?this.clone(!0):this).get();f(e[h])[b](j),d=d.concat(j)}return this.pushStack(d,a,e.selector)}}),f.extend({clone:function(a,b,c){var d,e,g,h=f.support.html5Clone||f.isXMLDoc(a)||!bc.test("<"+a.nodeName+">")?a.cloneNode(!0):bo(a);if((!f.support.noCloneEvent||!f.support.noCloneChecked)&&(a.nodeType===1||a.nodeType===11)&&!f.isXMLDoc(a)){bk(a,h),d=bl(a),e=bl(h);for(g=0;d[g];++g)e[g]&&bk(d[g],e[g])}if(b){bj(a,h);if(c){d=bl(a),e=bl(h);for(g=0;d[g];++g)bj(d[g],e[g])}}d=e=null;return h},clean:function(a,b,d,e){var g,h,i,j=[];b=b||c,typeof b.createElement=="undefined"&&(b=b.ownerDocument||b[0]&&b[0].ownerDocument||c);for(var k=0,l;(l=a[k])!=null;k++){typeof l=="number"&&(l+="");if(!l)continue;if(typeof l=="string")if(!_.test(l))l=b.createTextNode(l);else{l=l.replace(Y,"<$1></$2>");var m=(Z.exec(l)||["",""])[1].toLowerCase(),n=bg[m]||bg._default,o=n[0],p=b.createElement("div"),q=bh.childNodes,r;b===c?bh.appendChild(p):U(b).appendChild(p),p.innerHTML=n[1]+l+n[2];while(o--)p=p.lastChild;if(!f.support.tbody){var s=$.test(l),t=m==="table"&&!s?p.firstChild&&p.firstChild.childNodes:n[1]==="<table>"&&!s?p.childNodes:[];for(i=t.length-1;i>=0;--i)f.nodeName(t[i],"tbody")&&!t[i].childNodes.length&&t[i].parentNode.removeChild(t[i])}!f.support.leadingWhitespace&&X.test(l)&&p.insertBefore(b.createTextNode(X.exec(l)[0]),p.firstChild),l=p.childNodes,p&&(p.parentNode.removeChild(p),q.length>0&&(r=q[q.length-1],r&&r.parentNode&&r.parentNode.removeChild(r)))}var u;if(!f.support.appendChecked)if(l[0]&&typeof (u=l.length)=="number")for(i=0;i<u;i++)bn(l[i]);else bn(l);l.nodeType?j.push(l):j=f.merge(j,l)}if(d){g=function(a){return!a.type||be.test(a.type)};for(k=0;j[k];k++){h=j[k];if(e&&f.nodeName(h,"script")&&(!h.type||be.test(h.type)))e.push(h.parentNode?h.parentNode.removeChild(h):h);else{if(h.nodeType===1){var v=f.grep(h.getElementsByTagName("script"),g);j.splice.apply(j,[k+1,0].concat(v))}d.appendChild(h)}}}return j},cleanData:function(a){var b,c,d=f.cache,e=f.event.special,g=f.support.deleteExpando;for(var h=0,i;(i=a[h])!=null;h++){if(i.nodeName&&f.noData[i.nodeName.toLowerCase()])continue;c=i[f.expando];if(c){b=d[c];if(b&&b.events){for(var j in b.events)e[j]?f.event.remove(i,j):f.removeEvent(i,j,b.handle);b.handle&&(b.handle.elem=null)}g?delete i[f.expando]:i.removeAttribute&&i.removeAttribute(f.expando),delete d[c]}}}});var bp=/alpha\([^)]*\)/i,bq=/opacity=([^)]*)/,br=/([A-Z]|^ms)/g,bs=/^[\-+]?(?:\d*\.)?\d+$/i,bt=/^-?(?:\d*\.)?\d+(?!px)[^\d\s]+$/i,bu=/^([\-+])=([\-+.\de]+)/,bv=/^margin/,bw={position:"absolute",visibility:"hidden",display:"block"},bx=["Top","Right","Bottom","Left"],by,bz,bA;f.fn.css=function(a,c){return f.access(this,function(a,c,d){return d!==b?f.style(a,c,d):f.css(a,c)},a,c,arguments.length>1)},f.extend({cssHooks:{opacity:{get:function(a,b){if(b){var c=by(a,"opacity");return c===""?"1":c}return a.style.opacity}}},cssNumber:{fillOpacity:!0,fontWeight:!0,lineHeight:!0,opacity:!0,orphans:!0,widows:!0,zIndex:!0,zoom:!0},cssProps:{"float":f.support.cssFloat?"cssFloat":"styleFloat"},style:function(a,c,d,e){if(!!a&&a.nodeType!==3&&a.nodeType!==8&&!!a.style){var g,h,i=f.camelCase(c),j=a.style,k=f.cssHooks[i];c=f.cssProps[i]||i;if(d===b){if(k&&"get"in k&&(g=k.get(a,!1,e))!==b)return g;return j[c]}h=typeof d,h==="string"&&(g=bu.exec(d))&&(d=+(g[1]+1)*+g[2]+parseFloat(f.css(a,c)),h="number");if(d==null||h==="number"&&isNaN(d))return;h==="number"&&!f.cssNumber[i]&&(d+="px");if(!k||!("set"in k)||(d=k.set(a,d))!==b)try{j[c]=d}catch(l){}}},css:function(a,c,d){var e,g;c=f.camelCase(c),g=f.cssHooks[c],c=f.cssProps[c]||c,c==="cssFloat"&&(c="float");if(g&&"get"in g&&(e=g.get(a,!0,d))!==b)return e;if(by)return by(a,c)},swap:function(a,b,c){var d={},e,f;for(f in b)d[f]=a.style[f],a.style[f]=b[f];e=c.call(a);for(f in b)a.style[f]=d[f];return e}}),f.curCSS=f.css,c.defaultView&&c.defaultView.getComputedStyle&&(bz=function(a,b){var c,d,e,g,h=a.style;b=b.replace(br,"-$1").toLowerCase(),(d=a.ownerDocument.defaultView)&&(e=d.getComputedStyle(a,null))&&(c=e.getPropertyValue(b),c===""&&!f.contains(a.ownerDocument.documentElement,a)&&(c=f.style(a,b))),!f.support.pixelMargin&&e&&bv.test(b)&&bt.test(c)&&(g=h.width,h.width=c,c=e.width,h.width=g);return c}),c.documentElement.currentStyle&&(bA=function(a,b){var c,d,e,f=a.currentStyle&&a.currentStyle[b],g=a.style;f==null&&g&&(e=g[b])&&(f=e),bt.test(f)&&(c=g.left,d=a.runtimeStyle&&a.runtimeStyle.left,d&&(a.runtimeStyle.left=a.currentStyle.left),g.left=b==="fontSize"?"1em":f,f=g.pixelLeft+"px",g.left=c,d&&(a.runtimeStyle.left=d));return f===""?"auto":f}),by=bz||bA,f.each(["height","width"],function(a,b){f.cssHooks[b]={get:function(a,c,d){if(c)return a.offsetWidth!==0?bB(a,b,d):f.swap(a,bw,function(){return bB(a,b,d)})},set:function(a,b){return bs.test(b)?b+"px":b}}}),f.support.opacity||(f.cssHooks.opacity={get:function(a,b){return bq.test((b&&a.currentStyle?a.currentStyle.filter:a.style.filter)||"")?parseFloat(RegExp.$1)/100+"":b?"1":""},set:function(a,b){var c=a.style,d=a.currentStyle,e=f.isNumeric(b)?"alpha(opacity="+b*100+")":"",g=d&&d.filter||c.filter||"";c.zoom=1;if(b>=1&&f.trim(g.replace(bp,""))===""){c.removeAttribute("filter");if(d&&!d.filter)return}c.filter=bp.test(g)?g.replace(bp,e):g+" "+e}}),f(function(){f.support.reliableMarginRight||(f.cssHooks.marginRight={get:function(a,b){return f.swap(a,{display:"inline-block"},function(){return b?by(a,"margin-right"):a.style.marginRight})}})}),f.expr&&f.expr.filters&&(f.expr.filters.hidden=function(a){var b=a.offsetWidth,c=a.offsetHeight;return b===0&&c===0||!f.support.reliableHiddenOffsets&&(a.style&&a.style.display||f.css(a,"display"))==="none"},f.expr.filters.visible=function(a){return!f.expr.filters.hidden(a)}),f.each({margin:"",padding:"",border:"Width"},function(a,b){f.cssHooks[a+b]={expand:function(c){var d,e=typeof c=="string"?c.split(" "):[c],f={};for(d=0;d<4;d++)f[a+bx[d]+b]=e[d]||e[d-2]||e[0];return f}}});var bC=/%20/g,bD=/\[\]$/,bE=/\r?\n/g,bF=/#.*$/,bG=/^(.*?):[ \t]*([^\r\n]*)\r?$/mg,bH=/^(?:color|date|datetime|datetime-local|email|hidden|month|number|password|range|search|tel|text|time|url|week)$/i,bI=/^(?:about|app|app\-storage|.+\-extension|file|res|widget):$/,bJ=/^(?:GET|HEAD)$/,bK=/^\/\//,bL=/\?/,bM=/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,bN=/^(?:select|textarea)/i,bO=/\s+/,bP=/([?&])_=[^&]*/,bQ=/^([\w\+\.\-]+:)(?:\/\/([^\/?#:]*)(?::(\d+))?)?/,bR=f.fn.load,bS={},bT={},bU,bV,bW=["*/"]+["*"];try{bU=e.href}catch(bX){bU=c.createElement("a"),bU.href="",bU=bU.href}bV=bQ.exec(bU.toLowerCase())||[],f.fn.extend({load:function(a,c,d){if(typeof a!="string"&&bR)return bR.apply(this,arguments);if(!this.length)return this;var e=a.indexOf(" ");if(e>=0){var g=a.slice(e,a.length);a=a.slice(0,e)}var h="GET";c&&(f.isFunction(c)?(d=c,c=b):typeof c=="object"&&(c=f.param(c,f.ajaxSettings.traditional),h="POST"));var i=this;f.ajax({url:a,type:h,dataType:"html",data:c,complete:function(a,b,c){c=a.responseText,a.isResolved()&&(a.done(function(a){c=a}),i.html(g?f("<div>").append(c.replace(bM,"")).find(g):c)),d&&i.each(d,[c,b,a])}});return this},serialize:function(){return f.param(this.serializeArray())},serializeArray:function(){return this.map(function(){return this.elements?f.makeArray(this.elements):this}).filter(function(){return this.name&&!this.disabled&&(this.checked||bN.test(this.nodeName)||bH.test(this.type))}).map(function(a,b){var c=f(this).val();return c==null?null:f.isArray(c)?f.map(c,function(a,c){return{name:b.name,value:a.replace(bE,"\r\n")}}):{name:b.name,value:c.replace(bE,"\r\n")}}).get()}}),f.each("ajaxStart ajaxStop ajaxComplete ajaxError ajaxSuccess ajaxSend".split(" "),function(a,b){f.fn[b]=function(a){return this.on(b,a)}}),f.each(["get","post"],function(a,c){f[c]=function(a,d,e,g){f.isFunction(d)&&(g=g||e,e=d,d=b);return f.ajax({type:c,url:a,data:d,success:e,dataType:g})}}),f.extend({getScript:function(a,c){return f.get(a,b,c,"script")},getJSON:function(a,b,c){return f.get(a,b,c,"json")},ajaxSetup:function(a,b){b?b$(a,f.ajaxSettings):(b=a,a=f.ajaxSettings),b$(a,b);return a},ajaxSettings:{url:bU,isLocal:bI.test(bV[1]),global:!0,type:"GET",contentType:"application/x-www-form-urlencoded; charset=UTF-8",processData:!0,async:!0,accepts:{xml:"application/xml, text/xml",html:"text/html",text:"text/plain",json:"application/json, text/javascript","*":bW},contents:{xml:/xml/,html:/html/,json:/json/},responseFields:{xml:"responseXML",text:"responseText"},converters:{"* text":a.String,"text html":!0,"text json":f.parseJSON,"text xml":f.parseXML},flatOptions:{context:!0,url:!0}},ajaxPrefilter:bY(bS),ajaxTransport:bY(bT),ajax:function(a,c){function w(a,c,l,m){if(s!==2){s=2,q&&clearTimeout(q),p=b,n=m||"",v.readyState=a>0?4:0;var o,r,u,w=c,x=l?ca(d,v,l):b,y,z;if(a>=200&&a<300||a===304){if(d.ifModified){if(y=v.getResponseHeader("Last-Modified"))f.lastModified[k]=y;if(z=v.getResponseHeader("Etag"))f.etag[k]=z}if(a===304)w="notmodified",o=!0;else try{r=cb(d,x),w="success",o=!0}catch(A){w="parsererror",u=A}}else{u=w;if(!w||a)w="error",a<0&&(a=0)}v.status=a,v.statusText=""+(c||w),o?h.resolveWith(e,[r,w,v]):h.rejectWith(e,[v,w,u]),v.statusCode(j),j=b,t&&g.trigger("ajax"+(o?"Success":"Error"),[v,d,o?r:u]),i.fireWith(e,[v,w]),t&&(g.trigger("ajaxComplete",[v,d]),--f.active||f.event.trigger("ajaxStop"))}}typeof a=="object"&&(c=a,a=b),c=c||{};var d=f.ajaxSetup({},c),e=d.context||d,g=e!==d&&(e.nodeType||e instanceof f)?f(e):f.event,h=f.Deferred(),i=f.Callbacks("once memory"),j=d.statusCode||{},k,l={},m={},n,o,p,q,r,s=0,t,u,v={readyState:0,setRequestHeader:function(a,b){if(!s){var c=a.toLowerCase();a=m[c]=m[c]||a,l[a]=b}return this},getAllResponseHeaders:function(){return s===2?n:null},getResponseHeader:function(a){var c;if(s===2){if(!o){o={};while(c=bG.exec(n))o[c[1].toLowerCase()]=c[2]}c=o[a.toLowerCase()]}return c===b?null:c},overrideMimeType:function(a){s||(d.mimeType=a);return this},abort:function(a){a=a||"abort",p&&p.abort(a),w(0,a);return this}};h.promise(v),v.success=v.done,v.error=v.fail,v.complete=i.add,v.statusCode=function(a){if(a){var b;if(s<2)for(b in a)j[b]=[j[b],a[b]];else b=a[v.status],v.then(b,b)}return this},d.url=((a||d.url)+"").replace(bF,"").replace(bK,bV[1]+"//"),d.dataTypes=f.trim(d.dataType||"*").toLowerCase().split(bO),d.crossDomain==null&&(r=bQ.exec(d.url.toLowerCase()),d.crossDomain=!(!r||r[1]==bV[1]&&r[2]==bV[2]&&(r[3]||(r[1]==="http:"?80:443))==(bV[3]||(bV[1]==="http:"?80:443)))),d.data&&d.processData&&typeof d.data!="string"&&(d.data=f.param(d.data,d.traditional)),bZ(bS,d,c,v);if(s===2)return!1;t=d.global,d.type=d.type.toUpperCase(),d.hasContent=!bJ.test(d.type),t&&f.active++===0&&f.event.trigger("ajaxStart");if(!d.hasContent){d.data&&(d.url+=(bL.test(d.url)?"&":"?")+d.data,delete d.data),k=d.url;if(d.cache===!1){var x=f.now(),y=d.url.replace(bP,"$1_="+x);d.url=y+(y===d.url?(bL.test(d.url)?"&":"?")+"_="+x:"")}}(d.data&&d.hasContent&&d.contentType!==!1||c.contentType)&&v.setRequestHeader("Content-Type",d.contentType),d.ifModified&&(k=k||d.url,f.lastModified[k]&&v.setRequestHeader("If-Modified-Since",f.lastModified[k]),f.etag[k]&&v.setRequestHeader("If-None-Match",f.etag[k])),v.setRequestHeader("Accept",d.dataTypes[0]&&d.accepts[d.dataTypes[0]]?d.accepts[d.dataTypes[0]]+(d.dataTypes[0]!=="*"?", "+bW+"; q=0.01":""):d.accepts["*"]);for(u in d.headers)v.setRequestHeader(u,d.headers[u]);if(d.beforeSend&&(d.beforeSend.call(e,v,d)===!1||s===2)){v.abort();return!1}for(u in{success:1,error:1,complete:1})v[u](d[u]);p=bZ(bT,d,c,v);if(!p)w(-1,"No Transport");else{v.readyState=1,t&&g.trigger("ajaxSend",[v,d]),d.async&&d.timeout>0&&(q=setTimeout(function(){v.abort("timeout")},d.timeout));try{s=1,p.send(l,w)}catch(z){if(s<2)w(-1,z);else throw z}}return v},param:function(a,c){var d=[],e=function(a,b){b=f.isFunction(b)?b():b,d[d.length]=encodeURIComponent(a)+"="+encodeURIComponent(b)};c===b&&(c=f.ajaxSettings.traditional);if(f.isArray(a)||a.jquery&&!f.isPlainObject(a))f.each(a,function(){e(this.name,this.value)});else for(var g in a)b_(g,a[g],c,e);return d.join("&").replace(bC,"+")}}),f.extend({active:0,lastModified:{},etag:{}});var cc=f.now(),cd=/(\=)\?(&|$)|\?\?/i;f.ajaxSetup({jsonp:"callback",jsonpCallback:function(){return f.expando+"_"+cc++}}),f.ajaxPrefilter("json jsonp",function(b,c,d){var e=typeof b.data=="string"&&/^application\/x\-www\-form\-urlencoded/.test(b.contentType);if(b.dataTypes[0]==="jsonp"||b.jsonp!==!1&&(cd.test(b.url)||e&&cd.test(b.data))){var g,h=b.jsonpCallback=f.isFunction(b.jsonpCallback)?b.jsonpCallback():b.jsonpCallback,i=a[h],j=b.url,k=b.data,l="$1"+h+"$2";b.jsonp!==!1&&(j=j.replace(cd,l),b.url===j&&(e&&(k=k.replace(cd,l)),b.data===k&&(j+=(/\?/.test(j)?"&":"?")+b.jsonp+"="+h))),b.url=j,b.data=k,a[h]=function(a){g=[a]},d.always(function(){a[h]=i,g&&f.isFunction(i)&&a[h](g[0])}),b.converters["script json"]=function(){g||f.error(h+" was not called");return g[0]},b.dataTypes[0]="json";return"script"}}),f.ajaxSetup({accepts:{script:"text/javascript, application/javascript, application/ecmascript, application/x-ecmascript"},contents:{script:/javascript|ecmascript/},converters:{"text script":function(a){f.globalEval(a);return a}}}),f.ajaxPrefilter("script",function(a){a.cache===b&&(a.cache=!1),a.crossDomain&&(a.type="GET",a.global=!1)}),f.ajaxTransport("script",function(a){if(a.crossDomain){var d,e=c.head||c.getElementsByTagName("head")[0]||c.documentElement;return{send:function(f,g){d=c.createElement("script"),d.async="async",a.scriptCharset&&(d.charset=a.scriptCharset),d.src=a.url,d.onload=d.onreadystatechange=function(a,c){if(c||!d.readyState||/loaded|complete/.test(d.readyState))d.onload=d.onreadystatechange=null,e&&d.parentNode&&e.removeChild(d),d=b,c||g(200,"success")},e.insertBefore(d,e.firstChild)},abort:function(){d&&d.onload(0,1)}}}});var ce=a.ActiveXObject?function(){for(var a in cg)cg[a](0,1)}:!1,cf=0,cg;f.ajaxSettings.xhr=a.ActiveXObject?function(){return!this.isLocal&&ch()||ci()}:ch,function(a){f.extend(f.support,{ajax:!!a,cors:!!a&&"withCredentials"in a})}(f.ajaxSettings.xhr()),f.support.ajax&&f.ajaxTransport(function(c){if(!c.crossDomain||f.support.cors){var d;return{send:function(e,g){var h=c.xhr(),i,j;c.username?h.open(c.type,c.url,c.async,c.username,c.password):h.open(c.type,c.url,c.async);if(c.xhrFields)for(j in c.xhrFields)h[j]=c.xhrFields[j];c.mimeType&&h.overrideMimeType&&h.overrideMimeType(c.mimeType),!c.crossDomain&&!e["X-Requested-With"]&&(e["X-Requested-With"]="XMLHttpRequest");try{for(j in e)h.setRequestHeader(j,e[j])}catch(k){}h.send(c.hasContent&&c.data||null),d=function(a,e){var j,k,l,m,n;try{if(d&&(e||h.readyState===4)){d=b,i&&(h.onreadystatechange=f.noop,ce&&delete cg[i]);if(e)h.readyState!==4&&h.abort();else{j=h.status,l=h.getAllResponseHeaders(),m={},n=h.responseXML,n&&n.documentElement&&(m.xml=n);try{m.text=h.responseText}catch(a){}try{k=h.statusText}catch(o){k=""}!j&&c.isLocal&&!c.crossDomain?j=m.text?200:404:j===1223&&(j=204)}}}catch(p){e||g(-1,p)}m&&g(j,k,m,l)},!c.async||h.readyState===4?d():(i=++cf,ce&&(cg||(cg={},f(a).unload(ce)),cg[i]=d),h.onreadystatechange=d)},abort:function(){d&&d(0,1)}}}});var cj={},ck,cl,cm=/^(?:toggle|show|hide)$/,cn=/^([+\-]=)?([\d+.\-]+)([a-z%]*)$/i,co,cp=[["height","marginTop","marginBottom","paddingTop","paddingBottom"],["width","marginLeft","marginRight","paddingLeft","paddingRight"],["opacity"]],cq;f.fn.extend({show:function(a,b,c){var d,e;if(a||a===0)return this.animate(ct("show",3),a,b,c);for(var g=0,h=this.length;g<h;g++)d=this[g],d.style&&(e=d.style.display,!f._data(d,"olddisplay")&&e==="none"&&(e=d.style.display=""),(e===""&&f.css(d,"display")==="none"||!f.contains(d.ownerDocument.documentElement,d))&&f._data(d,"olddisplay",cu(d.nodeName)));for(g=0;g<h;g++){d=this[g];if(d.style){e=d.style.display;if(e===""||e==="none")d.style.display=f._data(d,"olddisplay")||""}}return this},hide:function(a,b,c){if(a||a===0)return this.animate(ct("hide",3),a,b,c);var d,e,g=0,h=this.length;for(;g<h;g++)d=this[g],d.style&&(e=f.css(d,"display"),e!=="none"&&!f._data(d,"olddisplay")&&f._data(d,"olddisplay",e));for(g=0;g<h;g++)this[g].style&&(this[g].style.display="none");return this},_toggle:f.fn.toggle,toggle:function(a,b,c){var d=typeof a=="boolean";f.isFunction(a)&&f.isFunction(b)?this._toggle.apply(this,arguments):a==null||d?this.each(function(){var b=d?a:f(this).is(":hidden");f(this)[b?"show":"hide"]()}):this.animate(ct("toggle",3),a,b,c);return this},fadeTo:function(a,b,c,d){return this.filter(":hidden").css("opacity",0).show().end().animate({opacity:b},a,c,d)},animate:function(a,b,c,d){function g(){e.queue===!1&&f._mark(this);var b=f.extend({},e),c=this.nodeType===1,d=c&&f(this).is(":hidden"),g,h,i,j,k,l,m,n,o,p,q;b.animatedProperties={};for(i in a){g=f.camelCase(i),i!==g&&(a[g]=a[i],delete a[i]);if((k=f.cssHooks[g])&&"expand"in k){l=k.expand(a[g]),delete a[g];for(i in l)i in a||(a[i]=l[i])}}for(g in a){h=a[g],f.isArray(h)?(b.animatedProperties[g]=h[1],h=a[g]=h[0]):b.animatedProperties[g]=b.specialEasing&&b.specialEasing[g]||b.easing||"swing";if(h==="hide"&&d||h==="show"&&!d)return b.complete.call(this);c&&(g==="height"||g==="width")&&(b.overflow=[this.style.overflow,this.style.overflowX,this.style.overflowY],f.css(this,"display")==="inline"&&f.css(this,"float")==="none"&&(!f.support.inlineBlockNeedsLayout||cu(this.nodeName)==="inline"?this.style.display="inline-block":this.style.zoom=1))}b.overflow!=null&&(this.style.overflow="hidden");for(i in a)j=new f.fx(this,b,i),h=a[i],cm.test(h)?(q=f._data(this,"toggle"+i)||(h==="toggle"?d?"show":"hide":0),q?(f._data(this,"toggle"+i,q==="show"?"hide":"show"),j[q]()):j[h]()):(m=cn.exec(h),n=j.cur(),m?(o=parseFloat(m[2]),p=m[3]||(f.cssNumber[i]?"":"px"),p!=="px"&&(f.style(this,i,(o||1)+p),n=(o||1)/j.cur()*n,f.style(this,i,n+p)),m[1]&&(o=(m[1]==="-="?-1:1)*o+n),j.custom(n,o,p)):j.custom(n,h,""));return!0}var e=f.speed(b,c,d);if(f.isEmptyObject(a))return this.each(e.complete,[!1]);a=f.extend({},a);return e.queue===!1?this.each(g):this.queue(e.queue,g)},stop:function(a,c,d){typeof a!="string"&&(d=c,c=a,a=b),c&&a!==!1&&this.queue(a||"fx",[]);return this.each(function(){function h(a,b,c){var e=b[c];f.removeData(a,c,!0),e.stop(d)}var b,c=!1,e=f.timers,g=f._data(this);d||f._unmark(!0,this);if(a==null)for(b in g)g[b]&&g[b].stop&&b.indexOf(".run")===b.length-4&&h(this,g,b);else g[b=a+".run"]&&g[b].stop&&h(this,g,b);for(b=e.length;b--;)e[b].elem===this&&(a==null||e[b].queue===a)&&(d?e[b](!0):e[b].saveState(),c=!0,e.splice(b,1));(!d||!c)&&f.dequeue(this,a)})}}),f.each({slideDown:ct("show",1),slideUp:ct("hide",1),slideToggle:ct("toggle",1),fadeIn:{opacity:"show"},fadeOut:{opacity:"hide"},fadeToggle:{opacity:"toggle"}},function(a,b){f.fn[a]=function(a,c,d){return this.animate(b,a,c,d)}}),f.extend({speed:function(a,b,c){var d=a&&typeof a=="object"?f.extend({},a):{complete:c||!c&&b||f.isFunction(a)&&a,duration:a,easing:c&&b||b&&!f.isFunction(b)&&b};d.duration=f.fx.off?0:typeof d.duration=="number"?d.duration:d.duration in f.fx.speeds?f.fx.speeds[d.duration]:f.fx.speeds._default;if(d.queue==null||d.queue===!0)d.queue="fx";d.old=d.complete,d.complete=function(a){f.isFunction(d.old)&&d.old.call(this),d.queue?f.dequeue(this,d.queue):a!==!1&&f._unmark(this)};return d},easing:{linear:function(a){return a},swing:function(a){return-Math.cos(a*Math.PI)/2+.5}},timers:[],fx:function(a,b,c){this.options=b,this.elem=a,this.prop=c,b.orig=b.orig||{}}}),f.fx.prototype={update:function(){this.options.step&&this.options.step.call(this.elem,this.now,this),(f.fx.step[this.prop]||f.fx.step._default)(this)},cur:function(){if(this.elem[this.prop]!=null&&(!this.elem.style||this.elem.style[this.prop]==null))return this.elem[this.prop];var a,b=f.css(this.elem,this.prop);return isNaN(a=parseFloat(b))?!b||b==="auto"?0:b:a},custom:function(a,c,d){function h(a){return e.step(a)}var e=this,g=f.fx;this.startTime=cq||cr(),this.end=c,this.now=this.start=a,this.pos=this.state=0,this.unit=d||this.unit||(f.cssNumber[this.prop]?"":"px"),h.queue=this.options.queue,h.elem=this.elem,h.saveState=function(){f._data(e.elem,"fxshow"+e.prop)===b&&(e.options.hide?f._data(e.elem,"fxshow"+e.prop,e.start):e.options.show&&f._data(e.elem,"fxshow"+e.prop,e.end))},h()&&f.timers.push(h)&&!co&&(co=setInterval(g.tick,g.interval))},show:function(){var a=f._data(this.elem,"fxshow"+this.prop);this.options.orig[this.prop]=a||f.style(this.elem,this.prop),this.options.show=!0,a!==b?this.custom(this.cur(),a):this.custom(this.prop==="width"||this.prop==="height"?1:0,this.cur()),f(this.elem).show()},hide:function(){this.options.orig[this.prop]=f._data(this.elem,"fxshow"+this.prop)||f.style(this.elem,this.prop),this.options.hide=!0,this.custom(this.cur(),0)},step:function(a){var b,c,d,e=cq||cr(),g=!0,h=this.elem,i=this.options;if(a||e>=i.duration+this.startTime){this.now=this.end,this.pos=this.state=1,this.update(),i.animatedProperties[this.prop]=!0;for(b in i.animatedProperties)i.animatedProperties[b]!==!0&&(g=!1);if(g){i.overflow!=null&&!f.support.shrinkWrapBlocks&&f.each(["","X","Y"],function(a,b){h.style["overflow"+b]=i.overflow[a]}),i.hide&&f(h).hide();if(i.hide||i.show)for(b in i.animatedProperties)f.style(h,b,i.orig[b]),f.removeData(h,"fxshow"+b,!0),f.removeData(h,"toggle"+b,!0);d=i.complete,d&&(i.complete=!1,d.call(h))}return!1}i.duration==Infinity?this.now=e:(c=e-this.startTime,this.state=c/i.duration,this.pos=f.easing[i.animatedProperties[this.prop]](this.state,c,0,1,i.duration),this.now=this.start+(this.end-this.start)*this.pos),this.update();return!0}},f.extend(f.fx,{tick:function(){var a,b=f.timers,c=0;for(;c<b.length;c++)a=b[c],!a()&&b[c]===a&&b.splice(c--,1);b.length||f.fx.stop()},interval:13,stop:function(){clearInterval(co),co=null},speeds:{slow:600,fast:200,_default:400},step:{opacity:function(a){f.style(a.elem,"opacity",a.now)},_default:function(a){a.elem.style&&a.elem.style[a.prop]!=null?a.elem.style[a.prop]=a.now+a.unit:a.elem[a.prop]=a.now}}}),f.each(cp.concat.apply([],cp),function(a,b){b.indexOf("margin")&&(f.fx.step[b]=function(a){f.style(a.elem,b,Math.max(0,a.now)+a.unit)})}),f.expr&&f.expr.filters&&(f.expr.filters.animated=function(a){return f.grep(f.timers,function(b){return a===b.elem}).length});var cv,cw=/^t(?:able|d|h)$/i,cx=/^(?:body|html)$/i;"getBoundingClientRect"in c.documentElement?cv=function(a,b,c,d){try{d=a.getBoundingClientRect()}catch(e){}if(!d||!f.contains(c,a))return d?{top:d.top,left:d.left}:{top:0,left:0};var g=b.body,h=cy(b),i=c.clientTop||g.clientTop||0,j=c.clientLeft||g.clientLeft||0,k=h.pageYOffset||f.support.boxModel&&c.scrollTop||g.scrollTop,l=h.pageXOffset||f.support.boxModel&&c.scrollLeft||g.scrollLeft,m=d.top+k-i,n=d.left+l-j;return{top:m,left:n}}:cv=function(a,b,c){var d,e=a.offsetParent,g=a,h=b.body,i=b.defaultView,j=i?i.getComputedStyle(a,null):a.currentStyle,k=a.offsetTop,l=a.offsetLeft;while((a=a.parentNode)&&a!==h&&a!==c){if(f.support.fixedPosition&&j.position==="fixed")break;d=i?i.getComputedStyle(a,null):a.currentStyle,k-=a.scrollTop,l-=a.scrollLeft,a===e&&(k+=a.offsetTop,l+=a.offsetLeft,f.support.doesNotAddBorder&&(!f.support.doesAddBorderForTableAndCells||!cw.test(a.nodeName))&&(k+=parseFloat(d.borderTopWidth)||0,l+=parseFloat(d.borderLeftWidth)||0),g=e,e=a.offsetParent),f.support.subtractsBorderForOverflowNotVisible&&d.overflow!=="visible"&&(k+=parseFloat(d.borderTopWidth)||0,l+=parseFloat(d.borderLeftWidth)||0),j=d}if(j.position==="relative"||j.position==="static")k+=h.offsetTop,l+=h.offsetLeft;f.support.fixedPosition&&j.position==="fixed"&&(k+=Math.max(c.scrollTop,h.scrollTop),l+=Math.max(c.scrollLeft,h.scrollLeft));return{top:k,left:l}},f.fn.offset=function(a){if(arguments.length)return a===b?this:this.each(function(b){f.offset.setOffset(this,a,b)});var c=this[0],d=c&&c.ownerDocument;if(!d)return null;if(c===d.body)return f.offset.bodyOffset(c);return cv(c,d,d.documentElement)},f.offset={bodyOffset:function(a){var b=a.offsetTop,c=a.offsetLeft;f.support.doesNotIncludeMarginInBodyOffset&&(b+=parseFloat(f.css(a,"marginTop"))||0,c+=parseFloat(f.css(a,"marginLeft"))||0);return{top:b,left:c}},setOffset:function(a,b,c){var d=f.css(a,"position");d==="static"&&(a.style.position="relative");var e=f(a),g=e.offset(),h=f.css(a,"top"),i=f.css(a,"left"),j=(d==="absolute"||d==="fixed")&&f.inArray("auto",[h,i])>-1,k={},l={},m,n;j?(l=e.position(),m=l.top,n=l.left):(m=parseFloat(h)||0,n=parseFloat(i)||0),f.isFunction(b)&&(b=b.call(a,c,g)),b.top!=null&&(k.top=b.top-g.top+m),b.left!=null&&(k.left=b.left-g.left+n),"using"in b?b.using.call(a,k):e.css(k)}},f.fn.extend({position:function(){if(!this[0])return null;var a=this[0],b=this.offsetParent(),c=this.offset(),d=cx.test(b[0].nodeName)?{top:0,left:0}:b.offset();c.top-=parseFloat(f.css(a,"marginTop"))||0,c.left-=parseFloat(f.css(a,"marginLeft"))||0,d.top+=parseFloat(f.css(b[0],"borderTopWidth"))||0,d.left+=parseFloat(f.css(b[0],"borderLeftWidth"))||0;return{top:c.top-d.top,left:c.left-d.left}},offsetParent:function(){return this.map(function(){var a=this.offsetParent||c.body;while(a&&!cx.test(a.nodeName)&&f.css(a,"position")==="static")a=a.offsetParent;return a})}}),f.each({scrollLeft:"pageXOffset",scrollTop:"pageYOffset"},function(a,c){var d=/Y/.test(c);f.fn[a]=function(e){return f.access(this,function(a,e,g){var h=cy(a);if(g===b)return h?c in h?h[c]:f.support.boxModel&&h.document.documentElement[e]||h.document.body[e]:a[e];h?h.scrollTo(d?f(h).scrollLeft():g,d?g:f(h).scrollTop()):a[e]=g},a,e,arguments.length,null)}}),f.each({Height:"height",Width:"width"},function(a,c){var d="client"+a,e="scroll"+a,g="offset"+a;f.fn["inner"+a]=function(){var a=this[0];return a?a.style?parseFloat(f.css(a,c,"padding")):this[c]():null},f.fn["outer"+a]=function(a){var b=this[0];return b?b.style?parseFloat(f.css(b,c,a?"margin":"border")):this[c]():null},f.fn[c]=function(a){return f.access(this,function(a,c,h){var i,j,k,l;if(f.isWindow(a)){i=a.document,j=i.documentElement[d];return f.support.boxModel&&j||i.body&&i.body[d]||j}if(a.nodeType===9){i=a.documentElement;if(i[d]>=i[e])return i[d];return Math.max(a.body[e],i[e],a.body[g],i[g])}if(h===b){k=f.css(a,c),l=parseFloat(k);return f.isNumeric(l)?l:k}f(a).css(c,h)},c,a,arguments.length,null)}}),a.jQuery=a.$=f,typeof define=="function"&&define.amd&&define.amd.jQuery&&define("jquery",[],function(){return f})})(window);
// Underscore.js 1.3.3
// (c) 2009-2012 Jeremy Ashkenas, DocumentCloud Inc.
// Underscore is freely distributable under the MIT license.
// Portions of Underscore are inspired or borrowed from Prototype,
// Oliver Steele's Functional, and John Resig's Micro-Templating.
// For all details and documentation:
// http://documentcloud.github.com/underscore
(function(){function r(a,c,d){if(a===c)return 0!==a||1/a==1/c;if(null==a||null==c)return a===c;a._chain&&(a=a._wrapped);c._chain&&(c=c._wrapped);if(a.isEqual&&b.isFunction(a.isEqual))return a.isEqual(c);if(c.isEqual&&b.isFunction(c.isEqual))return c.isEqual(a);var e=l.call(a);if(e!=l.call(c))return!1;switch(e){case "[object String]":return a==""+c;case "[object Number]":return a!=+a?c!=+c:0==a?1/a==1/c:a==+c;case "[object Date]":case "[object Boolean]":return+a==+c;case "[object RegExp]":return a.source==
c.source&&a.global==c.global&&a.multiline==c.multiline&&a.ignoreCase==c.ignoreCase}if("object"!=typeof a||"object"!=typeof c)return!1;for(var f=d.length;f--;)if(d[f]==a)return!0;d.push(a);var f=0,g=!0;if("[object Array]"==e){if(f=a.length,g=f==c.length)for(;f--&&(g=f in a==f in c&&r(a[f],c[f],d)););}else{if("constructor"in a!="constructor"in c||a.constructor!=c.constructor)return!1;for(var h in a)if(b.has(a,h)&&(f++,!(g=b.has(c,h)&&r(a[h],c[h],d))))break;if(g){for(h in c)if(b.has(c,h)&&!f--)break;
g=!f}}d.pop();return g}var s=this,I=s._,o={},k=Array.prototype,p=Object.prototype,i=k.slice,J=k.unshift,l=p.toString,K=p.hasOwnProperty,y=k.forEach,z=k.map,A=k.reduce,B=k.reduceRight,C=k.filter,D=k.every,E=k.some,q=k.indexOf,F=k.lastIndexOf,p=Array.isArray,L=Object.keys,t=Function.prototype.bind,b=function(a){return new m(a)};"undefined"!==typeof exports?("undefined"!==typeof module&&module.exports&&(exports=module.exports=b),exports._=b):s._=b;b.VERSION="1.3.3";var j=b.each=b.forEach=function(a,
c,d){if(a!=null)if(y&&a.forEach===y)a.forEach(c,d);else if(a.length===+a.length)for(var e=0,f=a.length;e<f;e++){if(e in a&&c.call(d,a[e],e,a)===o)break}else for(e in a)if(b.has(a,e)&&c.call(d,a[e],e,a)===o)break};b.map=b.collect=function(a,c,b){var e=[];if(a==null)return e;if(z&&a.map===z)return a.map(c,b);j(a,function(a,g,h){e[e.length]=c.call(b,a,g,h)});if(a.length===+a.length)e.length=a.length;return e};b.reduce=b.foldl=b.inject=function(a,c,d,e){var f=arguments.length>2;a==null&&(a=[]);if(A&&
a.reduce===A){e&&(c=b.bind(c,e));return f?a.reduce(c,d):a.reduce(c)}j(a,function(a,b,i){if(f)d=c.call(e,d,a,b,i);else{d=a;f=true}});if(!f)throw new TypeError("Reduce of empty array with no initial value");return d};b.reduceRight=b.foldr=function(a,c,d,e){var f=arguments.length>2;a==null&&(a=[]);if(B&&a.reduceRight===B){e&&(c=b.bind(c,e));return f?a.reduceRight(c,d):a.reduceRight(c)}var g=b.toArray(a).reverse();e&&!f&&(c=b.bind(c,e));return f?b.reduce(g,c,d,e):b.reduce(g,c)};b.find=b.detect=function(a,
c,b){var e;G(a,function(a,g,h){if(c.call(b,a,g,h)){e=a;return true}});return e};b.filter=b.select=function(a,c,b){var e=[];if(a==null)return e;if(C&&a.filter===C)return a.filter(c,b);j(a,function(a,g,h){c.call(b,a,g,h)&&(e[e.length]=a)});return e};b.reject=function(a,c,b){var e=[];if(a==null)return e;j(a,function(a,g,h){c.call(b,a,g,h)||(e[e.length]=a)});return e};b.every=b.all=function(a,c,b){var e=true;if(a==null)return e;if(D&&a.every===D)return a.every(c,b);j(a,function(a,g,h){if(!(e=e&&c.call(b,
a,g,h)))return o});return!!e};var G=b.some=b.any=function(a,c,d){c||(c=b.identity);var e=false;if(a==null)return e;if(E&&a.some===E)return a.some(c,d);j(a,function(a,b,h){if(e||(e=c.call(d,a,b,h)))return o});return!!e};b.include=b.contains=function(a,c){var b=false;if(a==null)return b;if(q&&a.indexOf===q)return a.indexOf(c)!=-1;return b=G(a,function(a){return a===c})};b.invoke=function(a,c){var d=i.call(arguments,2);return b.map(a,function(a){return(b.isFunction(c)?c||a:a[c]).apply(a,d)})};b.pluck=
function(a,c){return b.map(a,function(a){return a[c]})};b.max=function(a,c,d){if(!c&&b.isArray(a)&&a[0]===+a[0])return Math.max.apply(Math,a);if(!c&&b.isEmpty(a))return-Infinity;var e={computed:-Infinity};j(a,function(a,b,h){b=c?c.call(d,a,b,h):a;b>=e.computed&&(e={value:a,computed:b})});return e.value};b.min=function(a,c,d){if(!c&&b.isArray(a)&&a[0]===+a[0])return Math.min.apply(Math,a);if(!c&&b.isEmpty(a))return Infinity;var e={computed:Infinity};j(a,function(a,b,h){b=c?c.call(d,a,b,h):a;b<e.computed&&
(e={value:a,computed:b})});return e.value};b.shuffle=function(a){var b=[],d;j(a,function(a,f){d=Math.floor(Math.random()*(f+1));b[f]=b[d];b[d]=a});return b};b.sortBy=function(a,c,d){var e=b.isFunction(c)?c:function(a){return a[c]};return b.pluck(b.map(a,function(a,b,c){return{value:a,criteria:e.call(d,a,b,c)}}).sort(function(a,b){var c=a.criteria,d=b.criteria;return c===void 0?1:d===void 0?-1:c<d?-1:c>d?1:0}),"value")};b.groupBy=function(a,c){var d={},e=b.isFunction(c)?c:function(a){return a[c]};
j(a,function(a,b){var c=e(a,b);(d[c]||(d[c]=[])).push(a)});return d};b.sortedIndex=function(a,c,d){d||(d=b.identity);for(var e=0,f=a.length;e<f;){var g=e+f>>1;d(a[g])<d(c)?e=g+1:f=g}return e};b.toArray=function(a){return!a?[]:b.isArray(a)||b.isArguments(a)?i.call(a):a.toArray&&b.isFunction(a.toArray)?a.toArray():b.values(a)};b.size=function(a){return b.isArray(a)?a.length:b.keys(a).length};b.first=b.head=b.take=function(a,b,d){return b!=null&&!d?i.call(a,0,b):a[0]};b.initial=function(a,b,d){return i.call(a,
0,a.length-(b==null||d?1:b))};b.last=function(a,b,d){return b!=null&&!d?i.call(a,Math.max(a.length-b,0)):a[a.length-1]};b.rest=b.tail=function(a,b,d){return i.call(a,b==null||d?1:b)};b.compact=function(a){return b.filter(a,function(a){return!!a})};b.flatten=function(a,c){return b.reduce(a,function(a,e){if(b.isArray(e))return a.concat(c?e:b.flatten(e));a[a.length]=e;return a},[])};b.without=function(a){return b.difference(a,i.call(arguments,1))};b.uniq=b.unique=function(a,c,d){var d=d?b.map(a,d):a,
e=[];a.length<3&&(c=true);b.reduce(d,function(d,g,h){if(c?b.last(d)!==g||!d.length:!b.include(d,g)){d.push(g);e.push(a[h])}return d},[]);return e};b.union=function(){return b.uniq(b.flatten(arguments,true))};b.intersection=b.intersect=function(a){var c=i.call(arguments,1);return b.filter(b.uniq(a),function(a){return b.every(c,function(c){return b.indexOf(c,a)>=0})})};b.difference=function(a){var c=b.flatten(i.call(arguments,1),true);return b.filter(a,function(a){return!b.include(c,a)})};b.zip=function(){for(var a=
i.call(arguments),c=b.max(b.pluck(a,"length")),d=Array(c),e=0;e<c;e++)d[e]=b.pluck(a,""+e);return d};b.indexOf=function(a,c,d){if(a==null)return-1;var e;if(d){d=b.sortedIndex(a,c);return a[d]===c?d:-1}if(q&&a.indexOf===q)return a.indexOf(c);d=0;for(e=a.length;d<e;d++)if(d in a&&a[d]===c)return d;return-1};b.lastIndexOf=function(a,b){if(a==null)return-1;if(F&&a.lastIndexOf===F)return a.lastIndexOf(b);for(var d=a.length;d--;)if(d in a&&a[d]===b)return d;return-1};b.range=function(a,b,d){if(arguments.length<=
1){b=a||0;a=0}for(var d=arguments[2]||1,e=Math.max(Math.ceil((b-a)/d),0),f=0,g=Array(e);f<e;){g[f++]=a;a=a+d}return g};var H=function(){};b.bind=function(a,c){var d,e;if(a.bind===t&&t)return t.apply(a,i.call(arguments,1));if(!b.isFunction(a))throw new TypeError;e=i.call(arguments,2);return d=function(){if(!(this instanceof d))return a.apply(c,e.concat(i.call(arguments)));H.prototype=a.prototype;var b=new H,g=a.apply(b,e.concat(i.call(arguments)));return Object(g)===g?g:b}};b.bindAll=function(a){var c=
i.call(arguments,1);c.length==0&&(c=b.functions(a));j(c,function(c){a[c]=b.bind(a[c],a)});return a};b.memoize=function(a,c){var d={};c||(c=b.identity);return function(){var e=c.apply(this,arguments);return b.has(d,e)?d[e]:d[e]=a.apply(this,arguments)}};b.delay=function(a,b){var d=i.call(arguments,2);return setTimeout(function(){return a.apply(null,d)},b)};b.defer=function(a){return b.delay.apply(b,[a,1].concat(i.call(arguments,1)))};b.throttle=function(a,c){var d,e,f,g,h,i,j=b.debounce(function(){h=
g=false},c);return function(){d=this;e=arguments;f||(f=setTimeout(function(){f=null;h&&a.apply(d,e);j()},c));g?h=true:i=a.apply(d,e);j();g=true;return i}};b.debounce=function(a,b,d){var e;return function(){var f=this,g=arguments;d&&!e&&a.apply(f,g);clearTimeout(e);e=setTimeout(function(){e=null;d||a.apply(f,g)},b)}};b.once=function(a){var b=false,d;return function(){if(b)return d;b=true;return d=a.apply(this,arguments)}};b.wrap=function(a,b){return function(){var d=[a].concat(i.call(arguments,0));
return b.apply(this,d)}};b.compose=function(){var a=arguments;return function(){for(var b=arguments,d=a.length-1;d>=0;d--)b=[a[d].apply(this,b)];return b[0]}};b.after=function(a,b){return a<=0?b():function(){if(--a<1)return b.apply(this,arguments)}};b.keys=L||function(a){if(a!==Object(a))throw new TypeError("Invalid object");var c=[],d;for(d in a)b.has(a,d)&&(c[c.length]=d);return c};b.values=function(a){return b.map(a,b.identity)};b.functions=b.methods=function(a){var c=[],d;for(d in a)b.isFunction(a[d])&&
c.push(d);return c.sort()};b.extend=function(a){j(i.call(arguments,1),function(b){for(var d in b)a[d]=b[d]});return a};b.pick=function(a){var c={};j(b.flatten(i.call(arguments,1)),function(b){b in a&&(c[b]=a[b])});return c};b.defaults=function(a){j(i.call(arguments,1),function(b){for(var d in b)a[d]==null&&(a[d]=b[d])});return a};b.clone=function(a){return!b.isObject(a)?a:b.isArray(a)?a.slice():b.extend({},a)};b.tap=function(a,b){b(a);return a};b.isEqual=function(a,b){return r(a,b,[])};b.isEmpty=
function(a){if(a==null)return true;if(b.isArray(a)||b.isString(a))return a.length===0;for(var c in a)if(b.has(a,c))return false;return true};b.isElement=function(a){return!!(a&&a.nodeType==1)};b.isArray=p||function(a){return l.call(a)=="[object Array]"};b.isObject=function(a){return a===Object(a)};b.isArguments=function(a){return l.call(a)=="[object Arguments]"};b.isArguments(arguments)||(b.isArguments=function(a){return!(!a||!b.has(a,"callee"))});b.isFunction=function(a){return l.call(a)=="[object Function]"};
b.isString=function(a){return l.call(a)=="[object String]"};b.isNumber=function(a){return l.call(a)=="[object Number]"};b.isFinite=function(a){return b.isNumber(a)&&isFinite(a)};b.isNaN=function(a){return a!==a};b.isBoolean=function(a){return a===true||a===false||l.call(a)=="[object Boolean]"};b.isDate=function(a){return l.call(a)=="[object Date]"};b.isRegExp=function(a){return l.call(a)=="[object RegExp]"};b.isNull=function(a){return a===null};b.isUndefined=function(a){return a===void 0};b.has=function(a,
b){return K.call(a,b)};b.noConflict=function(){s._=I;return this};b.identity=function(a){return a};b.times=function(a,b,d){for(var e=0;e<a;e++)b.call(d,e)};b.escape=function(a){return(""+a).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;").replace(/'/g,"&#x27;").replace(/\//g,"&#x2F;")};b.result=function(a,c){if(a==null)return null;var d=a[c];return b.isFunction(d)?d.call(a):d};b.mixin=function(a){j(b.functions(a),function(c){M(c,b[c]=a[c])})};var N=0;b.uniqueId=
function(a){var b=N++;return a?a+b:b};b.templateSettings={evaluate:/<%([\s\S]+?)%>/g,interpolate:/<%=([\s\S]+?)%>/g,escape:/<%-([\s\S]+?)%>/g};var u=/.^/,n={"\\":"\\","'":"'",r:"\r",n:"\n",t:"\t",u2028:"\u2028",u2029:"\u2029"},v;for(v in n)n[n[v]]=v;var O=/\\|'|\r|\n|\t|\u2028|\u2029/g,P=/\\(\\|'|r|n|t|u2028|u2029)/g,w=function(a){return a.replace(P,function(a,b){return n[b]})};b.template=function(a,c,d){d=b.defaults(d||{},b.templateSettings);a="__p+='"+a.replace(O,function(a){return"\\"+n[a]}).replace(d.escape||
u,function(a,b){return"'+\n_.escape("+w(b)+")+\n'"}).replace(d.interpolate||u,function(a,b){return"'+\n("+w(b)+")+\n'"}).replace(d.evaluate||u,function(a,b){return"';\n"+w(b)+"\n;__p+='"})+"';\n";d.variable||(a="with(obj||{}){\n"+a+"}\n");var a="var __p='';var print=function(){__p+=Array.prototype.join.call(arguments, '')};\n"+a+"return __p;\n",e=new Function(d.variable||"obj","_",a);if(c)return e(c,b);c=function(a){return e.call(this,a,b)};c.source="function("+(d.variable||"obj")+"){\n"+a+"}";return c};
b.chain=function(a){return b(a).chain()};var m=function(a){this._wrapped=a};b.prototype=m.prototype;var x=function(a,c){return c?b(a).chain():a},M=function(a,c){m.prototype[a]=function(){var a=i.call(arguments);J.call(a,this._wrapped);return x(c.apply(b,a),this._chain)}};b.mixin(b);j("pop,push,reverse,shift,sort,splice,unshift".split(","),function(a){var b=k[a];m.prototype[a]=function(){var d=this._wrapped;b.apply(d,arguments);var e=d.length;(a=="shift"||a=="splice")&&e===0&&delete d[0];return x(d,
this._chain)}});j(["concat","join","slice"],function(a){var b=k[a];m.prototype[a]=function(){return x(b.apply(this._wrapped,arguments),this._chain)}});m.prototype.chain=function(){this._chain=true;return this};m.prototype.value=function(){return this._wrapped}}).call(this);

/** vim: et:ts=4:sw=4:sts=4
 * @license RequireJS 1.0.2 Copyright (c) 2010-2011, The Dojo Foundation All Rights Reserved.
 * Available via the MIT or new BSD license.
 * see: http://github.com/jrburke/requirejs for details
 */
/*jslint strict: false, plusplus: false, sub: true */
/*global window: false, navigator: false, document: false, importScripts: false,
  jQuery: false, clearInterval: false, setInterval: false, self: false,
  setTimeout: false, opera: false */

var requirejs, require, define;
(function () {
    //Change this version number for each release.
    var version = "1.0.2",
        commentRegExp = /(\/\*([\s\S]*?)\*\/|([^:]|^)\/\/(.*)$)/mg,
        cjsRequireRegExp = /require\(\s*["']([^'"\s]+)["']\s*\)/g,
        currDirRegExp = /^\.\//,
        jsSuffixRegExp = /\.js$/,
        ostring = Object.prototype.toString,
        ap = Array.prototype,
        aps = ap.slice,
        apsp = ap.splice,
        isBrowser = !!(typeof window !== "undefined" && navigator && document),
        isWebWorker = !isBrowser && typeof importScripts !== "undefined",
        //PS3 indicates loaded and complete, but need to wait for complete
        //specifically. Sequence is "loading", "loaded", execution,
        // then "complete". The UA check is unfortunate, but not sure how
        //to feature test w/o causing perf issues.
        readyRegExp = isBrowser && navigator.platform === 'PLAYSTATION 3' ?
                      /^complete$/ : /^(complete|loaded)$/,
        defContextName = "_",
        //Oh the tragedy, detecting opera. See the usage of isOpera for reason.
        isOpera = typeof opera !== "undefined" && opera.toString() === "[object Opera]",
        empty = {},
        contexts = {},
        globalDefQueue = [],
        interactiveScript = null,
        checkLoadedDepth = 0,
        useInteractive = false,
        req, cfg = {}, currentlyAddingScript, s, head, baseElement, scripts, script,
        src, subPath, mainScript, dataMain, i, ctx, jQueryCheck, checkLoadedTimeoutId;

    function isFunction(it) {
        return ostring.call(it) === "[object Function]";
    }

    function isArray(it) {
        return ostring.call(it) === "[object Array]";
    }

    /**
     * Simple function to mix in properties from source into target,
     * but only if target does not already have a property of the same name.
     * This is not robust in IE for transferring methods that match
     * Object.prototype names, but the uses of mixin here seem unlikely to
     * trigger a problem related to that.
     */
    function mixin(target, source, force) {
        for (var prop in source) {
            if (!(prop in empty) && (!(prop in target) || force)) {
                target[prop] = source[prop];
            }
        }
        return req;
    }

    /**
     * Constructs an error with a pointer to an URL with more information.
     * @param {String} id the error ID that maps to an ID on a web page.
     * @param {String} message human readable error.
     * @param {Error} [err] the original error, if there is one.
     *
     * @returns {Error}
     */
    function makeError(id, msg, err) {
        var e = new Error(msg + '\nhttp://requirejs.org/docs/errors.html#' + id);
        if (err) {
            e.originalError = err;
        }
        return e;
    }

    /**
     * Used to set up package paths from a packagePaths or packages config object.
     * @param {Object} pkgs the object to store the new package config
     * @param {Array} currentPackages an array of packages to configure
     * @param {String} [dir] a prefix dir to use.
     */
    function configurePackageDir(pkgs, currentPackages, dir) {
        var i, location, pkgObj;

        for (i = 0; (pkgObj = currentPackages[i]); i++) {
            pkgObj = typeof pkgObj === "string" ? { name: pkgObj } : pkgObj;
            location = pkgObj.location;

            //Add dir to the path, but avoid paths that start with a slash
            //or have a colon (indicates a protocol)
            if (dir && (!location || (location.indexOf("/") !== 0 && location.indexOf(":") === -1))) {
                location = dir + "/" + (location || pkgObj.name);
            }

            //Create a brand new object on pkgs, since currentPackages can
            //be passed in again, and config.pkgs is the internal transformed
            //state for all package configs.
            pkgs[pkgObj.name] = {
                name: pkgObj.name,
                location: location || pkgObj.name,
                //Remove leading dot in main, so main paths are normalized,
                //and remove any trailing .js, since different package
                //envs have different conventions: some use a module name,
                //some use a file name.
                main: (pkgObj.main || "main")
                      .replace(currDirRegExp, '')
                      .replace(jsSuffixRegExp, '')
            };
        }
    }

    /**
     * jQuery 1.4.3-1.5.x use a readyWait/ready() pairing to hold DOM
     * ready callbacks, but jQuery 1.6 supports a holdReady() API instead.
     * At some point remove the readyWait/ready() support and just stick
     * with using holdReady.
     */
    function jQueryHoldReady($, shouldHold) {
        if ($.holdReady) {
            $.holdReady(shouldHold);
        } else if (shouldHold) {
            $.readyWait += 1;
        } else {
            $.ready(true);
        }
    }

    if (typeof define !== "undefined") {
        //If a define is already in play via another AMD loader,
        //do not overwrite.
        return;
    }

    if (typeof requirejs !== "undefined") {
        if (isFunction(requirejs)) {
            //Do not overwrite and existing requirejs instance.
            return;
        } else {
            cfg = requirejs;
            requirejs = undefined;
        }
    }

    //Allow for a require config object
    if (typeof require !== "undefined" && !isFunction(require)) {
        //assume it is a config object.
        cfg = require;
        require = undefined;
    }

    /**
     * Creates a new context for use in require and define calls.
     * Handle most of the heavy lifting. Do not want to use an object
     * with prototype here to avoid using "this" in require, in case it
     * needs to be used in more super secure envs that do not want this.
     * Also there should not be that many contexts in the page. Usually just
     * one for the default context, but could be extra for multiversion cases
     * or if a package needs a special context for a dependency that conflicts
     * with the standard context.
     */
    function newContext(contextName) {
        var context, resume,
            config = {
                waitSeconds: 7,
                baseUrl: "./",
                paths: {},
                pkgs: {},
                catchError: {}
            },
            defQueue = [],
            specified = {
                "require": true,
                "exports": true,
                "module": true
            },
            urlMap = {},
            defined = {},
            loaded = {},
            waiting = {},
            waitAry = [],
            urlFetched = {},
            managerCounter = 0,
            managerCallbacks = {},
            plugins = {},
            //Used to indicate which modules in a build scenario
            //need to be full executed.
            needFullExec = {},
            fullExec = {},
            resumeDepth = 0;

        /**
         * Trims the . and .. from an array of path segments.
         * It will keep a leading path segment if a .. will become
         * the first path segment, to help with module name lookups,
         * which act like paths, but can be remapped. But the end result,
         * all paths that use this function should look normalized.
         * NOTE: this method MODIFIES the input array.
         * @param {Array} ary the array of path segments.
         */
        function trimDots(ary) {
            var i, part;
            for (i = 0; (part = ary[i]); i++) {
                if (part === ".") {
                    ary.splice(i, 1);
                    i -= 1;
                } else if (part === "..") {
                    if (i === 1 && (ary[2] === '..' || ary[0] === '..')) {
                        //End of the line. Keep at least one non-dot
                        //path segment at the front so it can be mapped
                        //correctly to disk. Otherwise, there is likely
                        //no path mapping for a path starting with '..'.
                        //This can still fail, but catches the most reasonable
                        //uses of ..
                        break;
                    } else if (i > 0) {
                        ary.splice(i - 1, 2);
                        i -= 2;
                    }
                }
            }
        }

        /**
         * Given a relative module name, like ./something, normalize it to
         * a real name that can be mapped to a path.
         * @param {String} name the relative name
         * @param {String} baseName a real name that the name arg is relative
         * to.
         * @returns {String} normalized name
         */
        function normalize(name, baseName) {
            var pkgName, pkgConfig;

            //Adjust any relative paths.
            if (name && name.charAt(0) === ".") {
                //If have a base name, try to normalize against it,
                //otherwise, assume it is a top-level require that will
                //be relative to baseUrl in the end.
                if (baseName) {
                    if (config.pkgs[baseName]) {
                        //If the baseName is a package name, then just treat it as one
                        //name to concat the name with.
                        baseName = [baseName];
                    } else {
                        //Convert baseName to array, and lop off the last part,
                        //so that . matches that "directory" and not name of the baseName's
                        //module. For instance, baseName of "one/two/three", maps to
                        //"one/two/three.js", but we want the directory, "one/two" for
                        //this normalization.
                        baseName = baseName.split("/");
                        baseName = baseName.slice(0, baseName.length - 1);
                    }

                    name = baseName.concat(name.split("/"));
                    trimDots(name);

                    //Some use of packages may use a . path to reference the
                    //"main" module name, so normalize for that.
                    pkgConfig = config.pkgs[(pkgName = name[0])];
                    name = name.join("/");
                    if (pkgConfig && name === pkgName + '/' + pkgConfig.main) {
                        name = pkgName;
                    }
                } else if (name.indexOf("./") === 0) {
                    // No baseName, so this is ID is resolved relative
                    // to baseUrl, pull off the leading dot.
                    name = name.substring(2);
                }
            }
            return name;
        }

        /**
         * Creates a module mapping that includes plugin prefix, module
         * name, and path. If parentModuleMap is provided it will
         * also normalize the name via require.normalize()
         *
         * @param {String} name the module name
         * @param {String} [parentModuleMap] parent module map
         * for the module name, used to resolve relative names.
         *
         * @returns {Object}
         */
        function makeModuleMap(name, parentModuleMap) {
            var index = name ? name.indexOf("!") : -1,
                prefix = null,
                parentName = parentModuleMap ? parentModuleMap.name : null,
                originalName = name,
                normalizedName, url, pluginModule;

            if (index !== -1) {
                prefix = name.substring(0, index);
                name = name.substring(index + 1, name.length);
            }

            if (prefix) {
                prefix = normalize(prefix, parentName);
            }

            //Account for relative paths if there is a base name.
            if (name) {
                if (prefix) {
                    pluginModule = defined[prefix];
                    if (pluginModule && pluginModule.normalize) {
                        //Plugin is loaded, use its normalize method.
                        normalizedName = pluginModule.normalize(name, function (name) {
                            return normalize(name, parentName);
                        });
                    } else {
                        normalizedName = normalize(name, parentName);
                    }
                } else {
                    //A regular module.
                    normalizedName = normalize(name, parentName);

                    url = urlMap[normalizedName];
                    if (!url) {
                        //Calculate url for the module, if it has a name.
                        url = context.nameToUrl(normalizedName, null, parentModuleMap);

                        //Store the URL mapping for later.
                        urlMap[normalizedName] = url;
                    }
                }
            }

            return {
                prefix: prefix,
                name: normalizedName,
                parentMap: parentModuleMap,
                url: url,
                originalName: originalName,
                fullName: prefix ? prefix + "!" + (normalizedName || '') : normalizedName
            };
        }

        /**
         * Determine if priority loading is done. If so clear the priorityWait
         */
        function isPriorityDone() {
            var priorityDone = true,
                priorityWait = config.priorityWait,
                priorityName, i;
            if (priorityWait) {
                for (i = 0; (priorityName = priorityWait[i]); i++) {
                    if (!loaded[priorityName]) {
                        priorityDone = false;
                        break;
                    }
                }
                if (priorityDone) {
                    delete config.priorityWait;
                }
            }
            return priorityDone;
        }

        function makeContextModuleFunc(func, relModuleMap, enableBuildCallback) {
            return function () {
                //A version of a require function that passes a moduleName
                //value for items that may need to
                //look up paths relative to the moduleName
                var args = aps.call(arguments, 0), lastArg;
                if (enableBuildCallback &&
                    isFunction((lastArg = args[args.length - 1]))) {
                    lastArg.__requireJsBuild = true;
                }
                args.push(relModuleMap);
                return func.apply(null, args);
            };
        }

        /**
         * Helper function that creates a require function object to give to
         * modules that ask for it as a dependency. It needs to be specific
         * per module because of the implication of path mappings that may
         * need to be relative to the module name.
         */
        function makeRequire(relModuleMap, enableBuildCallback) {
            var modRequire = makeContextModuleFunc(context.require, relModuleMap, enableBuildCallback);

            mixin(modRequire, {
                nameToUrl: makeContextModuleFunc(context.nameToUrl, relModuleMap),
                toUrl: makeContextModuleFunc(context.toUrl, relModuleMap),
                defined: makeContextModuleFunc(context.requireDefined, relModuleMap),
                specified: makeContextModuleFunc(context.requireSpecified, relModuleMap),
                isBrowser: req.isBrowser
            });
            return modRequire;
        }

        /*
         * Queues a dependency for checking after the loader is out of a
         * "paused" state, for example while a script file is being loaded
         * in the browser, where it may have many modules defined in it.
         */
        function queueDependency(manager) {
            context.paused.push(manager);
        }

        function execManager(manager) {
            var i, ret, err, errFile, errModuleTree,
                cb = manager.callback,
                map = manager.map,
                fullName = map.fullName,
                args = manager.deps,
                listeners = manager.listeners,
                cjsModule;

            //Call the callback to define the module, if necessary.
            if (cb && isFunction(cb)) {
                if (config.catchError.define) {
                    try {
                        ret = req.execCb(fullName, manager.callback, args, defined[fullName]);
                    } catch (e) {
                        err = e;
                    }
                } else {
                    ret = req.execCb(fullName, manager.callback, args, defined[fullName]);
                }

                if (fullName) {
                    //If setting exports via "module" is in play,
                    //favor that over return value and exports. After that,
                    //favor a non-undefined return value over exports use.
                    cjsModule = manager.cjsModule;
                    if (cjsModule &&
                        cjsModule.exports !== undefined &&
                        //Make sure it is not already the exports value
                        cjsModule.exports !== defined[fullName]) {
                        ret = defined[fullName] = manager.cjsModule.exports;
                    } else if (ret === undefined && manager.usingExports) {
                        //exports already set the defined value.
                        ret = defined[fullName];
                    } else {
                        //Use the return value from the function.
                        defined[fullName] = ret;
                        //If this module needed full execution in a build
                        //environment, mark that now.
                        if (needFullExec[fullName]) {
                            fullExec[fullName] = true;
                        }
                    }
                }
            } else if (fullName) {
                //May just be an object definition for the module. Only
                //worry about defining if have a module name.
                ret = defined[fullName] = cb;

                //If this module needed full execution in a build
                //environment, mark that now.
                if (needFullExec[fullName]) {
                    fullExec[fullName] = true;
                }
            }

            //Clean up waiting. Do this before error calls, and before
            //calling back listeners, so that bookkeeping is correct
            //in the event of an error and error is reported in correct order,
            //since the listeners will likely have errors if the
            //onError function does not throw.
            if (waiting[manager.id]) {
                delete waiting[manager.id];
                manager.isDone = true;
                context.waitCount -= 1;
                if (context.waitCount === 0) {
                    //Clear the wait array used for cycles.
                    waitAry = [];
                }
            }

            //Do not need to track manager callback now that it is defined.
            delete managerCallbacks[fullName];

            //Allow instrumentation like the optimizer to know the order
            //of modules executed and their dependencies.
            if (req.onResourceLoad && !manager.placeholder) {
                req.onResourceLoad(context, map, manager.depArray);
            }

            if (err) {
                errFile = (fullName ? makeModuleMap(fullName).url : '') ||
                           err.fileName || err.sourceURL;
                errModuleTree = err.moduleTree;
                err = makeError('defineerror', 'Error evaluating ' +
                                'module "' + fullName + '" at location "' +
                                errFile + '":\n' +
                                err + '\nfileName:' + errFile +
                                '\nlineNumber: ' + (err.lineNumber || err.line), err);
                err.moduleName = fullName;
                err.moduleTree = errModuleTree;
                return req.onError(err);
            }

            //Let listeners know of this manager's value.
            for (i = 0; (cb = listeners[i]); i++) {
                cb(ret);
            }

            return undefined;
        }

        /**
         * Helper that creates a callack function that is called when a dependency
         * is ready, and sets the i-th dependency for the manager as the
         * value passed to the callback generated by this function.
         */
        function makeArgCallback(manager, i) {
            return function (value) {
                //Only do the work if it has not been done
                //already for a dependency. Cycle breaking
                //logic in forceExec could mean this function
                //is called more than once for a given dependency.
                if (!manager.depDone[i]) {
                    manager.depDone[i] = true;
                    manager.deps[i] = value;
                    manager.depCount -= 1;
                    if (!manager.depCount) {
                        //All done, execute!
                        execManager(manager);
                    }
                }
            };
        }

        function callPlugin(pluginName, depManager) {
            var map = depManager.map,
                fullName = map.fullName,
                name = map.name,
                plugin = plugins[pluginName] ||
                        (plugins[pluginName] = defined[pluginName]),
                load;

            //No need to continue if the manager is already
            //in the process of loading.
            if (depManager.loading) {
                return;
            }
            depManager.loading = true;

            load = function (ret) {
                depManager.callback = function () {
                    return ret;
                };
                execManager(depManager);

                loaded[depManager.id] = true;

                //The loading of this plugin
                //might have placed other things
                //in the paused queue. In particular,
                //a loader plugin that depends on
                //a different plugin loaded resource.
                resume();
            };

            //Allow plugins to load other code without having to know the
            //context or how to "complete" the load.
            load.fromText = function (moduleName, text) {
                /*jslint evil: true */
                var hasInteractive = useInteractive;

                //Indicate a the module is in process of loading.
                loaded[moduleName] = false;
                context.scriptCount += 1;

                //Indicate this is not a "real" module, so do not track it
                //for builds, it does not map to a real file.
                context.fake[moduleName] = true;

                //Turn off interactive script matching for IE for any define
                //calls in the text, then turn it back on at the end.
                if (hasInteractive) {
                    useInteractive = false;
                }

                req.exec(text);

                if (hasInteractive) {
                    useInteractive = true;
                }

                //Support anonymous modules.
                context.completeLoad(moduleName);
            };

            //No need to continue if the plugin value has already been
            //defined by a build.
            if (fullName in defined) {
                load(defined[fullName]);
            } else {
                //Use parentName here since the plugin's name is not reliable,
                //could be some weird string with no path that actually wants to
                //reference the parentName's path.
                plugin.load(name, makeRequire(map.parentMap, true), load, config);
            }
        }

        /**
         * Adds the manager to the waiting queue. Only fully
         * resolved items should be in the waiting queue.
         */
        function addWait(manager) {
            if (!waiting[manager.id]) {
                waiting[manager.id] = manager;
                waitAry.push(manager);
                context.waitCount += 1;
            }
        }

        /**
         * Function added to every manager object. Created out here
         * to avoid new function creation for each manager instance.
         */
        function managerAdd(cb) {
            this.listeners.push(cb);
        }

        function getManager(map, shouldQueue) {
            var fullName = map.fullName,
                prefix = map.prefix,
                plugin = prefix ? plugins[prefix] ||
                                (plugins[prefix] = defined[prefix]) : null,
                manager, created, pluginManager;

            if (fullName) {
                manager = managerCallbacks[fullName];
            }

            if (!manager) {
                created = true;
                manager = {
                    //ID is just the full name, but if it is a plugin resource
                    //for a plugin that has not been loaded,
                    //then add an ID counter to it.
                    id: (prefix && !plugin ?
                        (managerCounter++) + '__p@:' : '') +
                        (fullName || '__r@' + (managerCounter++)),
                    map: map,
                    depCount: 0,
                    depDone: [],
                    depCallbacks: [],
                    deps: [],
                    listeners: [],
                    add: managerAdd
                };

                specified[manager.id] = true;

                //Only track the manager/reuse it if this is a non-plugin
                //resource. Also only track plugin resources once
                //the plugin has been loaded, and so the fullName is the
                //true normalized value.
                if (fullName && (!prefix || plugins[prefix])) {
                    managerCallbacks[fullName] = manager;
                }
            }

            //If there is a plugin needed, but it is not loaded,
            //first load the plugin, then continue on.
            if (prefix && !plugin) {
                pluginManager = getManager(makeModuleMap(prefix), true);
                pluginManager.add(function (plugin) {
                    //Create a new manager for the normalized
                    //resource ID and have it call this manager when
                    //done.
                    var newMap = makeModuleMap(map.originalName, map.parentMap),
                        normalizedManager = getManager(newMap, true);

                    //Indicate this manager is a placeholder for the real,
                    //normalized thing. Important for when trying to map
                    //modules and dependencies, for instance, in a build.
                    manager.placeholder = true;

                    normalizedManager.add(function (resource) {
                        manager.callback = function () {
                            return resource;
                        };
                        execManager(manager);
                    });
                });
            } else if (created && shouldQueue) {
                //Indicate the resource is not loaded yet if it is to be
                //queued.
                loaded[manager.id] = false;
                queueDependency(manager);
                addWait(manager);
            }

            return manager;
        }

        function main(inName, depArray, callback, relModuleMap) {
            var moduleMap = makeModuleMap(inName, relModuleMap),
                name = moduleMap.name,
                fullName = moduleMap.fullName,
                manager = getManager(moduleMap),
                id = manager.id,
                deps = manager.deps,
                i, depArg, depName, depPrefix, cjsMod;

            if (fullName) {
                //If module already defined for context, or already loaded,
                //then leave. Also leave if jQuery is registering but it does
                //not match the desired version number in the config.
                if (fullName in defined || loaded[id] === true ||
                    (fullName === "jquery" && config.jQuery &&
                     config.jQuery !== callback().fn.jquery)) {
                    return;
                }

                //Set specified/loaded here for modules that are also loaded
                //as part of a layer, where onScriptLoad is not fired
                //for those cases. Do this after the inline define and
                //dependency tracing is done.
                specified[id] = true;
                loaded[id] = true;

                //If module is jQuery set up delaying its dom ready listeners.
                if (fullName === "jquery" && callback) {
                    jQueryCheck(callback());
                }
            }

            //Attach real depArray and callback to the manager. Do this
            //only if the module has not been defined already, so do this after
            //the fullName checks above. IE can call main() more than once
            //for a module.
            manager.depArray = depArray;
            manager.callback = callback;

            //Add the dependencies to the deps field, and register for callbacks
            //on the dependencies.
            for (i = 0; i < depArray.length; i++) {
                depArg = depArray[i];
                //There could be cases like in IE, where a trailing comma will
                //introduce a null dependency, so only treat a real dependency
                //value as a dependency.
                if (depArg) {
                    //Split the dependency name into plugin and name parts
                    depArg = makeModuleMap(depArg, (name ? moduleMap : relModuleMap));
                    depName = depArg.fullName;
                    depPrefix = depArg.prefix;

                    //Fix the name in depArray to be just the name, since
                    //that is how it will be called back later.
                    depArray[i] = depName;

                    //Fast path CommonJS standard dependencies.
                    if (depName === "require") {
                        deps[i] = makeRequire(moduleMap);
                    } else if (depName === "exports") {
                        //CommonJS module spec 1.1
                        deps[i] = defined[fullName] = {};
                        manager.usingExports = true;
                    } else if (depName === "module") {
                        //CommonJS module spec 1.1
                        manager.cjsModule = cjsMod = deps[i] = {
                            id: name,
                            uri: name ? context.nameToUrl(name, null, relModuleMap) : undefined,
                            exports: defined[fullName]
                        };
                    } else if (depName in defined && !(depName in waiting) &&
                               (!(fullName in needFullExec) ||
                                (fullName in needFullExec && fullExec[depName]))) {
                        //Module already defined, and not in a build situation
                        //where the module is a something that needs full
                        //execution and this dependency has not been fully
                        //executed. See r.js's requirePatch.js for more info
                        //on fullExec.
                        deps[i] = defined[depName];
                    } else {
                        //Mark this dependency as needing full exec if
                        //the current module needs full exec.
                        if (fullName in needFullExec) {
                            needFullExec[depName] = true;
                            //Reset state so fully executed code will get
                            //picked up correctly.
                            delete defined[depName];
                            urlFetched[depArg.url] = false;
                        }

                        //Either a resource that is not loaded yet, or a plugin
                        //resource for either a plugin that has not
                        //loaded yet.
                        manager.depCount += 1;
                        manager.depCallbacks[i] = makeArgCallback(manager, i);
                        getManager(depArg, true).add(manager.depCallbacks[i]);
                    }
                }
            }

            //Do not bother tracking the manager if it is all done.
            if (!manager.depCount) {
                //All done, execute!
                execManager(manager);
            } else {
                addWait(manager);
            }
        }

        /**
         * Convenience method to call main for a define call that was put on
         * hold in the defQueue.
         */
        function callDefMain(args) {
            main.apply(null, args);
        }

        /**
         * jQuery 1.4.3+ supports ways to hold off calling
         * calling jQuery ready callbacks until all scripts are loaded. Be sure
         * to track it if the capability exists.. Also, since jQuery 1.4.3 does
         * not register as a module, need to do some global inference checking.
         * Even if it does register as a module, not guaranteed to be the precise
         * name of the global. If a jQuery is tracked for this context, then go
         * ahead and register it as a module too, if not already in process.
         */
        jQueryCheck = function (jqCandidate) {
            if (!context.jQuery) {
                var $ = jqCandidate || (typeof jQuery !== "undefined" ? jQuery : null);

                if ($) {
                    //If a specific version of jQuery is wanted, make sure to only
                    //use this jQuery if it matches.
                    if (config.jQuery && $.fn.jquery !== config.jQuery) {
                        return;
                    }

                    if ("holdReady" in $ || "readyWait" in $) {
                        context.jQuery = $;

                        //Manually create a "jquery" module entry if not one already
                        //or in process. Note this could trigger an attempt at
                        //a second jQuery registration, but does no harm since
                        //the first one wins, and it is the same value anyway.
                        callDefMain(["jquery", [], function () {
                            return jQuery;
                        }]);

                        //Ask jQuery to hold DOM ready callbacks.
                        if (context.scriptCount) {
                            jQueryHoldReady($, true);
                            context.jQueryIncremented = true;
                        }
                    }
                }
            }
        };

        function forceExec(manager, traced) {
            if (manager.isDone) {
                return undefined;
            }

            var fullName = manager.map.fullName,
                depArray = manager.depArray,
                i, depName, depManager, prefix, prefixManager, value;

            if (fullName) {
                if (traced[fullName]) {
                    return defined[fullName];
                }

                traced[fullName] = true;
            }

            //Trace through the dependencies.
            if (depArray) {
                for (i = 0; i < depArray.length; i++) {
                    //Some array members may be null, like if a trailing comma
                    //IE, so do the explicit [i] access and check if it has a value.
                    depName = depArray[i];
                    if (depName) {
                        //First, make sure if it is a plugin resource that the
                        //plugin is not blocked.
                        prefix = makeModuleMap(depName).prefix;
                        if (prefix && (prefixManager = waiting[prefix])) {
                            forceExec(prefixManager, traced);
                        }
                        depManager = waiting[depName];
                        if (depManager && !depManager.isDone && loaded[depName]) {
                            value = forceExec(depManager, traced);
                            manager.depCallbacks[i](value);
                        }
                    }
                }
            }

            return fullName ? defined[fullName] : undefined;
        }

        /**
         * Checks if all modules for a context are loaded, and if so, evaluates the
         * new ones in right dependency order.
         *
         * @private
         */
        function checkLoaded() {
            var waitInterval = config.waitSeconds * 1000,
                //It is possible to disable the wait interval by using waitSeconds of 0.
                expired = waitInterval && (context.startTime + waitInterval) < new Date().getTime(),
                noLoads = "", hasLoadedProp = false, stillLoading = false, prop,
                err, manager;

            //If there are items still in the paused queue processing wait.
            //This is particularly important in the sync case where each paused
            //item is processed right away but there may be more waiting.
            if (context.pausedCount > 0) {
                return undefined;
            }

            //Determine if priority loading is done. If so clear the priority. If
            //not, then do not check
            if (config.priorityWait) {
                if (isPriorityDone()) {
                    //Call resume, since it could have
                    //some waiting dependencies to trace.
                    resume();
                } else {
                    return undefined;
                }
            }

            //See if anything is still in flight.
            for (prop in loaded) {
                if (!(prop in empty)) {
                    hasLoadedProp = true;
                    if (!loaded[prop]) {
                        if (expired) {
                            noLoads += prop + " ";
                        } else {
                            stillLoading = true;
                            break;
                        }
                    }
                }
            }

            //Check for exit conditions.
            if (!hasLoadedProp && !context.waitCount) {
                //If the loaded object had no items, then the rest of
                //the work below does not need to be done.
                return undefined;
            }
            if (expired && noLoads) {
                //If wait time expired, throw error of unloaded modules.
                err = makeError("timeout", "Load timeout for modules: " + noLoads);
                err.requireType = "timeout";
                err.requireModules = noLoads;
                return req.onError(err);
            }
            if (stillLoading || context.scriptCount) {
                //Something is still waiting to load. Wait for it, but only
                //if a timeout is not already in effect.
                if ((isBrowser || isWebWorker) && !checkLoadedTimeoutId) {
                    checkLoadedTimeoutId = setTimeout(function () {
                        checkLoadedTimeoutId = 0;
                        checkLoaded();
                    }, 50);
                }
                return undefined;
            }

            //If still have items in the waiting cue, but all modules have
            //been loaded, then it means there are some circular dependencies
            //that need to be broken.
            //However, as a waiting thing is fired, then it can add items to
            //the waiting cue, and those items should not be fired yet, so
            //make sure to redo the checkLoaded call after breaking a single
            //cycle, if nothing else loaded then this logic will pick it up
            //again.
            if (context.waitCount) {
                //Cycle through the waitAry, and call items in sequence.
                for (i = 0; (manager = waitAry[i]); i++) {
                    forceExec(manager, {});
                }

                //If anything got placed in the paused queue, run it down.
                if (context.paused.length) {
                    resume();
                }

                //Only allow this recursion to a certain depth. Only
                //triggered by errors in calling a module in which its
                //modules waiting on it cannot finish loading, or some circular
                //dependencies that then may add more dependencies.
                //The value of 5 is a bit arbitrary. Hopefully just one extra
                //pass, or two for the case of circular dependencies generating
                //more work that gets resolved in the sync node case.
                if (checkLoadedDepth < 5) {
                    checkLoadedDepth += 1;
                    checkLoaded();
                }
            }

            checkLoadedDepth = 0;

            //Check for DOM ready, and nothing is waiting across contexts.
            req.checkReadyState();

            return undefined;
        }

        /**
         * Resumes tracing of dependencies and then checks if everything is loaded.
         */
        resume = function () {
            var manager, map, url, i, p, args, fullName;

            resumeDepth += 1;

            if (context.scriptCount <= 0) {
                //Synchronous envs will push the number below zero with the
                //decrement above, be sure to set it back to zero for good measure.
                //require() calls that also do not end up loading scripts could
                //push the number negative too.
                context.scriptCount = 0;
            }

            //Make sure any remaining defQueue items get properly processed.
            while (defQueue.length) {
                args = defQueue.shift();
                if (args[0] === null) {
                    return req.onError(makeError('mismatch', 'Mismatched anonymous define() module: ' + args[args.length - 1]));
                } else {
                    callDefMain(args);
                }
            }

            //Skip the resume of paused dependencies
            //if current context is in priority wait.
            if (!config.priorityWait || isPriorityDone()) {
                while (context.paused.length) {
                    p = context.paused;
                    context.pausedCount += p.length;
                    //Reset paused list
                    context.paused = [];

                    for (i = 0; (manager = p[i]); i++) {
                        map = manager.map;
                        url = map.url;
                        fullName = map.fullName;

                        //If the manager is for a plugin managed resource,
                        //ask the plugin to load it now.
                        if (map.prefix) {
                            callPlugin(map.prefix, manager);
                        } else {
                            //Regular dependency.
                            if (!urlFetched[url] && !loaded[fullName]) {
                                req.load(context, fullName, url);

                                //Mark the URL as fetched, but only if it is
                                //not an empty: URL, used by the optimizer.
                                //In that case we need to be sure to call
                                //load() for each module that is mapped to
                                //empty: so that dependencies are satisfied
                                //correctly.
                                if (url.indexOf('empty:') !== 0) {
                                    urlFetched[url] = true;
                                }
                            }
                        }
                    }

                    //Move the start time for timeout forward.
                    context.startTime = (new Date()).getTime();
                    context.pausedCount -= p.length;
                }
            }

            //Only check if loaded when resume depth is 1. It is likely that
            //it is only greater than 1 in sync environments where a factory
            //function also then calls the callback-style require. In those
            //cases, the checkLoaded should not occur until the resume
            //depth is back at the top level.
            if (resumeDepth === 1) {
                checkLoaded();
            }

            resumeDepth -= 1;

            return undefined;
        };

        //Define the context object. Many of these fields are on here
        //just to make debugging easier.
        context = {
            contextName: contextName,
            config: config,
            defQueue: defQueue,
            waiting: waiting,
            waitCount: 0,
            specified: specified,
            loaded: loaded,
            urlMap: urlMap,
            urlFetched: urlFetched,
            scriptCount: 0,
            defined: defined,
            paused: [],
            pausedCount: 0,
            plugins: plugins,
            needFullExec: needFullExec,
            fake: {},
            fullExec: fullExec,
            managerCallbacks: managerCallbacks,
            makeModuleMap: makeModuleMap,
            normalize: normalize,
            /**
             * Set a configuration for the context.
             * @param {Object} cfg config object to integrate.
             */
            configure: function (cfg) {
                var paths, prop, packages, pkgs, packagePaths, requireWait;

                //Make sure the baseUrl ends in a slash.
                if (cfg.baseUrl) {
                    if (cfg.baseUrl.charAt(cfg.baseUrl.length - 1) !== "/") {
                        cfg.baseUrl += "/";
                    }
                }

                //Save off the paths and packages since they require special processing,
                //they are additive.
                paths = config.paths;
                packages = config.packages;
                pkgs = config.pkgs;

                //Mix in the config values, favoring the new values over
                //existing ones in context.config.
                mixin(config, cfg, true);

                //Adjust paths if necessary.
                if (cfg.paths) {
                    for (prop in cfg.paths) {
                        if (!(prop in empty)) {
                            paths[prop] = cfg.paths[prop];
                        }
                    }
                    config.paths = paths;
                }

                packagePaths = cfg.packagePaths;
                if (packagePaths || cfg.packages) {
                    //Convert packagePaths into a packages config.
                    if (packagePaths) {
                        for (prop in packagePaths) {
                            if (!(prop in empty)) {
                                configurePackageDir(pkgs, packagePaths[prop], prop);
                            }
                        }
                    }

                    //Adjust packages if necessary.
                    if (cfg.packages) {
                        configurePackageDir(pkgs, cfg.packages);
                    }

                    //Done with modifications, assing packages back to context config
                    config.pkgs = pkgs;
                }

                //If priority loading is in effect, trigger the loads now
                if (cfg.priority) {
                    //Hold on to requireWait value, and reset it after done
                    requireWait = context.requireWait;

                    //Allow tracing some require calls to allow the fetching
                    //of the priority config.
                    context.requireWait = false;
                    //But first, call resume to register any defined modules that may
                    //be in a data-main built file before the priority config
                    //call. Also grab any waiting define calls for this context.
                    context.takeGlobalQueue();
                    resume();

                    context.require(cfg.priority);

                    //Trigger a resume right away, for the case when
                    //the script with the priority load is done as part
                    //of a data-main call. In that case the normal resume
                    //call will not happen because the scriptCount will be
                    //at 1, since the script for data-main is being processed.
                    resume();

                    //Restore previous state.
                    context.requireWait = requireWait;
                    config.priorityWait = cfg.priority;
                }

                //If a deps array or a config callback is specified, then call
                //require with those args. This is useful when require is defined as a
                //config object before require.js is loaded.
                if (cfg.deps || cfg.callback) {
                    context.require(cfg.deps || [], cfg.callback);
                }
            },

            requireDefined: function (moduleName, relModuleMap) {
                return makeModuleMap(moduleName, relModuleMap).fullName in defined;
            },

            requireSpecified: function (moduleName, relModuleMap) {
                return makeModuleMap(moduleName, relModuleMap).fullName in specified;
            },

            require: function (deps, callback, relModuleMap) {
                var moduleName, fullName, moduleMap;
                if (typeof deps === "string") {
                    if (isFunction(callback)) {
                        //Invalid call
                        return req.onError(makeError("requireargs", "Invalid require call"));
                    }

                    //Synchronous access to one module. If require.get is
                    //available (as in the Node adapter), prefer that.
                    //In this case deps is the moduleName and callback is
                    //the relModuleMap
                    if (req.get) {
                        return req.get(context, deps, callback);
                    }

                    //Just return the module wanted. In this scenario, the
                    //second arg (if passed) is just the relModuleMap.
                    moduleName = deps;
                    relModuleMap = callback;

                    //Normalize module name, if it contains . or ..
                    moduleMap = makeModuleMap(moduleName, relModuleMap);
                    fullName = moduleMap.fullName;

                    if (!(fullName in defined)) {
                        return req.onError(makeError("notloaded", "Module name '" +
                                    moduleMap.fullName +
                                    "' has not been loaded yet for context: " +
                                    contextName));
                    }
                    return defined[fullName];
                }

                //Call main but only if there are dependencies or
                //a callback to call.
                if (deps && deps.length || callback) {
                    main(null, deps, callback, relModuleMap);
                }

                //If the require call does not trigger anything new to load,
                //then resume the dependency processing.
                if (!context.requireWait) {
                    while (!context.scriptCount && context.paused.length) {
                        //For built layers, there can be some defined
                        //modules waiting for intake into the context,
                        //in particular module plugins. Take them.
                        context.takeGlobalQueue();
                        resume();
                    }
                }
                return context.require;
            },

            /**
             * Internal method to transfer globalQueue items to this context's
             * defQueue.
             */
            takeGlobalQueue: function () {
                //Push all the globalDefQueue items into the context's defQueue
                if (globalDefQueue.length) {
                    //Array splice in the values since the context code has a
                    //local var ref to defQueue, so cannot just reassign the one
                    //on context.
                    apsp.apply(context.defQueue,
                               [context.defQueue.length - 1, 0].concat(globalDefQueue));
                    globalDefQueue = [];
                }
            },

            /**
             * Internal method used by environment adapters to complete a load event.
             * A load event could be a script load or just a load pass from a synchronous
             * load call.
             * @param {String} moduleName the name of the module to potentially complete.
             */
            completeLoad: function (moduleName) {
                var args;

                context.takeGlobalQueue();

                while (defQueue.length) {
                    args = defQueue.shift();

                    if (args[0] === null) {
                        args[0] = moduleName;
                        break;
                    } else if (args[0] === moduleName) {
                        //Found matching define call for this script!
                        break;
                    } else {
                        //Some other named define call, most likely the result
                        //of a build layer that included many define calls.
                        callDefMain(args);
                        args = null;
                    }
                }
                if (args) {
                    callDefMain(args);
                } else {
                    //A script that does not call define(), so just simulate
                    //the call for it. Special exception for jQuery dynamic load.
                    callDefMain([moduleName, [],
                                moduleName === "jquery" && typeof jQuery !== "undefined" ?
                                function () {
                                    return jQuery;
                                } : null]);
                }

                //If a global jQuery is defined, check for it. Need to do it here
                //instead of main() since stock jQuery does not register as
                //a module via define.
                jQueryCheck();

                //Doing this scriptCount decrement branching because sync envs
                //need to decrement after resume, otherwise it looks like
                //loading is complete after the first dependency is fetched.
                //For browsers, it works fine to decrement after, but it means
                //the checkLoaded setTimeout 50 ms cost is taken. To avoid
                //that cost, decrement beforehand.
                if (req.isAsync) {
                    context.scriptCount -= 1;
                }
                resume();
                if (!req.isAsync) {
                    context.scriptCount -= 1;
                }
            },

            /**
             * Converts a module name + .extension into an URL path.
             * *Requires* the use of a module name. It does not support using
             * plain URLs like nameToUrl.
             */
            toUrl: function (moduleNamePlusExt, relModuleMap) {
                var index = moduleNamePlusExt.lastIndexOf("."),
                    ext = null;

                if (index !== -1) {
                    ext = moduleNamePlusExt.substring(index, moduleNamePlusExt.length);
                    moduleNamePlusExt = moduleNamePlusExt.substring(0, index);
                }

                return context.nameToUrl(moduleNamePlusExt, ext, relModuleMap);
            },

            /**
             * Converts a module name to a file path. Supports cases where
             * moduleName may actually be just an URL.
             */
            nameToUrl: function (moduleName, ext, relModuleMap) {
                var paths, pkgs, pkg, pkgPath, syms, i, parentModule, url,
                    config = context.config;

                //Normalize module name if have a base relative module name to work from.
                moduleName = normalize(moduleName, relModuleMap && relModuleMap.fullName);

                //If a colon is in the URL, it indicates a protocol is used and it is just
                //an URL to a file, or if it starts with a slash or ends with .js, it is just a plain file.
                //The slash is important for protocol-less URLs as well as full paths.
                if (req.jsExtRegExp.test(moduleName)) {
                    //Just a plain path, not module name lookup, so just return it.
                    //Add extension if it is included. This is a bit wonky, only non-.js things pass
                    //an extension, this method probably needs to be reworked.
                    url = moduleName + (ext ? ext : "");
                } else {
                    //A module that needs to be converted to a path.
                    paths = config.paths;
                    pkgs = config.pkgs;

                    syms = moduleName.split("/");
                    //For each module name segment, see if there is a path
                    //registered for it. Start with most specific name
                    //and work up from it.
                    for (i = syms.length; i > 0; i--) {
                        parentModule = syms.slice(0, i).join("/");
                        if (paths[parentModule]) {
                            syms.splice(0, i, paths[parentModule]);
                            break;
                        } else if ((pkg = pkgs[parentModule])) {
                            //If module name is just the package name, then looking
                            //for the main module.
                            if (moduleName === pkg.name) {
                                pkgPath = pkg.location + '/' + pkg.main;
                            } else {
                                pkgPath = pkg.location;
                            }
                            syms.splice(0, i, pkgPath);
                            break;
                        }
                    }

                    //Join the path parts together, then figure out if baseUrl is needed.
                    url = syms.join("/") + (ext || ".js");
                    url = (url.charAt(0) === '/' || url.match(/^\w+:/) ? "" : config.baseUrl) + url;
                }

                return config.urlArgs ? url +
                                        ((url.indexOf('?') === -1 ? '?' : '&') +
                                         config.urlArgs) : url;
            }
        };

        //Make these visible on the context so can be called at the very
        //end of the file to bootstrap
        context.jQueryCheck = jQueryCheck;
        context.resume = resume;

        return context;
    }

    /**
     * Main entry point.
     *
     * If the only argument to require is a string, then the module that
     * is represented by that string is fetched for the appropriate context.
     *
     * If the first argument is an array, then it will be treated as an array
     * of dependency string names to fetch. An optional function callback can
     * be specified to execute when all of those dependencies are available.
     *
     * Make a local req variable to help Caja compliance (it assumes things
     * on a require that are not standardized), and to give a short
     * name for minification/local scope use.
     */
    req = requirejs = function (deps, callback) {

        //Find the right context, use default
        var contextName = defContextName,
            context, config;

        // Determine if have config object in the call.
        if (!isArray(deps) && typeof deps !== "string") {
            // deps is a config object
            config = deps;
            if (isArray(callback)) {
                // Adjust args if there are dependencies
                deps = callback;
                callback = arguments[2];
            } else {
                deps = [];
            }
        }

        if (config && config.context) {
            contextName = config.context;
        }

        context = contexts[contextName] ||
                  (contexts[contextName] = newContext(contextName));

        if (config) {
            context.configure(config);
        }

        return context.require(deps, callback);
    };

    /**
     * Support require.config() to make it easier to cooperate with other
     * AMD loaders on globally agreed names.
     */
    req.config = function (config) {
        return req(config);
    };

    /**
     * Export require as a global, but only if it does not already exist.
     */
    if (!require) {
        require = req;
    }

    /**
     * Global require.toUrl(), to match global require, mostly useful
     * for debugging/work in the global space.
     */
    req.toUrl = function (moduleNamePlusExt) {
        return contexts[defContextName].toUrl(moduleNamePlusExt);
    };

    req.version = version;

    //Used to filter out dependencies that are already paths.
    req.jsExtRegExp = /^\/|:|\?|\.js$/;
    s = req.s = {
        contexts: contexts,
        //Stores a list of URLs that should not get async script tag treatment.
        skipAsync: {}
    };

    req.isAsync = req.isBrowser = isBrowser;
    if (isBrowser) {
        head = s.head = document.getElementsByTagName("head")[0];
        //If BASE tag is in play, using appendChild is a problem for IE6.
        //When that browser dies, this can be removed. Details in this jQuery bug:
        //http://dev.jquery.com/ticket/2709
        baseElement = document.getElementsByTagName("base")[0];
        if (baseElement) {
            head = s.head = baseElement.parentNode;
        }
    }

    /**
     * Any errors that require explicitly generates will be passed to this
     * function. Intercept/override it if you want custom error handling.
     * @param {Error} err the error object.
     */
    req.onError = function (err) {
        throw err;
    };

    /**
     * Does the request to load a module for the browser case.
     * Make this a separate function to allow other environments
     * to override it.
     *
     * @param {Object} context the require context to find state.
     * @param {String} moduleName the name of the module.
     * @param {Object} url the URL to the module.
     */
    req.load = function (context, moduleName, url) {
        req.resourcesReady(false);

        context.scriptCount += 1;
        req.attach(url, context, moduleName);

        //If tracking a jQuery, then make sure its ready callbacks
        //are put on hold to prevent its ready callbacks from
        //triggering too soon.
        if (context.jQuery && !context.jQueryIncremented) {
            jQueryHoldReady(context.jQuery, true);
            context.jQueryIncremented = true;
        }
    };

    function getInteractiveScript() {
        var scripts, i, script;
        if (interactiveScript && interactiveScript.readyState === 'interactive') {
            return interactiveScript;
        }

        scripts = document.getElementsByTagName('script');
        for (i = scripts.length - 1; i > -1 && (script = scripts[i]); i--) {
            if (script.readyState === 'interactive') {
                return (interactiveScript = script);
            }
        }

        return null;
    }

    /**
     * The function that handles definitions of modules. Differs from
     * require() in that a string for the module should be the first argument,
     * and the function to execute after dependencies are loaded should
     * return a value to define the module corresponding to the first argument's
     * name.
     */
    define = function (name, deps, callback) {
        var node, context;

        //Allow for anonymous functions
        if (typeof name !== 'string') {
            //Adjust args appropriately
            callback = deps;
            deps = name;
            name = null;
        }

        //This module may not have dependencies
        if (!isArray(deps)) {
            callback = deps;
            deps = [];
        }

        //If no name, and callback is a function, then figure out if it a
        //CommonJS thing with dependencies.
        if (!deps.length && isFunction(callback)) {
            //Remove comments from the callback string,
            //look for require calls, and pull them into the dependencies,
            //but only if there are function args.
            if (callback.length) {
                callback
                    .toString()
                    .replace(commentRegExp, "")
                    .replace(cjsRequireRegExp, function (match, dep) {
                        deps.push(dep);
                    });

                //May be a CommonJS thing even without require calls, but still
                //could use exports, and module. Avoid doing exports and module
                //work though if it just needs require.
                //REQUIRES the function to expect the CommonJS variables in the
                //order listed below.
                deps = (callback.length === 1 ? ["require"] : ["require", "exports", "module"]).concat(deps);
            }
        }

        //If in IE 6-8 and hit an anonymous define() call, do the interactive
        //work.
        if (useInteractive) {
            node = currentlyAddingScript || getInteractiveScript();
            if (node) {
                if (!name) {
                    name = node.getAttribute("data-requiremodule");
                }
                context = contexts[node.getAttribute("data-requirecontext")];
            }
        }

        //Always save off evaluating the def call until the script onload handler.
        //This allows multiple modules to be in a file without prematurely
        //tracing dependencies, and allows for anonymous module support,
        //where the module name is not known until the script onload event
        //occurs. If no context, use the global queue, and get it processed
        //in the onscript load callback.
        (context ? context.defQueue : globalDefQueue).push([name, deps, callback]);

        return undefined;
    };

    define.amd = {
        multiversion: true,
        plugins: true,
        jQuery: true
    };

    /**
     * Executes the text. Normally just uses eval, but can be modified
     * to use a more environment specific call.
     * @param {String} text the text to execute/evaluate.
     */
    req.exec = function (text) {
        return eval(text);
    };

    /**
     * Executes a module callack function. Broken out as a separate function
     * solely to allow the build system to sequence the files in the built
     * layer in the right sequence.
     *
     * @private
     */
    req.execCb = function (name, callback, args, exports) {
        return callback.apply(exports, args);
    };


    /**
     * Adds a node to the DOM. Public function since used by the order plugin.
     * This method should not normally be called by outside code.
     */
    req.addScriptToDom = function (node) {
        //For some cache cases in IE 6-8, the script executes before the end
        //of the appendChild execution, so to tie an anonymous define
        //call to the module name (which is stored on the node), hold on
        //to a reference to this node, but clear after the DOM insertion.
        currentlyAddingScript = node;
        if (baseElement) {
            head.insertBefore(node, baseElement);
        } else {
            head.appendChild(node);
        }
        currentlyAddingScript = null;
    };

    /**
     * callback for script loads, used to check status of loading.
     *
     * @param {Event} evt the event from the browser for the script
     * that was loaded.
     *
     * @private
     */
    req.onScriptLoad = function (evt) {
        //Using currentTarget instead of target for Firefox 2.0's sake. Not
        //all old browsers will be supported, but this one was easy enough
        //to support and still makes sense.
        var node = evt.currentTarget || evt.srcElement, contextName, moduleName,
            context;

        if (evt.type === "load" || (node && readyRegExp.test(node.readyState))) {
            //Reset interactive script so a script node is not held onto for
            //to long.
            interactiveScript = null;

            //Pull out the name of the module and the context.
            contextName = node.getAttribute("data-requirecontext");
            moduleName = node.getAttribute("data-requiremodule");
            context = contexts[contextName];

            contexts[contextName].completeLoad(moduleName);

            //Clean up script binding. Favor detachEvent because of IE9
            //issue, see attachEvent/addEventListener comment elsewhere
            //in this file.
            if (node.detachEvent && !isOpera) {
                //Probably IE. If not it will throw an error, which will be
                //useful to know.
                node.detachEvent("onreadystatechange", req.onScriptLoad);
            } else {
                node.removeEventListener("load", req.onScriptLoad, false);
            }
        }
    };

    /**
     * Attaches the script represented by the URL to the current
     * environment. Right now only supports browser loading,
     * but can be redefined in other environments to do the right thing.
     * @param {String} url the url of the script to attach.
     * @param {Object} context the context that wants the script.
     * @param {moduleName} the name of the module that is associated with the script.
     * @param {Function} [callback] optional callback, defaults to require.onScriptLoad
     * @param {String} [type] optional type, defaults to text/javascript
     * @param {Function} [fetchOnlyFunction] optional function to indicate the script node
     * should be set up to fetch the script but do not attach it to the DOM
     * so that it can later be attached to execute it. This is a way for the
     * order plugin to support ordered loading in IE. Once the script is fetched,
     * but not executed, the fetchOnlyFunction will be called.
     */
    req.attach = function (url, context, moduleName, callback, type, fetchOnlyFunction) {
        var node;
        if (isBrowser) {
            //In the browser so use a script tag
            callback = callback || req.onScriptLoad;
            node = context && context.config && context.config.xhtml ?
                    document.createElementNS("http://www.w3.org/1999/xhtml", "html:script") :
                    document.createElement("script");
            node.type = type || "text/javascript";
            node.charset = "utf-8";
            //Use async so Gecko does not block on executing the script if something
            //like a long-polling comet tag is being run first. Gecko likes
            //to evaluate scripts in DOM order, even for dynamic scripts.
            //It will fetch them async, but only evaluate the contents in DOM
            //order, so a long-polling script tag can delay execution of scripts
            //after it. But telling Gecko we expect async gets us the behavior
            //we want -- execute it whenever it is finished downloading. Only
            //Helps Firefox 3.6+
            //Allow some URLs to not be fetched async. Mostly helps the order!
            //plugin
            node.async = !s.skipAsync[url];

            if (context) {
                node.setAttribute("data-requirecontext", context.contextName);
            }
            node.setAttribute("data-requiremodule", moduleName);

            //Set up load listener. Test attachEvent first because IE9 has
            //a subtle issue in its addEventListener and script onload firings
            //that do not match the behavior of all other browsers with
            //addEventListener support, which fire the onload event for a
            //script right after the script execution. See:
            //https://connect.microsoft.com/IE/feedback/details/648057/script-onload-event-is-not-fired-immediately-after-script-execution
            //UNFORTUNATELY Opera implements attachEvent but does not follow the script
            //script execution mode.
            if (node.attachEvent && !isOpera) {
                //Probably IE. IE (at least 6-8) do not fire
                //script onload right after executing the script, so
                //we cannot tie the anonymous define call to a name.
                //However, IE reports the script as being in "interactive"
                //readyState at the time of the define call.
                useInteractive = true;


                if (fetchOnlyFunction) {
                    //Need to use old school onreadystate here since
                    //when the event fires and the node is not attached
                    //to the DOM, the evt.srcElement is null, so use
                    //a closure to remember the node.
                    node.onreadystatechange = function (evt) {
                        //Script loaded but not executed.
                        //Clear loaded handler, set the real one that
                        //waits for script execution.
                        if (node.readyState === 'loaded') {
                            node.onreadystatechange = null;
                            node.attachEvent("onreadystatechange", callback);
                            fetchOnlyFunction(node);
                        }
                    };
                } else {
                    node.attachEvent("onreadystatechange", callback);
                }
            } else {
                node.addEventListener("load", callback, false);
            }
            node.src = url;

            //Fetch only means waiting to attach to DOM after loaded.
            if (!fetchOnlyFunction) {
                req.addScriptToDom(node);
            }

            return node;
        } else if (isWebWorker) {
            //In a web worker, use importScripts. This is not a very
            //efficient use of importScripts, importScripts will block until
            //its script is downloaded and evaluated. However, if web workers
            //are in play, the expectation that a build has been done so that
            //only one script needs to be loaded anyway. This may need to be
            //reevaluated if other use cases become common.
            importScripts(url);

            //Account for anonymous modules
            context.completeLoad(moduleName);
        }
        return null;
    };

    //Look for a data-main script attribute, which could also adjust the baseUrl.
    if (isBrowser) {
        //Figure out baseUrl. Get it from the script tag with require.js in it.
        scripts = document.getElementsByTagName("script");

        for (i = scripts.length - 1; i > -1 && (script = scripts[i]); i--) {
            //Set the "head" where we can append children by
            //using the script's parent.
            if (!head) {
                head = script.parentNode;
            }

            //Look for a data-main attribute to set main script for the page
            //to load. If it is there, the path to data main becomes the
            //baseUrl, if it is not already set.
            if ((dataMain = script.getAttribute('data-main'))) {
                if (!cfg.baseUrl) {
                    //Pull off the directory of data-main for use as the
                    //baseUrl.
                    src = dataMain.split('/');
                    mainScript = src.pop();
                    subPath = src.length ? src.join('/')  + '/' : './';

                    //Set final config.
                    cfg.baseUrl = subPath;
                    //Strip off any trailing .js since dataMain is now
                    //like a module name.
                    dataMain = mainScript.replace(jsSuffixRegExp, '');
                }

                //Put the data-main script in the files to load.
                cfg.deps = cfg.deps ? cfg.deps.concat(dataMain) : [dataMain];

                break;
            }
        }
    }

    //See if there is nothing waiting across contexts, and if not, trigger
    //resourcesReady.
    req.checkReadyState = function () {
        var contexts = s.contexts, prop;
        for (prop in contexts) {
            if (!(prop in empty)) {
                if (contexts[prop].waitCount) {
                    return;
                }
            }
        }
        req.resourcesReady(true);
    };

    /**
     * Internal function that is triggered whenever all scripts/resources
     * have been loaded by the loader. Can be overridden by other, for
     * instance the domReady plugin, which wants to know when all resources
     * are loaded.
     */
    req.resourcesReady = function (isReady) {
        var contexts, context, prop;

        //First, set the public variable indicating that resources are loading.
        req.resourcesDone = isReady;

        if (req.resourcesDone) {
            //If jQuery with DOM ready delayed, release it now.
            contexts = s.contexts;
            for (prop in contexts) {
                if (!(prop in empty)) {
                    context = contexts[prop];
                    if (context.jQueryIncremented) {
                        jQueryHoldReady(context.jQuery, false);
                        context.jQueryIncremented = false;
                    }
                }
            }
        }
    };

    //FF < 3.6 readyState fix. Needed so that domReady plugin
    //works well in that environment, since require.js is normally
    //loaded via an HTML script tag so it will be there before window load,
    //where the domReady plugin is more likely to be loaded after window load.
    req.pageLoaded = function () {
        if (document.readyState !== "complete") {
            document.readyState = "complete";
        }
    };
    if (isBrowser) {
        if (document.addEventListener) {
            if (!document.readyState) {
                document.readyState = "loading";
                window.addEventListener("load", req.pageLoaded, false);
            }
        }
    }

    //Set up default context. If require was a configuration object, use that as base config.
    req(cfg);

    //If modules are built into require.js, then need to make sure dependencies are
    //traced. Use a setTimeout in the browser world, to allow all the modules to register
    //themselves. In a non-browser env, assume that modules are not built into require.js,
    //which seems odd to do on the server.
    if (req.isAsync && typeof setTimeout !== "undefined") {
        ctx = s.contexts[(cfg.context || defContextName)];
        //Indicate that the script that includes require() is still loading,
        //so that require()'d dependencies are not traced until the end of the
        //file is parsed (approximated via the setTimeout call).
        ctx.requireWait = true;
        setTimeout(function () {
            ctx.requireWait = false;

            //Any modules included with the require.js file will be in the
            //global queue, assign them to this context.
            ctx.takeGlobalQueue();

            //Allow for jQuery to be loaded/already in the page, and if jQuery 1.4.3,
            //make sure to hold onto it for readyWait triggering.
            ctx.jQueryCheck();

            if (!ctx.scriptCount) {
                ctx.resume();
            }
            req.checkReadyState();
        }, 0);
    }
}());/* Modernizr 2.0.6 (Custom Build) | MIT & BSD
 * Build: http://www.modernizr.com/download/#-backgroundsize-borderimage-borderradius-boxshadow-flexbox-multiplebgs-opacity-rgba-textshadow-cssanimations-csscolumns-cssgradients-csstransforms-csstransforms3d-csstransitions-applicationcache-canvas-canvastext-draganddrop-hashchange-audio-video-indexeddb-input-inputtypes-localstorage-postmessage-websockets-websqldatabase-webworkers-geolocation-touch-iepp-mq-cssclasses-addtest-teststyles-testprop-testallprops-hasevent-prefixes-domprefixes-file_api
 */
;window.Modernizr=function(a,b,c){function H(){e.input=function(a){for(var b=0,c=a.length;b<c;b++)s[a[b]]=a[b]in l;return s}("autocomplete autofocus list placeholder max min multiple pattern required step".split(" ")),e.inputtypes=function(a){for(var d=0,e,f,h,i=a.length;d<i;d++)l.setAttribute("type",f=a[d]),e=l.type!=="text",e&&(l.value=m,l.style.cssText="position:absolute;visibility:hidden;",/^range$/.test(f)&&l.style.WebkitAppearance!==c?(g.appendChild(l),h=b.defaultView,e=h.getComputedStyle&&h.getComputedStyle(l,null).WebkitAppearance!=="textfield"&&l.offsetHeight!==0,g.removeChild(l)):/^(search|tel)$/.test(f)||(/^(url|email)$/.test(f)?e=l.checkValidity&&l.checkValidity()===!1:/^color$/.test(f)?(g.appendChild(l),g.offsetWidth,e=l.value!=m,g.removeChild(l)):e=l.value!=m)),r[a[d]]=!!e;return r}("search tel url email datetime date month week time datetime-local number range color".split(" "))}function F(a,b){var c=a.charAt(0).toUpperCase()+a.substr(1),d=(a+" "+p.join(c+" ")+c).split(" ");return E(d,b)}function E(a,b){for(var d in a)if(k[a[d]]!==c)return b=="pfx"?a[d]:!0;return!1}function D(a,b){return!!~(""+a).indexOf(b)}function C(a,b){return typeof a===b}function B(a,b){return A(o.join(a+";")+(b||""))}function A(a){k.cssText=a}var d="2.0.6",e={},f=!0,g=b.documentElement,h=b.head||b.getElementsByTagName("head")[0],i="modernizr",j=b.createElement(i),k=j.style,l=b.createElement("input"),m=":)",n=Object.prototype.toString,o=" -webkit- -moz- -o- -ms- -khtml- ".split(" "),p="Webkit Moz O ms Khtml".split(" "),q={},r={},s={},t=[],u=function(a,c,d,e){var f,h,j,k=b.createElement("div");if(parseInt(d,10))while(d--)j=b.createElement("div"),j.id=e?e[d]:i+(d+1),k.appendChild(j);f=["&shy;","<style>",a,"</style>"].join(""),k.id=i,k.innerHTML+=f,g.appendChild(k),h=c(k,a),k.parentNode.removeChild(k);return!!h},v=function(b){if(a.matchMedia)return matchMedia(b).matches;var c;u("@media "+b+" { #"+i+" { position: absolute; } }",function(b){c=(a.getComputedStyle?getComputedStyle(b,null):b.currentStyle).position=="absolute"});return c},w=function(){function d(d,e){e=e||b.createElement(a[d]||"div"),d="on"+d;var f=d in e;f||(e.setAttribute||(e=b.createElement("div")),e.setAttribute&&e.removeAttribute&&(e.setAttribute(d,""),f=C(e[d],"function"),C(e[d],c)||(e[d]=c),e.removeAttribute(d))),e=null;return f}var a={select:"input",change:"input",submit:"form",reset:"form",error:"img",load:"img",abort:"img"};return d}(),x,y={}.hasOwnProperty,z;!C(y,c)&&!C(y.call,c)?z=function(a,b){return y.call(a,b)}:z=function(a,b){return b in a&&C(a.constructor.prototype[b],c)};var G=function(c,d){var f=c.join(""),g=d.length;u(f,function(c,d){var f=b.styleSheets[b.styleSheets.length-1],h=f.cssRules&&f.cssRules[0]?f.cssRules[0].cssText:f.cssText||"",i=c.childNodes,j={};while(g--)j[i[g].id]=i[g];e.touch="ontouchstart"in a||j.touch.offsetTop===9,e.csstransforms3d=j.csstransforms3d.offsetLeft===9},g,d)}([,["@media (",o.join("touch-enabled),("),i,")","{#touch{top:9px;position:absolute}}"].join(""),["@media (",o.join("transform-3d),("),i,")","{#csstransforms3d{left:9px;position:absolute}}"].join("")],[,"touch","csstransforms3d"]);q.flexbox=function(){function c(a,b,c,d){a.style.cssText=o.join(b+":"+c+";")+(d||"")}function a(a,b,c,d){b+=":",a.style.cssText=(b+o.join(c+";"+b)).slice(0,-b.length)+(d||"")}var d=b.createElement("div"),e=b.createElement("div");a(d,"display","box","width:42px;padding:0;"),c(e,"box-flex","1","width:10px;"),d.appendChild(e),g.appendChild(d);var f=e.offsetWidth===42;d.removeChild(e),g.removeChild(d);return f},q.canvas=function(){var a=b.createElement("canvas");return!!a.getContext&&!!a.getContext("2d")},q.canvastext=function(){return!!e.canvas&&!!C(b.createElement("canvas").getContext("2d").fillText,"function")},q.touch=function(){return e.touch},q.geolocation=function(){return!!navigator.geolocation},q.postmessage=function(){return!!a.postMessage},q.websqldatabase=function(){var b=!!a.openDatabase;return b},q.indexedDB=function(){for(var b=-1,c=p.length;++b<c;)if(a[p[b].toLowerCase()+"IndexedDB"])return!0;return!!a.indexedDB},q.hashchange=function(){return w("hashchange",a)&&(b.documentMode===c||b.documentMode>7)},q.draganddrop=function(){return w("dragstart")&&w("drop")},q.websockets=function(){for(var b=-1,c=p.length;++b<c;)if(a[p[b]+"WebSocket"])return!0;return"WebSocket"in a},q.rgba=function(){A("background-color:rgba(150,255,150,.5)");return D(k.backgroundColor,"rgba")},q.multiplebgs=function(){A("background:url(https://),url(https://),red url(https://)");return/(url\s*\(.*?){3}/.test(k.background)},q.backgroundsize=function(){return F("backgroundSize")},q.borderimage=function(){return F("borderImage")},q.borderradius=function(){return F("borderRadius")},q.boxshadow=function(){return F("boxShadow")},q.textshadow=function(){return b.createElement("div").style.textShadow===""},q.opacity=function(){B("opacity:.55");return/^0.55$/.test(k.opacity)},q.cssanimations=function(){return F("animationName")},q.csscolumns=function(){return F("columnCount")},q.cssgradients=function(){var a="background-image:",b="gradient(linear,left top,right bottom,from(#9f9),to(white));",c="linear-gradient(left top,#9f9, white);";A((a+o.join(b+a)+o.join(c+a)).slice(0,-a.length));return D(k.backgroundImage,"gradient")},q.csstransforms=function(){return!!E(["transformProperty","WebkitTransform","MozTransform","OTransform","msTransform"])},q.csstransforms3d=function(){var a=!!E(["perspectiveProperty","WebkitPerspective","MozPerspective","OPerspective","msPerspective"]);a&&"webkitPerspective"in g.style&&(a=e.csstransforms3d);return a},q.csstransitions=function(){return F("transitionProperty")},q.video=function(){var a=b.createElement("video"),c=!1;try{if(c=!!a.canPlayType){c=new Boolean(c),c.ogg=a.canPlayType('video/ogg; codecs="theora"');var d='video/mp4; codecs="avc1.42E01E';c.h264=a.canPlayType(d+'"')||a.canPlayType(d+', mp4a.40.2"'),c.webm=a.canPlayType('video/webm; codecs="vp8, vorbis"')}}catch(e){}return c},q.audio=function(){var a=b.createElement("audio"),c=!1;try{if(c=!!a.canPlayType)c=new Boolean(c),c.ogg=a.canPlayType('audio/ogg; codecs="vorbis"'),c.mp3=a.canPlayType("audio/mpeg;"),c.wav=a.canPlayType('audio/wav; codecs="1"'),c.m4a=a.canPlayType("audio/x-m4a;")||a.canPlayType("audio/aac;")}catch(d){}return c},q.localstorage=function(){try{return!!localStorage.getItem}catch(a){return!1}},q.webworkers=function(){return!!a.Worker},q.applicationcache=function(){return!!a.applicationCache};for(var I in q)z(q,I)&&(x=I.toLowerCase(),e[x]=q[I](),t.push((e[x]?"":"no-")+x));e.input||H(),e.addTest=function(a,b){if(typeof a=="object")for(var d in a)z(a,d)&&e.addTest(d,a[d]);else{a=a.toLowerCase();if(e[a]!==c)return;b=typeof b=="boolean"?b:!!b(),g.className+=" "+(b?"":"no-")+a,e[a]=b}return e},A(""),j=l=null,a.attachEvent&&function(){var a=b.createElement("div");a.innerHTML="<elem></elem>";return a.childNodes.length!==1}()&&function(a,b){function s(a){var b=-1;while(++b<g)a.createElement(f[b])}a.iepp=a.iepp||{};var d=a.iepp,e=d.html5elements||"abbr|article|aside|audio|canvas|datalist|details|figcaption|figure|footer|header|hgroup|mark|meter|nav|output|progress|section|summary|time|video",f=e.split("|"),g=f.length,h=new RegExp("(^|\\s)("+e+")","gi"),i=new RegExp("<(/*)("+e+")","gi"),j=/^\s*[\{\}]\s*$/,k=new RegExp("(^|[^\\n]*?\\s)("+e+")([^\\n]*)({[\\n\\w\\W]*?})","gi"),l=b.createDocumentFragment(),m=b.documentElement,n=m.firstChild,o=b.createElement("body"),p=b.createElement("style"),q=/print|all/,r;d.getCSS=function(a,b){if(a+""===c)return"";var e=-1,f=a.length,g,h=[];while(++e<f){g=a[e];if(g.disabled)continue;b=g.media||b,q.test(b)&&h.push(d.getCSS(g.imports,b),g.cssText),b="all"}return h.join("")},d.parseCSS=function(a){var b=[],c;while((c=k.exec(a))!=null)b.push(((j.exec(c[1])?"\n":c[1])+c[2]+c[3]).replace(h,"$1.iepp_$2")+c[4]);return b.join("\n")},d.writeHTML=function(){var a=-1;r=r||b.body;while(++a<g){var c=b.getElementsByTagName(f[a]),d=c.length,e=-1;while(++e<d)c[e].className.indexOf("iepp_")<0&&(c[e].className+=" iepp_"+f[a])}l.appendChild(r),m.appendChild(o),o.className=r.className,o.id=r.id,o.innerHTML=r.innerHTML.replace(i,"<$1font")},d._beforePrint=function(){p.styleSheet.cssText=d.parseCSS(d.getCSS(b.styleSheets,"all")),d.writeHTML()},d.restoreHTML=function(){o.innerHTML="",m.removeChild(o),m.appendChild(r)},d._afterPrint=function(){d.restoreHTML(),p.styleSheet.cssText=""},s(b),s(l);d.disablePP||(n.insertBefore(p,n.firstChild),p.media="print",p.className="iepp-printshim",a.attachEvent("onbeforeprint",d._beforePrint),a.attachEvent("onafterprint",d._afterPrint))}(a,b),e._version=d,e._prefixes=o,e._domPrefixes=p,e.mq=v,e.hasEvent=w,e.testProp=function(a){return E([a])},e.testAllProps=F,e.testStyles=u,g.className=g.className.replace(/\bno-js\b/,"")+(f?" js "+t.join(" "):"");return e}(this,this.document),Modernizr.addTest("file",function(){return!!(window.File&&window.FileList&&window.FileReader)});// Backbone.js 0.9.2

// (c) 2010-2012 Jeremy Ashkenas, DocumentCloud Inc.
// Backbone may be freely distributed under the MIT license.
// For all details and documentation:
// http://backbonejs.org
(function(){var l=this,y=l.Backbone,z=Array.prototype.slice,A=Array.prototype.splice,g;g="undefined"!==typeof exports?exports:l.Backbone={};g.VERSION="0.9.2";var f=l._;!f&&"undefined"!==typeof require&&(f=require("underscore"));var i=l.jQuery||l.Zepto||l.ender;g.setDomLibrary=function(a){i=a};g.noConflict=function(){l.Backbone=y;return this};g.emulateHTTP=!1;g.emulateJSON=!1;var p=/\s+/,k=g.Events={on:function(a,b,c){var d,e,f,g,j;if(!b)return this;a=a.split(p);for(d=this._callbacks||(this._callbacks=
{});e=a.shift();)f=(j=d[e])?j.tail:{},f.next=g={},f.context=c,f.callback=b,d[e]={tail:g,next:j?j.next:f};return this},off:function(a,b,c){var d,e,h,g,j,q;if(e=this._callbacks){if(!a&&!b&&!c)return delete this._callbacks,this;for(a=a?a.split(p):f.keys(e);d=a.shift();)if(h=e[d],delete e[d],h&&(b||c))for(g=h.tail;(h=h.next)!==g;)if(j=h.callback,q=h.context,b&&j!==b||c&&q!==c)this.on(d,j,q);return this}},trigger:function(a){var b,c,d,e,f,g;if(!(d=this._callbacks))return this;f=d.all;a=a.split(p);for(g=
z.call(arguments,1);b=a.shift();){if(c=d[b])for(e=c.tail;(c=c.next)!==e;)c.callback.apply(c.context||this,g);if(c=f){e=c.tail;for(b=[b].concat(g);(c=c.next)!==e;)c.callback.apply(c.context||this,b)}}return this}};k.bind=k.on;k.unbind=k.off;var o=g.Model=function(a,b){var c;a||(a={});b&&b.parse&&(a=this.parse(a));if(c=n(this,"defaults"))a=f.extend({},c,a);b&&b.collection&&(this.collection=b.collection);this.attributes={};this._escapedAttributes={};this.cid=f.uniqueId("c");this.changed={};this._silent=
{};this._pending={};this.set(a,{silent:!0});this.changed={};this._silent={};this._pending={};this._previousAttributes=f.clone(this.attributes);this.initialize.apply(this,arguments)};f.extend(o.prototype,k,{changed:null,_silent:null,_pending:null,idAttribute:"id",initialize:function(){},toJSON:function(){return f.clone(this.attributes)},get:function(a){return this.attributes[a]},escape:function(a){var b;if(b=this._escapedAttributes[a])return b;b=this.get(a);return this._escapedAttributes[a]=f.escape(null==
b?"":""+b)},has:function(a){return null!=this.get(a)},set:function(a,b,c){var d,e;f.isObject(a)||null==a?(d=a,c=b):(d={},d[a]=b);c||(c={});if(!d)return this;d instanceof o&&(d=d.attributes);if(c.unset)for(e in d)d[e]=void 0;if(!this._validate(d,c))return!1;this.idAttribute in d&&(this.id=d[this.idAttribute]);var b=c.changes={},h=this.attributes,g=this._escapedAttributes,j=this._previousAttributes||{};for(e in d){a=d[e];if(!f.isEqual(h[e],a)||c.unset&&f.has(h,e))delete g[e],(c.silent?this._silent:
b)[e]=!0;c.unset?delete h[e]:h[e]=a;!f.isEqual(j[e],a)||f.has(h,e)!=f.has(j,e)?(this.changed[e]=a,c.silent||(this._pending[e]=!0)):(delete this.changed[e],delete this._pending[e])}c.silent||this.change(c);return this},unset:function(a,b){(b||(b={})).unset=!0;return this.set(a,null,b)},clear:function(a){(a||(a={})).unset=!0;return this.set(f.clone(this.attributes),a)},fetch:function(a){var a=a?f.clone(a):{},b=this,c=a.success;a.success=function(d,e,f){if(!b.set(b.parse(d,f),a))return!1;c&&c(b,d)};
a.error=g.wrapError(a.error,b,a);return(this.sync||g.sync).call(this,"read",this,a)},save:function(a,b,c){var d,e;f.isObject(a)||null==a?(d=a,c=b):(d={},d[a]=b);c=c?f.clone(c):{};if(c.wait){if(!this._validate(d,c))return!1;e=f.clone(this.attributes)}a=f.extend({},c,{silent:!0});if(d&&!this.set(d,c.wait?a:c))return!1;var h=this,i=c.success;c.success=function(a,b,e){b=h.parse(a,e);if(c.wait){delete c.wait;b=f.extend(d||{},b)}if(!h.set(b,c))return false;i?i(h,a):h.trigger("sync",h,a,c)};c.error=g.wrapError(c.error,
h,c);b=this.isNew()?"create":"update";b=(this.sync||g.sync).call(this,b,this,c);c.wait&&this.set(e,a);return b},destroy:function(a){var a=a?f.clone(a):{},b=this,c=a.success,d=function(){b.trigger("destroy",b,b.collection,a)};if(this.isNew())return d(),!1;a.success=function(e){a.wait&&d();c?c(b,e):b.trigger("sync",b,e,a)};a.error=g.wrapError(a.error,b,a);var e=(this.sync||g.sync).call(this,"delete",this,a);a.wait||d();return e},url:function(){var a=n(this,"urlRoot")||n(this.collection,"url")||t();
return this.isNew()?a:a+("/"==a.charAt(a.length-1)?"":"/")+encodeURIComponent(this.id)},parse:function(a){return a},clone:function(){return new this.constructor(this.attributes)},isNew:function(){return null==this.id},change:function(a){a||(a={});var b=this._changing;this._changing=!0;for(var c in this._silent)this._pending[c]=!0;var d=f.extend({},a.changes,this._silent);this._silent={};for(c in d)this.trigger("change:"+c,this,this.get(c),a);if(b)return this;for(;!f.isEmpty(this._pending);){this._pending=
{};this.trigger("change",this,a);for(c in this.changed)!this._pending[c]&&!this._silent[c]&&delete this.changed[c];this._previousAttributes=f.clone(this.attributes)}this._changing=!1;return this},hasChanged:function(a){return!arguments.length?!f.isEmpty(this.changed):f.has(this.changed,a)},changedAttributes:function(a){if(!a)return this.hasChanged()?f.clone(this.changed):!1;var b,c=!1,d=this._previousAttributes,e;for(e in a)if(!f.isEqual(d[e],b=a[e]))(c||(c={}))[e]=b;return c},previous:function(a){return!arguments.length||
!this._previousAttributes?null:this._previousAttributes[a]},previousAttributes:function(){return f.clone(this._previousAttributes)},isValid:function(){return!this.validate(this.attributes)},_validate:function(a,b){if(b.silent||!this.validate)return!0;var a=f.extend({},this.attributes,a),c=this.validate(a,b);if(!c)return!0;b&&b.error?b.error(this,c,b):this.trigger("error",this,c,b);return!1}});var r=g.Collection=function(a,b){b||(b={});b.model&&(this.model=b.model);b.comparator&&(this.comparator=b.comparator);
this._reset();this.initialize.apply(this,arguments);a&&this.reset(a,{silent:!0,parse:b.parse})};f.extend(r.prototype,k,{model:o,initialize:function(){},toJSON:function(a){return this.map(function(b){return b.toJSON(a)})},add:function(a,b){var c,d,e,g,i,j={},k={},l=[];b||(b={});a=f.isArray(a)?a.slice():[a];c=0;for(d=a.length;c<d;c++){if(!(e=a[c]=this._prepareModel(a[c],b)))throw Error("Can't add an invalid model to a collection");g=e.cid;i=e.id;j[g]||this._byCid[g]||null!=i&&(k[i]||this._byId[i])?
l.push(c):j[g]=k[i]=e}for(c=l.length;c--;)a.splice(l[c],1);c=0;for(d=a.length;c<d;c++)(e=a[c]).on("all",this._onModelEvent,this),this._byCid[e.cid]=e,null!=e.id&&(this._byId[e.id]=e);this.length+=d;A.apply(this.models,[null!=b.at?b.at:this.models.length,0].concat(a));this.comparator&&this.sort({silent:!0});if(b.silent)return this;c=0;for(d=this.models.length;c<d;c++)if(j[(e=this.models[c]).cid])b.index=c,e.trigger("add",e,this,b);return this},remove:function(a,b){var c,d,e,g;b||(b={});a=f.isArray(a)?
a.slice():[a];c=0;for(d=a.length;c<d;c++)if(g=this.getByCid(a[c])||this.get(a[c]))delete this._byId[g.id],delete this._byCid[g.cid],e=this.indexOf(g),this.models.splice(e,1),this.length--,b.silent||(b.index=e,g.trigger("remove",g,this,b)),this._removeReference(g);return this},push:function(a,b){a=this._prepareModel(a,b);this.add(a,b);return a},pop:function(a){var b=this.at(this.length-1);this.remove(b,a);return b},unshift:function(a,b){a=this._prepareModel(a,b);this.add(a,f.extend({at:0},b));return a},
shift:function(a){var b=this.at(0);this.remove(b,a);return b},get:function(a){return null==a?void 0:this._byId[null!=a.id?a.id:a]},getByCid:function(a){return a&&this._byCid[a.cid||a]},at:function(a){return this.models[a]},where:function(a){return f.isEmpty(a)?[]:this.filter(function(b){for(var c in a)if(a[c]!==b.get(c))return!1;return!0})},sort:function(a){a||(a={});if(!this.comparator)throw Error("Cannot sort a set without a comparator");var b=f.bind(this.comparator,this);1==this.comparator.length?
this.models=this.sortBy(b):this.models.sort(b);a.silent||this.trigger("reset",this,a);return this},pluck:function(a){return f.map(this.models,function(b){return b.get(a)})},reset:function(a,b){a||(a=[]);b||(b={});for(var c=0,d=this.models.length;c<d;c++)this._removeReference(this.models[c]);this._reset();this.add(a,f.extend({silent:!0},b));b.silent||this.trigger("reset",this,b);return this},fetch:function(a){a=a?f.clone(a):{};void 0===a.parse&&(a.parse=!0);var b=this,c=a.success;a.success=function(d,
e,f){b[a.add?"add":"reset"](b.parse(d,f),a);c&&c(b,d)};a.error=g.wrapError(a.error,b,a);return(this.sync||g.sync).call(this,"read",this,a)},create:function(a,b){var c=this,b=b?f.clone(b):{},a=this._prepareModel(a,b);if(!a)return!1;b.wait||c.add(a,b);var d=b.success;b.success=function(e,f){b.wait&&c.add(e,b);d?d(e,f):e.trigger("sync",a,f,b)};a.save(null,b);return a},parse:function(a){return a},chain:function(){return f(this.models).chain()},_reset:function(){this.length=0;this.models=[];this._byId=
{};this._byCid={}},_prepareModel:function(a,b){b||(b={});a instanceof o?a.collection||(a.collection=this):(b.collection=this,a=new this.model(a,b),a._validate(a.attributes,b)||(a=!1));return a},_removeReference:function(a){this==a.collection&&delete a.collection;a.off("all",this._onModelEvent,this)},_onModelEvent:function(a,b,c,d){("add"==a||"remove"==a)&&c!=this||("destroy"==a&&this.remove(b,d),b&&a==="change:"+b.idAttribute&&(delete this._byId[b.previous(b.idAttribute)],this._byId[b.id]=b),this.trigger.apply(this,
arguments))}});f.each("forEach,each,map,reduce,reduceRight,find,detect,filter,select,reject,every,all,some,any,include,contains,invoke,max,min,sortBy,sortedIndex,toArray,size,first,initial,rest,last,without,indexOf,shuffle,lastIndexOf,isEmpty,groupBy".split(","),function(a){r.prototype[a]=function(){return f[a].apply(f,[this.models].concat(f.toArray(arguments)))}});var u=g.Router=function(a){a||(a={});a.routes&&(this.routes=a.routes);this._bindRoutes();this.initialize.apply(this,arguments)},B=/:\w+/g,
C=/\*\w+/g,D=/[-[\]{}()+?.,\\^$|#\s]/g;f.extend(u.prototype,k,{initialize:function(){},route:function(a,b,c){g.history||(g.history=new m);f.isRegExp(a)||(a=this._routeToRegExp(a));c||(c=this[b]);g.history.route(a,f.bind(function(d){d=this._extractParameters(a,d);c&&c.apply(this,d);this.trigger.apply(this,["route:"+b].concat(d));g.history.trigger("route",this,b,d)},this));return this},navigate:function(a,b){g.history.navigate(a,b)},_bindRoutes:function(){if(this.routes){var a=[],b;for(b in this.routes)a.unshift([b,
this.routes[b]]);b=0;for(var c=a.length;b<c;b++)this.route(a[b][0],a[b][1],this[a[b][1]])}},_routeToRegExp:function(a){a=a.replace(D,"\\$&").replace(B,"([^/]+)").replace(C,"(.*?)");return RegExp("^"+a+"$")},_extractParameters:function(a,b){return a.exec(b).slice(1)}});var m=g.History=function(){this.handlers=[];f.bindAll(this,"checkUrl")},s=/^[#\/]/,E=/msie [\w.]+/;m.started=!1;f.extend(m.prototype,k,{interval:50,getHash:function(a){return(a=(a?a.location:window.location).href.match(/#(.*)$/))?a[1]:
""},getFragment:function(a,b){if(null==a)if(this._hasPushState||b){var a=window.location.pathname,c=window.location.search;c&&(a+=c)}else a=this.getHash();a.indexOf(this.options.root)||(a=a.substr(this.options.root.length));return a.replace(s,"")},start:function(a){if(m.started)throw Error("Backbone.history has already been started");m.started=!0;this.options=f.extend({},{root:"/"},this.options,a);this._wantsHashChange=!1!==this.options.hashChange;this._wantsPushState=!!this.options.pushState;this._hasPushState=
!(!this.options.pushState||!window.history||!window.history.pushState);var a=this.getFragment(),b=document.documentMode;if(b=E.exec(navigator.userAgent.toLowerCase())&&(!b||7>=b))this.iframe=i('<iframe src="javascript:0" tabindex="-1" />').hide().appendTo("body")[0].contentWindow,this.navigate(a);this._hasPushState?i(window).bind("popstate",this.checkUrl):this._wantsHashChange&&"onhashchange"in window&&!b?i(window).bind("hashchange",this.checkUrl):this._wantsHashChange&&(this._checkUrlInterval=setInterval(this.checkUrl,
this.interval));this.fragment=a;a=window.location;b=a.pathname==this.options.root;if(this._wantsHashChange&&this._wantsPushState&&!this._hasPushState&&!b)return this.fragment=this.getFragment(null,!0),window.location.replace(this.options.root+"#"+this.fragment),!0;this._wantsPushState&&this._hasPushState&&b&&a.hash&&(this.fragment=this.getHash().replace(s,""),window.history.replaceState({},document.title,a.protocol+"//"+a.host+this.options.root+this.fragment));if(!this.options.silent)return this.loadUrl()},
stop:function(){i(window).unbind("popstate",this.checkUrl).unbind("hashchange",this.checkUrl);clearInterval(this._checkUrlInterval);m.started=!1},route:function(a,b){this.handlers.unshift({route:a,callback:b})},checkUrl:function(){var a=this.getFragment();a==this.fragment&&this.iframe&&(a=this.getFragment(this.getHash(this.iframe)));if(a==this.fragment)return!1;this.iframe&&this.navigate(a);this.loadUrl()||this.loadUrl(this.getHash())},loadUrl:function(a){var b=this.fragment=this.getFragment(a);return f.any(this.handlers,
function(a){if(a.route.test(b))return a.callback(b),!0})},navigate:function(a,b){if(!m.started)return!1;if(!b||!0===b)b={trigger:b};var c=(a||"").replace(s,"");this.fragment!=c&&(this._hasPushState?(0!=c.indexOf(this.options.root)&&(c=this.options.root+c),this.fragment=c,window.history[b.replace?"replaceState":"pushState"]({},document.title,c)):this._wantsHashChange?(this.fragment=c,this._updateHash(window.location,c,b.replace),this.iframe&&c!=this.getFragment(this.getHash(this.iframe))&&(b.replace||
this.iframe.document.open().close(),this._updateHash(this.iframe.location,c,b.replace))):window.location.assign(this.options.root+a),b.trigger&&this.loadUrl(a))},_updateHash:function(a,b,c){c?a.replace(a.toString().replace(/(javascript:|#).*$/,"")+"#"+b):a.hash=b}});var v=g.View=function(a){this.cid=f.uniqueId("view");this._configure(a||{});this._ensureElement();this.initialize.apply(this,arguments);this.delegateEvents()},F=/^(\S+)\s*(.*)$/,w="model,collection,el,id,attributes,className,tagName".split(",");
f.extend(v.prototype,k,{tagName:"div",$:function(a){return this.$el.find(a)},initialize:function(){},render:function(){return this},remove:function(){this.$el.remove();return this},make:function(a,b,c){a=document.createElement(a);b&&i(a).attr(b);c&&i(a).html(c);return a},setElement:function(a,b){this.$el&&this.undelegateEvents();this.$el=a instanceof i?a:i(a);this.el=this.$el[0];!1!==b&&this.delegateEvents();return this},delegateEvents:function(a){if(a||(a=n(this,"events"))){this.undelegateEvents();
for(var b in a){var c=a[b];f.isFunction(c)||(c=this[a[b]]);if(!c)throw Error('Method "'+a[b]+'" does not exist');var d=b.match(F),e=d[1],d=d[2],c=f.bind(c,this),e=e+(".delegateEvents"+this.cid);""===d?this.$el.bind(e,c):this.$el.delegate(d,e,c)}}},undelegateEvents:function(){this.$el.unbind(".delegateEvents"+this.cid)},_configure:function(a){this.options&&(a=f.extend({},this.options,a));for(var b=0,c=w.length;b<c;b++){var d=w[b];a[d]&&(this[d]=a[d])}this.options=a},_ensureElement:function(){if(this.el)this.setElement(this.el,
!1);else{var a=n(this,"attributes")||{};this.id&&(a.id=this.id);this.className&&(a["class"]=this.className);this.setElement(this.make(this.tagName,a),!1)}}});o.extend=r.extend=u.extend=v.extend=function(a,b){var c=G(this,a,b);c.extend=this.extend;return c};var H={create:"POST",update:"PUT","delete":"DELETE",read:"GET"};g.sync=function(a,b,c){var d=H[a];c||(c={});var e={type:d,dataType:"json"};c.url||(e.url=n(b,"url")||t());if(!c.data&&b&&("create"==a||"update"==a))e.contentType="application/json",
e.data=JSON.stringify(b.toJSON());g.emulateJSON&&(e.contentType="application/x-www-form-urlencoded",e.data=e.data?{model:e.data}:{});if(g.emulateHTTP&&("PUT"===d||"DELETE"===d))g.emulateJSON&&(e.data._method=d),e.type="POST",e.beforeSend=function(a){a.setRequestHeader("X-HTTP-Method-Override",d)};"GET"!==e.type&&!g.emulateJSON&&(e.processData=!1);return i.ajax(f.extend(e,c))};g.wrapError=function(a,b,c){return function(d,e){e=d===b?e:d;a?a(b,e,c):b.trigger("error",b,e,c)}};var x=function(){},G=function(a,
b,c){var d;d=b&&b.hasOwnProperty("constructor")?b.constructor:function(){a.apply(this,arguments)};f.extend(d,a);x.prototype=a.prototype;d.prototype=new x;b&&f.extend(d.prototype,b);c&&f.extend(d,c);d.prototype.constructor=d;d.__super__=a.prototype;return d},n=function(a,b){return!a||!a[b]?null:f.isFunction(a[b])?a[b]():a[b]},t=function(){throw Error('A "url" property or function must be specified');}}).call(this);

// Backbone.ModelBinder v0.1.4
// (c) 2012 Bart Wood
// Distributed Under MIT License

(function() {

    if(!Backbone){
        throw 'Please include Backbone.js before Backbone.ModelBinder.js';
    }

    Backbone.ModelBinder = function(){
        _.bindAll(this);
    };

    // Current version of the library.
    Backbone.ModelBinder.VERSION = '0.1.4';
    Backbone.ModelBinder.Constants = {};
    Backbone.ModelBinder.Constants.ModelToView = 'ModelToView';
    Backbone.ModelBinder.Constants.ViewToModel = 'ViewToModel';

    _.extend(Backbone.ModelBinder.prototype, {

        bind:function (model, rootEl, attributeBindings) {
            this.unbind();

            this._model = model;
            this._rootEl = rootEl;

            if (!this._model) throw 'model must be specified';
            if (!this._rootEl) throw 'rootEl must be specified';

            if(attributeBindings){
                // Create a deep clone of the attribute bindings
                this._attributeBindings = $.extend(true, {}, attributeBindings);

                this._initializeAttributeBindings();
                this._initializeElBindings();
            }
            else {
                this._initializeDefaultBindings();
            }

            this._bindModelToView();
            this._bindViewToModel();
        },

        unbind:function () {
            this._unbindModelToView();
            this._unbindViewToModel();

            if(this._attributeBindings){
                delete this._attributeBindings;
                this._attributeBindings = undefined;
            }
        },

        // Converts the input bindings, which might just be empty or strings, to binding objects
        _initializeAttributeBindings:function () {
            var attributeBindingKey, inputBinding, attributeBinding, elementBindingCount, elementBinding;

            for (attributeBindingKey in this._attributeBindings) {
                inputBinding = this._attributeBindings[attributeBindingKey];

                if (_.isString(inputBinding)) {
                    attributeBinding = {elementBindings: [{selector: inputBinding}]};
                }
                else if (_.isArray(inputBinding)) {
                    attributeBinding = {elementBindings: inputBinding};
                }
                else if(_.isObject(inputBinding)){
                    attributeBinding = {elementBindings: [inputBinding]};
                }
                else {
                    throw 'Unsupported type passed to Model Binder ' + attributeBinding;
                }

                // Add a linkage from the element binding back to the attribute binding
                for(elementBindingCount = 0; elementBindingCount < attributeBinding.elementBindings.length; elementBindingCount++){
                    elementBinding = attributeBinding.elementBindings[elementBindingCount];
                    elementBinding.attributeBinding = attributeBinding;
                }

                attributeBinding.attributeName = attributeBindingKey;
                this._attributeBindings[attributeBindingKey] = attributeBinding;
            }
        },

        // If the bindings are not specified, the default binding is performed on the name attribute
        _initializeDefaultBindings: function(){
            var elCount, namedEls, namedEl, name;
            this._attributeBindings = {};
            namedEls = $('[name]', this._rootEl);

            for(elCount = 0; elCount < namedEls.length; elCount++){
                namedEl = namedEls[elCount];
                name = $(namedEl).attr('name');

                // For elements like radio buttons we only want a single attribute binding with possibly multiple element bindings
                if(!this._attributeBindings[name]){
                    var attributeBinding =  {attributeName: name};
                    attributeBinding.elementBindings = [{attributeBinding: attributeBinding, boundEls: [namedEl]}];
                    this._attributeBindings[name] = attributeBinding;
                }
                else{
                    this._attributeBindings[name].elementBindings.push({attributeBinding: this._attributeBindings[name], boundEls: [namedEl]});
                }
            }
        },

        _initializeElBindings:function () {
            var bindingKey, attributeBinding, bindingCount, elementBinding, foundEls, elCount, el;
            for (bindingKey in this._attributeBindings) {
                attributeBinding = this._attributeBindings[bindingKey];

                for (bindingCount = 0; bindingCount < attributeBinding.elementBindings.length; bindingCount++) {
                    elementBinding = attributeBinding.elementBindings[bindingCount];
                    if (elementBinding.selector === '') {
						foundEls = $(this._rootEl);
                    }
                    else {
                        foundEls = $(elementBinding.selector, this._rootEl);
                    }

                    if (foundEls.length === 0) {
                        throw 'Bad binding found. No elements returned for binding selector ' + elementBinding.selector;
                    }
                    else {
                        elementBinding.boundEls = [];
                        for (elCount = 0; elCount < foundEls.length; elCount++) {
                            el = foundEls[elCount];
                            elementBinding.boundEls.push(el);
                        }
                    }
                }
            }
        },

        _bindModelToView: function () {
            this._model.on('change', this._onModelChange, this);

            this._copyModelAttributesToView();
        },

        // should only be called when initially binding the model to the view
        _copyModelAttributesToView: function(){
            var attributeName, attributeBinding;

            for (attributeName in this._attributeBindings) {
                if(this._model.has(attributeName)){
                    attributeBinding = this._attributeBindings[attributeName];
                    this._copyModelToView(attributeBinding);
                }
            }
        },

        _unbindModelToView: function(){
            if(this._model){
                this._model.off('change', this._onModelChange);
                this._model = undefined;
            }
        },

        // THINK HERE IS AN ISSUE

        _bindViewToModel:function () {
            $(this._rootEl).delegate('', 'change', this._onElChanged);
            // The change event doesn't work properly for contenteditable elements - but blur does
            $(this._rootEl).delegate('[contenteditable]', 'blur', this._onElChanged);
        },

        _unbindViewToModel: function(){
            if(this._rootEl){
                $(this._rootEl).undelegate('', 'change', this._onElChanged);
                $(this._rootEl).undelegate('[contenteditable]', 'blur', this._onElChanged);
            }
        },

        _onElChanged:function (event) {
            var el = $(event.target)[0];
            var elementBinding = this._getElBinding(el);
            if (elementBinding) {
                this._copyViewToModel(elementBinding, el);
            }
        },

        _getElBinding:function (findEl) {
            var attributeName, attributeBinding, elementBindingCount, elementBinding, boundElCount, boundEl;

            for (attributeName in this._attributeBindings) {
                attributeBinding = this._attributeBindings[attributeName];

                for (elementBindingCount = 0; elementBindingCount < attributeBinding.elementBindings.length; elementBindingCount++) {
                    elementBinding = attributeBinding.elementBindings[elementBindingCount];

                    for (boundElCount = 0; boundElCount < elementBinding.boundEls.length; boundElCount++) {
                        boundEl = elementBinding.boundEls[boundElCount];

                        if (boundEl === findEl) {
                            return elementBinding;
                        }
                    }
                }
            }

            return undefined;
        },

        _onModelChange:function () {
            var changedAttribute, attributeBinding;

            for (changedAttribute in this._model.changedAttributes()) {
                attributeBinding = this._attributeBindings[changedAttribute];
                if (attributeBinding) {
                    this._copyModelToView(attributeBinding);
                }
            }
        },

        _copyModelToView:function (attributeBinding) {
            var elementBindingCount, elementBinding, boundElCount, boundEl;
            var value = this._model.get(attributeBinding.attributeName);

            for (elementBindingCount = 0; elementBindingCount < attributeBinding.elementBindings.length; elementBindingCount++) {
                elementBinding = attributeBinding.elementBindings[elementBindingCount];

                var convertedValue = this._getConvertedValue(Backbone.ModelBinder.Constants.ModelToView, elementBinding, value);

                for (boundElCount = 0; boundElCount < elementBinding.boundEls.length; boundElCount++) {
                    boundEl = elementBinding.boundEls[boundElCount];
                    this._setEl($(boundEl), elementBinding, convertedValue);
                }
            }
        },

        _setEl: function (el, elementBinding, convertedValue) {
            if (elementBinding.elAttribute) {
                this._setElAttribute(el, elementBinding, convertedValue);
            }
            else {
                this._setElValue(el, convertedValue);
            }
        },

        _setElAttribute:function (el, elementBinding, convertedValue) {
            switch (elementBinding.elAttribute) {
                case 'html':
                    el.html(convertedValue);
                    break;
                case 'text':
                    el.text(convertedValue);
                    break;
                case 'enabled':
                    el.attr('disabled', !convertedValue);
                    break;
                case 'displayed':
                    el[convertedValue ? 'show' : 'hide']();
                    break;
                case 'hidden':
                    el[convertedValue ? 'hide' : 'show']();
                    break;
                case 'css':
                    el.css(elementBinding.cssAttribute, convertedValue);
                    break;
                case 'class':
                    if(!convertedValue){
                        var previousValue = this._model.previous(elementBinding.attributeBinding.attributeName);
                        previousValue = this._getConvertedValue(Backbone.ModelBinder.Constants.ModelToView, elementBinding, previousValue);

                        if(previousValue){
                            el.removeClass(previousValue);
                        }
                    }
                    else{
                        el.addClass(convertedValue);
                    }
                    break;
                default:
                    el.attr(elementBinding.elAttribute, convertedValue);
            }
        },

        _setElValue:function (el, convertedValue) {
            if(el.attr('type')){
                switch (el.attr('type')) {
                    case 'radio':
                        if (el.val() === convertedValue) {
                            el.attr('checked', 'checked');
                        }
                        break;
                    case 'checkbox':
                        if (convertedValue) {
                            el.attr('checked', 'checked');
                        }
                        else {
                            el.removeAttr('checked');
                        }
                        break;
                    default:
                        el.val(convertedValue);
                }
            }
            else if(el.is('input') || el.is('select') || el.is('textarea')){
                el.val(convertedValue);
            }
            else {
                el.text(convertedValue);
            }
        },

        _copyViewToModel: function (elementBinding, el) {
            if (!elementBinding.isSetting) {
                elementBinding.isSetting = true;
                this._setModel(elementBinding, $(el));

                if(elementBinding.converter){
                    this._copyModelToView(elementBinding.attributeBinding);
                }

                elementBinding.isSetting = false;
            }
        },

        _getElValue: function(elementBinding, el){
            switch (el.attr('type')) {
                case 'checkbox':
                    return el.prop('checked') ? true : false;
                default:
                    if(el.attr('contenteditable') !== undefined){
                        return el.html();
                    }
                    else {
                        return el.val();
                    }
            }
        },

        _setModel: function (elementBinding, el) {
            var data = {};
            var elVal = this._getElValue(elementBinding, el);
            elVal = this._getConvertedValue(Backbone.ModelBinder.Constants.ViewToModel, elementBinding, elVal);
            data[elementBinding.attributeBinding.attributeName] = elVal;
            this._model.set(data, {changeSource: 'ModelBinder'});
        },

        _getConvertedValue: function (direction, elementBinding, value) {
            if (elementBinding.converter) {
                value = elementBinding.converter(direction, value, elementBinding.attributeBinding.attributeName, this._model);
            }

            return value;
        }
    });

    Backbone.ModelBinder.CollectionConverter = function(collection){
        this._collection = collection;

        if(!this._collection){
            throw 'Collection must be defined';
        }
        _.bindAll(this, 'convert');
    };

    _.extend(Backbone.ModelBinder.CollectionConverter.prototype, {
        convert: function(direction, value){
            if (direction === Backbone.ModelBinder.Constants.ModelToView) {
                return value ? value.id : undefined;
            }
            else {
                return this._collection.get(value);
            }
        }
    });

    // A static helper function to create a default set of bindings that you can customize before calling the bind() function
    // rootEl - where to find all of the bound elements
    // attributeType - probably 'name' or 'id' in most cases
    // converter(optional) - the default converter you want applied to all your bindings
    // elAttribute(optional) - the default elAttribute you want applied to all your bindings
    Backbone.ModelBinder.createDefaultBindings = function(rootEl, attributeType, converter, elAttribute){
        var foundEls, elCount, foundEl, attributeName;
        var bindings = {};

        foundEls = $('[' + attributeType + ']', rootEl);

        for(elCount = 0; elCount < foundEls.length; elCount++){
            foundEl = foundEls[elCount];
            attributeName = $(foundEl).attr(attributeType);

            if(!bindings[attributeName]){
                var attributeBinding =  {selector: '[' + attributeType + '="' + attributeName + '"]'};
                bindings[attributeName] = attributeBinding;

                if(converter){
                    bindings[attributeName].converter = converter;
                }

                if(elAttribute){
                    bindings[attributeName].elAttribute = elAttribute;
                }
            }
        }

        return bindings;
    };

    // Helps you to combine 2 sets of bindings
    Backbone.ModelBinder.combineBindings = function(destination, source){
        _.each(source, function(value, key){
            var elementBinding = {selector: value.selector};

            if(value.converter){
                elementBinding.converter = value.converter;
            }

            if(value.elAttribute){
                elementBinding.elAttribute = value.elAttribute;
            }

            if(!destination[key]){
                destination[key] = elementBinding;
            }
            else {
                destination[key] = [destination[key], elementBinding];
            }
        });
    };


    return Backbone.ModelBinder;

}(this));
// Backbone.Validation v0.5.2
//
// Copyright (C)2011-2012 Thomas Pedersen
// Distributed under MIT License
//
// Documentation and full license available at:
// http://thedersen.github.com/backbone.validation

Backbone.Validation = (function(Backbone, _, undefined) {
    var defaultOptions = {
        forceUpdate: false,
        selector: 'name'
    };

    var getValidatedAttrs = function(model){
        return _.reduce(_.keys(model.validation), function(memo, key){
            memo[key] = undefined;
            return memo;
        }, {});
    };

    var getValidators = function(model, validation, attr) {
        var attrValidation = validation[attr] || {};

        if (_.isFunction(attrValidation)) {
            return attrValidation;
        } else if(_.isString(attrValidation)) {
            return model[attrValidation];
        } else if(!_.isArray(attrValidation)) {
            attrValidation = [attrValidation];
        }

        return _.reduce(attrValidation, function(memo, attrValidation){
            _.each(_.without(_.keys(attrValidation), 'msg'), function(validator){
                memo.push({
                    fn: Backbone.Validation.validators[validator],
                    val: attrValidation[validator],
                    msg: attrValidation.msg
                });
            });
            return memo;
        }, []);
    };

    var hasChildValidaton = function(validation, attr) {
        return _.isObject(validation) && _.isObject(validation[attr]) && _.isObject(validation[attr].validation);
    };

    var validateAttr = function(model, validation, attr, value, computed) {
        var validators = getValidators(model, validation, attr);

        if (_.isFunction(validators)) {
            return validators.call(model, value, attr, computed);
        }

        return _.reduce(validators, function(memo, validator){
            var result = validator.fn.call(Backbone.Validation.validators, value, attr, validator.val, model, computed);
            if(result === false || memo === false) {
                return false;
            }
            if (result && !memo) {
                return validator.msg || result;
            }
            return memo;
        }, '');
    };

    var validateAll = function(model, validation, attrs, computed, view, options) {
        if (!attrs) {
          return false;
        }
        var isValid = true, error;
        for (var validatedAttr in validation) {
            error = validateAttr(model, validation, validatedAttr, model.get(validatedAttr), computed);
            if (_.isUndefined(attrs[validatedAttr]) && error) {
                isValid = false;
                break;
            } else if(!error && view) {
                options.valid(view, validatedAttr, options.selector);
            }
            if (error !== false && hasChildValidaton(validation, validatedAttr)) {
                isValid = validateAll(model, validation[validatedAttr].validation, attrs[validatedAttr], computed);
            }
        }
        return isValid;
    };

    var validateObject = function(view, model, validation, attrs, options, attrPath) {
        attrPath = attrPath || '';
        var result, error, changedAttr,
            errorMessages = [],
            invalidAttrs = [],
            isValid = true,
            computed = _.extend(model.toJSON(), attrs);

        for (changedAttr in attrs) {
            error = validateAttr(model, validation, changedAttr, attrs[changedAttr], computed);
            if (error) {
                errorMessages.push(error);
                invalidAttrs.push(attrPath + changedAttr);
                isValid = false;
                if(view) options.invalid(view, changedAttr, error, options.selector);
            } else {
                if(view) options.valid(view, changedAttr, options.selector);
            }

            if (error !== false && hasChildValidaton(validation, changedAttr)) {

                result = validateObject(view, model, validation[changedAttr].validation, attrs[changedAttr], options, attrPath + changedAttr + '.');

                Array.prototype.push.apply(errorMessages, result.errorMessages);
                Array.prototype.push.apply(invalidAttrs, result.invalidAttrs);
                isValid = isValid && result.isValid;
            }
        }

        if (isValid) {
            isValid = validateAll(model, validation, attrs, computed, view, options);
        }

        return {
            errorMessages: errorMessages,
            invalidAttrs: invalidAttrs,
            isValid: isValid
        };
    };

    var mixin = function(view, options) {
        return {
            isValid: function(option) {
                if(_.isString(option)){
                    return !validateAttr(this, this.validation, option, this.get(option), this.toJSON());
                }
                if(_.isArray(option)){
                    for (var i = 0; i < option.length; i++) {
                        if(validateAttr(this, this.validation, option[i], this.get(option[i]), this.toJSON())){
                            return false;
                        }
                    }
                    return true;
                }
                if(option === true) {
                    this.validate();
                }
                return this.validation ? this._isValid : true;
            },
            validate: function(attrs, setOptions){
                var model = this,
                    opt = _.extend({}, options, setOptions);
                if(!attrs){
                    return model.validate.call(model, _.extend(getValidatedAttrs(model), model.toJSON()));
                }

                var result = validateObject(view, model, model.validation, attrs, opt);
                model._isValid = result.isValid;

                _.defer(function() {
                    model.trigger('validated', model._isValid, model, result.invalidAttrs);
                    model.trigger('validated:' + (model._isValid ? 'valid' : 'invalid'), model, result.invalidAttrs);
                });

                if (!opt.forceUpdate && result.errorMessages.length > 0) {
                    return result.errorMessages;
                }
            }
        };
    };

    var bindModel = function(view, model, options) {
        _.extend(model, mixin(view, options));
    };

    var unbindModel = function(model) {
        delete model.validate;
        delete model.isValid;
    };

    var collectonAdd = function(model) {
        bindModel(this.view, model, this.options);
    };

    var collectionRemove = function(model) {
        unbindModel(model);
    };

    return {
        version: '0.5.2',

        configure: function(options) {
            _.extend(defaultOptions, options);
        },

        bind: function(view, options) {
            var model = view.model,
                collection = view.collection,
                opt = _.extend({}, defaultOptions, Backbone.Validation.callbacks, options);

            if(model) {
                bindModel(view, model, opt);
            }
            if(collection) {
                collection.each(function(model){
                    bindModel(view, model, opt);
                });
                collection.bind('add', collectonAdd, {view: view, options: opt});
                collection.bind('remove', collectionRemove);
            }
        },

        unbind: function(view) {
            var model = view.model,
                collection = view.collection;
            if(model) {
                unbindModel(view.model);
            }
            if(collection) {
                collection.each(function(model){
                    unbindModel(model);
                });
                collection.unbind('add', collectonAdd);
                collection.unbind('remove', collectionRemove);
            }
        },

        mixin: mixin(null, defaultOptions)
    };
} (Backbone, _));

Backbone.Validation.callbacks = {
    valid: function(view, attr, selector) {
        view.$('[' + selector + '~=' + attr + ']')
            .removeClass('invalid')
            .removeAttr('data-error');
    },

    invalid: function(view, attr, error, selector) {
        view.$('[' + selector + '~=' + attr + ']')
            .addClass('invalid')
            .attr('data-error', error);
    }
};

Backbone.Validation.patterns = {
    digits: /^\d+$/,
    number: /^-?(?:\d+|\d{1,3}(?:,\d{3})+)(?:\.\d+)?$/,
    email: /^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))$/i,
    url: /^(https?|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i
};

Backbone.Validation.messages = {
    required: '{0} is required',
    acceptance: '{0} must be accepted',
    min: '{0} must be grater than or equal to {1}',
    max: '{0} must be less than or equal to {1}',
    range: '{0} must be between {1} and {2}',
    length: '{0} must be {1} characters',
    minLength: '{0} must be at least {1} characters',
    maxLength: '{0} must be at most {1} characters',
    rangeLength: '{0} must be between {1} and {2} characters',
    oneOf: '{0} must be one of: {1}',
    equalTo: '{0} must be the same as {1}',
    pattern: '{0} must be a valid {1}',
    object: '{0} must be an object'
};

Backbone.Validation.validators = (function(patterns, messages, _) {
    var trim = String.prototype.trim ?
                function(text) {
                    return text === null ? '' : String.prototype.trim.call(text);
                } :
                function(text) {
                    var trimLeft = /^\s+/,
                        trimRight = /\s+$/;

                    return text === null ? '' : text.toString().replace(trimLeft, '').replace(trimRight, '');
                };
    var format = function() {
        var args = Array.prototype.slice.call(arguments);
        var text = args.shift();
        return text.replace(/\{(\d+)\}/g, function(match, number) {
            return typeof args[number] != 'undefined' ? args[number] : match;
        });
    };
    var isNumber = function(value){
        return _.isNumber(value) || (_.isString(value) && value.match(patterns.number));
    };
    var hasValue = function(value) {
        return !(_.isNull(value) || _.isUndefined(value) || (_.isString(value) && trim(value) === ''));
    };

    return {
        fn: function(value, attr, fn, model, computed) {
            if(_.isString(fn)){
                fn = model[fn];
            }
            return fn.call(model, value, attr, computed);
        },
        required: function(value, attr, required, model) {
            var isRequired = _.isFunction(required) ? required.call(model) : required;
            if(!isRequired && !hasValue(value)) {
                return false; // overrides all other validators
            }
            if (isRequired && !hasValue(value)) {
                return format(messages.required, attr);
            }
        },
        acceptance: function(value, attr) {
            if(value !== 'true' && (!_.isBoolean(value) || value === false)) {
                return format(messages.acceptance, attr);
            }
        },
        min: function(value, attr, minValue) {
            if (!isNumber(value) || value < minValue) {
                return format(messages.min, attr, minValue);
            }
        },
        max: function(value, attr, maxValue) {
            if (!isNumber(value) || value > maxValue) {
                return format(messages.max, attr, maxValue);
            }
        },
        range: function(value, attr, range) {
            if(!isNumber(value) || value < range[0] || value > range[1]) {
                return format(messages.range, attr, range[0], range[1]);
            }
        },
        length: function(value, attr, length) {
            if (!hasValue(value) || trim(value).length !== length) {
                return format(messages.length, attr, length);
            }
        },
        minLength: function(value, attr, minLength) {
            if (!hasValue(value) || trim(value).length < minLength) {
                return format(messages.minLength, attr, minLength);
            }
        },
        maxLength: function(value, attr, maxLength) {
            if (!hasValue(value) || trim(value).length > maxLength) {
                return format(messages.maxLength, attr, maxLength);
            }
        },
        rangeLength: function(value, attr, range) {
            if(!hasValue(value) || trim(value).length < range[0] || trim(value).length > range[1]) {
                return format(messages.rangeLength, attr, range[0], range[1]);
            }
        },
        oneOf: function(value, attr, values) {
            if(!_.include(values, value)){
                return format(messages.oneOf, attr, values.join(', '));
            }
        },
        equalTo: function(value, attr, equalTo, model, computed) {
            if(value !== computed[equalTo]) {
                return format(messages.equalTo, attr, equalTo);
            }
        },
        pattern: function(value, attr, pattern) {
            if (!hasValue(value) || !value.toString().match(patterns[pattern] || pattern)) {
                return format(messages.pattern, attr, pattern);
            }
        },
        validation: function(value, attr, objectValue) {
            if (!_.isObject(value)) {
                return format(messages.object, attr);
            }
        }
    };
} (Backbone.Validation.patterns, Backbone.Validation.messages, _));
_.extend(Backbone.Validation.callbacks, {
    valid: function (view, attr, selector) {
        view.$('[' + selector + '~=' + attr + ']').closest('.control-group').removeClass('error');
        view.$('[' + selector + '~=' + attr + ']').closest('.control-group').find('.errortext').remove();

    },
    invalid: function (view, attr, error, selector) {
        view.$('[' + selector + '~=' + attr + ']').closest('.control-group').addClass('error');

        //clean up first
        view.$('[' + selector + '~=' + attr + ']').closest('.control-group').find('.errortext').remove();
        view.$('[' + selector + '~=' + attr + ']').closest('.control-group').append(
            $('<span>')
                .addClass('help-inline errortext')
                .text(error)
        );
    }
});

_.extend(Backbone.Validation.validators, {
    greaterThan: function (value, attr, greaterThan, model, computed) {
        if (value < computed[greaterThan]) {
            return 'error';
        }
    },
    smallerThan: function (value, attr, smallerThan, model, computed) {
        if (value > computed[smallerThan]) {
            return 'error';
        }
    }
});
// doT.js
// 2011, Laura Doktorova, https://github.com/olado/doT
//
// doT.js is an open source component of http://bebedo.com
// Licensed under the MIT license.
//
(function() {
	"use strict";

	var doT = {
		version: '0.2.0',
		templateSettings: {
			evaluate:    /\{\{([\s\S]+?)\}\}/g,
			interpolate: /\{\{=([\s\S]+?)\}\}/g,
			encode:      /\{\{!([\s\S]+?)\}\}/g,
			use:         /\{\{#([\s\S]+?)\}\}/g,
			define:      /\{\{##\s*([\w\.$]+)\s*(\:|=)([\s\S]+?)#\}\}/g,
			conditional: /\{\{\?(\?)?\s*([\s\S]*?)\s*\}\}/g,
			iterate:     /\{\{~\s*(?:\}\}|([\s\S]+?)\s*\:\s*([\w$]+)\s*(?:\:\s*([\w$]+))?\s*\}\})/g,
			varname: 'it',
			strip: true,
			append: true,
			selfcontained: false
		},
		template: undefined, //fn, compile template
		compile:  undefined  //fn, for express
	};

	var global = (function(){ return this || (0,eval)('this'); }());

	if (typeof module !== 'undefined' && module.exports) {
		module.exports = doT;
	} else if (typeof define === 'function' && define.amd  && false /*just disable it*/) {
		define(function(){return doT;});
	} else {
		global.doT = doT;
	}

	function encodeHTMLSource() {
		var encodeHTMLRules = { "&": "&#38;", "<": "&#60;", ">": "&#62;", '"': '&#34;', "'": '&#39;', "/": '&#47;' },
			matchHTML = /&(?!\\w+;)|<|>|"|'|\//g;
		return function(code) {
			return code ? code.toString().replace(matchHTML, function(m) {return encodeHTMLRules[m] || m;}) : code;
		};
	}
	global.encodeHTML = encodeHTMLSource();

	var startend = {
		append: { start: "'+(",      end: ")+'",      startencode: "'+encodeHTML(" },
		split:  { start: "';out+=(", end: ");out+='", startencode: "';out+=encodeHTML("}
	}, skip = /$^/;

	function resolveDefs(c, block, def) {
		return ((typeof block === 'string') ? block : block.toString())
		.replace(c.define || skip, function(m, code, assign, value) {
			if (code.indexOf('def.') === 0) {
				code = code.substring(4);
			}
			if (!(code in def)) {
				if (assign === ':') {
					def[code]= value;
				} else {
					eval("def['"+code+"']=" + value);
				}
			}
			return '';
		})
		.replace(c.use || skip, function(m, code) {
			var v = eval(code);
			return v ? resolveDefs(c, v, def) : v;
		});
	}

	function unescape(code) {
		return code.replace(/\\('|\\)/g, "$1").replace(/[\r\t\n]/g, ' ');
	}

	doT.template = function(tmpl, c, def) {
		c = c || doT.templateSettings;
		var cse = c.append ? startend.append : startend.split, str, needhtmlencode, sid=0, indv;

		if (c.use || c.define) {
			var olddef = global.def; global.def = def || {}; // workaround minifiers
			str = resolveDefs(c, tmpl, global.def);
			global.def = olddef;
		} else str = tmpl;

		str = ("var out='" + (c.strip ? str.replace(/(^|\r|\n)\t* +| +\t*(\r|\n|$)/g,' ')
					.replace(/\r|\n|\t|\/\*[\s\S]*?\*\//g,''): str)
			.replace(/'|\\/g, '\\$&')
			.replace(c.interpolate || skip, function(m, code) {
				return cse.start + unescape(code) + cse.end;
			})
			.replace(c.encode || skip, function(m, code) {
				needhtmlencode = true;
				return cse.startencode + unescape(code) + cse.end;
			})
			.replace(c.conditional || skip, function(m, elsecase, code) {
				return elsecase ?
					(code ? "';}else if(" + unescape(code) + "){out+='" : "';}else{out+='") :
					(code ? "';if(" + unescape(code) + "){out+='" : "';}out+='");
			})
			.replace(c.iterate || skip, function(m, iterate, vname, iname) {
				if (!iterate) return "';} } out+='";
				sid+=1; indv=iname || "i"+sid; iterate=unescape(iterate);
				return "';var arr"+sid+"="+iterate+";if(arr"+sid+"){var "+indv+"=-1,l"+sid+"=arr"+sid+".length-1;while("+indv+"<l"+sid+"){"
					+vname+"=arr"+sid+"["+indv+"+=1];out+='";
			})
			.replace(c.evaluate || skip, function(m, code) {
				return "';" + unescape(code) + "out+='";
			})
			+ "';return out;")
			.replace(/\n/g, '\\n').replace(/\t/g, '\\t').replace(/\r/g, '\\r')
			.replace(/(\s|;|}|^|{)out\+='';/g, '$1').replace(/\+''/g, '')
			.replace(/(\s|;|}|^|{)out\+=''\+/g,'$1out+=');

		if (needhtmlencode && c.selfcontained) {
			str = "var encodeHTML=(" + encodeHTMLSource.toString() + "());" + str;
		}
		try {
			return new Function(c.varname, str);
		} catch (e) {
			if (typeof console !== 'undefined') console.log("Could not create a template function: " + str);
			throw e;
		}
	};

	doT.compile = function(tmpl, def) {
		return doT.template(tmpl, null, def);
	};
}());
(function(){function f(a){return a.replace(/(?:^|(\/))[^\/]+$/,"$1")}function g(a,b){return b.replace(/url\((?!\/|[A-Za-z][A-Za-z0-9+.-]*\:)/g,"url("+a)}function h(a,b,c){return $('<style type="text/css">'+g(f(a),b)+"</style>").attr("data-require-src",a).insertBefore($(c).first())}function i(b){return a=b,$.when.apply($,_.map(c,function(a){return e(a.source).done(function(b){a.node?a.node.text(g(a.path,b)):a.node=h(a.name,b,"script")})}))}var a,b,c,d,e;define("text",{load:function(a,b,c,d){$.ajax
({url:d.baseUrl+a,dataType:"text"}).done(c)}}),define("css",{load:function(a,b,c,d){require(["text!"+a]).done(function(b){c(h(d.baseUrl+a,b,"title"))})}}),a="",b={},c=[b],e=function(){function d(a){return b[a.split("/")[1]]}var b={tree:{}},c=b;return function(){var a,b,e;typeof environment=="object"&&{}.toString.call(environment)==="[object Environment]"?(typeof a=="undefined"?b={}:b=a.less={},e=b.tree={},b.mode="rhino"):typeof a=="undefined"?(b=c,e=d("./tree"),b.mode="node"):(typeof a.less=="undefined"&&
(a.less={}),b=a.less,e=a.less.tree={},b.mode="browser"),b.Parser=function(c){var f,g,h,i,j,k,l,m,n,o=this,p=function(){},q=this.imports={paths:c&&c.paths||[],queue:[],files:{},contents:{},mime:c&&c.mime,error:null,push:function(a,d){var e=this;this.queue.push(a),b.Parser.importer(a,this.paths,function(b,c,f){e.queue.splice(e.queue.indexOf(a),1);var g=a in e.files;e.files[a]=c,e.contents[a]=f,b&&!e.error&&(e.error=b),d(b,c,g),e.queue.length===0&&p()},c)}};function r(){i=l[h],j=g,m=g}function s(){l
[h]=i,g=j,m=g}function t(){g>m&&(l[h]=l[h].slice(g-m),m=g)}function u(a){var b,c,d,e,i,j,k,o;if(a instanceof Function)return a.call(n.parsers);else if(typeof a=="string")b=f.charAt(g)===a?a:null,d=1,t();else{t();if(b=a.exec(l[h]))d=b[0].length;else return null}if(b){o=g+=d,j=g+l[h].length-d;while(g<j){e=f.charCodeAt(g);if(e!==32&&e!==10&&e!==9)break;g++}l[h]=l[h].slice(d+(g-o)),m=g,l[h].length===0&&h<l.length-1&&h++;if(typeof b=="string")return b;else return b.length===1?b[0]:b}}function v(a,b){var c=
u(a);if(!c)w(b||(typeof a=="string"?"expected '"+a+"' got '"+f.charAt(g)+"'":"unexpected token"));else return c}function w(a,b){throw{index:g,type:b||"Syntax",message:a}}function x(a){if(typeof a=="string")return f.charAt(g)===a;else if(a.test(l[h]))return!0;else return!1}function y(a){if(b.mode==="node")return d("path").basename(a);else return a.match(/[^\/]+$/)[0]}function z(a,b){if(a.filename&&b.filename&&a.filename!==b.filename)return n.imports.contents[y(a.filename)];else return f}function A
(a,b){for(var c=a,d=-1;c>=0&&b.charAt(c)!=="\n";c--)d++;return{line:typeof a=="number"?(b.slice(0,a).match(/\n/g)||"").length:null,column:d}}function B(a,b){var c=z(a,b),d=A(a.index,c),e=d.line,f=d.column,g=c.split("\n");this.type=a.type||"Syntax",this.message=a.message,this.filename=a.filename||b.filename,this.index=a.index,this.line=typeof e=="number"?e+1:null,this.callLine=a.call&&A(a.call,c).line+1,this.callExtract=g[A(a.call,c).line],this.stack=a.stack,this.column=f,this.extract=[g[e-1],g[e]
,g[e+1]]}return this.env=c=c||{},this.optimization="optimization"in this.env?this.env.optimization:1,this.env.filename=this.env.filename||null,n={imports:q,parse:function(a,i){var j,o,q,r,s,t,v=[],w,x=null;g=h=m=k=0,f=a.replace(/\r\n/g,"\n"),l=function(a){var b=0,d=/[^"'`\{\}\/\(\)\\]+/g,e=/\/\*(?:[^*]|\*+[^\/*])*\*+\/|\/\/.*/g,g=/"((?:[^"\\\r\n]|\\.)*)"|'((?:[^'\\\r\n]|\\.)*)'|`((?:[^`\\\r\n]|\\.)*)`/g,h=0,i,j=a[0],k;for(var l=0,m,n;l<f.length;l++){d.lastIndex=l,(i=d.exec(f))&&i.index===l&&(l+=i
[0].length,j.push(i[0])),m=f.charAt(l),e.lastIndex=g.lastIndex=l,(i=g.exec(f))&&i.index===l&&(l+=i[0].length,j.push(i[0]),m=f.charAt(l)),!k&&m==="/"&&(n=f.charAt(l+1),(n==="/"||n==="*")&&(i=e.exec(f))&&i.index===l&&(l+=i[0].length,j.push(i[0]),m=f.charAt(l)));switch(m){case"{":if(!k){h++,j.push(m);break};case"}":if(!k){h--,j.push(m),a[++b]=j=[];break};case"(":if(!k){k=!0,j.push(m);break};case")":if(k){k=!1,j.push(m);break};default:j.push(m)}}return h>0&&(x=new B({index:l,type:"Parse",message:"missing closing `}`"
,filename:c.filename},c)),a.map(function(a){return a.join("")})}([[]]);if(x)return i(x);try{j=new e.Ruleset([],u(this.parsers.primary)),j.root=!0}catch(y){return i(new B(y,c))}j.toCSS=function(a){var f,g,h;return function(f,g){var h=[],i;f=f||{},typeof g=="object"&&!Array.isArray(g)&&(g=Object.keys(g).map(function(a){var b=g[a];return b instanceof e.Value||(b instanceof e.Expression||(b=new e.Expression([b])),b=new e.Value([b])),new e.Rule("@"+a,b,!1,0)}),h=[new e.Ruleset(null,g)]);try{var j=a.call
(this,{frames:h}).toCSS([],{compress:f.compress||!1})}catch(k){throw new B(k,c)}if(i=n.imports.error)if(i instanceof B)throw i;else throw new B(i,c);if(f.yuicompress&&b.mode==="node")return d("./cssmin").compressor.cssmin(j);else if(f.compress)return j.replace(/(\s)+/g,"$1");else return j}}(j.eval);if(g<f.length-1){g=k,t=f.split("\n"),s=(f.slice(0,g).match(/\n/g)||"").length+1;for(var z=g,A=-1;z>=0&&f.charAt(z)!=="\n";z--)A++;x={type:"Parse",message:"Syntax Error on line "+s,index:g,filename:c.filename
,line:s,column:A,extract:[t[s-2],t[s-1],t[s]]}}this.imports.queue.length>0?p=function(){i(x,j)}:i(x,j)},parsers:{primary:function(){var a,b=[];while((a=u(this.mixin.definition)||u(this.rule)||u(this.ruleset)||u(this.mixin.call)||u(this.comment)||u(this.directive))||u(/^[\s\n]+/))a&&b.push(a);return b},comment:function(){var a;if(f.charAt(g)!=="/")return;if(f.charAt(g+1)==="/")return new e.Comment(u(/^\/\/.*/),!0);else if(a=u(/^\/\*(?:[^*]|\*+[^\/*])*\*+\/\n?/))return new e.Comment(a)},entities:{quoted
:function(){var a,b=g,c;f.charAt(b)==="~"&&(b++,c=!0);if(f.charAt(b)!=='"'&&f.charAt(b)!=="'")return;c&&u("~");if(a=u(/^"((?:[^"\\\r\n]|\\.)*)"|'((?:[^'\\\r\n]|\\.)*)'/))return new e.Quoted(a[0],a[1]||a[2],c)},keyword:function(){var a;if(a=u(/^[_A-Za-z-][_A-Za-z0-9-]*/))if(e.colors.hasOwnProperty(a))return new e.Color(e.colors[a].slice(1));else return new e.Keyword(a)},call:function(){var a,b,d=g;if(!(a=/^([\w-]+|%|progid:[\w\.]+)\(/.exec(l[h])))return;a=a[1].toLowerCase();if(a==="url")return null
;else g+=a.length;if(a==="alpha")return u(this.alpha);u("("),b=u(this.entities.arguments);if(!u(")"))return;if(a)return new e.Call(a,b,d,c.filename)},arguments:function(){var a=[],b;while(b=u(this.entities.assignment)||u(this.expression)){a.push(b);if(!u(","))break}return a},literal:function(){return u(this.entities.dimension)||u(this.entities.color)||u(this.entities.quoted)},assignment:function(){var a,b;if((a=u(/^\w+(?=\s?=)/i))&&u("=")&&(b=u(this.entity)))return new e.Assignment(a,b)},url:function(
){var a;if(f.charAt(g)!=="u"||!u(/^url\(/))return;return a=u(this.entities.quoted)||u(this.entities.variable)||u(this.entities.dataURI)||u(/^[-\w%@$\/.&=:;#+?~]+/)||"",v(")"),new e.URL(a.value||a.data||a instanceof e.Variable?a:new e.Anonymous(a),q.paths)},dataURI:function(){var a;if(u(/^data:/)){a={},a.mime=u(/^[^\/]+\/[^,;)]+/)||"",a.charset=u(/^;\s*charset=[^,;)]+/)||"",a.base64=u(/^;\s*base64/)||"",a.data=u(/^,\s*[^)]+/);if(a.data)return a}},variable:function(){var a,b=g;if(f.charAt(g)==="@"&&
(a=u(/^@@?[\w-]+/)))return new e.Variable(a,b,c.filename)},color:function(){var a;if(f.charAt(g)==="#"&&(a=u(/^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})/)))return new e.Color(a[1])},dimension:function(){var a,b=f.charCodeAt(g);if(b>57||b<45||b===47)return;if(a=u(/^(-?\d*\.?\d+)(px|%|em|rem|pc|ex|in|deg|s|ms|pt|cm|mm|rad|grad|turn)?/))return new e.Dimension(a[1],a[2])},javascript:function(){var a,b=g,c;f.charAt(b)==="~"&&(b++,c=!0);if(f.charAt(b)!=="`")return;c&&u("~");if(a=u(/^`([^`]*)`/))return new e.JavaScript
(a[1],g,c)}},variable:function(){var a;if(f.charAt(g)==="@"&&(a=u(/^(@[\w-]+)\s*:/)))return a[1]},shorthand:function(){var a,b;if(!x(/^[@\w.%-]+\/[@\w.-]+/))return;if((a=u(this.entity))&&u("/")&&(b=u(this.entity)))return new e.Shorthand(a,b)},mixin:{call:function(){var a=[],b,d,h,i=g,j=f.charAt(g),k=!1;if(j!=="."&&j!=="#")return;while(b=u(/^[#.](?:[\w-]|\\(?:[a-fA-F0-9]{1,6} ?|[^a-fA-F0-9]))+/))a.push(new e.Element(d,b,g)),d=u(">");u("(")&&(h=u(this.entities.arguments))&&u(")"),u(this.important)&&
(k=!0);if(a.length>0&&(u(";")||x("}")))return new e.mixin.Call(a,h||[],i,c.filename,k)},definition:function(){var a,b=[],c,d,h,i,j,k=!1;if(f.charAt(g)!=="."&&f.charAt(g)!=="#"||x(/^[^{]*(;|})/))return;r();if(c=u(/^([#.](?:[\w-]|\\(?:[a-fA-F0-9]{1,6} ?|[^a-fA-F0-9]))+)\s*\(/)){a=c[1];do if(f.charAt(g)==="."&&u(/^\.{3}/)){k=!0;break}else if(h=u(this.entities.variable)||u(this.entities.literal)||u(this.entities.keyword))if(h instanceof e.Variable)if(u(":"))i=v(this.expression,"expected expression"),
b.push({name:h.name,value:i});else if(u(/^\.{3}/)){b.push({name:h.name,variadic:!0}),k=!0;break}else b.push({name:h.name});else b.push({value:h});else break;while(u(","));v(")"),u(/^when/)&&(j=v(this.conditions,"expected condition")),d=u(this.block);if(d)return new e.mixin.Definition(a,b,d,j,k);else s()}}},entity:function(){return u(this.entities.literal)||u(this.entities.variable)||u(this.entities.url)||u(this.entities.call)||u(this.entities.keyword)||u(this.entities.javascript)||u(this.comment)
},end:function(){return u(";")||x("}")},alpha:function(){var a;if(!u(/^\(opacity=/i))return;if(a=u(/^\d+/)||u(this.entities.variable))return v(")"),new e.Alpha(a)},element:function(){var a,b,c,d;c=u(this.combinator),a=u(/^(?:\d+\.\d+|\d+)%/)||u(/^(?:[.#]?|:*)(?:[\w-]|\\(?:[a-fA-F0-9]{1,6} ?|[^a-fA-F0-9]))+/)||u("*")||u(this.attribute)||u(/^\([^)@]+\)/),a||u("(")&&(d=u(this.entities.variable))&&u(")")&&(a=new e.Paren(d));if(a)return new e.Element(c,a,g);if(c.value&&c.value.charAt(0)==="&")return new 
e.Element(c,null,g)},combinator:function(){var a,b=f.charAt(g);if(b===">"||b==="+"||b==="~"){g++;while(f.charAt(g)===" ")g++;return new e.Combinator(b)}else if(b==="&"){a="&",g++,f.charAt(g)===" "&&(a="& ");while(f.charAt(g)===" ")g++;return new e.Combinator(a)}else if(f.charAt(g-1)===" ")return new e.Combinator(" ");else return new e.Combinator(null)},selector:function(){var a,b,c=[],d,h;if(u("("))return a=u(this.entity),v(")"),new e.Selector([new e.Element("",a,g)]);while(b=u(this.element)){d=f
.charAt(g),c.push(b);if(d==="{"||d==="}"||d===";"||d===",")break}if(c.length>0)return new e.Selector(c)},tag:function(){return u(/^[a-zA-Z][a-zA-Z-]*[0-9]?/)||u("*")},attribute:function(){var a="",b,c,d;if(!u("["))return;if(b=u(/^[a-zA-Z-]+/)||u(this.entities.quoted))(d=u(/^[|~*$^]?=/))&&(c=u(this.entities.quoted)||u(/^[\w-]+/))?a=[b,d,c.toCSS?c.toCSS():c].join(""):a=b;if(!u("]"))return;if(a)return"["+a+"]"},block:function(){var a;if(u("{")&&(a=u(this.primary))&&u("}"))return a},ruleset:function(
){var a=[],b,d,f;r();while(b=u(this.selector)){a.push(b),u(this.comment);if(!u(","))break;u(this.comment)}if(a.length>0&&(d=u(this.block)))return new e.Ruleset(a,d,c.strictImports);else k=g,s()},rule:function(){var a,b,c=f.charAt(g),d,i;r();if(c==="."||c==="#"||c==="&")return;if(a=u(this.variable)||u(this.property)){a.charAt(0)!="@"&&(i=/^([^@+\/'"*`(;{}-]*);/.exec(l[h]))?(g+=i[0].length-1,b=new e.Anonymous(i[1])):a==="font"?b=u(this.font):b=u(this.value),d=u(this.important);if(b&&u(this.end))return new 
e.Rule(a,b,d,j);else k=g,s()}},"import":function(){var a,b,c=g,d=u(/^@import(?:-(once))?\s+/);if(d&&(a=u(this.entities.quoted)||u(this.entities.url))){b=u(this.mediaFeatures);if(u(";"))return new e.Import(a,q,b,d[1]==="once",c)}},mediaFeature:function(){var a,b,c=[];do if(a=u(this.entities.keyword))c.push(a);else if(u("(")){b=u(this.property),a=u(this.entity);if(u(")"))if(b&&a)c.push(new e.Paren(new e.Rule(b,a,null,g,!0)));else if(a)c.push(new e.Paren(a));else return null;else return null}while(a
);if(c.length>0)return new e.Expression(c)},mediaFeatures:function(){var a,b=[];do if(a=u(this.mediaFeature)){b.push(a);if(!u(","))break}else if(a=u(this.entities.variable)){b.push(a);if(!u(","))break}while(a);return b.length>0?b:null},media:function(){var a,b;if(u(/^@media/)){a=u(this.mediaFeatures);if(b=u(this.block))return new e.Media(b,a)}},directive:function(){var a,b,c,d,h,i;if(f.charAt(g)!=="@")return;if(b=u(this["import"])||u(this.media))return b;else if(a=u(/^@page|@keyframes/)||u(/^@(?:-webkit-|-moz-|-o-|-ms-)[a-z0-9-]+/
)){d=(u(/^[^{]+/)||"").trim();if(c=u(this.block))return new e.Directive(a+" "+d,c)}else if(a=u(/^@[-a-z]+/))if(a==="@font-face"){if(c=u(this.block))return new e.Directive(a,c)}else if((b=u(this.entity))&&u(";"))return new e.Directive(a,b)},font:function(){var a=[],b=[],c,d,f,g;while(g=u(this.shorthand)||u(this.entity))b.push(g);a.push(new e.Expression(b));if(u(","))while(g=u(this.expression)){a.push(g);if(!u(","))break}return new e.Value(a)},value:function(){var a,b=[],c;while(a=u(this.expression
)){b.push(a);if(!u(","))break}if(b.length>0)return new e.Value(b)},important:function(){if(f.charAt(g)==="!")return u(/^! *important/)},sub:function(){var a;if(u("(")&&(a=u(this.expression))&&u(")"))return a},multiplication:function(){var a,b,c,d;if(a=u(this.operand)){while(!x(/^\/\*/)&&(c=u("/")||u("*"))&&(b=u(this.operand)))d=new e.Operation(c,[d||a,b]);return d||a}},addition:function(){var a,b,c,d;if(a=u(this.multiplication)){while((c=u(/^[-+]\s+/)||f.charAt(g-1)!=" "&&(u("+")||u("-")))&&(b=u(
this.multiplication)))d=new e.Operation(c,[d||a,b]);return d||a}},conditions:function(){var a,b,c=g,d;if(a=u(this.condition)){while(u(",")&&(b=u(this.condition)))d=new e.Condition("or",d||a,b,c);return d||a}},condition:function(){var a,b,c,d,f=g,h=!1;u(/^not/)&&(h=!0),v("(");if(a=u(this.addition)||u(this.entities.keyword)||u(this.entities.quoted))return(d=u(/^(?:>=|=<|[<=>])/))?(b=u(this.addition)||u(this.entities.keyword)||u(this.entities.quoted))?c=new e.Condition(d,a,b,f,h):w("expected expression"
):c=new e.Condition("=",a,new e.Keyword("true"),f,h),v(")"),u(/^and/)?new e.Condition("and",c,u(this.condition)):c},operand:function(){var a,b=f.charAt(g+1);f.charAt(g)==="-"&&(b==="@"||b==="(")&&(a=u("-"));var c=u(this.sub)||u(this.entities.dimension)||u(this.entities.color)||u(this.entities.variable)||u(this.entities.call);return a?new e.Operation("*",[new e.Dimension(-1),c]):c},expression:function(){var a,b,c=[],d;while(a=u(this.addition)||u(this.entity))c.push(a);if(c.length>0)return new e.Expression
(c)},property:function(){var a;if(a=u(/^(\*?-?[-a-z_0-9]+)\s*:/))return a[1]}}}};if(b.mode==="browser"||b.mode==="rhino")b.Parser.importer=function(a,b,c,d){!/^([a-z]+:)?\//.test(a)&&b.length>0&&(a=b[0]+a),loadStyleSheet({href:a,title:a,type:d.mime},function(e){e&&typeof d.errback=="function"?d.errback.call(null,a,b,c,d):c.apply(null,arguments)},!0)}}(),function(a){a.find=function(a,b){for(var c=0,d;c<a.length;c++)if(d=b.call(a,a[c]))return d;return null},a.jsify=function(a){if(Array.isArray(a.value
)&&a.value.length>1)return"["+a.value.map(function(a){return a.toCSS(!1)}).join(", ")+"]";else return a.toCSS(!1)}}(d("./tree")),function(a){a.colors={aliceblue:"#f0f8ff",antiquewhite:"#faebd7",aqua:"#00ffff",aquamarine:"#7fffd4",azure:"#f0ffff",beige:"#f5f5dc",bisque:"#ffe4c4",black:"#000000",blanchedalmond:"#ffebcd",blue:"#0000ff",blueviolet:"#8a2be2",brown:"#a52a2a",burlywood:"#deb887",cadetblue:"#5f9ea0",chartreuse:"#7fff00",chocolate:"#d2691e",coral:"#ff7f50",cornflowerblue:"#6495ed",cornsilk
:"#fff8dc",crimson:"#dc143c",cyan:"#00ffff",darkblue:"#00008b",darkcyan:"#008b8b",darkgoldenrod:"#b8860b",darkgray:"#a9a9a9",darkgrey:"#a9a9a9",darkgreen:"#006400",darkkhaki:"#bdb76b",darkmagenta:"#8b008b",darkolivegreen:"#556b2f",darkorange:"#ff8c00",darkorchid:"#9932cc",darkred:"#8b0000",darksalmon:"#e9967a",darkseagreen:"#8fbc8f",darkslateblue:"#483d8b",darkslategray:"#2f4f4f",darkslategrey:"#2f4f4f",darkturquoise:"#00ced1",darkviolet:"#9400d3",deeppink:"#ff1493",deepskyblue:"#00bfff",dimgray:"#696969"
,dimgrey:"#696969",dodgerblue:"#1e90ff",firebrick:"#b22222",floralwhite:"#fffaf0",forestgreen:"#228b22",fuchsia:"#ff00ff",gainsboro:"#dcdcdc",ghostwhite:"#f8f8ff",gold:"#ffd700",goldenrod:"#daa520",gray:"#808080",grey:"#808080",green:"#008000",greenyellow:"#adff2f",honeydew:"#f0fff0",hotpink:"#ff69b4",indianred:"#cd5c5c",indigo:"#4b0082",ivory:"#fffff0",khaki:"#f0e68c",lavender:"#e6e6fa",lavenderblush:"#fff0f5",lawngreen:"#7cfc00",lemonchiffon:"#fffacd",lightblue:"#add8e6",lightcoral:"#f08080",lightcyan
:"#e0ffff",lightgoldenrodyellow:"#fafad2",lightgray:"#d3d3d3",lightgrey:"#d3d3d3",lightgreen:"#90ee90",lightpink:"#ffb6c1",lightsalmon:"#ffa07a",lightseagreen:"#20b2aa",lightskyblue:"#87cefa",lightslategray:"#778899",lightslategrey:"#778899",lightsteelblue:"#b0c4de",lightyellow:"#ffffe0",lime:"#00ff00",limegreen:"#32cd32",linen:"#faf0e6",magenta:"#ff00ff",maroon:"#800000",mediumaquamarine:"#66cdaa",mediumblue:"#0000cd",mediumorchid:"#ba55d3",mediumpurple:"#9370d8",mediumseagreen:"#3cb371",mediumslateblue
:"#7b68ee",mediumspringgreen:"#00fa9a",mediumturquoise:"#48d1cc",mediumvioletred:"#c71585",midnightblue:"#191970",mintcream:"#f5fffa",mistyrose:"#ffe4e1",moccasin:"#ffe4b5",navajowhite:"#ffdead",navy:"#000080",oldlace:"#fdf5e6",olive:"#808000",olivedrab:"#6b8e23",orange:"#ffa500",orangered:"#ff4500",orchid:"#da70d6",palegoldenrod:"#eee8aa",palegreen:"#98fb98",paleturquoise:"#afeeee",palevioletred:"#d87093",papayawhip:"#ffefd5",peachpuff:"#ffdab9",peru:"#cd853f",pink:"#ffc0cb",plum:"#dda0dd",powderblue
:"#b0e0e6",purple:"#800080",red:"#ff0000",rosybrown:"#bc8f8f",royalblue:"#4169e1",saddlebrown:"#8b4513",salmon:"#fa8072",sandybrown:"#f4a460",seagreen:"#2e8b57",seashell:"#fff5ee",sienna:"#a0522d",silver:"#c0c0c0",skyblue:"#87ceeb",slateblue:"#6a5acd",slategray:"#708090",slategrey:"#708090",snow:"#fffafa",springgreen:"#00ff7f",steelblue:"#4682b4",tan:"#d2b48c",teal:"#008080",thistle:"#d8bfd8",tomato:"#ff6347",turquoise:"#40e0d0",violet:"#ee82ee",wheat:"#f5deb3",white:"#ffffff",whitesmoke:"#f5f5f5"
,yellow:"#ffff00",yellowgreen:"#9acd32"}}(d("./tree")),function(a){a.functions={rgb:function(a,b,c){return this.rgba(a,b,c,1)},rgba:function(b,d,e,f){var g=[b,d,e].map(function(a){return c(a)}),f=c(f);return new a.Color(g,f)},hsl:function(a,b,c){return this.hsla(a,b,c,1)},hsla:function(a,b,d,e){a=c(a)%360/360,b=c(b),d=c(d),e=c(e);var f=d<=.5?d*(b+1):d+b-d*b,g=d*2-f;return this.rgba(h(a+1/3)*255,h(a)*255,h(a-1/3)*255,e);function h(a){a=a<0?a+1:a>1?a-1:a;if(a*6<1)return g+(f-g)*a*6;else if(a*2<1)return f
;else if(a*3<2)return g+(f-g)*(2/3-a)*6;else return g}},hue:function(b){return new a.Dimension(Math.round(b.toHSL().h))},saturation:function(b){return new a.Dimension(Math.round(b.toHSL().s*100),"%")},lightness:function(b){return new a.Dimension(Math.round(b.toHSL().l*100),"%")},alpha:function(b){return new a.Dimension(b.toHSL().a)},saturate:function(a,c){var e=a.toHSL();return e.s+=c.value/100,e.s=d(e.s),b(e)},desaturate:function(a,c){var e=a.toHSL();return e.s-=c.value/100,e.s=d(e.s),b(e)},lighten
:function(a,c){var e=a.toHSL();return e.l+=c.value/100,e.l=d(e.l),b(e)},darken:function(a,c){var e=a.toHSL();return e.l-=c.value/100,e.l=d(e.l),b(e)},fadein:function(a,c){var e=a.toHSL();return e.a+=c.value/100,e.a=d(e.a),b(e)},fadeout:function(a,c){var e=a.toHSL();return e.a-=c.value/100,e.a=d(e.a),b(e)},fade:function(a,c){var e=a.toHSL();return e.a=c.value/100,e.a=d(e.a),b(e)},spin:function(a,c){var d=a.toHSL(),e=(d.h+c.value)%360;return d.h=e<0?360+e:e,b(d)},mix:function(b,c,d){var e=d.value/100
,f=e*2-1,g=b.toHSL().a-c.toHSL().a,h=((f*g==-1?f:(f+g)/(1+f*g))+1)/2,i=1-h,j=[b.rgb[0]*h+c.rgb[0]*i,b.rgb[1]*h+c.rgb[1]*i,b.rgb[2]*h+c.rgb[2]*i],k=b.alpha*e+c.alpha*(1-e);return new a.Color(j,k)},greyscale:function(b){return this.desaturate(b,new a.Dimension(100))},e:function(b){return new a.Anonymous(b instanceof a.JavaScript?b.evaluated:b)},escape:function(b){return new a.Anonymous(encodeURI(b.value).replace(/=/g,"%3D").replace(/:/g,"%3A").replace(/#/g,"%23").replace(/;/g,"%3B").replace(/\(/g,"%28"
).replace(/\)/g,"%29"))},"%":function(b){var c=Array.prototype.slice.call(arguments,1),d=b.value;for(var e=0;e<c.length;e++)d=d.replace(/%[sda]/i,function(a){var b=a.match(/s/i)?c[e].value:c[e].toCSS();return a.match(/[A-Z]$/)?encodeURIComponent(b):b});return d=d.replace(/%%/g,"%"),new a.Quoted('"'+d+'"',d)},round:function(a){return this._math("round",a)},ceil:function(a){return this._math("ceil",a)},floor:function(a){return this._math("floor",a)},_math:function(b,d){if(d instanceof a.Dimension)return new 
a.Dimension(Math[b](c(d)),d.unit);else if(typeof d=="number")return Math[b](d);else throw{type:"Argument",message:"argument must be a number"}},argb:function(b){return new a.Anonymous(b.toARGB())},percentage:function(b){return new a.Dimension(b.value*100,"%")},color:function(b){if(b instanceof a.Quoted)return new a.Color(b.value.slice(1));else throw{type:"Argument",message:"argument must be a string"}},iscolor:function(b){return this._isa(b,a.Color)},isnumber:function(b){return this._isa(b,a.Dimension
)},isstring:function(b){return this._isa(b,a.Quoted)},iskeyword:function(b){return this._isa(b,a.Keyword)},isurl:function(b){return this._isa(b,a.URL)},ispixel:function(b){return b instanceof a.Dimension&&b.unit==="px"?a.True:a.False},ispercentage:function(b){return b instanceof a.Dimension&&b.unit==="%"?a.True:a.False},isem:function(b){return b instanceof a.Dimension&&b.unit==="em"?a.True:a.False},_isa:function(b,c){return b instanceof c?a.True:a.False}};function b(b){return a.functions.hsla(b.h
,b.s,b.l,b.a)}function c(b){if(b instanceof a.Dimension)return parseFloat(b.unit=="%"?b.value/100:b.value);else if(typeof b=="number")return b;else throw{error:"RuntimeError",message:"color functions take numbers as parameters"}}function d(a){return Math.min(1,Math.max(0,a))}}(d("./tree")),function(a){a.Dimension=function(a,b){this.value=parseFloat(a),this.unit=b||null},a.Dimension.prototype={eval:function(){return this},toColor:function(){return new a.Color([this.value,this.value,this.value])},toCSS
:function(){var a=this.value+this.unit;return a},operate:function(b,c){return new a.Dimension(a.operate(b,this.value,c.value),this.unit||c.unit)},compare:function(b){if(b instanceof a.Dimension)if(b.value>this.value)return-1;else if(b.value<this.value)return 1;else return 0;else return-1}}}(d("../tree")),function(a){a.URL=function(a,b){a.data?this.attrs=a:(typeof window!="undefined"&&!/^(?:https?:\/\/|file:\/\/|data:|\/)/.test(a.value)&&b.length>0&&(a.value=b[0]+(a.value.charAt(0)==="/"?a.value.slice
(1):a.value)),this.value=a,this.paths=b)},a.URL.prototype={toCSS:function(){return"url("+(this.attrs?"data:"+this.attrs.mime+this.attrs.charset+this.attrs.base64+this.attrs.data:this.value.toCSS())+")"},eval:function(b){return this.attrs?this:new a.URL(this.value.eval(b),this.paths)}}}(d("../tree")),function(a){a.Color=function(a,b){Array.isArray(a)?this.rgb=a:a.length==6?this.rgb=a.match(/.{2}/g).map(function(a){return parseInt(a,16)}):this.rgb=a.split("").map(function(a){return parseInt(a+a,16)
}),this.alpha=typeof b=="number"?b:1},a.Color.prototype={eval:function(){return this},toCSS:function(){if(this.alpha<1)return"rgba("+this.rgb.map(function(a){return Math.round(a)}).concat(this.alpha).join(", ")+")";else return"#"+this.rgb.map(function(a){return a=Math.round(a),a=(a>255?255:a<0?0:a).toString(16),a.length===1?"0"+a:a}).join("")},operate:function(b,c){var d=[];c instanceof a.Color||(c=c.toColor());for(var e=0;e<3;e++)d[e]=a.operate(b,this.rgb[e],c.rgb[e]);return new a.Color(d,this.alpha+
c.alpha)},toHSL:function(){var a=this.rgb[0]/255,b=this.rgb[1]/255,c=this.rgb[2]/255,d=this.alpha,e=Math.max(a,b,c),f=Math.min(a,b,c),g,h,i=(e+f)/2,j=e-f;if(e===f)g=h=0;else{h=i>.5?j/(2-e-f):j/(e+f);switch(e){case a:g=(b-c)/j+(b<c?6:0);break;case b:g=(c-a)/j+2;break;case c:g=(a-b)/j+4}g/=6}return{h:g*360,s:h,l:i,a:d}},toARGB:function(){var a=[Math.round(this.alpha*255)].concat(this.rgb);return"#"+a.map(function(a){return a=Math.round(a),a=(a>255?255:a<0?0:a).toString(16),a.length===1?"0"+a:a}).join
("")}}}(d("../tree")),function(a){a.Quoted=function(a,b,c,d){this.escaped=c,this.value=b||"",this.quote=a.charAt(0),this.index=d},a.Quoted.prototype={toCSS:function(){if(this.escaped)return this.value;else return this.quote+this.value+this.quote},eval:function(b){var c=this,d=this.value.replace(/`([^`]+)`/g,function(d,e){return(new a.JavaScript(e,c.index,!0)).eval(b).value}).replace(/@\{([\w-]+)\}/g,function(d,e){var f=(new a.Variable("@"+e,c.index)).eval(b);return"value"in f?f.value:f.toCSS()});
return new a.Quoted(this.quote+d+this.quote,d,this.escaped,this.index)}}}(d("../tree")),function(a){a.Alpha=function(a){this.value=a},a.Alpha.prototype={toCSS:function(){return"alpha(opacity="+(this.value.toCSS?this.value.toCSS():this.value)+")"},eval:function(a){return this.value.eval&&(this.value=this.value.eval(a)),this}}}(d("../tree")),function(a){a.Ruleset=function(a,b,c){this.selectors=a,this.rules=b,this._lookups={},this.strictImports=c},a.Ruleset.prototype={eval:function(b){var c=this.selectors&&
this.selectors.map(function(a){return a.eval(b)}),d=new a.Ruleset(c,this.rules.slice(0),this.strictImports);d.root=this.root,d.allowImports=this.allowImports,b.frames.unshift(d);if(d.root||d.allowImports||!d.strictImports)for(var e=0;e<d.rules.length;e++)d.rules[e]instanceof a.Import&&Array.prototype.splice.apply(d.rules,[e,1].concat(d.rules[e].eval(b)));for(var e=0;e<d.rules.length;e++)d.rules[e]instanceof a.mixin.Definition&&(d.rules[e].frames=b.frames.slice(0));for(var e=0;e<d.rules.length;e++
)d.rules[e]instanceof a.mixin.Call&&Array.prototype.splice.apply(d.rules,[e,1].concat(d.rules[e].eval(b)));for(var e=0,f;e<d.rules.length;e++)f=d.rules[e],f instanceof a.mixin.Definition||(d.rules[e]=f.eval?f.eval(b):f);return b.frames.shift(),d},match:function(a){return!a||a.length===0},variables:function(){if(this._variables)return this._variables;else return this._variables=this.rules.reduce(function(b,c){return c instanceof a.Rule&&c.variable===!0&&(b[c.name]=c),b},{})},variable:function(a){return this
.variables()[a]},rulesets:function(){if(this._rulesets)return this._rulesets;else return this._rulesets=this.rules.filter(function(b){return b instanceof a.Ruleset||b instanceof a.mixin.Definition})},find:function(b,c){c=c||this;var d=[],e,f,g=b.toCSS();if(g in this._lookups)return this._lookups[g];return this.rulesets().forEach(function(e){if(e!==c)for(var g=0;g<e.selectors.length;g++)if(f=b.match(e.selectors[g])){b.elements.length>e.selectors[g].elements.length?Array.prototype.push.apply(d,e.find
(new a.Selector(b.elements.slice(1)),c)):d.push(e);break}}),this._lookups[g]=d},toCSS:function(b,c){var d=[],e=[],f=[],g=[],h=[],i,j;this.root||(b.length===0?h=this.selectors.map(function(a){return[a]}):this.joinSelectors(h,b,this.selectors));for(var k=0;k<this.rules.length;k++)j=this.rules[k],j.rules||j instanceof a.Directive||j instanceof a.Media?g.push(j.toCSS(h,c)):j instanceof a.Comment?j.silent||(this.root?g.push(j.toCSS(c)):e.push(j.toCSS(c))):j.toCSS&&!j.variable?e.push(j.toCSS(c)):j.value&&!
j.variable&&e.push(j.value.toString());g=g.join("");if(this.root)d.push(e.join(c.compress?"":"\n"));else if(e.length>0){i=h.map(function(a){return a.map(function(a){return a.toCSS(c)}).join("").trim()}).join(c.compress?",":",\n");for(var k=e.length-1;k>=0;k--)f.indexOf(e[k])===-1&&f.unshift(e[k]);e=f,d.push(i,(c.compress?"{":" {\n  ")+e.join(c.compress?"":"\n  ")+(c.compress?"}":"\n}\n"))}return d.push(g),d.join("")+(c.compress?"\n":"")},joinSelectors:function(a,b,c){for(var d=0;d<c.length;d++)this
.joinSelector(a,b,c[d])},joinSelector:function(b,c,d){var e=[],f=[],g=[],h=[],i=!1,j;for(var k=0;k<d.elements.length;k++)j=d.elements[k],j.combinator.value.charAt(0)==="&"&&(i=!0),i?h.push(j):g.push(j);i||(h=g,g=[]),g.length>0&&e.push(new a.Selector(g)),h.length>0&&f.push(new a.Selector(h));for(var l=0;l<c.length;l++)b.push(e.concat(c[l]).concat(f))}}}(d("../tree")),function(a){a.Comment=function(a,b){this.value=a,this.silent=!!b},a.Comment.prototype={toCSS:function(a){return a.compress?"":this.value
},eval:function(){return this}}}(d("../tree")),function(a){a.Value=function(a){this.value=a,this.is="value"},a.Value.prototype={eval:function(b){if(this.value.length===1)return this.value[0].eval(b);else return new a.Value(this.value.map(function(a){return a.eval(b)}))},toCSS:function(a){return this.value.map(function(b){return b.toCSS(a)}).join(a.compress?",":", ")}}}(d("../tree")),function(a){a.Media=function(b,c){var d=new a.Element("&",null,0),e=[new a.Selector([d])];this.features=new a.Value
(c),this.ruleset=new a.Ruleset(e,b),this.ruleset.allowImports=!0},a.Media.prototype={toCSS:function(a,b){var c=this.features.toCSS(b);return this.ruleset.root=a.length===0||a[0].multiMedia,"@media "+c+(b.compress?"{":" {\n  ")+this.ruleset.toCSS(a,b).trim().replace(/\n/g,"\n  ")+(b.compress?"}":"\n}\n")},eval:function(b){b.mediaBlocks||(b.mediaBlocks=[],b.mediaPath=[]);var c=b.mediaBlocks.length;b.mediaPath.push(this),b.mediaBlocks.push(this);var d=new a.Media([],[]);return d.features=this.features
.eval(b),b.frames.unshift(this.ruleset),d.ruleset=this.ruleset.eval(b),b.frames.shift(),b.mediaBlocks[c]=d,b.mediaPath.pop(),b.mediaPath.length===0?d.evalTop(b):d.evalNested(b)},variable:function(b){return a.Ruleset.prototype.variable.call(this.ruleset,b)},find:function(){return a.Ruleset.prototype.find.apply(this.ruleset,arguments)},rulesets:function(){return a.Ruleset.prototype.rulesets.apply(this.ruleset)},evalTop:function(b){var c=this;if(b.mediaBlocks.length>1){var d=new a.Element("&",null,0
),e=[new a.Selector([d])];c=new a.Ruleset(e,b.mediaBlocks),c.multiMedia=!0}return delete b.mediaBlocks,delete b.mediaPath,c},evalNested:function(b){var c,d,e=b.mediaPath.concat([this]);for(c=0;c<e.length;c++)d=e[c].features instanceof a.Value?e[c].features.value:e[c].features,e[c]=Array.isArray(d)?d:[d];return this.features=new a.Value(this.permute(e).map(function(b){b=b.map(function(b){return b.toCSS?b:new a.Anonymous(b)});for(c=b.length-1;c>0;c--)b.splice(c,0,new a.Anonymous("and"));return new 
a.Expression(b)})),new a.Ruleset([],[])},permute:function(a){if(a.length===0)return[];else if(a.length===1)return a[0];else{var b=[],c=this.permute(a.slice(1));for(var d=0;d<c.length;d++)for(var e=0;e<a[0].length;e++)b.push([a[0][e]].concat(c[d]));return b}}}}(d("../tree")),function(a){a.Assignment=function(a,b){this.key=a,this.value=b},a.Assignment.prototype={toCSS:function(){return this.key+"="+(this.value.toCSS?this.value.toCSS():this.value)},eval:function(a){return this.value.eval&&(this.value=
this.value.eval(a)),this}}}(d("../tree")),function(a){a.Anonymous=function(a){this.value=a.value||a},a.Anonymous.prototype={toCSS:function(){return this.value},eval:function(){return this}}}(d("../tree")),function(a){a.Selector=function(a){this.elements=a,this.elements[0].combinator.value===""&&(this.elements[0].combinator.value=" ")},a.Selector.prototype.match=function(a){var b=this.elements.length,c=a.elements.length,d=Math.min(b,c);if(b<c)return!1;else for(var e=0;e<d;e++)if(this.elements[e].value!==
a.elements[e].value)return!1;return!0},a.Selector.prototype.eval=function(b){return new a.Selector(this.elements.map(function(a){return a.eval(b)}))},a.Selector.prototype.toCSS=function(a){if(this._css)return this._css;return this._css=this.elements.map(function(b){if(typeof b=="string")return" "+b.trim();else return b.toCSS(a)}).join("")}}(d("../tree")),function(a){a.Operation=function(a,b){this.op=a.trim(),this.operands=b},a.Operation.prototype.eval=function(b){var c=this.operands[0].eval(b),d=
this.operands[1].eval(b),e;if(c instanceof a.Dimension&&d instanceof a.Color)if(this.op==="*"||this.op==="+")e=d,d=c,c=e;else throw{name:"OperationError",message:"Can't substract or divide a color from a number"};return c.operate(this.op,d)},a.operate=function(a,b,c){switch(a){case"+":return b+c;case"-":return b-c;case"*":return b*c;case"/":return b/c}}}(d("../tree")),function(a){a.Condition=function(a,b,c,d,e){this.op=a.trim(),this.lvalue=b,this.rvalue=c,this.index=d,this.negate=e},a.Condition.prototype
.eval=function(a){var b=this.lvalue.eval(a),c=this.rvalue.eval(a),d=this.index,e,e=function(a){switch(a){case"and":return b&&c;case"or":return b||c;default:if(b.compare)e=b.compare(c);else if(c.compare)e=c.compare(b);else throw{type:"Type",message:"Unable to perform comparison",index:d};switch(e){case-1:return a==="<"||a==="=<";case 0:return a==="="||a===">="||a==="=<";case 1:return a===">"||a===">="}}}(this.op);return this.negate?!e:e}}(d("../tree")),function(a){a.Variable=function(a,b,c){this.name=
a,this.index=b,this.file=c},a.Variable.prototype={eval:function(b){var c,d,e=this.name;e.indexOf("@@")==0&&(e="@"+(new a.Variable(e.slice(1))).eval(b).value);if(c=a.find(b.frames,function(a){if(d=a.variable(e))return d.value.eval(b)}))return c;else throw{type:"Name",message:"variable "+e+" is undefined",filename:this.file,index:this.index}}}}(d("../tree")),function(a){a.Call=function(a,b,c,d){this.name=a,this.args=b,this.index=c,this.filename=d},a.Call.prototype={eval:function(b){var c=this.args.
map(function(a){return a.eval(b)});if(this.name in a.functions)try{return a.functions[this.name].apply(a.functions,c)}catch(d){throw{type:d.type||"Runtime",message:"error evaluating function `"+this.name+"`"+(d.message?": "+d.message:""),index:this.index,filename:this.filename}}else return new a.Anonymous(this.name+"("+c.map(function(a){return a.toCSS()}).join(", ")+")")},toCSS:function(a){return this.eval(a).toCSS()}}}(d("../tree")),function(a){a.Keyword=function(a){this.value=a},a.Keyword.prototype=
{eval:function(){return this},toCSS:function(){return this.value},compare:function(b){if(b instanceof a.Keyword)return b.value===this.value?0:1;else return-1}},a.True=new a.Keyword("true"),a.False=new a.Keyword("false")}(d("../tree")),function(a){a.JavaScript=function(a,b,c){this.escaped=c,this.expression=a,this.index=b},a.JavaScript.prototype={eval:function(b){var c,d=this,e={},f=this.expression.replace(/@\{([\w-]+)\}/g,function(c,e){return a.jsify((new a.Variable("@"+e,d.index)).eval(b))});try{
f=new Function("return ("+f+")")}catch(g){throw{message:"JavaScript evaluation error: `"+f+"`",index:this.index}}for(var h in b.frames[0].variables())e[h.slice(1)]={value:b.frames[0].variables()[h].value,toJS:function(){return this.value.eval(b).toCSS()}};try{c=f.call(e)}catch(g){throw{message:"JavaScript evaluation error: '"+g.name+": "+g.message+"'",index:this.index}}if(typeof c=="string")return new a.Quoted('"'+c+'"',c,this.escaped,this.index);else if(Array.isArray(c))return new a.Anonymous(c.
join(", "));else return new a.Anonymous(c)}}}(d("../tree")),function(a){a.Expression=function(a){this.value=a},a.Expression.prototype={eval:function(b){if(this.value.length>1)return new a.Expression(this.value.map(function(a){return a.eval(b)}));else if(this.value.length===1)return this.value[0].eval(b);else return this},toCSS:function(a){return this.value.map(function(b){return b.toCSS?b.toCSS(a):""}).join(" ")}}}(d("../tree")),function(a){a.Directive=function(b,c,d){this.name=b,Array.isArray(c)?
(this.ruleset=new a.Ruleset([],c),this.ruleset.allowImports=!0):this.value=c},a.Directive.prototype={toCSS:function(a,b){if(this.ruleset)return this.ruleset.root=!0,this.name+(b.compress?"{":" {\n  ")+this.ruleset.toCSS(a,b).trim().replace(/\n/g,"\n  ")+(b.compress?"}":"\n}\n");else return this.name+" "+this.value.toCSS()+";\n"},eval:function(a){return a.frames.unshift(this),this.ruleset=this.ruleset&&this.ruleset.eval(a),a.frames.shift(),this},variable:function(b){return a.Ruleset.prototype.variable
.call(this.ruleset,b)},find:function(){return a.Ruleset.prototype.find.apply(this.ruleset,arguments)},rulesets:function(){return a.Ruleset.prototype.rulesets.apply(this.ruleset)}}}(d("../tree")),function(a){a.Import=function(b,c,d,e,f){var g=this;this.once=e,this.index=f,this._path=b,this.features=d&&new a.Value(d),b instanceof a.Quoted?this.path=/\.(le?|c)ss(\?.*)?$/.test(b.value)?b.value:b.value+".less":this.path=b.value.value||b.value,this.css=/css(\?.*)?$/.test(this.path),this.css||c.push(this
.path,function(b,c,d){b&&(b.index=f),d&&g.once&&(g.skip=d),g.root=c||new a.Ruleset([],[])})},a.Import.prototype={toCSS:function(a){var b=this.features?" "+this.features.toCSS(a):"";if(this.css)return"@import "+this._path.toCSS()+b+";\n";else return""},eval:function(b){var c,d=this.features&&this.features.eval(b);if(this.skip)return[];if(this.css)return this;else{c=new a.Ruleset([],this.root.rules.slice(0));for(var e=0;e<c.rules.length;e++)c.rules[e]instanceof a.Import&&Array.prototype.splice.apply
(c.rules,[e,1].concat(c.rules[e].eval(b)));return this.features?new a.Media(c.rules,this.features.value):c.rules}}}}(d("../tree")),function(a){a.Element=function(b,c,d){this.combinator=b instanceof a.Combinator?b:new a.Combinator(b),typeof c=="string"?this.value=c.trim():c?this.value=c:this.value="",this.index=d},a.Element.prototype.eval=function(b){return new a.Element(this.combinator,this.value.eval?this.value.eval(b):this.value,this.index)},a.Element.prototype.toCSS=function(a){return this.combinator
.toCSS(a||{})+(this.value.toCSS?this.value.toCSS(a):this.value)},a.Combinator=function(a){a===" "?this.value=" ":a==="& "?this.value="& ":this.value=a?a.trim():""},a.Combinator.prototype.toCSS=function(a){return{"":""," ":" ","&":"","& ":" ",":":" :","+":a.compress?"+":" + ","~":a.compress?"~":" ~ ",">":a.compress?">":" > "}[this.value]}}(d("../tree")),function(a){a.mixin={},a.mixin.Call=function(b,c,d,e,f){this.selector=new a.Selector(b),this.arguments=c,this.index=d,this.filename=e,this.important=
f},a.mixin.Call.prototype={eval:function(a){var b,c,d=[],e=!1;for(var f=0;f<a.frames.length;f++)if((b=a.frames[f].find(this.selector)).length>0){c=this.arguments&&this.arguments.map(function(b){return b.eval(a)});for(var g=0;g<b.length;g++)if(b[g].match(c,a))try{Array.prototype.push.apply(d,b[g].eval(a,this.arguments,this.important).rules),e=!0}catch(h){throw{message:h.message,index:this.index,filename:this.filename,stack:h.stack}}if(e)return d;else throw{type:"Runtime",message:"No matching definition was found for `"+
this.selector.toCSS().trim()+"("+this.arguments.map(function(a){return a.toCSS()}).join(", ")+")`",index:this.index,filename:this.filename}}throw{type:"Name",message:this.selector.toCSS().trim()+" is undefined",index:this.index,filename:this.filename}}},a.mixin.Definition=function(b,c,d,e,f){this.name=b,this.selectors=[new a.Selector([new a.Element(null,b)])],this.params=c,this.condition=e,this.variadic=f,this.arity=c.length,this.rules=d,this._lookups={},this.required=c.reduce(function(a,b){if(!b
.name||b.name&&!b.value)return a+1;else return a},0),this.parent=a.Ruleset.prototype,this.frames=[]},a.mixin.Definition.prototype={toCSS:function(){return""},variable:function(a){return this.parent.variable.call(this,a)},variables:function(){return this.parent.variables.call(this)},find:function(){return this.parent.find.apply(this,arguments)},rulesets:function(){return this.parent.rulesets.apply(this)},evalParams:function(b,c){var d=new a.Ruleset(null,[]),e;for(var f=0,g,h;f<this.params.length;f++
)if(h=this.params[f].name)if(this.params[f].variadic&&c){e=[];for(var i=f;i<c.length;i++)e.push(c[i].eval(b));d.rules.unshift(new a.Rule(h,(new a.Expression(e)).eval(b)))}else if(g=c&&c[f]||this.params[f].value)d.rules.unshift(new a.Rule(h,g.eval(b)));else throw{type:"Runtime",message:"wrong number of arguments for "+this.name+" ("+c.length+" for "+this.arity+")"};return d},eval:function(b,c,d){var e=this.evalParams(b,c),f,g=[],h,i;for(var j=0;j<Math.max(this.params.length,c&&c.length);j++)g.push
(c[j]||this.params[j].value);return e.rules.unshift(new a.Rule("@arguments",(new a.Expression(g)).eval(b))),h=d?this.rules.map(function(b){return new a.Rule(b.name,b.value,"!important",b.index)}):this.rules.slice(0),(new a.Ruleset(null,h)).eval({frames:[this,e].concat(this.frames,b.frames)})},match:function(a,b){var c=a&&a.length||0,d,e;if(!this.variadic){if(c<this.required)return!1;if(c>this.params.length)return!1;if(this.required>0&&c>this.params.length)return!1}if(this.condition&&!this.condition
.eval({frames:[this.evalParams(b,a)].concat(b.frames)}))return!1;d=Math.min(c,this.arity);for(var f=0;f<d;f++)if(!this.params[f].name&&a[f].eval(b).toCSS()!=this.params[f].value.eval(b).toCSS())return!1;return!0}}}(d("../tree")),function(a){a.Paren=function(a){this.value=a},a.Paren.prototype={toCSS:function(a){return"("+this.value.toCSS(a)+")"},eval:function(b){return new a.Paren(this.value.eval(b))}}}(d("../tree")),function(a){a.Rule=function(b,c,d,e,f){this.name=b,this.value=c instanceof a.Value?
c:new a.Value([c]),this.important=d?" "+d.trim():"",this.index=e,this.inline=f||!1,b.charAt(0)==="@"?this.variable=!0:this.variable=!1},a.Rule.prototype.toCSS=function(a){if(this.variable)return"";else return this.name+(a.compress?":":": ")+this.value.toCSS(a)+this.important+(this.inline?"":";")},a.Rule.prototype.eval=function(b){return new a.Rule(this.name,this.value.eval(b),this.important,this.index,this.inline)},a.Shorthand=function(a,b){this.a=a,this.b=b},a.Shorthand.prototype={toCSS:function(
a){return this.a.toCSS(a)+"/"+this.b.toCSS(a)},eval:function(){return this}}}(d("../tree")),b.Parser.importer=function(a,c,d){var e=c[0]?c[0]+"/"+a:a;window.require(["text!"+e],function(a){(new b.Parser({paths:[e.replace(/(?:^(\/)|\/|^)[^\/]*$/,"$1")],filename:e})).parse(a,function(a,b){if(a)return console.error("LESS error",a);d(b)})})},function(c){var d=new $.Deferred;try{(new b.Parser({paths:[""]})).parse(a+c,function(a,b){if(a)d.reject(a);else try{d.resolve(b.toCSS())}catch(c){console.error("LESS error"
,c)}})}catch(e){console.error("LESS error",e)}return d.promise()}}(),define("less",{load:function(b,d,f,g){require(["text!"+b]).pipe(function(b){if(a)return e(b).pipe(function(a){return{less:b,css:a}});else return{less:b}}).done(function(a){var d={name:g.baseUrl+b,source:a.less};a.css&&(d.node=h(d.name,a.css,"script")),c.push(d),f()}).fail(function(a){console.error("LESS error",a),f()})}}),define("themes",{set:function(a,c){var e;return a?e=["text!themes/"+a+"/definitions.less","text!themes/"+a+"/style.less"
,"text!themes/"+a+"/style.css"]:(e=["text!themes/default/definitions.less","text!themes/style.less"],a="default"),require(e).pipe(function(e,f,j){var k=ox.base+"/apps/themes/"+a+"/";return j=j||"",d?d.text(g(k,j)):d=h(k+"static.css",j,"script"),b.path=k,b.name=k+"dynamic.less",b.source=f,i(c||e)})},alter:function(b){return this.set("",a.replace(/^\s*@([\w-]+)\s*:.*$/gm,function(a,c){return c in b?"@"+c+":"+b[c]+";":a}))},getDefinitions:function(){return(a||"").replace(/:/g,": ")}})})(),define("gettext"
,function(){return{load:function(a,b,c){require(["io.ox/core/gettext"]).pipe(function(a){return a.language}).done(function(d){b([a+"."+d],c)})}}}),function(){function a(a){var b={},c={},d=function(b,c,d){return function(e){var f=$(d(e.data||e)).appendTo(this);a.point(b+"/"+c).invoke("draw",f,e)}};this.addPart=function(e,f){var g=$(f).filter(function(){return this.nodeType===1}),h=g.filter("extension");h.length>0?h.each(function(b){var c=$(this),f=c.html(),g=c.attr("id")||"default";a.point(e).extend
({id:g,index:(b+1)*100,draw:d(e,g,doT.template(f))})}):(c[e]=!0,b[e]=doT.template(f))},this.render=function(d,e,f){e=e!==undefined?e:{};if(c[d])return d in b?$(b[d](e)):$();else return f=f||$("<div>"),a.point(d).invoke("draw",f,e),f}}"use strict",define("dot",{load:function(b,c,d){c(["text!"+b,"io.ox/core/extensions"],function(b,c){var e=$(b).filter(function(){return this.nodeType===1}),f=e.filter("part"),g=new a(c);f.each(function(){var a=$(this),b=a.html(),c=a.attr("id")||"default";g.addPart(c,
b)}),d(g)})}})}(),function(){"use strict",$.original={val:$.fn.val},$.preventDefault=function(a){a.preventDefault()},$.escape=function(a){return String(a).replace(/([!"#$%&'()*+,.\/:;<=>?@\[\]\^`{|}~])/g,"\\$1")},$.button=function(a){var b,c,d,e=$.extend({label:"",click:$.noop,enabled:!0,data:{},css:{},primary:!1,info:!1,success:!1,warning:!1,danger:!1,inverse:!1},a||{});return e.purelink===!0?b="button io-ox-action-link":b="btn"+(e.enabled?"":" btn-disabled")+(e.primary?" btn-primary":"")+(e.info?" btn-info"
:"")+(e.success?" btn-success":"")+(e.warning?" btn-warning":"")+(e.danger?" btn-danger":"")+(e.inverse?" btn-inverse":""),e.label.nodeType===3?c=e.label:c=document.createTextNode(e.label),e.purelink===!0?d=$("<a>").addClass(b).append(c):d=$("<button>").addClass(b).append($("<span>").append(c)),d.on("click",e.data,e.click),e.id!==undefined&&d.attr("id",e.id),e.dataaction!==undefined&&d.attr("data-action",e.dataaction),e.tabIndex!==undefined&&d.attr("tabindex",e.tabIndex),d},$.fn.busy=function(a){
return this.each(function(){var b=$(this);clearTimeout(b.data("busy-timeout")),b.data("busy-timeout",setTimeout(function(){b.addClass("io-ox-busy"),a&&b.empty()},200))})},$.fn.idle=function(){return this.each(function(){var a=$(this);clearTimeout(a.data("busy-timeout")),a.removeClass("io-ox-busy")})},$.fn.intoViewport=function(a){var b,c,d,e,f,g;if(!a||this.length===0)return this;try{b=$(a),c=0,d=b.scrollTop(),e=0,f=this.offset().top+d-b.offset().top,g=0,f<d?(f=f<50?0:f,b.scrollTop(f)):(e=d+b.height
(),g=this.outerHeight(),f+g>e&&b.scrollTop(d+f+g-e))}catch(h){console.error("$.fn.intoViewport",this,h)}return this},$.fn.center=function(){return this.wrap($("<div>").addClass("io-ox-center")).parent()},$.fail=function(a,b){var c=$("<div>").addClass("io-ox-fail").append($("<span>").text(a));return b&&c.append($("<span>").text(" ")).append($("<a>",{href:"#"}).text("Retry").on("click",function(a){a.preventDefault(),$(this).closest(".io-ox-center").remove(),b.apply(this,arguments)})),c.center()},$.
fn.shake=function(a,b,c){var d,e,f,g,h,i,j;a=a||4,b=b||10,c=c||25,d=$.Deferred(),e=0,f=a*3,g=this.eq(0),h=g.css("position"),i=function(){++e===f&&(g.css("position",h),d.resolve())},h!=="absolute"&&g.css("position","relative");for(j=0;j<a;j++)g.animate({left:-b},c,i).animate({left:b},c*2,i).animate({left:0},c,i);return d},$.txt=function(a){return document.createTextNode(a!==undefined?a:"")},$.inlineEdit=function(){var a=function(a,b){a.text(b).insertBefore(this),$(this).remove()},b=function(b){a.call
(this,b.data.node,$(this).val())},c=function(b){var c;if(b.which===13){if(c=$.trim($(this).val()))a.call(this,b.data.node,c),b.data.node.trigger("update",c)}else b.which===27&&a.call(this,b.data.node,b.data.text)},d=function(){var a=$(this);$('<input type="text" class="nice-input">').css({width:a.width()+"px",fontSize:a.css("fontSize"),fontWeight:a.css("fontWeight"),lineHeight:a.css("lineHeight"),marginBottom:a.css("marginBottom")}).val(a.text()).on("keydown",{node:a,text:a.text()},c).on("blur",{
node:a},b).insertBefore(a).focus().select(),a.detach()};return $("<div>").on("dblclick",d)},$.fn.scrollable=function(){return $("<div>").addClass("scrollable-pane").appendTo(this.addClass("scrollable"))},$.labelize=function(){var a=1;return function(b,c){return b.attr("id")?c=b.attr("id"):c=(c||"field")+"_"+a++,$("<label>",{"for":c}).addClass("wrapping-label").append(b.attr("id",c))}}(),$.alert=function(a,b){return $("<div>").addClass("alert alert-block alert-error fade in").append($("<a>",{href:"#"
}).addClass("close").attr("data-dismiss","alert").html("&times;"),a?$("<h4>").addClass("alert-heading").text(a):$(),b?$("<p>").text(b):$())},$.linkSplit=function(a){var b,c=new RegExp("(?:https?://|ftp://|mailto:|news\\\\.|www\\\\.)[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]","gi"),d=[],e=0;while(b=c.exec(a))d.push($(document.createTextNode(a.substring(e,b.index)))),d.push($(document.createTextNode(" "))),d.push($("<a>",{href:b[0],target:"_blank"}).text(b[0])),d.push($(document.createTextNode
(" "))),e=b.index+b[0].length;return e<a.length&&d.push($(document.createTextNode(a.substring(e,a.length)))),d},$.jGrowl=function(a,b){$("#jGrowl").size()===0&&$('<div id="jGrowl"></div>').addClass(b&&b.position?b.position:$.jGrowl.defaults.position).appendTo("body"),$("#jGrowl").jGrowl(a,b)},$.fn.jGrowl=function(a,b){var c;if($.isFunction(this.each))return c=arguments,this.each(function(){var d=this;$(this).data("jGrowl.instance")===undefined&&($(this).data("jGrowl.instance",$.extend(new $.fn.jGrowl
,{notifications:[],element:null,interval:null})),$(this).data("jGrowl.instance").startup(this)),$.isFunction($(this).data("jGrowl.instance")[a])?$(this).data("jGrowl.instance")[a].apply($(this).data("jGrowl.instance"),$.makeArray(c).slice(1)):$(this).data("jGrowl.instance").create(a,b)})},$.extend($.fn.jGrowl.prototype,{defaults:{pool:0,header:"",group:"",sticky:!1,position:"top-right",glue:"after",theme:"default",themeState:"highlight",corners:"10px",check:250,life:3e3,closeDuration:"normal",openDuration
:"normal",easing:"swing",closer:!0,closeTemplate:"&times;",closerTemplate:"<div>[ close all ]</div>",log:function(){},beforeOpen:function(){},afterOpen:function(){},open:function(){},beforeClose:function(){},close:function(){},animateOpen:{opacity:"show"},animateClose:{opacity:"hide"}},notifications:[],element:null,interval:null,create:function(a,b){b=$.extend({},this.defaults,b),typeof b.speed!="undefined"&&(b.openDuration=b.speed,b.closeDuration=b.speed),this.notifications.push({message:a,options
:b}),b.log.apply(this.element,[this.element,a,b])},render:function(a){var b=this,c=a.message,d=a.options;d.themeState=d.themeState===""?"":"ui-state-"+d.themeState,a=$('<div class="jGrowl-notification '+d.themeState+" ui-corner-all"+(d.group!==undefined&&d.group!==""?" "+d.group:"")+'">'+'<div class="jGrowl-close">'+d.closeTemplate+"</div>"+'<div class="jGrowl-header">'+d.header+"</div>"+'<div class="jGrowl-message">'+c+"</div></div>").data("jGrowl",d).addClass(d.theme).children("div.jGrowl-close"
).bind("click.jGrowl",function(){$(this).parent().trigger("jGrowl.close")}).parent(),$(a).bind("mouseover.jGrowl",function(){$("div.jGrowl-notification",b.element).data("jGrowl.pause",!0)}).bind("mouseout.jGrowl",function(){$("div.jGrowl-notification",b.element).data("jGrowl.pause",!1)}).bind("jGrowl.beforeOpen",function(){d.beforeOpen.apply(a,[a,c,d,b.element])!==!1&&$(this).trigger("jGrowl.open")}).bind("jGrowl.open",function(){d.open.apply(a,[a,c,d,b.element])!==!1&&(d.glue==="after"?$("div.jGrowl-notification:last"
,b.element).after(a):$("div.jGrowl-notification:first",b.element).before(a),$(this).animate(d.animateOpen,d.openDuration,d.easing,function(){$.browser.msie&&(parseInt($(this).css("opacity"),10)===1||parseInt($(this).css("opacity"),10)===0)&&this.style.removeAttribute("filter"),$(this).data("jGrowl")!==null&&($(this).data("jGrowl").created=new Date),$(this).trigger("jGrowl.afterOpen")}))}).bind("jGrowl.afterOpen",function(){d.afterOpen.apply(a,[a,c,d,b.element])}).bind("jGrowl.beforeClose",function(
){d.beforeClose.apply(a,[a,c,d,b.element])!==!1&&$(this).trigger("jGrowl.close")}).bind("jGrowl.close",function(){$(this).data("jGrowl.pause",!0),$(this).animate(d.animateClose,d.closeDuration,d.easing,function(){$.isFunction(d.close)?d.close.apply(a,[a,c,d,b.element])!==!1&&$(this).remove():$(this).remove()})}).trigger("jGrowl.beforeOpen"),d.corners!==""&&$.fn.corner!==undefined&&$(a).corner(d.corners),$("div.jGrowl-notification:parent",b.element).size()>1&&$("div.jGrowl-closer",b.element).size(
)===0&&this.defaults.closer!==!1&&$(this.defaults.closerTemplate).addClass("jGrowl-closer "+this.defaults.themeState+" ui-corner-all").addClass(this.defaults.theme).appendTo(b.element).animate(this.defaults.animateOpen,this.defaults.speed,this.defaults.easing).bind("click.jGrowl",function(){$(this).siblings().trigger("jGrowl.beforeClose"),$.isFunction(b.defaults.closer)&&b.defaults.closer.apply($(this).parent()[0],[$(this).parent()[0]])})},update:function(){$(this.element).find("div.jGrowl-notification:parent"
).each(function(){$(this).data("jGrowl")!==undefined&&$(this).data("jGrowl").created!==undefined&&$(this).data("jGrowl").created.getTime()+parseInt($(this).data("jGrowl").life,10)<(new Date).getTime()&&$(this).data("jGrowl").sticky!==!0&&($(this).data("jGrowl.pause")===undefined||$(this).data("jGrowl.pause")!==!0)&&$(this).trigger("jGrowl.beforeClose")}),this.notifications.length>0&&(this.defaults.pool===0||$(this.element).find("div.jGrowl-notification:parent").size()<this.defaults.pool)&&this.render
(this.notifications.shift()),$(this.element).find("div.jGrowl-notification:parent").size()<2&&$(this.element).find("div.jGrowl-closer").animate(this.defaults.animateClose,this.defaults.speed,this.defaults.easing,function(){$(this).remove()})},startup:function(a){this.element=$(a).addClass("jGrowl").append('<div class="jGrowl-notification"></div>'),this.interval=setInterval(function(){$(a).data("jGrowl.instance").update()},parseInt(this.defaults.check,10)),$.browser.msie&&parseInt($.browser.version
,10)<7&&!window.XMLHttpRequest&&$(this.element).addClass("ie6")},shutdown:function(){$(this.element).removeClass("jGrowl").find("div.jGrowl-notification").remove(),clearInterval(this.interval)},close:function(){$(this.element).find("div.jGrowl-notification").each(function(){$(this).trigger("jGrowl.beforeClose")})}}),$.jGrowl.defaults=$.fn.jGrowl.prototype.defaults}(),function(){var a=navigator.userAgent,b=Object.prototype.toString.call(window.opera)==="[object Opera]",c=a.indexOf("AppleWebKit/")>-1
,d=a.indexOf("Chrome/")>-1,e=Array.prototype.slice,f=function(a,b){var c=(a||"").split(b===undefined?"&":b),d=0,e=c.length,f,g={},h=decodeURIComponent;for(;d<e;d++){f=c[d];var i=f.split(/\=/),j=i[0],k=i[1];if(j!==""||k!==undefined)g[h(j)]=k!==undefined?h(k):undefined}return g},g=function(a,b){return _(String(a).split("")).map(function(a){return String.fromCharCode(a.charCodeAt(0)+b)}).join("")},h=f(document.location.search.substr(1),/&/),i=(new Date).getTimezoneOffset()*60*1e3,j=function(){},k=function(
a,b,c){var d;return b&&b.hasOwnProperty("constructor")?d=b.constructor:d=function(){a.apply(this,arguments)},_.extend(d,a),j.prototype=a.prototype,d.prototype=new j,b&&_.extend(d.prototype,b),c&&_.extend(d,c),d.prototype.constructor=d,d.__super__=a.prototype,d};"use strict",_.browser={IE:navigator.appName!=="Microsoft Internet Explorer"?undefined:Number(navigator.appVersion.match(/MSIE (\d+\.\d+)/)[1]),Opera:b,WebKit:c,Safari:c&&!d,Chrome:c&&d,Firefox:a.indexOf("Gecko")>-1&&a.indexOf("KHTML")===-1
,MacOS:a.indexOf("Macintosh")>-1,iOS:/iPhone|iPad|iPod/.test(navigator.platform)&&c},_.url={param:function(a){return a===undefined?h:h[a]},hash:function(a,b){var c,d=location.href.split(/#/)[1]||"";d=f(d.substr(0,1)==="?"?g(decodeURIComponent(d.substr(1)),-1):d);if(b!==undefined)b===null?delete d[a]:d[a]=b,c=_.serialize(d,"&",function(a){return a.replace(/\=/g,"%3D").replace(/\&/g,"%26")}),document.location.hash=c,_.setCookie("hash",c);else return a===undefined?d:d[a]},redirect:function(a){var b=
location,c=b.protocol+"//"+b.host+b.pathname.replace(/\/[^\/]*$/,"/"+a);location.href=c}},_.mixin({serialize:function(a,b,c){var d,e=[],f=c||encodeURIComponent;if(typeof a=="object")for(d in a||{})e.push(f(d)+(a[d]!==undefined?"="+f(a[d]):""));return e.join(b===undefined?"&":b)},deserialize:f,rot:g,getCookie:function(a){return a=String(a||"\0"),_.chain(document.cookie.split(/; ?/)).filter(function(b){return b.substr(0,a.length)===a}).map(function(b){return decodeURIComponent(b.substr(a.length+1))
}).first().value()},setCookie:function(a,b){document.cookie=a+"="+encodeURIComponent(b)},inspect:function(a){var b=e.call(arguments);return b.unshift("Inspect"),console.debug.apply(console,b),a},call:function(a){var b,c,d;if(_.isFunction(a)){b=1,c=arguments.length,d=[];for(;b<c;b++)d.push(arguments[b]);return a.apply(a,d)}},now:function(){return(new Date).getTime()},utc:function(){return(new Date).getTime()-i},firstOf:function(){var a=e.call(arguments),b=0,c=a.length;for(;b<c;b++)if(a[b]!==undefined
)return a[b];return undefined},copy:function(){var a=_.isArray,b=function(c,d){var e=a(c)?new Array(c.length):{},f,g;for(g in c)f=c[g],e[g]=d&&typeof f=="object"&&f!==null?b(f,d):f;return e};return function(a,c){return typeof a!="object"||a===null?a:b(a,!!c)}}(),deepClone:function(a){return _.copy(a,!0)},lfo:function(){var a=e.call(arguments),b=a.shift(),c=b.count=(b.count||0)+1;return function(){var d=e.call(arguments);setTimeout(function(){c===b.count&&b.apply(b,a.concat(d))},0)}},queued:function(
a,b){var c={};return function(){var d=$.Deferred(),e=c[a]||(c[a]=[$.when()]),f=_(e).last(),g=$.makeArray(arguments);return e.push(d),f.done(function(){a.apply(null,g).done(function(){setTimeout(function(){e=_(e).without(d),d.resolve()},b||250)})}),d}},printf:function(a,b){var c;return _.isArray(b)||(b=e.call(arguments,1)),c=0,String(a).replace(/%(([0-9]+)\$)?[A-Za-z]/g,function(a,d,e){return d&&(c=e-1),b[c++]}).replace(/%%/,"%")},formatError:function(a,b){return _.printf(b||"Error: %1$s (%2$s, %3$s)"
,_.printf(a.error,a.error_params),a.code,a.error_id)},prewrap:function(a){return String(a).replace(/([\/\.\,\-]+)/g,"$1​")},pad:function(a,b,c){var d=String(a),e=b||1,f=e-d.length;return(f>0?(new Array(f+1)).join(c||"0"):"")+d},ellipsis:function(a,b){return a=String(a||""),a.length>b?a.substr(0,b-4)+" ...":a},getArray:function(a){return _.isArray(a)?a:[a]},every:function(a,b,c){var d=1e3;b==="hour"?d*=3600:b==="minute"&&(d*=60),setTimeout(function(){c(),setInterval(c,d*(a||1))},d-_.utc()%d+1)},wait
:function(a){var b=$.Deferred();return setTimeout(function(){b.resolve(),b=null},a||0),b},makeExtendable:function(){return function(a){return a.extend=function(a,b){var c=k(this,a,b);return c.extend=this.extend,c},a}}(),clock:function(){var a=null,b=1;return function(c){var d=_.now();console.debug("clock.t"+b++,d-(a||ox.t0),c||""),a=d}}(),cid:function(a){var b,c,d,e,f="recurrence_position";return typeof a=="string"?/^default/.test(a)?(c=a.split("."),b={folder_id:c.slice(0,-1).join("."),id:c.slice
(-1)+""}):(d=a.match(/^(\d*?)\.(\d+)(\.(\d+))?$/))&&d.length&&(b={folder_id:String(d[1]),id:d[2]+""},d[4]!==undefined&&(b[f]=d[4]+"")):typeof a=="object"&&a!==null&&(b=a.id,e=a.folder_id!==undefined?a.folder_id:a.folder,e!==undefined&&(b=e+"."+b),a[f]!==undefined&&a[f]!==null&&(b+="."+a[f])),b}}),window.assert=function(a,b){if(a)return;console.error(b||"Assertion failed!"),console.trace&&console.trace()},0===0&&delete window.assert}(),require({baseUrl:ox.base+"/apps",waitSeconds:60}),typeof window
.console=="undefined"&&(window.console={log:$.noop,debug:$.noop,error:$.noop}),$(document).ready(function(){var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p;"use strict";if(1>2){$("#io-ox-login-header").text("Yalla "+_.now()),$("#background_loader").hide(),$("#io-ox-login-screen").show();return}a=250,b=!1,n=encodeURIComponent,c=function(){$("#io-ox-login-username").focus().select()},d=function(){$("#io-ox-login-footer").remove(),$("#io-ox-login-username").attr("disabled","disabled"),$("#io-ox-login-password")
.val(""),$("#io-ox-login-form").off("submit"),d=i=j=m=null},e=function(b){ox.signin===!0?$("#background_loader").fadeIn(a,function(){var a=_.url.hash("ref"),c="#?"+n(_.rot("session="+ox.session+"&user="+ox.user+(a?"&ref="+n(a):""),1));b||!ox.online?_.url.redirect(c):$("#io-ox-login-form").off("submit").attr("action",ox.apiRoot+"/redirect").removeAttr("target").find("input[type=hidden][name=location]").val(ox.root+"/"+c).end().submit()}):f()},f=function(){d(),$("#io-ox-login-screen").hide(),$(this
).busy(),require("io.ox/core/config").load().done(function(){var a=require("io.ox/core/config").get("language");require("io.ox/core/gettext").setLanguage(a),$.when(require(["io.ox/core/main"]),require("themes").set("default")).done(function(a){a.launch()})})},g=e,h=function(a){var c,d,e,f;a.preventDefault(),c=function(){$("#io-ox-login-blocker").hide(),$("#io-ox-login-feedback").idle()},d=function(a){$("#io-ox-login-feedback").idle(),$("#login-box-content").stop().shake().done(function(){$("#io-ox-login-feedback"
).text(_.formatError(a,"%1$s")),c(),$("#io-ox-login-"+(b?"password":"username")).focus().select()})},e=$("#io-ox-login-username").val(),f=$("#io-ox-login-password").val(),$("#io-ox-login-blocker").show(),$("#io-ox-login-feedback").busy().empty();if($.trim(e).length===0||$.trim(f).length===0&&ox.online){d({error:"Please enter your credentials.",code:"UI-0001"});return}require("io.ox/core/session").login(e,f,$("#io-ox-login-store-box").prop("checked")).done(function(){c(),g()}).fail(d)},j=function(
a){return require(["io.ox/core/login."+a]).done(function(a){$("[data-i18n]").each(function(){var b=$(this),c=a(b.attr("data-i18n"));this.tagName==="INPUT"?b.val(c):b.text(c)})})},i=function(a){a.preventDefault(),j(a.data.id)},k=function(){var a=(navigator.language||navigator.userLanguage).substr(0,2),b="en_US",c="";for(c in ox.serverConfig.languages)if(c.substr(0,2)===a){b=c;break}return j(b)},function(){var c=[];ox.relogin=function(d,e){if(!ox.online)return;b?c.push({request:d,deferred:e}):(c=[{
request:d,deferred:e}],b=!0,$("#io-ox-login-header").html("Your session is expired."+"<br/>"+"<small>Please sign in again to continue.</small>"),$("#io-ox-login-form").on("submit",h),$("#io-ox-login-username").val(ox.user||""),$("#io-ox-login-password").val(""),g=function(){$("#io-ox-login-screen").fadeOut(a,function(){var a,d,e;$("#io-ox-login-screen-decorator").hide(),a=0,e=require("io.ox/core/http");for(;d=c[a];a++)e.retry(d.request).done(d.deferred.resolve).fail(d.deferred.fail);b=!1})},$("#io-ox-login-screen-decorator"
).show(),$("#io-ox-login-screen").addClass("relogin").fadeIn(a,function(){$("#io-ox-login-password").focus().select()}))}}(),l=function(){var a;function b(){ox.signin?m():(a=(location.hash||"").replace(/^#/,""),_.url.redirect("signin"+(a?"#ref="+n(a):"")))}_.url.hash("session")?(ox.session=_.url.hash("session"),ox.user=_.url.hash("user"),a=_.url.hash("ref"),_.url.redirect("#"+(a?decodeURIComponent(a):"")),f()):ox.serverConfig.autoLogin===!0&&ox.online?require("io.ox/core/session").autoLogin().done
(function(){e(!0)}).fail(b):b()},m=function(){var b,d,e=ox.serverConfig,f=e.languages,g="",j="",l="";if(f!==!1){b=$("#io-ox-language-list");for(g in f)b.append($("<a/>",{href:"#"}).on("click",{id:g},i).text(f[g])),b.append(document.createTextNode("  "))}else $("#io-ox-languages").remove();return j=e.pageHeader||"open xchange 7",$("#io-ox-login-header").html(j),l=e.copyright?e.copyright+" ":"",l+=e.version?"Version: "+e.version+" ":"",l+=e.buildDate?"("+e.buildDate+")":"",$("#io-ox-copyright").html
(l),e.autoLogin===!1&&$("#io-ox-login-store").remove(),e.forgotPassword===!1?$("#io-ox-forgot-password").remove():$("#io-ox-forgot-password").find("a").attr("href",e.forgotPassword),ox.online?$("#io-ox-login-password").removeAttr("disabled"):($("#io-ox-login-password").attr("disabled","disabled"),$("#io-ox-login-feedback").html("Offline mode")),_.browser.IE<=8&&(d="http://www.google.com/chromeframe/?user=true",$("#io-ox-login-feedback").html("<div>Your browser is slow and outdated!</div>"+'<div style="font-size: 0.8em">Try <a href="'+
d+'" target="_blank">Google Chrome Frame</a> '+"for much better performance. It&rsquo;s awesome! "+"Administrator rights are not required. Just restart IE after installation.</div>")),$.when(require(["io.ox/core/extensions"]).pipe(function(a){return a.loadPlugins()}),k()).done(function(){$("#io-ox-login-blocker").on("mousedown",!1),$("#io-ox-login-form").on("submit",h),$("#io-ox-login-username").removeAttr("disabled").focus().select(),$("#background_loader").idle().fadeOut(a,c)})},o=window.req=require
,require=function(a,b){var c;if(_.isArray(a))return c=$.Deferred().done(b||$.noop),o(a,c.resolve),c;else return o.apply(this,arguments)},define.async=function(){var a=function(a,b,c){return function(d,e,f,g){e(b,function(){var d=c.apply(null,arguments);d&&d.done?d.done(f):console.error('Module "'+a+'" does not return a deferred object!'),a=b=c=null})}};return function(b,c,d){define(b+":init",{load:a(b,c,d)}),define(b,[b+":init!"],_.identity)}}(),_.browser.Chrome||$("html").addClass("no-searchfield"
),Modernizr.touch||$("html").addClass("mouse"),_.browser.Firefox&&$("html").addClass("no-ellipsis"),$("#background_loader").busy(),p=function(){require([ox.base+"/src/serverconfig.js",ox.base+"/pre-core.js"]).done(function(a){ox.serverConfig=a,document.title=ox.serverConfig.pageTitle||"",l()})},ox.signin||($(window).on("online offline",function(b){b.type==="offline"?($("#io-ox-offline").text("Offline").fadeIn(a),ox.online=!1):($("#io-ox-offline").text("Online").fadeOut(a),ox.online=!0)}),ox.online||
$(window).trigger("offline")),$(window).on("blur focus",function(a){ox.windowState=a.type==="blur"?"background":"foreground"}),Modernizr.applicationcache?function(){var a=window.applicationCache,b=function(){a.removeEventListener("checking",f,!1),a.removeEventListener("cached",d,!1),a.removeEventListener("noupdate",d,!1),a.removeEventListener("error",d,!1),a.removeEventListener("updateready",c,!1)},c=function(){a.status===a.UPDATEREADY&&(b(),a.swapCache(),location.reload())},d=function(){b(),p(),
d=$.noop},e=setTimeout(d,500),f=function(){clearTimeout(e)};a.addEventListener("checking",f,!1),a.addEventListener("cached",d,!1),a.addEventListener("noupdate",d,!1),a.addEventListener("error",d,!1),a.addEventListener("updateready",c,!1)}():p()})