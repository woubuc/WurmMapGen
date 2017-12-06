'use strict';

WurmMapGen.util = {
	yx: L.latLng,
	xy: function(x, y) {
		return yx(-(y / WurmMapGen.data.config.xyMulitiplier), (x / WurmMapGen.data.config.xyMulitiplier));
	}
};