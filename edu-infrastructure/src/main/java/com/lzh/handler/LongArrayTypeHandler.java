package com.lzh.handler;

import com.lzh.vo.TimeBitmap;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.sql.*;

/**
 * 工业级转换器：实现 TimeBitmap 与 MySQL VARBINARY(128) 的互转
 * 优化点：
 * 1. 零拷贝思想：使用 ByteBuffer 视图直接转换，规避手动位移运算 。
 * 2. 精准匹配：MappedTypes 确保只处理 TimeBitmap，不干扰普通数组。
 */
@MappedTypes(TimeBitmap.class)
public class LongArrayTypeHandler extends BaseTypeHandler<TimeBitmap> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, TimeBitmap parameter, JdbcType jdbcType) throws SQLException {
        long[] bits = parameter.getBits();
        if (bits == null) {
            ps.setNull(i, Types.VARBINARY);
            return;
        }

        // 128字节 = 16个long * 8字节
        ByteBuffer buffer = ByteBuffer.allocate(bits.length * 8);
        for (long value : bits) {
            buffer.putLong(value);
        }
        ps.setBytes(i, buffer.array());
    }

    @Override
    public TimeBitmap getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return bytesToBitmap(rs.getBytes(columnName));
    }

    @Override
    public TimeBitmap getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return bytesToBitmap(rs.getBytes(columnIndex));
    }

    @Override
    public TimeBitmap getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return bytesToBitmap(cs.getBytes(columnIndex));
    }

    /**
     * 将数据库的二进制字节数组还原为领域层的 TimeBitmap 对象
     */
    private TimeBitmap bytesToBitmap(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return new TimeBitmap(new long[0]);
        }

        // 使用 NIO 视图极速还原，无需手动循环字节
        LongBuffer longBuffer = ByteBuffer.wrap(bytes).asLongBuffer();
        long[] result = new long[longBuffer.remaining()];
        longBuffer.get(result);

        return new TimeBitmap(result);
    }
}