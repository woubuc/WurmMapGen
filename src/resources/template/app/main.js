'use strict';

// IIFE scope
(function() {
	
	// Prepare arrays for loaded data
	WurmMapGen.data = {
		config: null,
		
		deeds: null,
		guardtowers: null,
		structures: null
	};
	
	// Helper function to fetch a dataset from a JSON file
	function fetchData(key) {
		return fetch('data/' + key + '.json')
			.then(function(response) { return response.json() })
			.then(function(responseData) {
				WurmMapGen.data[key] = responseData[key];
				return Promise.resolve();
			});
	}
	
	// Load data
	Promise.all([
		fetchData('config'),
		fetchData('deeds'),
		fetchData('guardtowers'),
		fetchData('structures')
	])
	.catch(function(err) {
		document.write('Something went wrong, map data could not be loaded'); // TODO add better error handling
	})
	.then(function() {
		// Add computed config values
		WurmMapGen.data.config.xyMulitiplier = (WurmMapGen.data.config.actualMapSize / 256);
		
		// Create the map
		WurmMapGen.map.create();
	});
})();
