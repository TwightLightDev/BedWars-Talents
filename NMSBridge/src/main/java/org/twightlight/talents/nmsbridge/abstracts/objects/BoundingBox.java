package org.twightlight.talents.nmsbridge.abstracts.objects;

public class BoundingBox {
   private double minX;
   private double minY;
   private double minZ;
   private double maxX;
   private double maxY;
   private double maxZ;

   public BoundingBox(double a, double b, double c, double d, double e, double f) {
      this.minX = a;
      this.minY = b;
      this.minZ = c;
      this.maxX = d;
      this.maxY = e;
      this.maxZ = f;
   }

   public double getMaxX() {
      return this.maxX;
   }

   public double getMaxY() {
      return this.maxY;
   }

   public double getMaxZ() {
      return this.maxZ;
   }

   public double getMinX() {
      return this.minX;
   }

   public double getMinY() {
      return this.minY;
   }

   public double getMinZ() {
      return this.minZ;
   }

   public double getHeight() {
      return this.maxY - this.minY;
   }

   public double getWidthX() {
      return this.maxX - this.minX;
   }

   public double getWidthZ() {
      return this.maxZ - this.minZ;
   }
}
