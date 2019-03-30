package com.sendtomoon.eroica.pizza.classloader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;

import com.sendtomoon.eroica.pizza.PizzaConstants;
import com.sendtomoon.eroica.pizza.PizzaException;
import com.sendtomoon.eroica.pizza.PizzaManager;

public class PizzaContentUtils {

	static InputStream toIputStream(PizzaManager mananger, PizzaURL pizzaURL) {
		return toIputStream(mananger, pizzaURL, true);
	}

	static String getTextContent(PizzaManager mananger, PizzaURL pizzaURL, boolean requriedExists) {
		if (pizzaURL.isBase64Content()) {
			throw new PizzaException("Pizza Resource [" + pizzaURL + "] not be text content.");
		}
		String content = mananger.get(pizzaURL.toPizzaPath());
		if (content == null) {
			if (requriedExists) {
				throw new PizzaException(
						"Pizza resource [" + pizzaURL.toString() + "] cannot be opened because it does not exist");
			} else {
				return null;
			}
		}

		return content;
	}

	static InputStream toIputStream(PizzaManager mananger, PizzaURL pizzaURL, boolean requriedExists) {
		String content = mananger.get(pizzaURL.toPizzaPath());
		if (content == null) {
			if (requriedExists) {
				throw new PizzaException(
						"Pizza resource [" + pizzaURL.toString() + "] cannot be opened because it does not exist");
			} else {
				return null;
			}
		}
		return toIputStream(pizzaURL, content);
	}

	public static InputStream toIputStream(PizzaURL pizzaURL, final String pizzaContent) {
		if (pizzaContent == null) {
			throw new PizzaException("Pizza resource [" + pizzaURL.toString() + "] error,content be empty.");
		}
		if (PizzaConstants.GROUP_RESOURCES.equals(pizzaURL.getPizzaGroup())) {
			try {
				ClasspathResourceContent content = ClasspathResourceContent.fromJSONString(pizzaContent);
				byte[] fileContent = content.getBase64datas().getBytes();
				fileContent = Base64.decodeBase64(fileContent);
				return new ByteArrayInputStream(fileContent);
			} catch (Throwable th) {
				throw new PizzaException("PizzaResource@" + pizzaURL + " content format error,cause:" + th.getMessage(),
						th);
			}
		} else if (pizzaURL.isBase64Content()) {
			try {
				byte[] fileContent = pizzaContent.getBytes();
				fileContent = Base64.decodeBase64(fileContent);
				return new ByteArrayInputStream(fileContent);
			} catch (Throwable th) {
				throw new PizzaException("PizzaResource@" + pizzaURL + " decodeBase64 failure,cause:" + th.getMessage(),
						th);
			}
		} else {
			return new ByteArrayInputStream(pizzaContent.getBytes(pizzaURL.getCharset()));
		}
	}
}
