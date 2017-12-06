'use strict';

// IIFE scope
(function() {

	// Prepare arrays for loaded data
	WurmMapGen.config = null;

	WurmMapGen.villages = null;
	WurmMapGen.guardtowers = null;
	WurmMapGen.structures = null;

	// Helper function to fetch a dataset from a JSON file
	function fetchData(key) {
		return fetch('data/' + key + '.json')
			.then(function(response) { return response.json() })
			.then(function(responseData) {
				WurmMapGen[key] = responseData[key];
				return Promise.resolve();
			});
	}

	// Load data
	Promise.all([
		fetchData('config'),
		fetchData('villages'),
		fetchData('guardtowers'),
		fetchData('structures')
	])
	.catch(function(err) {
		console.error(err);
		document.write('Something went wrong, map data could not be loaded'); // TODO add better error handling
	})
	.then(function() {
		// Add computed config values
		WurmMapGen.config.xyMulitiplier = (WurmMapGen.config.actualMapSize / 256);

		// Create the map
		WurmMapGen.map.create();
	});
})();
