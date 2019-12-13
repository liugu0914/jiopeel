package com.jiopeel.event;

import com.jiopeel.bean.UserGrant;
import com.jiopeel.config.exception.ServerException;
import com.jiopeel.constant.OauthConstant;
import com.jiopeel.logic.OauthLogic;
import com.jiopeel.util.BaseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @Description :授权登录
 * @auhor:lyc
 * @Date:2019年12月12日17:39:11
 */
@Slf4j
@Controller
public class OauthEvent {

    @Resource
    private OauthLogic logic;

    /**
     * 不同授权类型不同登陆方式
     *
     * @param granttype 授权类型 github
     * @return String
     * @auhor:lyc
     * @Date:2019/12/12 22:08
     */
    @RequestMapping(value = {"/oauth"}, method = RequestMethod.GET)
    public String oauth(@RequestParam(value = "granttype", required = false) String granttype) {
        String url = "";
        if (BaseUtil.empty(granttype))
            granttype = "local";
        switch (granttype) {
            case "github":
                url = String.format(OauthConstant.GITHUB_URL, OauthConstant.GITHUB_CLIENT_ID, OauthConstant.EDIRECT_URI + "/" + granttype);
                break;
            case "local":
                url = String.format(OauthConstant.local_url, OauthConstant.local_client_id, OauthConstant.EDIRECT_URI + "/" + granttype);
                break;
            default:
                url="/";
                break;
        }
        try {
            url= URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "redirect:" + url;
    }


    /**
     * 授权回调地址
     *
     * @param request
     * @param granttype 授权类型
     * @return
     * @auhor:lyc
     * @Date:2019/12/12 22:08
     */
    @RequestMapping(value = {"/oauth/redirect/{granttype}"}, method = RequestMethod.GET)
    public String redirect(HttpServletRequest request, @PathVariable("granttype") String granttype) {
        UserGrant userGrant = logic.redirectType(request, granttype);
        return "redirect:/main" ;
    }

    /**
     * @Description :通过access_token获取第三方用户信息
     * @Param: request
     * @Param: access_token
     * @Return: void
     * @auhor:lyc
     * @Date:2019/12/12 21:44
     */
    @ResponseBody
    @RequestMapping(value = {"/oauth/getuser"}, method = RequestMethod.GET)
    public void getOauthUserInfo(HttpServletRequest request, @RequestParam("access_token") String access_token,
                                 @RequestParam("granttype") String granttype) {
        if (BaseUtil.empty(access_token))
            throw new ServerException("access_token不能为空");
        logic.addOauthUser(access_token, granttype);
    }

}