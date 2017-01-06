var _ = require("lodash");

/**
 * 
 */
var rabbits = {
	
    /**
     * just a func
     * @param {numeric} p1 parameter one
     * @returns {String}
     */
    fn1: function(p1) {
        return "fn1 called with " + p1;
    },

    fld1: "text field"
};

function fn2() {
	rabbits.fn1(_.map(["1", "2"]));
}
