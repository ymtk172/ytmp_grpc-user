package com.yamalc.ytmp.grpc.client;

import com.yamalc.ytmp.grpc.user.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class UserApiClient {
    Logger logger = Logger.getLogger(getClass().getName());

    ManagedChannel channel;
    UserGrpc.UserBlockingStub blockingStub;

    public static UserApiClient create(String host, int port) {
        UserApiClient simpleClient = new UserApiClient();
        simpleClient.channel =
                ManagedChannelBuilder
                        .forAddress(host, port)
                        .usePlaintext()
                        .build();
        simpleClient.blockingStub =
                UserGrpc.newBlockingStub(simpleClient.channel);

        return simpleClient;
    }

    public UserInfoResponse getUserInfo(String userId) {
        UserIdRequest request =
                UserIdRequest
                        .newBuilder()
                        .setId(userId)
                        .build();

        try {
            UserInfoResponse response = blockingStub.getUserInfo(request);
            logger.info(String.format("response: result = %b %b", response.getId(), response.getDisplayName()));
            return response;
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);
            logger.info("error: status code = " + status.getCode() + ", description = " + status.getDescription());
            e.printStackTrace();
        }
        return null;
    }

    public RegisterUserInfoResponse registerUserInfoResponse(UserInfoRequest userInfoRequest) {
        List<UserInfo> userInfoList = userInfoRequest.getUserInfoList();
        UserInfoRequest.Builder request =
                UserInfoRequest
                        .newBuilder();
        userInfoList.forEach(request::addUserInfo);
        try {
            RegisterUserInfoResponse response = blockingStub.registerUserInfo(request.build());
            logger.info(String.format("response:targets = %s , result = %s", userInfoList.toString(), response.getResult().toString()));
            return response;
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);
            logger.info("error: status code = " + status.getCode() + ", description = " + status.getDescription());
            throw e;
        }
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5L, TimeUnit.SECONDS);
    }
}
