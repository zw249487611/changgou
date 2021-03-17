package com.changgou.order.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.entity.TokenDecode;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {


    @Autowired
    private CartService cartService;

    /**
     * 加入购物车
     * 1.加入购物车数据
     * 2、商品ID
     */
    @GetMapping("/add")
    public Result add(Integer num, String id) {
        //先把username写成死的，，未来再换

         /*OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
        String token = details.getTokenValue();*/
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        System.out.println(userInfo + "userinfo的信息");
        String username = userInfo.get("username");
        username = "itheima";
        cartService.add(num, id, username);

        return new Result(true, StatusCode.OK, "加入购物城成功");
    }

    /**
     * 购物车列表
     */
    @GetMapping("/list")
    public Result<List<OrderItem>> list() {
        //用户的令牌信息->解析令牌信息->username
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        System.out.println(userInfo + "userinfo的信息");
        String username = userInfo.get("username");
        //获取用户登录名
        List<OrderItem> orderItems = cartService.list(username);

        return new Result<List<OrderItem>>(true, StatusCode.OK, "查询购物车列表成功", orderItems);
    }
}
