package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.WareConstant;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.vo.PurchaseFinishVo;
import com.atguigu.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnrecievedPurchase(final Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        //每一个采购单都有一个status,是0或者是1就是未被领取（新建的和已分配的）。
        queryWrapper.eq("status",0).or().eq("status",1);

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchaseRequest(MergeVo vo) {
        Long purchaseId = vo.getPurchaseId();//看这个采购单是否存在，没有则需要新建
        if(purchaseId==null){//需要新建
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            //获得其id
            purchaseId = purchaseEntity.getId();
        }
        PurchaseEntity purchaseEntity2 = this.getById(purchaseId);
        //确保采购单是新建和分配状态，否则不能成功合并。
        if(purchaseEntity2.getStatus()==WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()
                ||purchaseEntity2.getStatus()==WareConstant.PurchaseStatusEnum.CREATED.getCode()){
            //采购单ID不为空
            //生成采购需求的entitylist
            Long finalpurchaseId=purchaseId;
            List<Long> items = vo.getItems();
            List<PurchaseDetailEntity> detailEntities = items.stream().map(item -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                detailEntity.setId(item);
                detailEntity.setPurchaseId(finalpurchaseId);
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
                return detailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(detailEntities);

            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(purchaseId);
            purchaseEntity.setUpdateTime(new Date());
            this.updateById(purchaseEntity);
        }

    }

    @Override
    public void received(List<Long> ids) {

        //1.确认当前采购单是新建还是已分配状态
        List<PurchaseEntity> purchaseEntities = ids.stream().map(id -> {
            PurchaseEntity purchaseEntity = this.getById(id);

            return purchaseEntity;
        }).filter(item -> {
            //过滤采购单，只有已分配和新建的才能被领取
            return item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()
                    || item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode();
        }).map(item->{
            //分配一个最新状态
            item.setUpdateTime(new Date());
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            return item;
        }).collect(Collectors.toList());

        //2.改变采购单状态
        this.updateBatchById(purchaseEntities);

        //3.改变采购单需求的状态
        purchaseEntities.forEach((item)->{
            //使用另一张表的service获得detailedentity的集合
            List<PurchaseDetailEntity> purchaseDetailEntityList=purchaseDetailService.listDetailByPurchaseId(item);

            //获得更新状态后的集合
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailEntityList.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.Buying.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            //使用另一张表的service进行更新操作。
            purchaseDetailService.updateBatchById(purchaseDetailEntities);
        });

    }

    @Override
    public void done(final PurchaseFinishVo purchaseFinishVo) {
        //1.改变采购项的状态
        Boolean flag=true;//检查采购项是否都是已完成采购。如果不是则返回未false

        Long finishVoId = purchaseFinishVo.getId();
        List<PurchaseItemDoneVo> purchaseFinishVoList = purchaseFinishVo.getList();//获得采购项list

        List<PurchaseDetailEntity> detailEntities=new ArrayList<>();
        for (PurchaseItemDoneVo item:purchaseFinishVoList){
            //更新采购项状态
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(item.getItemId());
            if(item.getStatus()==WareConstant.PurchaseDetailStatusEnum.Failed.getCode()){
                flag=false;
                detailEntity.setStatus(item.getStatus());
            }else{
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //3.将成功采购的进行入库WareSkuServie
                PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(item.getItemId());

                wareSkuService.addStock(purchaseDetailEntity.getSkuId(),purchaseDetailEntity.getWareId()
                ,purchaseDetailEntity.getSkuNum());

            }
            detailEntities.add(detailEntity);

        };
        //执行更新操作
        purchaseDetailService.updateBatchById(detailEntities);

        //2.改变采购单状态(当所有的采购项都是已完成时采购单才能进行修改）
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(finishVoId);
            if(flag){
                purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.FINISH.getCode());

            }else{
                purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.HASERROR.getCode());
            }
            purchaseEntity.setUpdateTime(new Date());
            //更新采购单
            this.updateById(purchaseEntity);

    }

}