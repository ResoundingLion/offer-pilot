package com.offerpilot.application.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 熔断降级规则配置
 *
 * 规则说明：
 * - 异常比例模式：10s 统计窗口内，最少 5 个请求中，异常比例超过 50% → 熔断 30s
 * - 熔断期间 Feign 调用直接走 fallbackFactory（<1ms），不等待超时
 * - 30s 后进入 HALF_OPEN 状态，放一个探针请求检测下游是否恢复
 *
 * 保护目标：
 * - companyClient:getCompanyById(Long)  → 查公司名
 * - positionClient:getPositionById(Long) → 查岗位名
 */
@Slf4j
@Configuration
public class SentinelConfig {

    @PostConstruct
    public void init() {
        List<DegradeRule> rules = new ArrayList<>();

        // ————— 公司名查询熔断 —————
        DegradeRule companyRule = new DegradeRule("companyClient:getCompanyById(Long)")
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setCount(0.5)               // 50% 请求异常 → 熔断
                .setTimeWindow(30)           // 熔断持续时间（秒）
                .setMinRequestAmount(5)      // 最小请求数（防误判）
                .setStatIntervalMs(10000);   // 统计时间窗口（毫秒）
        rules.add(companyRule);

        // ————— 岗位名查询熔断 —————
        DegradeRule positionRule = new DegradeRule("positionClient:getPositionById(Long)")
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setCount(0.5)
                .setTimeWindow(30)
                .setMinRequestAmount(5)
                .setStatIntervalMs(10000);
        rules.add(positionRule);

        DegradeRuleManager.loadRules(rules);
        log.info("===== Sentinel 熔断规则加载完成：{} 条 =====", rules.size());
    }
}
