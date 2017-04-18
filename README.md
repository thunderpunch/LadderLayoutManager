# LadderLayoutManager

A card stack effect LayoutManger for android.

演示

|vertical|horizontal|
|:---:|:---:|
|![vertical](/gif/vertical.gif)|![horizontal](/gif/horizontal.gif)|

## Usage

- create a new instance. then set it to RecyclerView directly

```java
LadderLayoutManager llm = new LadderLayoutManager(1.5f, 0.85f, LadderLayoutManager.HORIZONTAL).
RecyclerView rcv = (RecyclerView)findViewById(R.id.rcv);
rcv.setLayoutManager(llm);
```

参数说明

```java
/**
 * @param itemHeightWidthRatio childview的纵横比。所有childview都会按该纵横比展示
 * @param scale                chidview每一层级相对于上一层级的缩放量
 * @param orientation          布局方向 -纵向或者横向
 */
public LadderLayoutManager(float itemHeightWidthRatio, float scale, int orientation) {
    this.mItemHeightWidthRatio = itemHeightWidthRatio;
    this.mOrientation = orientation;
    this.mScale = scale;
    this.mChildSize = new int[2];
    this.mInterpolator = new DecelerateInterpolator();
}
```

## Customizations

- **设置childview的最大展示数量**   

  如果不设置，默认LadderLayoutManager将展示在空间允许范围的所有childview

  ```java
  public void setMaxItemLayoutCount(int count) {
      mMaxItemLayoutCount = Math.max(2, count);
      if (getChildCount() > 0) {
          requestLayout();
      }
  }
  ```

- **设置堆叠中的childview的展示高度**

  如果不设置，默认展示高度为  横向布局childview宽度x0.2 / 纵向布局childview高度x0.2 

  ```java
  public void setChildPeekSize(int childPeekSize) {
      mChildPeekSizeInput = childPeekSize;
      mCheckedChildSize = false;
      if (getChildCount() > 0) {
          requestLayout();
      }
  }
  ```

- **设置childview排列顺序**

  ```java
  /**
   * @param reverse 是否反向显示数据，默认false
   */
  public void setReverse(boolean reverse) {
      if (mReverse != reverse) {
          mReverse = reverse;
          if (getChildCount() > 0) {
              requestLayout();
          }
      }
  }
  ```

- **设置消失点的偏移量**

  演示参考 [horizontal](/gif/horizontal.gif)

  ```java
  /**
   * @param offset 消失点的偏移量 范围[-1,1]，默认0（居中）
   */
  public void setVanishOffset(float offset) {
      mVanishOffset = offset;
      if (getChildCount() > 0) {
          requestLayout();
      }
  }
  ```

- **设置childview布局过程中的回调**

  目前用于动态改变阴影、透明度

  ```java
  public LadderLayoutManager setChildDecorateHelper(ChildDecorateHelper layoutHelper) {
      mDecorateHelper = layoutHelper;
      return this;
  }
  ```

  ```java
  public interface ChildDecorateHelper {
      /**
       * @param child
       * @param posOffsetPercent childview相对于自身起始位置的偏移量百分比 范围[0，1)
       * @param layoutPercent    childview 在整个布局中的位置百分比
       * @param isBottom         childview 是否处于底部
       */
      void decorateChild(
      View child, float posOffsetPercent, float layoutPercent, boolean isBottom);
  }
  ```

  - 可以按照需求自定义，也可以直接使用位于LadderLayoutManager内的DefaultChildDecorateHelper 

  ```java
  public static class DefaultChildDecorateHelper implements ChildDecorateHelper {
      private float mElevation;

      public DefaultChildDecorateHelper(float maxElevation) {
          mElevation = maxElevation;
      }

      @Override
      public void decorateChild
      (View child, float posOffsetPercent, float layoutPercent, boolean isBottom) {
          ViewCompat.setElevation(child, (float) (layoutPercent * mElevation * 0.7 + 				mElevation * 0.3));
      }
  }
  ```

## Extras

- 配合LadderSimpleSnapHelper (继承自SnapHelper)使用, 使RecyclerView在滑动结束时定位到某个位置

  ```java
  LadderLayoutManager llm = new LadderLayoutManager(1.5f, 0.85f, LadderLayoutManager.HORIZONTAL).
  rcv = (RecyclerView) findViewById(R.id.rcv);
  rcv.setLayoutManager(llm);

  new LadderSimpleSnapHelper().attachToRecyclerView(rcv);
  ```



项目实现过程中对于LayoutManger需要重写的方法参考了 [CarouselLayoutManager](https://github.com/Azoft/CarouselLayoutManager)



## License

```
Copyright 2017 thunderpunch

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
