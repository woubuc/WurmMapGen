<?php
	require 'rmi.php';
?>
function getGuardTowerBorders() {
	var guardTowerBorders = [];
	<?php
		$gts = getAllGuardTowers();
		foreach ($gts as $gt) {
		echo("\tguardTowerBorders.push(L.polygon([xy(".($gt[1][0] - 50).",".($gt[1][1] - 50).
		"),xy(".($gt[1][0] + 51).",".($gt[1][1] - 50).
		"),xy(".($gt[1][0] + 51).",".($gt[1][1] + 51).
		"),xy(".($gt[1][0] - 50).",".($gt[1][1] + 51).
		")], {color:'red',fillOpacity:0.1,weight:1}));\n");
		}
	?>
	return guardTowerBorders;
}

function getGuardTowers() {
	var guardTower = [];
	<?php
		$gts = getAllGuardTowers();
		foreach ($gts as $gt) {
			echo("\tguardTower.push(L.marker(xy(".($gt[1][0] + 0.5).",".($gt[1][1] + 0.5).
			"),{icon: guardTowerIcon}).bindPopup(\"<div align='center'><b>Guard Tower</b><br><i>Created by ".$gt[1][3]."</i></div><br><b>QL:</b> ".(round($gt[1][4], 2))."<br><b>DMG:</b> ".(round($gt[1][5], 2))."\"));\n");
		}
	?>
	return guardTower;
}