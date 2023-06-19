package org.example;

import java.io.*;

public class Poc {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String path = System.getProperty("user.dir")+ "\\1.txt";
        Person person = new Person("ceshi", 22);
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
        oos.writeObject(person);
        oos.close();
        FileInputStream fis = new FileInputStream(path);
        ObjectInputStream ois = new ObjectInputStream(fis);
        ois.readObject();
        ois.close();
    }

    public static class Person implements Serializable {
        private final String name;
        private final int age;
        private String a;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public void demo1() {
            int a = 8;
        }

        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            Runtime.getRuntime().exec("calc.exe");
        }
    }
}