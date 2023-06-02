package cart.integration;

import cart.dao.MemberDao;
import cart.domain.Member;
import cart.dto.CartItemRequest;
import cart.dto.OrderRequest;
import cart.dto.ProductRequest;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class OrderIntegrationTest extends IntegrationTest {

    @Autowired
    private MemberDao memberDao;

    private Long productId;
    private Member member;

    @BeforeEach
    void setUp() {
        super.setUp();

        productId = createProduct(new ProductRequest("치킨", 10_000, "http://example.com/chicken.jpg"));

        member = memberDao.getMemberById(1L);
    }

    @DisplayName("장바구니의 아이템들을 주문한다. - 성공")
    @Test
    void orderItems_success() {
        CartItemRequest cartItemRequest = new CartItemRequest(productId);
        requestAddCartItem(member, cartItemRequest);

        OrderRequest orderRequest = new OrderRequest(List.of(1L, 2L), 102000, 1L);

        ExtractableResponse<Response> orderResponse = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(member.getEmail(), member.getPassword())
                .body(orderRequest)
                .when()
                .post("/orders")
                .then()
                .log().all()
                .extract();

        ExtractableResponse<Response> findOrderResponse = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(member.getEmail(), member.getPassword())
                .when()
                .get(orderResponse.header("Location"))
                .then()
                .log().all()
                .extract();

        assertThat(orderResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        int price = findOrderResponse.body().jsonPath().getInt("totalPrice");
        assertThat(price).isEqualTo(102000);
    }

    @DisplayName("장바구니의 아이템들을 주문한다. - 성공")
    @Test
    void orderItems_success2() {
        CartItemRequest cartItemRequest = new CartItemRequest(productId);
        requestAddCartItem(member, cartItemRequest);

        OrderRequest orderRequest = new OrderRequest(List.of(1L), 22000, 1L);

        ExtractableResponse<Response> orderResponse = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(member.getEmail(), member.getPassword())
                .body(orderRequest)
                .when()
                .post("/orders")
                .then()
                .log().all()
                .extract();

        ExtractableResponse<Response> findOrderResponse = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(member.getEmail(), member.getPassword())
                .when()
                .get(orderResponse.header("Location"))
                .then()
                .log().all()
                .extract();

        assertThat(orderResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        int price = findOrderResponse.body().jsonPath().getInt("totalPrice");
        assertThat(price).isEqualTo(22000);
    }

    @DisplayName("장바구니의 아이템들을 쿠폰 선택 없이 주문한다. - 성공")
    @Test
    void orderItems_when_couponId_null_success() {

        OrderRequest orderRequest1 = new OrderRequest(List.of(1L), 23000, null);
        OrderRequest orderRequest2 = new OrderRequest(List.of(2L), 83000, 0L);

        ExtractableResponse<Response> orderResponseNull = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(member.getEmail(), member.getPassword())
                .body(orderRequest1)
                .when()
                .post("/orders")
                .then()
                .log().all()
                .extract();

        ExtractableResponse<Response> orderResponseZero = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(member.getEmail(), member.getPassword())
                .body(orderRequest2)
                .when()
                .post("/orders")
                .then()
                .log().all()
                .extract();

        ExtractableResponse<Response> NullCouponSpecific = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(member.getEmail(), member.getPassword())
                .when()
                .get(orderResponseNull.header("Location"))
                .then()
                .log().all()
                .extract();

        assertThat(orderResponseNull.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        int price = NullCouponSpecific.body().jsonPath().getInt("totalPrice");
        assertThat(price).isEqualTo(23000);


        ExtractableResponse<Response> zeroCouponSpecific = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(member.getEmail(), member.getPassword())
                .when()
                .get(orderResponseNull.header("Location"))
                .then()
                .log().all()
                .extract();

        assertThat(orderResponseNull.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        int price2 = zeroCouponSpecific.body().jsonPath().getInt("totalPrice");
        assertThat(price2).isEqualTo(23000);
    }

    @DisplayName("10만원 초과로 주문하면 1000원 쿠폰을 사용자에게 준다. - 성공")
    @Test
    void orderItems_more_than_100000_give_coupon_success() {
        //given
        ExtractableResponse<Response> memberCouponResponse1 = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(member.getEmail(), member.getPassword())
                .when()
                .get("/orders")
                .then()
                .log().all()
                .extract();

        List<Object> beforeCoupons = memberCouponResponse1.jsonPath().getList(".");


        //when
        OrderRequest orderRequest = new OrderRequest(List.of(1L,2L), 103000, null);

        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(member.getEmail(), member.getPassword())
                .body(orderRequest)
                .when()
                .post("/orders")
                .then()
                .log().all()
                .extract();

        //then
        ExtractableResponse<Response> memberCouponResponse2 = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(member.getEmail(), member.getPassword())
                .when()
                .get("/orders")
                .then()
                .log().all()
                .extract();

        List<Object> afterCoupons = memberCouponResponse2.jsonPath().getList(".");

        assertThat(beforeCoupons.size() + 1).isEqualTo(afterCoupons.size());
    }

    private Long createProduct(ProductRequest productRequest) {
        ExtractableResponse<Response> response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(productRequest)
                .when()
                .post("/products")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract();

        return getIdFromCreatedResponse(response);
    }

    private long getIdFromCreatedResponse(ExtractableResponse<Response> response) {
        return Long.parseLong(response.header("Location").split("/")[2]);
    }

    private ExtractableResponse<Response> requestAddCartItem(Member member, CartItemRequest cartItemRequest) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(member.getEmail(), member.getPassword())
                .body(cartItemRequest)
                .when()
                .post("/cart-items")
                .then()
                .log().all()
                .extract();
    }

}
