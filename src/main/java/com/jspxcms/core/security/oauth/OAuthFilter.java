package com.jspxcms.core.security.oauth;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.PathMatchingFilter;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import weibo4j.Account;

import com.jspxcms.common.web.Servlets;
import com.jspxcms.core.domain.User;
import com.jspxcms.core.security.ShiroUser;
import com.jspxcms.core.service.OperationLogService;
import com.jspxcms.core.service.UserShiroService;
import com.qq.connect.api.OpenID;
import com.qq.connect.oauth.Oauth;

/**
 * OAuthcFilter
 * 
 * @author liufang
 * 
 */
public class OAuthFilter extends PathMatchingFilter {
	private Logger logger = LoggerFactory.getLogger(OAuthFilter.class);

	/**
	 * 返回URL
	 */
	public static final String FALLBACK_URL_PARAM = "fallbackUrl";
	public static final String DEFAULT_LOGIN_URL = "/login.jspx";
	public static final String DEFAULT_SUCCESS_URL = "/";
	public static final String DEFAULT_OAUTH_REGISTER_URL = "/oauth/register.jspx";
	public static final String DEFAULT_ERROR_KEY_ATTRIBUTE_NAME = "shiroLoginFailure";
	public static final String OAUTH_TOKEN_SESSION_NAME = "oauthToken";

	private String loginUrl = DEFAULT_LOGIN_URL;
	private String successUrl = DEFAULT_SUCCESS_URL;
	private String oauthRegisterUrl = DEFAULT_OAUTH_REGISTER_URL;
	private String failureKeyAttribute = DEFAULT_ERROR_KEY_ATTRIBUTE_NAME;

	@Override
	public boolean onPreHandle(ServletRequest request,
			ServletResponse response, Object mappedValue) throws Exception {
		HttpServletRequest hsr = (HttpServletRequest) request;
		OAuthToken token;
		if ("true".equals(request.getParameter("session"))) {
			token = (OAuthToken) hsr.getSession().getAttribute(
					OAUTH_TOKEN_SESSION_NAME);
			// token要一直保存在session中，就不必移除了。
			// req.getSession().removeAttribute(OAUTH_TOKEN_SESSION_NAME);
			if (token == null) {
				throw new AuthenticationException(
						"oauth token cannot get from session.");
			}
			String sp = token.getProvider();
			String openid = token.getOpenid();
			User user = null;
			if (OAuthToken.SP_QQ.equals(sp)) {
				user = userShiroService.findByQqOpenid(openid);
			} else if (OAuthToken.SP_WEIBO.equals(sp)) {
				user = userShiroService.findByWeiboUid(openid);
			}
			if (user == null) {
				throw new AuthenticationException(
						"cannot found user by provider=" + sp + ",openid="
								+ openid);
			}
			token.setUserId(user.getId());
		} else {
			token = createToken(hsr);
			if (token.getUserId() == null) {
				hsr.getSession().setAttribute(OAUTH_TOKEN_SESSION_NAME, token);
				WebUtils.issueRedirect(request, response, getOauthRegisterUrl());
				return false;
			}
		}
		SavedRequest savedRequest = (SavedRequest) hsr.getSession()
				.getAttribute(WebUtils.SAVED_REQUEST_KEY);
		try {
			Subject subject = SecurityUtils.getSubject();
			// 防止session fixation attack(会话固定攻击)，让旧session失效
			if (subject.getSession(false) != null) {
				subject.logout();
			}
			subject.login(token);
			// 将SavedRequest放回session
			hsr.getSession().setAttribute(WebUtils.SAVED_REQUEST_KEY,
					savedRequest);
			// 将第三方认证信息保留在session中，以便使用。
			hsr.getSession().setAttribute(OAUTH_TOKEN_SESSION_NAME, token);
			ShiroUser shiroUser = (ShiroUser) subject.getPrincipal();
			String ip = Servlets.getRemoteAddr(request);
			userShiroService.updateLoginSuccess(shiroUser.id, ip);
			logService.loginSuccess(ip, shiroUser.id);
			issueSuccessRedirect(request, response);
			return false;
		} catch (AuthenticationException ae) {
			// 将SavedRequest放回session
			hsr.getSession().setAttribute(WebUtils.SAVED_REQUEST_KEY,
					savedRequest);
			setFailureAttribute(request, ae);
			return true;
		}
	}

	protected OAuthToken createToken(HttpServletRequest request)
			throws Exception {
		OAuthToken token = null;
		String ticket = null, openid = null;
		User user = null;
		String uri = request.getRequestURI();
		String provider = uri.substring(uri.lastIndexOf('/') + 1,
				uri.lastIndexOf('.'));
		if (OAuthToken.SP_QQ.equals(provider)) {
			ticket = (new Oauth()).getAccessTokenByRequest(request)
					.getAccessToken();
			if (StringUtils.isBlank(ticket)) {
				throw new AuthenticationException(
						"oauth qq access token not provided.");
			}
			logger.debug("oauth qq access token:" + ticket);
			openid = new OpenID(ticket).getUserOpenID();
			logger.debug("oauth qq open id:" + openid);
			if (StringUtils.isBlank(openid)) {
				throw new AuthenticationException(
						"oauth qq openid not provided.");
			}
			user = userShiroService.findByQqOpenid(openid);
		} else if (OAuthToken.SP_WEIBO.equals(provider)) {
			ticket = new weibo4j.Oauth().getAccessTokenByCode(
					request.getParameter("code")).getAccessToken();
			if (StringUtils.isBlank(ticket)) {
				throw new AuthenticationException(
						"oauth weibo access token not provided.");
			}
			logger.debug("oauth weibo access token:" + ticket);
			openid = new Account(ticket).getUid().getString("uid");
			logger.debug("oauth weibo uid:" + openid);
			if (StringUtils.isBlank(openid)) {
				throw new AuthenticationException(
						"oauth weibo uid not provided.");
			}
			user = userShiroService.findByWeiboUid(openid);
		} else {
			throw new AuthenticationException("OAuth provider not support:"
					+ provider);
		}
		if (user != null) {
			token = new OAuthToken(provider, ticket, openid, user.getId());
		} else {
			// openid对应的用户不存在。
			token = new OAuthToken(provider, ticket, openid);
		}
		logger.debug("OAuth token:" + token);
		return token;
	}

	protected void issueSuccessRedirect(ServletRequest req, ServletResponse resp)
			throws Exception {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		HttpSession session = request.getSession();
		String successUrl = (String) session.getAttribute(FALLBACK_URL_PARAM);
		session.removeAttribute(FALLBACK_URL_PARAM);
		if (StringUtils.isBlank(successUrl)) {
			successUrl = getSuccessUrl();
		}
		WebUtils.redirectToSavedRequest(request, response, successUrl);
	}

	protected void setFailureAttribute(ServletRequest request,
			AuthenticationException ae) {
		String className = ae.getClass().getName();
		request.setAttribute(getFailureKeyAttribute(), className);
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public String getSuccessUrl() {
		return successUrl;
	}

	public void setSuccessUrl(String successUrl) {
		this.successUrl = successUrl;
	}

	public String getOauthRegisterUrl() {
		return oauthRegisterUrl;
	}

	public void setOauthRegisterUrl(String oauthRegisterUrl) {
		this.oauthRegisterUrl = oauthRegisterUrl;
	}

	public String getFailureKeyAttribute() {
		return failureKeyAttribute;
	}

	public void setFailureKeyAttribute(String failureKeyAttribute) {
		this.failureKeyAttribute = failureKeyAttribute;
	}

	private UserShiroService userShiroService;
	private OperationLogService logService;

	@Autowired
	public void setOperationLogService(OperationLogService logService) {
		this.logService = logService;
	}

	@Autowired
	public void setUserShiroService(UserShiroService userShiroService) {
		this.userShiroService = userShiroService;
	}

}
