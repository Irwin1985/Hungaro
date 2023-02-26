public class test {
    public static void main(String[] args) {
        String result = test();
        System.out.println(result);
    }

    public static String test() {
        try {
            int[] arr = {1, 2, 3};
            System.out.println(arr[10]);
            return "try";
        // } catch (Exception e) {
        //     return "catch";
        } finally {
            System.out.println("Finally");
        }
    }
}
