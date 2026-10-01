package com.yuchen.kami.service;

import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.entity.ShopOrder;
import com.yuchen.kami.mapper.ShopOrderMapper;
import com.yuchen.kami.mapper.ShopUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private ShopOrderMapper shopOrderMapper;
    @Mock private ShopUserMapper shopUserMapper;
    @Mock private ProductService productService;

    @InjectMocks private OrderService orderService;

    @Test
    void cancelOrder_shouldCancel_whenPending() {
        ShopOrder order = new ShopOrder();
        order.setId(1L);
        order.setUserId(99L);
        order.setStatus(ShopOrder.STATUS_PENDING);

        when(shopOrderMapper.selectById(1L)).thenReturn(order);

        orderService.cancelOrder(1L, 99L);

        assertEquals(ShopOrder.STATUS_CANCELLED, order.getStatus());
        verify(shopOrderMapper).updateById(order);
    }

    @Test
    void cancelOrder_shouldReject_whenNotPending() {
        ShopOrder order = new ShopOrder();
        order.setId(2L);
        order.setUserId(99L);
        order.setStatus(ShopOrder.STATUS_DELIVERED);

        when(shopOrderMapper.selectById(2L)).thenReturn(order);

        assertThrows(BusinessException.class, () -> orderService.cancelOrder(2L, 99L));
    }
}
