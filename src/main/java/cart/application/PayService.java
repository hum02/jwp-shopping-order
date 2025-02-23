package cart.application;

import cart.dao.MemberDao;
import cart.domain.Member;
import cart.domain.general.Money;
import cart.domain.Order;
import org.springframework.stereotype.Service;

@Service
public class PayService {

    private final MemberDao memberDao;

    public PayService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public Order pay(Order order, Money deliveryFee, Money discounting, Long id) {
        Order confirmed = order.confirmOrder(deliveryFee, discounting);
        Member member = memberDao.getMemberById(id);
        member.payMoney(confirmed);
        memberDao.updateMember(member);
        return confirmed;
    }
}
