package cart.application;

import cart.dao.CouponDao;
import cart.domain.*;
import cart.dto.CouponResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponService {

    private static final int COUPON_ADD_PRICE = 100_000;
    private static final int DEFAULT_DISCOUNT_MONEY = 1_000;

    private final CouponDao couponDao;

    public CouponService(CouponDao couponDao) {
        this.couponDao = couponDao;
    }

    @Transactional
    public Money apply(Order order, Long couponId) {
        Coupon coupon = couponDao.findUnusedById(couponId);
        Money discounting = order.apply(coupon);
        couponDao.updateToUsed(couponId);
        return discounting;
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> findByMember(Member member) {
        List<Coupon> coupons = couponDao.findByMemberId(member.getId());
        return coupons.stream().map(CouponResponse::of).collect(Collectors.toList());
    }

    @Transactional
    public void addCouponDependsOnPay(Member member, Order order) {
        if (!order.isBiggerPrice(Money.from(COUPON_ADD_PRICE))) {
            return;
        }

        Coupon coupon = new FixedDiscountCoupon(member.getId(), "10만원 주문 - 1000원 할인쿠폰", "image.com", DEFAULT_DISCOUNT_MONEY);
        couponDao.save(coupon);
    }
}
