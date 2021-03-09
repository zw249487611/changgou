package com.changgou.goods.feign;

import com.changgou.goods.entity.Result;
import com.changgou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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
}
