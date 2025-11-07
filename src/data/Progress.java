package data;

public final class Progress {

  private Progress() {
  }

  private static boolean skeletonLordDefeated;

  public static boolean isSkeletonLordDefeated() {
    return skeletonLordDefeated;
  }

  public static void setSkeletonLordDefeated(boolean defeated) {
    skeletonLordDefeated = defeated;
  }
}
