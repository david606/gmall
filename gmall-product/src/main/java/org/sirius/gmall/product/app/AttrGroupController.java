package org.sirius.gmall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sirius.gmall.product.entity.AttrEntity;
import org.sirius.gmall.product.service.AttrAttrgroupRelationService;
import org.sirius.gmall.product.service.AttrService;
import org.sirius.gmall.product.service.CategoryService;
import org.sirius.gmall.product.vo.AttrGroupRelationVo;
import org.sirius.gmall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.sirius.gmall.product.entity.AttrGroupEntity;
import org.sirius.gmall.product.service.AttrGroupService;
import org.sirius.common.utils.PageUtils;
import org.sirius.common.utils.R;

import javax.annotation.Resource;


/**
 * 属性分组
 *
 * @author David
 * @email my_good_dream@126.com
 * @date 2022-03-30 15:00:46
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {

    @Resource
    private AttrGroupService attrGroupService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private AttrService attrService;

    @Resource
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @PostMapping(value = "/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> relationVos) {
        attrAttrgroupRelationService.saveBatch(relationVos);
        return R.ok();
    }

    /**
     * 获取分类下所有分组&关联属性
     * <pre>
     * 1. 找出当前分类下的所有分组
     * 2. 找出每个分组下的所有属性
     *
     * @param catelogId
     * @return
     */
    @GetMapping(value = "/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId) {
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data", vos);
    }

    /**
     * 获取属性分组有关联的其他属性
     *
     * @param attrgroupId
     * @return
     */
    @GetMapping(value = "/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> entities = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", entities);
    }

    /**
     * 获取属性分组没有关联的其他属性
     */
    @GetMapping(value = "/{attrgroupId}/noattr/relation")
    public R attrNoAttrRelation(@RequestParam Map<String, Object> params,
                                @PathVariable("attrgroupId") Long attrgroupId) {

        PageUtils page = attrService.getNoRelationAttr(params, attrgroupId);
        return R.ok().put("page", page);
    }

    /**
     * 根据分类返回属性分组分页列表
     *
     * @param params    查询关键字
     * @param catelogId 分类Id
     * @return 分页列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId) {
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 根据分组Id,获取分组信息（用于回显数据）
     * <pre>
     *
     * </pre>
     *
     * @param attrGroupId 分组Id
     * @return AttrGroupEntity
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        // 查找分组所属分类的完整的三级（父子孙）路径,在分组信息能正常回显所属分类
        Long[] path = categoryService.findCatalogPath(catelogId);
        attrGroup.setCatelogPath(path);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));
        return R.ok();
    }

    /**
     * 批量删除关联关系
     *
     * @param vos
     * @return
     */
    @PostMapping(value = "/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos) {
        attrService.deleteRelation(vos);
        return R.ok();
    }
}
