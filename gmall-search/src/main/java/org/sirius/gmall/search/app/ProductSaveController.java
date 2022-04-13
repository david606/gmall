package org.sirius.gmall.search.app;

import lombok.extern.slf4j.Slf4j;
import org.sirius.common.es.SkuEsModel;
import org.sirius.common.exception.BizCodeEnum;
import org.sirius.common.utils.R;
import org.sirius.gmall.search.service.ProductSaveService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static org.sirius.common.exception.BizCodeEnum.*;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/10 下午8:08
 */
@RestController
@RequestMapping("/search/product")
@Slf4j
public class ProductSaveController {

    @Resource
    private ProductSaveService productSaveService;

    /**
     * 搜索服务保存商品搜索模型
     *
     * @param skuEsModels
     * @return
     */
    @PostMapping(value = "/save/esmodel")
    public R saveSpuEsModel(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean status = false;

        // 如果保存过程中有异常，返回前端异常码
        try {
            status = productSaveService.saveSpuEsModel(skuEsModels);
        } catch (IOException e) {
            R.error(PRODUCT_UP_EXCEPTION.getCode(), PRODUCT_UP_EXCEPTION.getMsg());
        }

        // 如果没有上架成功返回异常码
        if (Boolean.FALSE.equals(status)) {
            R.error(PRODUCT_UP_EXCEPTION.getCode(), PRODUCT_UP_EXCEPTION.getMsg());
        }

        return R.ok();
    }
}
