package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.AttrResVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author fishingfreedom
 * @email 601514291@qq.com
 * @date 2020-12-19 13:23:57
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params, Long attrgroupId,String type);

    void saveAttr(AttrVo attrVo);

    void removeDetail(List<Long> asList);


    AttrResVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrGroupId);

    PageUtils queryNonAttrListPage(Map<String, Object> params, Long attrgroupId);
}

