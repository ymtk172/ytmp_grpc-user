package com.yamalc.ytmp.grpc.client;

import com.yamalc.ytmp.grpc.user.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

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

    public RegisterUserInfoResponse registerUserInfoResponse(String id, String displayName) {
        UserInfoRequest request =
                UserInfoRequest
                        .newBuilder()
                        .setId(id)
                        .setDisplayName(displayName)
                        .build();

        try {
            RegisterUserInfoResponse response = blockingStub.registerUserInfo(request);
            logger.info(String.format("response: result = %b", response.getResultCode()));
            return response;
        } catch (StatusRuntimeException e) {
            Status status = Status.fromThrowable(e);
            logger.info("error: status code = " + status.getCode() + ", description = " + status.getDescription());
            e.printStackTrace();
        }
        return null;
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5L, TimeUnit.SECONDS);
    }
}
