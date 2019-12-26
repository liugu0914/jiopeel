package com.jiopeel.core.logic;


import com.alibaba.fastjson.JSON;
import com.jiopeel.core.base.Base;
import com.jiopeel.core.bean.*;
import com.jiopeel.core.config.exception.ServerException;
import com.jiopeel.core.config.interceptor.PageIntercept;
import com.jiopeel.core.constant.Constant;
import com.jiopeel.core.constant.OauthConstant;
import com.jiopeel.core.constant.UserConstant;
import com.jiopeel.core.dao.UserDao;
import com.jiopeel.core.util.BaseUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 登陆信息处理
 */
@Service
public class LoginLogic extends BaseLogic {

    @Resource
    private UserDao dao;
    @Resource
    private OauthLogic oauthLogic;


    /**
     * 注册操作
     *
     * @return
     */
    @Transactional
    public Base addregister(User user) {
        if (BaseUtil.empty(user))
            throw new ServerException("用户对象不能为空");
        if (BaseUtil.empty(user.getAccount()))
            throw new ServerException("账号不能为空");
        if (BaseUtil.empty(user.getPassword()))
            throw new ServerException("密码不能为空");
        if (BaseUtil.empty(user.getEmail()))
            throw new ServerException("邮箱不能为空");
        int o = dao.queryOne("login.checkAccount", user);
        if (o > 0)
            throw new ServerException("该账号已存在");
        user.createUUID();
        user.createTime();
        user.setType(UserConstant.USER_TYPE_LOCAL);
        user.setEnable(Constant.ENABLE_YES);
        user.setUsername(user.getAccount());
        user.setPassword(BaseUtil.MD5(user.getPassword()));
        boolean s = dao.add("login.saveUser", user);
        return s ? Base.suc("注册成功") : Base.fail("注册失败");
    }

    /**
     * 登陆操作操作
     * x
     *
     * @param tmpUser   页面登陆数据
     * @param request
     * @param response
     * @param client_id
     * @return code
     * @Author lyc
     * @Date:2019/12/12 23:42
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public String dologin(User tmpUser, HttpServletRequest request, HttpServletResponse response,
                          String client_id) {
        if (!OauthConstant.local_client_id.equals(client_id))
            throw new ServerException("client_id无法识别");
        String password = tmpUser.getPassword();
        if (BaseUtil.empty(tmpUser))
            throw new ServerException("用户对象不能为空");
        if (BaseUtil.empty(tmpUser.getAccount()))
            throw new ServerException("账号不能为空");
        if (BaseUtil.empty(password))
            throw new ServerException("密码不能为空");
        User user = dao.queryOne("login.getUserbyaccount", tmpUser);
        if (user == null)
            throw new ServerException("该账号不存在");
        password = BaseUtil.MD5(password);
        if (!user.getPassword().equals(password))
            throw new ServerException("密码不正确");
        if (!Constant.ENABLE_YES.equals(user.getEnable()))
            throw new ServerException("该账号已被禁用");
        oauthLogic.BoxuserAgent(user.getId(), request);
        //code
        String code = BaseUtil.getUUID();
        OauthToken oauthToken = oauthLogic.RedisCode(user, code);
        oauthLogic.AddTokenCookie(response, oauthToken);
        return code;
    }


    /**
     * 退出登陆
     *
     * @param request
     * @Author lyc
     * @Date:2019/12/12 23:42
     */
    public void loginOut(HttpServletRequest request) {
        oauthLogic.loginOut(request);
    }


    public void dosomething(String userId) {
//        List<UserGrant> lists = new ArrayList<UserGrant>();
//        for (int i = 0; i < 1; i++) {
            UserGrant bean = new UserGrant();
            bean.createTime();
            bean.createUUID();
            bean.setUserid("e017cc0c0b9c47e6aa8accf9a938c467");
            bean.setGranttype("github12212");
            dao.upd(bean,"userid","id","ctime");
//            lists.add(bean);
//        }
//        dao.addBatch(lists);
        Page<User> userPage = dao.queryPageList("login.getUser", userId, new Page<User>());
        System.out.println(JSON.toJSONString(userPage));
    }
}
