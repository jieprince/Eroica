package com.sendtomoon.eroica.eoapp.esa.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;

import com.sendtomoon.eroica.common.app.dto.ServiceRequest;

public abstract class AbstractESAFilter implements ESAFilter,Ordered{
	
	//private List<String> filtActionNames;
	
	private Set<String> includeSet;
	
	private Set<String> excludeSet;
	
	
	private int order = 0;
	
	//private boolean filtAll=false;
	
	protected  Log logger= LogFactory.getLog(this.getClass());

	
	/***
	 * @deprecated
	 * @return
	 */
	public List<String> getFiltActionNames() {
		if(includeSet==null){
			return null;
		}
		String [] arr=new String[includeSet.size()];
		includeSet.toArray(arr);
		return Arrays.asList(arr);
	}

	/***
	 *  @deprecated
	 * @param actionNames
	 */
	public void setFiltActionNames(List<String> actionNames) {
		if(actionNames==null || actionNames.size()==0){
			this.includeSet=null;
		}
		HashSet<String> set=new HashSet<String>();
		for(String an:actionNames){
			set.add(an);
		}
		this.includeSet=set;
	}

	/***
	 *  @deprecated
	 * @param actionName
	 */
	public void setFiltActionName(String actionName) {
		HashSet<String> set=new HashSet<String>();
		set.add(actionName);
		this.includeSet=set;
	}

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	/***
	 * @deprecated
	 * @param filtAll
	 */
	public boolean isFiltAll() {
		//return includeAll;
		return true;
	}
	/***
	 * @deprecated
	 * @param filtAll
	 */
	public void setFiltAll(boolean filtAll) {
		//this.includeAll = filtAll;
	}

	@Override
	public boolean match(ServiceRequest request) throws Throwable {
		if(request==null){
			return false;
		}
		String actionName=request.getRequestedServiceID();
		if(actionName==null){
			return false;
		}
		Set<String> excludeSet=this.getExcludeSet();
		if(excludeSet!=null && excludeSet.size()>0){
			if(excludeSet.contains(actionName)){
				return false;
			}else{
				return true;
			}
		}else{
			Set<String> includeSet=this.getIncludeSet();
			if(includeSet!=null && includeSet.size()>0){
				return includeSet.contains(actionName);
			}else{
				//包括所有
				return true;
			}
		}
	}

	public Set<String> getIncludeSet() {
		return includeSet;
	}

	public void setIncludeSet(Set<String> includeSet) {
		this.includeSet = includeSet;
	}

	public Set<String> getExcludeSet() {
		return excludeSet;
	}

	public void setExcludeSet(Set<String> excludeSet) {
		this.excludeSet = excludeSet;
	}

	
	
}
