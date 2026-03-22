package com.example.student.university_career_platform.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;        // 200 表示成功， 400 表示前段错误， 500 表示服务器错误等
    private  String msg;
    private  T data;

    // 成功是的静态方法
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    // 失败是的静态方法
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
}
