package com.jiopeel.logic;

import com.alibaba.fastjson.JSONObject;
import com.jiopeel.bean.OauthToken;
import com.jiopeel.bean.UserGrant;
import com.jiopeel.config.exception.ServerException;
import com.jiopeel.config.redis.RedisUtil;
import com.jiopeel.constant.OauthConstant;
import com.jiopeel.constant.OauthConstant;
import com.jiopeel.dao.UserDao;
import com.jiopeel.dao.UserGrantDao;
import com.jiopeel.util.BaseUtil;
import com.jiopeel.util.HttpTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OauthLogic {

    @Resource
    private UserGrantDao dao;


    @Resource
    private RedisUtil redisUtil;

    /**
     * @Description :保存第三方登陆信息
     * @Param: access_token
     * @Param: granttype 授权类型
     * @Return: userGrant
     * @auhor:lyc
     * @Date:2019/12/12 21:49
     */
    @Transactional(rollbackFor = {ServerException.class, Exception.class})
    public UserGrant addOauthUser(String access_token, String granttype) {
        if (BaseUtil.empty(access_token))
            throw new ServerException("access_token不能为空");
        if (BaseUtil.empty(granttype))
            throw new ServerException("granttype不能为空");
        UserGrant userGrant = null;
        switch (granttype) {
            case "github":
                //组装Url
                String user_url = String.format(OauthConstant.GITHUB_USER, access_token);
                //请求地址
                String result = HttpTool.get(user_url);
                userGrant = savebyGithub(result, access_token, granttype);
                break;
            default:
                break;
        }
        if (BaseUtil.empty(userGrant))
            throw new ServerException("信息有误，授权登陆失败！");
        UserGrant user_data = dao.queryOne("login.getuserGrant", userGrant);
        if (BaseUtil.empty(user_data)) {
            if (!dao.add("login.saveuserGrant", userGrant))
                throw new ServerException("信息有误，授权登陆失败！");
        }else {
            user_data.updTime();
            user_data.setImgurl(userGrant.getImgurl());
            user_data.setNickname(userGrant.getNickname());
            user_data.setPassword(userGrant.getPassword());
            if (!dao.upd("login.upduserGrant", user_data))
                throw new ServerException("信息有误，授权登陆失败！");
        }
        return userGrant;
    }

    /**
     * @Description :处理github第三方登陆信息
     * @Param: result
     * @Param: access_token
     * @Param: granttype
     * @Return: UserGrant
     * @auhor:lyc
     * @Date:2019/12/12 21:49
     */
    private UserGrant savebyGithub(String result, String access_token, String granttype) {
        JSONObject obj = (JSONObject) JSONObject.parse(result);
        UserGrant userGrant = UserGrant.builder()
                .granttype(granttype)
                .imgurl(obj.getString("avatar_url"))
                .onlyid(obj.getString("id"))
                .nickname(obj.getString("login"))
                .password(access_token)
                .build();
        userGrant.createUUID();
        userGrant.createTime();
        return userGrant;
    }

    /**
     * @Description :处理第三方登陆信息
     * @Param: request
     * @Param: granttype 授权类型
     * @Return: UserGrant
     * @auhor:lyc
     * @Date:2019/12/12 21:49
     */
    public String redirectType(HttpServletRequest request, String granttype) {
        if (BaseUtil.empty(granttype))
            throw new ServerException("授权类型不能为空");
        Map<String, String[]> parameterMap = request.getParameterMap();
        String access_token = null;
        switch (granttype) {
            case "github":
                access_token = getTokenbyGithub(parameterMap);
                addOauthUser(access_token, granttype);
                break;
            case "local":
                access_token = getTokenbyLocal(parameterMap);
                break;
            default:
                break;
        }
        return access_token;
    }

    /**
     * @Description :处理本地登陆信息
     * @Param: parameterMap
     * @Return: String 返回access_token
     * @auhor:lyc
     * @Date:2019/12/12 21:49
     */
    private String getTokenbyLocal(Map<String, String[]> parameterMap) {
        String access_token = null;
        if (parameterMap.containsKey(OauthConstant.CODE)) {
            String code = parameterMap.get(OauthConstant.CODE)[0];
            Map<String, Object> params=new HashMap<String, Object>();
            params.put(OauthConstant.CLIENT_ID,OauthConstant.local_client_id);
            params.put(OauthConstant.CLIENT_SECRET,OauthConstant.local_client_secret);
            params.put(OauthConstant.CODE,code);
            String res = HttpTool.post(OauthConstant.local_token,params);
            JSONObject parse = (JSONObject) JSONObject.parse(res);
            access_token = parse.getString(OauthConstant.ACCESS_TOKEN);
        }
        return access_token;
    }

    /**
     * @Description :处理github登陆信息
     * @Param: parameterMap
     * @Return: String 返回access_token
     * @auhor:lyc
     * @Date:2019/12/12 21:49
     */
    private String getTokenbyGithub(Map<String, String[]> parameterMap) {
        String access_token = null;
        if (parameterMap.containsKey(OauthConstant.CODE)) {
            String code = parameterMap.get(OauthConstant.CODE)[0];
            Map<String, Object> params=new HashMap<String, Object>();
            params.put(OauthConstant.CLIENT_ID,OauthConstant.GITHUB_CLIENT_ID);
            params.put(OauthConstant.CLIENT_SECRET,OauthConstant.GITHUB_CLIENT_SECRET);
            params.put(OauthConstant.CODE,code);
            String res = HttpTool.post(OauthConstant.GITHUB_TOKEN,params);
            res = BaseUtil.Url2JSON(res);
            JSONObject parse = (JSONObject) JSONObject.parse(res);
            access_token = parse.getString(OauthConstant.ACCESS_TOKEN);
        }
        return access_token;
    }


    /**
     * @Description :验证授权码
     * @Param: parameterMap
     * @Return: OauthToken
     * @auhor:lyc
     * @Date:2019/12/12 21:49
     */
    public OauthToken chkLocalOauth(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (!parameterMap.containsKey(OauthConstant.CODE))
            throw new ServerException("授权码不能为空");
        if (!parameterMap.containsKey(OauthConstant.CLIENT_ID))
            throw new ServerException(OauthConstant.CLIENT_ID+"不能为空");
        if (!parameterMap.containsKey(OauthConstant.CLIENT_SECRET))
            throw new ServerException(OauthConstant.CLIENT_SECRET+"不能为空");
        String code =parameterMap.get(OauthConstant.CODE)[0];
        String client_id=parameterMap.get(OauthConstant.CLIENT_ID)[0];
        String client_secret=parameterMap.get(OauthConstant.CLIENT_SECRET)[0];
        if (!OauthConstant.local_client_id.equals(client_id))
            throw new ServerException(OauthConstant.CLIENT_ID+"不匹配");
        if (!OauthConstant.local_client_secret.equals(client_secret))
            throw new ServerException(OauthConstant.CLIENT_SECRET+"不匹配");
        if (!redisUtil.hasKey(code))
            throw new ServerException("授权码已失效");
        return (OauthToken)redisUtil.get(code);
    }
}
