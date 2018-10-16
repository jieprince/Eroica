package com.sendtomoon.eroica.eoapp.protocol;

import com.sendtomoon.eroica.eoapp.esa.ESADefinition;
import com.sendtomoon.eroica.eoapp.sar.SARContext;



public interface ESAExporter {
	
	 boolean export(SARContext sar,ESADefinition  definition);
	 
	 
	 boolean unexport(SARContext sar,String esaName);
	 
}
