<?php
	require 'rmi.php';
?>
function getStructures() {
	var structureBorders = [];
	<?php
		$structures = getAllStructures();
		foreach ($structures as $structure) {
		echo("\tstructureBorders.push(L.polygon([xy(".$structure[1]['MinX'].",".$structure[1]['MinY']."),xy(".($structure[1]['MaxX']+1).",".$structure[1]['MinY']."),xy(".($structure[1]['MaxX']+1).",".($structure[1]['MaxY']+1)."),xy(".$structure[1]['MinX'].",".($structure[1]['MaxY']+1).")], {color:'blue',fillOpacity:0,weight:1}).bindPopup(\"".$structure[1]['Name']."\"));\n");
		}
	?>
	return structureBorders;
}