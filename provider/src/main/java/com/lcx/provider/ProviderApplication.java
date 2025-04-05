package com.lcx.provider;

import com.lcx.common.service.IUserService;
import com.lcx.provider.service.ipml.UserServiceImpl;
import com.lcx.rpc.bootstrap.ProviderBootStrap;
import com.lcx.rpc.common.model.ServiceRegisterInfo;

import java.util.ArrayList;
import java.util.List;

public class ProviderApplication {

    public static void main(String[] args) {
        // 要注册的服务
        List<ServiceRegisterInfo> serviceRegisterInfoList = new ArrayList<>();

        ServiceRegisterInfo serviceRegisterInfo = ServiceRegisterInfo.builder()
                .serviceName(IUserService.class.getName())
                .implClass(UserServiceImpl.class)
                .canRetry(false)
                .build();
        serviceRegisterInfoList.add(serviceRegisterInfo);

        // 服务提供者初始化
        ProviderBootStrap.init(serviceRegisterInfoList);
    }

}
