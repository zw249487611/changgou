package com.changgou.goods.feign;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 远程调用
 */
@FeignClient(value = "goods")
@RequestMapping("/sku")
public interface SkuFeign {

    /**
     * 调用goodsl里的，查询SKu全部数据，，这边正常不能全部查询出来的，万一数据量太大呢
     * 但此处开发测试，就查全部吧
     */
    @GetMapping
    Result<List<Sku>> findAll();

    /***
     * 根据ID查询Sku数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Sku> findById(@PathVariable String id);


    /**
     * 商品信息递减
     * Map<Key,Value> :key，要递减的商品ID
     *                  value:要递减的数量
     */
    @GetMapping("/decr/count")
    public Result decrCount(@RequestParam Map<String, Integer> decrmap);
}
