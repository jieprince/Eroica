package com.pingan.pafa.pizza.zip;

import org.springframework.mock.web.MockServletContext;

import com.pingan.pafa.pola.PolaBean;
import com.sendtomoon.eroica.pizza.zip.HtmlZipWebDeployer;

public class HtmlZipWebDeployerTests {

	public void test1() throws Exception {
		PolaBean pola = new PolaBean();
		pola.setConfig("default");
		pola.init();
		HtmlZipWebDeployer deployer = new HtmlZipWebDeployer();
		deployer.setPola(pola);
		deployer.setPizzaURL("/zip/test1.zip.data");
		deployer.setServletContext(new MockServletContext());
	}

	public void test2() throws Exception {
		PolaBean pola = new PolaBean();
		pola.setConfig("default");
		pola.init();
		HtmlZipWebDeployer deployer = new HtmlZipWebDeployer();
		deployer.setPola(pola);
		deployer.setPizzaURL("pizza:/zip/test.zip.data");
		deployer.setServletContext(new MockServletContext());
	}

	public static void main(String args[]) throws Exception {
		HtmlZipWebDeployerTests test = new HtmlZipWebDeployerTests();
		test.test1();
		test.test2();
	}
}
