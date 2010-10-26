jQuery('a.light_box.image').fancybox(
  {
    'titleShow':false
  }
);
jQuery('a.light_box.swf').fancybox(
  { 
    'padding'			: 0,
		'autoScale'			: false,
  	'transitionIn'		: 'none',
	  'transitionOut'		: 'none'
  }
);
    
jQuery('#upsell_window .detail').hide();
jQuery('#upsell_window .detail_show').toggle(
  function () {
    jQuery('#upsell_window .detail').show();
    jQuery(this).html('[close]');
  },
  function () {
    jQuery('#upsell_window .detail').hide();
    jQuery(this).html('[more]');
  }
);