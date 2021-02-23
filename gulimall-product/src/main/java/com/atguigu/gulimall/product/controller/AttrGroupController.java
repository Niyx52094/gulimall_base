package com.atguigu.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrRelationDelete;
import com.atguigu.gulimall.product.vo.AttrgroupWithAttr;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 属性分组
 *
 * @author fishingfreedom
 * @email 601514291@qq.com
 * @date 2020-12-19 13:23:57
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;


    ///product/attrgroup/{catelogId}/withattr
    /**
     * 获取分类下所有分组&关联属性
     */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrgroupWithAttrs(@PathVariable("catelogId") Long catId){

        //1.查出当前分类下的所有属性分组
        //2.每个属性分组的所有属性
       List<AttrgroupWithAttr> data =attrGroupService.getAttrGroupWithAttr(catId);
        return R.ok().put("data",data);
    }
    ///product/attrgroup/attr/relation

    /**
     *
     * 选中后新增关联
     * @param vos
     * @return
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrRelationDelete> vos){
        relationService.saveBatch(vos);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
   // @RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrGroupService.queryPage(params);

        return R.ok().put("page", page);
    }
    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    // @RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId){
    //PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params,catelogId);

        return R.ok().put("page",page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
		Long[] path=categoryService.findFullPath(attrGroup.getCatelogId());
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 关联属性回显
     */
    @RequestMapping("/{attrgroupId}/attr/relation")
    //@RequiresPermissions("product:attrgroup:info")
    public R attrRelation(@PathVariable("attrgroupId") Long attrGroupId){
        List<AttrEntity> attrEntities = attrService.getRelationAttr(attrGroupId);
        return R.ok().put("data", attrEntities);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

   // /product/attrgroup/attr/relation/delete
    /**
     * 删除
     */
    @PostMapping("/attr/relation/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R deleteRelation(@RequestBody AttrRelationDelete[] attrRelationDelete){
        relationService.removeRelation(attrRelationDelete);
        return R.ok();
    }

///product/attrgroup/{attrgroupId}/noattr/relation
    /**
     * 还没有被关联的属性回显
     */
    @RequestMapping("/{attrgroupId}/noattr/relation")
    // @RequiresPermissions("product:attrgroup:list")
    public R nonAttrList(@RequestParam Map<String, Object> params,
                  @PathVariable("attrgroupId") Long attrgroupId){
        PageUtils page = attrService.queryNonAttrListPage(params,attrgroupId);
        return R.ok().put("page", page);
    }



}
