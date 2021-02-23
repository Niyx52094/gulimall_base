package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {


        /**
         *   wareId: 123,//仓库id
         *    skuId: 123//商品id
         *    返回这俩的交集获得的商品
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String wareId =(String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId)){
            queryWrapper.eq("ware_id",wareId);
        }
        String skuId =(String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId)){
            queryWrapper.eq("sku_id",skuId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(final Long skuId, final Long wareId, final Integer skuNum) {
        //1。判断如果还没有库存记录，则是新增，否则时更新
        List<WareSkuEntity> wareSkuEntities = this.baseMapper.selectList(
                new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId)
        );
        if(wareSkuEntities==null){
            //如果没有这个实体类，就进行直接更新
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            //获取远程查询sku的名字,使用try，则如果失败事务不需要回滚。
            try{
                R info = productFeignService.info(skuId);
                Map<String,Object> data =(Map) info.get("skuInfo");
                if(info.getCode()==0){
                    //说明查询成功
                    wareSkuEntity.setSkuName((String)data.get("skuName"));
                }
            }catch(Exception e){

            }


            this.baseMapper.insert(wareSkuEntity);
        }else{
            //如果已有，则进行更新操作。
            this.baseMapper.addStock(skuId,wareId,skuNum);
        }

    }

}