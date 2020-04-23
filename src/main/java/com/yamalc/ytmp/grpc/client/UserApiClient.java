package com.yamalc.ytmp.grpc.client;

import com.yamalc.ytmp.grpc.user.AuthenticateRequest;
import com.yamalc.ytmp.grpc.user.AuthenticateResponse;
import com.yamalc.ytmp.grpc.user.AuthenticateResponseType;
import com.yamalc.ytmp.grpc.user.UserGrpc;
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

    public AuthenticateResponseType authenticate(String id, String password) {
        AuthenticateRequest request =
                AuthenticateRequest
                        .newBuilder()
                        .setId(id)
                        .setPassword(password)
                        .build();

        try {
            AuthenticateResponse response = blockingStub.authenticate(request);
            logger.info(String.format("response: result = %b", response.getAuthenticateResult()));
            return response.getAuthenticateResult();
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
