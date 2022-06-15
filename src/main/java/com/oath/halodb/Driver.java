package com.oath.halodb;

import com.oath.halodb.HaloDB;
import com.oath.halodb.HaloDBException;
import com.oath.halodb.HaloDBOptions;

import java.util.Arrays;
import java.util.Random;

public class Driver {

  private static final Random random = new Random();

  public static void main(String[] args) throws HaloDBException {
    HaloDB db = HaloDB.open("arjunsk", new HaloDBOptions());
    byte[] key = generateRandomByteArray();
    byte[] val = generateRandomByteArray();
    db.put(key, val);
    System.out.println(Arrays.toString(val));
    System.out.println(Arrays.toString(db.get(key)));

    db.close();

    System.out.println(Runtime.getRuntime().availableProcessors());
  }

  public static byte[] generateRandomByteArray() {
    int length = random.nextInt(Byte.MAX_VALUE) + 1;
    byte[] array = new byte[length];
    random.nextBytes(array);

    return array;
  }
}
