package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.atguigu.gulimall.product.vo.SpuSaveVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.service.SkuInfoService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        /**
         * key:
         * catelogId: 0
         * brandId: 0
         * min: 0
         * max: 0
         */
        //key
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)) {
            wrapper.and((w)->{
                w.eq("spu_id",key).or().like("sku_name",key);
            });
        }
        //catelogId
        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId)&&!catelogId.equals("0")) {
            wrapper.eq("catalog_id",catelogId);

        }
        //brandId
        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId)&&!brandId.equals("0")) {
            wrapper.eq("brand_id",brandId);
        }
        //min
        String min = (String) params.get("min");
        if(!StringUtils.isEmpty(min)&&!min.equals("0")) {
            wrapper.ge("price",min);//ge shi  greater than and equal to
        }
        //max
        String max = (String) params.get("max");
        if(!StringUtils.isEmpty(max)&&!max.equals("0")) {
            wrapper.le("price",max);//le shi  less than and equal to
        }


        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }


}