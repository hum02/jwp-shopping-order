package cart.domain;

import cart.exception.CannotApplyCouponException;

public class FixedDiscountCoupon extends Coupon {

    private final Long id;
    private final Long member_id;
    private final String name;
    private final String imageUrl;
    private final int discountPrice;

    public FixedDiscountCoupon(long member_id, String name, String imageUrl, int discountPrice) {
        this(null, member_id, name, imageUrl, discountPrice);
    }

    public FixedDiscountCoupon(Long id, Long member_id, String name, String imageUrl, int discountPrice) {
        this.id = id;
        this.member_id = member_id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.discountPrice = discountPrice;
    }

    @Override
    public Money apply(Money price) {
        try {
            return Money.from(discountPrice);
        } catch (Exception e) {
            throw new CannotApplyCouponException("유효하지 않은 할인금액입니다. 할인금액은 0이상의 정수이어야 합니다.");
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Money getDiscountPrice() {
        return Money.from(this.discountPrice);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Long getMemberId() {
        return member_id;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }
}
