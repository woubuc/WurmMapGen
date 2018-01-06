'use strict';

(function() {

var markerIcon = L.Icon.extend({options: {
	iconSize:     [32, 37], // size of the icon
	iconAnchor:   [16, 37], // point of the icon which will correspond to marker's location
	popupAnchor:  [0, -37] // point from which the popup should open relative to the iconAnchor
}});

WurmMapGen.markers = {

	/**
	 * Gets a marker icon (instance of L.Icon) for the given item and configuration
	 *
	 * @param  {string}  itemType  The type of the item, should be one of 'village', 'guardtower', 'player'.
	 * @param  {Object}  [item]    The item data, only required if itemType is 'village'.
	 *
	 * @returns  {L.Icon}  The marker icon
	 */
	getMarker: function(itemType, item) {
		if (!item) { item = {}; }

		// Classic style1 (letter-based) markers
		if (WurmMapGen.config.markerType === 1) {
			if (itemType === 'guardtower') { return WurmMapGen.markers.style1.guardtower; }
			if (itemType === 'player') { return WurmMapGen.markers.style1.player; }
			if (itemType === 'portal') { return WurmMapGen.markers.style2.portal; }

			if (item.permanent) { return WurmMapGen.markers.style1.main; }
			return WurmMapGen.markers.style1['letter_' + item.name.charAt(0).toLowerCase()];
		}

		// Updated village size markers based on citizen count
		if (WurmMapGen.config.markerType === 2) {
			if (itemType === 'guardtower') { return WurmMapGen.markers.style2.guardtower; }
			if (itemType === 'player') { return WurmMapGen.markers.style2.player; }
			if (itemType === 'portal') { return WurmMapGen.markers.style2.portal; }

			if (item.citizens < 2) { return WurmMapGen.markers.style2.village_solo; }
			if (item.citizens < 9) { return WurmMapGen.markers.style2.village_small; }
			return WurmMapGen.markers.style2.village_large;
		}

		// Text labels
		if (WurmMapGen.config.markerType === 3) {
			if (itemType === 'guardtower') { return WurmMapGen.markers.style3.guardtower; }
			if (itemType === 'player') { return WurmMapGen.markers.style3.player; }
			if (itemType === 'portal') { return WurmMapGen.markers.style3.portal; }

			return L.divIcon({
				className: 'icon-textlabel',
				html: '<div class="icon-inner">' + WurmMapGen.util.escapeHtml(item.name) + '</div>'
			});
		}

		throw new Error('Invalid marker type config value: ' + WurmMapGen.config.markerType);
	},

	// Classic style markers
	style1: {
		letter_a: new markerIcon({iconUrl: 'markers/letter_a.png'}),
		letter_b: new markerIcon({iconUrl: 'markers/letter_b.png'}),
		letter_c: new markerIcon({iconUrl: 'markers/letter_c.png'}),
		letter_d: new markerIcon({iconUrl: 'markers/letter_d.png'}),
		letter_e: new markerIcon({iconUrl: 'markers/letter_e.png'}),
		letter_f: new markerIcon({iconUrl: 'markers/letter_f.png'}),
		letter_g: new markerIcon({iconUrl: 'markers/letter_g.png'}),
		letter_h: new markerIcon({iconUrl: 'markers/letter_h.png'}),
		letter_i: new markerIcon({iconUrl: 'markers/letter_i.png'}),
		letter_j: new markerIcon({iconUrl: 'markers/letter_j.png'}),
		letter_k: new markerIcon({iconUrl: 'markers/letter_k.png'}),
		letter_l: new markerIcon({iconUrl: 'markers/letter_l.png'}),
		letter_m: new markerIcon({iconUrl: 'markers/letter_m.png'}),
		letter_n: new markerIcon({iconUrl: 'markers/letter_n.png'}),
		letter_o: new markerIcon({iconUrl: 'markers/letter_o.png'}),
		letter_p: new markerIcon({iconUrl: 'markers/letter_p.png'}),
		letter_q: new markerIcon({iconUrl: 'markers/letter_q.png'}),
		letter_r: new markerIcon({iconUrl: 'markers/letter_r.png'}),
		letter_s: new markerIcon({iconUrl: 'markers/letter_s.png'}),
		letter_t: new markerIcon({iconUrl: 'markers/letter_t.png'}),
		letter_u: new markerIcon({iconUrl: 'markers/letter_u.png'}),
		letter_v: new markerIcon({iconUrl: 'markers/letter_v.png'}),
		letter_w: new markerIcon({iconUrl: 'markers/letter_w.png'}),
		letter_x: new markerIcon({iconUrl: 'markers/letter_x.png'}),
		letter_y: new markerIcon({iconUrl: 'markers/letter_y.png'}),
		letter_z: new markerIcon({iconUrl: 'markers/letter_z.png'}),

		main: new markerIcon({iconUrl: 'markers/main.png'}),
		player: new markerIcon({iconUrl: 'markers/player.png'}),
		guardtower: new markerIcon({iconUrl: 'markers/guardtower.png'})
	},

	// New style markers
	style2: {
		village_solo: new markerIcon({iconUrl: 'markers/v2-village-solo.png'}),
		village_small: new markerIcon({iconUrl: 'markers/v2-village-small.png'}),
		village_large: new markerIcon({iconUrl: 'markers/v2-village-large.png'}),

		portal: new markerIcon({iconUrl: 'markers/v2-portal.png'}),
		player: new markerIcon({iconUrl: 'markers/v2-player.png'}),
		guardtower: new markerIcon({iconUrl: 'markers/v2-guardtower.png'})
	},

	// Simple icons to go with the text labels
	style3: {
		portal: new markerIcon({iconUrl: 'markers/v2-portal-icon.png'}),
		player: new markerIcon({iconUrl: 'markers/v2-player-icon.png'}),
		guardtower: new markerIcon({iconUrl: 'markers/v2-guardtower-icon.png'})
	}
};

// End IIFE
})();
