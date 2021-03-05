package com.changgou.goods.controller;

import com.changgou.goods.entity.PageResult;
import com.changgou.goods.entity.Result;
import com.changgou.goods.entity.StatusCode;
import com.changgou.goods.goods.pojo.Brand;
import com.changgou.goods.service.BrandService;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 跨域:A域名访问B域名的数据
 *      域名或者请求端口或者协议不一致的时候，就跨域了
 *         www.itheima.com  html www.itcast.cn
 *         ……
 *
 */
@RestController
@RequestMapping(value = "/brand")
@CrossOrigin //跨域
public class BrandController {

    @Autowired
    private BrandService brandService;
    /**
     * 查询所有品牌
     */
    @GetMapping
    public Result<List<Brand>> findAll() {
        List<Brand> brandList = brandService.findAll();
        return new Result(true, StatusCode.OK,"查询成功",brandList) ;
    }

    /**
     * 根据id查询商品品牌
     */

    @GetMapping(value = "/{id}")
    public Result<Brand> findById(@PathVariable(value = "id") Integer id) {

        Brand brand = brandService.findById(id);
        return new Result<Brand>(true, StatusCode.OK, "根据id查询成功", brand);
    }

    /**
     * 增加品牌
     */
    @PostMapping
    public Result add(@RequestBody Brand brand){
        brandService.add(brand);
        return new Result(true,StatusCode.OK,"添加成功");
    }

    /**
     * 增加品牌
     */
    @PutMapping(value = "/{id}")
    public Result update(@PathVariable(value = "id") Integer id, @RequestBody Brand brand){
        brand.setId(id);

        brandService.update(brand);
        return new Result(true,StatusCode.OK,"修改成功");
    }

    /**
     * 删除品牌
     */
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable(value = "id") Integer id) {
        brandService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功哦");
    }

    /**
     * 多条件查询
     */
    @PostMapping(value = "/search" )
    public Result<List<Brand>> findList(@RequestBody Brand brand) {
        List<Brand> list = brandService.findList(brand);
        return new Result<List<Brand>>(true, StatusCode.OK, "根据条件查询成功", list);

    }

    /**
     * 分页查询
     */
    @GetMapping(value = "/search/{page}/{size}")
    public Result<Page<Brand>> findPage(@PathVariable("page") Integer page, @PathVariable("size") Integer size) {
        Page<Brand> brandPage = brandService.findPage(page, size);
        return new Result<Page<Brand>>(true, StatusCode.OK, "分页查询成功啦", brandPage);
    }

    /**
     * 分页+条件查询
     */
    @PostMapping(value = "/search/{page}/{size}")
    public Result<Page<Brand>> findPage(@RequestBody Brand brand,@PathVariable("page") Integer page, @PathVariable("size") Integer size) {
        Page<Brand> brandPage = brandService.findPage(brand,page, size);
        return new Result<Page<Brand>>(true, StatusCode.OK, "分页查询成功啦", brandPage);
    }

}
