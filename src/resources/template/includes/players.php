<?php
	require 'rmi.php';
?>
function getPlayerMarkers() {
	var playerMarkers = [];
	<?php
		$players = getOnlinePlayers();
		foreach ($players as $player) {
			echo("\tplayerMarkers.push(L.marker(xy(".($player[1][1] + 0.5).",".($player[1][2] + 0.5)."),{icon: playerIcon}).bindPopup(\"".$player[1][0]."\"));\n");
		}
	?>
	return playerMarkers;
}