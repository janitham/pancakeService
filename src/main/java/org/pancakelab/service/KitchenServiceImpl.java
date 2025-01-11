package org.pancakelab.service;

import org.pancakelab.model.ORDER_STATUS;
import org.pancakelab.model.OrderInfo;

import java.util.UUID;
import java.util.concurrent.*;

public class KitchenServiceImpl implements KitchenService {
    private static KitchenServiceImpl instance;
    private final ExecutorService deliveryExecutor;
    private final BlockingDeque<UUID> deliveryQueue;
    private final ConcurrentMap<UUID, OrderInfo> orders;

    private KitchenServiceImpl(
            final BlockingDeque<UUID> deliveryQueue,
            final ConcurrentMap<UUID, OrderInfo> orders,
            final ExecutorService executorService
    ) {
        this.deliveryQueue = deliveryQueue;
        this.orders = orders;
        this.deliveryExecutor = executorService;
    }

    public static synchronized KitchenServiceImpl getInstance(
            final int numberOfChefsInTheKitchen,
            final BlockingDeque<UUID> deliveryQueue,
            final ConcurrentMap<UUID, OrderInfo> orders
    ) {
        if (instance == null) {
            var deliveryExecutor = Executors.newFixedThreadPool(numberOfChefsInTheKitchen);
            instance = new KitchenServiceImpl(deliveryQueue, orders, deliveryExecutor);
        }
        return instance;
    }

    public static synchronized KitchenServiceImpl getInstance(
            final BlockingDeque<UUID> deliveryQueue,
            final ConcurrentMap<UUID, OrderInfo> orders,
            final ExecutorService deliveryExecutor
    ) {
        if (instance == null) {
            instance = new KitchenServiceImpl(deliveryQueue, orders, deliveryExecutor);
        }
        return instance;
    }

    @Override
    public Future<ORDER_STATUS> processOrder(UUID orderId) {
        return deliveryExecutor.submit(new PreparationTask(deliveryQueue, orders, orderId));
    }

    public void shutdown() {
        deliveryExecutor.shutdown();
        try {
            if (!deliveryExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                deliveryExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            deliveryExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}