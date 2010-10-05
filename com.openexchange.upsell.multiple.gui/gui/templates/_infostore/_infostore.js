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