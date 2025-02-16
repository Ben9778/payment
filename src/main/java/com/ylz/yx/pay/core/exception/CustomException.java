package com.ylz.yx.pay.core.exception;

import com.ylz.core.YLZException;

/**
 * 自定义异常
 *
 */
public class CustomException extends YLZException
{
    private static final long serialVersionUID = 1L;

    private Integer status;

    private final String title;

    public CustomException(String title)
    {
        this.title = title;
    }

    public CustomException(Integer status, String title)
    {
        this.title = title;
        this.status = status;
    }

    public CustomException(String title, Throwable e)
    {
        super(title, e);
        this.title = title;
    }

    public String getTitle()
    {
        return title;
    }

    public Integer getStatus()
    {
        return status;
    }
}
