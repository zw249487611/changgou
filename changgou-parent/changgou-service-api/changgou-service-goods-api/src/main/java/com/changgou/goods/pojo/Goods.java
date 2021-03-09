package com.changgou.goods.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 商品信息组合对象
 * List<Sku>
 *     spu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Goods implements Serializable {

    //Spu信息
    private Spu spu;

    //Sku信息
    private List<Sku> skuList;
}
