package com.offerpilot.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.offerpilot.common.exception.BusinessException;
import com.offerpilot.notification.entity.Notification;
import com.offerpilot.notification.mapper.NotificationMapper;
import com.offerpilot.notification.service.impl.NotificationServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * NotificationServiceImpl 单元测试
 * <p>
 * 覆盖：create 落库 / listByUserId 查询 / countUnread 未读数 / markRead 单条已读（含越权）/ markAllRead 全部已读
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeAll
    static void initMyBatisPlus() {
        // 初始化 MyBatis-Plus 的 lambda 缓存，否则 LambdaUpdateWrapper
        // 在使用 Notification::getXxx 时会抛 "can not find lambda cache" 异常
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new Configuration(), "");
        TableInfoHelper.initTableInfo(assistant, Notification.class);
    }

    // ========================================================================
    // 1. create —— 落库
    // ========================================================================

    @Nested
    @DisplayName("create 创建通知")
    class Create {

        @Test
        @DisplayName("插入一条未读通知，字段完整")
        void createInsertsUnreadNotification() {
            notificationService.create(3L, "STATUS_CHANGE", "投递成功", "字节跳动 · 后端工程师 已投递，祝好运！");

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationMapper).insert(captor.capture());

            Notification saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(3L);
            assertThat(saved.getType()).isEqualTo("STATUS_CHANGE");
            assertThat(saved.getTitle()).isEqualTo("投递成功");
            assertThat(saved.getContent()).isEqualTo("字节跳动 · 后端工程师 已投递，祝好运！");
            assertThat(saved.getIsRead()).isFalse();
        }
    }

    // ========================================================================
    // 2. listByUserId —— 查询
    // ========================================================================

    @Nested
    @DisplayName("listByUserId 查询")
    class ListByUserId {

        @Test
        @DisplayName("按 userId 过滤并返回通知列表")
        void returnsNotificationsOfUser() {
            Notification n1 = new Notification();
            n1.setId(1L);
            n1.setUserId(3L);
            n1.setIsRead(false);

            Notification n2 = new Notification();
            n2.setId(2L);
            n2.setUserId(3L);
            n2.setIsRead(true);

            when(notificationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(n1, n2));

            List<Notification> result = notificationService.listByUserId(3L);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
        }
    }

    // ========================================================================
    // 3. countUnread —— 未读数
    // ========================================================================

    @Nested
    @DisplayName("countUnread 未读数")
    class CountUnread {

        @Test
        @DisplayName("有未读时返回计数")
        void returnsUnreadCount() {
            when(notificationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            long count = notificationService.countUnread(3L);

            assertThat(count).isEqualTo(5L);
        }

        @Test
        @DisplayName("selectCount 返回 null 时兜底为 0")
        void nullCountFallsBackToZero() {
            when(notificationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(null);

            long count = notificationService.countUnread(3L);

            assertThat(count).isZero();
        }
    }

    // ========================================================================
    // 4. markRead —— 单条已读（含越权）
    // ========================================================================

    @Nested
    @DisplayName("markRead 单条已读")
    class MarkRead {

        @Test
        @DisplayName("通知存在且归属正确时更新为已读")
        void marksReadWhenOwnerMatches() {
            Notification notification = new Notification();
            notification.setId(1L);
            notification.setUserId(3L);
            notification.setIsRead(false);
            when(notificationMapper.selectById(1L)).thenReturn(notification);

            notificationService.markRead(1L, 3L);

            ArgumentCaptor<LambdaUpdateWrapper<Notification>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
            verify(notificationMapper).update(isNull(), captor.capture());
            assertThat(captor.getValue()).isNotNull();
        }

        @Test
        @DisplayName("通知不存在时抛出 NOT_FOUND")
        void throwsWhenNotFound() {
            when(notificationMapper.selectById(99L)).thenReturn(null);

            assertThatThrownBy(() -> notificationService.markRead(99L, 3L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("通知不存在");

            verify(notificationMapper, never()).update(any(), any());
        }

        @Test
        @DisplayName("越权操作时抛出 FORBIDDEN")
        void throwsWhenNotOwner() {
            Notification notification = new Notification();
            notification.setId(1L);
            notification.setUserId(3L);
            when(notificationMapper.selectById(1L)).thenReturn(notification);

            assertThatThrownBy(() -> notificationService.markRead(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("无权操作此通知");

            verify(notificationMapper, never()).update(any(), any());
        }
    }

    // ========================================================================
    // 5. markAllRead —— 全部已读
    // ========================================================================

    @Nested
    @DisplayName("markAllRead 全部已读")
    class MarkAllRead {

        @Test
        @DisplayName("更新该用户全部通知为已读")
        void marksAllReadForUser() {
            notificationService.markAllRead(3L);

            verify(notificationMapper).update(isNull(), any(LambdaUpdateWrapper.class));
        }
    }
}
