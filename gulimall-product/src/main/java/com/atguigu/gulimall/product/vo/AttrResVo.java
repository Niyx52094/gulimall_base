package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.AttrEntity;
import lombok.Data;

@Data
public class AttrResVo extends AttrVo {

    /**
     * 分类名字
     */
    private String catelogName;

    /**
     * 所属分组名字
     */
    private String groupName;

    /**
     * 所属分类完整id
     */
    private Long[] catelogPath;
}
