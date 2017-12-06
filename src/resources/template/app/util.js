'use strict';

WurmMapGen.util = {
	yx: L.latLng,
	xy: function(x, y) {
		return yx(-(y / WurmMapGen.config.xyMulitiplier), (x / WurmMapGen.config.xyMulitiplier));
	}
};
