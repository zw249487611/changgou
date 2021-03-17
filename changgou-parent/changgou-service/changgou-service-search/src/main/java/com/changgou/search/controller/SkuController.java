package com.changgou.search.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.search.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;
    /**
     * 导入数据
     */
    @GetMapping("/import")
    public Result importData() {
        skuService.importData();
        return new Result(true, StatusCode.OK, "导入数据OK");
    }

    /**
     * 调用搜索实现
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map<String,String> searchMap) {
        return skuService.search(searchMap);
    }

}
