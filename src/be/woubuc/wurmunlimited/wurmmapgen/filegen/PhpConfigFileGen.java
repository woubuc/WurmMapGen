package be.woubuc.wurmunlimited.wurmmapgen.filegen;

import be.woubuc.wurmunlimited.wurmmapgen.Logger;
import be.woubuc.wurmunlimited.wurmmapgen.WurmMapGen;

public final class PhpConfigFileGen extends FileGen {
	
	public PhpConfigFileGen() {
		setFileName("config.php");
	}
	
	@Override
	protected String generateData() {
		Logger.title("PHP config data");
		return String.format("<?php\n$conf_rmi_host = '%s';\n$conf_rmi_port = '%s';\n?>",
				WurmMapGen.properties.rmiHost.replace("'", "\\'"),
				WurmMapGen.properties.rmiPort.replace("'", "\\'")
		);
	}
	
}
