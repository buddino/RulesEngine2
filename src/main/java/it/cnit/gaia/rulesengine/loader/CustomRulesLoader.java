package it.cnit.gaia.rulesengine.loader;

import org.apache.http.annotation.Experimental;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

@Experimental
public class CustomRulesLoader {
	private String classpath = "./CustomRules/";
	private File file = new File(classpath);
	private ClassLoader classLoader;

	public CustomRulesLoader() throws MalformedURLException, ClassNotFoundException {
		URL url = file.toURI().toURL();
		URL[] urls = new URL[]{url};
		classLoader = new URLClassLoader(urls);
	}

	public Class loadClass(String className) throws ClassNotFoundException {
		return classLoader.loadClass(className);
	}

	public CustomRulesLoader setClasspath(String classpath) {
		this.classpath = classpath;
		return this;
	}
}
