package com.jspxcms.core.web.fore;

import static com.jspxcms.core.security.oauth.OAuthFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME;
import static com.jspxcms.core.security.oauth.OAuthFilter.FALLBACK_URL_PARAM;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import weibo4j.model.WeiboException;

import com.jspxcms.common.captcha.Captchas;
import com.jspxcms.common.security.CredentialsDigest;
import com.jspxcms.common.web.Servlets;
import com.jspxcms.common.web.Validations;
import com.jspxcms.core.domain.GlobalRegister;
import com.jspxcms.core.domain.Site;
import com.jspxcms.core.domain.User;
import com.jspxcms.core.security.oauth.OAuthFilter;
import com.jspxcms.core.security.oauth.OAuthToken;
import com.jspxcms.core.service.MemberGroupService;
import com.jspxcms.core.service.OrgService;
import com.jspxcms.core.service.UserService;
import com.jspxcms.core.support.Context;
import com.jspxcms.core.support.ForeContext;
import com.jspxcms.core.support.Response;
import com.octo.captcha.service.CaptchaService;
import com.qq.connect.QQConnectException;
import com.qq.connect.oauth.Oauth;

/**
 * 公开认证平台登录注册
 * 
 * @author liufang
 * 
 */
@Controller
public class OAuthController {
	private Logger logger = LoggerFactory.getLogger(OAuthController.class);

	/**
	 * 注册模板
	 */
	public static final String OAUTH_REGISTER_TEMPLATE = "sys_oauth_register.html";

	@RequestMapping(value = "/oauth/qq_login.jspx")
	public void qqLogin(HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model modelMap)
			throws IOException, QQConnectException {
		String url = new Oauth().getAuthorizeURL(request);
		logger.debug("QQ authorize url:" + url);
		saveFallbackUrl(request);
		response.sendRedirect(url);
	}

	@RequestMapping(value = "/oauth/weibo_login.jspx")
	public void weiboLogin(String fallbackUrl, HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model modelMap)
			throws IOException, WeiboException {
		String url = new weibo4j.Oauth().authorize("code");
		logger.debug("WEIBO authorize url:" + url);
		saveFallbackUrl(request);
		response.sendRedirect(url);
	}

	private void saveFallbackUrl(HttpServletRequest request) {
		String fallbackUrl = request.getParameter(FALLBACK_URL_PARAM);
		if (StringUtils.isNotBlank(fallbackUrl)) {
			request.getSession().setAttribute(FALLBACK_URL_PARAM, fallbackUrl);
		}
	}

	@RequestMapping(value = "/oauth/authc/{provider}.jspx")
	public String authcFail(String fallbackUrl, HttpServletRequest request,
			RedirectAttributes ra) {
		Object errorName = request
				.getAttribute(DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
		if (errorName != null) {
			ra.addFlashAttribute(DEFAULT_ERROR_KEY_ATTRIBUTE_NAME, errorName);
		}
		ra.addAttribute(FALLBACK_URL_PARAM, fallbackUrl);
		return "redirect:/login.jspx";
	}

	@RequestMapping(value = "/oauth/register.jspx")
	public String oauthRegisterForm(String fallbackUrl,
			HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model modelMap) throws IOException,
			WeiboException {
		Response resp = new Response(request, response, modelMap);
		Site site = Context.getCurrentSite(request);
		GlobalRegister registerConf = site.getGlobal().getRegister();
		if (registerConf.getMode() == GlobalRegister.MODE_OFF) {
			return resp.warning("register.off");
		}
		Map<String, Object> data = modelMap.asMap();
		ForeContext.setData(data, request);
		return site.getTemplate(OAUTH_REGISTER_TEMPLATE);
	}

	@RequestMapping(value = "/oauth/register.jspx", method = RequestMethod.POST)
	public String oauthRegisterSubmit(String username, String password,
			String email, String gender, Date birthDate, String bio,
			String comeFrom, String qq, String msn, String weixin,
			HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model modelMap) {
		Response resp = new Response(request, response, modelMap);
		Site site = Context.getCurrentSite(request);
		GlobalRegister reg = site.getGlobal().getRegister();
		OAuthToken token = (OAuthToken) request.getSession().getAttribute(
				OAuthFilter.OAUTH_TOKEN_SESSION_NAME);
		if (StringUtils.isBlank(password)) {
			password = RandomStringUtils.randomAscii(32);
		}
		String result = validateRegisterSubmit(request, resp, reg, token,
				username, password, email, gender);
		if (resp.hasErrors()) {
			return result;
		}

		String ip = Servlets.getRemoteAddr(request);
		int groupId = reg.getGroupId();
		int orgId = reg.getOrgId();
		int status = User.NORMAL;
		String qqOpenid = null;
		String weiboUid = null;
		if (token.isQq()) {
			qqOpenid = token.getOpenid();
		} else if (token.isWeibo()) {
			weiboUid = token.getOpenid();
		}
		userService.register(ip, groupId, orgId, status, username, password,
				email, qqOpenid, weiboUid, gender, birthDate, bio, comeFrom,
				qq, msn, weixin);
		return "redirect:/oauth/authc/session.jspx?session=true";
	}

	@RequestMapping(value = "/oauth/bind.jspx", method = RequestMethod.POST)
	public String oauthBindSubmit(String captcha, String username,
			String password, HttpServletRequest request,
			HttpServletResponse response, org.springframework.ui.Model modelMap) {
		Response resp = new Response(request, response, modelMap);
		OAuthToken token = (OAuthToken) request.getSession().getAttribute(
				OAuthFilter.OAUTH_TOKEN_SESSION_NAME);

		List<String> messages = resp.getMessages();
		if (!Captchas.isValid(captchaService, request, captcha)) {
			return resp.post(100, "error.captcha");
		}
		if (!Validations.exist(token)) {
			return resp.post(501, "register.oauthTokenNotFound");
		}
		if (!Validations.notEmpty(username, messages, "username")) {
			return resp.post(401);
		}
		User user = userService.findByUsername(username);
		if (!credentialsDigest.matches(user.getPassword(), password,
				user.getSaltBytes())) {
			return resp.post(502, "member.passwordError");
		}

		if (token.isQq()) {
			user.setQqOpenid(token.getOpenid());
		} else if (token.isWeibo()) {
			user.setWeiboUid(token.getOpenid());
		}
		userService.update(user, user.getDetail());
		return "redirect:/oauth/authc/session.jspx?session=true";
	}

	private String validateRegisterSubmit(HttpServletRequest request,
			Response resp, GlobalRegister reg, OAuthToken token,
			String username, String password, String email, String gender) {
		List<String> messages = resp.getMessages();
		if (reg.getMode() == GlobalRegister.MODE_OFF) {
			return resp.post(501, "register.off");
		}
		Integer groupId = reg.getGroupId();
		if (groupService.get(groupId) == null) {
			return resp.post(502, "register.groupNotSet");
		}
		Integer orgId = reg.getOrgId();
		if (orgService.get(orgId) == null) {
			return resp.post(503, "register.orgNotSet");
		}
		if (!Validations.exist(token)) {
			return resp.post(504, "register.oauthTokenNotFound");
		}
		if (!Validations.notEmpty(username, messages, "username")) {
			return resp.post(401);
		}
		if (!Validations.length(username, reg.getMinLength(),
				reg.getMaxLength(), messages, "username")) {
			return resp.post(402);
		}
		if (!Validations.pattern(username, reg.getValidCharacter(), messages,
				"username")) {
			return resp.post(403);
		}
		if (!Validations.notEmpty(password, messages, "password")) {
			return resp.post(404);
		}
		if (!Validations.email(email, messages, "email")) {
			return resp.post(406);
		}
		if (!Validations.pattern(gender, "[F,M]", messages, "gender")) {
			return resp.post(407);
		}
		return null;
	}

	@Autowired
	private CaptchaService captchaService;
	@Autowired
	private MemberGroupService groupService;
	@Autowired
	private OrgService orgService;
	@Autowired
	private UserService userService;
	@Autowired
	private CredentialsDigest credentialsDigest;
}
