package com.ylz.yx.pay.interceptor;

import com.alibaba.druid.util.StringUtils;
import com.ylz.svc.web.token.JwtUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

	/**
	 * 在请求被处理之前调用
	 *
	 * @param request
	 * @param response
	 * @param handler
	 * @return
	 * @throws IOException
	 * @author hongjq 2020.12.03
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

		String account = "";
		String token = request.getParameter("token");
		try {
			account = JwtUtils.validateToken(token);
		} catch (Exception e) {

		}

		String url = request.getServletPath();

		if (url.indexOf("/api/") != -1 || !url.contains("/uploadFiles/excel")) {

			String[] urls = url.split("/");
			if (urls.length < 2) {
				response.sendError(404, "地址有误");
				return false;
			}

			if (StringUtils.isEmpty(account)) {
				response.sendError(403, "权限不足，请获取token");
				return false;
			}
		}
		return true;
	}

}
