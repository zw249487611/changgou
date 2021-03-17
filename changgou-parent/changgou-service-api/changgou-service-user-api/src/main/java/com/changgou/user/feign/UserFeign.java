package com.changgou.user.feign;

import com.changgou.entity.Result;
import com.changgou.user.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "user")
@RequestMapping("/user")
public interface UserFeign {

    /**
     * 根据id查询用户信息
     * @param id
     * @return
     */
    @GetMapping({"/load/{id}"})
    public Result<User> findById(@PathVariable String id);

    /**
     * 添加用户积分
     */
    @GetMapping("/points/add")
    public Result addPoints(@RequestParam Integer points);
}
