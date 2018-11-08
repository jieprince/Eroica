package com.sendtomoon.eroica.dubbo.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import com.sendtomoon.eroica.common.beans.json.JsonMapUtils;
import com.sendtomoon.eroica.common.web.WebException;
import com.sendtomoon.eroica.common.web.util.JsonToFormParamsUtils;

public class WeServiceRequest implements  HttpServletRequest{
	
	private Map<String,String[]> headers;
	
	private Cookie[] cookies;
	
	private String method;
	
	private String charset;
	
	private String queryString;
	
	private int contentLength=-1;
	
	private ServletContext servletContext;
	
	private Map<String,String[]> parameters;
	
	private String path;
	
	private String scheme;
	
	private int serverPort;
	
	private String serverName;
	
	private String remoteAddr;
	
	
	private Map<String,Object> attributes=new LinkedHashMap<String,Object>();
	
	public WeServiceRequest(ServletContext servletContext){
		this.servletContext=servletContext;
	}
	
	public WeServiceRequest(Map<Object,Object> model,ServletContext servletContext){
		this.servletContext=servletContext;
		this.headers=(Map<String,String[]>)model.get("headers");
		if(headers==null){
			headers=new LinkedHashMap<String,String[]>();
		}
		this.method=(String)model.get("method");
		this.charset=(String)model.get("charset");
		this.contentLength=Integer.parseInt((String)model.get("contentLength"));
		Map<Object,Object>[] cookiesMap=(Map<Object,Object>[])model.get("cookies");
		if(cookiesMap!=null && cookiesMap.length>0){
			Cookie[] cookies=new Cookie[cookiesMap.length];
			for(int i=0;i<cookiesMap.length;i++){
				cookies[i]=new WeServiceCookie(cookiesMap[i]).toHttpCookie();
			}
			this.cookies=cookies;
		}
		this.parameters=(Map<String,String[]>)model.get("parameters");
		if(parameters==null){
			parameters=new LinkedHashMap<String,String[]>();
		}
		this.path=(String)model.get("path");
		this.remoteAddr=(String)model.get("remoteAddr");
		this.queryString=(String)model.get("queryString");
		this.scheme=(String)model.get("scheme");
		this.serverName=(String)model.get("serverName");
		this.serverPort=Integer.parseInt((String)model.get("serverPort"));
	}
	
	public Map<Object,Object> toModel(){
		Map<Object,Object> model=new LinkedHashMap<Object,Object>();
		model.put("headers", headers);
		model.put("method", method);
		model.put("charset", charset);
		model.put("contentLength", String.valueOf(contentLength));
		if(cookies!=null && cookies.length>0){
			Map<?,?>[] cookiesArr=new LinkedHashMap[cookies.length];
			for(int i=0;i<cookies.length;i++){
				cookiesArr[i]=new WeServiceCookie(cookies[i]).toModel();
			}
			model.put("cookies",cookiesArr);
		}
		model.put("parameters", this.parameters);
		model.put("path", path);
		model.put("remoteAddr", remoteAddr);
		model.put("queryString", queryString);
		model.put("scheme", scheme);
		model.put("serverName", serverName);
		model.put("serverPort", String.valueOf(serverPort));
		return model;
	}
	
	public String toString(){
		return this.toModel().toString();
	}
	
	public WeServiceRequest(HttpServletRequest request,String path){
		Map<String,String[]> headers=new LinkedHashMap<String,String[]>();
		Enumeration<String> headerNames=request.getHeaderNames();
		while(headerNames.hasMoreElements()){
			String name=headerNames.nextElement();
			Enumeration<String> values=request.getHeaders(name);
			List<String> list=new ArrayList<String>();
			while(values.hasMoreElements()){
				list.add(values.nextElement());
			}
			String[] arr=new String[list.size()];
			list.toArray(arr);
			headers.put(name, arr);
		}
		this.headers=headers;
		//----------------------------
		this.method=request.getMethod();
		this.charset=request.getCharacterEncoding();
		this.contentLength=request.getContentLength();
		this.cookies=request.getCookies();
		parameters=new LinkedHashMap<String,String[]>();
		Map<String,String[]> params=request.getParameterMap();
		if(params!=null){
			parameters.putAll(params);
		}
		String contentType=request.getContentType();
		if("POST".equals(method) && contentType!=null && contentType.length()>0){
			MediaType type=MediaType.parseMediaType(contentType);
			if(MediaType.APPLICATION_JSON.includes(type)){
				params=readParameters(request);
				if(params!=null)parameters.putAll(params);
			}
		}
		this.path=path;
		this.remoteAddr=request.getRemoteAddr();
		this.queryString=request.getQueryString();
		//
		this.scheme=request.getScheme();
		this.serverName=request.getServerName();
		this.serverPort=request.getServerPort();
		this.servletContext=request.getServletContext();
	}
	
	private Map<String,String[]> readParameters(HttpServletRequest request){
		String requestParams=null;
		try {
			BufferedReader reader=request.getReader();
			StringWriter out=new StringWriter(128);
			//------------------------------------
			char[] buffer = new char[512];
			int bytesRead = -1;
			while ((bytesRead = reader.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			requestParams=out.toString();
		} catch (IOException e) {
			throw new WebException(e.getMessage(),e);
		}
		if(StringUtils.hasText(requestParams)){
			Map jsonMap=null;
			try{
				 jsonMap=JsonMapUtils.toHashMap(requestParams);
			}catch(Exception ex){
				throw new WebException("PostJson["+requestParams+"] format error:"+ex.getMessage(),ex);
			}
			JsonToFormParamsUtils utils=new JsonToFormParamsUtils();
			return utils.toHttpParams(jsonMap);
		}else{
			return null;
		}
	}
	
	
	

	@Override
	public String getAuthType() {
		throw new java.lang.UnsupportedOperationException("Not support method:getAuthType");
	}

	@Override
	public Cookie[] getCookies() {
		return cookies;
	}
	
	@Override
	public long getDateHeader(String name) {
		throw new java.lang.UnsupportedOperationException("Not support method:getAuthType");
	}

	@Override
	public String getHeader(String name) {
		String[] values=headers.get(name);
		if(values==null || values.length==0){
			return null;
		}
		return values[0];
	}
	
	

	@Override
	public Enumeration<String> getHeaders(String name) {
		final String[] values=headers.get(name);
		if(values==null || values.length==0){
			return null;
		}
		return new ArrayEnumeration<String>(values);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return new IteratorEnumeration<String>(headers.keySet().iterator());
	}

	@Override
	public int getIntHeader(String name) {
		String header=getHeader(name);
		return header==null?null:Integer.parseInt(header);
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getPathInfo() {
		return path;
	}

	@Override
	public String getPathTranslated() {
		return path;
	}

	@Override
	public String getContextPath() {
		return servletContext.getContextPath();
	}

	@Override
	public String getQueryString() {
		return this.queryString;
	}

	@Override
	public String getRemoteUser() {
		throw new java.lang.UnsupportedOperationException("Not support method:getRemoteUser");
	}

	@Override
	public boolean isUserInRole(String role) {
		throw new java.lang.UnsupportedOperationException("Not support method:isUserInRole");
	}

	@Override
	public Principal getUserPrincipal() {
		throw new java.lang.UnsupportedOperationException("Not support method:getUserPrincipal");
	}

	@Override
	public String getRequestedSessionId() {
		return null;
	}

	@Override
	public String getRequestURI() {
		return this.getContextPath()+path;
	}

	@Override
	 public StringBuffer getRequestURL() {
		StringBuffer url = new StringBuffer(this.scheme);
		url.append("://").append(this.serverName).append(':').append(this.serverPort);
		url.append(getRequestURI());
		return url;
	}

	@Override
	public String getServletPath() {
		return "";
	}

	@Override
	public HttpSession getSession(boolean create) {
		if(create){
			throw new java.lang.UnsupportedOperationException("Not support method:getSession");
		}
		return null;
	}

	@Override
	public HttpSession getSession() {
		return getSession(true);
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		throw new java.lang.UnsupportedOperationException("Not support method:authenticate");
	}

	@Override
	public void login(String username, String password) throws ServletException {
		throw new java.lang.UnsupportedOperationException("Not support method:login");
	}

	@Override
	public void logout() throws ServletException {
		throw new java.lang.UnsupportedOperationException("Not support method:logout");
	}

	@Override
	public Collection<Part> getParts() throws IllegalStateException, IOException, ServletException {
		throw new java.lang.UnsupportedOperationException("Not support method:getParts");
	}

	@Override
	public Part getPart(String name) throws IllegalStateException, IOException, ServletException {
		throw new java.lang.UnsupportedOperationException("Not support method:getPart");
	}

	
	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new IteratorEnumeration<String>(this.attributes.keySet().iterator());
	}

	@Override
	public String getCharacterEncoding() {
		return charset;
	}

	@Override
	public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
		this.charset=enc;
	}

	@Override
	public int getContentLength() {
		return contentLength;
	}

	@Override
	public String getContentType() {
		return MediaType.APPLICATION_FORM_URLENCODED_VALUE.toString();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		throw new java.lang.UnsupportedOperationException("Not support method:getInputStream");
	}

	@Override
	public String getParameter(String name) {
		String[] values=parameters.get(name);
		if(values!=null && values.length>0){
			return values[0];
		}
		return null;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return this.parameters;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return new IteratorEnumeration<String>(parameters.keySet().iterator());
	}

	@Override
	public String[] getParameterValues(String name) {
		return parameters.get(name);
	}

	@Override
	public String getProtocol() {
		return this.scheme;
	}

	@Override
	public String getScheme() {
		return scheme;
	}

	@Override
	public String getServerName() {
		return serverName;
	}

	@Override
	public int getServerPort() {
		return serverPort;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		throw new java.lang.UnsupportedOperationException("Not support method:getReader()");
	}

	@Override
	public String getRemoteAddr() {
		return remoteAddr;
	}

	@Override
	public String getRemoteHost() {
		return remoteAddr;
	}

	@Override
	public void setAttribute(String name, Object o) {
		this.attributes.put(name,o);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return null;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return this.getServletContext().getRequestDispatcher(path);
		//throw new java.lang.UnsupportedOperationException("Not support method:getRequestDispatcher");
	}

	@Override
	public String getRealPath(String path) {
		return this.getServletContext().getRealPath(path);
	}

	@Override
	public int getRemotePort() {
		return 0;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public String getLocalAddr() {
		return null;
	}

	@Override
	public int getLocalPort() {
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		return this.servletContext;
	}

	@Override
	public AsyncContext startAsync() {
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		throw new java.lang.UnsupportedOperationException("Not support method:getAsyncContext");
	}

	

	@Override
	public DispatcherType getDispatcherType() {
		throw new java.lang.UnsupportedOperationException("Not support method:getDispatcherType");
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	

}

class ArrayEnumeration<T> implements Enumeration<T>{
	
	private T array[];
	
	private int i;
	
	public ArrayEnumeration(T array[]){
		this.array=array;
	}

	@Override
	public boolean hasMoreElements() {
		return i<array.length;
	}

	@Override
	public T nextElement() {
		return array[i++];
	}
	
}

class IteratorEnumeration<T> implements Enumeration<T>{
	
	private Iterator<T> iterator;
	
	
	public IteratorEnumeration(Iterator<T> iterator){
		this.iterator=iterator;
	}

	@Override
	public boolean hasMoreElements() {
		return iterator.hasNext();
	}

	@Override
	public T nextElement() {
		return iterator.next();
	}
	
}
