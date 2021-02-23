package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.AttrRelationDelete;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author fishingfreedom
 * @email 601514291@qq.com
 * @date 2020-12-19 13:23:57
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void removeRelation(AttrRelationDelete[] attrRelationDelete);

    void saveBatch(List<AttrRelationDelete> vos);
}

