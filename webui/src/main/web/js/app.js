var _ = require("lodash");


/**
 * rabbits app
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

	/**
	 * @type {Boolean}
	 */
	fld1: true
};

/**
 * standalone func
 */
function fn3() {
	rabbits.fn1(_.map(["1", "2"]));
	rabbits.fld1 = 484;
	const a = 1;

	const b = 2;
	const f = () => {};
	if (b === 2) {
		b = 1;
	}
	new Acl();
}

/**
 * @class my custom class
 */
class Acl {
	constructor() {
		fn3();
        rabbits.fld1 = 1;
	}
}



