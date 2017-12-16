<?php

class WurmRMI {

	private $host;
	private $port;

	/**
	 * Creates a new RMI connection
	 *
	 * @param  string  $host  The hostname to connect to
	 * @param  string  $port  The port to connect to
	 */
	public function __construct($host = 'localhost', $port = '8080') {
		$this->host = $host;
		$this->port = $port;
	}

	/**
	* Sends an RMI command to the Wurm server
	*
	* @param  string  $cmd  The command to send
	*
	* @return  string[]  The RMI data response
	*/
	public function sendCommand($cmd) {
		// Load RMI response
		$lines = file($this->getUrl($cmd));

		// Trim whitespace from each line
		foreach ($lines as &$line) {
			$line = trim($line);
		}

		// If RMI returned no data, return empty array
		if (count($lines) == 1 && $lines[0] == '') {
			return array();
		}

		// Parse data into key-value array
		$output = array();
		foreach ($lines as $key => $value) {
			$parts = explode('=', $value);
			$output[$parts[0]] = $parts[1];
		}
		return $output;
	}

	/**
	* Parses an array string as returned by the WebRMI interface (`[a,b,c]`)
	* into an array
	*
	* @param  string  $string  The array string
	*
	* @return  string[]  The parsed array
	*/
	public function parseArrayString($string) {
		if (substr($string, 0, 1) == '[') {
			$string = substr($string, 1);
		}
		if (substr($string, -1, 1) == ']') {
			$string = substr($string, 0, -1);
		}

		return explode(',', $string);
	}

	/**
	* Returns the WebRMI url for a given path
	*
	* @param  string  $path  The request path
	*
	* @return  string  The formatted URL
	*/
	private function getUrl($path) {
		return sprintf('http://%s:%s/%s', $this->host, $this->port, $path);
	}
}

?>
