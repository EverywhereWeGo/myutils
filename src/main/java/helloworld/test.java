package helloworld;
public class test {
    public static int k = 0;
    public static test t1 = new test("t1");
    public static test t2 = new test("t2");
    public static int i = print("i");
    public static int n = 99;

    static {
        print("静态块");

        System.out.println(k);
        System.out.println(t1);
        System.out.println(t2);
        System.out.println(i);
        System.out.println(n);
        System.out.println("step2");
    }


    public int j = print("j");

    {
        System.out.println("step4");
        print("构造块");
    }

    public test(String str) {
        System.out.println((++j) + ":" + str + "   i=" + i + "    n=" +n);
        ++i;
        ++n;
        System.out.println();
    }

    public static int print(String str) {
        System.out.println((++k) + ":" + str + "   i=" + i + "    n=" + n);
        ++n;
        System.out.println();
        return ++i;
    }
    public static void main(String args[]) {
        test t = new test("init");
    }
}