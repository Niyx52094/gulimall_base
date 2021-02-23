package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class PurchaseFinishVo {
    private Long id;//采购单ID
    private List<PurchaseItemDoneVo> list;//采购项集合
}
