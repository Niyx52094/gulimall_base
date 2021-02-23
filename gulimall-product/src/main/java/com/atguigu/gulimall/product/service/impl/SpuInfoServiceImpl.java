package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }
// TODO 高级部分，完善回滚录入等功能
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1.保存spu基本信息（pms_spu_info,三张表里面）
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);
        //2.保存Spu的描述图片；pms_spu_info_desc,
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(infoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveBaseSpuInfoDesc(spuInfoDescEntity);
        //3.保存spu的图片集；pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(infoEntity.getId(),images);

        //4.保存spu的规格参数；（pms_product_attr_value这张表）
        List<BaseAttrs> voBaseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = voBaseAttrs.stream().map((attr) -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(attr.getAttrId());
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            productAttrValueEntity.setAttrName(attrEntity.getAttrName());
            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            productAttrValueEntity.setSpuId(infoEntity.getId());

            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(productAttrValueEntities);
        //5 保存spu的积分信息（跨库到sms，sms_spu_bounds）
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());

        R r = couponFeignService.saveSpuBounds(spuBoundTo);

        if(r.getCode()!=0){
            log.error("远程保存spu积分信息失败");
        }

        //6.保存当前spu对应的所有sku信息。
        List<Skus> skus = vo.getSkus();
        if(skus!=null&&skus.size()>0){
//            private String skuName;
//            private BigDecimal price;
//            private String skuTitle;
//            private String skuSubtitle;
            skus.forEach(item->{
                String defaultImg="";
                for(Images image : item.getImages()){
                    if(image.getDefaultImg()==1){
                        defaultImg=image.getImgUrl();
                    }
                }

                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);

                //6.1 保存sku的基本信息 (pms_sku_info)
                skuInfoService.saveSkuInfo(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();

                //6.2 保存sku的图片（pms_sku_images）

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    return skuImagesEntity;
                }).filter(entity->{
                    //返回true就是需要，返回false就是剔除
                    return StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveSkuImages(imagesEntities);
                //TODO 没有图片，路径无需保存

                //6.3 保存sku的销售属性信息：（pms_sku_sale_attr_value）

                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(attritem -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attritem,skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveSkuSaleAttrValueEntities(skuSaleAttrValueEntities);


                //6.4 sku的优惠满减信息(跨库到sms,sms_sku_ladder,sms_sku_full_reduction sms_member_price)
                SkuReductionTo skuReductionTo = new SkuReductionTo();

                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);

                if(skuReductionTo.getFullCount()>0
                        || (skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")))>0){
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if(r1.getCode()!=0){
                        log.error("远程保存sku积分信息失败");
                    }
                }

            });
        }








    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }

    @Override
    public PageUtils queryPageByCondition( Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        /**
         * status
         * key:
         * brandId: 0
         * catelogId: 0
         */
        String key = (String)params.get("key");
        if(!StringUtils.isEmpty(key)){
            queryWrapper.and(w->{
                w.eq("id",key).or().like("spu_name",key).or().like("spu_description",key);
            });
        }
        //status
        String status = (String)params.get("status");
        if(!StringUtils.isEmpty(status)){
            queryWrapper.eq("publish_status",status);
        }
        //brandId
        String brandId = (String)params.get("brandId");
        if(!StringUtils.isEmpty(brandId)&&!brandId.equals("0")){
            queryWrapper.eq("brand_id",brandId);
        }
        //catelogId
        String catelogId = (String)params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId)&&!catelogId.equals("0")){
            queryWrapper.eq("catalog_id",catelogId);
        }


        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }


}