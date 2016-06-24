package de.pcl.smartshirt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class ColorConfig {
	protected static String CONFIG_PATH = "config/color.properties";
	private Properties prop;
	private String selected = "";
	private static int H_LOW = 0;
	private static int S_LOW = 1;
	private static int V_LOW = 2;
	private static int H_HIGH = 3;
	private static int S_HIGH = 4;
	private static int V_HIGH = 5;


	public ColorConfig() {
		loadProperties();
	}


	private void loadProperties() {
		prop = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(CONFIG_PATH);
			
		} catch (FileNotFoundException e) {

			File f = new File(CONFIG_PATH);
			try {
				f.createNewFile();
				in = new FileInputStream(CONFIG_PATH);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
		}
		try {
			prop.load(in);
		} catch (IOException e) {
			// ignore
		}	
	}

	
	public void removeConfig(String name) {
		prop.remove(name);
		try {
			FileOutputStream out = new FileOutputStream(CONFIG_PATH);
			prop.store(out, "---No Comment---");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveConfig(String name, int hLow, int sLow, int vLow, int hHigh, int sHigh, int vHigh) {
		String value = "" + hLow + ";" + sLow + ";" + vLow + ";" + hHigh + ";" + sHigh + ";" + vHigh;
		prop.setProperty(name, value);
		try {
			FileOutputStream out = new FileOutputStream(CONFIG_PATH);
			prop.store(out, "---No Comment---");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public List<String> listConfigs() {
		Enumeration<Object> keys = prop.keys();
		LinkedList<String> list = new LinkedList<String>();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			list.add(key);
		}
		return list;
	}


	public void selectConfig(String name) {
		this.selected = name;
	}


	public int getLowerH() {
		String config = prop.getProperty(selected);
		String[] values = config.split(";");
		String value = values[H_LOW];
		return Integer.parseInt(value);
	}


	public int getLowerS() {
		String config = prop.getProperty(selected);
		String[] values = config.split(";");
		String value = values[S_LOW];
		return Integer.parseInt(value);
	}


	public int getLowerV() {
		String config = prop.getProperty(selected);
		String[] values = config.split(";");
		String value = values[V_LOW];
		return Integer.parseInt(value);
	}


	public int getUpperH() {
		String config = prop.getProperty(selected);
		String[] values = config.split(";");
		String value = values[H_HIGH];
		return Integer.parseInt(value);
	}


	public int getUpperS() {
		String config = prop.getProperty(selected);
		String[] values = config.split(";");
		String value = values[S_HIGH];
		return Integer.parseInt(value);
	}


	public int getUpperV() {
		String config = prop.getProperty(selected);
		String[] values = config.split(";");
		String value = values[V_HIGH];
		return Integer.parseInt(value);
	}
}
