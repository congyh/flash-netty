package the.flash.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import the.flash.protocol.PacketCodeC;

public class Spliter extends LengthFieldBasedFrameDecoder {
    private static final int LENGTH_FIELD_OFFSET = 7;
    private static final int LENGTH_FIELD_LENGTH = 4;
    private int count = 0;

    public Spliter() {
        super(Integer.MAX_VALUE, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 注意: tcp传输的一个物理帧的的大小是512bytes, 如果发生了拆包,
        // 那么直接调用in.getInt(in.readerIndex())就很有可能出现数组越界的情况
        // 测试用例: 客户端发送"test1"
        // 服务端可以查到此输出: in.readerIndex(): 510, in.readableBytes(): 2
        if (in.readableBytes() < 4) {
            System.out.println("in.readerIndex(): " + in.readerIndex() +  ", in.readableBytes(): " + in.readableBytes());
        }
        if (in.readableBytes() >= 4 && in.getInt(in.readerIndex()) != PacketCodeC.MAGIC_NUMBER) {
            ctx.channel().close();
            return null;
        }

        return super.decode(ctx, in);
    }
}
