package top.zhangpy.mychat.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Result <T> {
    @Schema(description = "返回码")
    private Integer code;

    @Schema(description = "返回消息")
    private String message;

    @Schema(description = "返回数据")
    private T data;

    public Result(){}

    public static <T> Result<T> build(T body, Integer code, String message) {
        Result<T> result = new Result<T>();
        if (body != null) {
            result.setData(body);
        }
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static<T> Result<T> ok(){
        return Result.ok(null);
    }

    public static<T> Result<T> ok(T data){
        return build(data,200,"成功");
    }

    public static<T> Result<T> ok(T data,String message){
        return build(data,200,message);
    }

    public static<T> Result<T> fail(){
        return Result.fail(null);
    }

    public static<T> Result<T> fail(T data){
        return build(data,400,"失败");
    }

    public static<T> Result<T> fail(T data,String message){
        return build(data,400,message);
    }

    public static<T> Result<T> fail(Integer code,String message, T data){
        return build(data,code,message);
    }

}
