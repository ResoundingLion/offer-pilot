package com.offerpilot.application.enums;

/**
 * 投递状态 —— 核心状态机
 * <p>
 * 正向流转：SAVED → APPLIED → ONLINE_ASSESSMENT → INTERVIEW → HR_INTERVIEW → OFFER
 * 终止态：  REJECTED（被拒） / WITHDRAWN（主动撤回），可从任一状态跳入
 * </p>
 */
public enum ApplicationStatus {

    /**
     * 收藏（刚收藏岗位，尚未正式投递）
     */
    SAVED,

    /**
     * 已投递（简历已投出）
     */
    APPLIED,

    /**
     * 在线笔试
     */
    ONLINE_ASSESSMENT,

    /**
     * 面试中（包含一面/二面/三面等所有技术面）
     */
    INTERVIEW,

    /**
     * HR 面
     */
    HR_INTERVIEW,

    /**
     * 已拿到 Offer
     */
    OFFER,

    /**
     * 已淘汰（任何阶段被拒均落入此状态）
     */
    REJECTED,

    /**
     * 主动撤回（用户自行取消投递）
     */
    WITHDRAWN
}
