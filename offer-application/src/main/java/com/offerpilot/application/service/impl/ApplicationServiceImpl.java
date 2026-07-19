package com.offerpilot.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.offerpilot.api.client.CompanyClient;
import com.offerpilot.api.client.PositionClient;
import com.offerpilot.api.dto.CompanyDTO;
import com.offerpilot.api.dto.PositionDTO;
import com.offerpilot.application.converter.ApplicationConverter;
import com.offerpilot.application.dto.AdvanceRequest;
import com.offerpilot.application.entity.Application;
import com.offerpilot.application.entity.Interview;
import com.offerpilot.application.entity.Offer;
import com.offerpilot.application.enums.ApplicationStatus;
import com.offerpilot.application.enums.InterviewResult;
import com.offerpilot.application.enums.InterviewRound;
import com.offerpilot.application.enums.OfferStatus;
import com.offerpilot.application.mapper.ApplicationMapper;
import com.offerpilot.application.mapper.InterviewMapper;
import com.offerpilot.application.mapper.OfferMapper;
import com.offerpilot.application.service.ApplicationService;
import com.offerpilot.application.service.InterviewService;
import com.offerpilot.application.service.OfferService;
import com.offerpilot.application.vo.ApplicationVO;
import com.offerpilot.application.vo.DashboardVO;
import com.offerpilot.application.vo.PipelineVO;
import com.offerpilot.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;

import static com.offerpilot.common.result.ResultCode.BAD_REQUEST;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationMapper applicationMapper;
    private final InterviewMapper interviewMapper;
    private final OfferMapper offerMapper;
    private final InterviewService interviewService;
    private final OfferService offerService;
    private final CompanyClient companyClient;
    private final PositionClient positionClient;

    @Override
    public Application findById(Long id) {
        return applicationMapper.selectById(id);
    }

    @Override
    public List<Application> findAll() {
        return applicationMapper.selectList(null);
    }

    @Override
    public Application create(Application application) {
        applicationMapper.insert(application);
        return application;
    }

    @Override
    public Application update(Application application) {
        applicationMapper.updateById(application);
        return application;
    }

    @Override
    public void deleteById(Long id) {
        applicationMapper.deleteById(id);
    }

    @Override
    public List<Application> findAllByUserId(Long userId) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getUserId,userId)
                .orderByDesc(Application::getUpdatedAt);
        return applicationMapper.selectList(wrapper);
    }

    @Override
    public Application updateStatus(Long id, String status) {
        // 1、查投递记录
        Application application = applicationMapper.selectById(id);
        if (application == null) {
            return null;
        }

        // 2、字符串 → 枚举
        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(BAD_REQUEST, "无效的投递状态: " + status);
        }

        ApplicationStatus currentStatus = application.getStatus();

        // 3、终止态不能再变更
        if (currentStatus == ApplicationStatus.REJECTED || currentStatus == ApplicationStatus.WITHDRAWN) {
            throw new BusinessException(BAD_REQUEST, "已终止的投递无法变更状态");
        }

        // 4、校验流转是否合法
        Set<ApplicationStatus> allowed = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new BusinessException(BAD_REQUEST,
                    String.format("不允许从 %s 变更为 %s", currentStatus, newStatus));
        }

        // 5、通过，更新
        application.setStatus(newStatus);
        applicationMapper.updateById(application);
        return application;
    }


    // ===== 跨服务组装 =====

    @Override
    public ApplicationVO enrichVO(Application application) {
        if (application == null) {
            return null;
        }
        ApplicationVO vo = ApplicationConverter.convertToVO(application);

        // 跨服务查询公司名
        try {
            CompanyDTO company = companyClient.getCompanyById(application.getCompanyId());
            if (company != null) {
                vo.setCompanyName(company.getName());
            }
        } catch (Exception e) {
            // Feign 调用失败时，companyName 保持 null，不影响主流程
        }

        // 跨服务查询岗位名
        try {
            PositionDTO position = positionClient.getPositionById(application.getPositionId());
            if (position != null) {
                vo.setPositionTitle(position.getTitle());
            }
        } catch (Exception e) {
            // Feign 调用失败时，positionTitle 保持 null
        }

        return vo;
    }

    // ===== 仪表盘统计 =====

    @Override
    public DashboardVO getDashboardStats() {
        List<Application> all = applicationMapper.selectList(null);
        long total = all.size();

        // 活跃状态（非终止态）
        Set<ApplicationStatus> terminalStates = Set.of(ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN);
        long activeCount = all.stream()
                .filter(a -> !terminalStates.contains(a.getStatus()))
                .count();

        // 面试中的数量
        long interviewCount = all.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.INTERVIEW
                        || a.getStatus() == ApplicationStatus.HR_INTERVIEW)
                .count();

        // Offer 数量
        long offerCount = all.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.OFFER)
                .count();

        // 近 14 天每日趋势
        LocalDate today = LocalDate.now();
        LocalDate fourteenDaysAgo = today.minusDays(13);
        Map<LocalDate, Long> dailyMap = all.stream()
                .filter(a -> a.getAppliedAt() != null)
                .filter(a -> a.getAppliedAt().toLocalDate().isAfter(fourteenDaysAgo.minusDays(1)))
                .collect(Collectors.groupingBy(
                        a -> a.getAppliedAt().toLocalDate(),
                        Collectors.counting()
                ));

        List<DashboardVO.DailyTrendItem> dailyTrend = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            LocalDate day = fourteenDaysAgo.plusDays(i);
            dailyTrend.add(DashboardVO.DailyTrendItem.builder()
                    .date(day.toString())
                    .count(dailyMap.getOrDefault(day, 0L))
                    .build());
        }

        // 渠道分布
        Map<String, Long> sourceMap = all.stream()
                .filter(a -> a.getSource() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getSource().name(),
                        Collectors.counting()
                ));

        List<DashboardVO.SourceItem> sourceDistribution = sourceMap.entrySet().stream()
                .map(e -> DashboardVO.SourceItem.builder()
                        .source(e.getKey())
                        .count(e.getValue())
                        .build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());

        return DashboardVO.builder()
                .totalApplications(total)
                .interviewCount(interviewCount)
                .offerCount(offerCount)
                .activeCount(activeCount)
                .dailyTrend(dailyTrend)
                .sourceDistribution(sourceDistribution)
                .build();
    }

    // ===== 一键推进 =====

    /**
     * Pipeline 阶段 key → ApplicationStatus 映射
     */
    private static final Map<String, ApplicationStatus> STAGE_TO_STATUS = Map.ofEntries(
            Map.entry("APPLIED", ApplicationStatus.APPLIED),
            Map.entry("ASSESSMENT", ApplicationStatus.ONLINE_ASSESSMENT),
            Map.entry("EXAM", ApplicationStatus.ONLINE_ASSESSMENT),
            Map.entry("INTERVIEW_1", ApplicationStatus.INTERVIEW),
            Map.entry("INTERVIEW_2", ApplicationStatus.INTERVIEW),
            Map.entry("INTERVIEW_3", ApplicationStatus.INTERVIEW),
            Map.entry("INTERVIEW_4", ApplicationStatus.INTERVIEW),
            Map.entry("HR_INTERVIEW", ApplicationStatus.HR_INTERVIEW),
            Map.entry("OFFER", ApplicationStatus.OFFER),
            Map.entry("REJECTED", ApplicationStatus.REJECTED),
            Map.entry("WITHDRAWN", ApplicationStatus.WITHDRAWN)
    );

    /** 面试轮次 key → InterviewRound */
    private static final Map<String, InterviewRound> STAGE_TO_ROUND = Map.of(
            "INTERVIEW_1", InterviewRound.FIRST,
            "INTERVIEW_2", InterviewRound.SECOND,
            "INTERVIEW_3", InterviewRound.THIRD,
            "INTERVIEW_4", InterviewRound.FOURTH,
            "HR_INTERVIEW", InterviewRound.HR
    );

    @Override
    public ApplicationVO advance(Long id, AdvanceRequest request) {
        // 1. 查投递记录
        Application app = applicationMapper.selectById(id);
        if (app == null) return null;

        String targetStage = request.getTargetStage();
        if (targetStage == null || targetStage.isBlank()) {
            throw new BusinessException(BAD_REQUEST, "目标阶段不能为空");
        }

        // 2. 解析目标状态
        ApplicationStatus targetStatus = STAGE_TO_STATUS.get(targetStage);
        if (targetStatus == null) {
            throw new BusinessException(BAD_REQUEST, "无效的目标阶段: " + targetStage);
        }

        // 3. 校验当前状态是否允许变更
        ApplicationStatus currentStatus = app.getStatus();
        if (currentStatus == ApplicationStatus.REJECTED || currentStatus == ApplicationStatus.WITHDRAWN) {
            throw new BusinessException(BAD_REQUEST, "已终止的投递无法变更状态");
        }

        // 4. 更新状态 + currentStage
        // Offer 特殊处理：没填薪资不改状态（只是占位），只记录 currentStage
        boolean isOfferPlaceholder = "OFFER".equals(targetStage) && request.getOfferSalary() == null;

        if (isOfferPlaceholder) {
            // Offer 占位：只设 currentStage，不改变 application.status
            applicationMapper.update(null, new LambdaUpdateWrapper<Application>()
                    .eq(Application::getId, id)
                    .set(Application::getCurrentStage, targetStage)
                    .set(Application::getUpdatedAt, LocalDateTime.now()));
        } else if (targetStatus != currentStatus) {
            // 跨状态推进 → 校验流转
            Set<ApplicationStatus> allowed = ALLOWED_TRANSITIONS.get(currentStatus);
            if (allowed == null || !allowed.contains(targetStatus)) {
                throw new BusinessException(BAD_REQUEST,
                        String.format("不允许从 %s 变更为 %s", currentStatus, targetStatus));
            }
            applicationMapper.update(null, new LambdaUpdateWrapper<Application>()
                    .eq(Application::getId, id)
                    .set(Application::getStatus, targetStatus)
                    .set(Application::getCurrentStage, targetStage)
                    .set(Application::getUpdatedAt, LocalDateTime.now()));
        } else {
            // 同 status 内子阶段推进（如 ASSESSMENT→EXAM / 一面→二面）
            applicationMapper.update(null, new LambdaUpdateWrapper<Application>()
                    .eq(Application::getId, id)
                    .set(Application::getCurrentStage, targetStage)
                    .set(Application::getUpdatedAt, LocalDateTime.now()));
        }

        // 5. 如果是面试轮次 → 创建/更新面试记录
        InterviewRound round = STAGE_TO_ROUND.get(targetStage);
        if (round != null && request.getInterviewScheduledAt() != null) {
            // 查是否已有该轮次面试记录（补结果场景）
            List<Interview> existingInterviews = interviewService.findByApplicationId(id);
            Interview existingIv = existingInterviews.stream()
                    .filter(iv -> iv.getRound() == round)
                    .findFirst().orElse(null);

            if (existingIv != null) {
                // 更新已有面试
                if (request.getInterviewScheduledAt() != null) existingIv.setScheduledAt(request.getInterviewScheduledAt());
                if (request.getInterviewType() != null) existingIv.setInterviewType(request.getInterviewType());
                if (request.getInterviewInterviewer() != null) existingIv.setInterviewer(request.getInterviewInterviewer());
                if (request.getInterviewResult() != null) existingIv.setResult(request.getInterviewResult());
                if (request.getInterviewFeedback() != null) existingIv.setFeedback(request.getInterviewFeedback());
                interviewService.update(existingIv);
            } else {
                // 新增面试
                Interview interview = new Interview();
                interview.setApplicationId(id);
                interview.setRound(round);
                interview.setScheduledAt(request.getInterviewScheduledAt());
                interview.setInterviewType(request.getInterviewType());
                interview.setLocation(request.getInterviewLocation());
                interview.setInterviewer(request.getInterviewInterviewer());
                interview.setResult(request.getInterviewResult());
                interview.setFeedback(request.getInterviewFeedback());
                interviewService.create(interview);
            }

            // 面试失败 → 同时设为拒绝
            if (request.getInterviewResult() == InterviewResult.FAILED) {
                applicationMapper.update(null, new LambdaUpdateWrapper<Application>()
                        .eq(Application::getId, id)
                        .set(Application::getStatus, ApplicationStatus.REJECTED)
                        .set(Application::getCurrentStage, targetStage)
                        .set(Application::getUpdatedAt, LocalDateTime.now()));
            }
        }

        // 6. 如果是 Offer → 创建/更新
        if ("OFFER".equals(targetStage) && request.getOfferSalary() != null) {
            Offer existing = offerService.findByApplicationId(id);
            if (existing == null) {
                Offer offer = new Offer();
                offer.setApplicationId(id);
                offer.setSalary(request.getOfferSalary());
                offer.setBonus(request.getOfferBonus());
                offer.setStock(request.getOfferStock());
                offer.setBenefits(request.getOfferBenefits());
                offer.setDeadline(request.getOfferDeadline());
                offer.setRemark(request.getOfferRemark());
                offer.setStatus(OfferStatus.PENDING);
                offerService.create(offer);
            } else {
                existing.setSalary(request.getOfferSalary());
                existing.setBonus(request.getOfferBonus());
                existing.setStock(request.getOfferStock());
                existing.setBenefits(request.getOfferBenefits());
                existing.setDeadline(request.getOfferDeadline());
                existing.setRemark(request.getOfferRemark());
                offerService.update(existing);
            }
        }

        // 7. 返回完整 VO
        return enrichVO(applicationMapper.selectById(id));
    }

    // ===== Pipeline 流水线 =====

    @Override
    public List<PipelineVO> getPipeline() {
        List<Application> all = applicationMapper.selectList(null);
        // 按更新时间倒序
        all.sort((a, b) -> {
            LocalDateTime ua = a.getUpdatedAt();
            LocalDateTime ub = b.getUpdatedAt();
            if (ua == null && ub == null) return 0;
            if (ua == null) return 1;
            if (ub == null) return -1;
            return ub.compareTo(ua);
        });

        // 批量加载面试记录和 Offer
        List<Long> ids = all.stream().map(Application::getId).collect(Collectors.toList());
        Map<Long, List<Interview>> interviewMap = batchLoadInterviews(ids);
        Map<Long, Offer> offerMap = batchLoadOffers(ids);

        return all.stream().map(app -> {
            List<Interview> interviews = interviewMap.getOrDefault(app.getId(), List.of());
            Offer offer = offerMap.get(app.getId());

            List<PipelineVO.StageInfo> stages = buildStages(app, interviews, offer);

            // 跨服务查公司 + 岗位名
            String companyName = null;
            String positionTitle = null;
            try {
                var company = companyClient.getCompanyById(app.getCompanyId());
                if (company != null) companyName = company.getName();
            } catch (Exception ignored) {}
            try {
                var position = positionClient.getPositionById(app.getPositionId());
                if (position != null) positionTitle = position.getTitle();
            } catch (Exception ignored) {}

            return PipelineVO.builder()
                    .applicationId(app.getId())
                    .companyName(companyName)
                    .positionTitle(positionTitle)
                    .updatedAt(app.getUpdatedAt())
                    .stages(stages)
                    .build();
        }).collect(Collectors.toList());
    }

    /** 批量加载面试记录 */
    private Map<Long, List<Interview>> batchLoadInterviews(List<Long> applicationIds) {
        if (applicationIds.isEmpty()) return Map.of();
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Interview>();
        wrapper.in(Interview::getApplicationId, applicationIds);
        return interviewMapper.selectList(wrapper).stream()
                .collect(Collectors.groupingBy(Interview::getApplicationId));
    }

    /** 批量加载 Offer */
    private Map<Long, Offer> batchLoadOffers(List<Long> applicationIds) {
        if (applicationIds.isEmpty()) return Map.of();
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Offer>();
        wrapper.in(Offer::getApplicationId, applicationIds);
        return offerMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(Offer::getApplicationId, o -> o, (a, b) -> a));
    }

    /** 构建单个投递的阶段灯列表 */
    private List<PipelineVO.StageInfo> buildStages(Application app, List<Interview> interviews, Offer offer) {
        Set<String> enabled = parsePipelineConfig(app);
        List<StageDef> stageDefs = buildStageDefs(enabled);

        // 面试按 round 建索引
        Map<String, Interview> interviewByStage = new HashMap<>();
        Set<String> passedRounds = new HashSet<>();
        for (Interview iv : interviews) {
            String key = switch (iv.getRound()) {
                case HR -> "HR_INTERVIEW";
                case FIRST -> "INTERVIEW_1";
                case SECOND -> "INTERVIEW_2";
                case THIRD -> "INTERVIEW_3";
                case FOURTH -> "INTERVIEW_4";
            };
            interviewByStage.merge(key, iv, (a, b) ->
                    a.getUpdatedAt() != null && b.getUpdatedAt() != null && b.getUpdatedAt().isAfter(a.getUpdatedAt()) ? b : a);
            if (iv.getResult() == InterviewResult.PASSED) {
                passedRounds.add(key);
            }
        }

        ApplicationStatus status = app.getStatus();
        int statusOrd = status.ordinal();
        boolean isTerminal = status == ApplicationStatus.REJECTED || status == ApplicationStatus.WITHDRAWN;

        // 找到哪个阶段是"当前阶段"（ACTIVE）
        int activeIdx = resolveActiveIndex(stageDefs, status, app.getCurrentStage(), enabled, passedRounds, interviewByStage);
        // 如果是终止态，找到哪个阶段"出事了"
        int failIdx = isTerminal ? resolveFailIndex(stageDefs, interviews, status) : -1;

        List<PipelineVO.StageInfo> result = new ArrayList<>();
        for (int i = 0; i < stageDefs.size(); i++) {
            StageDef def = stageDefs.get(i);
            String stageKey = def.key;
            Interview iv = interviewByStage.get(stageKey);

            String stageStatus = computeStageStatus(
                    i, stageKey, stageDefs, status, statusOrd, isTerminal,
                    activeIdx, failIdx, iv, passedRounds, offer, enabled, interviews);

            result.add(PipelineVO.StageInfo.builder()
                    .stage(stageKey).label(def.label).status(stageStatus).build());
        }
        return result;
    }

    /** 核心：计算每个阶段的状态灯 */
    private String computeStageStatus(int i, String stageKey, List<StageDef> stageDefs,
                                      ApplicationStatus status, int statusOrd, boolean isTerminal,
                                      int activeIdx, int failIdx, Interview iv,
                                      Set<String> passedRounds, Offer offer,
                                      Set<String> enabled, List<Interview> interviews) {

        // ── 终止态 ──
        if (status == ApplicationStatus.WITHDRAWN) return "WITHDRAWN";

        if (status == ApplicationStatus.REJECTED) {
            // 该阶段有 FAILED 面试 → 红叉
            if (iv != null && iv.getResult() == InterviewResult.FAILED) return "FAILED";
            // failIdx 之前的 → 已完成
            if (failIdx >= 0 && i < failIdx) return "COMPLETED";
            // failIdx 位置 → 红叉
            if (failIdx >= 0 && i == failIdx) return "FAILED";
            // failIdx 之后的 → 暗灯
            if (failIdx >= 0) return "PENDING";
            // 没有 failIdx（直接拒绝，无具体失败阶段）→ 一律已完成
            return "COMPLETED";
        }

        // ── SAVED → 全暗 ──
        if (status == ApplicationStatus.SAVED) return "PENDING";

        // ── 正常流转 ──
        boolean isPast = i < activeIdx;
        boolean isCurrent = i == activeIdx;
        boolean isFuture = i > activeIdx;

        if (isPast) {
            // 已过的阶段 → 绿色 ✓（除非有 FAILED 面试）
            if (iv != null && iv.getResult() == InterviewResult.FAILED) return "FAILED";
            return "COMPLETED";
        }

        if (isCurrent) {
            // ASSESSMENT/EXAM 子阶段
            if ("ASSESSMENT".equals(stageKey) || "EXAM".equals(stageKey)) return "ACTIVE";

            // 面试轮次
            if (stageKey.startsWith("INTERVIEW_") || "HR_INTERVIEW".equals(stageKey)) {
                if (iv == null) return "ACTIVE"; // 还没面试记录 → 当前
                if (iv.getResult() == InterviewResult.FAILED) return "FAILED";
                if (iv.getResult() == InterviewResult.PASSED) return "COMPLETED";
                return "ACTIVE"; // PENDING → 当前进行中
            }

            // Offer
            if ("OFFER".equals(stageKey)) {
                if (offer != null && offer.getStatus() == OfferStatus.ACCEPTED) return "COMPLETED";
                if (offer != null && offer.getStatus() == OfferStatus.DECLINED) return "FAILED";
                return "ACTIVE";
            }

            // 普通阶段（APPLIED）
            return "ACTIVE";
        }

        // isFuture → 暗灯
        // 特殊处理：OFFER 阶段如果状态已经 OFFER 但还没到 activeIdx（被前面的跳过）
        // 这种情况由 activeIdx 解析处理，不会走到这里
        return "PENDING";
    }

    /**
     * 解析当前阶段索引（ACTIVE 的灯）
     *
     * 规则：
     * - 按 status 定位到对应的阶段区间
     * - 对 INTERVIEW 状态：遍历面试轮次，找到第一轮没有 PASSED 的
     * - 对 ONLINE_ASSESSMENT：如果 ASSESSMENT+EXAM 都启用，ASSESSMENT 是第一个
     * - 对高级状态（HR_INTERVIEW / OFFER）：定位到对应的阶段
     */
    private int resolveActiveIndex(List<StageDef> stageDefs, ApplicationStatus status,
                                   String currentStage, Set<String> enabled,
                                   Set<String> passedRounds,
                                   Map<String, Interview> interviewByStage) {
        // ── 优先使用 currentStage（精确的流水线位置）──
        if (currentStage != null && !currentStage.isBlank()) {
            int idx = findStageIndex(stageDefs, currentStage);
            if (idx >= 0) return idx;
        }

        // ── 无 currentStage（旧数据），用 status 反推 ──
        String targetPrefix;
        int targetOrd = status.ordinal();

        if (targetOrd <= ApplicationStatus.APPLIED.ordinal()) {
            targetPrefix = "APPLIED";
        } else if (targetOrd == ApplicationStatus.ONLINE_ASSESSMENT.ordinal()) {
            // 旧数据：如果有 ASSESSMENT 启用则走 ASSESSMENT，否则 EXAM
            int assessIdx = findStageIndex(stageDefs, "ASSESSMENT");
            if (assessIdx >= 0) return assessIdx;
            int examIdx = findStageIndex(stageDefs, "EXAM");
            if (examIdx >= 0) return examIdx;
            return findStageIndex(stageDefs, "APPLIED");
        } else if (targetOrd == ApplicationStatus.INTERVIEW.ordinal()) {
            // 旧数据：找第一轮没 PASSED 的
            String[] roundKeys = {"INTERVIEW_1", "INTERVIEW_2", "INTERVIEW_3", "INTERVIEW_4"};
            for (String rk : roundKeys) {
                int idx = findStageIndex(stageDefs, rk);
                if (idx < 0) continue;
                if (passedRounds.contains(rk)) continue;
                return idx;
            }
            for (int j = roundKeys.length - 1; j >= 0; j--) {
                int idx = findStageIndex(stageDefs, roundKeys[j]);
                if (idx >= 0) return idx;
            }
            return findStageIndex(stageDefs, "INTERVIEW_1");
        } else if (targetOrd == ApplicationStatus.HR_INTERVIEW.ordinal()) {
            targetPrefix = "HR_INTERVIEW";
        } else {
            targetPrefix = "OFFER";
        }

        int idx = findStageIndex(stageDefs, targetPrefix);
        return idx >= 0 ? idx : 0;
    }

    /**
     * 在终止态时，找到"出事"的阶段索引
     * - WITHDRAWN → 没有具体出事阶段，统一灰色
     * - REJECTED → 找到有 FAILED 面试的阶段，或按 status 推断
     */
    private int resolveFailIndex(List<StageDef> stageDefs, List<Interview> interviews, ApplicationStatus status) {
        if (status != ApplicationStatus.REJECTED) return -1;

        // 看哪个面试记录 FAILED 了
        for (Interview iv : interviews) {
            if (iv.getResult() == InterviewResult.FAILED) {
                String key = switch (iv.getRound()) {
                    case HR -> "HR_INTERVIEW";
                    case FIRST -> "INTERVIEW_1";
                    case SECOND -> "INTERVIEW_2";
                    case THIRD -> "INTERVIEW_3";
                    case FOURTH -> "INTERVIEW_4";
                };
                int idx = findStageIndex(stageDefs, key);
                if (idx >= 0) return idx;
            }
        }

        // 没有 FAILED 面试记录 → 用 status 定位
        // 如果状态从 ONLINE_ASSESSMENT 直接变成 REJECTED → 笔试阶段失败
        // 但我们现在没记录从哪里拒绝的，只能看 status 之前的阶段
        // 直接返回 -1，由 computeStageStatus 处理为 COMPLETED
        return -1;
    }

    /** 在阶段定义列表中查找特定 key 的索引 */
    private int findStageIndex(List<StageDef> stageDefs, String key) {
        for (int i = 0; i < stageDefs.size(); i++) {
            if (stageDefs.get(i).key.equals(key)) return i;
        }
        return -1;
    }

    /** 解析 pipeline_config 为可选阶段集合 */
    private Set<String> parsePipelineConfig(Application app) {
        String config = app.getPipelineConfig();
        if (config == null || config.isBlank()) return Set.of();
        Set<String> result = new HashSet<>(Arrays.asList(config.split(",")));
        result.removeIf(String::isBlank);
        return result;
    }

    /** 按顺序构建阶段定义列表 */
    private List<StageDef> buildStageDefs(Set<String> enabled) {
        List<StageDef> defs = new ArrayList<>();
        defs.add(new StageDef("APPLIED", "已投递"));
        if (enabled.contains("ASSESSMENT")) defs.add(new StageDef("ASSESSMENT", "测评"));
        if (enabled.contains("EXAM")) defs.add(new StageDef("EXAM", "笔试"));
        defs.add(new StageDef("INTERVIEW_1", "一面"));
        if (enabled.contains("INTERVIEW_2")) defs.add(new StageDef("INTERVIEW_2", "二面"));
        if (enabled.contains("INTERVIEW_3")) defs.add(new StageDef("INTERVIEW_3", "三面"));
        if (enabled.contains("INTERVIEW_4")) defs.add(new StageDef("INTERVIEW_4", "四面"));
        defs.add(new StageDef("HR_INTERVIEW", "HR面"));
        defs.add(new StageDef("OFFER", "Offer"));
        return defs;
    }

    @lombok.Value
    private static class StageDef {
        String key;
        String label;
    }

    // 当前状态 → 允许跳转到的状态集合
    private static final Map<ApplicationStatus, Set<ApplicationStatus>>
            ALLOWED_TRANSITIONS = Map.of(
            ApplicationStatus.SAVED, Set.of(ApplicationStatus.APPLIED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.APPLIED, Set.of(ApplicationStatus.ONLINE_ASSESSMENT, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.ONLINE_ASSESSMENT, Set.of(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.INTERVIEW, Set.of(ApplicationStatus.HR_INTERVIEW, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.HR_INTERVIEW, Set.of(ApplicationStatus.OFFER, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.OFFER, Set.of(ApplicationStatus.WITHDRAWN)
            // REJECTED 和 WITHDRAWN 是终止态，没有可跳转的目标，所以不在这里
    );

}
