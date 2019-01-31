package com.sendtomoon.eroica.fling.root;

import com.sendtomoon.eroica.pizza.Pizza;

public class RootContextTests {

	public static void main(String args[]) {
		System.setProperty("eoapp.name", "test");
		System.setProperty("fling.cmd.enable", "false");
		System.out.println(Pizza.getManager());
	}
}
