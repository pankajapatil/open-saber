package io.opensaber.registry.interceptor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import io.opensaber.registry.interceptor.handler.BaseResponseHandler;
import io.opensaber.registry.middleware.impl.JSONLDConverter;
import io.opensaber.registry.middleware.util.Constants;

@Component
public class JSONLDConversionInterceptor extends BaseResponseHandler implements HandlerInterceptor{
	
	private static Logger logger = LoggerFactory.getLogger(JSONLDConversionInterceptor.class);
	
	private JSONLDConverter jsonldConverter;
	
	@Autowired
	public JSONLDConversionInterceptor(JSONLDConverter jsonldConverter){
		this.jsonldConverter = jsonldConverter;
	}

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object object) throws Exception {
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,HttpServletResponse response,
	  Object handler, ModelAndView modelAndView) throws Exception {
		logger.info("RESPONSE COMMITTED:"+response.isCommitted());
		setResponse(response);
		Map<String,Object> responseMap = jsonldConverter.execute(getResponseBodyMap());
		if(responseMap.get(Constants.RESPONSE_ATTRIBUTE)!=null){
			setFormattedResponse(responseMap.get(Constants.RESPONSE_ATTRIBUTE).toString());
			writeResponseBody(getFormattedResponse());
		}
		response = getResponse();		
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
	  Object handler, Exception ex) {

	}

}
