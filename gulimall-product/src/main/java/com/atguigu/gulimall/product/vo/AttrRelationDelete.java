package com.atguigu.gulimall.product.vo;

import lombok.Data;

@Data
public class AttrRelationDelete {
    //    [{"attrId":1,"attrGroupId":2}]
    private Long attrId;
    private Long attrGroupId;
}
