package com.offerpilot.application.enums;

/**
 * Offer 状态
 */
public enum OfferStatus {

    /**
     * 待接受（收到 Offer，尚未决定）
     */
    PENDING,

    /**
     * 已接受（接 Offer）
     */
    ACCEPTED,

    /**
     * 已拒绝（拒 Offer）
     */
    DECLINED
}
