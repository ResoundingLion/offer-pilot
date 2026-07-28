package com.offerpilot.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.offerpilot.application.entity.Offer;
import com.offerpilot.application.enums.OfferStatus;
import com.offerpilot.application.mapper.OfferMapper;
import com.offerpilot.application.service.impl.OfferServiceImpl;
import com.offerpilot.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OfferServiceImpl 单元测试
 *
 * 覆盖：基础 CRUD、updateStatus 状态流转（PENDING→ACCEPTED/DECLINED 合法/非法）
 */
@ExtendWith(MockitoExtension.class)
class OfferServiceImplTest {

    @Mock
    private OfferMapper offerMapper;

    private OfferServiceImpl offerService;

    private Offer offer;

    @BeforeEach
    void setUp() {
        offerService = new OfferServiceImpl(offerMapper);

        offer = new Offer();
        offer.setId(1L);
        offer.setApplicationId(100L);
        offer.setSalary("30K*15");
        offer.setBonus("10w");
        offer.setStatus(OfferStatus.PENDING);
        offer.setCreatedAt(LocalDateTime.now());
        offer.setUpdatedAt(LocalDateTime.now());
    }

    // ========================================================================
    // 1. 基础 CRUD（5 个标准方法）
    // ========================================================================

    @Nested
    @DisplayName("基础 CRUD")
    class BasicCrud {

        @Test
        @DisplayName("findById 返回正确记录")
        void findById() {
            when(offerMapper.selectById(1L)).thenReturn(offer);
            assertThat(offerService.findById(1L)).isEqualTo(offer);
        }

        @Test
        @DisplayName("findById 不存在时返回 null")
        void findByIdNotFound() {
            when(offerMapper.selectById(999L)).thenReturn(null);
            assertThat(offerService.findById(999L)).isNull();
        }

        @Test
        @DisplayName("findAll 返回全部")
        void findAll() {
            when(offerMapper.selectList(null)).thenReturn(java.util.List.of(offer));
            assertThat(offerService.findAll()).hasSize(1).containsExactly(offer);
        }

        @Test
        @DisplayName("create 调用 insert 并返回")
        void create() {
            when(offerMapper.insert(offer)).thenReturn(1);
            assertThat(offerService.create(offer)).isEqualTo(offer);
            verify(offerMapper).insert(offer);
        }

        @Test
        @DisplayName("update 调用 updateById")
        void update() {
            when(offerMapper.updateById(offer)).thenReturn(1);
            assertThat(offerService.update(offer)).isEqualTo(offer);
            verify(offerMapper).updateById(offer);
        }

        @Test
        @DisplayName("deleteById 调用 deleteById")
        void deleteById() {
            offerService.deleteById(1L);
            verify(offerMapper).deleteById(1L);
        }

        @Test
        @DisplayName("findByApplicationId 按 applicationId 查询")
        void findByApplicationId() {
            when(offerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(offer);
            assertThat(offerService.findByApplicationId(100L)).isEqualTo(offer);
        }
    }

    // ========================================================================
    // 2. updateStatus 状态流转
    // ========================================================================

    @Nested
    @DisplayName("状态流转 updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("PENDING → ACCEPTED 合法")
        void pendingToAccepted() {
            when(offerMapper.selectById(1L)).thenReturn(offer);
            when(offerMapper.updateById(any(Offer.class))).thenReturn(1);

            Offer result = offerService.updateStatus(1L, "ACCEPTED");
            assertThat(result.getStatus()).isEqualTo(OfferStatus.ACCEPTED);
        }

        @Test
        @DisplayName("PENDING → DECLINED 合法")
        void pendingToDeclined() {
            when(offerMapper.selectById(1L)).thenReturn(offer);
            when(offerMapper.updateById(any(Offer.class))).thenReturn(1);

            Offer result = offerService.updateStatus(1L, "DECLINED");
            assertThat(result.getStatus()).isEqualTo(OfferStatus.DECLINED);
        }

        @Test
        @DisplayName("PENDING → PENDING 非法 → 抛异常")
        void pendingToPendingInvalid() {
            when(offerMapper.selectById(1L)).thenReturn(offer);

            assertThatThrownBy(() -> offerService.updateStatus(1L, "PENDING"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不能变更为");
        }

        @Test
        @DisplayName("ACCEPTED 状态下不能再次变更")
        void acceptedCannotChange() {
            offer.setStatus(OfferStatus.ACCEPTED);
            when(offerMapper.selectById(1L)).thenReturn(offer);

            assertThatThrownBy(() -> offerService.updateStatus(1L, "DECLINED"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只能对「待接受」");
        }

        @Test
        @DisplayName("Offer 不存在时返回 null")
        void notFoundReturnsNull() {
            when(offerMapper.selectById(999L)).thenReturn(null);
            assertThat(offerService.updateStatus(999L, "ACCEPTED")).isNull();
        }

        @Test
        @DisplayName("无效的状态字符串 → 抛异常")
        void invalidStatusString() {
            when(offerMapper.selectById(1L)).thenReturn(offer);

            assertThatThrownBy(() -> offerService.updateStatus(1L, "INVALID_STATUS"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无效的 Offer 状态");
        }
    }
}
