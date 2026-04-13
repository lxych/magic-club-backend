package cn.magicclub.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 *
 * @author demo
 * @since 2025-04-13
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("业务异常 [{}] - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<String> handleBindException(BindException e, HttpServletRequest request) {
        log.error("参数绑定异常 [{}] - {}", request.getRequestURI(), e.getMessage());
        return Result.error(400, "参数格式错误");
    }

    /**
     * 其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 [{}] - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(500, "系统繁忙，请稍后再试");
    }
}
