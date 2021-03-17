package com.changgou.user.controller;
import com.alibaba.fastjson.JSON;
import com.changgou.entity.*;
import com.changgou.user.pojo.User;
import com.changgou.user.service.UserService;
import com.changgou.util.JwtUtil;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserService userService;

    /**
     * 查询全部数据
     * 只允许管理员admin角色访问，其他的角色无法访问
     * @return
     */
    @PreAuthorize("hasAnyRole('user','accountant','salesman')")
    @GetMapping
    public Result findAll(){
        List<User> userList = userService.findAll();
        return new Result(true, StatusCode.OK,"查询成功",userList) ;
    }

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping({"/{id}","/load/{id}"})
    public Result<User> findById(@PathVariable String id){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = userService.findById(id);
        return new Result(true,StatusCode.OK,"查询成功",user);
    }

    /*@GetMapping("/load/{username}")
    public User findUserInfo(@PathVariable("username") String username){
        User user = userService.findById(username);
        return user;
    }*/



    /***
     * 新增数据
     * @param user
     * @return
     */
    @PostMapping
    public Result add(@RequestBody User user){
        userService.add(user);
        return new Result(true,StatusCode.OK,"添加成功");
    }


    /***
     * 修改数据
     * @param user
     * @param username
     * @return
     */
    @PutMapping(value="/{username}")
    public Result update(@RequestBody User user,@PathVariable String username){
        user.setUsername(username);
        userService.update(user);
        return new Result(true,StatusCode.OK,"修改成功");
    }


    /***
     * 根据ID删除品牌数据
     * @param username
     * @return
     */
    @DeleteMapping(value = "/{username}" )
    public Result delete(@PathVariable String username){
        userService.delete(username);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findList(@RequestParam Map searchMap){
        List<User> list = userService.findList(searchMap);
        return new Result(true,StatusCode.OK,"查询成功",list);
    }


    /***
     * 分页搜索实现
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result findPage(@RequestParam Map searchMap, @PathVariable  int page, @PathVariable  int size){
        Page<User> pageList = userService.findPage(searchMap, page, size);
        PageResult pageResult=new PageResult(pageList.getTotal(),pageList.getResult());
        return new Result(true,StatusCode.OK,"查询成功",pageResult);
    }


    /**
     * 用户登录
     */
    @GetMapping("/login")
    public Result login(String username, String password, HttpServletResponse response) {
        //查询数据库中用户的信息
        User user = userService.findById(username);

        //对比密码
        //要用密文来比对哦，这边用工具类就好
        if (BCrypt.checkpw(password,user.getPassword())) {
            //创建用户令牌信息
            Map<String, Object> tokenMap = new HashMap<String, Object>();
            tokenMap.put("roke", "USER");
            tokenMap.put("success", "SUCCESS");
            tokenMap.put("username", username);
            String token = JwtUtil.createJWT(UUID.randomUUID().toString(), JSON.toJSONString(tokenMap), null);

            //吧令牌信息存入cookie
            Cookie cookie = new Cookie("Authorization", token);
            cookie.setDomain("localhost");
            cookie.setPath("/");
            response.addCookie(cookie);

            //把令牌作为参数给用户


            //成功，
            return new Result(true,StatusCode.OK,"登录成功",token);
        }
            //不匹配
        return new Result(false,StatusCode.LOGINERROR,"账号或者密码不匹配");
    }

    /**
     * 添加用户积分
     */
    @GetMapping("/points/add")
    public Result addPoints(Integer points) {
        //获取用户名
        String username = TokenDecode.getUserInfo().get("username");
        //调用service增加积分
        userService.addPoints(username, points);
        return new Result(true, StatusCode.OK, "添加积分成功");
    }
}
