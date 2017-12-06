function setViewOnMainDeed(map) {
	map.setView(xy(config.actualMapSize/2,config.actualMapSize/2), config.mapMaxZoom-3)
}

function deedBorders() {
	var deedBorders = [];
	return deedBorders;
}

function deedMarkers() {
	var deedMarkers = [];
	return deedMarkers;
}