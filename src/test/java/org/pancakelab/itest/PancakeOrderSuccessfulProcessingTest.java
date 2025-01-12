package org.pancakelab.itest;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.pancakelab.model.DeliveryInfo;
import org.pancakelab.model.ORDER_STATUS;
import org.pancakelab.model.OrderInfo;
import org.pancakelab.model.PancakeServiceException;
import org.pancakelab.service.*;
import org.pancakelab.util.PancakeFactoryMenu;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeOrderSuccessfulProcessingTest {

    private static final ConcurrentMap<UUID, OrderInfo> orders = new ConcurrentHashMap<>();
    private static final BlockingDeque<UUID> deliveryQueue = new LinkedBlockingDeque<>();
    private static Thread deliveryService;
    private static KitchenService kitchenService;
    private static OrderService orderService;
    private static UUID orderId;
    private static DeliveryInfo deliveryInfo;

    @BeforeAll
    public static void init() {
        deliveryInfo = new DeliveryInfo("1", "2");
        deliveryService = new Thread(new DeliveryServiceImpl(orders, deliveryQueue));
        kitchenService = KitchenServiceImpl.getInstance(2, deliveryQueue, orders);
        orderService = new OrderServiceImpl(kitchenService, orders);
    }

    @Test
    @Order(1)
    public void whenValidPancakeOrderIsPlaced_thenOrderShouldBePlaced() throws PancakeServiceException {
        orderId = orderService.createOrder(deliveryInfo);
        assertNotNull(orderId);
    }

    @Test
    @Order(2)
    public void whenOrderIsUpdatedWithItemsInTheMenu_thenOrderShouldContainTheItems() {
        var pancakes = Map.of(
                PancakeFactoryMenu.PANCAKE_TYPE.DARK_CHOCOLATE_PANCAKE, 1,
                PancakeFactoryMenu.PANCAKE_TYPE.MILK_CHOCOLATE_PANCAKE, 2
        );
        orderService.addPancakes(orderId, pancakes);
        assertEquals(pancakes, orderService.orderSummary(orderId));
    }

    @Test
    @Order(3)
    public void whenOrderIsCompleted_thenOrderShouldBeProcessedByTheKitchenAndRemoved() throws ExecutionException, InterruptedException {
        var future = orderService.complete(orderId);
        Awaitility.await().until(() -> deliveryQueue.size() == 1);
        assertTrue(deliveryQueue.contains(orderId));
        assertTrue(future.isDone());
        assertEquals(ORDER_STATUS.READY_FOR_DELIVERY, orders.get(orderId).getStatus());
        assertEquals(future.get(), ORDER_STATUS.READY_FOR_DELIVERY);
    }

    @Test
    @Order(4)
    public void whenOrderIsReceivedByTheDeliveryService_thenOrderShouldBeDelivered() {
        deliveryService.start();
        Awaitility.await().until(deliveryQueue::isEmpty);
    }

    @AfterAll
    public static void tearDown() {
        deliveryService.interrupt();
        try {
            deliveryService.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        kitchenService.shutdown();
    }
}