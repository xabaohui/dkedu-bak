package com.jspxcms.core.security.oauth;

import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jspxcms.common.security.UnactivatedAccountException;
import com.jspxcms.core.domain.Site;
import com.jspxcms.core.domain.User;
import com.jspxcms.core.security.ShiroUser;
import com.jspxcms.core.service.UserShiroService;
import com.jspxcms.core.support.Context;

/**
 * OAuthRealm
 * 
 * @author liufang
 * 
 */
public class OAuthRealm extends AuthorizingRealm {
	public OAuthRealm() {
		setAuthenticationTokenClass(OAuthToken.class);
	}

	protected UserShiroService userShiroService;

	/**
	 * 认证回调函数,登录时调用.
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken authcToken) throws AuthenticationException {
		OAuthToken token = (OAuthToken) authcToken;
		Integer userId = token.getUserId();
		User user = null;
		if (token.isQq()) {
			user = userShiroService.get(userId);
		} else if (token.isWeibo()) {
			user = userShiroService.get(userId);
		}
		// 前后台登录共用，非管理员也可登录。
		if (user != null) {
			if (user.isNormal()) {
				return new SimpleAuthenticationInfo(new ShiroUser(user.getId(),
						user.getUsername()), token.getTicket(), getName());
			} else if (user.isLocked()) {
				throw new LockedAccountException();
			} else if (user.isUnactivated()) {
				throw new UnactivatedAccountException();
			}
		}
		return null;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		SimpleAuthorizationInfo auth = new SimpleAuthorizationInfo();
		ShiroUser shiroUser = (ShiroUser) principals.getPrimaryPrincipal();
		User user = userShiroService.get(shiroUser.id);
		Site site = Context.getCurrentSite();
		if (user != null && site != null) {
			Set<String> perms = user.getPerms(site.getId());
			if (!CollectionUtils.isEmpty(perms)) {
				auth.setStringPermissions(perms);
			}
			if (user.isSuper()) {
				auth.addRole("super");
			}
		}
		return auth;
	}

	@Autowired
	public void setUserShiroService(UserShiroService userShiroService) {
		this.userShiroService = userShiroService;
	}
}
