package org.pancakelab.model;

import org.pancakelab.util.PancakeFactoryMenu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrderDetails {
    private final DeliveryInfo deliveryInfo;
    private final Map<Pancake, Integer> pancakes;
    private final UUID orderId;

    private OrderDetails(UUID orderId, DeliveryInfo deliveryInfo, Map<Pancake, Integer> pancakes) {
        this.orderId = orderId;
        this.deliveryInfo = deliveryInfo;
        this.pancakes = Map.copyOf(pancakes);
    }

    public UUID getOrderId() {
        return orderId;
    }

    public DeliveryInfo getDeliveryInfo() {
        return deliveryInfo;
    }

    public Map<Pancake, Integer> getPancakes() {
        return pancakes;
    }

    @Override
    public String toString() {
        return "OrderDetails{" +
                "deliveryInfo=" + deliveryInfo +
                ", pancakes=" + pancakes +
                '}';
    }

    public static class Builder {
        private DeliveryInfo deliveryInfo;
        private final Map<Pancake, Integer> pancakes = new HashMap<>();
        private UUID orderId;
        private Map<PancakeFactoryMenu.PANCAKE_TYPE, Integer> pancakeItems;

        public Builder withOrderId(UUID orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder withDeliveryInfo(DeliveryInfo deliveryInfo) {
            this.deliveryInfo = deliveryInfo;
            return this;
        }

        public Builder addPancake(Pancake pancake, int quantity) {
            this.pancakes.put(pancake, quantity);
            return this;
        }

        public Builder addPancake(Pancake pancake) {
            this.pancakes.merge(pancake, 1, Integer::sum);
            return this;
        }

        public OrderDetails build() {
            if (deliveryInfo == null) {
                throw new IllegalArgumentException("DeliveryInfo is required");
            }
            if (orderId == null) {
                orderId = UUID.randomUUID();
            }
            if(pancakeItems == null){
                throw new IllegalArgumentException("Order can not be completed without pancakes");
            }
            return new OrderDetails(orderId, deliveryInfo, pancakes);
        }

        public Builder withPanCakes(Map<PancakeFactoryMenu.PANCAKE_TYPE, Integer> pancakeTypeIntegerMap) {
            this.pancakeItems = new HashMap<>(pancakeTypeIntegerMap);
            return this;
        }
    }
}