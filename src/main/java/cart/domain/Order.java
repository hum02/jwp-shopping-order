package cart.domain;

import cart.exception.InvalidOrderCalculationException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Order {

    private final List<CartItem> cartItems;
    private final Member member;
    private final Money price;
    private final OrderState state;

    private Order(List<CartItem> cartItems, Member member, Money price) {
        validateOwner(cartItems, member);
        this.cartItems = cartItems;
        this.member = member;
        this.price = price;
        this.state = OrderState.ORDERED;
    }

    private Order(List<CartItem> cartItems, Member member, Money price, OrderState orderState) {
        validateOwner(cartItems, member);
        this.cartItems = cartItems;
        this.member = member;
        this.price = price;
        this.state = orderState;
    }

    public static Order of(List<CartItem> cartItems, Member member, int price) {
        return new Order(cartItems, member, Money.from(price));
    }

    public Money apply(Coupon coupon) {
        return coupon.apply(price);
    }

    private void validateOwner(List<CartItem> cartItems, Member member) {
        cartItems.forEach(cartItem -> cartItem.checkOwner(member));
    }

    public Order confirmOrder(Money deliveryFee, Money discountingPrice) {
        Money totalPrice = cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(Money.from(0), Money::plus);

        Money calculated = totalPrice.minus(discountingPrice).plus(deliveryFee);
        if (!this.price.equals(calculated)) {
            throw new InvalidOrderCalculationException("요청한 결제금액과 실제 계산 금액이 다릅니다. 잘못된 주문요청입니다.");
        }
        return new Order(this.cartItems, this.member, this.price, OrderState.CONFIRMED);
    }

    public boolean isBiggerPrice(Money money) {
        return this.price.isBiggerThan(money);
    }
    public List<Long> getCartItemIds() {
        return this.cartItems.stream()
                .map(CartItem::getId)
                .collect(Collectors.toList());
    }

    public OrderState getState() {
        return state;
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public Member getMember() {
        return member;
    }

    public Money getPrice() {
        return price;
    }

    enum OrderState {
        ORDERED, CONFIRMED
    }
}
