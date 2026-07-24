package com.offerpilot.application.service;

import com.offerpilot.api.client.CompanyClient;
import com.offerpilot.api.client.PositionClient;
import com.offerpilot.api.dto.CompanyDTO;
import com.offerpilot.api.dto.PositionDTO;
import com.offerpilot.application.dto.AdvanceRequest;
import com.offerpilot.application.entity.Application;
import com.offerpilot.application.entity.Interview;
import com.offerpilot.application.entity.Offer;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import com.offerpilot.application.enums.ApplicationSource;
import com.offerpilot.application.enums.ApplicationStatus;
import com.offerpilot.application.mapper.ApplicationMapper;
import com.offerpilot.application.mapper.InterviewMapper;
import com.offerpilot.application.mapper.OfferMapper;
import com.offerpilot.application.service.impl.ApplicationServiceImpl;
import com.offerpilot.application.vo.ApplicationVO;
import com.offerpilot.application.vo.DashboardVO;
import com.offerpilot.application.vo.PipelineVO;
import com.offerpilot.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static com.offerpilot.application.enums.ApplicationStatus.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ApplicationServiceImpl 单元测试
 *
 * 覆盖：CRUD、状态机流转、Feign 跨服务组装、Dashboard 统计、Pipeline 流水线、一键推进
 */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock private ApplicationMapper applicationMapper;
    @Mock private InterviewMapper interviewMapper;
    @Mock private OfferMapper offerMapper;
    @Mock private InterviewService interviewService;
    @Mock private OfferService offerService;
    @Mock private CompanyClient companyClient;
    @Mock private PositionClient positionClient;
    @Mock private CacheService cacheService;

    private ApplicationServiceImpl service;

    private Application app;
    private final Long userId = 1L;

    @BeforeAll
    static void initMyBatisPlus() {
        // 初始化 MyBatis-Plus 的 lambda 缓存，否则 LambdaUpdateWrapper
        // 在使用 Application::getXxx 时会抛 "can not find lambda cache" 异常
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new Configuration(), "");
        TableInfoHelper.initTableInfo(assistant, Application.class);
    }

    @BeforeEach
    void setUp() {
        service = new ApplicationServiceImpl(
                applicationMapper, interviewMapper, offerMapper,
                interviewService, offerService, companyClient, positionClient, cacheService);

        // 让 CacheService 的 mock 实际执行 loader（走 Feign 调用）
        lenient().when(cacheService.getOrLoad(anyString(), eq(String.class), any()))
                .thenAnswer(invocation -> {
                    Supplier<String> loader = invocation.getArgument(2);
                    return loader.get();
                });

        app = new Application();
        app.setId(100L);
        app.setUserId(userId);
        app.setCompanyId(10L);
        app.setPositionId(20L);
        app.setStatus(SAVED);
        app.setAppliedAt(LocalDateTime.now());
        app.setCurrentStage("APPLIED");
    }

    // ========================================================================
    // 1. 基础 CRUD
    // ========================================================================

    @Nested
    @DisplayName("基础 CRUD")
    class BasicCrud {

        @Test
        @DisplayName("findById 返回正确记录")
        void findById() {
            when(applicationMapper.selectById(100L)).thenReturn(app);
            assertThat(service.findById(100L)).isEqualTo(app);
        }

        @Test
        @DisplayName("findById 不存在时返回 null")
        void findByIdNotFound() {
            when(applicationMapper.selectById(999L)).thenReturn(null);
            assertThat(service.findById(999L)).isNull();
        }

        @Test
        @DisplayName("create 调用 insert 并返回")
        void create() {
            when(applicationMapper.insert(app)).thenReturn(1);
            assertThat(service.create(app)).isEqualTo(app);
            verify(applicationMapper).insert(app);
        }

        @Test
        @DisplayName("findAllByUserId 按 userId 过滤并按更新时间倒序")
        void findAllByUserId() {
            when(applicationMapper.selectList(any())).thenReturn(List.of(app));
            List<Application> result = service.findAllByUserId(userId);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("deleteById 调用 delete")
        void deleteById() {
            service.deleteById(100L);
            verify(applicationMapper).deleteById(100L);
        }
    }

    // ========================================================================
    // 2. 状态机流转
    // ========================================================================

    @Nested
    @DisplayName("状态机流转")
    class StatusTransition {

        // ---- 合法流转 ----

        @Test
        @DisplayName("SAVED → APPLIED 合法")
        void savedToApplied() {
            givenAppWithStatus(SAVED);
            expectUpdate();
            Application result = service.updateStatus(100L, "APPLIED");
            assertThat(result.getStatus()).isEqualTo(APPLIED);
        }

        @Test
        @DisplayName("APPLIED → ONLINE_ASSESSMENT 合法")
        void appliedToOnlineAssessment() {
            givenAppWithStatus(APPLIED);
            expectUpdate();
            Application result = service.updateStatus(100L, "ONLINE_ASSESSMENT");
            assertThat(result.getStatus()).isEqualTo(ONLINE_ASSESSMENT);
        }

        @Test
        @DisplayName("ONLINE_ASSESSMENT → INTERVIEW 合法")
        void onlineAssessmentToInterview() {
            givenAppWithStatus(ONLINE_ASSESSMENT);
            expectUpdate();
            Application result = service.updateStatus(100L, "INTERVIEW");
            assertThat(result.getStatus()).isEqualTo(INTERVIEW);
        }

        @Test
        @DisplayName("INTERVIEW → HR_INTERVIEW 合法")
        void interviewToHr() {
            givenAppWithStatus(INTERVIEW);
            expectUpdate();
            Application result = service.updateStatus(100L, "HR_INTERVIEW");
            assertThat(result.getStatus()).isEqualTo(HR_INTERVIEW);
        }

        @Test
        @DisplayName("HR_INTERVIEW → OFFER 合法")
        void hrToOffer() {
            givenAppWithStatus(HR_INTERVIEW);
            expectUpdate();
            Application result = service.updateStatus(100L, "OFFER");
            assertThat(result.getStatus()).isEqualTo(OFFER);
        }

        @Test
        @DisplayName("SAVED → WITHDRAWN 终止流转合法")
        void savedToWithdrawn() {
            givenAppWithStatus(SAVED);
            expectUpdate();
            Application result = service.updateStatus(100L, "WITHDRAWN");
            assertThat(result.getStatus()).isEqualTo(WITHDRAWN);
        }

        @Test
        @DisplayName("全链正向流转验证：SAVED → OFFER 不绕路")
        void fullChainForward() {
            // SAVED → APPLIED
            givenAppWithStatus(SAVED); expectUpdate();
            assertThat(service.updateStatus(100L, "APPLIED").getStatus()).isEqualTo(APPLIED);

            // APPLIED → ONLINE_ASSESSMENT
            givenAppWithStatus(APPLIED); expectUpdate();
            assertThat(service.updateStatus(100L, "ONLINE_ASSESSMENT").getStatus()).isEqualTo(ONLINE_ASSESSMENT);

            // ONLINE_ASSESSMENT → INTERVIEW
            givenAppWithStatus(ONLINE_ASSESSMENT); expectUpdate();
            assertThat(service.updateStatus(100L, "INTERVIEW").getStatus()).isEqualTo(INTERVIEW);

            // INTERVIEW → HR_INTERVIEW
            givenAppWithStatus(INTERVIEW); expectUpdate();
            assertThat(service.updateStatus(100L, "HR_INTERVIEW").getStatus()).isEqualTo(HR_INTERVIEW);

            // HR_INTERVIEW → OFFER
            givenAppWithStatus(HR_INTERVIEW); expectUpdate();
            assertThat(service.updateStatus(100L, "OFFER").getStatus()).isEqualTo(OFFER);
        }

        // ---- 非法流转 ----

        @Test
        @DisplayName("SAVED → INTERVIEW 跳过中间状态 → 抛异常")
        void savedToInterviewInvalid() {
            givenAppWithStatus(SAVED);
            assertThatThrownBy(() -> service.updateStatus(100L, "INTERVIEW"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不允许");
        }

        @Test
        @DisplayName("SAVED → OFFER 跳过全部 → 抛异常")
        void savedToOfferInvalid() {
            givenAppWithStatus(SAVED);
            assertThatThrownBy(() -> service.updateStatus(100L, "OFFER"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不允许");
        }

        @Test
        @DisplayName("SAVED → REJECTED 跳过中间 → 抛异常")
        void savedToRejectedInvalid() {
            givenAppWithStatus(SAVED);
            assertThatThrownBy(() -> service.updateStatus(100L, "REJECTED"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不允许");
        }

        @Test
        @DisplayName("终止态 REJECTED 不可变更")
        void rejectedCannotChange() {
            givenAppWithStatus(REJECTED);
            assertThatThrownBy(() -> service.updateStatus(100L, "WITHDRAWN"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("已终止");
        }

        @Test
        @DisplayName("终止态 WITHDRAWN 不可变更")
        void withdrawnCannotChange() {
            givenAppWithStatus(WITHDRAWN);
            assertThatThrownBy(() -> service.updateStatus(100L, "APPLIED"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("已终止");
        }

        @Test
        @DisplayName("无效的状态字符串 → 抛异常")
        void invalidStatusString() {
            givenAppWithStatus(SAVED);
            assertThatThrownBy(() -> service.updateStatus(100L, "INVALID_STATUS"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无效");
        }

        @Test
        @DisplayName("不存在的 id → 返回 null")
        void notFound() {
            when(applicationMapper.selectById(999L)).thenReturn(null);
            assertThat(service.updateStatus(999L, "APPLIED")).isNull();
        }
    }

    // ========================================================================
    // 3. Feign 跨服务组装
    // ========================================================================

    @Nested
    @DisplayName("Feign 跨服务组装 enrichVO")
    class EnrichVO {

        @Test
        @DisplayName("Feign 成功返回 companyName + positionTitle")
        void feignSuccess() {
            app.setStatus(APPLIED);
            when(companyClient.getCompanyById(10L)).thenReturn(new CompanyDTO(10L, "字节跳动"));
            when(positionClient.getPositionById(20L)).thenReturn(new PositionDTO(20L, "后端开发"));

            ApplicationVO vo = service.enrichVO(app);
            assertThat(vo.getCompanyName()).isEqualTo("字节跳动");
            assertThat(vo.getPositionTitle()).isEqualTo("后端开发");
        }

        @Test
        @DisplayName("Feign 失败时 companyName/positionTitle 显示加载失败，不影响主流程")
        void feignFailure() {
            app.setStatus(APPLIED);
            when(companyClient.getCompanyById(10L)).thenThrow(new RuntimeException("服务不可用"));
            when(positionClient.getPositionById(20L)).thenThrow(new RuntimeException("服务不可用"));

            ApplicationVO vo = service.enrichVO(app);
            assertThat(vo.getCompanyName()).isEqualTo("(⏳ 加载失败)");
            assertThat(vo.getPositionTitle()).isEqualTo("(⏳ 加载失败)");
            // 核心字段不受影响
            assertThat(vo.getId()).isEqualTo(100L);
            assertThat(vo.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("入参为 null 时返回 null")
        void nullInput() {
            assertThat(service.enrichVO(null)).isNull();
        }
    }

    // ========================================================================
    // 4. Dashboard 统计
    // ========================================================================

    @Nested
    @DisplayName("Dashboard 统计")
    class DashboardStats {

        @Test
        @DisplayName("统计数据正确聚合")
        void stats() {
            List<Application> all = List.of(
                    createApp(1L, APPLIED, "BOSS_ZHIPIN", LocalDateTime.now().minusDays(1)),
                    createApp(2L, INTERVIEW, "LINKEDIN", LocalDateTime.now().minusDays(2)),
                    createApp(3L, OFFER, "BOSS_ZHIPIN", LocalDateTime.now().minusDays(3)),
                    createApp(4L, REJECTED, null, LocalDateTime.now().minusDays(4)),
                    createApp(5L, WITHDRAWN, "CAMPUS_RECRUITMENT", LocalDateTime.now().minusDays(5))
            );
            when(applicationMapper.selectList(any())).thenReturn(all);

            DashboardVO stats = service.getDashboardStats(userId);

            assertThat(stats.getTotalApplications()).isEqualTo(5);
            assertThat(stats.getActiveCount()).isEqualTo(3);  // 排除 REJECTED + WITHDRAWN
            assertThat(stats.getInterviewCount()).isEqualTo(1); // INTERVIEW 状态
            assertThat(stats.getOfferCount()).isEqualTo(1);    // OFFER 状态
            assertThat(stats.getDailyTrend()).hasSize(14);
            assertThat(stats.getSourceDistribution()).hasSize(3); // BOSS_ZHIPIN, LINKEDIN, CAMPUS_RECRUITMENT
        }

        @Test
        @DisplayName("无数据时统计返回零值")
        void emptyStats() {
            when(applicationMapper.selectList(any())).thenReturn(List.of());
            DashboardVO stats = service.getDashboardStats(userId);
            assertThat(stats.getTotalApplications()).isZero();
            assertThat(stats.getActiveCount()).isZero();
            assertThat(stats.getInterviewCount()).isZero();
            assertThat(stats.getOfferCount()).isZero();
            assertThat(stats.getDailyTrend()).hasSize(14);
            assertThat(stats.getSourceDistribution()).isEmpty();
        }
    }

    // ========================================================================
    // 5. Pipeline 流水线
    // ========================================================================

    @Nested
    @DisplayName("Pipeline 流水线")
    class Pipeline {

        @Test
        @DisplayName("返回按更新时间倒序的流水线")
        void pipeline() {
            Application a1 = createApp(1L, APPLIED, "BOSS_ZHIPIN", LocalDateTime.now());
            a1.setCompanyId(10L);
            a1.setPositionId(20L);
            Application a2 = createApp(2L, SAVED, "LINKEDIN", LocalDateTime.now().minusHours(1));
            a2.setCompanyId(11L);
            a2.setPositionId(21L);

            when(applicationMapper.selectList(any())).thenReturn(new java.util.ArrayList<>(List.of(a1, a2)));
            when(interviewMapper.selectList(any())).thenReturn(List.of());
            when(offerMapper.selectList(any())).thenReturn(List.of());
            when(companyClient.getCompanyById(10L)).thenReturn(new CompanyDTO(10L, "字节跳动"));
            when(companyClient.getCompanyById(11L)).thenReturn(new CompanyDTO(11L, "阿里巴巴"));

            List<PipelineVO> result = service.getPipeline(userId);
            assertThat(result).hasSize(2);
            // 按 updatedAt 倒序
            assertThat(result.get(0).getCompanyName()).isEqualTo("字节跳动");
            assertThat(result.get(1).getCompanyName()).isEqualTo("阿里巴巴");
        }
    }

    // ========================================================================
    // 6. 一键推进
    // ========================================================================

    @Nested
    @DisplayName("一键推进 advance")
    class Advance {

        @Test
        @DisplayName("所有权校验失败抛异常")
        void ownershipCheck() {
            app.setUserId(999L);
            when(applicationMapper.selectById(100L)).thenReturn(app);
            AdvanceRequest req = new AdvanceRequest();
            req.setTargetStage("APPLIED");
            assertThatThrownBy(() -> service.advance(100L, userId, req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权操作");
        }

        @Test
        @DisplayName("正常推进到下一阶段")
        void normalAdvance() {
            app.setStatus(SAVED);
            app.setCurrentStage(null);
            when(applicationMapper.selectById(100L)).thenReturn(app);

            AdvanceRequest req = new AdvanceRequest();
            req.setTargetStage("APPLIED");

            // 更新后的 application
            Application updated = new Application();
            updated.setId(100L);
            updated.setUserId(userId);
            updated.setCompanyId(10L);
            updated.setPositionId(20L);
            updated.setStatus(APPLIED);
            updated.setCurrentStage("APPLIED");
            when(applicationMapper.selectById(100L)).thenReturn(app, updated);
            when(companyClient.getCompanyById(10L)).thenReturn(new CompanyDTO(10L, "字节跳动"));
            when(positionClient.getPositionById(20L)).thenReturn(new PositionDTO(20L, "后端开发"));

            ApplicationVO vo = service.advance(100L, userId, req);
            assertThat(vo).isNotNull();
            assertThat(vo.getStatus()).isEqualTo(APPLIED);
        }

        @Test
        @DisplayName("存在不返回 404，找不到返回 null")
        void notFound() {
            when(applicationMapper.selectById(999L)).thenReturn(null);
            AdvanceRequest req = new AdvanceRequest();
            req.setTargetStage("OFFER");
            assertThat(service.advance(999L, userId, req)).isNull();
        }
    }

    // ========================================================================
    // 工具方法
    // ========================================================================

    private void givenAppWithStatus(ApplicationStatus status) {
        app.setStatus(status);
        when(applicationMapper.selectById(100L)).thenReturn(app);
    }

    private void expectUpdate() {
        when(applicationMapper.updateById(any(Application.class))).thenReturn(1);
    }

    private Application createApp(Long id, ApplicationStatus status, String source, LocalDateTime time) {
        Application a = new Application();
        a.setId(id);
        a.setUserId(userId);
        a.setStatus(status);
        a.setSource(source != null ? ApplicationSource.valueOf(source) : null);
        a.setAppliedAt(time);
        a.setCreatedAt(time);
        a.setUpdatedAt(time);
        return a;
    }
}
