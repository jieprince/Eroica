package com.pingan.pafa.pizza.classloader;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.sendtomoon.eroica.pizza.Pizza;
import com.sendtomoon.eroica.pizza.classloader.ClasspathResourceContent;
import com.sendtomoon.eroica.pizza.classloader.ClasspathResourceKey;
import com.sendtomoon.eroica.pizza.classloader.PizzaClassLoader;

public class PizzaClassLoaderTests {

	@Test
	public void testPizzaResource() throws Exception {
		ClasspathResourceContent content = ClasspathResourceContent
				.create(IOUtils.toByteArray(this.getClass().getResourceAsStream("merchant.xml")));
		String sarName = "pafa5_sample_helloworld";
		ClasspathResourceKey key = ClasspathResourceKey.valueOf(sarName, "merchant.xml");
		Pizza.getManager().set(Pizza.GROUP_RESOURCES, key.toString(), content.toJSONString());
		File dir = new File(this.getClass().getResource("/").getFile());
		dir.mkdirs();
		PizzaClassLoader test = new PizzaClassLoader(sarName, dir.toURI().toURL());
		URL url = test.getResource("merchant.xml");
		System.err.println("URL=" + url);
		System.err.println("URL=" + url.getFile());
		File fileout = new File(dir, key.toString());
		FileUtils.writeStringToFile(fileout, content.toJSONString());
	}

}
