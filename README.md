# Accordion Swipe Layout

Easy accordion swipe layout for Android. 
<img src="http://i.giphy.com/3o6ZsX9bv3ou6IXdcY.gif" width="300">

Very easy to use

## Step 1
### Gradle

Add to root project gradle
```groovy
allprojects {
    repositories {
        maven {
            url "https://jitpack.io"
        }
    }
}
```

Add dependency to app gradle
```groovy
compile 'com.github.alexandrius:accordion-swipe-layout:0.1.2'
```

## Step 2
Create main layout for your swipable item. 

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="#a9a9a9"
        android:gravity="center"
        android:text="This is sample" />
</LinearLayout>
```

## Step 3
Create array.xml in your values folder
Add custom integer arrays for drawables and swipable item backgrounds

### Example:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <integer-array name="rightColors">
        <item>@color/color1</item>
        <item>@color/color2</item>
        <item>@color/color3</item>
    </integer-array>

    <integer-array name="leftColors">
        <item>@color/color4</item>
        <item>@color/color5</item>
        <item>@color/color6</item>
    </integer-array>

    <integer-array name="rightDrawables">
        <item>@mipmap/ic_reload</item>
        <item>@mipmap/ic_settings</item>
        <item>@mipmap/ic_trash</item>
    </integer-array>

    <integer-array name="leftDrawables">
        <item>@mipmap/ic_reload</item>
    </integer-array>

    <string-array name="rightTexts">
        <item>@string/reload</item>
        <item>@string/settings</item>
        <item>@string/trash</item>
    </string-array>

</resources>
```

## Step 4
Add SwipeLayout into your layout

```xml
<com.alexandrius.accordionswipelayout.library.SwipeLayout
    android:id="@+id/swipe_layout"
    android:layout_width="match_parent"
    android:layout_height="80dp"
    app:iconSize="@dimen/icon_size"
    app:layout="@layout/sample_item"
    app:leftItemColors="@array/leftColors"
    app:leftItemIcons="@array/leftDrawables"
    app:rightItemColors="@array/rightColors"
    app:rightItemIcons="@array/rightDrawables"
    app:swipeItemWidth="@dimen/swipe_item_width" />
```

### Available attrs:
  1. iconSize
  2. layout - pass id of previously created layout
  3. leftItemColors
  4. leftItemIcons
  5. leftTextColors
  6. rightTextColors
  7. rightItemColors
  8. rightItemIcons
  9. swipeItemWidth
  10. leftStrings
  11. rightStrings
  12. textSize
  13. textTopMargin
  14. customFont


## Step 5
Add click listener to swipe items
```java
SwipeLayout swipeLayout = (SwipeLayout) findViewById(R.id.swipe_layout);
swipeLayout.setOnSwipeItemClickListener(new SwipeLayout.OnSwipeItemClickListener() {
    @Override
    public void onSwipeItemClick(boolean left, int index) {
        if (left) {
            switch (index) {
                case 0:
                    break;
            }
        } else {
            switch (index) {
                case 0:
                    break;
            }
        }
    }
});
```


### Expand and collapse programmatically
```java
ITEM_STATE_LEFT_EXPAND
ITEM_STATE_RIGHT_EXPAND
ITEM_STATE_COLLAPSED

swipeLayout.setItemState(SwipeLayout.ITEM_STATE_LEFT_EXPAND, animated);

```

That's pretty much it. Thanks
