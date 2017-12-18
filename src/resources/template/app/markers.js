'use strict';

(function() {

var markerIcon = L.Icon.extend({options: {
	iconSize:     [32, 37], // size of the icon
	iconAnchor:   [16, 37], // point of the icon which will correspond to marker's location
	popupAnchor:  [0, -37] // point from which the popup should open relative to the iconAnchor
}});

WurmMapGen.markers = {
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
};

// End IIFE
})();
