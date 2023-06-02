package cart.domain;

import cart.domain.general.Money;

public interface DeliveryPolicy {
    Money calculate(Order order);
}
