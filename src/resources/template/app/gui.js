'use strict';

(function() {

WurmMapGen.gui = new Vue({
	el: '#gui',

	data: {
		sidebarVisible: (window.innerWidth > 1200),
		loaded: false,

		showStructures: true,
		showPlayers: true,

		showVillages: true,
		showVillageBorders: true,

		showTowers: true,
		showTowerBorders: true
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
			this.showStructures = WurmMapGen.util.getConfig('structureBorders', true);

			this.showVillages = WurmMapGen.util.getConfig('villageMarkers', true);
			this.showVillageBorders = WurmMapGen.util.getConfig('villageBorders', true);

			this.showTowers = WurmMapGen.util.getConfig('guardtowerMarkers', true);
			this.showTowerBorders = WurmMapGen.util.getConfig('guardtowerBorders', false);

			this.loaded = true;
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
