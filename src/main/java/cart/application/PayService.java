package cart.application;

import cart.dao.MemberDao;
import cart.domain.Member;
import cart.domain.Money;
import cart.domain.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayService {

    private final MemberDao memberDao;

    public PayService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    @Transactional
    public Order payOrder(Order order, Money deliveryFee, Money discounting, Long id) {
        Order confirmed = order.confirmOrder(deliveryFee, discounting);
        Member member = memberDao.getMemberById(id);
        member.payMoney(confirmed);
        memberDao.updateMember(member);
        return confirmed;
    }
}
