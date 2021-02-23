package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseFinishVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author fishingfreedom
 * @email 601514291@qq.com
 * @date 2020-12-21 20:40:50
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnrecievedPurchase(Map<String, Object> params);

    void mergePurchaseRequest(MergeVo vo);

    void received(List<Long> ids);

    void done(PurchaseFinishVo purchaseFinishVo);
}

