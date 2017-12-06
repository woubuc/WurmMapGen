var markerIcon = L.Icon.extend({options: {
	iconSize:     [32, 37], // size of the icon
	iconAnchor:   [16, 37], // point of the icon which will correspond to marker's location
	popupAnchor:  [0, -37] // point from which the popup should open relative to the iconAnchor
}});

var letter_aIcon = new markerIcon({iconUrl: 'markers/letter_a.png'}),
	letter_bIcon = new markerIcon({iconUrl: 'markers/letter_b.png'}),
	letter_cIcon = new markerIcon({iconUrl: 'markers/letter_c.png'}),
	letter_dIcon = new markerIcon({iconUrl: 'markers/letter_d.png'}),
	letter_eIcon = new markerIcon({iconUrl: 'markers/letter_e.png'}),
	letter_fIcon = new markerIcon({iconUrl: 'markers/letter_f.png'}),
	letter_gIcon = new markerIcon({iconUrl: 'markers/letter_g.png'}),
	letter_hIcon = new markerIcon({iconUrl: 'markers/letter_h.png'}),
	letter_iIcon = new markerIcon({iconUrl: 'markers/letter_i.png'}),
	letter_jIcon = new markerIcon({iconUrl: 'markers/letter_j.png'}),
	letter_kIcon = new markerIcon({iconUrl: 'markers/letter_k.png'}),
	letter_lIcon = new markerIcon({iconUrl: 'markers/letter_l.png'}),
	letter_mIcon = new markerIcon({iconUrl: 'markers/letter_m.png'}),
	letter_nIcon = new markerIcon({iconUrl: 'markers/letter_n.png'}),
	letter_oIcon = new markerIcon({iconUrl: 'markers/letter_o.png'}),
	letter_pIcon = new markerIcon({iconUrl: 'markers/letter_p.png'}),
	letter_qIcon = new markerIcon({iconUrl: 'markers/letter_q.png'}),
	letter_rIcon = new markerIcon({iconUrl: 'markers/letter_r.png'}),
	letter_sIcon = new markerIcon({iconUrl: 'markers/letter_s.png'}),
	letter_tIcon = new markerIcon({iconUrl: 'markers/letter_t.png'}),
	letter_uIcon = new markerIcon({iconUrl: 'markers/letter_u.png'}),
	letter_vIcon = new markerIcon({iconUrl: 'markers/letter_v.png'}),
	letter_wIcon = new markerIcon({iconUrl: 'markers/letter_w.png'}),
	letter_xIcon = new markerIcon({iconUrl: 'markers/letter_x.png'}),
	letter_yIcon = new markerIcon({iconUrl: 'markers/letter_y.png'}),
	letter_zIcon = new markerIcon({iconUrl: 'markers/letter_z.png'}),
	mainIcon = new markerIcon({iconUrl: 'markers/main.png'}),
	playerIcon = new markerIcon({iconUrl: 'markers/player.png'}),
	guardTowerIcon = new markerIcon({iconUrl: 'markers/guardtower.png'});