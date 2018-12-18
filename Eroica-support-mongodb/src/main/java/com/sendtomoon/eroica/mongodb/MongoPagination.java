package com.sendtomoon.eroica.mongodb;

import java.util.List;

import com.sendtomoon.eroica.common.biz.dto.BaseDTO;

public class MongoPagination<T> extends BaseDTO {

	private static final long serialVersionUID = 1L;

	private int pageNo;

	private int pageLimitSize;

	private long totalSize = -1;

	private String[] orderBy;

	private boolean isDesc;

	private List<T> pojos;

	public MongoPagination(int pageNo, int pageLimitSize) {
		this.pageNo = pageNo;
		this.pageLimitSize = pageLimitSize;
	}

	public MongoPagination(int pageNo, int pageLimitSize, int totalSize) {
		this.pageNo = pageNo;
		this.pageLimitSize = pageLimitSize;
		this.totalSize = totalSize;
	}

	public MongoPagination() {
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageLimitSize() {
		return pageLimitSize;
	}

	public void setPageLimitSize(int pageLimitSize) {
		this.pageLimitSize = pageLimitSize;
	}

	public long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	public String[] getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String... orderBy) {
		this.orderBy = orderBy;
	}

	public boolean isDesc() {
		return isDesc;
	}

	public void setDesc(boolean isDesc) {
		this.isDesc = isDesc;
	}

	public List<T> getPojos() {
		return pojos;
	}

	public void setPojos(List<T> pojos) {
		this.pojos = pojos;
	}

}
