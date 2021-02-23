package com.atguigu.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import com.atguigu.gulimall.product.vo.AttrResVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 商品属性
 *
 * @author fishingfreedom
 * @email 601514291@qq.com
 * @date 2020-12-19 13:23:57
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    ProductAttrValueService productAttrValueService;

    ///product/attr/base/listforspu/{spuId}
    /**
     * 查询出spuarrt
     */
    @RequestMapping("/base/listforspu/{spuId}")
    // @RequiresPermissions("product:attr:list")
    public R baseArrtlistForSpu(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> data = productAttrValueService.baseAttrListspu(spuId);

        return R.ok().put("data", data);
    }

    ////product/attr/update/{spuId}
    /**
     * 修改attr参数
     */
    @PostMapping("/update/{spuId}")
    public R updateAttrForSpu(@PathVariable("spuId") Long spuId,
                              @RequestBody List<ProductAttrValueEntity> entities){
        productAttrValueService.updateAttrListspu(spuId,entities);

        return R.ok();
    }


    /**
     * 列表
     */
    @RequestMapping("/{attrType}/list/{categoryId}")
   // @RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("attrType") String type,
                  @PathVariable("categoryId") Long categoryId){
        PageUtils page = attrService.queryPage(params,categoryId,type);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		AttrResVo attrResVo = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", attrResVo);
    }

    /**
     * 保存
     * @param attrVo
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attrVo){
		attrService.saveAttr(attrVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeDetail(Arrays.asList(attrIds));

        return R.ok();
    }

}
