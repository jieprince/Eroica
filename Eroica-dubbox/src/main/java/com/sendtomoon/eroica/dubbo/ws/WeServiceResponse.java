package com.sendtomoon.eroica.dubbo.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class WeServiceResponse implements HttpServletResponse{
	
	private List<Cookie> cookies;
	
	private int status=200;
	
	private String errorMessage;
	
	private boolean committed;
	
	private String location;
	
	
	private String contentType;
	
	private String charset;
	
	private Locale locale;
	
	private Map<String,String[]> headers;
	
	private ByteArrayOutputStream content;
	
	private ServletOutputStream servletout;
	
	public WeServiceResponse(){
		headers=new LinkedHashMap<String,String[]>();
		content=new ByteArrayOutputStream(1024);
		servletout=new ServletOutputStream(){
			@Override
			public void write(int b) throws IOException {
				content.write(b);
			}
		};
	}
	
	public Map<Object,Object> toModel(){
		 Map<Object,Object> model=new LinkedHashMap<Object,Object>();
		 model.put("headers", headers);
		 if(charset!=null)model.put("charset", charset);
		 model.put("status", Integer.valueOf(status));
		 if(errorMessage!=null)model.put("errorMessage", errorMessage);
		 if(cookies!=null && cookies.size()>0){
			Map<?,?>[] cookiesArr=new LinkedHashMap[cookies.size()];
			for(int i=0;i<cookies.size();i++){
				cookiesArr[i]=new WeServiceCookie(cookies.get(i)).toModel();
			}
			model.put("cookies",cookiesArr);
		 }
		 if(content!=null && content.size()>0){
			 model.put("content", content.toByteArray());
		 }	
		 if(location!=null){
			 model.put("location", this.location);
		 }
		 return model;
	}
	
	public WeServiceResponse(Map<Object,Object> model) throws IOException{
		this.charset=(String)model.get("charset");
		Object status=model.get("status");
		if(status!=null){
			this.status=(Integer)status;
		}
		this.errorMessage=(String)model.get("errorMessage");
		this.headers=(Map<String,String[]>)model.get("headers");
		this.location=(String)model.get("location");
		Map<Object,Object>[] cookiesMap=(Map<Object,Object>[])model.get("cookies");
		if(cookiesMap!=null && cookiesMap.length>0){
			List<Cookie> cookieList=new ArrayList<Cookie>();
			for(int i=0;i<cookiesMap.length;i++){
				Map<Object,Object> cookieModel=cookiesMap[i];
				if(cookieModel==null){
					continue;
				}
				cookieList.add(new WeServiceCookie(cookieModel).toHttpCookie());
			}
			this.cookies=cookieList;
		}
		byte[] byteContent=(byte[])model.get("content");
		this.content=new ByteArrayOutputStream(1024);
		if(byteContent!=null){
			this.content.write(byteContent);
		}
		servletout=new ServletOutputStream(){
			@Override
			public void write(int b) throws IOException {
				content.write(b);
			}
		};
	}
	public void write(HttpServletResponse response) throws IOException{
		if(cookies!=null && cookies.size()>0){
			for(Cookie cookie:cookies){
				response.addCookie(cookie);
			}
		}
		if(headers!=null && headers.size()>0){
			Set<String> keys=headers.keySet();
			for(String key:keys){
				String values[]=headers.get(key);
				for(String value:values){
					response.addHeader(key, value);
				}
			}
		}
		if(charset!=null){
			response.setCharacterEncoding(charset);;
		}
		if(content!=null){
			ServletOutputStream output=response.getOutputStream();
			output.write(content.toByteArray());
			//output.flush();
		}
		if(this.location!=null){
			response.sendRedirect(location);
		}else{
			if(status!=200){
				response.sendError(status, errorMessage);
			}
		}
	}
	
	@Override
	public void addCookie(Cookie cookie) {
		if(cookies==null){
			cookies=new ArrayList<Cookie>();
		}
		cookies.add(cookie);
	}

	@Override
	public boolean containsHeader(String name) {
		return headers.get(name)!=null;
	}

	@Override
	public String encodeURL(String url) {
		try {
			return URLEncoder.encode(url,this.getCharacterEncoding());
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	@Override
	public String encodeRedirectURL(String url) {
		return encodeURL(url);
	}

	@Override
	public String encodeUrl(String url) {
		return encodeURL(url);
	}

	@Override
	public String encodeRedirectUrl(String url) {
		return encodeURL(url);
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		if (isCommitted()) {
			 throw new IllegalStateException("Cannot set error status - response is already committed");
		}
		this.status=sc;
		this.errorMessage=msg;
		this.committed = true;
	}

	@Override
	public void sendError(int sc) throws IOException {
		if (isCommitted()) {
			 throw new IllegalStateException("Cannot set error status - response is already committed");
		}
		this.status=sc;
		this.committed = true;
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		if (isCommitted()) {
			throw new IllegalStateException("Cannot send redirect - response is already committed");
		}
		this.location=location;
		setStatus(302);
		this.committed = true;
	}

	@Override
	public void setDateHeader(String name, long date) {
		throw new java.lang.UnsupportedOperationException("Not support method:setDateHeader");
	}

	@Override
	public void addDateHeader(String name, long date) {
		throw new java.lang.UnsupportedOperationException("Not support method:addDateHeader");
		
	}

	@Override
	public void setHeader(String name, String value) {
		if(value==null)return;
		//
		this.headers.put(name, new String[]{value});
	}

	@Override
	public void addHeader(String name, String value) {
		if(value==null)return;
		//
		String[] values=this.headers.get(name);
		if(values==null){
			this.headers.put(name, new String[]{value});
		}else{
			String[] values2=Arrays.copyOf(values, values.length+1);
			values2[values.length]=value;
			this.headers.put(name, values2);
		}
	}

	@Override
	public void setIntHeader(String name, int value) {
		this.setHeader(name, String.valueOf(value));
	}

	@Override
	public void addIntHeader(String name, int value) {
		this.addHeader(name, String.valueOf(value));
	}

	@Override
	public void setStatus(int sc) {
		this.status=sc;
	}

	@Override
	public void setStatus(int sc, String sm) {
		this.status=sc;
		this.errorMessage=sm;
	}

	@Override
	public int getStatus() {
		return this.status;
	}

	@Override
	public String getHeader(String name) {
		String values[]=this.headers.get(name);
		if(values!=null && values.length>0){
			return values[0];
		}
		return null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		String values[]=this.headers.get(name);
		if(values!=null && values.length>0){
			return Arrays.asList(values);
		}
		return null;
	}

	@Override
	public Collection<String> getHeaderNames() {
		return this.headers.keySet();
	}



	@Override
	public void setCharacterEncoding(String charset) {
		this.charset=charset;
		if (this.contentType != null && !this.contentType.toLowerCase().contains("charset=")) {
			StringBuilder sb = new StringBuilder(this.contentType);
			sb.append(";").append("charset=").append(this.charset);
			this.setHeader("Content-Type", sb.toString());
		}
	}

	@Override
	public String getCharacterEncoding() {
		return charset;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return servletout;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		Writer targetWriter = this.charset != null ? new OutputStreamWriter(this.content, this.charset)
			: new OutputStreamWriter(this.content);
		
		return new PrintWriter(targetWriter);
	}

	@Override
	public void setContentLength(int len) {
		this.setHeader("Content-Length", String.valueOf(len));
	}

	@Override
	public void setContentType(String type) {
		this.contentType=type;
		if (this.charset != null && !this.contentType.toLowerCase().contains("charset=")) {
			StringBuilder sb = new StringBuilder(this.contentType);
			sb.append(";").append("charset=").append(this.charset);
			this.setHeader("Content-Type", sb.toString());
		}else{
			this.setHeader("Content-Type", contentType);
		}
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public void setBufferSize(int size) {
		
	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public void flushBuffer() throws IOException {
		
	}

	@Override
	public boolean isCommitted() {
		return this.committed;
	}

	@Override
	public void reset() {
		resetBuffer();
		this.charset = null;
		this.contentType = null;
		this.locale = null;
		this.cookies.clear();
		this.headers.clear();
		this.status = 200;
		this.errorMessage = null;
	}

	@Override
	public void resetBuffer() {
		if (isCommitted()) {
			throw new IllegalStateException("Cannot reset buffer - response is already committed");
		}
		this.content.reset();
	}

	@Override
	public void setLocale(Locale loc) {
		this.locale=loc;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}



	

}
