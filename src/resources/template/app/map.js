'use strict';

WurmMapGen.map = {
	
	/**
	 * Initialises and creates the map interface
	 */
	create: function() {
		var config = WurmMapGen.data.config;
		var xy = WurmMapGen.util.xy;
		
		// Set up the map
		var map = WurmMapGen.map.map = L.map('map', {
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

		// Create layer groups
		var deedBorders = L.layerGroup();
		var deedMarkers = L.layerGroup();
		var guardtowerBorders = L.layerGroup();
		var guardtowerMarkers = L.layerGroup();
		var structureBorders = L.layerGroup();
		var playerMarkers = L.layerGroup();
		
		// Add deeds
		for (var i = 0; i < WurmMapGen.data.deeds.length; i++) {
			var deed = WurmMapGen.data.deeds[i];
			
			// Create polygon based on deed border data
			var border = L.polygon([
				xy(deed.borders[0], deed.borders[1]),
				xy(deed.borders[2], deed.borders[1]),
				xy(deed.borders[2], deed.borders[3]),
				xy(deed.borders[0], deed.borders[3])
			], {
				color: (deed.permanent ? 'orange' : 'white'),
				fillOpacity: 0,
				weight: 1
			});
			
			var marker = L.marker(xy(deed.x, deed.y),
				{icon: WurmMapGen.markers[deed.permanent ? 'main' : 'letter_' + deed.name.charAt(0)]}
			);
				
			marker.bindPopup('<div align="center"><b>' + deed.name + '</b><br><i>' + deed.motto + '</i></div><br><b>Mayor:</b> ' + deed.mayor + '<br><b>Citizens:</b> ' + deed.citizens + '');
			
			// Open the marker popup when the border is clicked
			border.on('click', function() { marker.openPopup(); });
			
			deedBorders.addLayer(border);
			deedMarkers.addLayer(marker);
		}
		
		// Add guard towers
		for (var i = 0; i < WurmMapGen.data.guardtowers.length; i++) {
			var tower = WurmMapGen.data.guardtowers[i];
			
			// Create polygon based on guard tower border data
			var border = L.polygon([
				xy(tower.borders[0], tower.borders[1]),
				xy(tower.borders[2], tower.borders[1]),
				xy(tower.borders[2], tower.borders[3]),
				xy(tower.borders[0], tower.borders[3])
			], {
				color: 'red',
				fillOpacity: 0.1,
				weight: 1
			});
			
			var marker = L.marker(xy(deed.x, deed.y),
				{icon: WurmMapGen.markers.guardtower}
			);
				
			marker.bindPopup('<div align="center"><b>' + deed.name + '</b><br><i>' + deed.motto + '</i></div><br><b>Mayor:</b> ' + deed.mayor + '<br><b>Citizens:</b> ' + deed.citizens + '');
			
			// Open the marker popup when the border is clicked
			border.on('click', function() { marker.openPopup(); });
			
			guardtowerBorders.addLayer(border);
			guardtowerMarkers.addLayer(marker);
		}
		
		// Add layers to map
		deedBorders.addTo(map);
		deedMarkers.addTo(map);
		guardtowerBorders.addTo(map);
		guardtowerMarkers.addTo(map);
		structureBorders.addTo(map);
		playerMarkers.addTo(map);
		
        // Add overlay control
		var overlayData = {
			"Player Markers": playerMarkers,
			"Structure Borders": structureBorders,
			"Deed Borders": deedBorders,
			"Deed Markers": deedMarkers,
			"Guard Tower Markers": guardtowerMarkers,
			"Guard Tower Borders": guardtowerBorders
		};
		
		L.control.layers(null, overlayData).addTo(map);
		
		// Add coordinates display
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
	}
};