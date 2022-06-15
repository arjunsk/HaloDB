package com.oath.halodb;

public class Driver1 {
  public static void main(String[] args) {
    System.out.println(roundUpToPowerOf2(7*2,1000));
  }

    static int roundUpToPowerOf2(int number, int max) {
        return number >= max
                ? max
                : (number > 1) ? Integer.highestOneBit((number - 1) << 1) : 1;
    }

}
