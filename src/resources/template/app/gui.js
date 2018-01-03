'use strict';

(function() {

WurmMapGen.gui = new Vue({
	el: '#gui',

	data: {
		sidebarVisible: (window.innerWidth > 1200),
		sidebarToggled: 0,
		mapResizeInterval: null,

		loaded: false,

		searchQuery: '',

		playerCount: 0,

		showStructures: true,
		showPlayers: true,
		showPortals: true,

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
			if (WurmMapGen.players && this.showPlayers) {
				for (i = 0; i < WurmMapGen.players.length; i++) {
					var player = WurmMapGen.players[i];

					var name = escapeHtml(player.name);

					// Check if player name matches query
					if ((index = name.toLowerCase().indexOf(query)) > -1) {
						results.push({
							type: 'player',
							x: player.x,
							y: player.y,
							label: '<p>' + name.slice(0, index) + '<strong>' + name.slice(index, index + query.length) + '</strong>' + name.slice(index + query.length) + '</p>'
						});
					}

					if (results.length >= 8) return results;
				}
			}

			// Find villages that match the search query
			if (this.showVillages) {
				for (i = 0; i < WurmMapGen.villages.length; i++) {
					var village = WurmMapGen.villages[i];

					var name = escapeHtml(village.name);
					var mayor = escapeHtml(village.mayor);

					var label = '';

					// Check if name matches query
					if ((index = name.toLowerCase().indexOf(query)) > -1) {
						label = '<p>' + name.slice(0, index) + '<strong>' + name.slice(index, index + query.length) + '</strong>' + name.slice(index + query.length) + '</p><p class="small">Mayor: ' + mayor + '</p>';

					// Check if mayor name matches query
					} else if ((index = mayor.toLowerCase().indexOf(query)) > -1) {
						label = '<p>' + name + '</p><p class="small">Mayor: ' + mayor.slice(0, index) + '<strong>' + mayor.slice(index, index + query.length) + '</strong>' + mayor.slice(index + query.length) + '</p>';
					}

					if (label != '') {
						results.push({
							type: 'village',
							x: village.x,
							y: village.y,
							label: label
						});
					}

					if (results.length >= 8) return results;
				}
			}

			// Find structures that match the search query
			if (this.showStructures) {
				for (i = 0; i < WurmMapGen.structures.length; i++) {
					var structure = WurmMapGen.structures[i];

					var name = escapeHtml(structure.name);
					var creator = escapeHtml(structure.creator);

					var label = '';

					// Check if structure name matches query
					if ((index = name.toLowerCase().indexOf(query)) > -1) {
						label = '<p>' + name.slice(0, index) + '<strong>' + name.slice(index, index + query.length) + '</strong>' + name.slice(index + query.length) + '</p><p class="small">Created by ' + creator + '</p>';

					// Check if creator name matches query
					} else if ((index = creator.toLowerCase().indexOf(query)) > -1) {
						label = '<p>' + name + '</p><p class="small">Created by ' + creator.slice(0, index) + '<strong>' + creator.slice(index, index + query.length) + '</strong>' + creator.slice(index + query.length) + '</p>';
					}

					if (label != '') {
						results.push({
							type: 'structure',
							x: (structure.borders[0] + structure.borders[2]) / 2,
							y: (structure.borders[1] + structure.borders[3]) / 2,
							label: label
						});
					}

					if (results.length >= 8) return results;
				}
			}

			if (this.showPortals) {
				for (i = 0; i < WurmMapGen.portals.length; i++) {
					var portal = WurmMapGen.portals[i];

					var name = escapeHtml(portal.name);

					if ((index = name.toLowerCase().indexOf(query)) > -1) {
						results.push({
							type: 'portal',
							x: portal.x,
							y: portal.y,
							label: '<p>' + name + '</p><p class="small">Portal</p>'
						});
					}

					if (results.length >= 8) return results;
				}
			}

			// Find guard towers that match the search query
			if (this.showTowers) {
				for (i = 0; i < WurmMapGen.guardtowers.length; i++) {
					var tower = WurmMapGen.guardtowers[i];

					var creator = escapeHtml(tower.creator);

					// Check if player name matches query
					if ((index = creator.toLowerCase().indexOf(query)) > -1) {
						results.push({
							type: 'guardtower',
							x: tower.x,
							y: tower.y,
							label: '<p>Guard tower</p><p class="small">Created by ' + creator.slice(0, index) + '<strong>' + creator.slice(index, index + query.length) + '</strong>' + creator.slice(index + query.length) + '</p>'
						});
					}

					if (results.length >= 8) return results;
				}
			}

			return results;
		}
	},

	watch: {
		// When the sidebar is toggled, update the map
		sidebarVisible: function() {
			this.sidebarToggled = Date.now();
			this.mapResizeInterval = window.setInterval(function() {
				WurmMapGen.map.map.invalidateSize({debounceMoveend: true});
				if (WurmMapGen.gui.sidebarToggled < Date.now() - 150) {
					clearInterval(WurmMapGen.gui.mapResizeInterval);
				}
			}, 10);
		},

		showStructures: function(value) {
        	this._setMapLayer('structureBorders', value);
        },
		showPortals: function(value) {
        	this._setMapLayer('portalMarkers', value);
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
			this.showPortals = WurmMapGen.util.getConfig('portalMarkers', true);

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
