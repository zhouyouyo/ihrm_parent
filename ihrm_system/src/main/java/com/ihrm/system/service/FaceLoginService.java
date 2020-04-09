package com.ihrm.system.service;


import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.system.User;
import com.ihrm.domain.system.response.FaceLoginResult;
import com.ihrm.domain.system.response.QRCode;
import com.ihrm.system.dao.UserDao;
import com.ihrm.system.utils.BaiduAiUtil;
import com.ihrm.system.utils.QRCodeUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@Service
public class FaceLoginService {

    @Value("${qr.url}")
    private String url;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private QRCodeUtil qrCodeUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private BaiduAiUtil baiduAiUtil;

    @Autowired
    private UserDao userDao;

	//创建二维码
    public QRCode getQRCode() throws Exception {
        //构建二维码唯一标识
        String code = idWorker.nextId()+"";
        //生成二维码
        String content = url+"?code="+code;
        String file = qrCodeUtil.crateQRCode(content);
        System.out.println("file = " + file);
        //将初始化的二维码状态存入到redis中
        FaceLoginResult result = new FaceLoginResult("-1");//标识未使用状态
        redisTemplate.boundValueOps(this.getCacheKey(code)).set(result,30, TimeUnit.SECONDS);//存储对象，过期时间，过期时间单位
        return new QRCode(code,file);
    }

	//根据唯一标识，查询用户是否登录成功
    public FaceLoginResult checkQRCode(String code) {
        FaceLoginResult result = (FaceLoginResult) redisTemplate.opsForValue().get(this.getCacheKey(code));
        return result;
    }

	//扫描二维码之后，使用拍摄照片进行登录,登录成功之后返回该用户id
    public String loginByFace(String code, MultipartFile attachment) throws Exception {
        String userId = baiduAiUtil.faceSearch(Base64Utils.encodeToString(attachment.getBytes()));
        FaceLoginResult result = new FaceLoginResult("0");//登录失败
        if (userId!=null){
            //自动登录
            User user = userDao.findById(userId).get();
            Subject subject = SecurityUtils.getSubject();
            subject.login(new UsernamePasswordToken(user.getMobile(),user.getPassword()));
            //获取sessionId
            String token = (String) subject.getSession().getId();
            result = new FaceLoginResult("1",token,userId);//登录成功
        }
        //更新redis中二维码状态
        redisTemplate.boundValueOps(this.getCacheKey(code)).set(result,30,TimeUnit.SECONDS);
        return userId;
    }

	//构造缓存key
    private String getCacheKey(String code) {
        return "qrcode_" + code;
    }
}
