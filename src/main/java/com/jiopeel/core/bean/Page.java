package com.jiopeel.core.bean;

import lombok.Data;

import java.util.List;

@Data
public class Page<E> {

    /**
     * 第几页
     */
    private int pageNum;
    /**
     * 每页大小
     */
    private int pageSize;
    /**
     * 开始行
     */
    private int startRow;
    /**
     * 结束行
     */
    private int endRow;
    /**
     * 总行数
     */
    private int total;
    /**
     * 总页数
     */
    private int pages;
    /**
     * 返回集合
     */
    private List<E> result;

    public Page(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.startRow = pageNum > 0 ? (pageNum - 1) * pageSize : 0;
        this.endRow = pageNum * pageSize;
    }

}