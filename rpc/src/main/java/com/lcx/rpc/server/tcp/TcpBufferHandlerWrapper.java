package com.lcx.rpc.server.tcp;

import com.lcx.rpc.protocol.ProtocolConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * tcp缓冲区处理包装器（装饰器设计模式）：解决粘包半包问题
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {

    // 记录解析器
    private RecordParser recordParser;

    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        init(bufferHandler);
    }

    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }

    /**
     * 初始化recordParser
     * @param handler buffer处理器
     */
    private void init(Handler<Buffer> handler) {
        recordParser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);
        recordParser.setOutput(new Handler<>() {
            private int size = -1;
            // 一次完整的读取（头 + 体）
            private Buffer buffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                if(size == -1) { // 读的消息头
                    // 读取消息体大小
                    recordParser.fixedSizeMode(buffer.getInt(ProtocolConstant.MESSAGE_LENGTH_OFFSET));
                    // 加入缓冲区
                    this.buffer.appendBuffer(buffer);
                }else{ // 读的消息体
                    this.buffer.appendBuffer(buffer);
                    // 处理一条完整的消息
                    handler.handle(this.buffer);

                    // 重置一轮
                    size = -1;
                    recordParser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                    this.buffer = Buffer.buffer();
                }
            }
        });
    }
}
