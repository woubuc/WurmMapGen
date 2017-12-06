<?php

	$GLOBALS['RMI_ADDRESS'] = "localhost:8080";

	function sendCommand($cmd) {
		$lines = file('http://'.$GLOBALS['RMI_ADDRESS'].'/'.$cmd);
		foreach ($lines as &$line) {
			$line = trim($line);
		}
		if (isset($lines[0]) && $lines[0] == "") {
			return array();
		}
		return $lines;
	}

	function parseArray($array, $explode = true, $nameKey = false) {
		if ($explode) {
			foreach ($array as $k => $v) {
				$tmp = explode("=", $v);
				if ($nameKey) {
                			$new[$tmp[0]] = $tmp[1];
            			} else {
                			$array[$k] = array($tmp[0], $tmp[1]);;
            			}
        		}
        		if ($nameKey) {
            			return $new;
        		}
    		}
    		return $array;
	}

	function get_string_between($string, $start, $end) {
		$string = ' ' . $string;
		$ini = strpos($string, $start);
		if ($ini == 0) return '';
		$ini += strlen($start);
		$len = strpos($string, $end, $ini) - $ini;
		return substr($string, $ini, $len);
	}

	function getOnlinePlayers() {
		$players = parseArray(sendCommand("getOnlinePlayers"));
		foreach ($players as &$player) {
			$player[1] = explode(",", get_string_between($player[1], "[", "]"));
		}
		return $players;
	}
	
	function getAllGuardTowers() {
		$guardTowers = parseArray(sendCommand("getAllGuardTowers"));
		foreach ($guardTowers as &$guardTower) {
			$guardTower[1] = explode(",", get_string_between($guardTower[1], "[", "]"));
		}
		return $guardTowers;
	}
	
	function getAllStructures() {
		$structures = parseArray(sendCommand("getAllStructures"));
		foreach ($structures as &$structure) {
			$structure[1] = parseArray(sendCommand("getStructureSummary?" . $structure[0]), true, true);
		}
		return $structures;
	}
?>