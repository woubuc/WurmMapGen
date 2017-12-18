<?php

// Create RMI connection
require_once('../includes/rmi.php');
$rmi = new WurmRMI();

// Get list of players
$players = $rmi->sendCommand('getOnlinePlayers');

// Parse player data
$output = array();
foreach ($players as $key => $player) {
	$playerData = $rmi->parseArrayString($player);
	$player = array(
		'id' => $key,
		'name' => $playerData[0],
		'x' => $playerData[1],
		'y' => $playerData[2]
	);
	array_push($output, $player);
}

// Return JSON data
header('Content-Type: application/json');
echo json_encode(array('players' => $output));

?>
