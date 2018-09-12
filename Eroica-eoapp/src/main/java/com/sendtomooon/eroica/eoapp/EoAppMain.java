package com.sendtomooon.eroica.eoapp;

import java.io.IOException;

public class EoAppMain {

	public static void main(String args[]) throws IOException {
		EoApp.getInstance().startup();
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
