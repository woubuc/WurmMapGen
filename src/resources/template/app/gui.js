'use strict';

(function() {

WurmMapGen.gui = new Vue({
	el: '#gui',

	data: {
		sidebarVisible: (window.innerWidth > 1200),
		loaded: false,

		searchQuery: '',

		playerCount: 0,

		showStructures: true,
		showPlayers: true,

		showVillages: true,
		showVillageBorders: true,

		showTowers: true,
		showTowerBorders: true
	},

	computed: {
		playerCountLabel: function() {
			return (this.playerCount === 1 ? '1 player online' : this.playerCount + ' players online');
		},

		searchResultsOpen: function() {
			return (this.searchResults.length > 0);
		},

		searchResults: function() {
			// Return empty results if no search query is given
			if (this.searchQuery.length < 1) {
				return [];
			}

			var escapeHtml = WurmMapGen.util.escapeHtml;

			var query = this.searchQuery.toLowerCase();
			var results = [];
			var i, index;

			// Find online players who match the search query
			if (WurmMapGen.players) {
				for (i = 0; i < WurmMapGen.players.length; i++) {
					var player = WurmMapGen.players[i];

					var name = escapeHtml(player.name);

					if ((index = name.toLowerCase().indexOf(query)) > -1) {
						results.push({
							type: 'player',
							x: player.x,
							y: player.y,
							label: '<p>' + name.slice(0, index) + '<strong>' + name.slice(index, index + query.length) + '</strong>' + name.slice(index + query.length) + '</p>'
						});
					}

					if (results.length >= 8) {
						break;
					}
				}

				if (results.length >= 8) {
					return results;
				}
			}

			// Find villages that match the search query
			for (i = 0; i < WurmMapGen.villages.length; i++) {
				var village = WurmMapGen.villages[i];

				var name = escapeHtml(village.name);
				var mayor = escapeHtml(village.mayor);

				if ((index = name.toLowerCase().indexOf(query)) > -1) {
					results.push({
						type: 'village',
						x: village.x,
						y: village.y,
						label: '<p>' + name.slice(0, index) + '<strong>' + name.slice(index, index + query.length) + '</strong>' + name.slice(index + query.length) + '</p><p class="small">Mayor: ' + mayor + '</p>'
					});
				} else if ((index = mayor.toLowerCase().indexOf(query)) > -1) {
					results.push({
						type: 'village',
						x: village.x,
						y: village.y,
						label: '<p>' + name + '</p><p class="small">Mayor: ' + mayor.slice(0, index) + '<strong>' + mayor.slice(index, index + query.length) + '</strong>' + mayor.slice(index + query.length) + '</p>'
					});
				}

				if (results.length >= 8) {
					break;
				}
			}

			return results;
		}
	},

	watch: {
		showStructures: function(value) {
        	this._setMapLayer('structureBorders', value);
        },
		showPlayers: function(value) {
        	this._setMapLayer('playerMarkers', value);
        },

		showVillages: function(value) {
			this._setMapLayer('villageMarkers', value);

			if (value === false) {
				this._setMapLayer('villageBorders', false);
			} else {
				this._setMapLayer('villageBorders', this.showVillageBorders);
			}
		},
		showVillageBorders: function(value) {
			if (this.showVillages === true) {
				this._setMapLayer('villageBorders', value);
			} else {
				// Don't show the map layer but still save the setting
				WurmMapGen.util.setConfig('villageBorders', value);
			}
		},

		showTowers: function(value) {
			this._setMapLayer('guardtowerMarkers', value);

			if (value === false) {
				this._setMapLayer('guardtowerBorders', false);
			} else {
				this._setMapLayer('guardtowerBorders', this.showTowerBorders);
			}
		},
		showTowerBorders: function(value) {
			if (this.showTowers === true) {
				this._setMapLayer('guardtowerBorders', value);
			} else {
				// Don't show the map layer but still save the setting
				WurmMapGen.util.setConfig('guardtowerBorders', value);
			}
		}
	},

	methods: {
		/**
		 * Initialises the GUI. Should be called once when the rest of the
		 * application has been loaded and initialised.
		 */
		init: function() {
			if (WurmMapGen.players) {
				this.playerCount = WurmMapGen.players.length;
			}

			this.showStructures = WurmMapGen.util.getConfig('structureBorders', true);

			this.showVillages = WurmMapGen.util.getConfig('villageMarkers', true);
			this.showVillageBorders = WurmMapGen.util.getConfig('villageBorders', true);

			this.showTowers = WurmMapGen.util.getConfig('guardtowerMarkers', true);
			this.showTowerBorders = WurmMapGen.util.getConfig('guardtowerBorders', false);

			this.loaded = true;
		},

		/**
		 * Focuses the map on coordinates
		 *
		 * @param  {number}  x  The x coordinate
		 * @param  {number}  y  The y coordinate
		 */
		focusMap: function(x, y) {
			WurmMapGen.map.map.setView(WurmMapGen.util.xy(x, y), WurmMapGen.config.mapMaxZoom - 1);
		},

		/**
		 * Enables or disables a map layer
		 *
		 * @param  {string}  name    The name of the map layer (key in the `WurmMapGen.map.layers` object)
		 * @param  {boolean}  value  True to enable the layer
		 *
		 * @private
		 */
		_setMapLayer: function(name, value) {
			var layer = WurmMapGen.map.layers[name];

			// If the layer is already set to the wanted value, then we
			// shouldn't change anything
			if (WurmMapGen.map.map.hasLayer(layer) == value) { return; }

			// Apply the changes to the map
			if (value === false) {
				WurmMapGen.map.map.removeLayer(layer);
			} else {
				WurmMapGen.map.map.addLayer(layer);
			}

			// Persist the user settings in a config cookie
			WurmMapGen.util.setConfig(name, value);
		}
	}
});

// End IIFE
})();
