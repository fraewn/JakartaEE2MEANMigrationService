package com.migration.service.service.util;

import java.io.File;

import org.ini4j.Ini;

public class EnvironmentUtils {
	public String getCredential(String infrastructure, String key) throws Exception {
		Ini ini = new Ini(new File("conf.ini"));
		return ini.get(infrastructure, key);
	}

	public String getEnvironment(String sectionName, String key_ip, String key_port, String key_portType)
			throws Exception {
		Ini ini = new Ini(new File("conf.ini"));
		String portType = ini.get(sectionName, key_portType);
		String ip = ini.get(sectionName, key_ip);
		String port = ini.get(sectionName, key_port);
		return portType + "://" + ip + ":" + port;
	}
}
