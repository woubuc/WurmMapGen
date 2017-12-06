<!DOCTYPE html>
<html>
	<head>
		<title>WU MapGen</title>
		<meta charset="utf-8"/>
		<meta name="viewport" content="initial-scale=1.0, user-scalable=no"/>
		<link rel="stylesheet" href="https://unpkg.com/leaflet@1.1.0/dist/leaflet.css" integrity="sha512-wcw6ts8Anuw10Mzh9Ytw4pylW8+NAD4ch3lqm9lzAsTxg0GFeJgoAtxuCLREZSC5lUXdVyo/7yfsqFjQ4S+aKw==" crossorigin=""/>
		<link rel="stylesheet" href="dist/Leaflet.Coordinates-0.1.5.css"/>
		<script src="https://unpkg.com/leaflet@1.1.0/dist/leaflet.js" integrity="sha512-mNqn2Wg7tSToJhvHcqfzLMU6J4mkOImSPTxVZAdo+lcPlk+GhZmYgACEe0x35K7YzW1zJ7XyJV/TT1MrdXvMcA==" crossorigin=""></script>
		<script type="text/javascript" src="dist/Leaflet.Coordinates-0.1.5.min.js"></script>
		<script type="text/javascript" src="includes/config.js"></script>
		<script type="text/javascript" src="includes/markers.js"></script>
		<script type="text/javascript" src="includes/deeds.js"></script>
		<script type="text/javascript" src="includes/players.php"></script>
		<script type="text/javascript" src="includes/structures.php"></script>
		<script type="text/javascript" src="includes/guardtowers.php"></script>
		<script>
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

			L.control.layers(null, overlayData).addTo(map);
			
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

			setViewOnMainDeed(map);

		}
		</script>
		<style>
			html, body, #map { width:100%; height:100%; margin:0; padding:0; z-index: 1; }
			img { 
				image-rendering: optimizeSpeed;			/* STOP SMOOTHING, GIVE ME SPEED	*/
				image-rendering: -moz-crisp-edges;		/* Firefox				*/
				image-rendering: -o-crisp-edges;		/* Opera				*/
				image-rendering: -webkit-optimize-contrast;	/* Chrome (and eventually Safari)	*/
				image-rendering: pixelated;			/* Chrome				*/
				image-rendering: optimize-contrast;		/* CSS3 Proposed			*/
				-ms-interpolation-mode: nearest-neighbor;	/* IE8+					*/
			}
		</style>
	</head>
	<body onload="init()">
		<div id="map"></div>
	</body>
</html>