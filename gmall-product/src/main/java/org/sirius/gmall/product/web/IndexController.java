package org.sirius.gmall.product.web;

import org.sirius.gmall.product.entity.CategoryEntity;
import org.sirius.gmall.product.service.CategoryService;
import org.sirius.gmall.product.vo.Catelog2Vo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author david
 * @email my_good_dream@126.com
 * @date 2022/4/4 下午12:01
 */
@Controller
public class IndexController {

    @Resource
    private CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {

        System.out.println("" + Thread.currentThread().getId());
        // 查出所有的1级分类
        List<CategoryEntity> categories= categoryService.getLevel1Category();

        // 视图解析器进行拼串：
        // classpath:/templates/ +返回值+  .html
        model.addAttribute("categories", categories);
        return "index";
    }


    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        Map<String, List<Catelog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }
}
