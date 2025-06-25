package org.example.cmmaiagent2.serializer;

import com.alibaba.dashscope.common.Message;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Component
public class MessageSerializer {

    // ⚠️ 静态 Kryo 实例（线程不安全，建议改用局部实例）
    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        // 设置实例化策略（需确保兼容所有 Message 实现类）
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    /**
     * 使用 Kryo 将 Message 序列化为 Base64 字符串
     */
    public static String serialize(Message message) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Output output = new Output(baos)) {
            kryo.writeClassAndObject(output, message);  // ⚠️ 依赖动态注册和实例化策略
            output.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("序列化失败", e);
        }
    }

    /**
     * 使用 Kryo 将 Base64 字符串反序列化为 Message 对象
     */
    public static Message deserialize(String base64) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             Input input = new Input(bais)) {
            return (Message) kryo.readClassAndObject(input);  // ⚠️ 依赖动态注册和实例化策略
        } catch (IOException e) {
            throw new RuntimeException("反序列化失败", e);
        }
    }
}

