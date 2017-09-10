(function($){
    $.fn.money_field = function(opts) {
        var defaults = { width: null, symbol: '$' };
        var opts     = $.extend(defaults, opts);
        return this.each(function() {
            if(opts.width)
                $(this).css('width', opts.width + 'px');
            var before = $("<span class='input-group-addon'>" + opts.symbol + "</span>");
            if(opts.padding)
                before.css("padding", opts.padding);
            $(this).wrap("<div class='input-group'>").before(before);
        });
    };
})(jQuery);