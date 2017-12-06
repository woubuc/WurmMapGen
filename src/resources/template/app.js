function init() {

	var map = L.map('map', {
		maxBounds: [xy(0,0), xy(config.actualMapSize,config.actualMapSize)],
		maxZoom: config.mapMaxZoom,
		minZoom: config.mapMinZoom,
		crs: L.CRS.Simple
	});

	var mapBounds = new L.LatLngBounds(
		map.unproject([0, config.maxMapSize], config.mapMaxZoom),
		map.unproject([config.maxMapSize, 0], config.mapMaxZoom));

	map.fitBounds(mapBounds);

	var wurmMapLayer = L.tileLayer('images/{x}-{y}.png', {
		maxNativeZoom: config.nativeZoom,
		minNativeZoom: config.nativeZoom,
		minZoom: config.mapMinZoom,
		maxZoom: config.mapMaxZoom,
		bounds: mapBounds,
		attribution: 'Rendered with <a href="https://github.com/Garrett92/WurmMapGen">WurmMapGen</a>',
		noWrap: true,
		tms: false
	}).addTo(map);

	var villageBorders = L.layerGroup(deedBorders()).addTo(map);
	var villageMarkers = L.layerGroup(deedMarkers()).addTo(map);
	var playerMarkers = L.layerGroup(getPlayerMarkers()).addTo(map);
	var structureBorders = L.layerGroup(getStructures()).addTo(map);
	var guardTowerMarkers = L.layerGroup(getGuardTowers()).addTo(map);
	var guardTowerBorders = L.layerGroup(getGuardTowerBorders());

	var overlayData = {
		"Player Markers": playerMarkers,
		"Structure Borders": structureBorders,
		"Village Borders": villageBorders,
		"Village Markers": villageMarkers,
		"Guard Tower Markers": guardTowerMarkers,
		"Guard Tower Borders": guardTowerBorders
	};

	L.control.coordinates({
		position:"bottomleft",
		labelFormatterLng : function(e){
			if (e < 0) {
				e = ((180 + e) + 180);
			}
			return Math.floor(e * xyMulitiplier)+" x,"
			},
		labelFormatterLat : function(e){
			return Math.floor((-e)*xyMulitiplier)+" y"
			}
	}).addTo(map);

	L.control.layers(null, overlayData).addTo(map);

	setViewOnMainDeed(map);
}
