package com.sendtomoon.eroica.eoapp.sar;

import java.util.Collection;

import com.sendtomoon.eroica.common.app.biz.ac.ApplicationControllerLocal;
import com.sendtomoon.eroica.eoapp.esa.ESADefinition;

public interface SARDispatcher extends  ApplicationControllerLocal {

	 
	Collection<ESADefinition> getESADefinitions();
	
}
