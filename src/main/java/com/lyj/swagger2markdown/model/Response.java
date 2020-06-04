package com.lyj.swagger2markdown.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by XiuYin.Cui on 2018/1/11.
 */
@Data
public class Response implements Serializable{

    /**
     * 返回参数
     */
    private String description;

    /**
     * 参数名
     */
    private String name;

    /**
     * 备注
     */
    private String remark;
}
