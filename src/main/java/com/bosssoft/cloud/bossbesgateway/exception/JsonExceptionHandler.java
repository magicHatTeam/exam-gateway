package com.bosssoft.cloud.bossbesgateway.exception;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.boss.bes.core.data.pojo.CommonResponse;
import com.boss.bes.core.data.pojo.ResultEnum;
import com.boss.bes.core.data.util.ResponseUtil;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

/**
 * 统一异常处理
 * @author likang
 * @date 2019/8/20 16:04
 */
public class JsonExceptionHandler extends DefaultErrorWebExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(JsonExceptionHandler.class);

    public JsonExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
                                ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    /**
     * 获取异常属性
     */
    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        Throwable error = super.getError(request);
        CommonResponse commonResponse;
        if (error instanceof AppException){
            AppException appException = (AppException) error;
            commonResponse = ResponseUtil.buildError(appException.getResultEnum());
        }else if (error instanceof JWTDecodeException) {
            commonResponse = ResponseUtil.buildError(ResultEnum.TOKEN_INVALID);
        }else if(error instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) error;
            commonResponse = ResponseUtil.buildError(String.valueOf(responseStatusException.getStatus().value()),responseStatusException.getMessage());
        }else {
            commonResponse = ResponseUtil.buildError(error.getMessage());
        }
        Map<String, Object> map = new HashMap<>(2);
        map.put("head", commonResponse.getHead());
        map.put("body", commonResponse.getBody());
        logger.error("Error found: ", error);
        return map;
    }

    /**
     * 指定响应处理方法为JSON处理的方法
     * @param errorAttributes errorAttributes
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * 根据code获取对应的HttpStatus
     * @param errorAttributes errorAttributes
     */
    @Override
    protected HttpStatus getHttpStatus(Map<String, Object> errorAttributes) {
        return HttpStatus.OK;
    }
}
