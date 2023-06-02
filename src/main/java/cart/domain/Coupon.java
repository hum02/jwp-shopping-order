package cart.domain;

import cart.domain.general.Money;

public abstract class Coupon {
    public abstract Money apply(Money price);

    public abstract Long getId();

    public abstract Money getDiscountPrice();

    public abstract String getName();
    public abstract Long getMemberId();

    public abstract String getImageUrl();

}
