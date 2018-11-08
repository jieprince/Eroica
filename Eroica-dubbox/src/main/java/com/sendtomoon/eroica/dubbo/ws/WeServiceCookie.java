package com.sendtomoon.eroica.dubbo.ws;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

public class WeServiceCookie {
	
	private String name;
	private String value;
	private String comment;
	private String domain;
	private int maxAge = -1;
	private String path;
	private boolean secure;
	private int version = 0;
	private boolean httpOnly;
	
	public WeServiceCookie(){
		
	}
	
	public WeServiceCookie(Map<Object,Object> model){
		this.name=(String)model.get("name");
		this.value=(String)model.get("value");
		this.comment=(String)model.get("comment");
		this.domain=(String)model.get("domain");
		this.path=(String)model.get("path");
		String str=(String)model.get("maxAge");
		if(str!=null){
			this.maxAge=Integer.parseInt(str);
		}
		str=(String)model.get("version");
		if(str!=null){
			this.version=Integer.parseInt(str);
		}
		str=(String)model.get("secure");
		if(str!=null){
			this.secure=Boolean.parseBoolean(str);
		}
		str=(String)model.get("httpOnly");
		if(str!=null){
			this.httpOnly=Boolean.parseBoolean(str);
		}
	}
	
	protected Map<Object,Object> toModel(){
		Map<Object,Object> model=new LinkedHashMap<Object,Object>();
		model.put("name", name);
		model.put("value", value);
		if(comment!=null)model.put("comment", comment);
		if(domain!=null)model.put("domain", domain);
		if(path!=null)model.put("path", path);
		if(maxAge!=-1)model.put("maxAge", String.valueOf(maxAge));
		if(version!=0)model.put("version", String.valueOf(version));
		if(httpOnly)model.put("httpOnly", String.valueOf(httpOnly));
		if(secure)model.put("secure", String.valueOf(secure));
		return model;
	}
	
	public String toString(){
		return this.toModel().toString();
	}
	
	public WeServiceCookie(Cookie cookie){
		this.name=cookie.getName();
		this.value=cookie.getValue();
		this.comment=cookie.getComment();
		this.domain=cookie.getDomain();
		this.maxAge=cookie.getMaxAge();
		this.path=cookie.getPath();
		this.secure=cookie.getSecure();
		this.version=cookie.getVersion();
		this.httpOnly=cookie.isHttpOnly();
	}
	
	public Cookie toHttpCookie(){
		Cookie cookie=new Cookie(name,value);
		if(comment!=null)cookie.setComment(comment);
		if(domain!=null)cookie.setDomain(domain);
		if(maxAge!=-1)cookie.setMaxAge(maxAge);
		if(path!=null)cookie.setPath(path);
		if(secure)cookie.setSecure(secure);
		if(version!=0)cookie.setVersion(version);
		if(httpOnly)cookie.setHttpOnly(httpOnly);
		return cookie;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public boolean isHttpOnly() {
		return httpOnly;
	}

	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}
	
	
	

}
