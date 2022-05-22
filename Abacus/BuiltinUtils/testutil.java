package Abacus.BuiltinUtils;

import java.util.Scanner;

public class testutil {
    public static void main(String[] args) throws Exception {
        Matrix matrix1 = new Matrix();
        matrix1.input(new Scanner(System.in));
        System.out.println(matrix1.determinant());
    }
}
