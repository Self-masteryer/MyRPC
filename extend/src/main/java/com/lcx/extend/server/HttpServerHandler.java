package com.lcx.extend.server;

import com.lcx.extend.model.RpcRequest;
import com.lcx.extend.model.RpcResponse;
import com.lcx.extend.register.LocalRegister;
import com.lcx.extend.serializer.JdkSerializer;
import com.lcx.extend.server.ipml.VertxHttpServerImpl;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class HttpServerHandler  implements Handler<HttpServerRequest> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(VertxHttpServerImpl.class.getName());

    @Override
    public void handle(HttpServerRequest request) {
        final JdkSerializer serializer = new JdkSerializer();
        log.info("Receive request:{} {}", request.method(), request.uri());
        request.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest;
            try {
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            RpcResponse rpcResponse = new RpcResponse();
            if (rpcRequest == null) {
                rpcResponse.setMessage("Request is null");
                doResponse(request, rpcResponse, serializer);
                return;
            }
            try {
                Class<?> clazz = LocalRegister.get(rpcRequest.getServiceName());
                Method method = clazz.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                method.setAccessible(true);
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object result = method.invoke(constructor.newInstance(), rpcRequest.getArgs());

                rpcResponse.setData(result);
                rpcResponse.setDataType(result.getClass());
                rpcResponse.setMessage("success");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }
            doResponse(request, rpcResponse, serializer);
        });

    }

    private void doResponse(HttpServerRequest request, RpcResponse rpcResponse, JdkSerializer serializer) {
        HttpServerResponse response = request.response();
        response.putHeader("content-type", "application/json");
        try {
            byte[] serialized = serializer.serialize(rpcResponse);
            response.end(Buffer.buffer(serialized));
        } catch (IOException e) {
            e.printStackTrace();
            response.end(Buffer.buffer());
        }
    }
}
