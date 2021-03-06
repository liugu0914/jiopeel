package com.jiopeel.sys.event;

import com.jiopeel.core.base.Base;
import com.jiopeel.core.bean.TreeNode;
import com.jiopeel.core.config.annotation.FormParam;
import com.jiopeel.core.event.BaseEvent;
import com.jiopeel.core.util.BaseUtil;
import com.jiopeel.core.util.WebUtil;
import com.jiopeel.sys.bean.form.PermissionForm;
import com.jiopeel.sys.bean.result.PermissionResult;
import com.jiopeel.sys.constant.SysConstant;
import com.jiopeel.sys.logic.AppLogic;
import com.jiopeel.sys.logic.PermissionLogic;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author ：lyc
 * @description：权限Event层
 * @date ：2019/12/20 10:25
 */
@Controller
@RequestMapping("/admin/permission")
public class PermissionEvent extends BaseEvent {

    @Resource
    private PermissionLogic logic;

    @Resource
    private AppLogic appLogic;

    /**
     * @Description :获取查询主页面
     * @Param: query
     * @Return: Base
     * @auhor:lyc
     * @Date: 2020年07月12日22:31:13
     */
    @RequestMapping(value = "main", method = {RequestMethod.GET})
    public String main(Model model) {
        model.addAttribute("apps", appLogic.list());
        return "sys/permission/main";
    }


    /**
     * @Description :配置权限
     * @Param: model
     * @auhor: lyc
     * @Date: 2020年07月12日17:49:24
     */
    @RequestMapping(value = "data", method = {RequestMethod.POST})
    public String data(Model model) {
        Map<String, String> map = WebUtil.getParam2Map(request);
        model.addAttribute("appid", map.get("appid"));
        model.addAttribute("list", logic.list(map.get("appid"), SysConstant.PERMISSION_TYPE_SYS));
        return "sys/permission/data";
    }

    /**
     * @Description :配置权限
     * @Param: model
     * @auhor: lyc
     * @Date: 2020年07月12日17:49:24
     */
    @RequestMapping(value = "info", method = {RequestMethod.POST})
    public String info(Model model) {
        Map<String, String> map = WebUtil.getParam2Map(request);
        PermissionResult bean = logic.getInfo(map.get("id"));
        if (BaseUtil.empty(bean.getType()))
            bean.setType(map.get("type"));
        if (BaseUtil.empty(bean.getAppid()))
            bean.setAppid(map.get("appid"));
        if (BaseUtil.empty(bean.getMenuid()))
            bean.setMenuid(map.get("menuid"));
        model.addAttribute("bean", bean);
        return "sys/permission/info";
    }


    /**
     * @Description :配置菜单
     * @Param: model
     * @auhor: lyc
     * @Date: 2020年07月12日17:49:24
     */
    @RequestMapping(value = "selectMenu", method = {RequestMethod.POST})
    public String selectMenu(Model model) {
        Map<String, String> map = WebUtil.getParam2Map(request);
        PermissionResult bean = logic.getInfo(map.get("id"));
        if (BaseUtil.empty(bean.getType()))
            bean.setType(map.get("type"));
        if (BaseUtil.empty(bean.getAppid()))
            bean.setAppid(map.get("appid"));
        model.addAttribute("bean", bean);
        return "sys/permission/selectMenu";
    }

    /**
     * @Description :功能权限
     * @Param: model
     * @auhor: lyc
     * @Date: 2020年07月12日17:49:24
     */
    @RequestMapping(value = "fuc", method = {RequestMethod.POST})
    public String getFucPermission(Model model) {
        Map<String, String> map = WebUtil.getParam2Map(request);
        String id =map.get("id");
        List<PermissionResult> list = logic.getFucPermission(id);
        model.addAttribute("list", list);
        model.addAttribute("map", map);
        return "sys/permission/fuc";
    }

    /**
     * @Description :保存
     * @Param: form
     * @Return: Base
     * @auhor:lyc
     * @Date:2019/12/21 00:02
     */
    @ResponseBody
    @RequestMapping(value = "save", method = {RequestMethod.POST})
    public Base save(@ModelAttribute PermissionForm form) {
        return logic.save(form);
    }

    /**
     * @Description :删除
     * @Param: ids
     * @Return: Base
     * @auhor:lyc
     * @Date:2019/12/21 00:02
     */
    @ResponseBody
    @RequestMapping(value = "del", method = {RequestMethod.POST})
    public Base del(@RequestParam("id") String ids) {
        return logic.del(ids);
    }

    /**
     * @return
     * @Description :查询对于应用配置菜单
     * @Param: model
     * @auhor: lyc
     * @Date: 2020年07月12日17:49:24
     */
    @ResponseBody
    @RequestMapping(value = "configTree", method = {RequestMethod.POST})
    public Base configTree() {
        Map<String, String> param2Map = WebUtil.getParam2Map(request);
        return Base.suc(logic.configTree(param2Map));
    }

    /**
     * @return
     * @Description :查询菜单
     * @Param: model
     * @auhor: lyc
     * @Date: 2020年07月12日17:49:24
     */
    @ResponseBody
    @RequestMapping(value = "menuTree", method = {RequestMethod.POST})
    public Base menuTree() {
        Map<String, String> param2Map = WebUtil.getParam2Map(request);
        return Base.suc(logic.menuTree(param2Map));
    }


    /**
     * @Description :保存菜单
     * @Param: form
     * @Return: Base
     * @auhor:lyc
     * @Date:2019/12/21 00:02
     */
    @ResponseBody
    @RequestMapping(value = "saveMenu", method = {RequestMethod.POST})
    public Base saveMenu(@FormParam List<TreeNode> list) {
        Map<String, String> param2Map = WebUtil.getParam2Map(request);
        return Base.suc(logic.saveMenu(list,param2Map));
    }
}
