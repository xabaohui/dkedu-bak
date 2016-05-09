package com.jspxcms.core.security.oauth;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weibo4j.Users;
import weibo4j.model.User;

import com.qq.connect.api.qzone.UserInfo;
import com.qq.connect.javabeans.qzone.UserInfoBean;

/**
 * OAuthToken
 * 
 * @author liufang
 * 
 */
public class OAuthToken implements AuthenticationToken {
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.getLogger(OAuthToken.class);

	public static final String SP_QQ = "qq";
	public static final String SP_WEIBO = "weibo";

	public OAuthToken(String provider, String ticket, String openid) {
		this.provider = provider;
		this.ticket = ticket;
		this.openid = openid;
	}

	public OAuthToken(String provider, String ticket, String openid,
			Integer userId) {
		this.provider = provider;
		this.ticket = ticket;
		this.openid = openid;
		this.userId = userId;
	}

	private void fetchUserInfo() {
		try {
			if (isQq()) {
				UserInfoBean userBean = new UserInfo(ticket, openid)
						.getUserInfo();
				nickname = userBean.getNickname();
				avatarLarge = userBean.getAvatar().getAvatarURL100();
				avatarSmall = userBean.getAvatar().getAvatarURL50();
				nicknameRandom = (provider + "_" + RandomStringUtils
						.randomAlphanumeric(8)).toUpperCase();
			} else if (isWeibo()) {
				Users um = new Users(ticket);
				User user = um.showUserById(openid);
				nickname = user.getScreenName();
				avatarLarge = user.getAvatarLarge();
				avatarSmall = user.getProfileImageUrl();
				nicknameRandom = (provider + "_" + RandomStringUtils
						.randomAlphanumeric(5)).toUpperCase();
			}
		} catch (Exception e) {
			logger.error(null, e);
		}
	}

	public String getNickname() {
		if (StringUtils.isBlank(nickname)) {
			fetchUserInfo();
		}
		return nickname;
	}

	public String getNicknameRandom() {
		if (StringUtils.isBlank(nicknameRandom)) {
			fetchUserInfo();
		}
		return nicknameRandom;
	}

	public String getAvatarLarge() {
		if (StringUtils.isBlank(avatarLarge)) {
			fetchUserInfo();
		}
		return avatarLarge;
	}

	public String getAvatarSmall() {
		if (StringUtils.isBlank(avatarSmall)) {
			fetchUserInfo();
		}
		return avatarSmall;
	}

	public boolean isQq() {
		return SP_QQ.equals(this.provider);
	}

	public boolean isWeibo() {
		return SP_WEIBO.equals(this.provider);
	}

	public Object getPrincipal() {
		return this.openid;
	}

	public Object getCredentials() {
		return this.ticket;
	}

	private String provider;
	private String openid;
	private String ticket;
	private Integer userId;
	private String nickname;
	private String nicknameRandom;

	public void setNicknameRandom(String nicknameRandom) {
		this.nicknameRandom = nicknameRandom;
	}

	private String avatarLarge;
	private String avatarSmall;

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setAvatarLarge(String avatarLarge) {
		this.avatarLarge = avatarLarge;
	}

	public void setAvatarSmall(String avatarSmall) {
		this.avatarSmall = avatarSmall;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "provider=" + this.provider + ";ticket=" + this.ticket
				+ ";openid=" + this.openid + ";userId=" + this.userId;
	}

}
