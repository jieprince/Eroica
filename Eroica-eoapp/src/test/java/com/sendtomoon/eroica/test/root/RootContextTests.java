package com.sendtomoon.eroica.test.root;

import com.sendtomoon.eroica.pizza.Pizza;

public class RootContextTests {

	public static void main(String args[]){
		System.setProperty("eoapp.name", "test");
		//-------
		System.out.println(Pizza.getPizzaContext());
		
	}
}
